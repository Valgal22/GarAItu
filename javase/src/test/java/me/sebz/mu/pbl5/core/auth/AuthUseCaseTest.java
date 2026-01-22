package me.sebz.mu.pbl5.core.auth;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AuthUseCaseTest {

    @Test
    void login_fails_fast_when_validation_fails_and_does_not_call_gateway() {
        FakeAuthGateway gw = new FakeAuthGateway();
        AuthUseCase uc = new AuthUseCase(gw);

        AtomicReference<Result<SessionInfo>> out = new AtomicReference<>();
        uc.login(new LoginRequest("bad-email", "pw"), out::set);

        assertNotNull(out.get());
        assertFalse(out.get().isOk());
        assertEquals("Please enter a valid email address", out.get().getError());
        assertEquals(0, gw.loginCalls.get());
    }

    @Test
    void login_returns_error_when_gateway_fails() {
        FakeAuthGateway gw = new FakeAuthGateway();
        gw.willLoginReturn(Result.fail("401"));
        AuthUseCase uc = new AuthUseCase(gw);

        AtomicReference<Result<SessionInfo>> out = new AtomicReference<>();
        uc.login(new LoginRequest("a@b.com", "secret"), out::set);

        assertNotNull(out.get());
        assertFalse(out.get().isOk());
        assertEquals("401", out.get().getError());
        assertEquals(1, gw.loginCalls.get());
        assertEquals("a@b.com", gw.getLastLoginEmail());
        assertEquals("secret", gw.getLastLoginPassword());
    }

    @Test
    void login_maps_session_fields_on_success_with_numbers_and_booleans() {
        FakeAuthGateway gw = new FakeAuthGateway();

        Map<String, Object> resp = new HashMap<>();
        resp.put("role", "0");               // admin
        resp.put("session", "token-123");
        resp.put("familyGroupId", 55);       // Number -> long
        resp.put("memberId", 99L);
        resp.put("chatId", "chat-777");
        resp.put("hasEmbedding", true);

        gw.willLoginReturn(Result.ok(resp));

        AuthUseCase uc = new AuthUseCase(gw);

        AtomicReference<Result<SessionInfo>> out = new AtomicReference<>();
        uc.login(new LoginRequest("a@b.com", "secret"), out::set);

        assertTrue(out.get().isOk());
        SessionInfo s = out.get().getData();
        assertNotNull(s);

        assertEquals("token-123", s.getSessionToken());
        assertEquals("0", s.getRole());
        assertEquals(55L, s.getFamilyGroupId());
        assertEquals(99L, s.getMemberId());
        assertEquals("chat-777", s.getChatId());
        assertTrue(s.hasEmbedding());
    }

    @Test
    void login_maps_hasEmbedding_from_string_boolean_too() {
        FakeAuthGateway gw = new FakeAuthGateway();

        Map<String, Object> resp = new HashMap<>();
        resp.put("role", "2");
        resp.put("session", "t");
        resp.put("hasEmbedding", "true"); // String -> boolean

        gw.willLoginReturn(Result.ok(resp));
        AuthUseCase uc = new AuthUseCase(gw);

        AtomicReference<Result<SessionInfo>> out = new AtomicReference<>();
        uc.login(new LoginRequest("a@b.com", "secret"), out::set);

        assertTrue(out.get().isOk());
        assertTrue(out.get().getData().hasEmbedding());
    }

    @Test
    void register_fails_fast_when_validation_fails_and_does_not_call_gateway() {
        FakeAuthGateway gw = new FakeAuthGateway();
        AuthUseCase uc = new AuthUseCase(gw);

        RegisterRequest req = new RegisterRequest("Maider", "bad", "123456", "ctx", (short) 0, "c");
        AtomicReference<Result<Void>> out = new AtomicReference<>();
        uc.register(req, out::set);

        assertNotNull(out.get());
        assertFalse(out.get().isOk());
        assertEquals("Please enter a valid email address", out.get().getError());
        assertEquals(0, gw.registerCalls.get());
    }

    @Test
    void register_returns_ok_when_gateway_ok() {
        FakeAuthGateway gw = new FakeAuthGateway();
        gw.willRegisterReturn(Result.ok(new HashMap<>()));
        AuthUseCase uc = new AuthUseCase(gw);

        RegisterRequest req = new RegisterRequest("Maider", "a@b.com", "123456", "ctx", (short) 1, "chat");
        AtomicReference<Result<Void>> out = new AtomicReference<>();
        uc.register(req, out::set);

        assertTrue(out.get().isOk());
        assertNull(out.get().getData());
        assertEquals(1, gw.registerCalls.get());

        assertNotNull(gw.getLastRegisterRequest());
        assertEquals("Maider", gw.getLastRegisterRequest().getName());
        assertEquals("a@b.com", gw.getLastRegisterRequest().getEmail());
        assertEquals("123456", gw.getLastRegisterRequest().getPassword());
        assertEquals("ctx", gw.getLastRegisterRequest().getContext());
        assertEquals(1, gw.getLastRegisterRequest().getRole());
        assertEquals("chat", gw.getLastRegisterRequest().getChatId());
    }

    @Test
    void register_returns_error_when_gateway_fails() {
        FakeAuthGateway gw = new FakeAuthGateway();
        gw.willRegisterReturn(Result.fail("409"));
        AuthUseCase uc = new AuthUseCase(gw);

        RegisterRequest req = new RegisterRequest("Maider", "a@b.com", "123456", "ctx", (short) 2, "chat");
        AtomicReference<Result<Void>> out = new AtomicReference<>();
        uc.register(req, out::set);

        assertFalse(out.get().isOk());
        assertEquals("409", out.get().getError());
        assertEquals(1, gw.registerCalls.get());
    }
}
