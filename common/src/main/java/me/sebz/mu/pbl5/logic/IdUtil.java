package me.sebz.mu.pbl5.logic;

public final class IdUtil {
    private IdUtil() {}

    public static boolean areIdsEqual(String id1, String id2) {
        if (id1 == null || id2 == null) return false;
        try {
            double d1 = Double.parseDouble(id1);
            double d2 = Double.parseDouble(id2);
            return Math.abs(d1 - d2) < 0.001;
        } catch (NumberFormatException e) {
            return id1.equals(id2);
        }
    }

    public static String toSafeIdString(Object idObj) {
        if (idObj == null) return null;
        if (idObj instanceof Number) return String.valueOf(((Number) idObj).longValue());
        return String.valueOf(idObj);
    }
}
