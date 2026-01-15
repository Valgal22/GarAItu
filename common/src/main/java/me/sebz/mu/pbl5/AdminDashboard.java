package me.sebz.mu.pbl5;

import com.codename1.ui.*;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.components.ToastBar;

import java.util.Map;

public class AdminDashboard extends Form {

    private Container memberListContainer;

    public AdminDashboard() {
        super("Admin Dashboard", BoxLayout.y());
        this.setScrollableY(true);

        this.add(new Label("Group Members"));

        memberListContainer = new Container(BoxLayout.y());
        this.add(memberListContainer);

        Button refreshButton = new Button("Refresh List");
        refreshButton.setMaterialIcon(FontImage.MATERIAL_REFRESH, 4);
        refreshButton.addActionListener(e -> fetchMembers());
        this.add(refreshButton);

        Button inviteButton = new Button("Generate Invite Code");
        inviteButton.addActionListener(e -> generateInvite());
        this.add(inviteButton);

        Button logoutButton = new Button("Logout");
        logoutButton.setUIID("ButtonSecondary");
        logoutButton.addActionListener(e -> MemoryLens.logout());
        this.add(logoutButton);

        // Initial fetch
        this.show(); // Ensure UI is ready
        fetchMembers();
    }

    private void fetchMembers() {
        Long groupId = MemoryLens.getFamilyGroupId();
        if (groupId == null) {
            Dialog.show("Error", "No Group ID. Login again.", "OK", null);
            return;
        }

        // Endpoint: /api/group/{id}/member
        GenericNetworkService.getInstance().get("/api/group/" + groupId + "/member",
                new GenericNetworkService.NetworkCallback() {
                    @Override
                    public void onSuccess(Map<String, Object> response) {
                        System.out.println("DEBUG: Members Response: " + response); // LOGGING ADDED

                        java.util.List<Map<String, Object>> list;
                        if (response.containsKey("root")) {
                            list = (java.util.List<Map<String, Object>>) response.get("root");
                        } else {
                            list = null; // fallback
                        }

                        Display.getInstance().callSerially(() -> {
                            memberListContainer.removeAll();
                            if (list != null) {
                                for (Map<String, Object> member : list) {
                                    addMemberRow(member);
                                }
                            } else {
                                memberListContainer.add(new Label("No members found."));
                            }
                            memberListContainer.revalidate();
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Display.getInstance().callSerially(() -> {
                            ToastBar.showErrorMessage("Failed to load members: " + errorMessage);
                        });
                    }
                });
    }

    private void addMemberRow(Map<String, Object> member) {
        String rawName = (String) member.get("username");
        if (rawName == null)
            rawName = (String) member.get("email");

        // Ensure name is effectively final for lambda
        final String name = (rawName != null) ? rawName : "Unknown";

        Object roleObj = member.get("role"); // 0=Admin, 1=Patient, 2=Family
        String roleName = "Unknown";
        String rStr = String.valueOf(roleObj);

        if ("0.0".equals(rStr))
            roleName = "Admin";
        else if ("1.0".equals(rStr))
            roleName = "Patient";
        else if ("2.0".equals(rStr))
            roleName = "Family Member";

        Object mIdObj = member.get("id");
        final String memberId;
        if (mIdObj instanceof Number) {
            memberId = String.valueOf(((Number) mIdObj).longValue());
        } else {
            memberId = String.valueOf(mIdObj);
        }

        Container row = new Container(new com.codename1.ui.layouts.BorderLayout());
        row.setUIID("MemberRow"); // Optional styling
        row.getAllStyles().setPadding(2, 2, 2, 2);
        row.getAllStyles().setBorder(com.codename1.ui.plaf.Border.createLineBorder(1, 0xcccccc));

        String chatId = (String) member.get("chatId");
        String info = name + " (" + roleName + ")";
        if (chatId != null && !chatId.isEmpty()) {
            info += " [TG: " + chatId + "]";
        }
        Label nameLabel = new Label(info);
        Button deleteBtn = new Button();
        deleteBtn.setMaterialIcon(FontImage.MATERIAL_DELETE, 4);
        deleteBtn.addActionListener(e -> {
            boolean confirm = Dialog.show("Confirm", "Delete " + name + "?", "Yes", "No");
            if (confirm) {
                deleteMember(memberId); // memberId is final
            }
        });

        row.add(com.codename1.ui.layouts.BorderLayout.CENTER, nameLabel);
        row.add(com.codename1.ui.layouts.BorderLayout.EAST, deleteBtn);

        memberListContainer.add(row);
    }

    private void deleteMember(String memberId) {
        Long groupId = MemoryLens.getFamilyGroupId();
        if (groupId == null)
            return;

        // DELETE /garAItu/group/{groupId}/member/{memberId}
        String endpoint = "/garAItu/group/" + groupId + "/member/" + memberId;

        GenericNetworkService.getInstance().delete(endpoint, new GenericNetworkService.NetworkCallback() {
            @Override
            public void onSuccess(Map<String, Object> response) {
                Display.getInstance().callSerially(() -> {
                    ToastBar.showInfoMessage("Member deleted.");
                    fetchMembers(); // refresh
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Display.getInstance().callSerially(() -> {
                    Dialog.show("Error", "Delete failed: " + errorMessage, "OK", null);
                });
            }
        });
    }

    private void generateInvite() {
        Long groupId = MemoryLens.getFamilyGroupId();
        if (groupId == null) {
            Dialog.show("Error", "No Group ID found. Please login again.", "OK", null);
            return;
        }
        GenericNetworkService.getInstance().get("/api/groups/" + groupId + "/invite",
                new GenericNetworkService.NetworkCallback() {
                    @Override
                    public void onSuccess(Map<String, Object> response) {

                        String code = (String) response.get("code");
                        Display.getInstance().callSerially(() -> {
                            Dialog.show("Invite Code", "Share this code: " + code, "OK", null);
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Display.getInstance().callSerially(() -> {
                            Dialog.show("Error", "Failed to connect: " + errorMessage, "OK", null);
                        });
                    }
                });
    }
}
