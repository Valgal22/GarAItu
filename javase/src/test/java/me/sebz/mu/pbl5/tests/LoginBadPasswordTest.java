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

public class LoginBadPasswordTest extends AbstractTest {

    @Override
    public boolean runTest() throws Exception {
        // Setup Mocks
        NetworkClient mockClient = mock(NetworkClient.class);
        MemoryLens.setNetworkClient(mockClient);

        // Mock Login Failure
        doAnswer(invocation -> {
            NetworkClient.Callback cb = invocation.getArgument(2);
            Map<String, Object> data = invocation.getArgument(1);
            String pass = (String) data.get("password");
            
            if ("badpassword".equals(pass)) {
                cb.onFailure("Incorrect password");
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
        setText("loginEmail", "member@m.com");
        setText("loginPassword", "badpassword");
        clickButtonByName("loginBtn");
        
        waitForFormTitle("Login Failed");
        if (Display.getInstance().getCurrent() != null) {
            Display.getInstance().getCurrent().setName("Form_16");
        }
        assertTextArea("Incorrect password");
        goBack();
        
        waitForFormName("loginForm");
        
        // Reset fields
        setText("loginPassword", "");
        setText("loginEmail", "");
        
        return true;
    }
}
