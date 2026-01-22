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

public class CreateAdminSuccessTest extends AbstractTest {

    @Override
    public boolean runTest() throws Exception {
        // Setup Global Mock (Good practice to reset/set globally if we move to global mocks)
        NetworkClient mockClient = mock(NetworkClient.class);
        MemoryLens.setNetworkClient(mockClient);

        // Mock Register Call to return successful "ok: true"
        doAnswer(invocation -> {
            NetworkClient.Callback cb = invocation.getArgument(2);
            Map<String, Object> response = new HashMap<>();
            response.put("ok", Boolean.TRUE);
            cb.onSuccess(response);
            return null;
        }).when(mockClient).post(eq("/api/auth/register"), anyMap(), any(NetworkClient.Callback.class));

        // Inject Mock
        // Note: GenericNetworkService.getInstance() is normally used, but we replace the UseCase directly
        AuthUseCase mockUseCase = new AuthUseCase(new AuthGatewayNodeRed(mockClient));
        MemoryLens.setAuthUseCase(mockUseCase);
        
        // Ensure global client is set for any other logic
        MemoryLens.setNetworkClient(mockClient);

        // Run UI Test Steps
        waitForFormName("loginForm");
        clickButtonByName("goRegisterBtn");
        waitForFormName("registerForm");
        setText("registerName", "admin");
        setText("registerEmail", "admin@a.com");
        setText("registerPassword", "admin123");
        setText("registerChatId", "1414134108");
        clickButtonByName("registerBtn");
        
        // Wait for Success dialog
        waitForFormTitle("Success");
        
        // Ensure the dialog is named expectedly if asserting text on it
        if (Display.getInstance().getCurrent() != null) {
            Display.getInstance().getCurrent().setName("Form_1");
        }
        
        assertTextArea("Account created! Please login to join a group.");
        
        // goBack(); 
        // Using explicit click to ensure dialog closes and triggers subsequent logic
        clickButtonByLabel("OK");
        waitForFormName("loginForm");
        
        return true;
    }
}
