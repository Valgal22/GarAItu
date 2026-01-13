package me.sebz.mu.pbl5.logic;

public final class RoleUtil {
    private RoleUtil() {}

    public static boolean isAdmin(String role) {
        if (role == null) return false;
        return role.startsWith("0") || "ADMIN".equalsIgnoreCase(role);
    }

    public static boolean isPatient(String role) {
        if (role == null) return false;
        return role.startsWith("1") || "PATIENT".equalsIgnoreCase(role);
    }

    public static String roleName(Object roleObj) {
        String r = String.valueOf(roleObj);
        if ("0".equals(r) || "0.0".equals(r)) return "Admin";
        if ("1".equals(r) || "1.0".equals(r)) return "Patient";
        if ("2".equals(r) || "2.0".equals(r)) return "Family Member";
        return "Unknown";
    }
}
