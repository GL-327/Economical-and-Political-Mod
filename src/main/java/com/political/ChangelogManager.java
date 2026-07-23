package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The changelog. Every version group lists its changes in chat. Each change carries a clickable
 * "dig deeper" prompt that prints a longer explanation of that one thing.
 *
 * Data only. {@link ChangelogGui} renders the version picker and {@code /changelog} routes the
 * click actions back here.
 */
public final class ChangelogManager {

    private ChangelogManager() {}

    /** One line in a changelog, with an optional deeper explanation. */
    public record Entry(String summary, String detailKey, String detail) {}

    /** A grouped version block (for example "V4.0 - V4.9"). */
    public record Version(String id, String title, Item icon, String tagline, List<Entry> entries) {}

    private static final List<Version> VERSIONS = new ArrayList<>();
    private static final Map<String, String> DETAILS = new LinkedHashMap<>();
    private static final Map<String, Version> BY_ID = new LinkedHashMap<>();

    private static void add(Version v) {
        VERSIONS.add(v);
        BY_ID.put(v.id().toLowerCase(), v);
        for (Entry e : v.entries()) {
            if (e.detailKey() != null && e.detail() != null) {
                DETAILS.put(e.detailKey(), e.detail());
            }
        }
    }

    private static Entry e(String summary, String key, String detail) {
        return new Entry(summary, key, detail);
    }

    static {
        // ── V0.1 - V1.0 ──
        add(new Version("v1", "V0.1 - V1.0", Items.WOODEN_PICKAXE, "Foundation", List.of(
                e("Coins, credits and a player bank with interest", "v1_econ",
                        "The base economy. Coins are the working currency and credits are the premium one. "
                        + "The bank pays interest on stored coins on a slow timer. This is the money layer every "
                        + "later system plugs into."),
                e("Political offices: Chair, Vice Chair and Judge", "v1_gov",
                        "A weekly election picks a Chair and Vice Chair. The Chair sets tax, interest and picks "
                        + "server wide perks. A Judge is appointed by operators and can impeach, exile and pardon."),
                e("Political parties with budgets and perks", "v1_party",
                        "Players join parties, donate coins for support, and the party spends its budget on perks "
                        + "that help its members. Tax revenue is split across party budgets."),
                e("Slayer bounties across seven boss types", "v1_slayer",
                        "Seven slayer families, each with five tiers. Matching gear does more damage and takes less. "
                        + "Bosses drop the swords, armor and cores used to craft the next tier."),
                e("Custom item framework on vanilla items", "v1_items",
                        "Every custom item is a normal Minecraft item wearing a hidden id, a name and lore. This is "
                        + "why the whole mod runs server side with no client download.")
        )));

        // ── V2.0 - V2.6 ──
        add(new Version("v2", "V2.0 - V2.6", Items.ENCHANTING_TABLE, "Systems Expansion", List.of(
                e("Magic: spells, mana and a skill tree", "v2_magic",
                        "Thirty three spells, a mana pool, and a five branch skill tree. Magic experience comes from "
                        + "killing magic creatures which are reskinned vanilla mobs with their own drops."),
                e("Mastery families and the Heart of Mastery tree", "v2_mastery",
                        "Six mastery families level up from the matching activity. A coin funded Heart of Mastery tree "
                        + "buys permanent bonuses across mining, combat and fortune."),
                e("Ten player classes with permanent bonuses", "v2_class",
                        "Pick a class to gain attribute bonuses and permanent effects. Your class also decides which "
                        + "mastery family your kills feed."),
                e("Dragon chestplates and dragon swords", "v2_dragon",
                        "A large set of dragon themed chestplates and swords, each with its own on hit and passive "
                        + "behaviour and particle trail."),
                e("Auction house, stock market and crypto market", "v2_markets",
                        "A player auction house plus two simulated markets with moving prices, brokers and fees, so "
                        + "coins have somewhere to go and grow.")
        )));

        // ── V3.0 - V3.5 ──
        add(new Version("v3", "V3.0 - V3.5", Items.NETHERITE_BLOCK, "Block Arsenal and Fixes", List.of(
                e("Over two thousand block themed tools, weapons and armor", "v3_blockgear",
                        "Armor, swords and tools for almost every block in the game. Each set has its own identity, "
                        + "and every custom item now carries stat lines that feed the stats engine."),
                e("Skyblock style stats system", "v3_stats",
                        "Strength, Crit Chance, Crit Damage, Defense, Health, Speed, Intelligence and Ferocity. Stats "
                        + "are read straight from item lore, so any item with a stat line just works. Open it with the "
                        + "Stats screen."),
                e("Mob scaling with ten tiers", "v3_scaling",
                        "Wild mobs can roll a scaling tier from Enhanced up to Eternal, gaining health, damage and a "
                        + "coin bounty. Tiers now persist across restarts instead of stacking."),
                e("Unique full set bonus for every armor set", "v3_setbonus",
                        "All 144 block armor materials received a named four piece set bonus with its own mechanic, "
                        + "particle and sound, replacing the old shared handful of effects."),
                e("Weapon ability visuals and a pile of bug fixes", "v3_visuals",
                        "Every named weapon ability got particles and sound. Corrupted colour codes, a broken Gavel "
                        + "ability and a Ferocity interaction were all fixed.")
        )));

        // ── V4.0 - V4.9 ──
        add(new Version("v4", "V4.0 - V4.9", Items.BELL, "Civilization", List.of(
                e("Settlements founded with a Cornerstone", "v4_settlement",
                        "Place a Cornerstone to found a settlement. It scans what you build around it and sorts you "
                        + "into a tier from Outpost up to Elysium based on beds, appeal, population and treasury."),
                e("Fallout style settler economy", "v4_economy",
                        "Citizen villagers wander in and take jobs bounded by the workstations you build. They produce "
                        + "food, goods, coin and defense. Food must cover the population or growth stops and morale falls."),
                e("Appeal reworked to stop block spam abuse", "v4_appeal",
                        "Appeal now uses diminishing returns, per type caps, per category caps and a footprint check. "
                        + "A thousand diamond blocks no longer buys a Utopia. Variety and real building do."),
                e("NPC democracy and a full government hierarchy", "v4_democracy",
                        "Cities elect a Constituent. Metropolises elect a Chair and Vice. Utopias form a High Council "
                        + "with a Prime Seat, ministries and timed edicts. Citizen NPCs vote using their own opinions."),
                e("Wars between rival governments", "v4_war",
                        "Every metropolis is its own realm. A weighted War Council votes on going to war. Elected "
                        + "officials outweigh the unelected Shadow Government, and the Chair holds a veto."),
                e("Twelve magic schools and JJK style techniques", "v4_technique",
                        "Twelve school skill trees plus twenty four Magic Techniques. Ten are common, and several are "
                        + "server exclusive, held by only one player until death or renouncement."),
                e("Working Backrooms and crafting fixes", "v4_backrooms",
                        "The Backrooms is now a real void dimension with generated rooms, wanderers, loot and exit "
                        + "portals. Seventy one failing recipes were repaired down to zero."),
                e("Everything moved into clickable GUIs", "v4_gui",
                        "The Civilization Hub links every system. War, government, high council, settlement economy, "
                        + "backrooms and stats all opened by clicking. Commands still exist but are never required.")
        )));

        // ── V5.0 - V5.3 ──
        add(new Version("v5", "V5.0 - V5.3", Items.ENDER_EYE, "The Living Realm", List.of(
                e("Expeditions: instanced procedural dungeons", "v5_expedition",
                        "Five tiers of instanced dungeon with generated floors, combat rooms, lever puzzles, vaults "
                        + "and a boss arena. Higher tiers launch from a City or better. Vault chests only fill once "
                        + "their guards die."),
                e("Guilds and Orders", "v5_guild",
                        "Cross settlement player groups with ranks, a shared treasury, guild levels and an eleven perk "
                        + "tree. A guild can claim a hall at a Town or better settlement."),
                e("Caravans and inter settlement trade", "v5_trade",
                        "Settlements form trade routes with supply and demand pricing. A caravan NPC physically travels "
                        + "the route and can be ambushed, especially during a war between the two realms."),
                e("Seasons that change the harvest", "v5_season",
                        "A four season cycle changes crop growth and settlement food output. Winter granaries really do "
                        + "run thin, which feeds straight into the settler economy."),
                e("Bestiary and the Great Hunt", "v5_bestiary",
                        "Track your kills of every mob type for small permanent bonuses at ten, fifty, two hundred fifty "
                        + "and a thousand kills. A rotating Great Hunt spawns a rare named beast for the first to fell it.")
        )));
    }

