package me.sebz.mu.pbl5;

import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.MultipartRequest;
import com.codename1.io.NetworkManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class GenericNetworkService {

    private static final String BASE_URL = "http://localhost:1880"; // Node-RED URL
    private static GenericNetworkService instance;

    private GenericNetworkService() {
    }

    public static GenericNetworkService getInstance() {
        if (instance == null) {
            instance = new GenericNetworkService();
        }
        return instance;
    }

    public interface NetworkCallback {
        void onSuccess(Map<String, Object> response);

        void onFailure(String errorMessage);
    }

    public void get(String endpoint, NetworkCallback callback) {
        String fullUrl = BASE_URL + endpoint;
        System.out.println("[Network] GET Request: " + fullUrl);

        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(fullUrl);
        req.setHttpMethod("GET");
        req.addRequestHeader("Accept", "application/json");
        String token = MemoryLens.getSessionToken();
        if (token != null) {
            req.addRequestHeader("X-Session-Id", token);
        }

        req.addResponseListener(evt -> {
            System.out.println("[Network] Response Code: " + req.getResponseCode());
            if (req.getResponseCode() == 200) {
                parseResponse(req.getResponseData(), callback);
            } else {
                callback.onFailure("Server Error: " + req.getResponseCode());
            }
        });
        req.addExceptionListener(evt -> {
            System.out.println("[Network] Exception: " + evt.getError());
            callback.onFailure("Connection Failed. Please check if Node-RED is running at " + BASE_URL);
        });
        req.setFailSilently(true);
        NetworkManager.getInstance().addToQueue(req);
    }

    public void post(String endpoint, Map<String, Object> data, NetworkCallback callback) {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(BASE_URL + endpoint);
        req.setHttpMethod("POST");
        req.addRequestHeader("Content-Type", "application/json");
        req.addRequestHeader("Accept", "application/json");
        String token = MemoryLens.getSessionToken();
        if (token != null) {
            req.addRequestHeader("X-Session-Id", token);
        }

        String jsonBody = me.sebz.mu.pbl5.utils.JsonUtil.buildJson(data);
        req.setRequestBody(jsonBody);

        req.addResponseListener(evt -> {
            if (req.getResponseCode() == 200 || req.getResponseCode() == 201) {
                parseResponse(req.getResponseData(), callback);
            } else {
                callback.onFailure("Server Error: " + req.getResponseCode());
            }
        });
        req.addExceptionListener(evt -> {
            callback.onFailure("Connection Failed. Please check your internet or server status.");
        });
        req.setFailSilently(true);
        NetworkManager.getInstance().addToQueue(req);
    }

    public void upload(String endpoint, String filePath, Map<String, Object> data, NetworkCallback callback) {
        MultipartRequest req = new MultipartRequest();
        req.setUrl(BASE_URL + endpoint);
        String token = MemoryLens.getSessionToken();
        if (token != null) {
            req.addRequestHeader("X-Session-Id", token);
        }

        try {
            req.addData("file", filePath, "image/jpeg");
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                req.addArgument(entry.getKey(), entry.getValue().toString());
            }
        } catch (IOException e) {
            callback.onFailure("File error: " + e.getMessage());
            return;
        }

        req.addResponseListener(evt -> {
            if (req.getResponseCode() == 200 || req.getResponseCode() == 201) {
                parseResponse(req.getResponseData(), callback);
            } else {
                callback.onFailure("Server Error: " + req.getResponseCode());
            }
        });
        req.addExceptionListener(evt -> {
            callback.onFailure("Connection Failed. Please check your internet or server status.");
        });
        req.setFailSilently(true);
        NetworkManager.getInstance().addToQueue(req);
    }

    private void parseResponse(byte[] data, NetworkCallback callback) {
        try {
            JSONParser p = new JSONParser();
            Map<String, Object> result = p.parseJSON(new InputStreamReader(new ByteArrayInputStream(data), "UTF-8"));
            callback.onSuccess(result);
        } catch (IOException e) {
            callback.onFailure("JSON Parse Error: " + e.getMessage());
        }
    }
}
