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
import net.minecraft.item.Items;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
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
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.RaycastContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import java.util.HashSet;
import java.util.Set;
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
    private static final long ULTRA_OVERCLOCKED_COOLDOWN_MS = 10000; // 15 seconds
    private static final Map<UUID, Boolean> berserkerHelmetActive = new HashMap<>();
    private static final Map<UUID, Long> berserkerWarningCooldown = new HashMap<>();
    private static final long WARNING_COOLDOWN_MS = 3000; // 3 seconds between warnings

    // Ender Phase Helmet state tracking
    private static final Map<UUID, Long> enderWarpCooldowns = new HashMap<>();
    private static final long ENDER_WARP_COOLDOWN_MS = 2000; // 2 seconds
    private static final double ENDER_WARP_MAX_DISTANCE = 100.0; // blocks (matches item lore: "up to 100 blocks")
    /** Debounce to prevent UseItemCallback + UseBlockCallback both firing on the same interaction. */
    private static final Map<UUID, Long> helmetInteractionDebounce = new HashMap<>();
    private static final long HELMET_DEBOUNCE_MS = 300; // 300ms window
    private static final Map<UUID, Long> enderPhaseCooldowns = new HashMap<>();
    private static final long ENDER_PHASE_COOLDOWN_MS = 120_000; // 2 minutes
    private static final Map<UUID, Long> enderPhaseActiveUntil = new HashMap<>();
    private static final long ENDER_PHASE_DURATION_MS = 15_000; // 15 seconds

    public static void register() {
        registerBountyItemRestrictions();
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

        // Ender Phase Helmet - right-click handler
        // Guard against the OFF_HAND duplicate: Minecraft sends PlayerInteractItemC2SPacket
        // for both hands when right-clicking with empty hands. Without this guard the
        // MAIN_HAND call would activate Ender Phase Mode and the OFF_HAND call (fired
        // immediately after) would see the mode as active and deactivate it again.
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            ItemStack helmet = serverPlayer.getEquippedStack(EquipmentSlot.HEAD);
            if (!SlayerItems.isEnderPhaseHelmet(helmet)) return ActionResult.PASS;

            // Check level requirement
            if (!SlayerItems.canUseEnderPhaseHelmet(serverPlayer)) {
                int req = SlayerItems.ENDER_PHASE_HELMET_LEVEL_REQ;
                serverPlayer.sendMessage(
                    Text.literal("⛔ Requires Enderman Bounty Level " + req + " to use!").formatted(Formatting.RED), true);
                return ActionResult.PASS;
            }

            return handleEnderPhaseActivation(serverPlayer);
        });

        // Ender Phase Helmet - also trigger when right-clicking a block with the helmet
        // (fallback for cases where UseItemCallback does not fire with empty hands)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            // Only intercept when main hand is empty
            if (!serverPlayer.getMainHandStack().isEmpty()) return ActionResult.PASS;

            ItemStack helmet = serverPlayer.getEquippedStack(EquipmentSlot.HEAD);
            if (!SlayerItems.isEnderPhaseHelmet(helmet)) return ActionResult.PASS;

            if (!SlayerItems.canUseEnderPhaseHelmet(serverPlayer)) {
                int req = SlayerItems.ENDER_PHASE_HELMET_LEVEL_REQ;
                serverPlayer.sendMessage(
                    Text.literal("⛔ Requires Enderman Bounty Level " + req + " to use!").formatted(Formatting.RED), true);
                return ActionResult.PASS;
            }

            return handleEnderPhaseActivation(serverPlayer);
        });
    }

    /**
     * Core logic for Voidwalker Crown right-click and sneak+right-click abilities.
     * Called from both UseItemCallback and UseBlockCallback so it fires reliably
     * regardless of what (if anything) is in the player's hands.
     *
     * <p>A 300ms debounce prevents the double-fire that occurs when the player
     * right-clicks on a block with empty hands: Minecraft sends both a
     * UseItemCallback AND a UseBlockCallback for the same interaction in that case.
     * Without the debounce, Sneak+RClick would activate Phase Mode and then the
     * second callback would immediately see it as active and deactivate it.</p>
     */
    private static ActionResult handleEnderPhaseActivation(ServerPlayerEntity serverPlayer) {
            long now = System.currentTimeMillis();
            UUID uuid = serverPlayer.getUuid();

            // Debounce — skip duplicate invocations from the same user interaction
            Long lastInteraction = helmetInteractionDebounce.get(uuid);
            if (lastInteraction != null && now - lastInteraction < HELMET_DEBOUNCE_MS) {
                return ActionResult.CONSUME;
            }
            helmetInteractionDebounce.put(uuid, now);

            if (serverPlayer.isSneaking()) {
                // Sneak + right-click: Toggle Ender Phase Mode
                Long activeUntil = enderPhaseActiveUntil.get(uuid);
                boolean isInPhaseMode = activeUntil != null && now < activeUntil;

                if (isInPhaseMode) {
                    // Deactivate
                    enderPhaseActiveUntil.remove(uuid);
                    enderPhaseCooldowns.put(uuid, now + ENDER_PHASE_COOLDOWN_MS);
                    serverPlayer.removeStatusEffect(StatusEffects.INVISIBILITY);
                    serverPlayer.removeStatusEffect(StatusEffects.WITHER);
                    serverPlayer.sendMessage(Text.literal("⟡ Ender Phase Mode deactivated.")
                            .formatted(Formatting.DARK_PURPLE), true);
                    return ActionResult.CONSUME;
                }

                Long cooldownUntil = enderPhaseCooldowns.get(uuid);
                if (cooldownUntil != null && now < cooldownUntil) {
                    long remainingSec = (cooldownUntil - now) / 1000;
                    serverPlayer.sendMessage(Text.literal("⟡ Ender Phase on cooldown: " + remainingSec + "s")
                            .formatted(Formatting.RED), true);
                    return ActionResult.CONSUME;
                }

                // Activate Ender Phase Mode
                enderPhaseActiveUntil.put(uuid, now + ENDER_PHASE_DURATION_MS);
                // ambient=false, showParticles=false, showIcon=true
                serverPlayer.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, true));
                // Brief blindness flash (purple tint) to signal mode activation; ambient=false, showParticles=true, showIcon=false
                serverPlayer.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.BLINDNESS, 300, 3, false, true, false));
                serverPlayer.sendMessage(Text.literal("⟡ Ender Phase Mode ACTIVE — 15 seconds!")
                        .formatted(Formatting.DARK_PURPLE, Formatting.BOLD), true);
                return ActionResult.CONSUME;
            } else {
                // Normal right-click: Ender Warp (Phase Teleport — teleport up to 100 blocks in look direction)
                Long cooldownUntil = enderWarpCooldowns.get(uuid);
                if (cooldownUntil != null && now < cooldownUntil) {
                    return ActionResult.CONSUME;
                }

                ServerWorld serverWorld = (ServerWorld) serverPlayer.getEntityWorld();
                Vec3d eyePos = serverPlayer.getEyePos();
                Vec3d lookVec = serverPlayer.getRotationVec(1.0f);
                Vec3d targetPos = eyePos.add(lookVec.multiply(ENDER_WARP_MAX_DISTANCE));

                net.minecraft.util.hit.BlockHitResult hitResult = serverWorld.raycast(
                        new RaycastContext(eyePos, targetPos,
                                RaycastContext.ShapeType.OUTLINE,
                                RaycastContext.FluidHandling.NONE,
                                serverPlayer));

                Vec3d dest = hitResult.getType() == net.minecraft.util.hit.HitResult.Type.MISS
                        ? targetPos
                        : hitResult.getPos();
                // Place player slightly above the hit surface to avoid clipping
                serverPlayer.teleport(serverWorld,
                        dest.x, dest.y + 0.1, dest.z,
                        Set.of(), serverPlayer.getYaw(), serverPlayer.getPitch(), false);

                // Ender particles at destination
                serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.PORTAL,
                        dest.x, dest.y + 0.5, dest.z, 15, 0.3, 0.5, 0.3, 0.05);

                serverWorld.playSound(null, serverPlayer.getBlockPos(),
                        SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                        SoundCategory.PLAYERS, 1.0f, 1.0f);

                enderWarpCooldowns.put(uuid, now + ENDER_WARP_COOLDOWN_MS);
                return ActionResult.CONSUME;
            }
    }

    /** Called on player disconnect to clean up per-player state and prevent memory leaks. */
    public static void onPlayerDisconnect(UUID uuid) {
        enderWarpCooldowns.remove(uuid);
        enderPhaseCooldowns.remove(uuid);
        enderPhaseActiveUntil.remove(uuid);
        helmetInteractionDebounce.remove(uuid);
        gavelCooldowns.remove(uuid);
        ultraOverclockedCooldowns.remove(uuid);
        berserkerHelmetActive.remove(uuid);
        berserkerWarningCooldown.remove(uuid);
        hpebmUseTicks.remove(uuid);
        hpebmLastRightClick.remove(uuid);
        wasSwinging.remove(uuid);
    }

    // ============================================================
