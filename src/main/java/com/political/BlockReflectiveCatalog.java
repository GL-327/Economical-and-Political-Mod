package com.political;

import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/** Enumerate BlockWeapons / BlockArmor / BlockTools factory methods so all ~660 items get GUI coverage. */
public final class BlockReflectiveCatalog {

    private BlockReflectiveCatalog() {}

    public static void addBlockWeapons(List<ItemStack> out) {
        invokeCreateMethods(BlockWeapons.class, out);
    }

    public static void addBlockArmor(List<ItemStack> out) {
        invokeCreateMethods(BlockArmor.class, out);
    }

    public static void addBlockTools(List<ItemStack> out) {
        invokeCreateMethods(BlockTools.class, out);
    }

    private static void invokeCreateMethods(Class<?> clazz, List<ItemStack> out) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (!Modifier.isPublic(m.getModifiers()) || !Modifier.isStatic(m.getModifiers())) continue;
            if (!m.getName().startsWith("create")) continue;
            if (m.getParameterCount() != 0) continue;
            if (!ItemStack.class.equals(m.getReturnType())) continue;
            try {
                ItemStack s = (ItemStack) m.invoke(null);
                if (s != null && !s.isEmpty()) {
                    out.add(s.copy());
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static void addEnchantedBlockGear(List<ItemStack> out) {
        List<ItemStack> base = new java.util.ArrayList<>();
        addBlockWeapons(base);
        addBlockArmor(base);
        addBlockTools(base);
        for (ItemStack b : base) {
            out.add(EnchantedSectorFactory.skyblockEnchantVisual(b));
        }
    }
}
