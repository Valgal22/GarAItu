package me.sebz.mu.pbl5;

import com.codename1.ui.Button;
import com.codename1.ui.ComboBox;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import me.sebz.mu.pbl5.core.auth.AuthGatewayNodeRed;
import me.sebz.mu.pbl5.core.auth.AuthUseCase;
import me.sebz.mu.pbl5.core.auth.RegisterRequest;

public class RegistrationForm extends Form {

    private static final String UIID_TEXT_FIELD = "TextField";

    private final AuthUseCase authUseCase =
            new AuthUseCase(new AuthGatewayNodeRed(GenericNetworkService.getInstance()));

    public RegistrationForm() {
        super("Register", new BorderLayout());
        setName("registerForm");

        Container center = new Container(BoxLayout.y());
        center.setName("registerCenter");
        center.setScrollableY(true);
        center.getAllStyles().setPadding(10, 10, 10, 10);

        Label title = new Label("Join MemoryLens");
        title.setName("registerTitle");
        title.setUIID("Title");
        title.getAllStyles().setAlignment(Component.CENTER);

        TextField nameField = new TextField("", "Full Name", 20, TextArea.ANY);
        nameField.setName("registerName");
        nameField.setUIID(UIID_TEXT_FIELD);

        TextField emailField = new TextField("", "Email", 20, TextArea.EMAILADDR);
        emailField.setName("registerEmail");
        emailField.setUIID(UIID_TEXT_FIELD);

        TextField passwordField = new TextField("", "Password", 20, TextArea.PASSWORD);
        passwordField.setName("registerPassword");
        passwordField.setUIID(UIID_TEXT_FIELD);

        String[] roles = { "Caregiver (Admin)", "Patient", "Family Member" };
        ComboBox<String> rolePicker = new ComboBox<>((Object[]) roles);
        rolePicker.setName("registerRole");
        rolePicker.setUIID(UIID_TEXT_FIELD);

        TextField contextField = new TextField("", "Context (e.g. Who are you?)", 20, TextArea.ANY);
        contextField.setName("registerContext");
        contextField.setUIID(UIID_TEXT_FIELD);

        TextField chatId = new TextField("", "Telegram Chat ID (Optional)", 20, TextArea.ANY);
        chatId.setName("registerChatId");
        chatId.setUIID(UIID_TEXT_FIELD);

        Button helpButton = new Button("?");
        helpButton.setName("registerChatHelpBtn");
        helpButton.setUIID("ButtonSecondary");
        helpButton.addActionListener(e ->
                Dialog.show("Telegram ID help",
                        "Search for '@userinfobot' on Telegram.\n" +
                                "Click 'Start'.\n" +
                                "Copy the ID number it gives you and paste it here.",
                        "OK", null)
        );

        Button registerButton = new Button("Register");
        registerButton.setName("registerBtn");
        registerButton.setMaterialIcon(FontImage.MATERIAL_PERSON_ADD, 5);

        registerButton.addActionListener(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String context = contextField.getText();
            String cId = chatId.getText();

            short role = (short) rolePicker.getSelectedIndex();

            registerButton.setEnabled(false);

            RegisterRequest req = new RegisterRequest(name, email, password, context, role, cId);
            authUseCase.register(req, result -> {
                Display.getInstance().callSerially(() -> {
                    registerButton.setEnabled(true);

                    if (!result.isOk()) {
                        Dialog.show("Error", "Registration failed: " + result.getError(), "OK", null);
                        return;
                    }

                    Dialog.show("Success", "Account created! Please login to join a group.", "OK", null);
                    MemoryLens.showLoginScreen();
                });
            });
        });

        Button backButton = new Button("Back to Login");
        backButton.setName("backToLoginBtn");
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

        add(BorderLayout.CENTER, center);
    }
}