// BLOCK BOUNTY ITEM USE (non-usable/eatable) [1]
// ============================================================
    public static void registerBountyItemRestrictions() {
        // Block using/eating bounty items
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) return ActionResult.PASS;

            ItemStack held = player.getStackInHand(hand);

            // Block using cores and bounty items
            if (isBountyItemRestricted(held)) {
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });
    }

    // Check if item should be blocked from use
    public static boolean isBountyItemRestricted(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        // Check custom name for bounty items
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;

        String nameStr = name.getString();
        return nameStr.contains("Core") ||
                nameStr.contains("Bounty Sword") ||
                nameStr.contains("Slayer Sword") ||
                nameStr.contains("Berserker Helmet") ||
                nameStr.contains("Venomous Crawler") ||
                nameStr.contains("Bone Desperado") ||
                nameStr.contains("Gelatinous Rustler") ||
                nameStr.contains("Sculk Terror");
    }

    // Check if item is a bounty item (for shop restriction)
    public static boolean isBountyItem(ItemStack stack) {
        return SlayerItems.isAnyBountyItem(stack) || isSlayerCore(stack);
    }

    /**
     * Returns true for any custom item that should be protected from vanilla consumption
     * (placement, crafting, smelting).
     */
    public static boolean isProtectedItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Check NBT-based bounty identification first (works even without custom name)
        if (SlayerItems.isAnyBountyItem(stack)) return true;
        // Then check custom name for other protected items
        if (stack.get(DataComponentTypes.CUSTOM_NAME) == null) return false;
        String name = stack.get(DataComponentTypes.CUSTOM_NAME).getString();
        return name.contains("Core") ||
                name.contains("Chunk") ||
                name.contains("Bounty") ||
                name.contains("HPEBM") ||
                name.contains("Overclocked") ||
                name.contains("Hermes") ||
                name.contains("Gavel") ||
                name.contains("Harvey") ||
                name.contains("Desperado") ||
                name.contains("Venomous") ||
                name.contains("Berserker") ||
                name.contains("Crawler") ||
                name.contains("Warden") ||
                name.contains("Sculk Terror") ||
                name.contains("Outlaw") ||
                name.contains("Rustler") ||
                name.contains("Phantom") ||
                name.contains("Bandit") ||
                name.contains("Plasma Emitter");
    }

    public static boolean isSlayerCore(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains("Core");
    }
