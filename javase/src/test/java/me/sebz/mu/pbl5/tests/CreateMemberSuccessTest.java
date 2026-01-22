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

public class CreateMemberSuccessTest extends AbstractTest {

    @Override
    public boolean runTest() throws Exception {
        // Setup Mocks
        NetworkClient mockClient = mock(NetworkClient.class);
        MemoryLens.setNetworkClient(mockClient);

        doAnswer(invocation -> {
            NetworkClient.Callback cb = invocation.getArgument(2);
            Map<String, Object> response = new HashMap<>();
            response.put("ok", Boolean.TRUE);
            cb.onSuccess(response);
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
        setText("registerName", "member");
        setText("registerEmail", "member@m.com");
        setText("registerPassword", "member123");
        
        // Select Role: Family Member (Index 2 in roles array)
        if (Display.getInstance().getCurrent() != null) {
            // Ensure UIID naming if needed, but selectInList usually works by component index or UI logic
        }
        
        // The original test used index 2 for "Family Member"
        // String[] roles = { "Caregiver (Admin)", "Patient", "Family Member" };
        selectInList(new int[]{0, 8}, 2); // 0,8 might be path to ComboBox in component tree
        
        clickButtonByName("registerBtn");
        
        waitForFormTitle("Success");
        if (Display.getInstance().getCurrent() != null) {
            Display.getInstance().getCurrent().setName("Form_Success");
        }
        assertTextArea("Account created! Please login to join a group.");
        
        goBack(); // Closes dialog
        waitForFormName("loginForm");
        return true;
    }
}
