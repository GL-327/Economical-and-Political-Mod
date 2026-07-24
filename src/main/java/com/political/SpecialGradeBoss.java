package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * V6.4–V6.5 — the model for a named Special Grade boss.
 *
 * <p>Everything is a dressed vanilla mob (no registered entities): each boss is a base
 * {@link EntityType} whose attributes, name, tags and behaviour are rewritten at spawn and driven
 * by {@link SpecialGradeBossManager}. The six bosses are deliberately mechanically distinct:
 *
 * <ul>
 *   <li>{@code RADIAS} — a <b>regenerator</b> that mends itself unless burst down.</li>
 *   <li>{@code KAGE} — a <b>teleporting blitzer</b> that blinks onto its target and strikes.</li>
 *   <li>{@code BROOD_SOVEREIGN} — a <b>summoner</b> that floods the arena with adds.</li>
 *   <li>{@code MALEVOLENCE} — an <b>area-denial</b> curse that burns the ground you stand on.</li>
 *   <li>{@code MIRRORED_FIST} — a <b>counter-attacker</b> that punishes big hits with a shockwave.</li>
 *   <li>{@code SUKORO} — a <b>two-phase transformer</b> that is reborn stronger at half health.</li>
 * </ul>
 *
 * <p>Grade is a tiny self-contained concept (see {@link Grade}); this pillar imports no external
 * cursed-spirit classes.
 */
public enum SpecialGradeBoss {

    RADIAS("radias", "Radias, the Evermending", "❣",
            EntityType.RAVAGER, ChatFormatting.GREEN, BossEvent.BossBarColor.GREEN,
            4000.0, 12.0, Mechanic.REGENERATOR, DomainExpansion.Theme.CEASELESS_MARSH,
            15_000L, 45_000L,
            new String[]{
                    "A curse of endless recovery. Its flesh knits shut",
                    "faster than most can carve it open — the only answer",
                    "is overwhelming, sustained damage."
            }),

    KAGE("kage", "Kage, the Hollow Blink", "✦",
            EntityType.ENDERMAN, ChatFormatting.DARK_PURPLE, BossEvent.BossBarColor.PURPLE,
            2600.0, 20.0, Mechanic.BLITZER, DomainExpansion.Theme.FRACTURED_VOID,
            18_000L, 55_000L,
            new String[]{
                    "It does not walk. It arrives. Kage folds the space",
                    "between itself and its prey and lands a single,",
                    "devastating strike before folding away again."
            }),

    BROOD_SOVEREIGN("brood_sovereign", "The Brood Sovereign", "❂",
            EntityType.EVOKER, ChatFormatting.LIGHT_PURPLE, BossEvent.BossBarColor.PINK,
            3000.0, 10.0, Mechanic.SUMMONER, DomainExpansion.Theme.ENDLESS_BROOD,
            16_000L, 60_000L,
            new String[]{
                    "The Sovereign does not fight — it births. Cut down its",
                    "spawn and it births more. Kill the mother quickly, or",
                    "drown in her children."
            }),

    MALEVOLENCE("malevolence", "Malevolent Shrine", "☒",
            EntityType.BLAZE, ChatFormatting.GOLD, BossEvent.BossBarColor.YELLOW,
            2800.0, 14.0, Mechanic.AREA_DENIAL, DomainExpansion.Theme.WITHERING_GARDEN,
            20_000L, 70_000L,
            new String[]{
                    "A shrine of pure malevolence. It sentences the ground",
                    "beneath your feet to burn. Stand still and you are ash;",
                    "keep moving and you may yet live."
            }),

    MIRRORED_FIST("mirrored_fist", "The Mirrored Fist", "⊘",
            EntityType.PIGLIN_BRUTE, ChatFormatting.RED, BossEvent.BossBarColor.RED,
            3400.0, 16.0, Mechanic.COUNTER, DomainExpansion.Theme.REVERSAL_FIELD,
            19_000L, 65_000L,
            new String[]{
                    "Strike it too greedily and the blow returns to you.",
                    "The Fist answers heavy damage with a reversing",
                    "shockwave. Patience beats it; recklessness feeds it."
            }),

    SUKORO("sukoro", "Sukoro, the Twice-Born", "☠",
            EntityType.WITHER_SKELETON, ChatFormatting.DARK_RED, BossEvent.BossBarColor.WHITE,
            3600.0, 18.0, Mechanic.TRANSFORMER, DomainExpansion.Theme.IRON_COFFIN,
            25_000L, 90_000L,
            new String[]{
                    "Bring it to half and it does not die — it is reborn.",
                    "The second Sukoro hits harder, moves faster, and no",
                    "longer pretends to be merciful."
            });