// Add this method to CustomItemHandler.java:
// Add this method to CustomItemHandler.java:

    // Slime boots death save tracking
    private static final Map<UUID, Long> slimeBootsCooldowns = new HashMap<>();
    private static final Map<UUID, Long> slimeBootsActiveUntil = new HashMap<>();
    private static final Set<UUID> slimeBootsShrunk = new HashSet<>();
    private static final long SLIME_BOOTS_COOLDOWN_MS = 3200 * 1000; // 53 minutes 20 seconds
    private static final long SLIME_BOOTS_ACTIVE_MS = 30 * 1000; // 30 seconds

    // Level warning cooldown to prevent spam
    private static final Map<UUID, Long> levelWarningCooldowns = new HashMap<>();

    public static ItemStack createHPEBM(int mk) {
        // mk 6 and 7 are both the Ultra Overclocked (Mk.VI) — the max tier
        if (mk >= 6) return createUltraOverclockedBeam();

        ItemStack weapon = new ItemStack(Items.IRON_SHOVEL);

        // Unique names per tier
        String[] names = {
            "HPEBM Mk.I — Prototype",
            "HPEBM Mk.II — Standard Issue",
            "HPEBM Mk.III — Advanced",
            "HPEBM Mk.IV — Elite",
            "HPEBM Mk.V — Legendary"
        };
        String name = (mk >= 1 && mk <= 5) ? names[mk - 1] : "HPEBM Mk" + mk;

        Formatting color = switch (mk) {
            case 1 -> Formatting.WHITE;
            case 2 -> Formatting.GREEN;
            case 3 -> Formatting.YELLOW;
            case 4 -> Formatting.GOLD;
            case 5 -> Formatting.LIGHT_PURPLE;
            default -> Formatting.WHITE;
        };

        weapon.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(name).formatted(color, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));

        // Tier classification
        String tierTag = switch (mk) {
            case 1 -> "◆ COMMON";
            case 2 -> "◆ UNCOMMON";
            case 3 -> "◆ RARE";
            case 4 -> "◆ EPIC";
            case 5 -> "◆ LEGENDARY";
            default -> "◆ UNKNOWN";
        };
        Formatting tierColor = switch (mk) {
            case 1 -> Formatting.GRAY;
            case 2 -> Formatting.GREEN;
            case 3 -> Formatting.BLUE;
            case 4 -> Formatting.GOLD;
            case 5 -> Formatting.LIGHT_PURPLE;
            default -> Formatting.GRAY;
        };
        lore.add(Text.literal(tierTag + " — High-Powered Energy Beam").formatted(tierColor, Formatting.BOLD));
        lore.add(Text.literal(""));

        // Unique flavor text per tier
        switch (mk) {
            case 1 -> {
                lore.add(Text.literal("An experimental energy emitter salvaged from").formatted(Formatting.GRAY));
                lore.add(Text.literal("classified research archives. Unstable but").formatted(Formatting.GRAY));
                lore.add(Text.literal("functional as a proof-of-concept weapon.").formatted(Formatting.GRAY));
            }
            case 2 -> {
                lore.add(Text.literal("Military-grade energy emitter issued to elite").formatted(Formatting.GRAY));
                lore.add(Text.literal("field operatives. Refined plasma containment").formatted(Formatting.GRAY));
                lore.add(Text.literal("ensures consistent output under field conditions.").formatted(Formatting.GRAY));
            }
            case 3 -> {
                lore.add(Text.literal("Equipped with an enhanced focusing crystal,").formatted(Formatting.GRAY));
                lore.add(Text.literal("this unit concentrates plasma into a tighter").formatted(Formatting.GRAY));
                lore.add(Text.literal("beam with explosive endpoint detonation.").formatted(Formatting.GRAY));
            }
            case 4 -> {
                lore.add(Text.literal("Forged from rare celestial alloys, this unit").formatted(Formatting.GRAY));
                lore.add(Text.literal("was commissioned by the highest echelons of").formatted(Formatting.GRAY));
                lore.add(Text.literal("command. Few have ever seen one in person.").formatted(Formatting.GRAY));
            }
            case 5 -> {
                lore.add(Text.literal("The pinnacle of plasma engineering. Rumored").formatted(Formatting.GRAY));
                lore.add(Text.literal("to have been assembled using the core of a").formatted(Formatting.GRAY));
                lore.add(Text.literal("collapsed star. Maximum power. Maximum risk.").formatted(Formatting.GRAY));
            }
        }

        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(color));
        lore.add(Text.literal("⚡ Damage/tick: ").formatted(Formatting.WHITE)
                .append(Text.literal(String.valueOf(5 * mk)).formatted(Formatting.RED, Formatting.BOLD)));
        int xpCost = Math.max(1, mk - 2);
        lore.add(Text.literal("💡 XP Cost: ").formatted(Formatting.WHITE)
                .append(Text.literal(xpCost + " level(s)/sec").formatted(Formatting.YELLOW)));
        lore.add(Text.literal("📡 Beam Range: ").formatted(Formatting.WHITE)
                .append(Text.literal("30 blocks").formatted(Formatting.AQUA)));

        if (mk >= 3) {
            float explosionDmg = switch (mk) {
                case 3, 4 -> 0.0f;
                case 5 -> 4.0f;
                default -> 0.0f;
            };
            if (mk >= 5) {
                lore.add(Text.literal("💥 Endpoint Explosion: ").formatted(Formatting.WHITE)
                        .append(Text.literal(explosionDmg + " dmg, 2.0 radius").formatted(Formatting.GOLD)));
            }
        }

        lore.add(Text.literal(""));
        lore.add(Text.literal("Right-click: Continuous beam (hold)").formatted(Formatting.YELLOW));

        weapon.set(DataComponentTypes.LORE, new LoreComponent(lore));

        if (mk >= 4) {
            weapon.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        if (mk == 7) {
            SlayerItems.setCustomItemId(weapon, "hpebm_ultra");
        } else {
            SlayerItems.setCustomItemId(weapon, "hpebm_mk" + mk);
        }
        return weapon;
    }

    /** @deprecated Use {@link #createTheGavel()} instead. */
    @Deprecated
    public static ItemStack createGavel() {
        return createTheGavel();
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

        // Fling the player upward
        player.setVelocity(player.getVelocity().add(0, 1.5, 0));
        player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(player));

        // Wind/cloud particles
        world.spawnParticles(ParticleTypes.CLOUD,
                player.getX(), player.getY() + 0.5, player.getZ(),
                20, 0.5, 0.2, 0.5, 0.05);
        world.spawnParticles(ParticleTypes.POOF,
                player.getX(), player.getY(), player.getZ(),
                15, 0.6, 0.1, 0.6, 0.1);

        // Launch sound
        world.playSoundFromEntity(null, player, SoundEvents.ITEM_TRIDENT_THROW.value(),
                SoundCategory.PLAYERS, 1.0f, 0.6f);

        player.sendMessage(Text.literal("⚡ GAVEL LAUNCH! ⚡").formatted(Formatting.GOLD, Formatting.BOLD), true);
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

        // Check if player is right-clicking - if so, don't trigger left-click ability
        long lastClick = hpebmLastRightClick.getOrDefault(uuid, 0L);
        boolean isRightClicking = (System.currentTimeMillis() - lastClick) < 200;

        // Detect new swing (left-click) - only if NOT ng
        if (isSwinging && !wasSwingingBefore && !isRightClicking) {
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

    /** Returns true if the given player is currently in Ender Phase Mode. */
    public static boolean isInEnderPhaseMode(java.util.UUID uuid) {
        Long activeUntil = enderPhaseActiveUntil.get(uuid);
        return activeUntil != null && System.currentTimeMillis() < activeUntil;
    }

    public static void tickEnderPhaseHelmet(ServerPlayerEntity player) {
        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        if (!SlayerItems.isEnderPhaseHelmet(helmet)) {
            // If they removed the helmet while in phase, cancel phase
            UUID uuid = player.getUuid();
            if (enderPhaseActiveUntil.containsKey(uuid)) {
                enderPhaseActiveUntil.remove(uuid);
                player.removeStatusEffect(StatusEffects.INVISIBILITY);
                player.removeStatusEffect(StatusEffects.WITHER);
                enderPhaseCooldowns.put(uuid, System.currentTimeMillis() + ENDER_PHASE_COOLDOWN_MS);
            }
            return;
        }

        UUID uuid = player.getUuid();
        long now = System.currentTimeMillis();
        Long activeUntil = enderPhaseActiveUntil.get(uuid);

        if (activeUntil == null) return; // Not in phase mode

        if (now >= activeUntil) {
            // Phase has expired — apply scaling wither based on how long they overstayed
            long overstayMs = now - activeUntil;
            int witherAmplifier = (int) Math.min(4, overstayMs / 3000); // +1 level every 3 seconds
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WITHER, 60, witherAmplifier, false, true, true));

            if (overstayMs > 30_000) {
                // Forcibly end phase after 30 extra seconds
                enderPhaseActiveUntil.remove(uuid);
                player.removeStatusEffect(StatusEffects.INVISIBILITY);
                enderPhaseCooldowns.put(uuid, now + ENDER_PHASE_COOLDOWN_MS);
                player.sendMessage(Text.literal("⟡ Ender Phase Mode forcibly ended!")
                        .formatted(Formatting.DARK_RED, Formatting.BOLD), true);
            } else {
                long remainWarning = (activeUntil + 30_000 - now) / 1000;
                player.sendMessage(Text.literal("⟡ ⚠ Wither Lv" + (witherAmplifier + 1) + " — exit phase! " + remainWarning + "s left")
                        .formatted(Formatting.DARK_RED), true);
            }
        } else {
            // Still in phase — show countdown
            long remainMs = activeUntil - now;
            // Show countdown roughly once per second (~50ms window accounts for tick-timing variance)
            if (remainMs % 1000 < 50) {
                long remainSec = remainMs / 1000;
                player.sendMessage(Text.literal("⟡ Ender Phase: " + remainSec + "s")
                        .formatted(Formatting.DARK_PURPLE), true);
            }
            // Ensure invisibility is maintained; ambient=false, showParticles=false, showIcon=true
            if (!player.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.INVISIBILITY, 60, 0, false, false, true));
            }
        }
    }


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


        ItemStack beamItem = isAnyBeamWeapon(mainHand) ? mainHand : offHand;
        int tier = getBeamTier(beamItem);


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

    private static final Map<UUID, Long> levelWarningCooldown = new HashMap<>();
    private static final long LEVEL_WARNING_COOLDOWN_MS = 2000;


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

    public static void markRightClick(ServerPlayerEntity player) {
        hpebmLastRightClick.put(player.getUuid(), System.currentTimeMillis());
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
                case 2 ->
                        world.spawnParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.2);
                case 3 -> {
                    world.spawnParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.2);
                    world.spawnParticles(ParticleTypes.CRIT, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.15);
                }
                case 4 -> {
                    world.spawnParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 2, 0, 0, 0, 0.15);
                    world.spawnParticles(ParticleTypes.END_ROD, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.01);
                }
                case 5 -> {
                    world.spawnParticles(ParticleTypes.WITCH, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.01);
                    world.spawnParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 2, 0, 0, 0, 0.2);
                }
                case 6 -> {
                    world.spawnParticles(ParticleTypes.WITCH, basePos.x, basePos.y, basePos.z, 2, 0, 0, 0, 0.01);
                    world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.05);
                }
                case 7 -> {
                    // Reduced speed: 0.15 -> 0.05 (less movement = tighter beam)
                    world.spawnParticles(ParticleTypes.BUBBLE, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.015);
                    world.spawnParticles(ParticleTypes.REVERSE_PORTAL, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.5);
                    // Reduced delta (0.02 -> 0.0) and speed (0.2 -> 0.05)
                    world.spawnParticles(ParticleTypes.CRIT, basePos.x, basePos.y, basePos.z, 1, 0.0, 0.0, 0.0, 0.01);
                    world.spawnParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 1, 0.0, 0.0, 0.0, 0.01);
                    world.spawnParticles(ParticleTypes.WHITE_ASH, basePos.x, basePos.y, basePos.z, 1, 0.0, 0.0, 0.0, 0.01);
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
                world.spawnParticles(ParticleTypes.CRIT, outer.x, outer.y, outer.z, 1, 0, 0, 0, 0.015);
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

    public static void registerSlayerDamageHook() {
        // Hook into entity damage - Fabric API
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            // Only process if attacker is a player
            if (!(source.getAttacker() instanceof ServerPlayerEntity player)) {
                return true; // Allow damage, no modification
            }

            // Check if target is a slayer boss
            if (!SlayerManager.isSlayerBoss(entity.getUuid())) {
                return true;
            }

            // Get the weapon
            ItemStack weapon = player.getMainHandStack();

            // Calculate damage resistance bypass
            double damageResistance = SlayerManager.getDamageResistance(entity.getUuid());

            // Slayer weapons bypass resistance entirely
            if (SlayerItems.bypassesSlayerResistance(weapon)) {
                // No resistance applied, allow full damage
                return true;
            }

            // Non-slayer weapons take resistance penalty
            if (damageResistance > 0) {
                // We can't modify damage directly with ALLOW_DAMAGE
                // Instead we'll use a mixin or apply damage ourselves
                // For now, return true and handle in modification event
            }

            return true;
        });
    }

    public static float calculateSlayerDamage(ServerPlayerEntity attacker, LivingEntity target, float baseDamage) {
        ItemStack weapon = attacker.getMainHandStack();
        float modifiedDamage = baseDamage;

        // Apply T1 slayer sword multiplier (2x)
        double swordMultiplier = SlayerItems.getSlayerSwordDamageMultiplier(weapon, target, attacker);
        modifiedDamage *= (float) swordMultiplier;

        // Apply T2 slayer sword multiplier (3x) — only if T1 multiplier wasn't already applied
        if (swordMultiplier == 1.0) {
            double upgradedMultiplier = SlayerItems.getUpgradedSlayerSwordDamageMultiplier(weapon, target, attacker);
            modifiedDamage *= (float) upgradedMultiplier;
        }

        // Apply boss damage resistance (only for non-slayer weapons)
        if (SlayerManager.isSlayerBoss(target.getUuid())) {
            if (!SlayerItems.bypassesSlayerResistance(weapon) && !SlayerItems.isUpgradedSlayerSword(weapon)) {
                double resistance = SlayerManager.getDamageResistance(target.getUuid());
                modifiedDamage *= (float) (1.0 - resistance);
            }
        }

        // Crown of Greed: multiply damage by number of digits in coin count
        if (SlayerItems.isCrownOfGreed(attacker.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD))) {
            int coins = CoinManager.getCoins(attacker);
            int digits = coins > 0 ? String.valueOf(coins).length() : 1;
            modifiedDamage *= digits;
        }

        return modifiedDamage;
    }

    /** Tick handler for Crown of Greed: enforces 4 HP max health and suppresses potion effects. */
    public static void tickCrownOfGreed(ServerPlayerEntity player) {
        ItemStack helmet = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD);
        net.minecraft.util.Identifier modId = net.minecraft.util.Identifier.of("political", "crown_of_greed_hp");
        var healthAttr = player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.MAX_HEALTH);

        if (!SlayerItems.isCrownOfGreed(helmet)) {
            // Remove the modifier if crown is no longer worn
            if (healthAttr != null && healthAttr.getModifier(modId) != null) {
                healthAttr.removeModifier(modId);
            }
            return;
        }

        // Enforce 4 HP max health (2 hearts) via attribute modifier
        if (healthAttr != null && healthAttr.getModifier(modId) == null) {
            healthAttr.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                    modId, -16.0, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE));
        }
        // Cap health to 4 if it somehow exceeds it
        if (player.getHealth() > 4.0f) player.setHealth(4.0f);

        // Suppress all potion effects every second
        if (player.age % 20 == 0) {
            player.clearStatusEffects();
        }
    }

    public static void tickSlayerSystems(ServerPlayerEntity player) {
        // Already handled by SlayerManager.tick() at server level
    }

    public static void tickZombieBerserkerHelmet(ServerPlayerEntity player) {
        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        UUID uuid = player.getUuid();
        boolean wasActive = berserkerHelmetActive.getOrDefault(uuid, false);

        if (SlayerItems.isZombieBerserkerHelmet(helmet)) {
            boolean canUse = SlayerItems.canUseZombieBerserkerHelmet(player);

            if (canUse) {
                // Apply the berserker effect - half HP
                if (!wasActive) {
                    // Just equipped - apply HP reduction
                    applyBerserkerEffect(player, true);
                    berserkerHelmetActive.put(uuid, true);
                    player.sendMessage(Text.literal("☠ Berserker Mode ACTIVATED! ☠")
                            .formatted(Formatting.DARK_GREEN, Formatting.BOLD), true);
                }
            } else {
                // Player doesn't meet level requirement - show warning [1]
                long now = System.currentTimeMillis();
                long lastWarning = berserkerWarningCooldown.getOrDefault(uuid, 0L);

                if (now - lastWarning > WARNING_COOLDOWN_MS) {
                    player.sendMessage(Text.literal("⛔ Zombie Berserker Helmet requires Level 12!")
                            .formatted(Formatting.RED), true);
                    berserkerWarningCooldown.put(uuid, now);
                }

                // Remove effect if it was somehow active
                if (wasActive) {
                    applyBerserkerEffect(player, false);
                    berserkerHelmetActive.put(uuid, false);
                }
            }
        } else {
            // Helmet removed - restore normal HP
            if (wasActive) {
                applyBerserkerEffect(player, false);
                berserkerHelmetActive.put(uuid, false);
                player.sendMessage(Text.literal("Berserker Mode deactivated")
                        .formatted(Formatting.GRAY), true);
            }
        }
    }

    private static void applyBerserkerEffect(ServerPlayerEntity player, boolean activate) {
        var healthAttribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (healthAttribute == null) return;

        // Remove existing modifier if present
        Identifier modifierId = Identifier.of("political", "berserker_helmet_hp");
        healthAttribute.removeModifier(modifierId);

        if (activate) {
            // Add -50% max HP modifier
            EntityAttributeModifier modifier = new EntityAttributeModifier(
                    modifierId,
                    -0.5, // -50%
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            healthAttribute.addPersistentModifier(modifier);

            // Clamp current health if above new max
            float newMax = player.getMaxHealth();
            if (player.getHealth() > newMax) {
                player.setHealth(newMax);
            }
        } else {
            // Modifier already removed above, health will auto-adjust
        }
    }

    // Call this when player deals damage to apply the 4x multiplier (300% MORE damage dealt = 4x total)
    public static float getBerserkerDamageMultiplier(ServerPlayerEntity player) {
        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);

        if (SlayerItems.isZombieBerserkerHelmet(helmet) &&
                SlayerItems.canUseZombieBerserkerHelmet(player)) {
            return 4.0f; // +300% damage = 4x total
        }

        return 1.0f;
    }


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


    public static ItemStack createHarveysStick() {
        ItemStack stack = new ItemStack(Items.STICK);
        NbtCompound nbt = new NbtCompound();
        nbt.putByte("harveys_stick", (byte) 1);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Harvey's Stick").formatted(Formatting.GOLD, Formatting.BOLD));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal(""),
                Text.literal("◆ LEGENDARY WEAPON ◆").formatted(Formatting.GOLD, Formatting.BOLD),
                Text.literal(""),
                Text.literal("Attack: Lightning Strike").formatted(Formatting.YELLOW),
                Text.literal("  └ Summons lightning on hit").formatted(Formatting.GRAY),
                Text.literal(""),
                Text.literal("Damage: ").formatted(Formatting.GRAY)
                        .append(Text.literal("Lightning Strike").formatted(Formatting.RED, Formatting.BOLD)),
                Text.literal(""),
                Text.literal("§8[MFLUX: Contractor-issued armament | CRD-experimental]").formatted(Formatting.DARK_GRAY),
                Text.literal("「Unique」").formatted(Formatting.GOLD)
        )));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(stack, "harveys_stick");
        return stack;
    }

    public static ItemStack createTheGavel() {
        ItemStack stack = new ItemStack(Items.MACE);
        NbtCompound nbt = new NbtCompound();
        nbt.putByte("the_gavel", (byte) 1);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("The Gavel").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal(""),
                Text.literal("◆ JUDICIAL AUTHORITY ◆").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD),
                Text.literal(""),
                Text.literal("Right-click: Gavel Launch").formatted(Formatting.AQUA),
                Text.literal("  └ Flings you high into the air").formatted(Formatting.GRAY),
                Text.literal("  └ Consumes 1 Wind Charge").formatted(Formatting.GRAY),
                Text.literal("  └ 3s cooldown").formatted(Formatting.GRAY),
                Text.literal(""),
                Text.literal("§8[MFLUX: Judicial enforcement weapon | Contractor-grade]").formatted(Formatting.DARK_GRAY),
                Text.literal("「Unique」").formatted(Formatting.LIGHT_PURPLE)
        )));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        // Custom enchantments are NOT pre-applied — players apply them via the enchanting table.
        // The lore above lists compatible enchantments as a guide only.
        return stack;
    }

    public static ItemStack createHermesShoes() {
        ItemStack stack = new ItemStack(Items.IRON_BOOTS);
        NbtCompound nbt = new NbtCompound();
        nbt.putByte("hermes_shoes", (byte) 1);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Hermes Shoes").formatted(Formatting.AQUA, Formatting.BOLD));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal(""),
                Text.literal("◆ DIVINE FOOTWEAR ◆").formatted(Formatting.AQUA, Formatting.BOLD),
                Text.literal(""),
                Text.literal("Passive: Swift as the Wind").formatted(Formatting.GREEN),
                Text.literal("  └ Permanent Speed III while worn").formatted(Formatting.GRAY),
                Text.literal(""),
                Text.literal("Speed Bonus: ").formatted(Formatting.GRAY)
                        .append(Text.literal("+60%").formatted(Formatting.GREEN, Formatting.BOLD)),
                Text.literal(""),
                Text.literal("§8[MFLUX: Field-tested containment gear | Contractor Program]").formatted(Formatting.DARK_GRAY),
                Text.literal("「Unique」").formatted(Formatting.AQUA)
        )));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(stack, "hermes_shoes");
        return stack;
    }

    /** @deprecated Use {@link #createHPEBM(int)} with mk=1 instead. */
    @Deprecated
    public static ItemStack createHPEBM() {
        return createHPEBM(1);
    }

    public static ItemStack createWardenCore() {
        ItemStack stack = new ItemStack(Items.ECHO_SHARD);
        NbtCompound nbt = new NbtCompound();
        nbt.putByte("warden_core", (byte) 1);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Warden's Core").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal(""),
                Text.literal("◆ ANCIENT ARTIFACT ◆").formatted(Formatting.DARK_AQUA, Formatting.BOLD),
                Text.literal(""),
                Text.literal("The pulsating core of the Deep Dark's guardian").formatted(Formatting.AQUA),
                Text.literal("Pried from the bone cold chest of the Warden.").formatted(Formatting.AQUA),
                Text.literal(""),
                Text.literal("Drop Rate: ").formatted(Formatting.GRAY)
                        .append(Text.literal("0.1%").formatted(Formatting.RED, Formatting.BOLD))
                        .append(Text.literal(" from Wardens").formatted(Formatting.GRAY)),
                Text.literal(""),
                Text.literal("Used to craft Ultra weapons").formatted(Formatting.LIGHT_PURPLE),
                Text.literal(""),
                Text.literal("§8[MFLUX: CRD-006 specimen core | Anomalous biological residue]").formatted(Formatting.DARK_GRAY),
                Text.literal("「Crafting Material」").formatted(Formatting.DARK_AQUA)
        )));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }

    public static ItemStack createUltraOverclockedBeam() {
        ItemStack beam = new ItemStack(Items.GOLDEN_SHOVEL);
        beam.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("HPEBM Mk.VI — Ultra Overclocked")
                        .formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        beam.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("").formatted(Formatting.DARK_PURPLE),
                Text.literal("◆ MYTHIC — Beyond Classification ◆").formatted(Formatting.DARK_PURPLE, Formatting.BOLD),
                Text.literal(""),
                Text.literal("This unit operates beyond all known safety").formatted(Formatting.GRAY),
                Text.literal("parameters. Its containment field has been").formatted(Formatting.GRAY),
                Text.literal("removed entirely. Catastrophic by design.").formatted(Formatting.GRAY),
                Text.literal("Only one is known to exist.").formatted(Formatting.DARK_GRAY),
                Text.literal(""),
                Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_PURPLE),
                Text.literal("⚡ Damage/tick: ").formatted(Formatting.WHITE)
                        .append(Text.literal("35").formatted(Formatting.RED, Formatting.BOLD)),
                Text.literal("💡 XP Cost: ").formatted(Formatting.WHITE)
                        .append(Text.literal("5 levels/sec").formatted(Formatting.YELLOW)),
                Text.literal("📡 Beam Range: ").formatted(Formatting.WHITE)
                        .append(Text.literal("30 blocks").formatted(Formatting.AQUA)),
                Text.literal("💥 Endpoint Explosion: ").formatted(Formatting.WHITE)
                        .append(Text.literal("10 dmg, 3.0 radius").formatted(Formatting.GOLD)),
                Text.literal(""),
                Text.literal("Right-click: Devastating beam (hold)").formatted(Formatting.RED),
                Text.literal("Left-click: Sonic Devastation").formatted(Formatting.DARK_PURPLE),
                Text.literal("  └ AOE devastation, 10s cooldown").formatted(Formatting.GRAY),
                Text.literal(""),
                Text.literal("「Tier VI — Mythic」").formatted(Formatting.DARK_PURPLE),
                Text.literal("§8[MFLUX: Mk.VI beyond-class weapon | Containment VOID]").formatted(Formatting.DARK_GRAY)
        )));
        beam.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        beam.set(DataComponentTypes.MAX_DAMAGE, Integer.MAX_VALUE);
        beam.set(DataComponentTypes.DAMAGE, 0);
        return beam;
    }

    public static ItemStack createUltraBeam() {
        ItemStack stack = new ItemStack(Items.IRON_SHOVEL);
        NbtCompound nbt = new NbtCompound();
        nbt.putByte("ultra_beam", (byte) 1);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Ultra Beam Emitter").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal(""),
                Text.literal("◆ ENHANCED WEAPON ◆").formatted(Formatting.DARK_PURPLE, Formatting.BOLD),
                Text.literal(""),
                Text.literal("Ultra High Powered Energy Beam").formatted(Formatting.LIGHT_PURPLE),
                Text.literal(""),
                Text.literal("Right-click: Continuous beam attack").formatted(Formatting.RED),
                Text.literal("  └ Costs 1 XP level per second").formatted(Formatting.GRAY),
                Text.literal(""),
                Text.literal("Damage: ").formatted(Formatting.GRAY)
                        .append(Text.literal("3.0").formatted(Formatting.RED, Formatting.BOLD))
                        .append(Text.literal(" per tick (+200%)").formatted(Formatting.GREEN)),
                Text.literal(""),
                Text.literal("「Tier II」").formatted(Formatting.DARK_PURPLE)
        )));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        stack.set(DataComponentTypes.MAX_DAMAGE, Integer.MAX_VALUE);
        stack.set(DataComponentTypes.DAMAGE, 0);
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

        String titleSuffix = switch (mk) {
            case 1 -> "OVERCLOCKED";
            case 2 -> "SUPERCHARGED";
            case 3 -> "HYPERPOWERED";
            case 4 -> "DEVASTATOR";
            case 5 -> "MAXIMUM POWER";
            default -> "OVERCLOCKED";
        };

        String tierRoman = switch (mk) {
            case 1 -> "III";
            case 2 -> "IV";
            case 3 -> "V";
            case 4 -> "VI";
            case 5 -> "VII";
            default -> "III";
        };

        float baseDamage = switch (mk) {
            case 1 -> 3.6f;
            case 2 -> 4.2f;
            case 3 -> 4.8f;
            case 4 -> 5.4f;
            case 5 -> 6.0f;
            default -> 3.6f;
        };

        int damageBonus = 50 + (mk * 20);
        int xpCost = mk + 1;
        String suffix = mk == 5 ? " ✦ MAX ✦" : "";

        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Ultra Beam Emitter Mk" + mk + suffix).formatted(color, Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ " + titleSuffix + " ◆").formatted(color, Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Overclocked Energy Beam Module").formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Right-click: Continuous beam attack").formatted(Formatting.RED));
        lore.add(Text.literal("  └ Costs " + xpCost + " XP level(s) per second").formatted(Formatting.GRAY));

        if (mk >= 3) {
            float explosionDmg = switch (mk) {
                case 3 -> 4.0f;
                case 4 -> 6.0f;
                case 5 -> 10.0f;
                default -> 4.0f;
            };
            lore.add(Text.literal("  └ Explosive impact").formatted(Formatting.GOLD));
            lore.add(Text.literal(""));
            lore.add(Text.literal("Damage: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.1f", baseDamage)).formatted(Formatting.RED, Formatting.BOLD))
                    .append(Text.literal(" per tick (+" + damageBonus + "%)").formatted(Formatting.GREEN)));
            lore.add(Text.literal("Explosion: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.1f", explosionDmg)).formatted(Formatting.GOLD, Formatting.BOLD)));
        } else {
            lore.add(Text.literal(""));
            lore.add(Text.literal("Damage: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.1f", baseDamage)).formatted(Formatting.RED, Formatting.BOLD))
                    .append(Text.literal(" per tick (+" + damageBonus + "%)").formatted(Formatting.GREEN)));
        }

        lore.add(Text.literal(""));

        if (mk == 5) {
            lore.add(Text.literal("★ MAXIMUM POWER ACHIEVED ★").formatted(Formatting.GOLD, Formatting.BOLD));
            lore.add(Text.literal(""));
        }

        lore.add(Text.literal("「Tier " + tierRoman + "」").formatted(color));

        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        stack.set(DataComponentTypes.MAX_DAMAGE, Integer.MAX_VALUE);
        stack.set(DataComponentTypes.DAMAGE, 0);

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

        // Check for dragon's breath (only requirement now)
        ItemStack dragonBreath = findItemInInventory(player, Items.DRAGON_BREATH);
        if (dragonBreath == null || dragonBreath.isEmpty()) {
            player.sendMessage(Text.literal("Requires Dragon's Breath!")
                    .formatted(Formatting.RED), true);
            return false;
        }

        // Consume dragon's breath
        dragonBreath.decrement(1);

        // Set 15 second cooldown (300 ticks)
        player.getItemCooldownManager().set(stack, 200);

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
                world.spawnParticles(ParticleTypes.WAX_OFF, x, y, z, 1, 0.05, 0.05, 0.05, 0.01);
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

        // Prefer custom_item_id for reliable tier detection
        String customId = SlayerItems.getCustomItemId(stack);
        if (customId != null) {
            if (customId.equals("hpebm_ultra")) return 7;
            if (customId.equals("hpebm_mk5")) return 6;
            if (customId.equals("hpebm_mk4")) return 5;
            if (customId.equals("hpebm_mk3")) return 4;
            if (customId.equals("hpebm_mk2")) return 3;
            if (customId.equals("hpebm_mk1")) return 2;
        }

        // Fallback: check by custom name
        if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
            Text nameText = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (nameText != null) {
                String name = nameText.getString();
                if (name.contains("Ultra Overclocked") || name.contains("Mk.VI")) return 7;
                // New Roman numeral names
                if (name.contains("Mk.V")) return 6;
                if (name.contains("Mk.IV")) return 5;
                if (name.contains("Mk.III")) return 4;
                if (name.contains("Mk.II")) return 3;
                if (name.contains("Mk.I")) return 2;
                // Legacy numeric names
                if (name.contains("Mk5")) return 6;
                if (name.contains("Mk4")) return 5;
                if (name.contains("Mk3")) return 4;
                if (name.contains("Mk2")) return 3;
                if (name.contains("Mk1")) return 2;
                if (name.contains("Ultra")) return 1;
                if (name.contains("H.P.E.B.M.") || name.contains("HPEBM")) return 0; // Base tier
            }
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

    // ============================================================
