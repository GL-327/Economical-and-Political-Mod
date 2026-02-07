package com.political;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CustomItemHandler {
    private static final Map<UUID, Boolean> wasSwinging = new HashMap<>();
    private static final Map<UUID, Long> gavelCooldowns = new HashMap<>();
    private static final long GAVEL_COOLDOWN_MS = 3000;
    private static final int GAVEL_COOLDOWN_TICKS = 60;
    private static final Map<UUID, Integer> hpebmUseTicks = new HashMap<>();
    private static final Map<UUID, Long> hpebmLastRightClick = new HashMap<>();
    private static final Map<UUID, Long> ultraOverclockedCooldowns = new HashMap<>();
    private static final long ULTRA_OVERCLOCKED_COOLDOWN_MS = 15000; // 15 seconds
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
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity)) return ActionResult.PASS;

            ItemStack held = player.getStackInHand(hand);
            if (isAnyBeamWeapon(held)) {
                hpebmLastRightClick.put(player.getUuid(), System.currentTimeMillis());
                return ActionResult.CONSUME;
            }
            return ActionResult.PASS;
        });
        // The Gavel
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            ItemStack heldItem = player.getStackInHand(hand);

            if (isTheGavel(heldItem)) {
                if (useGavelAbility(serverPlayer, heldItem)) {
                    return ActionResult.SUCCESS;
                }
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        // Prevent beam weapon placement
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            ItemStack held = player.getStackInHand(hand);
            if (isAnyBeamWeapon(held)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }

    public static boolean useGavelAbility(ServerPlayerEntity player, ItemStack gavelStack) {
        UUID uuid = player.getUuid();
        long now = System.currentTimeMillis();

        if (gavelCooldowns.containsKey(uuid)) {
            long remaining = (gavelCooldowns.get(uuid) + GAVEL_COOLDOWN_MS) - now;
            if (remaining > 0) return false;
        }

        boolean found = false;
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isOf(Items.WIND_CHARGE)) {
                player.getInventory().getStack(i).decrement(1);
                found = true;
                break;
            }
        }
        if (!found) {
            player.sendMessage(Text.literal("Requires 1 Wind Charge!").formatted(Formatting.RED), true);
            return false;
        }

        gavelCooldowns.put(uuid, now);
        player.getItemCooldownManager().set(gavelStack, GAVEL_COOLDOWN_TICKS);

        ServerWorld world = player.getEntityWorld();

        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                player.getX(), player.getY() + 1, player.getZ(),
                3, 2.0, 1.0, 2.0, 0.0);

        world.playSoundFromEntity(null, player, SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
                SoundCategory.PLAYERS, 1.0f, 1.0f);

        Box box = new Box(
                player.getX() - 4.5, player.getY() - 4.5, player.getZ() - 4.5,
                player.getX() + 4.5, player.getY() + 4.5, player.getZ() + 4.5);

        for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, box, ent -> ent != player)) {
            e.damage(world, player.getDamageSources().playerAttack(player), 25.0f);
        }

        player.sendMessage(Text.literal("⚡ GAVEL STRIKE! ⚡").formatted(Formatting.GOLD, Formatting.BOLD), true);
        return true;
    }
    public static void tickUltraOverclockedLeftClick(ServerPlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();

        if (getBeamTier(mainHand) != 7) {
            wasSwinging.remove(player.getUuid());
            return;
        }

        UUID uuid = player.getUuid();
        boolean isSwinging = player.handSwinging;
        boolean wasSwingingBefore = wasSwinging.getOrDefault(uuid, false);

        // Detect new swing (left-click)
        if (isSwinging && !wasSwingingBefore) {
            useUltraOverclockedAbility(player, mainHand);
        }

        wasSwinging.put(uuid, isSwinging);
    }
    public static void tickHermesShoes(ServerPlayerEntity player) {
        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
        if (isHermesShoes(boots)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 2, true, false, false));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // HPEBM TICK SYSTEM
    // ═══════════════════════════════════════════════════════════════

    public static void tickHPEBM(ServerPlayerEntity player) {
        ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack offHand = player.getStackInHand(Hand.OFF_HAND);

        boolean holdingBeam = isAnyBeamWeapon(mainHand) || isAnyBeamWeapon(offHand);
        UUID uuid = player.getUuid();

        long lastClick = hpebmLastRightClick.getOrDefault(uuid, 0L);
        boolean rightClickHeld = (System.currentTimeMillis() - lastClick) < 200;

        if (!holdingBeam || !rightClickHeld) {
            hpebmUseTicks.remove(uuid);
            return;
        }

        // Get tier for XP scaling
        ItemStack beamItem = isAnyBeamWeapon(mainHand) ? mainHand : offHand;
        int tier = getBeamTier(beamItem);

        // XP cost: Mk1=1, Mk2=2, Mk3=3, Mk4=4, Mk5=5 (tiers 3-7)
        // Base HPEBM and Ultra Beam cost 1
        int xpCostPerSecond = Math.max(1, tier - 2);

        if (player.experienceLevel < xpCostPerSecond) {
            player.sendMessage(Text.literal("⚡ Not enough XP! Need " + xpCostPerSecond + " level(s)").formatted(Formatting.RED), true);
            hpebmUseTicks.remove(uuid);
            return;
        }

        int ticks = hpebmUseTicks.getOrDefault(uuid, 0) + 1;
        hpebmUseTicks.put(uuid, ticks);

        if (ticks == 1) {
            player.addExperienceLevels(-xpCostPerSecond);
            player.sendMessage(Text.literal("⚡ Beam Activated! -" + xpCostPerSecond + " XP Level(s)").formatted(Formatting.YELLOW), true);
        } else if (ticks % 20 == 0) {
            player.addExperienceLevels(-xpCostPerSecond);
            player.sendMessage(Text.literal("⚡ -" + xpCostPerSecond + " XP Level(s)").formatted(Formatting.YELLOW), true);
        }

        fireHPEBMBeam(player, ticks);
    }

    private static void fireHPEBMBeam(ServerPlayerEntity player, int ticks) {
        ServerWorld world = player.getEntityWorld();

        ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack offHand = player.getStackInHand(Hand.OFF_HAND);
        ItemStack beamItem = isAnyBeamWeapon(mainHand) ? mainHand : offHand;
        int tier = getBeamTier(beamItem);

        float baseDamage = switch (tier) {
            case 1 -> 1.0f;
            case 2 -> 3.0f;
            case 3 -> 3.6f;
            case 4 -> 4.2f;
            case 5 -> 4.8f;
            case 6 -> 5.4f;
            case 7 -> 6.0f;
            default -> 1.0f;
        };

        Vec3d eyePos = player.getEyePos();
        Vec3d lookDir = player.getRotationVec(1.0f);
        Vec3d sideOffset = lookDir.crossProduct(new Vec3d(0, 1, 0)).normalize().multiply(0.7);  // Was 0.4, now further right
        Vec3d start = eyePos.add(sideOffset).add(0, -0.7, 0);  // Was -0.4, now further down
        Vec3d direction = lookDir;

        Vec3d endPoint = findBeamEndpoint(world, player, start, direction, 30.0);
        double beamLength = start.distanceTo(endPoint);


        Box searchBox = new Box(
                start.x - 30, start.y - 30, start.z - 30,
                start.x + 30, start.y + 30, start.z + 30);

        for (Entity entity : world.getOtherEntities(player, searchBox, e -> e instanceof LivingEntity && e != player)) {
            if (!(entity instanceof LivingEntity living)) continue;

            Vec3d entityPos = new Vec3d(living.getX(), living.getY() + living.getHeight() / 2, living.getZ());
            Vec3d toEntity = entityPos.subtract(start);
            double distance = toEntity.length();
            if (distance > 30) continue;

            Vec3d projected = direction.multiply(toEntity.dotProduct(direction));
            double perpendicularDist = toEntity.subtract(projected).length();

            if (perpendicularDist < 1.5 && toEntity.dotProduct(direction) > 0) {
                living.timeUntilRegen = 0;
                living.damage(world, player.getDamageSources().magic(), baseDamage);

                world.spawnParticles(ParticleTypes.CRIT,
                        living.getX(), living.getY() + living.getHeight() / 2, living.getZ(),
                        3, 0.2, 0.2, 0.2, 0.2);
            }
        }

        if (tier >= 2) {
            fireUpgradedBeam(world, start, endPoint, ticks, tier, player);
        } else {
            fireOriginalBeam(world, start, direction, ticks, player);  // Added player parameter
        }

        if (ticks % 10 == 0) {
            float pitch = tier >= 2 ? 0.5f + (tier * 0.15f) : 1.5f;
            world.playSound(null, player.getBlockPos(),
                    SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.PLAYERS, 0.5f, pitch);
        }
    }

    private static Vec3d findBeamEndpoint(ServerWorld world, ServerPlayerEntity player, Vec3d start, Vec3d direction, double maxRange) {
        Vec3d end = start.add(direction.multiply(maxRange));

        net.minecraft.util.hit.BlockHitResult blockHit = world.raycast(new net.minecraft.world.RaycastContext(
                start, end,
                net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                net.minecraft.world.RaycastContext.FluidHandling.NONE,
                player
        ));

        if (blockHit.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
            return blockHit.getPos();
        }
        return end;
    }

    private static void fireOriginalBeam(ServerWorld world, Vec3d start, Vec3d direction, int ticks, ServerPlayerEntity player) {
        // Calculate actual endpoint via raycast
        Vec3d maxEnd = start.add(direction.multiply(45));
        net.minecraft.util.hit.BlockHitResult blockHit = world.raycast(new net.minecraft.world.RaycastContext(
                start, maxEnd,
                net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                net.minecraft.world.RaycastContext.FluidHandling.NONE,
                player
        ));

        Vec3d endPoint = maxEnd;
        if (blockHit.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
            endPoint = blockHit.getPos();
        }
        double beamLength = start.distanceTo(endPoint);

        double spiralRadius = 0.25;
        double spiralSpeed = 0.6;

        for (double i = 0; i < beamLength; i += 1.5) {
            Vec3d basePos = start.add(direction.multiply(i));
            Vec3d up = new Vec3d(0, 1, 0);
            if (Math.abs(direction.dotProduct(up)) > 0.99) up = new Vec3d(1, 0, 0);
            Vec3d perp1 = direction.crossProduct(up).normalize();
            Vec3d perp2 = direction.crossProduct(perp1).normalize();
            double angle = (ticks * spiralSpeed + i * 2) % (Math.PI * 2);

            world.spawnParticles(ParticleTypes.CRIT, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.2);

            Vec3d spiral1 = basePos.add(perp1.multiply(Math.cos(angle) * spiralRadius)).add(perp2.multiply(Math.sin(angle) * spiralRadius));
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, spiral1.x, spiral1.y, spiral1.z, 1, 0, 0, 0, 0.25);

            Vec3d spiral2 = basePos.add(perp1.multiply(Math.cos(angle + Math.PI) * spiralRadius)).add(perp2.multiply(Math.sin(angle + Math.PI) * spiralRadius));
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, spiral2.x, spiral2.y, spiral2.z, 1, 0, 0, 0, 0.25);
        }
    }

    // Upgraded beam (Tiers 2-7) - unique particles per tier, fast dissipating
    private static void fireUpgradedBeam(ServerWorld world, Vec3d start, Vec3d endPoint, int ticks, int tier, ServerPlayerEntity player) {
        Vec3d direction = endPoint.subtract(start).normalize();
        double beamLength = start.distanceTo(endPoint);

        double spiralSpeed = 0.6 + (tier * 0.08);
        double spiralRadius = 0.2 + (tier * 0.03);
        int helixCount = Math.min(tier, 3);

        for (double i = 0; i < beamLength; i += 1.2) {
            Vec3d basePos = start.add(direction.multiply(i));
            Vec3d up = new Vec3d(0, 1, 0);
            if (Math.abs(direction.dotProduct(up)) > 0.99) up = new Vec3d(1, 0, 0);
            Vec3d perp1 = direction.crossProduct(up).normalize();
            Vec3d perp2 = direction.crossProduct(perp1).normalize();
            double angle = (ticks * spiralSpeed + i * 2) % (Math.PI * 2);

            // Core particles per tier (fast-dissipating, no drop)
            switch (tier) {
                case 2 -> world.spawnParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.2);
                case 3 -> {
                    world.spawnParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.2);
                    world.spawnParticles(ParticleTypes.CRIT, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.15);
                }
                case 4 -> {
                    world.spawnParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 2, 0, 0, 0, 0.15);
                    world.spawnParticles(ParticleTypes.GLOW, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.1);
                }
                case 5 -> {
                    world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.025);
                    world.spawnParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 2, 0, 0, 0, 0.2);
                }
                case 6 -> {
                    world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, basePos.x, basePos.y, basePos.z, 2, 0, 0, 0, 0.025);
                    world.spawnParticles(ParticleTypes.SCULK_SOUL, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.025);
                }
                case 7 -> {
                    // Reduced speed: 0.15 -> 0.05 (less movement = tighter beam)
                    world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.005);
                    // Reduced delta (0.02 -> 0.0) and speed (0.2 -> 0.05)
                    world.spawnParticles(ParticleTypes.GLOW, basePos.x, basePos.y, basePos.z, 1, 0.0, 0.0, 0.0, 0.005);
                    world.spawnParticles(ParticleTypes.SCULK_SOUL, basePos.x, basePos.y, basePos.z, 1, 0.0, 0.0, 0.0, 0.025);
                }
            }

            // Spiral helixes
            for (int h = 0; h < helixCount; h++) {
                double helixAngle = angle + (h * Math.PI * 2 / helixCount);
                Vec3d spiral = basePos
                        .add(perp1.multiply(Math.cos(helixAngle) * spiralRadius))
                        .add(perp2.multiply(Math.sin(helixAngle) * spiralRadius));
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, spiral.x, spiral.y, spiral.z, 1, 0, 0, 0, 0.2);
            }

            if (tier >= 5) {
                double outerRadius = spiralRadius * 1.5;
                double outerAngle = -angle * 0.5;
                Vec3d outer = basePos
                        .add(perp1.multiply(Math.cos(outerAngle) * outerRadius))
                        .add(perp2.multiply(Math.sin(outerAngle) * outerRadius));
                world.spawnParticles(ParticleTypes.CRIT, outer.x, outer.y, outer.z, 1, 0, 0, 0, 0.15);
            }
        }

        // EXPLOSION AT END POINT - Mk3 (tier 5) and above
        if (tier >= 5 && ticks % 5 == 0) {
            double explosionRadius = switch (tier) {
                case 5 -> 2.0;
                case 6 -> 2.5;
                case 7 -> 3.0;
                default -> 2.0;
            };

            float explosionDamage = switch (tier) {
                case 5 -> 4.0f;
                case 6 -> 6.0f;
                case 7 -> 10.0f;
                default -> 4.0f;
            };

            world.spawnParticles(ParticleTypes.EXPLOSION, endPoint.x, endPoint.y, endPoint.z, 1, 0, 0, 0, 0);

            if (tier >= 7) {
                world.spawnParticles(ParticleTypes.GLOW, endPoint.x, endPoint.y, endPoint.z, 10, 0.5, 0.5, 0.5, 0.3);
            }

            Box explosionBox = new Box(
                    endPoint.x - explosionRadius, endPoint.y - explosionRadius, endPoint.z - explosionRadius,
                    endPoint.x + explosionRadius, endPoint.y + explosionRadius, endPoint.z + explosionRadius);

            for (Entity entity : world.getOtherEntities(player, explosionBox, e -> e instanceof LivingEntity)) {
                if (entity instanceof LivingEntity living) {
                    Vec3d livingPos = new Vec3d(living.getX(), living.getY(), living.getZ());
                    double dist = livingPos.distanceTo(endPoint);
                    if (dist <= explosionRadius) {
                        living.timeUntilRegen = 0;
                        living.damage(world, player.getDamageSources().magic(), explosionDamage);
                    }
                }
            }
        }
    }

