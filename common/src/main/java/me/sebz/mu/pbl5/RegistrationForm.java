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

        String[] roles = { "Caregiver (Admin)", "Patient", "Family Member" };
        ComboBox<String> rolePicker = new ComboBox<>((Object[]) roles);
        rolePicker.setUIID("TextField");

        TextField contextField = new TextField("", "Context (e.g. Who are you?)", 20, TextField.ANY);
        contextField.setUIID("TextField");

        TextField chatId = new TextField("", "Telegram Chat ID (Optional)", 20, TextField.ANY);
        chatId.setUIID("TextField");

        Button helpButton = new Button("?");
        helpButton.setUIID("ButtonSecondary");
        helpButton.addActionListener(e -> {
            Dialog.show("Telegram ID help",
                    "Search for '@userinfobot' on Telegram.\n" +
                            "Click 'Start'.\n" +
                            "Copy the ID number it gives you and paste it here.",
                    "OK", null);
        });

        Button registerButton = new Button("Register");
        registerButton.setMaterialIcon(FontImage.MATERIAL_PERSON_ADD, 5);

        registerButton.addActionListener(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String context = contextField.getText();
            String cId = chatId.getText();
            int roleIdx = rolePicker.getSelectedIndex();
            short role = (short) roleIdx;

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Dialog.show("Error", "Name, Email and Password are required", "OK", null);
                return;
            }

            Map<String, Object> regData = new HashMap<>();
            regData.put("name", name);
            regData.put("email", email);
            regData.put("password", password);
            regData.put("context", context);
            regData.put("role", role);
            regData.put("chatId", cId);

            GenericNetworkService.getInstance().post("/api/auth/register", regData,
                    new GenericNetworkService.NetworkCallback() {
                        @Override
                        public void onSuccess(Map<String, Object> response) {
                            Display.getInstance().callSerially(() -> {
                                Dialog.show("Success", "Account created! Please login to join a group.", "OK", null);
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
        center.add(new Label("Name:"));
        center.add(nameField);
        center.add(new Label("Email:"));
        center.add(emailField);
        center.add(new Label("Password:"));
        center.add(passwordField);
        center.add(new Label("Role:"));
        center.add(rolePicker);
        center.add(new Label("Context (optional):"));
        center.add(contextField);

        Container chatIdCnt = BorderLayout.center(chatId).add(BorderLayout.EAST, helpButton);
        center.add(new Label("Telegram Chat ID (optional):"));
        center.add(chatIdCnt);

        center.add(registerButton);
        center.add(backButton);

        this.add(BorderLayout.CENTER, center);
    }
}