// SLIME BOOTS TICK - Jump Boost, Fall Damage Negation
// ============================================================
    public static void tickSlimeBoots(ServerPlayerEntity player) {
        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
        if (!SlayerItems.isSlimeBoots(boots)) {
            // Remove shrink effect if no longer wearing boots
            if (slimeBootsShrunk.contains(player.getUuid())) {
                player.calculateDimensions();
                slimeBootsShrunk.remove(player.getUuid());
            }
            return;
        }

        // Check level requirement
        if (!SlayerItems.canUseSlimeBoots(player)) {
            sendLevelWarning(player, "Gelatinous Rustler Boots",
                    SlayerItems.SLIME_BOOTS_LEVEL_REQ, "Slime");
            return;
        }

        // Jump Boost III (buffed from II → III for Item #6)
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.JUMP_BOOST, 40, 2, true, false, false));

        // Check if shrunk state should end
        UUID uuid = player.getUuid();
        if (slimeBootsActiveUntil.containsKey(uuid)) {
            if (System.currentTimeMillis() > slimeBootsActiveUntil.get(uuid)) {
                // Time's up - return to normal size
                player.calculateDimensions();
                slimeBootsShrunk.remove(uuid);
                slimeBootsActiveUntil.remove(uuid);
                player.sendMessage(Text.literal("🟢 Slime form ended - returned to normal size!")
                        .formatted(Formatting.GREEN), true);
            }
        }
    }

    // Called when player takes fatal damage - returns true if death should be cancelled
    public static boolean trySlimeBootsDeathSave(ServerPlayerEntity player) {
        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
        if (!SlayerItems.isSlimeBoots(boots)) return false;
        if (!SlayerItems.canUseSlimeBoots(player)) return false;

        UUID uuid = player.getUuid();
        long now = System.currentTimeMillis();

        // Check cooldown
        if (slimeBootsCooldowns.containsKey(uuid)) {
            long cooldownEnd = slimeBootsCooldowns.get(uuid) + SLIME_BOOTS_COOLDOWN_MS;
            if (now < cooldownEnd) {
                return false; // On cooldown, death proceeds normally
            }
        }

        // Already in shrunk state - die for real
        if (slimeBootsShrunk.contains(uuid)) {
            return false;
        }

        // Activate death save!
        slimeBootsCooldowns.put(uuid, now);
        slimeBootsActiveUntil.put(uuid, now + SLIME_BOOTS_ACTIVE_MS);
        slimeBootsShrunk.add(uuid);

        // Set health to half a heart
        player.setHealth(1.0f);

        // Shrink player (half size)
        // Note: In 1.21+ use the scale attribute
        ServerWorld world = player.getEntityWorld();

        // Spawn 2 small slimes
        for (int i = 0; i < 2; i++) {
            net.minecraft.entity.mob.SlimeEntity slime = net.minecraft.entity.EntityType.SLIME.create(world, SpawnReason.TRIGGERED);
            if (slime != null) {
                slime.setSize(1, true); // Small slime
                slime.setPosition(player.getX() + (i - 0.5), player.getY(), player.getZ());
                world.spawnEntity(slime);
            }
        }

        // Effects
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_SLIME_SQUISH,
                SoundCategory.PLAYERS, 1.0f, 1.5f);
        world.spawnParticles(ParticleTypes.ITEM_SLIME,
                player.getX(), player.getY() + 1, player.getZ(), 20, 0.5, 0.5, 0.5, 0.1);

        player.sendMessage(Text.literal("☠ DEATH SAVED! You have 30 seconds at half size!")
                .formatted(Formatting.GREEN, Formatting.BOLD), false);
        player.sendMessage(Text.literal("⚠ If you die now, it's permanent!")
                .formatted(Formatting.RED), false);

        return true; // Cancel death
    }

    // ============================================================
