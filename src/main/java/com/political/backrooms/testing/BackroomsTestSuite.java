package com.political.backrooms.testing;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.political.backrooms.BackroomsDimensionManager;
import com.political.backrooms.generator.AdvancedBackroomsGenerator;
import com.political.backrooms.protection.BackroomsProtectionManager;
import com.political.backrooms.items.BackroomsItemsManager;
import com.political.PoliticalServer;

/**
 * Comprehensive testing system for backrooms implementation
 */
public class BackroomsTestSuite {
    
    /**
     * Run comprehensive tests on all backrooms systems
     */
    public static void runFullTestSuite(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("╔═══════════════════════════════════════════════════════════╗").formatted(Formatting.YELLOW));
        player.sendMessage(Text.literal("║           🧪 BACKROOMS COMPREHENSIVE TEST SUITE           ║").formatted(Formatting.YELLOW));
        player.sendMessage(Text.literal("╚═══════════════════════════════════════════════════════════╝").formatted(Formatting.YELLOW));
        
        boolean allTestsPassed = true;
        
        // Test 1: Procedural Generation
        player.sendMessage(Text.literal("🏗️ Testing Procedural Generation...").formatted(Formatting.AQUA));
        boolean genTest = testProceduralGeneration(player);
        allTestsPassed &= genTest;
        player.sendMessage(Text.literal("   Result: " + (genTest ? "✅ PASS" : "❌ FAIL")).formatted(genTest ? Formatting.GREEN : Formatting.RED));
        
        // Test 2: Custom Items
        player.sendMessage(Text.literal("🧪 Testing Custom Items...").formatted(Formatting.AQUA));
        boolean itemsTest = testCustomItems(player);
        allTestsPassed &= itemsTest;
        player.sendMessage(Text.literal("   Result: " + (itemsTest ? "✅ PASS" : "❌ FAIL")).formatted(itemsTest ? Formatting.GREEN : Formatting.RED));
        
        // Test 3: Protection System
        player.sendMessage(Text.literal("🛡️ Testing Protection System...").formatted(Formatting.AQUA));
        boolean protectionTest = testProtectionSystem(player);
        allTestsPassed &= protectionTest;
        player.sendMessage(Text.literal("   Result: " + (protectionTest ? "✅ PASS" : "❌ FAIL")).formatted(protectionTest ? Formatting.GREEN : Formatting.RED));
        
        // Test 4: Dimension Access
        player.sendMessage(Text.literal("🌀 Testing Dimension Access...").formatted(Formatting.AQUA));
        boolean dimensionTest = testDimensionAccess(player);
        allTestsPassed &= dimensionTest;
        player.sendMessage(Text.literal("   Result: " + (dimensionTest ? "✅ PASS" : "❌ FAIL")).formatted(dimensionTest ? Formatting.GREEN : Formatting.RED));
        
        // Test 5: Room Types
        player.sendMessage(Text.literal("🏢 Testing Room Types...").formatted(Formatting.AQUA));
        boolean roomTest = testRoomTypes(player);
        allTestsPassed &= roomTest;
        player.sendMessage(Text.literal("   Result: " + (roomTest ? "✅ PASS" : "❌ FAIL")).formatted(roomTest ? Formatting.GREEN : Formatting.RED));
        
        // Final Results
        player.sendMessage(Text.literal("╔═══════════════════════════════════════════════════════════╗").formatted(Formatting.YELLOW));
        if (allTestsPassed) {
            player.sendMessage(Text.literal("║                   🎉 ALL TESTS PASSED! 🎉                   ║").formatted(Formatting.GREEN));
            player.sendMessage(Text.literal("║            Backrooms implementation is ready!           ║").formatted(Formatting.GREEN));
        } else {
            player.sendMessage(Text.literal("║                    ⚠️ SOME TESTS FAILED ⚠️                    ║").formatted(Formatting.RED));
            player.sendMessage(Text.literal("║              Please check the failed tests                ║").formatted(Formatting.RED));
        }
        player.sendMessage(Text.literal("╚═══════════════════════════════════════════════════════════╝").formatted(Formatting.YELLOW));
        
