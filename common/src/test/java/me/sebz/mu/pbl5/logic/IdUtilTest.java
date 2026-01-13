package me.sebz.mu.pbl5.logic;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

public class IdUtilTest {

    @Test
    public void areIdsEqual_numericStrings_true() {
        assertTrue(IdUtil.areIdsEqual("1", "1"));
        assertTrue(IdUtil.areIdsEqual("1.0", "1"));
        assertTrue(IdUtil.areIdsEqual("2", "2.000"));
    }

    @Test
    public void areIdsEqual_different_false() {
        assertFalse(IdUtil.areIdsEqual("1", "2"));
        assertFalse(IdUtil.areIdsEqual(null, "1"));
        assertFalse(IdUtil.areIdsEqual("1", null));
    }

    @Test
    public void areIdsEqual_nonNumeric_triggersCatch_andUsesStringEquals() {
        // fuerza NumberFormatException -> entra en catch
        assertTrue(IdUtil.areIdsEqual("abc", "abc"));
        assertFalse(IdUtil.areIdsEqual("abc", "abd"));

        // mezcla: uno no numérico, otro numérico -> también cae en catch
        assertFalse(IdUtil.areIdsEqual("abc", "1"));
    }

    @Test
    public void toSafeIdString_number_toLongString() {
        assertEquals("5", IdUtil.toSafeIdString(5.0));
        assertEquals("7", IdUtil.toSafeIdString(7));
    }

    @Test
    public void toSafeIdString_string_kept() {
        assertEquals("abc", IdUtil.toSafeIdString("abc"));
        assertNull(IdUtil.toSafeIdString(null));
    }

    @Test
    public void idUtil_privateConstructor_covered() throws Exception {
        Constructor<IdUtil> c = IdUtil.class.getDeclaredConstructor();
        c.setAccessible(true);
        assertNotNull(c.newInstance());
    }
}
