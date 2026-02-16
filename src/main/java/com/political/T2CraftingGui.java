package com.political;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class T2CraftingGui {

    public static void open(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("T2 Bounty Armor Crafting"));

        fillBackground(gui);

        // Add category buttons for each slayer type
        int slot = 10;
        for (SlayerManager.SlayerType type : SlayerManager.SlayerType.values()) {
            int playerLevel = SlayerData.getSlayerLevel(player.getUuidAsString(), type);
            boolean unlocked = playerLevel >= SlayerItems.T2_ARMOR_LEVEL_REQ;

            ItemStack icon = getTypeIcon(type);

            GuiElementBuilder builder = new GuiElementBuilder(icon.getItem())
                    .setName(Text.literal(type.displayName + " T2 Armor")
                            .formatted(type.color, Formatting.BOLD));

            if (unlocked) {
                builder.addLoreLine(Text.literal(""));
                builder.addLoreLine(Text.literal("✓ UNLOCKED").formatted(Formatting.GREEN));
                builder.addLoreLine(Text.literal("Level: " + playerLevel + "/" + SlayerItems.T2_ARMOR_LEVEL_REQ)
                        .formatted(Formatting.GRAY));
                builder.addLoreLine(Text.literal(""));
                builder.addLoreLine(Text.literal("Click to view recipes").formatted(Formatting.YELLOW));
                builder.setCallback((idx, clickType, action) -> {
                    openTypeMenu(player, type);
                });
            } else {
                builder.addLoreLine(Text.literal(""));
                builder.addLoreLine(Text.literal("✗ LOCKED").formatted(Formatting.RED));
                builder.addLoreLine(Text.literal("Requires Level " + SlayerItems.T2_ARMOR_LEVEL_REQ)
                        .formatted(Formatting.GRAY));
                builder.addLoreLine(Text.literal("Current: " + playerLevel).formatted(Formatting.GRAY));
            }

            gui.setSlot(slot, builder.build());
            slot++;
            if (slot == 17) slot = 28; // Next row
        }

        // Info book
        gui.setSlot(49, new GuiElementBuilder(Items.BOOK)
                .setName(Text.literal("T2 Armor Info").formatted(Formatting.GOLD, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("T2 armor provides:").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("• Better stats than T1").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("• Higher boss damage reduction").formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("• Special set abilities").formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Craft by combining:").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("T1 piece + Cores + Materials").formatted(Formatting.GRAY))
                .build());

        gui.open();
    }

    private static void openTypeMenu(ServerPlayerEntity player, SlayerManager.SlayerType type) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
        gui.setTitle(Text.literal(type.displayName + " T2 Armor Crafting"));

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
                            .setName(preview.get(DataComponentTypes.CUSTOM_NAME));

                    // Add existing lore
                    var existingLore = preview.get(DataComponentTypes.LORE);
                    if (existingLore != null) {
                        for (Text line : existingLore.lines()) {
                            builder.addLoreLine(line);
                        }
                    }

                    // Add recipe info
                    for (Text line : T2CraftingHandler.getRecipeLore(recipe)) {
                        builder.addLoreLine(line);
                    }

                    builder.addLoreLine(Text.literal(""));
                    builder.addLoreLine(Text.literal("Click to craft").formatted(Formatting.GREEN));

                    final T2CraftingHandler.T2Recipe finalRecipe = recipe;
                    builder.setCallback((idx, clickType, action) -> {
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
                .setName(Text.literal("Back").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> {
                    open(player);
                })
                .build());

        gui.open();
    }

    private static void openCraftingConfirm(ServerPlayerEntity player, T2CraftingHandler.T2Recipe recipe) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("Craft " + recipe.type.displayName + " " + recipe.pieceName + " II"));

        fillBackground(gui);

        // Show required items
        // T1 Piece slot (10)
        gui.setSlot(10, new GuiElementBuilder(Items.ARMOR_STAND)
                .setName(Text.literal("T1 " + recipe.pieceName).formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Required: T1 " + recipe.type.displayName + " " + recipe.pieceName))
                .build());

        // Cores slot (12)
        gui.setSlot(12, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal(recipe.coresRequired + "x " + recipe.type.displayName + " Cores")
                        .formatted(Formatting.AQUA))
                .addLoreLine(Text.literal("Obtained from killing " + recipe.type.bossName))
                .build());

        // Special material slot (14)
        gui.setSlot(14, new GuiElementBuilder(recipe.specialMaterial.getItem())
                .setName(Text.literal(recipe.specialMaterialCount + "x " +
                                recipe.specialMaterial.getName().getString())
                        .formatted(Formatting.LIGHT_PURPLE))
                .setCount(recipe.specialMaterialCount)
                .build());

        // Arrow
        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("▼").formatted(Formatting.WHITE))
                .build());

        // Result preview (31)
        ItemStack result = T2CraftingHandler.craft(player, recipe);
        gui.setSlot(31, new GuiElementBuilder(result.getItem())
                .setName(result.get(DataComponentTypes.CUSTOM_NAME))
                .hideDefaultTooltip()
                .build());

        // Check if player has materials
        boolean hasMaterials = checkPlayerHasMaterials(player, recipe);

        // Craft button (40)
        if (hasMaterials) {
            gui.setSlot(40, new GuiElementBuilder(Items.LIME_CONCRETE)
                    .setName(Text.literal("✓ CRAFT").formatted(Formatting.GREEN, Formatting.BOLD))
                    .addLoreLine(Text.literal("Click to craft!").formatted(Formatting.YELLOW))
                    .setCallback((idx, clickType, action) -> {
                        if (consumeMaterials(player, recipe)) {
                            ItemStack crafted = T2CraftingHandler.craft(player, recipe);
                            player.giveItemStack(crafted);
                            player.sendMessage(Text.literal("§a§l✓ Crafted " +
                                    crafted.get(DataComponentTypes.CUSTOM_NAME).getString() + "!"), false);
                            player.getEntityWorld().playSound(null, player.getBlockPos(),
                                    SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                            gui.close();
                        }
                    })
                    .build());
        } else {
            gui.setSlot(40, new GuiElementBuilder(Items.RED_CONCRETE)
                    .setName(Text.literal("✗ Missing Materials").formatted(Formatting.RED, Formatting.BOLD))
                    .addLoreLine(Text.literal("Gather required items first").formatted(Formatting.GRAY))
                    .build());
        }

        // Back button
        gui.setSlot(36, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Back").formatted(Formatting.GRAY))
                .setCallback((idx, clickType, action) -> {
                    openTypeMenu(player, recipe.type);
                })
                .build());

        gui.open();
    }

    private static boolean checkPlayerHasMaterials(ServerPlayerEntity player, T2CraftingHandler.T2Recipe recipe) {
        // Check T1 piece
        boolean hasT1 = false;
        int coreCount = 0;
        int specialCount = 0;

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            // Check T1 piece
            Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (name != null) {
                String n = name.getString();
                if (!n.contains(" II") && n.contains(recipe.pieceName)) {
                    boolean typeMatch = switch (recipe.type) {
                        case ZOMBIE -> n.contains("Undying") || n.contains("Outlaw");
                        case SPIDER -> n.contains("Venomous") || n.contains("Bandit");
                        case SKELETON -> n.contains("Bone") || n.contains("Desperado");
                        case SLIME -> n.contains("Gelatinous") || n.contains("Rustler");
                        case ENDERMAN -> n.contains("Void") || n.contains("Phantom");
                        case WARDEN -> n.contains("Sculk") || n.contains("Terror");
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
            if (stack.isOf(recipe.specialMaterial.getItem())) {
                specialCount += stack.getCount();
            }
        }

        return hasT1 && coreCount >= recipe.coresRequired && specialCount >= recipe.specialMaterialCount;
    }

    private static boolean consumeMaterials(ServerPlayerEntity player, T2CraftingHandler.T2Recipe recipe) {
        // Consume T1 piece
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (name != null) {
                String n = name.getString();
                if (!n.contains(" II") && n.contains(recipe.pieceName)) {
                    boolean typeMatch = switch (recipe.type) {
                        case ZOMBIE -> n.contains("Undying") || n.contains("Outlaw");
                        case SPIDER -> n.contains("Venomous") || n.contains("Bandit");
                        case SKELETON -> n.contains("Bone") || n.contains("Desperado");
                        case SLIME -> n.contains("Gelatinous") || n.contains("Rustler");
                        case ENDERMAN -> n.contains("Void") || n.contains("Phantom");
                        case WARDEN -> n.contains("Sculk") || n.contains("Terror");
                    };
                    if (typeMatch) {
                        player.getInventory().removeStack(i, 1);
                        break;
                    }
                }
            }
        }

        // Consume cores
        int coresToConsume = recipe.coresRequired;
        for (int i = 0; i < player.getInventory().size() && coresToConsume > 0; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (SlayerItems.isSlayerCore(stack)) {
                SlayerManager.SlayerType coreType = SlayerItems.getCoreType(stack);
                if (coreType == recipe.type) {
                    int toRemove = Math.min(stack.getCount(), coresToConsume);
                    stack.decrement(toRemove);
                    coresToConsume -= toRemove;
                }
            }
        }

        // Consume special material
        int specialToConsume = recipe.specialMaterialCount;
        for (int i = 0; i < player.getInventory().size() && specialToConsume > 0; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(recipe.specialMaterial.getItem())) {
                int toRemove = Math.min(stack.getCount(), specialToConsume);
                stack.decrement(toRemove);
                specialToConsume -= toRemove;
            }
        }

        return true;
    }

    private static void fillBackground(SimpleGui gui) {
        ItemStack glass = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        glass.set(DataComponentTypes.CUSTOM_NAME, Text.literal(""));
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                    .setName(Text.literal(""))
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
            case WARDEN -> new ItemStack(Items.SCULK);
        };
    }
}