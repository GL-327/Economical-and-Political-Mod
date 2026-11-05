package com.political.government;
import com.political.PlayerBuffManager;
import com.political.TaxManager;


import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PerksGui {

    private static final List<String> selectedPerks = new ArrayList<>();
    private static int currentPoints = 0;
    /** 0 = positive, 1 = neutral, 2 = negative */
    private static int categoryIndex = 0;
    /** Page within the current category */
    private static int perkSubPage = 0;
    private static final int PERKS_PER_PAGE = 28;

    /** Returns an item that visually represents the given perk. */
    private static net.minecraft.world.item.Item getPerkIcon(String perkId) {
        return switch (perkId) {
            case "DOUBLE_HEALTH"               -> Items.ENCHANTED_GOLDEN_APPLE;
            case "DOUBLE_DAMAGE"               -> Items.DIAMOND_SWORD;
            case "INCREASED_ARMOUR"            -> Items.DIAMOND_CHESTPLATE;
            case "SOFTER_LANDING"              -> Items.FEATHER;
                        case "PUBLIC_WORKS"                -> Items.SUGAR;
            case "GOLDEN_AGE"                  -> Items.GOLDEN_APPLE;
            case "NATIONAL_UNITY"              -> Items.SHIELD;
            case "XP_TAX_CUTS"                 -> Items.EXPERIENCE_BOTTLE;
            case "GREEN_THUMB"                 -> Items.WHEAT_SEEDS;
            case "RESOURCE_SUBSIDY"            -> Items.FURNACE;
            case "MONSTER_CONTROL"             -> Items.TOTEM_OF_UNDYING;
            case "PROSPERITY_SURGE"            -> Items.NETHER_STAR;
            case "FORTIFIED_SHIELDS"           -> Items.NETHERITE_CHESTPLATE;
            case "SWIFT_HARVEST"               -> Items.DIAMOND_PICKAXE;
            case "IRON_STOMACH"                -> Items.COOKED_BEEF;
            case "LUCKY_FISHERMAN"             -> Items.FISHING_ROD;
            case "BATTLE_HARDENED"             -> Items.IRON_INGOT;
            case "MERCHANTS_FAVOUR"            -> Items.EMERALD;
            case "PHOENIX_BLESSING"            -> Items.FIRE_CHARGE;
            case "TREASURE_HUNTER"             -> Items.DIAMOND;
            case "DIPLOMATIC_IMMUNITY"         -> Items.GOLDEN_HELMET;
            case "NIGHTVISION_DECREE"          -> Items.ENDER_EYE;
            case "ETERNAL_FOG"                 -> Items.DYE.gray();
            case "BIGGER_ISNT_ALWAYS_BETTER"   -> Items.COOKIE;
            case "NIGHT_OWL_POLICY"            -> Items.PHANTOM_MEMBRANE;
            case "WILDLIFE_PROTECTION"         -> Items.OAK_SAPLING;
            case "BALANCED_BUDGET"             -> Items.PAPER;
            case "CULTURAL_FESTIVAL"           -> Items.FIREWORK_ROCKET;
            case "TALL_ORDER"                  -> Items.TALL_GRASS;
            case "ETERNAL_DAWN"                -> Items.CLOCK;
            case "CHAOS_LOTTERY"               -> Items.RABBIT_FOOT;
            case "SILENT_WORLD"                -> Items.NOTE_BLOCK;
            case "MIRROR_WORLD"                -> Items.DAYLIGHT_DETECTOR;
            case "TRADERS_GAMBIT"              -> Items.GOLD_INGOT;
            case "BLOOD_MOON"                  -> Items.MAGMA_BLOCK;
            case "GIANTS_PLAYGROUND"           -> Items.SLIME_BLOCK;
            case "CIVIL_UNREST"                -> Items.REDSTONE;
            case "CRIME_WAVE"                  -> Items.ROTTEN_FLESH;
            case "INFRASTRUCTURE_NEGLECT"      -> Items.COBBLESTONE;
            case "ENVIRONMENTAL_MISMANAGEMENT" -> Items.DEAD_BUSH;
            case "ECONOMIC_COLLAPSE"           -> Items.COAL;
            case "ARCANE_DECAY"                -> Items.SPIDER_EYE;
            case "REDUCED_PATROLS"             -> Items.IRON_SWORD;
            case "HEAVY_GRAVITY"               -> Items.ANVIL;
            case "VOID_TOUCHED"                -> Items.ENDER_PEARL;
            case "MONSTER_UPRISING"            -> Items.ZOMBIE_HEAD;
            case "MINOR_CORRUPTION"            -> Items.FERMENTED_SPIDER_EYE;
            case "GLASS_CANNON"                -> Items.GLASS;
            case "FAMINE"                      -> Items.BONE;
            case "CURSED_WATERS"               -> Items.PUFFERFISH;
            case "CREEPER_SURGE"               -> Items.CREEPER_HEAD;
            case "BRITTLE_TOOLS"               -> Items.WOODEN_PICKAXE;
            case "WITHERING_ECONOMY"           -> Items.WITHER_ROSE;
            case "PARANOIA"                    -> Items.ENDER_EYE;
            case "SCORCHED_EARTH"              -> Items.FIRE_CHARGE;
            case "AUCTION_TAX_FREE"            -> Items.GOLD_BLOCK;
            case "AUCTION_TAX_REDUCTION"       -> Items.GOLD_INGOT;
            case "AUCTION_TAX_INCREASE"        -> Items.IRON_INGOT;
            // Bounty perks (V2.0)
            case "BOUNTY_HUNTER_SURGE"         -> Items.BLAZE_POWDER;
            case "SLAYER_CONTRACTS"            -> Items.NETHER_STAR;
            case "WEAKENED_QUARRY"             -> Items.SPIDER_SPAWN_EGG;
            case "HUNTERS_INSTINCT"            -> Items.BOW;
            case "BOUNTY_TAX"                  -> Items.GOLD_NUGGET;
            case "HARDENED_QUARRY"             -> Items.OBSIDIAN;
            // Legacy bounty perks (kept for save compatibility)
            case "BOUNTY_HUNTERS_ZEAL"         -> Items.BLAZE_POWDER;
            case "SLAYERS_FORTUNE"             -> Items.NETHER_STAR;
            case "QUICK_CONTRACTS"             -> Items.CLOCK;
            case "VETERANS_DISCOUNT"           -> Items.EMERALD_BLOCK;
            case "BOUNTY_DROUGHT"              -> Items.BARRIER;
            case "WEAKENED_PREY"               -> Items.SPIDER_SPAWN_EGG;
            case "ACADEMY_GRANTS"              -> Items.WRITABLE_BOOK;
            case "MINERS_ALLIANCE"             -> Items.IRON_PICKAXE;
            case "RATIONS_PROGRAM"             -> Items.BREAD;
            case "VETERANS_CARE"               -> Items.GOLDEN_CHESTPLATE;
            case "OPEN_SKIES"                  -> Items.FEATHER;
            case "COMMONS_LICENSE"             -> Items.MAP;
            case "STORM_DRILL"                 -> Items.LIGHTNING_ROD.weathering().unaffected();
            case "AURORA_POLICY"               -> Items.BLUE_ICE;
            case "MUSEUM_CURFEW"               -> Items.ITEM_FRAME;
            case "COIN_FLIP_DECREE"            -> Items.PLAYER_HEAD;
            case "HUSHED_REALM"                -> Items.STRUCTURE_VOID;
            case "TIDE_LOCK"                   -> Items.PRISMARINE;
            case "BOUNTY_SUBSIDY_II"         -> Items.NETHERITE_SCRAP;
            case "SLAYER_TOLL"                 -> Items.GOLD_NUGGET;
            case "QUARRY_FATIGUE"              -> Items.COBBLED_DEEPSLATE;
            case "PLAGUE_WARNING"              -> Items.SPLASH_POTION;
            case "EMPTY_COFFERS"               -> Items.BUNDLE;
            case "RIOT_SEASON"                 -> Items.TNT;
            case "TARIFF_SPIKE"                -> Items.EMERALD;
            case "ARCANE_LEVY"                 -> Items.ENCHANTED_BOOK;
            default                            -> Items.PAPER;
        };
    }

    /** Where Back leads from the perk picker: the menu that opened it, remembered per player. */
    private static final com.political.core.gui.ReturnTo FROM = new com.political.core.gui.ReturnTo();

    /** Opens the picker from a parent menu (Back returns there), or with {@code null} from a command. */
    public static void openFrom(ServerPlayer player, boolean isChair, Runnable back) {
        FROM.set(player.getUUID(), back);
        open(player, isChair);
    }

    public static void open(ServerPlayer player, boolean isChair) {
        if (!PerkManager.canChangePerks(isChair)) {
            player.sendSystemMessage(Component.literal("Perks have already been selected for this term!").withStyle(ChatFormatting.RED));
            return;
        }

        // ADD THESE 5 LINES:
        if (!isChair && !PerkManager.isChairPerksSetThisTerm()) {
            player.sendSystemMessage(Component.literal("The Chair must select their perks first!").withStyle(ChatFormatting.RED));
            return;
        }

        selectedPerks.clear();
        // ...
        currentPoints = 0;
        categoryIndex = 0;
        perkSubPage = 0;
        openPage(player, isChair);
    }

    private static void openPage(ServerPlayer player, boolean isChair) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);

        Perk.PerkType[] types = { Perk.PerkType.POSITIVE, Perk.PerkType.NEUTRAL, Perk.PerkType.NEGATIVE };
        String[] typeNames = { "Positive", "Neutral", "Negative" };
        Perk.PerkType currentType = types[categoryIndex];

        List<Perk> allOfType = new ArrayList<>(PerkManager.getPerksByType(currentType));
        allOfType.sort(Comparator.comparing(p -> p.id));
        int numPages = Math.max(1, (allOfType.size() + PERKS_PER_PAGE - 1) / PERKS_PER_PAGE);
        if (perkSubPage >= numPages) {
            perkSubPage = numPages - 1;
        }
        int from = perkSubPage * PERKS_PER_PAGE;
        int to = Math.min(allOfType.size(), from + PERKS_PER_PAGE);
        List<Perk> perksToShow = allOfType.subList(from, to);

        String title = (isChair ? "Chair" : "Vice Chair") + " — " + typeNames[categoryIndex]
                + " (" + (perkSubPage + 1) + "/" + numPages + ")";
        gui.setTitle(Component.literal(title));

        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                    .setName(Component.literal(""))
                    .build());
        }

        // CHANGED: Chair gets 6 perks, Vice Chair gets 2 (1 fewer if corrupt)
        // DIPLOMATIC buff grants +1 extra perk slot
        String playerUuid = player.getUUID().toString();
        int baseMaxPerks = isChair ? (ElectionManager.isCorrupt(playerUuid) ? 5 : 6) : (ElectionManager.isCorrupt(playerUuid) ? 1 : 2);
        int maxPerks = baseMaxPerks + PlayerBuffManager.getExtraPerkSlots(playerUuid);

