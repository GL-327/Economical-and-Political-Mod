package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.particle.ParticleTypes;

import java.util.*;

/**
 * Custom arrows with various damage bonuses and effects.
 * These are crafted via the Fletching Table GUI.
 */
public class CustomArrows {

    // Arrow types with their properties
    public enum ArrowType {
        IRON_TIPPED("Iron-Tipped Arrow", 1.5f, Formatting.GRAY, "§7+0.5 damage per arrow"),
        STEEL_TIPPED("Steel-Tipped Arrow", 2.0f, Formatting.WHITE, "§f+1.0 damage per arrow"),
        DIAMOND_TIPPED("Diamond-Tipped Arrow", 3.0f, Formatting.AQUA, "§b+2.0 damage per arrow"),
        NETHERITE_TIPPED("Netherite-Tipped Arrow", 4.5f, Formatting.DARK_RED, "§c+3.5 damage per arrow"),
        EXPLOSIVE_ARROW("Explosive Arrow", 2.0f, Formatting.GOLD, "§6Creates explosion on hit"),
        INCENDIARY_ARROW("Incendiary Arrow", 1.5f, Formatting.RED, "§cSets target on fire for 5s"),
        POISON_ARROW("Venom Arrow", 1.0f, Formatting.DARK_GREEN, "§2Poisons target for 10s"),
        VOID_ARROW("Void Arrow", 6.0f, Formatting.DARK_PURPLE, "§5+5.0 damage, pierces armor"),
        SPECTRAL_ARROW("Spectral Arrow", 1.0f, Formatting.YELLOW, "§eGlows target for 15s"),
        // New arrows with special abilities
        FROST_ARROW("Frost Arrow", 1.5f, Formatting.BLUE, "§bFreezes target for 3s"),
        LIGHTNING_ARROW("Lightning Arrow", 3.0f, Formatting.YELLOW, "§eStrikes target with lightning"),
        HEALING_ARROW("Healing Arrow", 0.0f, Formatting.GREEN, "§aHeals target by 4 hearts"),
        GRAVITY_ARROW("Gravity Arrow", 1.0f, Formatting.DARK_GRAY, "§7Pulls nearby entities to impact"),
        BOUNCING_ARROW("Bouncing Arrow", 0.5f, Formatting.LIGHT_PURPLE, "§dBounces up to 3 times");

        private final String name;
        private final float bonusDamage;
        private final Formatting color;
        private final String description;

        ArrowType(String name, float bonusDamage, Formatting color, String description) {
            this.name = name;
            this.bonusDamage = bonusDamage;
            this.color = color;
            this.description = description;
        }

        public String getName() { return name; }
        public float getBonusDamage() { return bonusDamage; }
        public Formatting getColor() { return color; }
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

