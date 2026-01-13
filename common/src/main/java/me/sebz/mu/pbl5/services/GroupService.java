package me.sebz.mu.pbl5.services;

import java.util.HashMap;
import java.util.Map;

import me.sebz.mu.pbl5.logic.Validation;
import me.sebz.mu.pbl5.net.NetworkClient;

public class GroupService {

    public interface JoinCallback {
        void onSuccess(Long familyGroupId);
        void onFailure(String error);
    }

    public interface CreateCallback {
        void onSuccess(Long familyGroupId);
        void onFailure(String error);
    }

    private final NetworkClient net;

    public GroupService(NetworkClient net) {
        this.net = net;
    }

    public void joinWithCode(String inviteCode, JoinCallback cb) {
        if (Validation.isBlank(inviteCode)) {
            cb.onFailure("Please enter an invite code");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("inviteCode", inviteCode);

        net.post("/api/groups/join", data, new NetworkClient.Callback() {
            @Override
            public void onSuccess(Map<String, Object> response) {
                Long fgId = (response.get("familyGroupId") instanceof Number)
                        ? ((Number) response.get("familyGroupId")).longValue()
                        : null;
                cb.onSuccess(fgId);
            }

            @Override
            public void onFailure(String errorMessage) {
                cb.onFailure(errorMessage);
            }
        });
    }

    public void createGroup(String name, CreateCallback cb) {
        if (Validation.isBlank(name)) {
            cb.onFailure("Group name is required");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);

        net.post("/api/groups/create", data, new NetworkClient.Callback() {
            @Override
            public void onSuccess(Map<String, Object> response) {
                Long fgId = (response.get("id") instanceof Number) ? ((Number) response.get("id")).longValue() : null;
                cb.onSuccess(fgId);
            }

            @Override
            public void onFailure(String errorMessage) {
                cb.onFailure(errorMessage);
            }
        });
    }
}
