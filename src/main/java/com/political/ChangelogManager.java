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

    static {
        // ── V6.0 - V6.9 ──
        add(new Version("v6", "V6.0 - V6.9", Items.WITHER_SKELETON_SKULL, "Cursed Tides", List.of(
                e("Cursed Spirits graded like curses out of Jujutsu Kaisen", "v6_spirits",
                        "A new hostile threat layer. Cursed Spirits are graded from Grade 4 up to Special Grade, "
                        + "with a dozen distinct archetypes. Grade 4 curses are common. A Special Grade is a rare "
                        + "and dangerous sight. They drop Cursed Energy, a new currency."),
                e("Cursed Energy and the Curse Hunter rank", "v6_energy",
                        "Slaying spirits banks Cursed Energy, spent on the sorcerer techniques. Lifetime kills raise "
                        + "your Curse Hunter rank, which grants a small permanent edge against spirits, capped so it "
                        + "supplements your gear rather than replacing it."),
                e("Special Grade bosses with Domain Expansions", "v6_bosses",
                        "Named multi-phase boss fights summoned by a ritual item. Each boss can expand a Domain, a "
                        + "temporary arena that hits harder and applies its signature curse. Arenas are fully cleaned "
                        + "up afterward. Bosses drop strong gear and a trophy."),
                e("The Sorcerer Path: become a Jujutsu Sorcerer", "v6_sorcerer",
                        "Player progression that answers the curses. Rank up from Grade 4 to Special Grade, unlock "
                        + "cursed tools, and fight smarter with Black Flash timed hits and reverse cursed technique "
                        + "healing that spends energy so it is never free."),
                e("Binding Vows and your own Domain Expansion", "v6_vows",
                        "Swear binding vows for opt-in tradeoffs, like giving up ranged attacks for stronger melee. "
                        + "A Special Grade sorcerer can expand their own Domain as a finisher, on a long cooldown and "
                        + "a heavy energy cost."),
                e("A variation pass across the whole mod", "v6_variety",
                        "Rotating settlement events like festivals, plagues and gold rushes, plus more expedition "
                        + "rooms, more trade goods, season festivals, and passing world modifiers. The living world "
                        + "screen shows everything in motion at once."),
                e("A clickable changelog and a real world map", "v6_qol",
                        "This changelog, opened from the hub, with dig deeper prompts on every line. The Atlas now "
                        + "hands you a real filled map that draws the actual terrain as you carry it.")
        )));
    }

    static {
        // ── V7.0 - V7.9 ──
        add(new Version("v7", "V7.0 - V7.9", Items.NETHERITE_SWORD, "Clans and Cursed Steel", List.of(
                e("Sorcerer Clans, a JJK great-clan layer", "v7_clans",
                        "Found a sorcerer bloodline with its own signature cursed technique. Recruit members, "
                        + "rank them, build a shared treasury, raise clan prestige from what your members achieve, "
                        + "and feud with rival clans."),
                e("Inherited signature techniques", "v7_inherited",
                        "Each clan carries a signature cursed technique that members can channel once they meet the "
                        + "clan and rank requirements. They cost cursed energy and sit on a cooldown, so they read as "
                        + "a clan's calling card rather than a spam button."),
                e("The Cursed Arsenal", "v7_arsenal",
                        "A graded line of cursed weapons and armor from Grade 4 up to Special Grade. Each weapon type "
                        + "has its own signature effect, and higher grades are forged from the lower ones, so it is a "
                        + "progression to climb."),
                e("More cursed spirits", "v7_morespirits",
                        "New cursed spirit archetypes join the roster, each with its own behaviour, so the world stays "
                        + "dangerous and varied as you climb the ranks."),
                e("A modern look and a Featured board", "v7_modern",
                        "A shared GUI toolkit gives the screens one clean modern style, with borders, headers and stat "
                        + "pills. A Featured board on the hub, also opened with /whatsnew, summarises the latest systems "
                        + "and opens each by clicking."),
                e("Flourishes and item shine", "v7_flair",
                        "Tasteful particle and sound bursts play on moments like a magic level up or a boss spawn, and "
                        + "rare and above gear now carries a glint, so older content looks cooler without changing how "
                        + "it plays.")
        )));
    }

    static {
        // ── V8.0 ──
        add(new Version("v8", "V8.0", Items.END_ROD, "Spectacle", List.of(
                e("A cinematic effects engine", "v8_engine",
                        "Effects are now animated instead of instant. Each one registers with a scheduler and plays "
                        + "out over many ticks, so a beam can charge, fire, flicker and bloom where it lands. This is "
                        + "what separates a real set piece from a puff of smoke."),
                e("Every effect is layered like a film shot", "v8_layers",
                        "Each set piece is built from a bright dense core that reads as the shape, a sheath spun "
                        + "around it for thickness, a loose haze to soften the edge, a muzzle flash at the start and "
                        + "a bloom at the end, with a low sound body under a bright crack."),
                e("Nine signature set pieces", "v8_setpieces",
                        "Arcane, Inferno, Void and Cursed lances, a forked Storm Strike from the sky, a Domain "
                        + "Collapse that slams out and implodes, a falling Meteor, a Void Rift that tears open and "
                        + "snaps shut, and a soft Bloom for healing."),
                e("Real abilities were upgraded to use them", "v8_upgrades",
                        "Thunder strikes now call down a proper forked bolt with a ground shockwave. Void Strike "
                        + "tears a lance across the gap and opens a rift on the victim. Domain Expansion plays the "
                        + "full dome cinematic."),
                e("An effects gallery you can browse", "v8_gallery",
                        "Open the gallery from the hub, or with /vfx, and click any set piece to watch it play where "
                        + "you are aiming."),
                e("Built to stay safe under load", "v8_safety",
                        "The scheduler caps how many effects can run at once, and a failing effect is dropped rather "
                        + "than allowed to disturb the server tick.")
        )));
    }

    static {
        // ── Version X ── (there is no 9 or 10, this is one large update)
        add(new Version("vx", "Version X", Items.NETHERITE_INGOT, "The Pinnacle", List.of(
                e("Reforges, the content multiplier", "vx_reforge",
                        "Over forty reforges spread across weapons, armour, tools and everything else. Any item in "
                        + "the mod can carry one, and it renames the item and layers its own stats on top of what the "
                        + "item already gives. This is what turns one Cursed Blade into dozens of different Cursed "
                        + "Blades, so it multiplies the whole catalogue rather than adding to the end of it."),
                e("The reforge anvil", "vx_anvil",
                        "Hold an item and open the anvil from the hub or with /reforge. You can see what it carries "
                        + "now, reroll it for coins, strip it back to base, and browse every reforge the item could "
                        + "roll so you know what you are chasing."),
                e("Rerolling is a real coin sink", "vx_cost",
                        "A reroll starts at five thousand coins and climbs by two and a half thousand every time you "
                        + "reroll that same item, up to a hundred thousand. Chasing a perfect reforge on one weapon "
                        + "costs real money, which keeps it from being a free upgrade."),
                e("Reforges never stack", "vx_stack",
                        "The reforge block in an item's lore is delimited by a marker. Applying a new one strips "
                        + "everything from that marker down and writes a fresh block, so no amount of rerolling can "
                        + "pile stats on top of each other. The original name is remembered the first time, so "
                        + "prefixes never pile up either."),
                e("Forty five relics and an accessory bag", "vx_relics",
                        "Each relic is one hand written effect rather than a stat stick. They fire on a strike, on a "
                        + "kill, when you are hurt, or simply while carried. Only the ones in your bag are live, and "
                        + "the bag starts at three slots and grows to nine, so relics are a choice about what kind of "
                        + "fighter you are."),
                e("Twenty one companions", "vx_companions",
                        "Companions across five roles: damage, tank, support, forager and scout. One walks with you "
                        + "at a time. They level from your kills, with a daily cap so they cannot be grown by leaving "
                        + "the game running, and they are cleaned up when you log out."),
                e("A rotating contract board", "vx_contracts",
                        "Thirty two contract templates across hunt, gather, deliver, explore, craft and civic work. "
                        + "The board rewrites every two hours, you may hold three at once, and there is a cap on how "
                        + "many you can finish per rotation, so it pays steadily without ever beating going out and "
                        + "playing."),
                e("Fourteen more cinematic set pieces", "vx_vfx",
                        "An orbital strike, chain lightning that hops between targets, a rising pillar of flame, a "
                        + "singularity that drags everything in and detonates, a blade slash, a shock cone, healing "
                        + "rain, a frost nova, a summoning circle, a soul harvest, a comet volley, a shield dome, a "
                        + "ground fissure and an execution finisher."),
                e("The Aether Arsenal, a hundred new pieces", "vx_aether",
                        "Ten attunements forged into ten pieces each: blade, greataxe, lance, pickaxe, mattock, "
                        + "reaper, helm, cuirass, greaves and treads. Every slot has its own role and its own line "
                        + "of behaviour, so the set is a hundred distinct items rather than ten recoloured ten times."),
                e("Ten signature effects that exist nowhere else", "vx_signatures",
                        "Each attunement owns an animation built as a different shape, not a recoloured burst. A "
                        + "solar disc throwing spokes, shadow tendrils crawling out and snapping back, lightning "
                        + "arcing between orbiting anchors, a crystal lattice growing in stepped struts, a spiral of "
                        + "growth opening at the top, a pressure column bursting at the surface, a fire vortex "
                        + "tightening as it climbs, a starfield with constellation lines traced one by one, rings "
                        + "pulsing in a heartbeat rhythm, and a seam in the world tearing open then closing."),
                e("Attunements read at a glance", "vx_auras",
                        "Worn Aether armour gives off its attunement's aura, and each one moves differently, so you "
                        + "can tell what someone is wearing across a field. Weapons fire their signature on a strike "
                        + "and tools fire theirs as you break blocks, both on cooldowns so it cannot be spammed. A "
                        + "full set of one attunement answers when you are badly hurt."),
                e("The Runeforge, six hundred more pieces", "vx_runeforge",
                        "Forty runes struck into fifteen shapes. The shape decides how it fights and when it "
                        + "fires, the rune decides what happens when it does, so a Pyre Dagger and a Pyre "
                        + "Warhammer burn the same way and play nothing alike, while a Pyre Dagger and a "
                        + "Deepfreeze Dagger swing the same way and do opposite things."),
                e("Twenty rune effects", "vx_runeeffects",
                        "Burning, chilling, shock that arcs to neighbours, poison, wither, life stealing, armour "
                        + "breaking, rooting, blinding, levitation, pulling enemies in, throwing them out, and "
                        + "self buffs for haste, shielding, healing and speed, plus runes that draw cursed "
                        + "energy, coin or experience out of the moment."),
                e("The operator console replaces the admin panel", "vx_console",
                        "The old panel covered seven categories while the mod had grown past three hundred "
                        + "and eighty classes, so most of what an operator needs had no button at all. The new "
                        + "console covers thirteen areas including a per player card, live server health and a "
                        + "settlement inspector. Everything destructive asks first. The old panel is still one "
                        + "click away under Legacy."),
                e("Two custom villages", "vx_villages",
                        "A cherry village and a swamp village, eighty seven hand built pieces between them, "
                        + "generating on their own the way vanilla villages do. Both target biomes have no "
                        + "vanilla village at all, so they fill real gaps rather than competing for space. "
                        + "Open /villages to locate the nearest one."),
                e("The tier system actually works now", "vx_tiers_fixed",
                        "Settlement tiers were unreachable rather than broken. Growth sat near three percent a "
                        + "roll, only one settlement was rolled every ten seconds, and the food gate applied "
                        + "from the very first citizen, which deadlocked every new settlement at one resident "
                        + "since one person with no workstation grows no food. All three are fixed, and a new "
                        + "progress screen lists every requirement for the next tier and marks the one blocking "
                        + "you, because none of it was visible before."),
                e("Rare drops", "vx_raredrops",
                        "Forty four items that cannot be crafted and only fall off something. Every other reward "
                        + "in the mod can be planned for, and a server needs a few things that cannot. The odds "
                        + "are printed on the item and in the browser rather than hidden, so it is a known long "
                        + "shot instead of a rumour. A rare find tells you, a crazy one tells anybody close "
                        + "enough to have seen it, and an insane one tells the whole server."),
                e("Refined materials", "vx_materials",
                        "Twenty four materials that do not exist in vanilla, from mithril and titanium to "
                        + "tungsten, umber and soul string. Each runs a five step chain from a raw lump up to a "
                        + "super compacted block, nine of one making one of the next, so a super compacted block "
                        + "is six and a half thousand raw. Every step is a real recipe in the same store as "
                        + "everything else, so the whole chain shows in the recipe browser and can be edited "
                        + "like anything else rather than hidden behind a command. A raw lump is smelted out of "
                        + "vanilla goods, which ties the new economy to the existing one instead of running "
                        + "beside it."),
                e("Titles", "vx_titles",
                        "Every other reward is a thing you carry. A title is the one that shows without anybody "
                        + "asking, which makes it the right home for the milestones that are otherwise invisible: "
                        + "nobody can see that you maxed a collection or that a Nightmare is hunting you. Each "
                        + "title checks its own requirement live, so none can be held after the thing that "
                        + "granted it is gone, and the screen lists the unearned ones too because those are the "
                        + "half that tells you what is worth doing."),
                e("The Requisition Board", "vx_commissions",
                        "Ten thousand eight hundred and ninety nine postings, generated from a goods table "
                        + "rather than written by hand, which is the only way a set that size stays consistent. "
                        + "Hand in what a posting asks for and the goods are taken, not merely counted, so every "
                        + "commission is a decision about whether the reward is worth the material. Difficulty is "
                        + "value times count and everything else follows from it, from an Errand paying a couple "
                        + "of ingots up to an Impossibility asking for sixty four netherite blocks."),
                e("The Quartermaster's Warrant", "vx_warrant",
                        "Four pieces, paid out only by Great Works and Impossibilities. It cannot be crafted, "
                        + "bought, rolled out of a crate or traded. Each piece grants a quarter of one ability "
                        + "rather than a perk of its own, and worn together they become Levy: charge built from "
                        + "every hand in can be spent to requisition a posting outright and fill what is owed."),
                e("Crates", "vx_crates",
                        "Five crates bought with coins you earned. Every crate publishes its full odds per band "
                        + "before you buy, and a pity counter guarantees a good pull after a stated number of "
                        + "poor ones, so a losing streak has a known ceiling. Rewards are drawn from the live "
                        + "recipe catalogue, so they cover content added after the crates were written."),
                e("The PVE systems talk to each other", "vx_pve_crossover",
                        "A siege with nothing better available is led by a nameless ravager. A siege where any "
                        + "defender is being hunted by a Slayer or worse hands command to that grudge instead, so "
                        + "the thing that has been killing you turns up at the head of an army in the town you "
                        + "built. An incursion sovereign that kills you becomes your nemesis the same way any "
                        + "other mob would, and it stays in the fight while it does it."),
                e("Sieges", "vx_sieges",
                        "Something walks out of the dark and tries to take a settlement off you. The other PVE "
                        + "happens where a player is standing, a siege happens where a player decided to build, "
                        + "which is the one location the server knows matters to them. Attackers come in from "
                        + "outside and head for the cornerstone, and letting them hold it unopposed for thirty "
                        + "seconds costs the settlement appeal and a fifth of its people. Scale comes from the "
                        + "tier, so growing a settlement is a decision to invite bigger problems."),
                e("Incursions", "vx_incursions",
                        "A rift opens where you stand and becomes whatever the ground deserves. The biome "
                        + "under you picks the theme, the roster, the standing hazard and the sovereign that "
                        + "carries the final wave, so the same rift in a swamp, on a peak and in the nether is "
                        + "three different fights that nobody had to author per location. Seven waves, each "
                        + "harder than the last. Leave the ring and it collapses."),
                e("The Nemesis system", "vx_nemesis",
                        "A mob that kills you does not stay an ordinary mob. It takes a name, rolls two or "
                        + "three traits from a pool of twenty three, and starts hunting you specifically. Kill "
                        + "you again and it climbs a rank and gains another trait, up through Stalker, Hunter, "
                        + "Slayer, Warlord and Nightmare. Put it down and the grudge is settled for good, unless "
                        + "it rolled Cunning, in which case it runs, comes back scarred and is worse for it. "
                        + "Every grudge belongs to one player and nobody else can settle it."),
                e("The Bazaar", "vx_bazaar",
                        "A double sided order book for every collection material. Post a buy order or a sell "
                        + "offer at your own price, or take the other side instantly at the best price posted. "
                        + "A house book quotes either side at a deliberately poor spread, so the market still "
                        + "works with nobody else online and trading against real players is always better. "
                        + "Buy orders that fill while you are offline hold the goods until you collect them."),
                e("Collections", "vx_collections",
                        "Sixty collections across farming, mining, combat, foraging, fishing and the arcane. "
                        + "Every material you gather counts for the rest of your time on the server, and each "
                        + "collection pays out at nine thresholds in coins and a permanent stat bonus. It is the "
                        + "only progression in the mod that cannot be traded, bought or handed over."),
                e("Every line is keyed on its own catalyst", "vx_catalysts",
                        "Nine crafting catalysts, one per gear line, each craftable from a crafting core. "
                        + "A piece of gear now wants its line catalyst plus a core of the tier that cut "
                        + "deserves, so tools and charms sit at T1, weapons and light armour at T2, and the "
                        + "chest and legs at T3. The Runeforge and the Aether Arsenal were rebuilt onto the "
                        + "same footing so the whole expansion reads one way."),
                e("Grimoire and the Focus pool", "vx_grimoire",
                        "Forty five sigils cut into fourteen forms. Nothing in this line happens on its own. "
                        + "Sneak and use a sigil and it casts, spending Focus, and Focus caps and refills from "
                        + "Intelligence, which finally gives that stat a job. Forty three distinct castings, "
                        + "and the Circle you wear bends the whole pool."),
                e("Tempest and the charge reactions", "vx_tempest",
                        "Forty five storms cut into fourteen forms. Hits hang a charge on the target, and two "
                        + "different charges on one target react into something neither does alone. Twenty "
                        + "eight pairs, twenty eight results. Charges sit on the target rather than on you, so "
                        + "two players running different fronts reach results neither could alone."),
                e("Voyager and Leagues", "vx_voyager",
                        "Forty five waypoints cut into fourteen forms. This line asks where you are. A piece "
                        + "is asleep outside its reach and awake inside it, and everything scales with Leagues, "
                        + "which counts your distance from the nearest settlement. Wearing it at home is the "
                        + "weakest thing in the mod. Twelve Leagues out it is one of the strongest."),
                e("Regalia and Standing", "vx_regalia",
                        "Forty five offices cut into fourteen forms, and the one line that reads the politics. "
                        + "Standing is built from your settlement tier, the seat you hold and whether you are "
                        + "inside your own borders. A citizen with no seat gets almost nothing. A Chair at home "
                        + "in an Elysium gets the strongest numbers in the mod, and loses most of them the "
                        + "moment they lose an election. It is the exact mirror of Voyager."),
                e("Beastcraft and the Instinct meter", "vx_beastcraft",
                        "Forty five beasts cut into fourteen forms. Beastcraft does not tick and it does not "
                        + "wait for a lucky proc. Every hit you land feeds one Instinct meter, the clade of the "
                        + "piece decides how many hits fill it, and filling it hands the fight to the beast for a "
                        + "moment. Swarm pieces spend often and small, Leviathan pieces spend rarely and huge."),
                e("Clockwork and Pressure", "vx_clockwork",
                        "Forty five mechanisms cut into fourteen forms. Clockwork winds itself while carried and "
                        + "spends what it builds on breaking blocks, so it is the one line you can play without "
                        + "fighting anything. Duties run from smelting ore as it drops to boring a tunnel with a "
                        + "beam, and three of a Works lights that Works on top."),
                e("Starbound gear and the six houses", "vx_starbound",
                        "Thirty six constellations cut into twelve forms. Where a rune waits for an event, a "
                        + "constellation is simply always on, so this is what you wear when you want a standing "
                        + "shape to your character rather than a proc. Carry three pieces of one house and that "
                        + "house lights, adding its own standing bonus on top."),
                e("Version X is one update", "vx_naming",
                        "There is no version nine or ten. Everything here landed as a single large update, which is "
                        + "why the mod jumps from eight straight to Version X.")
        )));
    }

    /** Registered later by more code so the changelog stays in one place. */
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
