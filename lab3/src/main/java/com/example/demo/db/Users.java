package com.example.demo.db;

import java.sql.*;
import java.util.*;

public class Users {
    public static Map<String,Object> addUser(String name, String email) {
        try { return addUser(Db.connection(), name, email); }
        catch (SQLException e) { return ApiResponse.error("Ошибка добавления пользователя: " + e.getMessage()); }
    }
    static Map<String,Object> addUser(Connection c, String name, String email) throws SQLException {
        String sql = "INSERT INTO users(name,email) VALUES(?,?)";
        try (PreparedStatement ps=c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,name); 
            ps.setString(2,email);
            ps.executeUpdate();
            try (ResultSet keys=ps.getGeneratedKeys()) {
                Map<String,Object> data=new LinkedHashMap<>(); 
                data.put("name",name); data.put("email",email);
                return ApiResponse.created("Пользователь",keys.getLong(1),data);
            }
        }
    }
    public static boolean exists(Connection c,long id) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("SELECT 1 FROM users WHERE id=?")) {
            ps.setLong(1,id); try(ResultSet rs=ps.executeQuery()){return rs.next();}
        }
    }
    
public static Map<String, Object> updateUser(Long id, String name, String email) {
    if (id == null) {
        return ApiResponse.error("ID пользователя не может быть пустым");
    }

    String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";
    
    try (Connection c = Db.connection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        
        ps.setString(1, name);
        ps.setString(2, email);
        ps.setLong(3, id);
        
        int affectedRows = ps.executeUpdate();
        
        if (affectedRows == 0) {
            return ApiResponse.error("Пользователь с ID " + id + " не найден");
        }
        
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("name", name);
        data.put("email", email);
        
        return ApiResponse.success("Пользователь успешно обновлен", data);
        
    } catch (SQLException e) {
        return ApiResponse.error("Ошибка обновления пользователя: " + e.getMessage());
    }
}
    public static Map<String, Object> getUser(Long id) {
        try(Connection c=Db.connection(); 
            PreparedStatement ps=c.prepareStatement("SELECT id,name,email FROM users WHERE id=?")) 
        {
            ps.setLong(1,id); try(ResultSet r=ps.executeQuery()){
              String res = r.next()?String.format("{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\"}",
                  r.getLong("id"),r.getString("name"),r.getString("email")):null;
              if(res != null)return ApiResponse.success("Пользователь получен", res);
              return ApiResponse.error("Пользователя не существует");
            }
        } catch(SQLException e){return ApiResponse.error("Ошибка получения данных пользователя: " + e.getMessage());}
    }
    public static Map<String, Object> getUser() {
        List<String> out=new ArrayList<>();
        try(Connection c=Db.connection(); 
            Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT id,name,email FROM users")) 
        {
            while(r.next()) out.add(String.format("{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\"}",
                  r.getLong("id"),r.getString("name"),r.getString("email")));
        } catch(SQLException e){return ApiResponse.error("Ошибка получения данных пользователей" + e.getMessage());} 
        return ApiResponse.success("Данные пользователей полученны",out);
    }
    public static Map<String, Object> removeUser(Long id) {
        try(Connection c=Db.connection(); 
            PreparedStatement ps=c.prepareStatement("DELETE FROM users WHERE id=?")) 
        {
          ps.setLong(1,id);
          boolean res = ps.executeUpdate()>0;
          if(res)return ApiResponse.success("Пользователь удален",id + " Deleted");
          return ApiResponse.error("Невозвможно удалить пользователя");
        }
        catch(SQLException e){return ApiResponse.error("Ошибка удаления пользователя: " + e.getMessage());}
    }
}
