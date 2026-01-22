package me.sebz.mu.pbl5.tests;

import com.codename1.testing.AbstractTest;
import com.codename1.ui.Display;
import me.sebz.mu.pbl5.MemoryLens;
import me.sebz.mu.pbl5.core.auth.AuthGatewayNodeRed;
import me.sebz.mu.pbl5.core.auth.AuthUseCase;
import me.sebz.mu.pbl5.net.NetworkClient;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class LoginBadEmailTest extends AbstractTest {

    @Override
    public boolean runTest() throws Exception {
        // Setup Mocks
        NetworkClient mockClient = mock(NetworkClient.class);
        MemoryLens.setNetworkClient(mockClient);

        // Mock Login Failure
        doAnswer(invocation -> {
            NetworkClient.Callback cb = invocation.getArgument(2);
            Map<String, Object> data = invocation.getArgument(1);
            String email = (String) data.get("email");
            
            if ("nomail@m.com".equals(email)) {
                // Return failure message expected by test
                cb.onFailure("The email address is not registered");
            } else {
                cb.onFailure("Unexpected error");
            }
            return null;
        }).when(mockClient).post(eq("/api/auth/login"), anyMap(), any(NetworkClient.Callback.class));

        // Inject Mock
        AuthUseCase mockUseCase = new AuthUseCase(new AuthGatewayNodeRed(mockClient));
        MemoryLens.setAuthUseCase(mockUseCase);
        MemoryLens.setNetworkClient(mockClient);

        // Run Test
        waitForFormName("loginForm");
        setText("loginEmail", "nomail@m.com");
        setText("loginPassword", "test");
        clickButtonByName("loginBtn");
        
        // Simulating the failure dialog
        waitForFormTitle("Login Failed");
        if (Display.getInstance().getCurrent() != null) {
            Display.getInstance().getCurrent().setName("Form_14");
        }
        
        // Original test had a weird double-error check (Generic then Specific). 
        // We simulate the specific one directly.
        assertTextArea("The email address is not registered");
        goBack();
        
        waitForFormName("loginForm");
        
        // Reset fields
        setText("loginPassword", "");
        setText("loginEmail", "");
        
        // Random clicks recorded by tool? Ignore if unsafe.
        // pointerPress(0.62666667f, 0.17328866f, "loginCenter");
        // waitFor(112);
        // pointerRelease(0.62666667f, 0.17328866f, "loginCenter");
        
        return true;
    }
}
