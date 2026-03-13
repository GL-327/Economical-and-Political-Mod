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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import java.util.List;

/** Axe with a sonic pulse on kill */
public class EchoAxeItem extends Item {

    public EchoAxeItem(Settings settings) {
        super(settings);
    }

    public static AttributeModifiersComponent createAttributes() {
        return AttributeModifiersComponent.builder()
            .add(EntityAttributes.ATTACK_DAMAGE,
                new EntityAttributeModifier(Identifier.of("politicalserver", "echo_axe_damage"),
                    6.0, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .add(EntityAttributes.ATTACK_SPEED,
                new EntityAttributeModifier(Identifier.of("politicalserver", "echo_axe_speed"),
                    -3.1, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .build();
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Sonic pulse on kill: stun nearby entities if target is dead
        if (target.getHealth() <= 0 && attacker.getEntityWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.0f, 1.5f);
            Box aoeBox = new Box(target.getX() - 6, target.getY() - 4, target.getZ() - 6,
                target.getX() + 6, target.getY() + 4, target.getZ() + 6);
            List<LivingEntity> nearby = serverWorld.getEntitiesByClass(LivingEntity.class, aoeBox,
                e -> e != attacker);
            for (LivingEntity nearbyEntity : nearby) {
                nearbyEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 4));
            }
            if (attacker instanceof ServerPlayerEntity player) {
                player.sendMessage(Text.literal("⚡ Echo Pulse!").formatted(Formatting.AQUA), true);
            }
        }
        stack.damage(1, attacker, Hand.MAIN_HAND);
    }
}
