package com.political;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.political.settlement.Settlement;
import com.political.settlement.SettlementManager;
import com.political.settlement.SettlementScanner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Spawns and tracks citizen villagers (tags political_citizen + political_stl_&lt;id&gt;),
 * assigns jobs (incl. Sheriff/Deputy from personality), and runs sheriff hostile-clearing.
 * Citizens are re-identified by entity tags when their chunks load. Persisted to citizens.json.
 */
public class CitizenManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CitizenManager.class);


    public static final String TAG_CITIZEN = "political_citizen";
    public static final String TAG_PREFIX = "political_stl_";

    private static final Map<String, Citizen> citizens = new LinkedHashMap<>(); // villagerUuid -> Citizen
    private static Path dataFile;

    private static int tickCounter = 0;
    private static final int SHERIFF_INTERVAL = 20;   // ~1s
    private static final int RECONCILE_INTERVAL = 100; // ~5s
    private static final double SHERIFF_RADIUS = 24.0;
    private static final float SHERIFF_DAMAGE = 6.0f;

    // ── lifecycle ──

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("citizens.json");
        load();
    }

    private static void load() {
        citizens.clear();
        if (dataFile == null || !Files.exists(dataFile)) return;
        try (Reader r = Files.newBufferedReader(dataFile)) {
            Gson g = new GsonBuilder().create();
            Type t = new TypeToken<List<Citizen>>(){}.getType();
            List<Citizen> loaded = g.fromJson(r, t);
            if (loaded != null) {
                for (Citizen c : loaded) {
                    if (c != null && c.uuid != null) citizens.put(c.uuid, c);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load", e);
        }
    }

    public static void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            Gson g = new GsonBuilder().setPrettyPrinting().create();
            try (Writer w = Files.newBufferedWriter(dataFile)) {
                g.toJson(new ArrayList<>(citizens.values()), w);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save", e);
        }
    }

    // ── accessors ──

    public static Citizen getCitizen(String villagerUuid) {
        return citizens.get(villagerUuid);
    }

    public static boolean isCitizen(Villager v) {
        return v.entityTags().contains(TAG_CITIZEN) || citizens.containsKey(v.getUUID().toString());
    }

    /**
     * Removes citizens from a settlement for good, discarding the villagers if they are loaded.
     *
     * <p>Population is recounted from live records on every pass, so anything that is meant to cost
     * a settlement people has to take the records rather than the number. A siege that only
     * decremented the field would be undone by the next count.</p>
     *
     * @return how many were actually removed
     */
    public static int killCitizens(String settlementId, int count) {
        if (settlementId == null || count <= 0) return 0;
        List<Citizen> pool = citizensOf(settlementId);
        int removed = 0;
        for (Citizen c : pool) {
            if (removed >= count) break;
            citizens.remove(c.uuid);
            removed++;
            try {
                if (PoliticalServer.server == null) continue;
                for (ServerLevel level : PoliticalServer.server.getAllLevels()) {
                    net.minecraft.world.entity.Entity e =
                            level.getEntity(java.util.UUID.fromString(c.uuid));
                    if (e != null) {
                        e.discard();
                        break;
                    }
                }
            } catch (Exception ignored) {
                LOGGER.debug("CitizenManager.killCitizens: ignored an error", ignored);
                // A citizen whose villager is already gone still counts as removed.
            }
        }
        if (removed > 0) save();
        return removed;
    }

    public static int countCitizens(String settlementId) {
        int n = 0;
        for (Citizen c : citizens.values()) {
            if (settlementId.equals(c.settlementId)) n++;
        }
        return n;
    }

    public static List<Citizen> citizensOf(String settlementId) {
        List<Citizen> out = new ArrayList<>();
        for (Citizen c : citizens.values()) {
            if (settlementId.equals(c.settlementId)) out.add(c);
        }
        return out;
    }

    // ── spawning ──

    public static Villager spawnWanderInCitizen(ServerLevel world, Settlement s) {
        BlockPos base = s.cornerstonePos();
        // Reading the heightmap and adding an entity both load the chunk they touch, so refuse
        // outright rather than quietly pulling terrain into memory on a caller's behalf.
        if (!world.hasChunkAt(base)) return null;
        int ox = base.getX() + world.getRandom().nextInt(17) - 8;
        int oz = base.getZ() + world.getRandom().nextInt(17) - 8;
        BlockPos spawn = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(ox, base.getY(), oz));

        Villager v = new Villager(EntityTypes.VILLAGER, world);
        v.setPos(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
        v.setPersistenceRequired();
        v.setNoAi(false);

        Citizen citizen = Citizen.fromUuid(v.getUUID(), s.id);
        v.setCustomName(Component.literal(citizen.name).withStyle(ChatFormatting.GRAY));
        v.setCustomNameVisible(true);
        v.addTag(TAG_CITIZEN);
        v.addTag(TAG_PREFIX + s.id);

        world.addFreshEntity(v);

        citizens.put(citizen.uuid, citizen);
        if (!s.citizens().contains(citizen.uuid)) s.citizens().add(citizen.uuid);
        s.population = countCitizens(s.id);
        SettlementManager.save();
        return v;
    }

    // ── tick ──

    public static void tick(MinecraftServer server) {
        tickCounter++;

        if (tickCounter % RECONCILE_INTERVAL == 0) {
            for (Settlement s : SettlementManager.getAllSettlements()) {
                ServerLevel world = SettlementManager.levelFor(server, s);
                if (world == null) continue;
                if (!world.hasChunkAt(s.cornerstonePos())) continue;
                reconcile(world, s);
                assignJobs(s);
            }
        }

        if (tickCounter % SHERIFF_INTERVAL == 0) {
            for (Settlement s : SettlementManager.getAllSettlements()) {
                ServerLevel world = SettlementManager.levelFor(server, s);
                if (world == null) continue;
                if (!world.hasChunkAt(s.cornerstonePos())) continue;
                runSheriff(world, s);
            }
        }
    }

    /** Re-identify tagged villagers near the cornerstone; add any that lack a Citizen entry. */
    private static void reconcile(ServerLevel world, Settlement s) {
        int radius = SettlementScanner.radiusForTier(s.tierEnum());
        AABB box = new AABB(s.cornerstonePos()).inflate(radius);
        String tag = TAG_PREFIX + s.id;
        List<Villager> found = world.getEntitiesOfClass(Villager.class, box,
                v -> v.entityTags().contains(tag));
        for (Villager v : found) {
            String uuid = v.getUUID().toString();
            if (!citizens.containsKey(uuid)) {
                Citizen c = Citizen.fromUuid(v.getUUID(), s.id);
                if (v.getCustomName() != null) c.name = v.getCustomName().getString();
                citizens.put(uuid, c);
            }
            if (!s.citizens().contains(uuid)) s.citizens().add(uuid);
        }
        s.population = countCitizens(s.id);
    }

    /** Sheriff = highest charisma citizen; Deputy = second. Reflected in settlement.leaders. */
    private static void assignJobs(Settlement s) {
        List<Citizen> list = citizensOf(s.id);
        if (list.isEmpty()) {
            s.leaderMap().remove("sheriff");
            s.leaderMap().remove("deputy");
            return;
        }
        list.sort(Comparator.comparingDouble((Citizen c) -> c.charisma).reversed());
        // reset job ranks we manage
        for (Citizen c : list) {
            if (c.jobEnum() == Citizen.CitizenJob.SHERIFF || c.jobEnum() == Citizen.CitizenJob.DEPUTY) {
                c.setJob(Citizen.CitizenJob.CITIZEN);
            }
        }
        Citizen sheriff = list.get(0);
        sheriff.setJob(Citizen.CitizenJob.SHERIFF);
        s.leaderMap().put("sheriff", sheriff.uuid);
        if (list.size() > 1) {
            Citizen deputy = list.get(1);
            deputy.setJob(Citizen.CitizenJob.DEPUTY);
            s.leaderMap().put("deputy", deputy.uuid);
        } else {
            s.leaderMap().remove("deputy");
        }
        // Assign flavor jobs to the rest deterministically from personality.
        Citizen.CitizenJob[] pool = {
                Citizen.CitizenJob.INNKEEPER, Citizen.CitizenJob.BLACKSMITH, Citizen.CitizenJob.SCHOLAR,
                Citizen.CitizenJob.PRIEST, Citizen.CitizenJob.MERCHANT, Citizen.CitizenJob.COURIER,
                Citizen.CitizenJob.TAX_COLLECTOR
        };
        for (int i = 2; i < list.size(); i++) {
            Citizen c = list.get(i);
            if (c.jobEnum() == Citizen.CitizenJob.CITIZEN) {
                int idx = Math.floorMod((int) (c.charisma * 1000 + c.taxTolerance * 100), pool.length);
                c.setJob(pool[idx]);
            }
        }
    }

    private static void runSheriff(ServerLevel world, Settlement s) {
        String sheriffUuid = s.leaderMap().get("sheriff");
        if (sheriffUuid == null) return;

        Entity sheriffEntity = null;
        try {
            sheriffEntity = world.getEntity(UUID.fromString(sheriffUuid));
        } catch (Exception ignored) { LOGGER.debug("CitizenManager.runSheriff: ignored an error", ignored); }
        if (!(sheriffEntity instanceof Villager sheriff) || !sheriff.isAlive()) return;

        AABB box = sheriff.getBoundingBox().inflate(SHERIFF_RADIUS);
        List<Monster> hostiles = world.getEntitiesOfClass(Monster.class, box, m -> m.isAlive());
        if (hostiles.isEmpty()) return;

        // Attack the nearest hostile.
        Monster target = null;
        double bestSq = Double.MAX_VALUE;
        for (Monster m : hostiles) {
            double d2 = m.distanceToSqr(sheriff);
            if (d2 < bestSq) { bestSq = d2; target = m; }
        }
        if (target == null) return;

        target.hurtServer(world, world.damageSources().magic(), SHERIFF_DAMAGE);
        world.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                target.getX(), target.getY() + 1.0, target.getZ(), 6, 0.3, 0.3, 0.3, 0.02);
        world.playSound(null, sheriff.blockPosition(),
                net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP,
                net.minecraft.sounds.SoundSource.NEUTRAL, 0.6f, 1.0f);
    }

    /** Optionally give a spawned citizen a vanilla profession matching its job (cosmetic). */
    public static void applyProfession(Villager v, Citizen.CitizenJob job) {
        net.minecraft.resources.ResourceKey<VillagerProfession> prof = switch (job) {
            case BLACKSMITH -> VillagerProfession.WEAPONSMITH;
            case SCHOLAR -> VillagerProfession.LIBRARIAN;
            case PRIEST -> VillagerProfession.CLERIC;
            case MERCHANT -> VillagerProfession.FARMER;
            case INNKEEPER -> VillagerProfession.BUTCHER;
            case TAX_COLLECTOR -> VillagerProfession.CARTOGRAPHER;
            case SHERIFF, DEPUTY -> VillagerProfession.TOOLSMITH;
            default -> VillagerProfession.NITWIT;
        };
        try {
            v.setVillagerData(v.getVillagerData()
                    .withProfession(BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(prof)));
        } catch (Exception ignored) { LOGGER.debug("CitizenManager.applyProfession: ignored an error", ignored); }
    }
}
