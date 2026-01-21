package me.sebz.mu.pbl5.core.auth;

/**
 * Minimal Result wrapper that is easy to assert in JUnit.
 */
public final class Result<T> {
    private final T data;
    private final String error;

    private Result(T data, String error) {
        this.data = data;
        this.error = error;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(data, null);
    }

    public static <T> Result<T> fail(String error) {
        return new Result<>(null, error);
    }

    public boolean isOk() {
        return error == null;
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }
}
