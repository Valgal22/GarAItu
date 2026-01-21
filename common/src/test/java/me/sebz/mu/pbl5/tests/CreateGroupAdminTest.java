package me.sebz.mu.pbl5.tests;

import com.codename1.testing.AbstractTest;

import com.codename1.ui.Display;

public class CreateGroupAdminTest extends AbstractTest {
    public boolean runTest() throws Exception {
        waitForFormName("loginForm");
        setText("loginEmail", "admin@a.com");
        setText("loginPassword", "admin123");
        clickButtonByName("loginBtn");
        waitForFormName("groupOnboardingForm");
        clickButtonByName("createGroupBtn");
        waitForFormTitle("Create Group");
        Display.getInstance().getCurrent().setName("Form_1");
        setText("createGroupName", "test");
        goBack();
        waitForFormName("groupOnboardingForm");
        waitForFormTitle("Success");
        Display.getInstance().getCurrent().setName("Form_2");
        goBack();
        waitForFormName("groupOnboardingForm");
        waitForFormTitle("Admin Dashboard");
        Display.getInstance().getCurrent().setName("Form_3");
        clickButtonByLabel("Generate Invite Code");
        waitForFormTitle("Invite Code");
        Display.getInstance().getCurrent().setName("Form_4");
        goBack();
        waitForFormName("Form_3");
        com.codename1.ui.Display.getInstance().callSerially(() -> me.sebz.mu.pbl5.MemoryLens.logout());

        waitForFormName("loginForm");

        return true;
    }
}

