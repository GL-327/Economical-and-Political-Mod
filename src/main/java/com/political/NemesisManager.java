package com.political;


import com.political.economy.CoinManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Runs the grudge.
 *
 * <p>Ordinary mob deaths are forgettable. This makes one of them stick: whatever lands the killing
 * blow on a player is promoted into a named individual with rolled traits, and from then on it hunts
 * that specific player. Kill you again and it climbs a rank and gains another trait. Put it down and
 * it might still get away, come back scarred and be worse for it.</p>
 *
 * <p>The design leans on the one thing a random seed world has that a fixed island never will: the
 * server does not know what the world looks like, but it does know exactly how a given player has
 * been dying. That history is the content.</p>
 */
public final class NemesisManager {

    private NemesisManager() {}

    public static final String NEMESIS_TAG = "political_nemesis";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path savePath;
    private static final Random RNG = new Random();

    /** All grudges, keyed by nemesis id. */
    private static final Map<String, Nemesis> NEMESES = new HashMap<>();
    /** Live entity uuid to nemesis id, so a hit can be resolved without scanning. */
    private static final Map<String, String> LIVE = new HashMap<>();
    /** Per player cooldown on being ambushed, so this never becomes constant harassment. */
    private static final Map<String, Long> NEXT_HUNT = new HashMap<>();
    /** Nemeses that used their one death save this fight. */
    private static final Map<String, Boolean> USED_UNDYING = new HashMap<>();

    private static final long HUNT_COOLDOWN_MS = 9 * 60 * 1000L;
    private static final int MAX_PER_PLAYER = 6;

    public static boolean enabled = true;

