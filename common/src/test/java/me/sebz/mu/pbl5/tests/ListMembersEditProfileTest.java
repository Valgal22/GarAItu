package me.sebz.mu.pbl5.tests;

import com.codename1.testing.AbstractTest;

import com.codename1.ui.Display;

public class ListMembersEditProfileTest extends AbstractTest {
    public boolean runTest() throws Exception {
        waitForFormName("loginForm");
        setText("loginEmail", "member@m.com");
        setText("loginPassword", "member123");
        clickButtonByName("loginBtn");
        waitForFormName("Form_6");
        clickButtonByLabel("Refresh");
        clickButtonByLabel("Edit My Profile");
        waitForFormName("Form_7");
        setText(new int[]{1}, "member");
        setText(new int[]{3}, "your brother");
        pointerPress(0.456382f, 0.86419755f, new int[]{2});
        waitFor(83);
        pointerRelease(0.456382f, 0.86419755f, new int[]{2});
        clickButtonByLabel("Save Changes");
        waitForFormName("Form_8");
        goBack();
        waitForFormName("Form_7");
        waitForFormName("Form_6");
        com.codename1.ui.Display.getInstance().callSerially(() -> me.sebz.mu.pbl5.MemoryLens.logout());

        waitForFormName("loginForm");

        return true;
    }
}

