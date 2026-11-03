package com.political.mixin;

import com.political.SeasonManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The seasonal crop-growth hook.
 *
 * <p>Runs alongside the existing {@code CropBlockMixin} (which handles the GREEN_THUMB perk)
 * — both inject at HEAD and neither depends on the other's result.</p>
 *
 * <ul>
 *   <li><b>Multiplier &gt; 1</b> (spring, summer): an extra growth step is granted with
 *       probability {@code multiplier - 1}, exactly mirroring how the perk works.</li>
 *   <li><b>Multiplier &lt; 1</b> (winter): the random tick is cancelled with probability
 *       {@code 1 - multiplier}, so crops still grow — just slowly. Growth is never stopped
 *       outright, because a hard stop would strand players who only farm.</li>
 * </ul>
 */
@Mixin(CropBlock.class)
public class SeasonCropGrowthMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void livingworld_seasonalGrowth(BlockState state, ServerLevel world, BlockPos pos,
                                            RandomSource random, CallbackInfo ci) {
        double multiplier;
        try {
            multiplier = SeasonManager.cropGrowthMultiplier();
        } catch (Throwable t) {
            org.slf4j.LoggerFactory.getLogger("politicalserver.mixin").debug("SeasonCropGrowthMixin.livingworld_seasonalGrowth: ignored an error", t);
            return;
        }
        if (multiplier == 1.0) return;

        CropBlock crop = (CropBlock) (Object) this;

        if (multiplier > 1.0) {
            if (random.nextFloat() < (float) (multiplier - 1.0)) {
                int age = crop.getAge(state);
                if (age < crop.getMaxAge()) {
                    world.setBlock(pos, state.setValue(CropBlock.AGE,
                            Math.min(age + 1, crop.getMaxAge())), Block.UPDATE_CLIENTS);
                }
            }
            return;
        }

        // Lean season: skip a proportion of growth ticks entirely.
        if (random.nextFloat() < (float) (1.0 - multiplier)) {
            ci.cancel();
        }
    }
}
