package com.political.power.game;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.political.core.Clock;
import com.political.power.PowerSettings;
import com.political.power.cast.CastDecision;
import com.political.power.cast.CastPipeline;
import com.political.power.formula.Innate;
import com.political.power.formula.Reroll;
import com.political.power.formula.Tiers;
import com.political.power.formula.Vow;
import com.political.power.formula.VowSet;
import com.political.power.model.AbilitySpec;
import com.political.power.model.FailureReason;
import com.political.power.model.PowerCatalog;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.permission.v1.PermissionContext;
import net.fabricmc.fabric.api.permission.v1.PermissionEvents;
import net.fabricmc.fabric.api.permission.v1.PermissionNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A development check of the power system with Fabric fake players, for runs where no client is available. It is off
 * unless the server starts with {@code -Dpoliticalserver.powerSmoke=true}. It follows the owner's power test script
 * (program section 15, items 2 to 11) as far as a server without a client can: it buys a school tree, casts every
 * kind of ability at a real mob, tests refusals, vows, death, a second player, the Domain and the permission of the
 * admin command, builds every screen, logs one line per failed check and a summary, and removes the fake players'
 * state again. It proves the pieces work together on a real server; it does not replace a human playing with a
 * vanilla client (HANDOFF.md, M2.8).
 */
