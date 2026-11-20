package com.example.demo.db;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ApiResponse {
    private ApiResponse() {}

    public static Map<String, Object> success(String message, Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    public static Map<String, Object> error(String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("data", null);
        return response;
    }

    static Map<String, Object> created(String type, long id, Map<String, Object> fields) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.putAll(fields);
        return success(type + " успешно добавлен", data);
    }
}
