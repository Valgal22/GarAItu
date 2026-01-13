package me.sebz.mu.pbl5;

import com.codename1.ui.*;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.components.ToastBar;

import java.util.Map;

import me.sebz.mu.pbl5.logic.IdUtil;
import me.sebz.mu.pbl5.logic.RoleUtil;

public class AdminDashboard extends Form {

    private Container memberListContainer;

    public AdminDashboard() {
        super("Admin Dashboard", BoxLayout.y());
        this.setScrollableY(true);

        this.add(new Label("Group Members"));

        memberListContainer = new Container(BoxLayout.y());
        memberListContainer.setName("admin_member_list");
        this.add(memberListContainer);

        Button refreshButton = new Button("Refresh List");
        refreshButton.setMaterialIcon(FontImage.MATERIAL_REFRESH, 4);
        refreshButton.setName("admin_refresh");
        refreshButton.addActionListener(e -> fetchMembers());
        this.add(refreshButton);

        Button inviteButton = new Button("Generate Invite Code");
        inviteButton.setName("admin_invite");
        inviteButton.addActionListener(e -> generateInvite());
        this.add(inviteButton);

        Button logoutButton = new Button("Logout");
        logoutButton.setUIID("ButtonSecondary");
        logoutButton.setName("admin_logout");
        logoutButton.addActionListener(e -> MemoryLens.logout());
        this.add(logoutButton);

        this.show();
        fetchMembers();
    }

    private void fetchMembers() {
        Long groupId = MemoryLens.getFamilyGroupId();
        if (groupId == null) {
            Dialog.show("Error", "No Group ID. Login again.", "OK", null);
            return;
        }

        GenericNetworkService.getInstance().get("/api/group/" + groupId + "/member",
                new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
                    @Override
                    public void onSuccess(Map<String, Object> response) {
                        java.util.List<Map<String, Object>> list;
                        if (response.containsKey("root")) {
                            list = (java.util.List<Map<String, Object>>) response.get("root");
                        } else {
                            list = null;
                        }

                        Display.getInstance().callSerially(() -> {
                            memberListContainer.removeAll();
                            if (list != null) {
                                for (Map<String, Object> member : list) addMemberRow(member);
                            } else {
                                memberListContainer.add(new Label("No members found."));
                            }
                            memberListContainer.revalidate();
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Display.getInstance().callSerially(() -> ToastBar.showErrorMessage("Failed to load members: " + errorMessage));
                    }
                });
    }

    private void addMemberRow(Map<String, Object> member) {
        String rawName = (String) member.get("username");
        if (rawName == null) rawName = (String) member.get("email");
        final String name = (rawName != null) ? rawName : "Unknown";

        String roleName = RoleUtil.roleName(member.get("role"));
        final String memberId = IdUtil.toSafeIdString(member.get("id"));

        Container row = new Container(new com.codename1.ui.layouts.BorderLayout());
        row.setUIID("MemberRow");
        row.getAllStyles().setPadding(2, 2, 2, 2);
        row.getAllStyles().setBorder(com.codename1.ui.plaf.Border.createLineBorder(1, 0xcccccc));

        Label nameLabel = new Label(name + " (" + roleName + ")");
        Button deleteBtn = new Button();
        deleteBtn.setMaterialIcon(FontImage.MATERIAL_DELETE, 4);
        deleteBtn.setName("admin_delete_" + memberId);

        deleteBtn.addActionListener(e -> {
            boolean confirm = Dialog.show("Confirm", "Delete " + name + "?", "Yes", "No");
            if (confirm) deleteMember(memberId);
        });

        row.add(com.codename1.ui.layouts.BorderLayout.CENTER, nameLabel);
        row.add(com.codename1.ui.layouts.BorderLayout.EAST, deleteBtn);

        memberListContainer.add(row);
    }

    private void deleteMember(String memberId) {
        Long groupId = MemoryLens.getFamilyGroupId();
        if (groupId == null) return;

        String endpoint = "/garAItu/group/" + groupId + "/member/" + memberId;

        GenericNetworkService.getInstance().delete(endpoint, new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
            @Override
            public void onSuccess(Map<String, Object> response) {
                Display.getInstance().callSerially(() -> {
                    ToastBar.showInfoMessage("Member deleted.");
                    fetchMembers();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Display.getInstance().callSerially(() -> Dialog.show("Error", "Delete failed: " + errorMessage, "OK", null));
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
                new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
                    @Override
                    public void onSuccess(Map<String, Object> response) {
                        String code = (String) response.get("code");
                        Display.getInstance().callSerially(() -> Dialog.show("Invite Code", "Share this code: " + code, "OK", null));
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Display.getInstance().callSerially(() -> Dialog.show("Error", "Failed to connect: " + errorMessage, "OK", null));
                    }
                });
    }
}
