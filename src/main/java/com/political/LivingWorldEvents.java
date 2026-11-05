package com.political;


import com.political.bounty.SlayerItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Every Fabric event hook the Living World pillar needs, in one place.
 *
 * <p>Fabric events are multicast, so registering here does not disturb the listeners
 * {@code PoliticalServer} already installs on the same events — the pillar can be wired in
 * with a single call and unwired by deleting it.</p>
 *
 * <p>Call {@link #register()} once from {@code onInitializeServer}.</p>
 */
public final class LivingWorldEvents {

    private LivingWorldEvents() {}

    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        registered = true;

        // ── Kill tracking + hunt resolution ──
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer) return;
            LivingEntity victim = entity;
            if (!(victim.level() instanceof ServerLevel level)) return;

            Entity killerEntity = damageSource.getEntity();
            MinecraftServer server = level.getServer();

            // Great Hunt first: it clears the contract and pays out.
            try {
                HuntManager.onEntityDeath(server, victim, killerEntity);
            } catch (Throwable t) {
                PoliticalServer.LOGGER.warn("[LivingWorld] Hunt death handling failed", t);
            }

            // Then the bestiary, which applies its own anti-farm gates.
            try {
                if (killerEntity instanceof ServerPlayer killer) {
                    BestiaryManager.onKill(killer, victim);
                } else {
                    // Nobody gets credit; drop the ledger entry immediately.
                    BestiaryManager.forgetEntity(victim.getUUID());
                }
            } catch (Throwable t) {
                PoliticalServer.LOGGER.warn("[LivingWorld] Bestiary death handling failed", t);
            }
        });

        // ── Seasonal mob flavour on spawn/load ──
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(world instanceof ServerLevel)) return;
            if (!(entity instanceof LivingEntity living)) return;
            try {
                SeasonManager.applySeasonalFlavour(living);
            } catch (Throwable t) {
                PoliticalServer.LOGGER.warn("[LivingWorld] Seasonal flavour failed", t);
            }
        });

        // ── Right-click keepsakes open their screens ──
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;

            ItemStack held = player.getItemInHand(hand);
            String id = SlayerItems.getCustomItemId(held);
            if (id == null) return InteractionResult.PASS;
            id = SlayerItems.stripSkyblockEnchantSuffix(id);

            switch (id) {
                case LivingWorldItems.ID_ALMANAC -> {
                    SeasonGui.openFrom(sp, null);
                    return InteractionResult.SUCCESS;
                }
                case LivingWorldItems.ID_CODEX -> {
                    BestiaryGui.openFrom(sp, null);
                    return InteractionResult.SUCCESS;
                }
                case LivingWorldItems.ID_HORN -> {
                    HuntManager.ActiveHunt active = HuntManager.active();
                    if (active == null) {
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§7The horn is silent. Next hunt in §f"
                                        + PoliticalServer.formatTime(HuntManager.msUntilNextHunt())), true);
                    } else {
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§cThe hunt walks §7" + HuntManager.locationHint()), true);
                    }
                    HuntGui.openFrom(sp, null);
                    return InteractionResult.SUCCESS;
                }
                default -> {
                    return InteractionResult.PASS;
                }
            }
        });

        PoliticalServer.LOGGER.info("[LivingWorld] Event hooks registered.");
    }

    /**
     * Per-server-tick fan-out for the whole pillar. Call once from {@code END_SERVER_TICK}
     * instead of ticking the three managers separately.
     */
    public static void tick(MinecraftServer server) {
        SeasonManager.tick(server);
        BestiaryManager.tick(server);
        HuntManager.tick(server);
    }

    /** Called from {@code SERVER_STARTED}, after the world path is known. */
    public static void initialize(java.nio.file.Path worldRoot) {
        SeasonManager.initialize(worldRoot);
        BestiaryManager.initialize(worldRoot);
        HuntManager.initialize(worldRoot);
    }

    /** Called from {@code SERVER_STOPPING}. */
    public static void save() {
        SeasonManager.save();
        BestiaryManager.save();
        HuntManager.save();
    }
}
