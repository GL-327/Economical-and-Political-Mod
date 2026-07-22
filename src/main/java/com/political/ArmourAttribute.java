package com.political;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public enum ArmourAttribute {
    BURNING("burning", "Burning", EquipmentSlot.CHEST, 10, ChatFormatting.GOLD, 
            "Deals fire damage when on fire.", "Fire never self-extinguishes."),
    SIGHTLESS("sightless", "Sightless", EquipmentSlot.HEAD, 8, ChatFormatting.BLUE, 
            "Permanent Night Vision & Blindness immunity.", "Reduces FOV and adds vignette."),
    FRENZIED("frenzied", "Frenzied", EquipmentSlot.HEAD, 12, ChatFormatting.DARK_RED, 
            "+15% Attack Speed & Berserk buffs.", "Cannot eat and applies nausea."),
    GROUNDED("grounded", "Grounded", EquipmentSlot.LEGS, 8, ChatFormatting.DARK_GREEN, 
            "Lightning immunity & Projectile defense.", "Reduced jump height and no Elytra."),
    WEBBED("webbed", "Webbed", EquipmentSlot.LEGS, 7, ChatFormatting.WHITE, 
            "Bypass cobweb & Spider neutrality.", "Slowed on grass/leaves and climbing."),
    FROST("frost", "Frost", EquipmentSlot.FEET, 9, ChatFormatting.AQUA, 
            "Freeze water & Freeze immunity.", "Take 150% lava damage. Slowed in Nether."),
    PHANTOMSTEP("phantomstep", "Phantom Step", EquipmentSlot.FEET, 11, ChatFormatting.LIGHT_PURPLE, 
            "No fall damage & Stealth sounds.", "Phantoms always target you. Cannot sleep."),
    CURSED("cursed", "Cursed", null, 15, ChatFormatting.DARK_PURPLE, 
            "+6 Max HP & Lifesteal on kill.", "XP reduction and Villager hostility."),
    OVERGROWN("overgrown", "Overgrown", null, 7, ChatFormatting.GREEN, 
            "Regen while still on natural blocks.", "Reduced mining speed. Nether/End decay."),
    VOLATILE("volatile", "Volatile", null, 13, ChatFormatting.RED, 
            "Explode when low HP & Blast immunity.", "Blowback chance when using fire items.");

    public final String id;
    public final String displayName;
    public final EquipmentSlot slot; // null means ANY_SLOT
    public final int xpCost;
    public final ChatFormatting color;
    public final String buffSummary;
    public final String debuffSummary;

    ArmourAttribute(String id, String displayName, EquipmentSlot slot, int xpCost, ChatFormatting color, String buffSummary, String debuffSummary) {
        this.id = id;
        this.displayName = displayName;
        this.slot = slot;
        this.xpCost = xpCost;
        this.color = color;
        this.buffSummary = buffSummary;
        this.debuffSummary = debuffSummary;
    }

    public static ArmourAttribute fromId(String id) {
        for (ArmourAttribute attr : values()) {
            if (attr.id.equals(id)) return attr;
        }
        return null;
    }

    public boolean isValidSlot(EquipmentSlot slot) {
        if (this.slot == null) return true;
        return this.slot == slot;
    }
}
