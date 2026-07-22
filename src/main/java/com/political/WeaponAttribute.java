package com.political;

import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public enum WeaponAttribute {
    SHARP("sharp", "Sharp", 10, ChatFormatting.RED, 
            "+15% Attack Damage & Critical hits.", "Reduced durability and attack speed."),
    
    SWIFT("swift", "Swift", 8, ChatFormatting.YELLOW, 
            "+25% Attack Speed & No cooldown.", "-20% Attack Damage & reduced knockback."),
    
    VAMPIRIC("vampiric", "Vampiric", 12, ChatFormatting.DARK_RED, 
            "Lifesteal on hit (+10% max HP).", "No healing from other sources."),
    
    FROSTBITE("frostbite", "Frostbite", 9, ChatFormatting.AQUA, 
            "Slows enemies & Freezes water.", "Reduced damage in hot biomes."),
    
    THUNDER("thunder", "Thunder", 11, ChatFormatting.LIGHT_PURPLE, 
            "Lightning strikes & Chain damage.", "Damage to self in rain."),
    
    POISON("poison", "Poison", 7, ChatFormatting.DARK_GREEN, 
            "Poison enemies on hit.", "Poison yourself rarely."),
    
    EXPLOSIVE("explosive", "Explosive", 13, ChatFormatting.GOLD, 
            "Area damage & Knockback.", "Self-damage chance."),
    
    HOLY("holy", "Holy", 10, ChatFormatting.WHITE, 
            "Extra damage to undead.", "Reduced damage to normal mobs."),
    
    SHADOW("shadow", "Shadow", 11, ChatFormatting.DARK_GRAY, 
            "Invisibility on kill & Stealth.", "Reduced damage in daylight."),
    
    WIND("wind", "Wind", 8, ChatFormatting.GREEN, 
            "Knockback & Flight boost.", "Reduced damage on ground.");

    public final String id;
    public final String displayName;
    public final int xpCost;
    public final ChatFormatting color;
    public final String buffSummary;
    public final String debuffSummary;

    WeaponAttribute(String id, String displayName, int xpCost, ChatFormatting color, String buffSummary, String debuffSummary) {
        this.id = id;
        this.displayName = displayName;
        this.xpCost = xpCost;
        this.color = color;
        this.buffSummary = buffSummary;
        this.debuffSummary = debuffSummary;
    }

    public static WeaponAttribute fromId(String id) {
        for (WeaponAttribute attr : values()) {
            if (attr.id.equals(id)) return attr;
        }
        return null;
    }
}
