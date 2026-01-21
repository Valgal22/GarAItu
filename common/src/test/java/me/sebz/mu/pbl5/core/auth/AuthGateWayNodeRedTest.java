package me.sebz.mu.pbl5.core.auth;

import me.sebz.mu.pbl5.net.NetworkClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthGatewayNodeRedTest {

    @Test
    void login_calls_correct_endpoint_and_sends_email_password() {
        NetworkClient client = mock(NetworkClient.class);
        AuthGatewayNodeRed gw = new AuthGatewayNodeRed(client);

        final AtomicReference<Result<Map<String, Object>>> out = new AtomicReference<Result<Map<String, Object>>>();
        gw.login("a@b.com", "secret", new AuthGateway.Callback() {
            @Override public void onComplete(Result<Map<String, Object>> result) {
                out.set(result);
            }
        });

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<NetworkClient.Callback> cbCaptor = ArgumentCaptor.forClass(NetworkClient.Callback.class);

        verify(client).post(eq("/api/auth/login"), mapCaptor.capture(), cbCaptor.capture());

        @SuppressWarnings("unchecked")
        Map<String, Object> sent = (Map<String, Object>) mapCaptor.getValue();
        assertEquals("a@b.com", sent.get("email"));
        assertEquals("secret", sent.get("password"));
        assertNotNull(cbCaptor.getValue());

        // Aún no debería haber resultado porque no hemos invocado el callback de red
        assertNull(out.get());
    }

    @Test
    void login_success_maps_to_ok_result() {
        NetworkClient client = mock(NetworkClient.class);
        AuthGatewayNodeRed gw = new AuthGatewayNodeRed(client);

        final AtomicReference<NetworkClient.Callback> capturedCb = new AtomicReference<NetworkClient.Callback>();

        doAnswer(inv -> {
            NetworkClient.Callback cb = inv.getArgument(2);
            capturedCb.set(cb);
            return null;
        }).when(client).post(eq("/api/auth/login"), anyMap(), any(NetworkClient.Callback.class));

        final AtomicReference<Result<Map<String, Object>>> out = new AtomicReference<Result<Map<String, Object>>>();
        gw.login("a@b.com", "secret", new AuthGateway.Callback() {
            @Override public void onComplete(Result<Map<String, Object>> result) {
                out.set(result);
            }
        });

        assertNotNull(capturedCb.get());

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("session", "t");
        capturedCb.get().onSuccess(response);

        assertNotNull(out.get());
        assertTrue(out.get().isOk());
        assertEquals("t", out.get().getData().get("session"));
    }

    @Test
    void login_failure_maps_to_fail_result() {
        NetworkClient client = mock(NetworkClient.class);
        AuthGatewayNodeRed gw = new AuthGatewayNodeRed(client);

        final AtomicReference<NetworkClient.Callback> capturedCb = new AtomicReference<NetworkClient.Callback>();

        doAnswer(inv -> {
            capturedCb.set(inv.getArgument(2));
            return null;
        }).when(client).post(eq("/api/auth/login"), anyMap(), any(NetworkClient.Callback.class));

        final AtomicReference<Result<Map<String, Object>>> out = new AtomicReference<Result<Map<String, Object>>>();
        gw.login("a@b.com", "secret", new AuthGateway.Callback() {
            @Override public void onComplete(Result<Map<String, Object>> result) {
                out.set(result);
            }
        });

        assertNotNull(capturedCb.get());
        capturedCb.get().onFailure("401");

        assertNotNull(out.get());
        assertFalse(out.get().isOk());
        assertEquals("401", out.get().getError());
    }

    @Test
    void register_calls_correct_endpoint_and_sends_fields() {
        NetworkClient client = mock(NetworkClient.class);
        AuthGatewayNodeRed gw = new AuthGatewayNodeRed(client);

        RegisterRequest req = new RegisterRequest("Maider", "a@b.com", "123456", "ctx", (short) 2, "chat");

        final AtomicReference<Result<Map<String, Object>>> out = new AtomicReference<Result<Map<String, Object>>>();
        gw.register(req, new AuthGateway.Callback() {
            @Override public void onComplete(Result<Map<String, Object>> result) {
                out.set(result);
            }
        });

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);

        verify(client).post(eq("/api/auth/register"), mapCaptor.capture(), any(NetworkClient.Callback.class));

        @SuppressWarnings("unchecked")
        Map<String, Object> sent = (Map<String, Object>) mapCaptor.getValue();
        assertEquals("Maider", sent.get("name"));
        assertEquals("a@b.com", sent.get("email"));
        assertEquals("123456", sent.get("password"));
        assertEquals("ctx", sent.get("context"));
        assertEquals((short) 2, sent.get("role"));
        assertEquals("chat", sent.get("chatId"));

        // Aún no debería haber resultado porque no hemos invocado el callback de red
        assertNull(out.get());
    }

    @Test
    void register_success_and_failure_are_mapped() {
        NetworkClient client = mock(NetworkClient.class);
        AuthGatewayNodeRed gw = new AuthGatewayNodeRed(client);

        final AtomicReference<NetworkClient.Callback> capturedCb = new AtomicReference<NetworkClient.Callback>();
        doAnswer(inv -> {
            capturedCb.set(inv.getArgument(2));
            return null;
        }).when(client).post(eq("/api/auth/register"), anyMap(), any(NetworkClient.Callback.class));

        // success
        final AtomicReference<Result<Map<String, Object>>> out1 = new AtomicReference<Result<Map<String, Object>>>();
        gw.register(new RegisterRequest("Maider", "a@b.com", "123456", "ctx", (short) 1, "chat"),
                new AuthGateway.Callback() {
                    @Override public void onComplete(Result<Map<String, Object>> result) {
                        out1.set(result);
                    }
                });

        Map<String, Object> okResp = new HashMap<String, Object>();
        okResp.put("ok", Boolean.TRUE);
        capturedCb.get().onSuccess(okResp);

        assertTrue(out1.get().isOk());
        assertEquals(Boolean.TRUE, out1.get().getData().get("ok"));

        // failure
        final AtomicReference<Result<Map<String, Object>>> out2 = new AtomicReference<Result<Map<String, Object>>>();
        gw.register(new RegisterRequest("Maider", "a@b.com", "123456", "ctx", (short) 1, "chat"),
                new AuthGateway.Callback() {
                    @Override public void onComplete(Result<Map<String, Object>> result) {
                        out2.set(result);
                    }
                });

        capturedCb.get().onFailure("409");

        assertFalse(out2.get().isOk());
        assertEquals("409", out2.get().getError());
    }
}
