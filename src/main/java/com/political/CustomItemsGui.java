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

    // ── Category definitions ─────────────────────────────────────
    private enum Category {
        GEM_BLOCK_GEAR ("💎 Gem & Block Armor",         Items.EMERALD,            Formatting.GREEN),
        SWORDS_T1    ("⚔ Bounty Swords (T1)",               Items.IRON_SWORD,         Formatting.YELLOW),
        SWORDS_T2    ("⚔ Bounty Swords (T2)",               Items.DIAMOND_SWORD,      Formatting.AQUA),
        ARMOR_T1     ("🛡 Bounty Armor T1",                  Items.IRON_CHESTPLATE,    Formatting.GREEN),
        ARMOR_T2     ("🛡 Bounty Armor T2",                  Items.DIAMOND_CHESTPLATE, Formatting.DARK_AQUA),
        SPECIAL_ARMOR("🛡 Special / Legendary Armor",        Items.NETHERITE_HELMET,   Formatting.GOLD),
        HPEBM        ("⚡ HPEBM Weapons",                    Items.BLAZE_ROD,          Formatting.LIGHT_PURPLE),
        SPECIAL_ITEMS("🎁 Special Items",                    Items.STICK,              Formatting.GREEN),
        PIGLIN       ("👑 Piglin Items",                     Items.GOLDEN_HELMET,      Formatting.GOLD),
        GOLD_ARMOR   ("👑 Gold Armor Evolution",             Items.GOLDEN_CHESTPLATE,  Formatting.GOLD),
        MATERIALS    ("📦 Materials (Cores & Chunks)",       Items.SLIME_BALL,         Formatting.GRAY),
        COMPACTED    ("⬛ Compacted Materials",              Items.RAW_IRON,           Formatting.DARK_GRAY),
        SUPER_COMPACTED("⭐ Super Compacted",               Items.GOLD_BLOCK,         Formatting.GOLD),
        ENCHANTED_BLOCKS("✧ Enchanted Blocks",              Items.COBBLESTONE,        Formatting.LIGHT_PURPLE),
        ENCHANTED_CROPS("✧ Enchanted Crops & Flowers",      Items.WHEAT,              Formatting.GREEN),
        ATTRIBUTE_TOKENS("✧ Attribute Tokens",               Items.PAPER,              Formatting.LIGHT_PURPLE),
        WEAPON_ATTRIBUTES("⚔ Weapon Attributes",               Items.NETHER_STAR,        Formatting.RED),
        BLOCK_ARMOR   ("🧱 Block Armor",                    Items.OBSIDIAN,           Formatting.DARK_PURPLE),
        BLOCK_WEAPONS ("⚔ Block Weapons",                   Items.QUARTZ,             Formatting.WHITE),
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

    /** Returns all items belonging to a given category. */
    private static List<ItemStack> getItemsForCategory(Category cat) {
        List<ItemStack> items = new ArrayList<>();
        switch (cat) {
            case GEM_BLOCK_GEAR -> {
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
                
                // NEW BOSS DROPS
                items.add(SlayerItems.createUndeadHeart());
                items.add(SlayerItems.createSpectralQuiver());
                items.add(SlayerItems.createEchoingCore());
                items.add(SlayerItems.createEnderSword());
                items.add(SlayerItems.createAbyssalBlade());
                items.add(SlayerItems.createBouncySlime());
                items.add(SlayerItems.createVenomousDagger());
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
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                    items.add(SlayerItems.createCore(t));
                    items.add(SlayerItems.createChunk(t));
                }
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
            }
            case BLOCK_ARMOR -> {
                // Glass Armor
                items.add(BlockArmor.createGlassHelmet());
                items.add(BlockArmor.createGlassChestplate());
                items.add(BlockArmor.createGlassLeggings());
                items.add(BlockArmor.createGlassBoots());
                // Obsidian Armor
                items.add(BlockArmor.createObsidianHelmet());
                items.add(BlockArmor.createObsidianChestplate());
                items.add(BlockArmor.createObsidianLeggings());
                items.add(BlockArmor.createObsidianBoots());
                // Quartz Armor
                items.add(BlockArmor.createQuartzHelmet());
                items.add(BlockArmor.createQuartzChestplate());
                items.add(BlockArmor.createQuartzLeggings());
                items.add(BlockArmor.createQuartzBoots());
                // Glowstone Armor
                items.add(BlockArmor.createGlowstoneHelmet());
                items.add(BlockArmor.createGlowstoneChestplate());
                items.add(BlockArmor.createGlowstoneLeggings());
                items.add(BlockArmor.createGlowstoneBoots());
                // Redstone Armor
                items.add(BlockArmor.createRedstoneHelmet());
                items.add(BlockArmor.createRedstoneChestplate());
                items.add(BlockArmor.createRedstoneLeggings());
                items.add(BlockArmor.createRedstoneBoots());
                // Netherrack Armor
                items.add(BlockArmor.createNetherrackHelmet());
                items.add(BlockArmor.createNetherrackChestplate());
                items.add(BlockArmor.createNetherrackLeggings());
                items.add(BlockArmor.createNetherrackBoots());
                // End Stone Armor
                items.add(BlockArmor.createEndstoneHelmet());
                items.add(BlockArmor.createEndstoneChestplate());
                items.add(BlockArmor.createEndstoneLeggings());
                items.add(BlockArmor.createEndstoneBoots());
                // Ice Armor
                items.add(BlockArmor.createIceHelmet());
                items.add(BlockArmor.createIceChestplate());
                items.add(BlockArmor.createIceLeggings());
                items.add(BlockArmor.createIceBoots());
                // Prismarine Armor
                items.add(BlockArmor.createPrismarineHelmet());
                items.add(BlockArmor.createPrismarineChestplate());
                items.add(BlockArmor.createPrismarineLeggings());
                items.add(BlockArmor.createPrismarineBoots());
                // Terracotta Armor
                items.add(BlockArmor.createTerracottaHelmet());
                items.add(BlockArmor.createTerracottaChestplate());
                items.add(BlockArmor.createTerracottaLeggings());
                items.add(BlockArmor.createTerracottaBoots());
                // Mossy Armor
                items.add(BlockArmor.createMossyHelmet());
                items.add(BlockArmor.createMossyChestplate());
                items.add(BlockArmor.createMossyLeggings());
                items.add(BlockArmor.createMossyBoots());
                // Soul Sand Armor
                items.add(BlockArmor.createSoulSandHelmet());
                items.add(BlockArmor.createSoulSandChestplate());
                items.add(BlockArmor.createSoulSandLeggings());
                items.add(BlockArmor.createSoulSandBoots());
                // Magma Armor
                items.add(BlockArmor.createMagmaHelmet());
                items.add(BlockArmor.createMagmaChestplate());
                items.add(BlockArmor.createMagmaLeggings());
                items.add(BlockArmor.createMagmaBoots());
                // Sandstone Armor
                items.add(BlockArmor.createSandstoneHelmet());
                items.add(BlockArmor.createSandstoneChestplate());
                items.add(BlockArmor.createSandstoneLeggings());
                items.add(BlockArmor.createSandstoneBoots());
                // Amethyst Armor
                items.add(BlockArmor.createAmethystHelmet());
                items.add(BlockArmor.createAmethystChestplate());
                items.add(BlockArmor.createAmethystLeggings());
                items.add(BlockArmor.createAmethystBoots());
                // Coal Armor
                items.add(BlockArmor.createCoalHelmet());
                items.add(BlockArmor.createCoalChestplate());
                items.add(BlockArmor.createCoalLeggings());
                items.add(BlockArmor.createCoalBoots());
            }
            case BLOCK_WEAPONS -> {
                // Block Swords
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
            }
            case CUSTOM_ARROWS -> {
                // Add all custom arrow types
                for (CustomArrows.ArrowType type : CustomArrows.getAllArrowTypes()) {
                    items.add(CustomArrows.createArrow(type, 1));
                }
            }
        }
        return items;
    }

    // ──────────────────────────────────────────────────────────────
    // Page 0: Main Category Menu
    // ──────────────────────────────────────────────────────────────

    /** Entry point — opens the main category selection menu. */
    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal("§6§l/customitems — Choose Category"));

        // Background with dark theme
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("§6§l/customitems").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select a category to browse items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any item to receive it").formatted(Formatting.GRAY))
                .glow().build());

        // Place category buttons in a grid
        Category[] cats = Category.values();
        int[] catSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        for (int i = 0; i < cats.length && i < catSlots.length; i++) {
            Category cat = cats[i];
            gui.setSlot(catSlots[i], new GuiElementBuilder(cat.icon)
                    .setName(Text.literal(cat.label).formatted(cat.colour, Formatting.BOLD))
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("✨ Click to browse items").formatted(Formatting.YELLOW))
                    .setCallback((slot, type, action) -> openCategoryPage(player, cat, 0))
                    .build());
        }

        gui.open();
    }

    // ──────────────────────────────────────────────────────────────
    // GEM_BLOCK_GEAR: Armor Sets in Rows Display
    // Each row shows: [Block] [Sword] [Helmet] [Chest] [Legs] [Boots]
    // ──────────────────────────────────────────────────────────────

    private static void openGemBlockGearPage(ServerPlayerEntity player, int page) {
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
        ArmorSetDisplay[] sets = {
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
        };

        final int SETS_PER_PAGE = 4;
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
                    .addLoreLine(Text.literal("✨ Click to receive").formatted(Formatting.YELLOW))
                    .setCallback((s, t, a) -> giveItem(player, set.sword))
                    .build());

            // Armor pieces: Helmet, Chestplate, Leggings, Boots (shifted 1 slot right)
            ItemStack[] armorPieces = { set.helmet, set.chestplate, set.leggings, set.boots };
            for (int j = 0; j < 4; j++) {
                ItemStack armor = armorPieces[j];
                Text armorName = armor.get(DataComponentTypes.CUSTOM_NAME);
                gui.setSlot(slots[3 + j], new GuiElementBuilder(armor.getItem())
                        .setName(armorName != null ? armorName : Text.literal("Armor"))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("✨ Click to receive").formatted(Formatting.YELLOW))
                        .setCallback((s, t, a) -> giveItem(player, armor))
                        .build());
            }
        }

        // ← Back to main menu
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Categories").formatted(Formatting.YELLOW))
                .setCallback((s, t, a) -> open(player)).build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages))
                    .setCallback((s, t, a) -> openGemBlockGearPage(player, prev)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((s, t, a) -> openGemBlockGearPage(player, next)).build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages))
                .build());

        gui.open();
    }

    private static void giveItem(ServerPlayerEntity player, ItemStack item) {
        ItemStack copy = item.copy();
        if (!player.getInventory().insertStack(copy)) {
            player.dropItem(copy, false);
        }
        Text name = item.get(DataComponentTypes.CUSTOM_NAME);
        String itemName = name != null ? name.getString() : item.getItem().toString();
        player.sendMessage(Text.literal("✨ Given " + itemName + "!").formatted(Formatting.GREEN), false);
    }

    // Helper class for armor set display
    private record ArmorSetDisplay(net.minecraft.item.Item blockItem, ItemStack sword, ItemStack helmet,
                                    ItemStack chestplate, ItemStack leggings, ItemStack boots) {}

    // ──────────────────────────────────────────────────────────────
    // Page 1: Category Item List (Standard Layout)
    // ──────────────────────────────────────────────────────────────

    public static void openCategoryPage(ServerPlayerEntity player, Category cat, int page) {
        // Special handling for GEM_BLOCK_GEAR - armor sets in rows
        if (cat == Category.GEM_BLOCK_GEAR) {
            openGemBlockGearPage(player, page);
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
                .addLoreLine(Text.literal("Click an item to receive it").formatted(Formatting.GRAY))
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
                    .addLoreLine(Text.literal("✨ Click to receive").formatted(Formatting.YELLOW))
                    .setCallback((s, t, a) -> {
                        player.getInventory().insertStack(finalItem.copy());
                        String itemName = displayName != null ? displayName.getString() : item.getItem().toString();
                        player.sendMessage(Text.literal("✨ Given " + itemName + "!").formatted(Formatting.GREEN), false);
                    });
            gui.setSlot(slot, elem.build());
            shown++;
        }

        // ← Back to main menu
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Categories").formatted(Formatting.YELLOW))
                .setCallback((s, t, a) -> open(player)).build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + clampedPage + " / " + totalPages))
                    .setCallback((s, t, a) -> openCategoryPage(player, cat, prev)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((s, t, a) -> openCategoryPage(player, cat, next)).build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages))
                .build());

        gui.open();
    }
}
