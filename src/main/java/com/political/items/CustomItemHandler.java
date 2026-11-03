package com.political.items;
import com.political.core.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.util.Prediction;
import com.political.AscensionGrindManager;
import com.political.CollectionManager;
import com.political.CompanionManager;
import com.political.DanielsPickaxe;
import com.political.EnderPhaseHandler;
import com.political.FletchingTableGui;
import com.political.GardenHeartOverallsHandler;
import com.political.MasteryHeartTree;
import com.political.PlayerTeleportHelper;
import com.political.ProgressionTreeManager;
import com.political.Relic;
import com.political.RelicManager;
import com.political.SkeletonBowHandler;
import com.political.VenomHandler;
import com.political.VoidwalkerHandler;
import com.political.WeaponAbilityHandler;
import com.political.mixin.BountyDamageMixin;
import com.political.mixin.LivingEntityMixin;



import com.political.party.PoliticalPartyManager;
import com.political.stats.PlayerStatsManager;
import com.political.technique.TechniqueManager;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import com.political.bounty.SlayerItems;
import com.political.bounty.SlayerManager;
import com.political.economy.CoinManager;
import java.util.*;

public class CustomItemHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomItemHandler.class);

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
    private static final Map<UUID, Long> harveyStormcallCooldowns = new HashMap<>();
    private static final Map<UUID, Long> relicCooldowns = new HashMap<>();

    public static boolean isInEnderPhaseMode(UUID uuid) {
        return EnderPhaseHandler.isActive(uuid);
    }

    public static void register() {
        registerBountyItemRestrictions();

        // Prevent custom leather armor from being washed/undyed in a cauldron
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (world.getBlockState(hitResult.getBlockPos()).getBlock() instanceof AbstractCauldronBlock) {
                ItemStack held = player.getItemInHand(hand);
                if (isProtectedItem(held)) {
                    return InteractionResult.FAIL;
                }
            }
            // Fletching Table custom GUI
            if (world.getBlockState(hitResult.getBlockPos()).getBlock() == net.minecraft.world.level.block.Blocks.FLETCHING_TABLE) {
                if (player instanceof ServerPlayer sp) {
                    FletchingTableGui.openGui(sp);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });

        // Daniel's Pickaxe - 3x3 tunnel mining and vein mining
        // Block Tools - special abilities
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide()) return;
            if (!(player instanceof ServerPlayer sp)) return;
            
            ItemStack tool = player.getMainHandItem();
            // Collections count the block, not its drops.
            //
            // Rolling the loot table here ran it a second time, separately from the roll the game
            // had already done, so the number credited was a different random result than the one
            // the player actually picked up, and every fortune level inflated it. It also paid for
            // a full loot table evaluation on every block anybody breaks anywhere.
            //
            // One block counts as one, which is both deterministic and the thing a collection is
            // actually measuring: how much of this did you dig. That matches the choice already
            // made for combat collections, where looting does not inflate a kill count either.
            try {
                CollectionManager.recordBlock(sp, state);
            } catch (Exception ignored) {
                LOGGER.debug("CustomItemHandler.register: ignored an error", ignored);
                // A collection store that is not ready must never break mining.
            }
            if (DanielsPickaxe.isDanielsPickaxe(tool)) {
                DanielsPickaxe.onBlockBreak(sp, (ServerLevel) world, pos, state);
            }
            GardenHeartOverallsHandler.onMatureCropBroken(sp, (ServerLevel) world, pos, state);
            AscensionGrindManager.onBlockBroken(sp, state);
            PoliticalPartyManager.progressPartyCommission(sp, "MINE_BLOCKS", 1);
        });

        // Harvey's Stick - Lightning on attack / Ender Phase - block attacks on players

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;

            // Ender Phase: cannot attack other players while in phase
            if (isInEnderPhaseMode(sp.getUUID()) && entity instanceof Player) {
                return InteractionResult.FAIL;
            }

            ItemStack weapon = player.getItemInHand(hand);
            if (isHarveysStick(weapon) && entity instanceof LivingEntity target) {
                if (target != player) {
                    ServerLevel sw = (ServerLevel) world;

                    // Primary strike
                    LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(sw, EntitySpawnReason.TRIGGERED);
                    if (lightning != null) {
                        lightning.setPos(target.getX(), target.getY(), target.getZ());
                        sw.addFreshEntity(lightning);
                    }

                    // Chain arcs to nearby hostiles
                    int zaps = 0;
                    for (LivingEntity e : sw.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(6.0),
                            e -> e != null && e.isAlive() && e != player && e != target && !(e instanceof net.minecraft.world.entity.player.Player))) {
                        LightningBolt chain = EntityTypes.LIGHTNING_BOLT.create(sw, EntitySpawnReason.TRIGGERED);
                        if (chain != null) {
                            chain.setPos(e.getX(), e.getY(), e.getZ());
                            sw.addFreshEntity(chain);
                        }
                        e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, false, false, true));
                        zaps++;
                        if (zaps >= 4) break;
                    }

                    // Small cost so it isn't free DPS
                    sp.getFoodData().addExhaustion(0.6f);
                }
            }
            
            // Ender Sword: Teleport behind target on hit
            if (SlayerItems.isType(weapon, SlayerItems.ENDER_SWORD) && entity instanceof LivingEntity target) {
                Vec3 targetDir = target.getViewVector(1.0f).normalize().scale(-1.5);
                Vec3 behind = new Vec3(target.getX(), target.getY(), target.getZ()).add(targetDir);
                PlayerTeleportHelper.teleport(sp, (ServerLevel) world, behind.x, behind.y, behind.z, target.getYRot(), target.getXRot());
                world.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
            }

            // Abyssal Blade: Bonus damage to blinded targets
            if (SlayerItems.isType(weapon, SlayerItems.ABYSSAL_BLADE) && entity instanceof LivingEntity target) {
                if (target.hasEffect(MobEffects.BLINDNESS) || target.hasEffect(MobEffects.DARKNESS)) {
                    target.hurtServer((ServerLevel) world, sp.damageSources().playerAttack(sp), 5.0f); // Bonus damage
                    if (world instanceof ServerLevel sw) {
                        sw.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 1.0, target.getZ(), 4, 0.5, 0.5, 0.5, 0.05);
                    }
                }
            }
            
            // Venomous Dagger: stacking venom on hit
            if (VenomHandler.isVenomousDagger(weapon) && entity instanceof LivingEntity venomTarget) {
                if (venomTarget != player) {
                    VenomHandler.onHit(sp, venomTarget);
                }
            }

            if (entity instanceof LivingEntity) {
                LegendaryToolHandler.onEntityAttack(sp, (LivingEntity) entity, weapon);
            }
            
            if (entity instanceof LivingEntity target) {
                com.political.items.DragonPlateHandler.onWearerAttack(sp, target);
            }
            
            return InteractionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

            ItemStack held = player.getItemInHand(hand);

            if (TechniqueManager.tryUseFromItem(serverPlayer, held)) {
                return InteractionResult.SUCCESS;
            }

            String ascId = SlayerItems.getCustomItemId(held);
            if (ascId != null && ascId.startsWith("asc_")
                    && AscensionGrindManager.handleConsume(serverPlayer, ascId, held)) {
                return InteractionResult.SUCCESS;
            }
            
            // Custom items (skill tomes, coin pouches, etc.) are handled below.

            if (RelicManager.tryClaim(serverPlayer, held)) {
                return InteractionResult.SUCCESS;
            }

            if (CompanionManager.tryClaim(serverPlayer, held)) {
                return InteractionResult.SUCCESS;
            }

            if (CustomItemManager.handleItemUse(serverPlayer, held)) {
                return InteractionResult.SUCCESS;
            }

            // Beam Weapons
            if (isAnyBeamWeapon(held)) {
                hpebmLastRightClick.put(player.getUUID(), Clock.gameMillis());
                return InteractionResult.CONSUME;
            }

            // The Gavel
            if (isTheGavel(held)) {
                if (useGavelAbility(serverPlayer, held)) {
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }

            // Harvey's Stick: Stormcall (right-click) — consumes 1 copper ingot
            if (isHarveysStick(held)) {
                long now = Clock.gameMillis();
                long last = harveyStormcallCooldowns.getOrDefault(serverPlayer.getUUID(), 0L);
                if (now - last < 8000L) { // 8s cooldown
                    return InteractionResult.FAIL;
                }
                if (!consumeOne(serverPlayer, Items.COPPER_INGOT)) {
                    serverPlayer.sendSystemMessage(Component.literal("⚡ Stormcall requires 1 Copper Ingot.").withStyle(ChatFormatting.RED));
                    return InteractionResult.FAIL;
                }

                harveyStormcallCooldowns.put(serverPlayer.getUUID(), now);
                serverPlayer.getCooldowns().addCooldown(held, 160); // 8s

                ServerLevel sw = (ServerLevel) world;
                Vec3 start = serverPlayer.getEyePosition();
                Vec3 dir = serverPlayer.getViewVector(1.0f).normalize();
                Vec3 center = start.add(dir.scale(28.0));

                int strikes = 0;
                AABB area = new AABB(center.subtract(6, 4, 6), center.add(6, 4, 6));
                for (LivingEntity e : sw.getEntitiesOfClass(LivingEntity.class, area,
                        e -> e != null && e.isAlive() && !(e instanceof net.minecraft.world.entity.player.Player))) {
                    LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(sw, EntitySpawnReason.TRIGGERED);
                    if (bolt != null) {
                        bolt.setPos(e.getX(), e.getY(), e.getZ());
                        sw.addFreshEntity(bolt);
                    }
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, false, true));
                    strikes++;
                    if (strikes >= 8) break;
                }

                sw.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 8, 0.8, 0.6, 0.8, 0.08);
                sw.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.7f, 1.2f);

                // Drawback: brief frailty after calling the storm
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0, true, false, false));
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, true, false, false));
                return InteractionResult.SUCCESS;
            }

            // â”€â”€ Relic items (modern special items) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            String cid = SlayerItems.getCustomItemId(held);
            if (cid != null && (cid.equals("rift_mirror") || cid.equals("ember_signet") || cid.equals("frostbound_talisman")
                    || cid.equals("echo_chime") || cid.equals("terra_relay"))) {
                long now = Clock.gameMillis();
                long last = relicCooldowns.getOrDefault(serverPlayer.getUUID(), 0L);
                if (now - last < 6000L) { // shared 6s cooldown
                    return InteractionResult.FAIL;
                }
                relicCooldowns.put(serverPlayer.getUUID(), now);
                serverPlayer.getCooldowns().addCooldown(held, 120);

                if ("rift_mirror".equals(cid)) {
                    if (!consumeOne(serverPlayer, Items.ENDER_PEARL)) {
                        serverPlayer.sendSystemMessage(Component.literal("Requires 1 Ender Pearl.").withStyle(ChatFormatting.RED));
                        return InteractionResult.FAIL;
                    }
                    Vec3 dir = serverPlayer.getViewVector(1.0f).normalize();
                    Vec3 dest = new Vec3(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ()).add(dir.scale(24.0));
                    PlayerTeleportHelper.teleport(serverPlayer, (ServerLevel) world, dest.x, dest.y, dest.z, serverPlayer.getYRot(), serverPlayer.getXRot());
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0, true, false, false));
                    world.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
                if ("ember_signet".equals(cid)) {
                    if (!consumeOne(serverPlayer, Items.BLAZE_POWDER)) {
                        serverPlayer.sendSystemMessage(Component.literal("Requires 1 Blaze Powder.").withStyle(ChatFormatting.RED));
                        return InteractionResult.FAIL;
                    }
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 240, 0, true, false, false));
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 160, 1, true, false, false));
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, false, false));
                    world.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                            SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8f, 1.2f);
                    return InteractionResult.SUCCESS;
                }
                if ("frostbound_talisman".equals(cid)) {
                    if (!consumeOne(serverPlayer, Items.SNOWBALL)) {
                        serverPlayer.sendSystemMessage(Component.literal("Requires 1 Snowball.").withStyle(ChatFormatting.RED));
                        return InteractionResult.FAIL;
                    }
                    ServerLevel sw = (ServerLevel) world;
                    for (LivingEntity e : sw.getEntitiesOfClass(LivingEntity.class, serverPlayer.getBoundingBox().inflate(5.0),
                            e -> e != null && e.isAlive() && !(e instanceof net.minecraft.world.entity.player.Player))) {
                        e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1, false, false, true));
                    }
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 1, true, false, false));
                    sw.sendParticles(ParticleTypes.SNOWFLAKE, serverPlayer.getX(), serverPlayer.getY() + 0.8, serverPlayer.getZ(),
                            5, 0.8, 0.5, 0.8, 0.02);
                    return InteractionResult.SUCCESS;
                }
                if ("echo_chime".equals(cid)) {
                    if (!consumeOne(serverPlayer, Items.ECHO_SHARD)) {
                        serverPlayer.sendSystemMessage(Component.literal("Requires 1 Echo Shard.").withStyle(ChatFormatting.RED));
                        return InteractionResult.FAIL;
                    }
                    fireSonicBoom(serverPlayer, world);
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, true, false, false));
                    return InteractionResult.SUCCESS;
                }
                if ("terra_relay".equals(cid)) {
                    if (!consumeOne(serverPlayer, Items.DIRT)) {
                        serverPlayer.sendSystemMessage(Component.literal("Requires 1 Dirt.").withStyle(ChatFormatting.RED));
                        return InteractionResult.FAIL;
                    }
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.HASTE, 240, 1, true, false, false));
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, true, false, false));
                    serverPlayer.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 60, 0, true, false, false));
                    return InteractionResult.SUCCESS;
                }
            }

            // Echoing Core: Sonic Boom
            if (SlayerItems.isType(held, SlayerItems.ECHOING_CORE)) {
                if (!serverPlayer.getCooldowns().isOnCooldown(held)) {
                    serverPlayer.getCooldowns().addCooldown(held, 100); // 5s cooldown
                    fireSonicBoom(serverPlayer, world);
                    return InteractionResult.SUCCESS;
                }
            }

            // Daniel's Pickaxe: Toggle ability
            if (DanielsPickaxe.isDanielsPickaxe(held)) {
                DanielsPickaxe.toggleAbility(serverPlayer);
                return InteractionResult.SUCCESS;
            }

            // Bounty Item Restrictions (Cores, etc.)
            if (isBountyItemRestricted(held)) {
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });

        // Prevent beam weapon placement
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            ItemStack held = player.getItemInHand(hand);
            if (isAnyBeamWeapon(held)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });


    }

    private static void fireSonicBoom(ServerPlayer player, Level world) {
        ServerLevel serverWorld = (ServerLevel) world;
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getViewVector(1.0f);
        
        for (int i = 1; i <= 15; i++) {
            Vec3 pos = start.add(direction.scale(i));
            serverWorld.sendParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            
            AABB box = new AABB(pos.subtract(1.5, 1.5, 1.5), pos.add(1.5, 1.5, 1.5));
            for (Entity entity : serverWorld.getEntitiesOfClass(LivingEntity.class, box, e -> e != null && e.isAlive())) {
                if (entity instanceof LivingEntity living) {
                    living.hurtServer(serverWorld, player.damageSources().sonicBoom(player), 10.0f);
                    Vec3 push = direction.scale(1.0).add(0, 0.2, 0);
                    living.setDeltaMovement(push);
                }
            }
        }
        serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0f, 1.0f);
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
        EnderPhaseHandler.onDisconnect(uuid);
    }

    // ============================================================
    // FLAGSHIP ITEM TICK AGGREGATOR
    // One call per player per tick drives Voidwalker's Crown (Void Step,
    // Void Sight) and the Ender Phase Helmet (phase enter/exit + upkeep).
    // ============================================================
    public static void tickFlagshipItems(ServerPlayer player) {
        VoidwalkerHandler.tickPlayer(player);
        EnderPhaseHandler.tickPlayer(player);
    }

    /**
     * Voidwalker's Crown Phase Dodge. Call from the incoming-damage path
     * (LivingEntityMixin / BountyDamageMixin) before damage is applied.
     *
     * @return true if the hit was phased out entirely and should deal 0 damage.
     */
    public static boolean tryPhaseDodge(ServerPlayer player, net.minecraft.world.damagesource.DamageSource source, float amount) {
        return VoidwalkerHandler.tryPhaseDodge(player, source, amount);
    }

    // ============================================================
