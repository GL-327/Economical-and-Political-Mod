package com.political.core.gui;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;

/**
 * The few sounds screens use (STYLE.md), so the same event always sounds the same. Ids are strings for the
 * same reason as {@link Icon}: the list can be tested and documented without starting Minecraft.
 */
public enum UiSound {
    OPEN("minecraft:ui.button.click", 0.6f, 1.0f, "A screen opens or the player moves to another one"),
    SUCCESS("minecraft:entity.experience_orb.pickup", 0.7f, 1.2f, "An action worked: bought, saved, claimed"),
    DENIED("minecraft:entity.villager.no", 0.8f, 1.0f, "An action was refused: not enough coins, no permission"),
    NOTICE("minecraft:block.note_block.bell", 0.8f, 1.0f, "Something needs the player's attention");

    private final String soundId;
    private final float volume;
    private final float pitch;
    private final String use;

    UiSound(String soundId, float volume, float pitch, String use) {
        this.soundId = soundId;
        this.volume = volume;
        this.pitch = pitch;
        this.use = use;
    }

    public String soundId() {
        return soundId;
    }

    public float volume() {
        return volume;
    }

    public float pitch() {
        return pitch;
    }

    public String use() {
        return use;
    }

    /** Plays the sound for this player. Does nothing if the id does not exist in this version. */
    public void play(ServerPlayer player) {
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(soundId));
        if (event != null) {
            player.playSound(event, volume, pitch);
        }
    }
}
