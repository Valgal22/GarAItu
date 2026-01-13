package me.sebz.mu.pbl5.services;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GroupServiceTest {

    @Test
    public void joinWithCode_blank_noNetworkCall() {
        FakeNetworkClient net = new FakeNetworkClient();
        GroupService svc = new GroupService(net);

        final String[] err = {null};

        svc.joinWithCode("   ", new GroupService.JoinCallback() {
            @Override public void onSuccess(Long familyGroupId) { fail("should not succeed"); }
            @Override public void onFailure(String error) { err[0] = error; }
        });

        assertNotNull(err[0]);
        assertNull(net.lastMethod);
    }

    @Test
    public void joinWithCode_success_numberId_coversOnSuccessLine39() {
        FakeNetworkClient net = new FakeNetworkClient();
        Map<String,Object> resp = new HashMap<>();
        resp.put("familyGroupId", 123); // Number -> entra en ternario (linea 39)
        net.successResponse = resp;

        GroupService svc = new GroupService(net);

        final Long[] fg = {null};

        svc.joinWithCode("CODE", new GroupService.JoinCallback() {
            @Override public void onSuccess(Long familyGroupId) { fg[0] = familyGroupId; }
            @Override public void onFailure(String error) { fail("should not fail"); }
        });

        assertEquals("POST", net.lastMethod);
        assertEquals("/api/groups/join", net.lastEndpoint);
        assertEquals("CODE", net.lastData.get("inviteCode"));
        assertEquals(Long.valueOf(123), fg[0]);
    }

    @Test
    public void joinWithCode_success_nonNumberId_setsNull_coversLine39ElseBranch() {
        FakeNetworkClient net = new FakeNetworkClient();
        Map<String,Object> resp = new HashMap<>();
        resp.put("familyGroupId", "not-a-number"); // no Number -> null
        net.successResponse = resp;

        GroupService svc = new GroupService(net);

        final Long[] fg = {Long.valueOf(999)};

        svc.joinWithCode("CODE", new GroupService.JoinCallback() {
            @Override public void onSuccess(Long familyGroupId) { fg[0] = familyGroupId; }
            @Override public void onFailure(String error) { fail("should not fail"); }
        });

        assertNull(fg[0]);
    }

    @Test
    public void joinWithCode_failure_coversLines47_48() {
        FakeNetworkClient net = new FakeNetworkClient();
        net.shouldFail = true;
        net.failMsg = "boom";

        GroupService svc = new GroupService(net);

        final String[] err = {null};

        svc.joinWithCode("CODE", new GroupService.JoinCallback() {
            @Override public void onSuccess(Long familyGroupId) { fail("should not succeed"); }
            @Override public void onFailure(String error) { err[0] = error; }
        });

        assertEquals("boom", err[0]);
    }

    @Test
    public void createGroup_blank_noNetworkCall() {
        FakeNetworkClient net = new FakeNetworkClient();
        GroupService svc = new GroupService(net);

        final String[] err = {null};

        svc.createGroup("", new GroupService.CreateCallback() {
            @Override public void onSuccess(Long familyGroupId) { fail("should not succeed"); }
            @Override public void onFailure(String error) { err[0] = error; }
        });

        assertNotNull(err[0]);
        assertNull(net.lastMethod);
    }

    @Test
    public void createGroup_success_numberId_coversLine64() {
        FakeNetworkClient net = new FakeNetworkClient();
        Map<String,Object> resp = new HashMap<>();
        resp.put("id", 77); // Number -> fgId = 77 (linea 64)
        net.successResponse = resp;

        GroupService svc = new GroupService(net);

        final Long[] fg = {null};

        svc.createGroup("MyGroup", new GroupService.CreateCallback() {
            @Override public void onSuccess(Long familyGroupId) { fg[0] = familyGroupId; }
            @Override public void onFailure(String error) { fail("should not fail"); }
        });

        assertEquals("POST", net.lastMethod);
        assertEquals("/api/groups/create", net.lastEndpoint);
        assertEquals("MyGroup", net.lastData.get("name"));
        assertEquals(Long.valueOf(77), fg[0]);
    }

    @Test
    public void createGroup_success_nonNumberId_setsNull_coversLine64ElseBranch() {
        FakeNetworkClient net = new FakeNetworkClient();
        Map<String,Object> resp = new HashMap<>();
        resp.put("id", "x"); // no Number -> null
        net.successResponse = resp;

        GroupService svc = new GroupService(net);

        final Long[] fg = {Long.valueOf(999)};

        svc.createGroup("MyGroup", new GroupService.CreateCallback() {
            @Override public void onSuccess(Long familyGroupId) { fg[0] = familyGroupId; }
            @Override public void onFailure(String error) { fail("should not fail"); }
        });

        assertNull(fg[0]);
    }

    @Test
    public void createGroup_failure_coversLines70_71() {
        FakeNetworkClient net = new FakeNetworkClient();
        net.shouldFail = true;
        net.failMsg = "fail";

        GroupService svc = new GroupService(net);

        final String[] err = {null};

        svc.createGroup("MyGroup", new GroupService.CreateCallback() {
            @Override public void onSuccess(Long familyGroupId) { fail("should not succeed"); }
            @Override public void onFailure(String error) { err[0] = error; }
        });

        assertEquals("fail", err[0]);
    }
}
