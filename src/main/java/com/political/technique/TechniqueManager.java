package com.political.technique;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Learned techniques. Players start Novice from a Technique Manual, rank up by casting,
 * and may equip one technique at a time. Each ability has its own handler.
 */
public final class TechniqueManager {

    private TechniqueManager() {}

    public static class PlayerTechniqueData {
        public Set<String> learned = new HashSet<>();
        public String equipped = null;
        public Map<String, Integer> usesByTechnique = new HashMap<>();
        public String selectedAbility = null;
        public float energy = 100f;
        public long lastEnergyMs = 0L;
        public Map<String, Long> cooldowns = new HashMap<>();
        /** Legacy field from the awakening system; migrated into {@link #equipped}. */
        public String technique = null;
        public Map<String, Integer> abilityUses = new HashMap<>();
    }

    private static class SaveFile {
        Map<String, PlayerTechniqueData> players = new HashMap<>();
    }

    private static final Map<String, PlayerTechniqueData> DATA = new ConcurrentHashMap<>();
    private static final Map<String, Long> BARRIER_UNTIL = new ConcurrentHashMap<>();
    private static Path dataFile;

    public static void initialize(Path worldRoot) {
        dataFile = worldRoot.resolve("techniques.json");
        load();
    }

    private static void load() {
        if (dataFile == null || !Files.exists(dataFile)) return;
        try {
            String content = Files.readString(dataFile);
            Gson gson = new GsonBuilder().create();
            Type type = new TypeToken<SaveFile>() {}.getType();
            SaveFile save = gson.fromJson(content, type);
            if (save != null && save.players != null) {
                DATA.clear();
                DATA.putAll(save.players);
                for (PlayerTechniqueData data : DATA.values()) {
                    migrate(data);
                }
            }
        } catch (Exception e) {
            System.err.println("[TechniqueManager] Failed to load: " + e.getMessage());
        }
    }

    private static void migrate(PlayerTechniqueData data) {
        if (data.learned == null) data.learned = new HashSet<>();
        if (data.usesByTechnique == null) data.usesByTechnique = new HashMap<>();
        if (data.cooldowns == null) data.cooldowns = new HashMap<>();
        if (data.equipped == null && data.technique != null) {
            Technique old = Technique.byName(data.technique);
            if (old != null) {
                data.learned.add(old.name());
                data.equipped = old.name();
            } else {
                data.technique = null;
            }
        }
        if (data.abilityUses != null && !data.abilityUses.isEmpty() && data.equipped != null) {
            int total = 0;
            for (int v : data.abilityUses.values()) total += v;
            data.usesByTechnique.merge(data.equipped, total, Integer::sum);
            data.abilityUses.clear();
        }
    }