public final class PowerSmoke {

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerSmoke.class);
    public static final String PROPERTY = "politicalserver.powerSmoke";
    private static final UUID SECOND = UUID.fromString("00000000-0000-0000-0000-00000000b002");

    /** Whether the fake players count as administrators for the permission node (switched off to test the denial). */
    private static volatile boolean adminGranted = true;

    private int checks;
    private final List<String> failures = new ArrayList<>();
    /** Game time of this check; it only moves when {@link #advance} says so, so cooldowns can be tested in one tick. */
    private long ticks;

    private PowerSmoke() {
    }

    public static boolean requested() {
        String value = System.getProperty(PROPERTY);
        return value != null && !value.equalsIgnoreCase("false");
    }

    /** {@code write} and {@code read} are the two halves of the restart check; anything else is the full run. */
    private static String mode() {
        String value = System.getProperty(PROPERTY, "true");
        return value.equalsIgnoreCase("write") || value.equalsIgnoreCase("read") ? value.toLowerCase(java.util.Locale.ROOT) : "full";
    }

    /**
     * Runs the check once the spawn chunk ticks entities, because a mob only exists for the ray cast then. With no
     * player online nothing keeps that chunk ticking, so the check forces it for the length of the run and lets it go
     * again. Gives up after 400 ticks and says so (an empty server pauses itself after a minute).
     */
    public static void schedule() {
        boolean[] finished = {false};
        boolean[] forced = {false};
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (finished[0]) {
                return;
            }
            ServerLevel world = server.overworld();
            BlockPos spawn = world.getRespawnData().pos();
            if (!forced[0]) {
                forced[0] = true;
                world.setChunkForced(spawn.getX() >> 4, spawn.getZ() >> 4, true);
                return;
            }
            if (!world.isPositionEntityTicking(spawn)) {
                if (server.getTickCount() > 400) {
                    finished[0] = true;
                    world.setChunkForced(spawn.getX() >> 4, spawn.getZ() >> 4, false);
                    LOGGER.error("Power smoke check FAILED: the spawn chunk never started ticking entities");
                }
                return;
            }
            finished[0] = true;
            try {
                run(server);
            } finally {
                world.setChunkForced(spawn.getX() >> 4, spawn.getZ() >> 4, false);
            }
        });
    }

    private static void run(MinecraftServer server) {
        PowerSmoke smoke = new PowerSmoke();
        try {
            smoke.sequence(server);
        } catch (Exception e) {
            smoke.failures.add("threw " + e);
            LOGGER.error("Power smoke check threw", e);
        }
        if (smoke.failures.isEmpty()) {
            LOGGER.info("Power smoke check passed: {} checks", smoke.checks);
        } else {
            LOGGER.error("Power smoke check FAILED: {} of {} checks: {}", smoke.failures.size(), smoke.checks, smoke.failures);
        }
    }

    private void check(String what, boolean ok) {
        checks++;
        if (!ok) {
            failures.add(what);
        }
    }

    private void advance(long byTicks) {
        ticks += byTicks;
    }

    private long now() {
        return Clock.ticks();
    }

    private void sequence(MinecraftServer server) {
        long wallStart = Clock.wallMillis();
        ticks = server.getTickCount();
        Clock.useSource(new Clock.Source() {
            @Override
            public long ticks() {
                return ticks;
            }

            @Override
            public long wallMillis() {
                return wallStart + (ticks - server.getTickCount()) * Clock.MS_PER_TICK;
            }
        });
        boolean pvp = server.getGlobalGameRules().get(GameRules.PVP);
        try {
            everything(server);
        } finally {
            Clock.reset();
            server.getGlobalGameRules().set(GameRules.PVP, pvp, server);
        }
    }

    // ── the sequence ────────────────────────────────────────────────────────────────────────

    /**
     * Item 8 of the owner's script, the part a server can do alone: state written before a restart is there after it.
     * Run once with {@code write} (leaves the fake player's state in power.json), stop the server, run again with
     * {@code read} (checks it came back through the real file, then removes it).
     */
    private void restart(MinecraftServer server, boolean write) {
        FakePlayer fake = FakePlayer.get(server.overworld());
        String uuid = fake.getUUID().toString();
        if (write) {
            PowerService.reset(uuid);
            PowerService.onJoin(fake);
            check("a technique was drawn at join", PowerService.state(uuid).innate != null);
            check("awaken", PowerService.awaken(fake));
            check("choose Blue (a manual)", PowerService.rerollTo(fake, PowerCatalog.BLUE) == PowerService.RerollResult.OK);
            PowerService.state(uuid).uses.put(PowerCatalog.BLUE, 20);
            PowerService.addShards(fake, 60);
            PowerService.grantRerollCharge(fake);
            check("take a vow", PowerService.acceptVow(fake, Vow.NO_SPRINT) == VowSet.AcceptResult.ACCEPTED);
            PowerService.setResource(uuid, 33.0);
            LOGGER.info("Power smoke: state written for {} (Blue, awakened, 20 uses, 60 shards, 1 charge, vow no_sprint, resource 33)", uuid);
            return;
        }
        check("the innate technique came back", PowerCatalog.BLUE.equals(PowerService.state(uuid).innate));
        check("it is still awake", PowerService.awakened(uuid));
        check("tier 2 came back (20 uses is Adept)", PowerService.tier(uuid) == 2);
        check("the shards came back", PowerService.state(uuid).shards == 60 && PowerService.state(uuid).shardsEarnedTotal == 60);
        check("the reroll charge came back", PowerService.state(uuid).rerollCharges == 1 && PowerService.state(uuid).rerolls == 1);
        check("Blue is equipped", PowerCatalog.BLUE.equals(PowerService.state(uuid).equipped));
        check("the resource came back", PowerService.resource(uuid) == 33.0);
        PowerService.onJoin(fake);
        check("the vow is active again after joining", PowerService.vowsOf(uuid).isActive(Vow.NO_SPRINT));
        check("no cooldown survives a restart", PowerService.cooldownRemaining(uuid, PowerCatalog.abilities(PowerCatalog.BLUE).get(0)) == 0);
        screens(fake);
        PowerService.onLogout(fake);
        PowerService.reset(uuid);
    }

    private void everything(MinecraftServer server) {
        String mode = mode();
        if (!mode.equals("full")) {
            restart(server, mode.equals("write"));
            return;
        }
        ServerLevel world = server.overworld();
        FakePlayer fake = FakePlayer.get(world);
        FakePlayer other = FakePlayer.get(world, new GameProfile(SECOND, "[PowerSmokeB]"));
        String uuid = fake.getUUID().toString();
        String otherUuid = other.getUUID().toString();
        PermissionEvents.ON_REQUEST.register(new PermissionEvents.OnRequest() {
            @Override
            public <T> T handlePermissionRequest(PermissionContext context, PermissionNode<T> node) {
                boolean fakePlayer = FakePlayer.DEFAULT_UUID.equals(context.uuid()) || SECOND.equals(context.uuid());
                return adminGranted && PowerPermissions.ADMIN.equals(node.key()) && fakePlayer ? node.cast(Boolean.TRUE) : null;
            }
        });

        PowerService.reset(uuid);
        PowerService.reset(otherUuid);
        PowerService.onJoin(fake);
        PowerService.onJoin(other);

        progression(fake, uuid);
        screens(fake);
        barrierAndResource(fake, uuid);
        swapHandsInput(fake, uuid);

        BlockPos spawn = world.getRespawnData().pos();
        double x = spawn.getX() + 0.5;
        double z = spawn.getZ() + 0.5;
        world.getChunk(spawn);
        check("the spawn chunk ticks entities", world.isPositionEntityTicking(spawn));
        fake.snapTo(x, 300.0, z, 0.0f, 0.0f);
        Entity spawned = EntityTypes.ZOMBIE.create(world, EntitySpawnReason.MOB_SUMMONED);
        check("a zombie can be created", spawned instanceof Mob);
        if (spawned instanceof Mob dummy) {
            dummy.snapTo(x, 300.0, z + 9.0, 180.0f, 0.0f);
            dummy.setNoAi(true);
            check("the zombie joins the world", world.addFreshEntity(dummy));
            try {
                aimed(server, world, fake, uuid, dummy, x, z);
                secondPlayer(server, world, fake, uuid, other, otherUuid, dummy, x, z);
            } finally {
                dummy.discard();
            }
        }

        vowsAndDeath(fake, uuid);
        adminSurface(server, fake, uuid);

        // an administrator can reset; the old state is kept
        int quarantineBefore = PowerService.quarantineSize();
        PowerService.reset(uuid);
        check("reset keeps a copy", PowerService.quarantineSize() == quarantineBefore + 1);
        check("reset leaves no technique until the next join", PowerService.state(uuid).innate == null);
        PowerService.onJoin(fake);
        check("a new one is drawn at the next join", PowerService.state(uuid).innate != null);
        screens(fake);

        PowerService.onLogout(fake);
        PowerService.onLogout(other);
        PowerService.reset(uuid);
        PowerService.reset(otherUuid);
    }

    /** Script item 1 as far as a server goes: the technique rolled at join, awakening, mastery, rerolls. */
    private void progression(FakePlayer fake, String uuid) {
        String first = PowerService.state(uuid).innate;
        check("a technique was drawn at join (" + first + ")", first != null && Innate.pool().contains(first));
        check("the drawn technique is equipped", first != null && first.equals(PowerService.state(uuid).equipped));
        check("it sleeps at first", !PowerService.awakened(uuid) && PowerService.tier(uuid) == 0);
        CastDecision asleep = decision(fake);
        check("a sleeping technique cannot be cast (" + asleep + ")", asleep instanceof CastDecision.Rejected r && r.reason() == FailureReason.NOT_UNLOCKED);
        check("awaken", PowerService.awaken(fake));
        check("awakening twice does nothing", !PowerService.awaken(fake));
        check("awake means tier 1", PowerService.tier(uuid) == 1);

        // a technique chosen by a manual, then mastered
        check("choose Barrier (a manual)", PowerService.rerollTo(fake, PowerCatalog.BARRIER) == PowerService.RerollResult.OK
                || PowerCatalog.BARRIER.equals(first));
        check("the same technique again is refused", PowerService.rerollTo(fake, PowerCatalog.BARRIER) == PowerService.RerollResult.SAME_TECHNIQUE);
        check("mastery starts again", PowerService.tier(uuid) == 1);
        check("Domain is locked below Master", !PowerService.holds(uuid, PowerCatalog.DOMAIN));
        PowerService.state(uuid).uses.put(PowerCatalog.BARRIER, 100);
        check("tier 4 at Master", PowerService.tier(uuid) == Tiers.MAX_TIER);
        check("holds Barrier", PowerService.holds(uuid, PowerCatalog.BARRIER));
        check("Domain unlocks at Master", PowerService.holds(uuid, PowerCatalog.DOMAIN));
        check("does not hold Blue", !PowerService.holds(uuid, PowerCatalog.BLUE));

        // rerolls: shards, then a charge
        PowerService.state(uuid).shards = 0;
        check("a shard reroll without shards is refused", PowerService.rerollWithShards(fake) == PowerService.RerollResult.NOT_ENOUGH_SHARDS);
        PowerService.addShards(fake, 200);
        int cost = PowerService.rerollShardCost(uuid);
        check("the awakened price is " + cost, cost == Reroll.shardCost(PowerService.state(uuid).rerolls, true));
        int shardsBefore = PowerService.state(uuid).shards;
        check("a shard reroll works", PowerService.rerollWithShards(fake) == PowerService.RerollResult.OK);
        check("it cost " + cost + " shards", PowerService.state(uuid).shards == shardsBefore - cost);
        check("it gave a different technique", !PowerCatalog.BARRIER.equals(PowerService.state(uuid).innate));
        check("mastery starts again after a reroll", PowerService.tier(uuid) == 1);
        check("a reroll without a charge is refused", PowerService.rerollWithCharge(fake) == PowerService.RerollResult.NO_CHARGE);
        PowerService.grantRerollCharge(fake);
        check("a reroll with a charge works", PowerService.rerollWithCharge(fake) == PowerService.RerollResult.OK
                && PowerService.state(uuid).rerollCharges == 0);
        check("the reroll count is 3", PowerService.state(uuid).rerolls == 3);
        // back to the technique the rest of the check uses, mastered
        PowerService.rerollTo(fake, PowerCatalog.BARRIER);
        PowerService.state(uuid).uses.put(PowerCatalog.BARRIER, 100);
        check("tier 4 again", PowerService.tier(uuid) == Tiers.MAX_TIER);
        PowerService.learn(fake, PowerCatalog.REVERSE);
    }

    private void barrierAndResource(FakePlayer fake, String uuid) {
        check("equip Barrier", PowerService.equip(fake, PowerCatalog.BARRIER));
        check("Barrier is selected", PowerService.selected(uuid) != null && "barrier".equals(PowerService.selected(uuid).id()));
        double before = PowerService.resource(uuid);
        PowerService.cast(fake);
        double after = PowerService.resource(uuid);
        check("Barrier spent " + PowerSettings.BARRIER_COST.get() + " (was " + before + ", now " + after + ")",
                Math.abs((before - after) - PowerSettings.BARRIER_COST.get()) < 0.01);
        check("Barrier cooldown runs", PowerService.cooldownRemaining(uuid, PowerService.selected(uuid)) > 0);
        check("Barrier absorbs a hit", PowerService.absorbsDamage(fake, fake.damageSources().generic()));
        check("Barrier does not stop the void", !PowerService.absorbsDamage(fake, fake.damageSources().fellOutOfWorld()));
        double beforeSecond = PowerService.resource(uuid);
        PowerService.cast(fake);
        check("a second cast at once costs nothing", Math.abs(PowerService.resource(uuid) - beforeSecond) < 0.01);
        advance(PowerSettings.BARRIER_DURATION_TICKS.get() + 1L);
        check("Barrier ends after its duration", !PowerService.absorbsDamage(fake, fake.damageSources().generic()));
        PowerService.cast(fake);
        check("Barrier is still on cooldown after its duration", Math.abs(PowerService.resource(uuid) - beforeSecond) < 0.01);

        // the resource comes back after the delay, never above the maximum
        double spent = PowerService.resource(uuid);
        advance(PowerSettings.RESOURCE_REGEN_DELAY_TICKS.get() + 200L);
        PowerService.tickPlayer(fake);
        check("resource regenerates (" + spent + " to " + PowerService.resource(uuid) + ")", PowerService.resource(uuid) > spent);
        check("resource never passes the maximum", PowerService.resource(uuid) <= PowerService.maxResource(uuid));
    }

    /**
     * The cast input: the swap-hands packet of a sneaking player with a technique equipped is turned into a cast by
     * {@code SwapHandsCastMixin}. The packet goes through the fake player's real packet listener, so the mixin runs.
     */
    private void swapHandsInput(FakePlayer fake, String uuid) {
        advance(PowerSettings.BARRIER_COOLDOWN_TICKS.get() + 1L);
        PowerService.equip(fake, PowerCatalog.BARRIER);
        PowerService.setResource(uuid, PowerService.maxResource(uuid));
        ServerboundPlayerActionPacket swap = new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN);
        double before = PowerService.resource(uuid);

        fake.setShiftKeyDown(false);
        fake.connection.handlePlayerAction(swap);
        check("swap-hands without sneaking is not a cast", PowerService.resource(uuid) == before);

        fake.setShiftKeyDown(true);
        fake.connection.handlePlayerAction(swap);
        check("sneak and swap-hands casts the selected ability (" + before + " to " + PowerService.resource(uuid) + ")",
                Math.abs((before - PowerService.resource(uuid)) - PowerSettings.BARRIER_COST.get()) < 0.01);

        PowerService.unequip(uuid);
        check("without a technique the key press is left alone", !PowerInput.onSwapHands(fake));
        fake.setShiftKeyDown(false);
        advance(PowerSettings.BARRIER_COOLDOWN_TICKS.get() + 1L);
    }

    /** Script items 2 to 5 and 12: Blue, Red, a wall, Cursed Blast, Reverse on self, Black Flash. */
    private void aimed(MinecraftServer server, ServerLevel world, FakePlayer fake, String uuid, Mob dummy, double x, double z) {
        check("learn Blue", PowerService.learn(fake, PowerCatalog.BLUE) == PowerService.LearnResult.OK);
        check("learn Red", PowerService.learn(fake, PowerCatalog.RED) == PowerService.LearnResult.OK);
        check("learn Cursed Blast", PowerService.learn(fake, PowerCatalog.CURSED_BLAST) == PowerService.LearnResult.OK);

        // item 2: Blue at a mob nine blocks away pulls it toward the caster and costs resource
        castOf(fake, uuid, PowerCatalog.BLUE, "Blue", PowerSettings.BLUE_COST.get(), dummy);
        check("Blue pulls the target toward the caster (dz " + dummy.getDeltaMovement().z + ")", dummy.getDeltaMovement().z < 0.0);

        // item 3: Blue again is on cooldown (after the global cooldown, inside Blue's own)
        advance(PowerSettings.GLOBAL_COOLDOWN_TICKS.get() + 1L);
        double beforeAgain = PowerService.resource(uuid);
        CastDecision again = decision(fake);
        PowerService.cast(fake);
        check("Blue again is ON_COOLDOWN (" + again + ")", again instanceof CastDecision.Rejected r && r.reason() == FailureReason.ON_COOLDOWN && r.detail() > 0);
        check("a refused cast costs nothing", Math.abs(PowerService.resource(uuid) - beforeAgain) < 0.01);

        // a target beyond the range
        advance(PowerSettings.BLUE_COOLDOWN_TICKS.get() + 1L);
        dummy.snapTo(x, 300.0, z + PowerSettings.BLUE_RANGE.get() + 3.0, 180.0f, 0.0f);
        CastDecision far = decision(fake);
        check("Blue beyond its range is OUT_OF_RANGE (" + far + ")", far instanceof CastDecision.Rejected r && r.reason() == FailureReason.OUT_OF_RANGE);

        // item 4: Red through a wall is refused for line of sight; with the wall gone it works and pushes away
        dummy.snapTo(x, 300.0, z + 5.0, 180.0f, 0.0f);
        BlockPos wall = BlockPos.containing(x, 301.0, z + 2.0);
        world.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        PowerService.equip(fake, PowerCatalog.RED);
        PowerService.setResource(uuid, PowerService.maxResource(uuid));
        double beforeWall = PowerService.resource(uuid);
        CastDecision walled = decision(fake);
        PowerService.cast(fake);
        check("Red through a wall is NO_LINE_OF_SIGHT (" + walled + ")", walled instanceof CastDecision.Rejected r && r.reason() == FailureReason.NO_LINE_OF_SIGHT);
        check("nothing was spent behind the wall", Math.abs(PowerService.resource(uuid) - beforeWall) < 0.01);
        world.setBlockAndUpdate(wall, Blocks.AIR.defaultBlockState());
        castOf(fake, uuid, PowerCatalog.RED, "Red", PowerSettings.RED_COST.get(), dummy);
        check("Red pushes the target away from the caster (dz " + dummy.getDeltaMovement().z + ")", dummy.getDeltaMovement().z > 0.0);
        advance(PowerSettings.RED_COOLDOWN_TICKS.get() + 1L);

        // Cursed Blast hurts
        dummy.damageCooldownTime = 0;
        float healthBefore = dummy.getHealth();
        castOf(fake, uuid, PowerCatalog.CURSED_BLAST, "Cursed Blast", PowerSettings.BLAST_COST.get(), dummy);
        check("Cursed Blast hurts the target (" + healthBefore + " to " + dummy.getHealth() + ")", dummy.getHealth() < healthBefore);
        advance(PowerSettings.BLAST_COOLDOWN_TICKS.get() + 1L);

        // item 5: Reverse refuses a mob (players only); with nothing in the crosshair it heals the caster by what is
        // missing up to its cap, for 2 resource per health, and never above full health
        PowerService.equip(fake, PowerCatalog.REVERSE);
        PowerService.setResource(uuid, PowerService.maxResource(uuid));
        CastDecision onMob = decision(fake);
        check("Reverse refuses a mob (" + onMob + ")", onMob instanceof CastDecision.Rejected r && r.reason() == FailureReason.TARGET_INVALID);
        fake.setXRot(-90.0f);
        fake.setHealth(4.0f);
        double resourceBefore = PowerService.resource(uuid);
        PowerService.cast(fake);
        float healed = fake.getHealth() - 4.0f;
        check("Reverse heals the caster (4.0 to " + fake.getHealth() + ")", healed > 0.0f);
        check("Reverse heals at most its cap", healed <= PowerSettings.REVERSE_MAX_HEAL.get() + 0.01);
        check("Reverse costs resource for what it healed (" + resourceBefore + " to " + PowerService.resource(uuid) + ")",
                Math.abs((resourceBefore - PowerService.resource(uuid)) - healed * PowerSettings.REVERSE_COST_PER_HP.get()) < 0.05);
        advance(PowerSettings.GLOBAL_COOLDOWN_TICKS.get() + 1L);
        fake.setHealth(fake.getMaxHealth() - 0.5f);
        float nearlyFull = fake.getHealth();
        PowerService.cast(fake); // still on its own cooldown: nothing may happen
        check("Reverse on cooldown does nothing", fake.getHealth() == nearlyFull);
        advance(PowerSettings.REVERSE_COOLDOWN_TICKS.get() + 1L);
        PowerService.cast(fake);
        check("Reverse never overheals (" + fake.getHealth() + " of " + fake.getMaxHealth() + ")", fake.getHealth() <= fake.getMaxHealth() + 0.001f);
        fake.setXRot(0.0f);
        advance(PowerSettings.GLOBAL_COOLDOWN_TICKS.get() + 1L);

        // item 12 (the multiplier part): a melee hit right after a cast, with tier 4, at 5 percent should become a
        // Black Flash within 400 tries and is then the configured multiple of the damage
        PowerService.equip(fake, PowerCatalog.BARRIER);
        advance(PowerSettings.BARRIER_COOLDOWN_TICKS.get() + 1L);
        PowerService.setResource(uuid, PowerService.maxResource(uuid));
        PowerService.cast(fake);
        int flashes = 0;
        for (int i = 0; i < 400 && flashes == 0; i++) {
            float result = PowerDamage.outgoing(fake, dummy, fake.damageSources().playerAttack(fake), 10.0f);
            if (result != 10.0f) {
                flashes++;
                check("Black Flash multiplies by " + PowerSettings.BLACK_FLASH_MULTIPLIER.get() + " (10 to " + result + ")",
                        Math.abs(result - 10.0 * PowerSettings.BLACK_FLASH_MULTIPLIER.get()) < 0.01);
            }
        }
        check("Black Flash happens", flashes > 0);
        advance(PowerSettings.BLACK_FLASH_WINDOW_TICKS.get() + 1L);
        boolean lateFlash = false;
        for (int i = 0; i < 400 && !lateFlash; i++) {
            lateFlash = PowerDamage.outgoing(fake, dummy, fake.damageSources().playerAttack(fake), 10.0f) != 10.0f;
        }
        check("no Black Flash outside its window", !lateFlash);
    }

    /** Script items 10 and 11: a second player, PvP off, and the Domain. */
    private void secondPlayer(MinecraftServer server, ServerLevel world, FakePlayer fake, String uuid, FakePlayer other, String otherUuid,
                              Mob dummy, double x, double z) {
        PowerService.learn(other, PowerCatalog.BLUE);
        PowerService.learn(other, PowerCatalog.BARRIER);
        PowerService.learn(other, PowerCatalog.DOMAIN);
        PowerService.awaken(other);

        // item 10: with PvP off the first player cannot cast at the second; with PvP on the cast is valid. A fake
        // player is not in the world on its own, so it is added for the length of the check.
        other.snapTo(x, 300.0, z + 4.0, 0.0f, 0.0f);
        world.addNewPlayer(other);
        try {
            twoPlayers(server, world, fake, uuid, other, otherUuid, dummy, x, z);
        } finally {
            world.removePlayerImmediately(other, Entity.RemovalReason.DISCARDED);
        }
    }

    private void twoPlayers(MinecraftServer server, ServerLevel world, FakePlayer fake, String uuid, FakePlayer other, String otherUuid,
                            Mob dummy, double x, double z) {
        advance(PowerSettings.BLUE_COOLDOWN_TICKS.get() + 1L);
        PowerService.equip(fake, PowerCatalog.BLUE);
        PowerService.setResource(uuid, PowerService.maxResource(uuid));
        server.getGlobalGameRules().set(GameRules.PVP, false, server);
        CastDecision pvpOff = decision(fake);
        check("with PvP off a cast at another player is refused (" + pvpOff + ")", pvpOff instanceof CastDecision.Rejected r && r.reason() == FailureReason.TARGET_INVALID);
        server.getGlobalGameRules().set(GameRules.PVP, true, server);
        CastDecision pvpOn = decision(fake);
        check("with PvP on the same cast is approved (" + pvpOn + ")", pvpOn instanceof CastDecision.Approved);
        other.snapTo(x + 5.0, 300.0, z + 2.0, 0.0f, 0.0f); // out of the line of fire, inside the Domain-to-be
        double mine = PowerService.resource(uuid);
        PowerService.setResource(otherUuid, 1.0);
        check("the second player's pool is their own", PowerService.resource(otherUuid) == 1.0 && PowerService.resource(uuid) == mine);

        // the bosses: the one that awakens and the one that gives a reroll charge, for players near it
        PowerService.sleep(otherUuid);
        check("a technique stays asleep until its boss falls", !PowerService.awakened(otherUuid));
        Entity dragon = EntityTypes.ENDER_DRAGON.create(world, EntitySpawnReason.COMMAND);
        if (dragon instanceof net.minecraft.world.entity.LivingEntity boss) {
            boss.snapTo(x + 5.0, 300.0, z, 0.0f, 0.0f);
            PowerService.onMobDeath(boss);
            check("the Ender Dragon's death awakens a player near it", PowerService.awakened(otherUuid));
        } else {
            check("an Ender Dragon can be created", false);
        }
        int charges = PowerService.state(otherUuid).rerollCharges;
        Entity wither = EntityTypes.WITHER.create(world, EntitySpawnReason.COMMAND);
        if (wither instanceof net.minecraft.world.entity.LivingEntity boss) {
            boss.snapTo(x + 5.0, 300.0, z, 0.0f, 0.0f);
            PowerService.onMobDeath(boss);
            check("the Wither's death gives a reroll charge", PowerService.state(otherUuid).rerollCharges == charges + 1);
            boss.snapTo(x + 500.0, 300.0, z, 0.0f, 0.0f);
            PowerService.onMobDeath(boss);
            check("a boss far away gives nothing", PowerService.state(otherUuid).rerollCharges == charges + 1 + 1 - 1 || PowerService.state(otherUuid).rerollCharges == charges + 1);
        } else {
            check("a Wither can be created", false);
        }

        // item 11: the Domain
        advance(PowerSettings.GLOBAL_COOLDOWN_TICKS.get() + 1L);
        check("equip Domain", PowerService.equip(fake, PowerCatalog.DOMAIN));
        PowerService.setResource(uuid, PowerService.maxResource(uuid));
        long castAt = now();
        PowerService.cast(fake);
        check("Domain is running", DomainManager.isActive(fake.getUUID(), now()));
        check("the target is inside the Domain", DomainManager.inDomainOf(fake.getUUID(), dummy, now()));

        PowerService.equip(other, PowerCatalog.BARRIER);
        PowerService.setResource(otherUuid, PowerService.maxResource(otherUuid));
        CastDecision inside = decision(other);
        check("a second player cannot cast inside the Domain (" + inside + ")", inside instanceof CastDecision.Rejected r && r.reason() == FailureReason.DOMAIN_SUPPRESSED);

        other.snapTo(x + 18.0, 300.0, z, 0.0f, 0.0f); // outside the radius, closer than the minimum separation
        PowerService.equip(other, PowerCatalog.DOMAIN);
        CastDecision overlap = decision(other);
        check("an overlapping Domain is rejected (" + overlap + ")", overlap instanceof CastDecision.Rejected r && r.reason() == FailureReason.DOMAIN_OVERLAP);
        other.snapTo(x + 40.0, 300.0, z, 0.0f, 0.0f);
        PowerService.equip(other, PowerCatalog.BARRIER);
        CastDecision outside = decision(other);
        check("a second player outside the Domain may cast (" + outside + ")", outside instanceof CastDecision.Approved);

        // a strike inside the Domain is dealt as magic damage with the bonus, replacing the hit; outside the Black Flash
        // window, so that a chance event cannot change the number
        advance(PowerSettings.BLACK_FLASH_WINDOW_TICKS.get() + 1L);
        dummy.setHealth(dummy.getMaxHealth());
        dummy.damageCooldownTime = 0;
        float plainStart = dummy.getHealth();
        dummy.hurtServer(world, fake.damageSources().magic(), 8.0f);
        check("a plain 8.0 magic hit takes 8.0 from this mob (took " + (plainStart - dummy.getHealth()) + ")",
                Math.abs((plainStart - dummy.getHealth()) - 8.0f) < 0.01);
        dummy.setHealth(dummy.getMaxHealth());
        dummy.damageCooldownTime = 0;
        float full = dummy.getHealth();
        float left = PowerDamage.outgoing(fake, dummy, fake.damageSources().playerAttack(fake), 8.0f);
        double expected = 8.0 * (1.0 + PowerSettings.DOMAIN_DAMAGE_BONUS.get());
        check("a Domain strike replaces the hit (returned " + left + ")", left == 0.0f);
        check("a Domain strike deals " + expected + " (took " + (full - dummy.getHealth()) + ")", Math.abs((full - dummy.getHealth()) - expected) < 0.01);
        check("the strike guard is released", !PowerDamage.inDomainStrike());

        // the Domain ends after its duration, and its cooldown runs from then
        advance(castAt + PowerSettings.DOMAIN_DURATION_TICKS.get() - 1L - now());
        check("the Domain still stands one tick before its end", DomainManager.isActive(fake.getUUID(), now()));
        advance(1L);
        check("the Domain ends after " + PowerSettings.DOMAIN_DURATION_TICKS.get() + " ticks", !DomainManager.isActive(fake.getUUID(), now()));
        PowerService.tickDomains(server);
        check("the Domain cooldown runs after it ends", DomainManager.cooldownRemaining(fake.getUUID(), now()) > 0);

        // a Domain that is cut short by a dimension change
        advance(PowerSettings.DOMAIN_COOLDOWN_TICKS.get() + 1L);
        PowerService.setResource(uuid, PowerService.maxResource(uuid));
        PowerService.cast(fake);
        check("a second Domain can be cast after the cooldown", DomainManager.isActive(fake.getUUID(), now()));
        PowerService.onDimensionChange(fake);
        check("Domain ends on a dimension change", !DomainManager.isActive(fake.getUUID(), now()));
    }

    /** Script items 6 and 7: the armor vow, the limit of two, death. */
    private void vowsAndDeath(FakePlayer fake, String uuid) {
        advance(PowerSettings.DOMAIN_COOLDOWN_TICKS.get() + 1L);
        // item 6: NO_ARMOR while armored is refused; accepted without armor; breaks when armor goes back on
        fake.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        check("NO_ARMOR while armored is refused", PowerService.acceptVow(fake, Vow.NO_ARMOR) == VowSet.AcceptResult.CONDITION_NOT_MET);
        fake.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        double maxWithout = PowerService.maxResource(uuid);
        check("NO_ARMOR without armor is accepted", PowerService.acceptVow(fake, Vow.NO_ARMOR) == VowSet.AcceptResult.ACCEPTED);
        check("the vow raises the maximum by " + PowerSettings.VOW_NO_ARMOR_BONUS.get(),
                Math.abs(PowerService.maxResource(uuid) - (maxWithout + PowerSettings.VOW_NO_ARMOR_BONUS.get())) < 0.01);
        fake.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        PowerService.onEquipmentChange(fake);
        check("putting armor on breaks the vow", !PowerService.vowsOf(uuid).isActive(Vow.NO_ARMOR));
        check("the bonus is gone", Math.abs(PowerService.maxResource(uuid) - maxWithout) < 0.01);
        fake.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        check("a broken vow cannot be taken again at once",
                PowerService.acceptVow(fake, Vow.NO_ARMOR) == VowSet.AcceptResult.ON_COOLDOWN);
        advance(PowerSettings.VOW_REACCEPT_COOLDOWN_TICKS.get() + 1L);

        // two vows fit, the third is refused
        check("vow no sprint", PowerService.acceptVow(fake, Vow.NO_SPRINT) == VowSet.AcceptResult.ACCEPTED);
        check("vow no off hand", PowerService.acceptVow(fake, Vow.NO_OFFHAND) == VowSet.AcceptResult.ACCEPTED);
        check("a third vow is refused", PowerService.acceptVow(fake, Vow.NO_ARMOR) == VowSet.AcceptResult.LIMIT_REACHED);
        screens(fake);

        // item 7: death halves the resource and clears vows and cooldowns
        PowerService.equip(fake, PowerCatalog.BARRIER);
        advance(PowerSettings.BARRIER_COOLDOWN_TICKS.get() + 1L);
        PowerService.setResource(uuid, PowerService.maxResource(uuid));
        PowerService.cast(fake);
        AbilitySpec barrier = PowerService.selected(uuid);
        check("a cast set a cooldown before death", barrier != null && PowerService.cooldownRemaining(uuid, barrier) > 0);
        double beforeDeath = PowerService.resource(uuid);
        PowerService.onDeath(fake);
        check("death halves the resource (" + beforeDeath + " to " + PowerService.resource(uuid) + ")",
                Math.abs(PowerService.resource(uuid) - Math.floor(beforeDeath / 2.0)) < 1.01);
        check("death clears vows", PowerService.vowsOf(uuid).active().isEmpty());
        check("death clears cooldowns", barrier != null && PowerService.cooldownRemaining(uuid, barrier) == 0);
        check("death does not block the next vow", PowerService.acceptVow(fake, Vow.NO_SPRINT) == VowSet.AcceptResult.ACCEPTED);
    }

    /** Script item 9: the admin command and the admin screens answer to the permission, and nobody else. */
    private void adminSurface(MinecraftServer server, FakePlayer fake, String uuid) {
        CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
        CommandSourceStack console = server.createCommandSourceStack();
        String[] commands = {"poweradmin", "poweradmin info Bob", "poweradmin grant Bob BLUE", "poweradmin revoke Bob BLUE",
                "poweradmin awaken Bob", "poweradmin sleep Bob", "poweradmin reroll Bob", "poweradmin reroll Bob BLUE", "poweradmin charge Bob", "poweradmin mastery Bob 100",
                "poweradmin reset Bob confirm"};

        adminGranted = false;
        CommandSourceStack plain = fake.createCommandSourceStack();
        check("the fake player is not an administrator without the permission", !PowerPermissions.isAdmin(fake));
        for (String command : commands) {
            check("a non-op cannot use '" + command + "'", !fullyParses(dispatcher, plain, command));
            check("an operator can use '" + command + "'", fullyParses(dispatcher, console, command));
        }
        adminGranted = true;
        check("the permission node grants a player the admin tools", PowerPermissions.isAdmin(fake));
        for (String command : commands) {
            check("a player with the permission can use '" + command + "'", fullyParses(dispatcher, fake.createCommandSourceStack(), command));
        }
        screens(fake);
    }

    // ── helpers ─────────────────────────────────────────────────────────────────────────────

    private static boolean fullyParses(CommandDispatcher<CommandSourceStack> dispatcher, CommandSourceStack source, String command) {
        ParseResults<CommandSourceStack> parsed = dispatcher.parse(command, source);
        return !parsed.getReader().canRead() && parsed.getExceptions().isEmpty() && parsed.getContext().getCommand() != null;
    }

    private CastDecision decision(ServerPlayer player) {
        return CastPipeline.validate(new GameFacts(player, PowerService.runtime(player.getUUID()), now()));
    }

    /** Equips a technique, makes sure there is resource, casts at the target and checks the cost was taken. */
    private void castOf(FakePlayer fake, String uuid, String techniqueId, String name, int cost, Mob target) {
        check("equip " + name, PowerService.equip(fake, techniqueId));
        PowerService.setResource(uuid, PowerService.maxResource(uuid));
        target.setDeltaMovement(Vec3.ZERO);
        double before = PowerService.resource(uuid);
        CastDecision decision = decision(fake);
        PowerService.cast(fake);
        check(name + " spent " + cost + " (was " + before + ", now " + PowerService.resource(uuid) + "; pipeline said " + decision + ")",
                Math.abs((before - PowerService.resource(uuid)) - cost) < 0.01);
    }

    /** Builds every screen once. A screen that throws while its items are built is a failed check. */
    private void screens(ServerPlayer player) {
        String name = player.getUUID().toString().substring(0, 8);
        String id = player.getUUID().toString();
        run("hub of " + name, () -> PowerScreens.open(player));
        run("techniques of " + name, () -> PowerScreens.openTechniques(player, 0));
        run("abilities of " + name, () -> PowerScreens.openAbilities(player));
        run("reroll of " + name, () -> PowerScreens.openReroll(player));
        run("vows of " + name, () -> PowerScreens.openVows(player));
        run("admin list", () -> PowerAdminScreens.open(player, 0));
        run("admin player", () -> PowerAdminScreens.showPlayer(player, player));
        run("admin grant", () -> PowerAdminScreens.openGrant(player, id, 0));
        run("admin revoke", () -> PowerAdminScreens.openRevoke(player, id));
    }

    private void run(String screen, Runnable open) {
        checks++;
        try {
            open.run();
        } catch (RuntimeException e) {
            failures.add("screen " + screen + " threw " + e);
            LOGGER.error("Power smoke: screen {} threw", screen, e);
        }
    }
}