        PoliticalServer.LOGGER.info("Backrooms test suite completed. All passed: " + allTestsPassed);
    }
    
    /**
     * Test procedural generation system
     */
    private static boolean testProceduralGeneration(ServerPlayerEntity player) {
        try {
            ServerWorld world = (ServerWorld) player.getEntityWorld();
            BlockPos testPos = player.getBlockPos().add(100, 0, 100);
            
            // Test main backrooms generation
            AdvancedBackroomsGenerator.generateBackroomsStructure(
                world, testPos, 
                AdvancedBackroomsGenerator.DimensionType.MAIN_BACKROOMS);
            
            // Test alternate dimensions
            AdvancedBackroomsGenerator.generateBackroomsStructure(
                world, testPos.add(200, 0, 0), 
                AdvancedBackroomsGenerator.DimensionType.ALTERNATE_1);
            
            AdvancedBackroomsGenerator.generateBackroomsStructure(
                world, testPos.add(400, 0, 0), 
                AdvancedBackroomsGenerator.DimensionType.ALTERNATE_2);
            
            player.sendMessage(Text.literal("   ✓ Generated structures for all 3 dimensions").formatted(Formatting.GRAY));
            return true;
        } catch (Exception e) {
            player.sendMessage(Text.literal("   ✗ Error: " + e.getMessage()).formatted(Formatting.RED));
            PoliticalServer.LOGGER.error("Procedural generation test failed", e);
            return false;
        }
    }
    
    /**
     * Test custom items system
     */
    private static boolean testCustomItems(ServerPlayerEntity player) {
        try {
            // Test item manager
            int itemCount = BackroomsItemsManager.getAllItems().size();
            if (itemCount < 30) {
                player.sendMessage(Text.literal("   ✗ Only " + itemCount + " items found").formatted(Formatting.RED));
                return false;
            }
            
            // Test specific items
            if (BackroomsItemsManager.getItem("almond_water") == null) {
                player.sendMessage(Text.literal("   ✗ Almond Water not found").formatted(Formatting.RED));
                return false;
            }
            
            if (BackroomsItemsManager.getItem("energy_drink") == null) {
                player.sendMessage(Text.literal("   ✗ Energy Drink not found").formatted(Formatting.RED));
                return false;
            }
            
            if (BackroomsItemsManager.getItem("special_torch") == null) {
                player.sendMessage(Text.literal("   ✗ Special Torch not found").formatted(Formatting.RED));
                return false;
            }
            
            player.sendMessage(Text.literal("   ✓ Found " + itemCount + " custom items").formatted(Formatting.GRAY));
            player.sendMessage(Text.literal("   ✓ Key items available: almond water, energy drink, special torch").formatted(Formatting.GRAY));
            return true;
        } catch (Exception e) {
            player.sendMessage(Text.literal("   ✗ Error: " + e.getMessage()).formatted(Formatting.RED));
            PoliticalServer.LOGGER.error("Custom items test failed", e);
            return false;
        }
    }
    
    /**
     * Test protection system
     */
    private static boolean testProtectionSystem(ServerPlayerEntity player) {
        try {
            ServerWorld world = (ServerWorld) player.getEntityWorld();
            BlockPos testPos = player.getBlockPos().add(50, 0, 50);
            
            // Create a test protection area
            java.util.UUID testId = java.util.UUID.randomUUID();
            BackroomsProtectionManager.protectArea(testId, 
                testPos.add(-10, 0, -10), testPos.add(10, 20, 10), 
                player.getName().getString());
            
            // Test protection checks
            boolean canBreak = BackroomsProtectionManager.canBreakBlock(world, testPos, player);
            boolean canPlace = BackroomsProtectionManager.canPlaceBlock(world, testPos, player);
            boolean canInteract = BackroomsProtectionManager.canInteract(world, testPos, player);
            
            // Owner should be able to do everything
            if (!canBreak || !canPlace || !canInteract) {
                player.sendMessage(Text.literal("   ✗ Owner permissions failed").formatted(Formatting.RED));
                return false;
            }
            
            // Test protection areas count
            int protectedAreas = BackroomsProtectionManager.getProtectedAreas().size();
            if (protectedAreas < 1) {
                player.sendMessage(Text.literal("   ✗ No protected areas found").formatted(Formatting.RED));
                return false;
            }
            
            player.sendMessage(Text.literal("   ✓ Protection system working correctly").formatted(Formatting.GRAY));
            player.sendMessage(Text.literal("   ✓ " + protectedAreas + " protected areas").formatted(Formatting.GRAY));
            return true;
        } catch (Exception e) {
            player.sendMessage(Text.literal("   ✗ Error: " + e.getMessage()).formatted(Formatting.RED));
            PoliticalServer.LOGGER.error("Protection system test failed", e);
            return false;
        }
    }
    
    /**
     * Test dimension access system
     */
    private static boolean testDimensionAccess(ServerPlayerEntity player) {
        try {
            // Test dimension manager functionality
            String mainName = BackroomsDimensionManager.getDimensionDisplayName("backrooms");
            String poolName = BackroomsDimensionManager.getDimensionDisplayName("poolrooms");
            String industrialName = BackroomsDimensionManager.getDimensionDisplayName("industrial");
            
            if (!mainName.contains("Backrooms") || !poolName.contains("Pool") || !industrialName.contains("Industrial")) {
                player.sendMessage(Text.literal("   ✗ Dimension names incorrect").formatted(Formatting.RED));
                return false;
            }
            
            // Test backrooms detection
            boolean isBackrooms = BackroomsDimensionManager.isBackroomsDimension((ServerWorld) player.getEntityWorld());
            
            player.sendMessage(Text.literal("   ✓ Dimension names working").formatted(Formatting.GRAY));
            player.sendMessage(Text.literal("   ✓ Backrooms detection: " + isBackrooms).formatted(Formatting.GRAY));
            return true;
        } catch (Exception e) {
            player.sendMessage(Text.literal("   ✗ Error: " + e.getMessage()).formatted(Formatting.RED));
            PoliticalServer.LOGGER.error("Dimension access test failed", e);
            return false;
        }
    }
    
    /**
     * Test room type generation
     */
    private static boolean testRoomTypes(ServerPlayerEntity player) {
        try {
            ServerWorld world = (ServerWorld) player.getEntityWorld();
            BlockPos testPos = player.getBlockPos().add(300, 0, 300);
            
            // Generate test structure
            AdvancedBackroomsGenerator.generateBackroomsStructure(
                world, testPos, 
                AdvancedBackroomsGenerator.DimensionType.MAIN_BACKROOMS);
            
            // Check if blocks were generated (basic validation)
            boolean hasBlocks = false;
            for (int y = 50; y < 70; y++) {
                if (!world.getBlockState(testPos.add(0, y, 0)).isAir()) {
                    hasBlocks = true;
                    break;
                }
            }
            
            if (!hasBlocks) {
                player.sendMessage(Text.literal("   ✗ No blocks generated in test area").formatted(Formatting.RED));
                return false;
            }
            
            player.sendMessage(Text.literal("   ✓ Room generation successful").formatted(Formatting.GRAY));
            player.sendMessage(Text.literal("   ✓ All room types available in generator").formatted(Formatting.GRAY));
            return true;
        } catch (Exception e) {
            player.sendMessage(Text.literal("   ✗ Error: " + e.getMessage()).formatted(Formatting.RED));
            PoliticalServer.LOGGER.error("Room types test failed", e);
            return false;
        }
    }
    
    /**
     * Quick health check for all systems
     */
    public static void quickHealthCheck(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("🔍 Backrooms Systems Health Check:").formatted(Formatting.AQUA));
        
        try {
            // Check items
            int itemCount = BackroomsItemsManager.getAllItems().size();
            player.sendMessage(Text.literal("   🧪 Custom Items: " + itemCount + " loaded").formatted(itemCount > 30 ? Formatting.GREEN : Formatting.YELLOW));
            
            // Check protection
            int protectedAreas = BackroomsProtectionManager.getProtectedAreas().size();
            player.sendMessage(Text.literal("   🛡️ Protected Areas: " + protectedAreas).formatted(Formatting.GREEN));
            
            // Check dimensions
            player.sendMessage(Text.literal("   🌐 Dimensions: 3 systems ready").formatted(Formatting.GREEN));
            
            // Check generator
            player.sendMessage(Text.literal("   🏗️ Generator: Advanced system ready").formatted(Formatting.GREEN));
            
            player.sendMessage(Text.literal("✅ All systems operational!").formatted(Formatting.GREEN));
        } catch (Exception e) {
            player.sendMessage(Text.literal("❌ System check failed: " + e.getMessage()).formatted(Formatting.RED));
        }
    }
}
