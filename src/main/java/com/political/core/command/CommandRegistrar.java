package com.political.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The one place the mod's commands are registered (M1.8). It hands each command module a dispatcher that
 * records which module registered which root literal, so a name registered twice, or one that would
 * quietly merge into a vanilla or another mod's command, is caught instead of being merged by Brigadier.
 *
 * <p>Brigadier merges two registrations of the same root: the sub-commands of both are kept, the later
 * {@code executes} replaces the earlier, and the earlier {@code requires} stays. That is rarely what
 * anyone meant, and it made the real behavior of a command depend on registration order.</p>
 *
 * <p>In a development environment, or with {@code -Dpoliticalserver.commandsStrict=true}, {@link #finish()}
 * throws when any problem was found so it is fixed before it ships. Otherwise each problem is logged as an
 * error and Brigadier's merge applies, as it always did.</p>
 *
 * @param <S> the command source type ({@code CommandSourceStack} in the game)
 */
public final class CommandRegistrar<S> {

    public static final String STRICT_PROPERTY = "politicalserver.commandsStrict";
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandRegistrar.class);

    private final CommandDispatcher<S> real;
    private final boolean strict;
    private final Map<String, String> owners = new LinkedHashMap<>();
    private final Set<String> allowedShadows = new LinkedHashSet<>();
    private final List<String> problems = new ArrayList<>();

    /**
     * @param dispatcher the game's dispatcher
     * @param strict     throw from {@link #finish()} instead of only logging
     */
    public CommandRegistrar(CommandDispatcher<S> dispatcher, boolean strict) {
        this.real = dispatcher;
        this.strict = strict;
    }

    /**
     * Declares that the mod deliberately registers over an existing root (for example a vanilla command it
     * replaces). Say why in the call site; an undeclared shadow is reported as a problem.
     */
    public CommandRegistrar<S> allowShadowing(String root) {
        allowedShadows.add(root);
        return this;
    }

    /** A dispatcher for one module. Everything it registers lands in the real tree and is attributed to {@code owner}. */
    public CommandDispatcher<S> forModule(String owner) {
        return new Tracking(real.getRoot(), owner);
    }

    /** Runs a module's registration with its own attributed dispatcher. */
    public void module(String owner, java.util.function.Consumer<CommandDispatcher<S>> registration) {
        registration.accept(forModule(owner));
    }

    /** The problems found so far, one line each. */
    public List<String> problems() {
        return List.copyOf(problems);
    }

    /** Root literal to the module that registered it first. */
    public Map<String, String> owners() {
        return Map.copyOf(owners);
    }

    /** Call after the last module. Throws in strict mode when there are problems; otherwise logs them. */
    public void finish() {
        if (problems.isEmpty()) {
            LOGGER.info("Registered {} command roots from the mod with no duplicates", owners.size());
            return;
        }
        for (String problem : problems) {
            LOGGER.error("Command registration: {}", problem);
        }
        if (strict) {
            throw new IllegalStateException("command registration found " + problems.size() + " problem(s): " + problems);
        }
    }

    private final class Tracking extends CommandDispatcher<S> {
        private final String owner;

        Tracking(RootCommandNode<S> root, String owner) {
            super(root);
            this.owner = owner;
        }

        @Override
        public LiteralCommandNode<S> register(LiteralArgumentBuilder<S> command) {
            String name = command.getLiteral();
            String first = owners.get(name);
            if (first != null) {
                problems.add("/" + name + " is registered by " + first + " and again by " + owner);
            } else if (real.getRoot().getChild(name) != null && !allowedShadows.contains(name)) {
                problems.add("/" + name + " from " + owner + " would merge into a command that already exists (vanilla or another mod)");
            }
            owners.putIfAbsent(name, owner);
            return super.register(command);
        }
    }
}
