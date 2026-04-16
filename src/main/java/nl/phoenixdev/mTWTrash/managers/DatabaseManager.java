package nl.phoenixdev.mTWTrash.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import nl.phoenixdev.mTWTrash.MTWTrash;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final MTWTrash plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(MTWTrash plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File dbFile = new File(plugin.getDataFolder(), "database.db");

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");

        config.setMaximumPoolSize(1);
        config.setPoolName("MTWTrash-SQLite-Pool");

        try {
            dataSource = new HikariDataSource(config);
            createTables();
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Kon het SQLite-bestand niet initialiseren: " + e.getMessage(), e);
            return false;
        }
    }

    private void createTables() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS trash_cooldowns (" +
                             "player_uuid VARCHAR(36) NOT NULL, " +
                             "trashcan_id VARCHAR(100) NOT NULL, " +
                             "last_used BIGINT NOT NULL, " +
                             "PRIMARY KEY (player_uuid, trashcan_id)" +
                             ");")) {
            stmt.executeUpdate();
        }
    }

    public long getCooldown(UUID playerUUID, String trashcanId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT last_used FROM trash_cooldowns WHERE player_uuid = ? AND trashcan_id = ?")) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, trashcanId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("last_used");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Fout bij ophalen cooldown: " + e.getMessage(), e);
        }
        return 0L;
    }

    public void setCooldown(UUID playerUUID, String trashcanId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO trash_cooldowns (player_uuid, trashcan_id, last_used) VALUES (?, ?, ?)")) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, trashcanId);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Fout bij opslaan cooldown: " + e.getMessage(), e);
        }
    }

    public void clearCooldown(UUID playerUUID, String trashcanId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM trash_cooldowns WHERE player_uuid = ? AND trashcan_id = ?")) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, trashcanId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Fout bij verwijderen cooldown: " + e.getMessage(), e);
        }
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
