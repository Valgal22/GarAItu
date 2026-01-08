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

    private static final String BASE_URL = "http://localhost:1880"; // Node-RED
    private static GenericNetworkService instance;

    private GenericNetworkService() {
    }

    public static GenericNetworkService getInstance() {
        if (instance == null) {
            instance = new GenericNetworkService();
        }
        return instance;
    }

    /*
     * ==========================
     * CALLBACK
     * ==========================
     */
    public interface NetworkCallback {
        void onSuccess(Map<String, Object> response);

        void onFailure(String errorMessage);
    }

    /*
     * ==========================
     * GET (JSON)
     * ==========================
     */
    public void get(String endpoint, NetworkCallback callback) {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(BASE_URL + endpoint);
        req.setHttpMethod("GET");

        req.addRequestHeader("Accept", "application/json");

        String token = MemoryLens.getSessionToken();
        if (token != null) {
            req.addRequestHeader("X-Session-Id", token);
        }

        req.addResponseListener(evt -> {
            int code = req.getResponseCode();
            if (code == 200 || code == 201) {
                parseResponse(req.getResponseData(), callback);
            } else {
                callback.onFailure("Server Error: " + code);
            }
        });

        req.addExceptionListener(evt -> callback.onFailure("Connection failed. Is Node-RED running?"));

        NetworkManager.getInstance().addToQueue(req);
    }

    /*
     * ==========================
     * POST (JSON REAL)
     * ==========================
     */
    public void post(String endpoint, Map<String, Object> data, NetworkCallback callback) {

        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(BASE_URL + endpoint);

        // 🔑 CLAVES PARA JSON REAL
        req.setPost(true);
        req.setHttpMethod("POST");
        req.setContentType("application/json");
        req.addRequestHeader("Accept", "application/json");

        String token = MemoryLens.getSessionToken();
        if (token != null) {
            req.addRequestHeader("X-Session-Id", token);
        }

        // JSON body
        String jsonBody = me.sebz.mu.pbl5.utils.JsonUtil.buildJson(data);
        req.setRequestBody(jsonBody);
        req.setReadResponseForErrors(true);

        req.addResponseListener(evt -> {
            int code = req.getResponseCode();
            if (code == 200 || code == 201) {
                parseResponse(req.getResponseData(), callback);
            } else {
                callback.onFailure("Server Error: " + code);
            }
        });

        req.addExceptionListener(evt -> callback.onFailure("Connection failed. Is Node-RED running?"));

        NetworkManager.getInstance().addToQueue(req);
    }

    /*
     * ==========================
     * UPLOAD (MULTIPART)
     * ==========================
     */
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
            int code = req.getResponseCode();
            if (code == 200 || code == 201) {
                parseResponse(req.getResponseData(), callback);
            } else {
                callback.onFailure("Server Error: " + code);
            }
        });

        req.addExceptionListener(evt -> callback.onFailure("Upload failed. Check connection."));

        NetworkManager.getInstance().addToQueue(req);
    }

    /*
     * ==========================
     * RESPONSE PARSER
     * ==========================
     */
    private void parseResponse(byte[] data, NetworkCallback callback) {
        try {
            JSONParser parser = new JSONParser();
            Map<String, Object> result = parser.parseJSON(
                    new InputStreamReader(new ByteArrayInputStream(data), "UTF-8"));
            callback.onSuccess(result);
        } catch (IOException e) {
            callback.onFailure("JSON parse error: " + e.getMessage());
        }
    }
}
