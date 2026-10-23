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
            String url = "jdbc:mysql://localhost:3306/java_app";
            String user = "admin";
            String password = "12345";

            connection = DriverManager.getConnection(
                url, user, password
            );

            System.out.println("Connected to MySQL");
        }
    }
}
