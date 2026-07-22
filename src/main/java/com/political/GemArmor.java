package com.political;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

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
        applyLapisStats(stack, "Helmet", 3, 1, EquipmentSlot.HEAD);
        setCustomItemId(stack, "lapis_helmet");
        return stack;
    }
    
    public static ItemStack createLapisChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyLapisStats(stack, "Chestplate", 5, 2, EquipmentSlot.CHEST);
        setCustomItemId(stack, "lapis_chestplate");
        return stack;
    }
    
    public static ItemStack createLapisLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyLapisStats(stack, "Leggings", 4, 2, EquipmentSlot.LEGS);
        setCustomItemId(stack, "lapis_leggings");
        return stack;
    }
    
    public static ItemStack createLapisBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyLapisStats(stack, "Boots", 2, 1, EquipmentSlot.FEET);
        setCustomItemId(stack, "lapis_boots");
        return stack;
    }
    
    private static void applyLapisStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        // Dye color
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(LAPIS_COLOR));
        
        // Custom name
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Lapis Lazuli " + pieceName).withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.BOLD));
        
        // Custom durability
        stack.set(DataComponents.MAX_DAMAGE, LAPIS_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        // Attributes
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "lapis_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "lapis_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        // Lore
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_BLUE));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.BLUE)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ SET BONUS ━━━").withStyle(ChatFormatting.DARK_PURPLE));
        lore.add(Component.literal("✦ +12% XP per piece worn").withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.literal("✦ Full set: +48% XP & Water Breathing").withStyle(ChatFormatting.AQUA));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Enchanted with ancient magic").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // EMERALD ARMOR CREATION
    // ============================================================
    
    public static ItemStack createEmeraldHelmet() {
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        applyEmeraldStats(stack, "Helmet", 4, 3, EquipmentSlot.HEAD);
        setCustomItemId(stack, "emerald_helmet");
        return stack;
    }
    
    public static ItemStack createEmeraldChestplate() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        applyEmeraldStats(stack, "Chestplate", 7, 3, EquipmentSlot.CHEST);
        setCustomItemId(stack, "emerald_chestplate");
        return stack;
    }
    
    public static ItemStack createEmeraldLeggings() {
        ItemStack stack = new ItemStack(Items.LEATHER_LEGGINGS);
        applyEmeraldStats(stack, "Leggings", 6, 3, EquipmentSlot.LEGS);
        setCustomItemId(stack, "emerald_leggings");
        return stack;
    }
    
    public static ItemStack createEmeraldBoots() {
        ItemStack stack = new ItemStack(Items.LEATHER_BOOTS);
        applyEmeraldStats(stack, "Boots", 3, 3, EquipmentSlot.FEET);
        setCustomItemId(stack, "emerald_boots");
        return stack;
    }
    
    private static void applyEmeraldStats(ItemStack stack, String pieceName, int armorValue, int toughnessValue, EquipmentSlot slot) {
        // Dye color
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(EMERALD_COLOR));
        
        // Custom name
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Emerald " + pieceName).withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        
        // Custom durability
        stack.set(DataComponents.MAX_DAMAGE, EMERALD_DURABILITY);
        stack.set(DataComponents.DAMAGE, 0);
        
        // Attributes
        EquipmentSlotGroup modifierSlot = getSlotForPiece(pieceName);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "emerald_" + pieceName.toLowerCase() + "_armor"),
                                armorValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("politicalserver", "emerald_" + pieceName.toLowerCase() + "_toughness"),
                                toughnessValue,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        modifierSlot);
        
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());
        
        // Lore
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ STATS ━━━").withStyle(ChatFormatting.DARK_GREEN));
        lore.add(Component.literal("🛡 Armor: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + armorValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal("💎 Toughness: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("+" + toughnessValue).withStyle(ChatFormatting.GREEN)));
        lore.add(Component.literal(""));
        lore.add(Component.literal("━━━ SET BONUS ━━━").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal("✦ +7% coin gain per piece worn").withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("✦ Full set: +28% coins & Fortune III").withStyle(ChatFormatting.GOLD));
        lore.add(Component.literal(""));
        lore.add(Component.literal("§8Forged from wealth itself").withStyle(ChatFormatting.DARK_GRAY));
        
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    private static void setCustomItemId(ItemStack stack, String id) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("gem_armor_id", id);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        CustomItemLore.applySummarizedLore(stack);
    }
    
    public static String getGemArmorId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData nbt = stack.get(DataComponents.CUSTOM_DATA);
        if (nbt == null) return null;
        String id = nbt.copyTag().getStringOr("gem_armor_id", "");
        return id.isEmpty() ? null : id;
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
    public static int countLapisPieces(Player player) {
        int count = 0;
        if (isLapisArmor(player.getItemBySlot(EquipmentSlot.HEAD))) count++;
        if (isLapisArmor(player.getItemBySlot(EquipmentSlot.CHEST))) count++;
        if (isLapisArmor(player.getItemBySlot(EquipmentSlot.LEGS))) count++;
        if (isLapisArmor(player.getItemBySlot(EquipmentSlot.FEET))) count++;
        return count;
    }
    
    public static int countEmeraldPieces(Player player) {
        int count = 0;
        if (isEmeraldArmor(player.getItemBySlot(EquipmentSlot.HEAD))) count++;
        if (isEmeraldArmor(player.getItemBySlot(EquipmentSlot.CHEST))) count++;
        if (isEmeraldArmor(player.getItemBySlot(EquipmentSlot.LEGS))) count++;
        if (isEmeraldArmor(player.getItemBySlot(EquipmentSlot.FEET))) count++;
        return count;
    }
    
    /**
     * Get XP bonus from Lapis armor (10% per piece).
     */
    public static double getLapisXpBonus(Player player) {
        return countLapisPieces(player) * 0.12;
    }
    
    /**
     * Get coin bonus from Emerald armor (7% per piece).
     */
    public static double getEmeraldCoinBonus(Player player) {
        return countEmeraldPieces(player) * 0.07;
    }
    
    /**
     * Check if wearing full Lapis set.
     */
    public static boolean hasFullLapisSet(Player player) {
        return countLapisPieces(player) == 4;
    }
    
    /**
     * Check if wearing full Emerald set.
     */
    public static boolean hasFullEmeraldSet(Player player) {
        return countEmeraldPieces(player) == 4;
    }
    
    // ============================================================
    // GIVE ARMOR SETS
    // ============================================================
    
    public static void giveLapisSet(ServerPlayer player) {
        giveItem(player, createLapisHelmet());
        giveItem(player, createLapisChestplate());
        giveItem(player, createLapisLeggings());
        giveItem(player, createLapisBoots());
        player.sendSystemMessage(Component.literal("§b✔ Received full Lapis Lazuli armor set!"), false);
    }
    
    public static void giveEmeraldSet(ServerPlayer player) {
        giveItem(player, createEmeraldHelmet());
        giveItem(player, createEmeraldChestplate());
        giveItem(player, createEmeraldLeggings());
        giveItem(player, createEmeraldBoots());
        player.sendSystemMessage(Component.literal("§a✔ Received full Emerald armor set!"), false);
    }
    
    private static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
    }
    
    private static EquipmentSlotGroup getSlotForPiece(String pieceName) {
        return switch (pieceName) {
            case "Helmet" -> EquipmentSlotGroup.HEAD;
            case "Chestplate" -> EquipmentSlotGroup.CHEST;
            case "Leggings" -> EquipmentSlotGroup.LEGS;
            case "Boots" -> EquipmentSlotGroup.FEET;
            default -> EquipmentSlotGroup.ANY;
        };
    }
}
