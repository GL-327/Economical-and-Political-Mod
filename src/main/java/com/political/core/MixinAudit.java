package com.political.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dev and CI check: loads every class that a mixin targets, so a broken injection fails here with a
 * clear message instead of whenever that class happens to be loaded in play. Mixin applies lazily, so a
 * plain boot only exercises the targets that load at startup.
 *
 * Opt in with {@code -Dpoliticalserver.mixinAudit=true}. Without the property this does nothing.
 */
public final class MixinAudit {

    public static final String PROPERTY = "politicalserver.mixinAudit";
    private static final Logger LOGGER = LoggerFactory.getLogger(MixinAudit.class);

    private MixinAudit() {
    }

    public static void runIfRequested() {
        if (!Boolean.getBoolean(PROPERTY)) {
            return;
        }
        List<String> failures = run(MixinAudit.class.getClassLoader());
        if (failures.isEmpty()) {
            LOGGER.info("Mixin audit passed");
        } else {
            LOGGER.error("Mixin audit found {} failing target(s)", failures.size());
        }
    }

    /** Loads every mixin target, returns one line per target that failed to load or transform. */
    public static List<String> run(ClassLoader loader) {
        Set<String> targets = collectTargets(loader);
        List<String> failures = new ArrayList<>();
        for (String target : targets) {
            try {
                Class.forName(target, false, loader);
            } catch (Throwable t) {
                Throwable root = t;
                while (root.getCause() != null && root.getCause() != root) {
                    root = root.getCause();
                }
                String line = target + ": " + root;
                failures.add(line);
                LOGGER.error("Mixin audit failure on {}", line);
            }
        }
        LOGGER.info("Mixin audit loaded {} target classes", targets.size());
        return failures;
    }

    /**
     * Reads the target classes straight from the mixin classes' {@code @Mixin} annotations. The config
     * objects Mixin hands out are not filled in until their targets load, so they cannot be asked. The
     * config names come from this mod's own fabric.mod.json.
     */
    static Set<String> collectTargets(ClassLoader loader) {
        Set<String> targets = new TreeSet<>();
        // Not loader.getResourceAsStream: on the shared classpath that finds another mod's fabric.mod.json first.
        Path metadataPath = FabricLoader.getInstance().getModContainer("politicalserver")
                .flatMap(mod -> mod.findPath("fabric.mod.json")).orElse(null);
        if (metadataPath == null) {
            LOGGER.error("Mixin audit: fabric.mod.json of politicalserver not found");
            return targets;
        }
        try (InputStream in = Files.newInputStream(metadataPath)) {
            JsonObject metadata = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            for (JsonElement configName : metadata.getAsJsonArray("mixins")) {
                collectFromConfig(loader, configName.getAsString(), targets);
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Mixin audit could not read fabric.mod.json", e);
        }
        return targets;
    }

    private static void collectFromConfig(ClassLoader loader, String configName, Set<String> targets) {
        try (InputStream in = loader.getResourceAsStream(configName)) {
            if (in == null) {
                LOGGER.error("Mixin audit: config {} not found", configName);
                return;
            }
            JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            String pkg = json.has("package") ? json.get("package").getAsString() : "";
            for (String list : new String[] {"mixins", "server"}) {
                if (!json.has(list)) {
                    continue;
                }
                JsonArray names = json.getAsJsonArray(list);
                for (JsonElement name : names) {
                    readMixinTargets(loader, pkg + "." + name.getAsString(), targets);
                }
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Mixin audit could not read config {}", configName, e);
        }
    }

    private static void readMixinTargets(ClassLoader loader, String mixinClass, Set<String> out) throws IOException {
        try (InputStream in = loader.getResourceAsStream(mixinClass.replace('.', '/') + ".class")) {
            if (in == null) {
                LOGGER.error("Mixin audit: mixin class {} is listed but not found", mixinClass);
                return;
            }
            new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (!"Lorg/spongepowered/asm/mixin/Mixin;".equals(descriptor)) {
                        return null;
                    }
                    return new AnnotationVisitor(Opcodes.ASM9) {
                        @Override
                        public AnnotationVisitor visitArray(String name) {
                            return new AnnotationVisitor(Opcodes.ASM9) {
                                @Override
                                public void visit(String ignored, Object value) {
                                    if (value instanceof Type type) {
                                        out.add(type.getClassName());
                                    } else if (value instanceof String str) {
                                        out.add(str.replace('/', '.'));
                                    }
                                }
                            };
                        }
                    };
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
    }
}
