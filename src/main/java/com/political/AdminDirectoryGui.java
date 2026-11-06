package com.political;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.political.government.Treasury;



import com.political.bounty.BountyGui;
import com.political.economy.AuctionHouseGui;
import com.political.economy.BankGui;
import com.political.economy.BazaarGui;
import com.political.economy.ShopItemGui;
import com.political.economy.UndergroundAuctionGui;
import com.political.government.ChairGui;
import com.political.government.GovernmentGui;
import com.political.government.GovernmentTreasuryGui;
import com.political.government.JudgeGui;
import com.political.government.ViceChairGui;
import com.political.government.VoteGui;
import com.political.party.PartyJoinGui;
import com.political.party.PartyLeaderGui;
import com.political.recipe.RecipeEditorGui;
import com.political.recipe.RecipesGui;
import com.political.settlement.Settlement;
import com.political.settlement.SettlementEventGui;
import com.political.settlement.SettlementProgressGui;
import com.political.settlement.WarGui;
import com.political.technique.Technique;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Every menu in the mod, in one place.
 *
 * <p>The console groups what an operator reaches for daily, which is the right shape for daily work
 * and the wrong shape for the moment somebody needs the one screen they have never opened. This is
 * the other half: a flat, complete directory of every screen that exists, grouped, so nothing in the
 * mod is more than three clicks from the console.</p>
 *
 * <p>The list is generated from the actual set of screens rather than typed out, because a
 * hand written index of a hundred menus is out of date the day after it is written.</p>
 */