    /** Registered later by V6 code so the changelog stays in one place. */
    public static void registerVersion(Version v) {
        add(v);
    }

    public static List<Version> versions() {
        return VERSIONS;
    }

    public static Version byId(String id) {
        return id == null ? null : BY_ID.get(id.toLowerCase());
    }

    // ── Chat output ──────────────────────────────────────────────────────────

    /** Prints one version's change list to the player, each line with a dig deeper prompt. */
    public static void printVersion(ServerPlayer player, String id) {
        Version v = byId(id);
        if (v == null) {
            player.sendSystemMessage(Component.literal("§cUnknown version.").withStyle(ChatFormatting.RED));
            return;
        }
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§6§l" + v.title() + " §7- §f" + v.tagline()));
        player.sendSystemMessage(Component.literal("§8Click §b[dig deeper] §8on any line to learn more."));
        player.sendSystemMessage(Component.literal(""));
        for (Entry entry : v.entries()) {
            MutableComponent line = Component.literal("§7• §f").append(Component.literal(entry.summary()));
            if (entry.detailKey() != null) {
                line = line.append(Component.literal("  "))
                        .append(digDeeper(entry.detailKey()));
            }
            player.sendSystemMessage(line);
        }
        player.sendSystemMessage(Component.literal(""));
    }

    /** Prints the deeper explanation for one change. */
    public static void printDetail(ServerPlayer player, String key) {
        String detail = DETAILS.get(key);
        if (detail == null) {
            player.sendSystemMessage(Component.literal("§cNothing more to show for that.")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        player.sendSystemMessage(Component.literal(""));
        // Wrap the detail so long text stays readable in chat.
        for (String chunk : wrap(detail, 60)) {
            player.sendSystemMessage(Component.literal("§7" + chunk));
        }
        player.sendSystemMessage(Component.literal(""));
    }

    private static Component digDeeper(String key) {
        return Component.literal("§b§n[dig deeper]").withStyle(s -> s
                .withClickEvent(new ClickEvent.RunCommand("/changelog detail " + key))
                .withHoverEvent(new HoverEvent.ShowText(
                        Component.literal("§7Click to read more"))));
    }

    private static List<String> wrap(String text, int width) {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (line.length() + word.length() + 1 > width) {
                out.add(line.toString());
                line = new StringBuilder();
            }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        if (line.length() > 0) out.add(line.toString());
        return out;
    }
}
