package me.sebz.mu.pbl5;

import com.codename1.ui.*;
import com.codename1.ui.layouts.*;
import java.util.HashMap;
import java.util.Map;

public class GroupOnboardingForm extends Form {

    public GroupOnboardingForm() {
        super("Welcome", new BorderLayout());

        Container center = new Container(BoxLayout.y());
        center.setScrollableY(true);
        center.getAllStyles().setPadding(10, 10, 10, 10);

        Label title = new Label("Joining a Family Group");
        title.setUIID("Title");

        Label info = new Label("To proceed, you need to join a family group.");
        info.setUIID("Label");

        TextField inviteCodeField = new TextField("", "Invite Code", 20, TextField.ANY);
        inviteCodeField.setUIID("TextField");

        Button joinButton = new Button("Join with Code");
        joinButton.setMaterialIcon(FontImage.MATERIAL_GROUP_ADD, 5);

        joinButton.addActionListener(e -> {
            String code = inviteCodeField.getText();
            if (code.isEmpty()) {
                Dialog.show("Error", "Please enter an invite code", "OK", null);
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("inviteCode", code);

            GenericNetworkService.getInstance().post("/api/groups/join", data,
                    new GenericNetworkService.NetworkCallback() {
                        @Override
                        public void onSuccess(Map<String, Object> response) {
                            // After joining, we might need to refresh user data or just go to dashboard
                            Object fgIdObj = response.get("familyGroupId");
                            Long fgId = (fgIdObj instanceof Number) ? ((Number) fgIdObj).longValue() : null;
                            MemoryLens.setFamilyGroupId(fgId);

                            Display.getInstance().callSerially(() -> {
                                Dialog.show("Success", "You have joined the group!", "OK", null);
                                navigateToDashboard();
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Display.getInstance().callSerially(() -> {
                                Dialog.show("Error", "Could not join: " + errorMessage, "OK", null);
                            });
                        }
                    });
        });

        center.add(title);
        center.add(info);
        center.add(new Label("Invite Code:"));
        center.add(inviteCodeField);
        center.add(joinButton);

        // For caregivers/admins, allow creating a group
        String role = MemoryLens.getUserRole();
        // Allow if role is 0 (Admin) or explicit ADMIN string.
        // Handle "0.0" as seen in previous issues.
        boolean isAdmin = "0".equals(role) || "0.0".equals(role) || "ADMIN".equalsIgnoreCase(role);

        if (isAdmin) {
            Button createButton = new Button("Create New Family Group");
            createButton.setUIID("ButtonSecondary");
            createButton.setMaterialIcon(FontImage.MATERIAL_ADD_CIRCLE, 5);
            createButton.addActionListener(e -> showCreateGroupDialog());

            center.add(new Label("--- OR ---"));
            center.add(createButton);
        }

        this.add(BorderLayout.CENTER, center);
    }

    private void showCreateGroupDialog() {
        TextField groupNameField = new TextField("", "Group Name (e.g. Smith Family)", 20, TextField.ANY);
        Command ok = new Command("Create");
        Command cancel = new Command("Cancel");
        if (Dialog.show("Create Group", groupNameField, ok, cancel) == ok) {
            String name = groupNameField.getText();
            if (name.isEmpty())
                return;

            Map<String, Object> data = new HashMap<>();
            data.put("name", name);

            GenericNetworkService.getInstance().post("/api/groups/create", data,
                    new GenericNetworkService.NetworkCallback() {
                        @Override
                        public void onSuccess(Map<String, Object> response) {
                            Object fgIdObj = response.get("id");
                            Long fgId = (fgIdObj instanceof Number) ? ((Number) fgIdObj).longValue() : null;
                            MemoryLens.setFamilyGroupId(fgId);
                            // Set role to ADMIN (0) since creator is admin
                            MemoryLens.setUserRole("0");

                            Display.getInstance().callSerially(() -> {
                                Dialog.show("Success", "Group created successfully!", "OK", null);
                                navigateToDashboard();
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Display.getInstance().callSerially(() -> {
                                Dialog.show("Error", "Creation failed: " + errorMessage, "OK", null);
                            });
                        }
                    });
        }
    }

    private void navigateToDashboard() {
        MemoryLens.navigateToAppropriateDashboard();
    }
}