    public static void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            SaveFile save = new SaveFile();
            save.players = new HashMap<>(DATA);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (java.io.Writer w = Files.newBufferedWriter(dataFile)) {
                gson.toJson(save, w);
            }
        } catch (Exception e) {
            System.err.println("[TechniqueManager] Failed to save: " + e.getMessage());
        }
    }

    public static PlayerTechniqueData get(String uuid) {
        PlayerTechniqueData data = DATA.computeIfAbsent(uuid, k -> new PlayerTechniqueData());
        migrate(data);
        return data;
    }

    public static boolean hasTechnique(String uuid) {
        return getTechnique(uuid) != null;
    }

    public static Technique getTechnique(String uuid) {
        return Technique.byName(get(uuid).equipped);
    }

    public static Set<Technique> learned(String uuid) {
        Set<Technique> out = new HashSet<>();
        for (String name : get(uuid).learned) {
            Technique t = Technique.byName(name);
            if (t != null) out.add(t);
        }
        return out;
    }

    public static Technique.MasteryRank getRank(String uuid) {
        Technique t = getTechnique(uuid);
        if (t == null) return Technique.MasteryRank.NOVICE;
        return Technique.MasteryRank.forUses(usesOf(uuid, t));
    }

    public static int usesOf(String uuid, Technique technique) {
        return get(uuid).usesByTechnique.getOrDefault(technique.name(), 0);
    }

    public static int totalUses(String uuid) {
        Technique t = getTechnique(uuid);
        return t == null ? 0 : usesOf(uuid, t);
    }

    public static float getEnergy(String uuid) {
        regen(uuid);
        return get(uuid).energy;
    }

    public static int getMaxEnergy(String uuid) {
        return 80 + getRank(uuid).ordinal() * 20;
    }

    public static Technique.Ability getSelectedAbility(String uuid) {
        Technique t = getTechnique(uuid);
        if (t == null) return null;
        PlayerTechniqueData data = get(uuid);
        Technique.Ability selected = t.abilityById(data.selectedAbility);
        return selected != null ? selected : t.abilities.get(0);
    }

    public static boolean isAbilityUnlocked(String uuid, Technique.Ability ability) {
        return getRank(uuid).ordinal() >= ability.rankIndex();
    }

    public static void selectAbility(String uuid, String abilityId) {
        PlayerTechniqueData data = get(uuid);
        data.selectedAbility = abilityId;
        save();
    }

    public static boolean absorbOnHurt(ServerPlayer player, float amount) {
        Long until = BARRIER_UNTIL.get(player.getUUID().toString());
        return until != null && System.currentTimeMillis() < until;
    }

    public static void tick(ServerPlayer player) {
        regen(player.getUUID().toString());
    }

    public static boolean tryUseFromItem(ServerPlayer player, ItemStack stack) {
        Technique fromManual = TechniqueItems.fromStack(stack);
        if (fromManual == null) return false;
        if (learn(player, fromManual)) {
            stack.shrink(1);
        }
        return true;
    }

    public static void learnFromHeldManual(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        Technique fromManual = TechniqueItems.fromStack(held);
        if (fromManual == null) {
            player.sendSystemMessage(Component.literal("Hold a Technique Manual to learn it.")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        if (learn(player, fromManual)) {
            held.shrink(1);
        }
    }

    public static boolean learnFromManual(ServerPlayer player, Technique technique) {
        return learn(player, technique);
    }

    public static boolean learn(ServerPlayer player, Technique technique) {
        if (technique == null) return false;
        String uuid = player.getUUID().toString();
        PlayerTechniqueData data = get(uuid);
        if (data.learned.contains(technique.name())) {
            player.sendSystemMessage(Component.literal("You already know " + technique.displayName + ".")
                    .withStyle(ChatFormatting.YELLOW));
            return false;
        }
        data.learned.add(technique.name());
        data.usesByTechnique.putIfAbsent(technique.name(), 0);
        if (data.equipped == null) {
            data.equipped = technique.name();
            data.selectedAbility = technique.abilities.get(0).id();
        }
        save();
        player.sendSystemMessage(Component.literal("Learned " + technique.displayName + " at Novice.")
                .withStyle(technique.color, ChatFormatting.BOLD));
        return true;
    }

    public static boolean equip(ServerPlayer player, String rawName) {
        Technique technique = Technique.byDisplay(rawName);
        if (technique == null) {
            player.sendSystemMessage(Component.literal("Unknown technique.").withStyle(ChatFormatting.RED));
            return false;
        }
        String uuid = player.getUUID().toString();
        PlayerTechniqueData data = get(uuid);
        if (!data.learned.contains(technique.name())) {
            player.sendSystemMessage(Component.literal("You have not learned " + technique.displayName + ".")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        data.equipped = technique.name();
        data.selectedAbility = technique.abilities.get(0).id();
        save();
        player.sendSystemMessage(Component.literal("Equipped " + technique.displayName + ".")
                .withStyle(technique.color));
        return true;
    }

    public static void showRanks(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        player.sendSystemMessage(Component.literal("Techniques").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        Set<Technique> known = learned(uuid);
        if (known.isEmpty()) {
            player.sendSystemMessage(Component.literal("None learned. Right-click a Technique Manual.")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        Technique equipped = getTechnique(uuid);
        for (Technique technique : Technique.all()) {
            if (!known.contains(technique)) continue;
            int uses = usesOf(uuid, technique);
            Technique.MasteryRank rank = Technique.MasteryRank.forUses(uses);
            String mark = technique == equipped ? " [equipped]" : "";
            player.sendSystemMessage(Component.literal("  " + technique.displayName + " — " + rank.display
                    + " (" + uses + " uses)" + mark).withStyle(technique.color));
        }
    }

    public static boolean grant(ServerPlayer player, Technique technique) {
        learn(player, technique);
        return equip(player, technique.displayName);
    }

    public static void renounce(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        PlayerTechniqueData data = get(uuid);
        data.equipped = null;
        data.selectedAbility = null;
        save();
        player.sendSystemMessage(Component.literal("Unequipped your technique.").withStyle(ChatFormatting.GRAY));
    }

    public static void onPlayerDeath(ServerPlayer player) {
        // Learned techniques persist through death.
    }

    public static void castSelected(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        Technique technique = getTechnique(uuid);
        if (technique == null) {
            player.sendSystemMessage(Component.literal("Equip a technique first (/technique).")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        Technique.Ability ability = getSelectedAbility(uuid);
        if (ability == null) return;
        tryCast(player, technique, ability);
    }

    public static boolean tryCastSneak(ServerPlayer player) {
        if (!player.isShiftKeyDown()) return false;
        if (!hasTechnique(player.getUUID().toString())) return false;
        castSelected(player);
        return true;
    }

    private static void regen(String uuid) {
        PlayerTechniqueData data = get(uuid);
        long now = System.currentTimeMillis();
        if (data.lastEnergyMs <= 0) {
            data.lastEnergyMs = now;
            return;
        }
        long elapsed = now - data.lastEnergyMs;
        if (elapsed < 1000L) return;
        int ticks = (int) (elapsed / 1000L);
        data.lastEnergyMs = now;
        data.energy = Math.min(getMaxEnergy(uuid), data.energy + ticks * 4f);
    }

    private static void tryCast(ServerPlayer player, Technique technique, Technique.Ability ability) {
        String uuid = player.getUUID().toString();
        if (!isAbilityUnlocked(uuid, ability)) {
            player.sendSystemMessage(Component.literal("Rank up to use " + ability.name() + ".")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        PlayerTechniqueData data = get(uuid);
        regen(uuid);
        long now = System.currentTimeMillis();
        Long ready = data.cooldowns.get(ability.id());
        if (ready != null && now < ready) {
            long left = (ready - now + 999) / 1000;
            player.sendSystemMessage(Component.literal("Cooldown: " + left + "s").withStyle(ChatFormatting.RED));
            return;
        }
        if (data.energy < ability.energyCost()) {
            player.sendSystemMessage(Component.literal("Not enough energy.").withStyle(ChatFormatting.RED));
            return;
        }
        data.energy -= ability.energyCost();
        data.cooldowns.put(ability.id(), now + ability.cooldownTicks() * 50L);
        data.usesByTechnique.merge(technique.name(), 1, Integer::sum);
        save();

        ServerLevel world = player.level();
        dispatch(player, world, technique, ability);
        player.sendSystemMessage(Component.literal(ability.name()).withStyle(technique.color, ChatFormatting.BOLD)
                .append(Component.literal(" — " + ability.description()).withStyle(ChatFormatting.GRAY)));
    }

    private static void dispatch(ServerPlayer player, ServerLevel world, Technique technique, Technique.Ability ability) {
        switch (technique) {
            case EMBERHEART -> emberheart(player, world, ability.id());
            case GLACIER_SOUL -> glacier(player, world, ability.id());
            case STORM_VEIN -> storm(player, world, ability.id());
            case UMBRAL_STEP -> umbral(player, world, ability.id());
            case WILDGROWTH -> wildgrowth(player, world, ability.id());
            case IRONMARROW -> ironmarrow(player, world, ability.id());
            case TIDEBOUND -> tidebound(player, world, ability.id());
            case WHISPERWIND -> whisperwind(player, world, ability.id());
        }
    }

    private static void emberheart(ServerPlayer player, ServerLevel world, String id) {
        switch (id) {
            case "ember_lash" -> {
                LivingEntity target = rayTarget(player, 20);
                if (target == null) return;
                bolt(world, player, target, ParticleTypes.FLAME);
                hurt(player, world, target, 8f);
                target.igniteForSeconds(4);
            }
            case "cinder_bloom" -> {
                nova(player, world, 5.5, 7f, true);
                world.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 40, 2, 0.4, 2, 0.02);
            }
            case "pyre_dash" -> {
                dash(player, 8);
                player.igniteForSeconds(1);
                nova(player, world, 2.5, 4f, true);
            }
            case "hearthguard" -> {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, true, false, false));
            }
        }
        world.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8f, 1.1f);
    }

    private static void glacier(ServerPlayer player, ServerLevel world, String id) {
        switch (id) {
            case "frost_lance" -> {
                LivingEntity target = rayTarget(player, 20);
                if (target == null) return;
                bolt(world, player, target, ParticleTypes.SNOWFLAKE);
                hurt(player, world, target, 7.5f);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
                target.setTicksFrozen(target.getTicksRequiredToFreeze() + 80);
            }
            case "rime_field" -> {
                for (LivingEntity le : nearby(player, 6)) {
                    hurt(player, world, le, 5f);
                    le.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
                }
                world.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.4, player.getZ(), 50, 3, 0.2, 3, 0.01);
            }
            case "glacial_aegis" -> {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 120, 1, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 0, true, false, false));
            }
            case "permafrost" -> {
                for (LivingEntity le : nearby(player, 7)) {
                    le.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 4));
                    le.setTicksFrozen(le.getTicksRequiredToFreeze() + 160);
                    hurt(player, world, le, 4f);
                }
            }
        }
        world.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 0.7f, 0.8f);
    }

    private static void storm(ServerPlayer player, ServerLevel world, String id) {
        switch (id) {
            case "volt_strike" -> {
                LivingEntity target = rayTarget(player, 22);
                if (target == null) return;
                bolt(world, player, target, ParticleTypes.ELECTRIC_SPARK);
                hurt(player, world, target, 9f);
            }
            case "arc_chain" -> {
                LivingEntity first = rayTarget(player, 16);
                List<LivingEntity> chain = new ArrayList<>();
                if (first != null) chain.add(first);
                chain.addAll(nearby(player, 8));
                int n = 0;
                for (LivingEntity le : chain) {
                    if (n++ >= 4) break;
                    hurt(player, world, le, 6.5f);
                    world.sendParticles(ParticleTypes.ELECTRIC_SPARK, le.getX(), le.getEyeY(), le.getZ(), 16, 0.3, 0.4, 0.3, 0.05);
                }
            }
            case "thunder_step" -> dash(player, 10);
            case "overload" -> {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 160, 1, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 160, 1, true, false, false));
            }
        }
        world.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.35f, 1.6f);
    }

    private static void umbral(ServerPlayer player, ServerLevel world, String id) {
        switch (id) {
            case "shade_cut" -> {
                LivingEntity target = rayTarget(player, 16);
                if (target == null) return;
                hurt(player, world, target, 8.5f);
            }
            case "void_slip" -> dash(player, 12);
            case "night_shroud" -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 140, 0, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 140, 0, true, false, false));
            }
            case "umbral_rend" -> {
                LivingEntity target = rayTarget(player, 12);
                if (target == null) return;
                hurt(player, world, target, 10f);
                player.heal(4f);
            }
        }
        world.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 1.3f);
    }

    private static void wildgrowth(ServerPlayer player, ServerLevel world, String id) {
        switch (id) {
            case "thorn_lash" -> {
                LivingEntity target = rayTarget(player, 16);
                if (target == null) return;
                hurt(player, world, target, 7f);
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
            }
            case "bloom_ward" -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1, true, false, false));
                for (LivingEntity le : nearbyAllies(player, 6)) {
                    le.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, true, false, false));
                }
            }
            case "entangle" -> {
                for (LivingEntity le : nearby(player, 6)) {
                    le.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 3));
                    hurt(player, world, le, 4f);
                }
            }
            case "verdant_form" -> {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 200, 0, true, false, false));
                player.heal(4f);
            }
        }
        world.playSound(null, player.blockPosition(), SoundEvents.AZALEA_PLACE, SoundSource.PLAYERS, 0.8f, 1.0f);
    }

    private static void ironmarrow(ServerPlayer player, ServerLevel world, String id) {
        switch (id) {
            case "iron_smash" -> {
                LivingEntity target = rayTarget(player, 8);
                if (target == null) return;
                hurt(player, world, target, 10f);
                knock(player, target, 1.2, 0.35);
            }
            case "bulwark" -> {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 160, 1, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 0, true, false, false));
            }
            case "quake_stomp" -> {
                for (LivingEntity le : nearby(player, 5.5)) {
                    hurt(player, world, le, 7f);
                    knock(player, le, 1.4, 0.55);
                }
            }
            case "unbreakable" -> BARRIER_UNTIL.put(player.getUUID().toString(), System.currentTimeMillis() + 2500L);
        }
        world.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.4f, 0.8f);
    }

    private static void tidebound(ServerPlayer player, ServerLevel world, String id) {
        switch (id) {
            case "tide_lance" -> {
                LivingEntity target = rayTarget(player, 18);
                if (target == null) return;
                bolt(world, player, target, ParticleTypes.BUBBLE);
                hurt(player, world, target, 7.5f);
            }
            case "undertow" -> {
                for (LivingEntity le : nearby(player, 8)) {
                    Vec3 pull = player.position().subtract(le.position()).normalize().scale(0.9);
                    le.setDeltaMovement(le.getDeltaMovement().add(pull.x, 0.15, pull.z));
                    le.hurtMarked = true;
                    hurt(player, world, le, 4f);
                }
            }
            case "riptide" -> {
                for (LivingEntity le : nearby(player, 7)) {
                    hurt(player, world, le, 6f);
                    knock(player, le, 1.6, 0.4);
                }
            }
            case "deep_current" -> {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 300, 0, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 300, 0, true, false, false));
            }
        }
        world.playSound(null, player.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.7f, 1.0f);
    }

    private static void whisperwind(ServerPlayer player, ServerLevel world, String id) {
        switch (id) {
            case "gale_cut" -> {
                LivingEntity target = rayTarget(player, 16);
                if (target == null) return;
                bolt(world, player, target, ParticleTypes.CLOUD);
                hurt(player, world, target, 6.5f);
            }
            case "cyclone" -> {
                for (LivingEntity le : nearby(player, 6.5)) {
                    hurt(player, world, le, 5f);
                    knock(player, le, 0.4, 0.95);
                }
            }
            case "zephyr_dash" -> dash(player, 11);
            case "tempest_form" -> {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 180, 1, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 180, 1, true, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 180, 0, true, false, false));
            }
        }
        world.playSound(null, player.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.4f, 1.6f);
    }

    private static void hurt(ServerPlayer player, ServerLevel world, LivingEntity target, float amount) {
        float scaled = amount * (1f + getRank(player.getUUID().toString()).ordinal() * 0.08f);
        target.hurtServer(world, player.damageSources().magic(), scaled);
    }

    private static void knock(ServerPlayer player, LivingEntity target, double xz, double y) {
        Vec3 dir = target.position().subtract(player.position());
        if (dir.lengthSqr() < 0.0001) dir = player.getLookAngle();
        Vec3 n = dir.normalize();
        target.setDeltaMovement(target.getDeltaMovement().add(n.x * xz, y, n.z * xz));
        target.hurtMarked = true;
    }

    private static void dash(ServerPlayer player, double blocks) {
        Vec3 look = player.getLookAngle().normalize().scale(blocks * 0.28);
        player.setDeltaMovement(look.x, Math.max(0.15, look.y), look.z);
        player.hurtMarked = true;
        player.fallDistance = 0;
    }

    private static void nova(ServerPlayer player, ServerLevel world, double radius, float damage, boolean ignite) {
        for (LivingEntity le : nearby(player, radius)) {
            hurt(player, world, le, damage);
            if (ignite) le.igniteForSeconds(3);
        }
    }

    private static void bolt(ServerLevel world, ServerPlayer player, LivingEntity target, net.minecraft.core.particles.ParticleOptions particle) {
        Vec3 start = player.getEyePosition();
        Vec3 end = target.getEyePosition();
        Vec3 delta = end.subtract(start);
        int steps = 12;
        for (int i = 0; i <= steps; i++) {
            Vec3 p = start.add(delta.scale(i / (double) steps));
            world.sendParticles(particle, p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.0);
        }
    }

    private static List<LivingEntity> nearby(ServerPlayer player, double radius) {
        ServerLevel world = player.level();
        AABB box = player.getBoundingBox().inflate(radius);
        List<LivingEntity> out = new ArrayList<>();
        for (Entity e : world.getEntities(player, box)) {
            if (e instanceof LivingEntity le && e != player && !(e instanceof ServerPlayer)) {
                out.add(le);
            }
        }
        return out;
    }

    private static List<LivingEntity> nearbyAllies(ServerPlayer player, double radius) {
        ServerLevel world = player.level();
        AABB box = player.getBoundingBox().inflate(radius);
        List<LivingEntity> out = new ArrayList<>();
        for (Entity e : world.getEntities(player, box)) {
            if (e instanceof ServerPlayer other && other != player) out.add(other);
        }
        return out;
    }

    private static LivingEntity rayTarget(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(range));
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        LivingEntity best = null;
        double bestDist = range;
        for (Entity e : player.level().getEntities(player, box)) {
            if (!(e instanceof LivingEntity le) || e == player) continue;
            AABB hit = le.getBoundingBox().inflate(0.3);
            var clip = hit.clip(start, end);
            if (clip.isEmpty()) continue;
            double dist = start.distanceTo(clip.get());
            if (dist < bestDist) {
                bestDist = dist;
                best = le;
            }
        }
        return best;
    }

    // Compatibility shims for leftover callers.
    public static boolean attemptAwaken(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("Techniques are learned from a Technique Manual, not awakened.")
                .withStyle(ChatFormatting.YELLOW));
        return false;
    }

    public static boolean handleStaffSneakUse(ServerPlayer player, ItemStack stack) {
        return false;
    }
}
