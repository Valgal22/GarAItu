package me.sebz.mu.pbl5;

import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.MultipartRequest;
import com.codename1.io.NetworkManager;
import me.sebz.mu.pbl5.net.NetworkClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GenericNetworkService implements NetworkClient {

    private static final String BASE_URL = "http://localhost:1880"; // Node-RED
    private static final String HEADER_ACCEPT = "Accept";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String MSG_NODE_RED_DOWN = "Connection failed. Is Node-RED running?";
    private static final String MSG_UPLOAD_FAILED = "Upload failed. Check connection.";

    private static final String KEY_RECOGNIZED_PERSON = "recognizedPerson";
    private static final String KEY_RECOGNIZED_CONTEXT = "recognizedContext";
    private static final String HEADER_RECOGNIZED_PERSON = "X-Recognized-Person";
    private static final String HEADER_RECOGNIZED_CONTEXT = "X-Recognized-Context";

    // Singleton (kept as-is; Sonar info-level)
    private static final GenericNetworkService INSTANCE = new GenericNetworkService();

    private GenericNetworkService() {
    }

    public static GenericNetworkService getInstance() {
        return INSTANCE;
    }

    @Override
    public void get(String endpoint, NetworkClient.Callback callback) {
        ConnectionRequest req = baseRequest(endpoint, "GET");
        req.addResponseListener(evt -> handleStandardResponse(req, callback, false));
        req.addExceptionListener(evt -> callback.onFailure(MSG_NODE_RED_DOWN));
        NetworkManager.getInstance().addToQueue(req);
    }

    @Override
    public void post(String endpoint, Map<String, Object> data, NetworkClient.Callback callback) {
        ConnectionRequest req = jsonBodyRequest(endpoint, "POST", data);
        req.setPost(true);
        req.setReadResponseForErrors(true);

        req.addResponseListener(evt -> handleStandardResponse(req, callback, false));
        req.addExceptionListener(evt -> callback.onFailure(MSG_NODE_RED_DOWN));
        NetworkManager.getInstance().addToQueue(req);
    }

    @Override
    public void put(String endpoint, Map<String, Object> data, NetworkClient.Callback callback) {
        ConnectionRequest req = jsonBodyRequest(endpoint, "PUT", data);
        req.setWriteRequest(true);

        req.addResponseListener(evt -> handleStandardResponse(req, callback, true));
        req.addExceptionListener(evt -> callback.onFailure(MSG_NODE_RED_DOWN));
        NetworkManager.getInstance().addToQueue(req);
    }

    @Override
    public void delete(String endpoint, NetworkClient.Callback callback) {
        ConnectionRequest req = baseRequest(endpoint, "DELETE");

        req.addResponseListener(evt -> handleStandardResponse(req, callback, true));
        req.addExceptionListener(evt -> callback.onFailure(MSG_NODE_RED_DOWN));
        NetworkManager.getInstance().addToQueue(req);
    }

    @Override
    public void upload(String endpoint, String filePath, Map<String, Object> data, NetworkClient.Callback callback) {
        MultipartRequest req = new MultipartRequest();
        req.setUrl(BASE_URL + endpoint);
        addToken(req);
        req.setReadResponseForErrors(true);

        Map<String, Object> safeData = (data != null) ? data : Collections.emptyMap();

        try {
            req.addData("file", filePath, "image/jpeg");
            for (Map.Entry<String, Object> entry : safeData.entrySet()) {
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
            if (isSuccess(code)) {
                handleUploadSuccess(req, callback);
            } else {
                handleErrorResponse(req, callback);
            }
        });

        req.addExceptionListener(evt -> callback.onFailure(MSG_UPLOAD_FAILED));
        NetworkManager.getInstance().addToQueue(req);
    }

    private ConnectionRequest baseRequest(String endpoint, String method) {
        ConnectionRequest req = new ConnectionRequest();
        req.setUrl(BASE_URL + endpoint);
        req.setHttpMethod(method);
        req.addRequestHeader(HEADER_ACCEPT, CONTENT_TYPE_JSON);
        addToken(req);
        return req;
    }

    private ConnectionRequest jsonBodyRequest(String endpoint, String method, Map<String, Object> data) {
        ConnectionRequest req = baseRequest(endpoint, method);
        req.setContentType(CONTENT_TYPE_JSON);
        String jsonBody = me.sebz.mu.pbl5.utils.JsonUtil.buildJson(data);
        req.setRequestBody(jsonBody);
        return req;
    }

    private void handleStandardResponse(ConnectionRequest req, NetworkClient.Callback callback,
            boolean allowEmptyBody) {
        int code = req.getResponseCode();
        if (!isSuccess(code)) {
            handleErrorResponse(req, callback);
            return;
        }

        byte[] body = req.getResponseData();
        if (allowEmptyBody && (body == null || body.length == 0)) {
            callback.onSuccess(new HashMap<>());
            return;
        }

        parseResponse(body, callback);
    }

    private boolean isSuccess(int code) {
        return code == 200 || code == 201 || code == 204;
    }

    private void handleUploadSuccess(MultipartRequest req, NetworkClient.Callback callback) {
        String contentType = req.getResponseContentType();
        if (contentType != null && contentType.startsWith("audio/")) {
            Map<String, Object> result = new HashMap<>();
            result.put("audioData", req.getResponseData());
            result.put("contentType", contentType);

            Map<String, String> headers = readRecognizedHeaders(req);
            String person = headers.get(KEY_RECOGNIZED_PERSON);
            String context = headers.get(KEY_RECOGNIZED_CONTEXT);

            if (person != null) {
                result.put(KEY_RECOGNIZED_PERSON, person);
            }
            if (context != null) {
                result.put(KEY_RECOGNIZED_CONTEXT, context);
            }

            callback.onSuccess(result);
            return;
        }

        parseResponse(req.getResponseData(), callback);
    }

    private Map<String, String> readRecognizedHeaders(MultipartRequest req) {
        Map<String, String> out = new HashMap<>();
        out.put(KEY_RECOGNIZED_PERSON, getHeaderSafely(req, HEADER_RECOGNIZED_PERSON));
        out.put(KEY_RECOGNIZED_CONTEXT, getHeaderSafely(req, HEADER_RECOGNIZED_CONTEXT));
        return out;
    }

    private String getHeaderSafely(Object req, String headerName) {
        try {
            java.lang.reflect.Method m = req.getClass().getMethod("getHeader", String.class);
            Object v = m.invoke(req, headerName);
            return (v != null) ? String.valueOf(v) : null;
        } catch (Exception ignored) {
            // Some CN1 implementations use getResponseHeader instead.
        }

        try {
            java.lang.reflect.Method m = req.getClass().getMethod("getResponseHeader", String.class);
            Object v = m.invoke(req, headerName);
            return (v != null) ? String.valueOf(v) : null;
        } catch (Exception ignored) {
            // Header not available; return null.
        }

        return null;
    }

    private void addToken(ConnectionRequest req) {
        String token = MemoryLens.getSessionToken();
        if (token != null) {
            req.addRequestHeader("X-Session-Id", token);
        }
    }

    private void parseResponse(byte[] data, NetworkClient.Callback callback) {
        try {
            JSONParser parser = new JSONParser();
            Map<String, Object> result = parser.parseJSON(
                    new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8));
            System.out.println("DEBUG: API Response Result: " + result); // logger issue ignored as requested
            callback.onSuccess(result);
        } catch (IOException e) {
            callback.onFailure("JSON parse error: " + e.getMessage());
        }
    }

    private void handleErrorResponse(ConnectionRequest req, NetworkClient.Callback callback) {
        int code = req.getResponseCode();
        byte[] data = req.getResponseData();

        String errorMsg = "Server Error: " + code;
        String parsed = tryParseErrorMessage(data);
        if (parsed != null && !parsed.isEmpty()) {
            errorMsg = parsed;
        }

        callback.onFailure(errorMsg);
    }

    private String tryParseErrorMessage(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            JSONParser parser = new JSONParser();
            Map<String, Object> result = parser.parseJSON(
                    new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8));

            Object msg = result.get("message");
            if (msg != null) {
                return String.valueOf(msg);
            }
            Object err = result.get("error");
            if (err != null) {
                return String.valueOf(err);
            }
        } catch (Exception ignored) {
            // Not JSON or parse failed; keep default message.
        }

        return null;
    }
}
