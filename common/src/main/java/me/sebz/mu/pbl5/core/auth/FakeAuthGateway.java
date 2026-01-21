package me.sebz.mu.pbl5.core.auth;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class FakeAuthGateway implements AuthGateway {

    public final AtomicInteger loginCalls = new AtomicInteger(0);
    public final AtomicInteger registerCalls = new AtomicInteger(0);

    private String lastLoginEmail;
    private String lastLoginPassword;
    private RegisterRequest lastRegisterRequest;

    private Result<Map<String, Object>> loginResult = Result.fail("not configured");
    private Result<Map<String, Object>> registerResult = Result.fail("not configured");

    public void willLoginReturn(Result<Map<String, Object>> result) {
        this.loginResult = result;
    }

    public void willRegisterReturn(Result<Map<String, Object>> result) {
        this.registerResult = result;
    }

    public String getLastLoginEmail() {
        return lastLoginEmail;
    }

    public String getLastLoginPassword() {
        return lastLoginPassword;
    }

    public RegisterRequest getLastRegisterRequest() {
        return lastRegisterRequest;
    }

    @Override
    public void login(String email, String password, Callback callback) {
        loginCalls.incrementAndGet();
        lastLoginEmail = email;
        lastLoginPassword = password;
        callback.onComplete(loginResult);
    }

    @Override
    public void register(RegisterRequest request, Callback callback) {
        registerCalls.incrementAndGet();
        lastRegisterRequest = request;
        callback.onComplete(registerResult);
    }
}
