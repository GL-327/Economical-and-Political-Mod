package com.political.mixin;

import com.political.BlockArmor;
import com.political.GemArmor;
import com.political.SlayerItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents re-dyeing of custom items in the crafting grid.
 * This applies to all custom dyed leather armor and custom items.
 */
@Mixin(CraftingScreenHandler.class)
public class ItemDyeMixin {

    /**
     * Intercepts crafting updates to prevent dyeing custom items.
     */
    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private static void preventCustomItemDyeing(ScreenHandler handler, ServerWorld world,
                                                  PlayerEntity player, RecipeInputInventory craftingInventory,
                                                  CraftingResultInventory resultInventory,
                                                  RecipeEntry<?> recipe,
                                                  CallbackInfo ci) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        
        // Check the crafting grid for dye + custom item combinations
        int dyeSlot = -1;
        int armorSlot = -1;
        ItemStack armorStack = null;
        
        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack stack = craftingInventory.getStack(i);
            if (stack == null || stack.isEmpty()) continue;
            
            // Check if this is a dye item
            if (stack.getItem() instanceof DyeItem) {
                dyeSlot = i;
            }
            
            // Check if this is a custom dyed leather armor
            if (isCustomDyedItem(stack)) {
                armorSlot = i;
                armorStack = stack.copy();
            }
        }
        
        // If both dye and custom armor are present, prevent the crafting
        if (dyeSlot != -1 && armorSlot != -1 && armorStack != null) {
            serverPlayer.sendMessage(Text.literal("§c✖ Cannot re-dye custom items!").formatted(Formatting.RED), true);
            resultInventory.setStack(0, ItemStack.EMPTY);
            ci.cancel();
        }
    }
    
    /**
     * Checks if an item is a custom dyed item that should not be re-dyed.
     */
    private static boolean isCustomDyedItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        
        // Check if it's a leather armor piece with a custom ID
        if (stack.isOf(Items.LEATHER_HELMET) ||
            stack.isOf(Items.LEATHER_CHESTPLATE) ||
            stack.isOf(Items.LEATHER_LEGGINGS) ||
            stack.isOf(Items.LEATHER_BOOTS)) {
            
            // Check for custom item IDs from our custom item systems
            String customId = SlayerItems.getCustomItemId(stack);
            if (customId != null) return true;
            
            String gemArmorId = GemArmor.getGemArmorId(stack);
            if (gemArmorId != null) return true;
            
            String blockArmorId = BlockArmor.getBlockArmorId(stack);
            if (blockArmorId != null) return true;
        }
        
        return false;
    }
}
