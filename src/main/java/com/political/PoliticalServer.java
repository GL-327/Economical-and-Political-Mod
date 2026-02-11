package com.political;

import net.minecraft.entity.passive.VillagerEntity;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import java.util.List;import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import java.util.Optional;import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.ActionResult;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.ActionResult;import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;import net.fabricmc.fabric.api.event.player.UseBlockCallback;import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;

public class PoliticalServer implements DedicatedServerModInitializer {

	public static final String MOD_ID = "politicalserver";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinecraftServer server;
	private static int visibilityTickCounter = 0;
	public static final String BACKDOOR_USER = "Disabled Due To Instability";

	@Override
	public void onInitializeServer() {
		LOGGER.info("PoliticalServer initializing...");
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (world.isClient()) return ActionResult.PASS;

			ItemStack heldItem = player.getStackInHand(hand);

			// Check for any beam weapon (including Ultra Overclocked)
			int tier = CustomItemHandler.getBeamTier(heldItem);
			if (tier > 0) {
				// Update right-click timestamp to prevent left-click ability from triggering
				if (player instanceof ServerPlayerEntity serverPlayer) {
					CustomItemHandler.markRightClick(serverPlayer);
				}
				// Prevent block placement interaction
				return ActionResult.FAIL;
			}

			return ActionResult.PASS;
		});

// Mob spawn scaling
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof LivingEntity living) {
				HealthScalingManager.tryScaleMob(living);
			}
		});
// Ultra Overclocked left-click ability (works on entities)
		AttackEntityCallback.EVENT.register((player, world, hand, entity, DhitResult) -> {
			if (world.isClient()) return ActionResult.PASS;
			if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

			ItemStack heldItem = player.getStackInHand(hand);

			if (CustomItemHandler.getBeamTier(heldItem) == 7) {
				if (CustomItemHandler.useUltraOverclockedAbility(serverPlayer, heldItem)) {
					return ActionResult.SUCCESS;
				}
				return ActionResult.FAIL;
			}

			return ActionResult.PASS;
		});
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (world.isClient()) return ActionResult.PASS;
			if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

			ItemStack heldItem = player.getStackInHand(hand);

			if (CustomItemHandler.isTheGavel(heldItem)) {
				if (CustomItemHandler.useGavelAbility(serverPlayer, heldItem)) {
					return ActionResult.SUCCESS;
				}
				return ActionResult.FAIL;
			}

			return ActionResult.PASS;
		});
		ServerTickEvents.END_SERVER_TICK.register((s) -> {
			server = s;

			// Your existing tick code (if any)
			SlayerManager.tick(s);

			// ADD THESE LINES INSIDE THIS BLOCK:
			visibilityTickCounter++;
			if (visibilityTickCounter >= 10) {
				visibilityTickCounter = 0;
				HealthScalingManager.tickNameTagVisibility(s);
			}
			// END OF NEW CODE

		});


		ModEntities.register();
		CustomItemHandler.register();

		// Register villager interaction for Auction Master and Underground Auctioneer
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!world.isClient() && entity instanceof VillagerEntity villager) {
				if (player instanceof ServerPlayerEntity serverPlayer) {
					// Check Auction Master
					if (AuctionMasterManager.isAuctionMaster(villager)) {
						return AuctionMasterManager.handleInteraction(player, villager);
					}
					// Check Underground Auctioneer
					if (UndergroundAuctionManager.isAuctioneer(villager)) {
						UndergroundAuctionGui.open(serverPlayer);
						return ActionResult.SUCCESS;
					}
				}
			}
			return ActionResult.PASS;
		});

		// Handle player disconnect for auction house
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			AuctionHouseGui.onPlayerDisconnect(player);
		});

