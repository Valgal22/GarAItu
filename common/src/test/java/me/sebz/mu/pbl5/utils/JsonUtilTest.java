package me.sebz.mu.pbl5.utils;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilTest {

    @Test
    void buildJson_returns_empty_object_when_null() {
        assertEquals("{}", JsonUtil.buildJson(null));
    }

    @Test
    void buildJson_formats_strings_numbers_and_null() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", "Maider");
        data.put("age", 30);
        data.put("x", null);

        String json = JsonUtil.buildJson(data);

        // Como usas LinkedHashMap, el orden es estable.
        assertEquals("{\"name\":\"Maider\",\"age\":30,\"x\":null}", json);
    }

    @Test
    void buildJson_escapes_quotes_and_backslashes_in_keys_and_values() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("a\"b", "c\\d\"e");

        String json = JsonUtil.buildJson(data);

        assertEquals("{\"a\\\"b\":\"c\\\\d\\\"e\"}", json);
    }
}
