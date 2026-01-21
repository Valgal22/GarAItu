package me.sebz.mu.pbl5.core.auth;

import java.util.HashMap;
import java.util.Map;

import me.sebz.mu.pbl5.net.NetworkClient;

public final class AuthGatewayNodeRed implements AuthGateway {

    private final NetworkClient client;

    public AuthGatewayNodeRed(NetworkClient client) {
        this.client = client;
    }

    @Override
    public void login(String email, String password, Callback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);

        client.post("/api/auth/login", data, new NetworkClient.Callback() {
            @Override
            public void onSuccess(Map<String, Object> response) {
                callback.onComplete(Result.ok(response));
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onComplete(Result.fail(errorMessage));
            }
        });
    }

    @Override
    public void register(RegisterRequest request, Callback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", request.getName());
        data.put("email", request.getEmail());
        data.put("password", request.getPassword());
        data.put("context", request.getContext());
        data.put("role", request.getRole());
        data.put("chatId", request.getChatId());

        client.post("/api/auth/register", data, new NetworkClient.Callback() {
            @Override
            public void onSuccess(Map<String, Object> response) {
                callback.onComplete(Result.ok(response));
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onComplete(Result.fail(errorMessage));
            }
        });
    }
}
