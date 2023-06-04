package net.devscape.project.pvplog.listener;

import net.devscape.project.pvplog.PvPLog;
import net.devscape.project.pvplog.handlers.ParticleTask;
import net.devscape.project.pvplog.handlers.iPlayer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import static net.devscape.project.pvplog.utils.RegionUtils.isInRegion;
import static net.devscape.project.pvplog.utils.Utils.msgPlayer;

public class CombatListener implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(player);

        if (ip.isCombat()) {
            // do combat actions

            if (PvPLog.getPvPlog().getConfig().getBoolean("combat.leave-actions.kill")) {
                player.setHealth(0);
            }

            if (PvPLog.getPvPlog().getConfig().getBoolean("combat.leave-actions.ban")) {
                BanList banList = Bukkit.getBanList(BanList.Type.NAME);
                banList.addBan(player.getName(), "Combat logging", null, null);
                player.kickPlayer("You have been banned: Combat Logging!");
            }

            if (PvPLog.getPvPlog().getConfig().getBoolean("combat.leave-actions.clear-inv")) {
                player.getInventory().clear();
            }

            if (PvPLog.getPvPlog().getConfig().getBoolean("combat.leave-actions.execute-commands.enable")) {
                for (String cmd : PvPLog.getPvPlog().getConfig().getStringList("combat.leave-actions.execute-commands.commands")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                }
            }
        }

        ParticleTask.stopForPlayer(player);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(player);

        PlayerInventory inventory = player.getInventory();
        ItemStack item = inventory.getItemInMainHand();

        if (PvPLog.getPvPlog().getConfig().getBoolean("combat.current-actions.block-trident")) {
            if (item.getType() == Material.TRIDENT) {
                if (ip.isCombat()) {
                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.combat-block-item"));
                    event.setCancelled(true);
                }
            }
        }

        if (PvPLog.getPvPlog().getConfig().getBoolean("combat.current-actions.block-ender-pearl")) {
            if (item.getType() == Material.ENDER_PEARL) {
                if (ip.isCombat()) {
                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.combat-block-item"));
                    event.setCancelled(true);
                }
            }
        }

        if (PvPLog.getPvPlog().getConfig().getBoolean("combat.current-actions.block-chorus-fruit")) {
            if (item.getType() == Material.CHORUS_FRUIT) {
                if (ip.isCombat()) {
                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.combat-block-item"));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onElytra(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack item = inventory.getChestplate();

        iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(player);

        if (PvPLog.getPvPlog().getConfig().getBoolean("combat.current-actions.block-elytra")) {
            if (item != null && item.getType() == Material.ELYTRA) {
                if (ip.isCombat()) {
                    msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.combat-block-elytra"));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();

        iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(player);

        if (ip.isCombat()) {
            // do combat actions

            if (!PvPLog.getPvPlog().getConfig().getBoolean("combat.current-actions.block-all-commands")) {
                for (String cmd : PvPLog.getPvPlog().getConfig().getStringList("combat.current-actions.disabled-commands")) {
                    if (e.getMessage().startsWith("/" + cmd)) {
                        msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.combat-command-deny"));
                        e.setCancelled(true);
                        break;
                    }
                }
            } else {
                msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.combat-command-deny"));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerDeathEvent e) {
        Player player = e.getEntity();

        iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(player);

        if (ip.isCombat()) {
            ip.setCombat(false);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && (event.getDamager() instanceof Player || event.getDamager() instanceof Projectile)) {
            Player victim = (Player) event.getEntity();
            Player damager = null;

            if (event.getDamager() instanceof Player) {
                damager = (Player) event.getDamager();
            } else if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();
                if (projectile.getShooter() instanceof Player) {
                    damager = (Player) projectile.getShooter();
                }
            } else if (event.getDamager() instanceof ThrownPotion) {
                ThrownPotion potion = (ThrownPotion) event.getDamager();
                if (potion.getShooter() instanceof Player) {
                    damager = (Player) potion.getShooter();
                }
            }

            if (damager == null) {
                return;
            }

            if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null && Bukkit.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                for (String regions : PvPLog.getPvPlog().getConfig().getStringList("safe-zones.wg-regions")) {
                    if (isInRegion(victim, regions)) {
                        event.setCancelled(true);
                        msgPlayer(damager, PvPLog.getPvPlog().getConfig().getString("messages.pvp-denied"));
                        return;
                    }
                }
            }

            iPlayer ipVictim = PvPLog.getPvPlog().getPlayerManager().getPlayer(victim);
            iPlayer ipDamager = PvPLog.getPvPlog().getPlayerManager().getPlayer(damager);

            for (String worlds : PvPLog.getPvPlog().getConfig().getStringList("safe-zones.forced-worlds")) {
                if (victim.getLocation().getWorld().getName().equalsIgnoreCase(worlds)) {
                    if (!ipVictim.isPvP()) {
                        ipVictim.setPvP(true);
                        msgPlayer(victim, PvPLog.getPvPlog().getConfig().getString("messages.pvp-forced-world"));
                        return;
                    }
                }
            }

            if (!ipVictim.isPvP() && !ipDamager.isPvP() && (!ipVictim.isPlayerTrusted(damager) || !ipDamager.isPlayerTrusted(victim))) {
                event.setCancelled(true);
                msgPlayer(damager, PvPLog.getPvPlog().getConfig().getString("messages.pvp-denied"));
            } else {
                boolean isVictimPvPDisabled = !ipVictim.isPvP() && !ipDamager.isPlayerTrusted(victim);
                boolean areBothTrusted = ipVictim.isPlayerTrusted(damager) && ipDamager.isPlayerTrusted(victim);

                if ((ipVictim.isPvP() && ipDamager.isPvP()) || areBothTrusted) {
                    if (areBothTrusted && isVictimPvPDisabled) {
                        event.setCancelled(true);
                        msgPlayer(damager, PvPLog.getPvPlog().getConfig().getString("messages.pvp-denied"));
                    } else {
                        if (damager.getGameMode() == GameMode.CREATIVE) {
                            damager.setGameMode(GameMode.valueOf(PvPLog.getPvPlog().getConfig().getString("pvp.creative-setting").toUpperCase()));
                        }
                        ipVictim.intervalCombat(victim);
                        ipDamager.intervalCombat(damager);

                        // Spawn blood effect particles
                        if (PvPLog.getPvPlog().getConfig().getBoolean("pvp.blood-effect")) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Location location = victim.getLocation();
                                    victim.getWorld().spawnParticle(Particle.REDSTONE, location, 30, 0.5, 0.5, 0.5, 0, new Particle.DustOptions(org.bukkit.Color.RED, 1));
                                }
                            }.runTaskLater(PvPLog.getPvPlog(), 1); // Delay the blood effect to ensure the player is hit
                        }
                    }
                } else {
                    event.setCancelled(true);
                    msgPlayer(damager, PvPLog.getPvPlog().getConfig().getString("messages.pvp-denied"));
                }
            }
        }
    }
}