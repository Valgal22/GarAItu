package me.sebz.mu.pbl5.core.auth;

import java.util.Map;

import me.sebz.mu.pbl5.core.validation.InputValidator;
import me.sebz.mu.pbl5.core.validation.ValidationResult;

public final class AuthUseCase {

    public interface SessionCallback {
        void onComplete(Result<SessionInfo> result);
    }

    public interface SimpleCallback {
        void onComplete(Result<Void> result);
    }

    private final AuthGateway gateway;

    public AuthUseCase(AuthGateway gateway) {
        this.gateway = gateway;
    }

    public void login(LoginRequest request, SessionCallback callback) {
        ValidationResult vr = InputValidator.validateLogin(request.getEmail(), request.getPassword());
        if (!vr.isValid()) {
            callback.onComplete(Result.fail(vr.firstErrorOrNull()));
            return;
        }

        gateway.login(request.getEmail(), request.getPassword(), gwResult -> {
            if (!gwResult.isOk()) {
                callback.onComplete(Result.fail(gwResult.getError()));
                return;
            }
            callback.onComplete(Result.ok(mapSession(gwResult.getData())));
        });
    }

    public void register(RegisterRequest request, SimpleCallback callback) {
        ValidationResult vr = InputValidator.validateRegistration(request.getName(), request.getEmail(), request.getPassword());
        if (!vr.isValid()) {
            callback.onComplete(Result.fail(vr.firstErrorOrNull()));
            return;
        }

        gateway.register(request, gwResult -> {
            if (!gwResult.isOk()) {
                callback.onComplete(Result.fail(gwResult.getError()));
                return;
            }
            callback.onComplete(Result.ok(null));
        });
    }

    private SessionInfo mapSession(Map<String, Object> response) {
        String role = (response.get("role") != null) ? String.valueOf(response.get("role")) : null;
        String token = (response.get("session") != null) ? String.valueOf(response.get("session")) : null;
        Long familyGroupId = asLong(response.get("familyGroupId"));
        Long memberId = asLong(response.get("memberId"));
        boolean hasEmbedding = parseBoolean(response.get("hasEmbedding"));
        String chatId = (response.get("chatId") != null) ? String.valueOf(response.get("chatId")) : null;

        return new SessionInfo(token, role, memberId, familyGroupId, chatId, hasEmbedding);
    }

    private static Long asLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) return null;
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean parseBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }
}
