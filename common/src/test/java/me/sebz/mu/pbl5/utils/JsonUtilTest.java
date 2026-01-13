package me.sebz.mu.pbl5.utils;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilTest {

    @Test
    public void testEmptyMap() {
        Map<String, Object> data = new HashMap<>();
        String json = JsonUtil.buildJson(data);
        assertEquals("{}", json);
    }

    @Test
    public void testNullMap() {
        String json = JsonUtil.buildJson(null);
        assertEquals("{}", json);
    }

    @Test
    public void testSimpleString() {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        String json = JsonUtil.buildJson(data);
        assertEquals("{\"key\":\"value\"}", json);
    }

    @Test
    public void testSimpleNumber() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", 123);
        String json = JsonUtil.buildJson(data);
        assertEquals("{\"id\":123}", json);
    }

    @Test
    public void testMultipleFields() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Alice");
        data.put("age", 30);
        String json = JsonUtil.buildJson(data);
        // Order is not guaranteed in HashMap, so we check for containment of parts or
        // construct a known order map if needed,
        // but for now let's hope it's small enough or just check parts.
        // Better: Check if valid JSON object with those fields.
        // For simplicity in this string builder implementation test:
        assertTrue(json.contains("\"name\":\"Alice\""));
        assertTrue(json.contains("\"age\":30"));
        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
    }

    @Test
    public void testEscaping() {
        Map<String, Object> data = new HashMap<>();
        data.put("quote", "He said \"Hello\"");
        data.put("slash", "C:\\Path");
        String json = JsonUtil.buildJson(data);
        assertTrue(json.contains("\"quote\":\"He said \\\"Hello\\\"\""));
        assertTrue(json.contains("\"slash\":\"C:\\\\Path\""));
    }

    @Test
    public void testNullValue() {
        Map<String, Object> data = new HashMap<>();
        data.put("nullable", null);
        String json = JsonUtil.buildJson(data);
        assertEquals("{\"nullable\":null}", json);
    }
}
