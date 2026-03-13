package com.political.backrooms.item;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

/** 13 dmg, 1.2 speed. On hit: teleport attacker to just behind target. */
public class DimensionalRiftBladeItem extends Item {

    public DimensionalRiftBladeItem(Settings settings) {
        super(settings);
    }

    public static AttributeModifiersComponent createAttributes() {
        return AttributeModifiersComponent.builder()
            .add(EntityAttributes.ATTACK_DAMAGE,
                new EntityAttributeModifier(Identifier.of("politicalserver", "dim_rift_blade_dmg"),
                    12.0, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .add(EntityAttributes.ATTACK_SPEED,
                new EntityAttributeModifier(Identifier.of("politicalserver", "dim_rift_blade_spd"),
                    -2.8, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .build();
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Teleport attacker behind target
        Vec3d look = target.getRotationVec(1.0f);
        double bx = target.getX() - look.x * 1.5;
        double by = target.getY();
        double bz = target.getZ() - look.z * 1.5;
        attacker.teleport(bx, by, bz, true);
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 2));
        stack.damage(1, attacker, Hand.MAIN_HAND);
    }
}
