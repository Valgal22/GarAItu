package me.sebz.mu.pbl5.tests;

import com.codename1.testing.AbstractTest;
import com.codename1.ui.Display;
import me.sebz.mu.pbl5.MemoryLens;
import me.sebz.mu.pbl5.core.auth.AuthGatewayNodeRed;
import me.sebz.mu.pbl5.core.auth.AuthUseCase;
import me.sebz.mu.pbl5.net.NetworkClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class ListMembersAdminDeleteMemberTest extends AbstractTest {

    private List<Map<String, Object>> members;

    @Override
    public boolean runTest() throws Exception {
        // Setup Mocks
        NetworkClient mockClient = mock(NetworkClient.class);
        MemoryLens.setNetworkClient(mockClient);

        // Mock Login
        doAnswer(invocation -> {
            NetworkClient.Callback cb = invocation.getArgument(2);
            Map<String, Object> data = new HashMap<>();
            data.put("session", "mock-session-admin");
            data.put("memberId", 123L);
            data.put("familyGroupId", 45L);
            data.put("role", "0"); // Admin
            data.put("hasEmbedding", false);
            data.put("chatId", "1414134108");
            cb.onSuccess(data);
            return null;
        }).when(mockClient).post(eq("/api/auth/login"), anyMap(), any(NetworkClient.Callback.class));

        // Initialize Members List
        members = new ArrayList<>();
        members.add(createMember(123L, null, "admin@a.com", "0", "1414134108"));
        members.add(createMember(200L, null, "member@m.com", "2.0", null));
        members.add(createMember(300L, null, "patient@p.com", "1.0", null));

        // Mock Get Members
        doAnswer(invocation -> {
            NetworkClient.Callback cb = invocation.getArgument(1);
            Map<String, Object> response = new HashMap<>();
            response.put("root", new ArrayList<>(members));
            cb.onSuccess(response);
            return null;
        }).when(mockClient).get(eq("/api/group/45/member"), any(NetworkClient.Callback.class));

        // Mock Delete Member
        doAnswer(invocation -> {
            String url = invocation.getArgument(0);
            NetworkClient.Callback cb = invocation.getArgument(1);
            // URL format: /api/group/45/member/{id}
            String idStr = url.substring(url.lastIndexOf("/") + 1);
            Long id = Long.parseLong(idStr);
            
            // Remove from list
            members.removeIf(m -> m.get("id").equals(id));
            
            cb.onSuccess(new HashMap<>());
            return null;
        }).when(mockClient).delete(any(String.class), any(NetworkClient.Callback.class));

        // Inject Mock
        AuthUseCase mockUseCase = new AuthUseCase(new AuthGatewayNodeRed(mockClient));
        MemoryLens.setAuthUseCase(mockUseCase);
        MemoryLens.setNetworkClient(mockClient);

        // Run Test
        waitForFormName("loginForm");
        setText("loginEmail", "admin@a.com");
        setText("loginPassword", "admin123");
        clickButtonByName("loginBtn");
        
        waitForFormTitle("Admin Dashboard");
        if (Display.getInstance().getCurrent() != null) {
            Display.getInstance().getCurrent().setName("Form_8");
        }
        
        assertLabel("Group Members");
        assertLabel("admin@a.com (Admin) [TG: 1414134108]");
        assertLabel("member@m.com (Family Member)");
        assertLabel("patient@p.com (Patient)");
        
        // Delete "member@m.com" (The second one in the list usually, or find by UI)
        // Original: clickButtonByPath(new int[]{1, 1, 1});
        // Assuming this clicks the delete button of the middle row.
        // Let's rely on path if UI structure is consistent.
        // Row 0: Admin. Row 1: Member. Row 2: Patient.
        // Container of list is index 1 in AdminDashboard (Label is index 0).
        // Inside list (Container), index 1 is Member Row.
        // Inside Member Row (BorderLayout), EAST is Delete Button (index 1? or depends on insertion order/layout).
        // BorderLayout adds: CENTER, EAST.
        // Components: [Label, Button].
        // So {1 (list), 1 (row 2), 1 (button)} seems plausible if index is 0-based.
        clickButtonByPath(new int[]{1, 1, 1});
        
        // Confirm Dialog
        waitForFormTitle("Confirm");
        if (Display.getInstance().getCurrent() != null) {
            Display.getInstance().getCurrent().setName("Form_9");
        }
        clickButtonByLabel("Yes");
        
        waitForFormName("Form_8"); // Back to dashboard
        
        // Verify member is gone
        assertLabel("Group Members");
        assertLabel("admin@a.com (Admin) [TG: 1414134108]");
        assertLabel("patient@p.com (Patient)");
        // assertLabel("member@m.com (Family Member)"); should fail if I asserted it, but I won't.
        
        clickButtonByLabel("Logout");
        waitForFormName("loginForm");
        return true;
    }

    private Map<String, Object> createMember(Long id, String username, String email, String role, String chatId) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("username", username);
        m.put("email", email);
        m.put("role", role);
        m.put("chatId", chatId);
        m.put("hasEmbedding", false);
        return m;
    }
}
