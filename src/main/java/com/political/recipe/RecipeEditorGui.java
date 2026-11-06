package com.political.recipe;
import com.political.ArmourAttribute;
import com.political.CustomArrows;
import com.political.CustomItemLore;
import com.political.CustomItemsGui;
import com.political.DanielsPickaxe;
import com.political.FishingRods;
import com.political.RecipeCatalogUi;
import com.political.RecipeGuiTheme;
import com.political.SectorHeartItems;
import com.political.SkyblockTrinkets;
import com.political.WeaponAttribute;



import com.political.bounty.SlayerItems;
import com.political.bounty.SlayerManager;
import com.political.items.CustomItemHandler;
import com.political.items.CustomItemManager;
import com.political.items.DragonChestplateRegistry;
import com.political.items.GemGear;
import com.political.items.LegendaryTools;
import com.political.magic.MagicStaff;
import eu.pb4.sgui.api.ClickType;
import com.political.core.gui.CatalogUi;
import com.political.core.gui.CatalogModel;
import com.political.core.gui.Screen;
import com.political.core.gui.Elements;
import com.political.core.gui.Palette;
import com.political.core.gui.Pager;
import com.political.core.gui.ReturnTo;
import com.political.core.text.Text;
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

    private static final ReturnTo PARENT = new ReturnTo();
    private static final ReturnTo ROOT_PAGE = new ReturnTo();
    private static final ReturnTo LIST_RETURN = new ReturnTo();

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
                // Block armors - Obsidian
                // Block armors - Quartz
                // Block armors - Glowstone
                // Block armors - Redstone
                // Block armors - Netherrack
                // Block armors - End Stone
                // Block armors - Ice
                // Block armors - Prismarine
                // Block armors - Terracotta
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
            case BLOCK_WEAPONS -> {
            }
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
        Runnable previous = ROOT_PAGE.get(player.getUUID());
        if (previous != null) previous.run(); else openRoot(player, 0);
    }

    public static void openFrom(ServerPlayer player, Runnable parent) {
        // A command is an explicit root even if its packet follows a menu close in the same server tick.
        if (parent == null) com.political.core.gui.MenuTrail.forget(player.getUUID());
        PARENT.set(player.getUUID(), parent);
        openRoot(player, 0);
    }

    private static Component categoryTitle(Category cat) {
        String key = "political.catalog.category." + cat.name().toLowerCase(java.util.Locale.ROOT);
        return Text.tr(key);
    }

    public static List<Category> catalogCategories() {
        List<Category> categories = new ArrayList<>(List.of(Category.GEM_BLOCK_GEAR, Category.DRAGON_CHESTPLATES, Category.BOUNTY_SETS,
                Category.SPECIAL_ARMOR, Category.GOLD_ARMOR, Category.CUSTOM_TOOLS, Category.SPECIAL_ITEMS, Category.PIGLIN));
        for (Category cat : Category.values()) if (!categories.contains(cat)) categories.add(cat);
        return categories;
    }

    private static void openRoot(ServerPlayer player, int page) {
        java.util.List<CatalogUi.Tile> tiles = new ArrayList<>();
        for (Category cat : catalogCategories()) {
            tiles.add(new CatalogUi.Tile(cat.icon, categoryTitle(cat),
                    (cat == Category.GEM_BLOCK_GEAR ? CustomItemsGui.catalogItemCount(CustomItemsGui.Category.CUSTOM_ARMOR) : getItemsForCategory(cat).size()),
                    () -> openCatalogCategory(player, cat)));
        }
        showRoot(player, page, List.copyOf(tiles));
    }

    private static void openCatalogCategory(ServerPlayer player, Category cat) {
        if (cat == Category.BOUNTY_SETS) openBountySetsPage(player, 0);
        else if (cat == Category.MOB_DROPS) openMobDropsHub(player);
        else if (cat == Category.CUSTOM_TOOLS) {
            CustomItemsGui.openToolSetsPage(player, 0, (p, item) -> {
                var current = eu.pb4.sgui.api.SguiUtils.getCurrentGui(p);
                if (current instanceof SimpleGui source) LIST_RETURN.set(p.getUUID(), () -> source.open());
                openRecipeEditor(p, item, SlayerRecipes.getRecipeForOutput(item), Category.CUSTOM_TOOLS);
            }, RecipeEditorGui::openMainMenu);
        } else openCategoryPage(player, cat, 0);
    }

    private static void showRoot(ServerPlayer player, int page, List<CatalogUi.Tile> tiles) {
        ROOT_PAGE.set(player.getUUID(), () -> showRoot(player, page, tiles));
        CatalogUi.categories(player, Text.tr("political.catalog.editor"), Text.tr("political.catalog.edit_hint"),
                tiles, page, next -> showRoot(player, next, tiles), () -> openSearchGui(player), PARENT.get(player.getUUID())).open();
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
        final int finalPage = clampedPage;

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
        CatalogUi.search(player, query -> showSearchResults(player, query), () -> openMainMenu(player));
    }

    private static void showSearchResults(ServerPlayer player, String searchTerm) {
        List<ItemStack> items = new ArrayList<>();
        for (Category cat : Category.values()) items.addAll(getItemsForCategory(cat));
        openEditorSearch(player, RecipeItems.unique(items), searchTerm, () -> openMainMenu(player));
    }

    private static void openBountySetsPage(ServerPlayer player, int page) {
        LIST_RETURN.set(player.getUUID(), () -> openBountySetsPage(player, page));
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
        final int finalPage = clampedPage;
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

            gui.setSlot(row1Base + 1, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
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

            gui.setSlot(row2Base + 1, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
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

        LIST_RETURN.set(player.getUUID(), () -> openCategoryPage(player, cat, page));
        List<SlayerRecipes.Recipe> existingRecipes = RecipeConfigManager.getRecipes();
        List<ItemStack> items = getItemsForCategory(cat);

        boolean sparse = RecipeCatalogUi.sparseMinTwoPagesEditor(cat);
        int totalPages = RecipeCatalogUi.totalPages(items.size(), sparse);
        int clampedPage = Math.min(page, totalPages - 1);

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Text.plain(cat.label).withStyle(cat.colour, ChatFormatting.BOLD));

        if (cat == Category.BLOCK_WEAPONS) {
            for (int i = 0; i < 54; i++) {
                gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Text.plain("")).build());
            }
        } else {
            RecipeGuiTheme.fillCategoryItemGrid(gui, cat == Category.SPECIAL_ITEMS);
        }

        boolean[] used = new boolean[54];

        // Header
        gui.setSlot(4, new GuiElementBuilder(cat.icon)
                .setName(Text.plain(cat.label).withStyle(cat.colour, ChatFormatting.BOLD))
                .addLoreLine(Text.plain(""))
                .addLoreLine(Text.plain("Click an item to edit its recipe").withStyle(ChatFormatting.GRAY))
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
                            : Text.plain(customId != null ? customId : "Unknown"))
                    .addLoreLine(Text.plain(""));
            if (finalHasRecipe) {
                elem.addLoreLine(recipeEnabled
                        ? Text.plain("✔ Recipe ENABLED").withStyle(ChatFormatting.GREEN)
                        : Text.plain("✘ Recipe DISABLED").withStyle(ChatFormatting.RED));
                elem.addLoreLine(Text.plain("Left-click: edit  |  Right-click: toggle").withStyle(ChatFormatting.GRAY));
            } else {
                elem.addLoreLine(Text.plain("➕ No recipe — click to create").withStyle(ChatFormatting.YELLOW));
            }
            final int finalPage = clampedPage;
            elem.setCallback((s, t, a, __slotGui) -> {
                if (finalHasRecipe && t == eu.pb4.sgui.api.ClickType.MOUSE_RIGHT) {
                    boolean nowEnabled = RecipeConfigManager.toggleRecipe(finalRecipeName);
                    String status = nowEnabled ? "ENABLED" : "DISABLED";
                    player.sendSystemMessage(Text.plain("Recipe '" + finalRecipeName + "' is now " + status).withStyle(nowEnabled ? ChatFormatting.GREEN : ChatFormatting.RED), false);
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
                .setName(Text.plain("← Back to Categories").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> openMainMenu(player)).build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.plain("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Text.plain("Page " + clampedPage + " / " + totalPages))
                    .setCallback((s, t, a, __slotGui) -> openCategoryPage(player, cat, prev)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.plain("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Text.plain("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((s, t, a, __slotGui) -> openCategoryPage(player, cat, next)).build());
        }

        gui.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(Text.plain("Page " + (clampedPage + 1) + " / " + totalPages))
                .build());

        gui.open();
    }

    /** Search and the old tool/bounty set lists opened the indexed recipe on either click, without a toggle. */
    static boolean usesIndexedRecipeLookup(String category) {
        return category == null || category.equals("CUSTOM_TOOLS") || category.equals("BOUNTY_SETS");
    }

    private static void openEditorList(ServerPlayer player, Component title, Item icon, List<ItemStack> items,
                                       Category cat, int page, Runnable back) {
        int clampedPage = Pager.clamp(page, Pager.pageCount(items.size(), CatalogModel.ITEMS_PER_PAGE));
        Runnable reopen = () -> openEditorList(player, title, icon, items, cat, clampedPage, back);
        LIST_RETURN.set(player.getUUID(), reopen);
        List<SlayerRecipes.Recipe> existingRecipes = RecipeConfigManager.getRecipes();
        boolean indexed = usesIndexedRecipeLookup(cat == null ? null : cat.name());
        Screen screen = CatalogUi.items(player, title, icon, Text.tr("political.catalog.edit"), items.size(), clampedPage,
                next -> openEditorList(player, title, icon, items, cat, next, back),
                () -> CatalogUi.search(player, query -> openEditorSearch(player, items, query, reopen), reopen), back);
        int start = clampedPage * CatalogModel.ITEMS_PER_PAGE;
        int[] slots = CatalogModel.itemSlots();
        for (int shown = 0; shown < slots.length && start + shown < items.size(); shown++) {
            ItemStack item = items.get(start + shown);
            Component displayName = item.get(DataComponents.CUSTOM_NAME);
            String customId = SlayerItems.getCustomItemId(item);

            // Check for existing recipe
            SlayerRecipes.Recipe existingRecipe = indexed ? SlayerRecipes.getRecipeForOutput(item) : null;
            boolean hasRecipe = existingRecipe != null;
            if (!indexed && customId != null) {
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
                            : Text.plain(customId != null ? customId : item.getHoverName().getString()))
                    .addLoreLine(Text.plain(""));
            if (finalHasRecipe) {
                if (indexed) {
                    elem.addLoreLine(Text.tr("political.catalog.edit").withStyle(Palette.WARN.style()));
                } else {
                    elem.addLoreLine(recipeEnabled
                            ? Text.tr("political.catalog.enabled").withStyle(Palette.GOOD.style())
                            : Text.tr("political.catalog.disabled").withStyle(Palette.BAD.style()));
                    elem.addLoreLine(Text.tr("political.catalog.toggle").withStyle(Palette.MUTED.style()));
                }
            } else {
                elem.addLoreLine(Text.tr("political.catalog.create").withStyle(Palette.WARN.style()));
            }
            elem.setCallback((s, t, a, __slotGui) -> {
                if (!indexed && finalHasRecipe && t == eu.pb4.sgui.api.ClickType.MOUSE_RIGHT) {
                    boolean nowEnabled = RecipeConfigManager.toggleRecipe(finalRecipeName);
                    Component status = Text.tr(nowEnabled ? "political.catalog.enabled" : "political.catalog.disabled");
                    player.sendSystemMessage(Text.tr("political.catalog.toggled", Text.plain(finalRecipeName), status).withStyle(nowEnabled ? ChatFormatting.GREEN : ChatFormatting.RED), false);
                    reopen.run();
                } else {
                    openRecipeEditor(player, finalItem, finalRecipe, cat);
                }
            });
            screen.set(slots[shown], elem.build());
        }
        screen.open();
    }

    private static void openEditorSearch(ServerPlayer player, List<ItemStack> source, String query, Runnable back) {
        List<ItemStack> matches = source.stream().filter(item -> !item.isEmpty()
                && CatalogModel.matches(item.getHoverName().getString(), query)).toList();
        openEditorList(player, Text.tr("political.catalog.results"), Items.SPYGLASS, matches, null, 0, back);
    }

    // ──────────────────────────────────────────────────────────────
    // GEM_BLOCK_GEAR: Armor Sets in Rows Display
    // Each row: [Block] [gap] [Sword] [Helmet] [Chest] [Legs] [Boots]
    // ──────────────────────────────────────────────────────────────

    private static void openGemBlockGearPage(ServerPlayer player, int page) {
        LIST_RETURN.set(player.getUUID(), () -> openGemBlockGearPage(player, page));
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Gem & block armor — editor").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));

        // Plain black shell (excluded from shared theme — same as /customitems gem & block hub)
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal("")).build());
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
            new ArmorSetDisplay(Items.EMERALD, GemGear.createEmeraldSword(), GemGear.createEmeraldHelmet(),
                    GemGear.createEmeraldChestplate(), GemGear.createEmeraldLeggings(), GemGear.createEmeraldBoots()),
            new ArmorSetDisplay(Items.LAPIS_LAZULI, GemGear.createLapisSword(), GemGear.createLapisHelmet(),
                    GemGear.createLapisChestplate(), GemGear.createLapisLeggings(), GemGear.createLapisBoots())
        };

        List<SlayerRecipes.Recipe> existingRecipes = RecipeConfigManager.getRecipes();

        final int SETS_PER_PAGE = 4;
        int totalPages = (sets.length + SETS_PER_PAGE - 1) / SETS_PER_PAGE;
        int clampedPage = Math.min(page, totalPages - 1);
        final int finalPage = clampedPage;

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
            gui.setSlot(slots[1], new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
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
                    LIST_RETURN.getOr(player.getUUID(), () -> openMainMenu(player)).run();
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

        gui.setSlot(SLOT_SAVE, new GuiElementBuilder(Items.DYE.lime())
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
                        LIST_RETURN.getOr(player.getUUID(), () -> openMainMenu(player)).run();
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
                    GuiElementBuilder builder = new GuiElementBuilder(Items.STAINED_GLASS_PANE.red())
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
        openRoot(player, page);
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
