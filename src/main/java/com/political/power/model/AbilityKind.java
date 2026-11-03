package com.political.power.model;

/** What an ability does, which decides how the validation pipeline and the effect executor treat it. */
public enum AbilityKind {
    /** Pulls its target toward the caster. */
    BLUE,
    /** Pushes its target away from the caster. */
    RED,
    /** Instant ray that damages the first living target. */
    BLAST,
    /** Heals the caster or a player; its cost depends on how much it heals. */
    REVERSE,
    /** A short window that blocks incoming damage. */
    BARRIER,
    /** A Domain around the caster; capstone only. */
    DOMAIN,
    /** One of the 32 abilities of the eight older techniques, run by the legacy effect code. */
    LEGACY
}
