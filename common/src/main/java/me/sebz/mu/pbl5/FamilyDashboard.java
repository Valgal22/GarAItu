package me.sebz.mu.pbl5;

import com.codename1.capture.Capture;
import com.codename1.components.ToastBar;
import com.codename1.ui.*;
import com.codename1.ui.layouts.BoxLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import me.sebz.mu.pbl5.logic.IdUtil;
import me.sebz.mu.pbl5.logic.RoleUtil;

public class FamilyDashboard extends Form {

    private TextField nameField;
    private ComboBox<String> relationshipPicker;
    private TextArea contextArea;
    private Label imageLabel;
    private String imagePath;

    private Container memberListContainer;

    public FamilyDashboard() {
        super("Loading...", BoxLayout.y());
        this.setScrollableY(true);

        this.add(new Label("Loading Profile..."));
        checkEmbeddingStatus();
    }

    private void checkEmbeddingStatus() {
        Long groupId = MemoryLens.getFamilyGroupId();
        Long memberId = MemoryLens.getMemberId();

        if (groupId == null || memberId == null) {
            showRegistrationForm();
            return;
        }

        GenericNetworkService.getInstance().get("/api/group/" + groupId + "/member",
                new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
                    @Override
                    public void onSuccess(Map<String, Object> response) {
                        java.util.List<Map<String, Object>> list;
                        if (response.containsKey("root")) list = (java.util.List<Map<String, Object>>) response.get("root");
                        else list = null;

                        boolean myEmbeddingStatus = false;

                        if (list != null) {
                            for (Map<String, Object> member : list) {
                                String mIdStr = String.valueOf(member.get("id"));
                                if (IdUtil.areIdsEqual(mIdStr, String.valueOf(memberId))) {
                                    Object heObj = member.get("hasEmbedding");
                                    if (heObj instanceof Boolean) myEmbeddingStatus = (Boolean) heObj;
                                    else if (heObj != null) myEmbeddingStatus = Boolean.parseBoolean(String.valueOf(heObj));
                                    break;
                                }
                            }
                        }

                        final boolean finalStatus = myEmbeddingStatus;

                        Display.getInstance().callSerially(() -> {
                            MemoryLens.setHasEmbedding(finalStatus);
                            if (finalStatus) showMemberListView();
                            else showRegistrationForm();
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Display.getInstance().callSerially(() -> {
                            ToastBar.showErrorMessage("Failed to check status: " + errorMessage);
                            showRegistrationForm();
                        });
                    }
                });
    }

    private void showRegistrationForm() {
        this.removeAll();
        this.setTitle("Add Your Profile");

        Label title = new Label("Register Face");
        title.setUIID("Title");

        nameField = new TextField("", "Name", 20, TextField.ANY);
        nameField.setName("fam_name");

        relationshipPicker = new ComboBox<>("Spouse", "Child", "Sibling", "Friend", "Caregiver");
        relationshipPicker.setName("fam_relation");

        contextArea = new TextArea(5, 20);
        contextArea.setHint("Context (e.g., 'This is your daughter Sarah')");
        contextArea.setName("fam_context");

        Button photoButton = new Button("Take Photo");
        photoButton.setMaterialIcon(FontImage.MATERIAL_CAMERA_ALT, 5);
        photoButton.setName("fam_take_photo");
        photoButton.addActionListener(e -> capturePhoto());

        imageLabel = new Label("No Image Selected");

        Button uploadButton = new Button("Upload Profile");
        uploadButton.setMaterialIcon(FontImage.MATERIAL_CLOUD_UPLOAD, 5);
        uploadButton.setName("fam_upload");
        uploadButton.addActionListener(e -> uploadData());

        Container content = new Container(BoxLayout.y());
        content.add(title);
        content.add(new Label("Name:"));
        content.add(nameField);
        content.add(new Label("Relationship:"));
        content.add(relationshipPicker);
        content.add(new Label("Context:"));
        content.add(contextArea);
        content.add(photoButton);
        content.add(imageLabel);
        content.add(uploadButton);

        this.add(content);
        addLogoutButton();
        this.revalidate();
    }

    private void showMemberListView() {
        this.removeAll();
        this.setTitle("Family Group");

        this.add(new Label("Group Members"));

        memberListContainer = new Container(BoxLayout.y());
        memberListContainer.setName("fam_member_list");
        this.add(memberListContainer);

        Button refreshButton = new Button("Refresh");
        refreshButton.setName("fam_refresh");
        refreshButton.addActionListener(e -> fetchMembers());
        this.add(refreshButton);

        Button editProfileButton = new Button("Edit My Profile");
        editProfileButton.setMaterialIcon(FontImage.MATERIAL_EDIT, 5);
        editProfileButton.setName("fam_edit");
        editProfileButton.addActionListener(e -> showEditProfileDialog());
        this.add(editProfileButton);

        addLogoutButton();

        this.show();
        fetchMembers();
    }

    private void fetchMembers() {
        Long groupId = MemoryLens.getFamilyGroupId();
        if (groupId == null) return;

        GenericNetworkService.getInstance().get("/api/group/" + groupId + "/member",
                new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
                    @Override
                    public void onSuccess(Map<String, Object> response) {
                        java.util.List<Map<String, Object>> list;
                        if (response.containsKey("root")) list = (java.util.List<Map<String, Object>>) response.get("root");
                        else list = null;

                        Display.getInstance().callSerially(() -> {
                            memberListContainer.removeAll();
                            if (list != null) {
                                for (Map<String, Object> member : list) addMemberRow(member);
                            } else {
                                memberListContainer.add(new Label("No members found."));
                            }
                            memberListContainer.revalidate();
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Display.getInstance().callSerially(() -> ToastBar.showErrorMessage("Load failed: " + errorMessage));
                    }
                });
    }

    private void addMemberRow(Map<String, Object> member) {
        String name = (String) member.get("username");
        if (name == null) name = (String) member.get("email");

        String roleName = RoleUtil.roleName(member.get("role"));

        Container row = new Container(new com.codename1.ui.layouts.BorderLayout());
        row.setUIID("MemberRow");
        row.getAllStyles().setBorder(com.codename1.ui.plaf.Border.createLineBorder(1, 0xcccccc));
        row.getAllStyles().setPadding(2, 2, 2, 2);

        row.add(com.codename1.ui.layouts.BorderLayout.CENTER, new Label(name + " (" + roleName + ")"));
        memberListContainer.add(row);
    }

    private void showEditProfileDialog() {
        Form editForm = new Form("Edit Profile", BoxLayout.y());
        TextField editName = new TextField("", "Name", 20, TextField.ANY);
        TextArea editContext = new TextArea(3, 20);
        Label imgReminder = new Label("Image: Already Registered (Face Embedding Set)");
        imgReminder.getAllStyles().setFgColor(0x008000);

        editForm.add(new Label("Name:"));
        editForm.add(editName);
        editForm.add(new Label("Context:"));
        editForm.add(editContext);
        editForm.add(imgReminder);

        Button saveBtn = new Button("Save Changes");
        saveBtn.setName("fam_save_edit");
        saveBtn.addActionListener(e -> {
            String newName = editName.getText();
            String newContext = editContext.getText();

            if (newName == null || newName.trim().isEmpty()) {
                Dialog.show("Error", "Name cannot be empty", "OK", null);
                return;
            }

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("name", newName);
            updateData.put("context", newContext);

            GenericNetworkService.getInstance().put("/garAItu/member/" + MemoryLens.getMemberId(), updateData,
                    new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
                        @Override
                        public void onSuccess(Map<String, Object> response) {
                            Display.getInstance().callSerially(() -> {
                                Dialog.show("Success", "Profile Updated", "OK", null);
                                showMemberListView();
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Display.getInstance().callSerially(() -> Dialog.show("Error", "Update failed: " + errorMessage, "OK", null));
                        }
                    });
        });
        editForm.add(saveBtn);

        Command backCmd = new Command("Back") {
            @Override
            public void actionPerformed(com.codename1.ui.events.ActionEvent evt) {
                showMemberListView();
            }
        };
        editForm.setBackCommand(backCmd);
        editForm.getToolbar().setBackCommand(backCmd);
        editForm.show();
    }

    private void addLogoutButton() {
        Button logoutButton = new Button("Logout");
        logoutButton.setUIID("ButtonSecondary");
        logoutButton.setName("fam_logout");
        logoutButton.addActionListener(e -> MemoryLens.logout());
        this.add(logoutButton);
    }

    private void capturePhoto() {
        String filePath = Capture.capturePhoto(1024, -1);
        if (filePath != null) {
            imagePath = filePath;
            try {
                Image img = Image.createImage(filePath);
                imageLabel.setIcon(img.scaled(300, 300));
                imageLabel.setText("");
                this.revalidate();
            } catch (IOException e) {
                ToastBar.showErrorMessage("Error: " + e.getMessage());
            }
        }
    }

    private void uploadData() {
        if (imagePath == null) {
            ToastBar.showErrorMessage("Please take a photo first.");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", nameField.getText());
        data.put("relationship", relationshipPicker.getSelectedItem());
        data.put("context", contextArea.getText());

        Long mId = MemoryLens.getMemberId();
        Long gId = MemoryLens.getFamilyGroupId();

        if (mId == null || gId == null) {
            Dialog.show("Error", "Session invalid. Login again.", "OK", null);
            return;
        }

        data.put("id", mId);
        data.put("groupId", gId);

        GenericNetworkService.getInstance().upload("/api/upload", imagePath, data,
                new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
                    @Override
                    public void onSuccess(Map<String, Object> response) {
                        Display.getInstance().callSerially(() -> {
                            Dialog.show("Success", "Profile uploaded!", "OK", null);
                            MemoryLens.setHasEmbedding(true);
                            showMemberListView();
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Display.getInstance().callSerially(() -> Dialog.show("Error", errorMessage, "OK", null));
                    }
                });
    }
}
