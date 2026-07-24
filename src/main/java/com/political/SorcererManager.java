package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * V6.6 — The Sorcerer Path. Per-player Jujutsu Sorcerer progression: grade, cursed energy,
 * binding vows, Reverse Cursed Technique (a paid healing toggle), and Black Flash (a timed-hit
 * critical that builds a combo meter). This is the human answer to the cursed spirits.
 *
 * <p>Persisted to {@code sorcerer.json}. All combat behaviour is enforced through this class's
 * own Fabric event listeners — no vanilla or existing combat class is modified.</p>
 */
public final class SorcererManager {

    private SorcererManager() {}

    // ── Tunables ────────────────────────────────────────────────
    public static final double BASE_MAX_CE = 100.0;
    public static final double BASE_REGEN_PER_SEC = 2.0;
    private static final double BF_BASE_CHANCE = 0.10;
    private static final long BF_SWEET_MIN_MS = 350;
    private static final long BF_SWEET_MAX_MS = 900;
    private static final int COMBO_MAX = 10;
    private static final long COMBO_DECAY_MS = 4000;
    private static final double BF_CE_REWARD = 10.0;
    private static final int RCT_INTERVAL_TICKS = 10;
    private static final float RCT_HEAL_HP = 2.0f;      // one heart per tick-window
    private static final double RCT_CE_PER_HEAL = 8.0;  // paid healing — never free sustain
    private static final double SPIRIT_CE_REWARD = 3.0;

    private static final Identifier HEALTH_MOD_ID = Identifier.fromNamespaceAndPath("political", "sorcerer_health");
    private static final Identifier SPEED_MOD_ID = Identifier.fromNamespaceAndPath("political", "sorcerer_speed");

    // ── Data ────────────────────────────────────────────────────

    public static final class PlayerSorcerer {
        public boolean enrolled = false;
        public String rank = SorcererRank.GRADE_4.name();
        public int spiritsSlain = 0;
        public long ceSpent = 0L;
        public double cursedEnergy = BASE_MAX_CE;
        public boolean reverseCursedActive = false;
        public List<String> vows = new ArrayList<>();
        public boolean claimedSpecialTool = false;
        public long lastDomainMs = 0L;
        public long domainUntilTick = 0L;
        // transient-ish combat state
        public int comboCount = 0;
        public long lastMeleeMs = 0L;
    }

    private static final class SaveFile {
        Map<String, PlayerSorcerer> players = new HashMap<>();
    }

    private static final Map<String, PlayerSorcerer> DATA = new ConcurrentHashMap<>();
    private static Path dataFile;
    /** Guards the bonus-damage we deal inside AFTER_DAMAGE from recursively re-triggering. */
    private static boolean reentrant = false;

