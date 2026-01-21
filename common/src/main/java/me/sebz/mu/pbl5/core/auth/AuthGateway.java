package me.sebz.mu.pbl5.core.auth;

import java.util.Map;

public interface AuthGateway {

    interface Callback {
        void onComplete(Result<Map<String, Object>> result);
    }

    void login(String email, String password, Callback callback);

    void register(RegisterRequest request, Callback callback);
}
