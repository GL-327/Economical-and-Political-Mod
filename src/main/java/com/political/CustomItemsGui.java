package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for the /customitems admin command.
 * Shows ALL custom items organized by categories for easy access.
 */
public class CustomItemsGui {

    public interface BountyItemClickHandler {
        void onClick(ServerPlayerEntity player, ItemStack item);
    }

    public interface BackHandler {
        void open(ServerPlayerEntity player);
    }

    // ── Category definitions ─────────────────────────────────────
    public enum Category {
        CUSTOM_ARMOR ("💎 Custom Armor",                    Items.EMERALD,            Formatting.GREEN),
        SWORDS_T1    ("⚔ Bounty Swords (T1)",               Items.IRON_SWORD,         Formatting.YELLOW),
        SWORDS_T2    ("⚔ Bounty Swords (T2)",               Items.DIAMOND_SWORD,      Formatting.AQUA),
        ARMOR_T1     ("🛡 Bounty Armor T1",                  Items.IRON_CHESTPLATE,    Formatting.GREEN),
        ARMOR_T2     ("🛡 Bounty Armor T2",                  Items.DIAMOND_CHESTPLATE, Formatting.DARK_AQUA),
        SPECIAL_ARMOR("🛡 Special / Legendary Armor",        Items.NETHERITE_HELMET,   Formatting.GOLD),
        HPEBM        ("⚡ HPEBM Weapons",                    Items.BLAZE_ROD,          Formatting.LIGHT_PURPLE),
        BOSS_DROPS   ("🎁 Boss Drops",                       Items.WITHER_SKELETON_SKULL, Formatting.DARK_PURPLE),
        SPECIAL_ITEMS("🎁 Special Items",                    Items.STICK,              Formatting.GREEN),
        CUSTOM_TOOLS ("⛏ Custom Tools",                    Items.NETHERITE_PICKAXE,  Formatting.DARK_PURPLE),
        PIGLIN       ("👑 Piglin Items",                     Items.GOLDEN_HELMET,      Formatting.GOLD),
        GOLD_ARMOR   ("👑 Gold Armor Evolution",             Items.GOLDEN_CHESTPLATE,  Formatting.GOLD),
        MATERIALS    ("📦 Materials (Cores & Chunks)",       Items.SLIME_BALL,         Formatting.GRAY),
        COMPACTED    ("⬛ Compacted Materials",              Items.RAW_IRON,           Formatting.DARK_GRAY),
        SUPER_COMPACTED("⭐ Super Compacted",               Items.GOLD_BLOCK,         Formatting.GOLD),
        ENCHANTED_BLOCKS("✧ Enchanted Blocks",              Items.COBBLESTONE,        Formatting.LIGHT_PURPLE),
        ENCHANTED_CROPS("✧ Enchanted Crops & Flowers",      Items.WHEAT,              Formatting.GREEN),
        ATTRIBUTE_TOKENS("✧ Attribute Tokens",               Items.PAPER,              Formatting.LIGHT_PURPLE),
        WEAPON_ATTRIBUTES("⚔ Weapon Attributes",               Items.NETHER_STAR,        Formatting.RED),
        BLOCK_TOOLS   ("⛏ Block Tools",                     Items.DIAMOND_PICKAXE,    Formatting.AQUA),
        CUSTOM_ARROWS ("➵ Custom Arrows",                    Items.TIPPED_ARROW,       Formatting.GOLD);

        final String label;
        final net.minecraft.item.Item icon;
        final Formatting colour;

        Category(String label, net.minecraft.item.Item icon, Formatting colour) {
            this.label = label;
            this.icon  = icon;
            this.colour = colour;
        }
    }

