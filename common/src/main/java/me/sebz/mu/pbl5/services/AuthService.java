package me.sebz.mu.pbl5.services;

import java.util.HashMap;
import java.util.Map;

import me.sebz.mu.pbl5.logic.Validation;
import me.sebz.mu.pbl5.net.NetworkClient;

public class AuthService {

    public interface LoginCallback {
        void onSuccess(String sessionToken, String role, Long familyGroupId, Long memberId, boolean hasEmbedding);
        void onFailure(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(String error);
    }

    private final NetworkClient net;

    public AuthService(NetworkClient net) {
        this.net = net;
    }

    public void register(String name, String email, String password, String context, short role, SimpleCallback cb) {
        if (Validation.isBlank(name) || Validation.isBlank(email) || Validation.isBlank(password)) {
            cb.onFailure("Name, Email and Password are required");
            return;
        }
        if (!Validation.isValidEmail(email)) {
            cb.onFailure("Please enter a valid email address");
            return;
        }

        Map<String, Object> regData = new HashMap<>();
        regData.put("name", name);
        regData.put("email", email);
        regData.put("password", password);
        regData.put("context", context);
        regData.put("role", role);

        net.post("/api/auth/register", regData, new NetworkClient.Callback() {
            @Override public void onSuccess(Map<String, Object> response) { cb.onSuccess(); }
            @Override public void onFailure(String errorMessage) { cb.onFailure(errorMessage); }
        });
    }

    public void login(String email, String password, LoginCallback cb) {
        if (Validation.isBlank(email) || Validation.isBlank(password)) {
            cb.onFailure("Please fill all fields");
            return;
        }
        if (!Validation.isValidEmail(email)) {
            cb.onFailure("Please enter a valid email address");
            return;
        }

        Map<String, Object> loginData = new HashMap<>();
        loginData.put("email", email);
        loginData.put("password", password);

        net.post("/api/auth/login", loginData, new NetworkClient.Callback() {
            @Override
            public void onSuccess(Map<String, Object> response) {
                String role = String.valueOf(response.get("role"));
                String token = (String) response.get("session");

                Long fgId = toLong(response.get("familyGroupId"));
                Long mId = toLong(response.get("memberId"));

                boolean hasEmb = false;
                if (response.containsKey("hasEmbedding")) {
                    Object v = response.get("hasEmbedding");
                    if (v instanceof Boolean) hasEmb = (Boolean) v;
                    else if (v != null) hasEmb = Boolean.parseBoolean(String.valueOf(v));
                }

                cb.onSuccess(token, role, fgId, mId, hasEmb);
            }

            @Override
            public void onFailure(String errorMessage) {
                cb.onFailure(errorMessage);
            }
        });
    }

    private static Long toLong(Object v) {
        if (v instanceof Number) return ((Number) v).longValue();
        return null;
    }
}
