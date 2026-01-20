package me.sebz.mu.pbl5;

import com.codename1.ui.Button;
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

    public PatientDashboard() {
        super("MemoryLens", new BorderLayout());

        Button uploadImageButton = new Button("Upload Image");
        uploadImageButton.setUIID("Button");
        uploadImageButton.setMaterialIcon(FontImage.MATERIAL_CLOUD_UPLOAD, 7);
        uploadImageButton.addActionListener(e -> uploadImage());

        Button logoutButton = new Button("Logout");
        logoutButton.setUIID("ButtonSecondary");
        logoutButton.addActionListener(e -> MemoryLens.logout());

        Container bottomContainer = new Container(new BorderLayout());
        bottomContainer.add(BorderLayout.CENTER, uploadImageButton);
        bottomContainer.add(BorderLayout.SOUTH, logoutButton);

        add(BorderLayout.CENTER, bottomContainer);
    }

    private void uploadImage() {
        Display.getInstance().openGallery(e -> {
            if (e == null || e.getSource() == null) {
                return;
            }

            String filePath = (String) e.getSource();

            Map<String, Object> params = new HashMap<>();
            Long groupId = MemoryLens.getFamilyGroupId();
            if (groupId == null) {
                Dialog.show(TITLE_ERROR, "Group ID is null. Please login again.", "OK", null);
                return;
            }
            params.put("groupId", groupId);

            GenericNetworkService.getInstance().upload("/api/recognize", filePath, params,
                    new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
                        @Override
                        public void onSuccess(Map<String, Object> response) {
                            String message = buildResultMessage(response);
                            Display.getInstance().callSerially(() ->
                                    Dialog.show("Result", message, "OK", null)
                            );
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Display.getInstance().callSerially(() ->
                                    Dialog.show(TITLE_ERROR, "Upload failed: " + errorMessage, "OK", null)
                            );
                        }
                    });
        }, Display.GALLERY_IMAGE);
    }

    private String buildResultMessage(Map<String, Object> response) {
        Object root = (response != null) ? response.get("root") : null;
        if (!(root instanceof java.util.List)) {
            return "Result: Desconocido";
        }

        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> list = (java.util.List<Map<String, Object>>) root;
        if (list.isEmpty()) {
            return "Result: Desconocido";
        }

        Map<String, Object> top = list.get(0);
        if (top == null) {
            return "Result: Desconocido";
        }

        Object sim = top.get("similarity");
        Double score = (sim instanceof Number) ? ((Number) sim).doubleValue() : null;

        if (score == null || score <= 0.4) {
            return "Result: Desconocido";
        }

        String name = decodeSafely((String) top.get("name"), "Desconocido");
        String context = decodeSafely((String) top.get("context"), "");

        if (context.isEmpty()) {
            return "This is " + name;
        }
        return "This is " + name + " (" + context + ")";
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
