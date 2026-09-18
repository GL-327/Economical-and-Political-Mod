package com.political;


import com.political.bounty.SlayerItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

/**
 * V5 Pillar 2 items. Only one exists: the <b>Guild Charter</b>, the craftable licence that
 * (together with 250,000 coins) founds a guild. Making the charter craftable rather than
 * purchasable keeps guild creation gated behind material effort as well as coins.
 */
public final class GuildItems {

    private GuildItems() {}

    public static final String CHARTER_ID = "guild_charter";

    public static ItemStack createGuildCharter() {
        ItemStack s = new ItemStack(Items.WRITTEN_BOOK);
        s.set(DataComponents.CUSTOM_NAME, Component.literal("✦ Guild Charter")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        java.util.List<Component> lore = new java.util.ArrayList<>();
        lore.add(Component.literal("§7A sealed writ of association,"));
        lore.add(Component.literal("§7recognised across every settlement."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§7Consumed when you found a guild."));
        lore.add(Component.literal("§7Founding also costs §6"
                + GuildManager.fmt(GuildManager.CREATION_COST) + " coins§7."));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Open the Guild menu to use it."));
        s.set(DataComponents.LORE, new ItemLore(lore));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        SlayerItems.setCustomItemId(s, CHARTER_ID);
        return s;
    }

    public static boolean isCharter(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String id = SlayerItems.getCustomItemId(stack);
        if (id == null) return false;
        return CHARTER_ID.equals(SlayerItems.stripSkyblockEnchantSuffix(id));
    }

    public static boolean hasCharter(ServerPlayer player) {
        if (player == null) return false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (isCharter(player.getInventory().getItem(i))) return true;
        }
        return false;
    }

    /** Removes exactly one charter. Returns false if none was found (nothing is changed). */
    public static boolean consumeCharter(ServerPlayer player) {
        if (player == null) return false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (isCharter(st)) {
                st.shrink(1);
                if (st.isEmpty()) player.getInventory().setItem(i, ItemStack.EMPTY);
                return true;
            }
        }
        return false;
    }

    public static void giveCharter(ServerPlayer player) {
        if (player == null) return;
        ItemStack s = createGuildCharter();
        if (!player.getInventory().add(s)) {
            player.drop(s, false);
        }
    }
}
