package com.political.backrooms.item;

import net.minecraft.item.Item;

// Placeable torch item that repels Smilers in the Backrooms dimension.
// The repel effect is handled via BackroomsMobs tick behavior.
public class DeterrentTorchItem extends Item {

    public DeterrentTorchItem(Settings settings) {
        super(settings);
    }
}