    // Helper method to give item to player
    private static void giveItem(ServerPlayerEntity player, ItemStack item) {
        if (player.getInventory().insertStack(item.copy())) {
            Text customName = item.get(DataComponentTypes.CUSTOM_NAME);
            String itemName = customName != null ? customName.getString() : item.getItem().toString();
            player.sendMessage(Text.literal("✅ ")
                    .append(Text.literal(itemName).formatted(Formatting.GREEN))
                    .append(Text.literal(" added to inventory!").formatted(Formatting.GRAY)));
        } else {
            player.sendMessage(Text.literal("❌ Inventory full! Make some space and try again.").formatted(Formatting.RED));
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
                items.add(BlockArmor.createEndStoneHelmet());
                items.add(BlockArmor.createEndStoneChestplate());
                items.add(BlockArmor.createEndStoneLeggings());
                items.add(BlockArmor.createEndStoneBoots());
                items.add(BlockArmor.createSnowBlockHelmet());
                items.add(BlockArmor.createSnowBlockChestplate());
                items.add(BlockArmor.createSnowBlockLeggings());
                items.add(BlockArmor.createSnowBlockBoots());
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
                items.add(BlockWeapons.createEndStoneSword());
                items.add(BlockWeapons.createSnowBlockSword());
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
                items.add(SlayerItems.createSpiderLeggings());
                items.add(SlayerItems.createSkeletonBow());
                items.add(SlayerItems.createSlimeBoots());
                items.add(SlayerItems.createWardenChestplate());
                items.add(SlayerItems.createVoidwalkerCrown());
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
                // DRAGON CHESTPLATES - Special Items (No Recipe)
                items.add(DragonChestplates.createDragonChestplate1());
                items.add(DragonChestplates.createDragonChestplate2());
                items.add(DragonChestplates.createDragonChestplate3());
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
            }
            case COMPACTED -> {
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
            }
            case SUPER_COMPACTED -> {
                items.add(SlayerItems.createSuperCompactedGold());
                items.add(SlayerItems.createSuperCompactedIron());
                items.add(SlayerItems.createSuperCompactedDiamond());
                items.add(SlayerItems.createSuperCompactedEmerald());
                items.add(SlayerItems.createSuperCompactedNetherite());
            }
            case ENCHANTED_BLOCKS -> {
                items.add(SlayerItems.createEnchantedCobblestone());
                items.add(SlayerItems.createEnchantedStone());
                items.add(SlayerItems.createEnchantedIronBlock());
                items.add(SlayerItems.createEnchantedGoldBlock());
                items.add(SlayerItems.createEnchantedDiamondBlock());
                items.add(SlayerItems.createEnchantedEmeraldBlock());
                items.add(SlayerItems.createEnchantedLapisBlock());
                items.add(SlayerItems.createEnchantedCoalBlock());
                items.add(SlayerItems.createEnchantedCopperBlock());
                items.add(SlayerItems.createEnchantedNetheriteBlock());
                items.add(SlayerItems.createEnchantedQuartzBlock());
                items.add(SlayerItems.createEnchantedRedstoneBlock());
                items.add(SlayerItems.createEnchantedBlackstone());
                items.add(SlayerItems.createEnchantedGildedBlackstone());
                items.add(SlayerItems.createEnchantedOakLog());
                items.add(SlayerItems.createEnchantedSand());
                items.add(SlayerItems.createEnchantedRedSand());
                items.add(SlayerItems.createEnchantedGravel());
                items.add(SlayerItems.createEnchantedNetherrack());
                items.add(SlayerItems.createEnchantedEndStone());
                items.add(SlayerItems.createEnchantedObsidian());
                items.add(SlayerItems.createEnchantedGlowstone());
                items.add(SlayerItems.createEnchantedPrismarine());
                items.add(SlayerItems.createEnchantedBasalt());
                items.add(SlayerItems.createEnchantedDeepslate());
                items.add(SlayerItems.createEnchantedCobbledDeepslate());
                items.add(SlayerItems.createEnchantedTuff());
                items.add(SlayerItems.createEnchantedCalcite());
                items.add(SlayerItems.createEnchantedAmethystBlock());
                items.add(SlayerItems.createEnchantedGlass());
                items.add(SlayerItems.createEnchantedIce());
                items.add(SlayerItems.createEnchantedPackedIce());
                items.add(SlayerItems.createEnchantedBlueIce());
                items.add(SlayerItems.createEnchantedTerracotta());
                items.add(SlayerItems.createEnchantedClay());
                items.add(SlayerItems.createEnchantedBrick());
                items.add(SlayerItems.createEnchantedNetherBrick());
                items.add(SlayerItems.createEnchantedPurpurBlock());
                items.add(SlayerItems.createEnchantedSeaLantern());
                items.add(SlayerItems.createEnchantedShroomlight());
            }
            case ENCHANTED_CROPS -> {
                // Enchanted flowers
                items.add(SlayerItems.createEnchantedRose());
                items.add(SlayerItems.createEnchantedDandelion());
                // Enchanted crops
                items.add(SlayerItems.createEnchantedWheat());
                items.add(SlayerItems.createEnchantedCarrot());
                items.add(SlayerItems.createEnchantedPotato());
                items.add(SlayerItems.createEnchantedSugarCane());
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
            case BLOCK_TOOLS -> {
                // Glass tools
                items.add(BlockTools.createGlassPickaxe());
                items.add(BlockTools.createGlassAxe());
                items.add(BlockTools.createGlassShovel());
                items.add(BlockTools.createGlassHoe());
                // Obsidian tools
                items.add(BlockTools.createObsidianPickaxe());
                items.add(BlockTools.createObsidianAxe());
                items.add(BlockTools.createObsidianShovel());
                items.add(BlockTools.createObsidianHoe());
                // Quartz tools
                items.add(BlockTools.createQuartzPickaxe());
                items.add(BlockTools.createQuartzAxe());
                items.add(BlockTools.createQuartzShovel());
                items.add(BlockTools.createQuartzHoe());
                // Glowstone tools
                items.add(BlockTools.createGlowstonePickaxe());
                items.add(BlockTools.createGlowstoneAxe());
                items.add(BlockTools.createGlowstoneShovel());
                items.add(BlockTools.createGlowstoneHoe());
                // Redstone tools
                items.add(BlockTools.createRedstonePickaxe());
                items.add(BlockTools.createRedstoneAxe());
                items.add(BlockTools.createRedstoneShovel());
                items.add(BlockTools.createRedstoneHoe());
                // Netherrack tools
                items.add(BlockTools.createNetherrackPickaxe());
                items.add(BlockTools.createNetherrackAxe());
                items.add(BlockTools.createNetherrackShovel());
                items.add(BlockTools.createNetherrackHoe());
                // End Stone tools
                items.add(BlockTools.createEndstonePickaxe());
                items.add(BlockTools.createEndstoneAxe());
                items.add(BlockTools.createEndstoneShovel());
                items.add(BlockTools.createEndstoneHoe());
                // Ice tools
                items.add(BlockTools.createIcePickaxe());
                items.add(BlockTools.createIceAxe());
                items.add(BlockTools.createIceShovel());
                items.add(BlockTools.createIceHoe());
                // Prismarine tools
                items.add(BlockTools.createPrismarinePickaxe());
                items.add(BlockTools.createPrismarineAxe());
                items.add(BlockTools.createPrismarineShovel());
                items.add(BlockTools.createPrismarineHoe());
                // Terracotta tools
                items.add(BlockTools.createTerracottaPickaxe());
                items.add(BlockTools.createTerracottaAxe());
                items.add(BlockTools.createTerracottaShovel());
                items.add(BlockTools.createTerracottaHoe());
                // Mossy tools
                items.add(BlockTools.createMossyPickaxe());
                items.add(BlockTools.createMossyAxe());
                items.add(BlockTools.createMossyShovel());
                items.add(BlockTools.createMossyHoe());
                // Soul Sand tools
                items.add(BlockTools.createSoulSandPickaxe());
                items.add(BlockTools.createSoulSandAxe());
                items.add(BlockTools.createSoulSandShovel());
                items.add(BlockTools.createSoulSandHoe());
                // Magma tools
                items.add(BlockTools.createMagmaPickaxe());
                items.add(BlockTools.createMagmaAxe());
                items.add(BlockTools.createMagmaShovel());
                items.add(BlockTools.createMagmaHoe());
                // Sandstone tools
                items.add(BlockTools.createSandstonePickaxe());
                items.add(BlockTools.createSandstoneAxe());
                items.add(BlockTools.createSandstoneShovel());
                items.add(BlockTools.createSandstoneHoe());
                // Amethyst tools
                items.add(BlockTools.createAmethystPickaxe());
                items.add(BlockTools.createAmethystAxe());
                items.add(BlockTools.createAmethystShovel());
                items.add(BlockTools.createAmethystHoe());
                // Coal tools
                items.add(BlockTools.createCoalPickaxe());
                items.add(BlockTools.createCoalAxe());
                items.add(BlockTools.createCoalShovel());
                items.add(BlockTools.createCoalHoe());
                // 36+ NEW BLOCK TOOLS
                items.add(BlockTools.createDiamondBlockPickaxe());
                items.add(BlockTools.createDiamondBlockAxe());
                items.add(BlockTools.createDiamondBlockShovel());
                items.add(BlockTools.createDiamondBlockHoe());
                items.add(BlockTools.createEmeraldBlockPickaxe());
                items.add(BlockTools.createEmeraldBlockAxe());
                items.add(BlockTools.createEmeraldBlockShovel());
                items.add(BlockTools.createEmeraldBlockHoe());
                items.add(BlockTools.createGoldBlockPickaxe());
                items.add(BlockTools.createGoldBlockAxe());
                items.add(BlockTools.createGoldBlockShovel());
                items.add(BlockTools.createGoldBlockHoe());
                items.add(BlockTools.createIronBlockPickaxe());
                items.add(BlockTools.createIronBlockAxe());
                items.add(BlockTools.createIronBlockShovel());
                items.add(BlockTools.createIronBlockHoe());
                items.add(BlockTools.createLapisBlockPickaxe());
                items.add(BlockTools.createLapisBlockAxe());
                items.add(BlockTools.createLapisBlockShovel());
                items.add(BlockTools.createLapisBlockHoe());
                items.add(BlockTools.createCopperBlockPickaxe());
                items.add(BlockTools.createCopperBlockAxe());
                items.add(BlockTools.createCopperBlockShovel());
                items.add(BlockTools.createCopperBlockHoe());
                items.add(BlockTools.createAncientDebrisPickaxe());
                items.add(BlockTools.createAncientDebrisAxe());
                items.add(BlockTools.createAncientDebrisShovel());
                items.add(BlockTools.createAncientDebrisHoe());
                items.add(BlockTools.createBasaltPickaxe());
                items.add(BlockTools.createBasaltAxe());
                items.add(BlockTools.createBasaltShovel());
                items.add(BlockTools.createBasaltHoe());
                items.add(BlockTools.createBlackstonePickaxe());
                items.add(BlockTools.createBlackstoneAxe());
                items.add(BlockTools.createBlackstoneShovel());
                items.add(BlockTools.createBlackstoneHoe());
                items.add(BlockTools.createBoneBlockPickaxe());
                items.add(BlockTools.createBoneBlockAxe());
                items.add(BlockTools.createBoneBlockShovel());
                items.add(BlockTools.createBoneBlockHoe());
                items.add(BlockTools.createBrickPickaxe());
                items.add(BlockTools.createBrickAxe());
                items.add(BlockTools.createBrickShovel());
                items.add(BlockTools.createBrickHoe());
                items.add(BlockTools.createCactusPickaxe());
                items.add(BlockTools.createCactusAxe());
                items.add(BlockTools.createCactusShovel());
                items.add(BlockTools.createCactusHoe());
                items.add(BlockTools.createCalcitePickaxe());
                items.add(BlockTools.createCalciteAxe());
                items.add(BlockTools.createCalciteShovel());
                items.add(BlockTools.createCalciteHoe());
                items.add(BlockTools.createDeepslatePickaxe());
                items.add(BlockTools.createDeepslateAxe());
                items.add(BlockTools.createDeepslateShovel());
                items.add(BlockTools.createDeepslateHoe());
                items.add(BlockTools.createDripstonePickaxe());
                items.add(BlockTools.createDripstoneAxe());
                items.add(BlockTools.createDripstoneShovel());
                items.add(BlockTools.createDripstoneHoe());
                items.add(BlockTools.createHayPickaxe());
                items.add(BlockTools.createHayAxe());
                items.add(BlockTools.createHayShovel());
                items.add(BlockTools.createHayHoe());
                items.add(BlockTools.createHoneycombPickaxe());
                items.add(BlockTools.createHoneycombAxe());
                items.add(BlockTools.createHoneycombShovel());
                items.add(BlockTools.createHoneycombHoe());
                items.add(BlockTools.createLilyPadPickaxe());
                items.add(BlockTools.createLilyPadAxe());
                items.add(BlockTools.createLilyPadShovel());
                items.add(BlockTools.createLilyPadHoe());
                items.add(BlockTools.createMelonPickaxe());
                items.add(BlockTools.createMelonAxe());
                items.add(BlockTools.createMelonShovel());
                items.add(BlockTools.createMelonHoe());
                items.add(BlockTools.createMossBlockPickaxe());
                items.add(BlockTools.createMossBlockAxe());
                items.add(BlockTools.createMossBlockShovel());
                items.add(BlockTools.createMossBlockHoe());
                items.add(BlockTools.createMyceliumPickaxe());
                items.add(BlockTools.createMyceliumAxe());
                items.add(BlockTools.createMyceliumShovel());
                items.add(BlockTools.createMyceliumHoe());
                items.add(BlockTools.createNetherBrickPickaxe());
                items.add(BlockTools.createNetherBrickAxe());
                items.add(BlockTools.createNetherBrickShovel());
                items.add(BlockTools.createNetherBrickHoe());
                items.add(BlockTools.createPumpkinPickaxe());
                items.add(BlockTools.createPumpkinAxe());
                items.add(BlockTools.createPumpkinShovel());
                items.add(BlockTools.createPumpkinHoe());
                items.add(BlockTools.createPurpurPickaxe());
                items.add(BlockTools.createPurpurAxe());
                items.add(BlockTools.createPurpurShovel());
                items.add(BlockTools.createPurpurHoe());
                items.add(BlockTools.createSandPickaxe());
                items.add(BlockTools.createSandAxe());
                items.add(BlockTools.createSandShovel());
                items.add(BlockTools.createSandHoe());
                items.add(BlockTools.createSculkPickaxe());
                items.add(BlockTools.createSculkAxe());
                items.add(BlockTools.createSculkShovel());
                items.add(BlockTools.createSculkHoe());
                items.add(BlockTools.createShroomlightPickaxe());
                items.add(BlockTools.createShroomlightAxe());
                items.add(BlockTools.createShroomlightShovel());
                items.add(BlockTools.createShroomlightHoe());
                items.add(BlockTools.createSlimePickaxe());
                items.add(BlockTools.createSlimeAxe());
                items.add(BlockTools.createSlimeShovel());
                items.add(BlockTools.createSlimeHoe());
                items.add(BlockTools.createSmoothStonePickaxe());
                items.add(BlockTools.createSmoothStoneAxe());
                items.add(BlockTools.createSmoothStoneShovel());
                items.add(BlockTools.createSmoothStoneHoe());
                items.add(BlockTools.createSnowPickaxe());
                items.add(BlockTools.createSnowAxe());
                items.add(BlockTools.createSnowShovel());
                items.add(BlockTools.createSnowHoe());
                items.add(BlockTools.createSoulSoilPickaxe());
                items.add(BlockTools.createSoulSoilAxe());
                items.add(BlockTools.createSoulSoilShovel());
                items.add(BlockTools.createSoulSoilHoe());
                items.add(BlockTools.createSpongePickaxe());
                items.add(BlockTools.createSpongeAxe());
                items.add(BlockTools.createSpongeShovel());
                items.add(BlockTools.createSpongeHoe());
                items.add(BlockTools.createTargetPickaxe());
                items.add(BlockTools.createTargetAxe());
                items.add(BlockTools.createTargetShovel());
                items.add(BlockTools.createTargetHoe());
                items.add(BlockTools.createTntPickaxe());
                items.add(BlockTools.createTntAxe());
                items.add(BlockTools.createTntShovel());
                items.add(BlockTools.createTntHoe());
                items.add(BlockTools.createWarpedPickaxe());
                items.add(BlockTools.createWarpedAxe());
                items.add(BlockTools.createWarpedShovel());
                items.add(BlockTools.createWarpedHoe());
                items.add(BlockTools.createWetSpongePickaxe());
                items.add(BlockTools.createWetSpongeAxe());
                items.add(BlockTools.createWetSpongeShovel());
                items.add(BlockTools.createWetSpongeHoe());
                // NEW STRIPPED LOG TOOLS
                items.add(BlockTools.createStrippedSpruceLogPickaxe()); items.add(BlockTools.createStrippedSpruceLogAxe()); items.add(BlockTools.createStrippedSpruceLogShovel()); items.add(BlockTools.createStrippedSpruceLogHoe());
                items.add(BlockTools.createStrippedBirchLogPickaxe()); items.add(BlockTools.createStrippedBirchLogAxe()); items.add(BlockTools.createStrippedBirchLogShovel()); items.add(BlockTools.createStrippedBirchLogHoe());
                items.add(BlockTools.createStrippedDarkOakLogPickaxe()); items.add(BlockTools.createStrippedDarkOakLogAxe()); items.add(BlockTools.createStrippedDarkOakLogShovel()); items.add(BlockTools.createStrippedDarkOakLogHoe());
                items.add(BlockTools.createStrippedJungleLogPickaxe()); items.add(BlockTools.createStrippedJungleLogAxe()); items.add(BlockTools.createStrippedJungleLogShovel()); items.add(BlockTools.createStrippedJungleLogHoe());
                items.add(BlockTools.createStrippedAcaciaLogPickaxe()); items.add(BlockTools.createStrippedAcaciaLogAxe()); items.add(BlockTools.createStrippedAcaciaLogShovel()); items.add(BlockTools.createStrippedAcaciaLogHoe());
                items.add(BlockTools.createStrippedMangroveLogPickaxe()); items.add(BlockTools.createStrippedMangroveLogAxe()); items.add(BlockTools.createStrippedMangroveLogShovel()); items.add(BlockTools.createStrippedMangroveLogHoe());
                items.add(BlockTools.createStrippedCherryLogPickaxe()); items.add(BlockTools.createStrippedCherryLogAxe()); items.add(BlockTools.createStrippedCherryLogShovel()); items.add(BlockTools.createStrippedCherryLogHoe());
                items.add(BlockTools.createStrippedBambooBlockPickaxe()); items.add(BlockTools.createStrippedBambooBlockAxe()); items.add(BlockTools.createStrippedBambooBlockShovel()); items.add(BlockTools.createStrippedBambooBlockHoe());
                items.add(BlockTools.createStrippedCrimsonStemPickaxe()); items.add(BlockTools.createStrippedCrimsonStemAxe()); items.add(BlockTools.createStrippedCrimsonStemShovel()); items.add(BlockTools.createStrippedCrimsonStemHoe());
                items.add(BlockTools.createStrippedWarpedStemPickaxe()); items.add(BlockTools.createStrippedWarpedStemAxe()); items.add(BlockTools.createStrippedWarpedStemShovel()); items.add(BlockTools.createStrippedWarpedStemHoe());
                // NEW PLANK TOOLS
                items.add(BlockTools.createSprucePlanksPickaxe()); items.add(BlockTools.createSprucePlanksAxe()); items.add(BlockTools.createSprucePlanksShovel()); items.add(BlockTools.createSprucePlanksHoe());
                items.add(BlockTools.createBirchPlanksPickaxe()); items.add(BlockTools.createBirchPlanksAxe()); items.add(BlockTools.createBirchPlanksShovel()); items.add(BlockTools.createBirchPlanksHoe());
                items.add(BlockTools.createJunglePlanksPickaxe()); items.add(BlockTools.createJunglePlanksAxe()); items.add(BlockTools.createJunglePlanksShovel()); items.add(BlockTools.createJunglePlanksHoe());
                items.add(BlockTools.createAcaciaPlanksPickaxe()); items.add(BlockTools.createAcaciaPlanksAxe()); items.add(BlockTools.createAcaciaPlanksShovel()); items.add(BlockTools.createAcaciaPlanksHoe());
                items.add(BlockTools.createDarkOakPlanksPickaxe()); items.add(BlockTools.createDarkOakPlanksAxe()); items.add(BlockTools.createDarkOakPlanksShovel()); items.add(BlockTools.createDarkOakPlanksHoe());
                items.add(BlockTools.createMangrovePlanksPickaxe()); items.add(BlockTools.createMangrovePlanksAxe()); items.add(BlockTools.createMangrovePlanksShovel()); items.add(BlockTools.createMangrovePlanksHoe());
                items.add(BlockTools.createCherryPlanksPickaxe()); items.add(BlockTools.createCherryPlanksAxe()); items.add(BlockTools.createCherryPlanksShovel()); items.add(BlockTools.createCherryPlanksHoe());
                items.add(BlockTools.createBambooPlanksPickaxe()); items.add(BlockTools.createBambooPlanksAxe()); items.add(BlockTools.createBambooPlanksShovel()); items.add(BlockTools.createBambooPlanksHoe());
                items.add(BlockTools.createCrimsonPlanksPickaxe()); items.add(BlockTools.createCrimsonPlanksAxe()); items.add(BlockTools.createCrimsonPlanksShovel()); items.add(BlockTools.createCrimsonPlanksHoe());
                items.add(BlockTools.createWarpedPlanksPickaxe()); items.add(BlockTools.createWarpedPlanksAxe()); items.add(BlockTools.createWarpedPlanksShovel()); items.add(BlockTools.createWarpedPlanksHoe());
                items.add(BlockTools.createOakPlanksPickaxe()); items.add(BlockTools.createOakPlanksAxe()); items.add(BlockTools.createOakPlanksShovel()); items.add(BlockTools.createOakPlanksHoe());
                // NEW STONE/MUD/LOG TOOLS
                items.add(BlockTools.createOakLogPickaxe()); items.add(BlockTools.createOakLogAxe()); items.add(BlockTools.createOakLogShovel()); items.add(BlockTools.createOakLogHoe());
                items.add(BlockTools.createSpruceLogPickaxe()); items.add(BlockTools.createSpruceLogAxe()); items.add(BlockTools.createSpruceLogShovel()); items.add(BlockTools.createSpruceLogHoe());
                items.add(BlockTools.createBirchLogPickaxe()); items.add(BlockTools.createBirchLogAxe()); items.add(BlockTools.createBirchLogShovel()); items.add(BlockTools.createBirchLogHoe());
                items.add(BlockTools.createStoneBricksPickaxe()); items.add(BlockTools.createStoneBricksAxe()); items.add(BlockTools.createStoneBricksShovel()); items.add(BlockTools.createStoneBricksHoe());
                items.add(BlockTools.createCobblestonePickaxe()); items.add(BlockTools.createCobblestoneAxe()); items.add(BlockTools.createCobblestoneShovel()); items.add(BlockTools.createCobblestoneHoe());
                items.add(BlockTools.createMossyCobblestonePickaxe()); items.add(BlockTools.createMossyCobblestoneAxe()); items.add(BlockTools.createMossyCobblestoneShovel()); items.add(BlockTools.createMossyCobblestoneHoe());
                items.add(BlockTools.createCobbledDeepslatePickaxe()); items.add(BlockTools.createCobbledDeepslateAxe()); items.add(BlockTools.createCobbledDeepslateShovel()); items.add(BlockTools.createCobbledDeepslateHoe());
                items.add(BlockTools.createMudBricksPickaxe()); items.add(BlockTools.createMudBricksAxe()); items.add(BlockTools.createMudBricksShovel()); items.add(BlockTools.createMudBricksHoe());
                items.add(BlockTools.createMangroveRootsPickaxe()); items.add(BlockTools.createMangroveRootsAxe()); items.add(BlockTools.createMangroveRootsShovel()); items.add(BlockTools.createMangroveRootsHoe());
                items.add(BlockTools.createMuddyMangroveRootsPickaxe()); items.add(BlockTools.createMuddyMangroveRootsAxe()); items.add(BlockTools.createMuddyMangroveRootsShovel()); items.add(BlockTools.createMuddyMangroveRootsHoe());
                // NEW COPPER/NETHERITE/FROGLIGHT TOOLS
                items.add(BlockTools.createNetheriteBlockPickaxe()); items.add(BlockTools.createNetheriteBlockAxe()); items.add(BlockTools.createNetheriteBlockShovel()); items.add(BlockTools.createNetheriteBlockHoe());
                items.add(BlockTools.createChiseledCopperPickaxe()); items.add(BlockTools.createChiseledCopperAxe()); items.add(BlockTools.createChiseledCopperShovel()); items.add(BlockTools.createChiseledCopperHoe());
                items.add(BlockTools.createCutCopperPickaxe()); items.add(BlockTools.createCutCopperAxe()); items.add(BlockTools.createCutCopperShovel()); items.add(BlockTools.createCutCopperHoe());
                items.add(BlockTools.createExposedCopperPickaxe()); items.add(BlockTools.createExposedCopperAxe()); items.add(BlockTools.createExposedCopperShovel()); items.add(BlockTools.createExposedCopperHoe());
                items.add(BlockTools.createWeatheredCopperPickaxe()); items.add(BlockTools.createWeatheredCopperAxe()); items.add(BlockTools.createWeatheredCopperShovel()); items.add(BlockTools.createWeatheredCopperHoe());
                items.add(BlockTools.createOxidisedCopperPickaxe()); items.add(BlockTools.createOxidisedCopperAxe()); items.add(BlockTools.createOxidisedCopperShovel()); items.add(BlockTools.createOxidisedCopperHoe());
                items.add(BlockTools.createWaxedCutCopperPickaxe()); items.add(BlockTools.createWaxedCutCopperAxe()); items.add(BlockTools.createWaxedCutCopperShovel()); items.add(BlockTools.createWaxedCutCopperHoe());
                items.add(BlockTools.createPolishedBasaltPickaxe()); items.add(BlockTools.createPolishedBasaltAxe()); items.add(BlockTools.createPolishedBasaltShovel()); items.add(BlockTools.createPolishedBasaltHoe());
                items.add(BlockTools.createVerdantFroglightPickaxe()); items.add(BlockTools.createVerdantFroglightAxe()); items.add(BlockTools.createVerdantFroglightShovel()); items.add(BlockTools.createVerdantFroglightHoe());
                items.add(BlockTools.createPearlescentFroglightPickaxe()); items.add(BlockTools.createPearlescentFroglightAxe()); items.add(BlockTools.createPearlescentFroglightShovel()); items.add(BlockTools.createPearlescentFroglightHoe());
                items.add(BlockTools.createOchreFroglightPickaxe()); items.add(BlockTools.createOchreFroglightAxe()); items.add(BlockTools.createOchreFroglightShovel()); items.add(BlockTools.createOchreFroglightHoe());
                // NEW MATERIAL ITEM TOOLS
                items.add(BlockTools.createIronNuggetPickaxe()); items.add(BlockTools.createIronNuggetAxe()); items.add(BlockTools.createIronNuggetShovel()); items.add(BlockTools.createIronNuggetHoe());
                items.add(BlockTools.createGoldNuggetPickaxe()); items.add(BlockTools.createGoldNuggetAxe()); items.add(BlockTools.createGoldNuggetShovel()); items.add(BlockTools.createGoldNuggetHoe());
                items.add(BlockTools.createCopperIngotPickaxe()); items.add(BlockTools.createCopperIngotAxe()); items.add(BlockTools.createCopperIngotShovel()); items.add(BlockTools.createCopperIngotHoe());
                items.add(BlockTools.createEmeraldPickaxe()); items.add(BlockTools.createEmeraldAxe()); items.add(BlockTools.createEmeraldShovel()); items.add(BlockTools.createEmeraldHoe());
                items.add(BlockTools.createLapisLazuliPickaxe()); items.add(BlockTools.createLapisLazuliAxe()); items.add(BlockTools.createLapisLazuliShovel()); items.add(BlockTools.createLapisLazuliHoe());
                items.add(BlockTools.createAmethystShardPickaxe()); items.add(BlockTools.createAmethystShardAxe()); items.add(BlockTools.createAmethystShardShovel()); items.add(BlockTools.createAmethystShardHoe());
                items.add(BlockTools.createFlintPickaxe()); items.add(BlockTools.createFlintAxe()); items.add(BlockTools.createFlintShovel()); items.add(BlockTools.createFlintHoe());
                items.add(BlockTools.createBoneMealPickaxe()); items.add(BlockTools.createBoneMealAxe()); items.add(BlockTools.createBoneMealShovel()); items.add(BlockTools.createBoneMealHoe());
                items.add(BlockTools.createCharcoalPickaxe()); items.add(BlockTools.createCharcoalAxe()); items.add(BlockTools.createCharcoalShovel()); items.add(BlockTools.createCharcoalHoe());
                items.add(BlockTools.createEndStonePickaxe()); items.add(BlockTools.createEndStoneAxe()); items.add(BlockTools.createEndStoneShovel()); items.add(BlockTools.createEndStoneHoe());
                items.add(BlockTools.createSnowBlockPickaxe()); items.add(BlockTools.createSnowBlockAxe()); items.add(BlockTools.createSnowBlockShovel()); items.add(BlockTools.createSnowBlockHoe());
            }
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
        }
        return items;
    }

