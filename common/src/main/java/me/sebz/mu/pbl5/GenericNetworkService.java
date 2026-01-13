package me.sebz.mu.pbl5;

import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.MultipartRequest;
import com.codename1.io.NetworkManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import me.sebz.mu.pbl5.net.NetworkClient;
import me.sebz.mu.pbl5.net.TokenProvider;

public class GenericNetworkService implements NetworkClient {

    private static GenericNetworkService instance;

    private final String baseUrl;
    private final TokenProvider tokenProvider;

    private GenericNetworkService(String baseUrl, TokenProvider tokenProvider) {
        this.baseUrl = baseUrl;
        this.tokenProvider = tokenProvider;
    }

    // Mantiene compatibilidad con tu código actual
    public static GenericNetworkService getInstance() {
        if (instance == null) {
            instance = new GenericNetworkService("http://localhost:1880", () -> MemoryLens.getSessionToken());
        }
        return instance;
    }

    // Útil para tests (puedes crear otra instancia con token/baseUrl controlados)
    public static GenericNetworkService createForTesting(String baseUrl, TokenProvider tokenProvider) {
        return new GenericNetworkService(baseUrl, tokenProvider);
    }

    @Override
    public void get(String endpoint, NetworkClient.Callback callback) {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(baseUrl + endpoint);
        req.setHttpMethod("GET");
        req.addRequestHeader("Accept", "application/json");
        addToken(req);

        req.addResponseListener(evt -> {
            int code = req.getResponseCode();
            if (code == 200 || code == 201) parseResponse(req.getResponseData(), callback);
            else handleErrorResponse(req, callback);
        });

        req.addExceptionListener(evt -> callback.onFailure("Connection failed. Is Node-RED running?"));
        NetworkManager.getInstance().addToQueue(req);
    }

    @Override
    public void post(String endpoint, Map<String, Object> data, NetworkClient.Callback callback) {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(baseUrl + endpoint);

        req.setPost(true);
        req.setHttpMethod("POST");
        req.setContentType("application/json");
        req.addRequestHeader("Accept", "application/json");
        addToken(req);

        String jsonBody = me.sebz.mu.pbl5.utils.JsonUtil.buildJson(data);
        req.setRequestBody(jsonBody);
        req.setReadResponseForErrors(true);

        req.addResponseListener(evt -> {
            int code = req.getResponseCode();
            if (code == 200 || code == 201) parseResponse(req.getResponseData(), callback);
            else handleErrorResponse(req, callback);
        });

        req.addExceptionListener(evt -> callback.onFailure("Connection failed. Is Node-RED running?"));
        NetworkManager.getInstance().addToQueue(req);
    }

    @Override
    public void put(String endpoint, Map<String, Object> data, NetworkClient.Callback callback) {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(baseUrl + endpoint);
        req.setHttpMethod("PUT");

        req.setContentType("application/json");
        req.addRequestHeader("Accept", "application/json");
        addToken(req);

        String jsonBody = me.sebz.mu.pbl5.utils.JsonUtil.buildJson(data);
        req.setRequestBody(jsonBody);
        req.setWriteRequest(true);

        req.addResponseListener(evt -> {
            int code = req.getResponseCode();
            if (code == 200 || code == 201 || code == 204) {
                if (req.getResponseData() != null && req.getResponseData().length > 0) parseResponse(req.getResponseData(), callback);
                else callback.onSuccess(new java.util.HashMap<>());
            } else handleErrorResponse(req, callback);
        });

        req.addExceptionListener(evt -> callback.onFailure("Connection failed. Is Node-RED running?"));
        NetworkManager.getInstance().addToQueue(req);
    }

    @Override
    public void delete(String endpoint, NetworkClient.Callback callback) {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(baseUrl + endpoint);
        req.setHttpMethod("DELETE");

        req.addRequestHeader("Accept", "application/json");
        addToken(req);

        req.addResponseListener(evt -> {
            int code = req.getResponseCode();
            if (code == 200 || code == 201 || code == 204) {
                if (req.getResponseData() != null && req.getResponseData().length > 0) parseResponse(req.getResponseData(), callback);
                else callback.onSuccess(new java.util.HashMap<>());
            } else handleErrorResponse(req, callback);
        });

        req.addExceptionListener(evt -> callback.onFailure("Connection failed. Is Node-RED running?"));
        NetworkManager.getInstance().addToQueue(req);
    }

    @Override
    public void upload(String endpoint, String filePath, Map<String, Object> data, NetworkClient.Callback callback) {
        MultipartRequest req = new MultipartRequest();
        req.setUrl(baseUrl + endpoint);
        addToken(req);

        try {
            req.addData("file", filePath, "image/jpeg");

            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Object val = entry.getValue();
                if (val != null) req.addArgument(entry.getKey(), val.toString());
            }
        } catch (IOException e) {
            callback.onFailure("File error: " + e.getMessage());
            return;
        }

        req.addResponseListener(evt -> {
            int code = req.getResponseCode();
            if (code == 200 || code == 201) parseResponse(req.getResponseData(), callback);
            else handleErrorResponse(req, callback);
        });

        req.addExceptionListener(evt -> callback.onFailure("Upload failed. Check connection."));
        NetworkManager.getInstance().addToQueue(req);
    }

    private void addToken(ConnectionRequest req) {
        String token = tokenProvider != null ? tokenProvider.getToken() : null;
        if (token != null) req.addRequestHeader("X-Session-Id", token);
    }

    private void parseResponse(byte[] data, NetworkClient.Callback callback) {
        try {
            JSONParser parser = new JSONParser();
            Map<String, Object> result = parser.parseJSON(
                    new InputStreamReader(new ByteArrayInputStream(data), "UTF-8"));
            callback.onSuccess(result);
        } catch (IOException e) {
            callback.onFailure("JSON parse error: " + e.getMessage());
        }
    }

    private void handleErrorResponse(ConnectionRequest req, NetworkClient.Callback callback) {
        int code = req.getResponseCode();
        byte[] data = req.getResponseData();
        String errorMsg = "Server Error: " + code;

        if (data != null && data.length > 0) {
            try {
                JSONParser parser = new JSONParser();
                Map<String, Object> result = parser.parseJSON(
                        new InputStreamReader(new ByteArrayInputStream(data), "UTF-8"));

                if (result.containsKey("message")) errorMsg = (String) result.get("message");
                else if (result.containsKey("error")) errorMsg = (String) result.get("error");
            } catch (Exception e) {
                // ignore
            }
        }
        callback.onFailure(errorMsg);
    }
}
