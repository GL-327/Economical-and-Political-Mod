package com.political.power.game;

import java.util.function.Predicate;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

/**
 * Who may use the power administration (grant, revoke, change school, reset). It is one permission node of the
 * Fabric permission API: a permissions mod can grant it by name, and without one it needs the operator level
 * "owners", the same level the other admin commands of this mod ask for.
 */
public final class PowerPermissions {

    public static final Identifier ADMIN = Identifier.fromNamespaceAndPath("politicalserver", "power.admin");
    public static final PermissionLevel ADMIN_DEFAULT = PermissionLevel.OWNERS;

    private PowerPermissions() {
    }

    /** For a command's {@code requires}. */
    public static Predicate<CommandSourceStack> adminCommand() {
        return PermissionPredicates.require(ADMIN, ADMIN_DEFAULT);
    }

    /** For a screen: checked when it opens and again on every click, because a screen can outlive a permission. */
    public static boolean isAdmin(ServerPlayer player) {
        return player.checkPermission(ADMIN, ADMIN_DEFAULT);
    }
}
