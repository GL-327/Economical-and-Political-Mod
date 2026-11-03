package com.political.core.gui;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Logs any icon or sound of the layout module whose id does not exist in this Minecraft version. */
public final class GuiSelfCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuiSelfCheck.class);

    private GuiSelfCheck() {
    }

    /** Returns true when every icon id and sound id resolves. */
    public static boolean run() {
        boolean ok = true;
        for (Icon icon : Icon.values()) {
            if (!BuiltInRegistries.ITEM.containsKey(Identifier.parse(icon.itemId()))) {
                LOGGER.error("GUI icon {} points at {}, which is not an item in this version", icon, icon.itemId());
                ok = false;
            }
        }
        for (UiSound sound : UiSound.values()) {
            if (!BuiltInRegistries.SOUND_EVENT.containsKey(Identifier.parse(sound.soundId()))) {
                LOGGER.error("GUI sound {} points at {}, which is not a sound in this version", sound, sound.soundId());
                ok = false;
            }
        }
        if (ok) {
            LOGGER.info("GUI layout module: all {} icons and {} sounds resolve", Icon.values().length, UiSound.values().length);
        }
        return ok;
    }
}
