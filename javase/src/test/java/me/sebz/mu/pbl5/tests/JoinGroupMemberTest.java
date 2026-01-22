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

public class JoinGroupMemberTest extends AbstractTest {

    @Override
    public boolean runTest() throws Exception {
        // Setup Mocks
        NetworkClient mockClient = mock(NetworkClient.class);
        MemoryLens.setNetworkClient(mockClient);

        // Mock Login
        doAnswer(invocation -> {
            NetworkClient.Callback cb = invocation.getArgument(2);
            Map<String, Object> data = new HashMap<>();
            data.put("session", "mock-session-member");
            data.put("memberId", 200L);
            data.put("role", "2.0"); // Family Member
            data.put("hasEmbedding", false);
            cb.onSuccess(data);
            return null;
        }).when(mockClient).post(eq("/api/auth/login"), anyMap(), any(NetworkClient.Callback.class));

        // Mock Join Group
        doAnswer(invocation -> {
            NetworkClient.Callback cb = invocation.getArgument(2);
            Map<String, Object> response = new HashMap<>();
            response.put("familyGroupId", 45L);
            cb.onSuccess(response);
            return null;
        }).when(mockClient).post(eq("/api/groups/join"), anyMap(), any(NetworkClient.Callback.class));

        // Mock Check Embedding (called by FamilyDashboard on load)
        doAnswer(invocation -> {
            NetworkClient.Callback cb = invocation.getArgument(1);
            Map<String, Object> response = new HashMap<>();
            // Empty list or list without me -> hasEmbedding=false
            response.put("root", new java.util.ArrayList<>());
            cb.onSuccess(response);
            return null;
        }).when(mockClient).get(eq("/api/group/45/member"), any(NetworkClient.Callback.class));

        // Inject Mock
        AuthUseCase mockUseCase = new AuthUseCase(new AuthGatewayNodeRed(mockClient));
        MemoryLens.setAuthUseCase(mockUseCase);
        MemoryLens.setNetworkClient(mockClient);

        // Run Test
        waitForFormName("loginForm");
        setText("loginEmail", "member@m.com");
        setText("loginPassword", "member123");
        clickButtonByName("loginBtn");
        
        waitForFormName("groupOnboardingForm");
        setText("inviteCode", "28F276C7");
        clickButtonByName("joinGroupBtn");
        
        waitForFormTitle("Success");
        if (Display.getInstance().getCurrent() != null) {
            Display.getInstance().getCurrent().setName("Form_6");
        }
        assertTextArea("You have joined the group!");
        goBack(); // Close success dialog
        
        // After joining, it navigates to dashboard.
        // For family members without embedding, it goes to "Add Your Profile" (Form_7 in original test)
        // Original: "waitForFormName("groupOnboardingForm");" then "waitForFormName("Form_7");"
        // Since callSerially is used, there might be a slight delay or order.
        // "navigateToDashboard()" calls logic which eventually replaces the form.
        
        // Wait for the new form
        waitForFormTitle("Add Your Profile");
        if (Display.getInstance().getCurrent() != null) {
            Display.getInstance().getCurrent().setName("Form_7");
        }
        
        assertLabel("Register Face");
        assertLabel("Name:");
        assertLabel("Relationship:");
        assertLabel("Context:");
        assertLabel("Take Photo");
        assertLabel("No Image Selected");
        assertLabel("Upload Profile");
        
        // Logout
        clickButtonByLabel("Logout");
        waitForFormName("loginForm");
        return true;
    }
}
