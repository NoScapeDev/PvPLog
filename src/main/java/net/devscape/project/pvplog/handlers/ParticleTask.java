package net.devscape.project.pvplog.handlers;

import net.devscape.project.pvplog.PvPLog;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParticleTask extends BukkitRunnable {

    private static final Map<UUID, ParticleTask> tasks = new HashMap<>();

    private final Player player;
    private final Plugin plugin;

    public ParticleTask(Player player, Plugin plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (PvPLog.getPvPlog().getPlayerManager().getPlayer(player).isPvP()) {
            float size = 2.0f;
            Bukkit.getScheduler().runTask(plugin, () -> player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 2.6, 0), 0, new Particle.DustOptions(Color.RED, size)));
        } else {
            cancel();
            tasks.remove(player.getUniqueId());
        }
    }

    public static void startForPlayer(Player player, Plugin plugin) {
        ParticleTask task = new ParticleTask(player, plugin);
        tasks.put(player.getUniqueId(), task);
        task.runTaskTimerAsynchronously(plugin, 0, 20); // Adjust the interval as needed
    }

    public static void stopForPlayer(Player player) {
        ParticleTask task = tasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
}