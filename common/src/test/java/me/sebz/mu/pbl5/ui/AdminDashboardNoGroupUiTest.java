package me.sebz.mu.pbl5.ui;

import com.codename1.testing.AbstractTest;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;

import me.sebz.mu.pbl5.AdminDashboard;
import me.sebz.mu.pbl5.MemoryLens;

public class AdminDashboardNoGroupUiTest extends AbstractTest {

    @Override
    public boolean runTest() throws Exception {
        // Forzamos groupId null para que NO haga llamadas de red y muestre el error determinista
        MemoryLens.setFamilyGroupId(null);

        new AdminDashboard(); // el constructor hace show() y fetchMembers()

        waitForFormTitle("Error");
        assertDialogBodyContains("No Group ID. Login again.");
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
