package com.political;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.util.Prediction;
import com.political.bounty.SlayerItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Rolls the rare drops and announces them.
 *
 * <p>The roll happens on the kill, once, against the one drop that matches the victim. Announcements
 * scale with the tier: a rare find tells the finder, a crazy one tells anybody close enough to have
 * seen it happen, and an insane one tells the server. That last case is the one that matters, since
 * somebody else's luck is only interesting if you hear about it.</p>
 *
 * <p>Drops are never guaranteed and never craftable. That is the whole role they play in the mod:
 * every other reward can be planned for, and a server needs a few things that cannot.</p>
 */
public final class RareDropManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RareDropManager.class);


    private RareDropManager() {}

    private static final Random RNG = new Random();
    /** How far away somebody has to be to have plausibly seen it. */
    private static final double NEARBY = 48.0;

    public static boolean enabled = true;

    // ── The item ──────────────────────────────────────────────────────────

    public static ItemStack create(RareDrop d) {
        ItemStack s = new ItemStack(d.base);
        s.set(DataComponents.CUSTOM_NAME,
                Component.literal(d.display).withStyle(d.colour, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8Rare Drop, " + d.tier.name().charAt(0)
                + d.tier.name().substring(1).toLowerCase(java.util.Locale.ROOT)));
        lore.add(Component.literal("§8\"" + d.flavor + "\""));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7" + d.use));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Drops from " + sourceName(d)));
        lore.add(Component.literal("§8Chance " + d.chanceText()));
        lore.add(Component.literal("§8This cannot be crafted."));

        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, d.itemId());
        return s;
    }

    public static String sourceName(RareDrop d) {
        try {
            return d.source.getDescription().getString();
        } catch (Exception e) {
            LOGGER.debug("RareDropManager.sourceName: ignored an error", e);
            return "something";
        }
    }

    public static RareDrop dropOf(ItemStack stack) {
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null || !id.startsWith("drop_")) return null;
        return RareDrop.byKey(SlayerItems.stripSkyblockEnchantSuffix(id).substring(5));
    }

    // ── The roll ──────────────────────────────────────────────────────────

    /** Called on every player kill. Rolls the one drop that matches the victim, if any. */
    public static void onKill(ServerPlayer player, LivingEntity victim) {
        if (!enabled || player == null || victim == null) return;
        try {
            for (RareDrop d : RareDrop.values()) {
                if (victim.getType() != d.source) continue;
                if (RNG.nextDouble() >= d.chance * luckOf(player)) return;
                award(player, d);
                return;
            }
        } catch (Exception e) {
            LOGGER.debug("RareDropManager.onKill: ignored an error", e);
            // A drop is never worth failing a kill over.
        }
    }

    /**
     * Luck, from things a player has genuinely earned.
     *
     * <p>Kept small on purpose. A drop that can be pushed from one in a thousand to one in fifty
     * stops being a rare drop and becomes a grind, so this tops out a little under double.</p>
     */
    private static double luckOf(ServerPlayer player) {
        double luck = 1.0;
        try {
            // Collections are the long game, so they are the main source.
            luck += Math.min(0.5, CollectionManager.totalTiers(player) / 1000.0);
            // A maxed collection is worth a real nudge.
            luck += Math.min(0.25, CollectionManager.maxedCount(player) * 0.025);
            // Wearing the warrant says you have done the work.
            if (WarrantArmour.fullSet(player)) luck += 0.2;
        } catch (Exception ignored) {
            LOGGER.debug("RareDropManager.luckOf: ignored an error", ignored);
            // Any system not ready simply contributes nothing.
        }
        return Math.min(1.95, luck);
    }

    private static void award(ServerPlayer player, RareDrop d) {
        ItemStack stack = create(d);
        if (!player.getInventory().add(stack.copy())) player.drop(stack.copy(), false, Prediction.SERVER_ONLY);
        announce(player, d);

        if (player.level() instanceof ServerLevel level) {
            boolean big = d.tier != RareDrop.Tier.RARE;
            level.playSound(null, player.blockPosition(),
                    big ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE : SoundEvents.NOTE_BLOCK_BELL.value(),
                    SoundSource.PLAYERS, 0.9f, big ? 1.0f : 1.5f);
            if (big) Vfx.bloom(level, player.position().add(0, 1, 0));
        }
    }

    private static void announce(ServerPlayer player, RareDrop d) {
        String line = "  " + d.tier.colour + "" + ChatFormatting.BOLD + d.tier.shout
                + " §r" + d.colour + d.display;

        switch (d.tier.reach) {
            case FINDER -> player.sendSystemMessage(Component.literal(line));
            case NEARBY -> {
                if (!(player.level() instanceof ServerLevel level)) {
                    player.sendSystemMessage(Component.literal(line));
                    return;
                }
                String withName = "  " + d.tier.colour + "" + ChatFormatting.BOLD + d.tier.shout
                        + " §r§7" + player.getName().getString() + " found "
                        + d.colour + d.display;
                for (ServerPlayer p : level.players()) {
                    if (p == player) {
                        p.sendSystemMessage(Component.literal(line));
                    } else if (p.distanceToSqr(player) <= NEARBY * NEARBY) {
                        p.sendSystemMessage(Component.literal(withName));
                    }
                }
            }
            case SERVER -> {
                if (PoliticalServer.server == null) {
                    player.sendSystemMessage(Component.literal(line));
                    return;
                }
                for (ServerPlayer p : PoliticalServer.server.getPlayerList().getPlayers()) {
                    p.sendSystemMessage(Component.literal(""));
                    p.sendSystemMessage(Component.literal("  " + d.tier.colour
                            + ChatFormatting.BOLD + d.tier.shout));
                    p.sendSystemMessage(Component.literal("  §7" + player.getName().getString()
                            + " found " + d.colour + ChatFormatting.BOLD + d.display));
                    p.sendSystemMessage(Component.literal("  §8from a " + sourceName(d)
                            + ", at " + d.chanceText()));
                    p.sendSystemMessage(Component.literal(""));
                }
            }
        }
    }

    /** Every drop item, for the catalogue screens. */
    public static List<ItemStack> allStacks() {
        List<ItemStack> out = new ArrayList<>();
        for (RareDrop d : RareDrop.values()) out.add(create(d));
        return out;
    }
}
