package me.minseok.velocitydiscordlogger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Database {

    private final PluginConfig config;
    private final Logger logger;
    private HikariDataSource dataSource;

    public Database(PluginConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("MySQL driver loaded successfully");
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load MySQL driver", e);
            return;
        }

        try {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getDatabaseHost() + ":" + config.getDatabasePort() + "/"
                    + config.getDatabaseName() + "?useSSL=false&allowPublicKeyRetrieval=true");
            hikariConfig.setUsername(config.getDatabaseUsername());
            hikariConfig.setPassword(config.getDatabasePassword());
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setMinimumIdle(2);
            hikariConfig.setConnectionTimeout(30000);
            hikariConfig.setIdleTimeout(600000);
            hikariConfig.setMaxLifetime(1800000);
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(hikariConfig);
            logger.info("HikariCP connection pool initialized with maxPoolSize=10, minIdle=2");

            createTable();
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
        }
    }

    private void createTable() {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS linked_accounts (" +
                                "uuid VARCHAR(36) PRIMARY KEY, " +
                                "discord_id VARCHAR(20) NOT NULL)")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to create linked_accounts table", e);
        }
    }

    public String getDiscordId(UUID uuid) {
        if (uuid == null) {
            logger.warn("UUID is null in getDiscordId");
            return null;
        }
        
        if (dataSource == null || dataSource.isClosed()) {
            logger.error("DataSource is not initialized or closed. Attempting to reconnect...");
            try {
                connect();
            } catch (Exception e) {
                logger.error("Database not connected and failed to reconnect.", e);
                return null;
            }
        }
        
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection
                        .prepareStatement("SELECT discord_id FROM linked_accounts WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String discordId = resultSet.getString("discord_id");
                    logger.info("Retrieved discord_id for uuid: " + uuid);
                    return discordId;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get discord id for uuid: " + uuid, e);
        }
        return null;
    }

    public void saveLinkedAccount(UUID uuid, String discordId) {
        if (uuid == null || discordId == null) {
            logger.warn("UUID or discordId is null in saveLinkedAccount");
            return;
        }
        
        if (dataSource == null || dataSource.isClosed()) {
            logger.error("DataSource is not initialized or closed. Attempting to reconnect...");
            try {
                connect();
            } catch (Exception e) {
                logger.error("Database not connected and failed to reconnect.", e);
                return;
            }
        }
        
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection
                        .prepareStatement(
                                "INSERT INTO linked_accounts (uuid, discord_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE discord_id = ?")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, discordId);
            statement.setString(3, discordId);
            statement.executeUpdate();
            logger.info("Saved linked account for uuid: " + uuid + " with discordId: " + discordId);
        } catch (SQLException e) {
            logger.error("Failed to save linked account for uuid: " + uuid, e);
        }
    }

    public void unlinkAccount(UUID uuid) {
        if (uuid == null) {
            logger.warn("UUID is null in unlinkAccount");
            return;
        }
        
        if (dataSource == null || dataSource.isClosed()) {
            logger.error("DataSource is not initialized or closed. Attempting to reconnect...");
            try {
                connect();
            } catch (Exception e) {
                logger.error("Database not connected and failed to reconnect.", e);
                return;
            }
        }
        
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection
                        .prepareStatement("DELETE FROM linked_accounts WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
            logger.info("Unlinked account for uuid: " + uuid);
        } catch (SQLException e) {
            logger.error("Failed to unlink account for uuid: " + uuid, e);
        }
    }

    public void close() {
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                logger.info("HikariCP connection pool closed successfully");
            }
        } catch (Exception e) {
            logger.error("Error closing database connection pool", e);
        }
    }

    /**
     * 연결 풀 상태 확인
     */
    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }
}
