package com.political.mixin;

import com.political.DanielsPickaxe;
import com.political.MasteryHeartTree;
import com.political.MasteryManager;
import com.political.PlayerBuffManager;
import com.political.ProgressionTreeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Applies MINER_MASTER buff mining speed bonus and Daniel's Pickaxe effects when breaking blocks.
 */
@Mixin(Player.class)
public class MiningSpeedMixin {

    @ModifyVariable(
            method = "getDestroySpeed",
            at = @At("RETURN"),
            ordinal = 0
    )
    private float modifyBlockBreakingSpeed(float speed, BlockState block) {
        Player player = (Player) (Object) this;
        
        // Only apply to server players
        if (player instanceof ServerPlayer serverPlayer) {
            // Apply Daniel's Pickaxe mining speed multiplier
            float danielsMultiplier = DanielsPickaxe.getMiningSpeedMultiplier(serverPlayer, block);
            if (danielsMultiplier != 1.0f) {
                speed *= danielsMultiplier;
            }
            
            // Apply MINER_MASTER buff mining speed bonus
            double miningBonus = PlayerBuffManager.getMiningSpeedBonus(serverPlayer.getUUID().toString());
            if (miningBonus > 0) {
                speed *= (1.0f + (float) miningBonus);
            }

            speed *= (float) MasteryHeartTree.miningSpeedMultiplier(serverPlayer.getUUID().toString());
            double masteryGather = MasteryManager.getMiningSpeedBonus(serverPlayer.getUUID().toString());
            if (masteryGather > 0) {
                speed *= (1.0f + (float) masteryGather);
            }

            double progressionMining = ProgressionTreeManager.getMiningSpeedBonus(serverPlayer.getUUID().toString());
            if (progressionMining > 0) {
                speed *= (1.0f + (float) progressionMining);
            }
        }
        
        return speed;
    }
}
