package me.sebz.mu.pbl5.services;

import java.util.HashMap;
import java.util.Map;
import me.sebz.mu.pbl5.net.NetworkClient;

public class FakeNetworkClient implements NetworkClient {

    public String lastMethod;
    public String lastEndpoint;
    public Map<String, Object> lastData;

    public boolean shouldFail = false;
    public String failMsg = "fail";
    public Map<String, Object> successResponse;

    private Map<String, Object> safeSuccess() {
        return successResponse != null ? successResponse : new HashMap<String, Object>();
    }

    @Override
    public void get(String endpoint, Callback callback) {
        lastMethod = "GET";
        lastEndpoint = endpoint;
        if (shouldFail) callback.onFailure(failMsg);
        else callback.onSuccess(safeSuccess());
    }

    @Override
    public void post(String endpoint, Map<String, Object> data, Callback callback) {
        lastMethod = "POST";
        lastEndpoint = endpoint;
        lastData = data;
        if (shouldFail) callback.onFailure(failMsg);
        else callback.onSuccess(safeSuccess());
    }

    @Override
    public void put(String endpoint, Map<String, Object> data, Callback callback) {
        lastMethod = "PUT";
        lastEndpoint = endpoint;
        lastData = data;
        if (shouldFail) callback.onFailure(failMsg);
        else callback.onSuccess(safeSuccess());
    }

    @Override
    public void delete(String endpoint, Callback callback) {
        lastMethod = "DELETE";
        lastEndpoint = endpoint;
        if (shouldFail) callback.onFailure(failMsg);
        else callback.onSuccess(safeSuccess());
    }

    @Override
    public void upload(String endpoint, String filePath, Map<String, Object> data, Callback callback) {
        lastMethod = "UPLOAD";
        lastEndpoint = endpoint;
        lastData = data;
        if (shouldFail) callback.onFailure(failMsg);
        else callback.onSuccess(safeSuccess());
    }
}
