package com.political;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Gem-based armor sets: Lapis and Emerald.
 * Created as dyed leather armor with custom stats and durability.
 */
public class GemArmor {

    // ============================================================
    // CONSTANTS
    // ============================================================
    
    // Lapis Armor - Magic-focused, between leather and iron
    public static final int LAPIS_DURABILITY = 250;
    public static final int LAPIS_COLOR = 0x1E3A8A; // Deep blue
    
    // Emerald Armor - Wealth-focused, between iron and diamond
    public static final int EMERALD_DURABILITY = 400;
    public static final int EMERALD_COLOR = 0x10B981; // Emerald green
    
    // ============================================================
    // ARMOR STATS
    // ============================================================
    
    // Lapis: Slightly better than leather, magic-themed
    // Helmet: 2 armor, 1 toughness | Chestplate: 4 armor, 1 toughness | Leggings: 3 armor, 1 toughness | Boots: 1 armor, 1 toughness
    // Bonus: +10% XP from all sources per piece
    
    // Emerald: Between iron and diamond, wealth-themed  
    // Helmet: 3 armor, 2 toughness | Chestplate: 6 armor, 2 toughness | Leggings: 5 armor, 2 toughness | Boots: 2 armor, 2 toughness
    // Bonus: +5% coin gain from all sources per piece
    
    // ============================================================
    // LAPIS ARMOR CREATION
    // ============================================================
    
