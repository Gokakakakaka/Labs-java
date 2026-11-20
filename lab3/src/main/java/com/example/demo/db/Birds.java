package com.example.demo.db;

import java.sql.*;
import java.util.*;

public class Birds {

    public static Map<String, Object> addBirds(
            Long lotId, Long userId, Long price, String date) {
        try {
            return addBird(Db.connection(), lotId, userId, price, date);
        } catch (SQLException e) {
            return ApiResponse.error("Ошибка добавления ставки: " + e.getMessage());
        }
    }

    static Map<String, Object> addBird(
            Connection c, long lotId, long userId, long price, String date)
            throws SQLException {
        String sql = "INSERT INTO birds(lot_id,user_id,price,date) VALUES(?,?,?,?)";
        try (PreparedStatement ps =
                     c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, lotId);
            ps.setLong(2, userId);
            ps.setLong(3, price);
            ps.setString(4, date);

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {

                if (!keys.next()) {
                    return ApiResponse.error("Ставка добавлена, но ID не получен");
                }

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("lot_id", lotId);
                data.put("user_id", userId);
                data.put("price", price);
                data.put("date", date);

                return ApiResponse.created("Ставка", keys.getLong(1), data);
            }
        }
    }

    static Map<String, Object> find(
            Connection c, long id, boolean lock) throws SQLException {
        String sql = "SELECT id,lot_id,user_id,price,date FROM birds WHERE id=?"
                + (lock ? " FOR UPDATE" : "");
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> data = new LinkedHashMap<>();

                for (String column : new String[]{
                        "id", "lot_id", "user_id", "price", "date"
                }) {
                    data.put(column, rs.getObject(column));
                }
                return data;
            }
        }
    }

    static Map<String, Object> highestBird(
            Connection c, long lotId) throws SQLException {
        String sql = "SELECT id,lot_id,user_id,price,date FROM birds "
                + "WHERE lot_id=? ORDER BY price DESC,id ASC LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, lotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> data = new LinkedHashMap<>();

                for (String column : new String[]{
                        "id", "lot_id", "user_id", "price", "date"
                }) {
                    data.put(column, rs.getObject(column));
                }
                return data;
            }
        }
    }

    static boolean hasBids(Connection c, long lotId) throws SQLException {
        String sql = "SELECT 1 FROM birds WHERE lot_id=? LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, lotId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    static long maxPrice(
            Connection c, long lotId, long fallback) throws SQLException {
        String sql = "SELECT MAX(price) FROM birds WHERE lot_id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, lotId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                long price = rs.getLong(1);
                return rs.wasNull() ? fallback : price;
            }
        }
    }

    static void delete(Connection c, long id) throws SQLException {
        String sql = "DELETE FROM birds WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public static Map<String, Object> updateBirds(
            Long id, Long lotId, Long userId, Long price, String date) {
        if (id == null) {
            return ApiResponse.error("ID ставки не может быть пустым");
        }
        String sql = "UPDATE birds SET lot_id=?,user_id=?,price=?,date=? WHERE id=?";
        try (Connection c = Db.connection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, lotId);
            ps.setObject(2, userId);
            ps.setObject(3, price);
            ps.setString(4, date);
            ps.setLong(5, id);

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                return ApiResponse.error("Ставка с ID " + id + " не найдена");
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", id);
            data.put("lot_id", lotId);
            data.put("user_id", userId);
            data.put("price", price);
            data.put("date", date);

            return ApiResponse.success("Ставка успешно обновлена", data);

        } catch (SQLException e) {
            return ApiResponse.error("Ошибка обновления ставки: " + e.getMessage());
        }
    }

    public static Map<String, Object> getBirds(Long id) {

        if (id == null) {
            return ApiResponse.error("ID ставки не может быть пустым");
        }

        try (Connection c = Db.connection()) {

            Map<String, Object> data = find(c, id, false);

            if (data == null) {
                return ApiResponse.error("Ставка не найдена");
            }

            return ApiResponse.success("Ставка получена", data);

        } catch (SQLException e) {
            return ApiResponse.error("Ошибка получения данных ставки: " + e.getMessage());
        }
    }

    public static Map<String, Object> getBirds() {

        List<Map<String, Object>> out = new ArrayList<>();

        String sql = "SELECT id,lot_id,user_id,price,date FROM birds";

        try (Connection c = Db.connection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {

            while (rs.next()) {

                Map<String, Object> data = new LinkedHashMap<>();

                for (String column : new String[]{
                        "id", "lot_id", "user_id", "price", "date"
                }) {
                    data.put(column, rs.getObject(column));
                }

                out.add(data);
            }

            return ApiResponse.success("Данные ставок получены", out);

        } catch (SQLException e) {
            return ApiResponse.error(
                    "Ошибка получения данных ставок: " + e.getMessage());
        }
    }

    public static Map<String, Object> removeBirds(Long id) {
        if (id == null) {
            return ApiResponse.error("ID ставки не может быть пустым");
        }
        String sql = "DELETE FROM birds WHERE id=?";
        try (Connection c = Db.connection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            boolean result = ps.executeUpdate() > 0;
            if (result) {
                return ApiResponse.success("Ставка удалена", id + " Deleted");
            }
            return ApiResponse.error("Невозможно удалить ставку");
        } catch (SQLException e) {
            return ApiResponse.error("Ошибка удаления ставки: " + e.getMessage());
        }
    }
}
