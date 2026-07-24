package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

/**
 * V7 Pillar A items. Only one exists: the <b>Clan Seal</b>, the craftable heirloom that
 * (together with 500,000 coins) founds a sorcerer clan. Keeping it craftable gates clan
 * creation behind material effort as well as coins.
 */
public final class ClanItems {

    private ClanItems() {}

    public static final String SEAL_ID = "clan_seal";

    public static ItemStack createClanSeal() {
        ItemStack s = new ItemStack(Items.HEART_OF_THE_SEA);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Clan Seal")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        java.util.List<Component> lore = new java.util.ArrayList<>();
        lore.add(Component.literal("§7A cursed heirloom pressed with the"));
        lore.add(Component.literal("§7mark of a would-be sorcerer bloodline."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Consumed when you found a clan."));
        lore.add(Component.literal("§7Founding also costs §6"
                + ClanManager.fmt(ClanManager.FOUND_COST) + " coins§7."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Open the Sorcerer Clans menu to use it."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        SlayerItems.setCustomItemId(s, SEAL_ID);
        return s;
    }

    public static boolean isSeal(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return false;
        return SEAL_ID.equals(SlayerItems.stripSkyblockEnchantSuffix(id));
    }

    public static boolean hasSeal(ServerPlayer player) {
        if (player == null) return false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (isSeal(player.getInventory().getItem(i))) return true;
        }
        return false;
    }

    /** Removes exactly one seal. Returns false if none was found (nothing is changed). */
    public static boolean consumeSeal(ServerPlayer player) {
        if (player == null) return false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (isSeal(st)) {
                st.shrink(1);
                if (st.isEmpty()) player.getInventory().setItem(i, ItemStack.EMPTY);
                return true;
            }
        }
        return false;
    }

    public static void giveSeal(ServerPlayer player) {
        if (player == null) return;
        ItemStack s = createClanSeal();
        if (!player.getInventory().add(s)) {
            player.drop(s, false);
        }
    }
}
