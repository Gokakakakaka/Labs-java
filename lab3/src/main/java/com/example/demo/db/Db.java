package com.example.demo.db;

import java.lang.reflect.*;
import java.sql.*;
import java.util.Properties;
import java.io.InputStream;

public final class Db {
    public static Connection connection() throws SQLException {
        if (con == null || con.isClosed()) connect();
        Connection raw = con;
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
            new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                if ("close".equals(method.getName())) return null;
                try { return method.invoke(raw, args); }
                catch (InvocationTargetException e) { throw e.getCause(); }
            });
    }
    
    public static Connection con;
    public static void connect() throws SQLException {
        if (con == null || con.isClosed()) {
            String url = System.getenv("DB_URL");
            String user = System.getenv("DB_USER");
            String password = System.getenv("DB_PASSWORD");

            con = DriverManager.getConnection(
                url, user, password
            );

            System.out.println("Connected to MySQL");
        }
    }

}
