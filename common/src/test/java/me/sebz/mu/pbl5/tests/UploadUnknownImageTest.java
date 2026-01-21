package me.sebz.mu.pbl5.tests;

import com.codename1.testing.AbstractTest;

import com.codename1.ui.Display;

public class UploadUnknownImageTest extends AbstractTest {
    public boolean runTest() throws Exception {
        waitForFormName("loginForm");
        setText("loginEmail", "patient@p.com");
        setText("loginPassword", "patient123");
        clickButtonByName("loginBtn");
        waitForFormTitle("MemoryLens");
        Display.getInstance().getCurrent().setName("Form_1");
        clickButtonByLabel("Upload Image");
        waitForFormTitle("Result");
        Display.getInstance().getCurrent().setName("Form_2");
        goBack();
        waitForFormName("Form_1");
        com.codename1.ui.Display.getInstance().callSerially(() -> me.sebz.mu.pbl5.MemoryLens.logout());

        waitForFormName("loginForm");

        return true;
    }
}