    // ── Persistence ─────────────────────────────────────────────

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("sorcerer.json");
        load();
    }

    private static void load() {
        if (dataFile == null || !Files.exists(dataFile)) return;
        try {
            String content = Files.readString(dataFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type type = new TypeToken<SaveFile>() {}.getType();
            SaveFile sf = gson.fromJson(content, type);
            if (sf != null && sf.players != null) {
                DATA.clear();
                DATA.putAll(sf.players);
                for (PlayerSorcerer p : DATA.values()) {
                    if (p.vows == null) p.vows = new ArrayList<>();
                }
            }
        } catch (Exception e) {
            System.err.println("[SorcererManager] Failed to load: " + e.getMessage());
        }
    }

    public static void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            SaveFile sf = new SaveFile();
            sf.players = new HashMap<>(DATA);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (java.io.Writer w = Files.newBufferedWriter(dataFile)) {
                gson.toJson(sf, w);
            }
        } catch (Exception e) {
            System.err.println("[SorcererManager] Failed to save: " + e.getMessage());
        }
    }

    // ── Accessors ───────────────────────────────────────────────

    public static PlayerSorcerer getOrNull(String uuid) {
        return DATA.get(uuid);
    }

    public static boolean isEnrolled(String uuid) {
        PlayerSorcerer p = DATA.get(uuid);
        return p != null && p.enrolled;
    }

    public static SorcererRank rankOf(String uuid) {
        PlayerSorcerer p = DATA.get(uuid);
        return p == null ? SorcererRank.GRADE_4 : SorcererRank.byName(p.rank);
    }

    public static List<BindingVow> activeVows(PlayerSorcerer p) {
        List<BindingVow> out = new ArrayList<>();
        if (p == null) return out;
        for (String id : p.vows) {
            BindingVow v = BindingVow.byId(id);
            if (v != null) out.add(v);
        }
        return out;
    }

    // ── Enrolment ───────────────────────────────────────────────

    public static boolean enroll(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        PlayerSorcerer p = DATA.computeIfAbsent(uuid, k -> new PlayerSorcerer());
        if (p.enrolled) return false;
        p.enrolled = true;
        p.rank = SorcererRank.GRADE_4.name();
        p.cursedEnergy = maxCe(p);
        save();
        player.sendSystemMessage(Component.literal("✦ You have awakened as a Jujutsu Sorcerer — ")
                .withStyle(ChatFormatting.LIGHT_PURPLE).append(SorcererRank.GRADE_4.title()));
        player.sendSystemMessage(Component.literal("§7Slay cursed spirits and spend cursed energy to rise in grade."));
        return true;
    }

    // ── Derived values ──────────────────────────────────────────

    public static double maxCe(PlayerSorcerer p) {
        SorcererRank rank = SorcererRank.byName(p.rank);
        double max = BASE_MAX_CE + rank.maxCeBonus;
        for (BindingVow v : activeVows(p)) max *= v.maxCeMult;
        return max;
    }

    public static double regenPerSec(PlayerSorcerer p) {
        SorcererRank rank = SorcererRank.byName(p.rank);
        double regen = BASE_REGEN_PER_SEC * rank.regenMult;
        for (BindingVow v : activeVows(p)) regen *= v.regenMult;
        return regen;
    }

    /** Total passive melee bonus fraction: rank + vows (capped) + current combo. */
    public static double meleeBonus(PlayerSorcerer p) {
        SorcererRank rank = SorcererRank.byName(p.rank);
        double vowBonus = 0.0;
        for (BindingVow v : activeVows(p)) vowBonus += v.meleeBonus;
        vowBonus = Math.min(0.50, vowBonus);
        double combo = Math.min(COMBO_MAX, p.comboCount) * 0.02;
        return rank.meleePct + vowBonus + combo;
    }

    private static double bfChanceMult(PlayerSorcerer p) {
        double m = 1.0;
        for (BindingVow v : activeVows(p)) m *= v.bfChanceMult;
        return m;
    }

    private static double bfBonusMult(PlayerSorcerer p) {
        double m = 1.0;
        for (BindingVow v : activeVows(p)) m *= v.bfBonusMult;
        return m;
    }

    private static int bfHeartCost(PlayerSorcerer p) {
        int c = 0;
        for (BindingVow v : activeVows(p)) c += v.bfHeartCost;
        return c;
    }

    private static int comboIncrement(PlayerSorcerer p) {
        double rate = 1.0;
        for (BindingVow v : activeVows(p)) rate *= v.comboRateMult;
        return (int) Math.max(1, Math.round(rate));
    }

    public static boolean rctDisabled(PlayerSorcerer p) {
        for (BindingVow v : activeVows(p)) if (v.disablesRct) return true;
        return false;
    }

    public static boolean domainDisabled(PlayerSorcerer p) {
        for (BindingVow v : activeVows(p)) if (v.disablesDomain) return true;
        return false;
    }

    // ── Cursed energy sink ──────────────────────────────────────

    /** Spend cursed energy. Returns false (and changes nothing) if there isn't enough. */
    public static boolean spendCe(ServerPlayer player, double amount) {
        PlayerSorcerer p = DATA.get(player.getUUID().toString());
        if (p == null || !p.enrolled) return false;
        if (p.cursedEnergy < amount) return false;
        p.cursedEnergy -= amount;
        p.ceSpent += (long) Math.ceil(amount);
        checkRankUp(player, p);
        return true;
    }

    // ── Vows ────────────────────────────────────────────────────

    public static boolean hasVow(PlayerSorcerer p, BindingVow vow) {
        return p != null && p.vows.contains(vow.id);
    }

    /** Toggle a binding vow. Adding one is limited by the rank's vow slots. */
    public static void toggleVow(ServerPlayer player, BindingVow vow) {
        PlayerSorcerer p = DATA.get(player.getUUID().toString());
        if (p == null || !p.enrolled) return;
        if (p.vows.contains(vow.id)) {
            p.vows.remove(vow.id);
            player.sendSystemMessage(Component.literal("§7Binding vow released: §f" + vow.display));
        } else {
            int slots = SorcererRank.byName(p.rank).vowSlots;
            if (p.vows.size() >= slots) {
                player.sendSystemMessage(Component.literal("§cYou can hold only " + slots
                        + " binding vow(s) at your grade.").withStyle(ChatFormatting.RED));
                return;
            }
            p.vows.add(vow.id);
            player.sendSystemMessage(Component.literal("§dBinding vow sworn: §f" + vow.display));
        }
        // Vows may disable RCT — turn it off if it just became illegal.
        if (p.reverseCursedActive && rctDisabled(p)) p.reverseCursedActive = false;
        save();
    }

    // ── Reverse Cursed Technique ────────────────────────────────

    public static void toggleReverseCursed(ServerPlayer player) {
        PlayerSorcerer p = DATA.get(player.getUUID().toString());
        if (p == null || !p.enrolled) return;
        if (rctDisabled(p)) {
            player.sendSystemMessage(Component.literal("§cA binding vow forbids your Reverse Cursed Technique.")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        p.reverseCursedActive = !p.reverseCursedActive;
        player.sendSystemMessage(Component.literal(p.reverseCursedActive
                ? "§d✦ Reverse Cursed Technique: §aON §7(heals while you hold cursed energy)"
                : "§d✦ Reverse Cursed Technique: §7OFF"));
        save();
    }

    // ── Special-grade tool claim ────────────────────────────────

    public static boolean claimSpecialTool(ServerPlayer player) {
        PlayerSorcerer p = DATA.get(player.getUUID().toString());
        if (p == null || !p.enrolled) return false;
        if (!SorcererRank.byName(p.rank).isAtLeast(SorcererRank.SPECIAL_GRADE)) {
            player.sendSystemMessage(Component.literal("§cOnly a Special Grade sorcerer may claim it.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        if (p.claimedSpecialTool) {
            player.sendSystemMessage(Component.literal("§7You have already claimed your Special Grade tool."));
            return false;
        }
        ItemStack tool = CursedToolFactory.create(CursedTool.SPECIAL_GRADE);
        if (!player.getInventory().add(tool)) player.drop(tool, false);
        p.claimedSpecialTool = true;
        save();
        player.sendSystemMessage(Component.literal("§d✦ You receive §f" + CursedTool.SPECIAL_GRADE.display));
        return true;
    }

    // ── Rank progression ────────────────────────────────────────

    private static void checkRankUp(ServerPlayer player, PlayerSorcerer p) {
        int mastery = TechniqueManager.totalUses(player.getUUID().toString());
        SorcererRank cur = SorcererRank.byName(p.rank);
        SorcererRank next = cur.next();
        boolean promoted = false;
        while (next != null && next.meetsRequirements(p.spiritsSlain, p.ceSpent, mastery)) {
            p.rank = next.name();
            promoted = true;
            SorcererRank finalNext = next;
            player.sendSystemMessage(Component.literal("§6§l✦ PROMOTION! §r§7You are now ")
                    .append(finalNext.title()));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1, player.getZ(),
                        60, 0.5, 1.0, 0.5, 0.2);
                sl.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
                        SoundSource.PLAYERS, 1.0f, 0.6f);
            }
            cur = next;
            next = cur.next();
        }
        if (promoted) save();
    }

    // ── Per-player tick (from PoliticalServer world/player loop) ─

    public static void tickPlayer(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        PlayerSorcerer p = DATA.get(uuid);
        if (p == null || !p.enrolled) return;

        long now = System.currentTimeMillis();
        double max = maxCe(p);
        if (p.cursedEnergy > max) p.cursedEnergy = max;
        if (p.cursedEnergy < max) {
            p.cursedEnergy = Math.min(max, p.cursedEnergy + regenPerSec(p) / 20.0);
        }

        if (p.comboCount > 0 && now - p.lastMeleeMs > COMBO_DECAY_MS) p.comboCount = 0;

        // Reverse Cursed Technique — paid healing.
        if (p.reverseCursedActive && !rctDisabled(p) && player.tickCount % RCT_INTERVAL_TICKS == 0) {
            float missing = player.getMaxHealth() - player.getHealth();
            if (missing > 0.05f) {
                if (p.cursedEnergy >= RCT_CE_PER_HEAL) {
                    float heal = Math.min(RCT_HEAL_HP, missing);
                    p.cursedEnergy -= RCT_CE_PER_HEAL;
                    p.ceSpent += (long) RCT_CE_PER_HEAL;
                    player.heal(heal);
                    if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.2, player.getZ(),
                                2, 0.3, 0.3, 0.3, 0.0);
                    }
                } else {
                    p.reverseCursedActive = false;
                    player.sendSystemMessage(Component.literal("§7Your cursed energy runs dry — Reverse Cursed Technique fades.")
                            .withStyle(ChatFormatting.GRAY));
                }
            }
        }

        // Domain Expansion active-tick.
        PlayerDomainExpansion.tick(player, p);

        // Attributes + mastery-based rank check every second.
        if (player.tickCount % 20 == 0) {
            applyAttributes(player, p);
            checkRankUp(player, p);
        }
    }

    private static void applyAttributes(ServerPlayer player, PlayerSorcerer p) {
        SorcererRank rank = SorcererRank.byName(p.rank);
        double health = rank.healthPct;
        double speed = 0.0;
        for (BindingVow v : activeVows(p)) {
            health += v.healthMult;
            speed += v.speedMult;
        }
        applyModifier(player.getAttribute(Attributes.MAX_HEALTH), HEALTH_MOD_ID, health,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        applyModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_MOD_ID, speed,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    private static void applyModifier(AttributeInstance inst, Identifier id, double value,
                                      AttributeModifier.Operation op) {
        if (inst == null) return;
        inst.removeModifier(id);
        if (value != 0.0) inst.addTransientModifier(new AttributeModifier(id, value, op));
    }

    // ── Event registration (call once from mod init) ────────────

    public static void registerEvents() {
        // Count cursed spirits (any hostile) slain by an enrolled sorcerer.
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer) return;
            if (!(entity instanceof Enemy)) return;
            Entity killer = source.getEntity();
            if (killer instanceof ServerPlayer p) onSpiritSlain(p);
        });

        // Melee bonus, Black Flash, and cursed-tool signatures.
        ServerLivingEntityEvents.AFTER_DAMAGE.register(SorcererManager::onAfterDamage);

        // Hollow Wicker Basket: a vow-holder's projectiles deal no damage.
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            Entity shooter = source.getEntity();
            Entity direct = source.getDirectEntity();
            if (shooter instanceof ServerPlayer p
                    && direct instanceof net.minecraft.world.entity.projectile.Projectile) {
                PlayerSorcerer ps = DATA.get(p.getUUID().toString());
                if (ps != null && ps.enrolled) {
                    for (BindingVow v : activeVows(ps)) {
                        if (v.cancelsProjectiles) return false;
                    }
                }
            }
            return true;
        });
    }

    private static void onSpiritSlain(ServerPlayer player) {
        PlayerSorcerer p = DATA.get(player.getUUID().toString());
        if (p == null || !p.enrolled) return;
        p.spiritsSlain++;
        p.cursedEnergy = Math.min(maxCe(p), p.cursedEnergy + SPIRIT_CE_REWARD);
        checkRankUp(player, p);
    }

    private static void onAfterDamage(LivingEntity target, DamageSource source,
                                      float baseDamageTaken, float damageTaken, boolean blocked) {
        if (reentrant || blocked || damageTaken <= 0.0f) return;
        Entity atk = source.getEntity();
        if (!(atk instanceof ServerPlayer p) || atk == target) return;
        if (source.getDirectEntity() != atk) return; // projectile / indirect → not a melee hit
        PlayerSorcerer ps = DATA.get(p.getUUID().toString());
        if (ps == null || !ps.enrolled) return;
        if (!(p.level() instanceof ServerLevel sl)) return;

        long now = System.currentTimeMillis();
        long dt = now - ps.lastMeleeMs;
        ps.lastMeleeMs = now;

        boolean rhythmic = dt >= BF_SWEET_MIN_MS && dt <= BF_SWEET_MAX_MS;
        if (dt < BF_SWEET_MIN_MS) ps.comboCount = 0; // spam / autoclick breaks the rhythm
        if (rhythmic) ps.comboCount = Math.min(COMBO_MAX, ps.comboCount + comboIncrement(ps));

        boolean blackFlash = rhythmic && p.getRandom().nextDouble() < BF_BASE_CHANCE * bfChanceMult(ps);

        reentrant = true;
        try {
            // Passive melee bonus (rank + vows + combo), dealt as extra magic damage.
            float bonus = damageTaken * (float) meleeBonus(ps);
            if (bonus > 0.01f && target.isAlive()) {
                target.invulnerableTime = 0;
                target.hurtServer(sl, p.damageSources().magic(), bonus);
            }

            applyCursedToolSignature(p, sl, target, damageTaken);

            if (blackFlash) {
                float bf = damageTaken * (float) (0.5 * bfBonusMult(ps));
                if (target.isAlive()) {
                    target.invulnerableTime = 0;
                    target.hurtServer(sl, p.damageSources().magic(), bf);
                }
                ps.cursedEnergy = Math.min(maxCe(ps), ps.cursedEnergy + BF_CE_REWARD);
                int hearts = bfHeartCost(ps);
                if (hearts > 0) p.hurtServer(sl, p.damageSources().magic(), hearts * 2.0f);
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT,
                        target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                        18, 0.35, 0.35, 0.35, 0.25);
                sl.sendParticles(ParticleTypes.SONIC_BOOM,
                        target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                        1, 0.0, 0.0, 0.0, 0.0);
                sl.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
                        SoundSource.PLAYERS, 1.2f, 0.5f);
                p.sendSystemMessage(Component.literal("§c§l⚡ BLACK FLASH! §r§7combo §fx" + ps.comboCount), true);
            }
        } finally {
            reentrant = false;
        }
    }

    private static void applyCursedToolSignature(ServerPlayer p, ServerLevel sl, LivingEntity target, float dmg) {
        CursedTool tool = CursedTool.of(p.getMainHandItem());
        if (tool == null) return;
        switch (tool) {
            case GRADE_4 -> {
                if (p.getRandom().nextDouble() < 0.20) {
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, true, false, true));
                }
            }
            case GRADE_3 -> {
                if (target.isAlive()) {
                    target.invulnerableTime = 0;
                    target.hurtServer(sl, p.damageSources().magic(), dmg * 0.15f);
                }
            }
            case GRADE_2 -> {
                if (p.getRandom().nextDouble() < 0.25 && target.isAlive()) {
                    target.invulnerableTime = 0;
                    target.hurtServer(sl, p.damageSources().magic(), dmg * 0.5f);
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK,
                            target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                            2, 0.1, 0.1, 0.1, 0.0);
                }
            }
            case GRADE_1 -> {
                for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class,
                        target.getBoundingBox().inflate(3.0), le -> le != p && le.isAlive())) {
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, true, false, true));
                    e.knockback(0.6, p.getX() - e.getX(), p.getZ() - e.getZ());
                }
                sl.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 0.5, target.getZ(),
                        1, 0.0, 0.0, 0.0, 0.0);
            }
            case SPECIAL_GRADE -> {
                if (target.isAlive()) {
                    target.invulnerableTime = 0;
                    target.hurtServer(sl, p.damageSources().magic(), dmg * 0.30f);
                }
                for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class,
                        target.getBoundingBox().inflate(4.0), le -> le != p && le != target && le.isAlive())) {
                    e.invulnerableTime = 0;
                    e.hurtServer(sl, p.damageSources().magic(), dmg * 0.40f);
                }
                sl.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + 0.8, target.getZ(),
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }
}