// ═══════════════════════════════════════════════════════════════
// ITEM DETECTION - Works in 1.21.11
// ═══════════════════════════════════════════════════════════════

    private static boolean hasCustomTag(ItemStack stack, String tagName) {
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return false;
        NbtCompound nbt = customData.copyNbt();
        if (!nbt.contains(tagName)) return false;
        try {
            return nbt.getByte(tagName).orElse((byte) 0) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isHarveysStick(ItemStack stack) {
        return stack.isOf(Items.STICK) && hasCustomTag(stack, "harveys_stick");
    }

    public static boolean isTheGavel(ItemStack stack) {
        return stack.isOf(Items.MACE) && hasCustomTag(stack, "the_gavel");
    }

    public static boolean isHermesShoes(ItemStack stack) {
        return stack.isOf(Items.IRON_BOOTS) && hasCustomTag(stack, "hermes_shoes");
    }

    // Add this method to CustomItemHandler
    public static boolean isHPEBM(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!stack.isOf(Items.IRON_SHOVEL)) return false;

        // Check for custom name component
        if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
            Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (name != null) {
                String nameStr = name.getString();
                return nameStr.contains("HPEBM") ||
                        nameStr.contains("Plasma Emitter") ||
                        nameStr.contains("Ultra Overclocked");
            }
        }
        return false;
    }

    public static boolean isWardenCore(ItemStack stack) {
        return stack.isOf(Items.ECHO_SHARD) && hasCustomTag(stack, "warden_core");
    }

