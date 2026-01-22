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

public class CreateUserErrorTest extends AbstractTest {

    @Override
    public boolean runTest() throws Exception {
         // Setup Mocks
        NetworkClient mockClient = mock(NetworkClient.class);
        MemoryLens.setNetworkClient(mockClient);

        // Mock Register Failure(s)
        doAnswer(invocation -> {
            NetworkClient.Callback cb = invocation.getArgument(2);
            // Verify what input we got to decide error
            Map<String, Object> data = invocation.getArgument(1);
            String email = (String) data.get("email");
            String pass = (String) data.get("password");
            
            if ("mal".equals(email)) {
                 cb.onFailure("Please enter a valid email address");
            } else if ("mal".equals(pass)) {
                 cb.onFailure("Password must be at least 6 characters");
            } else {
                 cb.onSuccess(new HashMap<>());
            }
            return null;
        }).when(mockClient).post(eq("/api/auth/register"), anyMap(), any(NetworkClient.Callback.class));

        // Inject Mock
        AuthUseCase mockUseCase = new AuthUseCase(new AuthGatewayNodeRed(mockClient));
        MemoryLens.setAuthUseCase(mockUseCase);
        MemoryLens.setNetworkClient(mockClient);

        // Run Test
        waitForFormName("loginForm");
        clickButtonByName("goRegisterBtn");
        waitForFormName("registerForm");
        setText("registerName", "mal");
        setText("registerEmail", "mal");
        setText("registerPassword", "mal");
        clickButtonByName("registerBtn");
        
        waitForFormTitle("Error");
        if (Display.getInstance().getCurrent() != null) {
            Display.getInstance().getCurrent().setName("Form_12");
        }
        assertTextArea("Registration failed: Please enter a valid email address");
        goBack(); // Close error
        
        waitForFormName("registerForm");
        setText("registerEmail", "mal@m.com"); // Fix email
        clickButtonByName("registerBtn");
        
        waitForFormTitle("Error");
        if (Display.getInstance().getCurrent() != null) {
            Display.getInstance().getCurrent().setName("Form_13");
        }
        assertTextArea("Registration failed: Password must be at least 6 characters");
        goBack(); // Close error
        
        waitForFormName("registerForm");
        clickButtonByName("backToLoginBtn");
        waitForFormName("loginForm");
        return true;
    }
}
