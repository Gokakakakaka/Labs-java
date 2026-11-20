package com.example.demo.db;

import java.sql.*;
import java.util.*;

public class Lots {
    public static Map<String, Object> addLots(
            Long itemId, Long price, String step, String date, String status) {

        try {
            return addLot(Db.connection(), itemId, price, price, step, date, status);
        } catch (SQLException e) {
            return ApiResponse.error("Ошибка добавления лота: " + e.getMessage());
        }
    }

    static Map<String, Object> addLot(
            Connection c, long itemId, long price, long startPrice,
            String step, String date, String status) throws SQLException {

        String sql = "INSERT INTO lots(item_id,pprice,start_price,step,date,status) "
                + "VALUES(?,?,?,?,?,?)";

        try (PreparedStatement ps =
                     c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, itemId);
            ps.setLong(2, price);
            ps.setLong(3, startPrice);
            ps.setString(4, step);
            ps.setString(5, date);
            ps.setString(6, status);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {

                if (!keys.next()) {
                    return ApiResponse.error("Лот добавлен, но ID не получен");
                }

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("item_id", itemId);
                data.put("pprice", price);
                data.put("start_price", startPrice);
                data.put("step", step);
                data.put("date", date);
                data.put("status", status);

                return ApiResponse.created("Лот", keys.getLong(1), data);
            }
        }
    }

    static Map<String, Object> find(
            Connection c, long id, boolean lock) throws SQLException {

        String sql = "SELECT id,item_id,pprice,start_price,step,date,status "
                + "FROM lots WHERE id=?"
                + (lock ? " FOR UPDATE" : "");

        try (PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                Map<String, Object> data = new LinkedHashMap<>();

                for (String column : new String[]{
                        "id", "item_id", "pprice", "start_price",
                        "step", "date", "status"
                }) {
                    data.put(column, rs.getObject(column));
                }

                return data;
            }
        }
    }

    static void updatePrice(
            Connection c, long id, long price) throws SQLException {

        String sql = "UPDATE lots SET pprice=? WHERE id=?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, price);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    static void updateStatus(
            Connection c, long id, String status, long price) throws SQLException {

        String sql = "UPDATE lots SET status=?,pprice=? WHERE id=?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setLong(2, price);
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    static void updateStatus(
            Connection c, long id, String status) throws SQLException {

        String sql = "UPDATE lots SET status=? WHERE id=?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public static Map<String, Object> updateLots(
            Long id, Long itemId, Long price,
            String step, String date, String status) {

        if (id == null) {
            return ApiResponse.error("ID лота не может быть пустым");
        }

        String sql = "UPDATE lots SET item_id=?,pprice=?,step=?,date=?,status=? WHERE id=?";

        try (Connection c = Db.connection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, itemId);
            ps.setObject(2, price);
            ps.setString(3, step);
            ps.setString(4, date);
            ps.setString(5, status);
            ps.setLong(6, id);

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                return ApiResponse.error("Лот с ID " + id + " не найден");
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", id);
            data.put("item_id", itemId);
            data.put("pprice", price);
            data.put("step", step);
            data.put("date", date);
            data.put("status", status);

            return ApiResponse.success("Лот успешно обновлен", data);

        } catch (SQLException e) {
            return ApiResponse.error("Ошибка обновления лота: " + e.getMessage());
        }
    }

    public static Map<String, Object> getLots(Long id) {

        if (id == null) {
            return ApiResponse.error("ID лота не может быть пустым");
        }

        try (Connection c = Db.connection()) {

            Map<String, Object> data = find(c, id, false);

            if (data == null) {
                return ApiResponse.error("Лот не найден");
            }

            return ApiResponse.success("Лот получен", data);

        } catch (SQLException e) {
            return ApiResponse.error("Ошибка получения данных лота: " + e.getMessage());
        }
    }

    public static Map<String, Object> getLots() {

        List<Map<String, Object>> out = new ArrayList<>();

        String sql = "SELECT id,item_id,pprice,start_price,step,date,status FROM lots";

        try (Connection c = Db.connection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {

            while (rs.next()) {

                Map<String, Object> data = new LinkedHashMap<>();

                for (String column : new String[]{
                        "id", "item_id", "pprice", "start_price",
                        "step", "date", "status"
                }) {
                    data.put(column, rs.getObject(column));
                }

                out.add(data);
            }

            return ApiResponse.success("Данные лотов получены", out);

        } catch (SQLException e) {
            return ApiResponse.error("Ошибка получения данных лотов: " + e.getMessage());
        }
    }

    public static Map<String, Object> removeLots(Long id) {

        if (id == null) {
            return ApiResponse.error("ID лота не может быть пустым");
        }

        String sql = "DELETE FROM lots WHERE id=?";

        try (Connection c = Db.connection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            boolean result = ps.executeUpdate() > 0;

            if (result) {
                return ApiResponse.success("Лот удален", id + " Deleted");
            }

            return ApiResponse.error("Невозможно удалить лот");

        } catch (SQLException e) {
            return ApiResponse.error("Ошибка удаления лота: " + e.getMessage());
        }
    }
}
