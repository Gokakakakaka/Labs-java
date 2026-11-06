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

public class Deals {

    public static boolean addDeals(
            Long lot_id, Long bird_id, Long price, String status) {

        String sql =
            "INSERT INTO deals (lot_id, bird_id, price, status) VALUES (?, ?, ?, ?)";

        try {
            if (Database.connection == null ||
                    Database.connection.isClosed()) {
                System.out.println("Database is 200");
                return false;
            }

            try (PreparedStatement statement =
                     Database.connection.prepareStatement(sql)) {

                statement.setLong(1, lot_id);
                statement.setLong(2, bird_id);
                statement.setLong(3, price);
                statement.setString(4, status);

                return statement.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error adding deal: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateDeals(
            Long id, Long lot_id, Long bird_id,
            Long price, String status) {

        String sql =
            "UPDATE deals SET lot_id = ?, bird_id = ?, price = ?, status = ? WHERE id = ?";

        try {
            if (Database.connection == null ||
                    Database.connection.isClosed()) {
                System.out.println("Database is 200");
                return false;
            }

            try (PreparedStatement statement =
                     Database.connection.prepareStatement(sql)) {

                statement.setLong(1, lot_id);
                statement.setLong(2, bird_id);
                statement.setLong(3, price);
                statement.setString(4, status);
                statement.setLong(5, id);

                return statement.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error updating deal: " + e.getMessage());
            return false;
        }
    }

    public static String getDeals(Long id) {
        String sql = "SELECT * FROM deals WHERE id = ?";

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
                        long dealId = result.getLong("id");
                        long lotId = result.getLong("lot_id");
                        long birdId = result.getLong("bird_id");
                        long price = result.getLong("price");
                        String status = result.getString("status");

                        return String.format(
                            "{\"id\":%d,\"lot_id\":%d," +
                            "\"bird_id\":%d,\"price\":%d,\"status\":\"%s\"}",
                            dealId, lotId, birdId, price, status
                        );
                    }

                    return null;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting deal: " + e.getMessage());
            return null;
        }
    }

    public static List<String> getDeals() {
        List<String> deals = new ArrayList<>();
        String sql = "SELECT * FROM deals";

        try {
            if (Database.connection == null ||
                    Database.connection.isClosed()) {
                return deals;
            }

            try (PreparedStatement statement =
                     Database.connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    long id = result.getLong("id");
                    long lotId = result.getLong("lot_id");
                    long birdId = result.getLong("bird_id");
                    long price = result.getLong("price");
                    String status = result.getString("status");

                    String deal = String.format(
                        "{\"id\":%d,\"lot_id\":%d," +
                        "\"bird_id\":%d,\"price\":%d,\"status\":\"%s\"}",
                        id, lotId, birdId, price, status
                    );

                    deals.add(deal);
                }
            }

            return deals;

        } catch (SQLException e) {
            System.err.println("Error getting deals: " + e.getMessage());
            return deals;
        }
    }

    public static boolean removeDeals(Long id) {
        String sql = "DELETE FROM deals WHERE id = ?";

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
            System.err.println("Error removing deal: " + e.getMessage());
            return false;
        }
    }
}