    // ──────────────────────────────────────────────────────────────
    // Page 0: Main Category Menu - Matches RecipesGui layout
    // ──────────────────────────────────────────────────────────────

    /** Entry point — opens the main category selection menu. */
    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📦 Custom Items"));

        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                        .setName(Text.literal("")).build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.BLUE_STAINED_GLASS_PANE)
                        .setName(Text.literal("")).build());
            }
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("📦 Custom Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select a category to get items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any item to receive it").formatted(Formatting.YELLOW))
                .glow().build());

        gui.setSlot(1, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Text.literal("🔍 Search Items").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Search items by name").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Supports partial matches").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openSearchGui(player))
                .build());

        // Row 1: Armor categories - spread across more slots
        gui.setSlot(10, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("💎 Custom Armor").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Emerald, Lapis armors").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.CUSTOM_ARMOR, 0))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⚔ Bounty Sets").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("T1 & T2 Armor + Swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openBountySetsPage(player, 0,
                        (p, item) -> giveItem(p, item),
                        CustomItemsGui::open))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Text.literal("🛡 Special Armor").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Legendary armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.SPECIAL_ARMOR, 0))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.GOLDEN_CHESTPLATE)
                .setName(Text.literal("👑 Gold Armor").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Gold armor tiers").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.GOLD_ARMOR, 0))
                .build());

        gui.setSlot(28, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Piglin Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Piglin-themed items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.PIGLIN, 0))
                .build());

        // Row 2: Weapons and Tools
        gui.setSlot(22, new GuiElementBuilder(Items.NETHERITE_PICKAXE)
                .setName(Text.literal("⛏ Tool Sets").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Block + Legendary tools").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openToolSetsPage(player, 0,
                        (p, item) -> giveItem(p, item),
                        CustomItemsGui::open))
                .build());

        gui.setSlot(23, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Text.literal("🎁 Boss Drops").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Rare boss drop items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("💀 Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.BOSS_DROPS, 0))
                .build());

        gui.setSlot(24, new GuiElementBuilder(Items.STICK)
                .setName(Text.literal("🎁 Special Items").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Special unique items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.SPECIAL_ITEMS, 0))
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.WHEAT)
                .setName(Text.literal("✧ Enchanted Crops").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Enchanted crops & flowers").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.ENCHANTED_CROPS, 0))
                .build());

        // Row 3: Materials and Tokens
        gui.setSlot(30, new GuiElementBuilder(Items.SLIME_BALL)
                .setName(Text.literal("📦 Materials").formatted(Formatting.GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Cores & chunks").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.MATERIALS, 0))
                .build());

        gui.setSlot(32, new GuiElementBuilder(Items.RAW_IRON)
                .setName(Text.literal("⬛ Compacted").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Compacted resources").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.COMPACTED, 0))
                .build());

        gui.setSlot(34, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("⭐ Super Compacted").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Super compacted").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.SUPER_COMPACTED, 0))
                .build());

        // Row 4: Additional categories
        gui.setSlot(38, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("✧ Armor Tokens").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Armor attributes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.ATTRIBUTE_TOKENS, 0))
                .build());

        gui.setSlot(40, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⚔ Weapon Tokens").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Weapon attributes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.WEAPON_ATTRIBUTES, 0))
                .build());

        gui.setSlot(42, new GuiElementBuilder(Items.BLAZE_ROD)
                .setName(Text.literal("⚡ HPEBM Weapons").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Energy beam weapons").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.HPEBM, 0))
                .build());

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED, Formatting.BOLD))
                .setCallback((slot, clickType, action) -> gui.close())
                .build());

        int[] menuSlots = {10, 12, 14, 16, 20, 22, 24, 28, 30, 32, 34, 38, 40, 42};
        for (int slot : menuSlots) {
            placeDiamondPlaceholder(gui, slot);
        }

        gui.open();
    }

    private static void placeDiamondPlaceholder(SimpleGui gui, int slot) {
        if (gui.getSlot(slot) != null) return;
        gui.setSlot(slot, new GuiElementBuilder(Items.DIAMOND_BLOCK)
                .setName(Text.literal(""))
                .build());
    }

    private static void openSearchGui(ServerPlayerEntity player) {
        eu.pb4.sgui.api.gui.SignGui signGui = new eu.pb4.sgui.api.gui.SignGui(player) {
            @Override
            public void onClose() {
                String searchTerm = this.getLine(0).getString().trim();
                if (!searchTerm.isEmpty()) {
                    showSearchResults(player, searchTerm);
                } else {
                    CustomItemsGui.open(player);
                }
            }
        };
        signGui.setLine(0, Text.literal("Search:").formatted(Formatting.AQUA));
        signGui.setLine(1, Text.literal("(type here)").formatted(Formatting.GRAY));
        signGui.open();
    }

    private static void showSearchResults(ServerPlayerEntity player, String searchTerm) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("🔍 Search: " + searchTerm));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Text.literal("🔍 Search Results").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Search: \"" + searchTerm + "\"").formatted(Formatting.YELLOW))
                .glow().build());

        List<ItemStack> results = new ArrayList<>();
        String searchLower = searchTerm.toLowerCase();

        for (Category cat : Category.values()) {
            for (ItemStack item : getItemsForCategory(cat)) {
                if (item == null || item.isEmpty()) continue;
                Text nameText = item.get(DataComponentTypes.CUSTOM_NAME);
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
            int slot = row * 9 + col;

            Text displayName = item.get(DataComponentTypes.CUSTOM_NAME);
            ItemStack clickItem = item;
            gui.setSlot(slot, new GuiElementBuilder(item.getItem())
                    .setName(displayName != null ? displayName : Text.literal(item.getItem().toString()))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to get item").formatted(Formatting.YELLOW))
                    .setCallback((s, t, a) -> giveItem(player, clickItem))
                    .build());

            shown++;
        }

        if (shown == 0) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("No results found").formatted(Formatting.RED))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Try a different search term").formatted(Formatting.GRAY))
                    .build());
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> open(player))
                .build());

        gui.setSlot(46, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Text.literal("🔍 Search Again").formatted(Formatting.AQUA))
                .setCallback((slot, type, action) -> openSearchGui(player))
                .build());

        gui.open();
    }

    public static void openToolSetsPage(ServerPlayerEntity player, int page, BountyItemClickHandler onItemClick, BackHandler onBack) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⛏ Tool Sets"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.NETHERITE_PICKAXE)
                .setName(Text.literal("⛏ Tool Sets").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("[Icon] [gap] [Pickaxe] [Axe] [Shovel] [Hoe]").formatted(Formatting.YELLOW))
                .glow().build());

        class ToolSetDisplay {
            final net.minecraft.item.Item icon;
            final String name;
            final Formatting color;
            final ItemStack pickaxe;
            final ItemStack axe;
            final ItemStack shovel;
            final ItemStack hoe;

            ToolSetDisplay(net.minecraft.item.Item icon, String name, Formatting color, ItemStack pickaxe, ItemStack axe, ItemStack shovel, ItemStack hoe) {
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
        sets.add(new ToolSetDisplay(Items.GLASS, "Glass", Formatting.AQUA,
                BlockTools.createGlassPickaxe(), BlockTools.createGlassAxe(), BlockTools.createGlassShovel(), BlockTools.createGlassHoe()));
        sets.add(new ToolSetDisplay(Items.OBSIDIAN, "Obsidian", Formatting.DARK_PURPLE,
                BlockTools.createObsidianPickaxe(), BlockTools.createObsidianAxe(), BlockTools.createObsidianShovel(), BlockTools.createObsidianHoe()));
        sets.add(new ToolSetDisplay(Items.QUARTZ_BLOCK, "Quartz", Formatting.WHITE,
                BlockTools.createQuartzPickaxe(), BlockTools.createQuartzAxe(), BlockTools.createQuartzShovel(), BlockTools.createQuartzHoe()));
        sets.add(new ToolSetDisplay(Items.GLOWSTONE, "Glowstone", Formatting.YELLOW,
                BlockTools.createGlowstonePickaxe(), BlockTools.createGlowstoneAxe(), BlockTools.createGlowstoneShovel(), BlockTools.createGlowstoneHoe()));
        sets.add(new ToolSetDisplay(Items.REDSTONE_BLOCK, "Redstone", Formatting.RED,
                BlockTools.createRedstonePickaxe(), BlockTools.createRedstoneAxe(), BlockTools.createRedstoneShovel(), BlockTools.createRedstoneHoe()));

        // Legendary tool sets (use pickaxe base item as icon)
        sets.add(new ToolSetDisplay(Items.NETHERITE_PICKAXE, "Dragon", Formatting.DARK_RED,
                LegendaryTools.createDragonPickaxe(), LegendaryTools.createDragonAxe(), LegendaryTools.createDragonShovel(), LegendaryTools.createDragonHoe()));
        sets.add(new ToolSetDisplay(Items.DIAMOND_PICKAXE, "Aether", Formatting.AQUA,
                LegendaryTools.createAetherPickaxe(), LegendaryTools.createAetherAxe(), LegendaryTools.createAetherShovel(), LegendaryTools.createAetherHoe()));
        sets.add(new ToolSetDisplay(Items.NETHERITE_PICKAXE, "Void", Formatting.DARK_PURPLE,
                LegendaryTools.createVoidPickaxe(), LegendaryTools.createVoidAxe(), LegendaryTools.createVoidShovel(), LegendaryTools.createVoidHoe()));
        sets.add(new ToolSetDisplay(Items.DIAMOND_PICKAXE, "Nature", Formatting.GREEN,
                LegendaryTools.createNaturePickaxe(), LegendaryTools.createNatureAxe(), LegendaryTools.createNatureShovel(), LegendaryTools.createNatureHoe()));
        sets.add(new ToolSetDisplay(Items.DIAMOND_PICKAXE, "Frost", Formatting.BLUE,
                LegendaryTools.createFrostPickaxe(), LegendaryTools.createFrostAxe(), LegendaryTools.createFrostShovel(), LegendaryTools.createFrostHoe()));
        sets.add(new ToolSetDisplay(Items.NETHERITE_PICKAXE, "Thunder", Formatting.YELLOW,
                LegendaryTools.createThunderPickaxe(), LegendaryTools.createThunderAxe(), LegendaryTools.createThunderShovel(), LegendaryTools.createThunderHoe()));
        sets.add(new ToolSetDisplay(Items.DIAMOND_PICKAXE, "Ocean", Formatting.DARK_BLUE,
                LegendaryTools.createOceanPickaxe(), LegendaryTools.createOceanAxe(), LegendaryTools.createOceanShovel(), LegendaryTools.createOceanHoe()));
        sets.add(new ToolSetDisplay(Items.NETHERITE_PICKAXE, "Lunar", Formatting.LIGHT_PURPLE,
                LegendaryTools.createLunarPickaxe(), LegendaryTools.createLunarAxe(), LegendaryTools.createLunarShovel(), LegendaryTools.createLunarHoe()));
        sets.add(new ToolSetDisplay(Items.NETHERITE_PICKAXE, "Solar", Formatting.GOLD,
                LegendaryTools.createSolarPickaxe(), LegendaryTools.createSolarAxe(), LegendaryTools.createSolarShovel(), LegendaryTools.createSolarHoe()));
        sets.add(new ToolSetDisplay(Items.DIAMOND_PICKAXE, "Terra", Formatting.DARK_GREEN,
                LegendaryTools.createTerraPickaxe(), LegendaryTools.createTerraAxe(), LegendaryTools.createTerraShovel(), LegendaryTools.createTerraHoe()));
        sets.add(new ToolSetDisplay(Items.NETHERITE_PICKAXE, "Phantom", Formatting.GRAY,
                LegendaryTools.createPhantomPickaxe(), LegendaryTools.createPhantomAxe(), LegendaryTools.createPhantomShovel(), LegendaryTools.createPhantomHoe()));
        sets.add(new ToolSetDisplay(Items.NETHERITE_PICKAXE, "Blood", Formatting.DARK_RED,
                LegendaryTools.createBloodPickaxe(), LegendaryTools.createBloodAxe(), LegendaryTools.createBloodShovel(), LegendaryTools.createBloodHoe()));
        sets.add(new ToolSetDisplay(Items.NETHERITE_PICKAXE, "Celestial", Formatting.DARK_AQUA,
                LegendaryTools.createCelestialPickaxe(), LegendaryTools.createCelestialAxe(), LegendaryTools.createCelestialShovel(), LegendaryTools.createCelestialHoe()));
        sets.add(new ToolSetDisplay(Items.NETHERITE_PICKAXE, "Shadow", Formatting.BLACK,
                LegendaryTools.createShadowPickaxe(), LegendaryTools.createShadowAxe(), LegendaryTools.createShadowShovel(), LegendaryTools.createShadowHoe()));
        sets.add(new ToolSetDisplay(Items.DIAMOND_PICKAXE, "Crystal", Formatting.WHITE,
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
                    .setName(Text.literal(set.name + " Tools").formatted(set.color, Formatting.BOLD))
                    .glow().build());
            gui.setSlot(base + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());

            ItemStack[] tools = { set.pickaxe, set.axe, set.shovel, set.hoe };
            for (int j = 0; j < tools.length; j++) {
                ItemStack tool = tools[j];
                Text toolName = tool.get(DataComponentTypes.CUSTOM_NAME);
                final ItemStack finalTool = tool;
                gui.setSlot(base + 2 + j, new GuiElementBuilder(tool.getItem())
                        .setName(toolName != null ? toolName : Text.literal(tool.getItem().toString()))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("✨ Click").formatted(Formatting.YELLOW))
                        .setCallback((s, t, a) -> onItemClick.onClick(player, finalTool))
                        .build());
            }
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((s, t, a) -> onBack.open(player)).build());

        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .setCallback((s, t, a) -> openToolSetsPage(player, prev, onItemClick, onBack)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .setCallback((s, t, a) -> openToolSetsPage(player, next, onItemClick, onBack)).build());
        }

        gui.setSlot(50, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages).formatted(Formatting.WHITE))
                .build());

        gui.open();
    }

    public static void openBountySetsPage(ServerPlayerEntity player, int page, BountyItemClickHandler onItemClick, BackHandler onBack) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("⚔ Bounty Sets (T1 & T2)"));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⚔ Bounty Sets (T1 & T2)").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Displayed in rows:").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("[Icon] [gap] [Sword] [Helmet] [Chest] [Legs] [Boots]").formatted(Formatting.YELLOW))
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
                    .setName(Text.literal(set.name + " T1").formatted(set.color, Formatting.BOLD))
                    .glow().build());
            gui.setSlot(row1Base + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());

            ItemStack[] t1Items = { set.t1Sword, set.t1Helmet, set.t1Chestplate, set.t1Leggings, set.t1Boots };
            for (int j = 0; j < t1Items.length; j++) {
                ItemStack item = t1Items[j];
                Text name = item.get(DataComponentTypes.CUSTOM_NAME);
                final ItemStack finalItem = item;
                gui.setSlot(row1Base + 2 + j, new GuiElementBuilder(item.getItem())
                        .setName(name != null ? name : Text.literal(item.getItem().toString()))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("✨ Click").formatted(Formatting.YELLOW))
                        .setCallback((s, t, a) -> onItemClick.onClick(player, finalItem))
                        .build());
            }

            int row2Base = (baseRow + 1) * 9;
            gui.setSlot(row2Base, new GuiElementBuilder(set.icon)
                    .setName(Text.literal(set.name + " T2").formatted(set.color, Formatting.BOLD))
                    .glow().build());
            gui.setSlot(row2Base + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());

            ItemStack[] t2Items = { set.t2Sword, set.t2Helmet, set.t2Chestplate, set.t2Leggings, set.t2Boots };
            for (int j = 0; j < t2Items.length; j++) {
                ItemStack item = t2Items[j];
                Text name = item.get(DataComponentTypes.CUSTOM_NAME);
                final ItemStack finalItem = item;
                gui.setSlot(row2Base + 2 + j, new GuiElementBuilder(item.getItem())
                        .setName(name != null ? name : Text.literal(item.getItem().toString()))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("✨ Click").formatted(Formatting.YELLOW))
                        .setCallback((s, t, a) -> onItemClick.onClick(player, finalItem))
                        .build());
            }
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((s, t, a) -> onBack.open(player)).build());

        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .setCallback((s, t, a) -> openBountySetsPage(player, prev, onItemClick, onBack)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .setCallback((s, t, a) -> openBountySetsPage(player, next, onItemClick, onBack)).build());
        }

        gui.setSlot(50, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages).formatted(Formatting.WHITE))
                .build());

        gui.open();
    }

    // ──────────────────────────────────────────────────────────────
    // GEM_BLOCK_GEAR: Armor Sets in Rows Display
    // Each row shows: [Block] [Sword] [Helmet] [Chest] [Legs] [Boots]
    // ──────────────────────────────────────────────────────────────

    public static void openGemBlockGearPage(ServerPlayerEntity player, int page) {
        openGemBlockGearPage(player, page, (p, item) -> giveItem(p, item), CustomItemsGui::open);
    }

    public static void openGemBlockGearPage(ServerPlayerEntity player, int page, BountyItemClickHandler onItemClick, BackHandler onBack) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6💎 Gem & Block Armor"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("💎 Gem & Block Armor").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Armor sets displayed in rows:").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("[Block] [Sword] [Helmet] [Chest] [Legs] [Boots]").formatted(Formatting.YELLOW))
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
                BlockArmor.createHayBlockHelmet(), BlockArmor.createHayBlockChestplate(),
                BlockArmor.createHayBlockLeggings(), BlockArmor.createHayBlockBoots()),
            new ArmorSetDisplay(Items.HONEYCOMB_BLOCK, BlockWeapons.createHoneycombSword(),
                BlockArmor.createHoneycombBlockHelmet(), BlockArmor.createHoneycombBlockChestplate(),
                BlockArmor.createHoneycombBlockLeggings(), BlockArmor.createHoneycombBlockBoots()),
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
                    .setName(Text.literal("§8" + set.blockItem.toString().replace("_", " ")).formatted(Formatting.DARK_GRAY))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("Armor Set Material").formatted(Formatting.GRAY))
                    .build());
            
            // Gap slot (shifted armor 1 to the right)
            gui.setSlot(slots[1], new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());

            // Sword
            Text swordName = set.sword.get(DataComponentTypes.CUSTOM_NAME);
            gui.setSlot(slots[2], new GuiElementBuilder(set.sword.getItem())
                    .setName(swordName != null ? swordName : Text.literal("Sword"))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to get item").formatted(Formatting.YELLOW))
                    .setCallback((s, t, a) -> onItemClick.onClick(player, set.sword))
                    .build());

            // Armor pieces: Helmet, Chestplate, Leggings, Boots (shifted 1 slot right)
            ItemStack[] armorPieces = { set.helmet, set.chestplate, set.leggings, set.boots };
            for (int j = 0; j < 4; j++) {
                ItemStack armor = armorPieces[j];
                Text armorName = armor.get(DataComponentTypes.CUSTOM_NAME);
                final ItemStack finalArmor = armor;
                gui.setSlot(slots[3 + j], new GuiElementBuilder(armor.getItem())
                        .setName(armorName != null ? armorName : Text.literal("Armor"))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("✨ Click to get item").formatted(Formatting.YELLOW))
                        .setCallback((s, t, a) -> onItemClick.onClick(player, finalArmor))
                        .build());
            }
        }

        // ← Back to main menu
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Categories").formatted(Formatting.YELLOW))
                .setCallback((s, t, a) -> onBack.open(player)).build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages))
                    .setCallback((s, t, a) -> openGemBlockGearPage(player, prev, onItemClick, onBack)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((s, t, a) -> openGemBlockGearPage(player, next, onItemClick, onBack)).build());
        }

        gui.setSlot(50, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages))
                .build());

        gui.open();
    }

    // Helper class for armor set display
    private record ArmorSetDisplay(net.minecraft.item.Item blockItem, ItemStack sword, ItemStack helmet,
                                    ItemStack chestplate, ItemStack leggings, ItemStack boots) {}

    // ──────────────────────────────────────────────────────────────
    // Page 1: Category Item List (Standard Layout)
    // ──────────────────────────────────────────────────────────────

    public static void openCategoryPage(ServerPlayerEntity player, Category cat, int page) {
        openCategoryPage(player, cat, page, (p, item) -> giveItem(p, item), CustomItemsGui::open);
    }

    public static void openCategoryPage(ServerPlayerEntity player, Category cat, int page, BountyItemClickHandler onItemClick, BackHandler onBack) {
        if (cat == Category.CUSTOM_ARMOR) {
            openGemBlockGearPage(player, page, onItemClick, onBack);
            return;
        }

        List<ItemStack> items = getItemsForCategory(cat);

        final int ITEMS_PER_PAGE = 28;
        int totalPages = Math.max(1, (items.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        int clampedPage = Math.min(page, totalPages - 1);

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6" + cat.label));

        // Background with dark theme
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(cat.icon)
                .setName(Text.literal(cat.label).formatted(cat.colour, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click an item to view recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Item slots (7 per row, rows 1-4, columns 1-7)
        int start = clampedPage * ITEMS_PER_PAGE;
        int shown = 0;
        for (int idx = start; idx < items.size() && shown < ITEMS_PER_PAGE; idx++) {
            ItemStack item = items.get(idx);
            int row = shown / 7;
            int col = shown % 7 + 1;
            int slot = row * 9 + col;

            Text displayName = item.get(DataComponentTypes.CUSTOM_NAME);

            final ItemStack finalItem = item;
            GuiElementBuilder elem = new GuiElementBuilder(item.getItem())
                    .setName(displayName != null ? displayName
                            : Text.literal(item.getItem().toString()))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to get item").formatted(Formatting.YELLOW))
                    .setCallback((s, t, a) -> onItemClick.onClick(player, finalItem));
            gui.setSlot(slot, elem.build());
            shown++;
        }

        // ← Back to main menu
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Categories").formatted(Formatting.YELLOW))
                .setCallback((s, t, a) -> onBack.open(player)).build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + clampedPage + " / " + totalPages))
                    .setCallback((s, t, a) -> openCategoryPage(player, cat, prev, onItemClick, onBack)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((s, t, a) -> openCategoryPage(player, cat, next, onItemClick, onBack)).build());
        }

        gui.setSlot(50, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages))
                .build());

        gui.open();
    }

    // ──────────────────────────────────────────────────────────────
    // RECIPE VIEW - Show item recipe in a GUI
    // ──────────────────────────────────────────────────────────────

    public static void showItemInfo(ServerPlayerEntity player, ItemStack resultItem) {
        showItemInfo(player, resultItem, CustomItemsGui::open);
    }

    public static void showItemInfo(ServerPlayerEntity player, ItemStack resultItem, BackHandler onBack) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("Recipe View"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header - Item name
        Text displayName = resultItem.get(DataComponentTypes.CUSTOM_NAME);
        if (displayName == null) {
            displayName = Text.literal(resultItem.getItem().toString());
        }
        gui.setSlot(4, new GuiElementBuilder(resultItem.getItem())
                .setName(displayName)
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Crafting Recipe").formatted(Formatting.GRAY))
                .glow().build());

        // Recipe grid slots (3x3)
        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};

        // Try to find the recipe from RecipeConfigManager
        SlayerRecipes.Recipe recipe = findRecipeForOutput(resultItem);

        if (recipe != null && recipe.ingredients != null) {
            // Display the actual recipe
            for (int i = 0; i < 9 && i < recipe.ingredients.length; i++) {
                ItemStack input = recipe.ingredients[i];
                if (input != null && !input.isEmpty()) {
                    Text inputName = input.get(DataComponentTypes.CUSTOM_NAME);
                    if (inputName == null) {
                        inputName = Text.literal(input.getItem().toString());
                    }
                    gui.setSlot(gridSlots[i], new GuiElementBuilder(input.getItem())
                            .setName(inputName)
                            .setCount(input.getCount())
                            .build());
                } else {
                    // Empty slot indicator
                    gui.setSlot(gridSlots[i], new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                            .setName(Text.literal("Empty").formatted(Formatting.GRAY))
                            .build());
                }
            }

            // Arrow indicator
            gui.setSlot(23, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("→").formatted(Formatting.YELLOW))
                    .build());

            // Result
            gui.setSlot(24, new GuiElementBuilder(resultItem)
                    .setName(displayName)
                    .glow().build());
        } else {
            // No recipe found - show message
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("No recipe found").formatted(Formatting.RED))
                    .addLoreLine(Text.literal("This item may be obtained"))
                    .addLoreLine(Text.literal("through other means."))
                    .build());
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Menu").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> onBack.open(player))
                .build());

        gui.open();
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
                Text recipeName = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
                Text outputName = output.get(DataComponentTypes.CUSTOM_NAME);
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