// ADDED: Get Chair's selected perks (for Vice Chair to see as blocked)
        List<String> chairPerks = PerkManager.getChairSelectedPerks();

// ADDED: Get Vice Chair's perk (for Chair to see as blocked)
        List<String> vcPerks = PerkManager.getViceChairPerks();

        int slot = 10;
        for (Perk perk : perksToShow) {
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;
            if (slot >= 44) break;

            boolean isSelected = selectedPerks.contains(perk.id);
// FIXED: Remove && isChair so Vice Chair also respects cooldowns
            boolean isOnCooldown = PerkManager.isPerkOnCooldown(perk.id);
            // ADDED: Vice Chair can't pick perks the Chair already selected
            boolean isChairPerk = !isChair && chairPerks.contains(perk.id);
            boolean isVicePerk = isChair && vcPerks.contains(perk.id);
            boolean canSelect = selectedPerks.size() < maxPerks || isSelected;

            GuiElementBuilder builder;

            // ADDED: Show Chair's perks as blocked for Vice Chair
            if (isChairPerk || isVicePerk) {
                builder = new GuiElementBuilder(Items.STAINED_GLASS_PANE.purple())
                        .setName(Component.literal(perk.name).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.STRIKETHROUGH))
                        .addLoreLine(Component.literal(perk.description).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal(isVicePerk ? "SELECTED BY VICE CHAIR" : "SELECTED BY CHAIR")
                                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                        .addLoreLine(Component.literal("You cannot select this perk").withStyle(ChatFormatting.DARK_PURPLE));
            } else if (isOnCooldown) {
                builder = new GuiElementBuilder(Items.BARRIER)
                        .setName(Component.literal(perk.name).withStyle(ChatFormatting.RED, ChatFormatting.STRIKETHROUGH))
                        .addLoreLine(Component.literal(perk.description).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("ON COOLDOWN").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                        .addLoreLine(Component.literal("Used last term by Chair or Vice Chair").withStyle(ChatFormatting.DARK_RED))
                        .addLoreLine(Component.literal("Unavailable this term — skip one term").withStyle(ChatFormatting.GRAY));
            } else if (isSelected) {
                builder = new GuiElementBuilder(getPerkIcon(perk.id))
                        .setName(Component.literal(perk.name).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                        .addLoreLine(Component.literal(perk.description).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal(""));

                // Only show points for Chair
                if (isChair) {
                    builder.addLoreLine(getPointsLore(perk.pointValue))
                            .addLoreLine(Component.literal(""));
                }

                builder.addLoreLine(Component.literal("✓ SELECTED - Click to remove").withStyle(ChatFormatting.YELLOW))
                        .glow()
                        .setCallback((index, type, containerInput, slotGui) -> {
                            selectedPerks.remove(perk.id);
                            currentPoints -= perk.pointValue;
                            openPage(player, isChair);
                        });
            } else if (!canSelect) {
                builder = new GuiElementBuilder(Items.CONCRETE.red())
                        .setName(Component.literal(perk.name).withStyle(ChatFormatting.RED))
                        .addLoreLine(Component.literal(perk.description).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal(""));

                if (isChair) {
                    builder.addLoreLine(getPointsLore(perk.pointValue))
                            .addLoreLine(Component.literal(""));
                }

                builder.addLoreLine(Component.literal("Maximum perks selected!").withStyle(ChatFormatting.RED));
            } else {
                builder = new GuiElementBuilder(getPerkIcon(perk.id))
                        .setName(Component.literal(perk.name).withStyle(ChatFormatting.WHITE))
                        .addLoreLine(Component.literal(perk.description).withStyle(ChatFormatting.GRAY))
                        .addLoreLine(Component.literal(""));

                if (isChair) {
                    builder.addLoreLine(getPointsLore(perk.pointValue))
                            .addLoreLine(Component.literal(""));
                }

                builder.addLoreLine(Component.literal("Click to select").withStyle(ChatFormatting.YELLOW))
                        .setCallback((index, type, containerInput, slotGui) -> {
                            selectedPerks.add(perk.id);
                            currentPoints += perk.pointValue;
                            openPage(player, isChair);
                        });
            }

            gui.setSlot(slot, builder.build());
            slot++;
        }

        // Sub-page within category
        gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Prev (page)").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> {
                    perkSubPage = Math.max(0, perkSubPage - 1);
                    openPage(player, isChair);
                })
                .build());

        gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("Next (page) →").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> {
                    perkSubPage++;
                    openPage(player, isChair);
                })
                .build());

        gui.setSlot(46, new GuiElementBuilder(Items.NAME_TAG)
                .setName(Component.literal("Category: " + typeNames[categoryIndex]).withStyle(ChatFormatting.LIGHT_PURPLE))
                .addLoreLine(Component.literal("Click: Positive → Neutral → Negative").withStyle(ChatFormatting.GRAY))
                .setCallback((index, type, containerInput, slotGui) -> {
                    categoryIndex = (categoryIndex + 1) % 3;
                    perkSubPage = 0;
                    openPage(player, isChair);
                })
                .build());

        // CHANGED: Different info display for Chair vs Vice Chair
        if (isChair) {
            gui.setSlot(48, new GuiElementBuilder(Items.BOOK)
                    .setName(Component.literal("Daily budget").withStyle(ChatFormatting.GOLD))
                    .addLoreLine(com.political.government.Treasury.statusLine())
                    .addLoreLine(Component.literal("Payroll: " + com.political.government.Treasury.format(
                            com.political.government.Treasury.dailyPayroll()) + " / day").withStyle(ChatFormatting.YELLOW))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Perks cost coins. Cuts save coins and hurt the world.")
                            .withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("Selected: " + selectedPerks.size() + "/" + maxPerks).withStyle(ChatFormatting.WHITE))
                    .build());
        } else {
// Vice Chair info
            gui.setSlot(48, new GuiElementBuilder(Items.BOOK)
                    .setName(Component.literal("Vice Chair Perks").withStyle(ChatFormatting.AQUA))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Select up to " + maxPerks + " perks").withStyle(ChatFormatting.WHITE))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("✓ No point balance required!").withStyle(ChatFormatting.GREEN))
                    .addLoreLine(Component.literal("Perks used last term (Chair + VC) are blocked this term").withStyle(ChatFormatting.DARK_GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Selected: " + selectedPerks.size() + "/" + maxPerks).withStyle(ChatFormatting.WHITE))
                    .build());
        }

        // CHANGED: Different confirm logic for Chair vs Vice Chair
        boolean canConfirm;
        if (isChair) {
            canConfirm = !selectedPerks.isEmpty() && selectedPerks.size() <= maxPerks;
        } else {
            canConfirm = !selectedPerks.isEmpty() && selectedPerks.size() <= maxPerks;
        }

        if (canConfirm) {
            gui.setSlot(50, new GuiElementBuilder(Items.CONCRETE.lime())
                    .setName(Component.literal("✓ CONFIRM PERKS").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Click to activate perks!").withStyle(ChatFormatting.YELLOW))
                    .glow()
                    .setCallback((index, type, containerInput, slotGui) -> {
                        if (isChair) {
                            // Chair activates their perks
                            PerkManager.activatePerks(new ArrayList<>(selectedPerks), PerkManager.getViceChairPerks());
                            player.sendSystemMessage(Component.literal("Chair perks activated!").withStyle(ChatFormatting.GREEN));
                        } else {
                            // Vice Chair: activate with Chair's perks + VC's perks
                            List<String> currentChairPerks = PerkManager.getChairSelectedPerks();
                            // Store first VC perk (for backwards compatibility)
                            PerkManager.setViceChairPerks(selectedPerks);
                            // Add all VC perks to active perks
                            List<String> allPerks = new ArrayList<>(currentChairPerks);
                            for (String vcPerkId : selectedPerks) {
                                if (!allPerks.contains(vcPerkId)) {
                                    allPerks.add(vcPerkId);
                                }
                            }
                            PerkManager.activatePerksDirectly(allPerks, selectedPerks);
                            player.sendSystemMessage(Component.literal("Vice Chair perks activated!").withStyle(ChatFormatting.GREEN));
                        }
                        player.closeContainer();
                    })
                    .build());
        } else {
            List<String> lore = new ArrayList<>();
            if (isChair) {
                if (selectedPerks.isEmpty()) {
                    lore.add("§c• Select at least 1 perk");
                }
                if (selectedPerks.size() > 6) {
                    lore.add("§c• Select a maximum of 6 perks or cuts");
                }
            } else {
                if (selectedPerks.isEmpty()) {
                    lore.add("§c• Select at least 1 perk");
                }
                if (selectedPerks.size() > maxPerks) {
                    lore.add("§c• Select a maximum of " + maxPerks + " perks");
                }
            }

            GuiElementBuilder confirmBuilder = new GuiElementBuilder(Items.CONCRETE.red())
                    .setName(Component.literal("✗ CANNOT CONFIRM").withStyle(ChatFormatting.RED));
            for (String line : lore) {
                confirmBuilder.addLoreLine(Component.literal(line));
            }
            gui.setSlot(50, confirmBuilder.build());
        }

        // Tax toggle button (Chair only)
        if (isChair) {
            boolean taxEnabled = TaxManager.isTaxEnabled();
            gui.setSlot(49, new GuiElementBuilder(taxEnabled ? Items.GOLD_BLOCK : Items.GOLD_INGOT)
                    .setName(Component.literal("Tax System").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Status: " + (taxEnabled ? "ENABLED" : "DISABLED")).withStyle(taxEnabled ? ChatFormatting.GREEN : ChatFormatting.RED))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("5 credits daily tax").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal("+50% interest per day").withStyle(ChatFormatting.GRAY))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("Click to toggle").withStyle(ChatFormatting.YELLOW))
                    .setCallback((index, type, containerInput, slotGui) -> {
                        TaxManager.setTaxEnabled(!taxEnabled);
                        openPage(player, isChair);
                    })
                    .build());
        }

        com.political.core.gui.BackSlot.add(gui, FROM.get(player.getUUID()));
        gui.open();
    }

    private static Component getPointsLore(int points) {
        if (points > 0) {
            return Component.literal("Points: +" + points).withStyle(ChatFormatting.GREEN);
        } else if (points < 0) {
            return Component.literal("Points: " + points).withStyle(ChatFormatting.RED);
        } else {
            return Component.literal("Points: 0").withStyle(ChatFormatting.YELLOW);
        }
    }
}