package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Main Upgrades GUI - Hub for all coin sink categories
 * Each category opens its own themed sub-GUI
 */
public class UpgradesGui {

    public static void openMainGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("§6§l⚡ Upgrades Hub"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        // Economic Upgrades
        gui.setSlot(10, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("§6§l💰 Economic Upgrades"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Bank vaults, stock tips,"))
                .addLoreLine(Component.literal("§7crypto boosts & more!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((index, type, containerInput, slotGui) -> openEconomicGui(player))
                .build());

        // Bounty Upgrades
        gui.setSlot(11, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("§4§l⚔️ Bounty Upgrades"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Bounty XP, protection,"))
                .addLoreLine(Component.literal("§7boss summons & more!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((index, type, containerInput, slotGui) -> openBountyGui(player))
                .build());

        // Combat Upgrades
        gui.setSlot(12, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Component.literal("§c§l🛡️ Combat Upgrades"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Damage boosts, defense,"))
                .addLoreLine(Component.literal("§7death protection & more!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((index, type, containerInput, slotGui) -> openCombatGui(player))
                .build());

        // Utility Upgrades
        gui.setSlot(13, new GuiElementBuilder(Items.CLOCK)
                .setName(Component.literal("§b§l🔧 Utility Upgrades"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Flight, mining, farming,"))
                .addLoreLine(Component.literal("§7weather & time control!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((index, type, containerInput, slotGui) -> openUtilityGui(player))
                .build());

        // Cosmetic Upgrades
        gui.setSlot(14, new GuiElementBuilder(Items.NAME_TAG)
                .setName(Component.literal("§d§l✨ Cosmetic Upgrades"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Titles, pets, perk slots,"))
                .addLoreLine(Component.literal("§7custom effects & more!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((index, type, containerInput, slotGui) -> openCosmeticGui(player))
                .build());

        // VIP Upgrades
        gui.setSlot(15, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("§e§l⭐ VIP Upgrades"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Chunk loaders, extra homes,"))
                .addLoreLine(Component.literal("§7dimension warps & more!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((index, type, containerInput, slotGui) -> openVipGui(player))
                .build());

        // Gambling
        gui.setSlot(16, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("§5§l🎰 Gambling"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Lottery, coin flip,"))
                .addLoreLine(Component.literal("§7slot machines & more!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((index, type, containerInput, slotGui) -> openGamblingGui(player))
                .build());

        // Premium Items
        gui.setSlot(17, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("§d§l✦ Premium Items"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Legendary weapons, armor,"))
                .addLoreLine(Component.literal("§7foods & artifacts!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cNo recipes - Coin purchase only!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((index, type, containerInput, slotGui) -> openPremiumItemsGui(player))
                .build());

        // Close
        gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§cClose"))
                .setCallback((index, type, containerInput, slotGui) -> player.closeContainer())
                .build());

        gui.open();
    }

    public static void openEconomicGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("§6§l💰 Economic Upgrades"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        gui.setSlot(10, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Component.literal("§5§l🏦 Bank Vault Expansion"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 500,000,000 coins"))
                .addLoreLine(Component.literal("§7Permanent!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§a+1B bank storage capacity!"))
                .addLoreLine(Component.literal("§7Max 5 expansions"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseBankVaultExpansion(player); openEconomicGui(player); })
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("§2§l📈 Stock Insider Tip"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 150,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aPredict next stock trends!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseInsiderTip(player); openEconomicGui(player); })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                .setName(Component.literal("§b§l₿ Crypto Mining Boost"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 100,000,000 coins"))
                .addLoreLine(Component.literal("§7Duration: 1 hour"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§a2x crypto mining rate!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseCryptoBoost(player); openEconomicGui(player); })
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal("§6§l⭐ Featured Auction"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 25,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aHighlight your next auction!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseFeaturedAuction(player); openEconomicGui(player); })
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("§6§l🗳️ Election Vote Boost"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 200,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aYour next vote counts DOUBLE!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseElectionBoost(player); openEconomicGui(player); })
                .build());

        gui.setSlot(18, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e← Back to Hub"))
                .setCallback((index, type, containerInput, slotGui) -> openMainGui(player))
                .build());

        gui.open();
    }

    public static void openBountyGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("§4§l⚔️ Bounty Upgrades"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        gui.setSlot(10, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("§4§l⚔️ Bounty XP Boost"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 75,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aInstant +10,000 Bounty XP!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseSlayerXpBoost(player); openBountyGui(player); })
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.SHIELD)
                .setName(Component.literal("§9§l🛡️ Bounty Protection"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 60,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aExtend protection by 24h!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseBountyProtectionExtension(player); openBountyGui(player); })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e← Back to Hub"))
                .setCallback((index, type, containerInput, slotGui) -> openMainGui(player))
                .build());

        gui.open();
    }

    public static void openCombatGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("§c§l🛡️ Combat Upgrades"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        gui.setSlot(10, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Component.literal("§c§l⚔️ +25% Damage Boost"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 45,000,000 coins"))
                .addLoreLine(Component.literal("§7Duration: 1 hour"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§a+25% damage dealt!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseDamageBoost(player); openCombatGui(player); })
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.DIAMOND_CHESTPLATE)
                .setName(Component.literal("§9§l🛡️ 25% Damage Reduction"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 40,000,000 coins"))
                .addLoreLine(Component.literal("§7Duration: 1 hour"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aTake 25% less damage!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseDefenseBoost(player); openCombatGui(player); })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.TOTEM_OF_UNDYING)
                .setName(Component.literal("§e§l💀 Death Protection"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 50,000,000 coins"))
                .addLoreLine(Component.literal("§7Duration: Until death or 24h"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aKeep inventory on death!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseDeathProtection(player); openCombatGui(player); })
                .build());

        gui.setSlot(18, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e← Back to Hub"))
                .setCallback((index, type, containerInput, slotGui) -> openMainGui(player))
                .build());

        gui.open();
    }

    public static void openUtilityGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("§b§l🔧 Utility Upgrades"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        gui.setSlot(10, new GuiElementBuilder(Items.ELYTRA)
                .setName(Component.literal("§d§l🕊️ Creative Flight"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 75,000,000 coins"))
                .addLoreLine(Component.literal("§7Duration: 1 hour"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aFly like creative mode!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseFlight(player); openUtilityGui(player); })
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.DIAMOND_PICKAXE)
                .setName(Component.literal("§b§l⛏️ 3x Mining Boost"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 35,000,000 coins"))
                .addLoreLine(Component.literal("§7Duration: 1 hour"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aTriple mining drops!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseMiningBoost(player); openUtilityGui(player); })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.WHEAT)
                .setName(Component.literal("§e§l🌾 Instant Crop Growth"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 30,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aGrow all crops in 50 block"))
                .addLoreLine(Component.literal("§aradius instantly!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseInstantCropGrowth(player); openUtilityGui(player); })
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.GLOWSTONE)
                .setName(Component.literal("§e§l☀️ Weather Control"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 100,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aSet weather for 1 hour!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick for clear weather"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseWeatherControl(player, true); openUtilityGui(player); })
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.CLOCK)
                .setName(Component.literal("§6§l⏰ Time Control"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 75,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aSet time to day or night!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick for daytime"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseTimeSet(player, true); openUtilityGui(player); })
                .build());

        gui.setSlot(18, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e← Back to Hub"))
                .setCallback((index, type, containerInput, slotGui) -> openMainGui(player))
                .build());

        gui.open();
    }

    public static void openCosmeticGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("§d§l✨ Cosmetic Upgrades"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        gui.setSlot(10, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("§5§l📖 Title Shop"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aPurchase custom titles!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to view"))
                .setCallback((index, type, containerInput, slotGui) -> CoinSinkManager.openTitleShop(player))
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.BONE)
                .setName(Component.literal("§f§l🐕 Pet Summon"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 40,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aSpawn a tamed pet!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick for wolf"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchasePet(player, "wolf"); openCosmeticGui(player); })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.ENCHANTED_GOLDEN_APPLE)
                .setName(Component.literal("§d§l✨ Extra Perk Slot"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 400,000,000 coins"))
                .addLoreLine(Component.literal("§7Permanent!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aUnlock an extra perk slot!"))
                .addLoreLine(Component.literal("§7Max 3 extra slots"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchasePerkSlot(player); openCosmeticGui(player); })
                .build());

        gui.setSlot(18, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e← Back to Hub"))
                .setCallback((index, type, containerInput, slotGui) -> openMainGui(player))
                .build());

        gui.open();
    }

    public static void openVipGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("§e§l⭐ VIP Upgrades"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        gui.setSlot(10, new GuiElementBuilder(Items.RED_BED)
                .setName(Component.literal("§c§l🏠 Extra Home Slot"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 25,000,000 coins"))
                .addLoreLine(Component.literal("§7Permanent!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aYou have: " + CoinSinkManager.getExtraHomes(player.getUUID().toString()) + " extra homes"))
                .addLoreLine(Component.literal("§7Max: 10 extra homes"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseExtraHome(player); openVipGui(player); })
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.REPEATING_COMMAND_BLOCK)
                .setName(Component.literal("§3§l🔄 Chunk Loader"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 100,000,000 coins"))
                .addLoreLine(Component.literal("§7Duration: 24 hours"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aKeep your chunk loaded!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseChunkLoader(player); openVipGui(player); })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.END_PORTAL_FRAME)
                .setName(Component.literal("§5§l🌀 Dimension Warp"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 30,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aWarp to any dimension!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eUse /warp <dimension>"))
                .build());

        gui.setSlot(18, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e← Back to Hub"))
                .setCallback((index, type, containerInput, slotGui) -> openMainGui(player))
                .build());

        gui.open();
    }

    public static void openGamblingGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("§5§l🎰 Gambling"));

        for (int i = 0; i < 27; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        gui.setSlot(10, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("§6§l🎫 Lottery Ticket"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 5,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aCurrent pot: " + String.format("%,d", CoinSinkManager.getLotteryPot()) + " coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to buy ticket"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseLotteryTicket(player); openGamblingGui(player); })
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("§e§l🪙 Coin Flip"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 10,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§a50/50 chance to double!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to flip"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseCoinFlip(player); openGamblingGui(player); })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.DISPENSER)
                .setName(Component.literal("§5§l🎰 Slot Machine"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 25,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aSpin for big rewards!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to spin"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseSlotMachine(player); openGamblingGui(player); })
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Component.literal("§5§l📦 Mystery Box"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 50,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§dRandom coin rewards!"))
                .addLoreLine(Component.literal("§71% chance for 500M jackpot!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to open"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseMysteryBox(player); openGamblingGui(player); })
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.YELLOW_SHULKER_BOX)
                .setName(Component.literal("§6§l📦 Legendary Mystery Box"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 150,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§dBetter odds!"))
                .addLoreLine(Component.literal("§75% chance for 1B jackpot!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to open"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseLegendaryMysteryBox(player); openGamblingGui(player); })
                .build());

        gui.setSlot(18, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e← Back to Hub"))
                .setCallback((index, type, containerInput, slotGui) -> openMainGui(player))
                .build());

        gui.open();
    }

    public static void openPremiumItemsGui(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("§d§l✦ Premium Items"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        // Row 1 - Weapons
        gui.setSlot(10, new GuiElementBuilder(Items.MACE)
                .setName(Component.literal("§6§lThe Gavel"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 500,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cLegendary Mace"))
                .addLoreLine(Component.literal("§7AoE stun on hit"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cNo recipe - Premium only!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseTheGavel(player); openPremiumItemsGui(player); })
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.STICK)
                .setName(Component.literal("§e§lHarvey's Stick"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 200,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eLegendary Weapon"))
                .addLoreLine(Component.literal("§7Lightning on hit"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cNo recipe - Premium only!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseHarveysStick(player); openPremiumItemsGui(player); })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.GOLDEN_SHOVEL)
                .setName(Component.literal("§b§lHPEBM Mk.I"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 150,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§bEnergy Beam Weapon"))
                .addLoreLine(Component.literal("§7Continuous damage beam"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cNo recipe - Premium only!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseHPEBM(player); openPremiumItemsGui(player); })
                .build());

        // Row 2 - Accessories
        gui.setSlot(19, new GuiElementBuilder(Items.IRON_BOOTS)
                .setName(Component.literal("§b§lHermes Shoes"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 300,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§bSpeed Boots"))
                .addLoreLine(Component.literal("§7Permanent Speed V"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cNo recipe - Premium only!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseHermesShoes(player); openPremiumItemsGui(player); })
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.AMETHYST_SHARD)
                .setName(Component.literal("§5§lRift Mirror"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 400,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§5Teleportation Item"))
                .addLoreLine(Component.literal("§7Warp to saved location"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cNo recipe - Premium only!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseRiftMirror(player); openPremiumItemsGui(player); })
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.BLAZE_POWDER)
                .setName(Component.literal("§c§lEmber Signet"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 350,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cFire Damage Ring"))
                .addLoreLine(Component.literal("§7Burn nearby enemies"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cNo recipe - Premium only!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseEmberSignet(player); openPremiumItemsGui(player); })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.SNOWBALL)
                .setName(Component.literal("§b§lFrostbound Talisman"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 350,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§bIce Protection"))
                .addLoreLine(Component.literal("§7Freeze nearby enemies"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cNo recipe - Premium only!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseFrostboundTalisman(player); openPremiumItemsGui(player); })
                .build());

        // Row 3 - More accessories
        gui.setSlot(28, new GuiElementBuilder(Items.BELL)
                .setName(Component.literal("§d§lEcho Chime"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 250,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§dSound Weapon"))
                .addLoreLine(Component.literal("§7Sonic damage burst"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cNo recipe - Premium only!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseEchoChime(player); openPremiumItemsGui(player); })
                .build());

        gui.setSlot(29, new GuiElementBuilder(Items.MOSS_BLOCK)
                .setName(Component.literal("§a§lTerra Relay"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 250,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§aEarth Powers"))
                .addLoreLine(Component.literal("§7Haste & regeneration"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cNo recipe - Premium only!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseTerraRelay(player); openPremiumItemsGui(player); })
                .build());

        // Row 3 - Foods
        gui.setSlot(31, new GuiElementBuilder(Items.ENCHANTED_GOLDEN_APPLE)
                .setName(Component.literal("§6§lGolden Apple of Kings"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 100,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Legendary Food"))
                .addLoreLine(Component.literal("§aRegen V, Resistance III"))
                .addLoreLine(Component.literal("§dAbsorption V, Fire Res"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cNo recipe - Premium only!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseGoldenAppleOfKings(player); openPremiumItemsGui(player); })
                .build());

        gui.setSlot(32, new GuiElementBuilder(Items.GOLDEN_APPLE)
                .setName(Component.literal("§d§l✦ Ambrosia ✦"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§6Cost: 250,000,000 coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§dFood of the Gods"))
                .addLoreLine(Component.literal("§aALL positive effects"))
                .addLoreLine(Component.literal("§afor 10 minutes each!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§cNo recipe - Premium only!"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to purchase"))
                .setCallback((index, type, containerInput, slotGui) -> { CoinSinkManager.purchaseAmbrosia(player); openPremiumItemsGui(player); })
                .build());

        // Back button
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§e← Back to Hub"))
                .setCallback((index, type, containerInput, slotGui) -> openMainGui(player))
                .build());

        gui.open();
    }
}
