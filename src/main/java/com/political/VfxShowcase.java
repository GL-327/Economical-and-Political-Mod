package com.political;


import com.political.settlement.WarGui;
import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/** The effects gallery. Pick a set piece and it plays in front of you. */
public final class VfxShowcase {

    private VfxShowcase() {}

    private record Entry(Vfx.Kind kind, String name, Item icon, ChatFormatting colour, String blurb) {}

    private static final Entry[] ENTRIES = {
            new Entry(Vfx.Kind.LANCE_ARCANE, "Arcane Lance", Items.END_ROD, ChatFormatting.LIGHT_PURPLE,
                    "Charges, then fires a searing beam with a spiralling sheath."),
            new Entry(Vfx.Kind.LANCE_INFERNO, "Inferno Lance", Items.BLAZE_ROD, ChatFormatting.GOLD,
                    "The same shot wrapped in fire and smoke."),
            new Entry(Vfx.Kind.LANCE_VOID, "Void Lance", Items.ENDER_EYE, ChatFormatting.DARK_PURPLE,
                    "A beam that drags the void along behind it."),
            new Entry(Vfx.Kind.LANCE_CURSED, "Cursed Lance", Items.WITHER_SKELETON_SKULL, ChatFormatting.DARK_GRAY,
                    "Soulfire and sculk, for the sorcerers."),
            new Entry(Vfx.Kind.STORM_STRIKE, "Storm Strike", Items.LIGHTNING_ROD.weathering().unaffected(), ChatFormatting.YELLOW,
                    "A bolt from the sky with forks and a ground shockwave."),
            new Entry(Vfx.Kind.DOMAIN_COLLAPSE, "Domain Collapse", Items.BEACON, ChatFormatting.AQUA,
                    "A dome slams out, holds, then implodes to a point."),
            new Entry(Vfx.Kind.METEOR, "Meteor", Items.FIRE_CHARGE, ChatFormatting.RED,
                    "A burning fall that cracks the ground on landing."),
            new Entry(Vfx.Kind.VOID_RIFT, "Void Rift", Items.ECHO_SHARD, ChatFormatting.DARK_AQUA,
                    "A tear that widens, churns, then snaps shut."),
            new Entry(Vfx.Kind.BLOOM, "Bloom", Items.GLISTERING_MELON_SLICE, ChatFormatting.GREEN,
                    "A soft rising halo for healing moments."),
    };

    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20};

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Effects Gallery").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        WarGui.fill(gui, 36);

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Effects Gallery").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Every set piece is animated over many ticks,"))
                .addLoreLine(Component.literal("§7with a core, a sheath, a haze and an impact."))
                .addLoreLine(Component.literal("§8Click one and it plays where you are looking."))
                .glow().build());

        for (int i = 0; i < ENTRIES.length && i < SLOTS.length; i++) {
            Entry e = ENTRIES[i];
            gui.setSlot(SLOTS[i], new GuiElementBuilder(e.icon())
                    .setName(Component.literal(e.name()).withStyle(e.colour(), ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7" + e.blurb()))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§eClick to play"))
                    .setCallback((a, b, c, d) -> {
                        player.closeContainer();
                        playFor(player, e.kind());
                    })
                    .build());
        }

        gui.setSlot(31, WarGui.back(() -> CivilizationHubGui.open(player)));
        gui.open();
    }

    /** Plays a set piece from the player's eye line, so beams fire where they are aiming. */
    public static void playFor(ServerPlayer player, Vfx.Kind kind) {
        if (!(player.level() instanceof ServerLevel level)) return;
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        switch (kind) {
            case STORM_STRIKE, METEOR -> {
                // Ground effects land where the player is aiming.
                Vec3 at = Vfx.raycast(level, eye, look, 30, player);
                Vfx.play(level, at, look, kind, player);
            }
            case DOMAIN_COLLAPSE, VOID_RIFT, BLOOM ->
                    Vfx.play(level, player.position().add(0, 1, 0), look, kind, player);
            default ->
                // Beams start just in front of the chest so they do not clip the camera.
                    Vfx.play(level, eye.add(look.scale(0.6)), look, kind, player);
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vfx").executes(ctx -> {
            open(ctx.getSource().getPlayerOrException());
            return 1;
        }));
        dispatcher.register(Commands.literal("effects").executes(ctx -> {
            open(ctx.getSource().getPlayerOrException());
            return 1;
        }));
    }
}
