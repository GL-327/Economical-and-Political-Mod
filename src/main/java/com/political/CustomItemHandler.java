package com.political;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.block.AbstractCauldronBlock;
import com.political.SlayerItems;
import com.political.SlayerManager;
import com.political.CoinManager;
import java.util.*;

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

    public static boolean isInEnderPhaseMode(UUID uuid) {
        return false;
    }

    public static void register() {
        registerBountyItemRestrictions();

        // Prevent custom leather armor from being washed/undyed in a cauldron
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (world.getBlockState(hitResult.getBlockPos()).getBlock() instanceof AbstractCauldronBlock) {
                ItemStack held = player.getStackInHand(hand);
                if (isProtectedItem(held)) {
                    return ActionResult.FAIL;
                }
            }
            // Fletching Table custom GUI
            if (world.getBlockState(hitResult.getBlockPos()).getBlock() == net.minecraft.block.Blocks.FLETCHING_TABLE) {
                if (player instanceof ServerPlayerEntity sp) {
                    FletchingTableGui.openGui(sp);
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });

        // Daniel's Pickaxe - 3x3 tunnel mining and vein mining
        // Block Tools - special abilities
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient()) return;
            if (!(player instanceof ServerPlayerEntity sp)) return;
            
            ItemStack tool = player.getMainHandStack();
            if (DanielsPickaxe.isDanielsPickaxe(tool)) {
                DanielsPickaxe.onBlockBreak(sp, (ServerWorld) world, pos, state);
            }
            // Block tools abilities
            BlockToolHandler.onBlockBreak(sp, world, pos, state, tool);
        });

        // Harvey's Stick - Lightning on attack / Ender Phase - block attacks on players

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;

            // Ender Phase: cannot attack other players while in phase
            // if (isInEnderPhaseMode(sp.getUuid()) && entity instanceof PlayerEntity) {
            //     return ActionResult.FAIL;
            // }

            ItemStack weapon = player.getStackInHand(hand);
            if (isHarveysStick(weapon) && entity instanceof LivingEntity target) {
                if (target != player) {
                    LightningEntity lightning = EntityType.LIGHTNING_BOLT.create((ServerWorld) world, SpawnReason.TRIGGERED);
                    if (lightning != null) {
                        lightning.setPosition(target.getX(), target.getY(), target.getZ());
                        world.spawnEntity(lightning);
                    }
                }
            }
            
            // Ender Sword: Teleport behind target on hit
            if (SlayerItems.isType(weapon, SlayerItems.ENDER_SWORD) && entity instanceof LivingEntity target) {
                Vec3d targetDir = target.getRotationVec(1.0f).normalize().multiply(-1.5);
                Vec3d behind = new Vec3d(target.getX(), target.getY(), target.getZ()).add(targetDir);
                sp.teleport((ServerWorld) world, behind.x, behind.y, behind.z, Set.of(), target.getYaw(), target.getPitch(), true);
                world.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }

            // Abyssal Blade: Bonus damage to blinded targets
            if (SlayerItems.isType(weapon, SlayerItems.ABYSSAL_BLADE) && entity instanceof LivingEntity target) {
                if (target.hasStatusEffect(StatusEffects.BLINDNESS) || target.hasStatusEffect(StatusEffects.DARKNESS)) {
                    target.damage((ServerWorld) world, sp.getDamageSources().playerAttack(sp), 5.0f); // Bonus damage
                    if (world instanceof ServerWorld sw) {
                        sw.spawnParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.5, 0.5, 0.5, 0.05);
                    }
                }
            }
            
            // Block Tools: onEntityHit abilities
            if (entity instanceof LivingEntity) {
                BlockToolHandler.onEntityHit(sp, entity, weapon);
            }
            
            // Block Weapons: process damage abilities
            if (entity instanceof LivingEntity target) {
                BlockWeaponHandler.processDamage(sp, target, 0); // Damage applied separately
            }
            
            // Storm Dragon Chestplate: Lightning strike on hit
            if (entity instanceof LivingEntity target) {
                ArmorAbilityHandler.onStormDragonHit(sp, target);
            }
            
            return ActionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            ItemStack held = player.getStackInHand(hand);

            // Beam Weapons
            if (isAnyBeamWeapon(held)) {
                hpebmLastRightClick.put(player.getUuid(), System.currentTimeMillis());
                return ActionResult.CONSUME;
            }

            // The Gavel
            if (isTheGavel(held)) {
                if (useGavelAbility(serverPlayer, held)) {
                    return ActionResult.SUCCESS;
                }
                return ActionResult.FAIL;
            }

            // Echoing Core: Sonic Boom
            if (SlayerItems.isType(held, SlayerItems.ECHOING_CORE)) {
                if (!serverPlayer.getItemCooldownManager().isCoolingDown(held)) {
                    serverPlayer.getItemCooldownManager().set(held, 100); // 5s cooldown
                    fireSonicBoom(serverPlayer, world);
                    return ActionResult.SUCCESS;
                }
            }

            // Daniel's Pickaxe: Toggle ability
            if (DanielsPickaxe.isDanielsPickaxe(held)) {
                DanielsPickaxe.toggleAbility(serverPlayer);
                return ActionResult.SUCCESS;
            }

            // Bounty Item Restrictions (Cores, etc.)
            if (isBountyItemRestricted(held)) {
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

    private static void fireSonicBoom(ServerPlayerEntity player, World world) {
        ServerWorld serverWorld = (ServerWorld) world;
        Vec3d start = player.getEyePos();
        Vec3d direction = player.getRotationVec(1.0f);
        
        for (int i = 1; i <= 15; i++) {
            Vec3d pos = start.add(direction.multiply(i));
            serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            
            Box box = new Box(pos.subtract(1.5, 1.5, 1.5), pos.add(1.5, 1.5, 1.5));
            for (Entity entity : serverWorld.getOtherEntities(player, box, e -> e instanceof LivingEntity)) {
                if (entity instanceof LivingEntity living) {
                    living.damage(serverWorld, player.getDamageSources().sonicBoom(player), 10.0f);
                    Vec3d push = direction.multiply(1.0).add(0, 0.2, 0);
                    living.setVelocity(push);
                }
            }
        }
        serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    /** Called on player disconnect to clean up per-player state and prevent memory leaks. */
    public static void onPlayerDisconnect(UUID uuid) {
        gavelCooldowns.remove(uuid);
        ultraOverclockedCooldowns.remove(uuid);
        berserkerHelmetActive.remove(uuid);
        berserkerWarningCooldown.remove(uuid);
        hpebmUseTicks.remove(uuid);
        hpebmLastRightClick.remove(uuid);
        wasSwinging.remove(uuid);
        DanielsPickaxe.onPlayerDisconnect(uuid);
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

    public static boolean isSlayerCore(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains("Core");
    }

    /**
     * Returns true for any custom item that should be protected from vanilla consumption
     * (placement, crafting, smelting).
     */
    public static boolean isProtectedItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        // Check NBT-based bounty identification first (works even without custom name)
        if (SlayerItems.isAnyBountyItem(stack)) return true;
        // Check custom item ID - if it has ANY custom ID, protect it from vanilla recipes
        String customId = SlayerItems.getCustomItemId(stack);
        if (customId != null) {
            return true;
        }
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
                name.contains("Plasma Emitter") ||
                name.contains("Enchanted Gilded Blackstone") ||
                name.contains("Enchanted Compacted") ||
                name.contains("✦ Enchanted") ||
                name.contains("✧ Enchanted") ||
                name.contains("Compacted") ||
                name.contains("Super Compacted") ||
                name.contains("Gilded Netherite") ||
                name.contains("Dragon Chestplate");
    }

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
        // Match actual fireBeam() damage values (mk1→tier2, mk2→tier3, etc.)
        float actualDamage = switch (mk) {
            case 1 -> 2.0f;
            case 2 -> 3.5f;
            case 3 -> 5.0f;
            case 4 -> 6.5f;
            case 5 -> 8.0f;
            default -> 1.0f;
        };
        lore.add(Text.literal("⚡ Damage/tick: ").formatted(Formatting.WHITE)
                .append(Text.literal(String.format("%.1f", actualDamage)).formatted(Formatting.RED, Formatting.BOLD)));
        int xpCost = Math.max(1, mk - 2);
        lore.add(Text.literal("💡 XP Cost: ").formatted(Formatting.WHITE)
                .append(Text.literal(xpCost + " level(s)/sec").formatted(Formatting.YELLOW)));
        lore.add(Text.literal("📡 Beam Range: ").formatted(Formatting.WHITE)
                .append(Text.literal((30 + mk * 2) + " blocks").formatted(Formatting.AQUA)));

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

        // Spawn a real wind charge entity at player position
        WindChargeEntity windCharge = new WindChargeEntity(EntityType.WIND_CHARGE, world);
        windCharge.setOwner(player);
        windCharge.setPosition(player.getX(), player.getY() + 1.0, player.getZ());
        
        // Aim the wind charge downward and slightly backward to create upward and forward launch
        Vec3d lookDirection = player.getRotationVec(1.0f);
        windCharge.setVelocity(-lookDirection.x * 0.3, -1.0, -lookDirection.z * 0.3);
        
        world.spawnEntity(windCharge);

        // Additional visual feedback
        ((ServerWorld) world).spawnParticles(ParticleTypes.CLOUD,
                player.getX(), player.getY() + 0.5, player.getZ(),
                10, 0.3, 0.1, 0.3, 0.05);

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

    public static void tickUndeadHeart(ServerPlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();
        if (SlayerItems.isType(main, SlayerItems.UNDEAD_HEART) || SlayerItems.isType(off, SlayerItems.UNDEAD_HEART)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 40, 4, true, false, false));
        }
        
        // Bouncy Slime: Jump Boost and Fall Resistance
        if (SlayerItems.isType(main, SlayerItems.BOUNCY_SLIME) || SlayerItems.isType(off, SlayerItems.BOUNCY_SLIME)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 40, 1, true, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 0, true, false, false));
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
            case 1 -> 2.0f;
            case 2 -> 3.5f;
            case 3 -> 5.0f;
            case 4 -> 6.5f;
            case 5 -> 8.0f;
            case 6 -> 9.5f;
            case 7 -> 11.0f;
            default -> 2.0f;
        };

        Vec3d eyePos = player.getEyePos();
        Vec3d lookDir = player.getRotationVec(1.0f);
        Vec3d sideOffset = lookDir.crossProduct(new Vec3d(0, 1, 0)).normalize().multiply(0.7);  // Was 0.4, now further right
        Vec3d start = eyePos.add(sideOffset).add(0, -0.7, 0);  // Was -0.4, now further down
        Vec3d direction = lookDir;

        Vec3d endPoint = findBeamEndpoint(world, player, start, direction, 30.0 + tier * 2.0);
        double beamLength = start.distanceTo(endPoint);

        Box searchBox = new Box(
                start.x - (30 + tier * 2.0), start.y - (30 + tier * 2.0), start.z - (30 + tier * 2.0),
                start.x + (30 + tier * 2.0), start.y + (30 + tier * 2.0), start.z + (30 + tier * 2.0));

        for (Entity entity : world.getOtherEntities(player, searchBox, e -> e instanceof LivingEntity && e != player)) {
            if (!(entity instanceof LivingEntity living)) continue;

            Vec3d entityPos = new Vec3d(living.getX(), living.getY() + living.getHeight() / 2, living.getZ());
            Vec3d toEntity = entityPos.subtract(start);
            double distance = toEntity.length();
            if (distance > 30) continue;

            Vec3d projected = direction.multiply(toEntity.dotProduct(direction));
            double perpendicularDist = toEntity.subtract(projected).length();

            if (perpendicularDist < (1.5 + tier * 0.1) && toEntity.dotProduct(direction) > 0) {
                living.timeUntilRegen = 0;
                living.damage(world, player.getDamageSources().magic(), baseDamage);

                ((ServerWorld) world).spawnParticles(ParticleTypes.CRIT,
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

            ((ServerWorld) world).spawnParticles(ParticleTypes.CRIT, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.2);

            Vec3d spiral1 = basePos.add(perp1.multiply(Math.cos(angle) * spiralRadius)).add(perp2.multiply(Math.sin(angle) * spiralRadius));
            ((ServerWorld) world).spawnParticles(ParticleTypes.ELECTRIC_SPARK, spiral1.x, spiral1.y, spiral1.z, 1, 0, 0, 0, 0.25);

            Vec3d spiral2 = basePos.add(perp1.multiply(Math.cos(angle + Math.PI) * spiralRadius)).add(perp2.multiply(Math.sin(angle + Math.PI) * spiralRadius));
            ((ServerWorld) world).spawnParticles(ParticleTypes.ELECTRIC_SPARK, spiral2.x, spiral2.y, spiral2.z, 1, 0, 0, 0, 0.25);
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
                        ((ServerWorld) world).spawnParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.2);
                case 3 -> {
                    world.spawnParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.2);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.CRIT, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.15);
                }
                case 4 -> {
                    ((ServerWorld) world).spawnParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 2, 0, 0, 0, 0.15);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.END_ROD, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.01);
                }
                case 5 -> {
                    ((ServerWorld) world).spawnParticles(ParticleTypes.WITCH, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.01);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 2, 0, 0, 0, 0.2);
                }
                case 6 -> {
                    ((ServerWorld) world).spawnParticles(ParticleTypes.WITCH, basePos.x, basePos.y, basePos.z, 2, 0, 0, 0, 0.01);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.DAMAGE_INDICATOR, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.05);
                }
                case 7 -> {
                    // Reduced speed: 0.15 -> 0.05 (less movement = tighter beam)
                    ((ServerWorld) world).spawnParticles(ParticleTypes.BUBBLE, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.015);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.REVERSE_PORTAL, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.5);
                    // Reduced delta (0.02 -> 0.0) and speed (0.2 -> 0.05)
                    ((ServerWorld) world).spawnParticles(ParticleTypes.CRIT, basePos.x, basePos.y, basePos.z, 1, 0.0, 0.0, 0.0, 0.01);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 1, 0.0, 0.0, 0.0, 0.01);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.WHITE_ASH, basePos.x, basePos.y, basePos.z, 1, 0.0, 0.0, 0.0, 0.01);
                }
            }

            // Spiral helixes
            for (int h = 0; h < helixCount; h++) {
                double helixAngle = angle + (h * Math.PI * 2 / helixCount);
                Vec3d spiral = basePos
                        .add(perp1.multiply(Math.cos(helixAngle) * spiralRadius))
                        .add(perp2.multiply(Math.sin(helixAngle) * spiralRadius));
                ((ServerWorld) world).spawnParticles(ParticleTypes.ELECTRIC_SPARK, spiral.x, spiral.y, spiral.z, 1, 0, 0, 0, 0.2);
            }

            if (tier >= 5) {
                double outerRadius = spiralRadius * 1.5;
                double outerAngle = -angle * 0.5;
                Vec3d outer = basePos
                        .add(perp1.multiply(Math.cos(outerAngle) * outerRadius))
                        .add(perp2.multiply(Math.sin(outerAngle) * outerRadius));
                ((ServerWorld) world).spawnParticles(ParticleTypes.CRIT, outer.x, outer.y, outer.z, 1, 0, 0, 0, 0.015);
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

            ((ServerWorld) world).spawnParticles(ParticleTypes.EXPLOSION, endPoint.x, endPoint.y, endPoint.z, 1, 0, 0, 0, 0);

            if (tier >= 7) {
                ((ServerWorld) world).spawnParticles(ParticleTypes.GLOW, endPoint.x, endPoint.y, endPoint.z, 10, 0.5, 0.5, 0.5, 0.3);
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

    /** Checks if player is wearing Crown of Greed or Crown of Midas */
    private static boolean isWearingCrownOfGreedOrMidas(ServerPlayerEntity player) {
        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        return SlayerItems.isCrownOfGreed(helmet) || SlayerItems.isCrownOfMidas(helmet);
    }

    public static void registerGoldOnlyRestrictions() {
        // Block non-gold item usage (right-click, interact)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (isWearingCrownOfGreedOrMidas((ServerPlayerEntity) player)) {
                ItemStack stack = player.getStackInHand(hand);
                if (!isGoldItem(stack)) {
                    player.sendMessage(Text.literal(" Only gold items can be used while wearing the crown!").formatted(Formatting.YELLOW), true);
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });

        // Block non-gold item usage on entities
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (isWearingCrownOfGreedOrMidas((ServerPlayerEntity) player)) {
                ItemStack stack = player.getStackInHand(hand);
                if (!isGoldItem(stack)) {
                    player.sendMessage(Text.literal(" Only gold items can be used while wearing the crown!").formatted(Formatting.YELLOW), true);
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });

        // Block non-gold item usage (general right-click)
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (isWearingCrownOfGreedOrMidas((ServerPlayerEntity) player)) {
                ItemStack stack = player.getStackInHand(hand);
                if (!isGoldItem(stack)) {
                    player.sendMessage(Text.literal(" Only gold items can be used while wearing the crown!").formatted(Formatting.YELLOW), true);
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
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

        // Crown of Midas: multiply damage by 2x number of digits in coin count
        if (SlayerItems.isCrownOfMidas(attacker.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD))) {
            int coins = CoinManager.getCoins(attacker);
            int digits = coins > 0 ? String.valueOf(coins).length() : 1;
            modifiedDamage *= (digits * 2);
        }

        // Midas's Sword: damage scales with kill count digits
        // Also has Crown synergy: wearing Crown of Greed/Midas doubles the damage bonus
        if (SlayerItems.isMidasSword(weapon)) {
            int kills = SlayerItems.getMidasSwordKills(weapon);
            int killDigits = kills > 0 ? String.valueOf(kills).length() : 1;
            // Base: damage multiplier = kill digits × 2
            modifiedDamage *= (killDigits * 2);
            
            // Crown synergy: if wearing Crown of Greed or Midas, double the multiplier
            if (SlayerItems.isCrownOfGreed(attacker.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD)) ||
                SlayerItems.isCrownOfMidas(attacker.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD))) {
                modifiedDamage *= 2;
            }
        }
        return modifiedDamage;
    }

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

        // Check for non-gold items in inventory and remove them
        if (player.age % 20 == 0) { // Check every second
            removeNonGoldItems(player);
        }

        // Enforce 4 HP max health (2 hearts) via attribute modifier
        if (healthAttr != null && healthAttr.getModifier(modId) == null) {
            healthAttr.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                    modId, -16.0, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE));
        }
        // Cap health to 4 if it somehow exceeds it
        if (player.getHealth() > 4.0f) player.setHealth(4.0f);
    }

    /** Tick handler for Crown of Midas: enforces 10 HP max health and gold-only restriction. */
    public static void tickCrownOfMidas(ServerPlayerEntity player) {
        ItemStack helmet = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD);
        net.minecraft.util.Identifier modId = net.minecraft.util.Identifier.of("political", "crown_of_midas_hp");
        var healthAttr = player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.MAX_HEALTH);

        if (!SlayerItems.isCrownOfMidas(helmet)) {
            // Remove the modifier if crown is no longer worn
            if (healthAttr != null && healthAttr.getModifier(modId) != null) {
                healthAttr.removeModifier(modId);
            }
            return;
        }

        // Check for non-gold items in inventory and remove them
        if (player.age % 20 == 0) { // Check every second
            removeNonGoldItems(player);
        }

        // Enforce 10 HP max health (5 hearts) via attribute modifier
        if (healthAttr != null && healthAttr.getModifier(modId) == null) {
            healthAttr.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                    modId, -10.0, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE));
        }
        // Cap health to 10 if it somehow exceeds it
        if (player.getHealth() > 10.0f) player.setHealth(10.0f);
    }

    /** Removes non-gold items from player's inventory (for Crown of Greed/Midas restriction). */
    private static void removeNonGoldItems(ServerPlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && !isGoldItem(stack)) {
                // Drop the item on the ground
                player.dropItem(stack, true, false);
                player.getInventory().removeStack(i);
                player.sendMessage(Text.literal(" Only gold items allowed while wearing the crown!").formatted(Formatting.YELLOW), true);
                break; // Only remove one item per check to avoid spam
            }
        }
    }

    /** Checks if an item is made of gold or is a gold-related item. */
    private static boolean isGoldItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        // Check for gold items by material type
        Item item = stack.getItem();
        boolean isVanillaGold = item == Items.GOLD_INGOT ||
               item == Items.GOLD_NUGGET ||
               item == Items.GOLD_BLOCK ||
               item == Items.GOLDEN_SWORD ||
               item == Items.GOLDEN_PICKAXE ||
               item == Items.GOLDEN_AXE ||
               item == Items.GOLDEN_SHOVEL ||
               item == Items.GOLDEN_HOE ||
               item == Items.GOLDEN_HELMET ||
               item == Items.GOLDEN_CHESTPLATE ||
               item == Items.GOLDEN_LEGGINGS ||
               item == Items.GOLDEN_BOOTS ||
               item == Items.GOLDEN_APPLE ||
               item == Items.ENCHANTED_GOLDEN_APPLE ||
               item == Items.RAW_GOLD ||
               item == Items.GILDED_BLACKSTONE ||
               item == Items.LIGHT_WEIGHTED_PRESSURE_PLATE ||
               item == Items.HEAVY_WEIGHTED_PRESSURE_PLATE;
        
        if (isVanillaGold) return true;
        
        // Check for custom gold items by name
        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName != null) {
            String name = customName.getString().toLowerCase();
            if (name.contains("gold")) return true;
        }
        
        // Check for T1/T2 piglin swords and armor (for Midas crown)
        String customId = SlayerItems.getCustomItemId(stack);
        if (customId != null) {
            // Check for T1/T2 piglin swords
            if (customId.equals("piglin_t1_sword") || customId.equals("piglin_t2_sword")) {
                return true;
            }
            // Check for T1/T2 piglin armor
            if (customId.startsWith("piglin_t1_") || customId.startsWith("piglin_t2_")) {
                return true;
            }
        }
        
        return false;
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
                        .append(Text.literal("6.0").formatted(Formatting.RED, Formatting.BOLD)),
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

            ((ServerWorld) world).spawnParticles(ParticleTypes.SONIC_BOOM, x, y, z, 1, 0, 0, 0, 0);

            if (i % 2 == 0) {
                ((ServerWorld) world).spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 2, 0.05, 0.05, 0.05, 0.01);
                ((ServerWorld) world).spawnParticles(ParticleTypes.GLOW, x, y, z, 1, 0.05, 0.05, 0.05, 0.01);
                ((ServerWorld) world).spawnParticles(ParticleTypes.WAX_OFF, x, y, z, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }

        // Play sonic boom sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 2.0f, 0.8f);

        // Explosion at impact
        world.createExplosion(player, impactPos.x, impactPos.y, impactPos.z,
                8.0f, true, net.minecraft.world.World.ExplosionSourceType.MOB);

        // Impact particles
        ((ServerWorld) world).spawnParticles(ParticleTypes.EXPLOSION_EMITTER, impactPos.x, impactPos.y, impactPos.z, 5, 1, 1, 1, 0);
        ((ServerWorld) world).spawnParticles(ParticleTypes.SOUL, impactPos.x, impactPos.y, impactPos.z, 30, 2, 2, 2, 0.1);
        ((ServerWorld) world).spawnParticles(ParticleTypes.SCULK_SOUL, impactPos.x, impactPos.y, impactPos.z, 20, 2, 2, 2, 0.05);

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
        ((ServerWorld) world).spawnParticles(ParticleTypes.ITEM_SLIME,
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

        // Night Vision (permanent while worn)
        StatusEffectInstance currentNightVision = player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (currentNightVision == null || currentNightVision.getDuration() < 220) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION, 400, 0, true, false, false));
        }

        // Darkness immunity (suppressed during Ender Phase Mode which intentionally applies Darkness)
        if (player.hasStatusEffect(StatusEffects.DARKNESS) && !isInEnderPhaseMode(player.getUuid())) {
            player.removeStatusEffect(StatusEffects.DARKNESS);
        }

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