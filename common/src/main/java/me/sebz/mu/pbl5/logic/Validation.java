package me.sebz.mu.pbl5.logic;

public final class Validation {
    private Validation() {}

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (isBlank(email)) return false;
        // Suficiente para validación de formulario (no regex “perfecta”)
        return email.contains("@") && email.contains(".");
    }
}