// ═══════════════════════════════════════════════════════════════
// ITEM CREATION
// ═══════════════════════════════════════════════════════════════

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
        ItemStack stack = new ItemStack(Items.IRON_SHOVEL);
        NbtCompound nbt = new NbtCompound();
        nbt.putByte("hpebm", (byte) 1);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("H.P.E.B.M.").formatted(Formatting.GREEN, Formatting.BOLD));
        return stack;
    }
    public static ItemStack createUltraOverclockedBeam() {
        ItemStack beam = new ItemStack(Items.GOLDEN_SHOVEL);
        beam.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Ultra Overclocked Energy Based Plasma Emitter")
                        .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        beam.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("").formatted(Formatting.DARK_PURPLE),
                Text.literal("◆ ULTIMATE WEAPON ◆").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD),
                Text.literal(""),
                Text.literal("Right-click: Devastating beam attack").formatted(Formatting.RED),
                Text.literal("Left-click: Sonic Devastation").formatted(Formatting.DARK_PURPLE),
                Text.literal("  └ 15s cooldown").formatted(Formatting.GRAY),
                Text.literal(""),
                Text.literal("Damage: ").formatted(Formatting.GRAY)
                        .append(Text.literal("250").formatted(Formatting.RED, Formatting.BOLD)),
                Text.literal("Explosion Power: ").formatted(Formatting.GRAY)
                        .append(Text.literal("8.0").formatted(Formatting.GOLD, Formatting.BOLD)),
                Text.literal(""),
                Text.literal("「Tier VII」").formatted(Formatting.DARK_PURPLE)
        )));
        beam.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
