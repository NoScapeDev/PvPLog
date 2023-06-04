package net.devscape.project.pvplog;

import net.devscape.project.pvplog.commands.PvPCommands;
import net.devscape.project.pvplog.commands.PvPLogCommand;
import net.devscape.project.pvplog.listener.CombatListener;
import net.devscape.project.pvplog.listener.PlayerJoinLeave;
import net.devscape.project.pvplog.listener.WorldListener;
import net.devscape.project.pvplog.managers.PlayerManager;
import net.devscape.project.pvplog.storage.Database;
import net.devscape.project.pvplog.storage.UserData;
import org.bukkit.plugin.java.JavaPlugin;

public final class PvPLog extends JavaPlugin {

    private static PvPLog pvplog;
    private PlayerManager playerManager;
    private static Database database;
    private UserData userData;

    @Override
    public void onEnable() {
        // Plugin startup logic

        init();

    }

    private void init() {
        pvplog = this;

        saveDefaultConfig();

        playerManager = new PlayerManager();

        database = new Database();
        userData = new UserData();

        getCommand("pvp").setExecutor(new PvPCommands());
        getCommand("pvplog").setExecutor(new PvPLogCommand());

        getServer().getPluginManager().registerEvents(new CombatListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinLeave(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public static PvPLog getPvPlog() {
        return pvplog;
    }

    public void reload() {
        super.reloadConfig();
    }

    public static Database getDatabase() {
        return database;
    }

    public UserData getUserData() {
        return userData;
    }
}
