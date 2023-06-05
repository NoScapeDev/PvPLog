package net.devscape.project.pvplog.commands;

import net.devscape.project.pvplog.PvPLog;
import net.devscape.project.pvplog.handlers.ParticleTask;
import net.devscape.project.pvplog.handlers.iPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

import static net.devscape.project.pvplog.utils.Utils.msgPlayer;

public class PvPCommands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        } else {

            Player player = (Player) sender;

            iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(player);

            if (cmd.getName().equalsIgnoreCase("pvp")) {
                if (player.hasPermission("pvplog.player")) {
                    if (args.length == 0) {
                        sendHelp(player);
                        return true;
                    } else if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("on")) {
                            for (String worlds : PvPLog.getPvPlog().getConfig().getStringList("safe-zones.forced-worlds")) {
                                if (Objects.requireNonNull(player.getLocation().getWorld()).getName().equalsIgnoreCase(worlds)) {
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.pvp-forced-world"));
                                    return true;
                                }
                            }

                            if (ip.isPvP()) {
                                msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.pvp-already-enabled"));
                                return true;
                            }

                            ParticleTask.startForPlayer(player, PvPLog.getPvPlog());
                            ip.setPvP(true);
                            msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.pvp-toggle-enabled"));
                            return true;
                        } else if (args[0].equalsIgnoreCase("off")) {
                            for (String worlds : PvPLog.getPvPlog().getConfig().getStringList("safe-zones.forced-worlds")) {
                                if (Objects.requireNonNull(player.getLocation().getWorld()).getName().equalsIgnoreCase(worlds)) {
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.pvp-forced-world"));
                                    return true;
                                }
                            }

                            if (!ip.isPvP()) {
                                msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.pvp-already-disabled"));
                                return true;
                            }

                            ParticleTask.stopForPlayer(player);
                            ip.setPvP(false);
                            msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.pvp-toggle-disabled"));
                            return true;
                        } else if (args[0].equalsIgnoreCase("trustlist")) {
                            if (PvPLog.getPvPlog().getConfig().getBoolean("trust.enable")) {
                                msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.trust-list-header"));

                                if (ip.getTrusted().size() != 0 && ip.getTrusted() != null) {
                                    for (UUID uuid : ip.getTrusted()) {
                                        OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
                                        msgPlayer(player, Objects.requireNonNull(PvPLog.getPvPlog().getConfig().getString("messages.trust-list-player"))
                                                .replaceAll("%player%", Objects.requireNonNull(target.getName())));
                                    }
                                } else {
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.no-players-trusted"));
                                }

                                return true;
                            } else {
                                msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.trust-disabled"));
                            }
                        } else if (args[0].equalsIgnoreCase("status")) {
                            if (ip.isPvP()) {
                                msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.pvp-status").replaceAll("%status%", "ON"));
                            } else {
                                msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.pvp-status").replaceAll("%status%", "OFF"));
                            }

                            return true;
                        }
                    } else if (args.length == 2) {
                        if (args[0].equalsIgnoreCase("trustaccept")) {
                            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                            if (PvPLog.getPvPlog().getConfig().getBoolean("trust.enable")) {
                                if (player.getName().equalsIgnoreCase(target.getName())) {
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.player-self"));
                                    return true;
                                }

                                if (ip.getTrusted().size() >= PvPLog.getPvPlog().getConfig().getInt("trust.max-trust-allowed")) {
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.trust-reached-max-player"));
                                    return true;
                                }

                                if (target.getPlayer() != null) {
                                    PvPLog.getPvPlog().getInviteManager().acceptInvite(player);
                                } else {
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.player-not-online"));
                                }
                            } else {
                                msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.trust-disabled"));
                            }

                            return true;
                        } else if (args[0].equalsIgnoreCase("trustdeny")) {
                            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                            if (PvPLog.getPvPlog().getConfig().getBoolean("trust.enable")) {
                                if (player.getName().equalsIgnoreCase(target.getName())) {
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.player-self"));
                                    return true;
                                }

                                if (target.getPlayer() != null) {
                                    PvPLog.getPvPlog().getInviteManager().declineInvite(player);
                                } else {
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.player-not-online"));
                                }

                            } else {
                                msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.trust-disabled"));
                            }
                            return true;
                        } else if (args[0].equalsIgnoreCase("trust")) {
                            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                            if (PvPLog.getPvPlog().getConfig().getBoolean("trust.enable")) {
                                if (player.getName().equalsIgnoreCase(target.getName())) {
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.player-self"));
                                    return true;
                                }

                                if (ip.getTrusted().size() >= PvPLog.getPvPlog().getConfig().getInt("trust.max-trust-allowed")) {
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.trust-reached-max-player"));
                                    return true;
                                }

                                if (target.getPlayer() != null) {
                                    PvPLog.getPvPlog().getInviteManager().sendInvite(player, target.getPlayer());
                                } else {
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.player-not-online"));
                                }
                            } else {
                                msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.trust-disabled"));
                            }

                            return true;
                        } else if (args[0].equalsIgnoreCase("untrust")) {
                            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                            if (PvPLog.getPvPlog().getConfig().getBoolean("trust.enable")) {
                                if (player.getName().equalsIgnoreCase(target.getName())) {
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.player-self"));
                                    return true;
                                }

                                if (ip.isPlayerTrusted(target)) {
                                    if (target.hasPlayedBefore()) {
                                        if (target.isOnline()) {
                                            iPlayer ipTarget = PvPLog.getPvPlog().getPlayerManager().getPlayer(target.getPlayer());
                                            ipTarget.getTrusted().remove(player.getUniqueId());

                                            msgPlayer(target.getPlayer(), Objects.requireNonNull(PvPLog.getPvPlog().getConfig().getString("messages.trust-remove-player")).replaceAll("%player%", Objects.requireNonNull(player.getName())));
                                        } else {
                                            PvPLog.getPvPlog().getUserData().removeTrusted(target, player);
                                        }

                                        ip.getTrusted().remove(target.getUniqueId());
                                        msgPlayer(player, Objects.requireNonNull(PvPLog.getPvPlog().getConfig().getString("messages.trust-remove-player")).replaceAll("%player%", Objects.requireNonNull(target.getName())));
                                    } else {
                                        msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.player-never-joined"));
                                    }
                                } else {
                                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.player-not-trusted"));
                                }
                            } else {
                                msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.trust-disabled"));
                            }
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
                "&8&m---------&f &6&lPvP Commands &8&m---------",
                "&7/pvp on",
                "&7/pvp off",
                "&7/pvp status",
                "&7/pvp trustlist",
                "&7/pvp trust (player)",
                "&7/pvp untrust (player)",
                "&7/pvp trustaccept (player)",
                "&7/pvp trustdeny (player)",
                "",
                "&eeg. /pvp trust DevScape",
                "&8&m-----------------------------------");
    }
}
