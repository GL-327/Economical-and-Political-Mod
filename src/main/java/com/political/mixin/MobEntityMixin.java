package com.political.mixin;

import com.political.PerkManager;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {

    private static final Identifier UPRISING_HEALTH_ID = Identifier.of("political", "monster_uprising");

    @Inject(method = "initialize", at = @At("TAIL"))
    private void political_onInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        MobEntity self = (MobEntity) (Object) this;

        // Only affect hostile mobs
        if (!(self instanceof HostileEntity)) return;

        // MONSTER_UPRISING - +25% health for hostile mobs
        if (PerkManager.hasActivePerk("MONSTER_UPRISING")) {
            EntityAttributeInstance health = self.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            if (health != null && !health.hasModifier(UPRISING_HEALTH_ID)) {
                EntityAttributeModifier modifier = new EntityAttributeModifier(
                        UPRISING_HEALTH_ID,
                        0.25, // +25% health
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                );
                health.addPersistentModifier(modifier);
                self.setHealth(self.getMaxHealth()); // Heal to new max
            }
        }
    }
}