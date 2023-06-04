package net.devscape.project.pvplog.listener;

import net.devscape.project.pvplog.PvPLog;
import net.devscape.project.pvplog.handlers.ParticleTask;
import net.devscape.project.pvplog.handlers.iPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinLeave implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        PvPLog.getPvPlog().getUserData().createPlayer(player);
        PvPLog.getPvPlog().getUserData().loadPlayer(player);

        if (PvPLog.getPvPlog().getPlayerManager().getPlayer(player).isPvP()) {
            ParticleTask.startForPlayer(player, PvPLog.getPvPlog());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(player);
        PvPLog.getPvPlog().getUserData().saveUser(player, ip);

        ParticleTask.stopForPlayer(player);

        PvPLog.getPvPlog().getPlayerManager().getPlayerList().remove(ip);
    }
}