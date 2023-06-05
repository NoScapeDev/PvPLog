package net.devscape.project.pvplog.commands;

import net.devscape.project.pvplog.PvPLog;
import net.devscape.project.pvplog.handlers.ParticleTask;
import net.devscape.project.pvplog.handlers.iPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
                    } else if (args.length == 3) {
                        if (args[0].equalsIgnoreCase("pvp")) {
                            if (args[1].equalsIgnoreCase("status")) {
                                Player target = Bukkit.getPlayer(args[2]);

                                if (target != null) {
                                    iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(target);
                                    if (ip.isPvP()) {
                                        msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.admin-check-status").replaceAll("%status%", "ON").replaceAll("%player%", target.getName()));
                                    } else {
                                        msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.admin-check-status").replaceAll("%status%", "OFF").replaceAll("%player%", target.getName()));
                                    }
                                } else {
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.player-not-online"));
                                }
                            } else if (args[1].equalsIgnoreCase("on")) {
                                OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);

                                if (!PvPLog.getPvPlog().getUserData().exists(target)) {
                                    msgPlayer(player.getPlayer(), PvPLog.getPvPlog().getConfig().getString("messages.pvp-toggle-disabled"));
                                    return true;
                                }

                                if (target.isOnline()) {
                                    iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(target);

                                    ParticleTask.startForPlayer(target.getPlayer(), PvPLog.getPvPlog());
                                    ip.setPvP(true);
                                    msgPlayer(target.getPlayer(), PvPLog.getPvPlog().getConfig().getString("messages.pvp-toggle-enabled"));
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.admin-set-pvp").replaceAll("%status%", "ON").replaceAll("%player%", target.getName()));
                                } else {
                                    PvPLog.getPvPlog().getUserData().loadOfflinePlayer(target);
                                    iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(target);
                                    ip.setPvP(false);

                                    PvPLog.getPvPlog().getUserData().saveUser(target, ip);
                                    PvPLog.getPvPlog().getPlayerManager().getPlayerList().remove(ip);
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.admin-set-pvp").replaceAll("%status%", "ON").replaceAll("%player%", target.getName()));
                                }
                            } else if (args[1].equalsIgnoreCase("off")) {
                                OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);

                                if (!PvPLog.getPvPlog().getUserData().exists(target)) {
                                    msgPlayer(player.getPlayer(), PvPLog.getPvPlog().getConfig().getString("messages.pvp-toggle-disabled"));
                                    return true;
                                }

                                if (target.isOnline()) {
                                    iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(target);

                                    ParticleTask.stopForPlayer(Objects.requireNonNull(target.getPlayer()));
                                    ip.setPvP(false);
                                    msgPlayer(target.getPlayer(), PvPLog.getPvPlog().getConfig().getString("messages.pvp-toggle-disabled"));
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.admin-set-pvp").replaceAll("%status%", "OFF").replaceAll("%player%", target.getName()));
                                } else {
                                    PvPLog.getPvPlog().getUserData().loadOfflinePlayer(target);
                                    iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(target);
                                    ip.setPvP(false);

                                    PvPLog.getPvPlog().getUserData().saveUser(target, ip);
                                    PvPLog.getPvPlog().getPlayerManager().getPlayerList().remove(ip);
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.admin-set-pvp").replaceAll("%status%", "OFF").replaceAll("%player%", target.getName()));
                                }
                            } else {
                                sendHelp(player);
                            }
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
                "&7/pvplog pvp status (player)",
                "&7/pvplog pvp on/off (player)",
                "",
                "&7Version: &f" + PvPLog.getPvPlog().getDescription().getVersion(),
                "&ePlugin Developed by &lDevScape#4278",
                "&8&m------------------------------");
    }
}