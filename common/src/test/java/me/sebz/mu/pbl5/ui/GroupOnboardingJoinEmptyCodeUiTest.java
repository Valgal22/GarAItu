package me.sebz.mu.pbl5.ui;

import com.codename1.testing.AbstractTest;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;

import me.sebz.mu.pbl5.GroupOnboardingForm;
import me.sebz.mu.pbl5.services.FakeNetworkClient;
import me.sebz.mu.pbl5.services.GroupService;

public class GroupOnboardingJoinEmptyCodeUiTest extends AbstractTest {

    @Override
    public boolean runTest() throws Exception {
        GroupService groupService = new GroupService(new FakeNetworkClient());
        new GroupOnboardingForm(groupService).show();

        // Join sin código => "Error" + "Could not join: Please enter an invite code"
        clickButtonByName("group_join");

        waitForFormTitle("Error");
        assertDialogBodyContains("Could not join: Please enter an invite code");
        closeDialogIfOpen();

        return true;
    }

    private void assertDialogBodyContains(String expected) {
        if (!(Display.getInstance().getCurrent() instanceof Dialog)) {
            fail("Expected Dialog, but current is " + Display.getInstance().getCurrent());
        }
        Dialog d = (Dialog) Display.getInstance().getCurrent();
        String body = d.getContentPane().toString();
        assertBool(body != null && body.contains(expected), "Dialog body should contain: " + expected);
    }

    private void closeDialogIfOpen() {
        if (Display.getInstance().getCurrent() instanceof Dialog) {
            ((Dialog) Display.getInstance().getCurrent()).dispose();
        }
    }
}
