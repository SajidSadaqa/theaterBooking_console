package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DatabaseConfig {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/theater_booking";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "AhmadSajidSura2003";

    private static HikariDataSource dataSource;

    static {
        {
            try {
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(DB_URL);
                config.setUsername(DB_USERNAME);
                config.setPassword(DB_PASSWORD);
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setConnectionTimeout(30000);
                config.setIdleTimeout(600000);
                config.setMaxLifetime(1800000);
                config.setAutoCommit(false);

                dataSource = new HikariDataSource(config);
                System.out.println("Database connection pool initialized successfully");
            } catch (Exception e)
            {
                System.err.println("Failed to initialize database connection: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
            System.out.println("Database connection pool closed");
        }
    }
}