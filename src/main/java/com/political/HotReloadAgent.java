package com.political;

import java.lang.instrument.Instrumentation;

/**
 * Holds the JVM instrumentation handle that makes live code reloading possible.
 *
 * <p>Redefining a class that is already loaded is not something a program can simply decide to do.
 * It needs an {@link Instrumentation}, and the only ways to get one are to be launched as a java
 * agent or to attach to your own process at runtime. This class is the landing point for both.</p>
 *
 * <p>There are three ways this ends up populated, in descending order of how much the server owner
 * had to know in advance:</p>
 *
 * <ol>
 *   <li><b>premain</b>, when the jar was passed with {@code -javaagent}. Nothing else to do.</li>
 *   <li><b>agentmain</b>, when the mod attached to its own process at startup. This needs
 *       {@code -Djdk.attach.allowAttachSelf=true} on the command line, which is one flag.</li>
 *   <li><b>Nothing</b>, in which case data reloading still works completely and only class
 *       redefinition is unavailable. The menu says so rather than failing quietly.</li>
 * </ol>
 *
 * <p>Worth being honest about the ceiling. Even with instrumentation, the JVM will only redefine
 * <em>method bodies</em>. Adding a method, removing a field or changing a class hierarchy needs a
 * restart, and no amount of cleverness here changes that. What it does cover is the common case:
 * tweaking a number, fixing a condition, changing what a handler does.</p>
 */
public final class HotReloadAgent {

    private HotReloadAgent() {}

    /**
     * Where the handle is parked, and why it is parked somewhere so odd.
     *
     * <p>An agent is loaded by the system class loader. Fabric loads mod classes in its own loader.
     * That means this class exists twice at runtime, and a plain static field would put the handle
     * in the copy the mod never reads, so the feature would report itself unavailable forever while
     * having worked perfectly.</p>
     *
     * <p>The system properties table is a {@code Hashtable<Object, Object>} that every loader can
     * see, so it is the one place both copies agree on. It looks like a hack because it is one, but
     * it is the standard hack, and the alternative is a feature that silently never works.</p>
     */
    private static final String HANDLE_KEY = "com.political.hotreload.instrumentation";
    private static final String SOURCE_KEY = "com.political.hotreload.source";

    /** Entry point when the jar is passed with -javaagent. */
    public static void premain(String args, Instrumentation inst) {
        park(inst, "javaagent");
    }

    /** Entry point when the jar is attached to a running JVM, including our own. */
    public static void agentmain(String args, Instrumentation inst) {
        park(inst, "self attached");
    }

    private static void park(Instrumentation inst, String how) {
        System.getProperties().put(HANDLE_KEY, inst);
        System.getProperties().put(SOURCE_KEY, how);
    }

    public static Instrumentation get() {
        Object o = System.getProperties().get(HANDLE_KEY);
        return o instanceof Instrumentation inst ? inst : null;
    }

    private static String source() {
        Object o = System.getProperties().get(SOURCE_KEY);
        return o instanceof String s ? s : "not attached";
    }

    public static boolean available() {
        Instrumentation inst = get();
        return inst != null && inst.isRedefineClassesSupported();
    }

    public static String describe() {
        Instrumentation inst = get();
        if (inst == null) return "not attached";
        return source() + (inst.isRedefineClassesSupported()
                ? ", redefinition supported" : ", redefinition NOT supported");
    }

    /**
     * Tries to attach to our own process so class reloading becomes available.
     *
     * <p>Called once at startup. Everything here is reflective because the attach API lives in the
     * {@code jdk.attach} module, which is present on a JDK and absent on a plain JRE, and a mod that
     * refused to load on a JRE because of an optional feature would be a bad trade.</p>
     *
     * @return null on success, or a sentence explaining what to do about it
     */
    public static String trySelfAttach() {
        if (available()) return null;

        if (!"true".equals(System.getProperty("jdk.attach.allowAttachSelf"))) {
            return "add -Djdk.attach.allowAttachSelf=true to the server start command";
        }

        try {
            Class<?> vmClass = Class.forName("com.sun.tools.attach.VirtualMachine");
            String pid = String.valueOf(ProcessHandle.current().pid());
            Object vm = vmClass.getMethod("attach", String.class).invoke(null, pid);
            try {
                String jar = ownJarPath();
                if (jar == null) return "could not find the mod jar to attach";
                vmClass.getMethod("loadAgent", String.class).invoke(vm, jar);
            } finally {
                vmClass.getMethod("detach").invoke(vm);
            }
            return available() ? null : "attached, but this JVM will not redefine classes";
        } catch (ClassNotFoundException e) {
            return "this is a JRE, not a JDK, so the attach API is missing";
        } catch (Exception e) {
            return "attach failed: " + e.getClass().getSimpleName() + " " + e.getMessage();
        }
    }

    /**
     * Where our own jar lives on disk, which is what the attach API wants.
     *
     * <p>Asks Fabric first, because Fabric is the thing that actually loaded us and therefore knows.
     * The protection domain is checked second, and a dev build is looked up last: none of the three
     * is reliable on its own, and the failure mode of getting this wrong is a feature that reports
     * itself unavailable while being perfectly capable.</p>
     */
    private static String ownJarPath() {
        // 1. Fabric knows exactly where it loaded the mod from. This is the production answer.
        try {
            var container = net.fabricmc.loader.api.FabricLoader.getInstance()
                    .getModContainer("politicalserver");
            if (container.isPresent()) {
                for (java.nio.file.Path path : container.get().getOrigin().getPaths()) {
                    java.io.File f = path.toFile();
                    if (f.isFile() && f.getName().endsWith(".jar")) {
                        return f.getAbsolutePath();
                    }
                }
            }
        } catch (Exception ignored) {
            // Older loader, or a mod container shape we do not recognise. Fall through.
        }

        // 2. Where this class was actually loaded from, which is a jar outside a dev environment.
        try {
            java.net.URL src = HotReloadAgent.class.getProtectionDomain()
                    .getCodeSource().getLocation();
            java.io.File f = new java.io.File(src.toURI());
            if (f.isFile()) return f.getAbsolutePath();
        } catch (Exception ignored) {
            // No code source. Fall through.
        }

        // 3. A dev run, where classes come from a directory and the only artefact carrying the
        // agent manifest is the built jar. The game directory is the run folder, so the project
        // root is its parent.
        try {
            java.nio.file.Path gameDir = net.fabricmc.loader.api.FabricLoader.getInstance()
                    .getGameDir();
            for (java.nio.file.Path candidate : new java.nio.file.Path[]{
                    gameDir.resolve("build").resolve("libs"),
                    gameDir.getParent() == null ? gameDir
                            : gameDir.getParent().resolve("build").resolve("libs")}) {
                java.io.File dir = candidate.toFile();
                if (!dir.isDirectory()) continue;
                java.io.File[] jars = dir.listFiles((d, n) ->
                        n.endsWith(".jar") && !n.contains("sources"));
                if (jars != null && jars.length > 0) return jars[0].getAbsolutePath();
            }
        } catch (Exception ignored) {
            // Nothing left to try.
        }
        return null;
    }
}
