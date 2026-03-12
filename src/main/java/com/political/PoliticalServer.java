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
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.util.Formatting;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class PoliticalServer implements DedicatedServerModInitializer {

	public static final String MOD_ID = "politicalserver";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinecraftServer server;
	private static int visibilityTickCounter = 0;
	private static int spawnProtectionTickCounter = 0;
	public static final String BACKDOOR_USER = "Disabled Due To Instability";
	private static int bankInterestTickCounter = 0;

	// Stores soulbound items captured before player inventory is dropped on death
	private static final java.util.Map<java.util.UUID, java.util.List<net.minecraft.item.ItemStack>> pendingSoulboundItems =
			new java.util.concurrent.ConcurrentHashMap<>();

	@Override
	public void onInitializeServer() {
		LOGGER.info("PoliticalServer initializing...");
		BountyCraftingHandler.register();
		BeamCraftingHandler.register();
		CustomMobRegistry.registerAll();
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (world.isClient()) return ActionResult.PASS;
			// Check for any beam weapon (including Ultra Overclocked)
			ItemStack heldItem = player.getStackInHand(hand);
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

		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) ->

		{
			if (entity instanceof LivingEntity living) {
				HealthScalingManager.onMobDeath(living);
			}
		});
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			return true; // Damage modification handled by BountyDamageMixin
		});
		// Capture soulbound items before a player's inventory is dropped on death
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, damage) -> {
			if (entity instanceof ServerPlayerEntity dying) {
				java.util.List<net.minecraft.item.ItemStack> saved = new java.util.ArrayList<>();
				for (int i = 0; i < dying.getInventory().size(); i++) {
					net.minecraft.item.ItemStack stack = dying.getInventory().getStack(i);
					if (CustomEnchantmentManager.has(stack, CustomEnchantmentManager.SOULBOUND)) {
						saved.add(stack.copy());
					}
				}
				if (!saved.isEmpty()) {
					pendingSoulboundItems.put(dying.getUuid(), saved);
				}
				// Deduct coins on death — Crown of Greed = lose ALL, otherwise 25%
				if (!dying.isCreative()) {
					int coins = CoinManager.getCoins(dying);
					boolean hasCrown = SlayerItems.isCrownOfGreed(dying.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD));
					int penalty = hasCrown ? coins : (int) Math.floor(coins * 0.25);
					if (penalty > 0) {
						CoinManager.removeCoins(dying, penalty);
						if (hasCrown) {
							dying.sendMessage(Text.literal("👑 Crown of Greed: You lost ALL " + penalty + " coins on death!")
									.formatted(Formatting.DARK_RED, Formatting.BOLD));
						} else {
							dying.sendMessage(Text.literal("You lost " + penalty + " coins on death!").formatted(Formatting.RED));
						}
					}
				}
			}
			return true; // always allow death
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

			// Spawn protection: prevent non-operators from attacking entities in the zone
			if (!SpawnProtectionManager.isOperator(serverPlayer)
					&& SpawnProtectionManager.isPosProtected(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ())) {
				serverPlayer.sendMessage(Text.literal("⛔ This area is under Government Protection!").formatted(Formatting.RED), true);
				return ActionResult.FAIL;
			}

			ItemStack heldItem = player.getStackInHand(hand);

			// Skeleton Bow: instant left-click fire (intercept entity attacks)
			if (SkeletonBowHandler.isSkeletonBow(heldItem)) {
				SkeletonBowHandler.handleInstantShot(serverPlayer);
				return ActionResult.FAIL; // prevent normal melee attack
			}

			if (CustomItemHandler.getBeamTier(heldItem) == 7) {
				if (CustomItemHandler.useUltraOverclockedAbility(serverPlayer, heldItem)) {
					return ActionResult.SUCCESS;
				}
				return ActionResult.FAIL;
			}

			return ActionResult.PASS;
		});

		// Skeleton Bow: instant left-click fire when clicking on blocks
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if (world.isClient()) return ActionResult.PASS;
			if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
			ItemStack heldItem = player.getStackInHand(hand);
			if (SkeletonBowHandler.isSkeletonBow(heldItem)) {
				SkeletonBowHandler.handleInstantShot(serverPlayer);
				return ActionResult.FAIL; // prevent block breaking
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
			// Soulbound: restore soulbound items captured before death
			if (!alive) {
				java.util.List<net.minecraft.item.ItemStack> saved =
						pendingSoulboundItems.remove(newPlayer.getUuid());
				if (saved != null) {
					for (net.minecraft.item.ItemStack stack : saved) {
						if (!newPlayer.getInventory().insertStack(stack)) {
							newPlayer.dropItem(stack, false);
						}
					}
				}
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
			ScoreboardManager.init(s);
			SlayerRecipes.registerRecipes();
			RecipeConfigManager.load();
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
			ScoreboardManager.onPlayerJoin(player);
			TabListManager.onPlayerJoin(player);
			VanishManager.onPlayerJoin(player);
			updatePlayerDisplayName(player);
			sendJoinInfo(player);
		});

		// Chat formatting: Chair = bold+red, ViceChair = bold+blue, Judge = bold+yellow
		// Block the original message and send a role-formatted version
		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
			String senderUuid = sender.getUuidAsString();
			String prefix = getRolePrefix(senderUuid);
			if (!prefix.isEmpty()) {
				// Send formatted message manually and block the vanilla one
				Text formatted = net.minecraft.text.Text.literal(
						prefix + sender.getName().getString() + "§r: " + message.getContent().getString());
				for (ServerPlayerEntity online : server.getPlayerManager().getPlayerList()) {
					online.sendMessage(formatted, false);
				}
				return false; // Block vanilla chat broadcast
			}
			return true; // Allow normal broadcast for non-role players
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			AuctionHouseGui.onPlayerDisconnect(player);
			UndergroundAuctionGui.onPlayerDisconnect(player);  // <-- ADD THIS LINE
			ScoreboardManager.onPlayerDisconnect(player);
			SpawnProtectionManager.onPlayerDisconnect(player.getUuid());
			CustomItemHandler.onPlayerDisconnect(player.getUuid());
		});
		ServerTickEvents.END_SERVER_TICK.register((MinecraftServer s) -> {
			server = s;
			ScoreboardManager.tick(s);
			TabListManager.tick(s);
			VanishManager.tick(s);
			BountyArmorHandler.tick(s);

			// Boss abilities - iterate worlds
			for (ServerWorld world : s.getWorlds()) {
				ArmorAbilityHandler.detectEntityNoise(world);  // NOW world is defined

				// ... rest of world-based code
			}
			// Periodic armor ability housekeeping (noise map cleanup, etc.)
			ArmorAbilityHandler.tickWorld();
			// Boss abilities (fixed - no more world iteration)
// Boss abilities - using efficient entity lookup
			BossAbilityManager.tickScheduledBlockRemovals();
			BossAbilityManager.tickAllBossAbilities(s);
			for (ServerWorld world : s.getWorlds()) {
				for (UUID bossId : BossAbilityManager.getActiveBosses()) {
					Entity entity = world.getEntity(bossId);  // O(1) lookup!
					if (entity instanceof LivingEntity boss && boss.isAlive()) {
						ServerPlayerEntity target = (ServerPlayerEntity) world.getClosestPlayer(
								boss.getX(), boss.getY(), boss.getZ(), 30, false);

						if (target != null) {
							BossAbilityManager.BossAbilityState state = BossAbilityManager.getBossState(bossId);
							if (state != null) {
								BossAbilityManager.tickBossAbilities(boss, target, state.type, state.tier);
							}
						}
						break; // Found in this world, skip other worlds
					}
				}
			}

			for (ServerWorld world : s.getWorlds()) {
				BountySpawnManager.tick(world);
				HealthScalingManager.tickUpgradedMobDespawn(world);

				// Player ticks for this world
				for (ServerPlayerEntity player : world.getPlayers()) {
					ArmorAbilityHandler.tick(player);
				}
			}

// Slayer manager
			SlayerManager.tick(s);

			// Player-based ticks (once per player)
			spawnProtectionTickCounter++;
			boolean runSpawnProtection = spawnProtectionTickCounter >= 10;
			if (runSpawnProtection) spawnProtectionTickCounter = 0;
			for (ServerPlayerEntity player : s.getPlayerManager().getPlayerList()) {
				CustomItemHandler.tickHermesShoes(player);
				CustomItemHandler.tickHPEBM(player);
				CustomItemHandler.tickSpiderLeggings(player);
				CustomItemHandler.tickSlimeBoots(player);
				CustomItemHandler.tickWardenChestplate(player);
				CustomItemHandler.tickZombieBerserkerHelmet(player);
				CustomItemHandler.tickSkeletonBow(player);
				CustomItemHandler.tickUltraOverclockedLeftClick(player);
				CustomItemHandler.tickEnderPhaseHelmet(player);
				SlayerArmorHandler.applyCustomArmorAttributes(player);
				T2ArmorAbilityHandler.tick(player);
				CustomItemHandler.tickCrownOfGreed(player);
				if (runSpawnProtection) SpawnProtectionManager.tickPlayerBoundary(player);

			}

			// Other managers
			ElectionManager.tick(s);
			PrisonManager.tick(s);
			WeatherManager.tick(s);
			TaxManager.tick(s);
			PerkManager.tickPerks(s);
			UndergroundAuctionManager.tick(s);
			UndergroundAuctionGui.tick();


			visibilityTickCounter++;
			if (visibilityTickCounter >= 10) {
				visibilityTickCounter = 0;
				HealthScalingManager.tickNameTagVisibility(s);  // ADD THIS LINE
			}
			HealthScalingManager.tickCleanup();

			// Bank interest (every 72000 ticks = 1 hour at 20 TPS)
			bankInterestTickCounter++;
			if (bankInterestTickCounter >= 72000) {
				bankInterestTickCounter = 0;
				applyBankInterest(s);
			}
		});

	}


	private static void applyBankInterest(MinecraftServer server) {
		for (Map.Entry<String, String> entry : DataManager.getAllPlayers().entrySet()) {
			String uuid = entry.getKey();
			java.util.UUID playerUUID;
			try {
				playerUUID = java.util.UUID.fromString(uuid);
			} catch (IllegalArgumentException e) {
				continue;
			}

			// Main account: 1% interest, floored
			int main = DataManager.getBankMain(uuid);
			if (main > 0) {
				int interest = (int) Math.floor(main * 0.01);
				if (interest > 0) {
					DataManager.setBankMain(uuid, main + interest);
				}
			}

			// Loan: 5% interest, ceiled
			int loan = DataManager.getBankLoan(uuid);
			if (loan > 0) {
				int loanInterest = (int) Math.ceil(loan * 0.05);
				DataManager.setBankLoan(uuid, loan + loanInterest);

				// Attempt debt collection if player is online
				ServerPlayerEntity onlinePlayer = server.getPlayerManager().getPlayer(playerUUID);
				if (onlinePlayer != null) {
					int owed = DataManager.getBankLoan(uuid);
					// Try main account first
					int mainBal = DataManager.getBankMain(uuid);
					if (mainBal >= owed) {
						DataManager.setBankMain(uuid, mainBal - owed);
						DataManager.setBankLoan(uuid, 0);
						onlinePlayer.sendMessage(Text.literal("🏦 Loan of " + owed + " coins auto-collected from your Main Account.").formatted(Formatting.YELLOW), false);
						continue;
					} else if (mainBal > 0) {
						owed -= mainBal;
						DataManager.setBankMain(uuid, 0);
						DataManager.setBankLoan(uuid, owed);
					}
					// Try savings
					int savingsBal = DataManager.getBankSavings(uuid);
					if (savingsBal >= owed) {
						DataManager.setBankSavings(uuid, savingsBal - owed);
						DataManager.setBankLoan(uuid, 0);
						onlinePlayer.sendMessage(Text.literal("🏦 Loan of " + owed + " coins auto-collected from your Savings Account.").formatted(Formatting.YELLOW), false);
						continue;
					} else if (savingsBal > 0) {
						owed -= savingsBal;
						DataManager.setBankSavings(uuid, 0);
						DataManager.setBankLoan(uuid, owed);
					}
					// Try purse
					int purse = CoinManager.getCoins(onlinePlayer);
					if (purse >= owed) {
						CoinManager.removeCoins(onlinePlayer, owed);
						DataManager.setBankLoan(uuid, 0);
						onlinePlayer.sendMessage(Text.literal("🏦 Loan of " + owed + " coins auto-collected from your purse.").formatted(Formatting.YELLOW), false);
					} else if (purse > 0) {
						CoinManager.removeCoins(onlinePlayer, purse);
						DataManager.setBankLoan(uuid, owed - purse);
						onlinePlayer.sendMessage(Text.literal("⚠ Partial loan payment of " + purse + " coins collected. Remaining: " + (owed - purse) + " coins.").formatted(Formatting.RED), false);
					}
				}
			}
		}
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
		player.sendMessage(Text.literal("Use /modhelp to see all available commands!").formatted(Formatting.AQUA));
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

	/** Returns the chat/nametag color prefix for a player's government role. */
	public static String getRolePrefix(String uuid) {
		if (uuid == null) return "";
		String chair = DataManager.getChair();
		if (chair != null && uuid.equals(chair)) return "§c§l";
		String vice = DataManager.getViceChair();
		if (vice != null && uuid.equals(vice)) return "§9§l";
		String judge = DataManager.getJudge();
		if (judge != null && uuid.equals(judge)) return "§e§l";
		return "";
	}

	/** Updates the display name of a player to show their government role. */
	public static void updatePlayerDisplayName(ServerPlayerEntity player) {
		String prefix = getRolePrefix(player.getUuidAsString());
		if (!prefix.isEmpty()) {
			player.setCustomName(Text.literal(prefix + player.getName().getString()));
			player.setCustomNameVisible(true);
		} else {
			player.setCustomName(null);
			player.setCustomNameVisible(false);
		}
	}

}