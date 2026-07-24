package com.political;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.UUID;

/**
 * V6.4–V6.5 — Domain Expansions.
 *
 * <p>A Domain Expansion is a temporary, themed arena a Special Grade boss stamps around itself
 * when a phase transition fires. This class is the <b>model</b>: the palette + signature effect
 * for each theme, and an immutable {@link Blueprint} describing one expansion to be stamped.
 *
 * <p>The heavy lifting (stamping blocks, tracking them, reverting on death / logout / shutdown /
 * restart) lives in {@link DomainManager}. Nothing here touches the world.
 *
 * <p><b>Self-contained grade note.</b> This pillar defines its own tiny grade concept
 * ({@link SpecialGradeBoss.Grade}); it does not depend on any other agent's cursed-spirit classes.
 */
public final class DomainExpansion {

    private DomainExpansion() {}

    /**
     * The visual + mechanical identity of a domain. Accent/pillar blocks are only ever stamped
     * into <b>air</b> (see {@link DomainManager}), so reverting them to air is always correct and
     * never destroys terrain.
     */
    public enum Theme {
        CEASELESS_MARSH(
                "Ceaseless Marsh", ChatFormatting.GREEN,
                Blocks.MOSS_BLOCK, Blocks.MANGROVE_ROOTS,
                MobEffects.POISON, 1, ParticleTypes.ITEM_SLIME,
                BossEvent.BossBarColor.GREEN,
                "The rot never stops mending itself."),

        FRACTURED_VOID(
                "Fractured Void", ChatFormatting.DARK_PURPLE,
                Blocks.OBSIDIAN, Blocks.END_ROD,
                MobEffects.DARKNESS, 0, ParticleTypes.PORTAL,
                BossEvent.BossBarColor.PURPLE,
                "Space folds; there is nowhere to stand that it cannot reach."),

        ENDLESS_BROOD(
                "Endless Brood", ChatFormatting.LIGHT_PURPLE,
                Blocks.COBWEB, Blocks.BONE_BLOCK,
                MobEffects.WEAKNESS, 0, ParticleTypes.SPORE_BLOSSOM_AIR,
                BossEvent.BossBarColor.PINK,
                "For every one you fell, two more are already born."),

        WITHERING_GARDEN(
                "Withering Garden", ChatFormatting.GOLD,
                Blocks.MAGMA_BLOCK, Blocks.WITHER_ROSE,
                MobEffects.WITHER, 0, ParticleTypes.FLAME,
                BossEvent.BossBarColor.YELLOW,
                "The ground itself has been sentenced to burn."),

        REVERSAL_FIELD(
                "Reversal Field", ChatFormatting.RED,
                Blocks.POLISHED_BLACKSTONE, Blocks.IRON_BARS,
                MobEffects.MINING_FATIGUE, 1, ParticleTypes.CRIT,
                BossEvent.BossBarColor.RED,
                "Every blow you land is a blow you invited."),

        IRON_COFFIN(
                "Coffin of the Iron Mountain", ChatFormatting.DARK_RED,
                Blocks.IRON_BARS, Blocks.IRON_BLOCK,
                MobEffects.SLOWNESS, 1, ParticleTypes.SOUL_FIRE_FLAME,
                BossEvent.BossBarColor.WHITE,
                "The lid closes. There is no light left to crawl toward.");

        public final String title;
        public final ChatFormatting color;
        public final Block accent;
        public final Block pillar;
        public final Holder<MobEffect> signature;
        public final int signatureAmp;
        public final ParticleOptions particle;
        public final BossEvent.BossBarColor barColor;
        public final String flavor;

        Theme(String title, ChatFormatting color, Block accent, Block pillar,
              Holder<MobEffect> signature, int signatureAmp, ParticleOptions particle,
              BossEvent.BossBarColor barColor, String flavor) {
            this.title = title;
            this.color = color;
            this.accent = accent;
            this.pillar = pillar;
            this.signature = signature;
            this.signatureAmp = signatureAmp;
            this.particle = particle;
            this.barColor = barColor;
            this.flavor = flavor;
        }
    }

    /**
     * An immutable request to expand a domain. Handed to {@link DomainManager#expand}.
     */
    public static final class Blueprint {
        public final UUID ownerEntity;
        public final String dim;
        public final BlockPos center;
        public final int radius;
        public final Theme theme;
        public final long durationMs;

        public Blueprint(UUID ownerEntity, String dim, BlockPos center, int radius,
                         Theme theme, long durationMs) {
            this.ownerEntity = ownerEntity;
            this.dim = dim;
            this.center = center;
            this.radius = radius;
            this.theme = theme;
            this.durationMs = durationMs;
        }
    }
}
