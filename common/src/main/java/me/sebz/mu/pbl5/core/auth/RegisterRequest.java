package me.sebz.mu.pbl5.core.auth;

public final class RegisterRequest {
    private final String name;
    private final String email;
    private final String password;
    private final String context;
    private final short role;
    private final String chatId;

    public RegisterRequest(String name, String email, String password, String context, short role, String chatId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.context = context;
        this.role = role;
        this.chatId = chatId;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getContext() { return context; }
    public short getRole() { return role; }
    public String getChatId() { return chatId; }
}
