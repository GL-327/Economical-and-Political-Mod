package com.political;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class ShopItemGui {

    private static final int ITEMS_PER_PAGE = 45;
    /** When true, list is enchanted block gear (custom ids ending in _sbl_ench). */
    private static boolean enchantedBlockGearMode = false;

    public static void open(ServerPlayer player) {
        openPage(player, 0, "");
    }

    public static void openPage(ServerPlayer player, int page, String filter) {
        if (enchantedBlockGearMode) {
            openEnchantedPage(player, page, filter);
        } else {
            openVanillaPage(player, page, filter);
        }
    }

    private static void openVanillaPage(ServerPlayer player, int page, String filter) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Shop Item Manager (vanilla)").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        List<Item> allItems = new ArrayList<>();
        for (String category : ShopManager.getCategoryNames()) {
            allItems.addAll(ShopManager.getItemsInCategory(category));
        }

        List<Item> filteredItems = new ArrayList<>();
        for (Item item : allItems) {
            String name = new ItemStack(item).getHoverName().getString().toLowerCase();
            String id = item.toString().toLowerCase();
            if (filter.isEmpty() || name.contains(filter.toLowerCase()) || id.contains(filter.toLowerCase())) {
                filteredItems.add(item);
            }
        }

        int totalPages = Math.max(1, (int) Math.ceil(filteredItems.size() / (double) ITEMS_PER_PAGE));
        int startIdx = page * ITEMS_PER_PAGE;
        int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, filteredItems.size());

        for (int i = 0; i < 54; i++) {
            if (i >= 45 && i < 54) {
                continue;
            }
            int itemIdx = startIdx + i;
            if (itemIdx < endIdx) {
                Item item = filteredItems.get(itemIdx);
                boolean disabled = ShopManager.isDisabled(item);
                int sellPrice = ShopManager.getSellPrice(item);
                int buyPrice = ShopManager.getBuyPrice(item);

                GuiElementBuilder builder = new GuiElementBuilder(item)
                        .setName(Component.literal((disabled ? "§c" : "§a") + new ItemStack(item).getHoverName().getString()))
                        .addLoreLine(Component.literal("§7Sell: §e" + sellPrice + " coins"))
                        .addLoreLine(Component.literal("§7Buy: §e" + buyPrice + " coins"))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal(disabled ? "§cDISABLED" : "§aENABLED"))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("§eLeft: " + (disabled ? "Enable" : "Disable")))
                        .addLoreLine(Component.literal("§bRight: Details + sign price"))
                        .glow(!disabled);

                final Item finalItem = item;
                builder.setCallback((index, type, containerInput, slotGui) -> {
                    if (type == ClickType.MOUSE_LEFT) {
                        if (disabled) {
                            ShopManager.enableItem(finalItem);
                            player.sendSystemMessage(Component.literal("§aEnabled " + finalItem.getName(new ItemStack(finalItem)).getString() + " in shop."), false);
                        } else {
                            ShopManager.disableItem(finalItem);
                            player.sendSystemMessage(Component.literal("§cDisabled " + finalItem.getName(new ItemStack(finalItem)).getString() + " from shop."), false);
                        }
                        openPage(player, page, filter);
                    } else if (type == ClickType.MOUSE_RIGHT) {
                        showVanillaItemDetails(player, finalItem, page, filter);
                    }
                });

                gui.setSlot(i, builder.build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal("")).build());
            }
        }

        addBottomBarVanilla(gui, player, page, filter, totalPages, filteredItems.size());
        gui.open();
    }

    private static void openEnchantedPage(ServerPlayer player, int page, String filter) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("Shop Item Manager (enchanted block gear)").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));

        List<ItemStack> all = EnchantedShopManager.allEnchantedBlockStacks();
        String f = filter.toLowerCase();
        List<ItemStack> filtered = new ArrayList<>();
        for (ItemStack s : all) {
            String cid = SlayerItems.getCustomItemId(s);
            Component cn = s.get(DataComponents.CUSTOM_NAME);
            String name = cn != null ? cn.getString().toLowerCase() : "";
            String idStr = cid != null ? cid.toLowerCase() : "";
            if (f.isEmpty() || name.contains(f) || idStr.contains(f)) {
                filtered.add(s);
            }
        }

        int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) ITEMS_PER_PAGE));
        int p = Math.min(Math.max(0, page), totalPages - 1);
        int startIdx = p * ITEMS_PER_PAGE;
        int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, filtered.size());

        for (int i = 0; i < 54; i++) {
            if (i >= 45 && i < 54) {
                continue;
            }
            int idx = startIdx + i;
            if (idx < endIdx) {
                ItemStack proto = filtered.get(idx);
                String cid = SlayerItems.getCustomItemId(proto);
                boolean disabled = cid != null && EnchantedShopManager.isDisabled(cid);
                int sell = EnchantedShopManager.getSellPrice(proto);
                int buy = EnchantedShopManager.getBuyPrice(proto);
                int ov = cid != null ? EnchantedShopManager.getSellOverrideOrZero(cid) : 0;
                Component disp = proto.get(DataComponents.CUSTOM_NAME);

                GuiElementBuilder b = new GuiElementBuilder(proto.getItem())
                        .setName(disp != null ? disp : Component.literal(cid != null ? cid : "?"))
                        .addLoreLine(Component.literal("§7Id: §f" + (cid != null ? cid : "?")))
                        .addLoreLine(Component.literal("§7Sell value: §e" + sell + " §8(buy " + buy + ")"))
                        .addLoreLine(ov > 0 ? Component.literal("§7Override sell: §6" + ov) : Component.literal("§7Override: §8default formula"))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal(disabled ? "§cDISABLED" : "§aENABLED"))
                        .addLoreLine(Component.literal(""))
                        .addLoreLine(Component.literal("§eLeft: " + (disabled ? "Enable" : "Disable")))
                        .addLoreLine(Component.literal("§bRight: Details + sign price"))
                        .glow(!disabled);

                final ItemStack stackCopy = proto.copy();
                b.setCallback((index, type, containerInput, slotGui) -> {
                    String id = SlayerItems.getCustomItemId(stackCopy);
                    if (id == null) return;
                    if (type == ClickType.MOUSE_LEFT) {
                        EnchantedShopManager.setDisabled(id, !EnchantedShopManager.isDisabled(id));
                        player.sendSystemMessage(Component.literal(EnchantedShopManager.isDisabled(id) ? "Disabled in shop: " + id : "Enabled in shop: " + id)
                                .withStyle(EnchantedShopManager.isDisabled(id) ? ChatFormatting.RED : ChatFormatting.GREEN), false);
                        openPage(player, p, filter);
                    } else if (type == ClickType.MOUSE_RIGHT) {
                        showEnchantedDetails(player, stackCopy, p, filter);
                    }
                });
                gui.setSlot(i, b.build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal("")).build());
            }
        }

        addBottomBarEnchanted(gui, player, p, filter, totalPages, filtered.size());
        gui.open();
    }

    private static void addBottomBarVanilla(SimpleGui gui, ServerPlayer player, int page, String filter, int totalPages, int count) {
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("§ePrevious Page"))
                    .addLoreLine(Component.literal("§7Page " + (page + 1) + " of " + totalPages))
                    .setCallback((index, type, containerInput, slotGui) -> openPage(player, page - 1, filter))
                    .build());
        } else {
            gui.setSlot(45, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal("§7First Page")).build());
        }

        gui.setSlot(46, new GuiElementBuilder(Items.LAPIS_LAZULI)
                .setName(Component.literal("§dEnchanted block gear").withStyle(ChatFormatting.LIGHT_PURPLE))
                .addLoreLine(Component.literal("§7Click to manage Skyblock-style gear"))
                .setCallback((i, t, containerInput, slotGui) -> {
                    enchantedBlockGearMode = true;
                    openPage(player, 0, filter);
                })
                .build());

        gui.setSlot(47, new GuiElementBuilder(Items.OAK_SIGN)
                .setName(Component.literal("§eSearch (sign)"))
                .addLoreLine(Component.literal("§7Filter the list"))
                .setCallback((i, t, containerInput, slotGui) -> openSearchSign(player, page, filter, false))
                .build());

        gui.setSlot(48, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§cClose"))
                .setCallback((index, type, containerInput, slotGui) -> gui.close())
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("§6Page " + (page + 1) + " / " + totalPages))
                .addLoreLine(Component.literal("§7Items: " + count))
                .build());

        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("§eNext Page"))
                    .setCallback((index, type, containerInput, slotGui) -> openPage(player, page + 1, filter))
                    .build());
        } else {
            gui.setSlot(53, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal("§7Last Page")).build());
        }
    }

    private static void addBottomBarEnchanted(SimpleGui gui, ServerPlayer player, int page, String filter, int totalPages, int count) {
        if (page > 0) {
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("§ePrevious Page"))
                    .addLoreLine(Component.literal("§7Page " + (page + 1) + " of " + totalPages))
                    .setCallback((index, type, containerInput, slotGui) -> openPage(player, page - 1, filter))
                    .build());
        } else {
            gui.setSlot(45, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal("§7First Page")).build());
        }

        gui.setSlot(46, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Component.literal("§6Vanilla shop items"))
                .addLoreLine(Component.literal("§7Back to normal /shop prices"))
                .setCallback((i, t, containerInput, slotGui) -> {
                    enchantedBlockGearMode = false;
                    openPage(player, 0, filter);
                })
                .build());

        gui.setSlot(47, new GuiElementBuilder(Items.OAK_SIGN)
                .setName(Component.literal("§eSearch (sign)"))
                .setCallback((i, t, containerInput, slotGui) -> openSearchSign(player, page, filter, true))
                .build());

        gui.setSlot(48, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§cClose"))
                .setCallback((index, type, containerInput, slotGui) -> gui.close())
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.BOOK)
                .setName(Component.literal("§6Page " + (page + 1) + " / " + totalPages))
                .addLoreLine(Component.literal("§7Items: " + count))
                .build());

        if (page < totalPages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Component.literal("§eNext Page"))
                    .setCallback((index, type, containerInput, slotGui) -> openPage(player, page + 1, filter))
                    .build());
        } else {
            gui.setSlot(53, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE).setName(Component.literal("§7Last Page")).build());
        }
    }

    private static void openSearchSign(ServerPlayer player, int returnPage, String oldFilter, boolean enchanted) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String q = this.getLine(0).getString().trim();
                enchantedBlockGearMode = enchanted;
                openPage(player, 0, q);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Enter filter").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("Close to apply").withStyle(ChatFormatting.GRAY));
        signGui.open();
    }

    private static void showVanillaItemDetails(ServerPlayer player, Item item, int returnPage, String filter) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("§6Item Details"));

        boolean disabled = ShopManager.isDisabled(item);
        int sellPrice = ShopManager.getSellPrice(item);
        int buyPrice = ShopManager.getBuyPrice(item);

        gui.setSlot(4, new GuiElementBuilder(item)
                .setName(Component.literal("§6" + new ItemStack(item).getHoverName().getString()))
                .addLoreLine(Component.literal("§7ID: §f" + item))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Sell Price: §e" + sellPrice + " coins"))
                .addLoreLine(Component.literal("§7Buy Price: §e" + buyPrice + " coins"))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal(disabled ? "§cStatus: DISABLED" : "§aStatus: ENABLED"))
                .glow(!disabled)
                .build());

        gui.setSlot(10, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Component.literal("§aEnable Item"))
                .setCallback((index, type, containerInput, slotGui) -> {
                    ShopManager.enableItem(item);
                    player.sendSystemMessage(Component.literal("§aEnabled " + new ItemStack(item).getHoverName().getString() + " in shop."), false);
                    openPage(player, returnPage, filter);
                })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.OAK_SIGN)
                .setName(Component.literal("§eSet sell price (sign)"))
                .addLoreLine(Component.literal("§7Buy becomes 10× sell"))
                .setCallback((index, type, containerInput, slotGui) -> openVanillaSellPriceSign(player, item, returnPage, filter))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.BARRIER)
                .setName(Component.literal("§cReset price + enable"))
                .addLoreLine(Component.literal("§7Removes override"))
                .setCallback((index, type, containerInput, slotGui) -> {
                    ShopManager.resetItemPrice(item);
                    player.sendSystemMessage(Component.literal("§aReset " + new ItemStack(item).getHoverName().getString() + " to defaults.").withStyle(ChatFormatting.GREEN), false);
                    openPage(player, returnPage, filter);
                })
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.RED_DYE)
                .setName(Component.literal("§cDisable Item"))
                .setCallback((index, type, containerInput, slotGui) -> {
                    ShopManager.disableItem(item);
                    player.sendSystemMessage(Component.literal("§cDisabled " + new ItemStack(item).getHoverName().getString() + " from shop."), false);
                    openPage(player, returnPage, filter);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§eBack to List"))
                .setCallback((index, type, containerInput, slotGui) -> openPage(player, returnPage, filter))
                .build());

        gui.open();
    }

    private static void openVanillaSellPriceSign(ServerPlayer player, Item item, int returnPage, String filter) {
        String itemName = ShopManager.getItemName(item);
        int currentPrice = ShopManager.getSellPrice(item);

        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim();
                if (!input.isEmpty()) {
                    try {
                        int newPrice = Integer.parseInt(input);
                        if (newPrice >= 0) {
                            ShopManager.setCustomPrice(item, newPrice);
                            int buyPrice = newPrice * 10;
                            player.sendSystemMessage(Component.literal("Set " + itemName + " sell to " + newPrice + " (buy: " + buyPrice + ")")
                                    .withStyle(ChatFormatting.GREEN), false);
                        } else {
                            player.sendSystemMessage(Component.literal("Price must be 0 or positive.").withStyle(ChatFormatting.RED), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendSystemMessage(Component.literal("Invalid number: " + input).withStyle(ChatFormatting.RED), false);
                    }
                }
                enchantedBlockGearMode = false;
                showVanillaItemDetails(player, item, returnPage, filter);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Enter sell price").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("Current: " + currentPrice).withStyle(ChatFormatting.GRAY));
        signGui.open();
    }

    private static void showEnchantedDetails(ServerPlayer player, ItemStack proto, int returnPage, String filter) {
        String cid = SlayerItems.getCustomItemId(proto);
        if (cid == null) {
            openPage(player, returnPage, filter);
            return;
        }

        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Enchanted gear").withStyle(ChatFormatting.LIGHT_PURPLE));

        boolean disabled = EnchantedShopManager.isDisabled(cid);
        int sell = EnchantedShopManager.getSellPrice(proto);
        int buy = EnchantedShopManager.getBuyPrice(proto);
        int ov = EnchantedShopManager.getSellOverrideOrZero(cid);
        Component disp = proto.get(DataComponents.CUSTOM_NAME);

        gui.setSlot(4, new GuiElementBuilder(proto.getItem())
                .setName(disp != null ? disp : Component.literal(cid))
                .addLoreLine(Component.literal("§7" + cid))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("§7Sell value: §e" + sell + " §8→ buy §f" + buy))
                .addLoreLine(ov > 0 ? Component.literal("§6Manual sell override") : Component.literal("§8Default: craft + 160 lapis"))
                .addLoreLine(Component.literal(disabled ? "§cDISABLED" : "§aENABLED"))
                .glow(!disabled)
                .build());

        gui.setSlot(10, new GuiElementBuilder(Items.LIME_DYE)
                .setName(Component.literal("§aEnable"))
                .setCallback((i, t, containerInput, slotGui) -> {
                    EnchantedShopManager.setDisabled(cid, false);
                    openPage(player, returnPage, filter);
                })
                .build());

        gui.setSlot(12, new GuiElementBuilder(Items.OAK_SIGN)
                .setName(Component.literal("§eSet sell value (sign)"))
                .addLoreLine(Component.literal("§7Buy = 10× sell"))
                .setCallback((i, t, containerInput, slotGui) -> openEnchantedSellSign(player, proto, returnPage, filter))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.HOPPER)
                .setName(Component.literal("§eClear override"))
                .addLoreLine(Component.literal("§7Back to formula pricing"))
                .setCallback((i, t, containerInput, slotGui) -> {
                    EnchantedShopManager.setSellOverride(cid, 0);
                    player.sendSystemMessage(Component.literal("Cleared sell override for " + cid).withStyle(ChatFormatting.GRAY), false);
                    openPage(player, returnPage, filter);
                })
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.RED_DYE)
                .setName(Component.literal("§cDisable"))
                .setCallback((i, t, containerInput, slotGui) -> {
                    EnchantedShopManager.setDisabled(cid, true);
                    openPage(player, returnPage, filter);
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("§eBack"))
                .setCallback((i, t, containerInput, slotGui) -> openPage(player, returnPage, filter))
                .build());

        gui.open();
    }

    private static void openEnchantedSellSign(ServerPlayer player, ItemStack proto, int returnPage, String filter) {
        String cid = SlayerItems.getCustomItemId(proto);
        if (cid == null) return;
        int current = EnchantedShopManager.getSellOverrideOrZero(cid);
        if (current <= 0) {
            current = EnchantedBlockGearPricing.defaultSellFromCraft(proto);
        }

        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String input = this.getLine(0).getString().trim();
                if (!input.isEmpty()) {
                    try {
                        int newPrice = Integer.parseInt(input);
                        if (newPrice <= 0) {
                            EnchantedShopManager.setSellOverride(cid, 0);
                            player.sendSystemMessage(Component.literal("Removed override; using default formula.").withStyle(ChatFormatting.GRAY), false);
                        } else {
                            EnchantedShopManager.setSellOverride(cid, newPrice);
                            player.sendSystemMessage(Component.literal("Set enchanted gear sell value to " + newPrice + " (buy: " + (newPrice * 10) + ")")
                                    .withStyle(ChatFormatting.GREEN), false);
                        }
                    } catch (NumberFormatException e) {
                        player.sendSystemMessage(Component.literal("Invalid number: " + input).withStyle(ChatFormatting.RED), false);
                    }
                }
                enchantedBlockGearMode = true;
                showEnchantedDetails(player, proto, returnPage, filter);
            }
        };
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal("^^^^^^^^^^^^^^").withStyle(ChatFormatting.YELLOW));
        signGui.setLine(2, Component.literal("Sell value (≤0 clears)").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("Current: " + current).withStyle(ChatFormatting.GRAY));
        signGui.open();
    }
}
