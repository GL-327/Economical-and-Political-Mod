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

/** 20 dmg, 0.6 speed. On kill: deal 10 damage to ALL nearby entities within 6 blocks. */
public class MementoMoriItem extends Item {

    public MementoMoriItem(Settings settings) {
        super(settings);
    }

    public static AttributeModifiersComponent createAttributes() {
        return AttributeModifiersComponent.builder()
            .add(EntityAttributes.ATTACK_DAMAGE,
                new EntityAttributeModifier(Identifier.of("politicalserver", "memento_mori_dmg"),
                    19.0, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .add(EntityAttributes.ATTACK_SPEED,
                new EntityAttributeModifier(Identifier.of("politicalserver", "memento_mori_spd"),
                    -3.4, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .build();
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.getHealth() <= 0 && attacker.getEntityWorld() instanceof ServerWorld serverWorld) {
            Box aoeBox = new Box(target.getX() - 6, target.getY() - 4, target.getZ() - 6,
                target.getX() + 6, target.getY() + 4, target.getZ() + 6);
            List<LivingEntity> nearby = serverWorld.getEntitiesByClass(LivingEntity.class, aoeBox,
                e -> e != attacker);
            for (LivingEntity e : nearby) {
                e.damage(serverWorld, serverWorld.getDamageSources().magic(), 10.0f);
            }
        }
        stack.damage(1, attacker, Hand.MAIN_HAND);
    }
}
