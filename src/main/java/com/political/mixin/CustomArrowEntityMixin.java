package com.political.mixin;

import com.political.CustomArrows;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to transfer custom arrow data to arrow entities when shot.
 */
@Mixin(ArrowEntity.class)
public abstract class CustomArrowEntityMixin extends PersistentProjectileEntity {

    protected CustomArrowEntityMixin(net.minecraft.entity.EntityType<? extends PersistentProjectileEntity> type, World world) {
        super(type, world);
    }

    /**
     * When an arrow is created from a player's inventory, check if it's a custom arrow
     * and tag the entity accordingly.
     */
    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;)V", at = @At("TAIL"))
    private void onArrowCreated(World world, LivingEntity owner, ItemStack stack, World world2, CallbackInfo ci) {
        // Check if the arrow stack is custom
        if (CustomArrows.isCustomArrow(stack)) {
            CustomArrows.ArrowType type = CustomArrows.getArrowType(stack);
            if (type != null) {
                // Add tag to arrow entity
                NbtCompound nbt = new NbtCompound();
                nbt.putByte("custom_arrow", (byte) 1);
                nbt.putString("arrow_type", type.name());
                
                // Store in entity data
                this.addCommandTag("custom_arrow");
                this.addCommandTag("arrow_type_" + type.name().toLowerCase());
            }
        }
    }
}
