package net.devscape.project.pvplog.listener;

import net.devscape.project.pvplog.PvPLog;
import net.devscape.project.pvplog.handlers.iPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import static net.devscape.project.pvplog.utils.Utils.msgPlayer;

public class WorldListener implements Listener {


    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent  e) {
        Player player = e.getPlayer();

        iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(player);

        for (String worlds : PvPLog.getPvPlog().getConfig().getStringList("safe-zones.forced-worlds")) {
            if (player.getLocation().getWorld().getName().equalsIgnoreCase(worlds)) {
                ip.setPvP(true);
                msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.pvp-forced-world"));
                break;
            }
        }
    }
}
