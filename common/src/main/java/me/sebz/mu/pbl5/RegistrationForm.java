package me.sebz.mu.pbl5;

import com.codename1.ui.*;
import com.codename1.ui.layouts.*;
import java.util.HashMap;
import java.util.Map;

public class RegistrationForm extends Form {

    public RegistrationForm() {
        super("Register", new BorderLayout());

        Container center = new Container(BoxLayout.y());
        center.setScrollableY(true);
        center.getAllStyles().setPadding(10, 10, 10, 10);

        Label title = new Label("Join MemoryLens");
        title.setUIID("Title");
        title.getAllStyles().setAlignment(Component.CENTER);

        TextField nameField = new TextField("", "Full Name", 20, TextField.ANY);
        nameField.setUIID("TextField");

        TextField emailField = new TextField("", "Email", 20, TextField.EMAILADDR);
        emailField.setUIID("TextField");

        TextField passwordField = new TextField("", "Password", 20, TextField.PASSWORD);
        passwordField.setUIID("TextField");

        TextField contextField = new TextField("", "Context (e.g. Who are you?)", 20, TextField.ANY);
        contextField.setUIID("TextField");

        TextField inviteCodeField = new TextField("", "Invite Code", 20, TextField.ANY);
        inviteCodeField.setUIID("TextField");

        Button registerButton = new Button("Register");
        registerButton.setMaterialIcon(FontImage.MATERIAL_PERSON_ADD, 5);

        registerButton.addActionListener(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String context = contextField.getText();
            String inviteCode = inviteCodeField.getText();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || inviteCode.isEmpty()) {
                Dialog.show("Error", "Name, Email, Password and Invite Code are required", "OK", null);
                return;
            }

            Map<String, Object> regData = new HashMap<>();
            regData.put("name", name);
            regData.put("email", email);
            regData.put("password", password);
            regData.put("context", context);
            regData.put("inviteCode", inviteCode);

            GenericNetworkService.getInstance().post("/api/auth/register", regData,
                    new GenericNetworkService.NetworkCallback() {
                        @Override
                        public void onSuccess(Map<String, Object> response) {
                            Display.getInstance().callSerially(() -> {
                                Dialog.show("Success", "Account created! You can now login.", "OK", null);
                                MemoryLens.showLoginScreen();
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Display.getInstance().callSerially(() -> {
                                Dialog.show("Error", "Registration failed: " + errorMessage, "OK", null);
                            });
                        }
                    });
        });

        Button backButton = new Button("Back to Login");
        backButton.setUIID("ButtonSecondary");
        backButton.addActionListener(e -> MemoryLens.showLoginScreen());

        center.add(title);
        center.add(nameField);
        center.add(emailField);
        center.add(passwordField);
        center.add(contextField);
        center.add(inviteCodeField);
        center.add(registerButton);
        center.add(backButton);

        this.add(BorderLayout.CENTER, center);
    }
}
