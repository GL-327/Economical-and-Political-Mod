package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for the /customitems admin command.
 * Shows ALL custom items organized by categories for easy access.
 */
public class CustomItemsGui {

    public interface BountyItemClickHandler {
        void onClick(ServerPlayer player, ItemStack item);
    }

    public interface BackHandler {
        void open(ServerPlayer player);
    }

    // ── Category definitions ─────────────────────────────────────
    public enum Category {
        CUSTOM_ARMOR ("💎 Custom Armor",                    Items.EMERALD,            ChatFormatting.GREEN),
        SWORDS_T1    ("⚔ Bounty Swords (T1)",               Items.IRON_SWORD,         ChatFormatting.YELLOW),
        SWORDS_T2    ("⚔ Bounty Swords (T2)",               Items.DIAMOND_SWORD,      ChatFormatting.AQUA),
        ARMOR_T1     ("🛡 Bounty Armor T1",                  Items.IRON_CHESTPLATE,    ChatFormatting.GREEN),
        ARMOR_T2     ("🛡 Bounty Armor T2",                  Items.DIAMOND_CHESTPLATE, ChatFormatting.DARK_AQUA),
        SPECIAL_ARMOR("🛡 Special / Legendary Armor",        Items.NETHERITE_HELMET,   ChatFormatting.GOLD),
        DRAGON_CHESTPLATES("🐉 Dragon chestplates",          Items.DRAGON_HEAD,        ChatFormatting.DARK_PURPLE),
        HPEBM        ("⚡ HPEBM Weapons",                    Items.BLAZE_ROD,          ChatFormatting.LIGHT_PURPLE),
        BOSS_DROPS   ("🎁 Boss Drops",                       Items.WITHER_SKELETON_SKULL, ChatFormatting.DARK_PURPLE),
        SPECIAL_ITEMS("🎁 Special Items",                    Items.STICK,              ChatFormatting.GREEN),
        CUSTOM_TOOLS ("⛏ Custom Tools",                    Items.NETHERITE_PICKAXE,  ChatFormatting.DARK_PURPLE),
        PIGLIN       ("👑 Piglin Items",                     Items.GOLDEN_HELMET,      ChatFormatting.GOLD),
        GOLD_ARMOR   ("👑 Gold Armor Evolution",             Items.GOLDEN_CHESTPLATE,  ChatFormatting.GOLD),
        MATERIALS    ("📦 Materials (Cores & Chunks)",       Items.SLIME_BALL,         ChatFormatting.GRAY),
        ATTRIBUTE_TOKENS("✧ Attribute Tokens",               Items.PAPER,              ChatFormatting.LIGHT_PURPLE),
        WEAPON_ATTRIBUTES("⚔ Weapon Attributes",               Items.NETHER_STAR,        ChatFormatting.RED),
        BLOCK_TOOLS   ("⛏ Block Tools",                     Items.DIAMOND_PICKAXE,    ChatFormatting.AQUA),
        CUSTOM_ARROWS ("➵ Custom Arrows",                    Items.TIPPED_ARROW,       ChatFormatting.GOLD),
        REFLECT_BLOCK_WEAPONS("⚔ Full block catalogue",     Items.NETHERITE_SWORD,    ChatFormatting.WHITE),
        REFLECT_BLOCK_ARMOR("🧱 Block armor (full)",        Items.NETHERITE_CHESTPLATE, ChatFormatting.DARK_PURPLE),
        ENCHANTED_BLOCK_GEAR("✨ Enchanted block gear",      Items.LAPIS_LAZULI,       ChatFormatting.AQUA),
        SECTOR_HEART_GEAR("♦ Verdant overalls T1–10",       Items.GOLDEN_HOE,         ChatFormatting.GREEN),
        DEEPSTRIKE_ITEMS("Deepstrike charter",              Items.NETHER_STAR,        ChatFormatting.GOLD),
        ASCENSION_MATERIALS ("⬆ Ascension Materials",        Items.PRISMARINE_SHARD,   ChatFormatting.AQUA),
        ASCENSION_RELICS    ("✧ Ascension Relics",           Items.DRAGON_BREATH,      ChatFormatting.LIGHT_PURPLE),
        MANAGER_ITEMS ("⭐ Manager Items",                    Items.NETHER_STAR,        ChatFormatting.YELLOW),
        MOB_DROPS     ("💀 Mob Drops",                      Items.WITHER_SKELETON_SKULL, ChatFormatting.RED),
        FISHING_RODS  ("🎣 Fishing Rods",                    Items.FISHING_ROD,           ChatFormatting.AQUA),
        MAGIC_STAVES  ("✦ Magic Staves",                    Items.BLAZE_ROD,             ChatFormatting.LIGHT_PURPLE);

        final String label;
        final net.minecraft.world.item.Item icon;
        final ChatFormatting colour;

        Category(String label, net.minecraft.world.item.Item icon, ChatFormatting colour) {
            this.label = label;
            this.icon  = icon;
            this.colour = colour;
        }
    }

    // Helper method to give item to player
    private static void giveItem(ServerPlayer player, ItemStack item) {
        if (player.addItem(item.copy())) {
            Component customName = item.get(DataComponents.CUSTOM_NAME);
            String itemName = customName != null ? customName.getString() : item.getItem().toString();
            player.sendSystemMessage(Component.literal("✅ ")
                    .append(Component.literal(itemName).withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(" added to inventory!").withStyle(ChatFormatting.GRAY)));
        } else {
            player.sendSystemMessage(Component.literal("❌ Inventory full! Make some space and try again.").withStyle(ChatFormatting.RED));
        }
    }

