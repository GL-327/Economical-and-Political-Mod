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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import java.util.List;

/** 14 dmg, 1.0 speed. On hit: chance to spread Wither I to nearby enemies. */
public class EntropyItem extends Item {

    public EntropyItem(Settings settings) {
        super(settings);
    }

    public static AttributeModifiersComponent createAttributes() {
        return AttributeModifiersComponent.builder()
            .add(EntityAttributes.ATTACK_DAMAGE,
                new EntityAttributeModifier(Identifier.of("politicalserver", "entropy_dmg"),
                    13.0, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .add(EntityAttributes.ATTACK_SPEED,
                new EntityAttributeModifier(Identifier.of("politicalserver", "entropy_spd"),
                    -3.0, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .build();
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 80, 0));
        // 40% chance to spread Wither to nearby entities
        if (attacker.getRandom().nextFloat() < 0.40f && attacker.getEntityWorld() instanceof ServerWorld sw) {
            Box box = new Box(target.getX() - 4, target.getY() - 3, target.getZ() - 4,
                target.getX() + 4, target.getY() + 3, target.getZ() + 4);
            List<LivingEntity> nearby = sw.getEntitiesByClass(LivingEntity.class, box,
                e -> e != attacker && e != target);
            for (LivingEntity e : nearby) {
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0));
            }
        }
        stack.damage(1, attacker, Hand.MAIN_HAND);
    }
}
