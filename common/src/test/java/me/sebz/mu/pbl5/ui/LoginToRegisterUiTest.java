package me.sebz.mu.pbl5.ui;

import com.codename1.testing.AbstractTest;

import me.sebz.mu.pbl5.MemoryLens;

public class LoginToRegisterUiTest extends AbstractTest {

    @Override
    public boolean runTest() throws Exception {
        MemoryLens.showLoginScreen();

        // Click en "Register" (botón del login)
        clickButtonByName("login_register");

        // Debe abrir el form "Register"
        waitForFormTitle("Register");
        return true;
    }
}
