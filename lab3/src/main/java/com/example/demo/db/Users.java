package com.example.demo.db;

import com.example.demo.db.Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;



public class Users {
    public static boolean addUser(String username, String email) {
        String sql =
            "INSERT INTO users (name,email) VALUES (?,?)";

        try {
            if (Database.connection == null || Database.connection.isClosed()) {
                System.out.println("Database is 200");
                return false;
            }

            try (PreparedStatement statement =
                     Database.connection.prepareStatement(sql)) {

                statement.setString(1, username);
                statement.setString(2, email);

                return statement.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println(
                "Error adding user: " + e.getMessage()
            );
            return false;
        }
    }

    public static boolean updateUser(Long id, String username, String email) {
      String sql = 
        "UPDATE users SET email = ?, name = ? WHERE id = ?";

      try {
          if (Database.connection == null || Database.connection.isClosed()) {
             System.out.println("Database is 200");
              return false;
          }

          try (PreparedStatement statement =
                   Database.connection.prepareStatement(sql)) {
              statement.setString(2, username);
              statement.setString(1, email);
              statement.setLong(3, id);
              return statement.executeUpdate() > 0;
          }

      } catch (SQLException e) {
          System.err.println(
              "Error updating user: " + e.getMessage()
          );
          return false;
        }
    }

  public static String getUser(Long id) {
    String sql = 
      "SELECT * FROM users WHERE id = ?";

      try {
        if (Database.connection == null ||
                Database.connection.isClosed()) {
            return null;
        }

        try (PreparedStatement statement =
                 Database.connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    long userId = result.getLong("id");
                      String name = result.getString("name");
                    String email = result.getString("email");

                    return String.format(
                        "{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\"}",
                        userId, name, email
                    );
                }

                return null;
            }
        }

      } catch (SQLException e) {
        System.err.println(
            "Error getting user: " + e.getMessage());
        return null;
      }
  }

  public static List getUser() {

    List<String> users = new ArrayList<>();
    String sql = 
      "SELECT * FROM users";

      try {
        if (Database.connection == null ||
                Database.connection.isClosed()) {
            return null;
        }

        try (PreparedStatement statement =
                 Database.connection.prepareStatement(sql)) {


            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    long userId = result.getLong("id");
                      String name = result.getString("name");
                    String email = result.getString("email");

                    String user = String.format(
                        "{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\"}",
                        userId, name, email
                    );
                    
                    users.add(user);

                }

                return users;
            }
        }

      } catch (SQLException e) {
        System.err.println(
            "Error getting user: " + e.getMessage());
        return null;
      }
  }

  public static boolean removeUser(Long id){ 
    String sql = 
      "DELETE FROM users WHERE id = ?";

      try {
        if (Database.connection == null ||
                Database.connection.isClosed()) {
            return false;
        }

        try (PreparedStatement statement =
                 Database.connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }

      } catch (SQLException e) {
        System.err.println(
            "Error getting user: " + e.getMessage());
        return false;
      }

  }



}