    public static ItemStack createLapisHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyLapisStats(stack, "Helmet", 2, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "lapis_helmet");
        return stack;
    }
    
    public static ItemStack createLapisChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyLapisStats(stack, "Chestplate", 4, 1, EquipmentSlot.CHEST);
        setCustomItemId(stack, "lapis_chestplate");
        return stack;
    }
    
    public static ItemStack createLapisLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyLapisStats(stack, "Leggings", 3, 1, EquipmentSlot.LEGS);
        setCustomItemId(stack, "lapis_leggings");
        return stack;
    }
    
    public static ItemStack createLapisBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyLapisStats(stack, "Boots", 1, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "lapis_boots");
        return stack;
    }
    
    private static void applyLapisStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        // Dye color
        stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(LAPIS_COLOR));
        
        // Custom name
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Lapis Lazuli " + pieceName).formatted(Formatting.DARK_BLUE, Formatting.BOLD));
        
        // Custom durability
        stack.set(DataComponentTypes.MAX_DAMAGE, LAPIS_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        // Attributes
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "lapis_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "lapis_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        // Lore
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_BLUE));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.BLUE)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.BLUE)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.DARK_PURPLE));
        lore.add(Text.literal("✦ +10% XP per piece worn").formatted(Formatting.LIGHT_PURPLE));
        lore.add(Text.literal("✦ Full set: +40% XP & Water Breathing").formatted(Formatting.AQUA));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Enchanted with ancient magic").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // EMERALD ARMOR CREATION
    // ============================================================
    
    public static ItemStack createEmeraldHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyEmeraldStats(stack, "Helmet", 3, 2, EquipmentSlot.HEAD);
        setCustomItemId(stack, "emerald_helmet");
        return stack;
    }
    
    public static ItemStack createEmeraldChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyEmeraldStats(stack, "Chestplate", 6, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "emerald_chestplate");
        return stack;
    }
    
    public static ItemStack createEmeraldLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyEmeraldStats(stack, "Leggings", 5, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "emerald_leggings");
        return stack;
    }
    
    public static ItemStack createEmeraldBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyEmeraldStats(stack, "Boots", 2, 2, EquipmentSlot.FEET);
        setCustomItemId(stack, "emerald_boots");
        return stack;
    }
    
    private static void applyEmeraldStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        // Dye color
        stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(EMERALD_COLOR));
        
        // Custom name
        stack.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("Emerald " + pieceName).formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        
        // Custom durability
        stack.set(DataComponentTypes.MAX_DAMAGE, EMERALD_DURABILITY);
        stack.set(DataComponentTypes.DAMAGE, 0);
        
        // Attributes
        AttributeModifierSlot modifierSlot = getSlotForPiece(pieceName);
        AttributeModifiersComponent.Builder attrBuilder = AttributeModifiersComponent.builder()
                .add(EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "emerald_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                Identifier.of("politicalserver", "emerald_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        // Lore
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ STATS ━━━").formatted(Formatting.DARK_GREEN));
        lore.add(Text.literal("🛡 Armor: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + armorValue).formatted(Formatting.GREEN)));
        lore.add(Text.literal("💎 Toughness: ").formatted(Formatting.WHITE)
                .append(Text.literal("+" + toughnessValue).formatted(Formatting.GREEN)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ SET BONUS ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("✦ +5% coin gain per piece worn").formatted(Formatting.YELLOW));
        lore.add(Text.literal("✦ Full set: +20% coins & Fortune III").formatted(Formatting.GOLD));
        lore.add(Text.literal(""));
        lore.add(Text.literal("§8Forged from wealth itself").formatted(Formatting.DARK_GRAY));
        
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    private static void setCustomItemId(ItemStack stack, String id) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("gem_armor_id", id);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }
    
    public static String getGemArmorId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return null;
        String id = nbt.copyNbt().getString("gem_armor_id", null);
        return (id != null && !id.isEmpty()) ? id : null;
    }
    
    public static boolean isGemArmor(ItemStack stack) {
        return getGemArmorId(stack) != null;
    }
    
    public static boolean isLapisArmor(ItemStack stack) {
        String id = getGemArmorId(stack);
        return id != null && id.startsWith("lapis_");
    }
    
    public static boolean isEmeraldArmor(ItemStack stack) {
        String id = getGemArmorId(stack);
        return id != null && id.startsWith("emerald_");
    }
    
    /**
     * Count how many pieces of gem armor the player is wearing.
     */
    public static int countLapisPieces(PlayerEntity player) {
        int count = 0;
        if (isLapisArmor(player.getEquippedStack(EquipmentSlot.HEAD))) count++;
        if (isLapisArmor(player.getEquippedStack(EquipmentSlot.CHEST))) count++;
        if (isLapisArmor(player.getEquippedStack(EquipmentSlot.LEGS))) count++;
        if (isLapisArmor(player.getEquippedStack(EquipmentSlot.FEET))) count++;
        return count;
    }
    
    public static int countEmeraldPieces(PlayerEntity player) {
        int count = 0;
        if (isEmeraldArmor(player.getEquippedStack(EquipmentSlot.HEAD))) count++;
        if (isEmeraldArmor(player.getEquippedStack(EquipmentSlot.CHEST))) count++;
        if (isEmeraldArmor(player.getEquippedStack(EquipmentSlot.LEGS))) count++;
        if (isEmeraldArmor(player.getEquippedStack(EquipmentSlot.FEET))) count++;
        return count;
    }
    
    /**
     * Get XP bonus from Lapis armor (10% per piece).
     */
    public static double getLapisXpBonus(PlayerEntity player) {
        return countLapisPieces(player) * 0.10;
    }
    
    /**
     * Get coin bonus from Emerald armor (5% per piece).
     */
    public static double getEmeraldCoinBonus(PlayerEntity player) {
        return countEmeraldPieces(player) * 0.05;
    }
    
    /**
     * Check if wearing full Lapis set.
     */
    public static boolean hasFullLapisSet(PlayerEntity player) {
        return countLapisPieces(player) == 4;
    }
    
    /**
     * Check if wearing full Emerald set.
     */
    public static boolean hasFullEmeraldSet(PlayerEntity player) {
        return countEmeraldPieces(player) == 4;
    }
    
    // ============================================================
    // GIVE ARMOR SETS
    // ============================================================
    
    public static void giveLapisSet(ServerPlayerEntity player) {
        giveItem(player, createLapisHelmet());
        giveItem(player, createLapisChestplate());
        giveItem(player, createLapisLeggings());
        giveItem(player, createLapisBoots());
        player.sendMessage(Text.literal("§b✔ Received full Lapis Lazuli armor set!"), false);
    }
    
    public static void giveEmeraldSet(ServerPlayerEntity player) {
        giveItem(player, createEmeraldHelmet());
        giveItem(player, createEmeraldChestplate());
        giveItem(player, createEmeraldLeggings());
        giveItem(player, createEmeraldBoots());
        player.sendMessage(Text.literal("§a✔ Received full Emerald armor set!"), false);
    }
    
    private static void giveItem(ServerPlayerEntity player, ItemStack stack) {
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }
    
    private static AttributeModifierSlot getSlotForPiece(String pieceName) {
        return switch (pieceName) {
            case "Helmet" -> AttributeModifierSlot.HEAD;
            case "Chestplate" -> AttributeModifierSlot.CHEST;
            case "Leggings" -> AttributeModifierSlot.LEGS;
            case "Boots" -> AttributeModifierSlot.FEET;
            default -> AttributeModifierSlot.ANY;
        };
    }
}
