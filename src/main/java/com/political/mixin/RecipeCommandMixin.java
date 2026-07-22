package com.political.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.RecipeCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Disables vanilla `/recipe` permissions, because this mod provides its own
 * clickable recipe-browser GUI at the same command label.
 */
@Mixin(RecipeCommand.class)
public abstract class RecipeCommandMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void political_disableVanillaRecipeCommand(CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo ci) {
        // Prevent vanilla from registering its restrictive command tree.
        ci.cancel();
    }
}

