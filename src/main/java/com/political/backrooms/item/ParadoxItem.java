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

/** 11 dmg, 1.6 speed. Heals attacker for 35% of damage dealt. */
public class ParadoxItem extends Item {

    public ParadoxItem(Settings settings) {
        super(settings);
    }

    public static AttributeModifiersComponent createAttributes() {
        return AttributeModifiersComponent.builder()
            .add(EntityAttributes.ATTACK_DAMAGE,
                new EntityAttributeModifier(Identifier.of("politicalserver", "paradox_dmg"),
                    10.0, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .add(EntityAttributes.ATTACK_SPEED,
                new EntityAttributeModifier(Identifier.of("politicalserver", "paradox_spd"),
                    -2.4, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .build();
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        attacker.heal(3.85f); // ~35% of 11 damage
        stack.damage(1, attacker, Hand.MAIN_HAND);
    }
}
