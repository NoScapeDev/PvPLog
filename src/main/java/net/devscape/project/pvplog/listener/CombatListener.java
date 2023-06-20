package net.devscape.project.pvplog.listener;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.devscape.project.pvplog.utils.RegionUtils.isInRegion;
import static net.devscape.project.pvplog.utils.Utils.format;
import static net.devscape.project.pvplog.utils.Utils.msgPlayer;
import static net.devscape.project.pvplog.utils.VaultUtils.add;
import static net.devscape.project.pvplog.utils.VaultUtils.take;

public class CombatListener implements Listener {

    private final Map<Player, Vector> playerPreviousLocations = new HashMap<>();

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
    public void onRegionPushback(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(player);

        // Check if the player is in combat mode
        if (ip.isCombat()) {
            if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null && Bukkit.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                if (PvPLog.getPvPlog().getConfig().getBoolean("safe-zones.safezone-region-push-back.enable")) {
                    for (String regions : PvPLog.getPvPlog().getConfig().getStringList("safe-zones.wg-regions")) {
                        if (isInRegion(player, regions)) {
                            Vector previousLocation = playerPreviousLocations.get(player);
                            if (previousLocation != null && !previousLocation.equals(player.getLocation().toVector())) {
                                // Player entered the region and has moved
                                Vector pushDirection = player.getLocation().getDirection().normalize().multiply(-1);
                                if (pushDirection.length() > 0 && Double.isFinite(pushDirection.getX()) && Double.isFinite(pushDirection.getY()) && Double.isFinite(pushDirection.getZ())) {
                                    double pushDistance = PvPLog.getPvPlog().getConfig().getDouble("safe-zones.safezone-region-push-back.force");

                                    pushDirection.setY(0);

                                    pushDirection.multiply(pushDistance);
                                    player.setVelocity(pushDirection);
                                }
                            }
                            return;
                        }
                    }

                    playerPreviousLocations.put(player, player.getLocation().toVector());
                }
            }
        } else {
            if (playerPreviousLocations.containsKey(player)) {
                playerPreviousLocations.remove(player);
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
    public void onPlayerDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof Player) {
            Player victim = (Player) e.getEntity();
            Player killer = victim.getKiller();

            if (PvPLog.getPvPlog().getConfig().getBoolean("economy.enable") && Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
                String killer_value = PvPLog.getPvPlog().getConfig().getString("economy.kill.value");
                String death_value = PvPLog.getPvPlog().getConfig().getString("economy.death.value");

                assert killer_value != null;
                if (killer_value.startsWith("-")) {
                    String message = PvPLog.getPvPlog().getConfig().getString("messages.kill-economy-taken");

                    int value = Integer.parseInt(killer_value.replace("-", ""));
                    message = message.replaceAll("%value%", String.valueOf(value));
                    take(killer, value);
                    killer.sendMessage(format(message));
                } else {
                    String message = PvPLog.getPvPlog().getConfig().getString("messages.kill-economy-add");

                    int value = Integer.parseInt(killer_value);
                    message = message.replaceAll("%value%", String.valueOf(value));
                    add(killer, value);
                    killer.sendMessage(format(message));
                }

                assert death_value != null;
                if (death_value.startsWith("-")) {
                    String message = PvPLog.getPvPlog().getConfig().getString("messages.death-economy-taken");

                    int value = Integer.parseInt(death_value.replace("-", ""));
                    message = message.replaceAll("%value%", String.valueOf(value));
                    take(victim, value);
                    victim.sendMessage(format(message));
                } else {
                    String message = PvPLog.getPvPlog().getConfig().getString("messages.death-economy-add");

                    int value = Integer.parseInt(death_value);
                    message = message.replaceAll("%value%", String.valueOf(value));
                    add(victim, value);

                    victim.sendMessage(format(message));
                }

            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && (event.getDamager() instanceof Player || event.getDamager() instanceof Projectile || event.getDamager() instanceof ThrownPotion)) {

            if (Bukkit.getServer().getPluginManager().getPlugin("Citizens") != null) {
                NPCRegistry npc = CitizensAPI.getNPCRegistry();

                if (npc.isNPC(event.getEntity())) {
                    return;
                }
            }

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

            iPlayer ipVictim = PvPLog.getPvPlog().getPlayerManager().getPlayer(victim);
            iPlayer ipDamager = PvPLog.getPvPlog().getPlayerManager().getPlayer(damager);

            if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null && Bukkit.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                for (String regions : PvPLog.getPvPlog().getConfig().getStringList("safe-zones.pvp-regions")) {
                    if (isInRegion(victim, regions) && isInRegion(damager, regions)) {
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

                        return;
                    }
                }

                for (String regions : PvPLog.getPvPlog().getConfig().getStringList("safe-zones.wg-regions")) {
                    if (isInRegion(victim, regions)) {
                        event.setCancelled(true);
                        msgPlayer(damager, PvPLog.getPvPlog().getConfig().getString("messages.pvp-denied"));
                        return;
                    }
                }
            }

            for (String worlds : PvPLog.getPvPlog().getConfig().getStringList("safe-zones.forced-worlds")) {
                if (victim.getLocation().getWorld().getName().equalsIgnoreCase(worlds)) {
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
                    return;
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