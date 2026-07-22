package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime + persistence for {@link Technique}s. Handles the Rite of Awakening, per-technique
 * mastery, technique-energy economy, the single-holder exclusive registry, and the actual
 * ability effects (dispatched by {@link Technique.Archetype}).
 *
 * <p>Persisted to {@code techniques.json} (world root): per-player data plus the global
 * exclusive-holder map.</p>
 */
public final class TechniqueManager {

    private TechniqueManager() {}

    public static class PlayerTechniqueData {
        public String technique = null;
        public Map<String, Integer> abilityUses = new HashMap<>();
        public String selectedAbility = null;
        public float energy = 100f;
        public long lastEnergyMs = 0L;
        public Map<String, Long> cooldowns = new HashMap<>();
        // Chronoshear echo
        public double echoX, echoY, echoZ;
        public float echoHealth = -1f;
        public boolean hasEcho = false;
    }

    private static class SaveFile {
        Map<String, PlayerTechniqueData> players = new HashMap<>();
        Map<String, String> exclusiveHolders = new HashMap<>();
    }

    private static final Map<String, PlayerTechniqueData> DATA = new ConcurrentHashMap<>();
    /** techniqueName -> holder uuid. Only ONE holder per exclusive technique server-wide. */
    private static final Map<String, String> EXCLUSIVE_HOLDERS = new ConcurrentHashMap<>();
    /** Transient combat windows (uuid -> end millis). */
    private static final Map<String, Long> BARRIER_UNTIL = new ConcurrentHashMap<>();
    private static final Map<String, Long> MIRROR_UNTIL = new ConcurrentHashMap<>();
    private static final Map<String, UUID> LAST_ATTACKER = new ConcurrentHashMap<>();

    private static Path dataFile;

