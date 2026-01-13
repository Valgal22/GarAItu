package me.sebz.mu.pbl5;

import com.codename1.capture.Capture;
import com.codename1.components.ToastBar;
import com.codename1.ui.*;
import com.codename1.ui.layouts.BorderLayout;

import java.util.HashMap;
import java.util.Map;

import me.sebz.mu.pbl5.logic.FaceResultUtil;

public class PatientDashboard extends Form {

    public PatientDashboard() {
        super("MemoryLens", new BorderLayout());

        Button scanFaceButton = new Button("Scan Face");
        scanFaceButton.setUIID("Button");
        scanFaceButton.setMaterialIcon(FontImage.MATERIAL_CAMERA_ALT, 7);
        scanFaceButton.setName("pat_scan");
        scanFaceButton.addActionListener(e -> scanFace());

        Button voiceQueryButton = new Button("Voice Query");
        voiceQueryButton.setUIID("ButtonSecondary");
        voiceQueryButton.setMaterialIcon(FontImage.MATERIAL_MIC, 7);
        voiceQueryButton.setName("pat_voice");
        voiceQueryButton.addActionListener(e -> recordVoice());

        this.add(BorderLayout.CENTER, scanFaceButton);

        Button uploadImageButton = new Button("Upload Image DEBUG");
        uploadImageButton.setUIID("ButtonSecondary");
        uploadImageButton.setMaterialIcon(FontImage.MATERIAL_CLOUD_UPLOAD, 7);
        uploadImageButton.setName("pat_upload");
        uploadImageButton.addActionListener(e -> uploadImage());

        Container bottomContainer = new Container(new BorderLayout());
        bottomContainer.add(BorderLayout.CENTER, voiceQueryButton);
        bottomContainer.add(BorderLayout.NORTH, uploadImageButton);

        Button logoutButton = new Button("Logout");
        logoutButton.setUIID("ButtonSecondary");
        logoutButton.setName("pat_logout");
        logoutButton.addActionListener(e -> MemoryLens.logout());
        bottomContainer.add(BorderLayout.SOUTH, logoutButton);

        this.add(BorderLayout.SOUTH, bottomContainer);
    }

    private void scanFace() {
        String filePath = Capture.capturePhoto(1024, -1);
        if (filePath != null) {
            ToastBar.showInfoMessage("Analyzing...");

            GenericNetworkService.getInstance().upload("/api/recognize", filePath,
                    new HashMap<>(),
                    new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
                        @Override
                        public void onSuccess(Map<String, Object> response) {
                            String message = FaceResultUtil.buildMessageFromResponse(response, 0.4);
                            Display.getInstance().callSerially(() -> Dialog.show("Result", message, "OK", null));
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Display.getInstance().callSerially(() -> Dialog.show("Error", "Could not recognize face: " + errorMessage, "OK", null));
                        }
                    });
        }
    }

    private void uploadImage() {
        Dialog.show("Debug", "Upload Button Clicked. Opening Gallery...", "OK", null);

        Display.getInstance().openGallery(e -> {
            if (e != null && e.getSource() != null) {
                String filePath = (String) e.getSource();

                ToastBar.showInfoMessage("Uploading...");

                Map<String, Object> params = new HashMap<>();
                Long groupId = MemoryLens.getFamilyGroupId();
                if (groupId != null) params.put("groupId", groupId);
                else {
                    Dialog.show("Error", "Group ID is null. Please login again.", "OK", null);
                    return;
                }

                GenericNetworkService.getInstance().upload("/api/recognize", filePath,
                        params,
                        new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
                            @Override
                            public void onSuccess(Map<String, Object> response) {
                                String message = FaceResultUtil.buildMessageFromResponse(response, 0.4);
                                Display.getInstance().callSerially(() -> Dialog.show("Result", message, "OK", null));
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Display.getInstance().callSerially(() -> Dialog.show("Error", "Upload failed: " + errorMessage, "OK", null));
                            }
                        });
            } else {
                Dialog.show("Info", "No image selected", "OK", null);
            }
        }, Display.GALLERY_IMAGE);
    }

    private void recordVoice() {
        ToastBar.showInfoMessage("Recording... (Simulated)");
        Dialog.show("Voice Query", "Recording audio and sending to Node-RED...", "OK", null);
    }
}
