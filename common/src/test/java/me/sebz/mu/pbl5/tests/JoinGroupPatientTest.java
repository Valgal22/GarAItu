package me.sebz.mu.pbl5.tests;

import com.codename1.testing.AbstractTest;

import com.codename1.ui.Display;

public class JoinGroupPatientTest extends AbstractTest {
    public boolean runTest() throws Exception {
        waitForFormName("loginForm");
        setText("loginEmail", "patient");
        setText("loginEmail", "patient@p.com");
        setText("loginPassword", "patient123");
        clickButtonByName("loginBtn");
        waitForFormName("groupOnboardingForm");
        setText("inviteCode", "28F276C7");
        clickButtonByName("joinGroupBtn");
        waitForFormTitle("Success");
        Display.getInstance().getCurrent().setName("Form_1");
        goBack();
        waitForFormName("groupOnboardingForm");
        waitForFormTitle("MemoryLens");
        Display.getInstance().getCurrent().setName("Form_2");
        com.codename1.ui.Display.getInstance().callSerially(() -> me.sebz.mu.pbl5.MemoryLens.logout());

        waitForFormName("loginForm");

        return true;
    }
}