// WARDEN CHESTPLATE TICK - Extra Health, Echo Location
// ============================================================
    public static void tickSpiderLeggings(ServerPlayerEntity player) {
        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        if (!SlayerItems.isSpiderLeggings(leggings)) return;

        if (!SlayerItems.canUseSpiderLeggings(player)) {
            return;
        }

        // Delegate to ArmorAbilityHandler which has the full venomous leggings implementation
        ArmorAbilityHandler.tickVenomousLeggings(player);

        // Wall climbing: when sneaking and touching a wall, apply levitation to simulate climbing
        if (player.isSneaking() && !player.isOnGround() && player.horizontalCollision) {
            net.minecraft.util.math.Vec3d vel = player.getVelocity();
            if (vel.y < 0) {
                player.setVelocity(vel.x, 0.1, vel.z);
                player.velocityDirty = true;
            }
        }
    }

    public static void tickWardenChestplate(ServerPlayerEntity player) {
        ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
        if (!SlayerItems.isWardenChestplate(chestplate)) return;

        if (!SlayerItems.canUseWardenChestplate(player)) {
            return;
        }

        // Extra health (+20 hearts = Health Boost X)
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.HEALTH_BOOST, 40, 9, true, false, false));

        // Resistance I for tankiness
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 40, 0, true, false, false));

        // ESP: Glow effect on nearby moving players (every 2 seconds) — 24 block range
        if (player.age % 40 == 0) {
            ServerWorld world = player.getEntityWorld();
            double radius = 24.0;

            for (ServerPlayerEntity nearby : world.getPlayers()) {
                if (nearby == player) continue;
                if (nearby.squaredDistanceTo(player) > radius * radius) continue;

                // Check if player is moving or making sound
                Vec3d velocity = nearby.getVelocity();
                boolean isMoving = velocity.horizontalLengthSquared() > 0.003;
                boolean isSprinting = nearby.isSprinting();
                boolean isSneaking = nearby.isSneaking();

                // Sneaking players are harder to detect
                if (isSneaking) continue;

                if (isMoving || isSprinting) {
                    // Apply brief glowing effect (1.5 seconds)
                    nearby.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.GLOWING, 30, 0, true, false, false));
                }
            }
        }
    }

    // ============================================================
