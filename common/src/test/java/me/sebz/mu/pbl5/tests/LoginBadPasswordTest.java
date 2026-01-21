package me.sebz.mu.pbl5.tests;

import com.codename1.testing.AbstractTest;

import com.codename1.ui.Display;

public class LoginBadPasswordTest extends AbstractTest {
    public boolean runTest() throws Exception {
        setText("loginPassword", "");
        setText("loginEmail", "");
        waitForFormName("loginForm");
        setText("loginEmail", "admin@a.com");
        setText("loginPassword", "ibgsiughw");
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

