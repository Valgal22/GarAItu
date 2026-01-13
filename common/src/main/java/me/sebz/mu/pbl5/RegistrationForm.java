package me.sebz.mu.pbl5;

import com.codename1.ui.*;
import com.codename1.ui.layouts.*;
import me.sebz.mu.pbl5.services.AuthService;

public class RegistrationForm extends Form {

    private final AuthService authService;

    public RegistrationForm() {
        this(new AuthService(GenericNetworkService.getInstance()));
    }

    // Para tests puedes inyectar un AuthService fake
    public RegistrationForm(AuthService authService) {
        super("Register", new BorderLayout());
        this.authService = authService;

        Container center = new Container(BoxLayout.y());
        center.setScrollableY(true);
        center.getAllStyles().setPadding(10, 10, 10, 10);

        Label title = new Label("Join MemoryLens");
        title.setUIID("Title");
        title.getAllStyles().setAlignment(Component.CENTER);

        TextField nameField = new TextField("", "Full Name", 20, TextField.ANY);
        nameField.setUIID("TextField");
        nameField.setName("reg_name");

        TextField emailField = new TextField("", "Email", 20, TextField.EMAILADDR);
        emailField.setUIID("TextField");
        emailField.setName("reg_email");

        TextField passwordField = new TextField("", "Password", 20, TextField.PASSWORD);
        passwordField.setUIID("TextField");
        passwordField.setName("reg_password");

        String[] roles = { "Caregiver (Admin)", "Patient", "Family Member" };
        ComboBox<String> rolePicker = new ComboBox<>((Object[]) roles);
        rolePicker.setUIID("TextField");
        rolePicker.setName("reg_role");

        TextField contextField = new TextField("", "Context (e.g. Who are you?)", 20, TextField.ANY);
        contextField.setUIID("TextField");
        contextField.setName("reg_context");

        Button registerButton = new Button("Register");
        registerButton.setMaterialIcon(FontImage.MATERIAL_PERSON_ADD, 5);
        registerButton.setName("reg_submit");

        registerButton.addActionListener(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String context = contextField.getText();
            short role = (short) rolePicker.getSelectedIndex();

            authService.register(name, email, password, context, role, new AuthService.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Display.getInstance().callSerially(() -> {
                        Dialog.show("Success", "Account created! Please login to join a group.", "OK", null);
                        MemoryLens.showLoginScreen();
                    });
                }

                @Override
                public void onFailure(String error) {
                    Display.getInstance().callSerially(() -> Dialog.show("Error", "Registration failed: " + error, "OK", null));
                }
            });
        });

        Button backButton = new Button("Back to Login");
        backButton.setUIID("ButtonSecondary");
        backButton.setName("reg_back");
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
        center.add(registerButton);
        center.add(backButton);

        this.add(BorderLayout.CENTER, center);
    }
}
