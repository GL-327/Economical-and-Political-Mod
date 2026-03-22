package com.political;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportGui {

    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("📋 Submit a Report"));

        for (int i = 0; i < 54; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i < 9 || i >= 45) {
                gui.setSlot(i, new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE)
                        .setName(Text.literal("")).build());
            } else {
                gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
                        .setName(Text.literal("")).build());
            }
        }

        gui.setSlot(4, new GuiElementBuilder(Items.WRITABLE_BOOK)
                .setName(Text.literal("📋 Report System").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Select a report type below").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Your report will be saved").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("and reviewed by staff").formatted(Formatting.GRAY))
                .glow()
                .build());

        gui.setSlot(20, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("👤 Report a Player").formatted(Formatting.RED, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Report cheating, harassment,").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("or rule violations").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to report player").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openPlayerReport(player))
                .build());

        gui.setSlot(24, new GuiElementBuilder(Items.BEE_SPAWN_EGG)
                .setName(Text.literal("🐛 Report a Bug").formatted(Formatting.GREEN, Formatting.BOLD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Report game bugs, glitches,").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("or broken features").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to report bug").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> openBugReport(player))
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("← Back").formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Go back").formatted(Formatting.DARK_GRAY))
                .setCallback((index, type, action) -> player.closeHandledScreen())
                .build());

        gui.open();
    }

    private static void openPlayerReport(ServerPlayerEntity player) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String playerName = this.getLine(0).getString().trim();
                String reason = this.getLine(1).getString().trim();
                
                if (playerName.isEmpty() || reason.isEmpty()) {
                    player.sendMessage(Text.literal("✗ Please fill in all fields!").formatted(Formatting.RED));
                    openMainMenu(player);
                    return;
                }
                
                saveReport(player, "PLAYER", playerName, reason);
                player.sendMessage(Text.literal("✅ Player report submitted!").formatted(Formatting.GREEN));
                player.sendMessage(Text.literal("Player: " + playerName + " | Reason: " + reason).formatted(Formatting.GRAY));
            }
        };
        
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("Enter player name:"));
        signGui.setLine(2, Text.literal(""));
        signGui.setLine(3, Text.literal("Reason:"));
        signGui.open();
    }

    private static void openBugReport(ServerPlayerEntity player) {
        SignGui signGui = new SignGui(player) {
            @Override
            public void onClose() {
                String bugType = this.getLine(0).getString().trim();
                String description = this.getLine(1).getString().trim();
                
                if (bugType.isEmpty() || description.isEmpty()) {
                    player.sendMessage(Text.literal("✗ Please fill in all fields!").formatted(Formatting.RED));
                    openMainMenu(player);
                    return;
                }
                
                saveReport(player, "BUG", bugType, description);
                player.sendMessage(Text.literal("✅ Bug report submitted!").formatted(Formatting.GREEN));
                player.sendMessage(Text.literal("Bug: " + bugType + " | Description: " + description).formatted(Formatting.GRAY));
            }
        };
        
        signGui.setLine(0, Text.literal(""));
        signGui.setLine(1, Text.literal("Bug type/area:"));
        signGui.setLine(2, Text.literal(""));
        signGui.setLine(3, Text.literal("Description:"));
        signGui.open();
    }

    private static void saveReport(ServerPlayerEntity player, String type, String subject, String details) {
        try {
            File reportDir = new File("config/political/reports");
            if (!reportDir.exists()) {
                reportDir.mkdirs();
            }
            
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String filename = "report_" + timestamp + "_" + player.getUuidAsString() + ".txt";
            File reportFile = new File(reportDir, filename);
            
            try (FileWriter writer = new FileWriter(reportFile)) {
                writer.write("=== REPORT ===\n");
                writer.write("Type: " + type + "\n");
                writer.write("Reporter: " + player.getName().getString() + " (" + player.getUuidAsString() + ")\n");
                writer.write("Subject: " + subject + "\n");
                writer.write("Details: " + details + "\n");
                writer.write("Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
                writer.write("Location: " + player.getX() + ", " + player.getY() + ", " + player.getZ() + "\n");
                writer.write("World: " + player.getEntityWorld().getRegistryKey().getValue() + "\n");
            }
            
            PoliticalServer.LOGGER.info("Report saved: " + filename + " by " + player.getName().getString());
            
        } catch (IOException e) {
            PoliticalServer.LOGGER.error("Failed to save report", e);
            player.sendMessage(Text.literal("✗ Failed to save report!").formatted(Formatting.RED));
        }
    }
}
