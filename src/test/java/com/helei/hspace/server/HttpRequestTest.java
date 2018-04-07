package com.helei.hspace.server;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.helei.hspace.server.HttpRequest.Method;

import org.junit.*;

public class HttpRequestTest {

    private InputStream makeRequest(String str) {
        byte[] buf = StandardCharsets.UTF_8.encode(str).array();
        System.out.println("fuck..:" + buf.length);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buf);
        return inputStream;
    }

    @Test
    public void testSimpleGet() throws Exception {
        InputStream inp = makeRequest("GET / HTTP/1.1\r\n\r\n");
        HttpRequest req = HttpRequest.parse(inp);
        assertEquals(Method.GET, req.getMethod());
        assertEquals("HTTP/1.1", req.getVersion());
        assertEquals("/", req.getUri());
    }

    @Test
    public void testParam() throws Exception {
        InputStream inp = makeRequest("POST /ab/c?a=b&cc=dd#anchors  HTTP/0.9\r\n\r\n");
        HttpRequest req = HttpRequest.parse(inp);
        assertEquals(Method.POST, req.getMethod());
        assertEquals("/ab/c", req.getUri());
        assertEquals("b", req.getParameter("a"));
        assertEquals("dd", req.getParameter("cc"));
        assertEquals("/ab/c?a=b&cc=dd#anchors", req.getRawQuery());
    }

    @Test
    public void testHeader() throws Exception {
        InputStream inp = makeRequest("POST /ab/c?a=b&cc=dd#anchors  HTTP/0.9\r\nHost: www.baidu.com\r\nContent-Type: hello\r\n\r\n");
        HttpRequest req = HttpRequest.parse(inp);
        assertEquals(Method.POST, req.getMethod());
        assertEquals("/ab/c", req.getUri());
        assertEquals("b", req.getParameter("a"));
        assertEquals("dd", req.getParameter("cc"));
        assertEquals("www.baidu.com", req.getHeader("Host"));
        assertEquals("hello", req.getHeader("Content-Type"));
    }

    @Test
    public void testRawBody() throws Exception {
        InputStream inp = makeRequest("POST /ab/c?a=b&cc=dd#anchors  HTTP/0.9\r\nHost: www.baidu.com\r\nContent-Type: hello\r\nContent-Length: 11\r\n\r\nfuckyou!!!!");
        HttpRequest req = HttpRequest.parse(inp);
        assertEquals(Method.POST, req.getMethod());
        assertEquals("/ab/c", req.getUri());
        assertEquals("b", req.getParameter("a"));
        assertEquals("dd", req.getParameter("cc"));
        assertEquals("www.baidu.com", req.getHeader("Host"));
        assertEquals("hello", req.getHeader("Content-Type"));
        assertEquals("fuckyou!!!!", new String(req.getRawBody(), StandardCharsets.UTF_8));
    }

}