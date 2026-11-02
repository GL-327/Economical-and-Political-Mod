package com.political.government;

/**
 * A national policy the Chair or Vice Chair can enact. Perks cost the treasury every day.
 * Cuts save coins by shrinking a department and applying a matching world penalty.
 */
public class Perk {

    public final String id;
    public final String name;
    public final String description;
    public final int pointValue;
    public final PerkType type;
    public final Kind kind;
    public final Department department;
    public final long dailyCost;
    public final long dailySaving;

    public Perk(String id, String name, String description, int pointValue, PerkType type) {
        this(id, name, description, pointValue, type, departmentFor(type, id), 0L, 0L);
    }

    public Perk(String id, String name, String description, int pointValue, PerkType type,
                Department department, long dailyCost, long dailySaving) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pointValue = pointValue;
        this.type = type;
        this.department = department;
        if (type == PerkType.NEGATIVE) {
            this.kind = Kind.CUT;
            this.dailyCost = 0L;
            this.dailySaving = dailySaving > 0 ? dailySaving : Math.max(5_000L, Math.abs(pointValue) * 20_000L);
        } else {
            this.kind = Kind.PERK;
            this.dailySaving = 0L;
            this.dailyCost = dailyCost > 0 ? dailyCost : Math.max(5_000L, Math.max(1, pointValue) * 25_000L);
        }
    }

    public enum PerkType {
        POSITIVE,
        NEUTRAL,
        NEGATIVE
    }

    public enum Kind {
        PERK,
        CUT
    }

    public enum Department {
        DEFENSE,
        INFRASTRUCTURE,
        AGRICULTURE,
        COMMERCE,
        WELFARE
    }

    private static Department departmentFor(PerkType type, String id) {
        if (id == null) return Department.WELFARE;
        String key = id.toUpperCase();
        if (key.contains("MONSTER") || key.contains("PATROL") || key.contains("BOUNTY") || key.contains("QUARRY")
                || key.contains("HUNTER") || key.contains("SLAYER") || key.contains("ARMOUR") || key.contains("DAMAGE")
                || key.contains("HEALTH") || key.contains("SHIELD") || key.contains("UNITY") || key.contains("PHOENIX")
                || key.contains("DIPLOMAT") || key.contains("HARDENED") || key.contains("CREEPER") || key.contains("CRIME")
                || key.contains("UPRISING") || key.contains("PARANOIA") || key.contains("GLASS")) {
            return Department.DEFENSE;
        }
        if (key.contains("WORK") || key.contains("INFRA") || key.contains("LANDING") || key.contains("SPEED")
                || key.contains("GRAVITY") || key.contains("PUBLIC") || key.contains("RESOURCE") || key.contains("FURNACE")
                || key.contains("BRITTLE") || key.contains("NEGLECT") || key.contains("SWIFT")) {
            return Department.INFRASTRUCTURE;
        }
        if (key.contains("GREEN") || key.contains("CROP") || key.contains("FAMINE") || key.contains("ENVIRON")
                || key.contains("THUMB") || key.contains("HARVEST") || key.contains("STOMACH") || key.contains("FISH")) {
            return Department.AGRICULTURE;
        }
        if (key.contains("AUCTION") || key.contains("TAX") || key.contains("TRADE") || key.contains("MERCHANT")
                || key.contains("ECONOM") || key.contains("COIN") || key.contains("TARIFF") || key.contains("MARKET")
                || key.contains("TREASURE") || key.contains("XP")) {
            return Department.COMMERCE;
        }
        return Department.WELFARE;
    }
}
