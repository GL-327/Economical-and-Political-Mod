package com.political;

public class Perk {

    public final String id;
    public final String name;
    public final String description;
    public final int pointValue;
    public final PerkType type;

    public Perk(String id, String name, String description, int pointValue, PerkType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pointValue = pointValue;
        this.type = type;
    }

    public enum PerkType {
        POSITIVE,
        NEUTRAL,
        NEGATIVE
    }
}
