package com.eric.GobangGameMC;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class GameInvitationManager {

    private final GobangGameMC plugin;
    private final Map<UUID, InvitationData> pendingInvitations;

    public static class InvitationData {
        private final UUID sender;
        private final UUID receiver;
        private final long timestamp;

        public InvitationData(UUID sender, UUID receiver) {
            this.sender = sender;
            this.receiver = receiver;
            this.timestamp = System.currentTimeMillis();
        }

        public UUID getSender() { return sender; }
        public UUID getReceiver() { return receiver; }
        public long getTimestamp() { return timestamp; }
    }

    public GameInvitationManager(GobangGameMC plugin) {
        this.plugin = plugin;
        this.pendingInvitations = new HashMap<>();
    }

    public void sendInvitation(Player sender, Player receiver) {
        // Check if receiver is already in a game
        if (plugin.isPlayerInGame(receiver)) {
            sender.sendMessage("§c" + receiver.getName() + " is already in a game!");
            return;
        }

        // Check if sender is already in a game
        if (plugin.isPlayerInGame(sender)) {
            sender.sendMessage("§cYou are already in a game!");
            return;
        }

        // Check if invitation already exists
        for (InvitationData data : pendingInvitations.values()) {
            if (data.getSender().equals(sender.getUniqueId()) &&
                    data.getReceiver().equals(receiver.getUniqueId())) {
                sender.sendMessage("§cYou have already invited " + receiver.getName() + "!");
                return;
            }
        }

        // Send invitation
        InvitationData invitation = new InvitationData(sender.getUniqueId(), receiver.getUniqueId());
        pendingInvitations.put(sender.getUniqueId(), invitation);

        sender.sendMessage("§aInvitation sent to " + receiver.getName() + "!");

        receiver.sendMessage("§6===================================");
        receiver.sendMessage("§e" + sender.getName() + " §ahas invited you to play Gobang!");
        receiver.sendMessage("§eUse §a/gobanggame accept " + sender.getName() + " §eto accept");
        receiver.sendMessage("§eor §c/gobanggame deny " + sender.getName() + " §eto deny");
        receiver.sendMessage("§6===================================");

        // Set timeout (5 minutes)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingInvitations.containsKey(sender.getUniqueId())) {
                pendingInvitations.remove(sender.getUniqueId());
                sender.sendMessage("§cYour invitation to " + receiver.getName() + " has expired.");
                receiver.sendMessage("§cThe invitation from " + sender.getName() + " has expired.");
            }
        }, 20L * 60 * 5); // 5 minutes
    }

    public boolean acceptInvitation(Player accepter, String senderName) {
        Player sender = Bukkit.getPlayer(senderName);
        if (sender == null) {
            accepter.sendMessage("§cPlayer not found!");
            return false;
        }

        InvitationData invitation = pendingInvitations.get(sender.getUniqueId());
        if (invitation == null || !invitation.getReceiver().equals(accepter.getUniqueId())) {
            accepter.sendMessage("§cYou don't have a pending invitation from " + senderName + "!");
            return false;
        }

        // Remove invitation
        pendingInvitations.remove(sender.getUniqueId());

        // Check if both players are still available
        if (plugin.isPlayerInGame(sender) || plugin.isPlayerInGame(accepter)) {
            accepter.sendMessage("§cOne of you is already in a game!");
            sender.sendMessage("§cCannot start game - one player is already in a game!");
            return false;
        }

        // Start the game
        new GobangGameManager(plugin, sender, accepter);

        accepter.sendMessage("§aGame started with " + sender.getName() + "!");
        sender.sendMessage("§a" + accepter.getName() + " accepted your invitation!");

        return true;
    }

    public boolean denyInvitation(Player denier, String senderName) {
        Player sender = Bukkit.getPlayer(senderName);
        if (sender == null) {
            denier.sendMessage("§cPlayer not found!");
            return false;
        }

        InvitationData invitation = pendingInvitations.get(sender.getUniqueId());
        if (invitation == null || !invitation.getReceiver().equals(denier.getUniqueId())) {
            denier.sendMessage("§cYou don't have a pending invitation from " + senderName + "!");
            return false;
        }

        // Remove invitation
        pendingInvitations.remove(sender.getUniqueId());

        denier.sendMessage("§cYou denied the invitation from " + sender.getName() + ".");
        sender.sendMessage("§c" + denier.getName() + " denied your invitation.");

        return true;
    }

    public boolean hasPendingInvitation(Player player) {
        for (InvitationData data : pendingInvitations.values()) {
            if (data.getReceiver().equals(player.getUniqueId())) {
                long currentTime = System.currentTimeMillis();
                long timeout = plugin.getConfig().getLong("invitations.timeout", 300) * 1000;
                if (currentTime - data.getTimestamp() < timeout) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasInvitationFrom(Player sender, Player receiver) {
        InvitationData invitation = pendingInvitations.get(sender.getUniqueId());
        return invitation != null && invitation.getReceiver().equals(receiver.getUniqueId());
    }

    public int getPendingInvitationCount(Player player) {
        int count = 0;
        long currentTime = System.currentTimeMillis();
        long timeout = plugin.getConfig().getLong("invitations.timeout", 300) * 1000;

        for (InvitationData data : pendingInvitations.values()) {
            if (data.getReceiver().equals(player.getUniqueId())) {
                // Check if expired
                if (currentTime - data.getTimestamp() < timeout) {
                    count++;
                }
            }
        }
        return count;
    }

    public int cleanupExpiredInvitations() {
        int removed = 0;
        long currentTime = System.currentTimeMillis();
        long timeout = plugin.getConfig().getLong("invitations.timeout", 300) * 1000;

        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, InvitationData> entry : pendingInvitations.entrySet()) {
            InvitationData data = entry.getValue();
            if (currentTime - data.getTimestamp() > timeout) {
                toRemove.add(entry.getKey());
                removed++;

                // Notify players
                Player sender = Bukkit.getPlayer(data.getSender());
                Player receiver = Bukkit.getPlayer(data.getReceiver());

                if (sender != null && sender.isOnline()) {
                    sender.sendMessage("§cYour invitation to " +
                            (receiver != null ? receiver.getName() : "player") + " has expired.");
                }
            }
        }

        for (UUID key : toRemove) {
            pendingInvitations.remove(key);
        }

        return removed;
    }

    public Map<UUID, InvitationData> getPendingInvitations() {
        return new HashMap<>(pendingInvitations);
    }
}