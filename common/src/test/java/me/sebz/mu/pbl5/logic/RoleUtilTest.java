package me.sebz.mu.pbl5.logic;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

public class RoleUtilTest {

    @Test
    public void roleName_knownRoles() {
        assertEquals("Admin", RoleUtil.roleName("0"));
        assertEquals("Patient", RoleUtil.roleName("1"));
        assertEquals("Family Member", RoleUtil.roleName("2"));
    }

    @Test
    public void roleName_knownRoles_decimalVariants() {
        assertEquals("Admin", RoleUtil.roleName("0.0"));
        assertEquals("Patient", RoleUtil.roleName("1.0"));
        assertEquals("Family Member", RoleUtil.roleName("2.0"));
    }

    @Test
    public void roleName_unknownOrNull() {
        assertEquals("Unknown", RoleUtil.roleName("9"));
        assertEquals("Unknown", RoleUtil.roleName(null));
    }

    @Test
    public void isAdmin_variants() {
        assertTrue(RoleUtil.isAdmin("0"));        // startsWith("0")
        assertTrue(RoleUtil.isAdmin("0.0"));      // startsWith("0") también
        assertTrue(RoleUtil.isAdmin("ADMIN"));    // equalsIgnoreCase
        assertFalse(RoleUtil.isAdmin("1"));
        assertFalse(RoleUtil.isAdmin(null));
    }

    @Test
    public void isPatient_variants() {
        assertTrue(RoleUtil.isPatient("1"));        // startsWith("1")
        assertTrue(RoleUtil.isPatient("1.0"));      // startsWith("1") también
        assertTrue(RoleUtil.isPatient("PATIENT"));  // equalsIgnoreCase
        assertFalse(RoleUtil.isPatient("0"));
        assertFalse(RoleUtil.isPatient(null));
    }

    @Test
    public void roleUtil_privateConstructor_covered() throws Exception {
        Constructor<RoleUtil> c = RoleUtil.class.getDeclaredConstructor();
        c.setAccessible(true);
        assertNotNull(c.newInstance());
    }
}
