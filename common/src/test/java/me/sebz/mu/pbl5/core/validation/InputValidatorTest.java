package me.sebz.mu.pbl5.core.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {

    @Test
    void validateLogin_ok_when_email_and_password_present_and_email_valid() {
        ValidationResult r = InputValidator.validateLogin("a@b.com", "secret");
        assertTrue(r.isValid());
        assertNull(r.firstErrorOrNull());
        assertEquals(0, r.getErrors().size());
    }

    @Test
    void validateLogin_fails_when_email_missing() {
        ValidationResult r = InputValidator.validateLogin("   ", "secret");
        assertFalse(r.isValid());
        assertEquals("Email is required", r.firstErrorOrNull());
    }

    @Test
    void validateLogin_fails_when_password_missing() {
        ValidationResult r = InputValidator.validateLogin("a@b.com", "");
        assertFalse(r.isValid());
        assertEquals("Password is required", r.firstErrorOrNull());
    }

    @Test
    void validateLogin_fails_when_email_invalid() {
        ValidationResult r = InputValidator.validateLogin("invalid-email", "secret");
        assertFalse(r.isValid());
        assertEquals("Please enter a valid email address", r.firstErrorOrNull());
    }

    @Test
    void validateRegistration_ok_when_fields_valid() {
        ValidationResult r = InputValidator.validateRegistration("Maider", "a@b.com", "123456");
        assertTrue(r.isValid());
    }

    @Test
    void validateRegistration_fails_when_password_too_short() {
        ValidationResult r = InputValidator.validateRegistration("Maider", "a@b.com", "123");
        assertFalse(r.isValid());
        assertEquals("Password must be at least 6 characters", r.firstErrorOrNull());
    }

    @Test
    void validateRegistration_fails_when_name_missing() {
        ValidationResult r = InputValidator.validateRegistration(" ", "a@b.com", "123456");
        assertFalse(r.isValid());
        assertEquals("Name is required", r.firstErrorOrNull());
    }

    @Test
    void validateRegistration_fails_when_email_invalid() {
        ValidationResult r = InputValidator.validateRegistration("Maider", "nope", "123456");
        assertFalse(r.isValid());
        assertEquals("Please enter a valid email address", r.firstErrorOrNull());
    }
}
