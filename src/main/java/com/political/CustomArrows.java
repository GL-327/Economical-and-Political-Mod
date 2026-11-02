package com.political;


import com.political.bounty.SlayerItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;

import java.util.*;

/**
 * Custom arrows with various damage bonuses and effects.
 * These are crafted via the Fletching Table GUI.
 */
public class CustomArrows {

    // Arrow types with their properties
    public enum ArrowType {
        IRON_TIPPED("Iron-Tipped Arrow", 1.5f, ChatFormatting.GRAY, "§7+0.5 damage per arrow"),
        STEEL_TIPPED("Steel-Tipped Arrow", 2.0f, ChatFormatting.WHITE, "§f+1.0 damage per arrow"),
        DIAMOND_TIPPED("Diamond-Tipped Arrow", 3.0f, ChatFormatting.AQUA, "§b+2.0 damage per arrow"),
        NETHERITE_TIPPED("Netherite-Tipped Arrow", 4.5f, ChatFormatting.DARK_RED, "§c+3.5 damage per arrow"),
        EXPLOSIVE_ARROW("Explosive Arrow", 2.0f, ChatFormatting.GOLD, "§6Creates explosion on hit"),
        INCENDIARY_ARROW("Incendiary Arrow", 1.5f, ChatFormatting.RED, "§cSets target on fire for 5s"),
        POISON_ARROW("Venom Arrow", 1.0f, ChatFormatting.DARK_GREEN, "§2Poisons target for 10s"),
        VOID_ARROW("Void Arrow", 6.0f, ChatFormatting.DARK_PURPLE, "§5+5.0 damage, pierces armor"),
        SPECTRAL_ARROW("Spectral Arrow", 1.0f, ChatFormatting.YELLOW, "§eGlows target for 15s"),
        // New arrows with special abilities
        FROST_ARROW("Frost Arrow", 1.5f, ChatFormatting.BLUE, "§bFreezes target for 3s"),
        LIGHTNING_ARROW("Lightning Arrow", 3.0f, ChatFormatting.YELLOW, "§eStrikes target with lightning"),
        HEALING_ARROW("Healing Arrow", 0.0f, ChatFormatting.GREEN, "§aHeals target by 4 hearts"),
        GRAVITY_ARROW("Gravity Arrow", 1.0f, ChatFormatting.DARK_GRAY, "§7Pulls nearby entities to impact"),
        BOUNCING_ARROW("Bouncing Arrow", 0.5f, ChatFormatting.LIGHT_PURPLE, "§dBounces up to 3 times");

        private final String name;
        private final float bonusDamage;
        private final ChatFormatting color;
        private final String description;

        ArrowType(String name, float bonusDamage, ChatFormatting color, String description) {
            this.name = name;
            this.bonusDamage = bonusDamage;
            this.color = color;
            this.description = description;
        }

        public String getName() { return name; }
        public float getBonusDamage() { return bonusDamage; }
        public ChatFormatting getColor() { return color; }
        public String getDescription() { return description; }
    }

    // Track custom arrow data
    private static final String TAG_CUSTOM_ARROW = "custom_arrow";
    private static final String TAG_ARROW_TYPE = "arrow_type";

    /**
     * Create a custom arrow item stack.
     */
    public static ItemStack createArrow(ArrowType type, int count) {
        ItemStack stack = new ItemStack(Items.TIPPED_ARROW, count);

        CompoundTag nbt = new CompoundTag();
        nbt.putByte(TAG_CUSTOM_ARROW, (byte) 1);
        nbt.putString(TAG_ARROW_TYPE, type.name());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(type.getName()).withStyle(type.getColor(), ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("◆ CUSTOM AMMUNITION ◆").withStyle(type.getColor(), ChatFormatting.BOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal(type.getDescription()).withStyle(ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(Component.literal("Base Damage: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.1f", type.getBonusDamage())).withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8[Fletching Table Crafted]").withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal("「Custom Arrow」").withStyle(type.getColor()));

        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, type == ArrowType.VOID_ARROW || type == ArrowType.NETHERITE_TIPPED);

        SlayerItems.setCustomItemId(stack, "custom_arrow_" + type.name().toLowerCase());

        return stack;
    }

    /**
     * Check if an item stack is a custom arrow.
     */
    public static boolean isCustomArrow(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag())).copyTag();
        return nbt.contains(TAG_CUSTOM_ARROW);
    }

