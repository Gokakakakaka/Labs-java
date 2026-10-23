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



public class Birds {
    public static boolean addBirds(Long lot_id, Long user_id, Long price, String date) {
        String sql =
            "INSERT INTO birds (lot_id,user_id,price,date) VALUES (?,?,?,?)";

        try {
            if (Database.connection == null || Database.connection.isClosed()) {
                System.out.println("Database is 200");
                return false;
            }

            try (PreparedStatement statement =
                     Database.connection.prepareStatement(sql)) {
                statement.setLong(1, lot_id);
                statement.setLong(2, user_id);
                statement.setLong(3, price);
                statement.setString(4, date);

                return statement.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println(
                "Error adding lot: " + e.getMessage()
            );
            return false;
        }
    }

    public static boolean updateBirds(Long id,Long lot_id, Long user_id, Long price, String date) {
      String sql = 
        "UPDATE birds SET lot_id=?,user_id=?,price=?,date=?  WHERE id = ?";

      try {
          if (Database.connection == null || Database.connection.isClosed()) {
             System.out.println("Database is 200");
              return false;
          }

          try (PreparedStatement statement =
                   Database.connection.prepareStatement(sql)) {
              statement.setLong(1, lot_id);
              statement.setLong(2, user_id);
              statement.setLong(3, price);
              statement.setString(4, date);
              statement.setLong(5, id);
              return statement.executeUpdate() > 0;
          }

      } catch (SQLException e) {
          System.err.println(
              "Error updating lot: " + e.getMessage()
          );
          return false;
        }
    }

  public static String getBirds(Long id) {
    String sql = 
      "SELECT * FROM birds WHERE id = ?";

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
                    long lot_id = result.getLong("lot_id");
                    long user_id = result.getLong("user_id");
                    long price = result.getLong("price"); 
                    String date = result.getString("date");
                    return String.format(
                        "{\"id\":%d,\"lot id\":%d,\"user id\":\"%d,\"price\":\"%d,\"date\":\"%s\"}",
                        Id,lot_id,user_id,price,date
                    );
                }

                return null;
            }
        }

      } catch (SQLException e) {
        System.err.println(
            "Error getting lot: " + e.getMessage());
        return null;
      }
  }

  public static List getBirds() {

    List<String> birds = new ArrayList<>();
    String sql = 
      "SELECT * FROM birds";

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
                    long lot_id = result.getLong("lot_id");
                    long user_id = result.getLong("user_id");
                    long price = result.getLong("price"); 
                    String date = result.getString("date");
                    String lot = String.format(
                        "{\"id\":%d,\"lot id\":%d,\"user id\":\"%d,\"price\":\"%d,\"date\":\"%s\"}",
                        Id,lot_id,user_id,price,date
                    );
                    
                    birds.add(lot);

                }

                return birds;
            }
        }

      } catch (SQLException e) {
        System.err.println(
            "Error getting user: " + e.getMessage());
        return null;
      }
  }

  public static boolean removeBirds(Long id){ 
    String sql = 
      "DELETE FROM birds WHERE id = ?";

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
            "Error getting lot: " + e.getMessage());
        return false;
      }
  }
}

