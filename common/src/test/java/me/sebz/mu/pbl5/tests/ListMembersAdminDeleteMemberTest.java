package me.sebz.mu.pbl5.tests;

import com.codename1.testing.AbstractTest;

import com.codename1.ui.Display;

public class ListMembersAdminDeleteMemberTest extends AbstractTest {
    public boolean runTest() throws Exception {
        waitForFormName("loginForm");
        setText("loginEmail", "admin@a.com");
        setText("loginPassword", "admin123");
        clickButtonByName("loginBtn");
        waitForFormTitle("Admin Dashboard");
        Display.getInstance().getCurrent().setName("Form_1");
        clickButtonByPath(new int[]{1, 1, 1});
        waitForFormTitle("Confirm");
        Display.getInstance().getCurrent().setName("Form_2");
        clickButtonByLabel("Yes");
        waitForFormName("Form_1");
        clickButtonByLabel("Refresh List");
        com.codename1.ui.Display.getInstance().callSerially(() -> me.sebz.mu.pbl5.MemoryLens.logout());

        waitForFormName("loginForm");

        return true;
    }
}