public final class AdminDirectoryGui {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminDirectoryGui.class);


    private AdminDirectoryGui() {}

    private static final int SIZE = 54;

    /** An entry opens either with no parent (its own Back decides) or from this directory, which it should lead back to. */
    private record Entry(String label, Opener opener, BackOpener fromOpener) {}

    private interface Opener {
        void open(ServerPlayer player);
    }

    private interface BackOpener {
        void open(ServerPlayer player, Runnable back);
    }

    private static final Map<String, List<Entry>> GROUPS = new LinkedHashMap<>();

    private static void add(String group, String label, Opener opener) {
        GROUPS.computeIfAbsent(group, k -> new ArrayList<>()).add(new Entry(label, opener, null));
    }

    /** An entry whose screen can be told where Back leads: the directory page that lists it. */
    private static void add(String group, String label, BackOpener opener) {
        GROUPS.computeIfAbsent(group, k -> new ArrayList<>()).add(new Entry(label, null, opener));
    }

    static {
        // Operator
        add("Operator", "Admin", p -> AdminGui.open(p));
        add("Operator", "Boss Config", p -> BossConfigGui.openMainMenu(p));
        add("Operator", "Bounty Config", p -> BountyConfigGui.open(p));
        add("Operator", "Civil Anti Cheat", (p, back) -> CivilAntiCheatGui.openFrom(p, back));
        add("Operator", "Crypto Admin", (p, back) -> CryptoAdminGui.openMainMenuFrom(p, back));
        add("Operator", "Item Model", (p, back) -> ItemModelGui.openFrom(p, back));
        add("Operator", "Player Buff Admin", (p, back) -> PlayerBuffAdminGui.openPlayerSelectFrom(p, back));
        add("Operator", "Recipe Editor", p -> RecipeEditorGui.openMainMenu(p));
        add("Operator", "Report", p -> ReportGui.openMainMenu(p));
        add("Operator", "Set Price", p -> SetPriceGui.openMainMenu(p));
        add("Operator", "Stock Admin", (p, back) -> StockAdminGui.openMainMenuFrom(p, back));
        add("Operator", "Texture Helper", p -> TextureHelperGui.openMainMenu(p));

        // Settlement
        add("Settlement", "Civilization Hub", p -> CivilizationHubGui.open(p));
        add("Settlement", "Guild", p -> GuildGui.open(p));
        add("Settlement", "Incursion", (p, back) -> IncursionGui.openFrom(p, back));
        add("Settlement", "Settlement Event", p -> SettlementEventGui.open(p));
        add("Settlement", "Settlement Progress", (p, back) -> SettlementProgressGui.openListFrom(p, back));
        add("Settlement", "Siege", (p, back) -> SiegeGui.openFrom(p, back));
        add("Settlement", "Village Atlas", (p, back) -> VillageAtlasGui.openFrom(p, back));

        // Politics
        add("Politics", "Chair", p -> ChairGui.open(p));
        add("Politics", "Dictator", p -> DictatorGui.open(p));
        add("Politics", "Government", p -> GovernmentGui.open(p));
        add("Politics", "Government Treasury", (p, back) -> GovernmentTreasuryGui.openFrom(p, back));
        add("Politics", "High Council", (p, back) -> HighCouncilGui.openFrom(p, back));
        add("Politics", "Impeachment", (p, back) -> ImpeachmentGui.openFrom(p, back));
        add("Politics", "Judge", p -> JudgeGui.open(p));
        add("Politics", "Party Join", p -> PartyJoinGui.open(p));
        add("Politics", "Party Leader", p -> PartyLeaderGui.open(p));
        add("Politics", "Party Leader Election", p -> PartyLeaderElectionGui.open(p));
        add("Politics", "Ruler", p -> RulerGui.open(p));
        add("Politics", "Vice Chair", p -> ViceChairGui.open(p));
        add("Politics", "Vote", p -> VoteGui.open(p));
        add("Politics", "War", (p, back) -> WarGui.openFrom(p, back));

        // Economy
        add("Economy", "Auction House", (p, back) -> AuctionHouseGui.openFrom(p, back));
        add("Economy", "Bank", p -> BankGui.open(p));
        add("Economy", "Bazaar", (p, back) -> BazaarGui.openFrom(p, back));
        add("Economy", "Commission", p -> CommissionGui.open(p));
        add("Economy", "Crate", (p, back) -> CrateGui.openFrom(p, back));
        add("Economy", "Credits Trade", p -> CreditsTradeGui.open(p));
        add("Economy", "Crypto Market", p -> CryptoMarketGui.openMainMenu(p));
        add("Economy", "Market", p -> MarketGui.open(p));
        add("Economy", "Shop", (p, back) -> ShopGui.openMainMenuFrom(p, back));
        add("Economy", "Shop Item", p -> ShopItemGui.open(p));
        add("Economy", "Special Shop", p -> SpecialShopGui.openMainMenu(p));
        add("Economy", "Stock Market", p -> StockMarketGui.openMainMenu(p));
        add("Economy", "Trade", p -> TradeGui.open(p));
        add("Economy", "Underground Auction", (p, back) -> UndergroundAuctionGui.openFrom(p, back));

        // Combat
        add("Combat", "Bestiary", (p, back) -> BestiaryGui.openFrom(p, back));
        add("Combat", "Bounty", p -> BountyGui.openMainMenu(p));
        add("Combat", "Expedition", p -> ExpeditionGui.open(p));
        add("Combat", "Hunt", (p, back) -> HuntGui.openFrom(p, back));
        add("Combat", "Hunts", p -> HuntsGui.openMainMenu(p));
        add("Combat", "Nemesis", (p, back) -> NemesisGui.openFrom(p, back));
        add("Combat", "World Boss", p -> WorldBossGui.openMainMenu(p));

        add("Combat", "Technique", p -> com.political.power.game.PowerAdminScreens.open(p, 0));

        // Progress
        add("Progress", "Ascension Grind", p -> AscensionGrindGui.openMain(p));
        add("Progress", "Ascension Hearts", p -> AscensionHeartsGui.openTreePicker(p));
        add("Progress", "Checkpoint", p -> CheckpointGui.openMain(p));
        add("Progress", "Class", p -> ClassGui.openMainMenu(p));
        add("Progress", "Collection", (p, back) -> CollectionGui.openFrom(p, back));
        add("Progress", "Mastery", p -> MasteryGui.openMainMenu(p));
        add("Progress", "Player Buff", p -> PlayerBuffGui.openMainMenu(p));
        add("Progress", "Player Stats", (p, back) -> PlayerStatsGui.openFrom(p, back));
        add("Progress", "Progression Tree", p -> ProgressionTreeGui.openMainMenu(p));
        add("Progress", "Skills", p -> SkillsGui.openMainMenu(p));
        add("Progress", "Title", (p, back) -> TitleGui.openFrom(p, back));
        add("Progress", "Upgrades", p -> UpgradesGui.openMainGui(p));

        // Content
        add("Content", "Companion", p -> CompanionGui.open(p));
        add("Content", "Contract", p -> ContractGui.open(p));
        add("Content", "Custom Items", (p, back) -> CustomItemsGui.openFrom(p, back));
        add("Content", "Expansion Catalog", (p, back) -> ExpansionCatalogGui.openMenuFrom(p, back));
        add("Content", "Fletching Table", p -> FletchingTableGui.openGui(p));
        add("Content", "Minion", (p, back) -> MinionGui.openFrom(p, back));
        add("Content", "Recipes", (p, back) -> RecipesGui.openFrom(p, back));
        add("Content", "Reforge", p -> ReforgeGui.open(p));
        add("Content", "Relic", p -> RelicGui.open(p));
        add("Content", "T2 Crafting", p -> T2CraftingGui.open(p));
        add("Content", "Uncraft", p -> UncraftGui.openUncraftMenu(p));
        add("Content", "Variety", p -> VarietyGui.open(p));

        // World
        add("World", "Atlas Map", p -> AtlasMapGui.open(p));
        add("World", "Fishing", p -> FishingGui.openMainMenu(p));
        add("World", "Living World", p -> LivingWorldGui.open(p));
        add("World", "Season", (p, back) -> SeasonGui.openFrom(p, back));

        // Other
        add("Other", "Changelog", p -> ChangelogGui.open(p));
        add("Other", "Civil Craft", p -> CivilCraftGui.open(p));
        add("Other", "Menu", p -> MenuGui.openMainMenu(p));
        add("Other", "Modern Showcase", p -> ModernShowcaseGui.open(p));
        add("Other", "Tax", (p, back) -> TaxGui.openFrom(p, back));

    }

    public static int totalMenus() {
        int n = 0;
        for (List<Entry> l : GROUPS.values()) n += l.size();
        return n;
    }

    // ── The index ─────────────────────────────────────────────────────────

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Every Menu")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        GuiKit.header(gui, Items.BOOKSHELF, "Every Menu", ChatFormatting.AQUA,
                totalMenus() + " screens across " + GROUPS.size() + " groups",
                "Nothing in the mod is unreachable from here");

        int[] slots = {19, 20, 21, 22, 23, 24, 25, 29, 30, 31, 32, 33};
        int i = 0;
        for (Map.Entry<String, List<Entry>> e : GROUPS.entrySet()) {
            if (i >= slots.length) break;
            String group = e.getKey();
            int count = e.getValue().size();
            gui.setSlot(slots[i++], GuiKit.tile(iconFor(group), group, colourFor(group),
                    () -> openGroup(player, group, 0),
                    count + (count == 1 ? " screen" : " screens")));
        }

        gui.setSlot(40, GuiKit.tile(Items.COMMAND_BLOCK, "Every command", ChatFormatting.RED,
                () -> AdminCommandsGui.open(player, 0),
                "Read live from the server",
                "Click any one to run it"));

        GuiKit.footer(gui, SIZE, player, () -> AdminPanel.open(player));
        gui.open();
    }

    private static void openGroup(ServerPlayer player, String group, int page) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal(group).withStyle(colourFor(group), ChatFormatting.BOLD));
        GuiKit.frame(gui, SIZE);

        List<Entry> list = GROUPS.getOrDefault(group, List.of());
        GuiKit.header(gui, iconFor(group), group, colourFor(group),
                list.size() + " screens");

        List<GuiElement> entries = new ArrayList<>();
        for (Entry en : list) {
            entries.add(GuiKit.action(iconFor(group), en.label(), ChatFormatting.WHITE,
                    () -> {
                        try {
                            if (en.fromOpener() != null) {
                                en.fromOpener().open(player, () -> openGroup(player, group, page));
                            } else {
                                en.opener().open(player);
                            }
                        } catch (Exception ex) {
                            LOGGER.warn("AdminDirectoryGui.openGroup: ignored an error", ex);
                            // Some screens need context an operator may not have, a settlement or
                            // a party. Say so plainly rather than closing with no explanation.
                            player.sendSystemMessage(Component.literal(
                                    "§cThat screen could not open: "
                                            + (ex.getMessage() == null
                                            ? ex.getClass().getSimpleName() : ex.getMessage())));
                        }
                    },
                    "Click to open"));
        }

        GuiKit.paint(gui, entries, page, GuiKit.LIST_SLOTS.length);
        GuiKit.pager(gui, SIZE, page, entries.size(), GuiKit.LIST_SLOTS.length,
                p -> openGroup(player, group, p));
        GuiKit.footer(gui, SIZE, player, () -> open(player));
        gui.open();
    }

    private static net.minecraft.world.item.Item iconFor(String group) {
        return switch (group.toLowerCase(Locale.ROOT)) {
            case "operator" -> Items.COMMAND_BLOCK;
            case "settlement" -> Items.CAMPFIRE;
            case "politics" -> Items.BELL;
            case "economy" -> Items.GOLD_INGOT;
            case "combat" -> Items.NETHERITE_SWORD;
            case "magic" -> Items.ENCHANTING_TABLE;
            case "progress" -> Items.EXPERIENCE_BOTTLE;
            case "content" -> Items.CRAFTING_TABLE;
            case "world" -> Items.COMPASS;
            default -> Items.PAPER;
        };
    }

    private static ChatFormatting colourFor(String group) {
        return switch (group.toLowerCase(Locale.ROOT)) {
            case "operator" -> ChatFormatting.RED;
            case "settlement" -> ChatFormatting.GREEN;
            case "politics" -> ChatFormatting.YELLOW;
            case "economy" -> ChatFormatting.GOLD;
            case "combat" -> ChatFormatting.DARK_RED;
            case "magic" -> ChatFormatting.LIGHT_PURPLE;
            case "progress" -> ChatFormatting.AQUA;
            case "content" -> ChatFormatting.DARK_PURPLE;
            case "world" -> ChatFormatting.DARK_AQUA;
            default -> ChatFormatting.GRAY;
        };
    }
}
