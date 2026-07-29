package com.political;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Gives custom items real models on vanilla clients.
 *
 * <p>Everything in this mod is a dressed vanilla stack rather than a registered item, which is what
 * lets it run server side with no client mod, and also why every one of the seven thousand odd
 * custom items has always looked exactly like whatever it was built on. A netherite sword named
 * Voidwyrm Fang still renders as a netherite sword.</p>
 *
 * <p>The <b>item_model</b> component fixes that without registering anything: point a stack at a
 * model id and the client draws that model instead. Polymer supplies the other half, generating and
 * serving the resource pack that defines those models, so players receive them automatically.</p>
 *
 * <h2>Why this is off until an item has art</h2>
 *
 * <p>A stack pointed at a model no pack defines does not fall back gracefully, it renders as the
 * missing texture. Blanket applying this to every custom item would therefore turn the entire mod
 * purple and black rather than making it look better. So the bridge only ever touches an item whose
 * model it can positively confirm exists, which means coverage grows on its own as art is added and
 * an item with no art keeps looking exactly as it does today.</p>
 */
public final class PolymerBridge {

    private PolymerBridge() {}

    public static final String NAMESPACE = "politicalserver";

    /** Model ids we can prove exist, because they shipped in the jar or the pack folder. */
    private static final Set<String> KNOWN_MODELS = Collections.synchronizedSet(new HashSet<>());
    /** Every custom id seen, so the manifest can list what still needs art. */
    private static final Set<String> SEEN_IDS = Collections.synchronizedSet(new TreeSet<>());

    private static boolean polymerPresent;
    private static boolean scanned;

    /** Master switch. Even when on, only items with a confirmed model are touched. */
    public static boolean enabled = true;

    // ── Startup ───────────────────────────────────────────────────────────

    public static void initialize() {
        polymerPresent = FabricLoader.getInstance().isModLoaded("polymer-core")
                || FabricLoader.getInstance().isModLoaded("polymer-resource-pack");
        scanModels();
        System.out.println("[Polymer] bridge ready. polymer=" + polymerPresent
                + " models=" + KNOWN_MODELS.size());
    }

    /**
     * Finds which item models actually exist.
     *
     * <p>Two sources: assets bundled in the jar, and a drop in folder on the server so an operator
     * can add art without rebuilding. Anything found here becomes eligible to be applied.</p>
     */
    private static void scanModels() {
        KNOWN_MODELS.clear();

        // Bundled assets, listed by an index file so this does not have to walk the jar.
        try (InputStream in = PolymerBridge.class.getResourceAsStream(
                "/assets/" + NAMESPACE + "/item_models.index")) {
            if (in != null) {
                String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                for (String line : body.split("\\R")) {
                    String id = line.trim();
                    if (!id.isEmpty() && !id.startsWith("#")) KNOWN_MODELS.add(id);
                }
            }
        } catch (Exception e) {
            System.out.println("[Polymer] could not read the bundled model index: " + e.getMessage());
        }

        // Operator drop in folder: config/political/item_models/<id>.json
        try {
            Path dir = FabricLoader.getInstance().getGameDir()
                    .resolve("config").resolve("political").resolve("item_models");
            if (Files.isDirectory(dir)) {
                try (var stream = Files.list(dir)) {
                    stream.filter(p -> p.getFileName().toString().endsWith(".json"))
                            .forEach(p -> {
                                String name = p.getFileName().toString();
                                KNOWN_MODELS.add(name.substring(0, name.length() - 5));
                            });
                }
            }
        } catch (Exception e) {
            System.out.println("[Polymer] could not read the model folder: " + e.getMessage());
        }
        scanned = true;
    }

    // ── The hook ──────────────────────────────────────────────────────────

    /**
     * Called for every custom item as it is stamped with its id. Applies a model only when one is
     * known to exist, so this is safe to leave on with no art at all.
     */
    public static void applyModel(ItemStack stack, String customItemId) {
        if (stack == null || stack.isEmpty() || customItemId == null || customItemId.isEmpty()) return;
        SEEN_IDS.add(customItemId);
        if (!enabled) return;
        if (!scanned) return;
        if (!KNOWN_MODELS.contains(customItemId)) return;
        try {
            stack.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(NAMESPACE, customItemId));
        } catch (Exception e) {
            // A bad id is not worth failing item creation over.
        }
    }

    // ── Reporting ─────────────────────────────────────────────────────────

    public static boolean polymerLoaded() {
        return polymerPresent;
    }

    public static int knownModels() {
        return KNOWN_MODELS.size();
    }

    public static int seenItems() {
        return SEEN_IDS.size();
    }

    public static boolean hasModel(String id) {
        return KNOWN_MODELS.contains(id);
    }

    /** Ids that have been created but have no art yet, alphabetically. */
    public static java.util.List<String> missing(int limit) {
        java.util.List<String> out = new java.util.ArrayList<>();
        synchronized (SEEN_IDS) {
            for (String id : SEEN_IDS) {
                if (out.size() >= limit) break;
                if (!KNOWN_MODELS.contains(id)) out.add(id);
            }
        }
        return out;
    }

    /**
     * Writes a manifest of every custom item, whether it has art and where that art would go.
     *
     * <p>This is the thing that makes the whole feature usable. Seven thousand items is far too many
     * to work through by guesswork, so the manifest is the worklist: drop a matching json into the
     * folder and that item starts rendering with it on the next reload.</p>
     */
    public static int writeManifest() {
        try {
            Path dir = FabricLoader.getInstance().getGameDir()
                    .resolve("config").resolve("political");
            Files.createDirectories(dir);
            Path out = dir.resolve("item_model_manifest.txt");

            StringBuilder sb = new StringBuilder();
            sb.append("# CivilCraft item model manifest\n");
            sb.append("# Drop a model json at config/political/item_models/<id>.json\n");
            sb.append("# and that item renders with it after a reload.\n");
            sb.append("#\n");
            sb.append("# have  id\n");
            int have = 0;
            synchronized (SEEN_IDS) {
                for (String id : SEEN_IDS) {
                    boolean ok = KNOWN_MODELS.contains(id);
                    if (ok) have++;
                    sb.append(ok ? "  yes  " : "  no   ").append(id).append('\n');
                }
            }
            sb.append("#\n# ").append(have).append(" of ").append(SEEN_IDS.size())
                    .append(" have art\n");
            Files.writeString(out, sb.toString(), StandardCharsets.UTF_8);
            return SEEN_IDS.size();
        } catch (Exception e) {
            System.out.println("[Polymer] could not write the manifest: " + e.getMessage());
            return 0;
        }
    }

    /** Rereads the drop in folder, so art can be added without a restart. */
    public static void reload() {
        scanModels();
    }
}
