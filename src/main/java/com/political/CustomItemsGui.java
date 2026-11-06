package com.political;


import com.political.bounty.SlayerItems;
import com.political.bounty.SlayerManager;
import com.political.items.CustomItemHandler;
import com.political.items.CustomItemManager;
import com.political.items.DragonChestplateRegistry;
import com.political.items.GemGear;
import com.political.items.LegendaryTools;
import com.political.party.PoliticalPartyManager;
import com.political.recipe.RecipeConfigManager;
import com.political.recipe.RecipesGui;
import com.political.recipe.SlayerRecipes;
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

    private static final ReturnTo PARENT = new ReturnTo();
    private static final ReturnTo ROOT_PAGE = new ReturnTo();

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

    public static Component categoryTitle(Category cat) {
        String key = "political.catalog.category." + cat.name().toLowerCase(java.util.Locale.ROOT);
        return Text.tr(key);
    }

    public static net.minecraft.world.item.Item categoryIcon(Category cat) { return cat.icon; }

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
    public static List<ItemStack> getItemsForCategory(Category cat) {
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
                // Block armors - Obsidian
                // Block armors - Quartz
                // Block armors - Glowstone
                // Block armors - Redstone
                // Block armors - Netherrack
                // Block armors - End Stone
                // Block armors - Ice
                // Block armors - Prismarine
                // Block armors - Terracotta
                // Block armors - Mossy
                // Block armors - Soul Sand
                // Block armors - Magma
                // Block armors - Sandstone
                // Block armors - Amethyst
                // Block armors - Coal
                // Block armors - Stripped Logs
                // Block armors - Planks
                // Block armors - Stone/Mud/Logs
                // Block armors - Copper/Netherite/Froglight/Basalt
                // Block armors - Material Items
                // Block armors - Charcoal/Coal
                // Block armors - End Stone/Snow Block
                // Block armors - New 25 materials
                // Block Weapons (matching sets)
                // 36+ NEW BLOCK SWORDS
                // NEW STRIPPED LOG SWORDS
                // NEW PLANK SWORDS
                // NEW STONE/MUD/LOG SWORDS
                // NEW COPPER/NETHERITE/FROGLIGHT SWORDS
                // NEW MATERIAL ITEM SWORDS
                // New 25 material swords
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
                items.add(SlayerItems.createDragonCrafterCoreT1());
                items.add(SlayerItems.createDragonCrafterCoreT2());
                items.add(SlayerItems.createDragonCrafterCoreT3());
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
                // Ocean Tools
                // Lunar Tools
                // Solar Tools
                // Terra Tools
                // Phantom Tools
                // Blood Tools
                // Celestial Tools
                // Shadow Tools
                // Crystal Tools
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
            case BLOCK_TOOLS -> { }
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
            case REFLECT_BLOCK_WEAPONS -> { }
            case REFLECT_BLOCK_ARMOR -> { }
            case ENCHANTED_BLOCK_GEAR -> { }
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
            case MAGIC_STAVES -> { }
        }
        return items;
    }

    // ──────────────────────────────────────────────────────────────
    // Page 0: Main Category Menu - Matches RecipesGui layout
    // ──────────────────────────────────────────────────────────────

    /** Entry point — opens the main category selection menu. */
    public static void open(ServerPlayer player) {
        Runnable previous = ROOT_PAGE.get(player.getUUID());
        if (previous != null) previous.run(); else openRoot(player, 0);
    }

    public static void openFrom(ServerPlayer player, Runnable parent) {
        // A command is an explicit root even if its packet follows a menu close in the same server tick.
        if (parent == null) com.political.core.gui.MenuTrail.forget(player.getUUID());
        PARENT.set(player.getUUID(), parent);
        openRoot(player, 0);
    }

    public static List<Category> catalogCategories() {
        List<Category> categories = new ArrayList<>(List.of(Category.CUSTOM_ARMOR, Category.DRAGON_CHESTPLATES,
                Category.SPECIAL_ARMOR, Category.GOLD_ARMOR, Category.CUSTOM_TOOLS, Category.SPECIAL_ITEMS, Category.PIGLIN));
        for (Category cat : Category.values()) if (!categories.contains(cat)) categories.add(cat);
        return categories;
    }

    private static void openRoot(ServerPlayer player, int page) {
        java.util.List<CatalogUi.Tile> tiles = new ArrayList<>();
        for (Category cat : catalogCategories()) {
            tiles.add(new CatalogUi.Tile(cat.icon, categoryTitle(cat),
                    catalogItemCount(cat), () -> openCatalogCategory(player, cat)));
        }
        tiles.add(2, new CatalogUi.Tile(Items.NETHER_STAR, Text.tr("political.catalog.bounty_sets"), bountyItems().size(),
                () -> openBountySetsPage(player, 0, CustomItemsGui::giveItem, CustomItemsGui::open)));
        showRoot(player, page, List.copyOf(tiles));
    }

    private static void openCatalogCategory(ServerPlayer player, Category cat) {
        if (cat == Category.MOB_DROPS) openMobDropsHub(player);
        else openCategoryPage(player, cat, 0);
    }

    public static int catalogItemCount(Category cat) {
        return cat == Category.CUSTOM_ARMOR ? gemArmorSets().length * 5 : getItemsForCategory(cat).size();
    }

    private static void showRoot(ServerPlayer player, int page, List<CatalogUi.Tile> tiles) {
        ROOT_PAGE.set(player.getUUID(), () -> showRoot(player, page, tiles));
        Screen screen = CatalogUi.categories(player, Text.tr("political.catalog.custom"), Text.tr("political.catalog.grant_hint"),
                tiles, page, next -> showRoot(player, next, tiles), () -> openSearchGui(player), PARENT.get(player.getUUID()));
        screen.set(51, Elements.tile(Items.END_CRYSTAL, Text.tr("political.catalog.new_content"), Palette.ACCENT,
                () -> ExpansionCatalogGui.openMenuFrom(player, () -> open(player))));
        screen.open();
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
        CatalogUi.search(player, query -> showSearchResults(player, query), () -> open(player));
    }

    private static void showSearchResults(ServerPlayer player, String searchTerm) {
        List<ItemStack> items = new ArrayList<>();
        for (Category cat : Category.values()) items.addAll(getItemsForCategory(cat));
        openFiltered(player, com.political.recipe.RecipeItems.unique(items), searchTerm, 0, CustomItemsGui::giveItem, CustomItemsGui::open);
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
        // New 25 material tool sets

        // Legendary tool sets (use pickaxe base item as icon)
        sets.add(new ToolSetDisplay(Items.DRAGON_HEAD, "Dragon", ChatFormatting.DARK_RED,
                LegendaryTools.createDragonPickaxe(), LegendaryTools.createDragonAxe(), LegendaryTools.createDragonShovel(), LegendaryTools.createDragonHoe()));
        sets.add(new ToolSetDisplay(Items.ENDER_PEARL, "Void", ChatFormatting.DARK_PURPLE,
                LegendaryTools.createVoidPickaxe(), LegendaryTools.createVoidAxe(), LegendaryTools.createVoidShovel(), LegendaryTools.createVoidHoe()));
        sets.add(new ToolSetDisplay(Items.OAK_SAPLING, "Nature", ChatFormatting.GREEN,
                LegendaryTools.createNaturePickaxe(), LegendaryTools.createNatureAxe(), LegendaryTools.createNatureShovel(), LegendaryTools.createNatureHoe()));
        sets.add(new ToolSetDisplay(Items.SNOW_BLOCK, "Frost", ChatFormatting.BLUE,
                LegendaryTools.createFrostPickaxe(), LegendaryTools.createFrostAxe(), LegendaryTools.createFrostShovel(), LegendaryTools.createFrostHoe()));

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
            gui.setSlot(base + 1, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
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
            gui.setSlot(row1Base + 1, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
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
            gui.setSlot(row2Base + 1, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
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
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
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
        ArmorSetDisplay[] allSets = gemArmorSets();

        final int SETS_PER_PAGE = 4;
        ArmorSetDisplay[] sets = allSets;
        
        // The block armor sets were removed with the cursor rewrite, so the list can be empty: at least one page, never a negative one.
        int totalPages = Math.max(1, (sets.length + SETS_PER_PAGE - 1) / SETS_PER_PAGE);
        int clampedPage = Math.max(0, Math.min(page, totalPages - 1));

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
            gui.setSlot(slots[1], new GuiElementBuilder(Items.STAINED_GLASS_PANE.black())
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

    private static ArmorSetDisplay[] gemArmorSets() {
        return new ArmorSetDisplay[] {
            new ArmorSetDisplay(Items.EMERALD, GemGear.createEmeraldSword(), GemGear.createEmeraldHelmet(),
                    GemGear.createEmeraldChestplate(), GemGear.createEmeraldLeggings(), GemGear.createEmeraldBoots()),
            new ArmorSetDisplay(Items.LAPIS_LAZULI, GemGear.createLapisSword(), GemGear.createLapisHelmet(),
                    GemGear.createLapisChestplate(), GemGear.createLapisLeggings(), GemGear.createLapisBoots())
        };
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
        gui.setTitle(Text.plain(skyblockSpecial ? "Skyblock-style trinkets" : cat.label)
                .withStyle(skyblockSpecial ? ChatFormatting.GOLD : cat.colour, ChatFormatting.BOLD));

        if (cat == Category.REFLECT_BLOCK_WEAPONS) {
            for (int i = 0; i < 54; i++) {
                gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Text.plain("")).build());
            }
        } else {
            RecipeGuiTheme.fillCategoryItemGrid(gui, skyblockSpecial);
        }

        boolean[] used = new boolean[54];

        // Header
        gui.setSlot(4, new GuiElementBuilder(cat.icon)
                .setName(Text.plain(cat.label).withStyle(cat.colour, ChatFormatting.BOLD))
                .addLoreLine(Text.plain(""))
                .addLoreLine(Text.plain(skyblockSpecial
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
                            : Text.plain(item.getItem().toString()))
                    .addLoreLine(Text.plain(""))
                    .addLoreLine(Text.plain("✨ Click to get item").withStyle(ChatFormatting.YELLOW))
                    .setCallback((s, t, a, __slotGui) -> onItemClick.onClick(player, finalItem));
            gui.setSlot(slot, elem.build());
            used[slot] = true;
            shown++;
        }

        boolean lastPageComingSoon = sparse && clampedPage == totalPages - 1;
        RecipeCatalogUi.fillEmptyGridSlots(gui, used, lastPageComingSoon);

        // ← Back to main menu
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Text.plain("← Back to Categories").withStyle(ChatFormatting.YELLOW))
                .setCallback((s, t, a, __slotGui) -> onBack.open(player)).build());

        // Pagination
        if (clampedPage > 0) {
            final int prev = clampedPage - 1;
            gui.setSlot(46, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.plain("← Previous Page").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Text.plain("Page " + clampedPage + " / " + totalPages))
                    .setCallback((s, t, a, __slotGui) -> openCategoryPage(player, cat, prev, onItemClick, onBack)).build());
        }
        if (clampedPage < totalPages - 1) {
            final int next = clampedPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.plain("Next Page →").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Text.plain("Page " + (clampedPage + 2) + " / " + totalPages))
                    .setCallback((s, t, a, __slotGui) -> openCategoryPage(player, cat, next, onItemClick, onBack)).build());
        }

        gui.setSlot(50, new GuiElementBuilder(Items.PAPER)
                .setName(Text.plain("Page " + (clampedPage + 1) + " / " + totalPages))
                .build());

        gui.open();
    }

    public static void openItemList(ServerPlayer player, Component title, net.minecraft.world.item.Item icon,
                                   List<ItemStack> items, int page, BountyItemClickHandler click, BackHandler back) {
        openItemList(player, title, icon, items, page, click, back, null);
    }

    /** The detail callback receives the current page's Back; grant and existing browser actions stay unchanged. */
    public static void openItemList(ServerPlayer player, Component title, net.minecraft.world.item.Item icon,
                                   List<ItemStack> items, int page, BountyItemClickHandler click, BackHandler back,
                                   java.util.function.BiConsumer<ItemStack, Runnable> detail) {
        int current = Pager.clamp(page, Pager.pageCount(items.size(), CatalogModel.ITEMS_PER_PAGE));
        Runnable reopen = () -> openItemList(player, title, icon, items, current, click, back, detail);
        Screen screen = CatalogUi.items(player, title, icon,
                Text.tr(click == null ? "political.catalog.view_hint" : "political.catalog.grant"), items.size(), current,
                next -> openItemList(player, title, icon, items, next, click, back, detail),
                () -> CatalogUi.search(player, query -> openFiltered(player, items, query, 0, click, p -> reopen.run(), detail), reopen),
                () -> back.open(player));
        int start = current * CatalogModel.ITEMS_PER_PAGE;
        int[] slots = CatalogModel.itemSlots();
        for (int i = 0; i < slots.length && start + i < items.size(); i++) {
            ItemStack item = items.get(start + i);
            screen.set(slots[i], CatalogUi.item(item, Text.tr(click == null ? "political.catalog.recipe" : "political.catalog.grant"), () -> {
                if (detail != null) detail.accept(item, reopen);
                else if (click == null) showItemInfo(player, item, p -> reopen.run());
                else click.onClick(player, item);
            }));
        }
        screen.open();
    }

    private static void openFiltered(ServerPlayer player, List<ItemStack> source, String query, int page,
                                     BountyItemClickHandler click, BackHandler back) {
        openFiltered(player, source, query, page, click, back, null);
    }

    private static void openFiltered(ServerPlayer player, List<ItemStack> source, String query, int page,
                                     BountyItemClickHandler click, BackHandler back,
                                     java.util.function.BiConsumer<ItemStack, Runnable> detail) {
        List<ItemStack> matches = source.stream().filter(item -> !item.isEmpty()
                && CatalogModel.matches(item.getHoverName().getString(), query)).toList();
        openItemList(player, Text.tr("political.catalog.results"), Items.SPYGLASS, matches, page, click, back, detail);
    }

    public static List<ItemStack> bountyItems() {
        List<ItemStack> items = new ArrayList<>();
        for (Category cat : List.of(Category.SWORDS_T1, Category.ARMOR_T1, Category.SWORDS_T2, Category.ARMOR_T2))
            items.addAll(getItemsForCategory(cat));
        return items;
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
                gui.setSlot(31, new GuiElementBuilder(Items.CONCRETE.lime())
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
                gui.setSlot(31, new GuiElementBuilder(Items.CONCRETE.red())
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

        gui.setSlot(45, Elements.back(() -> onBack.open(player)));
        gui.setSlot(53, Elements.close(player));

        gui.open();
    }

    private static boolean checkCanCraft(ServerPlayer player, SlayerRecipes.Recipe recipe) {
        return com.political.recipe.RecipeInventory.canCraft(player, recipe);
    }

    private static boolean instantCraft(ServerPlayer player, SlayerRecipes.Recipe recipe, ItemStack resultItem) {
        if (!com.political.recipe.RecipeInventory.craft(player, recipe)) return false;
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
