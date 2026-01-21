package me.sebz.mu.pbl5.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemberRoleUtilTest {

    @Test
    void roleNameFrom_admin_variants() {
        assertEquals("Admin", MemberRoleUtil.roleNameFrom(0));
        assertEquals("Admin", MemberRoleUtil.roleNameFrom(0.0));
        assertEquals("Admin", MemberRoleUtil.roleNameFrom("0"));
        assertEquals("Admin", MemberRoleUtil.roleNameFrom("0.0"));
    }

    @Test
    void roleNameFrom_patient_variants() {
        assertEquals("Patient", MemberRoleUtil.roleNameFrom(1));
        assertEquals("Patient", MemberRoleUtil.roleNameFrom(1.0));
        assertEquals("Patient", MemberRoleUtil.roleNameFrom("1"));
        assertEquals("Patient", MemberRoleUtil.roleNameFrom("1.0"));
    }

    @Test
    void roleNameFrom_family_variants() {
        assertEquals("Family Member", MemberRoleUtil.roleNameFrom(2));
        assertEquals("Family Member", MemberRoleUtil.roleNameFrom(2.0));
        assertEquals("Family Member", MemberRoleUtil.roleNameFrom("2"));
        assertEquals("Family Member", MemberRoleUtil.roleNameFrom("2.0"));
    }

    @Test
    void roleNameFrom_unknown_for_other_values() {
        assertEquals("Unknown", MemberRoleUtil.roleNameFrom(99));
        assertEquals("Unknown", MemberRoleUtil.roleNameFrom("X"));
        assertEquals("Unknown", MemberRoleUtil.roleNameFrom(null)); // String.valueOf(null) = "null"
    }
}
