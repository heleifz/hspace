package com.helei.hspace.router;
import static org.junit.Assert.assertEquals;

import org.junit.*;

import java.util.*;

import com.helei.hspace.router.Token.Type;

public class TokenTest {

    @BeforeClass
    public static void suiteSetup() {
    }
  
    @AfterClass
    public static void suiteTeardown() {
    }
  
    @Before
    public void setup() {
    }
  
    @After
    public void teardown() {
      // Tear down something for the test.
    }
  
    @Test
    public void testTokenizeSimple() throws Exception {
        String query = "/hello/world";
        List<Token> parts = Token.tokenize(query);
        assertEquals(2, parts.size());
        assertEquals(Type.TEXT, parts.get(0).getType());
        assertEquals("hello", parts.get(0).getText());
        assertEquals(Type.TEXT, parts.get(1).getType());
        assertEquals("world", parts.get(1).getText());
        query = "/hello/world34/";
        parts = Token.tokenize(query);
        assertEquals(2, parts.size());
        assertEquals(Type.TEXT, parts.get(0).getType());
        assertEquals("hello", parts.get(0).getText());
        assertEquals(Type.TEXT, parts.get(1).getType());
        assertEquals("world34", parts.get(1).getText());
    }

    @Test
    public void testMethod() throws Exception {
        String query = "[GET, POST]  /hello/world?sdaf=ad&asdf=3";
        List<Token> parts = Token.tokenize(query);
        assertEquals(3, parts.size());
        assertEquals(Type.METHOD, parts.get(0).getType());
        assertEquals("GET,POST", parts.get(0).getText());
        assertEquals(Type.TEXT, parts.get(1).getType());
        assertEquals("hello", parts.get(1).getText());
        assertEquals(Type.TEXT, parts.get(2).getType());
        assertEquals("world", parts.get(2).getText());
    }

    @Test
    public void testCapture() throws Exception {
        String query = "[GET, POST]  /:hello/world?sdaf=ad&asdf=3";
        List<Token> parts = Token.tokenize(query);
        assertEquals(3, parts.size());
        assertEquals(Type.METHOD, parts.get(0).getType());
        assertEquals("GET,POST", parts.get(0).getText());
        assertEquals(Type.CAPTURE, parts.get(1).getType());
        assertEquals("hello", parts.get(1).getText());
        assertEquals(Type.TEXT, parts.get(2).getType());
        assertEquals("world", parts.get(2).getText());
    }
}