// Alternative: just set max damage very high and don't let it break
        beam.set(DataComponentTypes.MAX_DAMAGE, Integer.MAX_VALUE);
        beam.set(DataComponentTypes.DAMAGE, 0);
        return beam;
    }
    public static ItemStack createWardenCore() {
        ItemStack stack = new ItemStack(Items.ECHO_SHARD);
        NbtCompound nbt = new NbtCompound();
        nbt.putByte("warden_core", (byte) 1);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Warden's Core").formatted(Formatting.DARK_AQUA, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("A pulsing core of sonic energy").formatted(Formatting.DARK_AQUA));
        lore.add(Text.literal("0.1% drop from Wardens").formatted(Formatting.GRAY));
        lore.add(Text.literal("Used to craft Ultra Beam weapons").formatted(Formatting.LIGHT_PURPLE));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return stack;
    }

    public static ItemStack createUltraBeam() {
        ItemStack stack = new ItemStack(Items.IRON_SHOVEL);
        NbtCompound nbt = new NbtCompound();
        nbt.putByte("ultra_beam", (byte) 1);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Ultra Beam Emitter").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("Ultra High Powered Energy Beam").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("Hold right click to fire a continuous beam").formatted(Formatting.GRAY));
        lore.add(Text.literal("Costs 1 XP level per second").formatted(Formatting.RED));
        lore.add(Text.literal("Tier: ULTRA").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("Damage: +50%").formatted(Formatting.GREEN));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return stack;
    }

    public static ItemStack createUltraBeamMk(int mk) {
        if (mk < 1 || mk > 5) return createUltraBeam();

        ItemStack stack = new ItemStack(Items.IRON_SHOVEL);
        NbtCompound nbt = new NbtCompound();
        nbt.putByte("ultra_beam_mk" + mk, (byte) 1);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        Formatting color = switch (mk) {
            case 1 -> Formatting.LIGHT_PURPLE;
            case 2 -> Formatting.DARK_PURPLE;
            case 3 -> Formatting.BLUE;
            case 4 -> Formatting.DARK_BLUE;
            case 5 -> Formatting.GOLD;
            default -> Formatting.LIGHT_PURPLE;
        };

        int damageBonus = 50 + (mk * 20);
        String suffix = mk == 5 ? " ✦ MAX ✦" : "";
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Ultra Beam Emitter Mk" + mk + suffix).formatted(color, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("Overclocked Energy Beam").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("Hold right click to fire a continuous beam").formatted(Formatting.GRAY));
        lore.add(Text.literal("Costs 1 XP level per second").formatted(Formatting.RED));
        lore.add(Text.literal("Tier: OVERCLOCKED Mk" + mk).formatted(color));
        lore.add(Text.literal("Damage: +" + damageBonus + "%").formatted(Formatting.GREEN));
        if (mk == 5) {
            lore.add(Text.literal("MAXIMUM POWER!").formatted(Formatting.GOLD, Formatting.BOLD));
        }
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        return stack;
    }

    private static ItemStack createCustomItem(Item baseItem, String tagName, String displayName, Formatting color) {
        ItemStack stack = new ItemStack(baseItem);
        NbtCompound nbt = new NbtCompound();
        nbt.putByte(tagName, (byte) 1);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName).formatted(color, Formatting.BOLD));
        return stack;
    }
    public static float getBeamExplosionPower(ItemStack stack) {
        int tier = getBeamTier(stack);
        return switch (tier) {
            case 1 -> 2.0f;
            case 2 -> 2.5f;
            case 3 -> 3.0f;
            case 4 -> 3.5f;
            case 5 -> 4.5f;
            case 6 -> 6.0f;   // Mk5
            case 7 -> 8.0f;   // Ultra Overclocked - massive
            default -> 1.5f;
        };
    }
    public static boolean useUltraOverclockedAbility(ServerPlayerEntity player, ItemStack stack) {
        if (getBeamTier(stack) != 7) return false;

        // Check cooldown
        if (player.getItemCooldownManager().isCoolingDown(stack)) {
            return false;
        }

        // Check for dragon's breath
        ItemStack dragonBreath = findItemInInventory(player, Items.DRAGON_BREATH);
        if (dragonBreath == null || dragonBreath.isEmpty()) {
            player.sendMessage(Text.literal("Requires Dragon's Breath!")
                    .formatted(Formatting.RED), true);
            return false;
        }

        // Check for XP (10 XP points)
        if (player.totalExperience < 10) {
            player.sendMessage(Text.literal("Requires 10 XP!")
                    .formatted(Formatting.RED), true);
            return false;
        }

        // Consume dragon's breath
        dragonBreath.decrement(1);

        // Consume XP
        player.addExperience(-10);

        // Set 15 second cooldown (300 ticks)
        player.getItemCooldownManager().set(stack, 300);

        ServerWorld world = player.getEntityWorld();
        Vec3d eyePos = player.getEyePos();
        Vec3d lookVec = player.getRotationVec(1.0f);
        Vec3d endPos = eyePos.add(lookVec.multiply(80));

        net.minecraft.util.hit.HitResult hitResult = world.raycast(new RaycastContext(
                eyePos, endPos,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
        ));

        Vec3d impactPos = (hitResult.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK)
                ? hitResult.getPos() : endPos;

        // Spawn beam particles along the path
        double distance = eyePos.distanceTo(impactPos);
        int particleCount = (int) (distance * 3);

        for (int i = 0; i < particleCount; i++) {
            double t = (double) i / particleCount;
            double x = eyePos.x + (impactPos.x - eyePos.x) * t;
            double y = eyePos.y + (impactPos.y - eyePos.y) * t;
            double z = eyePos.z + (impactPos.z - eyePos.z) * t;

            world.spawnParticles(ParticleTypes.SONIC_BOOM, x, y, z, 1, 0, 0, 0, 0);

            if (i % 2 == 0) {
                world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 2, 0.05, 0.05, 0.05, 0.01);
                world.spawnParticles(ParticleTypes.GLOW, x, y, z, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }

        // Play sonic boom sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 2.0f, 0.8f);

        // Explosion at impact
        world.createExplosion(player, impactPos.x, impactPos.y, impactPos.z,
                8.0f, true, net.minecraft.world.World.ExplosionSourceType.MOB);

        // Impact particles
        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, impactPos.x, impactPos.y, impactPos.z, 5, 1, 1, 1, 0);
        world.spawnParticles(ParticleTypes.SOUL, impactPos.x, impactPos.y, impactPos.z, 30, 2, 2, 2, 0.1);
        world.spawnParticles(ParticleTypes.SCULK_SOUL, impactPos.x, impactPos.y, impactPos.z, 20, 2, 2, 2, 0.05);

        // Damage nearby entities at impact
        List<LivingEntity> nearby = world.getEntitiesByClass(
                LivingEntity.class,
                new Box(impactPos.x - 6, impactPos.y - 6, impactPos.z - 6,
                        impactPos.x + 6, impactPos.y + 6, impactPos.z + 6),
                e -> e != player
        );

        for (LivingEntity entity : nearby) {
            double dist = Math.sqrt(
                    Math.pow(entity.getX() - impactPos.x, 2) +
                            Math.pow(entity.getY() - impactPos.y, 2) +
                            Math.pow(entity.getZ() - impactPos.z, 2)
            );
            float damage = (float) (150.0 * (1.0 - (dist / 6.0)));
            damage = Math.max(damage, 30.0f);
            entity.damage(world, world.getDamageSources().sonicBoom(player), damage);
        }

        player.sendMessage(Text.literal("⚡ SONIC DEVASTATION ⚡")
                .formatted(Formatting.DARK_PURPLE, Formatting.BOLD), true);

        return true;
    }

    // Helper method to find item in inventory
    private static ItemStack findItemInInventory(ServerPlayerEntity player, net.minecraft.item.Item item) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(item)) {
                return stack;
            }
        }
        return null;
    }

    public static int getUltraOverclockedCooldownSeconds(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        long now = System.currentTimeMillis();
        long lastUse = ultraOverclockedCooldowns.getOrDefault(uuid, 0L);
        long remaining = ULTRA_OVERCLOCKED_COOLDOWN_MS - (now - lastUse);

        if (remaining <= 0) return 0;
        return (int) Math.ceil(remaining / 1000.0);
    }
