package com.political.backrooms.item;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

/**
 * Generic weapon item for Backrooms melee weapons.
 * Configurable damage, attack speed, and optional on-hit effect.
 */
public class BackroomsWeaponItem extends Item {

    private final Consumer<LivingEntity> onHitEffect;

    public BackroomsWeaponItem(Settings settings) {
        this(settings, null);
    }

    public BackroomsWeaponItem(Settings settings, Consumer<LivingEntity> onHitEffect) {
        super(settings);
        this.onHitEffect = onHitEffect;
    }

    public static AttributeModifiersComponent createAttributes(String id, double damage, double speedOffset) {
        return AttributeModifiersComponent.builder()
            .add(EntityAttributes.ATTACK_DAMAGE,
                new EntityAttributeModifier(Identifier.of("politicalserver", id + "_dmg"),
                    damage, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .add(EntityAttributes.ATTACK_SPEED,
                new EntityAttributeModifier(Identifier.of("politicalserver", id + "_spd"),
                    speedOffset, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .build();
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (onHitEffect != null) {
            onHitEffect.accept(target);
        }
        stack.damage(1, attacker, Hand.MAIN_HAND);
    }
}