    /**
     * Get the arrow type from an item stack.
     */
    public static ArrowType getArrowType(ItemStack stack) {
        if (!isCustomArrow(stack)) return null;
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag())).copyTag();
        String typeName = nbt.getStringOr(TAG_ARROW_TYPE, "");
        try {
            return ArrowType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Called when a custom arrow hits a target.
     * Apply bonus damage and special effects.
     */
    public static void onArrowHit(Arrow arrow, LivingEntity target, ServerLevel world) {
        // Check if the shooter was holding custom arrows
        ItemStack arrowStack = null;
        if (arrow.getOwner() instanceof ServerPlayer player) {
            arrowStack = findCustomArrowInInventory(player);
        }
        if (arrowStack == null) return;

        ArrowType type = getArrowType(arrowStack);
        if (type == null) return;

        // Apply bonus damage
        float bonusDamage = type.getBonusDamage();
        
        // Special handling for void arrows (armor piercing)
        if (type == ArrowType.VOID_ARROW) {
            // Void arrows ignore armor
            target.hurtServer(world, arrow.damageSources().magic(), bonusDamage);
            world.sendParticles(ParticleTypes.PORTAL,
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    5, 0.5, 0.5, 0.5, 0.1);
        } else {
            // Normal bonus damage
            target.hurtServer(world, arrow.damageSources().arrow(arrow, arrow.getOwner()), bonusDamage);
        }

        // Apply special effects
        switch (type) {
            case EXPLOSIVE_ARROW -> {
                world.explode(arrow, arrow.getX(), arrow.getY(), arrow.getZ(), 2.0f, Level.ExplosionInteraction.MOB);
                world.sendParticles(ParticleTypes.EXPLOSION,
                        arrow.getX(), arrow.getY(), arrow.getZ(),
                        1, 0, 0, 0, 0);
            }
            case INCENDIARY_ARROW -> {
                target.setRemainingFireTicks(100); // 5 seconds
                world.sendParticles(ParticleTypes.FLAME,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        5, 0.3, 0.5, 0.3, 0.05);
            }
            case POISON_ARROW -> {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.POISON, 200, 1)); // 10s, level 2
                world.sendParticles(ParticleTypes.ITEM_SLIME,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        4, 0.3, 0.5, 0.3, 0.05);
            }
            case SPECTRAL_ARROW -> {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.GLOWING, 300)); // 15s
                world.sendParticles(ParticleTypes.END_ROD,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        4, 0.3, 0.5, 0.3, 0.05);
            }
            case DIAMOND_TIPPED -> {
                world.sendParticles(ParticleTypes.CRIT,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        3, 0.3, 0.5, 0.3, 0.1);
            }
            case NETHERITE_TIPPED -> {
                world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        4, 0.3, 0.5, 0.3, 0.05);
            }
            case FROST_ARROW -> {
                // Freeze target for 3 seconds
                target.setTicksFrozen(60);
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.SLOWNESS, 60, 4)); // 3s, slowness V
                world.sendParticles(ParticleTypes.SNOWFLAKE,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        5, 0.5, 0.5, 0.5, 0.1);
            }
            case LIGHTNING_ARROW -> {
                // Strike target with lightning
                net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.spawn(world, 
                        target.blockPosition(), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                world.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        5, 0.5, 0.5, 0.5, 0.1);
            }
            case HEALING_ARROW -> {
                // Heal target instead of damaging
                target.heal(8.0f); // 4 hearts
                world.sendParticles(ParticleTypes.HEART,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        4, 0.5, 0.5, 0.5, 0.1);
            }
            case GRAVITY_ARROW -> {
                // Pull nearby entities to impact point
                double pullRadius = 5.0;
                for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, 
                        target.getBoundingBox().inflate(pullRadius), e -> e != target && e.isAlive())) {
                    net.minecraft.world.phys.Vec3 direction = new net.minecraft.world.phys.Vec3(
                            target.getX() - entity.getX(),
                            target.getY() - entity.getY(),
                            target.getZ() - entity.getZ()
                    ).normalize().scale(1.5).add(0, 0.3, 0);
                    entity.addDeltaMovement(direction);
                }
                world.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        5, 0.5, 0.5, 0.5, 0.1);
            }
            case BOUNCING_ARROW -> {
                // Bouncing effect - knockback
                target.knockback(1.5, -arrow.getDeltaMovement().x, -arrow.getDeltaMovement().z);
                world.sendParticles(ParticleTypes.ITEM_SLIME,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        4, 0.3, 0.5, 0.3, 0.05);
            }
            default -> {
                // No special particles for basic arrows
            }
        }
    }

    /**
     * Find custom arrows in a player's inventory.
     */
    private static ItemStack findCustomArrowInInventory(ServerPlayer player) {
        // Check offhand first (arrow slot)
        ItemStack offhand = player.getOffhandItem();
        if (isCustomArrow(offhand)) return offhand;

        // Check inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isCustomArrow(stack)) return stack;
        }
        return null;
    }

    /**
     * Get all arrow types for GUI display.
     */
    public static ArrowType[] getAllArrowTypes() {
        return ArrowType.values();
    }

    /**
     * Get crafting recipe for an arrow type.
     * Returns an array of required items [base, tip material, modifier].
     */
    public static ItemStack[] getRecipe(ArrowType type) {
        return switch (type) {
            case IRON_TIPPED -> new ItemStack[]{
                    new ItemStack(Items.ARROW, 8),
                    new ItemStack(Items.IRON_INGOT),
                    null
            };
            case STEEL_TIPPED -> new ItemStack[]{
                    new ItemStack(Items.ARROW, 8),
                    new ItemStack(Items.IRON_BLOCK),
                    null
            };
            case DIAMOND_TIPPED -> new ItemStack[]{
                    new ItemStack(Items.ARROW, 8),
                    new ItemStack(Items.DIAMOND),
                    null
            };
            case NETHERITE_TIPPED -> new ItemStack[]{
                    new ItemStack(Items.ARROW, 8),
                    new ItemStack(Items.NETHERITE_INGOT),
                    null
            };
            case EXPLOSIVE_ARROW -> new ItemStack[]{
                    new ItemStack(Items.ARROW, 4),
                    new ItemStack(Items.TNT),
                    new ItemStack(Items.GUNPOWDER)
            };
            case INCENDIARY_ARROW -> new ItemStack[]{
                    new ItemStack(Items.ARROW, 8),
                    new ItemStack(Items.BLAZE_POWDER),
                    new ItemStack(Items.FIRE_CHARGE)
            };
            case POISON_ARROW -> new ItemStack[]{
                    new ItemStack(Items.ARROW, 8),
                    new ItemStack(Items.SPIDER_EYE),
                    new ItemStack(Items.PUFFERFISH)
            };
            case VOID_ARROW -> new ItemStack[]{
                    new ItemStack(Items.ARROW, 4),
                    new ItemStack(Items.ENDER_PEARL),
                    new ItemStack(Items.ECHO_SHARD)
            };
            case SPECTRAL_ARROW -> new ItemStack[]{
                    new ItemStack(Items.ARROW, 8),
                    new ItemStack(Items.GLOWSTONE_DUST),
                    new ItemStack(Items.GHAST_TEAR)
            };
            // New arrows
            case FROST_ARROW -> new ItemStack[]{
                    new ItemStack(Items.ARROW, 8),
                    new ItemStack(Items.ICE),
                    new ItemStack(Items.BLUE_ICE)
            };
            case LIGHTNING_ARROW -> new ItemStack[]{
                    new ItemStack(Items.ARROW, 4),
                    new ItemStack(Items.COPPER_INGOT),
                    new ItemStack(Items.LIGHTNING_ROD)
            };
            case HEALING_ARROW -> new ItemStack[]{
                    new ItemStack(Items.ARROW, 4),
                    new ItemStack(Items.GOLDEN_APPLE),
                    new ItemStack(Items.GLISTERING_MELON_SLICE)
            };
            case GRAVITY_ARROW -> new ItemStack[]{
                    new ItemStack(Items.ARROW, 8),
                    new ItemStack(Items.ENDER_EYE),
                    new ItemStack(Items.CHORUS_FRUIT)
            };
            case BOUNCING_ARROW -> new ItemStack[]{
                    new ItemStack(Items.ARROW, 8),
                    new ItemStack(Items.SLIME_BALL),
                    new ItemStack(Items.HONEY_BLOCK)
            };
        };
    }

    /**
     * Get the output count for a recipe.
     */
    public static int getOutputCount(ArrowType type) {
        return switch (type) {
            case IRON_TIPPED, STEEL_TIPPED, DIAMOND_TIPPED, NETHERITE_TIPPED -> 8;
            case EXPLOSIVE_ARROW -> 4;
            case INCENDIARY_ARROW, POISON_ARROW, SPECTRAL_ARROW -> 8;
            case VOID_ARROW -> 4;
            case FROST_ARROW, GRAVITY_ARROW, BOUNCING_ARROW -> 8;
            case LIGHTNING_ARROW, HEALING_ARROW -> 4;
        };
    }
}
