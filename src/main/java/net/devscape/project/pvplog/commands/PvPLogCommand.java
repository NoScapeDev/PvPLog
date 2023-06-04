package net.devscape.project.pvplog.commands;

import net.devscape.project.pvplog.PvPLog;
import net.devscape.project.pvplog.handlers.iPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.devscape.project.pvplog.utils.Utils.msgPlayer;

public class PvPLogCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        } else {

            Player player = (Player) sender;

            iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(player);

            if (cmd.getName().equalsIgnoreCase("pvplog")) {
                if (player.hasPermission("pvplog.admin")) {
                    if (args.length == 0) {
                        sendHelp(player);
                        return true;
                    } else if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("reload")) {
                            PvPLog.getPvPlog().reload();
                            msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.reloaded"));
                            return true;
                        } else {
                            sendHelp(player);
                        }
                    } else {
                        sendHelp(player);
                    }
                } else {
                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.no-permission"));
                }
            }
        }

        return false;
    }

    public void sendHelp(Player player) {
        // send help message.

        msgPlayer(player,
                "",
                "&8&m---------&f &6&lPvPLog &8&m---------",
                "&7/pvplog reload",
                "",
                "&7Version: &f" + PvPLog.getPvPlog().getDescription().getVersion(),
                "&ePlugin Developed by &lDevScape#4278",
                "&8&m------------------------------");
    }
}