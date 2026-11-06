package com.political.recipe;

/** Vanilla 26.3 gear silhouettes. Cores fill armor holes or replace a tool handle. */
public final class GearPatterns {
    private GearPatterns() {}

    public static String[] armor(String piece) {
        return switch (piece) {
            case "helmet" -> cells("MMM", "MCM", "   ");
            case "chestplate" -> cells("MCM", "MMM", "MMM");
            case "leggings" -> cells("MMM", "MCM", "M M");
            case "boots" -> cells("   ", "MCM", "M M");
            default -> throw new IllegalArgumentException("Unknown armor piece: " + piece);
        };
    }

    public static String[] tool(String piece) {
        return switch (piece) {
            case "sword" -> cells(" M ", " M ", " C ");
            case "pickaxe" -> cells("MMM", " C ", " S ");
            case "axe" -> cells("MM ", "MC ", " S ");
            case "shovel" -> cells(" M ", " C ", " S ");
            case "hoe" -> cells("MM ", " C ", " S ");
            default -> throw new IllegalArgumentException("Unknown tool: " + piece);
        };
    }

    private static String[] cells(String... rows) {
        String[] result = new String[9];
        for (int i = 0; i < 9; i++) result[i] = switch (rows[i / 3].charAt(i % 3)) {
            case 'M' -> "block";
            case 'C' -> "core";
            case 'S' -> "stick";
            default -> "";
        };
        return result;
    }
}
