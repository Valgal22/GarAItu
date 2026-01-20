package me.sebz.mu.pbl5.util;

public final class MemberRoleUtil {

    private MemberRoleUtil() {
        // Utility class
    }

    public static String roleNameFrom(Object roleObj) {
        String rStr = String.valueOf(roleObj);

        if ("0.0".equals(rStr) || "0".equals(rStr)) {
            return "Admin";
        }
        if ("1.0".equals(rStr) || "1".equals(rStr)) {
            return "Patient";
        }
        if ("2.0".equals(rStr) || "2".equals(rStr)) {
            return "Family Member";
        }
        return "Unknown";
    }
}
