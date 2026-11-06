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



public class Item {
    public static boolean addItem(Long user_id, String about, String cndn) {
        String sql =
            "INSERT INTO items (user_id,about,cndn) VALUES (?,?,?)";

        try {
            if (Database.connection == null || Database.connection.isClosed()) {
                System.out.println("Database is 200");
                return false;
            }

            try (PreparedStatement statement =
                     Database.connection.prepareStatement(sql)) {

                statement.setLong(1, user_id);
                statement.setString(2, about);
                statement.setString(3, cndn);

                return statement.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println(
                "Error adding user: " + e.getMessage()
            );
            return false;
        }
    }

    public static boolean updateItem(Long id, Long user_id, String about, String cndn) {
      String sql = 
        "UPDATE items SET user_id=?, about=?, cndn=?  WHERE id = ?";

      try {
          if (Database.connection == null || Database.connection.isClosed()) {
             System.out.println("Database is 200");
              return false;
          }

          try (PreparedStatement statement =
                   Database.connection.prepareStatement(sql)) {
              statement.setLong(1, user_id);
              statement.setString(2, about);
              statement.setString(3, cndn);
              statement.setLong(4, id);
              return statement.executeUpdate() > 0;
          }

      } catch (SQLException e) {
          System.err.println(
              "Error updating item: " + e.getMessage()
          );
          return false;
        }
    }

  public static String getItem(Long id) {
    String sql = 
      "SELECT * FROM items WHERE id = ?";

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
                    long Id = result.getLong("id");
                    long userId = result.getLong("user_id");
                    String about = result.getString("about");
                    String cndn = result.getString("cndn");

                    return String.format(
                        "{\"id\":%d,\"user id\":\"%d,\"about\":\"%s\",\"about\":\"%s\"}",
                        Id,userId, about, cndn
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

  public static List getItem() {

    List<String> items = new ArrayList<>();
    String sql = 
      "SELECT * FROM items";

      try {
        if (Database.connection == null ||
                Database.connection.isClosed()) {
            return null;
        }

        try (PreparedStatement statement =
                 Database.connection.prepareStatement(sql)) {


            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    long Id = result.getLong("id");
                    long userId = result.getLong("user_id");
                    String about = result.getString("about");
                    String cndn = result.getString("cndn");

                    String user = String.format(
                        "{\"id\":%d,\"user id\":\"%d,\"about\":\"%s\",\"about\":\"%s\"}",
                        Id,userId, about, cndn
                    );
                    
                    items.add(user);

                }

                return items;
            }
        }

      } catch (SQLException e) {
        System.err.println(
            "Error getting user: " + e.getMessage());
        return null;
      }
  }

  public static boolean removeItem(Long id){ 
    String sql = 
      "DELETE FROM items WHERE id = ?";

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

