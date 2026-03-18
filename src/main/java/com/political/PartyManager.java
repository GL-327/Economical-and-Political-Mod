package com.political;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple party system for MENTOR buff functionality.
 * Party members gain +5% XP when the party leader has the MENTOR buff.
 */
public class PartyManager {

    // Party data: party leader UUID -> set of member UUIDs
    private static final Map<String, Set<String>> parties = new ConcurrentHashMap<>();
    
    // Reverse lookup: member UUID -> party leader UUID
    private static final Map<String, String> memberToParty = new ConcurrentHashMap<>();
    
    // Track players helped to level 10 for MENTOR buff obtain condition
    private static final Map<String, Set<String>> playersHelpedToLevel10 = new ConcurrentHashMap<>();
    
    public static boolean createParty(ServerPlayerEntity leader) {
        String leaderUuid = leader.getUuidAsString();
        if (memberToParty.containsKey(leaderUuid)) {
            leader.sendMessage(Text.literal("You are already in a party!").formatted(Formatting.RED), false);
            return false;
        }
        
        parties.put(leaderUuid, ConcurrentHashMap.newKeySet());
        memberToParty.put(leaderUuid, leaderUuid); // Leader is also a member
        leader.sendMessage(Text.literal("§aCreated a new party! Invite players with /party invite <name>"), false);
        return true;
    }
    
    public static boolean invitePlayer(ServerPlayerEntity leader, ServerPlayerEntity target) {
        String leaderUuid = leader.getUuidAsString();
        String targetUuid = target.getUuidAsString();
        
        if (!parties.containsKey(leaderUuid)) {
            leader.sendMessage(Text.literal("You don't have a party! Create one first with /party create").formatted(Formatting.RED), false);
            return false;
        }
        
        if (memberToParty.containsKey(targetUuid)) {
            leader.sendMessage(Text.literal("That player is already in a party!").formatted(Formatting.RED), false);
            return false;
        }
        
        // Send invitation
        target.sendMessage(Text.literal("§e" + leader.getName().getString() + " has invited you to their party!"), false);
        target.sendMessage(Text.literal("§aType /party accept " + leader.getName().getString() + " to join."), false);
        leader.sendMessage(Text.literal("§aInvitation sent to " + target.getName().getString()), false);
        return true;
    }
    
    public static boolean acceptInvite(ServerPlayerEntity player, ServerPlayerEntity leader) {
        String playerUuid = player.getUuidAsString();
        String leaderUuid = leader.getUuidAsString();
        
        if (memberToParty.containsKey(playerUuid)) {
            player.sendMessage(Text.literal("You are already in a party!").formatted(Formatting.RED), false);
            return false;
        }
        
        Set<String> party = parties.get(leaderUuid);
        if (party == null) {
            player.sendMessage(Text.literal("That party no longer exists!").formatted(Formatting.RED), false);
            return false;
        }
        
        party.add(playerUuid);
        memberToParty.put(playerUuid, leaderUuid);
        
        player.sendMessage(Text.literal("§aYou joined " + leader.getName().getString() + "'s party!"), false);
        
        // Notify all party members
        for (String memberUuid : party) {
            ServerPlayerEntity member = PoliticalServer.server.getPlayerManager().getPlayer(UUID.fromString(memberUuid));
            if (member != null && !memberUuid.equals(playerUuid)) {
                member.sendMessage(Text.literal("§b" + player.getName().getString() + " joined the party!"), false);
            }
        }
        return true;
    }
    
    public static boolean leaveParty(ServerPlayerEntity player) {
        String playerUuid = player.getUuidAsString();
        String leaderUuid = memberToParty.remove(playerUuid);
        
        if (leaderUuid == null) {
            player.sendMessage(Text.literal("You are not in a party!").formatted(Formatting.RED), false);
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
                ServerPlayerEntity member = PoliticalServer.server.getPlayerManager().getPlayer(UUID.fromString(memberUuid));
                if (member != null) {
                    member.sendMessage(Text.literal("§cThe party has been disbanded!").formatted(Formatting.RED), false);
                }
            }
            parties.remove(leaderUuid);
        }
        
        player.sendMessage(Text.literal("§eYou left the party."), false);
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
    public static void onPlayerLevelUp(ServerPlayerEntity player, int newLevel) {
        if (newLevel < 10) return;
        
        String playerUuid = player.getUuidAsString();
        
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
                    ServerPlayerEntity leader = PoliticalServer.server.getPlayerManager().getPlayer(UUID.fromString(leaderUuid));
                    if (leader != null && !PlayerBuffManager.hasBuff(leaderUuid, PlayerBuffManager.PlayerBuff.MENTOR)) {
                        PlayerBuffManager.grantBuff(leader, PlayerBuffManager.PlayerBuff.MENTOR);
                        leader.sendMessage(Text.literal("§a§l✦ ACHIEVEMENT UNLOCKED: MENTOR BUFF!").formatted(Formatting.GREEN), false);
                        leader.sendMessage(Text.literal("§7You've helped 10 players reach level 10! Party members now gain +5% XP."), false);
                    }
                }
            }
        }
    }
    
    public static int getPlayersHelpedCount(String leaderUuid) {
        Set<String> helped = playersHelpedToLevel10.get(leaderUuid);
        return helped != null ? helped.size() : 0;
    }
    
    public static void listParty(ServerPlayerEntity player) {
        String playerUuid = player.getUuidAsString();
        String leaderUuid = memberToParty.get(playerUuid);
        
        if (leaderUuid == null) {
            player.sendMessage(Text.literal("You are not in a party.").formatted(Formatting.RED), false);
            return;
        }
        
        Set<String> members = parties.get(leaderUuid);
        if (members == null || members.isEmpty()) {
            player.sendMessage(Text.literal("Your party is empty.").formatted(Formatting.RED), false);
            return;
        }
        
        player.sendMessage(Text.literal("§6§l=== Your Party ==="), false);
        for (String memberUuid : members) {
            ServerPlayerEntity member = PoliticalServer.server.getPlayerManager().getPlayer(UUID.fromString(memberUuid));
            String name = member != null ? member.getName().getString() : "Offline";
            String role = memberUuid.equals(leaderUuid) ? " §e[Leader]" : "";
            boolean hasMentor = PlayerBuffManager.hasBuff(leaderUuid, PlayerBuffManager.PlayerBuff.MENTOR);
            String mentorStatus = hasMentor && !memberUuid.equals(leaderUuid) ? " §a[+5% XP]" : "";
            player.sendMessage(Text.literal("§7- " + name + role + mentorStatus), false);
        }
        
        if (PlayerBuffManager.hasBuff(leaderUuid, PlayerBuffManager.PlayerBuff.MENTOR)) {
            player.sendMessage(Text.literal("§aParty members gain +5% XP (MENTOR buff active)"), false);
        }
    }
}
