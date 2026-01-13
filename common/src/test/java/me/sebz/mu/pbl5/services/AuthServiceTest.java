package me.sebz.mu.pbl5.services;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    @Test
    public void register_missingFields_noNetworkCall() {
        FakeNetworkClient net = new FakeNetworkClient();
        AuthService svc = new AuthService(net);

        final String[] err = {null};

        svc.register("", "a@b.com", "123", "ctx", (short)0, new AuthService.SimpleCallback() {
            @Override public void onSuccess() { fail("should not succeed"); }
            @Override public void onFailure(String error) { err[0] = error; }
        });

        assertNotNull(err[0]);
        assertNull(net.lastMethod);
    }

    @Test
    public void register_invalidEmail_noNetworkCall() {
        FakeNetworkClient net = new FakeNetworkClient();
        AuthService svc = new AuthService(net);

        final String[] err = {null};

        svc.register("Ana", "invalid", "123", "ctx", (short)0, new AuthService.SimpleCallback() {
            @Override public void onSuccess() { fail("should not succeed"); }
            @Override public void onFailure(String error) { err[0] = error; }
        });

        assertNotNull(err[0]);
        assertNull(net.lastMethod);
    }

    @Test
    public void register_success_coversLine28_andCallbackSuccess() {
        FakeNetworkClient net = new FakeNetworkClient();
        net.successResponse = new HashMap<>();

        AuthService svc = new AuthService(net);

        final boolean[] ok = {false};

        svc.register("Ana", "a@b.com", "pw", "ctx", (short)1, new AuthService.SimpleCallback() {
            @Override public void onSuccess() { ok[0] = true; }
            @Override public void onFailure(String error) { fail("should not fail"); }
        });

        assertEquals("POST", net.lastMethod);
        assertEquals("/api/auth/register", net.lastEndpoint);

        // Cubre líneas 32-54 (creación del mapa)
        assertEquals("Ana", net.lastData.get("name"));
        assertEquals("a@b.com", net.lastData.get("email"));
        assertEquals("pw", net.lastData.get("password"));
        assertEquals("ctx", net.lastData.get("context"));
        assertEquals((short)1, net.lastData.get("role"));

        assertTrue(ok[0]);
    }

    @Test
    public void register_networkFailure_coversLine28FailureCallback() {
        FakeNetworkClient net = new FakeNetworkClient();
        net.shouldFail = true;
        net.failMsg = "boom";

        AuthService svc = new AuthService(net);

        final String[] err = {null};

        svc.register("Ana", "a@b.com", "pw", "ctx", (short)1, new AuthService.SimpleCallback() {
            @Override public void onSuccess() { fail("should not succeed"); }
            @Override public void onFailure(String error) { err[0] = error; }
        });

        assertEquals("boom", err[0]); // cubre cb.onFailure(errorMessage)
    }

    @Test
    public void login_blankFields_noNetworkCall() {
        FakeNetworkClient net = new FakeNetworkClient();
        AuthService svc = new AuthService(net);

        final String[] err = {null};

        svc.login("", "pw", new AuthService.LoginCallback() {
            @Override public void onSuccess(String t, String r, Long fg, Long mid, boolean he) { fail(); }
            @Override public void onFailure(String error) { err[0] = error; }
        });

        assertNotNull(err[0]);
        assertNull(net.lastMethod);
    }

    @Test
    public void login_invalidEmail_noNetworkCall() {
        FakeNetworkClient net = new FakeNetworkClient();
        AuthService svc = new AuthService(net);

        final String[] err = {null};

        svc.login("invalid", "pw", new AuthService.LoginCallback() {
            @Override public void onSuccess(String t, String r, Long fg, Long mid, boolean he) { fail(); }
            @Override public void onFailure(String error) { err[0] = error; }
        });

        assertNotNull(err[0]);
        assertNull(net.lastMethod);
    }

    @Test
    public void login_success_hasEmbeddingBoolean_true_coversLines74_77_andHasEmbBooleanBranch() {
        FakeNetworkClient net = new FakeNetworkClient();

        Map<String,Object> resp = new HashMap<>();
        resp.put("role", "0");
        resp.put("session", "TOKEN123");
        resp.put("familyGroupId", 10);   // toLong Number
        resp.put("memberId", 99);        // toLong Number
        resp.put("hasEmbedding", true);  // Boolean branch
        net.successResponse = resp;

        AuthService svc = new AuthService(net);

        final Object[] out = new Object[5];

        svc.login("a@b.com", "pw", new AuthService.LoginCallback() {
            @Override public void onSuccess(String t, String r, Long fg, Long mid, boolean he) {
                out[0]=t; out[1]=r; out[2]=fg; out[3]=mid; out[4]=he;
            }
            @Override public void onFailure(String error) { fail("should not fail"); }
        });

        assertEquals("POST", net.lastMethod);
        assertEquals("/api/auth/login", net.lastEndpoint);

        assertEquals("TOKEN123", out[0]);
        assertEquals("0", out[1]);
        assertEquals(Long.valueOf(10), out[2]);
        assertEquals(Long.valueOf(99), out[3]);
        assertEquals(Boolean.TRUE, out[4]);
    }

    @Test
    public void login_success_hasEmbeddingString_true_coversParseBooleanBranch_andToLongNull() {
        FakeNetworkClient net = new FakeNetworkClient();

        Map<String,Object> resp = new HashMap<>();
        resp.put("role", 1);               // roleObj -> String.valueOf
        resp.put("session", "T");
        resp.put("familyGroupId", "x");    // toLong -> null (no Number)
        resp.put("memberId", null);        // toLong -> null
        resp.put("hasEmbedding", "true");  // parseBoolean branch
        net.successResponse = resp;

        AuthService svc = new AuthService(net);

        final Long[] fg = {Long.valueOf(1)};
        final Long[] mid = {Long.valueOf(1)};
        final boolean[] hasEmb = {false};

        svc.login("a@b.com", "pw", new AuthService.LoginCallback() {
            @Override public void onSuccess(String t, String r, Long familyGroupId, Long memberId, boolean he) {
                fg[0] = familyGroupId;
                mid[0] = memberId;
                hasEmb[0] = he;
            }
            @Override public void onFailure(String error) { fail("should not fail"); }
        });

        assertNull(fg[0]);
        assertNull(mid[0]);
        assertTrue(hasEmb[0]);
    }

    @Test
    public void login_success_hasEmbeddingKeyPresentButNull_coversIfContainsKeyButNoElseIf() {
        FakeNetworkClient net = new FakeNetworkClient();

        Map<String,Object> resp = new HashMap<>();
        resp.put("role", "2");
        resp.put("session", "T2");
        resp.put("hasEmbedding", null); // containsKey true, v null -> mantiene false
        net.successResponse = resp;

        AuthService svc = new AuthService(net);

        final boolean[] hasEmb = {true};

        svc.login("a@b.com", "pw", new AuthService.LoginCallback() {
            @Override public void onSuccess(String t, String r, Long fg, Long mid, boolean he) {
                hasEmb[0] = he;
            }
            @Override public void onFailure(String error) { fail("should not fail"); }
        });

        assertFalse(hasEmb[0]);
    }

    @Test
    public void login_failure_coversLines91_92() {
        FakeNetworkClient net = new FakeNetworkClient();
        net.shouldFail = true;
        net.failMsg = "fail";

        AuthService svc = new AuthService(net);

        final String[] err = {null};

        svc.login("a@b.com", "pw", new AuthService.LoginCallback() {
            @Override public void onSuccess(String t, String r, Long fg, Long mid, boolean he) { fail(); }
            @Override public void onFailure(String error) { err[0] = error; }
        });

        assertEquals("fail", err[0]);
    }
        @Test
    public void register_blankFields_hitsFailureBranch_line28() {
        FakeNetworkClient net = new FakeNetworkClient();
        AuthService svc = new AuthService(net);

        final String[] err = {null};

        svc.register("   ", "a@b.com", "pw", "ctx", (short) 1, new AuthService.SimpleCallback() {
            @Override public void onSuccess() { fail("should not succeed"); }
            @Override public void onFailure(String error) { err[0] = error; }
        });

        assertNotNull(err[0]);
        assertNull(net.lastMethod);
    }

    @Test
    public void register_success_withNullContext_andRole_coversPutLine51() {
        FakeNetworkClient net = new FakeNetworkClient();
        net.successResponse = new HashMap<>();

        AuthService svc = new AuthService(net);

        final boolean[] ok = {false};

        svc.register("Ana", "ana@b.com", "pw", null, (short) 2, new AuthService.SimpleCallback() {
            @Override public void onSuccess() { ok[0] = true; }
            @Override public void onFailure(String error) { fail("should not fail"); }
        });

        assertEquals("POST", net.lastMethod);
        assertEquals("/api/auth/register", net.lastEndpoint);

        // Esto fuerza el put de context/role (y suele ser la línea 51)
        assertTrue(net.lastData.containsKey("context"));
        assertNull(net.lastData.get("context"));
        assertEquals((short) 2, net.lastData.get("role"));

        assertTrue(ok[0]);
    }

    @Test
    public void login_success_minimalResponse_coversLine74() {
        FakeNetworkClient net = new FakeNetworkClient();

        Map<String,Object> resp = new HashMap<>();
        resp.put("role", "1");
        resp.put("session", "TOK");
        resp.put("familyGroupId", 1);
        resp.put("memberId", 2);
        // NO ponemos hasEmbedding -> cubre el camino containsKey = false
        net.successResponse = resp;

        AuthService svc = new AuthService(net);

        final String[] token = {null};

        svc.login("a@b.com", "pw", new AuthService.LoginCallback() {
            @Override public void onSuccess(String sessionToken, String role, Long fg, Long mid, boolean hasEmb) {
                token[0] = sessionToken;
            }
            @Override public void onFailure(String error) { fail("should not fail"); }
        });

        assertEquals("TOK", token[0]);
    }
    
    @Test
    public void register_blankName_coversBranch() {
        AuthService svc = new AuthService(new FakeNetworkClient());
        final String[] err = {null};

        svc.register("   ", "a@b.com", "pw", "ctx", (short)1, new AuthService.SimpleCallback() {
            @Override public void onSuccess() { fail(); }
            @Override public void onFailure(String error) { err[0] = error; }
        });

        assertEquals("Name, Email and Password are required", err[0]);
    }

    @Test
    public void register_blankEmail_coversBranch() {
        AuthService svc = new AuthService(new FakeNetworkClient());
        final String[] err = {null};

        svc.register("Ana", "   ", "pw", "ctx", (short)1, new AuthService.SimpleCallback() {
            @Override public void onSuccess() { fail(); }
            @Override public void onFailure(String error) { err[0] = error; }
        });

        assertEquals("Name, Email and Password are required", err[0]);
    }

    @Test
    public void register_blankPassword_coversBranch() {
        AuthService svc = new AuthService(new FakeNetworkClient());
        final String[] err = {null};

        svc.register("Ana", "a@b.com", "   ", "ctx", (short)1, new AuthService.SimpleCallback() {
            @Override public void onSuccess() { fail(); }
            @Override public void onFailure(String error) { err[0] = error; }
        });

        assertEquals("Name, Email and Password are required", err[0]);
    }
    @Test
    public void register_networkFailure_coversCallbackFailureLine() {
        FakeNetworkClient net = new FakeNetworkClient();
        net.shouldFail = true;
        net.failMsg = "boom";

        AuthService svc = new AuthService(net);

        final String[] err = {null};

        svc.register("Ana", "a@b.com", "pw", "ctx", (short)1, new AuthService.SimpleCallback() {
            @Override public void onSuccess() { fail(); }
            @Override public void onFailure(String error) { err[0] = error; }
        });

        assertEquals("boom", err[0]);
    }


}
