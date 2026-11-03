package com.political;


import com.political.bounty.BountyGui;
import com.political.bounty.SlayerData;
import com.political.bounty.SlayerManager;
import com.political.economy.BankGui;
import com.political.economy.CoinManager;
import com.political.party.PartyJoinGui;
import com.political.party.PoliticalPartyManager;
import com.political.recipe.RecipesGui;
import com.political.technique.Technique;
import com.political.technique.TechniqueGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Items;

/**
 * Hub menu: category buttons open dedicated sub-GUIs with more detail.
 */
public class MenuGui {

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Player Hub").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal("")).build());
        }

        String uuid = player.getUUID().toString();

        gui.setSlot(4, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal(player.getName().getString()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Choose a category").withStyle(ChatFormatting.GRAY))
                .glow().build());

        gui.setSlot(8, new GuiElementBuilder(Items.BELL)
                .setName(Component.literal("Civilization").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Settlements, government, war, magic").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Everything in one place").withStyle(ChatFormatting.DARK_GRAY))
                .glow()
                .setCallback((a, b, c, d) -> CivilizationHubGui.open(player))
                .build());

        gui.setSlot(10, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Component.literal("Combat & Bounty").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Slayer types, totals, shortcuts").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> openCombatSection(player))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.ENCHANTING_TABLE)
                .setName(Component.literal("Mastery & Class").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("All six families + class").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> openMasterySection(player))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("Skills & Magic").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Magic, fishing, HOTG, progression tree").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> openSkillsSection(player))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Economy").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Coins, credits, bank, wealth").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> openEconomySection(player))
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("Party & Support").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Party info, support, commissions").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> openPartySection(player))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Enhancements").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Heart tree, buffs summary").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> MasteryGui.openEnhancementsGui(player))
                .build());

        gui.setSlot(24, new GuiElementBuilder(Items.CRAFTING_TABLE)
                .setName(Component.literal("Quick shortcuts").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("/recipe  /bounty  /shop  /bank").withStyle(ChatFormatting.DARK_GRAY))
                .addLoreLine(Component.literal("Click: open /recipe browser").withStyle(ChatFormatting.YELLOW))
                .setCallback((a, b, c, d) -> RecipesGui.openMainMenu(player))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.COMPASS)
                .setName(Component.literal("Skills GUI (old)").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Opens /skills hub").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((a, b, c, d) -> SkillsGui.openMainMenu(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("Close").withStyle(ChatFormatting.RED))
                .setCallback((a, b, c, d) -> player.closeContainer())
                .build());

        gui.open();
    }

    public static void openCombatSection(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Combat & Bounty").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal("")).build());
        }
        String uuid = player.getUUID().toString();

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal("Total Slayer: " + SlayerData.getTotalSlayerLevel(uuid)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Open bounty board to hunt").withStyle(ChatFormatting.GRAY))
                .glow().build());

        SlayerManager.SlayerType[] types = SlayerManager.SlayerType.values();
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        for (int i = 0; i < types.length && i < slots.length; i++) {
            SlayerManager.SlayerType t = types[i];
            long xp = SlayerData.getSlayerXp(uuid, t);
            int lv = SlayerManager.getLevelForXp(xp);
            gui.setSlot(slots[i], new GuiElementBuilder(t.icon)
                    .setName(Component.literal(t.displayName + " Lv." + lv).withStyle(t.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("XP: " + String.format("%,d", xp)).withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Boss: " + t.bossName).withStyle(ChatFormatting.DARK_GRAY))
                    .build());
        }

        gui.setSlot(38, new GuiElementBuilder(Items.DIAMOND_SWORD)
                .setName(Component.literal("Open Bounty Board").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .setCallback((a, b, c, d) -> BountyGui.openMainMenu(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> openMainMenu(player))
                .build());

        gui.open();
    }

    public static void openMasterySection(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Mastery & Class").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal("")).build());
        }
        String uuid = player.getUUID().toString();

        gui.setSlot(4, new GuiElementBuilder(Items.ENCHANTING_TABLE)
                .setName(Component.literal("Total mastery levels: " + MasteryManager.getTotalLevel(uuid) + "/60").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Grandmaster tracks: " + MasteryManager.getGrandmasterCount(uuid)).withStyle(ChatFormatting.AQUA))
                .build());

        MasteryManager.MasteryFamily[] families = MasteryManager.MasteryFamily.values();
        int[] slots = {10, 11, 12, 13, 14, 15, 19, 20, 21, 22, 23, 24};
        for (int i = 0; i < families.length && i < slots.length; i++) {
            MasteryManager.MasteryFamily f = families[i];
            int lv = MasteryManager.getMasteryLevel(uuid, f);
            var data = MasteryManager.getMasteryData(uuid, f);
            gui.setSlot(slots[i], new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
                    .setName(Component.literal(f.icon + " " + f.displayName + " Lv." + lv).withStyle(f.color, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("XP: " + data.xp).withStyle(ChatFormatting.GRAY))
                    .build());
        }

        ClassManager.PlayerClass pc = ClassManager.getPlayerClass(uuid);
        if (pc != null) {
            gui.setSlot(30, new GuiElementBuilder(Items.IRON_SWORD)
                    .setName(Component.literal("Class: " + pc.name + " Lv." + ClassManager.getClassLevel(uuid, pc)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .setCallback((a, b, c, d) -> ClassGui.openMainMenu(player))
                    .build());
        }

        gui.setSlot(38, new GuiElementBuilder(Items.HEART_OF_THE_SEA)
                .setName(Component.literal("Mastery GUI + Heart Tree").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((a, b, c, d) -> MasteryGui.openMainMenu(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> openMainMenu(player))
                .build());

        gui.open();
    }

    public static void openSkillsSection(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Skills & Magic").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal("")).build());
        }
        String uuid = player.getUUID().toString();

        gui.setSlot(10, new GuiElementBuilder(Items.ENCHANTED_BOOK)
                .setName(Component.literal("Techniques").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Learn from a Technique Manual.").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Equip one and rank it by use.").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> TechniqueGui.open(player))
                .build());

        FishingManager.PlayerFishingData fish = FishingManager.getData(uuid);
        gui.setSlot(12, new GuiElementBuilder(Items.FISHING_ROD)
                .setName(Component.literal("Fishing Lv." + fish.level).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Caught: " + fish.fishCaught + " | XP: " + fish.xp).withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> FishingGui.openMainMenu(player))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.AMETHYST_SHARD)
                .setName(Component.literal("HOTG — Magic Find " + AscensionGrindManager.getMagicFind(uuid)).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                .setCallback((a, b, c, d) -> AscensionGrindGui.openMain(player))
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.OAK_SAPLING)
                .setName(Component.literal("Progression tree").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal("Skill pts: " + ProgressionTreeManager.getSkillPoints(uuid)).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Tiers: " + ProgressionTreeManager.getTotalTiers(uuid) + "/60").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> ProgressionTreeGui.openMainMenu(player))
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.HEART_OF_THE_SEA)
                .setName(Component.literal("Heart tree (coins)").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .setCallback((a, b, c, d) -> MasteryGui.openHeartTree(player))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("Technique ranks").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .setCallback((a, b, c, d) -> TechniqueGui.open(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> openMainMenu(player))
                .build());

        gui.open();
    }

    public static void openEconomySection(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Economy").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal("")).build());
        }
        String uuid = player.getUUID().toString();
        long coins = CoinManager.getCoinsLong(uuid);
        int credits = CreditItem.countCredits(player);
        long bank = DataManager.getBankMain(uuid);
        long savings = DataManager.getBankSavings(uuid);
        long wealth = coins + bank + savings + credits * 10_000L;

        gui.setSlot(4, new GuiElementBuilder(Items.GOLD_BLOCK)
                .setName(Component.literal("Total wealth (equiv. coins)").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(String.format("%,d", wealth)).withStyle(ChatFormatting.YELLOW))
                .glow().build());

        gui.setSlot(11, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("Purse: " + String.format("%,d", coins)).withStyle(ChatFormatting.YELLOW))
                .build());
        gui.setSlot(12, new GuiElementBuilder(Items.DIAMOND)
                .setName(Component.literal("Credits: " + credits).withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("≈ " + String.format("%,d", credits * 10_000L) + " coins").withStyle(ChatFormatting.GRAY))
                .build());
        gui.setSlot(13, new GuiElementBuilder(Items.ENDER_CHEST)
                .setName(Component.literal("Bank main: " + String.format("%,d", bank)).withStyle(ChatFormatting.GREEN))
                .build());
        gui.setSlot(14, new GuiElementBuilder(Items.CHEST)
                .setName(Component.literal("Savings: " + String.format("%,d", savings)).withStyle(ChatFormatting.AQUA))
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.GOLD_NUGGET)
                .setName(Component.literal("Open /bank").withStyle(ChatFormatting.GREEN))
                .setCallback((a, b, c, d) -> BankGui.open(player))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> openMainMenu(player))
                .build());

        gui.open();
    }

    public static void openPartySection(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal("Party").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        for (int i = 0; i < 36; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray()).setName(Component.literal("")).build());
        }
        String uuid = player.getUUID().toString();
        long support = PoliticalPartyManager.getSupport(uuid);
        String pid = PoliticalPartyManager.getPlayerPartyId(uuid);

        gui.setSlot(4, new GuiElementBuilder(Items.EMERALD)
                .setName(Component.literal("Party support: " + support).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(pid != null ? "Party ID: " + pid : "Not in a party").withStyle(ChatFormatting.GRAY))
                .glow().build());

        gui.setSlot(11, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("Party commissions").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .setCallback((a, b, c, d) -> PartyJoinGui.openPartyCommissionsGui(player))
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("Party menu (/pp)").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .setCallback((a, b, c, d) -> PartyJoinGui.open(player))
                .build());

        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((a, b, c, d) -> openMainMenu(player))
                .build());

        gui.open();
    }
}