// SKELETON BOW - Instant-fire on left-click (swing detection)
// ============================================================
    public static boolean isSkeletonBowEquipped(ServerPlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();
        return SlayerItems.isSkeletonBow(mainHand) || SlayerItems.isSkeletonBow(offHand);
    }

    public static void tickSkeletonBow(ServerPlayerEntity player) {
        // Detect left-clicks in the air (no block/entity target) via hand swing
        SkeletonBowHandler.tickSwingDetection(player);
    }

    // ============================================================
// LEVEL WARNING HELPER (prevents spam)
// ============================================================
    public static void sendLevelWarning(ServerPlayerEntity player, String itemName, int reqLevel, String slayerType) {
        UUID uuid = player.getUuid();
        long now = System.currentTimeMillis();

        // Only warn once per 5 seconds
        if (levelWarningCooldowns.containsKey(uuid)) {
            if (now - levelWarningCooldowns.get(uuid) < 5000) {
                return;
            }
        }

        levelWarningCooldowns.put(uuid, now);
        player.sendMessage(Text.literal("⚠ " + itemName + " requires " + slayerType + " Bounty Level " + reqLevel + "!")
                .formatted(Formatting.RED), true);
    }

    // ============================================================
// PREVENT BOUNTY ITEMS FROM BEING USED/EATEN
// ============================================================
    public static boolean shouldBlockItemUse(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;

        String nameStr = name.getString();
        return nameStr.contains("Core") ||
                nameStr.contains("Bounty Sword") ||
                nameStr.contains("Slayer Sword");
    }
}