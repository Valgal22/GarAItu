package me.sebz.mu.pbl5;

import com.codename1.ui.*;
import com.codename1.ui.layouts.*;
import me.sebz.mu.pbl5.logic.RoleUtil;
import me.sebz.mu.pbl5.services.GroupService;

public class GroupOnboardingForm extends Form {

    private final GroupService groupService;

    public GroupOnboardingForm() {
        this(new GroupService(GenericNetworkService.getInstance()));
    }

    public GroupOnboardingForm(GroupService groupService) {
        super("Welcome", new BorderLayout());
        this.groupService = groupService;

        Container center = new Container(BoxLayout.y());
        center.setScrollableY(true);
        center.getAllStyles().setPadding(10, 10, 10, 10);

        Label title = new Label("Joining a Family Group");
        title.setUIID("Title");

        Label info = new Label("To proceed, you need to join a family group.");
        info.setUIID("Label");

        TextField inviteCodeField = new TextField("", "Invite Code", 20, TextField.ANY);
        inviteCodeField.setUIID("TextField");
        inviteCodeField.setName("group_invite");

        Button joinButton = new Button("Join with Code");
        joinButton.setMaterialIcon(FontImage.MATERIAL_GROUP_ADD, 5);
        joinButton.setName("group_join");

        joinButton.addActionListener(e -> {
            String code = inviteCodeField.getText();

            groupService.joinWithCode(code, new GroupService.JoinCallback() {
                @Override
                public void onSuccess(Long familyGroupId) {
                    MemoryLens.setFamilyGroupId(familyGroupId);
                    Display.getInstance().callSerially(() -> {
                        Dialog.show("Success", "You have joined the group!", "OK", null);
                        MemoryLens.navigateToAppropriateDashboard();
                    });
                }

                @Override
                public void onFailure(String error) {
                    Display.getInstance().callSerially(() -> Dialog.show("Error", "Could not join: " + error, "OK", null));
                }
            });
        });

        center.add(title);
        center.add(info);
        center.add(new Label("Invite Code:"));
        center.add(inviteCodeField);
        center.add(joinButton);

        if (RoleUtil.isAdmin(MemoryLens.getUserRole())) {
            Button createButton = new Button("Create New Family Group");
            createButton.setUIID("ButtonSecondary");
            createButton.setMaterialIcon(FontImage.MATERIAL_ADD_CIRCLE, 5);
            createButton.setName("group_create_open");
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

            groupService.createGroup(name, new GroupService.CreateCallback() {
                @Override
                public void onSuccess(Long fgId) {
                    MemoryLens.setFamilyGroupId(fgId);
                    MemoryLens.setUserRole("0");

                    Display.getInstance().callSerially(() -> {
                        Dialog.show("Success", "Group created successfully!", "OK", null);
                        MemoryLens.navigateToAppropriateDashboard();
                    });
                }

                @Override
                public void onFailure(String error) {
                    Display.getInstance().callSerially(() -> Dialog.show("Error", "Creation failed: " + error, "OK", null));
                }
            });
        }
    }
}
