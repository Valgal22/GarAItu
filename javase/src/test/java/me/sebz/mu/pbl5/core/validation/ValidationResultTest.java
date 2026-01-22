package me.sebz.mu.pbl5.core.validation;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {

    @Test
    void ok_is_valid_and_has_no_errors() {
        ValidationResult r = ValidationResult.ok();
        assertTrue(r.isValid());
        assertTrue(r.getErrors().isEmpty());
        assertNull(r.firstErrorOrNull());
    }

    @Test
    void ofErrors_makes_invalid_and_returns_first_error() {
        ValidationResult r = ValidationResult.ofErrors(Arrays.asList("e1", "e2"));
        assertFalse(r.isValid());
        assertEquals("e1", r.firstErrorOrNull());
        assertEquals(2, r.getErrors().size());
    }
}
