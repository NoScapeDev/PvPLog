package net.devscape.project.pvplog.managers;

import net.devscape.project.pvplog.PvPLog;
import net.devscape.project.pvplog.handlers.iPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.devscape.project.pvplog.utils.Utils.msgPlayer;

public class InviteManager {
    private final Map<UUID, UUID> pendingInvitations;
    private final Map<UUID, Integer> invitationTasks;

    public InviteManager() {
        pendingInvitations = new HashMap<>();
        invitationTasks = new HashMap<>();
    }

    public void sendInvite(Player sender, Player target) {
        UUID senderId = sender.getUniqueId();
        UUID targetId = target.getUniqueId();

        if (hasPendingInvite(targetId)) {
            msgPlayer(sender, PvPLog.getPvPlog().getConfig().getString("messages.trust-invite-already-sent"));
            return;
        }

        pendingInvitations.put(targetId, senderId);

        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(PvPLog.getPvPlog(), () -> {
            cancelInvite(targetId);
            msgPlayer(target, PvPLog.getPvPlog().getConfig().getString("messages.trust-invite-expired-self").replaceAll("%player%", sender.getName()));
        }, 20L * PvPLog.getPvPlog().getConfig().getInt("trust.invite-expire"));

        invitationTasks.put(targetId, taskId);

        msgPlayer(sender, PvPLog.getPvPlog().getConfig().getString("messages.trust-invite-trust").replaceAll("%player%", target.getName()));
        msgPlayer(target, PvPLog.getPvPlog().getConfig().getString("messages.trust-invite-received").replaceAll("%player%", sender.getName()));
    }

    public void acceptInvite(Player player) {
        UUID playerId = player.getUniqueId();

        if (!hasPendingInvite(playerId)) {
            msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.trust-invite-no-pending"));
            return;
        }

        UUID senderId = pendingInvitations.remove(playerId);
        cancelInvite(playerId);

        Player sender = player.getServer().getPlayer(senderId);
        if (sender != null) {
            iPlayer ipSender = PvPLog.getPvPlog().getPlayerManager().getPlayer(sender);
            iPlayer ipPlayer = PvPLog.getPvPlog().getPlayerManager().getPlayer(player);

            ipSender.getTrusted().add(player.getUniqueId());
            ipPlayer.getTrusted().add(sender.getUniqueId());
            msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.trust-invite-accept").replaceAll("%player%", sender.getName()));
            msgPlayer(sender, PvPLog.getPvPlog().getConfig().getString("messages.trust-invite-accept-self").replaceAll("%player%", player.getName()));
        } else {
            msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.player-not-online"));
            cancelInvite(playerId);
        }
    }

    public void declineInvite(Player player) {
        UUID playerId = player.getUniqueId();

        if (!hasPendingInvite(playerId)) {
            msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.trust-invite-no-pending"));
            return;
        }

        UUID senderId = pendingInvitations.remove(playerId);

        cancelInvite(playerId);

        Player sender = player.getServer().getPlayer(senderId);
        if (sender != null) {
            msgPlayer(sender, PvPLog.getPvPlog().getConfig().getString("messages.trust-invite-deny").replaceAll("%player%", player.getName()));
            msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.trust-invite-deny-self").replaceAll("%player%", sender.getName()));
        } else {
            msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.player-not-online"));
            cancelInvite(playerId);
        }
    }

    public boolean hasPendingInvite(UUID playerId) {
        return pendingInvitations.containsKey(playerId);
    }

    private void cancelInvite(UUID playerId) {
        Integer taskId = invitationTasks.remove(playerId);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        pendingInvitations.remove(playerId);
    }
}