    // ════════════════════════════════════════════════════════════

    /** A minimal, self-contained grade scale. Special Grade is the apex. */
    public enum Grade {
        GRADE_1("Grade 1", ChatFormatting.YELLOW),
        SPECIAL_GRADE("Special Grade", ChatFormatting.DARK_RED);

        public final String label;
        public final ChatFormatting color;

        Grade(String label, ChatFormatting color) {
            this.label = label;
            this.color = color;
        }
    }

    public enum Mechanic {
        REGENERATOR("Regenerator", "Mends itself over time — burst it down."),
        BLITZER("Blitzer", "Teleports to its target and strikes."),
        SUMMONER("Summoner", "Floods the arena with adds."),
        AREA_DENIAL("Area Denial", "Burns the ground you stand on."),
        COUNTER("Counter", "Punishes heavy hits with a shockwave."),
        TRANSFORMER("Transformer", "Reborn stronger at half health.");

        public final String label;
        public final String hint;

        Mechanic(String label, String hint) {
            this.label = label;
            this.hint = hint;
        }
    }

    public final String id;
    public final String title;
    public final String icon;
    public final EntityType<? extends net.minecraft.world.entity.LivingEntity> baseEntity;
    public final ChatFormatting color;
    public final BossEvent.BossBarColor barColor;
    public final double baseHealth;
    public final double attackDamage;
    public final Mechanic mechanic;
    public final DomainExpansion.Theme theme;
    public final long summonCoinCost;
    /** Coins the killer receives on a fair kill. Assists receive a fraction (see manager). */
    public final long coinReward;
    public final String[] lore;

    SpecialGradeBoss(String id, String title, String icon,
                     EntityType<? extends net.minecraft.world.entity.LivingEntity> baseEntity,
                     ChatFormatting color, BossEvent.BossBarColor barColor,
                     double baseHealth, double attackDamage, Mechanic mechanic,
                     DomainExpansion.Theme theme, long summonCoinCost, long coinReward,
                     String[] lore) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.baseEntity = baseEntity;
        this.color = color;
        this.barColor = barColor;
        this.baseHealth = baseHealth;
        this.attackDamage = attackDamage;
        this.mechanic = mechanic;
        this.theme = theme;
        this.summonCoinCost = summonCoinCost;
        this.coinReward = coinReward;
        this.lore = lore;
    }

    public Grade grade() {
        return Grade.SPECIAL_GRADE;
    }

    /** Per-summoner cooldown, in minutes (harder bosses lock you out longer). */
    public int cooldownMinutes() {
        return switch (this) {
            case RADIAS -> 45;
            case KAGE -> 55;
            case BROOD_SOVEREIGN -> 60;
            case MALEVOLENCE -> 70;
            case MIRRORED_FIST -> 65;
            case SUKORO -> 90;
        };
    }

    public long cooldownMsForSummoner() {
        return cooldownMinutes() * 60_000L;
    }

    public String displayName() {
        return icon + " " + title;
    }

    public String trophyId() {
        return "sgb_trophy_" + id;
    }

    public String gearId() {
        return "sgb_gear_" + id;
    }

    public static SpecialGradeBoss byId(String id) {
        if (id == null) return null;
        for (SpecialGradeBoss b : values()) {
            if (b.id.equalsIgnoreCase(id)) return b;
        }
        return null;
    }

    /**
     * Spawn the dressed base entity at {@code pos} with this boss's stat block, name, and identity
     * tags. Returns the spawned {@link Mob}, or {@code null} if the entity could not be created.
     */
    public Mob spawnEntity(ServerLevel level, BlockPos pos) {
        Entity e = baseEntity.spawn(level, pos, EntitySpawnReason.EVENT);
        if (!(e instanceof Mob mob)) {
            if (e != null) e.discard();
            return null;
        }

        var hp = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.setBaseValue(baseHealth);
            mob.setHealth((float) baseHealth);
        }
        var dmg = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) dmg.setBaseValue(attackDamage);
        var kbr = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbr != null) kbr.setBaseValue(1.0);

        mob.setCustomName(Component.literal(color + "" + ChatFormatting.BOLD + displayName()));
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();
        mob.addTag(SpecialGradeBossManager.BOSS_TAG);
        mob.addTag(SpecialGradeBossManager.bossTag(this));
        return mob;
    }
}