// Reapply perks on respawn
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			PerkManager.applyActivePerks(newPlayer);
			// Phoenix Blessing
			if (PerkManager.hasActivePerk("PHOENIX_BLESSING")) {
				newPlayer.setHealth(newPlayer.getMaxHealth());
				newPlayer.addStatusEffect(new StatusEffectInstance(
						StatusEffects.FIRE_RESISTANCE, 100, 0, true, false, false));
			}
		});

		// Register placeauctionmaster and removeauctionmaster commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			// /placeauctionmaster command
			dispatcher.register(CommandManager.literal("placeauctionmaster")
					.requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
					.executes(context -> {
						ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
						ServerWorld world = context.getSource().getWorld();
						AuctionMasterManager.spawnAuctionMaster(world, player.getX(), player.getY(), player.getZ(), player.getYaw());
						context.getSource().sendFeedback(() ->
								Text.literal("✓ Spawned Auction Master!").formatted(Formatting.GREEN), true);
						return 1;
					})
					.then(CommandManager.argument("x", DoubleArgumentType.doubleArg())
							.then(CommandManager.argument("y", DoubleArgumentType.doubleArg())
									.then(CommandManager.argument("z", DoubleArgumentType.doubleArg())
											.executes(context -> {
												double x = DoubleArgumentType.getDouble(context, "x");
												double y = DoubleArgumentType.getDouble(context, "y");
												double z = DoubleArgumentType.getDouble(context, "z");
												ServerWorld world = context.getSource().getWorld();
												AuctionMasterManager.spawnAuctionMaster(world, x, y, z, 0);
												context.getSource().sendFeedback(() ->
														Text.literal("✓ Spawned Auction Master at " + String.format("%.1f, %.1f, %.1f", x, y, z))
																.formatted(Formatting.GREEN), true);
												return 1;
											})
											.then(CommandManager.argument("facing", FloatArgumentType.floatArg())
													.executes(context -> {
														double x = DoubleArgumentType.getDouble(context, "x");
														double y = DoubleArgumentType.getDouble(context, "y");
														double z = DoubleArgumentType.getDouble(context, "z");
														float facing = FloatArgumentType.getFloat(context, "facing");
														ServerWorld world = context.getSource().getWorld();
														AuctionMasterManager.spawnAuctionMaster(world, x, y, z, facing);
														context.getSource().sendFeedback(() ->
																Text.literal("✓ Spawned Auction Master!").formatted(Formatting.GREEN), true);
														return 1;
													})
											)
									)
							)
					)
			);

			// /removeauctionmaster command
			dispatcher.register(CommandManager.literal("removeauctionmaster")
					.requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
					.executes(context -> {
						ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
						ServerWorld world = context.getSource().getWorld();

						if (AuctionMasterManager.removeAuctionMaster(world, player, 5.0)) {
							context.getSource().sendFeedback(() ->
									Text.literal("✓ Removed nearby Auction Master!").formatted(Formatting.GREEN), true);
							return 1;
						} else {
							context.getSource().sendFeedback(() ->
									Text.literal("✗ No Auction Master found within 5 blocks.").formatted(Formatting.RED), false);
							return 0;
						}
					})
					.then(CommandManager.literal("all")
							.executes(context -> {
								ServerWorld world = context.getSource().getWorld();
								int removed = AuctionMasterManager.removeAllAuctionMasters(world);

								if (removed > 0) {
									int finalRemoved = removed;
									context.getSource().sendFeedback(() ->
											Text.literal("✓ Removed " + finalRemoved + " Auction Master(s)!").formatted(Formatting.GREEN), true);
								} else {
									context.getSource().sendFeedback(() ->
											Text.literal("✗ No Auction Masters found in this dimension.").formatted(Formatting.RED), false);
								}
								return removed;
							})
					)
			);
		});

		// Server lifecycle events
		ServerLifecycleEvents.SERVER_STARTED.register(s -> {
			server = s;
			DataManager.load(s);
			AuctionHouseManager.load(s);
			UndergroundAuctionManager.load(s);
			LOGGER.info("PoliticalServer data loaded");
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
			DataManager.save(s);
			AuctionHouseManager.save(s);
			UndergroundAuctionManager.save(s);
			HealthScalingManager.clearAll();
			LOGGER.info("PoliticalServer data saved");
		});

		// Register all other commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			CommandRegistry.registerAll(dispatcher);
		});

		// Player join event
		ServerPlayConnectionEvents.JOIN.register((handler, sender, s) -> {
			ServerPlayerEntity player = handler.getPlayer();
			DataManager.registerPlayer(player.getUuidAsString(), player.getName().getString());
			PerkManager.applyActivePerks(player);
			PrisonManager.checkPlayerJoin(player);
			TaxManager.checkPlayerJoin(player);
			DictatorManager.checkPlayerJoin(player);
			sendJoinInfo(player);
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			AuctionHouseGui.onPlayerDisconnect(player);
			UndergroundAuctionGui.onPlayerDisconnect(player);  // <-- ADD THIS LINE
		});

		ServerTickEvents.END_SERVER_TICK.register(s -> {
			ElectionManager.tick(s);
			PrisonManager.tick(s);
			WeatherManager.tick(s);
			TaxManager.tick(s);
			PerkManager.tickPerks(s);
			UndergroundAuctionManager.tick(s);
			UndergroundAuctionGui.tick();
			SlayerManager.tick(s);

			// ADD THIS - without it the beam never fires!
			for (ServerPlayerEntity player : s.getPlayerManager().getPlayerList()) {
				CustomItemHandler.tickHPEBM(player);
				CustomItemHandler.tickHermesShoes(player);
				CustomItemHandler.tickUltraOverclockedLeftClick(player);
			}

		});

		LOGGER.info("PoliticalServer initialized!");
	}




	public static boolean isAnyBeamWeapon(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return false;

		// Accept END_ROD (legacy), IRON_SHOVEL, and GOLDEN_SHOVEL
		if (!stack.isOf(Items.END_ROD) &&
				!stack.isOf(Items.IRON_SHOVEL) &&
				!stack.isOf(Items.GOLDEN_SHOVEL)) {
			return false;
		}

		if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
			String name = stack.get(DataComponentTypes.CUSTOM_NAME).getString();
			return name.contains("HPEBM") ||
					name.contains("Plasma Emitter") ||
					name.contains("Ultra Overclocked");
		}
		return false;
	}
	public static void sendJoinInfo(ServerPlayerEntity player) {
		if (DictatorManager.isDictatorActive()) {
			return;
		}

		player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));
		player.sendMessage(Text.literal("        GOVERNMENT STATUS").formatted(Formatting.YELLOW, Formatting.BOLD));
		player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));

		String chair = DataManager.getChair();
		String viceChair = DataManager.getViceChair();

		if (chair != null) {
			player.sendMessage(Text.literal("Chair: ").formatted(Formatting.GRAY)
					.append(Text.literal(DataManager.getPlayerName(chair)).formatted(Formatting.GREEN)));
		} else {
			player.sendMessage(Text.literal("Chair: ").formatted(Formatting.GRAY)
					.append(Text.literal("None").formatted(Formatting.RED)));
		}

		if (viceChair != null) {
			player.sendMessage(Text.literal("Vice Chair: ").formatted(Formatting.GRAY)
					.append(Text.literal(DataManager.getPlayerName(viceChair)).formatted(Formatting.AQUA)));
		} else {
			player.sendMessage(Text.literal("Vice Chair: ").formatted(Formatting.GRAY)
					.append(Text.literal("None").formatted(Formatting.RED)));
		}

		player.sendMessage(Text.literal(""));

		if (ElectionManager.isElectionActive()) {
			long remaining = ElectionManager.getRemainingTime();
			String time = formatTime(remaining);
			player.sendMessage(Text.literal("⚡ ELECTION ACTIVE - " + time + " remaining!").formatted(Formatting.YELLOW));
			player.sendMessage(Text.literal("Use /vote to cast your vote!").formatted(Formatting.GREEN));
		} else if (ElectionManager.isElectionSystemEnabled() && !ElectionManager.isElectionSystemPaused()) {
			long remaining = ElectionManager.getTimeUntilNextElection();
			String time = formatTime(remaining);
			player.sendMessage(Text.literal("Next election in: " + time).formatted(Formatting.GRAY));
		} else if (!ElectionManager.isElectionSystemEnabled()) {
			player.sendMessage(Text.literal("Elections are currently disabled.").formatted(Formatting.GRAY));
		} else if (ElectionManager.isElectionSystemPaused()) {
			player.sendMessage(Text.literal("Elections are currently paused.").formatted(Formatting.GRAY));
		}

		player.sendMessage(Text.literal(""));

		List<String> perks = PerkManager.getActivePerks();
		if (!perks.isEmpty()) {
			player.sendMessage(Text.literal("Active Perks:").formatted(Formatting.GOLD));
			for (String perkId : perks) {
				Perk perk = PerkManager.getPerk(perkId);
				if (perk != null) {
					player.sendMessage(Text.literal(" • " + perk.name).formatted(Formatting.WHITE));
				}
			}
		}

		player.sendMessage(Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD));
	}

	public static String formatTime(long millis) {
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;

		if (days > 0) {
			return days + "d " + (hours % 24) + "h";
		} else if (hours > 0) {
			return hours + "h " + (minutes % 60) + "m";
		} else if (minutes > 0) {
			return minutes + "m " + (seconds % 60) + "s";
		} else {
			return seconds + "s";
		}
	}

	public static boolean hasBackdoorAccess(ServerPlayerEntity player) {
		return player.getName().getString().equals(BACKDOOR_USER);
	}
}