package me.sebz.mu.pbl5.core.auth;

public final class SessionInfo {
    private final String sessionToken;
    private final String role;
    private final Long memberId;
    private final Long familyGroupId;
    private final String chatId;
    private final boolean hasEmbedding;

    public SessionInfo(String sessionToken, String role, Long memberId, Long familyGroupId, String chatId, boolean hasEmbedding) {
        this.sessionToken = sessionToken;
        this.role = role;
        this.memberId = memberId;
        this.familyGroupId = familyGroupId;
        this.chatId = chatId;
        this.hasEmbedding = hasEmbedding;
    }

    public String getSessionToken() { return sessionToken; }
    public String getRole() { return role; }
    public Long getMemberId() { return memberId; }
    public Long getFamilyGroupId() { return familyGroupId; }
    public String getChatId() { return chatId; }
    public boolean hasEmbedding() { return hasEmbedding; }
}
