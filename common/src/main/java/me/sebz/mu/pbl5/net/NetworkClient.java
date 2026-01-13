package me.sebz.mu.pbl5.net;

import java.util.Map;

public interface NetworkClient {

    interface Callback {
        void onSuccess(Map<String, Object> response);
        void onFailure(String errorMessage);
    }

    void get(String endpoint, Callback callback);
    void post(String endpoint, Map<String, Object> data, Callback callback);
    void put(String endpoint, Map<String, Object> data, Callback callback);
    void delete(String endpoint, Callback callback);
    void upload(String endpoint, String filePath, Map<String, Object> data, Callback callback);
}
