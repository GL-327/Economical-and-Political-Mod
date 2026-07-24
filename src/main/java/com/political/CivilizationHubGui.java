package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/**
 * The Civilization Hub — one screen from which every V4/V5 system is reachable by clicking.
 * Commands remain available, but nothing in this mod should <i>require</i> typing a command.
 */
public final class CivilizationHubGui {

    private CivilizationHubGui() {}

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Civilization").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        WarGui.fill(gui, 54);

        String uuid = player.getUUID().toString();
        Settlement home = SettlementManager.getRegisteredSettlement(uuid);
        Settlement polity = WarManager.polityOf(player);

        // Header
        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal(player.getName().getString())
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Home: §f"
                        + (home == null ? "§8unregistered" : home.name)))
                .addLoreLine(Component.literal("§7Realm: §f"
                        + (polity == null ? "§8none" : polity.name)))
                .glow().build());

        // ── Row 1: settlement, atlas, economy ──
        gui.setSlot(10, new GuiElementBuilder(Items.BELL)
                .setName(Component.literal("My Settlement").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Tier progress, beds, appeal, leaders."))
                .addLoreLine(Component.literal(home == null
                        ? "§8Stand in a settlement or register first."
                        : "§eClick to open"))
                .setCallback((a, b, c, d) -> {
                    Settlement s = currentOrHome(player);
                    if (s != null) SettlementGui.open(player, s);
                    else player.sendSystemMessage(Component.literal("§7No settlement here."));
                })
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.FILLED_MAP)
                .setName(Component.literal("Atlas").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7World map of every known settlement,"))
                .addLoreLine(Component.literal("§7with a live leadership feed."))
                .setCallback((a, b, c, d) -> AtlasMapGui.open(player))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.WHEAT)
                .setName(Component.literal("Settlement Economy").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Food, goods, defense, happiness,"))
                .addLoreLine(Component.literal("§7workforce and income."))
                .setCallback((a, b, c, d) -> {
                    Settlement s = currentOrHome(player);
                    if (s != null) SettlementEconomyGui.open(player, s);
                    else player.sendSystemMessage(Component.literal("§7No settlement here."));
                })
                .build());

        // ── Row 1b: politics ──
        gui.setSlot(14, new GuiElementBuilder(Items.GOLDEN_HELMET)
                .setName(Component.literal("Government").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Chair, Vice, elections and perks."))
                .setCallback((a, b, c, d) -> GovernmentGui.open(player))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.BEACON)
                .setName(Component.literal("High Council").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Prime Seat, ministries and edicts."))
                .addLoreLine(Component.literal("§8Requires a UTOPIA settlement."))
                .setCallback((a, b, c, d) -> HighCouncilGui.open(player))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("War Room").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Declare war, vote, sue for peace."))
                .addLoreLine(Component.literal("§7See the council and the shadows."))
                .setCallback((a, b, c, d) -> WarGui.open(player))
                .build());

        // ── Row 2: magic ──
        gui.setSlot(19, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Component.literal("Magic Technique").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Your awakened technique, abilities"))
                .addLoreLine(Component.literal("§7and mastery rank."))
                .setCallback((a, b, c, d) -> TechniqueGui.open(player))
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.AMETHYST_CLUSTER)
                .setName(Component.literal("Schools of Magic").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§712 schools, each with a node tree."))
                .setCallback((a, b, c, d) -> SchoolTreeGui.openSchoolList(player))
                .build());

        gui.setSlot(21, new GuiElementBuilder(Items.BLAZE_ROD)
                .setName(Component.literal("Spells & Staff").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Spellbook, staff workshop, bestiary."))
                .setCallback((a, b, c, d) -> MagicGui.openMainMenu(player))
                .build());

        // ── Row 2b: content ──
        gui.setSlot(23, new GuiElementBuilder(Items.YELLOW_CONCRETE)
                .setName(Component.literal("The Backrooms").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Status, entry and escape."))
                .setCallback((a, b, c, d) -> BackroomsGui.open(player))
                .build());

        gui.setSlot(24, new GuiElementBuilder(Items.CRAFTING_TABLE)
                .setName(Component.literal("Recipes").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Browse every custom recipe."))
                .setCallback((a, b, c, d) -> RecipesGui.openMainMenu(player))
                .build());

        gui.setSlot(25, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Player Hub").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Stats, mastery, economy, party."))
                .setCallback((a, b, c, d) -> MenuGui.openMainMenu(player))
                .build());

        // ── Row 3: V5 systems ──
        gui.setSlot(28, new GuiElementBuilder(Items.ENDER_EYE)
                .setName(Component.literal("Expeditions").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Instanced procedural dungeons."))
                .addLoreLine(Component.literal("§7Five tiers, bosses, vaults and spoils."))
                .setCallback((a, b, c, d) -> ExpeditionGui.open(player))
                .build());

        gui.setSlot(29, new GuiElementBuilder(Items.SHIELD)
                .setName(Component.literal("Guilds & Orders").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Cross-settlement brotherhoods:"))
                .addLoreLine(Component.literal("§7ranks, treasury, perks and halls."))
                .setCallback((a, b, c, d) -> GuildGui.open(player))
                .build());

        gui.setSlot(30, new GuiElementBuilder(Items.CHEST_MINECART)
                .setName(Component.literal("Trade & Caravans").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Trade routes between settlements."))
                .addLoreLine(Component.literal("§8Caravans can be raided in wartime."))
                .setCallback((a, b, c, d) -> TradeGui.open(player))
                .build());

        gui.setSlot(32, new GuiElementBuilder(Items.WHEAT_SEEDS)
                .setName(Component.literal("Seasons").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7The turning year and what it"))
                .addLoreLine(Component.literal("§7does to your harvest."))
                .setCallback((a, b, c, d) -> SeasonGui.open(player))
                .build());

        gui.setSlot(33, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("Bestiary").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Everything you have hunted,"))
                .addLoreLine(Component.literal("§7and what that knowledge is worth."))
                .setCallback((a, b, c, d) -> BestiaryGui.open(player))
                .build());

        gui.setSlot(34, new GuiElementBuilder(Items.BOW)
                .setName(Component.literal("The Great Hunt").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7A rare beast walks the world."))
                .addLoreLine(Component.literal("§7First to fell it takes the trophy."))
                .setCallback((a, b, c, d) -> HuntGui.open(player))
                .build());

        // ── Row 4: V6 (curses and the sorcerer path) ──
        gui.setSlot(37, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal("Cursed Spirits").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Graded curses stalk the world. Hunt them,"))
                .addLoreLine(Component.literal("§7read the codex, and bank cursed energy."))
                .setCallback((a, b, c, d) -> CursedSpiritGui.open(player))
                .build());

        gui.setSlot(38, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("Sorcerer Path").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Rank up, swear binding vows, wield reverse"))
                .addLoreLine(Component.literal("§7cursed technique and expand your domain."))
                .setCallback((a, b, c, d) -> SorcererGui.open(player))
                .build());

        gui.setSlot(39, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Special Grade").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Summon and fell the named horrors."))
                .setCallback((a, b, c, d) -> SpecialGradeGui.open(player))
                .build());

        gui.setSlot(41, new GuiElementBuilder(Items.CLOCK)
                .setName(Component.literal("The Living World").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Seasons, festivals, events and world turns."))
                .setCallback((a, b, c, d) -> VarietyGui.open(player))
                .build());

        // ── V7 ──
        gui.setSlot(42, new GuiElementBuilder(Items.WITHER_SKELETON_SKULL)
                .setName(Component.literal("Sorcerer Clans").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Found a bloodline with a signature technique,"))
                .addLoreLine(Component.literal("§7build its prestige and feud with rivals."))
                .setCallback((a, b, c, d) -> ClanGui.open(player))
                .build());

        gui.setSlot(43, new GuiElementBuilder(Items.NETHERITE_SWORD)
                .setName(Component.literal("Cursed Arsenal").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Graded cursed weapons and armor."))
                .setCallback((a, b, c, d) -> CursedArsenalGui.open(player))
                .build());

        gui.setSlot(40, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("What Is New").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7The featured board of the latest systems,"))
                .addLoreLine(Component.literal("§7each one click away."))
                .setCallback((a, b, c, d) -> ModernShowcaseGui.open(player))
                .build());

        gui.setSlot(45, new GuiElementBuilder(Items.ANVIL)
                .setName(Component.literal("Reforge Anvil").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Reroll the reforge on any item you hold."))
                .addLoreLine(Component.literal("§8Over forty reforges across every gear class."))
                .setCallback((a, b, c, d) -> ReforgeGui.open(player))
                .build());

        gui.setSlot(44, new GuiElementBuilder(Items.END_ROD)
                .setName(Component.literal("Effects Gallery").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Lances, storms, domains and rifts."))
                .addLoreLine(Component.literal("§8Click any one to watch it play."))
                .setCallback((a, b, c, d) -> VfxShowcase.open(player))
                .build());

        gui.setSlot(48, new GuiElementBuilder(Items.WRITTEN_BOOK)
                .setName(Component.literal("Changelog").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Everything added, version by version."))
                .setCallback((a, b, c, d) -> ChangelogGui.open(player))
                .build());

        // ── Footer: live world state ──
        int settlements = SettlementManager.getAllSettlements().size();
        int activeWars = WarManager.activeWars().size();
        gui.setSlot(49, new GuiElementBuilder(Items.CLOCK)
                .setName(Component.literal("State of the World").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("§7Settlements: §f" + settlements))
                .addLoreLine(Component.literal("§7Active wars: §c" + activeWars))
                .addLoreLine(Component.literal("§7National Chair: §f"
                        + (DataManager.getChair() == null ? "§8vacant"
                        : DataManager.getPlayerName(DataManager.getChair()))))
                .build());

        gui.open();
    }

    /** The settlement the player is standing in, else the one they are registered to. */
    static Settlement currentOrHome(ServerPlayer player) {
        Settlement here = SettlementManager.getContaining(
                ((net.minecraft.server.level.ServerLevel) player.level())
                        .dimension().identifier().toString(),
                player.blockPosition());
        if (here != null) return here;
        return SettlementManager.getRegisteredSettlement(player.getUUID().toString());
    }
}
