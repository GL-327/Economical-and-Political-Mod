package com.political.mixin;

import com.political.PerkManager;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(Mob.class)
public abstract class MobEntityMixin {

    private static final Identifier UPRISING_HEALTH_ID = Identifier.fromNamespaceAndPath("political", "monster_uprising");
    private static final Identifier GIANTS_HEALTH_ID = Identifier.fromNamespaceAndPath("political", "giants_playground_health");
    private static final Identifier GIANTS_SPEED_ID = Identifier.fromNamespaceAndPath("political", "giants_playground_speed");

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void political_onFinalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, SpawnGroupData entityData, CallbackInfoReturnable<SpawnGroupData> cir) {
        Mob self = (Mob) (Object) this;

        // GIANTS_PLAYGROUND - +50% health, -20% speed for all mobs [1]
        if (PerkManager.hasActivePerk("GIANTS_PLAYGROUND")) {
            AttributeInstance maxHealth = self.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null && !maxHealth.hasModifier(GIANTS_HEALTH_ID)) {
                maxHealth.addPermanentModifier(new AttributeModifier(
                        GIANTS_HEALTH_ID,
                        0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                self.setHealth(self.getMaxHealth());
            }

            AttributeInstance speed = self.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null && !speed.hasModifier(GIANTS_SPEED_ID)) {
                speed.addPermanentModifier(new AttributeModifier(
                        GIANTS_SPEED_ID,
                        -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        // Only affect hostile mobs for remaining perks
        if (!(self instanceof Monster)) return;

        // MONSTER_UPRISING - +25% health for hostile mobs [5]
        if (PerkManager.hasActivePerk("MONSTER_UPRISING")) {
            AttributeInstance health = self.getAttribute(Attributes.MAX_HEALTH);
            if (health != null && !health.hasModifier(UPRISING_HEALTH_ID)) {
                AttributeModifier modifier = new AttributeModifier(
                        UPRISING_HEALTH_ID,
                        0.25,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                );
                health.addPermanentModifier(modifier);
                self.setHealth(self.getMaxHealth());
            }
        }

        // Enderman spawn multiplier - spawn additional endermen
        Level spawnLevel = self.level();
        if (self instanceof EnderMan && spawnLevel instanceof ServerLevel serverWorld) {
            float endermanMultiplier = PerkManager.getEndermanSpawnMultiplier();
            if (endermanMultiplier > 1.0f) {
                int extraSpawns = (int) (endermanMultiplier - 1.0f);
                Random random = new Random();
                // Chance for additional spawn based on decimal portion
                if (random.nextFloat() < (endermanMultiplier - 1.0f - extraSpawns)) {
                    extraSpawns++;
                }

                BlockPos pos = self.blockPosition();
                for (int i = 0; i < extraSpawns; i++) {
                    EnderMan extra = EntityType.ENDERMAN.create(serverWorld, EntitySpawnReason.NATURAL);
                    if (extra != null) {
                        extra.teleportTo(
                                pos.getX() + random.nextInt(5) - 2,
                                pos.getY(),
                                pos.getZ() + random.nextInt(5) - 2
                        );
                        extra.setYRot(random.nextFloat() * 360f);
                        serverWorld.addFreshEntity(extra);
                    }
                }
            }
        }
    }
}