        NbtCompound nbt = new NbtCompound();
        nbt.putByte(TAG_CUSTOM_ARROW, (byte) 1);
        nbt.putString(TAG_ARROW_TYPE, type.name());
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(type.getName()).formatted(type.getColor(), Formatting.BOLD));

        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ CUSTOM AMMUNITION ◆").formatted(type.getColor(), Formatting.BOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal(type.getDescription()).formatted(Formatting.GRAY));
        lore.add(Text.literal(""));
        lore.add(Text.literal("Base Damage: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.format("%.1f", type.getBonusDamage())).formatted(Formatting.RED, Formatting.BOLD)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8[Fletching Table Crafted]").formatted(Formatting.DARK_GRAY));
        lore.add(Text.literal("「Custom Arrow」").formatted(type.getColor()));

        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, type == ArrowType.VOID_ARROW || type == ArrowType.NETHERITE_TIPPED);

        SlayerItems.setCustomItemId(stack, "custom_arrow_" + type.name().toLowerCase());

        return stack;
    }

    /**
     * Check if an item stack is a custom arrow.
     */
    public static boolean isCustomArrow(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound())).copyNbt();
        return nbt.contains(TAG_CUSTOM_ARROW);
    }

    /**
     * Get the arrow type from an item stack.
     */
    public static ArrowType getArrowType(ItemStack stack) {
        if (!isCustomArrow(stack)) return null;
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound())).copyNbt();
        String typeName = nbt.getString(TAG_ARROW_TYPE, "");
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
    public static void onArrowHit(ArrowEntity arrow, LivingEntity target, ServerWorld world) {
        // Check if the shooter was holding custom arrows
        ItemStack arrowStack = null;
        if (arrow.getOwner() instanceof ServerPlayerEntity player) {
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
            target.damage(world, arrow.getDamageSources().magic(), bonusDamage);
            world.spawnParticles(ParticleTypes.PORTAL,
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    20, 0.5, 0.5, 0.5, 0.1);
        } else {
            // Normal bonus damage
            target.damage(world, arrow.getDamageSources().arrow(arrow, arrow.getOwner()), bonusDamage);
        }

        // Apply special effects
        switch (type) {
            case EXPLOSIVE_ARROW -> {
                world.createExplosion(arrow, arrow.getX(), arrow.getY(), arrow.getZ(), 2.0f, net.minecraft.world.World.ExplosionSourceType.MOB);
                world.spawnParticles(ParticleTypes.EXPLOSION,
                        arrow.getX(), arrow.getY(), arrow.getZ(),
                        1, 0, 0, 0, 0);
            }
            case INCENDIARY_ARROW -> {
                target.setFireTicks(100); // 5 seconds
                world.spawnParticles(ParticleTypes.FLAME,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        15, 0.3, 0.5, 0.3, 0.05);
            }
            case POISON_ARROW -> {
                target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                        net.minecraft.entity.effect.StatusEffects.POISON, 200, 1)); // 10s, level 2
                world.spawnParticles(ParticleTypes.ITEM_SLIME,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        10, 0.3, 0.5, 0.3, 0.05);
            }
            case SPECTRAL_ARROW -> {
                target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                        net.minecraft.entity.effect.StatusEffects.GLOWING, 300)); // 15s
                world.spawnParticles(ParticleTypes.END_ROD,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        10, 0.3, 0.5, 0.3, 0.05);
            }
            case DIAMOND_TIPPED -> {
                world.spawnParticles(ParticleTypes.CRIT,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        8, 0.3, 0.5, 0.3, 0.1);
            }
            case NETHERITE_TIPPED -> {
                world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        10, 0.3, 0.5, 0.3, 0.05);
            }
            case FROST_ARROW -> {
                // Freeze target for 3 seconds
                target.setFrozenTicks(60);
                target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                        net.minecraft.entity.effect.StatusEffects.SLOWNESS, 60, 4)); // 3s, slowness V
                world.spawnParticles(ParticleTypes.SNOWFLAKE,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        20, 0.5, 0.5, 0.5, 0.1);
            }
            case LIGHTNING_ARROW -> {
                // Strike target with lightning
                net.minecraft.entity.EntityType.LIGHTNING_BOLT.spawn(world, 
                        target.getBlockPos(), net.minecraft.entity.SpawnReason.TRIGGERED);
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        15, 0.5, 0.5, 0.5, 0.1);
            }
            case HEALING_ARROW -> {
                // Heal target instead of damaging
                target.heal(8.0f); // 4 hearts
                world.spawnParticles(ParticleTypes.HEART,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        10, 0.5, 0.5, 0.5, 0.1);
            }
            case GRAVITY_ARROW -> {
                // Pull nearby entities to impact point
                double pullRadius = 5.0;
                for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, 
                        target.getBoundingBox().expand(pullRadius), e -> e != target && e.isAlive())) {
                    net.minecraft.util.math.Vec3d direction = new net.minecraft.util.math.Vec3d(
                            target.getX() - entity.getX(),
                            target.getY() - entity.getY(),
                            target.getZ() - entity.getZ()
                    ).normalize().multiply(1.5);
                    entity.addVelocity(direction.x, direction.y + 0.3, direction.z);
                }
                world.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        15, 0.5, 0.5, 0.5, 0.1);
            }
            case BOUNCING_ARROW -> {
                // Bouncing effect - knockback
                target.takeKnockback(1.5, -arrow.getVelocity().x, -arrow.getVelocity().z);
                world.spawnParticles(ParticleTypes.ITEM_SLIME,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        10, 0.3, 0.5, 0.3, 0.05);
            }
            default -> {
                // No special particles for basic arrows
            }
        }
    }

    /**
     * Find custom arrows in a player's inventory.
     */
    private static ItemStack findCustomArrowInInventory(ServerPlayerEntity player) {
        // Check offhand first (arrow slot)
        ItemStack offhand = player.getOffHandStack();
        if (isCustomArrow(offhand)) return offhand;

        // Check inventory
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
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
