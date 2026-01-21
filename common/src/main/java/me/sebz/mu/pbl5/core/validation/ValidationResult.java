package me.sebz.mu.pbl5.core.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple validation result that is JUnit-friendly.
 */
public final class ValidationResult {
    private final List<String> errors;

    private ValidationResult(List<String> errors) {
        this.errors = errors;
    }

    public static ValidationResult ok() {
        return new ValidationResult(Collections.emptyList());
    }

    public static ValidationResult ofErrors(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return ok();
        }
        return new ValidationResult(Collections.unmodifiableList(new ArrayList<>(errors)));
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    public String firstErrorOrNull() {
        return errors.isEmpty() ? null : errors.get(0);
    }
}
