package me.sebz.mu.pbl5;

import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;

import java.util.HashMap;
import java.util.Map;

public class GroupOnboardingForm extends Form {

    private static final String TITLE_ERROR = "Error";

    public GroupOnboardingForm() {
        super("Welcome", new BorderLayout());
        setName("groupOnboardingForm");

        Container center = new Container(BoxLayout.y());
        center.setName("groupOnboardingCenter");
        center.setScrollableY(true);
        center.getAllStyles().setPadding(10, 10, 10, 10);

        Label title = new Label("Joining a Family Group");
        title.setName("groupOnboardingTitle");
        title.setUIID("Title");

        Label info = new Label("To proceed, you need to join a family group.");
        info.setName("groupOnboardingInfo");
        info.setUIID("Label");

        TextField inviteCodeField = new TextField("", "Invite Code", 20, TextArea.ANY);
        inviteCodeField.setName("inviteCode");
        inviteCodeField.setUIID("TextField");

        Button joinButton = new Button("Join with Code");
        joinButton.setName("joinGroupBtn");
        joinButton.setMaterialIcon(FontImage.MATERIAL_GROUP_ADD, 5);

        joinButton.addActionListener(e -> {
            String code = inviteCodeField.getText();
            if (code == null || code.trim().isEmpty()) {
                Dialog.show(TITLE_ERROR, "Please enter an invite code", "OK", null);
                return;
            }

            joinButton.setEnabled(false);

            Map<String, Object> data = new HashMap<>();
            data.put("inviteCode", code);

            MemoryLens.getNetworkClient().post("/api/groups/join", data,
                    new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
                        @Override
                        public void onSuccess(Map<String, Object> response) {
                            Object fgIdObj = response.get("familyGroupId");
                            Long fgId = (fgIdObj instanceof Number) ? ((Number) fgIdObj).longValue() : null;
                            MemoryLens.setFamilyGroupId(fgId);

                            Display.getInstance().callSerially(() -> {
                                joinButton.setEnabled(true);
                                Dialog.show("Success", "You have joined the group!", "OK", null);
                                navigateToDashboard();
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Display.getInstance().callSerially(() -> {
                                joinButton.setEnabled(true);
                                Dialog.show(TITLE_ERROR, "Could not join: " + errorMessage, "OK", null);
                            });
                        }
                    });
        });

        center.add(title);
        center.add(info);
        center.add(new Label("Invite Code:"));
        center.add(inviteCodeField);
        center.add(joinButton);

        String role = MemoryLens.getUserRole();
        boolean isAdmin = "0".equals(role) || "0.0".equals(role) || "ADMIN".equalsIgnoreCase(role);

        if (isAdmin) {
            Button createButton = new Button("Create New Family Group");
            createButton.setName("createGroupBtn");
            createButton.setUIID("ButtonSecondary");
            createButton.setMaterialIcon(FontImage.MATERIAL_ADD_CIRCLE, 5);
            createButton.addActionListener(e -> showCreateGroupDialog());

            center.add(new Label("--- OR ---"));
            center.add(createButton);
        }

        add(BorderLayout.CENTER, center);
    }

    private void showCreateGroupDialog() {
        TextField groupNameField = new TextField("", "Group Name (e.g. Smith Family)", 20, TextArea.ANY);
        groupNameField.setName("createGroupName");

        Command ok = new Command("Create");
        Command cancel = new Command("Cancel");

        if (Dialog.show("Create Group", groupNameField, ok, cancel) != ok) {
            return;
        }

        String name = groupNameField.getText();
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);

        MemoryLens.getNetworkClient().post("/api/groups/create", data,
                new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
                    @Override
                    public void onSuccess(Map<String, Object> response) {
                        Object fgIdObj = response.get("id");
                        Long fgId = (fgIdObj instanceof Number) ? ((Number) fgIdObj).longValue() : null;
                        MemoryLens.setFamilyGroupId(fgId);
                        MemoryLens.setUserRole("0");

                        Display.getInstance().callSerially(() -> {
                            Dialog.show("Success", "Group created successfully!", "OK", null);
                            navigateToDashboard();
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Display.getInstance().callSerially(
                                () -> Dialog.show(TITLE_ERROR, "Creation failed: " + errorMessage, "OK", null));
                    }
                });
    }

    private void navigateToDashboard() {
        MemoryLens.navigateToAppropriateDashboard();
    }
}
