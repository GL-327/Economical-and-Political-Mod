package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportGui {

    public static void openMainMenu(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(Component.literal("📋 Submit a Report"));

        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.purple())
                        .setName(Component.literal("")).build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.STAINED_GLASS_PANE.gray())
                        .setName(Component.literal("")).build());
            }
        }

        gui.setSlot(4, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Component.literal("📋 Report System").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Select a report type below").withStyle(ChatFormatting.YELLOW))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Your report will be saved").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("and reviewed by staff").withStyle(ChatFormatting.GRAY))
                .glow()
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.literal("👤 Report a Player").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Report cheating, harassment,").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("or rule violations").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to report player").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openPlayerReport(player))
                .build());

        gui.setSlot(24, new GuiElementBuilder(Items.BEE_SPAWN_EGG)
                .setName(Component.literal("🐛 Report a Bug").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Report game bugs, glitches,").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("or broken features").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal(""))
                .addLoreLine(Component.literal("Click to report bug").withStyle(ChatFormatting.YELLOW))
                .setCallback((index, type, containerInput, slotGui) -> openBugReport(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Component.literal("← Back").withStyle(ChatFormatting.GRAY))
                .addLoreLine(Component.literal("Go back").withStyle(ChatFormatting.DARK_GRAY))
                .setCallback((index, type, containerInput, slotGui) -> player.closeContainer())
                .build());

        gui.open();
    }

    private static void openPlayerReport(ServerPlayer player) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String playerName = this.getLine(0).getString().trim();
                String reason = this.getLine(1).getString().trim();
                
                if (playerName.isEmpty() || reason.isEmpty()) {
                    player.sendSystemMessage(Component.literal("✗ Please fill in all fields!").withStyle(ChatFormatting.RED));
                    openMainMenu(player);
                    return;
                }
                
                saveReport(player, "PLAYER", playerName, reason);
                player.sendSystemMessage(Component.literal("✅ Player report submitted!").withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.literal("Player: " + playerName + " | Reason: " + reason).withStyle(ChatFormatting.GRAY));
            }
        };
        
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal(""));
        signGui.setLine(2, Component.literal("L1=Name L2=Reason").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("Close to submit").withStyle(ChatFormatting.GRAY));
        signGui.open();
    }

    private static void openBugReport(ServerPlayer player) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onManualClose() {
                String bugType = this.getLine(0).getString().trim();
                String description = this.getLine(1).getString().trim();
                
                if (bugType.isEmpty() || description.isEmpty()) {
                    player.sendSystemMessage(Component.literal("✗ Please fill in all fields!").withStyle(ChatFormatting.RED));
                    openMainMenu(player);
                    return;
                }
                
                saveReport(player, "BUG", bugType, description);
                player.sendSystemMessage(Component.literal("✅ Bug report submitted!").withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.literal("Bug: " + bugType + " | Description: " + description).withStyle(ChatFormatting.GRAY));
            }
        };
        
        signGui.setLine(0, Component.literal(""));
        signGui.setLine(1, Component.literal(""));
        signGui.setLine(2, Component.literal("L1=Type L2=Details").withStyle(ChatFormatting.AQUA));
        signGui.setLine(3, Component.literal("Close to submit").withStyle(ChatFormatting.GRAY));
        signGui.open();
    }

    private static void saveReport(ServerPlayer player, String type, String subject, String details) {
        try {
            File reportDir = new File("config/political/reports");
            if (!reportDir.exists()) {
                reportDir.mkdirs();
            }
            
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String filename = "report_" + timestamp + "_" + player.getUUID().toString() + ".txt";
            File reportFile = new File(reportDir, filename);
            
            try (FileWriter writer = new FileWriter(reportFile)) {
                writer.write("=== REPORT ===\n");
                writer.write("Type: " + type + "\n");
                writer.write("Reporter: " + player.getName().getString() + " (" + player.getUUID().toString() + ")\n");
                writer.write("Subject: " + subject + "\n");
                writer.write("Details: " + details + "\n");
                writer.write("Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
                writer.write("Location: " + player.getX() + ", " + player.getY() + ", " + player.getZ() + "\n");
                writer.write("World: " + player.level().dimension().identifier().toString() + "\n");
            }
            
            PoliticalServer.LOGGER.info("Report saved: " + filename + " by " + player.getName().getString());
            
        } catch (IOException e) {
            PoliticalServer.LOGGER.error("Failed to save report", e);
            player.sendSystemMessage(Component.literal("✗ Failed to save report!").withStyle(ChatFormatting.RED));
        }
    }
}