// ═══════════════════════════════════════════════════════════════
// BEAM TIER SYSTEM
// ═══════════════════════════════════════════════════════════════

    public static int getBeamTier(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        if (!stack.isOf(Items.IRON_SHOVEL) && !stack.isOf(Items.GOLDEN_SHOVEL)) return 0;

        // Check by custom name first
        if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
            String name = stack.get(DataComponentTypes.CUSTOM_NAME).getString();

            if (name.contains("Ultra Overclocked")) return 7;  // Tier 7 - Ultimate
            if (name.contains("Mk5")) return 6;                 // Tier 6
            if (name.contains("Mk4")) return 5;                 // Tier 5
            if (name.contains("Mk3")) return 4;                 // Tier 4
            if (name.contains("Mk2")) return 3;                 // Tier 3
            if (name.contains("Mk1")) return 2;                 // Tier 2
            if (name.contains("Ultra")) return 1;               // Tier 1 (Ultra HPEBM, no Mk)
            if (name.contains("H.P.E.B.M.") || name.contains("HPEBM")) return 0; // Base tier
        }

        // Fallback: check NBT for legacy items
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null) {
            NbtCompound nbt = customData.copyNbt();
            if (nbt.contains("ultra_beam_mk5")) return 6;
            if (nbt.contains("ultra_beam_mk4")) return 5;
            if (nbt.contains("ultra_beam_mk3")) return 4;
            if (nbt.contains("ultra_beam_mk2")) return 3;
            if (nbt.contains("ultra_beam_mk1")) return 2;
            if (nbt.contains("ultra_beam")) return 1;
            if (nbt.contains("hpebm")) return 0;
        }

        return 0;
    }



    public static boolean isAnyBeamWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!stack.isOf(Items.IRON_SHOVEL) && !stack.isOf(Items.GOLDEN_SHOVEL)) return false;

        if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
            String name = stack.get(DataComponentTypes.CUSTOM_NAME).getString();
            return name.contains("HPEBM") ||
                    name.contains("H.P.E.B.M.") ||
                    name.contains("Plasma Emitter") ||
                    name.contains("Ultra Overclocked") ||
                    name.contains("Ultra Beam");
        }

        // Fallback: check NBT for legacy
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null) {
            NbtCompound nbt = customData.copyNbt();
            return nbt.contains("hpebm") || nbt.contains("ultra_beam") ||
                    nbt.contains("ultra_beam_mk1") || nbt.contains("ultra_beam_mk2") ||
                    nbt.contains("ultra_beam_mk3") || nbt.contains("ultra_beam_mk4") ||
                    nbt.contains("ultra_beam_mk5");
        }

        return false;
    }

    }