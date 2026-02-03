package com.political;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class CustomItemHandler {

    public static void register() {
        // Harvey's Stick - Lightning on attack
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            ItemStack held = player.getStackInHand(hand);
            if (isHarveysStick(held) && entity instanceof LivingEntity target) {
                if (target != player) {
                    LightningEntity lightning = EntityType.LIGHTNING_BOLT.create((ServerWorld) world, SpawnReason.TRIGGERED);
                    if (lightning != null) {
                        lightning.setPosition(target.getX(), target.getY(), target.getZ());
                        world.spawnEntity(lightning);
                    }
                }
            }

            return ActionResult.PASS;
        });

        // The Gavel - Wind charge launch on right click
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            ItemStack held = player.getStackInHand(hand);

            if (isTheGavel(held)) {
                int windChargeSlot = findWindCharge(player);
                if (windChargeSlot >= 0) {
                    player.getInventory().getStack(windChargeSlot).decrement(1);

                    // Launch player upward
                    player.setVelocity(player.getVelocity().add(0, 1.2, 0));
                    player.velocityDirty = true;

                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    public static void tickHermesShoes(ServerPlayerEntity player) {
        // Get boots using getEquippedStack
        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);

        if (isHermesShoes(boots)) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED, 40, 2, true, false, false
            ));
        }
    }

    private static boolean isHarveysStick(ItemStack stack) {
        if (!stack.isOf(Items.STICK)) return false;
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return false;
        return customData.copyNbt().getBoolean("harveys_stick").orElse(false);
    }

    private static boolean isTheGavel(ItemStack stack) {
        if (!stack.isOf(Items.MACE)) return false;
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return false;
        return customData.copyNbt().getBoolean("the_gavel").orElse(false);
    }

    public static boolean isHermesShoes(ItemStack stack) {
        if (!stack.isOf(Items.IRON_BOOTS)) return false;
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return false;
        return customData.copyNbt().getBoolean("hermes_shoes").orElse(false);
    }

    public static boolean isHPEBM(ItemStack stack) {
        if (!stack.isOf(Items.END_ROD)) return false;
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return false;
        return customData.copyNbt().getBoolean("hpebm").orElse(false);
    }

    private static int findWindCharge(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isOf(Items.WIND_CHARGE)) {
                return i;
            }
        }
        return -1;
    }
}