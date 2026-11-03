package com.political;


import com.political.economy.CoinManager;
import com.political.settlement.Settlement;
import com.political.settlement.SettlementManager;
import com.political.settlement.SettlementTier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Sieges: something walks out of the dark and tries to take a settlement off you.
 *
 * <p>The other PVE here happens where a player happens to be standing. A siege happens where a player
 * decided to build, which on a random seed is the only location the server can be certain matters to
 * them. That is the whole design: the stakes are not a loot table, they are the settlement's tier,
 * its appeal and its population, all of which took real time to earn and all of which a lost siege
 * takes back.</p>
 *
 * <p>Scale comes from the settlement itself. An outpost gets a handful of attackers and a metropolis
 * gets an army with a commander, so growing a settlement is genuinely a decision to invite bigger
 * problems rather than a pure upgrade.</p>
 */
public final class SiegeManager {

    private SiegeManager() {}

    public static final String ATTACKER_TAG = "political_siege";

    private static final Random RNG = new Random();
    /** How close an attacker has to get to the cornerstone to be counted as breaching. */
    private static final double BREACH_RADIUS = 6.0;
    /** Seconds of unopposed presence at the cornerstone before the settlement falls. */
    private static final int BREACH_SECONDS = 30;
    private static final double SPAWN_RING = 40.0;

    public static boolean enabled = true;

    public static final class Active {
        public final UUID id = UUID.randomUUID();
        public String settlementId;
        public String settlementName;
        public String dim;
        public BlockPos cornerstone;
        public int wave;
        public int totalWaves;
        public int ticks;
        public int breachTicks;
        public int slain;
        public final List<UUID> attackers = new ArrayList<>();
        public ServerBossEvent bar;
        public final List<String> defenders = new ArrayList<>();
        /** A defender's grudge that has taken command of this siege, if any. */
        public String commanderNemesisId = "";
    }

    private static final Map<String, Active> ACTIVE = new HashMap<>();

    public static java.util.Collection<Active> all() {
        return ACTIVE.values();
    }

    public static Active forSettlement(String settlementId) {
        return ACTIVE.get(settlementId);
    }

    public static int count() {
        return ACTIVE.size();
    }

    // ── Scale ─────────────────────────────────────────────────────────────

    /** How many waves a settlement of this tier has to survive. */
    public static int wavesFor(SettlementTier tier) {
        return switch (tier) {
            case OUTPOST, HAMLET -> 3;
            case VILLAGE, TOWN -> 4;
            case CITY -> 5;
            case METROPOLIS -> 6;
            case MEGALOPOLIS -> 7;
            case UTOPIA, ELYSIUM -> 8;
        };
    }

    /** What a defended siege pays out. */
    public static long rewardFor(SettlementTier tier, int slain) {
        return (tier.ordinal() + 1) * 8_000L + slain * 300L;
    }

    private static SettlementTier tierOf(Settlement s) {
        try {
            return SettlementTier.valueOf(s.tier);
        } catch (Exception e) {
            return SettlementTier.OUTPOST;
        }
    }

    // ── Starting ──────────────────────────────────────────────────────────

    /** Opens a siege against a settlement. Returns false when one is already running there. */
    public static boolean begin(MinecraftServer server, Settlement s) {
        if (!enabled || s == null) return false;
        if (ACTIVE.containsKey(s.id)) return false;

        ServerLevel level = SettlementManager.levelFor(server, s);
        if (level == null) return false;

        SettlementTier tier = tierOf(s);
        Active a = new Active();
        a.settlementId = s.id;
        a.settlementName = s.name;
        a.dim = s.worldDim;
        a.cornerstone = new BlockPos(s.cornerstoneX, s.cornerstoneY, s.cornerstoneZ);
        a.totalWaves = wavesFor(tier);
        a.wave = 0;
        a.bar = new ServerBossEvent(UUID.randomUUID(),
                Component.literal("Siege of " + s.name)
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
        ACTIVE.put(s.id, a);

        // Everybody near enough to help gets told, whether they live there or not.
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(Vec3.atCenterOf(a.cornerstone)) > 200 * 200) continue;
            a.bar.addPlayer(p);
            a.defenders.add(p.getUUID().toString());
            p.sendSystemMessage(Component.literal(""));
            p.sendSystemMessage(Component.literal("  §4§lSIEGE"));
            p.sendSystemMessage(Component.literal("  §7" + s.name + " §8("
                    + tier.colorCode + tier.displayName + "§8)"));
            p.sendSystemMessage(Component.literal("  §7" + a.totalWaves
                    + " waves. Hold the cornerstone."));
            p.sendSystemMessage(Component.literal("  §8Let them stand on it for "
                    + BREACH_SECONDS + " seconds and the settlement pays for it."));
            p.sendSystemMessage(Component.literal(""));
        }

