package me.sebz.mu.pbl5;

import com.codename1.capture.Capture;
import com.codename1.components.ToastBar;
import com.codename1.ui.Button;
import com.codename1.ui.ComboBox;
import com.codename1.ui.Command;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.layouts.BoxLayout;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FamilyDashboard extends Form {

    private static final String TITLE_ERROR = "Error";
    private static final String TITLE_SUCCESS = "Success";

    // Registration UI Fields
    private TextField nameField;
    private ComboBox<String> relationshipPicker;
    private TextArea contextArea;
    private Label imageLabel;
    private String imagePath;

    // List UI Fields
    private Container memberListContainer;

    public FamilyDashboard() {
        super("Loading...", BoxLayout.y());
        setScrollableY(true);

        add(new Label("Loading Profile..."));

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
                        List<Map<String, Object>> list = extractMemberList(response);
                        boolean myEmbeddingStatus = extractMyEmbeddingStatus(list, memberId);

                        Display.getInstance().callSerially(() -> applyEmbeddingStatus(myEmbeddingStatus));
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

    private void applyEmbeddingStatus(boolean hasEmbedding) {
        MemoryLens.setHasEmbedding(hasEmbedding);
        if (hasEmbedding) {
            showMemberListView();
        } else {
            showRegistrationForm();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractMemberList(Map<String, Object> response) {
        Object root = (response != null) ? response.get("root") : null;
        if (root instanceof List) {
            return (List<Map<String, Object>>) root;
        }
        return Collections.emptyList();
    }

    private boolean extractMyEmbeddingStatus(List<Map<String, Object>> list, Long memberId) {
        if (list == null || list.isEmpty() || memberId == null) {
            return false;
        }

        String myId = String.valueOf(memberId);
        for (Map<String, Object> member : list) {
            String mIdStr = String.valueOf(member.get("id"));
            if (areIdsEqual(mIdStr, myId)) {
                return parseBoolean(member.get("hasEmbedding"));
            }
        }
        return false;
    }

    private boolean parseBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }

    private boolean areIdsEqual(String id1, String id2) {
        if (id1 == null || id2 == null) {
            return false;
        }
        try {
            double d1 = Double.parseDouble(id1);
            double d2 = Double.parseDouble(id2);
            return Math.abs(d1 - d2) < 0.001;
        } catch (NumberFormatException e) {
            return id1.equals(id2);
        }
    }

    private void showRegistrationForm() {
        removeAll();
        setTitle("Add Your Profile");

        Label title = new Label("Register Face");
        title.setUIID("Title");

        nameField = new TextField("", "Name", 20, TextArea.ANY);
        relationshipPicker = new ComboBox<>("Spouse", "Child", "Sibling", "Friend", "Caregiver");
        contextArea = new TextArea(5, 20);
        contextArea.setHint("Context (e.g., 'This is your daughter Sarah')");

        Button photoButton = new Button("Take Photo");
        photoButton.setMaterialIcon(FontImage.MATERIAL_CAMERA_ALT, 5);
        photoButton.addActionListener(e -> capturePhoto());

        imageLabel = new Label("No Image Selected");

        Button uploadButton = new Button("Upload Profile");
        uploadButton.setMaterialIcon(FontImage.MATERIAL_CLOUD_UPLOAD, 5);
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

        add(content);
        addLogoutButton();
        revalidate();
    }

    private void showMemberListView() {
        removeAll();
        setTitle("Family Group");

        add(new Label("Group Members"));

        memberListContainer = new Container(BoxLayout.y());
        add(memberListContainer);

        Button refreshButton = new Button("Refresh");
        refreshButton.addActionListener(e -> fetchMembers());
        add(refreshButton);

        Button editProfileButton = new Button("Edit My Profile");
        editProfileButton.setMaterialIcon(FontImage.MATERIAL_EDIT, 5);
        editProfileButton.addActionListener(e -> showEditProfileDialog());
        add(editProfileButton);

        addLogoutButton();

        show();
        fetchMembers();
    }

    private void fetchMembers() {
        Long groupId = MemoryLens.getFamilyGroupId();
        if (groupId == null) {
            return;
        }

        GenericNetworkService.getInstance().get("/api/group/" + groupId + "/member",
                new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
                    @Override
                    public void onSuccess(Map<String, Object> response) {
                        List<Map<String, Object>> list = extractMemberList(response);

                        Display.getInstance().callSerially(() -> {
                            memberListContainer.removeAll();
                            if (!list.isEmpty()) {
                                for (Map<String, Object> member : list) {
                                    addMemberRow(member);
                                }
                            } else {
                                memberListContainer.add(new Label("No members found."));
                            }
                            memberListContainer.revalidate();
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Display.getInstance().callSerially(() ->
                                ToastBar.showErrorMessage("Load failed: " + errorMessage)
                        );
                    }
                });
    }

    private void addMemberRow(Map<String, Object> member) {
        String name = (String) member.get("username");
        if (name == null) {
            name = (String) member.get("email");
        }

        Object roleObj = member.get("role");
        String roleName = "Unknown";
        String rStr = String.valueOf(roleObj);

        if ("0.0".equals(rStr) || "0".equals(rStr)) {
            roleName = "Admin";
        } else if ("1.0".equals(rStr) || "1".equals(rStr)) {
            roleName = "Patient";
        } else if ("2.0".equals(rStr) || "2".equals(rStr)) {
            roleName = "Family Member";
        }

        Container row = new Container(new com.codename1.ui.layouts.BorderLayout());
        row.setUIID("MemberRow");
        row.getAllStyles().setBorder(com.codename1.ui.plaf.Border.createLineBorder(1, 0xcccccc));
        row.getAllStyles().setPadding(2, 2, 2, 2);

        row.add(com.codename1.ui.layouts.BorderLayout.CENTER, new Label(name + " (" + roleName + ")"));
        memberListContainer.add(row);
    }

    private void showEditProfileDialog() {
        Form editForm = new Form("Edit Profile", BoxLayout.y());
        TextField editName = new TextField("", "Name", 20, TextArea.ANY);
        TextArea editContext = new TextArea(3, 20);
        Label imgReminder = new Label("Image: Already Registered (Face Embedding Set)");
        imgReminder.getAllStyles().setFgColor(0x008000);

        editForm.add(new Label("Name:"));
        editForm.add(editName);
        editForm.add(new Label("Context:"));
        editForm.add(editContext);
        editForm.add(imgReminder);

        Button saveBtn = new Button("Save Changes");
        saveBtn.addActionListener(e -> {
            String newName = editName.getText();
            String newContext = editContext.getText();

            if (newName.isEmpty()) {
                Dialog.show(TITLE_ERROR, "Name cannot be empty", "OK", null);
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
                                Dialog.show(TITLE_SUCCESS, "Profile Updated", "OK", null);
                                showMemberListView();
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Display.getInstance().callSerially(() ->
                                    Dialog.show(TITLE_ERROR, "Update failed: " + errorMessage, "OK", null)
                            );
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
        logoutButton.addActionListener(e -> MemoryLens.logout());
        add(logoutButton);
    }

    private void capturePhoto() {
        String filePath = Capture.capturePhoto(1024, -1);
        if (filePath == null) {
            return;
        }

        imagePath = filePath;
        try {
            Image img = Image.createImage(filePath);
            imageLabel.setIcon(img.scaled(300, 300));
            imageLabel.setText("");
            revalidate();
        } catch (IOException e) {
            ToastBar.showErrorMessage(TITLE_ERROR + ": " + e.getMessage());
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
            Dialog.show(TITLE_ERROR, "Session invalid. Login again.", "OK", null);
            return;
        }

        data.put("id", mId);
        data.put("groupId", gId);

        GenericNetworkService.getInstance().upload("/api/upload", imagePath, data,
                new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
                    @Override
                    public void onSuccess(Map<String, Object> response) {
                        Display.getInstance().callSerially(() -> {
                            Dialog.show(TITLE_SUCCESS, "Profile uploaded!", "OK", null);
                            MemoryLens.setHasEmbedding(true);
                            showMemberListView();
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Display.getInstance().callSerially(() ->
                                Dialog.show(TITLE_ERROR, errorMessage, "OK", null)
                        );
                    }
                });
    }
}
