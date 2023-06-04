package net.devscape.project.pvplog.managers;

import net.devscape.project.pvplog.handlers.iPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerManager {

    private final List<iPlayer> playerList = new ArrayList<>();


    public PlayerManager() {}


    public iPlayer getPlayer(Player player) {
        for (iPlayer ip: playerList) {
            if (ip.getUuid().equals(player.getUniqueId().toString())) {
                return ip;
            }
        }
        return null;
    }

    public iPlayer getPlayer(OfflinePlayer player) {
        for (iPlayer ip: playerList) {
            if (ip.getUuid().equals(player.getUniqueId().toString())) {
                return ip;
            }
        }
        return null;
    }

    public void setToggle(Player player, boolean toggle) {
        iPlayer ip = getPlayer(player);
        ip.setPvP(toggle);
    }

    public void setCombat(Player player, boolean combat) {
        iPlayer ip = getPlayer(player);
        ip.setCombat(combat);
    }

    public List<iPlayer> getPlayerList() {
        return playerList;
    }
}
