package com.political.core.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The command source is a plain String here: the registrar does not care what the source is. */
class CommandRegistrarTest {

    private static LiteralArgumentBuilder<String> literal(String name) {
        return LiteralArgumentBuilder.<String>literal(name).executes(ctx -> 1);
    }

    @Test
    void distinctRootsFromDifferentModulesAreFine() {
        CommandDispatcher<String> real = new CommandDispatcher<>();
        CommandRegistrar<String> registrar = new CommandRegistrar<>(real, true);
        registrar.module("economy", d -> d.register(literal("coins")));
        registrar.module("politics", d -> d.register(literal("vote")));
        registrar.finish();
        assertEquals(List.of(), registrar.problems());
        assertEquals("economy", registrar.owners().get("coins"));
        assertTrue(real.getRoot().getChild("vote") != null, "registrations land in the real tree");
    }

    @Test
    void sameRootTwiceIsAProblemEvenInsideOneModule() {
        CommandDispatcher<String> real = new CommandDispatcher<>();
        CommandRegistrar<String> registrar = new CommandRegistrar<>(real, false);
        registrar.module("big-registry", d -> {
            d.register(literal("bounty"));
            d.register(literal("bounty"));
        });
        assertEquals(List.of("/bounty is registered by big-registry and again by big-registry"), registrar.problems());
        registrar.finish(); // not strict: logs, does not throw
    }

    @Test
    void strictModeThrowsOnFinish() {
        CommandDispatcher<String> real = new CommandDispatcher<>();
        CommandRegistrar<String> registrar = new CommandRegistrar<>(real, true);
        registrar.module("a", d -> d.register(literal("hunt")));
        registrar.module("b", d -> d.register(literal("hunt")));
        IllegalStateException e = assertThrows(IllegalStateException.class, registrar::finish);
        assertTrue(e.getMessage().contains("/hunt is registered by a and again by b"), e.getMessage());
    }

    @Test
    void shadowingAnExistingCommandIsReportedUnlessDeclared() {
        CommandDispatcher<String> real = new CommandDispatcher<>();
        real.register(literal("give")); // stands in for a vanilla command
        CommandRegistrar<String> strictRegistrar = new CommandRegistrar<>(real, true);
        strictRegistrar.module("mod", d -> d.register(literal("give")));
        assertEquals(1, strictRegistrar.problems().size());
        assertTrue(strictRegistrar.problems().get(0).contains("already exists"), strictRegistrar.problems().toString());

        CommandDispatcher<String> real2 = new CommandDispatcher<>();
        real2.register(literal("give"));
        CommandRegistrar<String> declared = new CommandRegistrar<>(real2, true).allowShadowing("give");
        declared.module("mod", d -> d.register(literal("give")));
        assertEquals(List.of(), declared.problems());
    }

    @Test
    void theRealDispatcherStillExecutesWhatWasRegistered() throws CommandSyntaxException {
        CommandDispatcher<String> real = new CommandDispatcher<>();
        CommandRegistrar<String> registrar = new CommandRegistrar<>(real, true);
        registrar.module("m", d -> d.register(LiteralArgumentBuilder.<String>literal("add")
                .then(RequiredArgumentBuilder.<String, Integer>argument("n", IntegerArgumentType.integer())
                        .executes(ctx -> IntegerArgumentType.getInteger(ctx, "n") + 1))));
        assertEquals(8, real.execute("add 7", "source"));
    }

    /**
     * The rule the M1.8 merges rely on: when a root is registered twice Brigadier keeps the sub-commands of
     * both, the executor of the later registration, and the requirement of the earlier one.
     */
    @Test
    void brigadierMergeKeepsFirstRequirementAndLastExecutor() throws CommandSyntaxException {
        CommandDispatcher<String> d = new CommandDispatcher<>();
        d.register(LiteralArgumentBuilder.<String>literal("c")
                .executes(ctx -> 1)
                .then(LiteralArgumentBuilder.<String>literal("add")
                        .requires(s -> s.equals("owner"))
                        .executes(ctx -> 10)));
        d.register(LiteralArgumentBuilder.<String>literal("c")
                .then(LiteralArgumentBuilder.<String>literal("add")
                        .requires(s -> true)
                        .executes(ctx -> 20))
                .then(LiteralArgumentBuilder.<String>literal("extra").executes(ctx -> 30)));

        assertEquals(1, d.execute("c", "anyone"), "a root executor only the first definition had survives");
        assertEquals(20, d.execute("c add", "owner"), "the later executor replaces the earlier");
        assertEquals(30, d.execute("c extra", "anyone"), "sub-commands of both are kept");
        assertThrows(CommandSyntaxException.class, () -> d.execute("c add", "anyone"),
                "the first definition's requirement stays, the later requirement is dropped");
    }

    @Test
    void treeDumpShowsShapeAndIgnoresRegistrationOrder() {
        CommandDispatcher<String> one = new CommandDispatcher<>();
        one.register(literal("b").then(literal("y")).then(literal("x")));
        one.register(LiteralArgumentBuilder.<String>literal("a").requires(s -> s.isEmpty())
                .then(RequiredArgumentBuilder.<String, Integer>argument("n", IntegerArgumentType.integer()).executes(ctx -> 0)));

        CommandDispatcher<String> two = new CommandDispatcher<>();
        two.register(LiteralArgumentBuilder.<String>literal("a").requires(s -> s.isEmpty())
                .then(RequiredArgumentBuilder.<String, Integer>argument("n", IntegerArgumentType.integer()).executes(ctx -> 0)));
        two.register(literal("b").then(literal("x")).then(literal("y")));

        String dump = CommandTreeDump.render(one.getRoot(), name -> true);
        assertEquals(dump, CommandTreeDump.render(two.getRoot(), name -> true));
        assertEquals("""
                a [req]
                  <n: IntegerArgumentType> [exec]
                b [exec]
                  x [exec]
                  y [exec]
                """, dump);
        assertFalse(dump.contains("[req]\n  <n") && dump.contains("<n: IntegerArgumentType> [req]"));
    }

    @Test
    void treeDumpCanLeaveRootsOut() {
        CommandDispatcher<String> d = new CommandDispatcher<>();
        d.register(literal("mine"));
        d.register(literal("vanilla"));
        assertEquals("mine [exec]\n", CommandTreeDump.render(d.getRoot(), name -> name.equals("mine")));
    }
}
