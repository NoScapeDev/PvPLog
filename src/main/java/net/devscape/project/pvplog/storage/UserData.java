package net.devscape.project.pvplog.storage;

import net.devscape.project.pvplog.PvPLog;
import net.devscape.project.pvplog.handlers.iPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UserData {
    public boolean exists(Player player) {
        try {
            PreparedStatement statement = PvPLog.getDatabase().getConnection().prepareStatement("SELECT * FROM `users` WHERE (UUID=?)");
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean exists(OfflinePlayer player) {
        try {
            PreparedStatement statement = PvPLog.getDatabase().getConnection().prepareStatement("SELECT * FROM `users` WHERE (UUID=?)");
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void createPlayer(Player player) {
        if (exists(player)) {
            return;
        }

        boolean defaultPvP = PvPLog.getPvPlog().getConfig().getBoolean("pvp.default-pvp");

        try (PreparedStatement statement = PvPLog.getDatabase().getConnection().prepareStatement(
                "INSERT INTO `users` (name, uuid, pvp, trusted) VALUES (?,?,?,?)")) {
            statement.setString(1, player.getName()); // name
            statement.setString(2, String.valueOf(player.getUniqueId())); // uuid
            statement.setBoolean(3, defaultPvP); // pvp
            statement.setString(4, ""); // trusted
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadPlayer(Player player) {
        try (PreparedStatement statement = PvPLog.getDatabase().getConnection().prepareStatement(
                "SELECT * FROM users WHERE uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                boolean pvp = result.getBoolean("pvp");
                String uuidID = result.getString("uuid");
                String trustedString = result.getString("trusted");

                iPlayer ip = new iPlayer(uuidID, pvp, false);

                trustedString = trustedString.replace("[", "");
                trustedString = trustedString.replace("]", "");

                if (!trustedString.isEmpty()) {

                    List<UUID> t = deserializeTrust(trustedString);

                    if (t.size() != 0) {
                        for (UUID u : t) {
                            ip.getTrusted().add(u);
                        }
                    }
                }

                PvPLog.getPvPlog().getPlayerManager().getPlayerList().add(ip);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void loadOfflinePlayer(OfflinePlayer player) {
        try (PreparedStatement statement = PvPLog.getDatabase().getConnection().prepareStatement(
                "SELECT * FROM users WHERE uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                boolean pvp = result.getBoolean("pvp");
                String uuidID = result.getString("uuid");
                String trustedString = result.getString("trusted");

                iPlayer ip = new iPlayer(uuidID, pvp, false);

                trustedString = trustedString.replace("[", "");
                trustedString = trustedString.replace("]", "");

                if (!trustedString.isEmpty()) {

                    List<UUID> t = deserializeTrust(trustedString);

                    if (t.size() != 0) {
                        for (UUID u : t) {
                            ip.getTrusted().add(u);
                        }
                    }
                }

                PvPLog.getPvPlog().getPlayerManager().getPlayerList().add(ip);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void addTrusted(OfflinePlayer player, Player trusted) {
        loadOfflinePlayer(player);

        iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(player);
        ip.getTrusted().add(trusted.getUniqueId());

        saveUser(player, ip);
        PvPLog.getPvPlog().getPlayerManager().getPlayerList().remove(ip);
    }

    public void removeTrusted(OfflinePlayer player, Player trusted) {
        loadOfflinePlayer(player);

        iPlayer ip = PvPLog.getPvPlog().getPlayerManager().getPlayer(player);
        ip.getTrusted().remove(trusted.getUniqueId());

        saveUser(player, ip);
        PvPLog.getPvPlog().getPlayerManager().getPlayerList().remove(ip);
    }

    public void saveUser(Player player, iPlayer ip) {
        String sql = "UPDATE `users` SET pvp=?, trusted=? WHERE UUID=?";

        try (PreparedStatement statement = PvPLog.getDatabase().getConnection().prepareStatement(sql)) {
            statement.setBoolean(1, ip.isPvP());
            statement.setString(2, serializeTrust(ip.getTrusted()));
            statement.setString(3, player.getUniqueId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveUser(OfflinePlayer player, iPlayer ip) {
        String sql = "UPDATE `users` SET pvp=?, trusted=? WHERE UUID=?";

        try (PreparedStatement statement = PvPLog.getDatabase().getConnection().prepareStatement(sql)) {
            statement.setBoolean(1, ip.isPvP());
            statement.setString(2, serializeTrust(ip.getTrusted()));
            statement.setString(3, player.getUniqueId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setPvP(OfflinePlayer player, boolean pvp) {
        String sql = "UPDATE `users` SET pvp=? WHERE (UUID=?)";
        try (PreparedStatement statement = PvPLog.getDatabase().getConnection().prepareStatement(sql)) {
            statement.setBoolean(1, pvp);
            statement.setString(2, player.getUniqueId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean getPvP(UUID uuid) {
        boolean value = false;
        String query = "SELECT * FROM `users` WHERE (UUID=?)";
        try (PreparedStatement statement = PvPLog.getDatabase().getConnection().prepareStatement(query)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    value = resultSet.getBoolean("pvp");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return value;
    }

    private String serializeTrust(List<UUID> trusted) {
        if (trusted == null || trusted.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (UUID uuid : trusted) {
            sb.append(uuid).append(",");
        }
        return sb.toString();
    }


    private List<UUID> deserializeTrust(String uuid) {
        String[] UUIDString = uuid.split(",");
        List<UUID> uuidList = new ArrayList<>();

        for (String uuidString : UUIDString) {
            uuidList.add(UUID.fromString(uuidString));
        }

        return uuidList;
    }
}