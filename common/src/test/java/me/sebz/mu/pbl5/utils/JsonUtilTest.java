package me.sebz.mu.pbl5.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilTest {

    @Test
    public void buildJson_nullMap_returnsEmptyObject() {
        assertEquals("{}", JsonUtil.buildJson(null));
    }

    @Test
    public void buildJson_stringsEscapeQuotesAndBackslash() {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("a", "hello");
        m.put("b", "x\"y");
        m.put("c", "path\\file");

        String json = JsonUtil.buildJson(m);

        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
        assertTrue(json.contains("\"a\":\"hello\""));
        assertTrue(json.contains("\"b\":\"x\\\"y\""));
        assertTrue(json.contains("\"c\":\"path\\\\file\""));
    }

    @Test
    public void buildJson_numbersAndNull() {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("n", 10);
        m.put("x", null);

        String json = JsonUtil.buildJson(m);

        assertTrue(json.contains("\"n\":10"));
        assertTrue(json.contains("\"x\":null"));
    }

    @Test
    public void buildJson_nullKey_coversEscapeNullBranch() {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put(null, "v");
        String json = JsonUtil.buildJson(m);

        // escape(null) devuelve ""
        assertTrue(json.contains("\"\":\"v\""));
    }

    @Test
    public void jsonUtil_privateConstructor_covered() throws Exception {
        Constructor<JsonUtil> c = JsonUtil.class.getDeclaredConstructor();
        c.setAccessible(true);
        assertNotNull(c.newInstance());
    }
}
