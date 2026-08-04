package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The wither, given variants and phases.
 *
 * <p>Vanilla's wither is one creature that does one thing at two speeds. This gives it four
 * bloodlines, picked when it is summoned and announced to everybody nearby, and four phases inside
 * each fight. Two summons are never quite the same fight, which is the difference between a boss
 * you beat once and a boss you go back to.</p>
 *
 * <h2>On the textures</h2>
 *
 * <p>A server side mod cannot ship a model. What it can do is put a textured player head on the
 * creature, which every vanilla client already knows how to render, so each bloodline genuinely
 * looks different with nothing installed. That is the same trick the block armour sets use, and it
 * is the only honest way to get a custom texture across to a client that has no mod.</p>
 */
public final class WitherFightManager {

    private WitherFightManager() {}

    private static final String TAGGED = "political_wither_variant";
    private static final float BONUS_HEALTH = 260f;

    private static final Map<UUID, Fight> FIGHTS = new HashMap<>();

    // ── Bloodlines ────────────────────────────────────────────────────────

    /**
     * The four bloodlines.
     *
     * <p>Each one changes what the fight punishes. Ashen burns the ground so standing still costs
     * you, Frostbound slows so kiting stops working, Voidtouched moves you around, and Bloodsworn
     * heals off what it hits so a long fight goes the wrong way.</p>
     */
    public enum Variant {
        ASHEN("Ashen Wither", ChatFormatting.GOLD, BossEvent.BossBarColor.RED,
                "It burns what it stands over.",
                // Blackstone, for a creature made of cinders.
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjIxYTM5MzBhZWU3M2M4NGE5ZDdkMWNhZjQ3YzQ0YWQ0ZjQ2Y2M0ZjBiNzUwMTA5NzQ4ODZlOTZjNzFhNzFmMSJ9fX0="),
        FROSTBOUND("Frostbound Wither", ChatFormatting.AQUA, BossEvent.BossBarColor.BLUE,
                "Everything near it slows down, including you.",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzk3YWMwZTVhZTUwNTk4YzJmOTU1NDQ2Y2Y5NGM3YjY5MTMyMTU1NDMwZDkxOTVjYTUyMzZhZGNlNDU0YTFmZCJ9fX0="),
        VOIDTOUCHED("Voidtouched Wither", ChatFormatting.DARK_PURPLE, BossEvent.BossBarColor.PURPLE,
                "It does not let you choose where you stand.",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2JlN2ZlYzk1OWM1ZjcwZWJhZjNiNGE2Y2Q0YzNjZjZlY2ZkOTFjMjE0ZGJiYTMxNmM0MmJmZjA5ZDU3ZGVmZiJ9fX0="),
        BLOODSWORN("Bloodsworn Wither", ChatFormatting.DARK_RED, BossEvent.BossBarColor.RED,
                "It mends itself off everything it touches.",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjdmZTRjYWQ4ZDNjMWY4ZTNiNTFmM2VkNGE1YWNmMzRlNGE0MjA3Nzc3ZDNhNzk1YzFkOTRhNGY0ODU1NzgwOSJ9fX0=");

        public final String display;
        public final ChatFormatting colour;
        public final BossEvent.BossBarColor bar;
        public final String blurb;
        public final String texture;

        Variant(String display, ChatFormatting colour, BossEvent.BossBarColor bar,
                String blurb, String texture) {
            this.display = display;
            this.colour = colour;
            this.bar = bar;
            this.blurb = blurb;
            this.texture = texture;
        }
    }

    /** Where a fight currently is. */
    public enum Phase {
        RISING("Rising", "It is still working out what you are."),
        BARRAGE("Barrage", "Skulls, and more of them than it has heads."),
        BULWARK("Bulwark", "It has put something between you and it. Break the guards."),
        LAST_BREATH("Last Breath", "It has nothing left to save.");

        public final String display;
        public final String blurb;

        Phase(String display, String blurb) {
            this.display = display;
            this.blurb = blurb;
        }
    }

    private static final class Fight {
        Variant variant;
        Phase phase = Phase.RISING;
        ServerBossEvent bar;
        long nextAttack;
        long telegraphAt;
        String telegraphed;
        final List<UUID> guards = new ArrayList<>();
        final Map<UUID, Float> damageBy = new HashMap<>();
    }

    // ── Tick ──────────────────────────────────────────────────────────────

    /**
     * Drives every live wither.
     *
     * <p>Withers can be summoned anywhere, so unlike the dragon there is no single arena to scan.
     * The loop walks the levels that actually have players in them, which keeps the cost tied to
     * where people are rather than to how many dimensions the mod ships.</p>
     */
    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 5 != 0) return;

        // Deduped across players. Scanning per player is what keeps the cost tied to where people
        // are, but two players standing near the same wither would otherwise drive it twice in one
        // tick: double attack timers, and a Bloodsworn healing once per onlooker.
        java.util.Set<UUID> driven = new java.util.HashSet<>();
        prune(server);
        for (ServerLevel level : server.getAllLevels()) {
            if (level.players().isEmpty()) continue;
            for (ServerPlayer p : level.players()) {
                List<WitherBoss> near = level.getEntitiesOfClass(WitherBoss.class,
                        p.getBoundingBox().inflate(80), w -> w.isAlive());
                for (WitherBoss w : near) {
                    if (!driven.add(w.getUUID())) continue;
                    Fight f = FIGHTS.computeIfAbsent(w.getUUID(), k -> begin(w, level));
                    run(w, level, f);
                }
            }
        }
    }


    /**
     * Drops fights whose wither is gone.
     *
     * <p>A wither that dies is cleaned by the death hook, but one that unloads with the chunk or is
     * removed by a command never reaches it, and its boss bar would sit in the map for the rest of
     * the server's uptime. Checked once every ten seconds rather than every pass, because it only
     * has to catch up eventually.</p>
     */
    private static void prune(MinecraftServer server) {
        if (FIGHTS.isEmpty()) return;
        if (server.getTickCount() % 200 != 0) return;
        FIGHTS.entrySet().removeIf(e -> {
            for (ServerLevel level : server.getAllLevels()) {
                var found = level.getEntity(e.getKey());
                if (found != null && found.isAlive()) return false;
            }
            e.getValue().bar.removeAllPlayers();
            return true;
        });
    }

    private static Fight begin(WitherBoss wither, ServerLevel level) {
        Fight f = new Fight();
        // Picked from the entity uuid, so the same wither is always the same bloodline no matter
        // how many times the fight map is rebuilt underneath it.
        Variant[] all = Variant.values();
        f.variant = all[Math.floorMod(wither.getUUID().hashCode(), all.length)];

        f.bar = new ServerBossEvent(UUID.randomUUID(),
                Component.literal(f.variant.display)
                        .withStyle(f.variant.colour, ChatFormatting.BOLD),
                f.variant.bar, BossEvent.BossBarOverlay.PROGRESS);
        f.bar.setProgress(1.0f);

        if (!wither.entityTags().contains(TAGGED)) {
            wither.addTag(TAGGED);
            dress(wither, f.variant);
            try {
                var attr = wither.getAttribute(
                        net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
                if (attr != null) {
                    attr.setBaseValue(attr.getBaseValue() + BONUS_HEALTH);
                    wither.setHealth(wither.getMaxHealth());
                }
            } catch (Exception ignored) {
                // A wither that will not take the extra health just dies sooner.
            }
        }

        for (ServerPlayer p : level.players()) {
            p.sendSystemMessage(Component.literal(""));
            p.sendSystemMessage(Component.literal("  " + f.variant.colour + "§l"
                    + f.variant.display.toUpperCase(java.util.Locale.ROOT)));
            p.sendSystemMessage(Component.literal("  §7" + f.variant.blurb));
            p.sendSystemMessage(Component.literal(""));
        }
        return f;
    }

    /**
     * Puts the bloodline's face on it.
     *
     * <p>A head in the helmet slot renders on a vanilla client with nothing installed, which is the
     * only way a server side mod gets a custom look across at all.</p>
     */
    private static void dress(WitherBoss wither, Variant v) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        UUID id = UUID.nameUUIDFromBytes(v.texture.getBytes(StandardCharsets.UTF_8));

        CompoundTag nbt = new CompoundTag();
        CompoundTag owner = new CompoundTag();
        CompoundTag props = new CompoundTag();
        CompoundTag textures = new CompoundTag();
        textures.putString("Value", v.texture);
        props.put("textures", textures);
        owner.put("properties", props);
        owner.putString("Id", id.toString());
        nbt.put("SkullOwner", owner);
        head.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

        wither.setItemSlot(EquipmentSlot.HEAD, head);
        wither.setCustomName(Component.literal(v.display).withStyle(v.colour, ChatFormatting.BOLD));
        wither.setCustomNameVisible(true);
        // It should not shed its own face on death.
        try {
            wither.setDropChance(EquipmentSlot.HEAD, 0f);
        } catch (Exception ignored) {
            // Older signature. Losing the head to the floor is cosmetic, not a fault.
        }
    }

    private static void run(WitherBoss wither, ServerLevel level, Fight f) {
        float frac = wither.getHealth() / Math.max(1f, wither.getMaxHealth());
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(wither) < 80 * 80) f.bar.addPlayer(p);
            else f.bar.removePlayer(p);
        }
        f.bar.setProgress(Math.max(0f, Math.min(1f, frac)));

