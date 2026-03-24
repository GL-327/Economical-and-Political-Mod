package com.political;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for the /setrecipe admin command.
 *
 * Navigation hierarchy:
 *   Main Menu (category list)
 *     → Category Page (all items in that category)
 *       → Recipe Editor (3×3 grid editor, save/clear, ← Back)
 */
public class RecipeEditorGui {

    // ── Recipe-editor layout constants (9×6 chest) ──────────────
    private static final int[] GRID_SLOTS   = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int SLOT_ARROW     = 23;
    private static final int SLOT_RESULT    = 24;
    private static final int SLOT_SAVE      = 33;
    private static final int SLOT_CLEAR     = 34;
    private static final int SLOT_LOCK      = 35;
    private static final int SLOT_BACK      = 45;
    private static final int SLOT_HEADER    = 4;

    // ── Category definitions ─────────────────────────────────────
    public enum Category {
        GEM_BLOCK_GEAR ("💎 Gem & Block Armor",         Items.EMERALD,            Formatting.GREEN),
        SPECIAL_ARMOR("🛡 Special / Legendary Armor",        Items.NETHERITE_HELMET,   Formatting.GOLD),
        HPEBM        ("⚡ HPEBM Weapons",                    Items.BLAZE_ROD,          Formatting.LIGHT_PURPLE),
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
        BLOCK_ARMOR   ("🧱 Block Armor",                    Items.OBSIDIAN,           Formatting.DARK_PURPLE),
        BLOCK_WEAPONS ("⚔ Block Weapons",                   Items.QUARTZ,             Formatting.WHITE),
        CUSTOM_ARROWS ("➵ Custom Arrows",                    Items.TIPPED_ARROW,       Formatting.GOLD),
        BOUNTY_SETS   ("⚔ Bounty Sets",                      Items.NETHER_STAR,        Formatting.DARK_PURPLE);

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
            }
            case BOUNTY_SETS -> {
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                    items.add(SlayerItems.createSlayerSword(t));
                    items.add(SlayerItems.createSlayerHelmet(t, 1));
                    items.add(SlayerItems.createSlayerChestplate(t, 1));
                    items.add(SlayerItems.createSlayerLeggings(t, 1));
                    items.add(SlayerItems.createSlayerBoots(t, 1));
                    items.add(SlayerItems.createUpgradedSlayerSword(t));
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

                // DRAGON CHESTPLATES
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
                items.add(SlayerItems.createEnchantedBlackstone());
                items.add(SlayerItems.createEnchantedGildedBlackstone());
                items.add(SlayerItems.createEnchantedOakLog());
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
                // 36+ NEW BLOCK ARMORS
                items.add(BlockArmor.createDiamondBlockHelmet());
                items.add(BlockArmor.createDiamondBlockChestplate());
                items.add(BlockArmor.createDiamondBlockLeggings());
                items.add(BlockArmor.createDiamondBlockBoots());
                items.add(BlockArmor.createEmeraldBlockHelmet());
                items.add(BlockArmor.createEmeraldBlockChestplate());
                items.add(BlockArmor.createEmeraldBlockLeggings());
                items.add(BlockArmor.createEmeraldBlockBoots());
                items.add(BlockArmor.createGoldBlockHelmet());
                items.add(BlockArmor.createGoldBlockChestplate());
                items.add(BlockArmor.createGoldBlockLeggings());
                items.add(BlockArmor.createGoldBlockBoots());
                items.add(BlockArmor.createIronBlockHelmet());
                items.add(BlockArmor.createIronBlockChestplate());
                items.add(BlockArmor.createIronBlockLeggings());
                items.add(BlockArmor.createIronBlockBoots());
                items.add(BlockArmor.createLapisBlockHelmet());
                items.add(BlockArmor.createLapisBlockChestplate());
                items.add(BlockArmor.createLapisBlockLeggings());
                items.add(BlockArmor.createLapisBlockBoots());
                items.add(BlockArmor.createCopperBlockHelmet());
                items.add(BlockArmor.createCopperBlockChestplate());
                items.add(BlockArmor.createCopperBlockLeggings());
                items.add(BlockArmor.createCopperBlockBoots());
                items.add(BlockArmor.createAncientDebrisHelmet());
                items.add(BlockArmor.createAncientDebrisChestplate());
                items.add(BlockArmor.createAncientDebrisLeggings());
                items.add(BlockArmor.createAncientDebrisBoots());
                items.add(BlockArmor.createBasaltHelmet());
                items.add(BlockArmor.createBasaltChestplate());
                items.add(BlockArmor.createBasaltLeggings());
                items.add(BlockArmor.createBasaltBoots());
                items.add(BlockArmor.createBlackstoneHelmet());
                items.add(BlockArmor.createBlackstoneChestplate());
                items.add(BlockArmor.createBlackstoneLeggings());
                items.add(BlockArmor.createBlackstoneBoots());
                items.add(BlockArmor.createBoneBlockHelmet());
                items.add(BlockArmor.createBoneBlockChestplate());
                items.add(BlockArmor.createBoneBlockLeggings());
                items.add(BlockArmor.createBoneBlockBoots());
                items.add(BlockArmor.createBrickHelmet());
                items.add(BlockArmor.createBrickChestplate());
                items.add(BlockArmor.createBrickLeggings());
                items.add(BlockArmor.createBrickBoots());
                items.add(BlockArmor.createCactusHelmet());
                items.add(BlockArmor.createCactusChestplate());
                items.add(BlockArmor.createCactusLeggings());
                items.add(BlockArmor.createCactusBoots());
                items.add(BlockArmor.createCalciteHelmet());
                items.add(BlockArmor.createCalciteChestplate());
                items.add(BlockArmor.createCalciteLeggings());
                items.add(BlockArmor.createCalciteBoots());
                items.add(BlockArmor.createDeepslateHelmet());
                items.add(BlockArmor.createDeepslateChestplate());
                items.add(BlockArmor.createDeepslateLeggings());
                items.add(BlockArmor.createDeepslateBoots());
                items.add(BlockArmor.createDripstoneHelmet());
                items.add(BlockArmor.createDripstoneChestplate());
                items.add(BlockArmor.createDripstoneLeggings());
                items.add(BlockArmor.createDripstoneBoots());
                items.add(BlockArmor.createHayHelmet());
                items.add(BlockArmor.createHayChestplate());
                items.add(BlockArmor.createHayLeggings());
                items.add(BlockArmor.createHayBoots());
                items.add(BlockArmor.createHoneycombHelmet());
                items.add(BlockArmor.createHoneycombChestplate());
                items.add(BlockArmor.createHoneycombLeggings());
                items.add(BlockArmor.createHoneycombBoots());
                items.add(BlockArmor.createLilyPadHelmet());
                items.add(BlockArmor.createLilyPadChestplate());
                items.add(BlockArmor.createLilyPadLeggings());
                items.add(BlockArmor.createLilyPadBoots());
                items.add(BlockArmor.createMelonHelmet());
                items.add(BlockArmor.createMelonChestplate());
                items.add(BlockArmor.createMelonLeggings());
                items.add(BlockArmor.createMelonBoots());
                items.add(BlockArmor.createMossBlockHelmet());
                items.add(BlockArmor.createMossBlockChestplate());
                items.add(BlockArmor.createMossBlockLeggings());
                items.add(BlockArmor.createMossBlockBoots());
                items.add(BlockArmor.createMyceliumHelmet());
                items.add(BlockArmor.createMyceliumChestplate());
                items.add(BlockArmor.createMyceliumLeggings());
                items.add(BlockArmor.createMyceliumBoots());
                items.add(BlockArmor.createNetherBrickHelmet());
                items.add(BlockArmor.createNetherBrickChestplate());
                items.add(BlockArmor.createNetherBrickLeggings());
                items.add(BlockArmor.createNetherBrickBoots());
                items.add(BlockArmor.createPumpkinHelmet());
                items.add(BlockArmor.createPumpkinChestplate());
                items.add(BlockArmor.createPumpkinLeggings());
                items.add(BlockArmor.createPumpkinBoots());
                items.add(BlockArmor.createPurpurHelmet());
                items.add(BlockArmor.createPurpurChestplate());
                items.add(BlockArmor.createPurpurLeggings());
                items.add(BlockArmor.createPurpurBoots());
                items.add(BlockArmor.createSandHelmet());
                items.add(BlockArmor.createSandChestplate());
                items.add(BlockArmor.createSandLeggings());
                items.add(BlockArmor.createSandBoots());
                items.add(BlockArmor.createSculkHelmet());
                items.add(BlockArmor.createSculkChestplate());
                items.add(BlockArmor.createSculkLeggings());
                items.add(BlockArmor.createSculkBoots());
                items.add(BlockArmor.createShroomlightHelmet());
                items.add(BlockArmor.createShroomlightChestplate());
                items.add(BlockArmor.createShroomlightLeggings());
                items.add(BlockArmor.createShroomlightBoots());
                items.add(BlockArmor.createSlimeHelmet());
                items.add(BlockArmor.createSlimeChestplate());
                items.add(BlockArmor.createSlimeLeggings());
                items.add(BlockArmor.createSlimeBoots());
                items.add(BlockArmor.createSmoothStoneHelmet());
                items.add(BlockArmor.createSmoothStoneChestplate());
                items.add(BlockArmor.createSmoothStoneLeggings());
                items.add(BlockArmor.createSmoothStoneBoots());
                items.add(BlockArmor.createSnowHelmet());
                items.add(BlockArmor.createSnowChestplate());
                items.add(BlockArmor.createSnowLeggings());
                items.add(BlockArmor.createSnowBoots());
                items.add(BlockArmor.createSoulSoilHelmet());
                items.add(BlockArmor.createSoulSoilChestplate());
                items.add(BlockArmor.createSoulSoilLeggings());
                items.add(BlockArmor.createSoulSoilBoots());
                items.add(BlockArmor.createSpongeHelmet());
                items.add(BlockArmor.createSpongeChestplate());
                items.add(BlockArmor.createSpongeLeggings());
                items.add(BlockArmor.createSpongeBoots());
                items.add(BlockArmor.createTargetHelmet());
                items.add(BlockArmor.createTargetChestplate());
                items.add(BlockArmor.createTargetLeggings());
                items.add(BlockArmor.createTargetBoots());
                items.add(BlockArmor.createTntHelmet());
                items.add(BlockArmor.createTntChestplate());
                items.add(BlockArmor.createTntLeggings());
                items.add(BlockArmor.createTntBoots());
                items.add(BlockArmor.createWarpedHelmet());
                items.add(BlockArmor.createWarpedChestplate());
                items.add(BlockArmor.createWarpedLeggings());
                items.add(BlockArmor.createWarpedBoots());
                items.add(BlockArmor.createWetSpongeHelmet());
                items.add(BlockArmor.createWetSpongeChestplate());
                items.add(BlockArmor.createWetSpongeLeggings());
                items.add(BlockArmor.createWetSpongeBoots());
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
    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6§l/setrecipe — Choose Category"));

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
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Text.literal("§6§l/setrecipe").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select a category to view and edit recipes").formatted(Formatting.GRAY))
                .glow().build());

