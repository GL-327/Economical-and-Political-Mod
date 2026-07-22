package com.political;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple party system for MENTOR buff functionality.
 * Party members gain +5% XP when the party leader has the MENTOR buff.
 */
public class PartyManager {

    // Party data: party leader UUID -> set of member UUIDs (leader included)
    private static final Map<String, Set<String>> parties = new ConcurrentHashMap<>();
    
    // Reverse lookup: member UUID -> party leader UUID
    private static final Map<String, String> memberToParty = new ConcurrentHashMap<>();
    
    // Track players helped to level 10 for MENTOR buff obtain condition
    private static final Map<String, Set<String>> playersHelpedToLevel10 = new ConcurrentHashMap<>();

    private static Path dataFile;

    // ════════════════════════════════════════════════════════════
    // PERSISTENCE
    // ════════════════════════════════════════════════════════════

    public static void initialize(Path dataDir) {
        dataFile = dataDir.resolve("political_parties.json");
        loadData();
    }

    private static void loadData() {
        if (dataFile == null || !Files.exists(dataFile)) return;
        try {
            String content = Files.readString(dataFile);
            Gson gson = new GsonBuilder().create();
            JsonElement root = JsonParser.parseString(content);
            if (!root.isJsonObject()) return;
            JsonObject obj = root.getAsJsonObject();

            parties.clear();
            memberToParty.clear();
            playersHelpedToLevel10.clear();

            if (obj.has("parties")) {
                Type type = new TypeToken<Map<String, List<String>>>(){}.getType();
                Map<String, List<String>> loaded = gson.fromJson(obj.get("parties"), type);
                if (loaded != null) {
                    for (Map.Entry<String, List<String>> entry : loaded.entrySet()) {
                        String leaderUuid = entry.getKey();
                        Set<String> members = ConcurrentHashMap.newKeySet();
                        members.addAll(entry.getValue());
                        if (!members.contains(leaderUuid)) members.add(leaderUuid);
                        parties.put(leaderUuid, members);
                        for (String memberUuid : members) {
                            memberToParty.put(memberUuid, leaderUuid);
                        }
                    }
                }
            }

            if (obj.has("playersHelpedToLevel10")) {
                Type type = new TypeToken<Map<String, List<String>>>(){}.getType();
                Map<String, List<String>> loaded = gson.fromJson(obj.get("playersHelpedToLevel10"), type);
                if (loaded != null) {
                    for (Map.Entry<String, List<String>> entry : loaded.entrySet()) {
                        Set<String> set = ConcurrentHashMap.newKeySet();
                        set.addAll(entry.getValue());
                        playersHelpedToLevel10.put(entry.getKey(), set);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[PartyManager] Failed to load data: " + e.getMessage());
        }
    }

    public static void saveData() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject out = new JsonObject();

            Map<String, List<String>> partiesToSave = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : parties.entrySet()) {
                partiesToSave.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            out.add("parties", gson.toJsonTree(partiesToSave));

            Map<String, List<String>> helpedToSave = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : playersHelpedToLevel10.entrySet()) {
                helpedToSave.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            out.add("playersHelpedToLevel10", gson.toJsonTree(helpedToSave));

            try (Writer writer = Files.newBufferedWriter(dataFile)) {
                gson.toJson(out, writer);
            }
        } catch (Exception e) {
            System.err.println("[PartyManager] Failed to save data: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════
    // PARTY OPERATIONS
    // ════════════════════════════════════════════════════════════

    public static boolean createParty(ServerPlayer leader) {
        String leaderUuid = leader.getUUID().toString();
        if (memberToParty.containsKey(leaderUuid)) {
            leader.sendSystemMessage(Component.literal("You are already in a party!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        Set<String> members = ConcurrentHashMap.newKeySet();
        members.add(leaderUuid);
        parties.put(leaderUuid, members);
        memberToParty.put(leaderUuid, leaderUuid);
        leader.sendSystemMessage(Component.literal("§aCreated a new party! Invite players with /party invite <name>"), false);
        saveData();
        return true;
    }
    
    public static boolean invitePlayer(ServerPlayer leader, ServerPlayer target) {
        String leaderUuid = leader.getUUID().toString();
        String targetUuid = target.getUUID().toString();
        
        if (!parties.containsKey(leaderUuid)) {
            leader.sendSystemMessage(Component.literal("You don't have a party! Create one first with /party create").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        if (memberToParty.containsKey(targetUuid)) {
            leader.sendSystemMessage(Component.literal("That player is already in a party!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        // Send invitation
        target.sendSystemMessage(Component.literal("§e" + leader.getName().getString() + " has invited you to their party!"), false);
        target.sendSystemMessage(Component.literal("§aType /party accept " + leader.getName().getString() + " to join."), false);
        leader.sendSystemMessage(Component.literal("§aInvitation sent to " + target.getName().getString()), false);
        return true;
    }
    
    public static boolean acceptInvite(ServerPlayer player, ServerPlayer leader) {
        String playerUuid = player.getUUID().toString();
        String leaderUuid = leader.getUUID().toString();
        
        if (memberToParty.containsKey(playerUuid)) {
            player.sendSystemMessage(Component.literal("You are already in a party!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        Set<String> party = parties.get(leaderUuid);
        if (party == null) {
            player.sendSystemMessage(Component.literal("That party no longer exists!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        party.add(playerUuid);
        memberToParty.put(playerUuid, leaderUuid);
        
        player.sendSystemMessage(Component.literal("§aYou joined " + leader.getName().getString() + "'s party!"), false);
        
        for (String memberUuid : party) {
            ServerPlayer member = PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(memberUuid));
            if (member != null && !memberUuid.equals(playerUuid)) {
                member.sendSystemMessage(Component.literal("§b" + player.getName().getString() + " joined the party!"), false);
            }
        }
        saveData();
        return true;
    }
    
    public static boolean leaveParty(ServerPlayer player) {
        String playerUuid = player.getUUID().toString();
        String leaderUuid = memberToParty.remove(playerUuid);
        
        if (leaderUuid == null) {
            player.sendSystemMessage(Component.literal("You are not in a party!").withStyle(ChatFormatting.RED), false);
            return false;
        }
        
        Set<String> party = parties.get(leaderUuid);
        if (party != null) {
            party.remove(playerUuid);
        }
        
        // If leader leaves, disband party
        if (leaderUuid.equals(playerUuid) && party != null) {
            for (String memberUuid : party) {
                memberToParty.remove(memberUuid);
                ServerPlayer member = PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(memberUuid));
                if (member != null) {
                    member.sendSystemMessage(Component.literal("§cThe party has been disbanded!").withStyle(ChatFormatting.RED), false);
                }
            }
            parties.remove(leaderUuid);
        }
        
        player.sendSystemMessage(Component.literal("§eYou left the party."), false);
        saveData();
        return true;
    }
    
    public static String getPartyLeader(String playerUuid) {
        return memberToParty.get(playerUuid);
    }
    
    public static Set<String> getPartyMembers(String leaderUuid) {
        return parties.getOrDefault(leaderUuid, Collections.emptySet());
    }
    
    public static boolean isInParty(String playerUuid) {
        return memberToParty.containsKey(playerUuid);
    }
    
    /**
     * Gets the party XP bonus for a player.
     * Returns 0.05 (5%) if the player is in a party and the leader has MENTOR buff.
     */
    public static double getPartyXpBonus(String playerUuid) {
        String leaderUuid = memberToParty.get(playerUuid);
        if (leaderUuid == null) return 0.0;
        
        // Check if leader has MENTOR buff
        return PlayerBuffManager.getPartyXpBonus(leaderUuid);
    }
    
    /**
     * Track a player reaching level 10 for MENTOR buff obtain condition.
     * Called when a player levels up.
     */
    public static void onPlayerLevelUp(ServerPlayer player, int newLevel) {
        if (newLevel < 10) return;
        
        String playerUuid = player.getUUID().toString();
        
        // Check all parties to see if this player is a member
        for (Map.Entry<String, Set<String>> entry : parties.entrySet()) {
            String leaderUuid = entry.getKey();
            Set<String> members = entry.getValue();
            
            if (members.contains(playerUuid)) {
                // Track that this leader helped a player reach level 10
                playersHelpedToLevel10.computeIfAbsent(leaderUuid, k -> ConcurrentHashMap.newKeySet()).add(playerUuid);
                
                // Check if leader qualifies for MENTOR buff (helped 10 unique players)
                Set<String> helped = playersHelpedToLevel10.get(leaderUuid);
                if (helped != null && helped.size() >= 10) {
                    ServerPlayer leader = PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(leaderUuid));
                    if (leader != null && !PlayerBuffManager.hasBuff(leaderUuid, PlayerBuffManager.PlayerBuff.MENTOR)) {
                        PlayerBuffManager.grantBuff(leader, PlayerBuffManager.PlayerBuff.MENTOR);
                        leader.sendSystemMessage(Component.literal("§a§l✦ ACHIEVEMENT UNLOCKED: MENTOR BUFF!").withStyle(ChatFormatting.GREEN), false);
                        leader.sendSystemMessage(Component.literal("§7You've helped 10 players reach level 10! Party members now gain +5% XP."), false);
                    }
                }
            }
        }
    }
    
    public static int getPlayersHelpedCount(String leaderUuid) {
        Set<String> helped = playersHelpedToLevel10.get(leaderUuid);
        return helped != null ? helped.size() : 0;
    }
    
    public static void listParty(ServerPlayer player) {
        String playerUuid = player.getUUID().toString();
        String leaderUuid = memberToParty.get(playerUuid);
        
        if (leaderUuid == null) {
            player.sendSystemMessage(Component.literal("You are not in a party.").withStyle(ChatFormatting.RED), false);
            return;
        }
        
        Set<String> members = parties.get(leaderUuid);
        if (members == null || members.isEmpty()) {
            player.sendSystemMessage(Component.literal("Your party is empty.").withStyle(ChatFormatting.RED), false);
            return;
        }
        
        player.sendSystemMessage(Component.literal("§6§l=== Your Party ==="), false);
        for (String memberUuid : members) {
            ServerPlayer member = PoliticalServer.server.getPlayerList().getPlayer(UUID.fromString(memberUuid));
            String name = member != null ? member.getName().getString() : "Offline";
            String role = memberUuid.equals(leaderUuid) ? " §e[Leader]" : "";
            boolean hasMentor = PlayerBuffManager.hasBuff(leaderUuid, PlayerBuffManager.PlayerBuff.MENTOR);
            String mentorStatus = hasMentor && !memberUuid.equals(leaderUuid) ? " §a[+5% XP]" : "";
            player.sendSystemMessage(Component.literal("§7- " + name + role + mentorStatus), false);
        }
        
        if (PlayerBuffManager.hasBuff(leaderUuid, PlayerBuffManager.PlayerBuff.MENTOR)) {
            player.sendSystemMessage(Component.literal("§aParty members gain +5% XP (MENTOR buff active)"), false);
        }
    }
}
