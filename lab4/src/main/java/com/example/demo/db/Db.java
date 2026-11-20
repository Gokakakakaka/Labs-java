package com.example.demo.db;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Единственная точка получения JDBC-соединений.
 * DataSource автоматически настраивается Spring Boot из application.yml
 * и использует пул соединений HikariCP.
 */
@Component
public final class Db {
    private static volatile DataSource dataSource;

    public Db(DataSource dataSource) {
        Db.dataSource = dataSource;
    }

    public static Connection connection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Источник данных ещё не инициализирован Spring");
        }
        return dataSource.getConnection();
    }
}