    /** Returns all items belonging to a given category. */
    private static List<ItemStack> getItemsForCategory(Category cat) {
        List<ItemStack> items = new ArrayList<>();
        switch (cat) {
            case CUSTOM_ARMOR -> {
                // Emerald armor
                items.add(GemGear.createEmeraldHelmet());
                items.add(GemGear.createEmeraldChestplate());
                items.add(GemGear.createEmeraldLeggings());
                items.add(GemGear.createEmeraldBoots());
                // Emerald tools
                items.add(GemGear.createEmeraldSword());
                items.add(GemGear.createEmeraldPickaxe());
                items.add(GemGear.createEmeraldAxe());
                items.add(GemGear.createEmeraldShovel());
                items.add(GemGear.createEmeraldHoe());
                // Lapis armor
                items.add(GemGear.createLapisHelmet());
                items.add(GemGear.createLapisChestplate());
                items.add(GemGear.createLapisLeggings());
                items.add(GemGear.createLapisBoots());
                // Lapis tools
                items.add(GemGear.createLapisSword());
                items.add(GemGear.createLapisPickaxe());
                items.add(GemGear.createLapisAxe());
                items.add(GemGear.createLapisShovel());
                items.add(GemGear.createLapisHoe());
                // Block armors - Glass
                items.add(BlockArmor.createGlassHelmet());
                items.add(BlockArmor.createGlassChestplate());
                items.add(BlockArmor.createGlassLeggings());
                items.add(BlockArmor.createGlassBoots());
                // Block armors - Obsidian
                items.add(BlockArmor.createObsidianHelmet());
                items.add(BlockArmor.createObsidianChestplate());
                items.add(BlockArmor.createObsidianLeggings());
                items.add(BlockArmor.createObsidianBoots());
                // Block armors - Quartz
                items.add(BlockArmor.createQuartzHelmet());
                items.add(BlockArmor.createQuartzChestplate());
                items.add(BlockArmor.createQuartzLeggings());
                items.add(BlockArmor.createQuartzBoots());
                // Block armors - Glowstone
                items.add(BlockArmor.createGlowstoneHelmet());
                items.add(BlockArmor.createGlowstoneChestplate());
                items.add(BlockArmor.createGlowstoneLeggings());
                items.add(BlockArmor.createGlowstoneBoots());
                // Block armors - Redstone
                items.add(BlockArmor.createRedstoneHelmet());
                items.add(BlockArmor.createRedstoneChestplate());
                items.add(BlockArmor.createRedstoneLeggings());
                items.add(BlockArmor.createRedstoneBoots());
                // Block armors - Netherrack
                items.add(BlockArmor.createNetherrackHelmet());
                items.add(BlockArmor.createNetherrackChestplate());
                items.add(BlockArmor.createNetherrackLeggings());
                items.add(BlockArmor.createNetherrackBoots());
                // Block armors - End Stone
                items.add(BlockArmor.createEndstoneHelmet());
                items.add(BlockArmor.createEndstoneChestplate());
                items.add(BlockArmor.createEndstoneLeggings());
                items.add(BlockArmor.createEndstoneBoots());
                // Block armors - Ice
                items.add(BlockArmor.createIceHelmet());
                items.add(BlockArmor.createIceChestplate());
                items.add(BlockArmor.createIceLeggings());
                items.add(BlockArmor.createIceBoots());
                // Block armors - Prismarine
                items.add(BlockArmor.createPrismarineHelmet());
                items.add(BlockArmor.createPrismarineChestplate());
                items.add(BlockArmor.createPrismarineLeggings());
                items.add(BlockArmor.createPrismarineBoots());
                // Block armors - Terracotta
                items.add(BlockArmor.createTerracottaHelmet());
                items.add(BlockArmor.createTerracottaChestplate());
                items.add(BlockArmor.createTerracottaLeggings());
                items.add(BlockArmor.createTerracottaBoots());
                // Block armors - Mossy
                items.add(BlockArmor.createMossyHelmet());
                items.add(BlockArmor.createMossyChestplate());
                items.add(BlockArmor.createMossyLeggings());
                items.add(BlockArmor.createMossyBoots());
                // Block armors - Soul Sand
                items.add(BlockArmor.createSoulSandHelmet());
                items.add(BlockArmor.createSoulSandChestplate());
                items.add(BlockArmor.createSoulSandLeggings());
                items.add(BlockArmor.createSoulSandBoots());
                // Block armors - Magma
                items.add(BlockArmor.createMagmaHelmet());
                items.add(BlockArmor.createMagmaChestplate());
                items.add(BlockArmor.createMagmaLeggings());
                items.add(BlockArmor.createMagmaBoots());
                // Block armors - Sandstone
                items.add(BlockArmor.createSandstoneHelmet());
                items.add(BlockArmor.createSandstoneChestplate());
                items.add(BlockArmor.createSandstoneLeggings());
                items.add(BlockArmor.createSandstoneBoots());
                // Block armors - Amethyst
                items.add(BlockArmor.createAmethystHelmet());
                items.add(BlockArmor.createAmethystChestplate());
                items.add(BlockArmor.createAmethystLeggings());
                items.add(BlockArmor.createAmethystBoots());
                // Block armors - Coal
                items.add(BlockArmor.createCoalHelmet());
                items.add(BlockArmor.createCoalChestplate());
                items.add(BlockArmor.createCoalLeggings());
                items.add(BlockArmor.createCoalBoots());
                // Block armors - Stripped Logs
                items.add(BlockArmor.createStrippedSpruceLogHelmet());
                items.add(BlockArmor.createStrippedSpruceLogChestplate());
                items.add(BlockArmor.createStrippedSpruceLogLeggings());
                items.add(BlockArmor.createStrippedSpruceLogBoots());
                items.add(BlockArmor.createStrippedBirchLogHelmet());
                items.add(BlockArmor.createStrippedBirchLogChestplate());
                items.add(BlockArmor.createStrippedBirchLogLeggings());
                items.add(BlockArmor.createStrippedBirchLogBoots());
                items.add(BlockArmor.createStrippedDarkOakLogHelmet());
                items.add(BlockArmor.createStrippedDarkOakLogChestplate());
                items.add(BlockArmor.createStrippedDarkOakLogLeggings());
                items.add(BlockArmor.createStrippedDarkOakLogBoots());
                items.add(BlockArmor.createStrippedJungleLogHelmet());
                items.add(BlockArmor.createStrippedJungleLogChestplate());
                items.add(BlockArmor.createStrippedJungleLogLeggings());
                items.add(BlockArmor.createStrippedJungleLogBoots());
                items.add(BlockArmor.createStrippedAcaciaLogHelmet());
                items.add(BlockArmor.createStrippedAcaciaLogChestplate());
                items.add(BlockArmor.createStrippedAcaciaLogLeggings());
                items.add(BlockArmor.createStrippedAcaciaLogBoots());
                items.add(BlockArmor.createStrippedMangroveLogHelmet());
                items.add(BlockArmor.createStrippedMangroveLogChestplate());
                items.add(BlockArmor.createStrippedMangroveLogLeggings());
                items.add(BlockArmor.createStrippedMangroveLogBoots());
                items.add(BlockArmor.createStrippedCherryLogHelmet());
                items.add(BlockArmor.createStrippedCherryLogChestplate());
                items.add(BlockArmor.createStrippedCherryLogLeggings());
                items.add(BlockArmor.createStrippedCherryLogBoots());
                items.add(BlockArmor.createStrippedBambooBlockHelmet());
                items.add(BlockArmor.createStrippedBambooBlockChestplate());
                items.add(BlockArmor.createStrippedBambooBlockLeggings());
                items.add(BlockArmor.createStrippedBambooBlockBoots());
                items.add(BlockArmor.createStrippedCrimsonStemHelmet());
                items.add(BlockArmor.createStrippedCrimsonStemChestplate());
                items.add(BlockArmor.createStrippedCrimsonStemLeggings());
                items.add(BlockArmor.createStrippedCrimsonStemBoots());
                items.add(BlockArmor.createStrippedWarpedStemHelmet());
                items.add(BlockArmor.createStrippedWarpedStemChestplate());
                items.add(BlockArmor.createStrippedWarpedStemLeggings());
                items.add(BlockArmor.createStrippedWarpedStemBoots());
                // Block armors - Planks
                items.add(BlockArmor.createSprucePlanksHelmet());
                items.add(BlockArmor.createSprucePlanksChestplate());
                items.add(BlockArmor.createSprucePlanksLeggings());
                items.add(BlockArmor.createSprucePlanksBoots());
                items.add(BlockArmor.createBirchPlanksHelmet());
                items.add(BlockArmor.createBirchPlanksChestplate());
                items.add(BlockArmor.createBirchPlanksLeggings());
                items.add(BlockArmor.createBirchPlanksBoots());
                items.add(BlockArmor.createJunglePlanksHelmet());
                items.add(BlockArmor.createJunglePlanksChestplate());
                items.add(BlockArmor.createJunglePlanksLeggings());
                items.add(BlockArmor.createJunglePlanksBoots());
                items.add(BlockArmor.createAcaciaPlanksHelmet());
                items.add(BlockArmor.createAcaciaPlanksChestplate());
                items.add(BlockArmor.createAcaciaPlanksLeggings());
                items.add(BlockArmor.createAcaciaPlanksBoots());
                items.add(BlockArmor.createDarkOakPlanksHelmet());
                items.add(BlockArmor.createDarkOakPlanksChestplate());
                items.add(BlockArmor.createDarkOakPlanksLeggings());
                items.add(BlockArmor.createDarkOakPlanksBoots());
                items.add(BlockArmor.createMangrovePlanksHelmet());
                items.add(BlockArmor.createMangrovePlanksChestplate());
                items.add(BlockArmor.createMangrovePlanksLeggings());
                items.add(BlockArmor.createMangrovePlanksBoots());
                items.add(BlockArmor.createCherryPlanksHelmet());
                items.add(BlockArmor.createCherryPlanksChestplate());
                items.add(BlockArmor.createCherryPlanksLeggings());
                items.add(BlockArmor.createCherryPlanksBoots());
                items.add(BlockArmor.createBambooPlanksHelmet());
                items.add(BlockArmor.createBambooPlanksChestplate());
                items.add(BlockArmor.createBambooPlanksLeggings());
                items.add(BlockArmor.createBambooPlanksBoots());
                items.add(BlockArmor.createCrimsonPlanksHelmet());
                items.add(BlockArmor.createCrimsonPlanksChestplate());
                items.add(BlockArmor.createCrimsonPlanksLeggings());
                items.add(BlockArmor.createCrimsonPlanksBoots());
                items.add(BlockArmor.createWarpedPlanksHelmet());
                items.add(BlockArmor.createWarpedPlanksChestplate());
                items.add(BlockArmor.createWarpedPlanksLeggings());
                items.add(BlockArmor.createWarpedPlanksBoots());
                items.add(BlockArmor.createOakPlanksHelmet());
                items.add(BlockArmor.createOakPlanksChestplate());
                items.add(BlockArmor.createOakPlanksLeggings());
                items.add(BlockArmor.createOakPlanksBoots());
                // Block armors - Stone/Mud/Logs
                items.add(BlockArmor.createOakLogHelmet());
                items.add(BlockArmor.createOakLogChestplate());
                items.add(BlockArmor.createOakLogLeggings());
                items.add(BlockArmor.createOakLogBoots());
                items.add(BlockArmor.createSpruceLogHelmet());
                items.add(BlockArmor.createSpruceLogChestplate());
                items.add(BlockArmor.createSpruceLogLeggings());
                items.add(BlockArmor.createSpruceLogBoots());
                items.add(BlockArmor.createBirchLogHelmet());
                items.add(BlockArmor.createBirchLogChestplate());
                items.add(BlockArmor.createBirchLogLeggings());
                items.add(BlockArmor.createBirchLogBoots());
                items.add(BlockArmor.createStoneBricksHelmet());
                items.add(BlockArmor.createStoneBricksChestplate());
                items.add(BlockArmor.createStoneBricksLeggings());
                items.add(BlockArmor.createStoneBricksBoots());
                items.add(BlockArmor.createCobblestoneHelmet());
                items.add(BlockArmor.createCobblestoneChestplate());
                items.add(BlockArmor.createCobblestoneLeggings());
                items.add(BlockArmor.createCobblestoneBoots());
                items.add(BlockArmor.createMossyCobblestoneHelmet());
                items.add(BlockArmor.createMossyCobblestoneChestplate());
                items.add(BlockArmor.createMossyCobblestoneLeggings());
                items.add(BlockArmor.createMossyCobblestoneBoots());
                items.add(BlockArmor.createCobbledDeepslateHelmet());
                items.add(BlockArmor.createCobbledDeepslateChestplate());
                items.add(BlockArmor.createCobbledDeepslateLeggings());
                items.add(BlockArmor.createCobbledDeepslateBoots());
                items.add(BlockArmor.createMudBricksHelmet());
                items.add(BlockArmor.createMudBricksChestplate());
                items.add(BlockArmor.createMudBricksLeggings());
                items.add(BlockArmor.createMudBricksBoots());
                items.add(BlockArmor.createMangroveRootsHelmet());
                items.add(BlockArmor.createMangroveRootsChestplate());
                items.add(BlockArmor.createMangroveRootsLeggings());
                items.add(BlockArmor.createMangroveRootsBoots());
                items.add(BlockArmor.createMuddyMangroveRootsHelmet());
                items.add(BlockArmor.createMuddyMangroveRootsChestplate());
                items.add(BlockArmor.createMuddyMangroveRootsLeggings());
                items.add(BlockArmor.createMuddyMangroveRootsBoots());
                // Block armors - Copper/Netherite/Froglight/Basalt
                items.add(BlockArmor.createNetheriteBlockHelmet());
                items.add(BlockArmor.createNetheriteBlockChestplate());
                items.add(BlockArmor.createNetheriteBlockLeggings());
                items.add(BlockArmor.createNetheriteBlockBoots());
                items.add(BlockArmor.createChiseledCopperHelmet());
                items.add(BlockArmor.createChiseledCopperChestplate());
                items.add(BlockArmor.createChiseledCopperLeggings());
                items.add(BlockArmor.createChiseledCopperBoots());
                items.add(BlockArmor.createCutCopperHelmet());
                items.add(BlockArmor.createCutCopperChestplate());
                items.add(BlockArmor.createCutCopperLeggings());
                items.add(BlockArmor.createCutCopperBoots());
                items.add(BlockArmor.createExposedCopperHelmet());
                items.add(BlockArmor.createExposedCopperChestplate());
                items.add(BlockArmor.createExposedCopperLeggings());
                items.add(BlockArmor.createExposedCopperBoots());
                items.add(BlockArmor.createWeatheredCopperHelmet());
                items.add(BlockArmor.createWeatheredCopperChestplate());
                items.add(BlockArmor.createWeatheredCopperLeggings());
                items.add(BlockArmor.createWeatheredCopperBoots());
                items.add(BlockArmor.createOxidisedCopperHelmet());
                items.add(BlockArmor.createOxidisedCopperChestplate());
                items.add(BlockArmor.createOxidisedCopperLeggings());
                items.add(BlockArmor.createOxidisedCopperBoots());
                items.add(BlockArmor.createWaxedCutCopperHelmet());
                items.add(BlockArmor.createWaxedCutCopperChestplate());
                items.add(BlockArmor.createWaxedCutCopperLeggings());
                items.add(BlockArmor.createWaxedCutCopperBoots());
                items.add(BlockArmor.createPolishedBasaltHelmet());
                items.add(BlockArmor.createPolishedBasaltChestplate());
                items.add(BlockArmor.createPolishedBasaltLeggings());
                items.add(BlockArmor.createPolishedBasaltBoots());
                items.add(BlockArmor.createVerdantFroglightHelmet());
                items.add(BlockArmor.createVerdantFroglightChestplate());
                items.add(BlockArmor.createVerdantFroglightLeggings());
                items.add(BlockArmor.createVerdantFroglightBoots());
                items.add(BlockArmor.createPearlescentFroglightHelmet());
                items.add(BlockArmor.createPearlescentFroglightChestplate());
                items.add(BlockArmor.createPearlescentFroglightLeggings());
                items.add(BlockArmor.createPearlescentFroglightBoots());
                items.add(BlockArmor.createOchreFroglightHelmet());
                items.add(BlockArmor.createOchreFroglightChestplate());
                items.add(BlockArmor.createOchreFroglightLeggings());
                items.add(BlockArmor.createOchreFroglightBoots());
                // Block armors - Material Items
                items.add(BlockArmor.createIronNuggetHelmet());
                items.add(BlockArmor.createIronNuggetChestplate());
                items.add(BlockArmor.createIronNuggetLeggings());
                items.add(BlockArmor.createIronNuggetBoots());
                items.add(BlockArmor.createGoldNuggetHelmet());
                items.add(BlockArmor.createGoldNuggetChestplate());
                items.add(BlockArmor.createGoldNuggetLeggings());
                items.add(BlockArmor.createGoldNuggetBoots());
                items.add(BlockArmor.createCopperIngotHelmet());
                items.add(BlockArmor.createCopperIngotChestplate());
                items.add(BlockArmor.createCopperIngotLeggings());
                items.add(BlockArmor.createCopperIngotBoots());
                items.add(BlockArmor.createEmeraldHelmet());
                items.add(BlockArmor.createEmeraldChestplate());
                items.add(BlockArmor.createEmeraldLeggings());
                items.add(BlockArmor.createEmeraldBoots());
                items.add(BlockArmor.createLapisLazuliHelmet());
                items.add(BlockArmor.createLapisLazuliChestplate());
                items.add(BlockArmor.createLapisLazuliLeggings());
                items.add(BlockArmor.createLapisLazuliBoots());
                items.add(BlockArmor.createRedstoneHelmet());
                items.add(BlockArmor.createRedstoneChestplate());
                items.add(BlockArmor.createRedstoneLeggings());
                items.add(BlockArmor.createRedstoneBoots());
                items.add(BlockArmor.createQuartzHelmet());
                items.add(BlockArmor.createQuartzChestplate());
                items.add(BlockArmor.createQuartzLeggings());
                items.add(BlockArmor.createQuartzBoots());
                items.add(BlockArmor.createAmethystShardHelmet());
                items.add(BlockArmor.createAmethystShardChestplate());
                items.add(BlockArmor.createAmethystShardLeggings());
                items.add(BlockArmor.createAmethystShardBoots());
                items.add(BlockArmor.createFlintHelmet());
                items.add(BlockArmor.createFlintChestplate());
                items.add(BlockArmor.createFlintLeggings());
                items.add(BlockArmor.createFlintBoots());
                items.add(BlockArmor.createBoneMealHelmet());
                items.add(BlockArmor.createBoneMealChestplate());
                items.add(BlockArmor.createBoneMealLeggings());
                items.add(BlockArmor.createBoneMealBoots());
                // Block armors - Charcoal/Coal
                items.add(BlockArmor.createCharcoalHelmet());
                items.add(BlockArmor.createCharcoalChestplate());
                items.add(BlockArmor.createCharcoalLeggings());
                items.add(BlockArmor.createCharcoalBoots());
                // Block armors - End Stone/Snow Block
                items.add(BlockArmor.createEndstoneHelmet());
                items.add(BlockArmor.createEndstoneChestplate());
                items.add(BlockArmor.createEndstoneLeggings());
                items.add(BlockArmor.createEndstoneBoots());
                items.add(BlockArmor.createSnowBlockHelmet());
                items.add(BlockArmor.createSnowBlockChestplate());
                items.add(BlockArmor.createSnowBlockLeggings());
                items.add(BlockArmor.createSnowBlockBoots());
                // Block armors - New 25 materials
                items.add(BlockArmor.createRawPrismarineHelmet());
                items.add(BlockArmor.createRawPrismarineChestplate());
                items.add(BlockArmor.createRawPrismarineLeggings());
                items.add(BlockArmor.createRawPrismarineBoots());
                items.add(BlockArmor.createClayHelmet());
                items.add(BlockArmor.createClayChestplate());
                items.add(BlockArmor.createClayLeggings());
                items.add(BlockArmor.createClayBoots());
                items.add(BlockArmor.createGlazedTerracottaHelmet());
                items.add(BlockArmor.createGlazedTerracottaChestplate());
                items.add(BlockArmor.createGlazedTerracottaLeggings());
                items.add(BlockArmor.createGlazedTerracottaBoots());
                items.add(BlockArmor.createConcreteHelmet());
                items.add(BlockArmor.createConcreteChestplate());
                items.add(BlockArmor.createConcreteLeggings());
                items.add(BlockArmor.createConcreteBoots());
                items.add(BlockArmor.createConcretePowderHelmet());
                items.add(BlockArmor.createConcretePowderChestplate());
                items.add(BlockArmor.createConcretePowderLeggings());
                items.add(BlockArmor.createConcretePowderBoots());
                items.add(BlockArmor.createBookshelfHelmet());
                items.add(BlockArmor.createBookshelfChestplate());
                items.add(BlockArmor.createBookshelfLeggings());
                items.add(BlockArmor.createBookshelfBoots());
                items.add(BlockArmor.createBarrelHelmet());
                items.add(BlockArmor.createBarrelChestplate());
                items.add(BlockArmor.createBarrelLeggings());
                items.add(BlockArmor.createBarrelBoots());
                items.add(BlockArmor.createSmithingTableHelmet());
                items.add(BlockArmor.createSmithingTableChestplate());
                items.add(BlockArmor.createSmithingTableLeggings());
                items.add(BlockArmor.createSmithingTableBoots());
                items.add(BlockArmor.createAnvilHelmet());
                items.add(BlockArmor.createAnvilChestplate());
                items.add(BlockArmor.createAnvilLeggings());
                items.add(BlockArmor.createAnvilBoots());
                items.add(BlockArmor.createGrindstoneHelmet());
                items.add(BlockArmor.createGrindstoneChestplate());
                items.add(BlockArmor.createGrindstoneLeggings());
                items.add(BlockArmor.createGrindstoneBoots());
                items.add(BlockArmor.createLecternHelmet());
                items.add(BlockArmor.createLecternChestplate());
                items.add(BlockArmor.createLecternLeggings());
                items.add(BlockArmor.createLecternBoots());
                items.add(BlockArmor.createComposterHelmet());
                items.add(BlockArmor.createComposterChestplate());
                items.add(BlockArmor.createComposterLeggings());
                items.add(BlockArmor.createComposterBoots());
                items.add(BlockArmor.createStonecutterHelmet());
                items.add(BlockArmor.createStonecutterChestplate());
                items.add(BlockArmor.createStonecutterLeggings());
                items.add(BlockArmor.createStonecutterBoots());
                items.add(BlockArmor.createDriedKelpHelmet());
                items.add(BlockArmor.createDriedKelpChestplate());
                items.add(BlockArmor.createDriedKelpLeggings());
                items.add(BlockArmor.createDriedKelpBoots());
                items.add(BlockArmor.createCoralBlockHelmet());
                items.add(BlockArmor.createCoralBlockChestplate());
                items.add(BlockArmor.createCoralBlockLeggings());
                items.add(BlockArmor.createCoralBlockBoots());
                items.add(BlockArmor.createLanternHelmet());
                items.add(BlockArmor.createLanternChestplate());
                items.add(BlockArmor.createLanternLeggings());
                items.add(BlockArmor.createLanternBoots());
                items.add(BlockArmor.createChainHelmet());
                items.add(BlockArmor.createChainChestplate());
                items.add(BlockArmor.createChainLeggings());
                items.add(BlockArmor.createChainBoots());
                items.add(BlockArmor.createLightningRodHelmet());
                items.add(BlockArmor.createLightningRodChestplate());
                items.add(BlockArmor.createLightningRodLeggings());
                items.add(BlockArmor.createLightningRodBoots());
                items.add(BlockArmor.createTintedGlassHelmet());
                items.add(BlockArmor.createTintedGlassChestplate());
                items.add(BlockArmor.createTintedGlassLeggings());
                items.add(BlockArmor.createTintedGlassBoots());
                items.add(BlockArmor.createCandleHelmet());
                items.add(BlockArmor.createCandleChestplate());
                items.add(BlockArmor.createCandleLeggings());
                items.add(BlockArmor.createCandleBoots());
                items.add(BlockArmor.createSculkSensorHelmet());
                items.add(BlockArmor.createSculkSensorChestplate());
                items.add(BlockArmor.createSculkSensorLeggings());
                items.add(BlockArmor.createSculkSensorBoots());
                items.add(BlockArmor.createSculkCatalystHelmet());
                items.add(BlockArmor.createSculkCatalystChestplate());
                items.add(BlockArmor.createSculkCatalystLeggings());
                items.add(BlockArmor.createSculkCatalystBoots());
                items.add(BlockArmor.createSculkShriekerHelmet());
                items.add(BlockArmor.createSculkShriekerChestplate());
                items.add(BlockArmor.createSculkShriekerLeggings());
                items.add(BlockArmor.createSculkShriekerBoots());
                items.add(BlockArmor.createBrewingStandHelmet());
                items.add(BlockArmor.createBrewingStandChestplate());
                items.add(BlockArmor.createBrewingStandLeggings());
                items.add(BlockArmor.createBrewingStandBoots());
                items.add(BlockArmor.createFurnaceHelmet());
                items.add(BlockArmor.createFurnaceChestplate());
                items.add(BlockArmor.createFurnaceLeggings());
                items.add(BlockArmor.createFurnaceBoots());
                // Block Weapons (matching sets)
                items.add(BlockWeapons.createGlassSword());
                items.add(BlockWeapons.createObsidianSword());
                items.add(BlockWeapons.createQuartzSword());
                items.add(BlockWeapons.createGlowstoneSword());
                items.add(BlockWeapons.createRedstoneSword());
                items.add(BlockWeapons.createNetherrackSword());
                items.add(BlockWeapons.createEndstoneSword());
                items.add(BlockWeapons.createIceSword());
                items.add(BlockWeapons.createPrismarineSword());
                items.add(BlockWeapons.createTerracottaSword());
                items.add(BlockWeapons.createMossySword());
                items.add(BlockWeapons.createSoulSandSword());
                items.add(BlockWeapons.createMagmaSword());
                items.add(BlockWeapons.createSandstoneSword());
                items.add(BlockWeapons.createAmethystSword());
                items.add(BlockWeapons.createCoalSword());
                // 36+ NEW BLOCK SWORDS
                items.add(BlockWeapons.createDiamondBlockSword());
                items.add(BlockWeapons.createEmeraldBlockSword());
                items.add(BlockWeapons.createGoldBlockSword());
                items.add(BlockWeapons.createIronBlockSword());
                items.add(BlockWeapons.createLapisBlockSword());
                items.add(BlockWeapons.createCopperBlockSword());
                items.add(BlockWeapons.createAncientDebrisSword());
                items.add(BlockWeapons.createBasaltSword());
                items.add(BlockWeapons.createBlackstoneSword());
                items.add(BlockWeapons.createBoneBlockSword());
                items.add(BlockWeapons.createBrickSword());
                items.add(BlockWeapons.createCactusSword());
                items.add(BlockWeapons.createCalciteSword());
                items.add(BlockWeapons.createDeepslateSword());
                items.add(BlockWeapons.createDripstoneSword());
                items.add(BlockWeapons.createHaySword());
                items.add(BlockWeapons.createHoneycombSword());
                items.add(BlockWeapons.createLilyPadSword());
                items.add(BlockWeapons.createMelonSword());
                items.add(BlockWeapons.createMossBlockSword());
                items.add(BlockWeapons.createMyceliumSword());
                items.add(BlockWeapons.createNetherBrickSword());
                items.add(BlockWeapons.createPumpkinSword());
                items.add(BlockWeapons.createPurpurSword());
                items.add(BlockWeapons.createSandSword());
                items.add(BlockWeapons.createSculkSword());
                items.add(BlockWeapons.createShroomlightSword());
                items.add(BlockWeapons.createSlimeSword());
                items.add(BlockWeapons.createSmoothStoneSword());
                items.add(BlockWeapons.createSnowSword());
                items.add(BlockWeapons.createSoulSoilSword());
                items.add(BlockWeapons.createSpongeSword());
                items.add(BlockWeapons.createTargetSword());
                items.add(BlockWeapons.createTntSword());
                items.add(BlockWeapons.createWarpedSword());
                items.add(BlockWeapons.createWetSpongeSword());
                // NEW STRIPPED LOG SWORDS
                items.add(BlockWeapons.createStrippedSpruceLogSword());
                items.add(BlockWeapons.createStrippedBirchLogSword());
                items.add(BlockWeapons.createStrippedDarkOakLogSword());
                items.add(BlockWeapons.createStrippedJungleLogSword());
                items.add(BlockWeapons.createStrippedAcaciaLogSword());
                items.add(BlockWeapons.createStrippedMangroveLogSword());
                items.add(BlockWeapons.createStrippedCherryLogSword());
                items.add(BlockWeapons.createStrippedBambooBlockSword());
                items.add(BlockWeapons.createStrippedCrimsonStemSword());
                items.add(BlockWeapons.createStrippedWarpedStemSword());
                // NEW PLANK SWORDS
                items.add(BlockWeapons.createSprucePlanksSword());
                items.add(BlockWeapons.createBirchPlanksSword());
                items.add(BlockWeapons.createJunglePlanksSword());
                items.add(BlockWeapons.createAcaciaPlanksSword());
                items.add(BlockWeapons.createDarkOakPlanksSword());
                items.add(BlockWeapons.createMangrovePlanksSword());
                items.add(BlockWeapons.createCherryPlanksSword());
                items.add(BlockWeapons.createBambooPlanksSword());
                items.add(BlockWeapons.createCrimsonPlanksSword());
                items.add(BlockWeapons.createWarpedPlanksSword());
                items.add(BlockWeapons.createOakPlanksSword());
                // NEW STONE/MUD/LOG SWORDS
                items.add(BlockWeapons.createOakLogSword());
                items.add(BlockWeapons.createSpruceLogSword());
                items.add(BlockWeapons.createBirchLogSword());
                items.add(BlockWeapons.createStoneBricksSword());
                items.add(BlockWeapons.createCobblestoneSword());
                items.add(BlockWeapons.createMossyCobblestoneSword());
                items.add(BlockWeapons.createCobbledDeepslateSword());
                items.add(BlockWeapons.createMudBricksSword());
                items.add(BlockWeapons.createMangroveRootsSword());
                items.add(BlockWeapons.createMuddyMangroveRootsSword());
                // NEW COPPER/NETHERITE/FROGLIGHT SWORDS
                items.add(BlockWeapons.createNetheriteBlockSword());
                items.add(BlockWeapons.createChiseledCopperSword());
                items.add(BlockWeapons.createCutCopperSword());
                items.add(BlockWeapons.createExposedCopperSword());
                items.add(BlockWeapons.createWeatheredCopperSword());
                items.add(BlockWeapons.createOxidisedCopperSword());
                items.add(BlockWeapons.createWaxedCutCopperSword());
                items.add(BlockWeapons.createPolishedBasaltSword());
                items.add(BlockWeapons.createVerdantFroglightSword());
                items.add(BlockWeapons.createPearlescentFroglightSword());
                items.add(BlockWeapons.createOchreFroglightSword());
                // NEW MATERIAL ITEM SWORDS
                items.add(BlockWeapons.createIronNuggetSword());
                items.add(BlockWeapons.createGoldNuggetSword());
                items.add(BlockWeapons.createCopperIngotSword());
                items.add(BlockWeapons.createEmeraldSword());
                items.add(BlockWeapons.createLapisLazuliSword());
                items.add(BlockWeapons.createAmethystShardSword());
                items.add(BlockWeapons.createFlintSword());
                items.add(BlockWeapons.createBoneMealSword());
                items.add(BlockWeapons.createCharcoalSword());
                items.add(BlockWeapons.createEndstoneSword());
                items.add(BlockWeapons.createSnowBlockSword());
                // New 25 material swords
                items.add(BlockWeapons.createRawPrismarineSword());
                items.add(BlockWeapons.createClaySword());
                items.add(BlockWeapons.createGlazedTerracottaSword());
                items.add(BlockWeapons.createConcreteSword());
                items.add(BlockWeapons.createConcretePowderSword());
                items.add(BlockWeapons.createBookshelfSword());
                items.add(BlockWeapons.createBarrelSword());
                items.add(BlockWeapons.createSmithingTableSword());
                items.add(BlockWeapons.createAnvilSword());
                items.add(BlockWeapons.createGrindstoneSword());
                items.add(BlockWeapons.createLecternSword());
                items.add(BlockWeapons.createComposterSword());
                items.add(BlockWeapons.createStonecutterSword());
                items.add(BlockWeapons.createDriedKelpSword());
                items.add(BlockWeapons.createCoralBlockSword());
                items.add(BlockWeapons.createLanternSword());
                items.add(BlockWeapons.createChainSword());
                items.add(BlockWeapons.createLightningRodSword());
                items.add(BlockWeapons.createTintedGlassSword());
                items.add(BlockWeapons.createCandleSword());
                items.add(BlockWeapons.createSculkSensorSword());
                items.add(BlockWeapons.createSculkCatalystSword());
                items.add(BlockWeapons.createSculkShriekerSword());
                items.add(BlockWeapons.createBrewingStandSword());
                items.add(BlockWeapons.createFurnaceSword());
            }
            case SWORDS_T1 -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values())
                    items.add(SlayerItems.createSlayerSword(t));
            }
            case SWORDS_T2 -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values())
                    items.add(SlayerItems.createUpgradedSlayerSword(t));
            }
            case ARMOR_T1 -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                    items.add(SlayerItems.createSlayerHelmet(t, 1));
                    items.add(SlayerItems.createSlayerChestplate(t, 1));
                    items.add(SlayerItems.createSlayerLeggings(t, 1));
                    items.add(SlayerItems.createSlayerBoots(t, 1));
                }
            }
            case ARMOR_T2 -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                    items.add(SlayerItems.createSlayerHelmet(t, 2));
                    items.add(SlayerItems.createSlayerChestplate(t, 2));
                    items.add(SlayerItems.createSlayerLeggings(t, 2));
                    items.add(SlayerItems.createSlayerBoots(t, 2));
                }
            }
            case SPECIAL_ARMOR -> {
                items.add(SlayerItems.createZombieBerserkerHelmet());
                items.add(SlayerItems.createEnderPhaseHelmet());
                items.add(SlayerItems.createSpiderLeggings());
                items.add(SlayerItems.createSkeletonBow());
                items.add(SlayerItems.createSlimeBoots());
                items.add(SlayerItems.createWardenChestplate());
                items.add(SlayerItems.createVoidwalkerCrown());
                items.add(CustomItemHandler.createWardenCore());
            }
            case DRAGON_CHESTPLATES -> {
                items.addAll(DragonChestplateRegistry.allStacks());
                items.addAll(DragonSwords.allStacks());
                items.add(SlayerItems.createDragonCrafterCoreT1());
                items.add(SlayerItems.createDragonCrafterCoreT2());
                items.add(SlayerItems.createDragonCrafterCoreT3());
                items.add(SlayerItems.createDragonCrafterCoreOmega());
            }
            case HPEBM -> {
                for (int mk = 1; mk <= 5; mk++)
                    items.add(CustomItemHandler.createHPEBM(mk));
                items.add(CustomItemHandler.createUltraOverclockedBeam());
            }
            case SPECIAL_ITEMS -> {
                items.add(CustomItemHandler.createTheGavel());
                items.add(CustomItemHandler.createHarveysStick());
                items.add(CustomItemHandler.createHermesShoes());
                items.add(DanielsPickaxe.create());
                items.add(CustomItemHandler.createRiftMirror());
                items.add(CustomItemHandler.createEmberSignet());
                items.add(CustomItemHandler.createFrostboundTalisman());
                items.add(CustomItemHandler.createEchoChime());
                items.add(CustomItemHandler.createTerraRelay());
                for (String sid : SkyblockTrinkets.allIds()) {
                    CustomItemManager.CustomItem ci = CustomItemManager.getItem(sid);
                    if (ci != null) {
                        items.add(ci.createStack(1));
                    }
                }
            }
            case CUSTOM_TOOLS -> {
                // 15 NEW LEGENDARY TOOL SETS
                // Dragon Tools
                items.add(LegendaryTools.createDragonPickaxe());
                items.add(LegendaryTools.createDragonAxe());
                items.add(LegendaryTools.createDragonShovel());
                items.add(LegendaryTools.createDragonHoe());
                // Aether Tools
                items.add(LegendaryTools.createAetherPickaxe());
                items.add(LegendaryTools.createAetherAxe());
                items.add(LegendaryTools.createAetherShovel());
                items.add(LegendaryTools.createAetherHoe());
                // Void Tools
                items.add(LegendaryTools.createVoidPickaxe());
                items.add(LegendaryTools.createVoidAxe());
                items.add(LegendaryTools.createVoidShovel());
                items.add(LegendaryTools.createVoidHoe());
                // Nature Tools
                items.add(LegendaryTools.createNaturePickaxe());
                items.add(LegendaryTools.createNatureAxe());
                items.add(LegendaryTools.createNatureShovel());
                items.add(LegendaryTools.createNatureHoe());
                // Frost Tools
                items.add(LegendaryTools.createFrostPickaxe());
                items.add(LegendaryTools.createFrostAxe());
                items.add(LegendaryTools.createFrostShovel());
                items.add(LegendaryTools.createFrostHoe());
                // Thunder Tools
                items.add(LegendaryTools.createThunderPickaxe());
                items.add(LegendaryTools.createThunderAxe());
                items.add(LegendaryTools.createThunderShovel());
                items.add(LegendaryTools.createThunderHoe());
                // Ocean Tools
                items.add(LegendaryTools.createOceanPickaxe());
                items.add(LegendaryTools.createOceanAxe());
                items.add(LegendaryTools.createOceanShovel());
                items.add(LegendaryTools.createOceanHoe());
                // Lunar Tools
                items.add(LegendaryTools.createLunarPickaxe());
                items.add(LegendaryTools.createLunarAxe());
                items.add(LegendaryTools.createLunarShovel());
                items.add(LegendaryTools.createLunarHoe());
                // Solar Tools
                items.add(LegendaryTools.createSolarPickaxe());
                items.add(LegendaryTools.createSolarAxe());
                items.add(LegendaryTools.createSolarShovel());
                items.add(LegendaryTools.createSolarHoe());
                // Terra Tools
                items.add(LegendaryTools.createTerraPickaxe());
                items.add(LegendaryTools.createTerraAxe());
                items.add(LegendaryTools.createTerraShovel());
                items.add(LegendaryTools.createTerraHoe());
                // Phantom Tools
                items.add(LegendaryTools.createPhantomPickaxe());
                items.add(LegendaryTools.createPhantomAxe());
                items.add(LegendaryTools.createPhantomShovel());
                items.add(LegendaryTools.createPhantomHoe());
                // Blood Tools
                items.add(LegendaryTools.createBloodPickaxe());
                items.add(LegendaryTools.createBloodAxe());
                items.add(LegendaryTools.createBloodShovel());
                items.add(LegendaryTools.createBloodHoe());
                // Celestial Tools
                items.add(LegendaryTools.createCelestialPickaxe());
                items.add(LegendaryTools.createCelestialAxe());
                items.add(LegendaryTools.createCelestialShovel());
                items.add(LegendaryTools.createCelestialHoe());
                // Shadow Tools
                items.add(LegendaryTools.createShadowPickaxe());
                items.add(LegendaryTools.createShadowAxe());
                items.add(LegendaryTools.createShadowShovel());
                items.add(LegendaryTools.createShadowHoe());
                // Crystal Tools
                items.add(LegendaryTools.createCrystalPickaxe());
                items.add(LegendaryTools.createCrystalAxe());
                items.add(LegendaryTools.createCrystalShovel());
                items.add(LegendaryTools.createCrystalHoe());
            }
            case PIGLIN -> {
                items.add(SlayerItems.createCrownOfGreed());
                items.add(SlayerItems.createCrownOfMidas());
                items.add(SlayerItems.createMidasSword());
                items.add(SlayerItems.createPiglinCore());
                items.add(SlayerItems.createPiglinFlesh());
            }
            case GOLD_ARMOR -> {
                // Pure Gold Armor (Tier 1)
                items.add(SlayerItems.createPureGoldHelmet());
                items.add(SlayerItems.createPureGoldChestplate());
                items.add(SlayerItems.createPureGoldLeggings());
                items.add(SlayerItems.createPureGoldBoots());
                
                // Polished Gold Armor (Tier 2)
                items.add(SlayerItems.createPolishedGoldHelmet());
                items.add(SlayerItems.createPolishedGoldChestplate());
                items.add(SlayerItems.createPolishedGoldLeggings());
                items.add(SlayerItems.createPolishedGoldBoots());
                
                // Shiny Gold Armor (Tier 3)
                items.add(SlayerItems.createShinyGoldHelmet());
                items.add(SlayerItems.createShinyGoldChestplate());
                items.add(SlayerItems.createShinyGoldLeggings());
                items.add(SlayerItems.createShinyGoldBoots());
                
                // Glistening Gold Armor (Tier 4)
                items.add(SlayerItems.createGlisteningGoldHelmet());
                items.add(SlayerItems.createGlisteningGoldChestplate());
                items.add(SlayerItems.createGlisteningGoldLeggings());
                items.add(SlayerItems.createGlisteningGoldBoots());
                
                // Gilded Armor (Tier 5)
                items.add(SlayerItems.createGildedHelmet());
                items.add(SlayerItems.createGildedChestplate());
                items.add(SlayerItems.createGildedLeggings());
                items.add(SlayerItems.createGildedBoots());
                
                // Add enchanted blackstone for gilded armor crafting
                items.add(SlayerItems.createEnchantedBlackstone());
                items.add(SlayerItems.createEnchantedGildedBlackstone());
                
                // Add gilded netherite armor (ultimate tier)
                items.add(SlayerItems.createGildedNetheriteHelmet());
                items.add(SlayerItems.createGildedNetheriteChestplate());
                items.add(SlayerItems.createGildedNetheriteLeggings());
                items.add(SlayerItems.createGildedNetheriteBoots());
            }
            case MATERIALS -> {
                // Cores & Chunks
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                    items.add(SlayerItems.createCore(t));
                    items.add(SlayerItems.createChunk(t));
                }
                // Boss Drops - rare items from bosses
                items.add(SlayerItems.createUndeadHeart());
                items.add(SlayerItems.createSpectralQuiver());
                items.add(SlayerItems.createEchoingCore());
                items.add(SlayerItems.createEnderSword());
                items.add(SlayerItems.createAbyssalBlade());
                items.add(SlayerItems.createBouncySlime());
                items.add(SlayerItems.createVenomousDagger());
                // Add attribute cores
                for (com.political.ArmourAttribute attr : com.political.ArmourAttribute.values()) {
                    items.add(SlayerItems.createAttributeCore(attr));
                }
                // Add Armor Crafter Cores
                items.add(SlayerItems.createCustomCrafterCoreT1());
                items.add(SlayerItems.createCustomCrafterCoreT2());
                items.add(SlayerItems.createCustomCrafterCoreT3());
                items.add(SlayerItems.createDragonCrafterCoreT1());
                items.add(SlayerItems.createDragonCrafterCoreT2());
                items.add(SlayerItems.createDragonCrafterCoreT3());
                items.add(CustomItemHandler.createWardenCore());
            }
            case ATTRIBUTE_TOKENS -> {
                // Add all 10 attribute tokens
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.BURNING));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.SIGHTLESS));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.FRENZIED));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.GROUNDED));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.WEBBED));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.FROST));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.PHANTOMSTEP));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.CURSED));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.OVERGROWN));
                items.add(SlayerItems.createAttributeToken(com.political.ArmourAttribute.VOLATILE));
            }
            case WEAPON_ATTRIBUTES -> {
                // Add all weapon attribute tokens
                for (WeaponAttribute attr : WeaponAttribute.values()) {
                    items.add(SlayerItems.createWeaponAttributeToken(attr));
                }
            }
            case BLOCK_TOOLS -> { BlockReflectiveCatalog.addBlockTools(items); }
            case CUSTOM_ARROWS -> {
                // Add all custom arrow types
                for (CustomArrows.ArrowType type : CustomArrows.getAllArrowTypes()) {
                    items.add(CustomArrows.createArrow(type, 1));
                }
            }
            case BOSS_DROPS -> {
                // NEW BOSS DROPS
                items.add(SlayerItems.createUndeadHeart());
                items.add(SlayerItems.createSpectralQuiver());
                items.add(SlayerItems.createEchoingCore());
                items.add(SlayerItems.createEnderSword());
                items.add(SlayerItems.createAbyssalBlade());
                items.add(SlayerItems.createBouncySlime());
                items.add(SlayerItems.createVenomousDagger());
            }
            case REFLECT_BLOCK_WEAPONS -> {
                BlockReflectiveCatalog.addBlockWeapons(items);
                BlockReflectiveCatalog.addBlockArmor(items);
            }
            case REFLECT_BLOCK_ARMOR -> BlockReflectiveCatalog.addBlockArmor(items);
            case ENCHANTED_BLOCK_GEAR -> BlockReflectiveCatalog.addEnchantedBlockGear(items);
            case SECTOR_HEART_GEAR -> {
                for (int t = 1; t <= 10; t++) {
                    items.add(SectorHeartItems.createGardenOveralls(t));
                }
            }
            case DEEPSTRIKE_ITEMS -> items.add(SectorHeartItems.createSyndicateCharter());
            case ASCENSION_MATERIALS -> {
                for (String id : List.of("asc_common_shard", "asc_vein_shard", "asc_blood_fragment", "asc_unstable_star")) {
                    var item = CustomItemManager.getItem(id);
                    if (item != null) items.add(item.createStack(1));
                }
            }
            case ASCENSION_RELICS -> {
                for (String id : List.of("asc_rngesus_essence", "asc_hyper_catalyst", "asc_mf_charm", "asc_void_lottery",
                        "asc_sector_compass", "asc_master_core")) {
                    var item = CustomItemManager.getItem(id);
                    if (item != null) items.add(item.createStack(1));
                }
            }
            case MANAGER_ITEMS -> {
                // Items from CustomItemManager
                for (CustomItemManager.CustomItem item : CustomItemManager.getAllItems()) {
                    items.add(item.createStack(1));
                }
            }
            case MOB_DROPS -> {
                // Bounty Boss Drops
                items.add(SlayerItems.createUndeadHeart());
                items.add(SlayerItems.createSpectralQuiver());
                items.add(SlayerItems.createEchoingCore());
                items.add(SlayerItems.createBouncySlime());
                items.add(SlayerItems.createEnderSword());
                items.add(SlayerItems.createAbyssalBlade());
                items.add(SlayerItems.createVenomousDagger());
                items.add(SlayerItems.createPiglinCore());
                items.add(SlayerItems.createPiglinFlesh());
                items.add(SlayerItems.createVoidwalkerCrown());
                items.add(SlayerItems.createEnderPhaseHelmet());
                items.add(SlayerItems.createZombieBerserkerHelmet());
                items.add(SlayerItems.createSpiderLeggings());
                items.add(SlayerItems.createSkeletonBow());
                items.add(SlayerItems.createSlimeBoots());
                items.add(SlayerItems.createWardenChestplate());
                items.add(SlayerItems.createCrownOfGreed());
                items.add(SlayerItems.createCrownOfMidas());
                items.add(SlayerItems.createMidasSword());
                items.add(CustomItemHandler.createWardenCore());
                // World Boss Trophies
                for (String id : List.of("trophy_hollow_knight", "trophy_mosswarden", "trophy_scorchfiend",
                        "trophy_skulltide_captain", "trophy_nethermaw", "trophy_wailing_phantom",
                        "trophy_crystal_golem", "trophy_blight_mother", "trophy_voidreaver",
                        "trophy_infernal_hydra", "trophy_ancient_one", "trophy_king_of_ashes")) {
                    var ci = CustomItemManager.getItem(id);
                    if (ci != null) items.add(ci.createStack(1));
                }
                for (String id : List.of("sovereign_commendation", "treasury_fragment", "treasury_sigil", "mastery_draught")) {
                    var ci = CustomItemManager.getItem(id);
                    if (ci != null) items.add(ci.createStack(1));
                }
                // Enhanced mob swords
                items.add(SlayerItems.createReinforcedIronSword());
                items.add(SlayerItems.createHardenedDiamondSword());
                items.add(SlayerItems.createVoidTouchedSword());
                items.add(SlayerItems.createSharpenedGoldSword());
                items.add(SlayerItems.createHeavyStoneSword());
                items.add(SlayerItems.createSwiftWoodenSword());
                // Cores, chunks, compacted materials
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                    items.add(SlayerItems.createCore(t));
                    items.add(SlayerItems.createChunk(t));
                }
                items.add(SlayerItems.createCompactedIron());
                items.add(SlayerItems.createCompactedGold());
                items.add(SlayerItems.createCompactedDiamond());
                items.add(SlayerItems.createCompactedEmerald());
                items.add(SlayerItems.createCompactedNetherite());
                items.add(SlayerItems.createCompactedCopper());
                items.add(SlayerItems.createCompactedCoal());
                items.add(SlayerItems.createCompactedLapis());
                items.add(SlayerItems.createCompactedRedstone());
                items.add(SlayerItems.createCompactedQuartz());
                items.add(SlayerItems.createSuperCompactedGold());
                items.add(SlayerItems.createSuperCompactedIron());
                items.add(SlayerItems.createSuperCompactedDiamond());
                items.add(SlayerItems.createSuperCompactedEmerald());
                items.add(SlayerItems.createSuperCompactedNetherite());
            }
            case FISHING_RODS -> {
                items.addAll(FishingRods.allRods());
            }
            case MAGIC_STAVES -> {
                items.addAll(MagicStaff.getAllStaffs());
            }
        }
        return items;
    }

    // ──────────────────────────────────────────────────────────────
    // Page 0: Main Category Menu - Matches RecipesGui layout
    // ──────────────────────────────────────────────────────────────

    /** Entry point — opens the main category selection menu. */
    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Custom Items").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        RecipeGuiTheme.fillDoubleChestCatalog(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Custom Items").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Admin: grant items by category").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Matches the /recipe browser layout").withStyle(ChatFormatting.DARK_GRAY))
                .glow().build());

        gui.setSlot(1, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Component.literal("🔍 Search Items").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Search items by name").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Supports partial matches").withStyle(ChatFormatting.GREEN))
                .setCallback((idx, type, containerInput, slotGui) -> openSearchGui(player))
                .build());

        // Row 1: Armor categories - spread across more slots
        gui.setSlot(10, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("💎 Custom Armor").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Emerald, Lapis armors").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCategoryPage(player, Category.CUSTOM_ARMOR, 0))
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.DRAGON_HEAD)
                .setName(Component.literal("🐉 Dragon chestplates").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("All draconic plates").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Dragon crafting cores (T1–T3 + Omega)").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCategoryPage(player, Category.DRAGON_CHESTPLATES, 0))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("⚔ Bounty Sets").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("T1 & T2 Armor + Swords").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openBountySetsPage(player, 0,
                        (p, item) -> giveItem(p, item),
                        CustomItemsGui::open))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Component.literal("🛡 Special Armor").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Legendary armor").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCategoryPage(player, Category.SPECIAL_ARMOR, 0))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.GOLDEN_CHESTPLATE)
                .setName(Component.literal("👑 Gold Armor").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Gold armor tiers").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCategoryPage(player, Category.GOLD_ARMOR, 0))
                .build());

        gui.setSlot(28, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("👑 Piglin Items").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Piglin-themed items").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCategoryPage(player, Category.PIGLIN, 0))
                .build());

        // Row 2: Weapons and Tools
        gui.setSlot(22, new GuiElementBuilder(Items.NETHERITE_PICKAXE)
                .setName(Component.literal("⛏ Tool Sets").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Block + Legendary tools").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openToolSetsPage(player, 0,
                        (p, item) -> giveItem(p, item),
                        CustomItemsGui::open))
                .build());

        gui.setSlot(23, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal("🎁 Boss Drops").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Rare boss drop items").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("💀 Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCategoryPage(player, Category.BOSS_DROPS, 0))
                .build());

        gui.setSlot(24, new GuiElementBuilder(Items.STICK)
                .setName(Component.literal("🎁 Special Items").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Special unique items").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCategoryPage(player, Category.SPECIAL_ITEMS, 0))
                .build());

        // Row 3: Materials and Tokens
        gui.setSlot(30, new GuiElementBuilder(Items.SLIME_BALL)
                .setName(Component.literal("📦 Materials").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Cores & chunks").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCategoryPage(player, Category.MATERIALS, 0))
                .build());

        // Row 4: Additional categories
        gui.setSlot(38, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("✧ Armor Tokens").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Armor attributes").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCategoryPage(player, Category.ATTRIBUTE_TOKENS, 0))
                .build());

        gui.setSlot(40, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("⚔ Weapon Tokens").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Weapon attributes").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCategoryPage(player, Category.WEAPON_ATTRIBUTES, 0))
                .build());

        gui.setSlot(42, new GuiElementBuilder(Items.BLAZE_ROD)
                .setName(Component.literal("⚡ HPEBM Weapons").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Energy beam weapons").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCategoryPage(player, Category.HPEBM, 0))
                .build());

        gui.setSlot(44, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("⭐ Manager Items").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Skill Tomes, Tokens, misc").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCategoryPage(player, Category.MANAGER_ITEMS, 0))
                .build());

        gui.setSlot(34, new GuiElementBuilder(Category.MOB_DROPS.icon)
                .setName(Component.literal(Category.MOB_DROPS.label).withStyle(Category.MOB_DROPS.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Bounty, World Boss & Enhanced drops").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("💀 Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMobDropsHub(player))
                .build());

        gui.setSlot(36, new GuiElementBuilder(Category.CUSTOM_ARROWS.icon)
                .setName(Component.literal(Category.CUSTOM_ARROWS.label).withStyle(Category.CUSTOM_ARROWS.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("All custom arrow types").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCategoryPage(player, Category.CUSTOM_ARROWS, 0))
                .build());

        gui.setSlot(46, new GuiElementBuilder(Category.ASCENSION_MATERIALS.icon)
                .setName(Component.literal(Category.ASCENSION_MATERIALS.label).withStyle(Category.ASCENSION_MATERIALS.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Shards & fragments for ascension").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCategoryPage(player, Category.ASCENSION_MATERIALS, 0))
                .build());

        gui.setSlot(48, new GuiElementBuilder(Category.ASCENSION_RELICS.icon)
                .setName(Component.literal(Category.ASCENSION_RELICS.label).withStyle(Category.ASCENSION_RELICS.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Rare relics for ascension").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCategoryPage(player, Category.ASCENSION_RELICS, 0))
                .build());

        gui.setSlot(50, new GuiElementBuilder(Items.FISHING_ROD)
                .setName(Component.literal("🎣 Fishing Rods").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("All custom fishing rods").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openCategoryPage(player, Category.FISHING_RODS, 0))
                .build());

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((slot, clicktype, containerInput, slotGui) -> gui.close())
                .build());

        gui.open();
    }

    private static void openMobDropsHub(ServerPlayer player) {
        openMobDropsHub(player, (p, item) -> giveItem(p, item), CustomItemsGui::open);
    }

    public static void openMobDropsHub(ServerPlayer player, BountyItemClickHandler onItemClick, BackHandler onBack) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Mob drops hub").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        RecipeGuiTheme.fillChest4Hub(gui);

        gui.setSlot(10, new GuiElementBuilder(Items.ROTTEN_FLESH)
                .setName(Component.literal("⚔ Bounty Boss Drops").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Unique drops from slayer bosses").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Zombie, Spider, Skeleton, Slime...").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMobDropSubPage(player, "bounty_boss", 0, onItemClick, onBack))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal("🏆 World Boss Drops").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Trophies & rewards from world bosses").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Hollow Knight, Voidreaver, etc.").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMobDropSubPage(player, "world_boss", 0, onItemClick, onBack))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("⚡ Enhanced Mob Drops").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Drops from scaled/enhanced mobs").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Reinforced swords, special gear").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMobDropSubPage(player, "enhanced", 0, onItemClick, onBack))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("📦 General Mob Materials").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Crafting materials from mobs").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Compacted ores, cores, chunks").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMobDropSubPage(player, "general_materials", 0, onItemClick, onBack))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.PRISMARINE_SHARD)
                .setName(Component.literal("⬆ Ascension Items").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Shards, fragments & relics").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Sector ascension materials").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMobDropSubPage(player, "ascension", 0, onItemClick, onBack))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> onBack.open(player))
                .build());
        gui.open();
    }

    public static List<ItemStack> getMobDropSubItems(String subCategory) {
        List<ItemStack> items = new ArrayList<>();
        switch (subCategory) {
            case "bounty_boss" -> {
                items.add(SlayerItems.createUndeadHeart());
                items.add(SlayerItems.createSpectralQuiver());
                items.add(SlayerItems.createEchoingCore());
                items.add(SlayerItems.createBouncySlime());
                items.add(SlayerItems.createEnderSword());
                items.add(SlayerItems.createAbyssalBlade());
                items.add(SlayerItems.createVenomousDagger());
                items.add(SlayerItems.createPiglinCore());
                items.add(SlayerItems.createPiglinFlesh());
                items.add(SlayerItems.createVoidwalkerCrown());
                items.add(SlayerItems.createEnderPhaseHelmet());
                items.add(SlayerItems.createZombieBerserkerHelmet());
                items.add(SlayerItems.createSpiderLeggings());
                items.add(SlayerItems.createSkeletonBow());
                items.add(SlayerItems.createSlimeBoots());
                items.add(SlayerItems.createWardenChestplate());
                items.add(SlayerItems.createCrownOfGreed());
                items.add(SlayerItems.createCrownOfMidas());
                items.add(SlayerItems.createMidasSword());
                items.add(CustomItemHandler.createWardenCore());
            }
            case "world_boss" -> {
                for (String id : List.of("trophy_hollow_knight", "trophy_mosswarden", "trophy_scorchfiend",
                        "trophy_skulltide_captain", "trophy_nethermaw", "trophy_wailing_phantom",
                        "trophy_crystal_golem", "trophy_blight_mother", "trophy_voidreaver",
                        "trophy_infernal_hydra", "trophy_ancient_one", "trophy_king_of_ashes")) {
                    var ci = CustomItemManager.getItem(id);
                    if (ci != null) items.add(ci.createStack(1));
                }
                for (String id : List.of("sovereign_commendation", "treasury_fragment", "treasury_sigil", "mastery_draught")) {
                    var ci = CustomItemManager.getItem(id);
                    if (ci != null) items.add(ci.createStack(1));
                }
            }
            case "enhanced" -> {
                items.add(SlayerItems.createReinforcedIronSword());
                items.add(SlayerItems.createHardenedDiamondSword());
                items.add(SlayerItems.createVoidTouchedSword());
                items.add(SlayerItems.createSharpenedGoldSword());
                items.add(SlayerItems.createHeavyStoneSword());
                items.add(SlayerItems.createSwiftWoodenSword());
            }
            case "general_materials" -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                    items.add(SlayerItems.createCore(t));
                    items.add(SlayerItems.createChunk(t));
                }
                items.add(SlayerItems.createCompactedIron());
                items.add(SlayerItems.createCompactedGold());
                items.add(SlayerItems.createCompactedDiamond());
                items.add(SlayerItems.createCompactedEmerald());
                items.add(SlayerItems.createCompactedNetherite());
                items.add(SlayerItems.createCompactedCopper());
                items.add(SlayerItems.createCompactedCoal());
                items.add(SlayerItems.createCompactedLapis());
                items.add(SlayerItems.createCompactedRedstone());
                items.add(SlayerItems.createCompactedQuartz());
                items.add(SlayerItems.createSuperCompactedGold());
                items.add(SlayerItems.createSuperCompactedIron());
                items.add(SlayerItems.createSuperCompactedDiamond());
                items.add(SlayerItems.createSuperCompactedEmerald());
                items.add(SlayerItems.createSuperCompactedNetherite());
            }
            case "ascension" -> {
                for (String id : List.of("asc_common_shard", "asc_vein_shard", "asc_blood_fragment",
                        "asc_unstable_star", "asc_rngesus_essence", "asc_hyper_catalyst",
                        "asc_mf_charm", "asc_void_lottery", "asc_sector_compass", "asc_master_core")) {
                    var ci = CustomItemManager.getItem(id);
                    if (ci != null) items.add(ci.createStack(1));
                }
            }
        }
        return items;
    }

    public static void openMobDropSubPage(ServerPlayer player, String subCategory, int page,
                                           BountyItemClickHandler onItemClick, BackHandler onBack) {
        List<ItemStack> items = getMobDropSubItems(subCategory);

        String title = switch (subCategory) {
            case "bounty_boss" -> "⚔ Bounty Boss Drops";
            case "world_boss" -> "🏆 World Boss Drops";
            case "enhanced" -> "⚡ Enhanced Mob Drops";
            case "general_materials" -> "📦 General Mob Materials";
            case "ascension" -> "⬆ Ascension Items";
            default -> "💀 Mob Drops";
        };

        final int ITEMS_PER_PAGE = 28;
        int totalPages = Math.max(1, (items.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        int clampedPage = Math.min(page, totalPages - 1);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(title).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

        RecipeGuiTheme.fillCategoryItemGrid(gui, false);

        boolean[] used = new boolean[54];

        gui.setSlot(4, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal(title).withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click an item to receive it").withStyle(ChatFormatting.GRAY))
                .glow().build());
        used[4] = true;

        int start = clampedPage * ITEMS_PER_PAGE;
        int shown = 0;
        for (int idx = start; idx < items.size() && shown < ITEMS_PER_PAGE; idx++) {
            ItemStack item = items.get(idx);
            int row = shown / 7;
            int col = shown % 7 + 1;
            int slot = (row + 1) * 9 + col;

            Component displayName = item.get(DataComponents.CUSTOM_NAME);
            String customId = SlayerItems.getCustomItemId(item);

            final ItemStack finalItem = item;
            ItemStack displayStack = CustomItemLore.forSelectPageIcon(item);
            GuiElementBuilder elem = new GuiElementBuilder(displayStack)
                    .setName(displayName != null ? displayName
                            : Component.literal(customId != null ? customId : "Unknown"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to receive").withStyle(ChatFormatting.GREEN));

            elem.setCallback((s, t, a, __slotGui) -> onItemClick.onClick(player, finalItem));
            gui.setSlot(slot, elem.build());
            used[slot] = true;
            shown++;
        }

        RecipeCatalogUi.fillEmptyGridSlots(gui, used, false);

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Mob Drops").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMobDropsHub(player, onItemClick, onBack)).build());

        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(48, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> openMobDropSubPage(player, subCategory, prev, onItemClick, onBack)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(50, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> openMobDropSubPage(player, subCategory, next, onItemClick, onBack)).build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Page " + (clampedPage + 1) + " / " + totalPages).withStyle(ChatFormatting.WHITE))
                .build());

        gui.open();
    }

    private static void openSearchGui(ServerPlayer player) {
        eu.pb4.sgui.api.gui.SignGui signGui = new eu.pb4.sgui.api.gui.SignGui(player) {
            @Override
            public void onManualClose() {
                String searchTerm = this.getLine(0).getString().trim();
                if (!searchTerm.isEmpty()) {
                    showSearchResults(player, searchTerm);
                } else {
                    CustomItemsGui.open(player);
                }
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Search items").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("(partial ok)").withStyle(ChatFormatting.GRAY));
        signGui.open();
    }

    private static void showSearchResults(ServerPlayer player, String searchTerm) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Search: " + searchTerm).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        RecipeGuiTheme.fillCategoryItemGrid(gui, false);

        boolean[] used = new boolean[54];

        gui.setSlot(4, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Component.literal("Search results").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Search: \"" + searchTerm + "\"").withStyle(ChatFormatting.YELLOW))
                .glow().build());
        used[4] = true;

        List<ItemStack> results = new ArrayList<>();
        String searchLower = searchTerm.toLowerCase();

        for (Category cat : Category.values()) {
            for (ItemStack item : getItemsForCategory(cat)) {
                if (item == null || item.isEmpty()) continue;
                Component nameText = item.get(DataComponents.CUSTOM_NAME);
                String itemName = nameText != null ? nameText.getString().toLowerCase() : item.getItem().toString().toLowerCase();
                if (itemName.contains(searchLower)) {
                    results.add(item);
                }
            }
        }

        int shown = 0;
        for (ItemStack item : results) {
            if (shown >= 28) break;
            int row = shown / 7;
            int col = shown % 7 + 1;
            int slot = (row + 1) * 9 + col;

            Component displayName = item.get(DataComponents.CUSTOM_NAME);
            ItemStack clickItem = item;
            ItemStack displayStack = CustomItemLore.forSelectPageIcon(item);
            gui.setSlot(slot, new GuiElementBuilder(displayStack)
                    .setName(displayName != null ? displayName : Component.literal(item.getItem().toString()))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("✨ Click to get item").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> giveItem(player, clickItem))
                    .build());
            used[slot] = true;
            shown++;
        }

        RecipeCatalogUi.fillEmptyGridSlots(gui, used, false);

        if (shown == 0) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No results found").withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Try a different search term").withStyle(ChatFormatting.GRAY))
                    .build());
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> open(player))
                .build());

        gui.setSlot(46, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Component.literal("🔍 Search Again").withStyle(ChatFormatting.AQUA))
                .setCallback((slot, type, containerInput, slotGui) -> openSearchGui(player))
                .build());

        gui.open();
    }

    public static void openToolSetsPage(ServerPlayer player, int page, BountyItemClickHandler onItemClick, BackHandler onBack) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Tool sets").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));

        RecipeGuiTheme.fillDoubleChestCatalog(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.NETHERITE_PICKAXE)
                .setName(Component.literal("⛏ Tool Sets").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("[Icon] [gap] [Pickaxe] [Axe] [Shovel] [Hoe]").withStyle(ChatFormatting.YELLOW))
                .glow().build());

        class ToolSetDisplay {
            final net.minecraft.world.item.Item icon;
            final String name;
            final ChatFormatting color;
            final ItemStack pickaxe;
            final ItemStack axe;
            final ItemStack shovel;
            final ItemStack hoe;

            ToolSetDisplay(net.minecraft.world.item.Item icon, String name, ChatFormatting color, ItemStack pickaxe, ItemStack axe, ItemStack shovel, ItemStack hoe) {
                this.icon = icon;
                this.name = name;
                this.color = color;
                this.pickaxe = pickaxe;
                this.axe = axe;
                this.shovel = shovel;
                this.hoe = hoe;
            }
        }

        List<ToolSetDisplay> sets = new ArrayList<>();

        // Block tool sets (use representative block icons)
        sets.add(new ToolSetDisplay(Items.GLASS, "Glass", ChatFormatting.AQUA,
                BlockTools.createGlassPickaxe(), BlockTools.createGlassAxe(), BlockTools.createGlassShovel(), BlockTools.createGlassHoe()));
        sets.add(new ToolSetDisplay(Items.OBSIDIAN, "Obsidian", ChatFormatting.DARK_PURPLE,
                BlockTools.createObsidianPickaxe(), BlockTools.createObsidianAxe(), BlockTools.createObsidianShovel(), BlockTools.createObsidianHoe()));
        sets.add(new ToolSetDisplay(Items.QUARTZ_BLOCK, "Quartz", ChatFormatting.WHITE,
                BlockTools.createQuartzPickaxe(), BlockTools.createQuartzAxe(), BlockTools.createQuartzShovel(), BlockTools.createQuartzHoe()));
        sets.add(new ToolSetDisplay(Items.GLOWSTONE, "Glowstone", ChatFormatting.YELLOW,
                BlockTools.createGlowstonePickaxe(), BlockTools.createGlowstoneAxe(), BlockTools.createGlowstoneShovel(), BlockTools.createGlowstoneHoe()));
        sets.add(new ToolSetDisplay(Items.REDSTONE_BLOCK, "Redstone", ChatFormatting.RED,
                BlockTools.createRedstonePickaxe(), BlockTools.createRedstoneAxe(), BlockTools.createRedstoneShovel(), BlockTools.createRedstoneHoe()));
        // New 25 material tool sets
        sets.add(new ToolSetDisplay(Items.PRISMARINE_SHARD, "Raw Prismarine", ChatFormatting.DARK_AQUA,
                BlockTools.createRawPrismarinePickaxe(), BlockTools.createRawPrismarineAxe(), BlockTools.createRawPrismarineShovel(), BlockTools.createRawPrismarineHoe()));
        sets.add(new ToolSetDisplay(Items.CLAY_BALL, "Clay", ChatFormatting.GRAY,
                BlockTools.createClayPickaxe(), BlockTools.createClayAxe(), BlockTools.createClayShovel(), BlockTools.createClayHoe()));
        sets.add(new ToolSetDisplay(Items.WHITE_GLAZED_TERRACOTTA, "Glazed Terracotta", ChatFormatting.GOLD,
                BlockTools.createGlazedTerracottaPickaxe(), BlockTools.createGlazedTerracottaAxe(), BlockTools.createGlazedTerracottaShovel(), BlockTools.createGlazedTerracottaHoe()));
        sets.add(new ToolSetDisplay(Items.WHITE_CONCRETE, "Concrete", ChatFormatting.WHITE,
                BlockTools.createConcretePickaxe(), BlockTools.createConcreteAxe(), BlockTools.createConcreteShovel(), BlockTools.createConcreteHoe()));
        sets.add(new ToolSetDisplay(Items.WHITE_CONCRETE_POWDER, "Concrete Powder", ChatFormatting.GRAY,
                BlockTools.createConcretePowderPickaxe(), BlockTools.createConcretePowderAxe(), BlockTools.createConcretePowderShovel(), BlockTools.createConcretePowderHoe()));
        sets.add(new ToolSetDisplay(Items.BOOKSHELF, "Bookshelf", ChatFormatting.DARK_RED,
                BlockTools.createBookshelfPickaxe(), BlockTools.createBookshelfAxe(), BlockTools.createBookshelfShovel(), BlockTools.createBookshelfHoe()));
        sets.add(new ToolSetDisplay(Items.BARREL, "Barrel", ChatFormatting.GOLD,
                BlockTools.createBarrelPickaxe(), BlockTools.createBarrelAxe(), BlockTools.createBarrelShovel(), BlockTools.createBarrelHoe()));
        sets.add(new ToolSetDisplay(Items.SMITHING_TABLE, "Smithing Table", ChatFormatting.DARK_GRAY,
                BlockTools.createSmithingTablePickaxe(), BlockTools.createSmithingTableAxe(), BlockTools.createSmithingTableShovel(), BlockTools.createSmithingTableHoe()));
        sets.add(new ToolSetDisplay(Items.ANVIL, "Anvil", ChatFormatting.DARK_GRAY,
                BlockTools.createAnvilPickaxe(), BlockTools.createAnvilAxe(), BlockTools.createAnvilShovel(), BlockTools.createAnvilHoe()));
        sets.add(new ToolSetDisplay(Items.GRINDSTONE, "Grindstone", ChatFormatting.GRAY,
                BlockTools.createGrindstonePickaxe(), BlockTools.createGrindstoneAxe(), BlockTools.createGrindstoneShovel(), BlockTools.createGrindstoneHoe()));
        sets.add(new ToolSetDisplay(Items.LECTERN, "Lectern", ChatFormatting.GOLD,
                BlockTools.createLecternPickaxe(), BlockTools.createLecternAxe(), BlockTools.createLecternShovel(), BlockTools.createLecternHoe()));
        sets.add(new ToolSetDisplay(Items.COMPOSTER, "Composter", ChatFormatting.GREEN,
                BlockTools.createComposterPickaxe(), BlockTools.createComposterAxe(), BlockTools.createComposterShovel(), BlockTools.createComposterHoe()));
        sets.add(new ToolSetDisplay(Items.STONECUTTER, "Stonecutter", ChatFormatting.GRAY,
                BlockTools.createStonecutterPickaxe(), BlockTools.createStonecutterAxe(), BlockTools.createStonecutterShovel(), BlockTools.createStonecutterHoe()));
        sets.add(new ToolSetDisplay(Items.DRIED_KELP_BLOCK, "Dried Kelp", ChatFormatting.DARK_GREEN,
                BlockTools.createDriedKelpPickaxe(), BlockTools.createDriedKelpAxe(), BlockTools.createDriedKelpShovel(), BlockTools.createDriedKelpHoe()));
        sets.add(new ToolSetDisplay(Items.BRAIN_CORAL_BLOCK, "Coral Block", ChatFormatting.LIGHT_PURPLE,
                BlockTools.createCoralBlockPickaxe(), BlockTools.createCoralBlockAxe(), BlockTools.createCoralBlockShovel(), BlockTools.createCoralBlockHoe()));
        sets.add(new ToolSetDisplay(Items.LANTERN, "Lantern", ChatFormatting.YELLOW,
                BlockTools.createLanternPickaxe(), BlockTools.createLanternAxe(), BlockTools.createLanternShovel(), BlockTools.createLanternHoe()));
        sets.add(new ToolSetDisplay(Items.IRON_BARS, "Chain", ChatFormatting.DARK_GRAY,
                BlockTools.createChainPickaxe(), BlockTools.createChainAxe(), BlockTools.createChainShovel(), BlockTools.createChainHoe()));
        sets.add(new ToolSetDisplay(Items.LIGHTNING_ROD, "Lightning Rod", ChatFormatting.GOLD,
                BlockTools.createLightningRodPickaxe(), BlockTools.createLightningRodAxe(), BlockTools.createLightningRodShovel(), BlockTools.createLightningRodHoe()));
        sets.add(new ToolSetDisplay(Items.TINTED_GLASS, "Tinted Glass", ChatFormatting.DARK_GRAY,
                BlockTools.createTintedGlassPickaxe(), BlockTools.createTintedGlassAxe(), BlockTools.createTintedGlassShovel(), BlockTools.createTintedGlassHoe()));
        sets.add(new ToolSetDisplay(Items.CANDLE, "Candle", ChatFormatting.YELLOW,
                BlockTools.createCandlePickaxe(), BlockTools.createCandleAxe(), BlockTools.createCandleShovel(), BlockTools.createCandleHoe()));
        sets.add(new ToolSetDisplay(Items.SCULK_SENSOR, "Sculk Sensor", ChatFormatting.DARK_AQUA,
                BlockTools.createSculkSensorPickaxe(), BlockTools.createSculkSensorAxe(), BlockTools.createSculkSensorShovel(), BlockTools.createSculkSensorHoe()));
        sets.add(new ToolSetDisplay(Items.SCULK_CATALYST, "Sculk Catalyst", ChatFormatting.DARK_AQUA,
                BlockTools.createSculkCatalystPickaxe(), BlockTools.createSculkCatalystAxe(), BlockTools.createSculkCatalystShovel(), BlockTools.createSculkCatalystHoe()));
        sets.add(new ToolSetDisplay(Items.SCULK_SHRIEKER, "Sculk Shrieker", ChatFormatting.DARK_AQUA,
                BlockTools.createSculkShriekerPickaxe(), BlockTools.createSculkShriekerAxe(), BlockTools.createSculkShriekerShovel(), BlockTools.createSculkShriekerHoe()));
        sets.add(new ToolSetDisplay(Items.BREWING_STAND, "Brewing Stand", ChatFormatting.LIGHT_PURPLE,
                BlockTools.createBrewingStandPickaxe(), BlockTools.createBrewingStandAxe(), BlockTools.createBrewingStandShovel(), BlockTools.createBrewingStandHoe()));
        sets.add(new ToolSetDisplay(Items.FURNACE, "Furnace", ChatFormatting.DARK_GRAY,
                BlockTools.createFurnacePickaxe(), BlockTools.createFurnaceAxe(), BlockTools.createFurnaceShovel(), BlockTools.createFurnaceHoe()));

        // Legendary tool sets (use pickaxe base item as icon)
        sets.add(new ToolSetDisplay(Items.DRAGON_HEAD, "Dragon", ChatFormatting.DARK_RED,
                LegendaryTools.createDragonPickaxe(), LegendaryTools.createDragonAxe(), LegendaryTools.createDragonShovel(), LegendaryTools.createDragonHoe()));
        sets.add(new ToolSetDisplay(Items.ELYTRA, "Aether", ChatFormatting.AQUA,
                LegendaryTools.createAetherPickaxe(), LegendaryTools.createAetherAxe(), LegendaryTools.createAetherShovel(), LegendaryTools.createAetherHoe()));
        sets.add(new ToolSetDisplay(Items.ENDER_PEARL, "Void", ChatFormatting.DARK_PURPLE,
                LegendaryTools.createVoidPickaxe(), LegendaryTools.createVoidAxe(), LegendaryTools.createVoidShovel(), LegendaryTools.createVoidHoe()));
        sets.add(new ToolSetDisplay(Items.OAK_SAPLING, "Nature", ChatFormatting.GREEN,
                LegendaryTools.createNaturePickaxe(), LegendaryTools.createNatureAxe(), LegendaryTools.createNatureShovel(), LegendaryTools.createNatureHoe()));
        sets.add(new ToolSetDisplay(Items.SNOW_BLOCK, "Frost", ChatFormatting.BLUE,
                LegendaryTools.createFrostPickaxe(), LegendaryTools.createFrostAxe(), LegendaryTools.createFrostShovel(), LegendaryTools.createFrostHoe()));
        sets.add(new ToolSetDisplay(Items.LIGHTNING_ROD, "Thunder", ChatFormatting.YELLOW,
                LegendaryTools.createThunderPickaxe(), LegendaryTools.createThunderAxe(), LegendaryTools.createThunderShovel(), LegendaryTools.createThunderHoe()));
        sets.add(new ToolSetDisplay(Items.HEART_OF_THE_SEA, "Ocean", ChatFormatting.DARK_BLUE,
                LegendaryTools.createOceanPickaxe(), LegendaryTools.createOceanAxe(), LegendaryTools.createOceanShovel(), LegendaryTools.createOceanHoe()));
        sets.add(new ToolSetDisplay(Items.PHANTOM_MEMBRANE, "Lunar", ChatFormatting.LIGHT_PURPLE,
                LegendaryTools.createLunarPickaxe(), LegendaryTools.createLunarAxe(), LegendaryTools.createLunarShovel(), LegendaryTools.createLunarHoe()));
        sets.add(new ToolSetDisplay(Items.FIRE_CHARGE, "Solar", ChatFormatting.GOLD,
                LegendaryTools.createSolarPickaxe(), LegendaryTools.createSolarAxe(), LegendaryTools.createSolarShovel(), LegendaryTools.createSolarHoe()));
        sets.add(new ToolSetDisplay(Items.GRASS_BLOCK, "Terra", ChatFormatting.DARK_GREEN,
                LegendaryTools.createTerraPickaxe(), LegendaryTools.createTerraAxe(), LegendaryTools.createTerraShovel(), LegendaryTools.createTerraHoe()));
        sets.add(new ToolSetDisplay(Items.SPECTRAL_ARROW, "Phantom", ChatFormatting.GRAY,
                LegendaryTools.createPhantomPickaxe(), LegendaryTools.createPhantomAxe(), LegendaryTools.createPhantomShovel(), LegendaryTools.createPhantomHoe()));
        sets.add(new ToolSetDisplay(Items.NETHER_WART, "Blood", ChatFormatting.DARK_RED,
                LegendaryTools.createBloodPickaxe(), LegendaryTools.createBloodAxe(), LegendaryTools.createBloodShovel(), LegendaryTools.createBloodHoe()));
        sets.add(new ToolSetDisplay(Items.NETHER_STAR, "Celestial", ChatFormatting.DARK_AQUA,
                LegendaryTools.createCelestialPickaxe(), LegendaryTools.createCelestialAxe(), LegendaryTools.createCelestialShovel(), LegendaryTools.createCelestialHoe()));
        sets.add(new ToolSetDisplay(Items.BLACK_DYE, "Shadow", ChatFormatting.BLACK,
                LegendaryTools.createShadowPickaxe(), LegendaryTools.createShadowAxe(), LegendaryTools.createShadowShovel(), LegendaryTools.createShadowHoe()));
        sets.add(new ToolSetDisplay(Items.DIAMOND, "Crystal", ChatFormatting.WHITE,
                LegendaryTools.createCrystalPickaxe(), LegendaryTools.createCrystalAxe(), LegendaryTools.createCrystalShovel(), LegendaryTools.createCrystalHoe()));

        int setsPerPage = 4;
        int totalPages = Math.max(1, (sets.size() + setsPerPage - 1) / setsPerPage);
        int clampedPage = Math.min(page, totalPages - 1);
        int startIdx = clampedPage * setsPerPage;
        int endIdx = Math.min(startIdx + setsPerPage, sets.size());

        for (int i = startIdx; i < endIdx; i++) {
            ToolSetDisplay set = sets.get(i);
            int row = i - startIdx + 1;
            int base = row * 9;

            gui.setSlot(base, new GuiElementBuilder(set.icon)
                    .setName(Component.literal(set.name + " Tools").withStyle(set.color, ChatFormatting.BOLD))
                    .glow().build());
            gui.setSlot(base + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());

            ItemStack[] tools = { set.pickaxe, set.axe, set.shovel, set.hoe };
            for (int j = 0; j < tools.length; j++) {
                ItemStack tool = tools[j];
                Component toolName = tool.get(DataComponents.CUSTOM_NAME);
                final ItemStack finalTool = tool;
                gui.setSlot(base + 2 + j, new GuiElementBuilder(tool.copy())
                        .setName(toolName != null ? toolName : Component.literal(tool.getItem().toString()))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("✨ Click").withStyle(ChatFormatting.YELLOW))
                        .setCallback((s, t, a, __slotGui) -> onItemClick.onClick(player, finalTool))
                        .build());
            }
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> onBack.open(player)).build());

        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> openToolSetsPage(player, prev, onItemClick, onBack)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> openToolSetsPage(player, next, onItemClick, onBack)).build());
        }

        gui.setSlot(50, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Page " + (clampedPage + 1) + " / " + totalPages).withStyle(ChatFormatting.WHITE))
                .build());

        gui.open();
    }

    public static void openBountySetsPage(ServerPlayer player, int page, BountyItemClickHandler onItemClick, BackHandler onBack) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("⚔ Bounty Sets (T1 & T2)"));

        RecipeGuiTheme.fillDoubleChestCatalog(gui);

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("⚔ Bounty Sets (T1 & T2)").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Displayed in rows:").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("[Icon] [gap] [Sword] [Helmet] [Chest] [Legs] [Boots]").withStyle(ChatFormatting.YELLOW))
                .glow().build());

        List<RecipesGui.BountySetDisplay> sets = new ArrayList<>();
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            sets.add(new RecipesGui.BountySetDisplay(type, type.icon, type.displayName, type.color,
                    SlayerItems.createSlayerSword(type),
                    SlayerItems.createSlayerHelmet(type, 1),
                    SlayerItems.createSlayerChestplate(type, 1),
                    SlayerItems.createSlayerLeggings(type, 1),
                    SlayerItems.createSlayerBoots(type, 1),
                    SlayerItems.createUpgradedSlayerSword(type),
                    SlayerItems.createSlayerHelmet(type, 2),
                    SlayerItems.createSlayerChestplate(type, 2),
                    SlayerItems.createSlayerLeggings(type, 2),
                    SlayerItems.createSlayerBoots(type, 2)));
        }

        int setsPerPage = 2;
        int totalPages = Math.max(1, (sets.size() + setsPerPage - 1) / setsPerPage);
        int clampedPage = Math.min(page, totalPages - 1);
        int startIdx = clampedPage * setsPerPage;
        int endIdx = Math.min(startIdx + setsPerPage, sets.size());

        for (int i = startIdx; i < endIdx; i++) {
            RecipesGui.BountySetDisplay set = sets.get(i);
            int setIndexOnPage = i - startIdx;
            int baseRow = 1 + (setIndexOnPage * 2);

            int row1Base = baseRow * 9;
            gui.setSlot(row1Base, new GuiElementBuilder(set.icon)
                    .setName(Component.literal(set.name + " T1").withStyle(set.color, ChatFormatting.BOLD))
                    .glow().build());
            gui.setSlot(row1Base + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());

            ItemStack[] t1Items = { set.t1Sword, set.t1Helmet, set.t1Chestplate, set.t1Leggings, set.t1Boots };
            for (int j = 0; j < t1Items.length; j++) {
                ItemStack item = t1Items[j];
                Component name = item.get(DataComponents.CUSTOM_NAME);
                final ItemStack finalItem = item;
                gui.setSlot(row1Base + 2 + j, new GuiElementBuilder(item.copy())
                        .setName(name != null ? name : Component.literal(item.getItem().toString()))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("✨ Click").withStyle(ChatFormatting.YELLOW))
                        .setCallback((s, t, a, __slotGui) -> onItemClick.onClick(player, finalItem))
                        .build());
            }

            int row2Base = (baseRow + 1) * 9;
            gui.setSlot(row2Base, new GuiElementBuilder(set.icon)
                    .setName(Component.literal(set.name + " T2").withStyle(set.color, ChatFormatting.BOLD))
                    .glow().build());
            gui.setSlot(row2Base + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());

            ItemStack[] t2Items = { set.t2Sword, set.t2Helmet, set.t2Chestplate, set.t2Leggings, set.t2Boots };
            for (int j = 0; j < t2Items.length; j++) {
                ItemStack item = t2Items[j];
                Component name = item.get(DataComponents.CUSTOM_NAME);
                final ItemStack finalItem = item;
                gui.setSlot(row2Base + 2 + j, new GuiElementBuilder(item.copy())
                        .setName(name != null ? name : Component.literal(item.getItem().toString()))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("✨ Click").withStyle(ChatFormatting.YELLOW))
                        .setCallback((s, t, a, __slotGui) -> onItemClick.onClick(player, finalItem))
                        .build());
            }
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> onBack.open(player)).build());

        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> openBountySetsPage(player, prev, onItemClick, onBack)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> openBountySetsPage(player, next, onItemClick, onBack)).build());
        }

        gui.setSlot(50, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Page " + (clampedPage + 1) + " / " + totalPages).withStyle(ChatFormatting.WHITE))
                .build());

        gui.open();
    }

    // ──────────────────────────────────────────────────────────────
    // GEM_BLOCK_GEAR: Armor Sets in Rows Display
    // Each row shows: [Block] [Sword] [Helmet] [Chest] [Legs] [Boots]
    // ──────────────────────────────────────────────────────────────

    public static void openGemBlockGearPage(ServerPlayer player, int page) {
        openGemBlockGearPage(player, page, (p, item) -> giveItem(p, item), CustomItemsGui::open);
    }

    public static void openGemBlockGearPage(ServerPlayer player, int page, BountyItemClickHandler onItemClick, BackHandler onBack) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("§6💎 Gem & Block Armor"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("💎 Gem & Block Armor").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Armor sets displayed in rows:").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("[Block] [Sword] [Helmet] [Chest] [Legs] [Boots]").withStyle(ChatFormatting.YELLOW))
                .glow().build());

        // Define armor sets: Block Item, Sword, Armor pieces
        ArmorSetDisplay[] allSets = {
            new ArmorSetDisplay(Items.GLASS, BlockWeapons.createGlassSword(),
                BlockArmor.createGlassHelmet(), BlockArmor.createGlassChestplate(),
                BlockArmor.createGlassLeggings(), BlockArmor.createGlassBoots()),
            new ArmorSetDisplay(Items.OBSIDIAN, BlockWeapons.createObsidianSword(),
                BlockArmor.createObsidianHelmet(), BlockArmor.createObsidianChestplate(),
                BlockArmor.createObsidianLeggings(), BlockArmor.createObsidianBoots()),
            new ArmorSetDisplay(Items.QUARTZ_BLOCK, BlockWeapons.createQuartzSword(),
                BlockArmor.createQuartzHelmet(), BlockArmor.createQuartzChestplate(),
                BlockArmor.createQuartzLeggings(), BlockArmor.createQuartzBoots()),
            new ArmorSetDisplay(Items.GLOWSTONE, BlockWeapons.createGlowstoneSword(),
                BlockArmor.createGlowstoneHelmet(), BlockArmor.createGlowstoneChestplate(),
                BlockArmor.createGlowstoneLeggings(), BlockArmor.createGlowstoneBoots()),
            new ArmorSetDisplay(Items.REDSTONE_BLOCK, BlockWeapons.createRedstoneSword(),
                BlockArmor.createRedstoneHelmet(), BlockArmor.createRedstoneChestplate(),
                BlockArmor.createRedstoneLeggings(), BlockArmor.createRedstoneBoots()),
            new ArmorSetDisplay(Items.NETHERRACK, BlockWeapons.createNetherrackSword(),
                BlockArmor.createNetherrackHelmet(), BlockArmor.createNetherrackChestplate(),
                BlockArmor.createNetherrackLeggings(), BlockArmor.createNetherrackBoots()),
            new ArmorSetDisplay(Items.END_STONE, BlockWeapons.createEndstoneSword(),
                BlockArmor.createEndstoneHelmet(), BlockArmor.createEndstoneChestplate(),
                BlockArmor.createEndstoneLeggings(), BlockArmor.createEndstoneBoots()),
            new ArmorSetDisplay(Items.PACKED_ICE, BlockWeapons.createIceSword(),
                BlockArmor.createIceHelmet(), BlockArmor.createIceChestplate(),
                BlockArmor.createIceLeggings(), BlockArmor.createIceBoots()),
            new ArmorSetDisplay(Items.PRISMARINE, BlockWeapons.createPrismarineSword(),
                BlockArmor.createPrismarineHelmet(), BlockArmor.createPrismarineChestplate(),
                BlockArmor.createPrismarineLeggings(), BlockArmor.createPrismarineBoots()),
            new ArmorSetDisplay(Items.TERRACOTTA, BlockWeapons.createTerracottaSword(),
                BlockArmor.createTerracottaHelmet(), BlockArmor.createTerracottaChestplate(),
                BlockArmor.createTerracottaLeggings(), BlockArmor.createTerracottaBoots()),
            new ArmorSetDisplay(Items.MOSSY_COBBLESTONE, BlockWeapons.createMossySword(),
                BlockArmor.createMossyHelmet(), BlockArmor.createMossyChestplate(),
                BlockArmor.createMossyLeggings(), BlockArmor.createMossyBoots()),
            new ArmorSetDisplay(Items.SOUL_SAND, BlockWeapons.createSoulSandSword(),
                BlockArmor.createSoulSandHelmet(), BlockArmor.createSoulSandChestplate(),
                BlockArmor.createSoulSandLeggings(), BlockArmor.createSoulSandBoots()),
            new ArmorSetDisplay(Items.MAGMA_BLOCK, BlockWeapons.createMagmaSword(),
                BlockArmor.createMagmaHelmet(), BlockArmor.createMagmaChestplate(),
                BlockArmor.createMagmaLeggings(), BlockArmor.createMagmaBoots()),
            new ArmorSetDisplay(Items.SANDSTONE, BlockWeapons.createSandstoneSword(),
                BlockArmor.createSandstoneHelmet(), BlockArmor.createSandstoneChestplate(),
                BlockArmor.createSandstoneLeggings(), BlockArmor.createSandstoneBoots()),
            new ArmorSetDisplay(Items.AMETHYST_BLOCK, BlockWeapons.createAmethystSword(),
                BlockArmor.createAmethystHelmet(), BlockArmor.createAmethystChestplate(),
                BlockArmor.createAmethystLeggings(), BlockArmor.createAmethystBoots()),
            new ArmorSetDisplay(Items.COAL_BLOCK, BlockWeapons.createCoalSword(),
                BlockArmor.createCoalHelmet(), BlockArmor.createCoalChestplate(),
                BlockArmor.createCoalLeggings(), BlockArmor.createCoalBoots()),
            new ArmorSetDisplay(Items.CHARCOAL, BlockWeapons.createCharcoalSword(),
                BlockArmor.createCharcoalHelmet(), BlockArmor.createCharcoalChestplate(),
                BlockArmor.createCharcoalLeggings(), BlockArmor.createCharcoalBoots()),
            // 36+ NEW BLOCK ARMOR SETS
            new ArmorSetDisplay(Items.DIAMOND_BLOCK, BlockWeapons.createDiamondBlockSword(),
                BlockArmor.createDiamondBlockHelmet(), BlockArmor.createDiamondBlockChestplate(),
                BlockArmor.createDiamondBlockLeggings(), BlockArmor.createDiamondBlockBoots()),
            new ArmorSetDisplay(Items.EMERALD_BLOCK, BlockWeapons.createEmeraldBlockSword(),
                BlockArmor.createEmeraldBlockHelmet(), BlockArmor.createEmeraldBlockChestplate(),
                BlockArmor.createEmeraldBlockLeggings(), BlockArmor.createEmeraldBlockBoots()),
            new ArmorSetDisplay(Items.GOLD_BLOCK, BlockWeapons.createGoldBlockSword(),
                BlockArmor.createGoldBlockHelmet(), BlockArmor.createGoldBlockChestplate(),
                BlockArmor.createGoldBlockLeggings(), BlockArmor.createGoldBlockBoots()),
            new ArmorSetDisplay(Items.IRON_BLOCK, BlockWeapons.createIronBlockSword(),
                BlockArmor.createIronBlockHelmet(), BlockArmor.createIronBlockChestplate(),
                BlockArmor.createIronBlockLeggings(), BlockArmor.createIronBlockBoots()),
            new ArmorSetDisplay(Items.LAPIS_BLOCK, BlockWeapons.createLapisBlockSword(),
                BlockArmor.createLapisBlockHelmet(), BlockArmor.createLapisBlockChestplate(),
                BlockArmor.createLapisBlockLeggings(), BlockArmor.createLapisBlockBoots()),
            new ArmorSetDisplay(Items.COPPER_BLOCK, BlockWeapons.createCopperBlockSword(),
                BlockArmor.createCopperBlockHelmet(), BlockArmor.createCopperBlockChestplate(),
                BlockArmor.createCopperBlockLeggings(), BlockArmor.createCopperBlockBoots()),
            new ArmorSetDisplay(Items.ANCIENT_DEBRIS, BlockWeapons.createAncientDebrisSword(),
                BlockArmor.createAncientDebrisHelmet(), BlockArmor.createAncientDebrisChestplate(),
                BlockArmor.createAncientDebrisLeggings(), BlockArmor.createAncientDebrisBoots()),
            new ArmorSetDisplay(Items.BASALT, BlockWeapons.createBasaltSword(),
                BlockArmor.createBasaltHelmet(), BlockArmor.createBasaltChestplate(),
                BlockArmor.createBasaltLeggings(), BlockArmor.createBasaltBoots()),
            new ArmorSetDisplay(Items.BLACKSTONE, BlockWeapons.createBlackstoneSword(),
                BlockArmor.createBlackstoneHelmet(), BlockArmor.createBlackstoneChestplate(),
                BlockArmor.createBlackstoneLeggings(), BlockArmor.createBlackstoneBoots()),
            new ArmorSetDisplay(Items.BONE_BLOCK, BlockWeapons.createBoneBlockSword(),
                BlockArmor.createBoneBlockHelmet(), BlockArmor.createBoneBlockChestplate(),
                BlockArmor.createBoneBlockLeggings(), BlockArmor.createBoneBlockBoots()),
            new ArmorSetDisplay(Items.BRICKS, BlockWeapons.createBrickSword(),
                BlockArmor.createBrickHelmet(), BlockArmor.createBrickChestplate(),
                BlockArmor.createBrickLeggings(), BlockArmor.createBrickBoots()),
            new ArmorSetDisplay(Items.CACTUS, BlockWeapons.createCactusSword(),
                BlockArmor.createCactusHelmet(), BlockArmor.createCactusChestplate(),
                BlockArmor.createCactusLeggings(), BlockArmor.createCactusBoots()),
            new ArmorSetDisplay(Items.CALCITE, BlockWeapons.createCalciteSword(),
                BlockArmor.createCalciteHelmet(), BlockArmor.createCalciteChestplate(),
                BlockArmor.createCalciteLeggings(), BlockArmor.createCalciteBoots()),
            new ArmorSetDisplay(Items.DEEPSLATE, BlockWeapons.createDeepslateSword(),
                BlockArmor.createDeepslateHelmet(), BlockArmor.createDeepslateChestplate(),
                BlockArmor.createDeepslateLeggings(), BlockArmor.createDeepslateBoots()),
            new ArmorSetDisplay(Items.DRIPSTONE_BLOCK, BlockWeapons.createDripstoneSword(),
                BlockArmor.createDripstoneHelmet(), BlockArmor.createDripstoneChestplate(),
                BlockArmor.createDripstoneLeggings(), BlockArmor.createDripstoneBoots()),
            new ArmorSetDisplay(Items.HAY_BLOCK, BlockWeapons.createHaySword(),
                BlockArmor.createHayHelmet(), BlockArmor.createHayChestplate(),
                BlockArmor.createHayLeggings(), BlockArmor.createHayBoots()),
            new ArmorSetDisplay(Items.HONEYCOMB_BLOCK, BlockWeapons.createHoneycombSword(),
                BlockArmor.createHoneycombHelmet(), BlockArmor.createHoneycombChestplate(),
                BlockArmor.createHoneycombLeggings(), BlockArmor.createHoneycombBoots()),
            new ArmorSetDisplay(Items.LILY_PAD, BlockWeapons.createLilyPadSword(),
                BlockArmor.createLilyPadHelmet(), BlockArmor.createLilyPadChestplate(),
                BlockArmor.createLilyPadLeggings(), BlockArmor.createLilyPadBoots()),
            new ArmorSetDisplay(Items.MELON, BlockWeapons.createMelonSword(),
                BlockArmor.createMelonHelmet(), BlockArmor.createMelonChestplate(),
                BlockArmor.createMelonLeggings(), BlockArmor.createMelonBoots()),
            new ArmorSetDisplay(Items.MOSS_BLOCK, BlockWeapons.createMossBlockSword(),
                BlockArmor.createMossBlockHelmet(), BlockArmor.createMossBlockChestplate(),
                BlockArmor.createMossBlockLeggings(), BlockArmor.createMossBlockBoots()),
            new ArmorSetDisplay(Items.MYCELIUM, BlockWeapons.createMyceliumSword(),
                BlockArmor.createMyceliumHelmet(), BlockArmor.createMyceliumChestplate(),
                BlockArmor.createMyceliumLeggings(), BlockArmor.createMyceliumBoots()),
            new ArmorSetDisplay(Items.NETHER_BRICKS, BlockWeapons.createNetherBrickSword(),
                BlockArmor.createNetherBrickHelmet(), BlockArmor.createNetherBrickChestplate(),
                BlockArmor.createNetherBrickLeggings(), BlockArmor.createNetherBrickBoots()),
            new ArmorSetDisplay(Items.PUMPKIN, BlockWeapons.createPumpkinSword(),
                BlockArmor.createPumpkinHelmet(), BlockArmor.createPumpkinChestplate(),
                BlockArmor.createPumpkinLeggings(), BlockArmor.createPumpkinBoots()),
            new ArmorSetDisplay(Items.PURPUR_BLOCK, BlockWeapons.createPurpurSword(),
                BlockArmor.createPurpurHelmet(), BlockArmor.createPurpurChestplate(),
                BlockArmor.createPurpurLeggings(), BlockArmor.createPurpurBoots()),
            new ArmorSetDisplay(Items.SAND, BlockWeapons.createSandSword(),
                BlockArmor.createSandHelmet(), BlockArmor.createSandChestplate(),
                BlockArmor.createSandLeggings(), BlockArmor.createSandBoots()),
            new ArmorSetDisplay(Items.SCULK, BlockWeapons.createSculkSword(),
                BlockArmor.createSculkHelmet(), BlockArmor.createSculkChestplate(),
                BlockArmor.createSculkLeggings(), BlockArmor.createSculkBoots()),
            new ArmorSetDisplay(Items.SHROOMLIGHT, BlockWeapons.createShroomlightSword(),
                BlockArmor.createShroomlightHelmet(), BlockArmor.createShroomlightChestplate(),
                BlockArmor.createShroomlightLeggings(), BlockArmor.createShroomlightBoots()),
            new ArmorSetDisplay(Items.SLIME_BLOCK, BlockWeapons.createSlimeSword(),
                BlockArmor.createSlimeHelmet(), BlockArmor.createSlimeChestplate(),
                BlockArmor.createSlimeLeggings(), BlockArmor.createSlimeBoots()),
            new ArmorSetDisplay(Items.SMOOTH_STONE, BlockWeapons.createSmoothStoneSword(),
                BlockArmor.createSmoothStoneHelmet(), BlockArmor.createSmoothStoneChestplate(),
                BlockArmor.createSmoothStoneLeggings(), BlockArmor.createSmoothStoneBoots()),
            new ArmorSetDisplay(Items.SNOW_BLOCK, BlockWeapons.createSnowSword(),
                BlockArmor.createSnowHelmet(), BlockArmor.createSnowChestplate(),
                BlockArmor.createSnowLeggings(), BlockArmor.createSnowBoots()),
            new ArmorSetDisplay(Items.SOUL_SOIL, BlockWeapons.createSoulSoilSword(),
                BlockArmor.createSoulSoilHelmet(), BlockArmor.createSoulSoilChestplate(),
                BlockArmor.createSoulSoilLeggings(), BlockArmor.createSoulSoilBoots()),
            new ArmorSetDisplay(Items.SPONGE, BlockWeapons.createSpongeSword(),
                BlockArmor.createSpongeHelmet(), BlockArmor.createSpongeChestplate(),
                BlockArmor.createSpongeLeggings(), BlockArmor.createSpongeBoots()),
            new ArmorSetDisplay(Items.TARGET, BlockWeapons.createTargetSword(),
                BlockArmor.createTargetHelmet(), BlockArmor.createTargetChestplate(),
                BlockArmor.createTargetLeggings(), BlockArmor.createTargetBoots()),
            new ArmorSetDisplay(Items.TNT, BlockWeapons.createTntSword(),
                BlockArmor.createTntHelmet(), BlockArmor.createTntChestplate(),
                BlockArmor.createTntLeggings(), BlockArmor.createTntBoots()),
            new ArmorSetDisplay(Items.WARPED_STEM, BlockWeapons.createWarpedSword(),
                BlockArmor.createWarpedHelmet(), BlockArmor.createWarpedChestplate(),
                BlockArmor.createWarpedLeggings(), BlockArmor.createWarpedBoots()),
            // MATERIAL ITEM ARMOR SETS
            new ArmorSetDisplay(Items.COPPER_INGOT, BlockWeapons.createCopperIngotSword(),
                BlockArmor.createCopperIngotHelmet(), BlockArmor.createCopperIngotChestplate(),
                BlockArmor.createCopperIngotLeggings(), BlockArmor.createCopperIngotBoots()),
            new ArmorSetDisplay(Items.EMERALD, BlockWeapons.createEmeraldSword(),
                BlockArmor.createEmeraldHelmet(), BlockArmor.createEmeraldChestplate(),
                BlockArmor.createEmeraldLeggings(), BlockArmor.createEmeraldBoots()),
            new ArmorSetDisplay(Items.LAPIS_LAZULI, BlockWeapons.createLapisLazuliSword(),
                BlockArmor.createLapisLazuliHelmet(), BlockArmor.createLapisLazuliChestplate(),
                BlockArmor.createLapisLazuliLeggings(), BlockArmor.createLapisLazuliBoots()),
            new ArmorSetDisplay(Items.AMETHYST_SHARD, BlockWeapons.createAmethystShardSword(),
                BlockArmor.createAmethystShardHelmet(), BlockArmor.createAmethystShardChestplate(),
                BlockArmor.createAmethystShardLeggings(), BlockArmor.createAmethystShardBoots()),
            new ArmorSetDisplay(Items.FLINT, BlockWeapons.createFlintSword(),
                BlockArmor.createFlintHelmet(), BlockArmor.createFlintChestplate(),
                BlockArmor.createFlintLeggings(), BlockArmor.createFlintBoots()),
            new ArmorSetDisplay(Items.BONE_MEAL, BlockWeapons.createBoneMealSword(),
                BlockArmor.createBoneMealHelmet(), BlockArmor.createBoneMealChestplate(),
                BlockArmor.createBoneMealLeggings(), BlockArmor.createBoneMealBoots()),
            // WOOD ARMOR SETS (Complete collection)
            new ArmorSetDisplay(Items.OAK_PLANKS, BlockWeapons.createOakPlanksSword(),
                BlockArmor.createOakPlanksHelmet(), BlockArmor.createOakPlanksChestplate(),
                BlockArmor.createOakPlanksLeggings(), BlockArmor.createOakPlanksBoots()),
            new ArmorSetDisplay(Items.SPRUCE_PLANKS, BlockWeapons.createSprucePlanksSword(),
                BlockArmor.createSprucePlanksHelmet(), BlockArmor.createSprucePlanksChestplate(),
                BlockArmor.createSprucePlanksLeggings(), BlockArmor.createSprucePlanksBoots()),
            new ArmorSetDisplay(Items.BIRCH_PLANKS, BlockWeapons.createBirchPlanksSword(),
                BlockArmor.createBirchPlanksHelmet(), BlockArmor.createBirchPlanksChestplate(),
                BlockArmor.createBirchPlanksLeggings(), BlockArmor.createBirchPlanksBoots()),
            new ArmorSetDisplay(Items.JUNGLE_PLANKS, BlockWeapons.createJunglePlanksSword(),
                BlockArmor.createJunglePlanksHelmet(), BlockArmor.createJunglePlanksChestplate(),
                BlockArmor.createJunglePlanksLeggings(), BlockArmor.createJunglePlanksBoots()),
            new ArmorSetDisplay(Items.ACACIA_PLANKS, BlockWeapons.createAcaciaPlanksSword(),
                BlockArmor.createAcaciaPlanksHelmet(), BlockArmor.createAcaciaPlanksChestplate(),
                BlockArmor.createAcaciaPlanksLeggings(), BlockArmor.createAcaciaPlanksBoots()),
            new ArmorSetDisplay(Items.DARK_OAK_PLANKS, BlockWeapons.createDarkOakPlanksSword(),
                BlockArmor.createDarkOakPlanksHelmet(), BlockArmor.createDarkOakPlanksChestplate(),
                BlockArmor.createDarkOakPlanksLeggings(), BlockArmor.createDarkOakPlanksBoots()),
            new ArmorSetDisplay(Items.MANGROVE_PLANKS, BlockWeapons.createMangrovePlanksSword(),
                BlockArmor.createMangrovePlanksHelmet(), BlockArmor.createMangrovePlanksChestplate(),
                BlockArmor.createMangrovePlanksLeggings(), BlockArmor.createMangrovePlanksBoots()),
            new ArmorSetDisplay(Items.CHERRY_PLANKS, BlockWeapons.createCherryPlanksSword(),
                BlockArmor.createCherryPlanksHelmet(), BlockArmor.createCherryPlanksChestplate(),
                BlockArmor.createCherryPlanksLeggings(), BlockArmor.createCherryPlanksBoots()),
            new ArmorSetDisplay(Items.BAMBOO_PLANKS, BlockWeapons.createBambooPlanksSword(),
                BlockArmor.createBambooPlanksHelmet(), BlockArmor.createBambooPlanksChestplate(),
                BlockArmor.createBambooPlanksLeggings(), BlockArmor.createBambooPlanksBoots()),
            new ArmorSetDisplay(Items.CRIMSON_PLANKS, BlockWeapons.createCrimsonPlanksSword(),
                BlockArmor.createCrimsonPlanksHelmet(), BlockArmor.createCrimsonPlanksChestplate(),
                BlockArmor.createCrimsonPlanksLeggings(), BlockArmor.createCrimsonPlanksBoots()),
            new ArmorSetDisplay(Items.WARPED_PLANKS, BlockWeapons.createWarpedPlanksSword(),
                BlockArmor.createWarpedPlanksHelmet(), BlockArmor.createWarpedPlanksChestplate(),
                BlockArmor.createWarpedPlanksLeggings(), BlockArmor.createWarpedPlanksBoots()),
            // STRIPPED LOG ARMOR SETS
            new ArmorSetDisplay(Items.STRIPPED_SPRUCE_LOG, BlockWeapons.createStrippedSpruceLogSword(),
                BlockArmor.createStrippedSpruceLogHelmet(), BlockArmor.createStrippedSpruceLogChestplate(),
                BlockArmor.createStrippedSpruceLogLeggings(), BlockArmor.createStrippedSpruceLogBoots()),
            new ArmorSetDisplay(Items.STRIPPED_BIRCH_LOG, BlockWeapons.createStrippedBirchLogSword(),
                BlockArmor.createStrippedBirchLogHelmet(), BlockArmor.createStrippedBirchLogChestplate(),
                BlockArmor.createStrippedBirchLogLeggings(), BlockArmor.createStrippedBirchLogBoots()),
            new ArmorSetDisplay(Items.STRIPPED_DARK_OAK_LOG, BlockWeapons.createStrippedDarkOakLogSword(),
                BlockArmor.createStrippedDarkOakLogHelmet(), BlockArmor.createStrippedDarkOakLogChestplate(),
                BlockArmor.createStrippedDarkOakLogLeggings(), BlockArmor.createStrippedDarkOakLogBoots()),
            new ArmorSetDisplay(Items.STRIPPED_JUNGLE_LOG, BlockWeapons.createStrippedJungleLogSword(),
                BlockArmor.createStrippedJungleLogHelmet(), BlockArmor.createStrippedJungleLogChestplate(),
                BlockArmor.createStrippedJungleLogLeggings(), BlockArmor.createStrippedJungleLogBoots()),
            new ArmorSetDisplay(Items.STRIPPED_ACACIA_LOG, BlockWeapons.createStrippedAcaciaLogSword(),
                BlockArmor.createStrippedAcaciaLogHelmet(), BlockArmor.createStrippedAcaciaLogChestplate(),
                BlockArmor.createStrippedAcaciaLogLeggings(), BlockArmor.createStrippedAcaciaLogBoots()),
            new ArmorSetDisplay(Items.STRIPPED_MANGROVE_LOG, BlockWeapons.createStrippedMangroveLogSword(),
                BlockArmor.createStrippedMangroveLogHelmet(), BlockArmor.createStrippedMangroveLogChestplate(),
                BlockArmor.createStrippedMangroveLogLeggings(), BlockArmor.createStrippedMangroveLogBoots()),
            new ArmorSetDisplay(Items.STRIPPED_CHERRY_LOG, BlockWeapons.createStrippedCherryLogSword(),
                BlockArmor.createStrippedCherryLogHelmet(), BlockArmor.createStrippedCherryLogChestplate(),
                BlockArmor.createStrippedCherryLogLeggings(), BlockArmor.createStrippedCherryLogBoots()),
            new ArmorSetDisplay(Items.STRIPPED_BAMBOO_BLOCK, BlockWeapons.createStrippedBambooBlockSword(),
                BlockArmor.createStrippedBambooBlockHelmet(), BlockArmor.createStrippedBambooBlockChestplate(),
                BlockArmor.createStrippedBambooBlockLeggings(), BlockArmor.createStrippedBambooBlockBoots()),
            new ArmorSetDisplay(Items.STRIPPED_CRIMSON_STEM, BlockWeapons.createStrippedCrimsonStemSword(),
                BlockArmor.createStrippedCrimsonStemHelmet(), BlockArmor.createStrippedCrimsonStemChestplate(),
                BlockArmor.createStrippedCrimsonStemLeggings(), BlockArmor.createStrippedCrimsonStemBoots()),
            new ArmorSetDisplay(Items.STRIPPED_WARPED_STEM, BlockWeapons.createStrippedWarpedStemSword(),
                BlockArmor.createStrippedWarpedStemHelmet(), BlockArmor.createStrippedWarpedStemChestplate(),
                BlockArmor.createStrippedWarpedStemLeggings(), BlockArmor.createStrippedWarpedStemBoots()),
            new ArmorSetDisplay(Items.CRIMSON_STEM, BlockWeapons.createCrimsonStemSword(),
                BlockArmor.createCrimsonStemHelmet(), BlockArmor.createCrimsonStemChestplate(),
                BlockArmor.createCrimsonStemLeggings(), BlockArmor.createCrimsonStemBoots()),
            new ArmorSetDisplay(Items.CRYING_OBSIDIAN, BlockWeapons.createCryingObsidianSword(),
                BlockArmor.createCryingObsidianHelmet(), BlockArmor.createCryingObsidianChestplate(),
                BlockArmor.createCryingObsidianLeggings(), BlockArmor.createCryingObsidianBoots()),
            new ArmorSetDisplay(Items.GILDED_BLACKSTONE, BlockWeapons.createGildedBlackstoneSword(),
                BlockArmor.createGildedBlackstoneHelmet(), BlockArmor.createGildedBlackstoneChestplate(),
                BlockArmor.createGildedBlackstoneLeggings(), BlockArmor.createGildedBlackstoneBoots()),
            new ArmorSetDisplay(Items.GRANITE, BlockWeapons.createGraniteSword(),
                BlockArmor.createGraniteHelmet(), BlockArmor.createGraniteChestplate(),
                BlockArmor.createGraniteLeggings(), BlockArmor.createGraniteBoots()),
            new ArmorSetDisplay(Items.DIORITE, BlockWeapons.createDioriteSword(),
                BlockArmor.createDioriteHelmet(), BlockArmor.createDioriteChestplate(),
                BlockArmor.createDioriteLeggings(), BlockArmor.createDioriteBoots()),
            new ArmorSetDisplay(Items.ANDESITE, BlockWeapons.createAndesiteSword(),
                BlockArmor.createAndesiteHelmet(), BlockArmor.createAndesiteChestplate(),
                BlockArmor.createAndesiteLeggings(), BlockArmor.createAndesiteBoots()),
            new ArmorSetDisplay(Items.POLISHED_GRANITE, BlockWeapons.createPolishedGraniteSword(),
                BlockArmor.createPolishedGraniteHelmet(), BlockArmor.createPolishedGraniteChestplate(),
                BlockArmor.createPolishedGraniteLeggings(), BlockArmor.createPolishedGraniteBoots()),
            new ArmorSetDisplay(Items.POLISHED_DIORITE, BlockWeapons.createPolishedDioriteSword(),
                BlockArmor.createPolishedDioriteHelmet(), BlockArmor.createPolishedDioriteChestplate(),
                BlockArmor.createPolishedDioriteLeggings(), BlockArmor.createPolishedDioriteBoots()),
            new ArmorSetDisplay(Items.POLISHED_ANDESITE, BlockWeapons.createPolishedAndesiteSword(),
                BlockArmor.createPolishedAndesiteHelmet(), BlockArmor.createPolishedAndesiteChestplate(),
                BlockArmor.createPolishedAndesiteLeggings(), BlockArmor.createPolishedAndesiteBoots()),
            new ArmorSetDisplay(Items.PACKED_ICE, BlockWeapons.createPackedIceSword(),
                BlockArmor.createPackedIceHelmet(), BlockArmor.createPackedIceChestplate(),
                BlockArmor.createPackedIceLeggings(), BlockArmor.createPackedIceBoots()),
            new ArmorSetDisplay(Items.BLUE_ICE, BlockWeapons.createBlueIceSword(),
                BlockArmor.createBlueIceHelmet(), BlockArmor.createBlueIceChestplate(),
                BlockArmor.createBlueIceLeggings(), BlockArmor.createBlueIceBoots()),
            new ArmorSetDisplay(Items.NETHER_GOLD_ORE, BlockWeapons.createNetherGoldOreSword(),
                BlockArmor.createNetherGoldOreHelmet(), BlockArmor.createNetherGoldOreChestplate(),
                BlockArmor.createNetherGoldOreLeggings(), BlockArmor.createNetherGoldOreBoots()),
            new ArmorSetDisplay(Items.DARK_OAK_LOG, BlockWeapons.createDarkOakLogSword(),
                BlockArmor.createDarkOakLogHelmet(), BlockArmor.createDarkOakLogChestplate(),
                BlockArmor.createDarkOakLogLeggings(), BlockArmor.createDarkOakLogBoots()),
            new ArmorSetDisplay(Items.JUNGLE_LOG, BlockWeapons.createJungleLogSword(),
                BlockArmor.createJungleLogHelmet(), BlockArmor.createJungleLogChestplate(),
                BlockArmor.createJungleLogLeggings(), BlockArmor.createJungleLogBoots()),
            new ArmorSetDisplay(Items.ACACIA_LOG, BlockWeapons.createAcaciaLogSword(),
                BlockArmor.createAcaciaLogHelmet(), BlockArmor.createAcaciaLogChestplate(),
                BlockArmor.createAcaciaLogLeggings(), BlockArmor.createAcaciaLogBoots()),
            new ArmorSetDisplay(Items.MANGROVE_LOG, BlockWeapons.createMangroveLogSword(),
                BlockArmor.createMangroveLogHelmet(), BlockArmor.createMangroveLogChestplate(),
                BlockArmor.createMangroveLogLeggings(), BlockArmor.createMangroveLogBoots()),
            new ArmorSetDisplay(Items.CHERRY_LOG, BlockWeapons.createCherryLogSword(),
                BlockArmor.createCherryLogHelmet(), BlockArmor.createCherryLogChestplate(),
                BlockArmor.createCherryLogLeggings(), BlockArmor.createCherryLogBoots()),
            new ArmorSetDisplay(Items.BAMBOO_BLOCK, BlockWeapons.createBambooBlockSword(),
                BlockArmor.createBambooBlockHelmet(), BlockArmor.createBambooBlockChestplate(),
                BlockArmor.createBambooBlockLeggings(), BlockArmor.createBambooBlockBoots()),
            new ArmorSetDisplay(Items.TUFF, BlockWeapons.createTuffSword(),
                BlockArmor.createTuffHelmet(), BlockArmor.createTuffChestplate(),
                BlockArmor.createTuffLeggings(), BlockArmor.createTuffBoots()),
            new ArmorSetDisplay(Items.POLISHED_TUFF, BlockWeapons.createPolishedTuffSword(),
                BlockArmor.createPolishedTuffHelmet(), BlockArmor.createPolishedTuffChestplate(),
                BlockArmor.createPolishedTuffLeggings(), BlockArmor.createPolishedTuffBoots()),
            new ArmorSetDisplay(Items.NETHER_WART_BLOCK, BlockWeapons.createNetherWartBlockSword(),
                BlockArmor.createNetherWartBlockHelmet(), BlockArmor.createNetherWartBlockChestplate(),
                BlockArmor.createNetherWartBlockLeggings(), BlockArmor.createNetherWartBlockBoots()),
            new ArmorSetDisplay(Items.WARPED_WART_BLOCK, BlockWeapons.createWarpedWartBlockSword(),
                BlockArmor.createWarpedWartBlockHelmet(), BlockArmor.createWarpedWartBlockChestplate(),
                BlockArmor.createWarpedWartBlockLeggings(), BlockArmor.createWarpedWartBlockBoots()),
            // STONE/MUD ARMOR SETS
            new ArmorSetDisplay(Items.STONE_BRICKS, BlockWeapons.createStoneBricksSword(),
                BlockArmor.createStoneBricksHelmet(), BlockArmor.createStoneBricksChestplate(),
                BlockArmor.createStoneBricksLeggings(), BlockArmor.createStoneBricksBoots()),
            new ArmorSetDisplay(Items.COBBLESTONE, BlockWeapons.createCobblestoneSword(),
                BlockArmor.createCobblestoneHelmet(), BlockArmor.createCobblestoneChestplate(),
                BlockArmor.createCobblestoneLeggings(), BlockArmor.createCobblestoneBoots()),
            new ArmorSetDisplay(Items.MOSSY_COBBLESTONE, BlockWeapons.createMossyCobblestoneSword(),
                BlockArmor.createMossyCobblestoneHelmet(), BlockArmor.createMossyCobblestoneChestplate(),
                BlockArmor.createMossyCobblestoneLeggings(), BlockArmor.createMossyCobblestoneBoots()),
            new ArmorSetDisplay(Items.COBBLED_DEEPSLATE, BlockWeapons.createCobbledDeepslateSword(),
                BlockArmor.createCobbledDeepslateHelmet(), BlockArmor.createCobbledDeepslateChestplate(),
                BlockArmor.createCobbledDeepslateLeggings(), BlockArmor.createCobbledDeepslateBoots()),
            new ArmorSetDisplay(Items.MUD_BRICKS, BlockWeapons.createMudBricksSword(),
                BlockArmor.createMudBricksHelmet(), BlockArmor.createMudBricksChestplate(),
                BlockArmor.createMudBricksLeggings(), BlockArmor.createMudBricksBoots()),
            new ArmorSetDisplay(Items.MANGROVE_ROOTS, BlockWeapons.createMangroveRootsSword(),
                BlockArmor.createMangroveRootsHelmet(), BlockArmor.createMangroveRootsChestplate(),
                BlockArmor.createMangroveRootsLeggings(), BlockArmor.createMangroveRootsBoots()),
            new ArmorSetDisplay(Items.MUDDY_MANGROVE_ROOTS, BlockWeapons.createMuddyMangroveRootsSword(),
                BlockArmor.createMuddyMangroveRootsHelmet(), BlockArmor.createMuddyMangroveRootsChestplate(),
                BlockArmor.createMuddyMangroveRootsLeggings(), BlockArmor.createMuddyMangroveRootsBoots()),
            // COPPER/NETHERITE/FROGLIGHT ARMOR SETS
            new ArmorSetDisplay(Items.NETHERITE_BLOCK, BlockWeapons.createNetheriteBlockSword(),
                BlockArmor.createNetheriteBlockHelmet(), BlockArmor.createNetheriteBlockChestplate(),
                BlockArmor.createNetheriteBlockLeggings(), BlockArmor.createNetheriteBlockBoots()),
            new ArmorSetDisplay(Items.CHISELED_COPPER, BlockWeapons.createChiseledCopperSword(),
                BlockArmor.createChiseledCopperHelmet(), BlockArmor.createChiseledCopperChestplate(),
                BlockArmor.createChiseledCopperLeggings(), BlockArmor.createChiseledCopperBoots()),
            new ArmorSetDisplay(Items.CUT_COPPER, BlockWeapons.createCutCopperSword(),
                BlockArmor.createCutCopperHelmet(), BlockArmor.createCutCopperChestplate(),
                BlockArmor.createCutCopperLeggings(), BlockArmor.createCutCopperBoots()),
            new ArmorSetDisplay(Items.EXPOSED_COPPER, BlockWeapons.createExposedCopperSword(),
                BlockArmor.createExposedCopperHelmet(), BlockArmor.createExposedCopperChestplate(),
                BlockArmor.createExposedCopperLeggings(), BlockArmor.createExposedCopperBoots()),
            new ArmorSetDisplay(Items.WEATHERED_COPPER, BlockWeapons.createWeatheredCopperSword(),
                BlockArmor.createWeatheredCopperHelmet(), BlockArmor.createWeatheredCopperChestplate(),
                BlockArmor.createWeatheredCopperLeggings(), BlockArmor.createWeatheredCopperBoots()),
            new ArmorSetDisplay(Items.OXIDIZED_COPPER, BlockWeapons.createOxidisedCopperSword(),
                BlockArmor.createOxidisedCopperHelmet(), BlockArmor.createOxidisedCopperChestplate(),
                BlockArmor.createOxidisedCopperLeggings(), BlockArmor.createOxidisedCopperBoots()),
            new ArmorSetDisplay(Items.WAXED_CUT_COPPER, BlockWeapons.createWaxedCutCopperSword(),
                BlockArmor.createWaxedCutCopperHelmet(), BlockArmor.createWaxedCutCopperChestplate(),
                BlockArmor.createWaxedCutCopperLeggings(), BlockArmor.createWaxedCutCopperBoots()),
            new ArmorSetDisplay(Items.POLISHED_BASALT, BlockWeapons.createPolishedBasaltSword(),
                BlockArmor.createPolishedBasaltHelmet(), BlockArmor.createPolishedBasaltChestplate(),
                BlockArmor.createPolishedBasaltLeggings(), BlockArmor.createPolishedBasaltBoots()),
            new ArmorSetDisplay(Items.VERDANT_FROGLIGHT, BlockWeapons.createVerdantFroglightSword(),
                BlockArmor.createVerdantFroglightHelmet(), BlockArmor.createVerdantFroglightChestplate(),
                BlockArmor.createVerdantFroglightLeggings(), BlockArmor.createVerdantFroglightBoots()),
            new ArmorSetDisplay(Items.PEARLESCENT_FROGLIGHT, BlockWeapons.createPearlescentFroglightSword(),
                BlockArmor.createPearlescentFroglightHelmet(), BlockArmor.createPearlescentFroglightChestplate(),
                BlockArmor.createPearlescentFroglightLeggings(), BlockArmor.createPearlescentFroglightBoots()),
            new ArmorSetDisplay(Items.OCHRE_FROGLIGHT, BlockWeapons.createOchreFroglightSword(),
                BlockArmor.createOchreFroglightHelmet(), BlockArmor.createOchreFroglightChestplate(),
                BlockArmor.createOchreFroglightLeggings(), BlockArmor.createOchreFroglightBoots()),
            // NUGGET ARMOR SETS
            new ArmorSetDisplay(Items.IRON_NUGGET, BlockWeapons.createIronNuggetSword(),
                BlockArmor.createIronNuggetHelmet(), BlockArmor.createIronNuggetChestplate(),
                BlockArmor.createIronNuggetLeggings(), BlockArmor.createIronNuggetBoots()),
            new ArmorSetDisplay(Items.GOLD_NUGGET, BlockWeapons.createGoldNuggetSword(),
                BlockArmor.createGoldNuggetHelmet(), BlockArmor.createGoldNuggetChestplate(),
                BlockArmor.createGoldNuggetLeggings(), BlockArmor.createGoldNuggetBoots()),
            // LOG ARMOR SETS
            new ArmorSetDisplay(Items.OAK_LOG, BlockWeapons.createOakLogSword(),
                BlockArmor.createOakLogHelmet(), BlockArmor.createOakLogChestplate(),
                BlockArmor.createOakLogLeggings(), BlockArmor.createOakLogBoots()),
            new ArmorSetDisplay(Items.SPRUCE_LOG, BlockWeapons.createSpruceLogSword(),
                BlockArmor.createSpruceLogHelmet(), BlockArmor.createSpruceLogChestplate(),
                BlockArmor.createSpruceLogLeggings(), BlockArmor.createSpruceLogBoots()),
            new ArmorSetDisplay(Items.BIRCH_LOG, BlockWeapons.createBirchLogSword(),
                BlockArmor.createBirchLogHelmet(), BlockArmor.createBirchLogChestplate(),
                BlockArmor.createBirchLogLeggings(), BlockArmor.createBirchLogBoots()),
            new ArmorSetDisplay(Items.LODESTONE, BlockWeapons.createLodestoneSword(),
                BlockArmor.createLodestoneHelmet(), BlockArmor.createLodestoneChestplate(),
                BlockArmor.createLodestoneLeggings(), BlockArmor.createLodestoneBoots()),
            new ArmorSetDisplay(Items.BLACKSTONE, BlockWeapons.createBlackstoneBricksSword(),
                BlockArmor.createBlackstoneBricksHelmet(), BlockArmor.createBlackstoneBricksChestplate(),
                BlockArmor.createBlackstoneBricksLeggings(), BlockArmor.createBlackstoneBricksBoots()),
            new ArmorSetDisplay(Items.POLISHED_BLACKSTONE, BlockWeapons.createPolishedBlackstoneSword(),
                BlockArmor.createPolishedBlackstoneHelmet(), BlockArmor.createPolishedBlackstoneChestplate(),
                BlockArmor.createPolishedBlackstoneLeggings(), BlockArmor.createPolishedBlackstoneBoots()),
            new ArmorSetDisplay(Items.SMOOTH_BASALT, BlockWeapons.createSmoothBasaltSword(),
                BlockArmor.createSmoothBasaltHelmet(), BlockArmor.createSmoothBasaltChestplate(),
                BlockArmor.createSmoothBasaltLeggings(), BlockArmor.createSmoothBasaltBoots()),
            new ArmorSetDisplay(Items.AMETHYST_CLUSTER, BlockWeapons.createAmethystClusterSword(),
                BlockArmor.createAmethystClusterHelmet(), BlockArmor.createAmethystClusterChestplate(),
                BlockArmor.createAmethystClusterLeggings(), BlockArmor.createAmethystClusterBoots()),
            // NEW 25 MATERIAL ARMOR SETS
            new ArmorSetDisplay(Items.PRISMARINE_SHARD, BlockWeapons.createRawPrismarineSword(),
                BlockArmor.createRawPrismarineHelmet(), BlockArmor.createRawPrismarineChestplate(),
                BlockArmor.createRawPrismarineLeggings(), BlockArmor.createRawPrismarineBoots()),
            new ArmorSetDisplay(Items.CLAY_BALL, BlockWeapons.createClaySword(),
                BlockArmor.createClayHelmet(), BlockArmor.createClayChestplate(),
                BlockArmor.createClayLeggings(), BlockArmor.createClayBoots()),
            new ArmorSetDisplay(Items.WHITE_GLAZED_TERRACOTTA, BlockWeapons.createGlazedTerracottaSword(),
                BlockArmor.createGlazedTerracottaHelmet(), BlockArmor.createGlazedTerracottaChestplate(),
                BlockArmor.createGlazedTerracottaLeggings(), BlockArmor.createGlazedTerracottaBoots()),
            new ArmorSetDisplay(Items.WHITE_CONCRETE, BlockWeapons.createConcreteSword(),
                BlockArmor.createConcreteHelmet(), BlockArmor.createConcreteChestplate(),
                BlockArmor.createConcreteLeggings(), BlockArmor.createConcreteBoots()),
            new ArmorSetDisplay(Items.WHITE_CONCRETE_POWDER, BlockWeapons.createConcretePowderSword(),
                BlockArmor.createConcretePowderHelmet(), BlockArmor.createConcretePowderChestplate(),
                BlockArmor.createConcretePowderLeggings(), BlockArmor.createConcretePowderBoots()),
            new ArmorSetDisplay(Items.BOOKSHELF, BlockWeapons.createBookshelfSword(),
                BlockArmor.createBookshelfHelmet(), BlockArmor.createBookshelfChestplate(),
                BlockArmor.createBookshelfLeggings(), BlockArmor.createBookshelfBoots()),
            new ArmorSetDisplay(Items.BARREL, BlockWeapons.createBarrelSword(),
                BlockArmor.createBarrelHelmet(), BlockArmor.createBarrelChestplate(),
                BlockArmor.createBarrelLeggings(), BlockArmor.createBarrelBoots()),
            new ArmorSetDisplay(Items.SMITHING_TABLE, BlockWeapons.createSmithingTableSword(),
                BlockArmor.createSmithingTableHelmet(), BlockArmor.createSmithingTableChestplate(),
                BlockArmor.createSmithingTableLeggings(), BlockArmor.createSmithingTableBoots()),
            new ArmorSetDisplay(Items.ANVIL, BlockWeapons.createAnvilSword(),
                BlockArmor.createAnvilHelmet(), BlockArmor.createAnvilChestplate(),
                BlockArmor.createAnvilLeggings(), BlockArmor.createAnvilBoots()),
            new ArmorSetDisplay(Items.GRINDSTONE, BlockWeapons.createGrindstoneSword(),
                BlockArmor.createGrindstoneHelmet(), BlockArmor.createGrindstoneChestplate(),
                BlockArmor.createGrindstoneLeggings(), BlockArmor.createGrindstoneBoots()),
            new ArmorSetDisplay(Items.LECTERN, BlockWeapons.createLecternSword(),
                BlockArmor.createLecternHelmet(), BlockArmor.createLecternChestplate(),
                BlockArmor.createLecternLeggings(), BlockArmor.createLecternBoots()),
            new ArmorSetDisplay(Items.COMPOSTER, BlockWeapons.createComposterSword(),
                BlockArmor.createComposterHelmet(), BlockArmor.createComposterChestplate(),
                BlockArmor.createComposterLeggings(), BlockArmor.createComposterBoots()),
            new ArmorSetDisplay(Items.STONECUTTER, BlockWeapons.createStonecutterSword(),
                BlockArmor.createStonecutterHelmet(), BlockArmor.createStonecutterChestplate(),
                BlockArmor.createStonecutterLeggings(), BlockArmor.createStonecutterBoots()),
            new ArmorSetDisplay(Items.DRIED_KELP_BLOCK, BlockWeapons.createDriedKelpSword(),
                BlockArmor.createDriedKelpHelmet(), BlockArmor.createDriedKelpChestplate(),
                BlockArmor.createDriedKelpLeggings(), BlockArmor.createDriedKelpBoots()),
            new ArmorSetDisplay(Items.BRAIN_CORAL_BLOCK, BlockWeapons.createCoralBlockSword(),
                BlockArmor.createCoralBlockHelmet(), BlockArmor.createCoralBlockChestplate(),
                BlockArmor.createCoralBlockLeggings(), BlockArmor.createCoralBlockBoots()),
            new ArmorSetDisplay(Items.LANTERN, BlockWeapons.createLanternSword(),
                BlockArmor.createLanternHelmet(), BlockArmor.createLanternChestplate(),
                BlockArmor.createLanternLeggings(), BlockArmor.createLanternBoots()),
            new ArmorSetDisplay(Items.IRON_BARS, BlockWeapons.createChainSword(),
                BlockArmor.createChainHelmet(), BlockArmor.createChainChestplate(),
                BlockArmor.createChainLeggings(), BlockArmor.createChainBoots()),
            new ArmorSetDisplay(Items.LIGHTNING_ROD, BlockWeapons.createLightningRodSword(),
                BlockArmor.createLightningRodHelmet(), BlockArmor.createLightningRodChestplate(),
                BlockArmor.createLightningRodLeggings(), BlockArmor.createLightningRodBoots()),
            new ArmorSetDisplay(Items.TINTED_GLASS, BlockWeapons.createTintedGlassSword(),
                BlockArmor.createTintedGlassHelmet(), BlockArmor.createTintedGlassChestplate(),
                BlockArmor.createTintedGlassLeggings(), BlockArmor.createTintedGlassBoots()),
            new ArmorSetDisplay(Items.CANDLE, BlockWeapons.createCandleSword(),
                BlockArmor.createCandleHelmet(), BlockArmor.createCandleChestplate(),
                BlockArmor.createCandleLeggings(), BlockArmor.createCandleBoots()),
            new ArmorSetDisplay(Items.SCULK_SENSOR, BlockWeapons.createSculkSensorSword(),
                BlockArmor.createSculkSensorHelmet(), BlockArmor.createSculkSensorChestplate(),
                BlockArmor.createSculkSensorLeggings(), BlockArmor.createSculkSensorBoots()),
            new ArmorSetDisplay(Items.SCULK_CATALYST, BlockWeapons.createSculkCatalystSword(),
                BlockArmor.createSculkCatalystHelmet(), BlockArmor.createSculkCatalystChestplate(),
                BlockArmor.createSculkCatalystLeggings(), BlockArmor.createSculkCatalystBoots()),
            new ArmorSetDisplay(Items.SCULK_SHRIEKER, BlockWeapons.createSculkShriekerSword(),
                BlockArmor.createSculkShriekerHelmet(), BlockArmor.createSculkShriekerChestplate(),
                BlockArmor.createSculkShriekerLeggings(), BlockArmor.createSculkShriekerBoots()),
            new ArmorSetDisplay(Items.BREWING_STAND, BlockWeapons.createBrewingStandSword(),
                BlockArmor.createBrewingStandHelmet(), BlockArmor.createBrewingStandChestplate(),
                BlockArmor.createBrewingStandLeggings(), BlockArmor.createBrewingStandBoots()),
            new ArmorSetDisplay(Items.FURNACE, BlockWeapons.createFurnaceSword(),
                BlockArmor.createFurnaceHelmet(), BlockArmor.createFurnaceChestplate(),
                BlockArmor.createFurnaceLeggings(), BlockArmor.createFurnaceBoots()),
        };

        final int SETS_PER_PAGE = 4;
        ArmorSetDisplay[] sets = allSets;
        
        int totalPages = (sets.length + SETS_PER_PAGE - 1) / SETS_PER_PAGE;
        int clampedPage = Math.min(page, totalPages - 1);

        int startIdx = clampedPage * SETS_PER_PAGE;
        for (int i = 0; i < SETS_PER_PAGE && (startIdx + i) < sets.length; i++) {
            ArmorSetDisplay set = sets[startIdx + i];
            int row = i + 1; // Rows 1-4 (slots 9-44)

            // Row layout: [0:Block] [1:gap] [2:Sword] [3:Helmet] [4:Chest] [5:Legs] [6:Boots]
            int[] slots = { row * 9, row * 9 + 1, row * 9 + 2, row * 9 + 3, row * 9 + 4, row * 9 + 5, row * 9 + 6 };

            // Block (decorative background)
            gui.setSlot(slots[0], new GuiElementBuilder(set.blockItem)
                    .setName(Component.literal("§8" + set.blockItem.toString().replace("_", " ")).withStyle(ChatFormatting.DARK_GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Armor Set Material").withStyle(ChatFormatting.GRAY))
                    .build());
            
            // Gap slot (shifted armor 1 to the right)
            gui.setSlot(slots[1], new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());

            // Sword
            Component swordName = set.sword.get(DataComponents.CUSTOM_NAME);
            gui.setSlot(slots[2], new GuiElementBuilder(set.sword.copy())
                    .setName(swordName != null ? swordName : Component.literal("Sword"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("✨ Click to get item").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> onItemClick.onClick(player, set.sword))
                    .build());

            // Armor pieces: Helmet, Chestplate, Leggings, Boots (shifted 1 slot right)
            ItemStack[] armorPieces = { set.helmet, set.chestplate, set.leggings, set.boots };
            for (int j = 0; j < 4; j++) {
                ItemStack armor = armorPieces[j];
                Component armorName = armor.get(DataComponents.CUSTOM_NAME);
                final ItemStack finalArmor = armor;
                gui.setSlot(slots[3 + j], new GuiElementBuilder(armor.copy())
                        .setName(armorName != null ? armorName : Component.literal("Armor"))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("✨ Click to get item").withStyle(ChatFormatting.YELLOW))
                        .setCallback((s, t, a, __slotGui) -> onItemClick.onClick(player, finalArmor))
                        .build());
            }
        }

        // ← Back to main menu
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Categories").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> onBack.open(player)).build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Page " + (clampedPage + 1) + " / " + totalPages))
                    .setCallback((s, t, a, __slotGui) -> openGemBlockGearPage(player, prev, onItemClick, onBack)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((s, t, a, __slotGui) -> openGemBlockGearPage(player, next, onItemClick, onBack)).build());
        }

        gui.setSlot(50, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Page " + (clampedPage + 1) + " / " + totalPages))
                .build());

        gui.open();
    }

    // Helper class for armor set display
    private record ArmorSetDisplay(net.minecraft.world.item.Item blockItem, ItemStack sword, ItemStack helmet,
                                    ItemStack chestplate, ItemStack leggings, ItemStack boots) {}

    // ──────────────────────────────────────────────────────────────
    // Page 1: Category Item List (Standard Layout)
    // ──────────────────────────────────────────────────────────────

    public static void openCategoryPage(ServerPlayer player, Category cat, int page) {
        openCategoryPage(player, cat, page, (p, item) -> giveItem(p, item), CustomItemsGui::open);
    }

    public static void openCategoryPage(ServerPlayer player, Category cat, int page, BountyItemClickHandler onItemClick, BackHandler onBack) {
        if (cat == Category.CUSTOM_ARMOR) {
            openGemBlockGearPage(player, page, onItemClick, onBack);
            return;
        }
        if (cat == Category.CUSTOM_TOOLS) {
            openToolSetsPage(player, page, onItemClick, onBack);
            return;
        }

        List<ItemStack> items = getItemsForCategory(cat);

        boolean sparse = RecipeCatalogUi.sparseMinTwoPagesCustom(cat);
        int totalPages = RecipeCatalogUi.totalPages(items.size(), sparse);
        int clampedPage = Math.min(page, totalPages - 1);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        boolean skyblockSpecial = cat == Category.SPECIAL_ITEMS;
        gui.setTitle(Component.literal(skyblockSpecial ? "Skyblock-style trinkets" : cat.label)
                .withStyle(skyblockSpecial ? ChatFormatting.GOLD : cat.colour, ChatFormatting.BOLD));

        if (cat == Category.REFLECT_BLOCK_WEAPONS) {
            for (int i = 0; i < 54; i++) {
                gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
            }
        } else {
            RecipeGuiTheme.fillCategoryItemGrid(gui, skyblockSpecial);
        }

        boolean[] used = new boolean[54];

        // Header
        gui.setSlot(4, new GuiElementBuilder(cat.icon)
                .setName(Component.literal(cat.label).withStyle(cat.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(skyblockSpecial
                        ? "Consumables & misc — right-click effects. Recipes in /recipe."
                        : "Click an item to view recipe").withStyle(ChatFormatting.GRAY))
                .glow().build());
        used[4] = true;

        // Item slots (7 per row, rows 1-4, columns 1-7)
        int start = clampedPage * RecipeCatalogUi.ITEM_SLOTS_PER_PAGE;
        int shown = 0;
        for (int idx = start; idx < items.size() && shown < RecipeCatalogUi.ITEM_SLOTS_PER_PAGE; idx++) {
            ItemStack item = items.get(idx);
            int row = shown / 7;
            int col = shown % 7 + 1;
            int slot = (row + 1) * 9 + col;

            Component displayName = item.get(DataComponents.CUSTOM_NAME);

            final ItemStack finalItem = item;
            ItemStack displayStack = CustomItemLore.forSelectPageIcon(item);
            GuiElementBuilder elem = new GuiElementBuilder(displayStack)
                    .setName(displayName != null ? displayName
                            : Component.literal(item.getItem().toString()))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("✨ Click to get item").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> onItemClick.onClick(player, finalItem));
            gui.setSlot(slot, elem.build());
            used[slot] = true;
            shown++;
        }

        boolean lastPageComingSoon = sparse && clampedPage == totalPages - 1;
        RecipeCatalogUi.fillEmptyGridSlots(gui, used, lastPageComingSoon);

        // ← Back to main menu
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Categories").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> onBack.open(player)).build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Page " + clampedPage + " / " + totalPages))
                    .setCallback((s, t, a, __slotGui) -> openCategoryPage(player, cat, prev, onItemClick, onBack)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((s, t, a, __slotGui) -> openCategoryPage(player, cat, next, onItemClick, onBack)).build());
        }

        gui.setSlot(50, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Page " + (clampedPage + 1) + " / " + totalPages))
                .build());

        gui.open();
    }

    // ──────────────────────────────────────────────────────────────
    // RECIPE VIEW - Show item recipe in a GUI
    // ──────────────────────────────────────────────────────────────

    public static void showItemInfo(ServerPlayer player, ItemStack resultItem) {
        showItemInfo(player, resultItem, CustomItemsGui::open);
    }

    public static void showItemInfo(ServerPlayer player, ItemStack resultItem, BackHandler onBack) {
        showItemInfo(player, resultItem, onBack, null);
    }

    public static void showItemInfo(ServerPlayer player, ItemStack resultItem, BackHandler onBack, SlayerRecipes.Recipe recipe) {
        int start = 0;
        if (recipe != null) {
            List<SlayerRecipes.Recipe> all = RecipeOutputIndex.listRecipesForOutput(resultItem);
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).name.equals(recipe.name)) {
                    start = i;
                    break;
                }
            }
        }
        showItemInfoVariant(player, resultItem, onBack, start);
    }

    /** Recipe browser (/recipe): supports multiple ingredient sets per output. */
    public static void showItemInfoForRecipeBrowser(ServerPlayer player, ItemStack resultItem, BackHandler onBack, int variantIndex) {
        showItemInfoVariant(player, resultItem, onBack, variantIndex);
    }

    private static void showItemInfoVariant(ServerPlayer player, ItemStack resultItem, BackHandler onBack, int variantIndex) {
        List<SlayerRecipes.Recipe> allRecipes = RecipeOutputIndex.listRecipesForOutput(resultItem);
        int idx = allRecipes.isEmpty() ? 0 : Math.floorMod(variantIndex, allRecipes.size());
        SlayerRecipes.Recipe finalRecipe = allRecipes.isEmpty() ? null : allRecipes.get(idx);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Recipe").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        RecipeGuiTheme.fillDoubleChestRecipeFrame(gui);

        // Header - Item name
        final Component displayName;
        Component name = resultItem.get(DataComponents.CUSTOM_NAME);
        if (name == null) {
            displayName = Component.literal(resultItem.getItem().toString());
        } else {
            displayName = name;
        }
        ItemStack headerStack = resultItem.copy();
        GuiElementBuilder head = new GuiElementBuilder(headerStack)
                .setName(displayName)
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Crafting Recipe").withStyle(ChatFormatting.GRAY));
        if (allRecipes.size() > 1) {
            head.addLoreLine(Component.literal("Variant " + (idx + 1) + " / " + allRecipes.size()).withStyle(ChatFormatting.AQUA));
            if (finalRecipe != null) {
                head.addLoreLine(Component.literal(finalRecipe.name).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        gui.setSlot(4, head.glow().build());

        if (allRecipes.size() > 1) {
            gui.setSlot(1, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Prev recipe").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> showItemInfoVariant(player, resultItem, onBack, idx - 1))
                    .build());
            gui.setSlot(7, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next recipe").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> showItemInfoVariant(player, resultItem, onBack, idx + 1))
                    .build());
        }

        // Recipe grid slots (3x3) centered in double chest
        int[] gridSlots = {19, 20, 21, 28, 29, 30, 37, 38, 39};

        if (finalRecipe != null && finalRecipe.ingredients != null) {
            // Display the actual recipe
            for (int i = 0; i < 9 && i < finalRecipe.ingredients.length; i++) {
                ItemStack input = finalRecipe.ingredients[i];
                if (input != null && !input.isEmpty()) {
                    ItemStack ing = input.copy();
                    ing.setCount(input.getCount());
                    Component inputName = input.get(DataComponents.CUSTOM_NAME);
                    if (inputName == null) {
                        inputName = Component.literal(input.getItem().toString());
                    }
                    gui.setSlot(gridSlots[i], new GuiElementBuilder(ing)
                            .setName(inputName)
                            .build());
                } else {
                    RecipeGuiTheme.setRecipeEmptyGridSlot(gui, gridSlots[i]);
                }
            }

            // Arrow indicator
            gui.setSlot(32, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("→").withStyle(ChatFormatting.YELLOW))
                    .build());

            // Result (full stack: name, lore, attributes, glint)
            gui.setSlot(33, new GuiElementBuilder(resultItem.copy())
                    .setName(displayName)
                    .glow().build());

            // Instant craft button - check if player has materials
            final SlayerRecipes.Recipe recipeForCraft = finalRecipe;
            boolean canCraft = checkCanCraft(player, finalRecipe);
            if (canCraft) {
                gui.setSlot(31, new GuiElementBuilder(Items.LIME_CONCRETE)
                        .setName(Component.literal("⚡ Instant Craft").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("Click to craft instantly!").withStyle(ChatFormatting.YELLOW))
                        .addLoreLine(Component.literal("Materials will be taken from your inventory").withStyle(ChatFormatting.GRAY))
                        .setCallback((slot, type, containerInput, slotGui) -> {
                            if (instantCraft(player, recipeForCraft, resultItem)) {
                                player.sendSystemMessage(Component.literal("✅ Crafted " + displayName.getString() + "!").withStyle(ChatFormatting.GREEN), false);
                                showItemInfoVariant(player, resultItem, onBack, idx);
                            } else {
                                player.sendSystemMessage(Component.literal("❌ Not enough materials!").withStyle(ChatFormatting.RED), false);
                            }
                        })
                        .build());
            } else {
                gui.setSlot(31, new GuiElementBuilder(Items.RED_CONCRETE)
                        .setName(Component.literal("⚠ Cannot Craft").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("You don't have the required materials").withStyle(ChatFormatting.GRAY))
                        .build());
            }
        } else {
            // No recipe found - show message
            gui.setSlot(31, new GuiElementBuilder(Items.BARRIER)
                    .setName(Component.literal("No recipe found").withStyle(ChatFormatting.RED))
                    .addLoreLine(Component.literal("This item may be obtained"))
                    .addLoreLine(Component.literal("through other means."))
                    .build());
        }

        // Back button
        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Menu").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> onBack.open(player))
                .build());

        gui.open();
    }

    private static boolean checkCanCraft(ServerPlayer player, SlayerRecipes.Recipe recipe) {
        if (recipe == null || recipe.ingredients == null) return false;
        
        for (ItemStack ingredient : recipe.ingredients) {
            if (ingredient == null || ingredient.isEmpty()) continue;
            
            int needed = ingredient.getCount();
            int found = 0;
            
            // Check player inventory using getInventory() method
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack invItem = player.getInventory().getItem(i);
                if (invItem != null && !invItem.isEmpty() && invItem.getItem() == ingredient.getItem()) {
                    found += invItem.getCount();
                }
            }
            
            if (found < needed) return false;
        }
        return true;
    }

    private static boolean instantCraft(ServerPlayer player, SlayerRecipes.Recipe recipe, ItemStack resultItem) {
        if (!checkCanCraft(player, recipe)) return false;
        
        // Remove ingredients from player inventory
        for (ItemStack ingredient : recipe.ingredients) {
            if (ingredient == null || ingredient.isEmpty()) continue;
            
            int needed = ingredient.getCount();
            
            // Remove from inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack invItem = player.getInventory().getItem(i);
                if (invItem != null && !invItem.isEmpty() && invItem.getItem() == ingredient.getItem()) {
                    if (invItem.getCount() <= needed) {
                        needed -= invItem.getCount();
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                    } else {
                        invItem.shrink(needed);
                        needed = 0;
                    }
                    if (needed == 0) break;
                }
            }
        }
        
        // Give result to player
        ItemStack result = resultItem.copy();
        player.addItem(result);
        PoliticalPartyManager.progressPartyCommission(player, "CRAFT_ITEMS", 1);
        
        return true;
    }

    // Find recipe for output item
    private static SlayerRecipes.Recipe findRecipeForOutput(ItemStack output) {
        if (output == null || output.isEmpty()) return null;

        String outputId = SlayerItems.getCustomItemId(output);

        // Check all recipes
        for (SlayerRecipes.Recipe recipe : RecipeConfigManager.getRecipes()) {
            if (recipe.result == null || recipe.result.isEmpty()) continue;

            // Try to match by custom item ID first
            if (outputId != null) {
                String recipeId = SlayerItems.getCustomItemId(recipe.result);
                if (outputId.equals(recipeId)) {
                    if (hasValidIngredients(recipe)) {
                        return recipe;
                    }
                }
            }

            // Fallback: match by item type and custom name
            if (recipe.result.getItem() == output.getItem()) {
                Component recipeName = recipe.result.get(DataComponents.CUSTOM_NAME);
                Component outputName = output.get(DataComponents.CUSTOM_NAME);
                if (recipeName != null && outputName != null && recipeName.equals(outputName)) {
                    if (hasValidIngredients(recipe)) {
                        return recipe;
                    }
                }
            }
        }
        return null;
    }

    private static boolean hasValidIngredients(SlayerRecipes.Recipe recipe) {
        if (recipe.ingredients == null) return false;
        for (ItemStack ing : recipe.ingredients) {
            if (ing != null && !ing.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
