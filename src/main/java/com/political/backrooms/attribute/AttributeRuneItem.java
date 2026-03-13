package com.political.backrooms.attribute;

import com.political.PoliticalServer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Enchanted name-tag-like item storing an ArmorAttribute.
 * Applied to player armor to grant that armor piece the attribute's effects.
 */
public class AttributeRuneItem extends Item {

    public static final String ATTRIBUTE_KEY = "backrooms_attribute";

    private final ArmorAttribute attribute;

    public AttributeRuneItem(ArmorAttribute attribute, Settings settings) {
        super(settings);
        this.attribute = attribute;
    }

    public ArmorAttribute getAttribute() {
        return attribute;
    }

    /** Returns the ArmorAttribute stored in an ItemStack's NBT, or null if absent.
     *  Falls back to the item's baked-in attribute when no NBT data is present
     *  (e.g. items given via /give without explicit NBT). */
    public static ArmorAttribute getAttributeFromStack(ItemStack stack) {
        // First, try reading from CUSTOM_DATA NBT (createRune() path)
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data != null) {
            NbtCompound nbt = data.copyNbt();
            String name = nbt.getString(ATTRIBUTE_KEY, null);
            if (name != null && !name.isEmpty()) {
                try {
                    return ArmorAttribute.valueOf(name);
                } catch (IllegalArgumentException ignored) {
                    // fall through to item-class attribute
                }
            }
        }
        // Fall back to the attribute baked into the item type itself
        if (stack.getItem() instanceof AttributeRuneItem rune) {
            return rune.getAttribute();
        }
        return null;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    /** Helper: creates a rune ItemStack for the given attribute. */
    public static ItemStack createRune(Item runeItem, ArmorAttribute attribute) {
        ItemStack stack = new ItemStack(runeItem);
        NbtCompound nbt = new NbtCompound();
        nbt.putString(ATTRIBUTE_KEY, attribute.name());
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        stack.set(DataComponentTypes.CUSTOM_NAME,
            Text.literal("✦ " + formatName(attribute) + " Rune").styled(s ->
                s.withColor(net.minecraft.util.Formatting.LIGHT_PURPLE).withItalic(false)));
        return stack;
    }

    private static String formatName(ArmorAttribute attr) {
        String raw = attr.name().replace('_', ' ');
        String[] words = raw.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(w.charAt(0)));
            sb.append(w.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
