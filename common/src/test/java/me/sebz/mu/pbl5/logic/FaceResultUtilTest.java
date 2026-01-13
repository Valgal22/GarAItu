package me.sebz.mu.pbl5.logic;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class FaceResultUtilTest {

    @Test
    public void nullResponse_unknown() {
        assertEquals("Result: Unknown", FaceResultUtil.buildMessageFromResponse(null, 0.4));
    }

    @Test
    public void rootMissing_orNullList_keepsDefaultMessage_coversLine18False() {
        Map<String,Object> resp1 = new HashMap<>();
        assertEquals("Result: Unknown", FaceResultUtil.buildMessageFromResponse(resp1, 0.4));

        Map<String,Object> resp2 = new HashMap<>();
        resp2.put("root", null);
        assertEquals("Result: Unknown", FaceResultUtil.buildMessageFromResponse(resp2, 0.4));
    }

    @Test
    public void emptyRootList_keepsDefaultMessage_coversLine18TrueButEmpty() {
        Map<String,Object> resp = new HashMap<>();
        resp.put("root", new ArrayList<Map<String,Object>>());
        assertEquals("Result: Unknown", FaceResultUtil.buildMessageFromResponse(resp, 0.4));
    }

    @Test
    public void topNull_keepsDefaultMessage_coversLine20False() {
        List<Map<String,Object>> root = new ArrayList<>();
        root.add(null); // top == null

        Map<String,Object> resp = new HashMap<>();
        resp.put("root", root);

        assertEquals("Result: Unknown", FaceResultUtil.buildMessageFromResponse(resp, 0.4));
    }

    @Test
    public void similarityNull_keepsDefaultMessage_coversLine22And25ScoreNull() {
        Map<String,Object> top = new HashMap<>();
        top.put("similarity", null);

        List<Map<String,Object>> root = new ArrayList<>();
        root.add(top);

        Map<String,Object> resp = new HashMap<>();
        resp.put("root", root);

        assertEquals("Result: Unknown", FaceResultUtil.buildMessageFromResponse(resp, 0.4));
    }

    @Test
    public void similarityBelowThreshold_keepsDefaultMessage_coversLine25False() {
        Map<String,Object> top = new HashMap<>();
        top.put("similarity", 0.2);
        top.put("name", "Ana");

        List<Map<String,Object>> root = new ArrayList<>();
        root.add(top);

        Map<String,Object> resp = new HashMap<>();
        resp.put("root", root);

        assertEquals("Result: Unknown", FaceResultUtil.buildMessageFromResponse(resp, 0.4));
    }

    @Test
    public void similarityAboveThreshold_withContext_coversLines28MessageContextBranch() {
        Map<String,Object> top = new HashMap<>();
        top.put("similarity", 0.9);        // Number branch in safeDouble
        top.put("name", "Ana");
        top.put("context", "Your sister");

        List<Map<String,Object>> root = new ArrayList<>();
        root.add(top);

        Map<String,Object> resp = new HashMap<>();
        resp.put("root", root);

        assertEquals("This is Ana (Your sister)", FaceResultUtil.buildMessageFromResponse(resp, 0.4));
    }

    @Test
    public void similarityAboveThreshold_noContext_orBlankContext_coversElseMessageBranch() {
        // context null
        Map<String,Object> top1 = new HashMap<>();
        top1.put("similarity", 0.9);
        top1.put("name", "Ana");
        top1.put("context", null);

        Map<String,Object> resp1 = new HashMap<>();
        resp1.put("root", Collections.singletonList(top1));
        assertEquals("This is Ana", FaceResultUtil.buildMessageFromResponse(resp1, 0.4));

        // context blank
        Map<String,Object> top2 = new HashMap<>();
        top2.put("similarity", 0.9);
        top2.put("name", "Ana");
        top2.put("context", "   "); // trim empty

        Map<String,Object> resp2 = new HashMap<>();
        resp2.put("root", Collections.singletonList(top2));
        assertEquals("This is Ana", FaceResultUtil.buildMessageFromResponse(resp2, 0.4));
    }

    @Test
    public void nameNull_stillProducesMessage_coversNsUnknown() {
        Map<String,Object> top = new HashMap<>();
        top.put("similarity", 0.9);
        top.put("name", null);
        top.put("context", "Friend");

        Map<String,Object> resp = new HashMap<>();
        resp.put("root", Collections.singletonList(top));

        assertEquals("This is Unknown (Friend)", FaceResultUtil.buildMessageFromResponse(resp, 0.4));
    }

    // ---- safeDouble branches (38-42) via reflection ----

    @Test
    public void safeDouble_null_returnsNull_coversLine38() throws Exception {
        assertNull(invokeSafeDouble(null));
    }

    @Test
    public void safeDouble_doubleInstance_coversLine39() throws Exception {
        assertEquals(1.25, invokeSafeDouble(Double.valueOf(1.25)), 0.000001);
    }

    @Test
    public void safeDouble_numberInstance_coversLine40() throws Exception {
        assertEquals(7.0, invokeSafeDouble(Integer.valueOf(7)), 0.000001);
    }

    @Test
    public void safeDouble_parseOk_coversLine41Try() throws Exception {
        assertEquals(3.14, invokeSafeDouble("3.14"), 0.000001);
    }

    @Test
    public void safeDouble_parseFail_coversLine42Catch() throws Exception {
        assertNull(invokeSafeDouble("not-a-number"));
    }

    @Test
    public void faceResultUtil_privateConstructor_covered() throws Exception {
        Constructor<FaceResultUtil> c = FaceResultUtil.class.getDeclaredConstructor();
        c.setAccessible(true);
        assertNotNull(c.newInstance());
    }

    private Double invokeSafeDouble(Object v) throws Exception {
        java.lang.reflect.Method m = FaceResultUtil.class.getDeclaredMethod("safeDouble", Object.class);
        m.setAccessible(true);
        return (Double) m.invoke(null, v);
    }
}
