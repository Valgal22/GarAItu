package me.sebz.mu.pbl5;

import com.codename1.components.ToastBar;
import com.codename1.media.Media;
import com.codename1.media.MediaManager;
import com.codename1.ui.Button;
import com.codename1.ui.CN1Constants;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.layouts.BorderLayout;

import java.util.HashMap;
import java.util.Map;

public class PatientDashboard extends Form {

    private static final String TITLE_ERROR = "Error";
    private static final String TITLE_RESULT = "Result";

    private static final String PREFIX_THIS_IS = "This is ";
    private static final String MSG_UNKNOWN_RESULT = "Result: Desconocido";

    public PatientDashboard() {
        super("MemoryLens", new BorderLayout());

        Button uploadImageButton = new Button("Upload Image");
        uploadImageButton.setUIID("Button");
        uploadImageButton.setMaterialIcon(FontImage.MATERIAL_CLOUD_UPLOAD, 7);
        uploadImageButton.addActionListener(e -> uploadImage());

        Button logoutButton = new Button("Logout");
        logoutButton.setUIID("ButtonSecondary");
        logoutButton.addActionListener(e -> MemoryLens.logout());

        Container container = new Container(new BorderLayout());
        container.add(BorderLayout.CENTER, uploadImageButton);
        container.add(BorderLayout.SOUTH, logoutButton);

        add(BorderLayout.CENTER, container);
    }

    private void uploadImage() {
        Display.getInstance().openGallery(e -> {
            if (e == null || e.getSource() == null) {
                Dialog.show("Info", "No image selected", "OK", null);
                return;
            }

            String filePath = (String) e.getSource();
            ToastBar.showInfoMessage("Uploading...");

            Long groupId = MemoryLens.getFamilyGroupId();
            if (groupId == null) {
                Dialog.show(TITLE_ERROR, "Group ID is null. Please login again.", "OK", null);
                return;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("groupId", groupId);

            GenericNetworkService.getInstance().upload("/api/recognize", filePath, params,
                    new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
                        @Override
                        public void onSuccess(Map<String, Object> response) {
                            Display.getInstance().callSerially(() -> {
                                if (response != null && response.containsKey("audioData")) {
                                    handleAudioResponse(response);
                                    return;
                                }
                                Dialog.show(TITLE_RESULT, buildResultMessage(response), "OK", null);
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Display.getInstance().callSerially(() ->
                                    Dialog.show(TITLE_ERROR, "Upload failed: " + errorMessage, "OK", null)
                            );
                        }
                    });
        }, CN1Constants.GALLERY_IMAGE);
    }

    private void handleAudioResponse(Map<String, Object> response) {
        byte[] audioData = decodeBase64Bytes(response.get("audioData"));

        String recognizedPerson = (String) response.get("recognizedPerson");
        String recognizedContext = (String) response.get("recognizedContext");

        String name = decodeSafely(recognizedPerson, "Desconocido");
        String context = decodeSafely(recognizedContext, "");

        if (audioData.length > 0) {
            try {
                Media m = MediaManager.createMedia(new java.io.ByteArrayInputStream(audioData), "audio/wav", () -> {});
                if (m != null) {
                    m.play();
                }
            } catch (Exception ignored) {
                // no debug logs
            }
        }

        String message = buildThisIsMessage(name, context);
        Dialog.show(TITLE_RESULT, message, "OK", null);
    }

    private String buildResultMessage(Map<String, Object> response) {
        Object root = (response != null) ? response.get("root") : null;
        if (!(root instanceof java.util.List)) {
            return MSG_UNKNOWN_RESULT;
        }

        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> list = (java.util.List<Map<String, Object>>) root;
        if (list.isEmpty()) {
            return MSG_UNKNOWN_RESULT;
        }

        Map<String, Object> top = list.get(0);
        if (top == null) {
            return MSG_UNKNOWN_RESULT;
        }

        Object sim = top.get("similarity");
        Double score = (sim instanceof Number) ? ((Number) sim).doubleValue() : null;

        if (score == null || score <= 0.4) {
            return MSG_UNKNOWN_RESULT;
        }

        String name = decodeSafely((String) top.get("name"), "Desconocido");
        String context = decodeSafely((String) top.get("context"), "");

        return buildThisIsMessage(name, context);
    }

    private String buildThisIsMessage(String name, String context) {
        String message = PREFIX_THIS_IS + name;
        if (context != null && !context.isEmpty()) {
            message += " (" + context + ")";
        }
        return message;
    }

    private byte[] decodeBase64Bytes(Object audioDataObj) {
        if (!(audioDataObj instanceof String)) {
            return new byte[0];
        }
        try {
            return java.util.Base64.getDecoder().decode((String) audioDataObj);
        } catch (Exception ignored) {
            return new byte[0];
        }
    }

    private String decodeSafely(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            if (value.contains("%")) {
                return com.codename1.io.Util.decode("UTF-8", value, false);
            }
            return value;
        } catch (Exception e) {
            return value;
        }
    }
}
