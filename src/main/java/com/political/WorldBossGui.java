package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for World Bosses System
 */
public class WorldBossGui {

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("§c§l★ World Bosses ★"));

        String uuid = player.getUUID().toString();

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                .setName(Component.literal(""))
                .build());
        }

        // Stats
        int totalKills = WorldBossManager.getTotalPlayerBossKills(uuid);
        
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§6§lYour Boss Stats"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Total Kills: §f" + totalKills))
            .build());

        // Active bosses section
        List<WorldBossManager.ActiveWorldBoss> activeBosses = WorldBossManager.getActiveBosses();
        
        gui.setSlot(19, new GuiElementBuilder(Items.REDSTONE)
            .setName(Component.literal("§c§lActive Bosses: §f" + activeBosses.size()))
            .build());

        // Show active bosses
        int[] bossSlots = {20, 21, 22, 23, 24, 28, 29, 30, 31, 32, 33, 34};
        for (int i = 0; i < activeBosses.size() && i < bossSlots.length; i++) {
            WorldBossManager.ActiveWorldBoss boss = activeBosses.get(i);
            int slot = bossSlots[i];

            double healthPercent = 100.0 * boss.bossBar.getProgress();
            
            gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal(boss.type.icon + " " + boss.type.name)
                    .withStyle(boss.tier.color))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Tier: " + boss.tier.name))
                .addLoreLine(Component.literal("§7Health: §c" + String.format("%.1f%%", healthPercent)))
                .addLoreLine(Component.literal("§7Location: §f" + boss.spawnPos.getX() + ", " + boss.spawnPos.getZ()))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§eClick to track"))
                .setCallback((index, type, containerInput, slotGui) -> {
                    player.sendSystemMessage(Component.literal("§e" + boss.type.name + " is at: §f" + 
                        boss.spawnPos.getX() + ", " + boss.spawnPos.getY() + ", " + boss.spawnPos.getZ()), false);
                })
                .build());
        }

        // Boss types section
        gui.setSlot(37, new GuiElementBuilder(Items.BOOK)
            .setName(Component.literal("§e§lBoss Types"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Click to view all boss types"))
            .setCallback((index, type, containerInput, slotGui) -> openBossTypes(player))
            .build());

        // Spawn cooldowns
        gui.setSlot(40, new GuiElementBuilder(Items.CLOCK)
            .setName(Component.literal("§b§lSpawn Status"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Cooldown: 30 minutes per boss"))
            .build());

        // Your kills
        gui.setSlot(43, new GuiElementBuilder(Items.DIAMOND_SWORD)
            .setName(Component.literal("§a§lYour Kills"))
            .addLoreLine(Component.literal(""))
            .addLoreLine(Component.literal("§7Total: §f" + totalKills))
            .setCallback((index, type, containerInput, slotGui) -> openPlayerKills(player))
            .build());

        gui.open();
    }

    public static void openBossTypes(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("§c§lWorld Boss Types"));

        String uuid = player.getUUID().toString();

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                .setName(Component.literal(""))
                .build());
        }

        // Back button
        gui.setSlot(0, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e← Back"))
            .setCallback((index, type, containerInput, slotGui) -> openMainMenu(player))
            .build());

        // List all boss types
        WorldBossManager.WorldBossType[] types = WorldBossManager.WorldBossType.values();
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            WorldBossManager.WorldBossType bossType = types[i];
            int slot = slots[i];
            int kills = WorldBossManager.getPlayerBossKills(uuid, bossType);
            long nextSpawn = WorldBossManager.getTimeUntilNextSpawn();

            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal(""));
            lore.add(Component.literal("§7" + bossType.description));
            lore.add(Component.literal(""));
            lore.add(Component.literal("§7Your Kills: §f" + kills));
            lore.add(Component.literal("§7Tier: §f" + bossType.tier.name));
            
            if (nextSpawn > 0) {
                lore.add(Component.literal("§bNext spawn in: " + (nextSpawn / 60000) + " min"));
            } else {
                lore.add(Component.literal("§aSpawning soon!"));
            }

            GuiElementBuilder builder = new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal(bossType.icon + " " + bossType.name).withStyle(ChatFormatting.RED));
            
            for (Component line : lore) {
                builder.addLoreLine(line);
            }

            gui.setSlot(slot, builder.build());
        }

        gui.open();
    }

    public static void openPlayerKills(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("§a§lYour Boss Kills"));

        String uuid = player.getUUID().toString();

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                .setName(Component.literal(""))
                .build());
        }

        // Back button
        gui.setSlot(0, new GuiElementBuilder(Items.ARROW)
            .setName(Component.literal("§e← Back"))
            .setCallback((index, type, containerInput, slotGui) -> openMainMenu(player))
            .build());

        // Total
        int total = WorldBossManager.getTotalPlayerBossKills(uuid);
        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
            .setName(Component.literal("§6§lTotal Kills: §f" + total))
            .build());

        // Per boss
        WorldBossManager.WorldBossType[] types = WorldBossManager.WorldBossType.values();
        int[] slots = {19, 20, 21, 22, 23, 24, 28, 29, 30, 31, 32, 33};

        for (int i = 0; i < types.length && i < slots.length; i++) {
            WorldBossManager.WorldBossType bossType = types[i];
            int slot = slots[i];
            int kills = WorldBossManager.getPlayerBossKills(uuid, bossType);

            gui.setSlot(slot, new GuiElementBuilder(kills > 0 ? Items.DIAMOND : Items.GRAY_DYE)
                .setName(Component.literal(bossType.icon + " " + bossType.name))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Kills: §f" + kills))
                .build());
        }

        gui.open();
    }

    public static void openAdminMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("§c§lWorld Boss Admin"));

        // Fill background
        for (int i = 0; i < 54; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                .setName(Component.literal(""))
                .build());
        }

        // Spawn options
        WorldBossManager.WorldBossType[] types = WorldBossManager.WorldBossType.values();
        WorldBossManager.BossTier[] tiers = WorldBossManager.BossTier.values();
        
        int slot = 10;
        for (WorldBossManager.WorldBossType type : types) {
            for (WorldBossManager.BossTier tier : tiers) {
                if (slot >= 54) break;
                
                gui.setSlot(slot, new GuiElementBuilder(Items.SPAWNER)
                    .setName(Component.literal("§aSpawn " + tier.name + " Boss"))
                    .addLoreLine(Component.literal(""))
                    .addLoreLine(Component.literal("§7Health: §c" + tier.baseHealth))
                    .addLoreLine(Component.literal("§7Reward: §6" + String.format("%,d", tier.coinReward)))
                    .setCallback((index, act, containerInput, slotGui) -> {
                        ServerLevel world = (ServerLevel) player.level();
                        WorldBossManager.forceSpawn(world, type);
                        player.sendSystemMessage(Component.literal("§aSpawned " + type.tier.name + " " + type.name + "!"));
                    })
                    .build());
                slot++;
            }
        }

        // Despawn all
        gui.setSlot(53, new GuiElementBuilder(Items.BARRIER)
            .setName(Component.literal("§cDespawn All Bosses"))
            .setCallback((index, type, containerInput, slotGui) -> {
                WorldBossManager.despawnAll();
                player.sendSystemMessage(Component.literal("§cDespawned all world bosses!"));
            })
            .build());

        gui.open();
    }
}
