package me.sebz.mu.pbl5;

import static com.codename1.ui.CN.*;
import com.codename1.system.Lifecycle;
import com.codename1.ui.*;
import com.codename1.ui.layouts.*;

import me.sebz.mu.pbl5.logic.RoleUtil;
import me.sebz.mu.pbl5.services.AuthService;

public class MemoryLens extends Lifecycle {
    private static String sessionToken;
    private static Long memberId;
    private static Long familyGroupId;
    private static String userRole;
    private static boolean hasEmbedding;

    public static boolean hasEmbedding() { return hasEmbedding; }
    public static void setHasEmbedding(boolean value) { hasEmbedding = value; }

    public static String getSessionToken() { return sessionToken; }
    public static void setSessionToken(String token) { sessionToken = token; }

    public static Long getMemberId() { return memberId; }
    public static void setMemberId(Long id) { memberId = id; }

    public static Long getFamilyGroupId() { return familyGroupId; }
    public static void setFamilyGroupId(Long id) { familyGroupId = id; }

    public static String getUserRole() { return userRole; }
    public static void setUserRole(String role) { userRole = role; }

    public static void navigateToAppropriateDashboard() {
        Display.getInstance().callSerially(() -> {
            if (familyGroupId == null) {
                new GroupOnboardingForm().show();
                return;
            }

            String roleShort = userRole != null ? userRole : "2";
            if (RoleUtil.isAdmin(roleShort)) new AdminDashboard().show();
            else if (RoleUtil.isPatient(roleShort)) new PatientDashboard().show();
            else new FamilyDashboard().show();
        });
    }

    @Override
    public void runApp() {
        showLoginScreen();
    }

    public static void logout() {
        showLoginScreen();
    }

    public static void showLoginScreen() {
        Form loginForm = new Form("Login", new BorderLayout());
        AuthService authService = new AuthService(GenericNetworkService.getInstance());

        Container center = new Container(BoxLayout.y());
        center.setScrollableY(true);
        center.getAllStyles().setPadding(10, 10, 10, 10);

        Label title = new Label("MemoryLens");
        title.setUIID("Title");
        title.getAllStyles().setAlignment(Component.CENTER);
        title.getAllStyles().setMarginBottom(10);

        TextField emailField = new TextField("", "Email", 20, TextField.EMAILADDR);
        emailField.setUIID("TextField");
        emailField.setName("login_email");

        TextField passwordField = new TextField("", "Password", 20, TextField.PASSWORD);
        passwordField.setUIID("TextField");
        passwordField.setName("login_password");

        Button loginButton = new Button("Login");
        loginButton.setMaterialIcon(FontImage.MATERIAL_LOGIN, 5);
        loginButton.setName("login_submit");

        Button registerButton = new Button("Register");
        registerButton.setUIID("ButtonSecondary");
        registerButton.setMaterialIcon(FontImage.MATERIAL_PERSON_ADD, 5);
        registerButton.setName("login_register");
        registerButton.addActionListener(e -> new RegistrationForm().show());

        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();

            authService.login(email, password, new AuthService.LoginCallback() {
                @Override
                public void onSuccess(String token, String role, Long fgId, Long mId, boolean hasEmb) {
                    MemoryLens.setSessionToken(token);
                    MemoryLens.setFamilyGroupId(fgId);
                    MemoryLens.setMemberId(mId);
                    MemoryLens.setUserRole(role);
                    MemoryLens.setHasEmbedding(hasEmb);
                    MemoryLens.navigateToAppropriateDashboard();
                }

                @Override
                public void onFailure(String error) {
                    Display.getInstance().callSerially(() -> Dialog.show("Login Failed", error, "OK", null));
                }
            });
        });

        center.add(title);
        center.add(new Label("Welcome Back"));
        center.add(emailField);
        center.add(passwordField);
        center.add(loginButton);
        center.add(registerButton);

        loginForm.add(BorderLayout.CENTER, center);
        loginForm.show();
    }
}