    // ── Persistence ─────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("techniques.json");
        load();
    }

    private static void load() {
        if (dataFile == null || !Files.exists(dataFile)) return;
        try {
            String content = Files.readString(dataFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type type = new TypeToken<SaveFile>() {}.getType();
            SaveFile sf = gson.fromJson(content, type);
            if (sf != null) {
                DATA.clear();
                if (sf.players != null) DATA.putAll(sf.players);
                EXCLUSIVE_HOLDERS.clear();
                if (sf.exclusiveHolders != null) EXCLUSIVE_HOLDERS.putAll(sf.exclusiveHolders);
            }
        } catch (Exception e) {
            System.err.println("[TechniqueManager] Failed to load: " + e.getMessage());
        }
    }

    public static void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            SaveFile sf = new SaveFile();
            sf.players = new HashMap<>(DATA);
            sf.exclusiveHolders = new HashMap<>(EXCLUSIVE_HOLDERS);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (java.io.Writer w = Files.newBufferedWriter(dataFile)) {
                gson.toJson(sf, w);
            }
        } catch (Exception e) {
            System.err.println("[TechniqueManager] Failed to save: " + e.getMessage());
        }
    }

    public static PlayerTechniqueData get(String uuid) {
        return DATA.computeIfAbsent(uuid, k -> new PlayerTechniqueData());
    }

    public static boolean hasTechnique(String uuid) {
        return get(uuid).technique != null;
    }

    public static Technique getTechnique(String uuid) {
        return Technique.byName(get(uuid).technique);
    }

    public static String getExclusiveHolder(Technique t) {
        return EXCLUSIVE_HOLDERS.get(t.name());
    }

    // ── Rite of Awakening ───────────────────────────────────────

    public static final int AWAKEN_LEVEL = 5;

    /**
     * Performs the Rite of Awakening: requires Magic L{@value #AWAKEN_LEVEL}, no existing
     * technique, and one Awakening Catalyst in the inventory (consumed on success).
     */
    public static boolean attemptAwaken(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        if (hasTechnique(uuid)) {
            player.sendSystemMessage(Component.literal("✦ You have already awakened a Technique. Use /technique renounce first.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        if (MagicManager.getLevel(uuid) < AWAKEN_LEVEL) {
            player.sendSystemMessage(Component.literal("✦ The Rite requires Magic Level " + AWAKEN_LEVEL + ".")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        if (!consumeCatalyst(player)) {
            player.sendSystemMessage(Component.literal("✦ The Rite requires an Awakening Catalyst.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        Technique rolled = roll(uuid);
        assignTechnique(player, rolled);
        performRiteEffects(player, rolled);
        return true;
    }

    private static boolean consumeCatalyst(ServerPlayer player) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack st = inv.getItem(i);
            if (!st.isEmpty() && "awakening_catalyst".equals(SlayerItems.getCustomItemId(st))) {
                st.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static Technique roll(String uuid) {
        Random rng = new Random();
        // 5% exclusive, weighted to unclaimed ones.
        if (rng.nextFloat() < 0.05f) {
            List<Technique> unclaimed = new ArrayList<>();
            for (Technique t : Technique.exclusives()) {
                if (!EXCLUSIVE_HOLDERS.containsKey(t.name())) unclaimed.add(t);
            }
            if (!unclaimed.isEmpty()) {
                return unclaimed.get(rng.nextInt(unclaimed.size()));
            }
            // all exclusives claimed → fall through to common
        }
        List<Technique> commons = Technique.commons();
        return commons.get(rng.nextInt(commons.size()));
    }

    private static void assignTechnique(ServerPlayer player, Technique t) {
        String uuid = player.getUUID().toString();
        PlayerTechniqueData d = get(uuid);
        d.technique = t.name();
        d.abilityUses.clear();
        d.selectedAbility = t.abilities.get(0).id();
        if (t.exclusive) {
            EXCLUSIVE_HOLDERS.put(t.name(), uuid);
        }
        save();
    }

    /** Admin/testing grant that bypasses catalyst + level requirements. */
    public static boolean grant(ServerPlayer player, Technique t) {
        String uuid = player.getUUID().toString();
        if (t.exclusive) {
            String holder = EXCLUSIVE_HOLDERS.get(t.name());
            if (holder != null && !holder.equals(uuid)) {
                player.sendSystemMessage(Component.literal("✦ That exclusive Technique already has a holder.")
                        .withStyle(ChatFormatting.RED));
                return false;
            }
        }
        // release any current exclusive first
        Technique cur = getTechnique(uuid);
        if (cur != null && cur.exclusive) EXCLUSIVE_HOLDERS.remove(cur.name());
        assignTechnique(player, t);
        return true;
    }

    public static void renounce(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        Technique t = getTechnique(uuid);
        if (t == null) return;
        if (t.exclusive) EXCLUSIVE_HOLDERS.remove(t.name());
        PlayerTechniqueData d = get(uuid);
        d.technique = null;
        d.selectedAbility = null;
        d.abilityUses.clear();
        save();
        player.sendSystemMessage(Component.literal("✦ You have renounced " + t.displayName + ".")
                .withStyle(ChatFormatting.GRAY));
    }

    /** Exclusive death-forfeit: a slain exclusive holder loses their technique. */
    public static void onPlayerDeath(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        Technique t = getTechnique(uuid);
        if (t == null || !t.exclusive) return;
        EXCLUSIVE_HOLDERS.remove(t.name());
        PlayerTechniqueData d = get(uuid);
        d.technique = null;
        d.selectedAbility = null;
        save();
        player.sendSystemMessage(Component.literal("✦ Death has torn " + t.displayName + " from you. It returns to the world.")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
    }

    // ── Mastery ─────────────────────────────────────────────────

    public static int totalUses(String uuid) {
        int sum = 0;
        for (int v : get(uuid).abilityUses.values()) sum += v;
        return sum;
    }

    public static Technique.MasteryRank getRank(String uuid) {
        return Technique.MasteryRank.forUses(totalUses(uuid));
    }

    public static boolean isAbilityUnlocked(String uuid, Technique.Ability ability) {
        return getRank(uuid).ordinal() >= ability.rankIndex();
    }

    // ── Ability selection ───────────────────────────────────────

    public static void selectAbility(String uuid, String abilityId) {
        get(uuid).selectedAbility = abilityId;
        save();
    }

    public static Technique.Ability getSelectedAbility(String uuid) {
        Technique t = getTechnique(uuid);
        if (t == null) return null;
        String sel = get(uuid).selectedAbility;
        if (sel == null) return t.abilities.get(0);
        Technique.Ability a = t.abilityById(sel);
        return a != null ? a : t.abilities.get(0);
    }

    // ── Technique energy ────────────────────────────────────────

    public static int getMaxEnergy(String uuid) {
        return 100 + MagicManager.getLevel(uuid);
    }

    public static float getEnergy(String uuid) {
        PlayerTechniqueData d = get(uuid);
        long now = System.currentTimeMillis();
        if (d.lastEnergyMs == 0L) d.lastEnergyMs = now;
        long elapsed = now - d.lastEnergyMs;
        if (elapsed > 0) {
            float regen = elapsed / 600f; // ~1 energy / 0.6s
            d.energy = Math.min(getMaxEnergy(uuid), d.energy + regen);
            d.lastEnergyMs = now;
        }
        return d.energy;
    }

    /**
     * True when the player carries the Technique Relic matching their own awakened technique.
     * This is what the relic lore promises ("Ability energy cost: -20%").
     */
    public static boolean hasMatchingRelic(ServerPlayer player) {
        if (player == null) return false;
        Technique t = getTechnique(player.getUUID().toString());
        if (t == null) return false;
        String want = "relic_" + t.name().toLowerCase(Locale.ROOT);
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack st = inv.getItem(i);
            if (st.isEmpty()) continue;
            String id = SlayerItems.getCustomItemId(st);
            if (id == null) continue;
            if (SlayerItems.stripSkyblockEnchantSuffix(id).equals(want)) return true;
        }
        return false;
    }

    private static boolean spendEnergy(ServerPlayer player, int amount) {
        String uuid = player.getUUID().toString();
        int cost = hasMatchingRelic(player) ? Math.max(1, Math.round(amount * 0.8f)) : amount;
        float cur = getEnergy(uuid);
        if (cur < cost) {
            player.sendSystemMessage(Component.literal("✦ Not enough Technique Energy! (" + (int) cur + "/" + cost + ")")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        get(uuid).energy = cur - cost;
        return true;
    }

    // ── Cooldowns ───────────────────────────────────────────────

    private static boolean onCooldown(String uuid, String abilityId) {
        Long end = get(uuid).cooldowns.get(abilityId);
        return end != null && System.currentTimeMillis() < end;
    }

    private static long cooldownRemaining(String uuid, String abilityId) {
        Long end = get(uuid).cooldowns.get(abilityId);
        if (end == null) return 0;
        return Math.max(0, end - System.currentTimeMillis());
    }

    private static void setCooldown(String uuid, Technique.Ability a) {
        get(uuid).cooldowns.put(a.id(), System.currentTimeMillis() + a.cooldownTicks() * 50L);
    }

    // ── Staff sneak-right-click entry point ─────────────────────

    /** Called from {@code MagicSpellHandler.handleStaffUse} when the caster is sneaking. */
    public static boolean handleStaffSneakUse(ServerPlayer player, ItemStack staff) {
        String uuid = player.getUUID().toString();
        Technique t = getTechnique(uuid);
        if (t == null) {
            player.sendSystemMessage(Component.literal("✦ You have not awakened a Technique. Use /awaken at Magic L" + AWAKEN_LEVEL + ".")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        Technique.Ability a = getSelectedAbility(uuid);
        if (a == null) return false;
        if (!isAbilityUnlocked(uuid, a)) {
            player.sendSystemMessage(Component.literal("✦ " + a.name() + " requires "
                    + Technique.MasteryRank.values()[a.rankIndex()].display + " mastery.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        if (onCooldown(uuid, a.id())) {
            player.sendSystemMessage(Component.literal("✦ " + a.name() + " on cooldown: "
                    + (cooldownRemaining(uuid, a.id()) / 1000) + "s").withStyle(ChatFormatting.YELLOW));
            return false;
        }
        if (!spendEnergy(player, a.energyCost())) return false;

        castAbility(player, t, a);
        setCooldown(uuid, a);
        // Mastery XP: one use.
        Technique.MasteryRank before = getRank(uuid);
        PlayerTechniqueData d = get(uuid);
        d.abilityUses.merge(a.id(), 1, Integer::sum);
        Technique.MasteryRank after = getRank(uuid);
        if (after != before) {
            player.sendSystemMessage(Component.literal("✦ " + t.displayName + " mastery: " + after.display + "!")
                    .withStyle(t.color, ChatFormatting.BOLD));
        }
        save();
        return true;
    }

    // ── Ability effects ─────────────────────────────────────────

    private static void castAbility(ServerPlayer player, Technique t, Technique.Ability a) {
        ServerLevel world = (ServerLevel) player.level();
        String uuid = player.getUUID().toString();
        float mastery = 1.0f + getRank(uuid).ordinal() * 0.12f;
        float power = a.power() * mastery;
        double radius = a.radius();
        int dur = a.durationTicks();

        ParticleOptions pcore = techniqueParticle(t);
        world.playSound(null, player.blockPosition(), techniqueSound(t), SoundSource.PLAYERS, 1.0f, 1.0f);
        world.sendParticles(pcore, player.getX(), player.getEyeY(), player.getZ(), 24, 0.5, 0.5, 0.5, 0.05);

        switch (a.archetype()) {
            case BURST -> doBurst(player, world, t, power, pcore);
            case NOVA -> doNova(player, world, power, radius, pcore);
            case BLINK -> doBlink(player, world, power, Math.max(6, radius), pcore);
            case BUFF -> doBuff(player, a, dur);
            case HEAL -> doHeal(player, world, t, a, radius, dur, pcore);
            case SUMMON -> doSummon(player, world, uuid);
            case METEOR -> doMeteor(player, world, power, radius, a.id().contains("barrage") || a.id().contains("time_collapse"));
            case TETHER -> doTether(player, world, power, Math.max(6, radius), pcore);
            case BARRIER -> doBarrier(player, dur);
            case DEVOUR -> doDevour(player, world, power);
            case MIRROR -> doMirror(player, world, power, dur, pcore);
            case ROOT -> doRoot(player, world, power, Math.max(4, radius), dur, pcore);
        }

        player.sendSystemMessage(Component.literal("✦ " + a.name()).withStyle(t.color, ChatFormatting.BOLD)
                .append(Component.literal(" — " + a.description()).withStyle(ChatFormatting.GRAY)));
    }

    private static void doBurst(ServerPlayer player, ServerLevel world, Technique t, float power, ParticleOptions p) {
        LivingEntity target = getTarget(player, 24);
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        for (double d = 0; d <= 24; d += 0.6) {
            Vec3 pt = start.add(look.scale(d));
            world.sendParticles(p, pt.x, pt.y, pt.z, 2, 0.05, 0.05, 0.05, 0.0);
            if (target != null && d >= start.distanceTo(target.getEyePosition())) break;
        }
        if (target == null) {
            player.sendSystemMessage(Component.literal("  No target in range.").withStyle(ChatFormatting.GRAY));
            return;
        }
        float dmg = power;
        if ((t == Technique.DAWNFORGED) && target.getType().getCategory() == MobCategory.MONSTER) dmg *= 1.35f;
        if (t == Technique.UMBRAL_STEP || t == Technique.THREADWEAVER) dmg *= 1.1f; // armor-piercing flavor
        // V4: sonic techniques bypass plate the same way; ash brands its victims.
        if (t == Technique.RESONANT_ECHO || t == Technique.VOICE_OF_RUIN) dmg *= 1.1f;
        if (t == Technique.LODESTONE_HEART) dmg *= 1.05f;
        target.hurt(world.damageSources().magic(), dmg);
        if (t == Technique.SOVEREIGN_OF_ASH) {
            target.setRemainingFireTicks(120);
            world.sendParticles(ParticleTypes.FLAME, target.getX(), target.getEyeY(), target.getZ(), 24, 0.3, 0.4, 0.3, 0.03);
        }
        if (t == Technique.THE_LAST_WINTER) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3));
            target.setTicksFrozen(target.getTicksRequiredToFreeze() + 200);
            world.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getEyeY(), target.getZ(), 24, 0.3, 0.4, 0.3, 0.03);
        }
        if (t == Technique.SPOREBLOOM) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
            world.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getEyeY(), target.getZ(), 20, 0.3, 0.4, 0.3, 0.02);
        }
        world.sendParticles(p, target.getX(), target.getEyeY(), target.getZ(), 20, 0.3, 0.3, 0.3, 0.05);
    }

    private static void doNova(ServerPlayer player, ServerLevel world, float power, double radius, ParticleOptions p) {
        double r = radius <= 0 ? 6 : radius;
        for (Entity e : world.getEntities(player, player.getBoundingBox().inflate(r))) {
            if (e instanceof LivingEntity le && e != player) {
                le.hurt(world.damageSources().magic(), power);
                Vec3 dir = e.position().subtract(player.position()).normalize().scale(1.4);
                e.setDeltaMovement(e.getDeltaMovement().add(dir.x, 0.3, dir.z));
                e.hurtMarked = true;
            }
        }
        for (int i = 0; i < 40; i++) {
            double a = i / 40.0 * Math.PI * 2;
            world.sendParticles(p, player.getX() + Math.cos(a) * r, player.getY() + 0.5, player.getZ() + Math.sin(a) * r, 2, 0.1, 0.1, 0.1, 0.02);
        }
    }

    private static void doBlink(ServerPlayer player, ServerLevel world, float power, double dist, ParticleOptions p) {
        Vec3 from = player.position();
        Vec3 look = player.getLookAngle();
        Vec3 to = from.add(look.scale(dist));
        BlockPos tp = BlockPos.containing(to);
        int safeY = tp.getY();
        for (int y = tp.getY() + 4; y >= tp.getY() - 4; y--) {
            BlockPos c = new BlockPos(tp.getX(), y, tp.getZ());
            if (!world.getBlockState(c).isSolid() && !world.getBlockState(c.above()).isSolid()
                    && world.getBlockState(c.below()).isSolid()) { safeY = y; break; }
        }
        for (double d = 0; d <= dist; d += 0.5) {
            Vec3 pt = from.add(look.scale(d));
            world.sendParticles(p, pt.x, pt.y + 1, pt.z, 3, 0.1, 0.3, 0.1, 0.0);
        }
        player.teleportTo(tp.getX() + 0.5, safeY, tp.getZ() + 0.5);
        player.fallDistance = 0;
        if (power > 0) {
            LivingEntity target = getTarget(player, 4);
            if (target != null) target.hurt(world.damageSources().magic(), power);
        }
    }

    private static void doBuff(ServerPlayer player, Technique.Ability a, int dur) {
        int d = dur <= 0 ? 200 : dur;
        String key = (a.id() + " " + a.name()).toLowerCase();
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, d, 1));
        if (key.contains("phoenix") || key.contains("ember")) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, d, 0));
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, d, 1));
        }
        if (key.contains("overload") || key.contains("tempest") || key.contains("zephyr") || key.contains("deep")) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, d, 1));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, d, 2));
        }
        if (key.contains("tempest")) player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 0));
        if (key.contains("deep") || key.contains("tide")) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, d, 0));
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, d, 0));
        }
        if (key.contains("shroud") || key.contains("umbral") || key.contains("abyssal")) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, d, 0));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, d, 1));
        }
        if (key.contains("bark") || key.contains("verdant") || key.contains("living") || key.contains("bulwark")) {
            player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, d, 2));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, d, 0));
        }
        if (key.contains("ward") || key.contains("astral") || key.contains("crimson")) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, d, 2));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, d, 0));
        }
        // ── V4 technique buffs ──
        if (key.contains("fungal") || key.contains("carapace")) {
            player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, d, 2));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, d, 2));
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 1, 0)); // token spore tick, harmless
        }
        if (key.contains("ferrous")) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, d, 3));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, d, 0));
        }
        if (key.contains("pyre") || key.contains("crown")) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, d, 0));
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, d, 2));
        }
        if (key.contains("winter") || key.contains("mantle")) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, d, 3));
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, d, 1));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, d, 0));
        }

        // Every buff gets a visible + audible tell.
        ServerLevel world = (ServerLevel) player.level();
        for (int i = 0; i < 24; i++) {
            double ang = i / 24.0 * Math.PI * 2;
            world.sendParticles(ParticleTypes.END_ROD,
                    player.getX() + Math.cos(ang) * 1.2, player.getY() + 0.4 + (i % 6) * 0.25,
                    player.getZ() + Math.sin(ang) * 1.2, 1, 0.0, 0.05, 0.0, 0.0);
        }
        world.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.4f);
    }

    private static void doHeal(ServerPlayer player, ServerLevel world, Technique t, Technique.Ability a,
                               double radius, int dur, ParticleOptions p) {
        String uuid = player.getUUID().toString();
        if (a.id().equals("rewind")) {
            PlayerTechniqueData d = get(uuid);
            if (d.hasEcho) {
                player.teleportTo(d.echoX, d.echoY, d.echoZ);
                if (d.echoHealth > 0) player.setHealth(Math.min(player.getMaxHealth(), d.echoHealth));
                d.hasEcho = false;
                player.sendSystemMessage(Component.literal("✦ Rewound to your echo.").withStyle(ChatFormatting.AQUA));
            } else {
                player.heal(8f);
            }
        } else {
            player.heal(player.getMaxHealth());
        }
        double r = radius <= 0 ? 8 : radius;
        int d = dur <= 0 ? 120 : dur;
        for (ServerPlayer nearby : world.getPlayers(pl -> pl.distanceToSqr(player) <= r * r)) {
            nearby.addEffect(new MobEffectInstance(MobEffects.REGENERATION, d, 1));
        }
        world.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1, player.getZ(), 20, 1.0, 1.0, 1.0, 0.0);
    }

    private static void doSummon(ServerPlayer player, ServerLevel world, String uuid) {
        int count = 2 + getRank(uuid).ordinal();
        for (int i = 0; i < count; i++) {
            BlockPos pos = player.blockPosition().offset(-2 + world.getRandom().nextInt(5), 0, -2 + world.getRandom().nextInt(5));
            Zombie z = EntityType.ZOMBIE.spawn(world, pos, EntitySpawnReason.EVENT);
            if (z == null) continue;
            z.setCustomName(Component.literal("✦ Shadow Soldier").withStyle(ChatFormatting.DARK_PURPLE));
            z.setCustomNameVisible(true);
            z.setPersistenceRequired();
            z.addTag("political_thrall");
            var hp = z.getAttribute(Attributes.MAX_HEALTH);
            if (hp != null) { hp.setBaseValue(30); z.setHealth(30); }
            var atk = z.getAttribute(Attributes.ATTACK_DAMAGE);
            if (atk != null) atk.setBaseValue(6);
            z.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0));
            z.addEffect(new MobEffectInstance(MobEffects.GLOWING, 6000, 0));
            world.sendParticles(ParticleTypes.SOUL, z.getX(), z.getY() + 1, z.getZ(), 20, 0.3, 0.5, 0.3, 0.02);
        }
        player.sendSystemMessage(Component.literal("✦ " + count + " soldiers arise!").withStyle(ChatFormatting.DARK_PURPLE));
    }

    private static void doMeteor(ServerPlayer player, ServerLevel world, float power, double radius, boolean barrage) {
        BlockHitResult hit = blockRay(player, 40);
        double x, y, z;
        if (hit != null) {
            BlockPos b = hit.getBlockPos();
            x = b.getX() + 0.5; y = b.getY() + 1; z = b.getZ() + 0.5;
        } else {
            Vec3 t = player.getEyePosition().add(player.getLookAngle().scale(20));
            x = t.x; y = t.y; z = t.z;
        }
        int strikes = barrage ? 5 : 1;
        double r = radius <= 0 ? 5 : radius;
        for (int s = 0; s < strikes; s++) {
            double ox = x + (barrage ? (world.getRandom().nextDouble() - 0.5) * r * 2 : 0);
            double oz = z + (barrage ? (world.getRandom().nextDouble() - 0.5) * r * 2 : 0);
            world.explode(player, ox, y, oz, power / 4f, Level.ExplosionInteraction.NONE);
            world.sendParticles(ParticleTypes.EXPLOSION, ox, y, oz, 4, 1, 1, 1, 0.0);
            for (Entity e : world.getEntities(player, new AABB(ox - r, y - 3, oz - r, ox + r, y + 4, oz + r))) {
                if (e instanceof LivingEntity le && e != player) {
                    le.hurt(world.damageSources().magic(), power);
                    le.setRemainingFireTicks(40);
                }
            }
        }
    }

    private static void doTether(ServerPlayer player, ServerLevel world, float power, double radius, ParticleOptions p) {
        for (Entity e : world.getEntities(player, player.getBoundingBox().inflate(radius))) {
            if (e instanceof LivingEntity le && e != player) {
                Vec3 pull = player.position().subtract(e.position()).normalize().scale(1.2);
                e.setDeltaMovement(pull.x, 0.25, pull.z);
                e.hurtMarked = true;
                if (power > 0) le.hurt(world.damageSources().magic(), power);
                Vec3 mid = e.position().add(player.position()).scale(0.5);
                world.sendParticles(p, mid.x, mid.y + 1, mid.z, 6, 0.2, 0.2, 0.2, 0.0);
            }
        }
    }

    private static void doBarrier(ServerPlayer player, int dur) {
        int d = dur <= 0 ? 60 : dur;
        BARRIER_UNTIL.put(player.getUUID().toString(), System.currentTimeMillis() + d * 50L);
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, d, 4));
        ServerLevel world = (ServerLevel) player.level();
        for (int i = 0; i < 30; i++) {
            double a = i / 30.0 * Math.PI * 2;
            world.sendParticles(ParticleTypes.END_ROD, player.getX() + Math.cos(a) * 1.5, player.getY() + 1,
                    player.getZ() + Math.sin(a) * 1.5, 1, 0.0, 0.2, 0.0, 0.0);
        }
        player.sendSystemMessage(Component.literal("✦ You are untouchable for " + (d / 20.0) + "s.")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    private static void doDevour(ServerPlayer player, ServerLevel world, float power) {
        LivingEntity target = getTarget(player, 14);
        if (target == null) {
            player.sendSystemMessage(Component.literal("  No prey in range.").withStyle(ChatFormatting.GRAY));
            return;
        }
        float before = target.getHealth();
        target.hurt(world.damageSources().magic(), power);
        float dealt = before - target.getHealth();
        player.heal(Math.max(2f, dealt * 0.5f));
        world.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getEyeY(), target.getZ(), 24, 0.3, 0.3, 0.3, 0.02);
        world.playSound(null, target.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0f, 0.6f);
    }

    private static void doMirror(ServerPlayer player, ServerLevel world, float power, int dur, ParticleOptions p) {
        String uuid = player.getUUID().toString();
        int d = dur <= 0 ? 100 : dur;
        MIRROR_UNTIL.put(uuid, System.currentTimeMillis() + d * 50L);
        if (power > 0) {
            UUID att = LAST_ATTACKER.get(uuid);
            if (att != null) {
                Entity e = world.getEntity(att);
                if (e instanceof LivingEntity le) {
                    le.hurt(world.damageSources().magic(), power);
                    world.sendParticles(p, le.getX(), le.getEyeY(), le.getZ(), 20, 0.3, 0.3, 0.3, 0.05);
                }
            }
        }
        player.sendSystemMessage(Component.literal("✦ Reflection active for " + (d / 20.0) + "s.")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    private static void doRoot(ServerPlayer player, ServerLevel world, float power, double radius, int dur, ParticleOptions p) {
        int d = dur <= 0 ? 80 : dur;
        for (Entity e : world.getEntities(player, player.getBoundingBox().inflate(radius))) {
            if (e instanceof LivingEntity le && e != player) {
                le.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, d, 6));
                le.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, d, 3));
                le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, d, 1));
                if (power > 0) le.hurt(world.damageSources().magic(), power);
                world.sendParticles(p, e.getX(), e.getY(), e.getZ(), 10, 0.2, 0.1, 0.2, 0.0);
            }
        }
    }

    // ── Echo recording (Chronoshear "Echo" ability) ─────────────

    /** Special-cased so the Echo ability records rather than heals. */
    public static void recordEcho(ServerPlayer player) {
        PlayerTechniqueData d = get(player.getUUID().toString());
        d.echoX = player.getX();
        d.echoY = player.getY();
        d.echoZ = player.getZ();
        d.echoHealth = player.getHealth();
        d.hasEcho = true;
    }

    // ── Combat-mixin hooks ──────────────────────────────────────

    public static boolean isDamageNegated(String uuid) {
        Long until = BARRIER_UNTIL.get(uuid);
        return until != null && System.currentTimeMillis() < until;
    }

    public static boolean isMirrorActive(String uuid) {
        Long until = MIRROR_UNTIL.get(uuid);
        return until != null && System.currentTimeMillis() < until;
    }

    /** Called from {@code TechniqueCombatMixin} at the HEAD of the player's damage path. */
    public static void onIncomingDamage(ServerPlayer victim, LivingEntity attacker, float amount) {
        String uuid = victim.getUUID().toString();
        LAST_ATTACKER.put(uuid, attacker.getUUID());
        if (isMirrorActive(uuid)) {
            MIRROR_UNTIL.remove(uuid);
            ServerLevel world = (ServerLevel) victim.level();
            attacker.hurt(world.damageSources().magic(), amount * 0.75f);
            world.sendParticles(ParticleTypes.GLOW, attacker.getX(), attacker.getEyeY(), attacker.getZ(), 16, 0.3, 0.3, 0.3, 0.05);
            victim.sendSystemMessage(Component.literal("✦ Reflected!").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    // ── Themed particles / sounds ───────────────────────────────

    private static ParticleOptions techniqueParticle(Technique t) {
        return switch (t) {
            case EMBERHEART, STARFALL_SOVEREIGN -> ParticleTypes.FLAME;
            case GLACIER_SOUL -> ParticleTypes.SNOWFLAKE;
            case STORM_VEIN -> ParticleTypes.ELECTRIC_SPARK;
            case UMBRAL_STEP, SHADOW_MONARCH, ABYSSAL_MAW -> ParticleTypes.SCULK_SOUL;
            case CRIMSON_PACT -> ParticleTypes.DUST_PLUME;
            case WILDGROWTH, WORLDROOT_AVATAR -> ParticleTypes.HAPPY_VILLAGER;
            case DAWNFORGED -> ParticleTypes.END_ROD;
            case IRONMARROW -> ParticleTypes.CRIT;
            case TIDEBOUND -> ParticleTypes.SPLASH;
            case WHISPERWIND, THREADWEAVER -> ParticleTypes.CLOUD;
            case HOLLOW_INFINITY, MIRROR_SOVEREIGN -> ParticleTypes.PORTAL;
            case CHRONOSHEAR -> ParticleTypes.ENCHANT;
            // ── V4 additions ──
            case RESONANT_ECHO -> ParticleTypes.NOTE;
            case SPOREBLOOM -> ParticleTypes.SPORE_BLOSSOM_AIR;
            case LODESTONE_HEART -> ParticleTypes.CRIT;
            case SOVEREIGN_OF_ASH -> ParticleTypes.SMOKE;
            case THE_LAST_WINTER -> ParticleTypes.SNOWFLAKE;
            case VOICE_OF_RUIN -> ParticleTypes.SCULK_SOUL;
            default -> ParticleTypes.ENCHANT;
        };
    }

    private static net.minecraft.sounds.SoundEvent techniqueSound(Technique t) {
        return switch (t) {
            case EMBERHEART, STARFALL_SOVEREIGN -> SoundEvents.BLAZE_SHOOT;
            case GLACIER_SOUL -> SoundEvents.GLASS_BREAK;
            case STORM_VEIN -> SoundEvents.LIGHTNING_BOLT_IMPACT;
            case UMBRAL_STEP, SHADOW_MONARCH, ABYSSAL_MAW, HOLLOW_INFINITY -> SoundEvents.ENDERMAN_TELEPORT;
            case CRIMSON_PACT -> SoundEvents.PLAYER_ATTACK_CRIT;
            case WILDGROWTH, WORLDROOT_AVATAR -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case DAWNFORGED -> SoundEvents.BEACON_ACTIVATE;
            case IRONMARROW -> SoundEvents.ANVIL_LAND;
            case TIDEBOUND -> SoundEvents.PLAYER_SPLASH;
            case WHISPERWIND, THREADWEAVER -> SoundEvents.ELYTRA_FLYING;
            case CHRONOSHEAR -> SoundEvents.ENCHANTMENT_TABLE_USE;
            case MIRROR_SOVEREIGN -> SoundEvents.AMETHYST_CLUSTER_BREAK;
            // ── V4 additions ──
            case RESONANT_ECHO -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case SPOREBLOOM -> SoundEvents.AMETHYST_CLUSTER_BREAK;
            case LODESTONE_HEART -> SoundEvents.ANVIL_LAND;
            case SOVEREIGN_OF_ASH -> SoundEvents.BLAZE_SHOOT;
            case THE_LAST_WINTER -> SoundEvents.GLASS_BREAK;
            case VOICE_OF_RUIN -> SoundEvents.WARDEN_HEARTBEAT;
            default -> SoundEvents.ENCHANTMENT_TABLE_USE;
        };
    }

    // ── Targeting utilities (mirrors MagicSpellHandler) ─────────

    private static LivingEntity getTarget(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(range));
        AABB area = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        LivingEntity closest = null;
        double best = range * range;
        for (Entity e : player.level().getEntities(player, area)) {
            if (!(e instanceof LivingEntity le) || e == player) continue;
            if (le.getType() == EntityType.ZOMBIE && le.entityTags().contains("political_thrall")) continue;
            AABB bb = e.getBoundingBox().inflate(0.3);
            Optional<Vec3> hit = bb.clip(start, end);
            if (hit.isPresent()) {
                double d = start.distanceToSqr(hit.get());
                if (d < best) { best = d; closest = le; }
            }
        }
        return closest;
    }

    private static BlockHitResult blockRay(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(range));
        ClipContext ctx = new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        BlockHitResult r = player.level().clip(ctx);
        return r.getType() == HitResult.Type.BLOCK ? r : null;
    }

    private static void performRiteEffects(ServerPlayer player, Technique t) {
        ServerLevel world = (ServerLevel) player.level();
        world.sendParticles(techniqueParticle(t), player.getX(), player.getY() + 1, player.getZ(), 80, 0.6, 1.0, 0.6, 0.1);
        world.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
        String rarity = t.exclusive ? "§d§lEXCLUSIVE" : "§bCommon";
        player.sendSystemMessage(Component.literal("✦ RITE OF AWAKENING ✦").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("You have awakened: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(t.displayName).withStyle(t.color, ChatFormatting.BOLD)));
        player.sendSystemMessage(Component.literal(rarity + " §7— " + t.flavor));
    }
}
