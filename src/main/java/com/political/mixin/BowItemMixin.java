package com.political.mixin;

import net.minecraft.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;

/** Skeleton Bow instant-fire is now handled via AttackEntityCallback / AttackBlockCallback in PoliticalServer. */
@Mixin(BowItem.class)
public class BowItemMixin {
}