        level.playSound(null, a.cornerstone, SoundEvents.RAID_HORN.value(),
                SoundSource.HOSTILE, 4.0f, 0.6f);
        Vfx.domainCollapse(level, Vec3.atCenterOf(a.cornerstone), 12.0, Vfx.CURSED);
        startWave(level, a, tier);
        return true;
    }

    private static void startWave(ServerLevel level, Active a, SettlementTier tier) {
        a.wave++;
        boolean last = a.wave >= a.totalWaves;
        int count = 4 + a.wave * 2 + tier.ordinal();

        broadcast(level, a, last
                ? "  §4§lTHE COMMANDER COMES"
                : "  §cWave " + a.wave + " §8of " + a.totalWaves);

        for (int i = 0; i < count; i++) {
            spawnAttacker(level, a, rosterFor(a.wave), false, tier);
        }
        if (last) {
            // If anybody defending here is being hunted by something serious, it leads the assault
            // rather than a nameless ravager. Nothing else in the mod produces a moment like a
            // player's own nemesis walking up their main street at the head of an army.
            Nemesis leader = pickCommander(a);
            if (leader != null && spawnNemesisCommander(level, a, leader, tier)) {
                broadcast(level, a, "  §4" + leader.fullName() + " §7leads them.");
            } else {
                spawnAttacker(level, a, EntityTypes.RAVAGER, true, tier);
            }
        }

        a.bar.setProgress(Math.min(1f, (a.wave - 1) / (float) a.totalWaves));
        a.bar.setName(Component.literal("Siege of " + a.settlementName
                + " §7wave " + a.wave + "/" + a.totalWaves)
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
    }

    /** The strongest dormant grudge held against anybody defending, if it is worth the job. */
    private static Nemesis pickCommander(Active a) {
        if (!a.commanderNemesisId.isEmpty()) return null;
        Nemesis best = null;
        for (String uuid : a.defenders) {
            Nemesis n = NemesisManager.strongestDormantFor(uuid);
            if (n == null) continue;
            // Below Slayer it has not earned the right to lead anything.
            if (n.rank().ordinal() < Nemesis.Rank.SLAYER.ordinal()) continue;
            if (best == null || n.rank().ordinal() > best.rank().ordinal()) best = n;
        }
        return best;
    }

    private static boolean spawnNemesisCommander(ServerLevel level, Active a, Nemesis n,
                                                 SettlementTier tier) {
        BlockPos at = ringPos(level, a);
        if (at == null) return false;
        Mob mob = NemesisManager.spawnAt(level, n, at);
        if (mob == null) return false;

        mob.addTag(ATTACKER_TAG);
        mob.setPersistenceRequired();
        // On top of everything the grudge already carries, it is leading an army.
        var hp = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.setBaseValue(hp.getBaseValue() * (1.0 + tier.ordinal() * 0.15));
            mob.setHealth(mob.getMaxHealth());
        }
        a.attackers.add(mob.getUUID());
        a.commanderNemesisId = n.id;
        Vfx.bloom(level, Vec3.atCenterOf(at));
        return true;
    }

    /** The attackers get nastier as the siege runs rather than simply more numerous. */
    private static EntityType<?> rosterFor(int wave) {
        if (wave <= 2) {
            return RNG.nextBoolean() ? EntityTypes.ZOMBIE : EntityTypes.SKELETON;
        }
        if (wave <= 4) {
            return switch (RNG.nextInt(3)) {
                case 0 -> EntityTypes.PILLAGER;
                case 1 -> EntityTypes.HUSK;
                default -> EntityTypes.STRAY;
            };
        }
        return switch (RNG.nextInt(4)) {
            case 0 -> EntityTypes.VINDICATOR;
            case 1 -> EntityTypes.PILLAGER;
            case 2 -> EntityTypes.WITCH;
            default -> EntityTypes.EVOKER;
        };
    }

    private static void spawnAttacker(ServerLevel level, Active a, EntityType<?> type,
                                      boolean commander, SettlementTier tier) {
        BlockPos at = ringPos(level, a);
        if (at == null) return;
        Entity raw = type.spawn(level, at, EntitySpawnReason.EVENT);
        if (!(raw instanceof Mob mob)) {
            if (raw != null) raw.discard();
            return;
        }

        mob.addTag(ATTACKER_TAG);
        mob.setPersistenceRequired();

        double scale = 1.0 + a.wave * 0.15 + tier.ordinal() * 0.1;
        var hp = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.setBaseValue(hp.getBaseValue() * (commander ? scale * 5.0 : scale));
            mob.setHealth(mob.getMaxHealth());
        }
        var atk = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) atk.setBaseValue(atk.getBaseValue() * (commander ? scale * 1.8 : scale));

        if (commander) {
            mob.setCustomName(Component.literal("Siege Commander")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
            mob.setCustomNameVisible(true);
            var kb = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (kb != null) kb.setBaseValue(1.0);
            Vfx.bloom(level, Vec3.atCenterOf(at));
        }

        a.attackers.add(mob.getUUID());
    }

    /** Attackers come from outside the settlement and walk in, which is what makes it a siege. */
    private static BlockPos ringPos(ServerLevel level, Active a) {
        for (int attempt = 0; attempt < 20; attempt++) {
            double ang = RNG.nextDouble() * Math.PI * 2;
            int x = a.cornerstone.getX() + (int) (Math.cos(ang) * SPAWN_RING);
            int z = a.cornerstone.getZ() + (int) (Math.sin(ang) * SPAWN_RING);
            BlockPos ground = level.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, a.cornerstone.getY(), z));
            if (Math.abs(ground.getY() - a.cornerstone.getY()) > 20) continue;
            if (!level.getBlockState(ground).isAir()) continue;
            return ground;
        }
        return null;
    }

    // ── Running ───────────────────────────────────────────────────────────

    public static void tick(MinecraftServer server) {
        if (ACTIVE.isEmpty()) return;

        Iterator<Map.Entry<String, Active>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Active a = it.next().getValue();
            ServerLevel level = levelOf(server, a);
            Settlement s = SettlementManager.get(a.settlementId);
            if (level == null || s == null) {
                cleanUp(level, a);
                it.remove();
                continue;
            }

            a.ticks++;

            // Prune the dead.
            a.attackers.removeIf(u -> {
                Entity e = level.getEntity(u);
                if (e == null || !e.isAlive()) {
                    a.slain++;
                    return true;
                }
                return false;
            });

            if (a.ticks % 20 != 0) continue;

            // Push whatever is still standing toward the cornerstone.
            Vec3 stone = Vec3.atCenterOf(a.cornerstone);
            int atStone = 0;
            for (UUID u : a.attackers) {
                Entity e = level.getEntity(u);
                if (!(e instanceof Mob mob)) continue;
                if (mob.distanceToSqr(stone) < BREACH_RADIUS * BREACH_RADIUS) {
                    atStone++;
                    continue;
                }
                if (mob.getTarget() == null) {
                    mob.getNavigation().moveTo(stone.x, stone.y, stone.z, 1.0);
                }
            }

            // Somebody standing on the cornerstone with nobody contesting it is how this is lost.
            boolean defended = anyDefenderNear(level, stone);
            if (atStone > 0 && !defended) {
                a.breachTicks++;
                if (a.breachTicks % 5 == 0) {
                    broadcast(level, a, "  §4The cornerstone is being taken. §7"
                            + (BREACH_SECONDS - a.breachTicks) + "s");
                }
                level.sendParticles(ParticleTypes.LARGE_SMOKE, stone.x, stone.y + 1, stone.z,
                        20, 1.5, 1.0, 1.5, 0.02);
            } else if (a.breachTicks > 0) {
                a.breachTicks = Math.max(0, a.breachTicks - 1);
            }

            if (a.breachTicks >= BREACH_SECONDS) {
                lose(level, a, s);
                it.remove();
                continue;
            }

            if (a.attackers.isEmpty()) {
                if (a.wave >= a.totalWaves) {
                    win(level, a, s);
                    it.remove();
                    continue;
                }
                startWave(level, a, tierOf(s));
            }
        }
    }

    private static boolean anyDefenderNear(ServerLevel level, Vec3 stone) {
        for (ServerPlayer p : level.players()) {
            if (p.isDeadOrDying()) continue;
            if (p.distanceToSqr(stone) < (BREACH_RADIUS + 6) * (BREACH_RADIUS + 6)) return true;
        }
        return false;
    }

    // ── Outcomes ──────────────────────────────────────────────────────────

    private static void win(ServerLevel level, Active a, Settlement s) {
        SettlementTier tier = tierOf(s);
        long reward = rewardFor(tier, a.slain);

        // Holding is worth something to the settlement itself, not only to the defenders. This
        // goes on the modifier because the scanner rebuilds appeal from blocks every pass and
        // would otherwise erase it within minutes.
        s.appealModifier += 40;
        s.appeal += 40;

        for (String uuid : a.defenders) {
            ServerPlayer p = playerOf(level, uuid);
            if (p == null) continue;
            CoinManager.giveCoins(p, reward);
            p.sendSystemMessage(Component.literal(""));
            p.sendSystemMessage(Component.literal("  §a§lSIEGE BROKEN"));
            p.sendSystemMessage(Component.literal("  §7" + s.name + " held."));
            p.sendSystemMessage(Component.literal("  §7" + a.slain + " put down"));
            p.sendSystemMessage(Component.literal("  §7Reward: §6" + GuiKit.num(reward) + " coins"));
            p.sendSystemMessage(Component.literal("  §8Appeal rose by 40."));
            p.sendSystemMessage(Component.literal(""));
        }

        level.playSound(null, a.cornerstone, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                SoundSource.PLAYERS, 2.0f, 1.0f);
        Vfx.bloom(level, Vec3.atCenterOf(a.cornerstone));
        SettlementManager.save();
        cleanUp(level, a);
    }

    private static void lose(ServerLevel level, Active a, Settlement s) {
        // The cost is taken out of what the settlement was, which is the only stake that matters.
        int appealLost = Math.min(s.appeal, 120);
        int popLost = Math.max(1, s.population / 5);
        s.appealModifier -= appealLost;
        s.appeal = Math.max(0, s.appeal - appealLost);
        // Population is recounted from live citizens, so the loss has to be real citizens rather
        // than a number that the next count puts straight back.
        CitizenManager.killCitizens(s.id, popLost);
        s.population = Math.max(0, s.population - popLost);

        for (String uuid : a.defenders) {
            ServerPlayer p = playerOf(level, uuid);
            if (p == null) continue;
            p.sendSystemMessage(Component.literal(""));
            p.sendSystemMessage(Component.literal("  §4§lTHE SETTLEMENT FELL"));
            p.sendSystemMessage(Component.literal("  §7" + s.name + " lost §c" + appealLost
                    + " §7appeal and §c" + popLost + " §7of its people."));
            p.sendSystemMessage(Component.literal("  §8It can be rebuilt. It will take a while."));
            p.sendSystemMessage(Component.literal(""));
        }

        level.playSound(null, a.cornerstone, SoundEvents.WITHER_DEATH,
                SoundSource.HOSTILE, 3.0f, 0.5f);
        Vfx.voidRift(level, Vec3.atCenterOf(a.cornerstone));
        SettlementManager.save();
        cleanUp(level, a);
    }

    private static void cleanUp(ServerLevel level, Active a) {
        if (a.bar != null) a.bar.removeAllPlayers();
        if (level == null) return;
        for (UUID u : new ArrayList<>(a.attackers)) {
            Entity e = level.getEntity(u);
            if (e != null) e.discard();
        }
        a.attackers.clear();
    }

    /** Clears every siege on shutdown so no attacker is left standing in a saved world. */
    public static void shutdown(MinecraftServer server) {
        for (Active a : new ArrayList<>(ACTIVE.values())) {
            cleanUp(levelOf(server, a), a);
        }
        ACTIVE.clear();
    }

    private static void broadcast(ServerLevel level, Active a, String message) {
        for (String uuid : a.defenders) {
            ServerPlayer p = playerOf(level, uuid);
            if (p != null) p.sendSystemMessage(Component.literal(message));
        }
    }

    private static ServerPlayer playerOf(ServerLevel level, String uuid) {
        try {
            return level.getServer().getPlayerList().getPlayer(UUID.fromString(uuid));
        } catch (Exception e) {
            return null;
        }
    }

    private static ServerLevel levelOf(MinecraftServer server, Active a) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().identifier().toString().equals(a.dim)) return level;
        }
        return null;
    }

    public static boolean isAttacker(Entity e) {
        return e != null && e.entityTags().contains(ATTACKER_TAG);
    }
}
