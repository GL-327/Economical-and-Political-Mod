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
    public enum Category {
        CUSTOM_ARMOR ("💎 Custom Armor",                    Items.EMERALD,            Formatting.GREEN),
        SWORDS_T1    ("⚔ Bounty Swords (T1)",               Items.IRON_SWORD,         Formatting.YELLOW),
        SWORDS_T2    ("⚔ Bounty Swords (T2)",               Items.DIAMOND_SWORD,      Formatting.AQUA),
        ARMOR_T1     ("🛡 Bounty Armor T1",                  Items.IRON_CHESTPLATE,    Formatting.GREEN),
        ARMOR_T2     ("🛡 Bounty Armor T2",                  Items.DIAMOND_CHESTPLATE, Formatting.DARK_AQUA),
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
    // Page 0: Main Category Menu - Matches RecipesGui layout
    // ──────────────────────────────────────────────────────────────

    /** Entry point — opens the main category selection menu. */
    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📦 Custom Items"));

        // Background with dark theme
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Text.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("📦 Custom Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select a category to get items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Click any item to receive it").formatted(Formatting.YELLOW))
                .glow().build());

        // Row 1: Armor categories - spread across more slots
        gui.setSlot(9, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("💎 Custom Armor").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Emerald, Lapis armors").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.CUSTOM_ARMOR, 0))
                .build());

        gui.setSlot(10, new GuiElementBuilder(Items.OBSIDIAN)
                .setName(Text.literal("🧱 Block Armor").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Block-themed armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.BLOCK_ARMOR, 0))
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⚔ Bounty Sets").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("T1 & T2 Armor + Swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.ARMOR_T1, 0))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.NETHERITE_HELMET)
                .setName(Text.literal("🛡 Special Armor").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Legendary armor").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.SPECIAL_ARMOR, 0))
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.GOLDEN_CHESTPLATE)
                .setName(Text.literal("👑 Gold Armor").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Gold armor tiers").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.GOLD_ARMOR, 0))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Text.literal("👑 Piglin Items").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Piglin-themed items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.PIGLIN, 0))
                .build());

        // Row 2: Weapons and Tools
        gui.setSlot(18, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⚔ Block Weapons").formatted(Formatting.WHITE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Block-themed swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.BLOCK_WEAPONS, 0))
                .build());

        gui.setSlot(19, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Text.literal("⚔ Bounty Swords T1").formatted(Formatting.YELLOW, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("T1 slayer swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.SWORDS_T1, 0))
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Text.literal("⚔ Bounty Swords T2").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("T2 slayer swords").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.SWORDS_T2, 0))
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.DIAMOND_PICKAXE)
                .setName(Text.literal("⛏ Block Tools").formatted(Formatting.AQUA, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Block-themed tools").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.BLOCK_TOOLS, 0))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.NETHERITE_PICKAXE)
                .setName(Text.literal("⛏ Custom Tools").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Legendary tools").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.CUSTOM_TOOLS, 0))
                .build());

        gui.setSlot(23, new GuiElementBuilder(Items.STICK)
                .setName(Text.literal("🎁 Special Items").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Special unique items").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.SPECIAL_ITEMS, 0))
                .build());

        // Row 3: Materials and Tokens
        gui.setSlot(27, new GuiElementBuilder(Items.SLIME_BALL)
                .setName(Text.literal("📦 Materials").formatted(Formatting.GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Cores & chunks").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.MATERIALS, 0))
                .build());

        gui.setSlot(28, new GuiElementBuilder(Items.RAW_IRON)
                .setName(Text.literal("⬛ Compacted").formatted(Formatting.DARK_GRAY, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Compacted resources").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.COMPACTED, 0))
                .build());

        gui.setSlot(29, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Text.literal("⭐ Super Compacted").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Super compacted").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.SUPER_COMPACTED, 0))
                .build());

        gui.setSlot(30, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("✧ Armor Tokens").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Armor attributes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.ATTRIBUTE_TOKENS, 0))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("⚔ Weapon Tokens").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Weapon attributes").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.WEAPON_ATTRIBUTES, 0))
                .build());

        gui.setSlot(32, new GuiElementBuilder(Items.BLAZE_ROD)
                .setName(Text.literal("⚡ HPEBM Weapons").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Energy beam weapons").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.HPEBM, 0))
                .build());

        // Row 4: Additional categories
        gui.setSlot(36, new GuiElementBuilder(Items.COBBLESTONE)
                .setName(Text.literal("✧ Enchanted Blocks").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Enchanted blocks").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.ENCHANTED_BLOCKS, 0))
                .build());

        gui.setSlot(37, new GuiElementBuilder(Items.WHEAT)
                .setName(Text.literal("✧ Enchanted Crops").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Enchanted crops & flowers").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.ENCHANTED_CROPS, 0))
                .build());

        gui.setSlot(38, new GuiElementBuilder(Items.TIPPED_ARROW)
                .setName(Text.literal("➵ Custom Arrows").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Special arrows").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("✨ Click to browse").formatted(Formatting.YELLOW))
                .setCallback((idx, type, action) -> openCategoryPage(player, Category.CUSTOM_ARROWS, 0))
                .build());

        // Close button
        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("✖ Close").formatted(Formatting.RED, Formatting.BOLD))
                .setCallback((slot, clickType, action) -> gui.close())
                .build());

        gui.open();
    }

    // ──────────────────────────────────────────────────────────────
    // GEM_BLOCK_GEAR: Armor Sets in Rows Display
    // Each row shows: [Block] [Sword] [Helmet] [Chest] [Legs] [Boots]
    // ──────────────────────────────────────────────────────────────

    public static void openGemBlockGearPage(ServerPlayerEntity player, int page) {
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
                    .addLoreLine(Text.literal("✨ Click to get item").formatted(Formatting.YELLOW))
                    .setCallback((s, t, a) -> giveItem(player, set.sword))
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
                        .setCallback((s, t, a) -> giveItem(player, finalArmor))
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
        // Special handling for CUSTOM_ARMOR - armor sets in rows
        if (cat == Category.CUSTOM_ARMOR) {
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
                    .setCallback((s, t, a) -> giveItem(player, finalItem));
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

        gui.setSlot(50, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("Page " + (clampedPage + 1) + " / " + totalPages))
                .build());

        gui.open();
    }

    // ──────────────────────────────────────────────────────────────
    // RECIPE VIEW - Show item recipe in a GUI
    // ──────────────────────────────────────────────────────────────

    private static void showItemInfo(ServerPlayerEntity player, ItemStack resultItem) {
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
                .setCallback((slot, type, action) -> open(player))
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
