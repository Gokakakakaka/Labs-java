package com.example.demo.db;

import java.sql.*;
import java.util.*;

public class Item {

    public static Map<String, Object> addItem(
            Long userId, String about, String cndn) {

        try {
            return addItem(Db.connection(), userId, about, cndn);
        } catch (SQLException e) {
            return ApiResponse.error("Ошибка добавления товара: " + e.getMessage());
        }
    }

    static Map<String, Object> addItem(
            Connection c, long userId, String about, String cndn)
            throws SQLException {

        String sql = "INSERT INTO items(user_id,about,cndn) VALUES(?,?,?)";

        try (PreparedStatement ps =
                     c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, userId);
            ps.setString(2, about);
            ps.setString(3, cndn);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {

                if (!keys.next()) {
                    return ApiResponse.error("Товар добавлен, но ID не получен");
                }

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("user_id", userId);
                data.put("about", about);
                data.put("cndn", cndn);

                return ApiResponse.created("Товар", keys.getLong(1), data);
            }
        }
    }

    public static Map<String, Object> getItem(Long id) {

        if (id == null) {
            return ApiResponse.error("ID товара не может быть пустым");
        }

        String sql = "SELECT id,user_id,about,cndn FROM items WHERE id=?";

        try (Connection c = Db.connection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return ApiResponse.error("Товар не найден");
                }

                Map<String, Object> data = new LinkedHashMap<>();

                data.put("id", rs.getLong("id"));
                data.put("user_id", rs.getLong("user_id"));
                data.put("about", rs.getString("about"));
                data.put("cndn", rs.getString("cndn"));

                return ApiResponse.success("Товар получен", data);
            }

        } catch (SQLException e) {
            return ApiResponse.error("Ошибка получения данных товара: " + e.getMessage());
        }
    }

    public static Map<String, Object> getItem() {

        List<Map<String, Object>> out = new ArrayList<>();

        String sql = "SELECT id,user_id,about,cndn FROM items";

        try (Connection c = Db.connection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {

            while (rs.next()) {

                Map<String, Object> data = new LinkedHashMap<>();

                data.put("id", rs.getLong("id"));
                data.put("user_id", rs.getLong("user_id"));
                data.put("about", rs.getString("about"));
                data.put("cndn", rs.getString("cndn"));

                out.add(data);
            }

            return ApiResponse.success("Данные товаров получены", out);

        } catch (SQLException e) {
            return ApiResponse.error("Ошибка получения данных товаров: " + e.getMessage());
        }
    }

    public static Map<String, Object> updateItem(
            Long id, Long userId, String about, String cndn) {

        if (id == null) {
            return ApiResponse.error("ID товара не может быть пустым");
        }

        String sql = "UPDATE items SET user_id=?,about=?,cndn=? WHERE id=?";

        try (Connection c = Db.connection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, userId);
            ps.setString(2, about);
            ps.setString(3, cndn);
            ps.setLong(4, id);

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                return ApiResponse.error("Товар с ID " + id + " не найден");
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", id);
            data.put("user_id", userId);
            data.put("about", about);
            data.put("cndn", cndn);

            return ApiResponse.success("Товар успешно обновлен", data);

        } catch (SQLException e) {
            return ApiResponse.error("Ошибка обновления товара: " + e.getMessage());
        }
    }

    public static Map<String, Object> removeItem(Long id) {

        if (id == null) {
            return ApiResponse.error("ID товара не может быть пустым");
        }

        String sql = "DELETE FROM items WHERE id=?";

        try (Connection c = Db.connection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            boolean result = ps.executeUpdate() > 0;

            if (result) {
                return ApiResponse.success("Товар удален", id + " Deleted");
            }

            return ApiResponse.error("Невозможно удалить товар");

        } catch (SQLException e) {
            return ApiResponse.error("Ошибка удаления товара: " + e.getMessage());
        }
    }
}
