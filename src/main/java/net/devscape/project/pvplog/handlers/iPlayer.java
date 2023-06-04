package net.devscape.project.pvplog.handlers;

import net.devscape.project.pvplog.PvPLog;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.devscape.project.pvplog.utils.Utils.msgPlayer;
import static net.devscape.project.pvplog.utils.Utils.sendActionBar;

public class iPlayer {

    private final String uuid;
    private boolean pvp;
    private boolean combat;

    /// to be implemented for toggling on/off trust for player.
    private boolean trustToggle;
    private final List<UUID> trusted = new ArrayList<>();

    private int time = PvPLog.getPvPlog().getConfig().getInt("combat.interval");

    public iPlayer(String uuid, boolean pvp, boolean combat) {
        this.uuid = uuid;
        this.pvp = pvp;
        this.combat = combat;
    }

    public List<UUID> getTrusted() {
        return trusted;
    }

    public boolean isCombat() {
        return combat;
    }

    public void setCombat(boolean combat) {
        this.combat = combat;
    }

    public boolean isPvP() {
        return pvp;
    }

    public void setPvP(boolean pvp) {
        this.pvp = pvp;
    }

    public boolean isPlayerTrusted(OfflinePlayer player) {
        return getTrusted().contains(player.getUniqueId());
    }

    public void intervalCombat(Player player) {
        if (!isCombat()) {
            combat = true;
            time = PvPLog.getPvPlog().getConfig().getInt("combat.interval");
            msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.combat-toggle-enabled"));

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isCombat()) {
                        msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.combat-toggle-disabled"));
                        sendActionBar(player, PvPLog.getPvPlog().getConfig().getString("messages.combat-toggle-disabled"));
                        resetCombatTimer();
                        cancel();
                        return;
                    }

                    if (time == 0) {
                        msgPlayer(player, PvPLog.getPvPlog().getConfig().getString("messages.combat-toggle-disabled"));
                        sendActionBar(player, PvPLog.getPvPlog().getConfig().getString("messages.combat-toggle-disabled"));
                        combat = false;
                        resetCombatTimer();
                        cancel();
                        return;
                    }

                    if (PvPLog.getPvPlog().getConfig().getBoolean("combat.action-bar")) {
                        sendActionBar(player, Objects.requireNonNull(PvPLog.getPvPlog().getConfig().getString("messages.combat-action-bar")).replaceAll("%time%", String.valueOf(time)));
                    }

                    time--;
                }
            }.runTaskTimer(PvPLog.getPvPlog(), 0, 20);
        } else {
            resetCombatTimer();
        }
    }

    // Method to reset the combat timer
    private void resetCombatTimer() {
        time = PvPLog.getPvPlog().getConfig().getInt("combat.interval");
    }

    // Method to stop the combat timer
    public void stopCombatTimer() {
        combat = false;
        resetCombatTimer();
    }

    public String getUuid() {
        return uuid;
    }

    public int getTime() {
        return time;
    }
}
