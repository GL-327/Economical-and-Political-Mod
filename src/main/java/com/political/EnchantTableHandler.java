package com.political;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * The enchanting table.
 *
 * <p>Right clicking a vanilla enchanting table opens the custom enchantments instead of the vanilla
 * screen. That is the whole point of doing it this way: nobody has to learn a command, nobody has to
 * find a special block, and the thing that has meant "enchant this" since 2011 keeps meaning it.</p>
 *
 * <p>The vanilla screen is not lost so much as made redundant. Everything it offered has a custom
 * equivalent in the eighty, priced in the same two currencies vanilla used, so there is nothing
 * you could have done at the old screen that you cannot do at this one.</p>
 */
public final class EnchantTableHandler {

    private EnchantTableHandler() {}

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            if (!world.getBlockState(hitResult.getBlockPos()).is(Blocks.ENCHANTING_TABLE)) {
                return InteractionResult.PASS;
            }
            // Sneaking falls through to vanilla, so anybody who wants the old table still has it.
            if (player.isShiftKeyDown()) return InteractionResult.PASS;

            CustomEnchantGui.open(sp);
            return InteractionResult.SUCCESS;
        });
    }

    // ── Currency ──────────────────────────────────────────────────────────

    /** Lapis the player is carrying. */
    public static int lapisHeld(ServerPlayer player) {
        int n = 0;
        for (ItemStack st : player.getInventory().getNonEquipmentItems()) {
            if (st.isEmpty() || !st.is(Items.LAPIS_LAZULI)) continue;
            // A lapis with a custom id is somebody's item, not currency.
            if (SlayerItems.getCustomItemId(st) != null) continue;
            n += st.getCount();
        }
        return n;
    }

    /** Takes lapis. Returns false and takes nothing when there is not enough. */
    public static boolean takeLapis(ServerPlayer player, int count) {
        if (count <= 0) return true;
        if (lapisHeld(player) < count) return false;
        int left = count;
        for (ItemStack st : player.getInventory().getNonEquipmentItems()) {
            if (left <= 0) break;
            if (st.isEmpty() || !st.is(Items.LAPIS_LAZULI)) continue;
            if (SlayerItems.getCustomItemId(st) != null) continue;
            int take = Math.min(left, st.getCount());
            st.shrink(take);
            left -= take;
        }
        return left <= 0;
    }

    /**
     * Charges for an enchantment, taking both currencies or neither.
     *
     * <p>Levels are checked before either is taken, because taking the lapis and then finding the
     * player a level short is the kind of bug people rightly never forgive.</p>
     */
    public static boolean charge(ServerPlayer player, int xpLevels, int lapis) {
        if (player.experienceLevel < xpLevels) return false;
        if (lapisHeld(player) < lapis) return false;
        if (!takeLapis(player, lapis)) return false;
        player.giveExperienceLevels(-xpLevels);
        return true;
    }

    /** Hands both back, for when an application is refused after payment. */
    public static void refund(ServerPlayer player, int xpLevels, int lapis) {
        player.giveExperienceLevels(xpLevels);
        if (lapis > 0) {
            ItemStack back = new ItemStack(Items.LAPIS_LAZULI, lapis);
            if (!player.getInventory().add(back)) player.drop(back, false);
        }
    }
}
