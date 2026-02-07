package com.political.mixin;

import com.political.PerkManager;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {

    private static final Identifier UPRISING_HEALTH_ID = Identifier.of("political", "monster_uprising");
    private static final Identifier GIANTS_HEALTH_ID = Identifier.of("political", "giants_playground_health");
    private static final Identifier GIANTS_SPEED_ID = Identifier.of("political", "giants_playground_speed");

    @Inject(method = "initialize", at = @At("RETURN"))
    private void political_onInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        MobEntity self = (MobEntity) (Object) this;

        // GIANTS_PLAYGROUND - +50% health, -20% speed for all mobs [1]
        if (PerkManager.hasActivePerk("GIANTS_PLAYGROUND")) {
            EntityAttributeInstance maxHealth = self.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            if (maxHealth != null && !maxHealth.hasModifier(GIANTS_HEALTH_ID)) {
                maxHealth.addPersistentModifier(new EntityAttributeModifier(
                        GIANTS_HEALTH_ID,
                        0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                self.setHealth(self.getMaxHealth());
            }

            EntityAttributeInstance speed = self.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
            if (speed != null && !speed.hasModifier(GIANTS_SPEED_ID)) {
                speed.addPersistentModifier(new EntityAttributeModifier(
                        GIANTS_SPEED_ID,
                        -0.2, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        // Only affect hostile mobs for remaining perks
        if (!(self instanceof HostileEntity)) return;

        // MONSTER_UPRISING - +25% health for hostile mobs [5]
        if (PerkManager.hasActivePerk("MONSTER_UPRISING")) {
            EntityAttributeInstance health = self.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            if (health != null && !health.hasModifier(UPRISING_HEALTH_ID)) {
                EntityAttributeModifier modifier = new EntityAttributeModifier(
                        UPRISING_HEALTH_ID,
                        0.25,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                );
                health.addPersistentModifier(modifier);
                self.setHealth(self.getMaxHealth());
            }
        }

        // Enderman spawn multiplier - spawn additional endermen
        if (self instanceof EndermanEntity && world instanceof ServerWorld serverWorld) {
            float endermanMultiplier = PerkManager.getEndermanSpawnMultiplier();
            if (endermanMultiplier > 1.0f) {
                int extraSpawns = (int) (endermanMultiplier - 1.0f);
                Random random = new Random();
                // Chance for additional spawn based on decimal portion
                if (random.nextFloat() < (endermanMultiplier - 1.0f - extraSpawns)) {
                    extraSpawns++;
                }

                BlockPos pos = self.getBlockPos();
                for (int i = 0; i < extraSpawns; i++) {
                    EndermanEntity extra = EntityType.ENDERMAN.create(serverWorld, SpawnReason.NATURAL);
                    if (extra != null) {
                        extra.refreshPositionAndAngles(
                                pos.getX() + random.nextInt(5) - 2,
                                pos.getY(),
                                pos.getZ() + random.nextInt(5) - 2,
                                random.nextFloat() * 360f, 0f
                        );
                        serverWorld.spawnEntity(extra);
                    }
                }
            }
        }
    }
}