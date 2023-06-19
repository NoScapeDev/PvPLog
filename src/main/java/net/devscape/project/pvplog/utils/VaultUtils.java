package net.devscape.project.pvplog.utils;

import net.devscape.project.pvplog.PvPLog;
import org.bukkit.entity.Player;

public class VaultUtils {

    public static void take(Player player, int value) {
        if (PvPLog.getEconomy().has(player, value)) {
            PvPLog.getEconomy().withdrawPlayer(player, value);
        }
    }

    public static void add(Player player, int value) {
        PvPLog.getEconomy().depositPlayer(player, value);
    }
}
