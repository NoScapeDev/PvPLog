package net.devscape.project.pvplog.storage;

import net.devscape.project.pvplog.PvPLog;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.logging.Logger;

public class Database {

    private final FileConfiguration config = PvPLog.getPvPlog().getConfig();

    private final String host = config.getString("data.host");
    private final int port = config.getInt("data.port");
    private final String database = config.getString("data.database");
    private final String username = config.getString("data.username");
    private final String password = config.getString("data.password");
    private final String options = config.getString("data.options");

    public Connection connection;
    private boolean isconnected = false;

    public Database() {
        this.connect(this.host, this.port, this.database, this.username, this.password, true);
    }

    public void openConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();

            this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?" + options, username, password);

        }
    }

    public void connect(String host, int port, String database, String user, String pass, boolean ssl) {
        try {
            synchronized (this) {
                if (connection != null && !connection.isClosed()) {
                    Logger.getLogger("Error: connection to sql was not successful.");
                    return;
                }
                Class.forName("com.mysql.cj.jdbc.Driver");
                this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + ssl + "&autoReconnect=true", user, pass);
                this.isconnected = true;
                createTable();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("MYSQL: Something wrong with connecting to mysql database type, check mysql data details before contacting the developer if you see this.");
        }
    }

    public void createTable() throws SQLException {
        String userTable = "CREATE TABLE IF NOT EXISTS users " +
                "(name VARCHAR(100), " +
                "uuid VARCHAR(100) primary key, " +
                "pvp VARCHAR(100), " +
                "trusted VARCHAR(100))";

        Statement stmt = connection.createStatement();
        stmt.execute(userTable);
    }

    public void updateQuery(String query) {
        Connection con = connection;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = con.prepareStatement(query);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(null, preparedStatement);
        }
    }

    public void disconnected() {
        try {
            if (isConnected()) {
                this.connection.close();
                this.isconnected = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet executeQuery(String query) {
        if (isConnected()) {
            return null;
        } else {
            try {
                this.connection.createStatement().executeQuery(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void closeResources(ResultSet rs, PreparedStatement st) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        return !this.isconnected;
    }

    public Connection getConnection() {
        return this.connection;
    }
}