package com.political;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class T2CraftingGui {

    public static void open(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("T2 Bounty Armor Crafting"));

        fillBackground(gui);

        // Add category buttons for each slayer type
        int slot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            int playerLevel = SlayerData.getSlayerLevel(player.getUUID().toString(), type);
            boolean unlocked = playerLevel >= SlayerItems.T2_ARMOR_LEVEL_REQ;

            ItemStack icon = getTypeIcon(type);

            GuiElementBuilder builder = new GuiElementBuilder(icon.getItem())
                    .setName(Component.literal(type.displayName + " T2 Armor")
                            .withStyle(type.color, ChatFormatting.BOLD));

            if (unlocked) {
                builder.addLoreLine(Component.literal(""));
                builder.addLoreLine(Component.literal("✓ UNLOCKED").withStyle(ChatFormatting.GREEN));
                builder.addLoreLine(Component.literal("Level: " + playerLevel + "/" + SlayerItems.T2_ARMOR_LEVEL_REQ)
                        .withStyle(ChatFormatting.GRAY));
                builder.addLoreLine(Component.literal(""));
                builder.addLoreLine(Component.literal("Click to view recipes").withStyle(ChatFormatting.YELLOW));
                builder.setCallback((idx, clicktype, containerInput, slotGui) -> {
                    openTypeMenu(player, type);
                });
            } else {
                builder.addLoreLine(Component.literal(""));
                builder.addLoreLine(Component.literal("✗ LOCKED").withStyle(ChatFormatting.RED));
                builder.addLoreLine(Component.literal("Requires Level " + SlayerItems.T2_ARMOR_LEVEL_REQ)
                        .withStyle(ChatFormatting.GRAY));
                builder.addLoreLine(Component.literal("Current: " + playerLevel).withStyle(ChatFormatting.GRAY));
            }

            gui.setSlot(slot, builder.build());
            slot++;
            if (slot == 17) slot = 28; // Next row
        }

        // Info book
        gui.setSlot(49, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("T2 Armor Info").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("T2 armor provides:").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("• Better stats than T1").withStyle(ChatFormatting.GREEN))
                .addLoreLine(Component.literal("• Higher boss damage reduction").withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("• Special set abilities").withStyle(ChatFormatting.LIGHT_PURPLE))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Craft by combining:").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("T1 piece + Cores + Materials").withStyle(ChatFormatting.GRAY))
                .build());

        gui.open();
    }

    private static void openTypeMenu(ServerPlayer player, SlayerManager.SlayerType type) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x4, player, false);
        gui.setTitle(Component.literal(type.displayName + " T2 Armor Crafting"));

        fillBackground(gui);

        List<T2CraftingHandler.T2Recipe> recipes = T2CraftingHandler.getAllRecipes();

        // Filter recipes for this type
        int slot = 10;
        String[] pieces = {"Helmet", "Chestplate", "Leggings", "Boots"};

        for (String piece : pieces) {
            for (T2CraftingHandler.T2Recipe recipe : recipes) {
                if (recipe.type == type && recipe.pieceName.equals(piece)) {
                    ItemStack preview = switch (piece) {
                        case "Helmet" -> SlayerItems.createT2Helmet(type);
                        case "Chestplate" -> SlayerItems.createT2Chestplate(type);
                        case "Leggings" -> SlayerItems.createT2Leggings(type);
                        case "Boots" -> SlayerItems.createT2Boots(type);
                        default -> ItemStack.EMPTY;
                    };

                    GuiElementBuilder builder = new GuiElementBuilder(preview.getItem())
                            .setName(preview.get(DataComponents.CUSTOM_NAME));

                    // Add existing lore
                    var existingLore = preview.get(DataComponents.LORE);
                    if (existingLore != null) {
                        for (Component line : existingLore.lines()) {
                            builder.addLoreLine(line);
                        }
                    }

                    // Add recipe info
                    for (Component line : T2CraftingHandler.getRecipeLore(recipe)) {
                        builder.addLoreLine(line);
                    }

                    builder.addLoreLine(Component.literal(""));
                    builder.addLoreLine(Component.literal("Click to craft").withStyle(ChatFormatting.GREEN));

                    final T2CraftingHandler.T2Recipe finalRecipe = recipe;
                    builder.setCallback((idx, clicktype, containerInput, slotGui) -> {
                        openCraftingConfirm(player, finalRecipe);
                    });

                    gui.setSlot(slot, builder.build());
                    slot++;
                    break;
                }
            }
        }

        // Back button
        gui.setSlot(31, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> {
                    open(player);
                })
                .build());

        gui.open();
    }

    private static void openCraftingConfirm(ServerPlayer player, T2CraftingHandler.T2Recipe recipe) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x5, player, false);
        gui.setTitle(Component.literal("Craft " + recipe.type.displayName + " " + recipe.pieceName + " II"));

        fillBackground(gui);

        // Show required items
        // T1 Piece slot (10)
        gui.setSlot(10, new GuiElementBuilder(Items.ARMOR_STAND)
                .setName(Component.literal("T1 " + recipe.pieceName).withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal("Required: T1 " + recipe.type.displayName + " " + recipe.pieceName))
                .build());

        // Cores slot (12)
        gui.setSlot(12, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Component.literal(recipe.coresRequired + "x " + recipe.type.displayName + " Cores")
                        .withStyle(ChatFormatting.AQUA))
                .addLoreLine(Component.literal("Obtained from killing " + recipe.type.bossName))
                .build());

        // Special material slot (14)
        gui.setSlot(14, new GuiElementBuilder(recipe.specialMaterial.getItem())
                .setName(Component.literal(recipe.specialMaterialCount + "x " +
                                recipe.specialMaterial.getHoverName().getString())
                        .withStyle(ChatFormatting.LIGHT_PURPLE))
                .setCount(recipe.specialMaterialCount)
                .build());

        // Arrow
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("▼").withStyle(ChatFormatting.WHITE))
                .build());

        // Result preview (31)
        ItemStack result = T2CraftingHandler.craft(player, recipe);
        gui.setSlot(31, new GuiElementBuilder(result.getItem())
                .setName(result.get(DataComponents.CUSTOM_NAME))
                .hideDefaultTooltip()
                .build());

        // Check if player has materials
        boolean hasMaterials = checkPlayerHasMaterials(player, recipe);

        // Craft button (40)
        if (hasMaterials) {
            gui.setSlot(40, new GuiElementBuilder(Items.LIME_CONCRETE)
                    .setName(Component.literal("✓ CRAFT").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Click to craft!").withStyle(ChatFormatting.YELLOW))
                    .setCallback((idx, clicktype, containerInput, slotGui) -> {
                        if (consumeMaterials(player, recipe)) {
                            ItemStack crafted = T2CraftingHandler.craft(player, recipe);
                            player.addItem(crafted);
                            player.sendSystemMessage(Component.literal("§a§l✓ Crafted " +
                                    crafted.get(DataComponents.CUSTOM_NAME).getString() + "!"), false);
                            player.level().playSound(null, player.blockPosition(),
                                    SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
                            gui.close();
                        }
                    })
                    .build());
        } else {
            gui.setSlot(40, new GuiElementBuilder(Items.RED_CONCRETE)
                    .setName(Component.literal("✗ Missing Materials").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .addLoreLine(Component.literal("Gather required items first").withStyle(ChatFormatting.GRAY))
                    .build());
        }

        // Back button
        gui.setSlot(36, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .setCallback((idx, clicktype, containerInput, slotGui) -> {
                    openTypeMenu(player, recipe.type);
                })
                .build());

        gui.open();
    }

    private static boolean checkPlayerHasMaterials(ServerPlayer player, T2CraftingHandler.T2Recipe recipe) {
        // Check T1 piece
        boolean hasT1 = false;
        int coreCount = 0;
        int specialCount = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            // Check T1 piece
            Component name = stack.get(DataComponents.CUSTOM_NAME);
            if (name != null) {
                String n = name.getString();
                if (!n.contains(" II") && n.contains(recipe.pieceName)) {
                    boolean typeMatch = switch (recipe.type) {
                        case ZOMBIE -> n.contains("Undying") || n.contains("Outlaw");
                        case SPIDER -> n.contains("Venomous") || n.contains("Bandit");
                        case SKELETON -> n.contains("Bone") || n.contains("Desperado");
                        case SLIME -> n.contains("Gelatinous") || n.contains("Rustler");
                        case ENDERMAN -> n.contains("Void") || n.contains("Phantom");
                        case IRON_GOLEM -> n.contains("Sculk") || n.contains("Terror");
                        case PIGLIN -> n.contains("Gilded") || n.contains("Ravager");
                    };
                    if (typeMatch) hasT1 = true;
                }

                // Check cores
                if (SlayerItems.isSlayerCore(stack)) {
                    SlayerManager.SlayerType coreType = SlayerItems.getCoreType(stack);
                    if (coreType == recipe.type) {
                        coreCount += stack.getCount();
                    }
                }
            }

            // Check special material
            if (stack.is(recipe.specialMaterial.getItem())) {
                specialCount += stack.getCount();
            }
        }

        return hasT1 && coreCount >= recipe.coresRequired && specialCount >= recipe.specialMaterialCount;
    }

    private static boolean consumeMaterials(ServerPlayer player, T2CraftingHandler.T2Recipe recipe) {
        // Consume T1 piece
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            Component name = stack.get(DataComponents.CUSTOM_NAME);
            if (name != null) {
                String n = name.getString();
                if (!n.contains(" II") && n.contains(recipe.pieceName)) {
                    boolean typeMatch = switch (recipe.type) {
                        case ZOMBIE -> n.contains("Undying") || n.contains("Outlaw");
                        case SPIDER -> n.contains("Venomous") || n.contains("Bandit");
                        case SKELETON -> n.contains("Bone") || n.contains("Desperado");
                        case SLIME -> n.contains("Gelatinous") || n.contains("Rustler");
                        case ENDERMAN -> n.contains("Void") || n.contains("Phantom");
                        case IRON_GOLEM -> n.contains("Sculk") || n.contains("Terror");
                        case PIGLIN -> n.contains("Gilded") || n.contains("Ravager");
                    };
                    if (typeMatch) {
                        player.getInventory().getItem(i).shrink(1);
                        break;
                    }
                }
            }
        }

        // Consume cores
        int coresToConsume = recipe.coresRequired;
        for (int i = 0; i < player.getInventory().getContainerSize() && coresToConsume > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (SlayerItems.isSlayerCore(stack)) {
                SlayerManager.SlayerType coreType = SlayerItems.getCoreType(stack);
                if (coreType == recipe.type) {
                    int toRemove = Math.min(stack.getCount(), coresToConsume);
                    stack.shrink(toRemove);
                    coresToConsume -= toRemove;
                }
            }
        }

        // Consume special material
        int specialToConsume = recipe.specialMaterialCount;
        for (int i = 0; i < player.getInventory().getContainerSize() && specialToConsume > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(recipe.specialMaterial.getItem())) {
                int toRemove = Math.min(stack.getCount(), specialToConsume);
                stack.shrink(toRemove);
                specialToConsume -= toRemove;
            }
        }

        return true;
    }

    private static void fillBackground(SimpleGui gui) {
        ItemStack glass = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        glass.set(DataComponents.CUSTOM_NAME, Component.literal(""));
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Component.literal(""))
                    .hideDefaultTooltip()
                    .build());
        }
    }

    private static ItemStack getTypeIcon(SlayerManager.SlayerType type) {
        return switch (type) {
            case ZOMBIE -> new ItemStack(Items.ZOMBIE_HEAD);
            case SPIDER -> new ItemStack(Items.SPIDER_EYE);
            case SKELETON -> new ItemStack(Items.SKELETON_SKULL);
            case SLIME -> new ItemStack(Items.SLIME_BALL);
            case ENDERMAN -> new ItemStack(Items.ENDER_PEARL);
            case IRON_GOLEM -> new ItemStack(Items.SCULK);
            case PIGLIN -> new ItemStack(Items.GOLD_NUGGET);
        };
    }
}