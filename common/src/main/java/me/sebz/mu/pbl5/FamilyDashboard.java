package me.sebz.mu.pbl5;

import com.codename1.capture.Capture;
import com.codename1.components.ToastBar;
import com.codename1.ui.*;
import com.codename1.ui.layouts.BoxLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FamilyDashboard extends Form {

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
        this.setScrollableY(true);
        
        // Initial Loading UI
        this.add(new Label("Loading Profile..."));
        
        // Fetch fresh status from server
        checkEmbeddingStatus();
    }

    private void checkEmbeddingStatus() {
        Long groupId = MemoryLens.getFamilyGroupId();
        Long memberId = MemoryLens.getMemberId(); // Logged in user

        if (groupId == null || memberId == null) {
             // Fallback if missing IDs (shouldn't happen on fresh login)
            showRegistrationForm();
            return;
        }

        GenericNetworkService.getInstance().get("/api/group/" + groupId + "/member",
            new GenericNetworkService.NetworkCallback() {
                @Override
                public void onSuccess(Map<String, Object> response) {
                    java.util.List<Map<String, Object>> list;
                    if (response.containsKey("root")) {
                        list = (java.util.List<Map<String, Object>>) response.get("root");
                    } else {
                        list = null; 
                    }
                    
                    boolean foundSelf = false;
                    boolean myEmbeddingStatus = false;

                    if (list != null) {
                        for (Map<String, Object> member : list) {
                            // Check if this is me
                            Object mIdObj = member.get("id");
                            String mIdStr = String.valueOf(mIdObj);
                            
                            // Safe compare (handle "9.0" vs "9")
                            if (areIdsEqual(mIdStr, String.valueOf(memberId))) {
                                foundSelf = true;
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
                        if (finalStatus) {
                            showMemberListView();
                        } else {
                            showRegistrationForm();
                        }
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    Display.getInstance().callSerially(() -> {
                        ToastBar.showErrorMessage("Failed to check status: " + errorMessage);
                        // Fallback
                        showRegistrationForm();
                    });
                }
            });
    }

    private boolean areIdsEqual(String id1, String id2) {
        if (id1 == null || id2 == null) return false;
        try {
            double d1 = Double.parseDouble(id1);
            double d2 = Double.parseDouble(id2);
            return Math.abs(d1 - d2) < 0.001; 
        } catch (NumberFormatException e) {
            return id1.equals(id2);
        }
    }

    /*
     * ==========================
     * VIEW 1: REGISTRATION
     * ==========================
     */
    private void showRegistrationForm() {
        this.removeAll();
        this.setTitle("Add Your Profile");

        Label title = new Label("Register Face");
        title.setUIID("Title");

        nameField = new TextField("", "Name", 20, TextField.ANY);
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

        this.add(content);
        addLogoutButton();
        this.revalidate();
    }

    /*
     * ==========================
     * VIEW 2: MEMBER LIST (Read-Only) + EDIT
     * ==========================
     */
    private void showMemberListView() {
        this.removeAll();
        this.setTitle("Family Group");

        this.add(new Label("Group Members"));

        memberListContainer = new Container(BoxLayout.y());
        this.add(memberListContainer);

        Button refreshButton = new Button("Refresh");
        refreshButton.addActionListener(e -> fetchMembers());
        this.add(refreshButton);

        Button editProfileButton = new Button("Edit My Profile");
        editProfileButton.setMaterialIcon(FontImage.MATERIAL_EDIT, 5);
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
            new GenericNetworkService.NetworkCallback() {
                @Override
                public void onSuccess(Map<String, Object> response) {
                    java.util.List<Map<String, Object>> list;
                    if (response.containsKey("root")) {
                        list = (java.util.List<Map<String, Object>>) response.get("root");
                    } else {
                        list = null; 
                    }

                    Display.getInstance().callSerially(() -> {
                        memberListContainer.removeAll();
                        if (list != null) {
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
                    Display.getInstance().callSerially(() -> ToastBar.showErrorMessage("Load failed: " + errorMessage));
                }
            });
    }

    private void addMemberRow(Map<String, Object> member) {
        String name = (String) member.get("username");
        if (name == null) name = (String) member.get("email");
        
        Object roleObj = member.get("role");
        String roleName = "Unknown";
        String rStr = String.valueOf(roleObj);
        
        if ("0.0".equals(rStr) || "0".equals(rStr)) roleName = "Admin";
        else if ("1.0".equals(rStr) || "1".equals(rStr)) roleName = "Patient";
        else if ("2.0".equals(rStr) || "2".equals(rStr)) roleName = "Family Member";

        Container row = new Container(new com.codename1.ui.layouts.BorderLayout());
        row.setUIID("MemberRow");
        row.getAllStyles().setBorder(com.codename1.ui.plaf.Border.createLineBorder(1, 0xcccccc));
        row.getAllStyles().setPadding(2,2,2,2);
        
        row.add(com.codename1.ui.layouts.BorderLayout.CENTER, new Label(name + " (" + roleName + ")"));
        memberListContainer.add(row);
    }

    private void showEditProfileDialog() {
        Form editForm = new Form("Edit Profile", BoxLayout.y());
        TextField editName = new TextField("", "Name", 20, TextField.ANY);
        TextArea editContext = new TextArea(3, 20);
        Label imgReminder = new Label("Image: Already Registered (Face Embedding Set)");
        imgReminder.getAllStyles().setFgColor(0x008000); // Green

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
                 Dialog.show("Error", "Name cannot be empty", "OK", null);
                 return;
            }

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("name", newName);
            updateData.put("context", newContext);
            // Relationship is removed as requested by user
            // groupId is removed as requested by user (only name and context)

            GenericNetworkService.getInstance().put("/garAItu/member/" + MemoryLens.getMemberId(), updateData,
                new GenericNetworkService.NetworkCallback() {
                    @Override
                    public void onSuccess(Map<String, Object> response) {
                         Display.getInstance().callSerially(() -> {
                             Dialog.show("Success", "Profile Updated", "OK", null);
                             showMemberListView(); 
                         });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Display.getInstance().callSerially(() -> {
                            Dialog.show("Error", "Update failed: " + errorMessage, "OK", null);
                        });
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
                new GenericNetworkService.NetworkCallback() {
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