// BLOCK BOUNTY ITEM USE (non-usable/eatable) [1]
// ============================================================
    public static void registerBountyItemRestrictions() {
        // Block using/eating bounty items
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide()) return InteractionResult.PASS;

            ItemStack held = player.getItemInHand(hand);

            // Block using cores and bounty items
            if (isBountyItemRestricted(held)) {
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });
    }

    // Check if item should be blocked from use
    public static boolean isBountyItemRestricted(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        // Check custom name for bounty items
        Component name = stack.get(DataComponents.CUSTOM_NAME);
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
        Component name = stack.get(DataComponents.CUSTOM_NAME);
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
        if (stack.get(DataComponents.CUSTOM_NAME) == null) return false;
        String name = stack.get(DataComponents.CUSTOM_NAME).getString();
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

        ChatFormatting color = switch (mk) {
            case 1 -> ChatFormatting.WHITE;
            case 2 -> ChatFormatting.GREEN;
            case 3 -> ChatFormatting.YELLOW;
            case 4 -> ChatFormatting.GOLD;
            case 5 -> ChatFormatting.LIGHT_PURPLE;
            default -> ChatFormatting.WHITE;
        };

        weapon.set(DataComponents.CUSTOM_NAME,
                Component.literal(name).withStyle(color, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));

        // Tier classification
        String tierTag = switch (mk) {
            case 1 -> "◆ COMMON";
            case 2 -> "◆ UNCOMMON";
            case 3 -> "◆ RARE";
            case 4 -> "◆ EPIC";
            case 5 -> "◆ LEGENDARY";
            default -> "◆ UNKNOWN";
        };
        ChatFormatting tierColor = switch (mk) {
            case 1 -> ChatFormatting.GRAY;
            case 2 -> ChatFormatting.GREEN;
            case 3 -> ChatFormatting.BLUE;
            case 4 -> ChatFormatting.GOLD;
            case 5 -> ChatFormatting.LIGHT_PURPLE;
            default -> ChatFormatting.GRAY;
        };
        lore.add(Component.literal(tierTag + " — High-Powered Energy Beam").withStyle(tierColor, ChatFormatting.BOLD));
        lore.add(Component.literal(""));

        // Unique flavor text per tier
        switch (mk) {
            case 1 -> {
                lore.add(Component.literal("An experimental energy emitter salvaged from").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("classified research archives. Unstable but").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("functional as a proof-of-concept weapon.").withStyle(ChatFormatting.GRAY));
            }
            case 2 -> {
                lore.add(Component.literal("Military-grade energy emitter issued to elite").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("field operatives. Refined plasma containment").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("ensures consistent output under field conditions.").withStyle(ChatFormatting.GRAY));
            }
            case 3 -> {
                lore.add(Component.literal("Equipped with an enhanced focusing crystal,").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("this unit concentrates plasma into a tighter").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("beam with explosive endpoint detonation.").withStyle(ChatFormatting.GRAY));
            }
            case 4 -> {
                lore.add(Component.literal("Forged from rare celestial alloys, this unit").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("was commissioned by the highest echelons of").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("command. Few have ever seen one in person.").withStyle(ChatFormatting.GRAY));
            }
            case 5 -> {
                lore.add(Component.literal("The pinnacle of plasma engineering. Rumored").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("to have been assembled using the core of a").withStyle(ChatFormatting.GRAY));
                lore.add(Component.literal("collapsed star. Maximum power. Maximum risk.").withStyle(ChatFormatting.GRAY));
            }
        }

        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(color));
        // Match actual fireBeam() damage values (mk1â†’tier2, mk2â†’tier3, etc.)
        float actualDamage = switch (mk) {
            case 1 -> 2.0f;
            case 2 -> 3.5f;
            case 3 -> 5.0f;
            case 4 -> 6.5f;
            case 5 -> 8.0f;
            default -> 1.0f;
        };
        lore.add(Component.literal("⚡ Damage/tick: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal(String.format("%.1f", actualDamage)).withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
        int xpCost = Math.max(1, mk - 2);
        lore.add(Component.literal("ðŸ’¡ XP Cost: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal(xpCost + " level(s)/sec").withStyle(ChatFormatting.YELLOW)));
        lore.add(Component.literal("ðŸ“¡ Beam Range: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal((30 + mk * 2) + " blocks").withStyle(ChatFormatting.AQUA)));

        if (mk >= 3) {
            float explosionDmg = switch (mk) {
                case 3, 4 -> 0.0f;
                case 5 -> 4.0f;
                default -> 0.0f;
            };
            if (mk >= 5) {
                lore.add(Component.literal("ðŸ’¥ Endpoint Explosion: ").withStyle(ChatFormatting.WHITE)
                        .append(Component.literal(explosionDmg + " dmg, 2.0 radius").withStyle(ChatFormatting.GOLD)));
            }
        }

        lore.add(Component.literal(""));
        lore.add(Component.literal("Right-click: Continuous beam (hold)").withStyle(ChatFormatting.YELLOW));

        weapon.set(DataComponents.LORE, new ItemLore(lore));

        if (mk >= 4) {
            weapon.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
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

    public static boolean useGavelAbility(ServerPlayer player, ItemStack gavelStack) {
        UUID uuid = player.getUUID();
        long now = Clock.gameMillis();

        if (gavelCooldowns.containsKey(uuid)) {
            long remaining = (gavelCooldowns.get(uuid) + GAVEL_COOLDOWN_MS) - now;
            if (remaining > 0) return false;
        }

        boolean found = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(Items.WIND_CHARGE)) {
                player.getInventory().getItem(i).shrink(1);
                found = true;
                break;
            }
        }
        if (!found) {
            player.sendSystemMessage(Component.literal("Requires 1 Wind Charge!").withStyle(ChatFormatting.RED));
            return false;
        }

        gavelCooldowns.put(uuid, now);
        player.getCooldowns().addCooldown(gavelStack, GAVEL_COOLDOWN_TICKS);

        ServerLevel world = player.level();

        Vec3 lookDirection = player.getViewVector(1.0f);
        double forwardStrength = 2.5;
        double upStrength = 1.8;
        player.setDeltaMovement(
                lookDirection.x * forwardStrength,
                upStrength,
                lookDirection.z * forwardStrength
        );
        player.syncVelocity = true;

        WindCharge windCharge = new WindCharge(EntityTypes.WIND_CHARGE, world);
        windCharge.setOwner(player);
        windCharge.setPos(player.getX(), player.getY(), player.getZ());
        windCharge.setDeltaMovement(0, -2.0, 0);
        world.addFreshEntity(windCharge);

        ((ServerLevel) world).sendParticles(ParticleTypes.CLOUD,
                player.getX(), player.getY() + 0.5, player.getZ(),
                8, 0.5, 0.2, 0.5, 0.08);

        player.sendSystemMessage(Component.literal("⚡ GAVEL LAUNCH! ⚡").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        return true;
    }

    public static void tickUltraOverclockedLeftClick(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();

        if (getBeamTier(mainHand) != 7) {
            wasSwinging.remove(player.getUUID());
            return;
        }

        UUID uuid = player.getUUID();
        boolean isSwinging = player.isSwinging();
        boolean wasSwingingBefore = wasSwinging.getOrDefault(uuid, false);

        // Check if player is right-clicking - if so, don't trigger left-click ability
        long lastClick = hpebmLastRightClick.getOrDefault(uuid, 0L);
        boolean isRightClicking = (Clock.gameMillis() - lastClick) < 200;

        // Detect new swing (left-click) - only if NOT ng
        if (isSwinging && !wasSwingingBefore && !isRightClicking) {
            useUltraOverclockedAbility(player, mainHand);
        }

        wasSwinging.put(uuid, isSwinging);
    }

    public static void tickUndeadHeart(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        if (SlayerItems.isType(main, SlayerItems.UNDEAD_HEART) || SlayerItems.isType(off, SlayerItems.UNDEAD_HEART)) {
            player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 40, 4, true, false, false));
        }
        
        // Bouncy Slime: Jump Boost and Fall Resistance
        if (SlayerItems.isType(main, SlayerItems.BOUNCY_SLIME) || SlayerItems.isType(off, SlayerItems.BOUNCY_SLIME)) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 40, 1, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 0, true, false, false));
        }
    }



    public static void tickHPEBM(ServerPlayer player) {
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);

        boolean holdingBeam = isAnyBeamWeapon(mainHand) || isAnyBeamWeapon(offHand);
        UUID uuid = player.getUUID();

        long lastClick = hpebmLastRightClick.getOrDefault(uuid, 0L);
        boolean rightClickHeld = (Clock.gameMillis() - lastClick) < 200;

        if (!holdingBeam || !rightClickHeld) {
            hpebmUseTicks.remove(uuid);
            return;
        }


        ItemStack beamItem = isAnyBeamWeapon(mainHand) ? mainHand : offHand;
        int tier = getBeamTier(beamItem);


        int xpCostPerSecond = Math.max(1, tier - 2);

        if (player.experienceLevel < xpCostPerSecond) {
            player.sendSystemMessage(Component.literal("⚡ Not enough XP! Need " + xpCostPerSecond + " level(s)").withStyle(ChatFormatting.RED));
            hpebmUseTicks.remove(uuid);
            return;
        }

        int ticks = hpebmUseTicks.getOrDefault(uuid, 0) + 1;
        hpebmUseTicks.put(uuid, ticks);

        if (ticks == 1) {
            player.giveExperienceLevels(-xpCostPerSecond);
            player.sendSystemMessage(Component.literal("⚡ Beam Activated! -" + xpCostPerSecond + " XP Level(s)").withStyle(ChatFormatting.YELLOW));
        } else if (ticks % 20 == 0) {
            player.giveExperienceLevels(-xpCostPerSecond);
            player.sendSystemMessage(Component.literal("⚡ -" + xpCostPerSecond + " XP Level(s)").withStyle(ChatFormatting.YELLOW));
        }

        fireHPEBMBeam(player, ticks);
    }

    private static final Map<UUID, Long> levelWarningCooldown = new HashMap<>();
    private static final long LEVEL_WARNING_COOLDOWN_MS = 2000;


    private static void fireHPEBMBeam(ServerPlayer player, int ticks) {
        ServerLevel world = player.level();

        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
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

        Vec3 eyePos = player.getEyePosition();
        Vec3 lookDir = player.getViewVector(1.0f);
        Vec3 sideOffset = lookDir.cross(new Vec3(0, 1, 0)).normalize().scale(0.7);  // Was 0.4, now further right
        Vec3 start = eyePos.add(sideOffset).add(0, -0.7, 0);  // Was -0.4, now further down
        Vec3 direction = lookDir;

        Vec3 endPoint = findBeamEndpoint(world, player, start, direction, 30.0 + tier * 2.0);
        double beamLength = start.distanceTo(endPoint);

        AABB searchAABB = new AABB(
                start.x - (30 + tier * 2.0), start.y - (30 + tier * 2.0), start.z - (30 + tier * 2.0),
                start.x + (30 + tier * 2.0), start.y + (30 + tier * 2.0), start.z + (30 + tier * 2.0));

        for (Entity entity : world.getEntitiesOfClass(LivingEntity.class, searchAABB, e -> e != null && e.isAlive() && e != player)) {
            if (!(entity instanceof LivingEntity living)) continue;

            Vec3 entityPos = new Vec3(living.getX(), living.getY() + living.getBbHeight() / 2, living.getZ());
            Vec3 toEntity = entityPos.subtract(start);
            double distance = toEntity.length();
            if (distance > 30) continue;

            Vec3 projected = direction.scale(toEntity.dot(direction));
            double perpendicularDist = toEntity.subtract(projected).length();

            if (perpendicularDist < (1.5 + tier * 0.1) && toEntity.dot(direction) > 0) {
                living.hurtTime = 0;
                living.hurtServer(world, player.damageSources().magic(), baseDamage);

                ((ServerLevel) world).sendParticles(ParticleTypes.CRIT,
                        living.getX(), living.getY() + living.getBbHeight() / 2, living.getZ(),
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
            world.playSound(null, player.blockPosition(),
                    SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.5f, pitch);
        }
    }

    private static Vec3 findBeamEndpoint(ServerLevel world, ServerPlayer player, Vec3 start, Vec3 direction, double maxRange) {
        Vec3 end = start.add(direction.scale(maxRange));

        net.minecraft.world.phys.BlockHitResult blockHit = world.clip(new net.minecraft.world.level.ClipContext(
                start, end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                player
        ));

        if (blockHit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            return blockHit.getLocation();
        }
        return end;
    }

    private static void fireOriginalBeam(ServerLevel world, Vec3 start, Vec3 direction, int ticks, ServerPlayer player) {
        // Calculate actual endpoint via raycast
        Vec3 maxEnd = start.add(direction.scale(45));
        net.minecraft.world.phys.BlockHitResult blockHit = world.clip(new net.minecraft.world.level.ClipContext(
                start, maxEnd,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                player
        ));

        Vec3 endPoint = maxEnd;
        if (blockHit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            endPoint = blockHit.getLocation();
        }
        double beamLength = start.distanceTo(endPoint);

        double spiralRadius = 0.25;
        double spiralSpeed = 0.6;

        for (double i = 0; i < beamLength; i += 1.5) {
            Vec3 basePos = start.add(direction.scale(i));
            Vec3 up = new Vec3(0, 1, 0);
            if (Math.abs(direction.dot(up)) > 0.99) up = new Vec3(1, 0, 0);
            Vec3 perp1 = direction.cross(up).normalize();
            Vec3 perp2 = direction.cross(perp1).normalize();
            double angle = (ticks * spiralSpeed + i * 2) % (Math.PI * 2);

            ((ServerLevel) world).sendParticles(ParticleTypes.CRIT, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.2);

            Vec3 spiral1 = basePos.add(perp1.scale(Math.cos(angle) * spiralRadius)).add(perp2.scale(Math.sin(angle) * spiralRadius));
            ((ServerLevel) world).sendParticles(ParticleTypes.ELECTRIC_SPARK, spiral1.x, spiral1.y, spiral1.z, 1, 0, 0, 0, 0.25);

            Vec3 spiral2 = basePos.add(perp1.scale(Math.cos(angle + Math.PI) * spiralRadius)).add(perp2.scale(Math.sin(angle + Math.PI) * spiralRadius));
            ((ServerLevel) world).sendParticles(ParticleTypes.ELECTRIC_SPARK, spiral2.x, spiral2.y, spiral2.z, 1, 0, 0, 0, 0.25);
        }
    }

    public static void markRightClick(ServerPlayer player) {
        hpebmLastRightClick.put(player.getUUID(), Clock.gameMillis());
    }

    // Upgraded beam (Tiers 2-7) - unique particles per tier, fast dissipating
    private static void fireUpgradedBeam(ServerLevel world, Vec3 start, Vec3 endPoint, int ticks, int tier, ServerPlayer player) {
        Vec3 direction = endPoint.subtract(start).normalize();
        double beamLength = start.distanceTo(endPoint);

        double spiralSpeed = 0.6 + (tier * 0.08);
        double spiralRadius = 0.2 + (tier * 0.03);
        int helixCount = Math.min(tier, 3);

        for (double i = 0; i < beamLength; i += 1.2) {
            Vec3 basePos = start.add(direction.scale(i));
            Vec3 up = new Vec3(0, 1, 0);
            if (Math.abs(direction.dot(up)) > 0.99) up = new Vec3(1, 0, 0);
            Vec3 perp1 = direction.cross(up).normalize();
            Vec3 perp2 = direction.cross(perp1).normalize();
            double angle = (ticks * spiralSpeed + i * 2) % (Math.PI * 2);

            // Core particles per tier (fast-dissipating, no drop)
            switch (tier) {
                case 2 ->
                        ((ServerLevel) world).sendParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.2);
                case 3 -> {
                    world.sendParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.2);
                    ((ServerLevel) world).sendParticles(ParticleTypes.CRIT, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.15);
                }
                case 4 -> {
                    ((ServerLevel) world).sendParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 2, 0, 0, 0, 0.15);
                    ((ServerLevel) world).sendParticles(ParticleTypes.END_ROD, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.01);
                }
                case 5 -> {
                    ((ServerLevel) world).sendParticles(ParticleTypes.WITCH, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.01);
                    ((ServerLevel) world).sendParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 2, 0, 0, 0, 0.2);
                }
                case 6 -> {
                    ((ServerLevel) world).sendParticles(ParticleTypes.WITCH, basePos.x, basePos.y, basePos.z, 2, 0, 0, 0, 0.01);
                    ((ServerLevel) world).sendParticles(ParticleTypes.DAMAGE_INDICATOR, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.05);
                }
                case 7 -> {
                    // Reduced speed: 0.15 -> 0.05 (less movement = tighter beam)
                    ((ServerLevel) world).sendParticles(ParticleTypes.BUBBLE, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.015);
                    ((ServerLevel) world).sendParticles(ParticleTypes.REVERSE_PORTAL, basePos.x, basePos.y, basePos.z, 1, 0, 0, 0, 0.5);
                    // Reduced delta (0.02 -> 0.0) and speed (0.2 -> 0.05)
                    ((ServerLevel) world).sendParticles(ParticleTypes.CRIT, basePos.x, basePos.y, basePos.z, 1, 0.0, 0.0, 0.0, 0.01);
                    ((ServerLevel) world).sendParticles(ParticleTypes.ENCHANTED_HIT, basePos.x, basePos.y, basePos.z, 1, 0.0, 0.0, 0.0, 0.01);
                    ((ServerLevel) world).sendParticles(ParticleTypes.WHITE_ASH, basePos.x, basePos.y, basePos.z, 1, 0.0, 0.0, 0.0, 0.01);
                }
            }

            // Spiral helixes
            for (int h = 0; h < helixCount; h++) {
                double helixAngle = angle + (h * Math.PI * 2 / helixCount);
                Vec3 spiral = basePos
                        .add(perp1.scale(Math.cos(helixAngle) * spiralRadius))
                        .add(perp2.scale(Math.sin(helixAngle) * spiralRadius));
                ((ServerLevel) world).sendParticles(ParticleTypes.ELECTRIC_SPARK, spiral.x, spiral.y, spiral.z, 1, 0, 0, 0, 0.2);
            }

            if (tier >= 5) {
                double outerRadius = spiralRadius * 1.5;
                double outerAngle = -angle * 0.5;
                Vec3 outer = basePos
                        .add(perp1.scale(Math.cos(outerAngle) * outerRadius))
                        .add(perp2.scale(Math.sin(outerAngle) * outerRadius));
                ((ServerLevel) world).sendParticles(ParticleTypes.CRIT, outer.x, outer.y, outer.z, 1, 0, 0, 0, 0.015);
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

            ((ServerLevel) world).sendParticles(ParticleTypes.EXPLOSION, endPoint.x, endPoint.y, endPoint.z, 1, 0, 0, 0, 0);

            if (tier >= 7) {
                ((ServerLevel) world).sendParticles(ParticleTypes.GLOW, endPoint.x, endPoint.y, endPoint.z, 4, 0.5, 0.5, 0.5, 0.3);
            }

            AABB explosionAABB = new AABB(
                    endPoint.x - explosionRadius, endPoint.y - explosionRadius, endPoint.z - explosionRadius,
                    endPoint.x + explosionRadius, endPoint.y + explosionRadius, endPoint.z + explosionRadius);

            for (Entity entity : world.getEntitiesOfClass(LivingEntity.class, explosionAABB, e -> e != null && e.isAlive())) {
                if (entity instanceof LivingEntity living) {
                    Vec3 livingPos = new Vec3(living.getX(), living.getY(), living.getZ());
                    double dist = livingPos.distanceTo(endPoint);
                    if (dist <= explosionRadius) {
                        living.hurtTime = 0;
                        living.hurtServer(world, player.damageSources().magic(), explosionDamage);
                    }
                }
            }
        }
    }

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ITEM DETECTION - Works in 1.21.11
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    private static boolean hasCustomTag(ItemStack stack, String tagName) {
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return false;
        CompoundTag nbt = customData.copyTag();
        if (!nbt.contains(tagName)) return false;
        try {
            return nbt.getByte(tagName).orElse((byte) 0) != 0;
        } catch (Exception e) {
            LOGGER.debug("CustomItemHandler.hasCustomTag: ignored an error", e);
            return false;
        }
    }

    private static boolean isHarveysStick(ItemStack stack) {
        return stack.is(Items.STICK) && hasCustomTag(stack, "harveys_stick");
    }

    private static boolean consumeOne(ServerPlayer player, Item item) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack st = inv.getItem(i);
            if (!st.isEmpty() && st.is(item)) {
                st.shrink(1);
                inv.setChanged();
                return true;
            }
        }
        return false;
    }

    public static boolean isTheGavel(ItemStack stack) {
        return stack.is(Items.MACE) && hasCustomTag(stack, "the_gavel");
    }

    public static boolean isHermesShoes(ItemStack stack) {
        return stack.is(Items.IRON_BOOTS) && hasCustomTag(stack, "hermes_shoes");
    }

    /** Checks if player is wearing Crown of Greed or Crown of Midas */
    private static boolean isWearingCrownOfGreedOrMidas(ServerPlayer player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        return SlayerItems.isCrownOfGreed(helmet) || SlayerItems.isCrownOfMidas(helmet);
    }

    public static void registerGoldOnlyRestrictions() {
        // Block non-gold item usage (right-click, interact)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (isWearingCrownOfGreedOrMidas((ServerPlayer) player)) {
                ItemStack stack = player.getItemInHand(hand);
                if (!isGoldItem(stack)) {
                    player.sendSystemMessage(Component.literal(" Only gold items can be used while wearing the crown!").withStyle(ChatFormatting.YELLOW));
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.PASS;
        });

        // Block non-gold item usage on entities
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (isWearingCrownOfGreedOrMidas((ServerPlayer) player)) {
                ItemStack stack = player.getItemInHand(hand);
                if (!isGoldItem(stack)) {
                    player.sendSystemMessage(Component.literal(" Only gold items can be used while wearing the crown!").withStyle(ChatFormatting.YELLOW));
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.PASS;
        });

        // Block non-gold item usage (general right-click)
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (isWearingCrownOfGreedOrMidas((ServerPlayer) player)) {
                ItemStack stack = player.getItemInHand(hand);
                if (!isGoldItem(stack)) {
                    player.sendSystemMessage(Component.literal(" Only gold items can be used while wearing the crown!").withStyle(ChatFormatting.YELLOW));
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.PASS;
        });
    }

    public static float calculateSlayerDamage(ServerPlayer attacker, LivingEntity target, float baseDamage) {
        ItemStack weapon = attacker.getMainHandItem();
        float modifiedDamage = Math.max(baseDamage, getListedLoreDamage(weapon));

        // Apply T1 slayer sword multiplier (2x)
        double swordMultiplier = SlayerItems.getSlayerSwordDamageMultiplier(weapon, target, attacker);
        modifiedDamage *= (float) swordMultiplier;

        // Apply T2 slayer sword multiplier (3x) — only if T1 multiplier wasn't already applied
        if (swordMultiplier == 1.0) {
            double upgradedMultiplier = SlayerItems.getUpgradedSlayerSwordDamageMultiplier(weapon, target, attacker);
            modifiedDamage *= (float) upgradedMultiplier;
        }

        // Apply boss damage resistance (only for non-slayer weapons)
        if (SlayerManager.isSlayerBoss(target.getUUID())) {
            if (!SlayerItems.bypassesSlayerResistance(weapon) && !SlayerItems.isUpgradedSlayerSword(weapon)) {
                double resistance = SlayerManager.getDamageResistance(target.getUUID());
                modifiedDamage *= (float) (1.0 - resistance);
            }
        }

        // Voidwalker's Crown - Dark Affinity: +20% damage at night or vs shrouded foes
        modifiedDamage = VoidwalkerHandler.applyDarkDamageBonus(attacker, target, modifiedDamage);

        // Crown of Greed: multiply damage by number of digits in coin count
        if (SlayerItems.isCrownOfGreed(attacker.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD))) {
            int coins = CoinManager.getCoins(attacker);
            int digits = coins > 0 ? String.valueOf(coins).length() : 1;
            modifiedDamage *= digits;
        }

        // Crown of Midas: multiply damage by 2x number of digits in coin count
        if (SlayerItems.isCrownOfMidas(attacker.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD))) {
            int coins = CoinManager.getCoins(attacker);
            int digits = coins > 0 ? String.valueOf(coins).length() : 1;
            modifiedDamage *= (digits * 2);
        }

        // Midas's Sword: damage scales with kill count digits
        // Also has Crown synergy: wearing Crown of Greed/Midas doubles the damage bonus
        if (SlayerItems.isMidasSword(weapon)) {
            int kills = SlayerItems.getMidasSwordKills(weapon);
            int killDigits = kills > 0 ? String.valueOf(kills).length() : 1;
            // Base: damage multiplier = kill digits Ã— 2
            modifiedDamage *= (killDigits * 2);
            
            // Crown synergy: if wearing Crown of Greed or Midas, double the multiplier
            if (SlayerItems.isCrownOfGreed(attacker.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD)) ||
                SlayerItems.isCrownOfMidas(attacker.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD))) {
                modifiedDamage *= 2;
            }
        }

        // Weapon abilities (covers custom attributes + named weapons + deterministic fallback).
        modifiedDamage = WeaponAbilityHandler.processDamage(attacker, target, modifiedDamage);

        if (SlayerManager.isSlayerBoss(target.getUUID())) {
            modifiedDamage *= MasteryHeartTree.slayerBossDamageMultiplier(attacker.getUUID().toString(), weapon);
        }
        modifiedDamage *= MasteryHeartTree.combatDamageMultiplier(attacker, target);

        double progressionCombatBonus = ProgressionTreeManager.getCombatDamageBonus(attacker.getUUID().toString());
        if (progressionCombatBonus > 0) {
            modifiedDamage *= (1.0f + (float) progressionCombatBonus);
        }

        // Zombie Berserker Helmet: +300% damage (4× total). Applied here so it survives BountyDamageMixin's
        // calculateSlayerDamage replacement (hurtServer mixin order no longer strips this bonus).
        modifiedDamage *= getBerserkerDamageMultiplier(attacker);

        // Skyblock-style stats: Strength multiplier, crit roll, Ferocity extra strikes, damage holograms.
        modifiedDamage = PlayerStatsManager.applyOutgoingDamage(attacker, target, modifiedDamage);

        return modifiedDamage;
    }

    /**
     * Reads the displayed lore damage and treats it as minimum effective hit damage.
     * This keeps custom weapon combat output aligned with what item lore says.
     */
    private static float getListedLoreDamage(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0f;
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null || lore.lines().isEmpty()) return 0f;
        float best = 0f;
        for (Component c : lore.lines()) {
            String s = c.getString();
            if (s == null) continue;
            String lower = s.toLowerCase(Locale.ROOT);
            // Only trust the actual stat line; ability text like "+32% damage" must not be parsed as base damage.
            if (!(lower.contains("⚔ damage:") || lower.trim().startsWith("damage:"))) continue;
            StringBuilder n = new StringBuilder();
            boolean seenDigit = false;
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if ((ch >= '0' && ch <= '9') || ch == '.' || ch == ',') {
                    n.append(ch);
                    seenDigit = true;
                } else if (seenDigit) {
                    break;
                }
            }
            if (!seenDigit) continue;
            try {
                float v = Float.parseFloat(n.toString().replace(",", ""));
                if (v > best) best = v;
            } catch (NumberFormatException ignored) {}
        }
        return best;
    }

    public static void tickCrownOfGreed(ServerPlayer player) {
        ItemStack helmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        net.minecraft.resources.Identifier modId = net.minecraft.resources.Identifier.fromNamespaceAndPath("political", "crown_of_greed_hp");
        var healthAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);

        if (!SlayerItems.isCrownOfGreed(helmet)) {
            // Remove the modifier if crown is no longer worn
            if (healthAttr != null && healthAttr.getModifier(modId) != null) {
                healthAttr.removeModifier(modId);
            }
            return;
        }

        // Check for non-gold items in inventory and remove them
        if (player.tickCount % 20 == 0) { // Check every second
            removeNonGoldItems(player);
        }

        // Enforce 4 HP max health (2 hearts) via attribute modifier
        if (healthAttr != null && healthAttr.getModifier(modId) == null) {
            healthAttr.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    modId, -16.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
        }
        // Cap health to 4 if it somehow exceeds it
        if (player.getHealth() > 4.0f) player.setHealth(4.0f);
    }

    /** Tick handler for Crown of Midas: enforces 10 HP max health and gold-only restriction. */
    public static void tickCrownOfMidas(ServerPlayer player) {
        ItemStack helmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        net.minecraft.resources.Identifier modId = net.minecraft.resources.Identifier.fromNamespaceAndPath("political", "crown_of_midas_hp");
        var healthAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);

        if (!SlayerItems.isCrownOfMidas(helmet)) {
            // Remove the modifier if crown is no longer worn
            if (healthAttr != null && healthAttr.getModifier(modId) != null) {
                healthAttr.removeModifier(modId);
            }
            return;
        }

        // Check for non-gold items in inventory and remove them
        if (player.tickCount % 20 == 0) { // Check every second
            removeNonGoldItems(player);
        }

        // Enforce 10 HP max health (5 hearts) via attribute modifier
        if (healthAttr != null && healthAttr.getModifier(modId) == null) {
            healthAttr.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    modId, -10.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
        }
        // Cap health to 10 if it somehow exceeds it
        if (player.getHealth() > 10.0f) player.setHealth(10.0f);
    }

    /** Removes non-gold items from player's inventory (for Crown of Greed/Midas restriction). */
    private static void removeNonGoldItems(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && !isGoldItem(stack)) {
                // Drop the item on the ground
                player.drop(stack, true, Prediction.SERVER_ONLY);
                player.getInventory().setItem(i, ItemStack.EMPTY);
                player.sendSystemMessage(Component.literal(" Only gold items allowed while wearing the crown!").withStyle(ChatFormatting.YELLOW));
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
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
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

    public static void tickSlayerSystems(ServerPlayer player) {
        // Already handled by SlayerManager.tick() at server level
    }

    public static void tickZombieBerserkerHelmet(ServerPlayer player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        UUID uuid = player.getUUID();
        boolean wasActive = berserkerHelmetActive.getOrDefault(uuid, false);

        if (SlayerItems.isZombieBerserkerHelmet(helmet)) {
            boolean canUse = SlayerItems.canUseZombieBerserkerHelmet(player);

            if (canUse) {
                // Apply the berserker effect - half HP
                if (!wasActive) {
                    // Just equipped - apply HP reduction
                    applyBerserkerEffect(player, true);
                    berserkerHelmetActive.put(uuid, true);
                    player.sendSystemMessage(Component.literal("\u2620 Berserker Mode ACTIVATED! \u2620")
                            .withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
                }
            } else {
                // Player doesn't meet level requirement - show warning [1]
                long now = Clock.gameMillis();
                long lastWarning = berserkerWarningCooldown.getOrDefault(uuid, 0L);

                if (now - lastWarning > WARNING_COOLDOWN_MS) {
                    player.sendSystemMessage(Component.literal("\u26D4 Zombie Berserker Helmet requires Level 12!")
                            .withStyle(ChatFormatting.RED));
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
                player.sendSystemMessage(Component.literal("Berserker Mode deactivated")
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private static void applyBerserkerEffect(ServerPlayer player, boolean activate) {
        var healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) return;

        // Remove existing modifier if present
        Identifier modifierId = Identifier.fromNamespaceAndPath("political", "berserker_helmet_hp");
        healthAttribute.removeModifier(modifierId);

        if (activate) {
            // Add -50% max HP modifier
            AttributeModifier modifier = new AttributeModifier(
                    modifierId,
                    -0.5, // -50%
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            healthAttribute.addPermanentModifier(modifier);

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
    public static float getBerserkerDamageMultiplier(ServerPlayer player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);

        if (SlayerItems.isZombieBerserkerHelmet(helmet) &&
                SlayerItems.canUseZombieBerserkerHelmet(player)) {
            return 4.0f; // +300% damage = 4x total
        }

        return 1.0f;
    }


    public static boolean isHPEBM(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!stack.is(Items.IRON_SHOVEL)) return false;

        // Check for custom name component
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            Component name = stack.get(DataComponents.CUSTOM_NAME);
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
        return stack.is(Items.ECHO_SHARD) && hasCustomTag(stack, "warden_core");
    }


    public static ItemStack createHarveysStick() {
        ItemStack stack = new ItemStack(Items.STICK);
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("harveys_stick", (byte) 1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Harvey's Stick").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(""),
                Component.literal("◆ LEGENDARY — STORM RELIC ◆").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                Component.literal(""),
                Component.literal("Attack: Chain Lightning").withStyle(ChatFormatting.YELLOW),
                Component.literal("  └ Strikes target + arcs to nearby hostiles").withStyle(ChatFormatting.GRAY),
                Component.literal(""),
                Component.literal("Right-click: Stormcall").withStyle(ChatFormatting.AQUA),
                Component.literal("  └ Calls lightning in an area at your aim point").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ Consumes 1 Copper Ingot").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ 8s cooldown").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal(""),
                Component.literal("Damage: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("Lightning").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)),
                Component.literal(""),
                Component.literal("[MFLUX: Contractor-issued armament | CRD-experimental]").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal("「Unique」").withStyle(ChatFormatting.GOLD)
        )));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(stack, "harveys_stick");
        return stack;
    }

    public static ItemStack createTheGavel() {
        ItemStack stack = new ItemStack(Items.MACE);
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("the_gavel", (byte) 1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("The Gavel").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(""),
                Component.literal("◆ JUDICIAL AUTHORITY ◆").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                Component.literal(""),
                Component.literal("Right-click: Gavel Launch").withStyle(ChatFormatting.AQUA),
                Component.literal("  └ Flings you high into the air").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ Consumes 1 Wind Charge").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ 3s cooldown").withStyle(ChatFormatting.GRAY),
                Component.literal(""),
                Component.literal("[MFLUX: Judicial enforcement weapon | Contractor-grade]").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal("「Unique」").withStyle(ChatFormatting.LIGHT_PURPLE)
        )));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        // Custom enchantments are NOT pre-applied — players apply them via the enchanting table.
        // The lore above lists compatible enchantments as a guide only.
        return stack;
    }

    public static ItemStack createHermesShoes() {
        ItemStack stack = new ItemStack(Items.IRON_BOOTS);
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("hermes_shoes", (byte) 1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Hermes Shoes").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(""),
                Component.literal("◆ DIVINE FOOTWEAR ◆").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                Component.literal(""),
                Component.literal("Passive: Swift as the Wind").withStyle(ChatFormatting.GREEN),
                Component.literal("  └ Permanent Speed III while worn").withStyle(ChatFormatting.GRAY),
                Component.literal(""),
                Component.literal("Speed Bonus: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("+60%").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)),
                Component.literal(""),
                Component.literal("[MFLUX: Field-tested containment gear | Contractor Program]").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal("「Unique」").withStyle(ChatFormatting.AQUA)
        )));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(stack, "hermes_shoes");
        return stack;
    }

    // â”€â”€ Modern relic items â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public static ItemStack createRiftMirror() {
        ItemStack stack = new ItemStack(Items.AMETHYST_SHARD);
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("rift_mirror", (byte) 1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Rift Mirror").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(""),
                Component.literal("◆ LEGENDARY — SPACE RELIC ◆").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                Component.literal(""),
                Component.literal("Right-click: Blink").withStyle(ChatFormatting.AQUA),
                Component.literal("  └ Teleports you forward").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ Consumes 1 Ender Pearl").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ Shared relic cooldown").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal(""),
                Component.literal("「Unique」").withStyle(ChatFormatting.LIGHT_PURPLE)
        )));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(stack, "rift_mirror");
        return stack;
    }

    public static ItemStack createEmberSignet() {
        ItemStack stack = new ItemStack(Items.BLAZE_POWDER);
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("ember_signet", (byte) 1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Ember Signet").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(""),
                Component.literal("◆ LEGENDARY — FIRE SEAL ◆").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                Component.literal(""),
                Component.literal("Right-click: Ember Oath").withStyle(ChatFormatting.AQUA),
                Component.literal("  └ Strength + Fire Resistance").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ Consumes 1 Blaze Powder").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ Drawback: brief weakness").withStyle(ChatFormatting.DARK_RED),
                Component.literal(""),
                Component.literal("「Unique」").withStyle(ChatFormatting.GOLD)
        )));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(stack, "ember_signet");
        return stack;
    }

    public static ItemStack createFrostboundTalisman() {
        ItemStack stack = new ItemStack(Items.SNOWBALL);
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("frostbound_talisman", (byte) 1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Frostbound Talisman").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(""),
                Component.literal("◆ LEGENDARY — ICE CHARM ◆").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                Component.literal(""),
                Component.literal("Right-click: Permafrost Pulse").withStyle(ChatFormatting.AQUA),
                Component.literal("  └ Slows nearby hostiles").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ Grants Absorption").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ Consumes 1 Snowball").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal(""),
                Component.literal("「Unique」").withStyle(ChatFormatting.AQUA)
        )));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(stack, "frostbound_talisman");
        return stack;
    }

    public static ItemStack createEchoChime() {
        ItemStack stack = new ItemStack(Items.BELL);
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("echo_chime", (byte) 1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Echo Chime").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(""),
                Component.literal("◆ LEGENDARY — SONIC RELIC ◆").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD),
                Component.literal(""),
                Component.literal("Right-click: Resonant Blast").withStyle(ChatFormatting.AQUA),
                Component.literal("  └ Sonic boom line attack").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ Consumes 1 Echo Shard").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal("  └ Drawback: brief darkness").withStyle(ChatFormatting.DARK_RED),
                Component.literal(""),
                Component.literal("「Unique」").withStyle(ChatFormatting.DARK_AQUA)
        )));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(stack, "echo_chime");
        return stack;
    }

    public static ItemStack createTerraRelay() {
        ItemStack stack = new ItemStack(Items.MOSS_BLOCK);
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("terra_relay", (byte) 1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Terra Relay").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(""),
                Component.literal("◆ LEGENDARY — EARTH NODE ◆").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                Component.literal(""),
                Component.literal("Right-click: Root Drive").withStyle(ChatFormatting.AQUA),
                Component.literal("  └ Haste + small regen").withStyle(ChatFormatting.GRAY),
                Component.literal("  └ Consumes 1 Dirt").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal("  └ Drawback: brief fatigue").withStyle(ChatFormatting.DARK_RED),
                Component.literal(""),
                Component.literal("「Unique」").withStyle(ChatFormatting.GREEN)
        )));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        SlayerItems.setCustomItemId(stack, "terra_relay");
        return stack;
    }

    /** @deprecated Use {@link #createHPEBM(int)} with mk=1 instead. */
    @Deprecated
    public static ItemStack createHPEBM() {
        return createHPEBM(1);
    }

    public static ItemStack createWardenCore() {
        ItemStack stack = new ItemStack(Items.ECHO_SHARD);
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("warden_core", (byte) 1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Warden's Core").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(""),
                Component.literal("◆ ANCIENT ARTIFACT ◆").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD),
                Component.literal(""),
                Component.literal("The pulsating core of the Deep Dark's guardian").withStyle(ChatFormatting.AQUA),
                Component.literal("Pried from the bone cold chest of the Warden.").withStyle(ChatFormatting.AQUA),
                Component.literal(""),
                Component.literal("Drop Rate: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("0.1%").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                        .append(Component.literal(" from Wardens").withStyle(ChatFormatting.GRAY)),
                Component.literal(""),
                Component.literal("Used to craft Ultra weapons").withStyle(ChatFormatting.LIGHT_PURPLE),
                Component.literal(""),
                Component.literal("[MFLUX: CRD-006 specimen core | Anomalous biological residue]").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal("「Crafting Material」").withStyle(ChatFormatting.DARK_AQUA)
        )));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }

    public static ItemStack createUltraOverclockedBeam() {
        ItemStack beam = new ItemStack(Items.GOLDEN_SHOVEL);
        beam.set(DataComponents.CUSTOM_NAME,
                Component.literal("HPEBM Mk.VI — Ultra Overclocked")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        beam.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("").withStyle(ChatFormatting.DARK_PURPLE),
                Component.literal("◆ MYTHIC — Beyond Classification ◆").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                Component.literal(""),
                Component.literal("This unit operates beyond all known safety").withStyle(ChatFormatting.GRAY),
                Component.literal("parameters. Its containment field has been").withStyle(ChatFormatting.GRAY),
                Component.literal("removed entirely. Catastrophic by design.").withStyle(ChatFormatting.GRAY),
                Component.literal("Only one is known to exist.").withStyle(ChatFormatting.DARK_GRAY),
                Component.literal(""),
                Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_PURPLE),
                Component.literal("⚡ Damage/tick: ").withStyle(ChatFormatting.WHITE)
                        .append(Component.literal("6.0").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)),
                Component.literal("ðŸ’¡ XP Cost: ").withStyle(ChatFormatting.WHITE)
                        .append(Component.literal("5 levels/sec").withStyle(ChatFormatting.YELLOW)),
                Component.literal("ðŸ“¡ Beam Range: ").withStyle(ChatFormatting.WHITE)
                        .append(Component.literal("30 blocks").withStyle(ChatFormatting.AQUA)),
                Component.literal("ðŸ’¥ Endpoint Explosion: ").withStyle(ChatFormatting.WHITE)
                        .append(Component.literal("10 dmg, 3.0 radius").withStyle(ChatFormatting.GOLD)),
                Component.literal(""),
                Component.literal("Right-click: Devastating beam (hold)").withStyle(ChatFormatting.RED),
                Component.literal("Left-click: Sonic Devastation").withStyle(ChatFormatting.DARK_PURPLE),
                Component.literal("  └ AOE devastation, 10s cooldown").withStyle(ChatFormatting.GRAY),
                Component.literal(""),
                Component.literal("「Tier VI — Mythic」").withStyle(ChatFormatting.DARK_PURPLE),
                Component.literal("[MFLUX: Mk.VI beyond-class weapon | Containment VOID]").withStyle(ChatFormatting.DARK_GRAY)
        )));
        beam.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        beam.set(DataComponents.MAX_DAMAGE, Integer.MAX_VALUE);
        beam.set(DataComponents.DAMAGE, 0);
        return beam;
    }

    public static ItemStack createUltraBeam() {
        ItemStack stack = new ItemStack(Items.IRON_SHOVEL);
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("ultra_beam", (byte) 1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Ultra Beam Emitter").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(""),
                Component.literal("◆ ENHANCED WEAPON ◆").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                Component.literal(""),
                Component.literal("Ultra High Powered Energy Beam").withStyle(ChatFormatting.LIGHT_PURPLE),
                Component.literal(""),
                Component.literal("Right-click: Continuous beam attack").withStyle(ChatFormatting.RED),
                Component.literal("  └ Costs 1 XP level per second").withStyle(ChatFormatting.GRAY),
                Component.literal(""),
                Component.literal("Damage: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("3.0").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                        .append(Component.literal(" per tick (+200%)").withStyle(ChatFormatting.GREEN)),
                Component.literal(""),
                Component.literal("「Tier II」").withStyle(ChatFormatting.DARK_PURPLE)
        )));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        stack.set(DataComponents.MAX_DAMAGE, Integer.MAX_VALUE);
        stack.set(DataComponents.DAMAGE, 0);
        return stack;
    }

    public static ItemStack createUltraBeamMk(int mk) {
        if (mk < 1 || mk > 5) return createUltraBeam();

        ItemStack stack = new ItemStack(Items.IRON_SHOVEL);
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("ultra_beam_mk" + mk, (byte) 1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

        ChatFormatting color = switch (mk) {
            case 1 -> ChatFormatting.LIGHT_PURPLE;
            case 2 -> ChatFormatting.DARK_PURPLE;
            case 3 -> ChatFormatting.BLUE;
            case 4 -> ChatFormatting.DARK_BLUE;
            case 5 -> ChatFormatting.GOLD;
            default -> ChatFormatting.LIGHT_PURPLE;
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

        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Ultra Beam Emitter Mk" + mk + suffix).withStyle(color, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ " + titleSuffix + " ◆").withStyle(color, ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Overclocked Energy Beam Module").withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Right-click: Continuous beam attack").withStyle(ChatFormatting.RED));
        lore.add(Component.literal("  └ Costs " + xpCost + " XP level(s) per second").withStyle(ChatFormatting.GRAY));

        if (mk >= 3) {
            float explosionDmg = switch (mk) {
                case 3 -> 4.0f;
                case 4 -> 6.0f;
                case 5 -> 10.0f;
                default -> 4.0f;
            };
            lore.add(Component.literal("  └ Explosive impact").withStyle(ChatFormatting.GOLD));
            lore.add(Component.literal(""));
            lore.add(Component.literal("Damage: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.1f", baseDamage)).withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .append(Component.literal(" per tick (+" + damageBonus + "%)").withStyle(ChatFormatting.GREEN)));
            lore.add(Component.literal("Explosion: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.1f", explosionDmg)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
        } else {
            lore.add(Component.literal(""));
            lore.add(Component.literal("Damage: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.1f", baseDamage)).withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .append(Component.literal(" per tick (+" + damageBonus + "%)").withStyle(ChatFormatting.GREEN)));
        }

        lore.add(Component.literal(""));

        if (mk == 5) {
            lore.add(Component.literal("★ MAXIMUM POWER ACHIEVED ★").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            lore.add(Component.literal(""));
        }

        lore.add(Component.literal("「Tier " + tierRoman + "」").withStyle(color));

        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        stack.set(DataComponents.MAX_DAMAGE, Integer.MAX_VALUE);
        stack.set(DataComponents.DAMAGE, 0);

        return stack;
    }

    private static ItemStack createCustomItem(Item baseItem, String tagName, String displayName, ChatFormatting color) {
        ItemStack stack = new ItemStack(baseItem);
        CompoundTag nbt = new CompoundTag();
        nbt.putByte(tagName, (byte) 1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(displayName).withStyle(color, ChatFormatting.BOLD));
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

    public static boolean useUltraOverclockedAbility(ServerPlayer player, ItemStack stack) {
        if (getBeamTier(stack) != 7) return false;

        // Check cooldown
        if (player.getCooldowns().isOnCooldown(stack)) {
            return false;
        }

        // Check for dragon's breath (only requirement now)
        ItemStack dragonBreath = findItemInInventory(player, Items.DRAGON_BREATH);
        if (dragonBreath == null || dragonBreath.isEmpty()) {
            player.sendSystemMessage(Component.literal("Requires Dragon's Breath!")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        // Consume dragon's breath
        dragonBreath.shrink(1);

        // Set 15 second cooldown (300 ticks)
        player.getCooldowns().addCooldown(stack, 200);

        ServerLevel world = player.level();
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0f);
        Vec3 endPos = eyePos.add(lookVec.scale(80));

        net.minecraft.world.phys.HitResult hitResult = world.clip(new ClipContext(
                eyePos, endPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        Vec3 impactPos = (hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK)
                ? hitResult.getLocation() : endPos;

        // Spawn beam particles along the path
        double distance = eyePos.distanceTo(impactPos);
        int particleCount = (int) (distance * 3);

        for (int i = 0; i < particleCount; i++) {
            double t = (double) i / particleCount;
            double x = eyePos.x + (impactPos.x - eyePos.x) * t;
            double y = eyePos.y + (impactPos.y - eyePos.y) * t;
            double z = eyePos.z + (impactPos.z - eyePos.z) * t;

            ((ServerLevel) world).sendParticles(ParticleTypes.SONIC_BOOM, x, y, z, 1, 0, 0, 0, 0);

            if (i % 2 == 0) {
                ((ServerLevel) world).sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 2, 0.05, 0.05, 0.05, 0.01);
                ((ServerLevel) world).sendParticles(ParticleTypes.GLOW, x, y, z, 1, 0.05, 0.05, 0.05, 0.01);
                ((ServerLevel) world).sendParticles(ParticleTypes.WAX_OFF, x, y, z, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }

        // Play sonic boom sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.0f, 0.8f);

        // Explosion at impact
        world.explode(player, impactPos.x, impactPos.y, impactPos.z,
                8.0f, true, net.minecraft.world.level.Level.ExplosionInteraction.MOB);

        // Impact particles
        ((ServerLevel) world).sendParticles(ParticleTypes.EXPLOSION_EMITTER, impactPos.x, impactPos.y, impactPos.z, 3, 0.8, 0.8, 0.8, 0);
        ((ServerLevel) world).sendParticles(ParticleTypes.SOUL, impactPos.x, impactPos.y, impactPos.z, 5, 1, 1, 1, 0.1);
        ((ServerLevel) world).sendParticles(ParticleTypes.SCULK_SOUL, impactPos.x, impactPos.y, impactPos.z, 5, 1, 1, 1, 0.05);

        // Damage nearby entities at impact
        List<LivingEntity> nearby = world.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(impactPos.x - 6, impactPos.y - 6, impactPos.z - 6,
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
            entity.hurtServer(world, world.damageSources().sonicBoom(player), damage);
        }

        player.sendSystemMessage(Component.literal("\u26A1 SONIC DEVASTATION \u26A1")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));

        return true;
    }

    // Helper method to find item in inventory
    private static ItemStack findItemInInventory(ServerPlayer player, net.minecraft.world.item.Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                return stack;
            }
        }
        return null;
    }

    public static int getUltraOverclockedCooldownSeconds(ServerPlayer player) {
        UUID uuid = player.getUUID();
        long now = Clock.gameMillis();
        long lastUse = ultraOverclockedCooldowns.getOrDefault(uuid, 0L);
        long remaining = ULTRA_OVERCLOCKED_COOLDOWN_MS - (now - lastUse);

        if (remaining <= 0) return 0;
        return (int) Math.ceil(remaining / 1000.0);
    }
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// BEAM TIER SYSTEM
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    public static int getBeamTier(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        if (!stack.is(Items.IRON_SHOVEL) && !stack.is(Items.GOLDEN_SHOVEL)) return 0;

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
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            Component nameText = stack.get(DataComponents.CUSTOM_NAME);
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
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag nbt = customData.copyTag();
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
        if (!stack.is(Items.IRON_SHOVEL) && !stack.is(Items.GOLDEN_SHOVEL)) return false;

        if (stack.has(DataComponents.CUSTOM_NAME)) {
            String name = stack.get(DataComponents.CUSTOM_NAME).getString();
            return name.contains("HPEBM") ||
                    name.contains("H.P.E.B.M.") ||
                    name.contains("Plasma Emitter") ||
                    name.contains("Ultra Overclocked") ||
                    name.contains("Ultra Beam");
        }

        // Fallback: check NBT for legacy
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag nbt = customData.copyTag();
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
    public static void tickSlimeBoots(ServerPlayer player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!SlayerItems.isSlimeBoots(boots)) {
            // Remove shrink effect if no longer wearing boots
            if (slimeBootsShrunk.contains(player.getUUID())) {
                player.refreshDimensions();
                slimeBootsShrunk.remove(player.getUUID());
            }
            return;
        }

        // Check level requirement
        if (!SlayerItems.canUseSlimeBoots(player)) {
            sendLevelWarning(player, "Gelatinous Rustler Boots",
                    SlayerItems.SLIME_BOOTS_LEVEL_REQ, "Slime");
            return;
        }

        // Jump Boost III (buffed from II â†’ III for Item #6)
        player.addEffect(new MobEffectInstance(
                MobEffects.JUMP_BOOST, 40, 2, true, false, false));

        // Check if shrunk state should end
        UUID uuid = player.getUUID();
        if (slimeBootsActiveUntil.containsKey(uuid)) {
            if (Clock.gameMillis() > slimeBootsActiveUntil.get(uuid)) {
                // Time's up - return to normal size
                player.refreshDimensions();
                slimeBootsShrunk.remove(uuid);
                slimeBootsActiveUntil.remove(uuid);
                player.sendSystemMessage(Component.literal("Slime form ended - returned to normal size!")
                        .withStyle(ChatFormatting.GREEN));
            }
        }
    }

    // Called when player takes fatal damage - returns true if death should be cancelled
    public static boolean trySlimeBootsDeathSave(ServerPlayer player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!SlayerItems.isSlimeBoots(boots)) return false;
        if (!SlayerItems.canUseSlimeBoots(player)) return false;

        UUID uuid = player.getUUID();
        long now = Clock.gameMillis();

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
        ServerLevel world = player.level();

        // Spawn 2 small slimes
        for (int i = 0; i < 2; i++) {
            net.minecraft.world.entity.monster.cubemob.Slime slime = net.minecraft.world.entity.EntityTypes.SLIME.create(world, EntitySpawnReason.TRIGGERED);
            if (slime != null) {
                slime.setSize(1, true); // Small slime
                slime.setPos(player.getX() + (i - 0.5), player.getY(), player.getZ());
                world.addFreshEntity(slime);
            }
        }

        // Effects
        world.playSound(null, player.blockPosition(), SoundEvents.SLIME_SQUISH,
                SoundSource.PLAYERS, 1.0f, 1.5f);
        ((ServerLevel) world).sendParticles(ParticleTypes.ITEM_SLIME,
                player.getX(), player.getY() + 1, player.getZ(), 5, 0.5, 0.5, 0.5, 0.1);

        player.sendSystemMessage(Component.literal("\u2620 DEATH SAVED! You have 30 seconds at half size!")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("\u26A0 If you die now, it's permanent!")
                .withStyle(ChatFormatting.RED));

        return true; // Cancel death
    }

    // ============================================================
// WARDEN CHESTPLATE TICK - Extra Health, Echo Location
// ============================================================
    public static void tickSpiderLeggings(ServerPlayer player) {
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        if (!SlayerItems.isSpiderLeggings(leggings)) return;

        if (!SlayerItems.canUseSpiderLeggings(player)) {
            return;
        }

        // Delegate to ArmorAbilityHandler which has the full venomous leggings implementation
        ArmorAbilityHandler.tickVenomousLeggings(player);

        // Wall climbing: when sneaking and touching a wall, apply levitation to simulate climbing
        if (player.isShiftKeyDown() && !player.onGround() && player.horizontalCollision) {
            net.minecraft.world.phys.Vec3 vel = player.getDeltaMovement();
            if (vel.y < 0) {
                player.setDeltaMovement(vel.x, 0.1, vel.z);
            }
        }
    }

    public static void tickWardenChestplate(ServerPlayer player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!SlayerItems.isWardenChestplate(chestplate)) return;

        if (!SlayerItems.canUseWardenChestplate(player)) {
            return;
        }

        // Extra health (+20 hearts = Health Boost X)
        player.addEffect(new MobEffectInstance(
                MobEffects.HEALTH_BOOST, 40, 9, true, false, false));

        // Resistance I for tankiness
        player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE, 40, 0, true, false, false));

        // Night Vision (permanent while worn)
        MobEffectInstance currentNightVision = player.getEffect(MobEffects.NIGHT_VISION);
        if (currentNightVision == null || currentNightVision.getDuration() < 220) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION, 400, 0, true, false, false));
        }

        // Darkness immunity (suppressed during Ender Phase Mode which intentionally applies Darkness)
        if (player.hasEffect(MobEffects.DARKNESS) && !isInEnderPhaseMode(player.getUUID())) {
            player.removeEffect(MobEffects.DARKNESS);
        }

        // ESP: Glow effect on nearby moving players (every 2 seconds) — 24 block range
        if (player.tickCount % 40 == 0) {
            ServerLevel world = player.level();
            double radius = 24.0;

            for (ServerPlayer nearby : world.players()) {
                if (nearby == player) continue;
                if (nearby.distanceToSqr(player) > radius * radius) continue;

                // Check if player is moving or making sound
                Vec3 velocity = nearby.getDeltaMovement();
                boolean isMoving = velocity.horizontalDistanceSqr() > 0.003;
                boolean isSprinting = nearby.isSprinting();
                boolean isSneaking = nearby.isShiftKeyDown();

                // Sneaking players are harder to detect
                if (isSneaking) continue;

                if (isMoving || isSprinting) {
                    // Apply brief glowing effect (1.5 seconds)
                    nearby.addEffect(new MobEffectInstance(
                            MobEffects.GLOWING, 30, 0, true, false, false));
                }
            }
        }
    }

    // ============================================================
