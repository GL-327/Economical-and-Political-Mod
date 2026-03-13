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
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

/** 15 dmg, 1.2 speed. On hit: strips Absorption/Resistance from target. */
public class TheLastWordItem extends Item {

    public TheLastWordItem(Settings settings) {
        super(settings);
    }

    public static AttributeModifiersComponent createAttributes() {
        return AttributeModifiersComponent.builder()
            .add(EntityAttributes.ATTACK_DAMAGE,
                new EntityAttributeModifier(Identifier.of("politicalserver", "the_last_word_dmg"),
                    14.0, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .add(EntityAttributes.ATTACK_SPEED,
                new EntityAttributeModifier(Identifier.of("politicalserver", "the_last_word_spd"),
                    -2.8, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .build();
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.removeStatusEffect(StatusEffects.ABSORPTION);
        target.removeStatusEffect(StatusEffects.RESISTANCE);
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 1));
        stack.damage(1, attacker, Hand.MAIN_HAND);
    }
}
