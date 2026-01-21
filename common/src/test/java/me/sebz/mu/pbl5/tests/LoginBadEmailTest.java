package me.sebz.mu.pbl5.tests;

import com.codename1.testing.AbstractTest;

import com.codename1.ui.Display;

public class LoginBadEmailTest extends AbstractTest {
    public boolean runTest() throws Exception {
        setText("loginPassword", "");
        waitForFormName("loginForm");
        setText("loginEmail", "adminnn@a.com");
        setText("loginPassword", "admin");
        clickButtonByName("loginBtn");
        waitForFormTitle("Connection Error");
        Display.getInstance().getCurrent().setName("Form_1");
        goBack();
        waitForFormName("loginForm");
        waitForFormTitle("Login Failed");
        Display.getInstance().getCurrent().setName("Form_2");
        goBack();
        waitForFormName("loginForm");
        com.codename1.ui.Display.getInstance().callSerially(() -> me.sebz.mu.pbl5.MemoryLens.logout());

        waitForFormName("loginForm");

        return true;
    }
}

