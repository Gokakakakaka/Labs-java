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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;


@Service
public class Users {
    
    private final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    public Users(JdbcTemplate jdbc, PasswordEncoder encoder) { this.jdbc = jdbc; this.encoder = encoder; }

    public boolean addUser(String name, String email, String password) {
        if (name == null || name.isBlank() || name.length() > 100){ System.out.println("Name error");return false;}
        if (email == null || !EMAIL.matcher(email).matches() || email.length() > 190) {System.out.println("Email error");return false;}
        if (password == null || password.length() < 8 || password.length() > 72
                || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*")
                || !password.matches(".*[0-9].*") || !password.matches(".*[^A-Za-z0-9].*")) {System.out.println("Pass error min 8 dig");return false;}
        try {
            jdbc.update("INSERT INTO users(name,email,password_hash,role,enabled,account_non_locked) VALUES(?,?,?,?,?,?)",
                name.trim(), email.trim().toLowerCase(), encoder.encode(password), "USER", true, true);
            return true;
        } catch (DuplicateKeyException e) {
            System.out.println("Error");
            return false;
        }
    }


public boolean updateUser(Long id, String username, String email, String password) {
    if (id == null) {
        return false;
    }

    String selectSql = "SELECT name, email, password_hash FROM users WHERE id = ?";
    String updateSql = "UPDATE users SET name = ?, email = ?, password_hash = ? WHERE id = ?";

    try {
        if (Database.connection == null || Database.connection.isClosed()) {
            return false;
        }

        String finalUsername = username;
        String finalEmail = email;
        String finalPassword = password;

        try (PreparedStatement statement = Database.connection.prepareStatement(selectSql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    System.out.println("User not found");
                    return false;
                }
                if (username == null) finalUsername = result.getString("name");
                if (email == null) finalEmail = result.getString("email");
                if (password == null) finalPassword = result.getString("password_hash");
            }
        }

        if (finalUsername == null || finalUsername.isBlank() || finalUsername.length() > 100) {
            System.out.println("Name error");
            return false;
        }
        if (finalEmail == null || !EMAIL.matcher(finalEmail).matches() || finalEmail.length() > 190) {
            System.out.println("Email error");
            return false;
        }
        
        if (password != null) {
            if (password.length() < 8 || password.length() > 72
                    || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*")
                    || !password.matches(".*[0-9].*") || !password.matches(".*[^A-Za-z0-9].*")) {
                System.out.println("Pass error min 8 dig");
                return false;
            }
            finalPassword = encoder.encode(password);
        }

        try (PreparedStatement updateStatement = Database.connection.prepareStatement(updateSql)) {
            updateStatement.setString(1, finalUsername.trim());
            updateStatement.setString(2, finalEmail.trim().toLowerCase());
            updateStatement.setString(3, finalPassword);
            updateStatement.setLong(4, id);

            int rowsUpdated = updateStatement.executeUpdate();
            return rowsUpdated > 0;
        }

    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
        return false;
    }
}

  public String getUser(Long id) {
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

  public List getUser() {

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

  public boolean removeUser(Long id){ 
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

