package me.sebz.mu.pbl5.tests;

import com.codename1.testing.AbstractTest;

import com.codename1.ui.Display;

public class UploadMemberImageErrorTest extends AbstractTest {
    public boolean runTest() throws Exception {
        waitForFormName("loginForm");
        setText("loginEmail", "member@m.com");
        setText("loginPassword", "member123");
        clickButtonByName("loginBtn");
        waitForFormName("Form_1");
        setText(new int[]{0, 2}, "fe");
        setText(new int[]{0, 6}, "fe");
        pointerPress(0.28851542f, 0.5925926f, new int[]{0, 5});
        waitFor(78);
        pointerRelease(0.28851542f, 0.5925926f, new int[]{0, 5});
        clickButtonByLabel("Take Photo");
        clickButtonByLabel("Upload Profile");
        waitForFormName("Form_2");
        goBack();
        waitForFormName("Form_1");
        waitForFormName("Form_3");
        goBack();
        waitForFormName("Form_1");
        com.codename1.ui.Display.getInstance().callSerially(() -> me.sebz.mu.pbl5.MemoryLens.logout());

        waitForFormName("loginForm");

        return true;
    }
}

