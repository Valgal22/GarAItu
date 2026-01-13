package me.sebz.mu.pbl5.utils;

import java.util.Map;

public class JsonUtil {

    private JsonUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Builds a simple JSON string from a Map<String, Object>.
     * Does not support nested maps or arrays intimately yet, mainly for simple
     * DTOs.
     * 
     * @param data The map containing key-value pairs.
     * @return A JSON formatted string.
     */
    public static String buildJson(Map<String, Object> data) {
        if (data == null) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int i = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (i > 0)
                sb.append(",");
            sb.append("\"").append(escape(entry.getKey())).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(escape((String) value)).append("\"");
            } else if (value == null) {
                sb.append("null");
            } else {
                sb.append(value);
            }
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        // Basic escaping
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