    // ── Persistence ───────────────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        savePath = worldRoot.resolve("political").resolve("nemeses.json");
        NEMESES.clear();
        LIVE.clear();
        if (savePath == null || !Files.exists(savePath)) return;
        try (Reader r = Files.newBufferedReader(savePath)) {
            java.lang.reflect.Type t = new TypeToken<Map<String, Nemesis>>() {}.getType();
            Map<String, Nemesis> data = GSON.fromJson(r, t);
            if (data != null) NEMESES.putAll(data);
            // Nothing survives a restart as a live entity, so every grudge starts dormant.
            for (Nemesis n : NEMESES.values()) {
                n.active = false;
                n.entityUuid = "";
            }
        } catch (Exception e) {
            System.out.println("[Nemesis] Could not read grudges: " + e.getMessage());
        }
    }

    public static void save() {
        if (savePath == null) return;
        try {
            Files.createDirectories(savePath.getParent());
            try (Writer w = Files.newBufferedWriter(savePath)) {
                GSON.toJson(NEMESES, w);
            }
        } catch (Exception e) {
            System.out.println("[Nemesis] Could not write grudges: " + e.getMessage());
        }
    }

    // ── Reading ───────────────────────────────────────────────────────────

    public static List<Nemesis> forPlayer(ServerPlayer player) {
        return forPlayer(player.getUUID().toString());
    }

    public static List<Nemesis> forPlayer(String uuid) {
        List<Nemesis> out = new ArrayList<>();
        for (Nemesis n : NEMESES.values()) {
            if (n.victimUuid.equals(uuid)) out.add(n);
        }
        out.sort((a, b) -> Integer.compare(b.rank().ordinal() * 100 + b.kills,
                a.rank().ordinal() * 100 + a.kills));
        return out;
    }

    public static Nemesis byEntity(Entity e) {
        if (e == null) return null;
        String id = LIVE.get(e.getUUID().toString());
        return id == null ? null : NEMESES.get(id);
    }

    public static boolean isNemesis(Entity e) {
        return e != null && e.entityTags().contains(NEMESIS_TAG) && byEntity(e) != null;
    }

    // ── The grudge loop ───────────────────────────────────────────────────

    /**
     * A player died. If a mob did it, that mob either becomes a nemesis or, if it already was one,
     * gets promoted.
     */
    public static void onPlayerDeath(ServerPlayer victim, DamageSource source) {
        if (!enabled) return;
        Entity killer = source.getEntity();
        if (!(killer instanceof LivingEntity mob) || killer instanceof ServerPlayer) return;

        // The killer was already carrying a grudge, so this is a promotion.
        Nemesis existing = byEntity(mob);
        if (existing != null && existing.victimUuid.equals(victim.getUUID().toString())) {
            boolean rankedUp = existing.promote(RNG);
            existing.active = false;
            existing.entityUuid = "";
            LIVE.remove(mob.getUUID().toString());
            announcePromotion(victim, existing, rankedUp);
            save();
            return;
        }

        if (forPlayer(victim).size() >= MAX_PER_PLAYER) return;

        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        Nemesis n = Nemesis.create(victim.getUUID().toString(), victim.getName().getString(),
                key == null ? "minecraft:zombie" : key.toString(), RNG);
        n.lastDim = victim.level().dimension().identifier().toString();
        n.lastX = victim.blockPosition().getX();
        n.lastY = victim.blockPosition().getY();
        n.lastZ = victim.blockPosition().getZ();
        NEMESES.put(n.id, n);

        // The mob that did it walks away rather than staying an ordinary mob, but only if it is
        // not currently part of a fight that counts bodies. An incursion or a siege treats a
        // vanished attacker as a dead one, so discarding a sovereign here would let a player
        // complete the whole encounter, and collect the reward, by deliberately dying to its boss.
        if (!IncursionManager.isIncursionMob(mob) && !SiegeManager.isAttacker(mob)) {
            mob.discard();
        }

        victim.sendSystemMessage(Component.literal(""));
        victim.sendSystemMessage(Component.literal("  §8Something took an interest in that."));
        victim.sendSystemMessage(Component.literal("  " + n.colouredName()));
        victim.sendSystemMessage(Component.literal("  §7It walked away. It will find you again."));
        victim.sendSystemMessage(Component.literal("  §8Check §7/grudges"));
        victim.sendSystemMessage(Component.literal(""));
        save();
    }

    private static void announcePromotion(ServerPlayer victim, Nemesis n, boolean rankedUp) {
        victim.sendSystemMessage(Component.literal(""));
        victim.sendSystemMessage(Component.literal("  " + n.colouredName()));
        if (rankedUp) {
            victim.sendSystemMessage(Component.literal("  §7Risen to "
                    + n.rank().colour + n.rank().label + "§7."));
        } else {
            victim.sendSystemMessage(Component.literal("  §7It learned something from that."));
        }
        List<NemesisTrait> traits = n.traitList();
        if (!traits.isEmpty()) {
            NemesisTrait newest = traits.get(traits.size() - 1);
            victim.sendSystemMessage(Component.literal("  §7Gained " + newest.colour
                    + newest.display + "§7: §8" + newest.effect));
        }
        victim.sendSystemMessage(Component.literal("  §7That is §c" + n.kills
                + "§7 it owes you for."));
        victim.sendSystemMessage(Component.literal(""));
    }

    /** A nemesis died. Either it is settled, or the cunning ones get away. */
    public static void onNemesisKilled(ServerPlayer player, LivingEntity mob) {
        Nemesis n = byEntity(mob);
        if (n == null) return;
        LIVE.remove(mob.getUUID().toString());
        USED_UNDYING.remove(n.id);

        // Cunning does not save it from the killing blow, it saves it from the consequences.
        if (n.has(NemesisTrait.CUNNING) && RNG.nextDouble() < 0.35) {
            n.flee(RNG);
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("  " + n.colouredName()
                    + " §7broke and ran."));
            player.sendSystemMessage(Component.literal("  §8It will be back, and it will be worse."));
            player.sendSystemMessage(Component.literal(""));
            if (player.level() instanceof ServerLevel level) {
                Vfx.puff(level, ParticleTypes.SMOKE, mob.position().add(0, 1, 0), 60, 1.2, 0.05);
            }
            save();
            return;
        }

        n.deaths++;
        n.active = false;
        n.entityUuid = "";

        long bounty = n.bounty();
        CoinManager.giveCoins(player, bounty);

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("  §a§lGRUDGE SETTLED"));
        player.sendSystemMessage(Component.literal("  " + n.colouredName()));
        player.sendSystemMessage(Component.literal("  §7Bounty: §6" + GuiKit.num(bounty) + " coins"));
        player.sendSystemMessage(Component.literal("  §8It killed you " + n.kills
                + (n.kills == 1 ? " time." : " times.")));
        player.sendSystemMessage(Component.literal(""));

        if (player.level() instanceof ServerLevel level) {
            VfxPlus.executionFlash(level, mob.position().add(0, 1, 0), Vfx.CURSED);
            level.playSound(null, mob.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                    SoundSource.PLAYERS, 0.8f, 0.8f);
        }

        // A settled grudge is gone for good. The rank it reached is the story.
        NEMESES.remove(n.id);
        save();
    }

    // ── Hunting ───────────────────────────────────────────────────────────

    /** Ticked by the server. Occasionally sends a dormant nemesis after its player. */
    public static void tick(MinecraftServer server) {
        if (!enabled) return;
        if (server.getTickCount() % 200 != 0) return;

        long now = System.currentTimeMillis();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String uuid = player.getUUID().toString();
            Long next = NEXT_HUNT.get(uuid);
            if (next != null && now < next) continue;

            List<Nemesis> mine = forPlayer(uuid);
            if (mine.isEmpty()) continue;

            List<Nemesis> dormant = new ArrayList<>();
            for (Nemesis n : mine) {
                if (!n.active) dormant.add(n);
            }
            if (dormant.isEmpty()) continue;

            // Not every window produces an ambush, otherwise it stops being a surprise.
            if (RNG.nextDouble() > 0.5) {
                NEXT_HUNT.put(uuid, now + HUNT_COOLDOWN_MS / 3);
                continue;
            }

            Nemesis pick = dormant.get(RNG.nextInt(dormant.size()));
            if (spawnFor(player, pick)) {
                NEXT_HUNT.put(uuid, now + HUNT_COOLDOWN_MS);
            }
        }
    }

    /** Puts a nemesis into the world near its victim and dresses it with everything it has earned. */
    public static boolean spawnFor(ServerPlayer player, Nemesis n) {
        if (!(player.level() instanceof ServerLevel level)) return false;

        BlockPos at = findSpawnNear(level, player);
        if (at == null) return false;

        EntityType<?> type = typeOf(n);
        if (type == null) return false;

        Entity raw = type.spawn(level, at, EntitySpawnReason.EVENT);
        if (!(raw instanceof Mob mob)) {
            if (raw != null) raw.discard();
            return false;
        }

        apply(mob, n);
        n.active = true;
        n.entityUuid = mob.getUUID().toString();
        n.lastSeen = System.currentTimeMillis();
        n.lastDim = level.dimension().identifier().toString();
        n.lastX = at.getX();
        n.lastY = at.getY();
        n.lastZ = at.getZ();
        LIVE.put(mob.getUUID().toString(), n.id);
        USED_UNDYING.remove(n.id);

        mob.setTarget(player);

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("  §c§lIT FOUND YOU"));
        player.sendSystemMessage(Component.literal("  " + n.colouredName()));
        player.sendSystemMessage(Component.literal("  §7" + n.summary()));
        StringBuilder tr = new StringBuilder("  §8");
        for (NemesisTrait t : n.traitList()) {
            tr.append(t.colour).append(t.display).append(" §8");
        }
        player.sendSystemMessage(Component.literal(tr.toString()));
        player.sendSystemMessage(Component.literal(""));

        level.playSound(null, player.blockPosition(), SoundEvents.RAVAGER_ROAR,
                SoundSource.HOSTILE, 1.2f, 0.5f);
        Vfx.domainCollapse(level, Vec3.atCenterOf(at), 5.0, Vfx.CURSED);
        save();
        return true;
    }

    /**
     * Places a specific nemesis at a chosen spot rather than near its victim. Used when another
     * system wants a grudge to show up somewhere on purpose, like a siege handing its command to
     * whatever has been hunting the defender.
     */
    public static Mob spawnAt(ServerLevel level, Nemesis n, BlockPos at) {
        EntityType<?> type = typeOf(n);
        if (type == null) return null;
        Entity raw = type.spawn(level, at, EntitySpawnReason.EVENT);
        if (!(raw instanceof Mob mob)) {
            if (raw != null) raw.discard();
            return null;
        }
        apply(mob, n);
        n.active = true;
        n.entityUuid = mob.getUUID().toString();
        n.lastSeen = System.currentTimeMillis();
        LIVE.put(mob.getUUID().toString(), n.id);
        USED_UNDYING.remove(n.id);
        save();
        return mob;
    }

    /** The nastiest dormant grudge a player has, or null. Used to pick a siege commander. */
    public static Nemesis strongestDormantFor(String uuid) {
        Nemesis best = null;
        for (Nemesis n : NEMESES.values()) {
            if (!n.victimUuid.equals(uuid) || n.active) continue;
            if (best == null || n.rank().ordinal() > best.rank().ordinal()) best = n;
        }
        return best;
    }

    private static EntityType<?> typeOf(Nemesis n) {
        try {
            Identifier id = Identifier.parse(n.entityType);
            return BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(EntityTypes.ZOMBIE);
        } catch (Exception e) {
            return EntityTypes.ZOMBIE;
        }
    }

    /** Somewhere solid, in the open, close enough to matter but not on top of the player. */
    private static BlockPos findSpawnNear(ServerLevel level, ServerPlayer player) {
        BlockPos base = player.blockPosition();
        for (int attempt = 0; attempt < 24; attempt++) {
            int dx = RNG.nextInt(29) - 14;
            int dz = RNG.nextInt(29) - 14;
            if (Math.abs(dx) < 6 && Math.abs(dz) < 6) continue;
            BlockPos ground = level.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    base.offset(dx, 0, dz));
            if (Math.abs(ground.getY() - base.getY()) > 12) continue;
            if (!level.getBlockState(ground).isAir()) continue;
            if (!level.getBlockState(ground.above()).isAir()) continue;
            return ground;
        }
        return null;
    }

    /** Everything a nemesis has earned, written onto the live mob. */
    private static void apply(Mob mob, Nemesis n) {
        mob.addTag(NEMESIS_TAG);
        mob.setCustomName(Component.literal(n.fullName())
                .withStyle(n.rank().colour, ChatFormatting.BOLD));
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();

        var hp = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            double base = hp.getBaseValue();
            double scaled = base * n.healthMultiplier();
            if (n.has(NemesisTrait.COLOSSAL)) scaled *= 1.8;
            if (n.has(NemesisTrait.ARMOURED)) scaled *= 1.3;
            hp.setBaseValue(scaled);
            mob.setHealth((float) scaled);
        }

        var atk = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) {
            double dmg = atk.getBaseValue() * n.damageMultiplier();
            if (n.has(NemesisTrait.BRUTAL)) dmg *= 1.5;
            atk.setBaseValue(dmg);
        }

        var speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            double s = speed.getBaseValue();
            if (n.has(NemesisTrait.SWIFT)) s *= 1.45;
            if (n.has(NemesisTrait.COLOSSAL)) s *= 0.85;
            speed.setBaseValue(s);
        }

        var kb = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kb != null && (n.has(NemesisTrait.VOIDBOUND) || n.has(NemesisTrait.COLOSSAL))) {
            kb.setBaseValue(1.0);
        }

        var scale = mob.getAttribute(Attributes.SCALE);
        if (scale != null && n.has(NemesisTrait.COLOSSAL)) {
            scale.setBaseValue(1.6);
        }

        if (n.has(NemesisTrait.REGENERATING)) {
            mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Integer.MAX_VALUE, 1,
                    true, false, false));
        }
        if (n.has(NemesisTrait.WARDED)) {
            mob.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, Integer.MAX_VALUE, 1,
                    true, false, false));
        }
        if (n.has(NemesisTrait.DREADFUL)) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0,
                    true, false, false));
        }
    }

    // ── Trait behaviour ───────────────────────────────────────────────────

    /**
     * A nemesis hit its victim. Returns the multiplier to apply to that hit.
     */
    public static float onNemesisHit(LivingEntity attacker, ServerPlayer victim, float damage) {
        Nemesis n = byEntity(attacker);
        if (n == null) return 1.0f;
        float mult = 1.0f;

        if (n.has(NemesisTrait.VENOMOUS)) {
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
            victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));
        }
        if (n.has(NemesisTrait.VAMPIRIC)) {
            attacker.heal(damage * 0.4f);
        }
        if (n.has(NemesisTrait.WITHERING)) {
            victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
            victim.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 1));
        }
        if (n.has(NemesisTrait.ENSNARING)) {
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 4));
        }
        if (n.has(NemesisTrait.BLOODSCENT)) {
            // Hits harder the worse shape you are already in.
            float missing = 1.0f - (victim.getHealth() / Math.max(1f, victim.getMaxHealth()));
            mult += missing * 0.6f;
        }
        if (n.has(NemesisTrait.FRENZIED)) {
            float own = 1.0f - (attacker.getHealth() / Math.max(1f, attacker.getMaxHealth()));
            mult += own * 0.5f;
        }
        if (n.has(NemesisTrait.STORMTOUCHED) && RNG.nextDouble() < 0.15
                && victim.level() instanceof ServerLevel level) {
            var bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.EVENT);
            if (bolt != null) {
                bolt.snapTo(victim.getX(), victim.getY(), victim.getZ());
                level.addFreshEntity(bolt);
            }
        }
        return mult;
    }

    /**
     * The player hit a nemesis. Returns the multiplier to apply, and handles the traits that react
     * to being struck rather than to striking.
     */
    public static float onNemesisHurt(ServerPlayer player, LivingEntity mob, float damage) {
        Nemesis n = byEntity(mob);
        if (n == null) return 1.0f;
        float mult = 1.0f;

        if (n.has(NemesisTrait.ARMOURED)) mult *= 0.66f;
        if (n.has(NemesisTrait.IRON_HIDE) && RNG.nextDouble() < 0.25) mult *= 0.1f;

        if (n.has(NemesisTrait.THORNED)) {
            player.hurtServer((ServerLevel) player.level(),
                    player.level().damageSources().thorns(mob), damage * 0.25f);
        }
        if (n.has(NemesisTrait.PHASING) && RNG.nextDouble() < 0.2) {
            Vec3 behind = player.position().subtract(player.getLookAngle().scale(1.5));
            mob.snapTo(behind.x, behind.y, behind.z);
            if (mob.level() instanceof ServerLevel level) Vfx.voidRift(level, behind);
        }
        if (n.has(NemesisTrait.SUMMONER) && mob.getHealth() < mob.getMaxHealth() * 0.5f
                && RNG.nextDouble() < 0.08) {
            summonHelp(mob, n);
        }

        // Undying is spent here rather than in the death hook. Refusing the killing blow by
        // shrinking the damage keeps every other system's view of the fight consistent, where
        // cancelling a death part way through would not.
        if (damage * mult >= mob.getHealth() && refuseDeath(mob, n)) {
            return 0.01f;
        }
        return mult;
    }

    /** Undying spends its one save. True means the killing blow was refused. */
    private static boolean refuseDeath(LivingEntity mob, Nemesis n) {
        if (!n.has(NemesisTrait.UNDYING)) return false;
        if (Boolean.TRUE.equals(USED_UNDYING.get(n.id))) return false;

        USED_UNDYING.put(n.id, true);
        mob.setHealth(mob.getMaxHealth() * 0.35f);
        for (ServerPlayer watcher : nearbyPlayers(mob)) {
            watcher.sendSystemMessage(Component.literal("  " + n.colouredName()
                    + " §7refuses to go down."));
        }
        mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 2));
        if (mob.level() instanceof ServerLevel level) {
            VfxPlus.executionFlash(level, mob.position().add(0, 1, 0), Vfx.INFERNO);
            level.playSound(null, mob.blockPosition(), SoundEvents.TOTEM_USE,
                    SoundSource.HOSTILE, 1.0f, 0.6f);
        }
        return true;
    }

    private static List<ServerPlayer> nearbyPlayers(LivingEntity mob) {
        List<ServerPlayer> out = new ArrayList<>();
        if (!(mob.level() instanceof ServerLevel level)) return out;
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(mob) < 64 * 64) out.add(p);
        }
        return out;
    }

    private static void summonHelp(LivingEntity mob, Nemesis n) {
        if (!(mob.level() instanceof ServerLevel level)) return;
        for (int i = 0; i < 2; i++) {
            Entity add = EntityTypes.ZOMBIE.spawn(level, mob.blockPosition().above(),
                    EntitySpawnReason.EVENT);
            if (add instanceof Mob m) {
                m.setCustomName(Component.literal("§8" + n.name + "'s Hand"));
                m.addTag(NEMESIS_TAG + "_add");
                var hp = m.getAttribute(Attributes.MAX_HEALTH);
                if (hp != null) {
                    hp.setBaseValue(hp.getBaseValue() * 0.6);
                    m.setHealth((float) hp.getBaseValue());
                }
            }
        }
        VfxPlus.summoningCircle(level, mob.position(), Vfx.CURSED);
    }

    /** Explosive goes off on death, which is the trait's whole joke. */
    public static void onNemesisDeathEffects(LivingEntity mob) {
        Nemesis n = byEntity(mob);
        if (n == null || !n.has(NemesisTrait.EXPLOSIVE)) return;
        if (!(mob.level() instanceof ServerLevel level)) return;
        VfxPlus.orbitalStrike(level, mob.position().add(0, 1, 0), Vfx.INFERNO);
        for (Entity e : level.getEntities(mob,
                new net.minecraft.world.phys.AABB(mob.position(), mob.position()).inflate(4))) {
            if (e instanceof LivingEntity le) {
                le.hurtServer(level, level.damageSources().explosion(mob, mob), 14f);
            }
        }
    }

    /** Called when a live nemesis entity vanishes without dying, so the grudge goes dormant. */
    public static void onEntityRemoved(Entity e) {
        String id = LIVE.remove(e.getUUID().toString());
        if (id == null) return;
        Nemesis n = NEMESES.get(id);
        if (n != null) {
            n.active = false;
            n.entityUuid = "";
        }
    }

    public static void clear(UUID uuid) {
        NEXT_HUNT.remove(uuid.toString());
    }

    /** Used by the console to wipe one player's grudges. */
    public static int clearFor(String victimUuid) {
        List<String> ids = new ArrayList<>();
        for (Nemesis n : NEMESES.values()) {
            if (n.victimUuid.equals(victimUuid)) ids.add(n.id);
        }
        for (String id : ids) NEMESES.remove(id);
        save();
        return ids.size();
    }

    public static int total() {
        return NEMESES.size();
    }
}
