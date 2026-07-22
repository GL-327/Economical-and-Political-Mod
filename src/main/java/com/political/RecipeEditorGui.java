package com.political;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

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
        GEM_BLOCK_GEAR ("💎 Gem & Block Armor",         Items.EMERALD,            ChatFormatting.GREEN),
        SPECIAL_ARMOR("🛡 Special / Legendary Armor",        Items.NETHERITE_HELMET,   ChatFormatting.GOLD),
        DRAGON_CHESTPLATES("🐉 Dragon chestplates",          Items.DRAGON_HEAD,        ChatFormatting.DARK_PURPLE),
        HPEBM        ("⚡ HPEBM Weapons",                    Items.BLAZE_ROD,          ChatFormatting.LIGHT_PURPLE),
        SPECIAL_ITEMS("🎁 Special Items",                    Items.STICK,              ChatFormatting.GREEN),
        CUSTOM_TOOLS ("⛏ Custom Tools",                    Items.NETHERITE_PICKAXE,  ChatFormatting.DARK_PURPLE),
        PIGLIN       ("👑 Piglin Items",                     Items.GOLDEN_HELMET,      ChatFormatting.GOLD),
        GOLD_ARMOR   ("👑 Gold Armor Evolution",             Items.GOLDEN_CHESTPLATE,  ChatFormatting.GRAY),
        MATERIALS    ("📦 Materials (Cores & Chunks)",       Items.SLIME_BALL,         ChatFormatting.GRAY),
        ATTRIBUTE_TOKENS("✧ Attribute Tokens",               Items.PAPER,              ChatFormatting.LIGHT_PURPLE),
        WEAPON_ATTRIBUTES("⚔ Weapon Attributes",               Items.NETHER_STAR,        ChatFormatting.RED),
        BLOCK_TOOLS   ("⛏ Block Tools",                     Items.DIAMOND_PICKAXE,    ChatFormatting.AQUA),
        BLOCK_ARMOR   ("🧱 Block Armor",                    Items.OBSIDIAN,           ChatFormatting.DARK_PURPLE),
        BLOCK_WEAPONS ("⚔ Full Block Catalogue",            Items.QUARTZ,             ChatFormatting.WHITE),
        ENCHANTED_BLOCK_GEAR("✨ Enchanted block gear",      Items.LAPIS_LAZULI,       ChatFormatting.AQUA),
        SECTOR_HEART_ITEMS("♦ Verdant overalls",            Items.GOLDEN_HOE,         ChatFormatting.GREEN),
        DEEPSTRIKE_SYNDICATE("Deepstrike charter",           Items.NETHER_STAR,        ChatFormatting.GOLD),
        CUSTOM_ARROWS ("➵ Custom Arrows",                    Items.TIPPED_ARROW,       ChatFormatting.GOLD),
        BOUNTY_SETS   ("⚔ Bounty Sets",                      Items.NETHER_STAR,        ChatFormatting.DARK_PURPLE),
        BOSS_TROPHIES ("🏆 World Boss Trophies",             Items.WITHER_SKELETON_SKULL, ChatFormatting.GOLD),
        SKILL_TOMES   ("📖 Skill Tomes",                      Items.ENCHANTED_BOOK,     ChatFormatting.AQUA),
        POWER_ITEMS   ("⚡ Power Items",                      Items.NETHER_STAR,        ChatFormatting.YELLOW),
        ASCENSION_MATERIALS ("⬆ Ascension Materials",        Items.PRISMARINE_SHARD,   ChatFormatting.AQUA),
        ASCENSION_RELICS    ("✧ Ascension Relics",           Items.DRAGON_BREATH,      ChatFormatting.LIGHT_PURPLE),
        MOB_DROPS           ("💀 Mob Drops",                  Items.WITHER_SKELETON_SKULL, ChatFormatting.RED),
        FISHING_RODS        ("🎣 Fishing Rods",              Items.FISHING_ROD,            ChatFormatting.AQUA),
        MAGIC_STAVES        ("✦ Magic Staves",              Items.BLAZE_ROD,              ChatFormatting.LIGHT_PURPLE);

        final String label;
        final net.minecraft.world.item.Item icon;
        final ChatFormatting colour;

        Category(String label, net.minecraft.world.item.Item icon, ChatFormatting colour) {
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
            case BOSS_TROPHIES -> {
                var item = CustomItemManager.getItem("trophy_hollow_knight"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("trophy_mosswarden"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("trophy_scorchfiend"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("trophy_skulltide_captain"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("trophy_nethermaw"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("trophy_wailing_phantom"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("trophy_crystal_golem"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("trophy_blight_mother"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("trophy_voidreaver"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("trophy_infernal_hydra"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("trophy_ancient_one"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("trophy_king_of_ashes"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("sovereign_commendation"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("treasury_fragment"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("treasury_sigil"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("mastery_draught"); if (item != null) items.add(item.createStack(1));
            }
            case SKILL_TOMES -> {
                var item = CustomItemManager.getItem("skill_tome_combat"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("skill_tome_mining"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("skill_tome_farming"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("skill_tome_agility"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("skill_tome_defense"); if (item != null) items.add(item.createStack(1));
            }
            case POWER_ITEMS -> {
                var item = CustomItemManager.getItem("class_change_token"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("hunt_skip_token"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("coin_pouch_small"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("coin_pouch_medium"); if (item != null) items.add(item.createStack(1));
                item = CustomItemManager.getItem("coin_pouch_large"); if (item != null) items.add(item.createStack(1));
            }
            case ASCENSION_MATERIALS -> {
                for (String id : List.of("asc_common_shard", "asc_vein_shard", "asc_blood_fragment", "asc_unstable_star")) {
                    var item = CustomItemManager.getItem(id);
                    if (item != null) {
                        items.add(item.createStack(1));
                    }
                }
            }
            case ASCENSION_RELICS -> {
                for (String id : List.of("asc_rngesus_essence", "asc_hyper_catalyst", "asc_mf_charm", "asc_void_lottery",
                        "asc_sector_compass", "asc_master_core")) {
                    var item = CustomItemManager.getItem(id);
                    if (item != null) {
                        items.add(item.createStack(1));
                    }
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
                    if (ci != null) items.add(ci.createStack(1));
                }
                
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
                items.add(SlayerItems.createDragonCrafterCoreOmega());
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
            case BLOCK_ARMOR -> { BlockReflectiveCatalog.addBlockArmor(items); }
            case BLOCK_WEAPONS -> {
                BlockReflectiveCatalog.addBlockWeapons(items);
                BlockReflectiveCatalog.addBlockArmor(items);
            }
            case ENCHANTED_BLOCK_GEAR -> BlockReflectiveCatalog.addEnchantedBlockGear(items);
            case SECTOR_HEART_ITEMS -> {
                for (int t = 1; t <= 10; t++) {
                    items.add(SectorHeartItems.createGardenOveralls(t));
                }
            }
            case DEEPSTRIKE_SYNDICATE -> items.add(SectorHeartItems.createSyndicateCharter());
            case CUSTOM_ARROWS -> {
                // Add all custom arrow types
                for (CustomArrows.ArrowType type : CustomArrows.getAllArrowTypes()) {
                    items.add(CustomArrows.createArrow(type, 1));
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
                // Slayer cores & chunks
                for (SlayerManager.SlayerType t : SlayerManager.SlayerType.values()) {
                    items.add(SlayerItems.createCore(t));
                    items.add(SlayerItems.createChunk(t));
                }
                // World Boss Trophies
                for (String id : List.of("trophy_hollow_knight", "trophy_mosswarden", "trophy_scorchfiend",
                        "trophy_skulltide_captain", "trophy_nethermaw", "trophy_wailing_phantom",
                        "trophy_crystal_golem", "trophy_blight_mother", "trophy_voidreaver",
                        "trophy_infernal_hydra", "trophy_ancient_one", "trophy_king_of_ashes")) {
                    var ci = CustomItemManager.getItem(id);
                    if (ci != null) items.add(ci.createStack(1));
                }
                // World Boss reward items
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
                // Compacted materials (mob-farmed resources)
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
    // Page 0: Main Category Menu
    // ──────────────────────────────────────────────────────────────

    /** Entry point — opens the main category selection menu. */
    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Set recipe").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        RecipeGuiTheme.fillDoubleChestCatalog(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(Component.literal("Recipe editor").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Choose a category — same shell as /recipe").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Click an output to edit or create its recipe").withStyle(ChatFormatting.DARK_GRAY))
                .glow().build());

        gui.setSlot(1, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Component.literal("🔍 Search Recipes").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Search items by name").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Opens editor on click").withStyle(ChatFormatting.GREEN))
                .setCallback((idx, type, containerInput, slotGui) -> openSearchGui(player))
                .build());

        // Place category buttons matching /recipe layout
        // Row 1: Armor categories (slots 9-14)
        gui.setSlot(10, new GuiElementBuilder(Category.GEM_BLOCK_GEAR.icon)
                .setName(Component.literal(Category.GEM_BLOCK_GEAR.label).withStyle(Category.GEM_BLOCK_GEAR.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to browse items").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openCategoryPage(player, Category.GEM_BLOCK_GEAR, 0))
                .build());

        gui.setSlot(20, new GuiElementBuilder(Category.DRAGON_CHESTPLATES.icon)
                .setName(Component.literal(Category.DRAGON_CHESTPLATES.label).withStyle(Category.DRAGON_CHESTPLATES.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Dragon plates & cores").withStyle(ChatFormatting.GRAY))
                .setCallback((slot, type, containerInput, slotGui) -> openCategoryPage(player, Category.DRAGON_CHESTPLATES, 0))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Category.BOUNTY_SETS.icon)
                .setName(Component.literal(Category.BOUNTY_SETS.label).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("T1 & T2 Armor + Swords").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openBountySetsPage(player, 0))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Category.SPECIAL_ARMOR.icon)
                .setName(Component.literal(Category.SPECIAL_ARMOR.label).withStyle(Category.SPECIAL_ARMOR.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to browse items").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openCategoryPage(player, Category.SPECIAL_ARMOR, 0))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Category.GOLD_ARMOR.icon)
                .setName(Component.literal(Category.GOLD_ARMOR.label).withStyle(Category.GOLD_ARMOR.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to browse items").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openCategoryPage(player, Category.GOLD_ARMOR, 0))
                .build());

        gui.setSlot(28, new GuiElementBuilder(Category.PIGLIN.icon)
                .setName(Component.literal(Category.PIGLIN.label).withStyle(Category.PIGLIN.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to browse items").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openCategoryPage(player, Category.PIGLIN, 0))
                .build());

        // Row 2: Weapons and Tools (slots 18-23)
        gui.setSlot(22, new GuiElementBuilder(Items.NETHERITE_PICKAXE)
                .setName(Component.literal("⛏ Tool Sets").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Block + Legendary tools").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> CustomItemsGui.openToolSetsPage(player, 0,
                        (p, item) -> {
                            SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(item);
                            openRecipeEditor(p, item, r, Category.CUSTOM_TOOLS);
                        },
                        RecipeEditorGui::openMainMenu))
                .build());

        gui.setSlot(24, new GuiElementBuilder(Category.SPECIAL_ITEMS.icon)
                .setName(Component.literal(Category.SPECIAL_ITEMS.label).withStyle(Category.SPECIAL_ITEMS.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to browse items").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openCategoryPage(player, Category.SPECIAL_ITEMS, 0))
                .build());

        // Row 3: Materials and Tokens (slots 27-32)
        gui.setSlot(30, new GuiElementBuilder(Category.MATERIALS.icon)
                .setName(Component.literal(Category.MATERIALS.label).withStyle(Category.MATERIALS.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to browse items").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openCategoryPage(player, Category.MATERIALS, 0))
                .build());

        gui.setSlot(34, new GuiElementBuilder(Category.MOB_DROPS.icon)
                .setName(Component.literal(Category.MOB_DROPS.label).withStyle(Category.MOB_DROPS.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Bounty, World Boss & Enhanced drops").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("💀 Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openMobDropsHub(player))
                .build());

        // Row 4: Additional categories (slots 36-38)
        gui.setSlot(38, new GuiElementBuilder(Category.ATTRIBUTE_TOKENS.icon)
                .setName(Component.literal(Category.ATTRIBUTE_TOKENS.label).withStyle(Category.ATTRIBUTE_TOKENS.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to browse items").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openCategoryPage(player, Category.ATTRIBUTE_TOKENS, 0))
                .build());

        gui.setSlot(40, new GuiElementBuilder(Category.WEAPON_ATTRIBUTES.icon)
                .setName(Component.literal(Category.WEAPON_ATTRIBUTES.label).withStyle(Category.WEAPON_ATTRIBUTES.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to browse items").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openCategoryPage(player, Category.WEAPON_ATTRIBUTES, 0))
                .build());

        gui.setSlot(42, new GuiElementBuilder(Category.HPEBM.icon)
                .setName(Component.literal(Category.HPEBM.label).withStyle(Category.HPEBM.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to browse items").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openCategoryPage(player, Category.HPEBM, 0))
                .build());

        gui.setSlot(36, new GuiElementBuilder(Category.CUSTOM_ARROWS.icon)
                .setName(Component.literal(Category.CUSTOM_ARROWS.label).withStyle(Category.CUSTOM_ARROWS.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to browse items").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openCategoryPage(player, Category.CUSTOM_ARROWS, 0))
                .build());

        gui.setSlot(46, new GuiElementBuilder(Category.ASCENSION_MATERIALS.icon)
                .setName(Component.literal(Category.ASCENSION_MATERIALS.label).withStyle(Category.ASCENSION_MATERIALS.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to browse items").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openCategoryPage(player, Category.ASCENSION_MATERIALS, 0))
                .build());

        gui.setSlot(48, new GuiElementBuilder(Category.ASCENSION_RELICS.icon)
                .setName(Component.literal(Category.ASCENSION_RELICS.label).withStyle(Category.ASCENSION_RELICS.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("✨ Click to browse items").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openCategoryPage(player, Category.ASCENSION_RELICS, 0))
                .build());

        gui.setSlot(44, new GuiElementBuilder(Items.FISHING_ROD)
                .setName(Component.literal("🎣 Fishing Rods").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Custom fishing rods").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openCategoryPage(player, Category.FISHING_RODS, 0))
                .build());

        gui.setSlot(53, new GuiElementBuilder(Items.BLAZE_ROD)
                .setName(Component.literal("✦ Magic Staves").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Magic staff weapons").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("✨ Click to browse").withStyle(ChatFormatting.YELLOW))
                .setCallback((slot, type, containerInput, slotGui) -> openCategoryPage(player, Category.MAGIC_STAVES, 0))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((slot, type, containerInput, slotGui) -> player.closeContainer())
                .build());

        gui.open();
    }

    private static void openMobDropsHub(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Mob drops hub").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        RecipeGuiTheme.fillChest4Hub(gui);

        gui.setSlot(10, new GuiElementBuilder(Items.ROTTEN_FLESH)
                .setName(Component.literal("⚔ Bounty Boss Drops").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Unique drops from slayer bosses").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Zombie, Spider, Skeleton, Slime...").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Edit recipes").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMobDropSubCategory(player, "bounty_boss", 0))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal("🏆 World Boss Drops").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Trophies & rewards from world bosses").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Hollow Knight, Voidreaver, etc.").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Edit recipes").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMobDropSubCategory(player, "world_boss", 0))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.IRON_SWORD)
                .setName(Component.literal("⚡ Enhanced Mob Drops").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Drops from scaled/enhanced mobs").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Reinforced swords, special gear").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Edit recipes").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMobDropSubCategory(player, "enhanced", 0))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("📦 General Mob Materials").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Crafting materials from mobs").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Compacted ores, cores, chunks").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Edit recipes").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMobDropSubCategory(player, "general_materials", 0))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.PRISMARINE_SHARD)
                .setName(Component.literal("⬆ Ascension Items").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Shards, fragments & relics").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Sector ascension materials").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Edit recipes").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMobDropSubCategory(player, "ascension", 0))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMainMenu(player))
                .build());
        gui.open();
    }

    private static List<ItemStack> getMobDropSubItems(String subCategory) {
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

    private static void openMobDropSubCategory(ServerPlayer player, String subCategory, int page) {
        List<SlayerRecipes.Recipe> existingRecipes = RecipeConfigManager.getRecipes();
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
                .addLoreLine(Component.literal("Click an item to edit its recipe").withStyle(ChatFormatting.GRAY))
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

            ItemStack displayStack = CustomItemLore.forSelectPageIcon(item);
            GuiElementBuilder elem = new GuiElementBuilder(displayStack)
                    .setName(displayName != null ? displayName
                            : Component.literal(customId != null ? customId : "Unknown"))
                    .addLoreLine(Component.literal(""));
            if (finalHasRecipe) {
                elem.addLoreLine(recipeEnabled
                        ? Component.literal("✔ Recipe ENABLED").withStyle(ChatFormatting.GREEN)
                        : Component.literal("✘ Recipe DISABLED").withStyle(ChatFormatting.RED));
                elem.addLoreLine(Component.literal("Left-click: edit  |  Right-click: toggle").withStyle(ChatFormatting.GRAY));
            } else {
                elem.addLoreLine(Component.literal("➕ No recipe — click to create").withStyle(ChatFormatting.YELLOW));
            }
            final int finalPage = clampedPage;
            elem.setCallback((s, t, a, __slotGui) -> {
                if (finalHasRecipe && t == eu.pb4.sgui.api.ClickType.MOUSE_RIGHT) {
                    boolean nowEnabled = RecipeConfigManager.toggleRecipe(finalRecipeName);
                    String status = nowEnabled ? "ENABLED" : "DISABLED";
                    player.sendSystemMessage(Component.literal("Recipe '" + finalRecipeName + "' is now " + status)
                            .withStyle(nowEnabled ? ChatFormatting.GREEN : ChatFormatting.RED), false);
                    openMobDropSubCategory(player, subCategory, finalPage);
                } else {
                    openRecipeEditor(player, finalItem, finalRecipe, Category.MOB_DROPS);
                }
            });
            gui.setSlot(slot, elem.build());
            used[slot] = true;
            shown++;
        }

        RecipeCatalogUi.fillEmptyGridSlots(gui, used, false);

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Mob Drops").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMobDropsHub(player)).build());

        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(48, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> openMobDropSubCategory(player, subCategory, prev)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(50, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> openMobDropSubCategory(player, subCategory, next)).build());
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
                    openMainMenu(player);
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
                .setName(Component.literal("🔍 Search Results").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Search: \"" + searchTerm + "\"").withStyle(ChatFormatting.YELLOW))
                .glow().build());
        used[4] = true;

        java.util.List<ItemStack> results = new java.util.ArrayList<>();
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
                    .addLoreLine(Component.literal("✨ Click to edit recipe").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(clickItem);
                        openRecipeEditor(player, clickItem, r, null);
                    })
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
                .setCallback((slot, type, containerInput, slotGui) -> openMainMenu(player))
                .build());

        gui.setSlot(46, new GuiElementBuilder(Items.SPYGLASS)
                .setName(Component.literal("🔍 Search Again").withStyle(ChatFormatting.AQUA))
                .setCallback((slot, type, containerInput, slotGui) -> openSearchGui(player))
                .build());

        gui.open();
    }

    private static void openBountySetsPage(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Bounty sets — editor").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));

        RecipeGuiTheme.fillDoubleChestCatalog(gui);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Category.BOUNTY_SETS.icon)
                .setName(Component.literal("⚔ Bounty Sets").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("T1 & T2 sets displayed in rows:").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("[Icon] [gap] [Sword] [Helmet] [Chest] [Legs] [Boots]").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Click items to edit recipes").withStyle(ChatFormatting.AQUA))
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
                    .setName(Component.literal(set.name + " T1").withStyle(set.color, ChatFormatting.BOLD))
                    .glow().build());

            gui.setSlot(row1Base + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
            
            gui.setSlot(row1Base + 2, new GuiElementBuilder(set.t1Sword.getItem())
                    .setName(set.t1Sword.get(DataComponents.CUSTOM_NAME))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t1Sword);
                        openRecipeEditor(player, set.t1Sword, r, Category.BOUNTY_SETS);
                    })
                    .build());
            gui.setSlot(row1Base + 3, new GuiElementBuilder(set.t1Helmet.getItem())
                    .setName(set.t1Helmet.get(DataComponents.CUSTOM_NAME))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t1Helmet);
                        openRecipeEditor(player, set.t1Helmet, r, Category.BOUNTY_SETS);
                    })
                    .build());
            gui.setSlot(row1Base + 4, new GuiElementBuilder(set.t1Chestplate.getItem())
                    .setName(set.t1Chestplate.get(DataComponents.CUSTOM_NAME))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t1Chestplate);
                        openRecipeEditor(player, set.t1Chestplate, r, Category.BOUNTY_SETS);
                    })
                    .build());
            gui.setSlot(row1Base + 5, new GuiElementBuilder(set.t1Leggings.getItem())
                    .setName(set.t1Leggings.get(DataComponents.CUSTOM_NAME))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t1Leggings);
                        openRecipeEditor(player, set.t1Leggings, r, Category.BOUNTY_SETS);
                    })
                    .build());
            gui.setSlot(row1Base + 6, new GuiElementBuilder(set.t1Boots.getItem())
                    .setName(set.t1Boots.get(DataComponents.CUSTOM_NAME))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t1Boots);
                        openRecipeEditor(player, set.t1Boots, r, Category.BOUNTY_SETS);
                    })
                    .build());

            // Row 2: T2 items
            int row2Base = (baseRow + 1) * 9;
            gui.setSlot(row2Base, new GuiElementBuilder(set.icon)
                    .setName(Component.literal(set.name + " T2").withStyle(set.color, ChatFormatting.BOLD))
                    .glow().build());

            gui.setSlot(row2Base + 1, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());
            
            gui.setSlot(row2Base + 2, new GuiElementBuilder(set.t2Sword.getItem())
                    .setName(set.t2Sword.get(DataComponents.CUSTOM_NAME))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t2Sword);
                        openRecipeEditor(player, set.t2Sword, r, Category.BOUNTY_SETS);
                    })
                    .build());
            gui.setSlot(row2Base + 3, new GuiElementBuilder(set.t2Helmet.getItem())
                    .setName(set.t2Helmet.get(DataComponents.CUSTOM_NAME))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t2Helmet);
                        openRecipeEditor(player, set.t2Helmet, r, Category.BOUNTY_SETS);
                    })
                    .build());
            gui.setSlot(row2Base + 4, new GuiElementBuilder(set.t2Chestplate.getItem())
                    .setName(set.t2Chestplate.get(DataComponents.CUSTOM_NAME))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t2Chestplate);
                        openRecipeEditor(player, set.t2Chestplate, r, Category.BOUNTY_SETS);
                    })
                    .build());
            gui.setSlot(row2Base + 5, new GuiElementBuilder(set.t2Leggings.getItem())
                    .setName(set.t2Leggings.get(DataComponents.CUSTOM_NAME))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t2Leggings);
                        openRecipeEditor(player, set.t2Leggings, r, Category.BOUNTY_SETS);
                    })
                    .build());
            gui.setSlot(row2Base + 6, new GuiElementBuilder(set.t2Boots.getItem())
                    .setName(set.t2Boots.get(DataComponents.CUSTOM_NAME))
                    .setCallback((idx, type, containerInput, slotGui) -> {
                        SlayerRecipes.Recipe r = SlayerRecipes.getRecipeForOutput(set.t2Boots);
                        openRecipeEditor(player, set.t2Boots, r, Category.BOUNTY_SETS);
                    })
                    .build());
        }

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> openBountySetsPage(player, prev)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> openBountySetsPage(player, next)).build());
        }

        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Categories").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMainMenu(player)).build());

        gui.open();
    }

    // ──────────────────────────────────────────────────────────────
    // Page 1: Category Item List
    // ──────────────────────────────────────────────────────────────

    public static void openCategoryPage(ServerPlayer player, Category cat, int page) {
        // Special handling for GEM_BLOCK_GEAR - armor sets in rows
        if (cat == Category.GEM_BLOCK_GEAR) {
            openGemBlockGearPage(player, page);
            return;
        }

        List<SlayerRecipes.Recipe> existingRecipes = RecipeConfigManager.getRecipes();
        List<ItemStack> items = getItemsForCategory(cat);

        boolean sparse = RecipeCatalogUi.sparseMinTwoPagesEditor(cat);
        int totalPages = RecipeCatalogUi.totalPages(items.size(), sparse);
        int clampedPage = Math.min(page, totalPages - 1);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(cat.label).withStyle(cat.colour, ChatFormatting.BOLD));

        if (cat == Category.BLOCK_WEAPONS) {
            for (int i = 0; i < 54; i++) {
                gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
            }
        } else {
            RecipeGuiTheme.fillCategoryItemGrid(gui, cat == Category.SPECIAL_ITEMS);
        }

        boolean[] used = new boolean[54];

        // Header
        gui.setSlot(4, new GuiElementBuilder(cat.icon)
                .setName(Component.literal(cat.label).withStyle(cat.colour, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click an item to edit its recipe").withStyle(ChatFormatting.GRAY))
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

            ItemStack displayStack = CustomItemLore.forSelectPageIcon(item);
            GuiElementBuilder elem = new GuiElementBuilder(displayStack)
                    .setName(displayName != null ? displayName
                            : Component.literal(customId != null ? customId : "Unknown"))
                    .addLoreLine(Component.literal(""));
            if (finalHasRecipe) {
                elem.addLoreLine(recipeEnabled
                        ? Component.literal("✔ Recipe ENABLED").withStyle(ChatFormatting.GREEN)
                        : Component.literal("✘ Recipe DISABLED").withStyle(ChatFormatting.RED));
                elem.addLoreLine(Component.literal("Left-click: edit  |  Right-click: toggle").withStyle(ChatFormatting.GRAY));
            } else {
                elem.addLoreLine(Component.literal("➕ No recipe — click to create").withStyle(ChatFormatting.YELLOW));
            }
            final int finalPage = clampedPage;
            elem.setCallback((s, t, a, __slotGui) -> {
                if (finalHasRecipe && t == eu.pb4.sgui.api.ClickType.MOUSE_RIGHT) {
                    boolean nowEnabled = RecipeConfigManager.toggleRecipe(finalRecipeName);
                    String status = nowEnabled ? "ENABLED" : "DISABLED";
                    player.sendSystemMessage(Component.literal("Recipe '" + finalRecipeName + "' is now " + status).withStyle(nowEnabled ? ChatFormatting.GREEN : ChatFormatting.RED), false);
                    openCategoryPage(player, cat, finalPage);
                } else {
                    openRecipeEditor(player, finalItem, finalRecipe, cat);
                }
            });
            gui.setSlot(slot, elem.build());
            used[slot] = true;
            shown++;
        }

        boolean lastPageComingSoon = sparse && clampedPage == totalPages - 1;
        RecipeCatalogUi.fillEmptyGridSlots(gui, used, lastPageComingSoon);

        // ← Back to main menu
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back to Categories").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMainMenu(player)).build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Page " + clampedPage + " / " + totalPages))
                    .setCallback((s, t, a, __slotGui) -> openCategoryPage(player, cat, prev)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((s, t, a, __slotGui) -> openCategoryPage(player, cat, next)).build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Page " + (clampedPage + 1) + " / " + totalPages))
                .build());

        gui.open();
    }

    // ──────────────────────────────────────────────────────────────
    // GEM_BLOCK_GEAR: Armor Sets in Rows Display
    // Each row: [Block] [gap] [Sword] [Helmet] [Chest] [Legs] [Boots]
    // ──────────────────────────────────────────────────────────────

    private static void openGemBlockGearPage(ServerPlayer player, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Gem & block armor — editor").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));

        // Plain black shell (excluded from shared theme — same as /customitems gem & block hub)
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.literal("")).build());
        }

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("💎 Gem & Block Armor").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Armor sets displayed in rows:").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("[Block] [gap] [Sword] [Helmet] [Chest] [Legs] [Boots]").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Click items to edit recipes").withStyle(ChatFormatting.AQUA))
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
                    .setName(Component.literal("§8" + set.blockItem.toString().replace("_", " ")).withStyle(ChatFormatting.DARK_GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Armor Set Material").withStyle(ChatFormatting.GRAY))
                    .build());

            // Gap slot
            gui.setSlot(slots[1], new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .build());

            // Items: Sword + 4 Armor pieces
            ItemStack[] items = { set.sword, set.helmet, set.chestplate, set.leggings, set.boots };
            for (int j = 0; j < 5; j++) {
                ItemStack item = items[j];
                Component itemName = item.get(DataComponents.CUSTOM_NAME);
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

                ItemStack rowDisp = CustomItemLore.forSelectPageIcon(item);
                GuiElementBuilder elem = new GuiElementBuilder(rowDisp)
                        .setName(itemName != null ? itemName : Component.literal(customId != null ? customId : "Unknown"))
                        .addLoreLine(Component.literal(""));
                if (finalHasRecipe) {
                    elem.addLoreLine(recipeEnabled
                            ? Component.literal("✔ Recipe ENABLED").withStyle(ChatFormatting.GREEN)
                            : Component.literal("✘ Recipe DISABLED").withStyle(ChatFormatting.RED));
                    elem.addLoreLine(Component.literal("Left: edit  |  Right: toggle").withStyle(ChatFormatting.GRAY));
                } else {
                    elem.addLoreLine(Component.literal("➕ No recipe — click to create").withStyle(ChatFormatting.YELLOW));
                }
                final int finalPage = clampedPage;
                elem.setCallback((s, t, a, __slotGui) -> {
                    if (finalHasRecipe && t == eu.pb4.sgui.api.ClickType.MOUSE_RIGHT) {
                        boolean nowEnabled = RecipeConfigManager.toggleRecipe(finalRecipeName);
                        String status = nowEnabled ? "ENABLED" : "DISABLED";
                        player.sendSystemMessage(Component.literal("Recipe '" + finalRecipeName + "' is now " + status).withStyle(nowEnabled ? ChatFormatting.GREEN : ChatFormatting.RED), false);
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
                .setName(Component.literal("← Back to Categories").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMainMenu(player)).build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Page " + (clampedPage + 1) + " / " + totalPages))
                    .setCallback((s, t, a, __slotGui) -> openGemBlockGearPage(player, prev)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((s, t, a, __slotGui) -> openGemBlockGearPage(player, next)).build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("Page " + (clampedPage + 1) + " / " + totalPages))
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
    public static void openRecipeEditor(ServerPlayer player, ItemStack resultItem,
                                         SlayerRecipes.Recipe existing, Category backCat) {
        String autoName = existing != null ? existing.name : null;
        openRecipeEditor(player, resultItem, existing, backCat, autoName);
    }

    /**
     * @param recipeSaveName if non-null, JSON recipe name used on save (required for alternates). If null, uses item display name.
     */
    public static void openRecipeEditor(ServerPlayer player, ItemStack resultItem,
                                         SlayerRecipes.Recipe existing, Category backCat, String recipeSaveName) {
        final ItemStack[] editableResult = {resultItem.copy()};
        ItemStack[] grid = new ItemStack[9];
        boolean[] lockedSlots = new boolean[9]; // Track which slots are locked
        for (int i = 0; i < 9; i++) {
            grid[i] = (existing != null && existing.ingredients != null && existing.ingredients[i] != null)
                    ? existing.ingredients[i].copy() : ItemStack.EMPTY;
            // Preserve the recipe's existing lock state so re-editing a locked (shaped) recipe
            // does not silently downgrade it to shapeless. Only default to unlocked for brand-new recipes.
            lockedSlots[i] = (existing != null && existing.lockedSlots != null && i < existing.lockedSlots.length)
                    ? existing.lockedSlots[i] : false;
        }

        String resultCustomId = SlayerItems.getCustomItemId(resultItem);
        final String saveNameHint = recipeSaveName;

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false) {
            @Override
            public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ContainerInput action) {
                for (int g = 0; g < 9; g++) {
                    if (index == GRID_SLOTS[g]) {
                        // Handle middle-click to toggle lock
                        if (type == ClickType.MOUSE_MIDDLE) {
                            lockedSlots[g] = !lockedSlots[g];
                            String status = lockedSlots[g] ? "§aLOCKED" : "§eUNLOCKED";
                            player.sendSystemMessage(Component.literal("Slot " + (g + 1) + " is now " + status), false);
                            refreshGrid(this, grid, editableResult[0], lockedSlots);
                            return true;
                        }
                        
                        // Prevent modification if slot is locked
                        if (lockedSlots[g]) {
                            player.sendSystemMessage(Component.literal("§cThis slot is locked! Use middle-click to unlock."), false);
                            return true;
                        }
                        
                        ItemStack cursor = player.containerMenu.getCarried();
                        if (!cursor.isEmpty()) {
                            // Allow full count for compacted materials and other items
                            grid[g] = cursor.copy();
                        } else {
                            grid[g] = ItemStack.EMPTY;
                        }
                        refreshGrid(this, grid, editableResult[0], lockedSlots);
                        return true;
                    }
                }
                if (index == SLOT_RESULT) {
                    int current = editableResult[0].getCount();
                    int next = type.isRight ? Math.max(1, current - 1) : Math.min(64, current + 1);
                    editableResult[0].setCount(next);
                    updateResultSlot(this, editableResult[0]);
                    player.sendSystemMessage(Component.literal("§eRecipe output amount: §f" + next), false);
                    return true;
                }
                if (index == SLOT_SAVE) {
                    saveRecipe(player, editableResult[0], grid, lockedSlots, saveNameHint);
                    player.closeContainer();
                    return true;
                }
                if (index == SLOT_CLEAR) {
                    for (int g = 0; g < 9; g++) {
                        if (!lockedSlots[g]) { // Only clear unlocked slots
                            grid[g] = ItemStack.EMPTY;
                        }
                    }
                    refreshGrid(this, grid, editableResult[0], lockedSlots);
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
                    player.sendSystemMessage(Component.literal(status), false);
                    refreshGrid(this, grid, editableResult[0], lockedSlots);
                    return true;
                }
                if (index == SLOT_BACK) {
                    player.closeContainer();
                    if (backCat != null) openCategoryPage(player, backCat, 0);
                    else openMainMenu(player);
                    return true;
                }
                return super.onAnyClick(index, type, action);
            }
        };

        Component resultName = resultItem.get(DataComponents.CUSTOM_NAME);
        gui.setTitle(Component.literal("§6Edit Recipe: §e")
                .append(resultName != null ? resultName
                        : Component.literal(resultCustomId != null ? resultCustomId : "?")));

        RecipeGuiTheme.fillDoubleChestCatalog(gui);

        gui.setSlot(SLOT_HEADER, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("§6Recipe Editor"))
                .addLoreLine(Component.literal("§7Hold an item and left-click a grid slot to set it"))
                .addLoreLine(Component.literal("§7Left-click empty cursor on a slot to clear it"))
                .build());

        gui.setSlot(SLOT_ARROW, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Component.literal("→").withStyle(ChatFormatting.YELLOW)).build());

        updateResultSlot(gui, editableResult[0]);

        gui.setSlot(SLOT_SAVE, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Component.literal("§a§lSave Recipe"))
                .addLoreLine(Component.literal("§7Click to save this recipe."))
                .glow().build());

        gui.setSlot(SLOT_CLEAR, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§c§lClear Grid"))
                .addLoreLine(Component.literal("§7Click to clear all ingredient slots."))
                .addLoreLine(Component.literal("§7Locked slots will not be cleared."))
                .build());

        gui.setSlot(SLOT_LOCK, new GuiElementBuilder(Items.TRIPWIRE_HOOK)
                .setName(Component.literal("§6§lToggle All Locks"))
                .addLoreLine(Component.literal("§7Click to lock/unlock all slots."))
                .addLoreLine(Component.literal("§7Middle-click individual slots to toggle."))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("§dAdd alternate recipe").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Same item output, different ingredients"))
                .addLoreLine(Component.literal("§7Sign: set a §funique §7recipe name"))
                .setCallback((a, b, c, __slotGui) -> openAlternateRecipeNameSign(player, editableResult[0], backCat))
                .build());

        if (existing != null) {
            gui.setSlot(16, new GuiElementBuilder(Items.LAVA_BUCKET)
                    .setName(Component.literal("§cDelete this variant").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("§7Removes: §f" + existing.name))
                    .setCallback((a, b, c, __slotGui) -> {
                        RecipeConfigManager.removeCustomRecipe(existing.name);
                        player.sendSystemMessage(Component.literal("§cRemoved recipe §f" + existing.name), false);
                        player.closeContainer();
                        if (backCat != null) {
                            openCategoryPage(player, backCat, 0);
                        } else {
                            openMainMenu(player);
                        }
                    })
                    .build());
        }

        gui.setSlot(SLOT_BACK, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(backCat != null ? "§7Return to " + backCat.label : "§7Return to main menu"))
                .build());

        // Initial grid display
        refreshGrid(gui, grid, editableResult[0], lockedSlots);
        gui.open();
    }

    /** Convenience overload — back goes to main menu. */
    public static void openRecipeEditor(ServerPlayer player, ItemStack resultItem,
                                         SlayerRecipes.Recipe existing) {
        openRecipeEditor(player, resultItem, existing, null);
    }

    private static void openAlternateRecipeNameSign(ServerPlayer player, ItemStack resultItem, Category backCat) {
        player.sendSystemMessage(Component.literal("Enter the recipe name on the sign (you can use multiple lines).")
                .withStyle(ChatFormatting.GRAY), false);
        eu.pb4.sgui.api.gui.SignGui signGui = new eu.pb4.sgui.api.gui.SignGui(player) {
            @Override
            public void onManualClose() {
                String l0 = this.getLine(0).getString().trim();
                String l1 = this.getLine(1).getString().trim();
                String l2 = this.getLine(2).getString().trim();
                String l3 = this.getLine(3).getString().trim();
                String name = String.join(" ", l0, l1, l2, l3).replaceAll("\\s+", " ").trim();
                if (name.isEmpty()) {
                    player.sendSystemMessage(Component.literal("Alternate cancelled (empty name).").withStyle(ChatFormatting.GRAY), false);
                    return;
                }
                openRecipeEditor(player, resultItem, null, backCat, name);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal(""));
        signGui.setLine(2, Component.literal(""));
        signGui.setLine(3, Component.literal(""));
        signGui.open();
    }

    /** Re-draws the 3×3 grid slots. */
    private static void refreshGrid(SimpleGui gui, ItemStack[] grid, ItemStack resultItem, boolean[] lockedSlots) {
        for (int g = 0; g < 9; g++) {
            ItemStack ing = grid[g];
            boolean isLocked = lockedSlots != null && lockedSlots[g];
            
            if (ing == null || ing.isEmpty()) {
                if (!isLocked) {
                    RecipeGuiTheme.setRecipeEmptyGridSlot(gui, GRID_SLOTS[g]);
                } else {
                    GuiElementBuilder builder = new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                            .setName(Component.literal("§c[Locked — middle-click to unlock]"))
                            .addLoreLine(Component.literal("§7Slot is locked"))
                            .addLoreLine(Component.literal("§7Middle-click to unlock"));
                    gui.setSlot(GRID_SLOTS[g], builder.build());
                }
            } else {
                Component ingName = ing.get(DataComponents.CUSTOM_NAME);
                GuiElementBuilder builder = new GuiElementBuilder(ing.copy())
                        .setName(ingName != null ? ingName : Component.literal(ing.getItem().toString()));
                
                // Add lock status to lore
                if (isLocked) {
                    builder.addLoreLine(Component.literal("§c§lLOCKED SLOT"));
                    builder.addLoreLine(Component.literal("§7Middle-click to unlock"));
                } else {
                    builder.addLoreLine(Component.literal("§7Click with item to replace, empty-hand to clear"));
                    builder.addLoreLine(Component.literal("§7Middle-click to lock slot"));
                }
                
                // Show count if more than 1
                if (ing.getCount() > 1) {
                    builder.setCount(ing.getCount());
                    builder.addLoreLine(Component.literal("§7Amount: " + ing.getCount()).withStyle(ChatFormatting.YELLOW));
                }
                
                // Add red dye overlay for locked slots
                if (isLocked) {
                    builder.glow();
                }
                
                gui.setSlot(GRID_SLOTS[g], builder.build());
            }
        }
    }

    private static void updateResultSlot(SimpleGui gui, ItemStack resultItem) {
        String resultCustomId = SlayerItems.getCustomItemId(resultItem);
        Component resultName = resultItem.get(DataComponents.CUSTOM_NAME);
        GuiElementBuilder res = new GuiElementBuilder(resultItem.copy())
                .setName(resultName != null ? resultName
                        : Component.literal(resultCustomId != null ? resultCustomId : "?"))
                .addLoreLine(Component.literal("§7Left-click: §a+1 output"))
                .addLoreLine(Component.literal("§7Right-click: §c-1 output"))
                .addLoreLine(Component.literal("§7Current amount: §e" + resultItem.getCount()))
                .glow();
        gui.setSlot(SLOT_RESULT, res.build());
    }

    /** Saves the recipe to RecipeConfigManager. */
    private static void saveRecipe(ServerPlayer player, ItemStack resultItem, ItemStack[] grid, boolean[] lockedSlots, String recipeSaveName) {
        Component nameTxt = resultItem.get(DataComponents.CUSTOM_NAME);
        String recipeName;
        if (recipeSaveName != null && !recipeSaveName.isBlank()) {
            recipeName = recipeSaveName.trim();
        } else {
            recipeName = nameTxt != null ? nameTxt.getString() : "Custom Recipe";
        }

        ItemStack[] ingredientsCopy = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            ingredientsCopy[i] = (grid[i] == null || grid[i].isEmpty()) ? ItemStack.EMPTY : grid[i].copy();
        }

        SlayerRecipes.Recipe newRecipe = new SlayerRecipes.Recipe(
                recipeName, resultItem.copy(), ingredientsCopy, lockedSlots, null, 0);
        RecipeConfigManager.addCustomRecipe(newRecipe);
        player.sendSystemMessage(Component.literal("§a✔ Recipe saved for §e" + recipeName + "§a!"), false);
    }

    // ──────────────────────────────────────────────────────────────
    // Legacy public entry-points (backwards compatibility)
    // ──────────────────────────────────────────────────────────────

    /** Opens the main category menu. Called by /setrecipe command. */
    public static void openRecipeList(ServerPlayer player) {
        openMainMenu(player);
    }

    /** Retained for backward-compat — now opens the main menu (page is ignored). */
    public static void openRecipeList(ServerPlayer player, int page) {
        openMainMenu(player);
    }

    // ──────────────────────────────────────────────────────────────
    // Legacy: read-only recipe view (still used by /recipes GUI)
    // ──────────────────────────────────────────────────────────────

    public static void openRecipeView(ServerPlayer player, SlayerRecipes.Recipe recipe) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("§6Recipe: §e" + recipe.name));

        RecipeGuiTheme.fillChest5HubRecipeFrame(gui);

        Component resultName = recipe.result.get(DataComponents.CUSTOM_NAME);
        gui.setSlot(SLOT_HEADER, new GuiElementBuilder(Items.KNOWLEDGE_BOOK)
                .setName(resultName != null ? resultName
                        : Component.literal(recipe.name).withStyle(ChatFormatting.GOLD))
                .glow().build());

        int[] viewGrid = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        for (int i = 0; i < 9; i++) {
            ItemStack ing = (recipe.ingredients != null && i < recipe.ingredients.length)
                    ? recipe.ingredients[i] : null;
            if (ing != null && !ing.isEmpty()) {
                ItemStack show = ing.copy();
                GuiElementBuilder eb = new GuiElementBuilder(show);
                Component ingName = ing.get(DataComponents.CUSTOM_NAME);
                if (ingName != null) eb.setName(ingName);
                if (ing.getCount() > 1) eb.setCount(ing.getCount());
                gui.setSlot(viewGrid[i], eb.build());
            } else {
                RecipeGuiTheme.setRecipeEmptyGridSlot(gui, viewGrid[i]);
            }
        }

        gui.setSlot(23, new GuiElementBuilder(Items.SPECTRAL_ARROW)
                .setName(Component.literal("→").withStyle(ChatFormatting.YELLOW)).build());
        if (!recipe.result.isEmpty()) {
            GuiElementBuilder res = new GuiElementBuilder(recipe.result.copy());
            Component rName = recipe.result.get(DataComponents.CUSTOM_NAME);
            if (rName != null) res.setName(rName);
            res.glow();
            gui.setSlot(24, res.build());
        }

        String slayerName = recipe.requiredSlayer != null ? recipe.requiredSlayer.displayName : "Any";
        gui.setSlot(34, new GuiElementBuilder(Items.PAPER)
                .setName(Component.literal("§eRecipe Info"))
                .addLoreLine(Component.literal("§7Type: §f" + slayerName))
                .addLoreLine(Component.literal("§7Required Level: §f" + recipe.requiredLevel)).build());

        gui.setSlot(36, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.YELLOW))
                .setCallback((idx, type, containerInput, slotGui) -> openMainMenu(player)).build());

        gui.open();
    }
}
