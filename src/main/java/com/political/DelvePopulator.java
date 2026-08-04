package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Fills a built floor: what lives in it, and what is hidden in it.
 *
 * <p>Mobs are placed per room rather than left to natural spawning, because the realm has no
 * skylight and no spawn rules and a delve that populates itself is a delve that populates
 * itself unevenly. Placing them means a room holds what its role says it holds.</p>
 *
 * <p>Secrets are the part worth getting right. In Skyblock a secret is a thing you had to look for,
 * not a thing you walked past, so these go in corners, behind the room's own geometry, and never in
 * the middle of the floor. Finding them is most of the score.</p>
 */
public final class DelvePopulator {

    private DelvePopulator() {}

    /** Tag on everything this class spawns, so cleanup never has to guess. */
    public static final String MOB_TAG = "political_delve_mob";
    /** On the elites, so the drop table can pay them properly. */
    public static final String ELITE_TAG = "political_delve_elite";

    /** Where each run's secrets are, keyed by packed position. */
    private static final Map<UUID, Map<Long, BlockPos>> SECRETS = new HashMap<>();
    /** Which mobs belong to which run. */
    private static final Map<UUID, List<UUID>> SPAWNED = new HashMap<>();

    // ── Heads ─────────────────────────────────────────────────────────────
    //
    // Delve mobs wear a head so they read as delve mobs at a glance rather than as the same
    // zombie you meet on the surface. A head renders on a vanilla client with nothing installed,
    // which is the only custom look a server side mod can actually deliver.

    private static final String HEAD_LOST = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQxNmI0ZTQ5MGY4YzM4YzE4MWM3ZTU5MjNlYWM3MjhiMzA3ZDQ3ZDZmMzE5N2NlNTFlMjJlZjM5MjA1YWEwOSJ9fX0=";
    private static final String HEAD_WARDEN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDRjZDBhMGRmMTVjMWIxYzc0ZTMxYzEwMzhkYzY1NGY4YmZmNGRlNTRjNWM5MjRlNzQwYTMzMGQwOTY2ZjNjOCJ9fX0=";
    private static final String HEAD_ELITE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzYzMjkzZmU1NmFiMjJhOTM3ZjhkMGFmOTM5NGMyYzMzODdiYzdkNmM3MTA0ODM5YzBlMTZjNGZlNzk5MzM3In19fQ==";

