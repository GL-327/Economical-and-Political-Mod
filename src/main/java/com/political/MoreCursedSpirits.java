package com.political;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * V7 Pillar B — more cursed spirit variety, summonable on demand.
 *
 * <p>{@link CursedSpiritManager} keeps its roster in a fixed list and exposes no registration hook,
 * so these nine new archetypes ride a standalone path instead: a craftable <b>Cursed Talisman</b>
 * (one per grade) that, on use, manifests a graded spirit for a fight. The path is entirely
 * self-contained: these spirits carry their own tags (never the manager's {@code political_cursed}
 * tag), this class drives their distinct auras on tick, and this class rewards the kill.</p>
 *
 * <h2>Anti-farm</h2>
 * <ul>
 *   <li>A talisman is consumed on use, so every summon has an up-front crafting cost.</li>
 *   <li>An 8s per-player cooldown stops burst summoning.</li>
 *   <li>At most {@link #PER_PLAYER_CAP} talisman spirits may be alive near one player.</li>
 *   <li>The reward (a little Cursed Energy plus reagents) is worth less than the talisman it took to
 *       summon the fight, so the loop is a challenge and a sink, never a net-positive farm.</li>
 * </ul>
 */
public final class MoreCursedSpirits {

    private MoreCursedSpirits() {}

    // ── Identity ────────────────────────────────────────────────
    public static final String SPIRIT_TAG = "political_arsenal_spirit";
    private static final String ARCH_TAG_PREFIX = "political_arsenal_arch_";
    private static final String GRADE_TAG_PREFIX = "political_arsenal_grade_";

    private static final long SUMMON_COOLDOWN_MS = 8000L;
    private static final int PER_PLAYER_CAP = 3;
    private static final int SUMMON_SCAN_RADIUS = 40;
    private static final int AURA_RADIUS = 12;
    private static final int AURA_INTERVAL_TICKS = 20;

    private static final Map<UUID, Long> nextSummonAt = new ConcurrentHashMap<>();
    private static int auraCounter = 0;
    private static boolean registered = false;

    // ── Aura flavours ───────────────────────────────────────────
    public enum Aura {
        ROT,     // hunger + weakness
        VENOM,   // poison
        EMBER,   // wither + fire feel
        CHILL,   // slowness + mining fatigue
        PLAGUE,  // poison + hunger
        RUSH,    // self strength + knockback
        QUAKE,   // mining fatigue + knockback
        HEX,     // weakness + darkness
        RUIN     // special grade cocktail + roar
    }

    // ── Archetype ───────────────────────────────────────────────
    public record Arch(String id, String name, EntityType<?> base, CursedGrade grade,
                       Aura aura, String flavour) {

        public String archTag() { return ARCH_TAG_PREFIX + id; }

        public static Arch byId(String id) {
            if (id == null) return null;
            for (Arch a : ROSTER) if (a.id.equals(id)) return a;
            return null;
        }

        public static List<Arch> ofGrade(CursedGrade g) {
            List<Arch> out = new ArrayList<>();
            for (Arch a : ROSTER) if (a.grade == g) out.add(a);
            return out;
        }
    }

    public static final List<Arch> ROSTER = List.of(
            new Arch("hunger_shade", "Hunger-Shade", EntityType.ZOMBIE, CursedGrade.GRADE_4, Aura.ROT,
                    "It gnaws at the appetite of everyone near it."),
            new Arch("spite_crawler", "Spite-Crawler", EntityType.SILVERFISH, CursedGrade.GRADE_4, Aura.VENOM,
                    "A small grudge that festers into venom."),
            new Arch("ash_walker", "Ash-Walker", EntityType.BLAZE, CursedGrade.GRADE_3, Aura.EMBER,
                    "It walks in a haze of cursed embers."),
            new Arch("frost_wretch", "Frost-Wretch", EntityType.STRAY, CursedGrade.GRADE_3, Aura.CHILL,
                    "The cold of a death no one mourned."),
            new Arch("plague_matron", "Plague-Matron", EntityType.WITCH, CursedGrade.GRADE_2, Aura.PLAGUE,
                    "She brews resentment into a spreading sickness."),
            new Arch("iron_revenant", "Iron-Revenant", EntityType.VINDICATOR, CursedGrade.GRADE_2, Aura.RUSH,
                    "A soldier's fury that would not lie down."),
            new Arch("calamity_bull", "Calamity-Bull", EntityType.RAVAGER, CursedGrade.GRADE_1, Aura.QUAKE,
                    "Its every step buckles the ground."),
            new Arch("hollow_conductor", "Hollow-Conductor", EntityType.EVOKER, CursedGrade.GRADE_1, Aura.HEX,
                    "It conducts a chorus of hexes with idle hands."),
            new Arch("throne_of_ruin", "Throne of Ruin", EntityType.RAVAGER, CursedGrade.SPECIAL_GRADE, Aura.RUIN,
                    "A Special Grade that wears catastrophe like a crown.")
    );

    // ════════════════════════════════════════════════════════════
    // Talisman items
    // ════════════════════════════════════════════════════════════
    public static final String TALISMAN_PREFIX = "cursed_talisman_";

    public static ItemStack createTalisman(CursedGrade g) {
        ItemStack s = new ItemStack(talismanBase(g));
        s.set(DataComponents.CUSTOM_NAME, Component.literal("☠ Cursed Talisman (" + g.label + ")")
                .withStyle(g.color, ChatFormatting.BOLD));
        List<Component> l = new ArrayList<>();
        l.add(Component.literal("§8A seal that calls a curse to you."));
        l.add(Component.literal("§7Use it to summon a " + g.label + " spirit."));
        l.add(Component.literal("§7Beat it for Cursed Energy and reagents."));
        l.add(Component.literal("§8Consumed on use. 8s cooldown."));
        l.add(Component.literal("§c「 Cursed Arsenal 」"));
        s.set(DataComponents.LORE, new ItemLore(l));
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(s, talismanId(g)); // LAST
        return s;
    }

    public static String talismanId(CursedGrade g) {
        return TALISMAN_PREFIX + CursedArsenal.gradeKey(g);
    }

    private static net.minecraft.world.item.Item talismanBase(CursedGrade g) {
        return switch (g) {
            case GRADE_4 -> Items.BONE;
            case GRADE_3 -> Items.SKELETON_SKULL;
            case GRADE_2 -> Items.WITHER_SKELETON_SKULL;
            case GRADE_1 -> Items.SOUL_LANTERN;
            case SPECIAL_GRADE -> Items.NETHER_STAR;
        };
    }

    private static CursedGrade talismanGrade(String id) {
        if (id == null || !id.startsWith(TALISMAN_PREFIX)) return null;
        return CursedArsenal.gradeFromKey(id.substring(TALISMAN_PREFIX.length()));
    }

    // ════════════════════════════════════════════════════════════
    // Registration
    // ════════════════════════════════════════════════════════════
    public static void register() {
        if (registered) return;
        registered = true;

        UseItemCallback.EVENT.register((Player player, Level world, InteractionHand hand) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            ItemStack held = player.getItemInHand(hand);
            CursedGrade g = talismanGrade(SlayerItems.getCustomItemId(held));
            if (g == null) return InteractionResult.PASS;
            if (useTalisman(sp, g, hand, held)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            try {
                if (entity instanceof ServerPlayer) return;
                if (!(entity.level() instanceof ServerLevel level)) return;
                if (!isTalismanSpirit(entity)) return;
                onSpiritDeath(level, entity, source.getEntity());
            } catch (Throwable t) {
                PoliticalServer.LOGGER.warn("[Arsenal] talisman spirit death failed", t);
            }
        });
    }

    // ── Summon ──────────────────────────────────────────────────
    private static boolean useTalisman(ServerPlayer player, CursedGrade g, InteractionHand hand, ItemStack held) {
        if (!(player.level() instanceof ServerLevel level)) return false;

        long now = System.currentTimeMillis();
        long ready = nextSummonAt.getOrDefault(player.getUUID(), 0L);
        if (now < ready) {
            player.sendSystemMessage(Component.literal("§8The seal is still cooling. Wait a moment.")
                    .withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        if (nearbyTalismanSpirits(level, player.blockPosition(), SUMMON_SCAN_RADIUS).size() >= PER_PLAYER_CAP) {
            player.sendSystemMessage(Component.literal("§cToo many cursed spirits are already loose near you.")
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }

        List<Arch> pool = Arch.ofGrade(g);
        if (pool.isEmpty()) return false;
        Arch arch = pool.get(level.getRandom().nextInt(pool.size()));

        Mob spirit = spawnNear(level, player, arch, g);
        if (spirit == null) {
            player.sendSystemMessage(Component.literal("§8There is no room to summon it here.")
                    .withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        held.shrink(1);
        nextSummonAt.put(player.getUUID(), now + SUMMON_COOLDOWN_MS);

        player.sendSystemMessage(Component.literal("☠ You unseal ")
                .withStyle(ChatFormatting.DARK_PURPLE)
                .append(Component.literal(arch.name()).withStyle(g.color, ChatFormatting.BOLD))
                .append(Component.literal(" §8— " + arch.flavour())));
        try {
            level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN,
                    SoundSource.HOSTILE, 0.9f, g == CursedGrade.SPECIAL_GRADE ? 0.6f : 1.1f);
        } catch (Throwable ignored) {}
        return true;
    }

    private static Mob spawnNear(ServerLevel level, ServerPlayer anchor, Arch arch, CursedGrade grade) {
        double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
        int radius = 6 + level.getRandom().nextInt(5);
        int x = (int) Math.round(anchor.getX() + Math.cos(angle) * radius);
        int z = (int) Math.round(anchor.getZ() + Math.sin(angle) * radius);
        int y;
        try {
            y = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, (int) anchor.getY(), z)).getY();
        } catch (Throwable t) {
            y = (int) anchor.getY();
        }
        if (y < level.getMinY() + 2) y = (int) anchor.getY();
        BlockPos pos = new BlockPos(x, y, z);

        Entity spawned;
        try {
            spawned = arch.base().spawn(level, pos, EntitySpawnReason.EVENT);
        } catch (Throwable t) {
            PoliticalServer.LOGGER.warn("[Arsenal] failed to spawn {}: {}", arch.id(), t.toString());
            return null;
        }
        if (!(spawned instanceof Mob mob)) {
            if (spawned != null) spawned.discard();
            return null;
        }
        dress(level, mob, arch, grade);
        return mob;
    }

    private static void dress(ServerLevel level, Mob mob, Arch arch, CursedGrade grade) {
        // Talisman spirits sit a touch above natural spawns so a summoned fight feels worth it.
        double health = grade.baseHealth() * 1.15;
        double damage = grade.baseDamage();

        var hp = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.setBaseValue(health);
            mob.setHealth((float) health);
        }
        var atk = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) atk.setBaseValue(damage);
        var kb = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kb != null) kb.setBaseValue(Math.min(1.0, kb.getBaseValue() + 0.15 + 0.06 * grade.ordinal()));

        mob.addTag(SPIRIT_TAG);
        mob.addTag(GRADE_TAG_PREFIX + CursedArsenal.gradeKey(grade));
        mob.addTag(arch.archTag());
        mob.setPersistenceRequired();

        mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 999999, 0, true, false, false));
        if (grade.ordinal() >= CursedGrade.GRADE_1.ordinal()) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 999999, 0, true, false, false));
        }

        equip(mob, grade);

        String symbol = grade == CursedGrade.SPECIAL_GRADE ? "☠ " : "✦ ";
        mob.setCustomName(Component.literal(symbol + arch.name() + " §8[" + grade.label + "]")
                .withStyle(grade.color, ChatFormatting.BOLD));
        mob.setCustomNameVisible(true);
    }

    private static void equip(Mob mob, CursedGrade grade) {
        ItemStack weapon = switch (grade) {
            case GRADE_4 -> null;
            case GRADE_3 -> new ItemStack(Items.STONE_AXE);
            case GRADE_2 -> new ItemStack(Items.IRON_AXE);
            case GRADE_1 -> new ItemStack(Items.DIAMOND_AXE);
            case SPECIAL_GRADE -> new ItemStack(Items.NETHERITE_AXE);
        };
        try {
            if (weapon != null) {
                mob.setItemSlot(EquipmentSlot.MAINHAND, weapon);
                mob.setDropChance(EquipmentSlot.MAINHAND, 0f);
            }
        } catch (Throwable ignored) {
            // some dressed types (silverfish, blaze) have no hand slot; harmless.
        }
    }

    // ── Identity reads ──────────────────────────────────────────
    public static boolean isTalismanSpirit(Entity e) {
        return e != null && e.entityTags().contains(SPIRIT_TAG);
    }

    private static CursedGrade gradeOf(Entity e) {
        for (String tag : e.entityTags()) {
            if (tag.startsWith(GRADE_TAG_PREFIX)) {
                CursedGrade g = CursedArsenal.gradeFromKey(tag.substring(GRADE_TAG_PREFIX.length()));
                if (g != null) return g;
            }
        }
        return null;
    }

    private static Arch archOf(Entity e) {
        for (String tag : e.entityTags()) {
            if (tag.startsWith(ARCH_TAG_PREFIX)) {
                Arch a = Arch.byId(tag.substring(ARCH_TAG_PREFIX.length()));
                if (a != null) return a;
            }
        }
        return null;
    }

    // ── Aura tick ───────────────────────────────────────────────
    public static void tick(MinecraftServer server) {
        if (server == null) return;
        if (++auraCounter < AURA_INTERVAL_TICKS) return;
        auraCounter = 0;
        try {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (p.isSpectator()) continue;
                if (!(p.level() instanceof ServerLevel level)) continue;
                for (Mob spirit : nearbyTalismanSpirits(level, p.blockPosition(), AURA_RADIUS)) {
                    CursedGrade g = gradeOf(spirit);
                    Arch a = archOf(spirit);
                    if (g == null || a == null) continue;
                    applyAura(level, spirit, p, g, a.aura());
                }
            }
        } catch (Throwable t) {
            PoliticalServer.LOGGER.warn("[Arsenal] aura tick failed", t);
        }
    }

    private static void applyAura(ServerLevel level, Mob spirit, ServerPlayer target,
                                  CursedGrade grade, Aura aura) {
        int amp = Math.min(2, grade.ordinal() / 2);
        int dur = 60;
        switch (aura) {
            case ROT -> {
                affect(target, MobEffects.HUNGER, dur, 0);
                if (level.getRandom().nextInt(4) == 0) affect(target, MobEffects.WEAKNESS, dur, 0);
            }
            case VENOM -> affect(target, MobEffects.POISON, dur, 0);
            case EMBER -> {
                affect(target, MobEffects.WITHER, dur, 0);
                try {
                    Vec3 c = spirit.getBoundingBox().getCenter();
                    level.sendParticles(ParticleTypes.FLAME, c.x, c.y + 0.3, c.z, 4, 0.3, 0.3, 0.3, 0.01);
                } catch (Throwable ignored) {}
            }
            case CHILL -> {
                affect(target, MobEffects.SLOWNESS, dur, Math.min(1, amp));
                affect(target, MobEffects.MINING_FATIGUE, dur, 0);
            }
            case PLAGUE -> {
                affect(target, MobEffects.POISON, dur, 0);
                affect(target, MobEffects.HUNGER, dur, 0);
            }
            case RUSH -> {
                spirit.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 0, true, false, false));
                if (level.getRandom().nextInt(5) == 0) knockback(spirit, target);
            }
            case QUAKE -> {
                affect(target, MobEffects.MINING_FATIGUE, dur, amp);
                if (level.getRandom().nextInt(5) == 0) knockback(spirit, target);
            }
            case HEX -> {
                affect(target, MobEffects.WEAKNESS, dur, 0);
                if (level.getRandom().nextInt(4) == 0) affect(target, MobEffects.DARKNESS, 60, 0);
            }
            case RUIN -> {
                affect(target, MobEffects.WITHER, dur, 1);
                affect(target, MobEffects.MINING_FATIGUE, dur, 1);
                affect(target, MobEffects.DARKNESS, dur, 0);
                spirit.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 1, true, false, false));
                if (level.getRandom().nextInt(8) == 0) {
                    knockback(spirit, target);
                    try {
                        level.playSound(null, spirit.blockPosition(), SoundEvents.WARDEN_ROAR,
                                SoundSource.HOSTILE, 1.2f, 0.7f);
                    } catch (Throwable ignored) {}
                }
            }
        }
        try {
            Vec3 c = spirit.getBoundingBox().getCenter();
            level.sendParticles(grade == CursedGrade.SPECIAL_GRADE ? ParticleTypes.SCULK_SOUL : ParticleTypes.SOUL,
                    c.x, c.y + 0.3, c.z, 3, 0.3, 0.4, 0.3, 0.01);
        } catch (Throwable ignored) {}
    }

    private static void affect(ServerPlayer p, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
                               int dur, int amp) {
        p.addEffect(new MobEffectInstance(effect, dur, Math.max(0, amp), true, true, true));
    }

    private static void knockback(Mob from, ServerPlayer target) {
        Vec3 dir = target.position().subtract(from.position()).normalize();
        target.setDeltaMovement(target.getDeltaMovement().add(dir.x * 0.6, 0.42, dir.z * 0.6));
        target.hurtMarked = true;
    }

    // ── Death & reward ──────────────────────────────────────────
    private static void onSpiritDeath(ServerLevel level, LivingEntity spirit, Entity killerEntity) {
        CursedGrade grade = gradeOf(spirit);
        if (grade == null) return;
        Arch arch = archOf(spirit);
        BlockPos pos = spirit.blockPosition();

        try {
            Vec3 c = spirit.getBoundingBox().getCenter();
            level.sendParticles(ParticleTypes.SCULK_SOUL, c.x, c.y + 0.4, c.z, 24, 0.4, 0.6, 0.4, 0.03);
            level.sendParticles(ParticleTypes.SMOKE, c.x, c.y + 0.4, c.z, 16, 0.3, 0.4, 0.3, 0.02);
        } catch (Throwable ignored) {}

        if (!(killerEntity instanceof ServerPlayer killer)) return;

        // Reward: kept below a natural spawn's payout so a summon is a challenge, not a farm.
        long energy = Math.max(1, Math.round(grade.cursedEnergyReward * 0.5));
        CursedEnergyManager.earn(killer, energy);
        CursedEnergyManager.recordKill(killer, grade);

        rollDrops(killer, level, grade);

        killer.sendSystemMessage(Component.literal("☠ Banished ")
                .withStyle(ChatFormatting.DARK_PURPLE)
                .append(Component.literal(arch != null ? arch.name() : "a cursed spirit")
                        .withStyle(grade.color, ChatFormatting.BOLD))
                .append(Component.literal(" §7(+§d" + energy + " Cursed Energy§7)")));

        if (grade == CursedGrade.SPECIAL_GRADE) {
            for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
                p.sendSystemMessage(Component.literal("§4☠ §cA summoned Special Grade curse fell to §f"
                        + killer.getName().getString() + "§c."));
            }
        }
        try {
            level.playSound(null, pos, SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 0.5f, 1.3f);
        } catch (Throwable ignored) {}
    }

    private static void rollDrops(ServerPlayer killer, ServerLevel level, CursedGrade grade) {
        var rng = level.getRandom();
        switch (grade) {
            case GRADE_4 -> {
                giveOrDrop(killer, CursedItems.createResidue());
            }
            case GRADE_3 -> {
                giveOrDrop(killer, CursedItems.createResidue());
                if (rng.nextInt(2) == 0) giveOrDrop(killer, CursedItems.createResidue());
            }
            case GRADE_2 -> {
                giveOrDrop(killer, CursedItems.createResidue());
                if (rng.nextInt(2) == 0) giveOrDrop(killer, CursedItems.createCore());
            }
            case GRADE_1 -> {
                giveOrDrop(killer, CursedItems.createCore());
                if (rng.nextInt(2) == 0) giveOrDrop(killer, CursedItems.createOrb());
            }
            case SPECIAL_GRADE -> {
                giveOrDrop(killer, CursedItems.createCore());
                giveOrDrop(killer, CursedItems.createCore());
                if (rng.nextInt(3) == 0) giveOrDrop(killer, CursedItems.createRelic());
            }
        }
    }

    private static void giveOrDrop(ServerPlayer p, ItemStack s) {
        if (s == null || s.isEmpty()) return;
        if (!p.getInventory().add(s)) p.drop(s, false);
    }

    private static List<Mob> nearbyTalismanSpirits(ServerLevel level, BlockPos center, int radius) {
        AABB box = new AABB(center).inflate(radius);
        return level.getEntitiesOfClass(Mob.class, box, MoreCursedSpirits::isTalismanSpirit);
    }
}
