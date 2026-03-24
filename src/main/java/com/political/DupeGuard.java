package com.political;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

    private static String fingerprint(ServerPlayerEntity player, ItemStack stack) {
        String cid = SlayerItems.getCustomItemId(stack);
        String reg = Registries.ITEM.getId(stack.getItem()).toString();
        int dmg = stack.getDamage();
        return player.getUuidAsString() + "|" + reg + "|" + (cid != null ? cid : "-") + "|" + dmg + "|" + stack.getCount();
    }

    public static void tick(MinecraftServer server) {
        if (server == null || !CivilAntiCheatManager.isDupeScanEnabled()) return;
        if (server.getTicks() % 100 != 0) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.isSpectator() || player.isCreative()) continue;
            scanAllSlots(player);
        }
    }

    private static void scanAllSlots(ServerPlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            scanStack(player, player.getInventory().getStack(i));
        }
    }

    private static void scanStack(ServerPlayerEntity player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        int max = stack.getMaxCount();
        int c = stack.getCount();
        boolean bad = c > max || c < 0;
        if (!bad && max == 1 && c > 1) bad = true;
        if (!bad && Registries.ITEM.getId(stack.getItem()).getPath().endsWith("_sword") && c > 1) bad = true;
        if (!bad) {
            String cid = SlayerItems.getCustomItemId(stack);
            if (cid != null && c > 16) bad = true;
        }
        if (!bad) return;

        String fp = fingerprint(player, stack);
        if (FLAGGED.add(fp)) {
            PoliticalServer.LOGGER.warn("[DupeGuard] Flagged {} ({}) stack {} x{} (max {})",
                    player.getName().getString(), player.getUuidAsString(),
                    Registries.ITEM.getId(stack.getItem()), c, max);
            player.sendMessage(Text.literal(PLAYER_NOTIFY).formatted(Formatting.RED, Formatting.BOLD), false);
            CivilAntiCheatManager.broadcastToOps(player.getEntityWorld().getServer(),
                    Text.literal("[CivilAC] Dupe heuristic: ").formatted(Formatting.DARK_RED)
                            .append(player.getDisplayName())
                            .append(Text.literal(" " + fp).formatted(Formatting.GRAY)));
        }
        if (CivilAntiCheatManager.isClampIllegalStacksEnabled() && c > max && max > 0) {
            stack.setCount(max);
        }
    }

    /** Record all current suspicious fingerprints for a player (for /quarantine). */
    public static void captureForQuarantine(ServerPlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            addIfSuspicious(player, player.getInventory().getStack(i));
        }
        for (int i = 0; i < player.getEnderChestInventory().size(); i++) {
            addIfSuspicious(player, player.getEnderChestInventory().getStack(i));
        }
    }

    private static void addIfSuspicious(ServerPlayerEntity player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        int max = stack.getMaxCount();
        int c = stack.getCount();
        if (c > max || (max == 1 && c > 1)) FLAGGED.add(fingerprint(player, stack));
    }

    public static int quarantinePlayer(ServerPlayerEntity target) {
        if (target == null) return 0;
        UUID uuid = target.getUuid();
        List<String> toMatch = new ArrayList<>();
        for (String fp : FLAGGED) {
            if (fp.startsWith(uuid + "|")) toMatch.add(fp);
        }
        int removed = 0;
        if (toMatch.isEmpty()) {
            for (int i = 0; i < target.getInventory().size(); i++) {
                ItemStack s = target.getInventory().getStack(i);
                if (s.isEmpty()) continue;
                if (s.getCount() > s.getMaxCount() || (s.getMaxCount() == 1 && s.getCount() > 1)) {
                    target.getInventory().setStack(i, ItemStack.EMPTY);
                    removed++;
                }
            }
            for (int i = 0; i < target.getEnderChestInventory().size(); i++) {
                ItemStack s = target.getEnderChestInventory().getStack(i);
                if (s.isEmpty()) continue;
                if (s.getCount() > s.getMaxCount() || (s.getMaxCount() == 1 && s.getCount() > 1)) {
                    target.getEnderChestInventory().setStack(i, ItemStack.EMPTY);
                    removed++;
                }
            }
            target.getInventory().markDirty();
            target.getEnderChestInventory().markDirty();
            return removed;
        }
        for (int i = 0; i < target.getInventory().size(); i++) {
            ItemStack s = target.getInventory().getStack(i);
            if (matches(s, target, toMatch)) {
                target.getInventory().setStack(i, ItemStack.EMPTY);
                removed++;
            }
        }
        for (int i = 0; i < target.getEnderChestInventory().size(); i++) {
            ItemStack s = target.getEnderChestInventory().getStack(i);
            if (matches(s, target, toMatch)) {
                target.getEnderChestInventory().setStack(i, ItemStack.EMPTY);
                removed++;
            }
        }
        target.getInventory().markDirty();
        target.getEnderChestInventory().markDirty();
        for (String fp : toMatch) FLAGGED.remove(fp);
        return removed;
    }

    private static boolean matches(ItemStack stack, ServerPlayerEntity target, List<String> toMatch) {
        if (stack == null || stack.isEmpty()) return false;
        String fp = fingerprint(target, stack);
        for (String t : toMatch) {
            if (Objects.equals(fp, t)) return true;
        }
        int max = stack.getMaxCount();
        if (stack.getCount() > max) return true;
        return false;
    }
}
