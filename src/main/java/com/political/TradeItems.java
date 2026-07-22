package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * V5 Pillar 3 items. All three are dressed vanilla stacks carrying a custom_item_id.
 *
 * <ul>
 *   <li><b>Caravan Charter</b> — consumed to open a trade route. Craftable
 *       ({@link TradeRecipeBootstrap}), so route creation costs materials as well as coins.</li>
 *   <li><b>Merchant's Seal</b> — consumed at the Trade GUI to grant a settlement +12% delivered
 *       route value for 24 hours. Craftable, but deliberately expensive.</li>
 *   <li><b>Letter of Marque</b> — <b>NOT craftable</b>. Dropped (25%) when you plunder a caravan.
 *       Consumed to plunder a caravan you are not at war with at a much better rate. The only way
 *       to obtain one is to have already raided, which keeps sanctioned banditry self-limiting.</li>
 * </ul>
 */
public final class TradeItems {

    private TradeItems() {}

    public static final String ID_CHARTER = "caravan_charter";
    public static final String ID_SEAL = "merchants_seal";
    public static final String ID_MARQUE = "letter_of_marque";

    // ── Factories ───────────────────────────────────────────────────────────

    public static ItemStack createCharter() {
        ItemStack s = new ItemStack(Items.PAPER);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Caravan Charter")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§7A sealed writ of passage between"));
        lore.add(Component.literal("§7two settlements."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§eConsumed when you open a trade route."));
        lore.add(Component.literal("§8Open the Trade Hall from /civ or /trade."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_CHARTER);
        return s;
    }

    public static ItemStack createMerchantSeal() {
        ItemStack s = new ItemStack(Items.HONEYCOMB);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Merchant's Seal")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§7Guild wax, pressed with the mark"));
        lore.add(Component.literal("§7of a trusted house."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§a+12% §7delivered route value"));
        lore.add(Component.literal("§7for §f24 hours§7, for one settlement."));
        lore.add(Component.literal("§8Applied from the Trade Hall."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_SEAL);
        return s;
    }

    public static ItemStack createLetterOfMarque() {
        ItemStack s = new ItemStack(Items.WRITTEN_BOOK);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Letter of Marque")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§7Someone, somewhere, signed for this."));
        lore.add(Component.literal("§7No one will admit who."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§cConsumed when you destroy a caravan"));
        lore.add(Component.literal("§cyou are §lnot§r§c at war with —"));
        lore.add(Component.literal("§7plunder §f55% §7instead of §f20%§7."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Not craftable. Dropped by plundering."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, ID_MARQUE);
        return s;
    }

    // ── Inventory helpers ───────────────────────────────────────────────────

    public static void give(ServerPlayer player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) return;
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    public static int count(ServerPlayer player, String customId) {
        if (player == null || customId == null) return 0;
        int n = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (matches(st, customId)) n += st.getCount();
        }
        return n;
    }

    /** Removes exactly one matching item. Returns false (and changes nothing) if none is held. */
    public static boolean consumeOne(ServerPlayer player, String customId) {
        if (player == null || customId == null) return false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (matches(st, customId)) {
                st.shrink(1);
                if (st.isEmpty()) player.getInventory().setItem(i, ItemStack.EMPTY);
                player.getInventory().setChanged();
                return true;
            }
        }
        return false;
    }

    private static boolean matches(ItemStack st, String customId) {
        if (st == null || st.isEmpty()) return false;
        String id = SlayerItems.getCustomItemId(st);
        if (id == null) return false;
        return customId.equals(SlayerItems.stripSkyblockEnchantSuffix(id));
    }
}
