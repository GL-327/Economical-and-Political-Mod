package com.political;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reloading the mod without stopping the server.
 *
 * <p>Two halves, and they are worth separating because one of them always works and the other
 * depends on how the server was started.</p>
 *
 * <p><b>Data</b> is everything living in a file: recipes, item edits, bazaar state, the product
 * catalogue. All of it can be dropped and read again on a running server with nothing special
 * enabled, which covers almost everything a server owner actually changes day to day. Retuning an
 * item, adding a recipe, fixing a price: none of that is code.</p>
 *
 * <p><b>Code</b> is the harder half. Classes already loaded into a running JVM can be replaced, but
 * only their method bodies, and only with an {@link Instrumentation} handle. Adding a method or a
 * field changes the shape of the class and the JVM will refuse, which is a hard limit of the
 * platform rather than something this class could be cleverer about. What it covers is the common
 * case: change what a method does, drop the new class file in, press the button.</p>
 */
public final class HotReloadManager {

    private HotReloadManager() {}

    /** Where an owner drops recompiled class files. */
    public static Path swapDir() {
        return FabricLoader.getInstance().getGameDir().resolve("hotswap");
    }

    /** The outcome of a reload, so the menu can say exactly what happened. */
    public record Result(boolean ok, List<String> lines) {
        public static Result of(boolean ok, String... lines) {
            return new Result(ok, new ArrayList<>(List.of(lines)));
        }
    }

    // ── Data ──────────────────────────────────────────────────────────────

    /**
     * Re-reads everything that lives in a file.
     *
     * <p>Order matters. Recipes come first because the known item list is harvested from them, and
     * the bazaar catalogue is rebuilt last because it reads prices that the earlier steps may have
     * changed.</p>
     */
    public static Result reloadData() {
        List<String> out = new ArrayList<>();
        boolean ok = true;
        long started = System.nanoTime();

        try {
            RecipeConfigManager.load();
            out.add("Recipes: " + SlayerRecipes.RECIPES.size() + " loaded");
        } catch (Exception e) {
            ok = false;
            out.add("Recipes failed: " + e.getMessage());
        }

        try {
            ItemOverrideManager.load();
            ItemOverrideManager.invalidateKnown();
            out.add("Item edits: " + ItemOverrideManager.count() + " loaded");
        } catch (Exception e) {
            ok = false;
            out.add("Item edits failed: " + e.getMessage());
        }

        try {
            BazaarProduct.rebuild();
            out.add("Bazaar: " + BazaarProduct.count() + " products");
        } catch (Exception e) {
            ok = false;
            out.add("Bazaar failed: " + e.getMessage());
        }

        // Every online player has their stats recomputed, so a changed item takes effect on the
        // people wearing it rather than only on the next person to log in.
        try {
            if (PoliticalServer.server != null) {
                for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                    PlayerStatsManager.invalidate(p);
                }
                out.add("Stats: recomputed for everyone online");
            }
        } catch (Exception e) {
            out.add("Stat refresh skipped: " + e.getMessage());
        }

