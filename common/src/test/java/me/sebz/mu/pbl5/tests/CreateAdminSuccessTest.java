package me.sebz.mu.pbl5.tests;

import com.codename1.testing.AbstractTest;

import com.codename1.ui.Display;

public class CreateAdminSuccessTest extends AbstractTest {
    public boolean runTest() throws Exception {
        waitForFormName("loginForm");
        clickButtonByName("goRegisterBtn");
        waitForFormName("registerForm");
        setText("registerName", "admin");
        setText("registerEmail", "admin@a.com");
        setText("registerPassword", "admin123");
        setText("registerChatId", "1414134108");
        clickButtonByName("registerBtn");
        waitForFormTitle("Success");
        Display.getInstance().getCurrent().setName("Form_1");
        goBack();
        waitForFormName("registerForm");
        waitForFormName("loginForm");
        com.codename1.ui.Display.getInstance().callSerially(() -> me.sebz.mu.pbl5.MemoryLens.logout());

        waitForFormName("loginForm");

        return true;
    }
}

