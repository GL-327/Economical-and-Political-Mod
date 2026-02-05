package com.political;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class CustomItemHandler {

    public static void register() {
        // Harvey's Stick - Lightning on attack
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity)) return ActionResult.PASS;

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

        // The Gavel AND HPEBM
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            ItemStack held = player.getStackInHand(hand);

            // The Gavel - Wind charge launch
            if (isTheGavel(held)) {
                int windChargeSlot = findWindCharge(player);
                if (windChargeSlot >= 0) {
                    player.getInventory().getStack(windChargeSlot).decrement(1);
                    player.setVelocity(player.getVelocity().add(0, 1.2, 0));
                    player.velocityDirty = true;
                    return ActionResult.SUCCESS;
                }
            }

            // HPEBM - Energy Beam
            if (isHPEBM(held)) {
                if (player.getItemCooldownManager().isCoolingDown(held)) {
                    return ActionResult.FAIL;
                }

                ServerWorld serverWorld = (ServerWorld) world;
                Vec3d start = player.getEyePos();
                Vec3d direction = player.getRotationVec(1.0f);

                Box searchBox = new Box(
                        start.x - 30, start.y - 30, start.z - 30,
                        start.x + 30, start.y + 30, start.z + 30
                );
                List<Entity> entities = world.getOtherEntities(player, searchBox, e -> e instanceof LivingEntity && e != player);

                for (Entity entity : entities) {
                    if (!(entity instanceof LivingEntity livingEntity)) continue;

                    Vec3d entityPos = new Vec3d(livingEntity.getX(), livingEntity.getY() + livingEntity.getHeight() / 2, livingEntity.getZ());
                    Vec3d toEntity = entityPos.subtract(start);
                    double distance = toEntity.length();
                    if (distance > 30) continue;

                    Vec3d projected = direction.multiply(toEntity.dotProduct(direction));
                    double perpendicularDist = toEntity.subtract(projected).length();

                    if (perpendicularDist < 1.5 && toEntity.dotProduct(direction) > 0) {
                        livingEntity.damage(serverWorld, player.getDamageSources().magic(), 8.0f);
                        serverWorld.spawnParticles(ParticleTypes.END_ROD,
                                livingEntity.getX(), livingEntity.getY() + livingEntity.getHeight() / 2, livingEntity.getZ(),
                                10, 0.3, 0.3, 0.3, 0.1);
                    }
                }

                // Visual beam
                for (int i = 0; i < 30; i++) {
                    Vec3d particlePos = start.add(direction.multiply(i));
                    serverWorld.spawnParticles(ParticleTypes.END_ROD,
                            particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
                }

                player.getItemCooldownManager().set(held, 60);
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });
    }

    public static void tickHermesShoes(ServerPlayerEntity player) {
        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
        if (isHermesShoes(boots)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 2, true, false, false));
        }
    }

    // ============ ITEM DETECTION - Works in 1.21.11 ============

    private static boolean hasCustomTag(ItemStack stack, String tagName) {
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return false;
        NbtCompound nbt = customData.copyNbt();
        if (!nbt.contains(tagName)) return false;
        try {
            // getByte returns Optional<Byte> in 1.21.11
            return nbt.getByte(tagName).orElse((byte) 0) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isHarveysStick(ItemStack stack) {
        return stack.isOf(Items.STICK) && hasCustomTag(stack, "harveys_stick");
    }

    private static boolean isTheGavel(ItemStack stack) {
        return stack.isOf(Items.MACE) && hasCustomTag(stack, "the_gavel");
    }

    public static boolean isHermesShoes(ItemStack stack) {
        return stack.isOf(Items.IRON_BOOTS) && hasCustomTag(stack, "hermes_shoes");
    }

    public static boolean isHPEBM(ItemStack stack) {
        return stack.isOf(Items.END_ROD) && hasCustomTag(stack, "hpebm");
    }

    private static int findWindCharge(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isOf(Items.WIND_CHARGE)) {
                return i;
            }
        }
        return -1;
    }

    // ============ ITEM CREATION ============

    public static ItemStack createHarveysStick() {
        return createCustomItem(Items.STICK, "harveys_stick", "Harvey's Stick", Formatting.GOLD);
    }

    public static ItemStack createTheGavel() {
        return createCustomItem(Items.MACE, "the_gavel", "The Gavel", Formatting.LIGHT_PURPLE);
    }

    public static ItemStack createHermesShoes() {
        return createCustomItem(Items.IRON_BOOTS, "hermes_shoes", "Hermes Shoes", Formatting.AQUA);
    }

    public static ItemStack createHPEBM() {
        return createCustomItem(Items.END_ROD, "hpebm", "HPEBM", Formatting.RED);
    }

    private static ItemStack createCustomItem(Item baseItem, String tagName, String displayName, Formatting color) {
        ItemStack stack = new ItemStack(baseItem);
        NbtCompound nbt = new NbtCompound();
        nbt.putByte(tagName, (byte) 1);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(color, Formatting.BOLD));
        return stack;
    }
}