    /** A player head carrying a texture, which every vanilla client renders unaided. */
    public static ItemStack head(String texture) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        CompoundTag nbt = new CompoundTag();
        CompoundTag owner = new CompoundTag();
        CompoundTag props = new CompoundTag();
        CompoundTag textures = new CompoundTag();
        textures.putString("Value", texture);
        props.put("textures", textures);
        owner.put("properties", props);
        owner.putString("Id", UUID.nameUUIDFromBytes(
                texture.getBytes(StandardCharsets.UTF_8)).toString());
        nbt.put("SkullOwner", owner);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        return stack;
    }

    // ── Populating ────────────────────────────────────────────────────────

    public static void populate(ServerLevel level, DelveRun run) {
        Random rng = new Random(run.seed ^ 0x5EC2E7L);
        Map<Long, BlockPos> secrets = new HashMap<>();
        List<UUID> mobs = new ArrayList<>();

        for (DelveBuilder.Placed p : run.layout.rooms) {
            for (int i = 0; i < p.kind.mobs; i++) {
                Mob m = spawnMob(level, run, p, rng, false);
                if (m != null) mobs.add(m.getUUID());
            }
            if (p.kind == DelveRoomKind.MINIBOSS) {
                Mob m = spawnMob(level, run, p, rng, true);
                if (m != null) mobs.add(m.getUUID());
            }
            if (p.kind == DelveRoomKind.TREASURE) {
                placeChest(level, p, run.floor, rng);
            }
        }

        // Secrets are spread across the floor rather than piled into whichever rooms happened to
        // allow them, so a run always has them somewhere worth walking to.
        List<DelveBuilder.Placed> candidates = new ArrayList<>();
        for (DelveBuilder.Placed p : run.layout.rooms) {
            for (int i = 0; i < p.kind.secretSlots; i++) candidates.add(p);
        }
        java.util.Collections.shuffle(candidates, rng);
        int want = Math.min(run.floor.secretCount(), candidates.size());
        for (int i = 0; i < want; i++) {
            BlockPos at = hideSecret(level, candidates.get(i), rng);
            if (at != null) secrets.put(at.asLong(), at);
        }

        run.secretsTotal = secrets.size();
        SECRETS.put(run.id, secrets);
        SPAWNED.put(run.id, mobs);
    }

    /**
     * Puts one mob in a room.
     *
     * <p>Scaled by floor depth so the seventh floor is not the first floor with more walking, and
     * tagged so cleanup can find it later without scanning for anything that looks hostile.</p>
     */
    private static Mob spawnMob(ServerLevel level, DelveRun run, DelveBuilder.Placed room,
                                Random rng, boolean elite) {
        BlockPos spot = scatter(room, rng);
        if (spot == null) return null;

        var type = elite ? run.floor.eliteMob : run.floor.commonMob;
        Mob m;
        try {
            var made = type.create(level, EntitySpawnReason.EVENT);
            if (!(made instanceof Mob mob)) return null;
            m = mob;
        } catch (Exception e) {
            return null;
        }

        m.setPos(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5);
        m.setPersistenceRequired();
        m.addTag(MOB_TAG);
        if (elite) m.addTag(ELITE_TAG);
        m.addTag(run.id.toString());

        float scale = run.floor.mobScale();
        try {
            var hp = m.getAttribute(Attributes.MAX_HEALTH);
            if (hp != null) {
                hp.setBaseValue(hp.getBaseValue() * scale * (elite ? 3.0 : 1.0));
                m.setHealth(m.getMaxHealth());
            }
            var dmg = m.getAttribute(Attributes.ATTACK_DAMAGE);
            if (dmg != null) {
                dmg.setBaseValue(dmg.getBaseValue() * scale * (elite ? 1.6 : 1.0));
            }
        } catch (Exception ignored) {
            // A mob without those attributes is still a mob. It just fights at stock strength.
        }

        m.setItemSlot(EquipmentSlot.HEAD, head(elite ? HEAD_ELITE : HEAD_LOST));
        try {
            m.setDropChance(EquipmentSlot.HEAD, 0f);
        } catch (Exception ignored) {
            // Cosmetic only.
        }
        m.setCustomName(Component.literal(elite
                        ? run.floor.display.replace("The ", "") + " Warden"
                        : "Lost " + run.floor.commonMob.getDescription().getString())
                .withStyle(elite ? ChatFormatting.LIGHT_PURPLE : run.floor.colour));
        m.setCustomNameVisible(elite);

        level.addFreshEntity(m);
        return m;
    }

    /** A random standable spot inside a room, or null when the room has no floor to stand on. */
    private static BlockPos scatter(DelveBuilder.Placed room, Random rng) {
        for (int tries = 0; tries < 12; tries++) {
            int x = room.origin.getX() + 2 + rng.nextInt(Math.max(1, room.size.getX() - 4));
            int z = room.origin.getZ() + 2 + rng.nextInt(Math.max(1, room.size.getZ() - 4));
            return new BlockPos(x, room.floorY + 1, z);
        }
        return null;
    }

    /** A chest of floor appropriate loot, standing in the middle of a treasure room. */
    private static void placeChest(ServerLevel level, DelveBuilder.Placed room,
                                   DelveDepth floor, Random rng) {
        BlockPos at = new BlockPos(room.origin.getX() + room.size.getX() / 2,
                room.floorY + 1, room.origin.getZ() + room.size.getZ() / 2);
        level.setBlock(at, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
            for (int i = 0; i < 3 + rng.nextInt(4); i++) {
                ItemStack loot = new ItemStack(floor.icon, 1 + rng.nextInt(3));
                chest.setItem(rng.nextInt(27), loot);
            }
        }
    }

    /**
     * Hides one secret.
     *
     * <p>In a corner, at floor level, marked with the floor's own light block so it is findable but
     * not obvious. A secret you can see from the doorway is not a secret, and one you could never
     * find is a waste of a room.</p>
     */
    private static BlockPos hideSecret(ServerLevel level, DelveBuilder.Placed room, Random rng) {
        int[][] corners = {
                {2, 2}, {room.size.getX() - 3, 2},
                {2, room.size.getZ() - 3}, {room.size.getX() - 3, room.size.getZ() - 3}
        };
        int[] c = corners[rng.nextInt(corners.length)];
        BlockPos at = new BlockPos(room.origin.getX() + c[0], room.floorY + 1,
                room.origin.getZ() + c[1]);
        try {
            level.setBlock(at, Blocks.SCULK_SHRIEKER.defaultBlockState(), 3);
        } catch (Exception e) {
            return null;
        }
        return at;
    }

    // ── During the run ────────────────────────────────────────────────────

    /** Whether this block is one of the run's secrets, and claims it if so. */
    public static boolean claimSecret(DelveRun run, BlockPos at, ServerPlayer by) {
        Map<Long, BlockPos> secrets = SECRETS.get(run.id);
        if (secrets == null || !secrets.containsKey(at.asLong())) return false;
        if (!run.takenSecrets.add(at.asLong())) return false;

        run.secretsFound++;
        if (by.level() instanceof ServerLevel level) {
            level.setBlock(at, Blocks.AIR.defaultBlockState(), 3);
            level.playSound(null, at, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7f, 1.6f);
        }
        for (ServerPlayer p : run.online()) {
            p.sendSystemMessage(Component.literal("§b✦ §7" + by.getName().getString()
                    + " found a secret. §f" + run.secretsFound + "/" + run.secretsTotal));
        }
        CoinManager.giveCoins(by, 2_500L * run.floor.tier);
        DelveDrops.onSecret(by, run);
        return true;
    }

    /**
     * Checks which rooms have been emptied.
     *
     * <p>Run on the slow tick rather than off every death, because a mob can die to fire, fall
     * damage or another mob, and hooking every one of those is more surface than counting is
     * worth.</p>
     */
    public static void tick(MinecraftServer server, DelveRun run) {
        if (server.getTickCount() % 40 != 0) return;
        ServerLevel level = DelveManager.realm(server);
        if (level == null) return;

        for (DelveBuilder.Placed p : run.layout.rooms) {
            if (!p.kind.countsForClear() || p.cleared) continue;
            AABB box = new AABB(
                    p.origin.getX(), p.floorY - 2, p.origin.getZ(),
                    p.origin.getX() + p.size.getX(), p.floorY + p.size.getY(),
                    p.origin.getZ() + p.size.getZ());
            List<Mob> left = level.getEntitiesOfClass(Mob.class, box,
                    m -> m.isAlive() && m.entityTags().contains(MOB_TAG)
                            && m.entityTags().contains(run.id.toString()));
            if (!left.isEmpty()) continue;

            p.cleared = true;
            run.roomsCleared++;
            for (ServerPlayer sp : run.online()) {
                sp.sendSystemMessage(Component.literal(run.floor.colour + "✔ §7Room cleared. §f"
                        + run.roomsCleared + "/" + run.roomsTotal));
            }
            if (run.roomsDone()) {
                for (ServerPlayer sp : run.online()) {
                    sp.sendSystemMessage(Component.literal(""));
                    sp.sendSystemMessage(Component.literal("  §4§lTHE WAY DOWN OPENS"));
                    sp.sendSystemMessage(Component.literal("  §7" + run.floor.bossName
                            + " is waiting."));
                    sp.sendSystemMessage(Component.literal(""));
                }
            }
        }
    }

    /** Removes everything this run put in the world. */
    public static void despawn(DelveRun run) {
        List<UUID> mobs = SPAWNED.remove(run.id);
        SECRETS.remove(run.id);
        if (mobs == null || PoliticalServer.server == null) return;
        ServerLevel level = DelveManager.realm(PoliticalServer.server);
        if (level == null) return;
        for (UUID u : mobs) {
            var e = level.getEntity(u);
            if (e != null) e.discard();
        }
    }

    /** Counts a kill toward the run's tally. */
    public static void onKill(ServerPlayer by, LivingEntity victim) {
        DelveRun run = DelveManager.runOf(by);
        if (run == null) return;
        if (!victim.entityTags().contains(MOB_TAG)) return;
        run.mobsKilled++;
    }
}
