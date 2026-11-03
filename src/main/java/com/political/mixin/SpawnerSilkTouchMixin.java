package com.political.mixin;

import com.political.PoliticalServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Allows silk touch pickaxes to pick up spawners.
 * When a player breaks a spawner with a silk touch tool, the spawner item drops.
 */
@Mixin(Block.class)
public class SpawnerSilkTouchMixin {

    @Inject(method = "playerDestroy", at = @At("HEAD"), cancellable = true)
    private void onSpawnerBreak(net.minecraft.server.level.ServerLevel world, ServerPlayer serverPlayer, BlockPos pos, BlockState state,
            BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
        
        if (world.isClientSide()) return;
        
        // Only handle spawner blocks
        if (state.getBlock() != Blocks.SPAWNER) return;
        
        // Check for silk touch enchantment
        if (!hasSilkTouch(tool)) return;
        
        // Cancel default behavior (no XP drop)
        ci.cancel();
        
        // Drop the spawner item
        ItemStack spawnerStack = new ItemStack(Items.SPAWNER);
        
        // Drop the spawner
        Block.popResource(world, pos, spawnerStack);
        
        // Notify player
        serverPlayer.sendSystemMessage(
            Component.literal("✦ Spawner captured with Silk Touch!").withStyle(ChatFormatting.AQUA),
            true
        );
        
        // Remove the block
        world.removeBlock(pos, false);
    }
    
    private static boolean hasSilkTouch(ItemStack tool) {
        if (PoliticalServer.server == null) return false;
        
        try {
            ResourceKey<net.minecraft.world.item.enchantment.Enchantment> silkTouchKey =
                ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("minecraft:silk_touch"));
            Optional<Holder.Reference<net.minecraft.world.item.enchantment.Enchantment>> entryOpt =
                PoliticalServer.server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(silkTouchKey);
            if (entryOpt.isEmpty()) return false;
            Holder<net.minecraft.world.item.enchantment.Enchantment> silkTouchEntry = entryOpt.get();
            
            ItemEnchantments enchantments = tool.getOrDefault(DataComponents.ENCHANTMENTS,
                ItemEnchantments.EMPTY);
            return enchantments.getLevel(silkTouchEntry) > 0;
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger("politicalserver.mixin").debug("SpawnerSilkTouchMixin.hasSilkTouch: ignored an error", e);
            return false;
        }
    }
}
