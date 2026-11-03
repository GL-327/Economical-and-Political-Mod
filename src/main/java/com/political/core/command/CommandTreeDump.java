package com.political.core.command;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * A text picture of a command tree, one line per node: its name, whether it is a literal or an argument
 * (and which type), whether it can be executed, and whether it has a requirement. Two dumps being equal
 * means two command setups accept the same input shapes, which is how M1.8 shows that merging duplicate
 * definitions did not change what a command accepts.
 *
 * <p>Dev tool: {@code -Dpoliticalserver.commandDump=<file>} writes the dump of the real tree after
 * registration.</p>
 */
public final class CommandTreeDump {

    public static final String PROPERTY = "politicalserver.commandDump";

    private CommandTreeDump() {
    }

    public static <S> String render(RootCommandNode<S> root, Predicate<String> includeRoot) {
        List<String> lines = new ArrayList<>();
        List<CommandNode<S>> children = new ArrayList<>(root.getChildren());
        children.sort(Comparator.comparing(CommandNode::getName));
        for (CommandNode<S> child : children) {
            if (includeRoot.test(child.getName())) {
                walk(child, 0, lines);
            }
        }
        return String.join("\n", lines) + "\n";
    }

    private static <S> void walk(CommandNode<S> node, int depth, List<String> lines) {
        StringBuilder line = new StringBuilder("  ".repeat(depth));
        if (node instanceof ArgumentCommandNode<S, ?> arg) {
            line.append('<').append(arg.getName()).append(": ").append(arg.getType().getClass().getSimpleName()).append('>');
        } else {
            line.append(node.getName());
        }
        if (node.getCommand() != null) {
            line.append(" [exec]");
        }
        if (node.getRequirement() != null && node.getRedirect() == null && !isTrivial(node)) {
            line.append(" [req]");
        }
        if (node.getRedirect() != null) {
            line.append(" -> ").append(node.getRedirect().getName());
        }
        lines.add(line.toString());
        List<CommandNode<S>> children = new ArrayList<>(node.getChildren());
        children.sort(Comparator.comparing(CommandNode::getName));
        for (CommandNode<S> child : children) {
            walk(child, depth + 1, lines);
        }
    }

    /** Brigadier gives every node a requirement that is always true; the default one carries no information. */
    private static <S> boolean isTrivial(CommandNode<S> node) {
        return node.getRequirement().getClass().getName().startsWith("com.mojang.brigadier.builder.ArgumentBuilder");
    }

    /** Writes the dump to the file named by {@link #PROPERTY}, if it is set. */
    public static <S> void writeIfRequested(RootCommandNode<S> root, Predicate<String> includeRoot) {
        String target = System.getProperty(PROPERTY);
        if (target == null || target.isBlank()) {
            return;
        }
        try {
            Path path = Path.of(target);
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, render(root, includeRoot), StandardCharsets.UTF_8);
        } catch (IOException e) {
            org.slf4j.LoggerFactory.getLogger(CommandTreeDump.class).error("Could not write the command dump to {}", target, e);
        }
    }
}