        long ms = (System.nanoTime() - started) / 1_000_000L;
        out.add("Took " + ms + " ms");
        return new Result(ok, out);
    }

    // ── Code ──────────────────────────────────────────────────────────────

    /** How many class files are waiting in the swap directory. */
    public static int pendingClasses() {
        try {
            Path dir = swapDir();
            if (!Files.isDirectory(dir)) return 0;
            try (var walk = Files.walk(dir)) {
                return (int) walk.filter(f -> f.toString().endsWith(".class")).count();
            }
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Replaces loaded classes with recompiled ones from the swap directory.
     *
     * <p>A class file carries the name of the class it defines, so the file can sit anywhere under
     * the directory and still find its target. That matters because the obvious thing to do is copy
     * a whole build output folder in, and requiring a particular layout would make that fail for a
     * reason nobody would guess.</p>
     */
    public static Result reloadClasses() {
        if (!HotReloadAgent.available()) {
            return Result.of(false,
                    "Class reloading is not available on this server.",
                    HotReloadAgent.describe(),
                    "",
                    "Add this to the start command and restart once:",
                    "-Djdk.attach.allowAttachSelf=true",
                    "",
                    "After that, code can be swapped without stopping again.");
        }

        Path dir = swapDir();
        if (!Files.isDirectory(dir)) {
            return Result.of(false, "No hotswap folder yet.",
                    "It was created for you at " + dir,
                    "Drop recompiled .class files in and try again.");
        }

        List<ClassDefinition> defs = new ArrayList<>();
        List<String> out = new ArrayList<>();
        List<Path> files = new ArrayList<>();

        try (var walk = Files.walk(dir)) {
            walk.filter(f -> f.toString().endsWith(".class")).forEach(files::add);
        } catch (Exception e) {
            return Result.of(false, "Could not read the hotswap folder: " + e.getMessage());
        }

        if (files.isEmpty()) {
            return Result.of(false, "Nothing to swap.",
                    "Put recompiled .class files in " + dir);
        }

        for (Path f : files) {
            try {
                byte[] bytes = Files.readAllBytes(f);
                String className = classNameOf(bytes);
                if (className == null) {
                    out.add("Skipped " + f.getFileName() + ": not a class file");
                    continue;
                }
                Class<?> target = Class.forName(className, false,
                        HotReloadManager.class.getClassLoader());
                defs.add(new ClassDefinition(target, bytes));
                out.add("Queued " + className);
            } catch (ClassNotFoundException e) {
                out.add("Skipped " + f.getFileName() + ": that class is not loaded");
            } catch (Exception e) {
                out.add("Skipped " + f.getFileName() + ": " + e.getMessage());
            }
        }

        if (defs.isEmpty()) {
            return new Result(false, out);
        }

        Instrumentation inst = HotReloadAgent.get();
        try {
            inst.redefineClasses(defs.toArray(new ClassDefinition[0]));
            out.add("");
            out.add("Redefined " + defs.size() + " classes. They are live now.");
            return new Result(true, out);
        } catch (UnsupportedOperationException e) {
            out.add("");
            out.add("The JVM refused: " + e.getMessage());
            out.add("A change that adds or removes a method or field cannot");
            out.add("be swapped into a running JVM. That one needs a restart.");
            return new Result(false, out);
        } catch (Exception e) {
            out.add("");
            out.add("Failed: " + e.getClass().getSimpleName() + " " + e.getMessage());
            return new Result(false, out);
        }
    }

    /**
     * Reads the class name straight out of the constant pool.
     *
     * <p>Walking the pool by hand rather than pulling in a bytecode library, because this needs
     * exactly one field out of the header and a dependency for that would be silly. The layout is
     * fixed by the class file format and has not changed in the parts that matter.</p>
     */
    private static String classNameOf(byte[] b) {
        try {
            java.io.DataInputStream in = new java.io.DataInputStream(
                    new java.io.ByteArrayInputStream(b));
            if (in.readInt() != 0xCAFEBABE) return null;
            in.readUnsignedShort();  // minor
            in.readUnsignedShort();  // major
            int poolCount = in.readUnsignedShort();

            String[] strings = new String[poolCount];
            int[] classNameIndex = new int[poolCount];

            for (int i = 1; i < poolCount; i++) {
                int tag = in.readUnsignedByte();
                switch (tag) {
                    case 1 -> strings[i] = in.readUTF();
                    case 7 -> classNameIndex[i] = in.readUnsignedShort();
                    case 8, 16, 19, 20 -> in.readUnsignedShort();
                    case 15 -> in.skipBytes(3);
                    case 3, 4, 9, 10, 11, 12, 17, 18 -> in.skipBytes(4);
                    case 5, 6 -> {
                        in.skipBytes(8);
                        // Longs and doubles take two pool slots, which is the one quirk in here.
                        i++;
                    }
                    default -> {
                        return null;
                    }
                }
            }

            in.readUnsignedShort();  // access flags
            int thisClass = in.readUnsignedShort();
            int nameIdx = classNameIndex[thisClass];
            String internal = strings[nameIdx];
            return internal == null ? null : internal.replace('/', '.');
        } catch (Exception e) {
            return null;
        }
    }

    // ── Startup ───────────────────────────────────────────────────────────

    /** Sets up the swap folder and tries to make class reloading available. */
    public static void initialize() {
        try {
            Files.createDirectories(swapDir());
        } catch (Exception ignored) {
            // A folder we could not make is reported later, when somebody tries to use it.
        }
        String why = HotReloadAgent.trySelfAttach();
        if (why == null) {
            PoliticalServer.LOGGER.info("Live reload: code and data, " + HotReloadAgent.describe());
        } else {
            PoliticalServer.LOGGER.info("Live reload: data only (" + why + ")");
        }
    }

    /** Tells everybody online that something changed under them. */
    public static void announce(ServerPlayer by, String what) {
        if (PoliticalServer.server == null) return;
        for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal("§b⟳ §7" + what
                    + " §8(" + by.getName().getString() + ")"));
        }
    }
}
