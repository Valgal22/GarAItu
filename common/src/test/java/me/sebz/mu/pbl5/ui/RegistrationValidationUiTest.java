package me.sebz.mu.pbl5.ui;

import com.codename1.testing.AbstractTest;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;

import me.sebz.mu.pbl5.RegistrationForm;
import me.sebz.mu.pbl5.services.AuthService;
import me.sebz.mu.pbl5.services.FakeNetworkClient;

public class RegistrationValidationUiTest extends AbstractTest {

    @Override
    public boolean runTest() throws Exception {
        // Red fake: no saldrá a Node-RED
        AuthService auth = new AuthService(new FakeNetworkClient());
        new RegistrationForm(auth).show();

        // 1) Submit vacío => Dialog "Error" con mensaje de validación
        clickButtonByName("reg_submit");
        waitForFormTitle("Error");
        assertDialogBodyContains("Registration failed: Name, Email and Password are required");
        closeDialogIfOpen();

        // 2) Email inválido
        setText("reg_name", "Test User");
        setText("reg_email", "no-es-email");
        setText("reg_password", "123456");
        clickButtonByName("reg_submit");

        waitForFormTitle("Error");
        assertDialogBodyContains("Registration failed: Please enter a valid email address");
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
