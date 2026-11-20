package com.example.demo.db;

import java.sql.*;
import java.util.*;

public class Deals {

    public static Map<String, Object> addDeals(
            Long lotId, Long birdId, Long price, String status) {

        try {
            return addDeal(Db.connection(), lotId, birdId, price, status);
        } catch (SQLException e) {
            return ApiResponse.error("Ошибка добавления сделки: " + e.getMessage());
        }
    }

    static Map<String, Object> addDeal(
            Connection c, long lotId, long birdId, long price, String status)
            throws SQLException {

        String sql = "INSERT INTO deals(lot_id,bird_id,price,status) VALUES(?,?,?,?)";

        try (PreparedStatement ps =
                     c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, lotId);
            ps.setLong(2, birdId);
            ps.setLong(3, price);
            ps.setString(4, status);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {

                if (!keys.next()) {
                    return ApiResponse.error("Сделка добавлена, но ID не получен");
                }

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("lot_id", lotId);
                data.put("bird_id", birdId);
                data.put("price", price);
                data.put("status", status);

                return ApiResponse.created("Сделка", keys.getLong(1), data);
            }
        }
    }

    static boolean existsForLot(Connection c, long lotId) throws SQLException {

        String sql = "SELECT 1 FROM deals WHERE lot_id=? LIMIT 1";

        try (PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, lotId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static Map<String, Object> updateDeals(
            Long id, Long lotId, Long birdId, Long price, String status) {

        if (id == null) {
            return ApiResponse.error("ID сделки не может быть пустым");
        }

        String sql = "UPDATE deals SET lot_id=?,bird_id=?,price=?,status=? WHERE id=?";

        try (Connection c = Db.connection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, lotId);
            ps.setObject(2, birdId);
            ps.setObject(3, price);
            ps.setString(4, status);
            ps.setLong(5, id);

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                return ApiResponse.error("Сделка с ID " + id + " не найдена");
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", id);
            data.put("lot_id", lotId);
            data.put("bird_id", birdId);
            data.put("price", price);
            data.put("status", status);

            return ApiResponse.success("Сделка успешно обновлена", data);

        } catch (SQLException e) {
            return ApiResponse.error("Ошибка обновления сделки: " + e.getMessage());
        }
    }

    public static Map<String, Object> getDeals(Long id) {

        if (id == null) {
            return ApiResponse.error("ID сделки не может быть пустым");
        }

        String sql = "SELECT id,lot_id,bird_id,price,status FROM deals WHERE id=?";

        try (Connection c = Db.connection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return ApiResponse.error("Сделка не найдена");
                }

                Map<String, Object> data = new LinkedHashMap<>();

                for (String column : new String[]{
                        "id", "lot_id", "bird_id", "price", "status"
                }) {
                    data.put(column, rs.getObject(column));
                }

                return ApiResponse.success("Сделка получена", data);
            }

        } catch (SQLException e) {
            return ApiResponse.error("Ошибка получения данных сделки: " + e.getMessage());
        }
    }

    public static Map<String, Object> getDeals() {

        List<Map<String, Object>> out = new ArrayList<>();

        String sql = "SELECT id,lot_id,bird_id,price,status FROM deals";

        try (Connection c = Db.connection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {

            while (rs.next()) {

                Map<String, Object> data = new LinkedHashMap<>();

                for (String column : new String[]{
                        "id", "lot_id", "bird_id", "price", "status"
                }) {
                    data.put(column, rs.getObject(column));
                }

                out.add(data);
            }

            return ApiResponse.success("Данные сделок получены", out);

        } catch (SQLException e) {
            return ApiResponse.error("Ошибка получения данных сделок: " + e.getMessage());
        }
    }

    public static Map<String, Object> removeDeals(Long id) {

        if (id == null) {
            return ApiResponse.error("ID сделки не может быть пустым");
        }

        String sql = "DELETE FROM deals WHERE id=?";

        try (Connection c = Db.connection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            boolean result = ps.executeUpdate() > 0;

            if (result) {
                return ApiResponse.success("Сделка удалена", id + " Deleted");
            }

            return ApiResponse.error("Невозможно удалить сделку");

        } catch (SQLException e) {
            return ApiResponse.error("Ошибка удаления сделки: " + e.getMessage());
        }
    }
}
