package com.political.backrooms.item;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import java.util.List;

/** 14 dmg, 0.8 speed, area damage on hit */
public class EchoScytheItem extends Item {

    public EchoScytheItem(Settings settings) {
        super(settings);
    }

    public static AttributeModifiersComponent createAttributes() {
        return AttributeModifiersComponent.builder()
            .add(EntityAttributes.ATTACK_DAMAGE,
                new EntityAttributeModifier(Identifier.of("politicalserver", "echo_scythe_damage"),
                    13.0, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .add(EntityAttributes.ATTACK_SPEED,
                new EntityAttributeModifier(Identifier.of("politicalserver", "echo_scythe_speed"),
                    -3.2, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .build();
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Area damage: hit all nearby entities for 3 HP
        if (attacker.getEntityWorld() instanceof ServerWorld serverWorld) {
            Box aoeBox = new Box(target.getX() - 3, target.getY() - 2, target.getZ() - 3,
                target.getX() + 3, target.getY() + 2, target.getZ() + 3);
            List<LivingEntity> nearby = serverWorld.getEntitiesByClass(LivingEntity.class, aoeBox,
                e -> e != attacker && e != target);
            for (LivingEntity nearbyEntity : nearby) {
                nearbyEntity.damage(serverWorld, serverWorld.getDamageSources().magic(), 3.0f);
            }
        }
        stack.damage(1, attacker, Hand.MAIN_HAND);
    }
}
