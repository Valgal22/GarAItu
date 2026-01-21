package me.sebz.mu.pbl5.tests;

import com.codename1.testing.AbstractTest;

import com.codename1.ui.Display;

public class CreateMemberSuccessTest extends AbstractTest {
    public boolean runTest() throws Exception {
        waitForFormName("loginForm");
        clickButtonByName("goRegisterBtn");
        waitForFormName("registerForm");
        setText("registerName", "member");
        setText("registerEmail", "member@m.com");
        setText("registerPassword", "member123");
        waitForUnnamedForm();
        Display.getInstance().getCurrent().setName("Form_1");
        selectInList(new int[]{0, 8}, 2);
        waitForFormName("registerForm");
        clickButtonByName("registerBtn");
        waitForFormTitle("Success");
        Display.getInstance().getCurrent().setName("Form_2");
        goBack();
        waitForFormName("registerForm");
        waitForFormName("loginForm");
        com.codename1.ui.Display.getInstance().callSerially(() -> me.sebz.mu.pbl5.MemoryLens.logout());

        waitForFormName("loginForm");

        return true;
    }
}

