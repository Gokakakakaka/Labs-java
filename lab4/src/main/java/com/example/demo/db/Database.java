package com.example.demo.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;


public class Database {

    public static Connection connection;

    public static void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = System.getenv("DB_URL");
            String user = System.getenv("DB_USER");
            String password = System.getenv("DB_PASSWORD");

            connection = DriverManager.getConnection(
                url, user, password
            );

            System.out.println("Connected to MySQL");
        }
    }
}
