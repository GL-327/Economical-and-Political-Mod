package com.political;

import net.minecraft.world.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Heuristic anti-dupe: flags impossible stacks, logs, and supports /quarantine cleanup.
 */
public final class DupeGuard {

    public static final String PLAYER_NOTIFY =
            "An error has been detected involving your UUID. The moderator has been informed";

    /** Fingerprints of stacks that should be stripped on quarantine. */
    private static final Set<String> FLAGGED = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static String fingerprint(ServerPlayer player, ItemStack stack) {
        String cid = SlayerItems.getCustomItemId(stack);
        String reg = ItemRegistryIds.asString(stack.getItem());
        int dmg = stack.getDamageValue();
        return player.getUUID().toString() + "|" + reg + "|" + (cid != null ? cid : "-") + "|" + dmg + "|" + stack.getCount();
    }

    public static void tick(MinecraftServer server) {
        if (server == null || !CivilAntiCheatManager.isDupeScanEnabled()) return;
        if (server.getTickCount() % 100 != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator() || player.isCreative()) continue;
            scanAllSlots(player);
        }
    }

    private static void scanAllSlots(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            scanStack(player, player.getInventory().getItem(i));
        }
    }

    private static void scanStack(ServerPlayer player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        int max = stack.getMaxStackSize();
        int c = stack.getCount();
        boolean bad = c > max || c < 0;
        if (!bad && max == 1 && c > 1) bad = true;
        if (!bad && ItemRegistryIds.asId(stack.getItem()).getPath().endsWith("_sword") && c > 1) bad = true;
        // Custom items: only flag truly UNSTACKABLE custom gear (max stack size 1) held in >1 count.
        // Stackable custom MATERIALS — cores, compacted/super-compacted ores, enchanted blocks — may
        // legitimately reach their real max stack size (17-64), so never flag them purely on count.
        if (!bad) {
            String cid = SlayerItems.getCustomItemId(stack);
            if (cid != null && max == 1 && c > 1) bad = true;
        }
        if (!bad) return;

        String fp = fingerprint(player, stack);
        if (FLAGGED.add(fp)) {
            PoliticalServer.LOGGER.warn("[DupeGuard] Flagged {} ({}) stack {} x{} (max {})",
                    player.getName().getString(), player.getUUID().toString(),
                    ItemRegistryIds.asString(stack.getItem()), c, max);
            player.sendSystemMessage(Component.literal(PLAYER_NOTIFY).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
            CivilAntiCheatManager.broadcastToOps(player.level().getServer(),
                    Component.literal("[CivilAC] Dupe heuristic: ").withStyle(ChatFormatting.DARK_RED)
                            .append(player.getDisplayName())
                            .append(Component.literal(" " + fp).withStyle(ChatFormatting.GRAY)));
        }
        if (CivilAntiCheatManager.isClampIllegalStacksEnabled() && c > max && max > 0) {
            stack.setCount(max);
        }
    }

    /** Record all current suspicious fingerprints for a player (for /quarantine). */
    public static void captureForQuarantine(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            addIfSuspicious(player, player.getInventory().getItem(i));
        }
        for (int i = 0; i < player.getEnderChestInventory().getContainerSize(); i++) {
            addIfSuspicious(player, player.getEnderChestInventory().getItem(i));
        }
    }

    private static void addIfSuspicious(ServerPlayer player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        int max = stack.getMaxStackSize();
        int c = stack.getCount();
        if (c > max || (max == 1 && c > 1)) FLAGGED.add(fingerprint(player, stack));
    }

    public static int quarantinePlayer(ServerPlayer target) {
        if (target == null) return 0;
        UUID uuid = target.getUUID();
        List<String> toMatch = new ArrayList<>();
        for (String fp : FLAGGED) {
            if (fp.startsWith(uuid + "|")) toMatch.add(fp);
        }
        int removed = 0;
        if (toMatch.isEmpty()) {
            for (int i = 0; i < target.getInventory().getContainerSize(); i++) {
                ItemStack s = target.getInventory().getItem(i);
                if (s.isEmpty()) continue;
                if (s.getCount() > s.getMaxStackSize() || (s.getMaxStackSize() == 1 && s.getCount() > 1)) {
                    target.getInventory().setItem(i, ItemStack.EMPTY);
                    removed++;
                }
            }
            for (int i = 0; i < target.getEnderChestInventory().getContainerSize(); i++) {
                ItemStack s = target.getEnderChestInventory().getItem(i);
                if (s.isEmpty()) continue;
                if (s.getCount() > s.getMaxStackSize() || (s.getMaxStackSize() == 1 && s.getCount() > 1)) {
                    target.getEnderChestInventory().setItem(i, ItemStack.EMPTY);
                    removed++;
                }
            }
            target.getInventory().setChanged();
            target.getEnderChestInventory().setChanged();
            return removed;
        }
        for (int i = 0; i < target.getInventory().getContainerSize(); i++) {
            ItemStack s = target.getInventory().getItem(i);
            if (matches(s, target, toMatch)) {
                target.getInventory().setItem(i, ItemStack.EMPTY);
                removed++;
            }
        }
        for (int i = 0; i < target.getEnderChestInventory().getContainerSize(); i++) {
            ItemStack s = target.getEnderChestInventory().getItem(i);
            if (matches(s, target, toMatch)) {
                target.getEnderChestInventory().setItem(i, ItemStack.EMPTY);
                removed++;
            }
        }
        target.getInventory().setChanged();
        target.getEnderChestInventory().setChanged();
        for (String fp : toMatch) FLAGGED.remove(fp);
        return removed;
    }

    private static boolean matches(ItemStack stack, ServerPlayer target, List<String> toMatch) {
        if (stack == null || stack.isEmpty()) return false;
        String fp = fingerprint(target, stack);
        for (String t : toMatch) {
            if (Objects.equals(fp, t)) return true;
        }
        int max = stack.getMaxStackSize();
        if (stack.getCount() > max) return true;
        return false;
    }
}
