package com.example.demo.db;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class Users {

    private static final Pattern EMAIL =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private static final PasswordEncoder encoder =
            new BCryptPasswordEncoder();

    static boolean checkCredentials(String name, String email, String password) {

        if (name == null || name.isBlank() || name.length() > 100) {
            return false;
        }

        if (email == null || !EMAIL.matcher(email).matches()
                || email.length() > 190) {
            return false;
        }

        if (password == null || password.length() < 8
                || password.length() > 72
                || !password.matches(".*[A-Z].*")
                || !password.matches(".*[a-z].*")
                || !password.matches(".*[0-9].*")
                || !password.matches(".*[^A-Za-z0-9].*")) {
            return false;
        }

        return true;
    }

    public static Map<String, Object> addUser(
            String name, String email, String password) {

        try {
            return addUser(Db.connection(), name, email, password);
        } catch (SQLException e) {
            return ApiResponse.error(
                    "Ошибка добавления пользователя: " + e.getMessage()
            );
        }
    }

    static Map<String, Object> addUser(
            Connection c, String name, String email, String password)
            throws SQLException {

        if (!checkCredentials(name, email, password)) {
            return ApiResponse.error("Ошибка ввода данных");
        }

        String sql = """
                INSERT INTO users
                (name, email, password_hash, role, enabled, account_non_locked)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps =
                     c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, encoder.encode(password));
            ps.setString(4, "USER");
            ps.setBoolean(5, true);
            ps.setBoolean(6, true);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {

                if (keys.next()) {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("name", name);
                    data.put("email", email);

                    return ApiResponse.created(
                            "Пользователь",
                            keys.getLong(1),
                            data
                    );
                }
            }
        }

        return ApiResponse.error("Не удалось получить ID пользователя");
    }

    public static boolean exists(Connection c, long id)
            throws SQLException {

        try (PreparedStatement ps =
                     c.prepareStatement("SELECT 1 FROM users WHERE id=?")) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static Map<String, Object> updateUser(
            Long id,
            String name,
            String email,
            String password,
            String role,
            boolean enabled,
            boolean anl) {

        if (id == null) {
            return ApiResponse.error("ID пользователя не может быть пустым");
        }

        if (!checkCredentials(name, email, password)) {
            return ApiResponse.error("Ошибка ввода данных");
        }

        String sql = """
                UPDATE users
                SET name = ?,
                    email = ?,
                    password_hash = ?,
                    role = ?,
                    enabled = ?,
                    account_non_locked = ?
                WHERE id = ?
                """;

        try (Connection c = Db.connection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, name.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, encoder.encode(password));
            ps.setString(4, role);
            ps.setBoolean(5, enabled);
            ps.setBoolean(6, anl);
            ps.setLong(7, id);

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                return ApiResponse.error(
                        "Пользователь с ID " + id + " не найден"
                );
            }

            Map<String, Object> data = new LinkedHashMap<>();

            data.put("id", id);
            data.put("name", name);
            data.put("email", email);
            data.put("role", role);
            data.put("enabled", enabled);
            data.put("account_non_locked", anl);

            return ApiResponse.success(
                    "Пользователь успешно обновлен", data
            );

        } catch (SQLException e) {
            return ApiResponse.error(
                    "Ошибка обновления пользователя: " + e.getMessage()
            );
        }
    }

    public static Map<String, Object> getUser(Long id) {

        try (Connection c = Db.connection();
             PreparedStatement ps =
                     c.prepareStatement(
                             "SELECT id, name, email FROM users WHERE id=?")) {

            ps.setLong(1, id);

            try (ResultSet r = ps.executeQuery()) {

                if (r.next()) {
                    Map<String, Object> data = new LinkedHashMap<>();

                    data.put("id", r.getLong("id"));
                    data.put("name", r.getString("name"));
                    data.put("email", r.getString("email"));

                    return ApiResponse.success(
                            "Пользователь получен", data
                    );
                }

                return ApiResponse.error("Пользователя не существует");
            }

        } catch (SQLException e) {
            return ApiResponse.error(
                    "Ошибка получения данных пользователя: " + e.getMessage()
            );
        }
    }

    public static Map<String, Object> getUser() {

        List<Map<String, Object>> out = new ArrayList<>();

        try (Connection c = Db.connection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(
                     "SELECT id, name, email FROM users")) {

            while (r.next()) {

                Map<String, Object> user = new LinkedHashMap<>();

                user.put("id", r.getLong("id"));
                user.put("name", r.getString("name"));
                user.put("email", r.getString("email"));

                out.add(user);
            }

        } catch (SQLException e) {
            return ApiResponse.error(
                    "Ошибка получения данных пользователей: " + e.getMessage()
            );
        }

        return ApiResponse.success(
                "Данные пользователей получены", out
        );
    }

    public static Map<String, Object> removeUser(Long id) {

        try (Connection c = Db.connection();
             PreparedStatement ps =
                     c.prepareStatement("DELETE FROM users WHERE id=?")) {

            ps.setLong(1, id);

            boolean res = ps.executeUpdate() > 0;

            if (res) {
                return ApiResponse.success(
                        "Пользователь удален", id + " Deleted"
                );
            }

            return ApiResponse.error("Невозможно удалить пользователя");

        } catch (SQLException e) {
            return ApiResponse.error(
                    "Ошибка удаления пользователя: " + e.getMessage()
            );
        }
    }
}

