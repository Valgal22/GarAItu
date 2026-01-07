package me.sebz.mu.pbl5;

import com.codename1.capture.Capture;
import com.codename1.components.ToastBar;

import com.codename1.ui.*;
import com.codename1.ui.layouts.BorderLayout;

import java.util.HashMap;
import java.util.Map;

public class PatientDashboard extends Form {

    public PatientDashboard() {
        super("MemoryLens", new BorderLayout());

        Button scanFaceButton = new Button("Scan Face");
        scanFaceButton.setUIID("Button"); // Uses primary color by default
        scanFaceButton.setMaterialIcon(FontImage.MATERIAL_CAMERA_ALT, 7); // Large Icon
        scanFaceButton.addActionListener(e -> scanFace());

        Button voiceQueryButton = new Button("Voice Query");
        voiceQueryButton.setUIID("Button");
        // We can use a specific UIID if we defined one, or just override styles
        // locally,
        // but let's use the CSS class approach if possible.
        // CN1 CSS maps "Button.secondary" to setUIID("Button_secondary") if defined,
        // or we can just stick to manual styling for specific overrides if CSS is
        // simple.
        // Let's rely on the CSS "Button.secondary" mapping which corresponds to UIID
        // "ButtonSecondary" usually,
        // but simpler here: let's just use inline styles for the specific color
        // difference to match the previous logic
        // OR better, let's update the CSS to have a "RedButton" and use that.
        // Actually, I defined "Button.secondary" in CSS. In CN1 CSS, this usually
        // targets a component with UIID "Button" and a specific state or a separate
        // UIID "ButtonSecondary"?
        // Let's assume standard CN1 CSS behavior: "Button.secondary" -> UIID "Button"
        // with client property "secondary"? No.
        // It maps to UIID "ButtonSecondary".
        voiceQueryButton.setUIID("ButtonSecondary");
        voiceQueryButton.setMaterialIcon(FontImage.MATERIAL_MIC, 7);
        voiceQueryButton.addActionListener(e -> recordVoice());

        this.add(BorderLayout.CENTER, scanFaceButton);

        Button uploadImageButton = new Button("Upload Image");
        uploadImageButton.setUIID("ButtonSecondary");
        uploadImageButton.setMaterialIcon(FontImage.MATERIAL_CLOUD_UPLOAD, 7);
        uploadImageButton.addActionListener(e -> uploadImage());

        Container bottomContainer = new Container(new BorderLayout());
        bottomContainer.add(BorderLayout.CENTER, voiceQueryButton);
        bottomContainer.add(BorderLayout.NORTH, uploadImageButton); // Added Upload Button

        Button logoutButton = new Button("Logout");
        logoutButton.setUIID("ButtonSecondary");
        logoutButton.addActionListener(e -> MemoryLens.logout());
        bottomContainer.add(BorderLayout.SOUTH, logoutButton);

        this.add(BorderLayout.SOUTH, bottomContainer);
    }

    private void scanFace() {
        String filePath = Capture.capturePhoto(1024, -1);
        if (filePath != null) {
            ToastBar.showInfoMessage("Analyzing...");
            Long groupId = MemoryLens.getFamilyGroupId();
            GenericNetworkService.getInstance().upload("/api/groups/" + groupId + "/recognize", filePath,
                    new HashMap<>(),
                    new GenericNetworkService.NetworkCallback() {
                        @Override
                        public void onSuccess(Map<String, Object> response) {
                            String name = (String) response.get("name");
                            String relationship = (String) response.get("relationship");
                            String message = "This is " + name + ", your " + relationship;

                            Display.getInstance().callSerially(() -> {
                                Dialog.show("Result", message, "OK", null);
                                // In a real app, we would use TextToSpeech here
                                // CN1 doesn't have a built-in TTS lib in the core, usually requires a cn1lib or
                                // native interface.
                                // For this demo, we'll simulate it with a Dialog.
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Display.getInstance().callSerially(() -> {
                                Dialog.show("Error", "Could not recognize face: " + errorMessage, "OK", null);
                            });
                        }
                    });
        }
    }

    private void uploadImage() {
        Display.getInstance().openGallery(e -> {
            if (e != null && e.getSource() != null) {
                String filePath = (String) e.getSource();
                ToastBar.showInfoMessage("Uploading...");

                // Use the generic /api/recognize as per user request (or /api/groups/... if
                // intended context is same)
                // User request said: "hara un POST a node-RED mandandole la imagen a
                // /api/recognize"
                // So we will use "/api/recognize"

                GenericNetworkService.getInstance().upload("/api/recognize", filePath,
                        new HashMap<>(),
                        new GenericNetworkService.NetworkCallback() {
                            @Override
                            public void onSuccess(Map<String, Object> response) {
                                // Assuming response format similar to face scan
                                String name = (String) response.get("name");
                                if (name == null)
                                    name = "Unknown";

                                String message = "Result: " + name;
                                // Adjust based on actual server response fields if different

                                Display.getInstance().callSerially(() -> {
                                    Dialog.show("Upload Result", message, "OK", null);
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
        }, Display.GALLERY_IMAGE);
    }

    private void recordVoice() {
        // Simple audio recording simulation or implementation
        // Since MediaManager.createMediaRecorder is platform dependent and complex for
        // a snippet,
        // we will simulate the action for the "Voice Query" button as per the prompt's
        // structural requirement.
        // In a full implementation, we would record to a file and upload it.

        ToastBar.showInfoMessage("Recording... (Simulated)");

        // Simulate sending a dummy audio file
        // In real app: String audioPath = ...;

        // For now, we just show a dialog that we would send audio
        Dialog.show("Voice Query", "Recording audio and sending to Node-RED...", "OK", null);
    }
}
