package me.sebz.mu.pbl5.core.validation;

import java.util.ArrayList;
import java.util.List;

public final class InputValidator {

    private InputValidator() {
    }

    public static ValidationResult validateLogin(String email, String password) {
        List<String> errors = new ArrayList<>();
        requireNonBlank(errors, email, "Email is required");
        requireNonBlank(errors, password, "Password is required");

        if (email != null && !email.trim().isEmpty() && !isValidEmail(email)) {
            errors.add("Please enter a valid email address");
        }
        return ValidationResult.ofErrors(errors);
    }

    public static ValidationResult validateRegistration(String name, String email, String password) {
        List<String> errors = new ArrayList<>();
        requireNonBlank(errors, name, "Name is required");
        requireNonBlank(errors, email, "Email is required");
        requireNonBlank(errors, password, "Password is required");

        if (email != null && !email.trim().isEmpty() && !isValidEmail(email)) {
            errors.add("Please enter a valid email address");
        }

        if (password != null && !password.isEmpty() && password.length() < 6) {
            errors.add("Password must be at least 6 characters");
        }

        return ValidationResult.ofErrors(errors);
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String e = email.trim();
        int at = e.indexOf('@');
        int dot = e.lastIndexOf('.');
        return at > 0 && dot > at + 1 && dot < e.length() - 1;
    }

    private static void requireNonBlank(List<String> errors, String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            errors.add(message);
        }
    }
}
