package me.sebz.mu.pbl5;

import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.MultipartRequest;
import com.codename1.io.NetworkManager;
import me.sebz.mu.pbl5.net.NetworkClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class GenericNetworkService implements NetworkClient {

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

    @Override
    public void get(String endpoint, Callback callback) {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(BASE_URL + endpoint);
        req.setHttpMethod("GET");
        req.addRequestHeader("Accept", "application/json");
        addToken(req);

        req.addResponseListener(evt -> {
            int code = req.getResponseCode();
            if (code == 200 || code == 201) {
                parseResponse(req.getResponseData(), callback);
            } else {
                handleErrorResponse(req, callback);
            }
        });

        req.addExceptionListener(evt -> callback.onFailure("Connection failed. Is Node-RED running?"));
        NetworkManager.getInstance().addToQueue(req);
    }

    @Override
    public void post(String endpoint, Map<String, Object> data, Callback callback) {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(BASE_URL + endpoint);
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
            if (code == 200 || code == 201) {
                parseResponse(req.getResponseData(), callback);
            } else {
                handleErrorResponse(req, callback);
            }
        });

        req.addExceptionListener(evt -> callback.onFailure("Connection failed. Is Node-RED running?"));
        NetworkManager.getInstance().addToQueue(req);
    }

    @Override
    public void put(String endpoint, Map<String, Object> data, Callback callback) {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(BASE_URL + endpoint);
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
                if (req.getResponseData() != null && req.getResponseData().length > 0) {
                    parseResponse(req.getResponseData(), callback);
                } else {
                    callback.onSuccess(new java.util.HashMap<>());
                }
            } else {
                handleErrorResponse(req, callback);
            }
        });

        req.addExceptionListener(evt -> callback.onFailure("Connection failed. Is Node-RED running?"));
        NetworkManager.getInstance().addToQueue(req);
    }

    @Override
    public void delete(String endpoint, Callback callback) {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(BASE_URL + endpoint);
        req.setHttpMethod("DELETE");
        req.addRequestHeader("Accept", "application/json");
        addToken(req);

        req.addResponseListener(evt -> {
            int code = req.getResponseCode();
            if (code == 200 || code == 201 || code == 204) {
                if (req.getResponseData() != null && req.getResponseData().length > 0) {
                    parseResponse(req.getResponseData(), callback);
                } else {
                    callback.onSuccess(new java.util.HashMap<>());
                }
            } else {
                handleErrorResponse(req, callback);
            }
        });

        req.addExceptionListener(evt -> callback.onFailure("Connection failed. Is Node-RED running?"));
        NetworkManager.getInstance().addToQueue(req);
    }

    @Override
    public void upload(String endpoint, String filePath, Map<String, Object> data, Callback callback) {
        MultipartRequest req = new MultipartRequest();
        req.setUrl(BASE_URL + endpoint);
        addToken(req);

        try {
            req.addData("file", filePath, "image/jpeg");
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Object val = entry.getValue();
                if (val != null) {
                    req.addArgument(entry.getKey(), val.toString());
                }
            }
        } catch (IOException e) {
            callback.onFailure("File error: " + e.getMessage());
            return;
        }

        req.addResponseListener(evt -> {
            int code = req.getResponseCode();
            if (code == 200 || code == 201) {
                String contentType = req.getResponseContentType();
                if (contentType != null && contentType.startsWith("audio/")) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("audioData", req.getResponseData());
                    result.put("contentType", contentType);

                    String recognizedPerson = null;
                    try {
                        java.lang.reflect.Method m = req.getClass().getMethod("getHeader", String.class);
                        recognizedPerson = (String) m.invoke(req, "X-Recognized-Person");
                    } catch (Exception e1) {
                        try {
                            java.lang.reflect.Method m = req.getClass().getMethod("getResponseHeader", String.class);
                            recognizedPerson = (String) m.invoke(req, "X-Recognized-Person");
                        } catch (Exception e2) {
                        }
                    }
                    if (recognizedPerson != null) {
                        result.put("recognizedPerson", recognizedPerson);
                    }
                    callback.onSuccess(result);
                } else {
                    parseResponse(req.getResponseData(), callback);
                }
            } else {
                handleErrorResponse(req, callback);
            }
        });

        req.addExceptionListener(evt -> callback.onFailure("Upload failed. Check connection."));
        NetworkManager.getInstance().addToQueue(req);
    }

    private void addToken(ConnectionRequest req) {
        String token = MemoryLens.getSessionToken();
        if (token != null) {
            req.addRequestHeader("X-Session-Id", token);
        }
    }

    private void parseResponse(byte[] data, Callback callback) {
        try {
            JSONParser parser = new JSONParser();
            Map<String, Object> result = parser.parseJSON(
                    new InputStreamReader(new ByteArrayInputStream(data), "UTF-8"));
            System.out.println("DEBUG: API Response Result: " + result);
            callback.onSuccess(result);
        } catch (IOException e) {
            callback.onFailure("JSON parse error: " + e.getMessage());
        }
    }

    private void handleErrorResponse(ConnectionRequest req, Callback callback) {
        int code = req.getResponseCode();
        byte[] data = req.getResponseData();
        String errorMsg = "Server Error: " + code;

        if (data != null && data.length > 0) {
            try {
                JSONParser parser = new JSONParser();
                Map<String, Object> result = parser.parseJSON(
                        new InputStreamReader(new ByteArrayInputStream(data), "UTF-8"));

                if (result.containsKey("message")) {
                    errorMsg = (String) result.get("message");
                } else if (result.containsKey("error")) {
                    errorMsg = (String) result.get("error");
                }
            } catch (Exception e) {
                // ignore
            }
        }
        callback.onFailure(errorMsg);
    }
}