// SKELETON BOW - Instant-fire on left-click (swing detection)
// ============================================================
    public static boolean isSkeletonBowEquipped(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return SlayerItems.isSkeletonBow(mainHand) || SlayerItems.isSkeletonBow(offHand);
    }

    public static void tickSkeletonBow(ServerPlayer player) {
        // Detect left-clicks in the air (no block/entity target) via hand swing
        SkeletonBowHandler.tickSwingDetection(player);
    }

    // ============================================================
// LEVEL WARNING HELPER (prevents spam)
// ============================================================
    public static void sendLevelWarning(ServerPlayer player, String itemName, int reqLevel, String slayerType) {
        UUID uuid = player.getUUID();
        long now = Clock.gameMillis();

        // Only warn once per 5 seconds
        if (levelWarningCooldowns.containsKey(uuid)) {
            if (now - levelWarningCooldowns.get(uuid) < 5000) {
                return;
            }
        }

        levelWarningCooldowns.put(uuid, now);
        player.sendSystemMessage(Component.literal("\u26A0 " + itemName + " requires " + slayerType + " Bounty Level " + reqLevel + "!")
                .withStyle(ChatFormatting.RED));
    }

    // ============================================================
// PREVENT BOUNTY ITEMS FROM BEING USED/EATEN
// ============================================================
    public static boolean shouldBlockItemUse(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null) return false;

        String nameStr = name.getString();
        return nameStr.contains("Core") ||
                nameStr.contains("Bounty Sword") ||
                nameStr.contains("Slayer Sword");
    }
}