        Phase want = frac > 0.75f ? Phase.RISING
                : frac > 0.45f ? Phase.BARRAGE
                : frac > 0.20f ? Phase.BULWARK
                : Phase.LAST_BREATH;
        if (want != f.phase) enterPhase(wither, level, f, want);

        long now = level.getServer().getTickCount();
        if (f.telegraphed != null && now >= f.telegraphAt) {
            resolve(wither, level, f);
            f.telegraphed = null;
        }
        if (now >= f.nextAttack && f.telegraphed == null) {
            telegraph(wither, level, f, now);
        }

        if (f.phase == Phase.BULWARK) {
            f.guards.removeIf(id -> {
                var e = level.getEntity(id);
                return e == null || !e.isAlive();
            });
        }

        // Bloodsworn mends itself while anything nearby is hurt, which is what makes it a race.
        if (f.variant == Variant.BLOODSWORN && wither.getHealth() < wither.getMaxHealth()) {
            wither.heal(0.25f);
        }
    }

    private static void enterPhase(WitherBoss wither, ServerLevel level, Fight f, Phase phase) {
        f.phase = phase;
        f.bar.setName(Component.literal(f.variant.display + " — " + phase.display)
                .withStyle(f.variant.colour, ChatFormatting.BOLD));
        f.nextAttack = level.getServer().getTickCount() + 50;

        for (ServerPlayer p : nearby(level, wither)) {
            p.sendSystemMessage(Component.literal(""));
            p.sendSystemMessage(Component.literal("  " + f.variant.colour + "§l"
                    + phase.display.toUpperCase(java.util.Locale.ROOT)));
            p.sendSystemMessage(Component.literal("  §7" + phase.blurb));
            p.sendSystemMessage(Component.literal(""));
            level.playSound(null, p.blockPosition(), SoundEvents.WITHER_SPAWN,
                    SoundSource.HOSTILE, 0.8f, phase == Phase.LAST_BREATH ? 0.6f : 1.0f);
        }

        if (phase == Phase.BULWARK) summonGuards(level, wither, f);
        if (phase == Phase.LAST_BREATH) {
            wither.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 600, 1));
            wither.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 20 * 600, 1));
        }
    }

    // ── Attacks ───────────────────────────────────────────────────────────

    private static void telegraph(WitherBoss wither, ServerLevel level, Fight f, long now) {
        String what = switch (f.phase) {
            case RISING -> "sweep";
            case BARRAGE -> "barrage";
            case BULWARK -> f.guards.isEmpty() ? "barrage" : "sweep";
            case LAST_BREATH -> (now / 200) % 2 == 0 ? "barrage" : "nova";
        };
        String warn = switch (what) {
            case "barrage" -> "§6⚠ It is drawing breath for a volley.";
            case "nova" -> "§4⚠ It is pulling everything inward.";
            default -> "§e⚠ It is winding up a sweep.";
        };
        for (ServerPlayer p : nearby(level, wither)) {
            p.sendSystemMessage(Component.literal(warn), true);
        }
        f.telegraphed = what;
        f.telegraphAt = now + (f.phase == Phase.LAST_BREATH ? 25 : 40);
        f.nextAttack = f.telegraphAt + (f.phase == Phase.LAST_BREATH ? 70 : 130);
    }

    private static void resolve(WitherBoss wither, ServerLevel level, Fight f) {
        List<ServerPlayer> targets = nearby(level, wither);
        float base = f.phase == Phase.LAST_BREATH ? 12f : 7f;

        for (ServerPlayer p : targets) {
            switch (f.telegraphed) {
                case "barrage" -> {
                    p.hurtServer(level, p.damageSources().mobAttack(wither), base);
                    p.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                }
                case "nova" -> {
                    p.hurtServer(level, p.damageSources().magic(), base * 1.6f);
                    p.setDeltaMovement(wither.position().subtract(p.position())
                            .normalize().scale(1.2));
                }
                default -> {
                    // A sweep only catches anybody close enough to be swept.
                    if (p.distanceToSqr(wither) < 36) {
                        p.hurtServer(level, p.damageSources().mobAttack(wither), base);
                        p.setDeltaMovement(p.getDeltaMovement().add(0, 0.7, 0));
                    }
                }
            }
            applyVariant(p, level, wither, f);
        }
    }

    /** The part of the fight that changes with the bloodline. */
    private static void applyVariant(ServerPlayer p, ServerLevel level, WitherBoss w, Fight f) {
        switch (f.variant) {
            case ASHEN -> p.igniteForSeconds(5);
            case FROSTBOUND -> {
                p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 2));
                p.setTicksFrozen(Math.min(p.getTicksFrozen() + 100, 300));
            }
            case VOIDTOUCHED -> p.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 0));
            case BLOODSWORN -> w.heal(4f);
        }
    }

    private static void summonGuards(ServerLevel level, WitherBoss wither, Fight f) {
        f.guards.clear();
        for (int i = 0; i < 4; i++) {
            double angle = (Math.PI / 2) * i;
            Mob guard = EntityType.WITHER_SKELETON.create(level, EntitySpawnReason.EVENT);
            if (guard == null) continue;
            guard.setPos(wither.getX() + Math.cos(angle) * 4, wither.getY(),
                    wither.getZ() + Math.sin(angle) * 4);
            guard.setCustomName(Component.literal("Wither Guard")
                    .withStyle(f.variant.colour));
            guard.setCustomNameVisible(true);
            guard.setPersistenceRequired();
            guard.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 600, 1));
            level.addFreshEntity(guard);
            f.guards.add(guard.getUUID());
        }
    }

    // ── Hooks ─────────────────────────────────────────────────────────────

    /** How much of a hit the wither takes. Bulwark holds while its guards live. */
    public static float incomingScale(WitherBoss wither) {
        Fight f = FIGHTS.get(wither.getUUID());
        if (f == null) return 1f;
        if (f.phase == Phase.BULWARK && !f.guards.isEmpty()) return 0.2f;
        return 1f;
    }

    /** How hard it hits back. */
    public static float outgoingScale(WitherBoss wither) {
        Fight f = FIGHTS.get(wither.getUUID());
        if (f == null) return 1f;
        return f.phase == Phase.LAST_BREATH ? 1.9f : 1.4f;
    }

    public static void onHurt(WitherBoss wither, ServerPlayer by, float amount) {
        Fight f = FIGHTS.get(wither.getUUID());
        if (f != null) f.damageBy.merge(by.getUUID(), amount, Float::sum);
    }

    /** Pays out by contribution and clears up after itself. */
    public static void onDeath(WitherBoss wither, ServerLevel level) {
        Fight f = FIGHTS.remove(wither.getUUID());
        if (f == null) return;

        for (UUID id : f.guards) {
            var e = level.getEntity(id);
            if (e != null) e.discard();
        }
        f.bar.removeAllPlayers();

        float total = 0;
        for (float v : f.damageBy.values()) total += v;

        List<ServerPlayer> present = nearby(level, wither);
        for (ServerPlayer p : present) {
            float mine = f.damageBy.getOrDefault(p.getUUID(), 0f);
            double share = total <= 0 ? 1.0 / Math.max(1, present.size()) : mine / total;
            long coins = Math.round(120_000 * share) + 12_000;
            CoinManager.giveCoins(p, coins);

            p.sendSystemMessage(Component.literal(""));
            p.sendSystemMessage(Component.literal("  " + f.variant.colour + "§lTHE "
                    + f.variant.display.toUpperCase(java.util.Locale.ROOT) + " FALLS"));
            p.sendSystemMessage(Component.literal("  §7You did §f"
                    + Math.round(share * 100) + "% §7of the damage"));
            p.sendSystemMessage(Component.literal("  §7Paid §6" + GuiKit.num(coins) + " coins"));

            if (p.getRandom().nextDouble() < 0.2 + share * 0.4) {
                ItemStack drop = RareDropManager.rollBossReward(p);
                if (drop != null && !drop.isEmpty()) {
                    if (!p.getInventory().add(drop)) p.drop(drop, false);
                    p.sendSystemMessage(Component.literal("  §6✦ "
                            + drop.getHoverName().getString()));
                }
            }
            p.sendSystemMessage(Component.literal(""));
        }
    }

    private static List<ServerPlayer> nearby(ServerLevel level, LivingEntity of) {
        List<ServerPlayer> out = new ArrayList<>();
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(of) < 80 * 80) out.add(p);
        }
        return out;
    }

    /** Which bloodline this wither is, for the bestiary and the admin screen. */
    public static Variant variantOf(WitherBoss wither) {
        Fight f = FIGHTS.get(wither.getUUID());
        return f == null ? null : f.variant;
    }
}
