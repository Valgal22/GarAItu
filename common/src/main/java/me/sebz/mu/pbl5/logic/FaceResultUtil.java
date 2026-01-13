package me.sebz.mu.pbl5.logic;

import java.util.List;
import java.util.Map;

public final class FaceResultUtil {
    private FaceResultUtil() {}

    public static String buildMessageFromResponse(Map<String, Object> response, double threshold) {
        if (response == null) return "Result: Unknown";

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("root");

        String name = "Unknown";
        String message = "Result: " + name;

        if (list != null && !list.isEmpty()) {
            Map<String, Object> top = list.get(0);
            if (top != null) {
                Double score = safeDouble(top.get("similarity"));
                if (score != null && score > threshold) {
                    Object n = top.get("name");
                    Object c = top.get("context");
                    String ns = n != null ? String.valueOf(n) : "Unknown";
                    String cs = c != null ? String.valueOf(c) : null;

                    if (cs != null && !cs.trim().isEmpty()) message = "This is " + ns + " (" + cs + ")";
                    else message = "This is " + ns;
                }
            }
        }

        return message;
    }

    private static Double safeDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Double) return (Double) v;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); }
        catch (Exception e) { return null; }
    }
}
