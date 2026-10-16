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



public class Lots {
    public static boolean addLots(Long item_id, Long pprice, String step, String date, String status) {
        String sql =
            "INSERT INTO lots (item_id,pprice,step,date,status) VALUES (?,?,?,?,?)";

        try {
            if (Database.connection == null || Database.connection.isClosed()) {
                System.out.println("Database is 200");
                return false;
            }

            try (PreparedStatement statement =
                     Database.connection.prepareStatement(sql)) {

                statement.setLong(1, item_id);
                statement.setLong(2, pprice);
                statement.setString(3, step);
                statement.setString(4, date);
                statement.setString(5, status);

                return statement.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println(
                "Error adding item: " + e.getMessage()
            );
            return false;
        }
    }

    public static boolean updateLots(Long id,Long item_id, Long pprice, String step, String date, String status) {
      String sql = 
        "UPDATE lots SET item_id=?, pprice=?, step=?,date=?,status=?  WHERE id = ?";

      try {
          if (Database.connection == null || Database.connection.isClosed()) {
             System.out.println("Database is 200");
              return false;
          }

          try (PreparedStatement statement =
                   Database.connection.prepareStatement(sql)) {
              statement.setLong(1, item_id);
              statement.setLong(2, pprice);
              statement.setString(3, step);
              statement.setString(4, date);
              statement.setString(5, status);

              return statement.executeUpdate() > 0;
          }

      } catch (SQLException e) {
          System.err.println(
              "Error updating lot: " + e.getMessage()
          );
          return false;
        }
    }

  public static String getLots(Long id) {
    String sql = 
      "SELECT * FROM lots WHERE id = ?";

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
                    long item_id = result.getLong("item_id");
                    long pprice = result.getLong("pprice"); 
                    String step = result.getString("step");
                    String date = result.getString("date");
                    String status = result.getString("status");
                    return String.format(
                        "{\"id\":%d,\"user id\":\"%d,\"first price\":\"%d,\"step\":\"%s\",\"date\":\"%s\",\"status\":\"%s\"}",
                        Id,item_id,pprice,step,date,status
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

  public static List getLots() {

    List<String> lots = new ArrayList<>();
    String sql = 
      "SELECT * FROM lots";

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
                    long item_id = result.getLong("item_id");
                    long pprice = result.getLong("pprice"); 
                    String step = result.getString("step");
                    String date = result.getString("date");
                    String status = result.getString("status");
                    String lot = String.format(
                        "{\"id\":%d,\"user id\":\"%d,\"first price\":\"%d,\"step\":\"%s\",\"date\":\"%s\",\"status\":\"%s\"}",
                        Id,item_id,pprice,step,date,status
                    );
                    
                    lots.add(lot);

                }

                return lots;
            }
        }

      } catch (SQLException e) {
        System.err.println(
            "Error getting user: " + e.getMessage());
        return null;
      }
  }

  public static boolean removeLots(Long id){ 
    String sql = 
      "DELETE FROM lots WHERE id = ?";

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

