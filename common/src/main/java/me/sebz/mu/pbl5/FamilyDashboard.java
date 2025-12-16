package me.sebz.mu.pbl5;

import com.codename1.capture.Capture;
import com.codename1.components.ToastBar;
import com.codename1.ui.*;
import com.codename1.ui.layouts.BoxLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FamilyDashboard extends Form {

    private TextField nameField;
    private ComboBox<String> relationshipPicker;
    private TextArea contextArea;
    private Label imageLabel;
    private String imagePath;

    public FamilyDashboard() {
        super("Family Dashboard", BoxLayout.y());
        this.setScrollableY(true);

        Label title = new Label("Add New Memory");
        title.setUIID("Title"); // Use the Title style defined in CSS

        nameField = new TextField("", "Name", 20, TextField.ANY);
        nameField.setUIID("TextField");

        relationshipPicker = new ComboBox<>("Spouse", "Child", "Sibling", "Friend", "Caregiver");
        // ComboBox styling is a bit more complex in CN1 CSS, but standard should look
        // okay with the theme.

        contextArea = new TextArea(5, 20);
        contextArea.setHint("Memory Context (e.g., 'This is your daughter Sarah')");
        contextArea.setUIID("TextField"); // Reuse TextField style for consistency

        Button photoButton = new Button("Take Photo");
        photoButton.setMaterialIcon(FontImage.MATERIAL_CAMERA_ALT, 5);
        photoButton.addActionListener(e -> capturePhoto());

        imageLabel = new Label("No Image Selected");
        imageLabel.getAllStyles().setAlignment(Component.CENTER);
        imageLabel.getAllStyles().setPadding(2, 2, 2, 2);

        Button uploadButton = new Button("Upload Memory");
        uploadButton.setMaterialIcon(FontImage.MATERIAL_CLOUD_UPLOAD, 5);
        uploadButton.addActionListener(e -> uploadData());

        Container content = new Container(BoxLayout.y());
        content.setUIID("Form"); // Ensure it picks up form background
        content.getAllStyles().setPadding(5, 5, 5, 5); // Add padding around the content

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

        Button logoutButton = new Button("Logout");
        logoutButton.setUIID("ButtonSecondary");
        logoutButton.addActionListener(e -> MemoryLens.logout());
        content.add(logoutButton);

        this.add(content);
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
                ToastBar.showErrorMessage("Error loading image: " + e.getMessage());
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

        GenericNetworkService.getInstance().upload("/api/upload", imagePath, data,
                new GenericNetworkService.NetworkCallback() {
                    @Override
                    public void onSuccess(Map<String, Object> response) {
                        Display.getInstance().callSerially(() -> {
                            Dialog.show("Success", "Memory uploaded successfully!", "OK", null);
                            nameField.clear();
                            contextArea.setText("");
                            imageLabel.setIcon(null);
                            imageLabel.setText("No Image Selected");
                            imagePath = null;
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Display.getInstance().callSerially(() -> {
                            Dialog.show("Error", "Upload failed: " + errorMessage, "OK", null);
                        });
                    }
                });
    }
}
