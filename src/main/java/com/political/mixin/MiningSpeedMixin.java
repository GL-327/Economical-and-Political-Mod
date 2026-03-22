package com.political.mixin;

import com.political.PlayerBuffManager;
import com.political.DanielsPickaxe;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Applies MINER_MASTER buff mining speed bonus and Daniel's Pickaxe effects when breaking blocks.
 */
@Mixin(PlayerEntity.class)
public class MiningSpeedMixin {

    @ModifyVariable(
            method = "getBlockBreakingSpeed",
            at = @At("RETURN"),
            ordinal = 0
    )
    private float modifyBlockBreakingSpeed(float speed, BlockState block) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        
        // Only apply to server players
        if (player instanceof ServerPlayerEntity serverPlayer) {
            // Apply Daniel's Pickaxe mining speed multiplier
            float danielsMultiplier = DanielsPickaxe.getMiningSpeedMultiplier(serverPlayer, block);
            if (danielsMultiplier != 1.0f) {
                speed *= danielsMultiplier;
            }
            
            // Apply MINER_MASTER buff mining speed bonus
            double miningBonus = PlayerBuffManager.getMiningSpeedBonus(serverPlayer.getUuidAsString());
            if (miningBonus > 0) {
                speed *= (1.0f + (float) miningBonus);
            }
        }
        
        return speed;
    }
}
