package me.sebz.mu.pbl5.tests;

import com.codename1.testing.AbstractTest;

import com.codename1.ui.Display;

public class JoinGroupMemberTest extends AbstractTest {
    public boolean runTest() throws Exception {
        waitForFormName("loginForm");
        setText("loginEmail", "member@m.com");
        setText("loginPassword", "member123");
        clickButtonByName("loginBtn");
        waitForFormName("groupOnboardingForm");
        setText("inviteCode", "28F276C7");
        clickButtonByName("joinGroupBtn");
        waitForFormTitle("Success");
        Display.getInstance().getCurrent().setName("Form_1");
        goBack();
        waitForFormName("groupOnboardingForm");
        waitForFormTitle("Loading...");
        Display.getInstance().getCurrent().setName("Form_2");
        com.codename1.ui.Display.getInstance().callSerially(() -> me.sebz.mu.pbl5.MemoryLens.logout());

        waitForFormName("loginForm");

        return true;
    }
}

