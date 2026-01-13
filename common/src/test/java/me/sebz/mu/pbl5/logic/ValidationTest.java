package me.sebz.mu.pbl5.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationTest {

    @Test
    public void isBlank_nullEmptySpaces_true() {
        assertTrue(Validation.isBlank(null));
        assertTrue(Validation.isBlank(""));
        assertTrue(Validation.isBlank("   "));
    }

    @Test
    public void isBlank_text_false() {
        assertFalse(Validation.isBlank("a"));
        assertFalse(Validation.isBlank("  a  "));
    }

    @Test
    public void isValidEmail_basicChecks() {
        assertFalse(Validation.isValidEmail(null));
        assertFalse(Validation.isValidEmail(""));
        assertFalse(Validation.isValidEmail("abc"));
        assertFalse(Validation.isValidEmail("abc@"));
        assertFalse(Validation.isValidEmail("@abc"));
        assertTrue(Validation.isValidEmail("a@b.com"));
        assertTrue(Validation.isValidEmail("name.surname@mail.es"));
    }
}
