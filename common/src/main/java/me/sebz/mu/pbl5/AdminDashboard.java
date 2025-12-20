package me.sebz.mu.pbl5;

import com.codename1.ui.*;
import com.codename1.ui.layouts.BoxLayout;

import java.util.Map;

public class AdminDashboard extends Form {

    public AdminDashboard() {
        super("Admin Dashboard", BoxLayout.y());

        this.add(new Label("Group Members"));

        // Placeholder for member list
        Container memberList = new Container(BoxLayout.y());
        memberList.add(new Label(" - Member 1 (Family)"));
        memberList.add(new Label(" - Member 2 (Patient)"));
        this.add(memberList);

        Button inviteButton = new Button("Generate Invite Code");
        inviteButton.addActionListener(e -> generateInvite());
        this.add(inviteButton);

        Button logoutButton = new Button("Logout");
        logoutButton.setUIID("ButtonSecondary"); // Use secondary style (red) for logout
        logoutButton.addActionListener(e -> MemoryLens.logout());
        this.add(logoutButton);
    }

    private void generateInvite() {
        Long groupId = MemoryLens.getFamilyGroupId();
        if (groupId == null) {
            Dialog.show("Error", "No Group ID found. Please login again.", "OK", null);
            return;
        }
        GenericNetworkService.getInstance().get("/api/groups/" + groupId + "/invite",
                new GenericNetworkService.NetworkCallback() {
                    @Override
                    public void onSuccess(Map<String, Object> response) {

                        String code = (String) response.get("code");
                        Display.getInstance().callSerially(() -> {
                            Dialog.show("Invite Code", "Share this code: " + code, "OK", null);
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // [DEV] Fallback for testing without backend
                        System.out.println("Network failed, using mock data. Error: " + errorMessage);
                        Display.getInstance().callSerially(() -> {
                            // Show error but also the mock code for demo
                            // Dialog.show("Error", "Failed to connect: " + errorMessage, "OK", null);
                            Dialog.show("Invite Code (Mock)", "Share this code: 12345-MOCK", "OK", null);
                        });
                    }
                });
    }
}