        gui.setSlot(1, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Text.literal("🔍 Search Recipes").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Search items by name").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Opens editor on click").formatted(Formatting.GREEN))
                .setCallback((idx, type, action) -> openSearchGui(player))
                .build());

        // Place category buttons matching /recipe layout
        // Row 1: Armor categories (slots 9-14)
        gui.setSlot(10, new GuiElementBuilder(Category.GEM_BLOCK_GEAR.icon)
                .setName(Text.literal(Category.GEM_BLOCK_GEAR.label).formatted(Category.GEM_BLOCK_GEAR.colour, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to browse items").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openCategoryPage(player, Category.GEM_BLOCK_GEAR, 0))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Category.BOUNTY_SETS.icon)
                .setName(Text.literal(Category.BOUNTY_SETS.label).formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("T1 & T2 Armor + Swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openBountySetsPage(player, 0))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Category.SPECIAL_ARMOR.icon)
                .setName(Text.literal(Category.SPECIAL_ARMOR.label).formatted(Category.SPECIAL_ARMOR.colour, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to browse items").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openCategoryPage(player, Category.SPECIAL_ARMOR, 0))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Category.GOLD_ARMOR.icon)
                .setName(Text.literal(Category.GOLD_ARMOR.label).formatted(Category.GOLD_ARMOR.colour, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to browse items").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openCategoryPage(player, Category.GOLD_ARMOR, 0))
                .build());

        gui.setSlot(28, new GuiElementBuilder(Category.PIGLIN.icon)
                .setName(Text.literal(Category.PIGLIN.label).formatted(Category.PIGLIN.colour, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to browse items").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openCategoryPage(player, Category.PIGLIN, 0))
                .build());

        // Row 2: Weapons and Tools (slots 18-23)
        gui.setSlot(22, new GuiElementBuilder(Items.NETHERITE_PICKAXE)
                .setName(Text.literal("⛏ Tool Sets").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Block + Legendary tools").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> CustomItemsGui.openToolSetsPage(player, 0,
                        (p, item) -> {
                            SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(item);
                            openRecipeEditor(p, item, r, Category.CUSTOM_TOOLS);
                        },
                        RecipeEditorGui::openMainMenu))
                .build());

        gui.setSlot(24, new GuiElementBuilder(Category.SPECIAL_ITEMS.icon)
                .setName(Text.literal(Category.SPECIAL_ITEMS.label).formatted(Category.SPECIAL_ITEMS.colour, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to browse items").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openCategoryPage(player, Category.SPECIAL_ITEMS, 0))
                .build());

        gui.setSlot(20, new GuiElementBuilder(Category.ENCHANTED_CROPS.icon)
                .setName(Text.literal(Category.ENCHANTED_CROPS.label).formatted(Category.ENCHANTED_CROPS.colour, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to browse items").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openCategoryPage(player, Category.ENCHANTED_CROPS, 0))
                .build());

        // Row 3: Materials and Tokens (slots 27-32)
        gui.setSlot(30, new GuiElementBuilder(Category.MATERIALS.icon)
                .setName(Text.literal(Category.MATERIALS.label).formatted(Category.MATERIALS.colour, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to browse items").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openCategoryPage(player, Category.MATERIALS, 0))
                .build());

        gui.setSlot(32, new GuiElementBuilder(Category.COMPACTED.icon)
                .setName(Text.literal(Category.COMPACTED.label).formatted(Category.COMPACTED.colour, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to browse items").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openCategoryPage(player, Category.COMPACTED, 0))
                .build());

        gui.setSlot(34, new GuiElementBuilder(Category.SUPER_COMPACTED.icon)
                .setName(Text.literal(Category.SUPER_COMPACTED.label).formatted(Category.SUPER_COMPACTED.colour, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to browse items").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openCategoryPage(player, Category.SUPER_COMPACTED, 0))
                .build());

        // Row 4: Additional categories (slots 36-38)
        gui.setSlot(38, new GuiElementBuilder(Category.ATTRIBUTE_TOKENS.icon)
                .setName(Text.literal(Category.ATTRIBUTE_TOKENS.label).formatted(Category.ATTRIBUTE_TOKENS.colour, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to browse items").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openCategoryPage(player, Category.ATTRIBUTE_TOKENS, 0))
                .build());

        gui.setSlot(40, new GuiElementBuilder(Category.WEAPON_ATTRIBUTES.icon)
                .setName(Text.literal(Category.WEAPON_ATTRIBUTES.label).formatted(Category.WEAPON_ATTRIBUTES.colour, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to browse items").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openCategoryPage(player, Category.WEAPON_ATTRIBUTES, 0))
                .build());

        gui.setSlot(42, new GuiElementBuilder(Category.HPEBM.icon)
                .setName(Text.literal(Category.HPEBM.label).formatted(Category.HPEBM.colour, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("✨ Click to browse items").formatted(Formatting.YELLOW))
                .setCallback((slot, type, action) -> openCategoryPage(player, Category.HPEBM, 0))
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
                    openMainMenu(player);
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

        java.util.List<ItemStack> results = new java.util.ArrayList<>();
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
                    .addLoreLine(Text.literal("✨ Click to edit recipe").formatted(Formatting.YELLOW))
                    .setCallback((s, t, a) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(clickItem);
                        openRecipeEditor(player, clickItem, r, null);
                    })
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
                .setCallback((slot, type, action) -> openMainMenu(player))
                .build());

        gui.setSlot(46, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Text.literal("🔍 Search Again").formatted(Formatting.AQUA))
                .setCallback((slot, type, action) -> openSearchGui(player))
                .build());

        gui.open();
    }

    private static void openBountySetsPage(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6⚔ Bounty Sets - Recipe Editor"));

        // Background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Category.BOUNTY_SETS.icon)
                .setName(Text.literal("⚔ Bounty Sets").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("T1 & T2 sets displayed in rows:").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("[Icon] [gap] [Sword] [Helmet] [Chest] [Legs] [Boots]").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Click items to edit recipes").formatted(Formatting.AQUA))
                .glow().build());

        // Define sets
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
            
            // Row 1: T1 items
            int row1Base = baseRow * 9;
            gui.setSlot(row1Base, new GuiElementBuilder(set.icon)
                    .setName(Text.literal(set.name + " T1").formatted(set.color, Formatting.BOLD))
                    .glow().build());

            gui.setSlot(row1Base + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
            
            gui.setSlot(row1Base + 2, new GuiElementBuilder(set.t1Sword.getItem())
                    .setName(set.t1Sword.get(DataComponentTypes.CUSTOM_NAME))
                    .setCallback((idx, type, action) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t1Sword);
                        openRecipeEditor(player, set.t1Sword, r, Category.BOUNTY_SETS);
                    })
                    .build());
            gui.setSlot(row1Base + 3, new GuiElementBuilder(set.t1Helmet.getItem())
                    .setName(set.t1Helmet.get(DataComponentTypes.CUSTOM_NAME))
                    .setCallback((idx, type, action) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t1Helmet);
                        openRecipeEditor(player, set.t1Helmet, r, Category.BOUNTY_SETS);
                    })
                    .build());
            gui.setSlot(row1Base + 4, new GuiElementBuilder(set.t1Chestplate.getItem())
                    .setName(set.t1Chestplate.get(DataComponentTypes.CUSTOM_NAME))
                    .setCallback((idx, type, action) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t1Chestplate);
                        openRecipeEditor(player, set.t1Chestplate, r, Category.BOUNTY_SETS);
                    })
                    .build());
            gui.setSlot(row1Base + 5, new GuiElementBuilder(set.t1Leggings.getItem())
                    .setName(set.t1Leggings.get(DataComponentTypes.CUSTOM_NAME))
                    .setCallback((idx, type, action) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t1Leggings);
                        openRecipeEditor(player, set.t1Leggings, r, Category.BOUNTY_SETS);
                    })
                    .build());
            gui.setSlot(row1Base + 6, new GuiElementBuilder(set.t1Boots.getItem())
                    .setName(set.t1Boots.get(DataComponentTypes.CUSTOM_NAME))
                    .setCallback((idx, type, action) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t1Boots);
                        openRecipeEditor(player, set.t1Boots, r, Category.BOUNTY_SETS);
                    })
                    .build());

            // Row 2: T2 items
            int row2Base = (baseRow + 1) * 9;
            gui.setSlot(row2Base, new GuiElementBuilder(set.icon)
                    .setName(Text.literal(set.name + " T2").formatted(set.color, Formatting.BOLD))
                    .glow().build());

            gui.setSlot(row2Base + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());
            
            gui.setSlot(row2Base + 2, new GuiElementBuilder(set.t2Sword.getItem())
                    .setName(set.t2Sword.get(DataComponentTypes.CUSTOM_NAME))
                    .setCallback((idx, type, action) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t2Sword);
                        openRecipeEditor(player, set.t2Sword, r, Category.BOUNTY_SETS);
                    })
                    .build());
            gui.setSlot(row2Base + 3, new GuiElementBuilder(set.t2Helmet.getItem())
                    .setName(set.t2Helmet.get(DataComponentTypes.CUSTOM_NAME))
                    .setCallback((idx, type, action) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t2Helmet);
                        openRecipeEditor(player, set.t2Helmet, r, Category.BOUNTY_SETS);
                    })
                    .build());
            gui.setSlot(row2Base + 4, new GuiElementBuilder(set.t2Chestplate.getItem())
                    .setName(set.t2Chestplate.get(DataComponentTypes.CUSTOM_NAME))
                    .setCallback((idx, type, action) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t2Chestplate);
                        openRecipeEditor(player, set.t2Chestplate, r, Category.BOUNTY_SETS);
                    })
                    .build());
            gui.setSlot(row2Base + 5, new GuiElementBuilder(set.t2Leggings.getItem())
                    .setName(set.t2Leggings.get(DataComponentTypes.CUSTOM_NAME))
                    .setCallback((idx, type, action) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t2Leggings);
                        openRecipeEditor(player, set.t2Leggings, r, Category.BOUNTY_SETS);
                    })
                    .build());
            gui.setSlot(row2Base + 6, new GuiElementBuilder(set.t2Boots.getItem())
                    .setName(set.t2Boots.get(DataComponentTypes.CUSTOM_NAME))
                    .setCallback((idx, type, action) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t2Boots);
                        openRecipeEditor(player, set.t2Boots, r, Category.BOUNTY_SETS);
                    })
                    .build());
        }

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("← Previous Page").formatted(Formatting.YELLOW))
                    .setCallback((s, t, a) -> openBountySetsPage(player, prev)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("Next Page →").formatted(Formatting.YELLOW))
                    .setCallback((s, t, a) -> openBountySetsPage(player, next)).build());
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Categories").formatted(Formatting.YELLOW))
                .setCallback((s, t, a) -> openMainMenu(player)).build());

        gui.open();
    }

    // ──────────────────────────────────────────────────────────────
    // Page 1: Category Item List
    // ──────────────────────────────────────────────────────────────

    public static void openCategoryPage(ServerPlayerEntity player, Category cat, int page) {
        // Special handling for GEM_BLOCK_GEAR - armor sets in rows
        if (cat == Category.GEM_BLOCK_GEAR) {
            openGemBlockGearPage(player, page);
            return;
        }

        List<SlayerRecipes.Recipe> existingRecipes = RecipeConfigManager.getRecipes();
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
                .addLoreLine(Text.literal("Click an item to edit its recipe").formatted(Formatting.GRAY))
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
            String customId = SlayerItems.getCustomItemId(item);

            // Check for existing recipe
            boolean hasRecipe = false;
            SlayerRecipes.Recipe existingRecipe = null;
            if (customId != null) {
                for (SlayerRecipes.Recipe r : existingRecipes) {
                    if (customId.equals(SlayerItems.getCustomItemId(r.result))) {
                        hasRecipe = true;
                        existingRecipe = r;
                        break;
                    }
                }
            }

            final ItemStack finalItem = item;
            final SlayerRecipes.Recipe finalRecipe = existingRecipe;
            final String finalRecipeName = existingRecipe != null ? existingRecipe.name : null;
            final boolean finalHasRecipe = hasRecipe;
            final boolean recipeEnabled = finalRecipeName != null && RecipeConfigManager.isRecipeEnabled(finalRecipeName);

            GuiElementBuilder elem = new GuiElementBuilder(item.getItem())
                    .setName(displayName != null ? displayName
                            : Text.literal(customId != null ? customId : "Unknown"))
                    .addLoreLine(Text.literal(""));
            if (finalHasRecipe) {
                elem.addLoreLine(recipeEnabled
                        ? Text.literal("✔ Recipe ENABLED").formatted(Formatting.GREEN)
                        : Text.literal("✘ Recipe DISABLED").formatted(Formatting.RED));
                elem.addLoreLine(Text.literal("Left-click: edit  |  Right-click: toggle").formatted(Formatting.GRAY));
            } else {
                elem.addLoreLine(Text.literal("➕ No recipe — click to create").formatted(Formatting.YELLOW));
            }
            final int finalPage = clampedPage;
            elem.setCallback((s, t, a) -> {
                if (finalHasRecipe && t == eu.pb4.sgui.api.ClickType.MOUSE_RIGHT) {
                    boolean nowEnabled = RecipeConfigManager.toggleRecipe(finalRecipeName);
                    String status = nowEnabled ? "ENABLED" : "DISABLED";
                    player.sendMessage(Text.literal("Recipe '" + finalRecipeName + "' is now " + status).formatted(nowEnabled ? Formatting.GREEN : Formatting.RED), false);
                    openCategoryPage(player, cat, finalPage);
                } else {
                    openRecipeEditor(player, finalItem, finalRecipe, cat);
                }
            });
            gui.setSlot(slot, elem.build());
            shown++;
        }

        // ← Back to main menu
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Categories").formatted(Formatting.YELLOW))
                .setCallback((s, t, a) -> openMainMenu(player)).build());

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

    // ──────────────────────────────────────────────────────────────
    // GEM_BLOCK_GEAR: Armor Sets in Rows Display
    // Each row: [Block] [gap] [Sword] [Helmet] [Chest] [Legs] [Boots]
    // ──────────────────────────────────────────────────────────────

    private static void openGemBlockGearPage(ServerPlayerEntity player, int page) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("§6💎 Gem & Block Armor - Recipe Editor"));

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
                .addLoreLine(Text.literal("[Block] [gap] [Sword] [Helmet] [Chest] [Legs] [Boots]").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Click items to edit recipes").formatted(Formatting.AQUA))
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
            new ArmorSetDisplay(Items.WET_SPONGE, BlockWeapons.createWetSpongeSword(),
                BlockArmor.createWetSpongeHelmet(), BlockArmor.createWetSpongeChestplate(),
                BlockArmor.createWetSpongeLeggings(), BlockArmor.createWetSpongeBoots()),
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

        List<SlayerRecipes.Recipe> existingRecipes = RecipeConfigManager.getRecipes();

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

            // Gap slot
            gui.setSlot(slots[1], new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
                    .build());

            // Items: Sword + 4 Armor pieces
            ItemStack[] items = { set.sword, set.helmet, set.chestplate, set.leggings, set.boots };
            for (int j = 0; j < 5; j++) {
                ItemStack item = items[j];
                Text itemName = item.get(DataComponentTypes.CUSTOM_NAME);
                String customId = SlayerItems.getCustomItemId(item);

                // Check for existing recipe
                boolean hasRecipe = false;
                SlayerRecipes.Recipe existingRecipe = null;
                if (customId != null) {
                    for (SlayerRecipes.Recipe r : existingRecipes) {
                        if (customId.equals(SlayerItems.getCustomItemId(r.result))) {
                            hasRecipe = true;
                            existingRecipe = r;
                            break;
                        }
                    }
                }

                final ItemStack finalItem = item;
                final SlayerRecipes.Recipe finalRecipe = existingRecipe;
                final String finalRecipeName = existingRecipe != null ? existingRecipe.name : null;
                final boolean finalHasRecipe = hasRecipe;
                final boolean recipeEnabled = finalRecipeName != null && RecipeConfigManager.isRecipeEnabled(finalRecipeName);

                GuiElementBuilder elem = new GuiElementBuilder(item.getItem())
                        .setName(itemName != null ? itemName : Text.literal(customId != null ? customId : "Unknown"))
                        .addLoreLine(Text.literal(""));
                if (finalHasRecipe) {
                    elem.addLoreLine(recipeEnabled
                            ? Text.literal("✔ Recipe ENABLED").formatted(Formatting.GREEN)
                            : Text.literal("✘ Recipe DISABLED").formatted(Formatting.RED));
                    elem.addLoreLine(Text.literal("Left: edit  |  Right: toggle").formatted(Formatting.GRAY));
                } else {
                    elem.addLoreLine(Text.literal("➕ No recipe — click to create").formatted(Formatting.YELLOW));
                }
                final int finalPage = clampedPage;
                elem.setCallback((s, t, a) -> {
                    if (finalHasRecipe && t == eu.pb4.sgui.api.ClickType.MOUSE_RIGHT) {
                        boolean nowEnabled = RecipeConfigManager.toggleRecipe(finalRecipeName);
                        String status = nowEnabled ? "ENABLED" : "DISABLED";
                        player.sendMessage(Text.literal("Recipe '" + finalRecipeName + "' is now " + status).formatted(nowEnabled ? Formatting.GREEN : Formatting.RED), false);
                        openGemBlockGearPage(player, finalPage);
                    } else {
                        openRecipeEditor(player, finalItem, finalRecipe, Category.GEM_BLOCK_GEAR);
                    }
                });
                gui.setSlot(slots[2 + j], elem.build());
            }
        }

        // ← Back to main menu
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back to Categories").formatted(Formatting.YELLOW))
                .setCallback((s, t, a) -> openMainMenu(player)).build());

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

    // Helper class for armor set display
    private record ArmorSetDisplay(Item blockItem, ItemStack sword, ItemStack helmet,
                                    ItemStack chestplate, ItemStack leggings, ItemStack boots) {}

    // ──────────────────────────────────────────────────────────────
    // Page 2: Recipe Editor  (unchanged core logic)
    // ──────────────────────────────────────────────────────────────

    /**
     * Opens the editable recipe grid for the given result item.
     *
     * @param player     the admin
     * @param resultItem the item the recipe produces
     * @param existing   an existing recipe to pre-populate (may be null)
     * @param backCat    the category to return to when ← Back is clicked (may be null → opens main menu)
     */
    public static void openRecipeEditor(ServerPlayerEntity player, ItemStack resultItem,
                                         SlayerRecipes.Recipe existing, Category backCat) {
        ItemStack[] grid = new ItemStack[9];
        boolean[] lockedSlots = new boolean[9]; // Track which slots are locked
        for (int i = 0; i < 9; i++) {
            grid[i] = (existing != null && existing.ingredients != null && existing.ingredients[i] != null)
                    ? existing.ingredients[i].copy() : ItemStack.EMPTY;
            lockedSlots[i] = false; // Start with all slots unlocked
        }

        String resultCustomId = SlayerItems.getCustomItemId(resultItem);

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false) {
            @Override
            public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
                for (int g = 0; g < 9; g++) {
                    if (index == GRID_SLOTS[g]) {
                        // Handle middle-click to toggle lock
                        if (type == ClickType.MOUSE_MIDDLE) {
                            lockedSlots[g] = !lockedSlots[g];
                            String status = lockedSlots[g] ? "§aLOCKED" : "§eUNLOCKED";
                            player.sendMessage(Text.literal("Slot " + (g + 1) + " is now " + status), false);
                            refreshGrid(this, grid, resultItem, lockedSlots);
                            return true;
                        }
                        
                        // Prevent modification if slot is locked
                        if (lockedSlots[g]) {
                            player.sendMessage(Text.literal("§cThis slot is locked! Use middle-click to unlock."), false);
                            return true;
                        }
                        
                        ItemStack cursor = player.currentScreenHandler.getCursorStack();
                        if (!cursor.isEmpty()) {
                            // Allow full count for compacted materials and other items
                            grid[g] = cursor.copy();
                        } else {
                            grid[g] = ItemStack.EMPTY;
                        }
                        refreshGrid(this, grid, resultItem, lockedSlots);
                        return true;
                    }
                }
                if (index == SLOT_SAVE) {
                    saveRecipe(player, resultItem, grid, lockedSlots);
                    player.closeHandledScreen();
                    return true;
                }
                if (index == SLOT_CLEAR) {
                    for (int g = 0; g < 9; g++) {
                        if (!lockedSlots[g]) { // Only clear unlocked slots
                            grid[g] = ItemStack.EMPTY;
                        }
                    }
                    refreshGrid(this, grid, resultItem, lockedSlots);
                    return true;
                }
                if (index == SLOT_LOCK) {
                    // Toggle all slots lock state
                    boolean allLocked = true;
                    for (boolean locked : lockedSlots) {
                        if (!locked) {
                            allLocked = false;
                            break;
                        }
                    }
                    
                    // If all are locked, unlock all. Otherwise, lock all.
                    for (int g = 0; g < 9; g++) {
                        lockedSlots[g] = !allLocked;
                    }
                    
                    String status = allLocked ? "§eAll slots UNLOCKED" : "§aAll slots LOCKED";
                    player.sendMessage(Text.literal(status), false);
                    refreshGrid(this, grid, resultItem, lockedSlots);
                    return true;
                }
                if (index == SLOT_BACK) {
                    player.closeHandledScreen();
                    if (backCat != null) openCategoryPage(player, backCat, 0);
                    else openMainMenu(player);
                    return true;
                }
                return super.onAnyClick(index, type, action);
            }
        };

        Text resultName = resultItem.get(DataComponentTypes.CUSTOM_NAME);
        gui.setTitle(Text.literal("§6Edit Recipe: §e")
                .append(resultName != null ? resultName
                        : Text.literal(resultCustomId != null ? resultCustomId : "?")));

        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 54; i++) gui.setSlot(i, bg.build());

        gui.setSlot(SLOT_HEADER, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal("§6Recipe Editor"))
                .addLoreLine(Text.literal("§7Hold an item and left-click a grid slot to set it"))
                .addLoreLine(Text.literal("§7Left-click empty cursor on a slot to clear it"))
                .build());

        gui.setSlot(SLOT_ARROW, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("→").formatted(Formatting.YELLOW)).build());

        GuiElementBuilder res = new GuiElementBuilder(resultItem.getItem())
                .setName(resultName != null ? resultName
                        : Text.literal(resultCustomId != null ? resultCustomId : "?"))
                .glow();
        gui.setSlot(SLOT_RESULT, res.build());

        gui.setSlot(SLOT_SAVE, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Text.literal("§a§lSave Recipe"))
                .addLoreLine(Text.literal("§7Click to save this recipe."))
                .glow().build());

        gui.setSlot(SLOT_CLEAR, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("§c§lClear Grid"))
                .addLoreLine(Text.literal("§7Click to clear all ingredient slots."))
                .addLoreLine(Text.literal("§7Locked slots will not be cleared."))
                .build());

        gui.setSlot(SLOT_LOCK, new GuiElementBuilder(Items.TRIPWIRE_HOOK)
                .setName(Text.literal("§6§lToggle All Locks"))
                .addLoreLine(Text.literal("§7Click to lock/unlock all slots."))
                .addLoreLine(Text.literal("§7Middle-click individual slots to toggle."))
                .build());

        gui.setSlot(SLOT_BACK, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(backCat != null ? "§7Return to " + backCat.label : "§7Return to main menu"))
                .build());

        // Initial grid display
        refreshGrid(gui, grid, resultItem, lockedSlots);
        gui.open();
    }

    /** Convenience overload — back goes to main menu. */
    public static void openRecipeEditor(ServerPlayerEntity player, ItemStack resultItem,
                                         SlayerRecipes.Recipe existing) {
        openRecipeEditor(player, resultItem, existing, null);
    }

    /** Re-draws the 3×3 grid slots. */
    private static void refreshGrid(SimpleGui gui, ItemStack[] grid, ItemStack resultItem, boolean[] lockedSlots) {
        for (int g = 0; g < 9; g++) {
            ItemStack ing = grid[g];
            boolean isLocked = lockedSlots != null && lockedSlots[g];
            
            if (ing == null || ing.isEmpty()) {
                GuiElementBuilder builder = new GuiElementBuilder(
                    isLocked ? Items.RED_STAINED_GLASS_PANE : Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setName(Text.literal(isLocked ? "§c[Locked — middle-click to unlock]" : "§8[Empty — click with item to set]"));
                
                if (isLocked) {
                    builder.addLoreLine(Text.literal("§7Slot is locked"))
                            .addLoreLine(Text.literal("§7Middle-click to unlock"));
                } else {
                    builder.addLoreLine(Text.literal("§7Middle-click to lock slot"));
                }
                
                gui.setSlot(GRID_SLOTS[g], builder.build());
            } else {
                Text ingName = ing.get(DataComponentTypes.CUSTOM_NAME);
                GuiElementBuilder builder = new GuiElementBuilder(ing.getItem())
                        .setName(ingName != null ? ingName : Text.of(ing.getItem().toString()));
                
                // Add lock status to lore
                if (isLocked) {
                    builder.addLoreLine(Text.literal("§c§lLOCKED SLOT"));
                    builder.addLoreLine(Text.literal("§7Middle-click to unlock"));
                } else {
                    builder.addLoreLine(Text.literal("§7Click with item to replace, empty-hand to clear"));
                    builder.addLoreLine(Text.literal("§7Middle-click to lock slot"));
                }
                
                // Show count if more than 1
                if (ing.getCount() > 1) {
                    builder.setCount(ing.getCount());
                    builder.addLoreLine(Text.literal("§7Amount: " + ing.getCount()).formatted(Formatting.YELLOW));
                }
                
                // Add red dye overlay for locked slots
                if (isLocked) {
                    builder.glow();
                }
                
                gui.setSlot(GRID_SLOTS[g], builder.build());
            }
        }
    }

    /** Saves the recipe to RecipeConfigManager. */
    private static void saveRecipe(ServerPlayerEntity player, ItemStack resultItem, ItemStack[] grid, boolean[] lockedSlots) {
        Text nameTxt = resultItem.get(DataComponentTypes.CUSTOM_NAME);
        String recipeName = nameTxt != null ? nameTxt.getString() : "Custom Recipe";

        String resultId = SlayerItems.getCustomItemId(resultItem);
        RecipeConfigManager.removeCustomRecipe(recipeName);
        if (resultId != null) {
            for (SlayerRecipes.Recipe existing : new ArrayList<>(RecipeConfigManager.getCustomRecipes())) {
                if (resultId.equals(SlayerItems.getCustomItemId(existing.result))) {
                    RecipeConfigManager.removeCustomRecipe(existing.name);
                }
            }
        }

        ItemStack[] ingredientsCopy = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            ingredientsCopy[i] = (grid[i] == null || grid[i].isEmpty()) ? ItemStack.EMPTY : grid[i].copy();
        }

        SlayerRecipes.Recipe newRecipe = new SlayerRecipes.Recipe(
                recipeName, resultItem.copy(), ingredientsCopy, lockedSlots, null, 0);
        RecipeConfigManager.addCustomRecipe(newRecipe);
        player.sendMessage(Text.literal("§a✔ Recipe saved for §e" + recipeName + "§a!"), false);
    }

    // ──────────────────────────────────────────────────────────────
    // Legacy public entry-points (backwards compatibility)
    // ──────────────────────────────────────────────────────────────

    /** Opens the main category menu. Called by /setrecipe command. */
    public static void openRecipeList(ServerPlayerEntity player) {
        openMainMenu(player);
    }

    /** Retained for backward-compat — now opens the main menu (page is ignored). */
    public static void openRecipeList(ServerPlayerEntity player, int page) {
        openMainMenu(player);
    }

    // ──────────────────────────────────────────────────────────────
    // Legacy: read-only recipe view (still used by /recipes GUI)
    // ──────────────────────────────────────────────────────────────

    public static void openRecipeView(ServerPlayerEntity player, SlayerRecipes.Recipe recipe) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("§6Recipe: §e" + recipe.name));

        GuiElementBuilder bg = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal(""));
        for (int i = 0; i < 45; i++) gui.setSlot(i, bg.build());

        Text resultName = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
        gui.setSlot(SLOT_HEADER, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(resultName != null ? resultName
                        : Text.literal(recipe.name).formatted(Formatting.GOLD))
                .glow().build());

        int[] viewGrid = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        for (int i = 0; i < 9; i++) {
            ItemStack ing = (recipe.ingredients != null && i < recipe.ingredients.length)
                    ? recipe.ingredients[i] : null;
            if (ing != null && !ing.isEmpty()) {
                GuiElementBuilder eb = new GuiElementBuilder(ing.getItem());
                Text ingName = ing.get(DataComponentTypes.CUSTOM_NAME);
                if (ingName != null) eb.setName(ingName);
                if (ing.getCount() > 1) eb.setCount(ing.getCount());
                gui.setSlot(viewGrid[i], eb.build());
            }
        }

        gui.setSlot(23, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Text.literal("→").formatted(Formatting.YELLOW)).build());
        if (!recipe.result.isEmpty()) {
            GuiElementBuilder res = new GuiElementBuilder(recipe.result.getItem());
            Text rName = recipe.result.get(DataComponentTypes.CUSTOM_NAME);
            if (rName != null) res.setName(rName);
            res.glow();
            gui.setSlot(24, res.build());
        }

        String slayerName = recipe.requiredSlayer != null ? recipe.requiredSlayer.displayName : "Any";
        gui.setSlot(34, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("§eRecipe Info"))
                .addLoreLine(Text.literal("§7Type: §f" + slayerName))
                .addLoreLine(Text.literal("§7Required Level: §f" + recipe.requiredLevel)).build());

        gui.setSlot(36, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openMainMenu(player)).build());

        gui.open();
    }
}
