package me.sebz.mu.pbl5;

import com.codename1.components.ToastBar;
import com.codename1.ui.Button;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.layouts.BoxLayout;
import me.sebz.mu.pbl5.ui.MemberListUiHelper;
import me.sebz.mu.pbl5.util.MemberRoleUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AdminDashboard extends Form {

    private static final String TITLE_ERROR = "Error";
    private static final String TITLE_CONFIRM = "Confirm";
    private static final String EMPTY_MEMBERS_MSG = "No members found.";
    private static final String MSG_LOAD_FAILED_PREFIX = "Failed to load members: ";

    private Container memberListContainer;

    public AdminDashboard() {
        super("Admin Dashboard", BoxLayout.y());
        setScrollableY(true);

        add(new Label("Group Members"));

        memberListContainer = new Container(BoxLayout.y());
        add(memberListContainer);

        Button refreshButton = new Button("Refresh List");
        refreshButton.setMaterialIcon(FontImage.MATERIAL_REFRESH, 4);
        refreshButton.addActionListener(e -> fetchMembers());
        add(refreshButton);

        Button inviteButton = new Button("Generate Invite Code");
        inviteButton.addActionListener(e -> generateInvite());
        add(inviteButton);

        Button logoutButton = new Button("Logout");
        logoutButton.setUIID("ButtonSecondary");
        logoutButton.addActionListener(e -> MemoryLens.logout());
        add(logoutButton);

        show();
        fetchMembers();
    }

    private void fetchMembers() {
        Long groupId = MemoryLens.getFamilyGroupId();
        if (groupId == null) {
            Dialog.show(TITLE_ERROR, "No Group ID. Login again.", "OK", null);
            return;
        }

        GenericNetworkService.getInstance().get("/api/group/" + groupId + "/member",
                new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public void onSuccess(Map<String, Object> response) {
                        List<Map<String, Object>> list;
                        Object root = (response != null) ? response.get("root") : null;
                        if (root instanceof List) {
                            list = (List<Map<String, Object>>) root;
                        } else {
                            list = Collections.emptyList();
                        }

                        MemberListUiHelper.renderMembers(
                                memberListContainer,
                                list,
                                AdminDashboard.this::addMemberRow,
                                EMPTY_MEMBERS_MSG
                        );
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        MemberListUiHelper.showLoadError(MSG_LOAD_FAILED_PREFIX, errorMessage);
                    }
                });
    }

    private void addMemberRow(Map<String, Object> member) {
        String rawName = (String) member.get("username");
        if (rawName == null) {
            rawName = (String) member.get("email");
        }
        final String name = (rawName != null) ? rawName : "Unknown";

        String roleName = MemberRoleUtil.roleNameFrom(member.get("role"));

        Object mIdObj = member.get("id");
        final String memberId = (mIdObj instanceof Number)
                ? String.valueOf(((Number) mIdObj).longValue())
                : String.valueOf(mIdObj);

        Container row = new Container(new com.codename1.ui.layouts.BorderLayout());
        row.setUIID("MemberRow");
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
            boolean confirm = Dialog.show(TITLE_CONFIRM, "Delete " + name + "?", "Yes", "No");
            if (confirm) {
                deleteMember(memberId);
            }
        });

        row.add(com.codename1.ui.layouts.BorderLayout.CENTER, nameLabel);
        row.add(com.codename1.ui.layouts.BorderLayout.EAST, deleteBtn);

        memberListContainer.add(row);
    }

    private void deleteMember(String memberId) {
        Long groupId = MemoryLens.getFamilyGroupId();
        if (groupId == null) {
            return;
        }

        String endpoint = "/api/group/" + groupId + "/member/" + memberId;

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
                Display.getInstance().callSerially(() ->
                        Dialog.show(TITLE_ERROR, "Delete failed: " + errorMessage, "OK", null)
                );
            }
        });
    }

    private void generateInvite() {
        Long groupId = MemoryLens.getFamilyGroupId();
        if (groupId == null) {
            Dialog.show(TITLE_ERROR, "No Group ID found. Please login again.", "OK", null);
            return;
        }

        GenericNetworkService.getInstance().get("/api/groups/" + groupId + "/invite",
                new me.sebz.mu.pbl5.net.NetworkClient.Callback() {
                    @Override
                    public void onSuccess(Map<String, Object> response) {
                        String code = (String) response.get("code");
                        Display.getInstance().callSerially(() ->
                                Dialog.show("Invite Code", "Share this code: " + code, "OK", null)
                        );
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Display.getInstance().callSerially(() ->
                                Dialog.show(TITLE_ERROR, "Failed to connect: " + errorMessage, "OK", null)
                        );
                    }
                });
    }
}
