package me.sebz.mu.pbl5.tests;

import com.codename1.testing.AbstractTest;

import com.codename1.ui.Display;

public class MemberUploadImageSuccessTest extends AbstractTest {
    public boolean runTest() throws Exception {
        waitForFormName("loginForm");
        setText("loginEmail", "member@m.com");
        setText("loginPassword", "member123");
        clickButtonByName("loginBtn");
        waitForFormTitle("Loading...");
        Display.getInstance().getCurrent().setName("Form_1");
        setText(new int[]{0, 2}, "member");
        setText(new int[]{0, 6}, "your friend");
        pointerPress(0.41830066f, 0.7777778f, new int[]{0, 5});
        waitFor(99);
        pointerRelease(0.41830066f, 0.7777778f, new int[]{0, 5});
        clickButtonByLabel("Take Photo");
        clickButtonByLabel("Upload Profile");
        waitForFormTitle("Success");
        Display.getInstance().getCurrent().setName("Form_2");
        goBack();
        waitForFormName("Form_1");
        com.codename1.ui.Display.getInstance().callSerially(() -> me.sebz.mu.pbl5.MemoryLens.logout());

        waitForFormName("loginForm");

        return true;
    }
}

