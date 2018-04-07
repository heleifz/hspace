package com.helei.hspace.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import com.helei.hspace.util.Pair;

class HttpRequest {
    
    static enum Method {
        GET, POST, 
        HEAD, PUT,
        DELETE
    }
    Method method;

    private String uri;
    private String version;

    private Map<String, String> header;
    private Map<String, String> getRequest = Collections.emptyMap();
    private Map<String, String> postRequest = Collections.emptyMap();
    private byte[] rawBody = new byte[0];
    private String rawQuery = "";

    private HttpRequest() {}

    // use regular expression to parse request
    private static Pattern requestLinePattern;
    private static Pattern headerPattern;
    private static final int CR = (int)'\r';
    private static final int LF = (int)'\n';

    static {
        StringBuilder requestPatternBuilder = new StringBuilder();
        requestPatternBuilder.append("(");
        // build method pattern
        int idx = 0;
        for (Method m : Method.values()) {
            if (idx > 0) {
                requestPatternBuilder.append("|");
            }
            requestPatternBuilder.append(m.toString());
            idx += 1;
        }
        requestPatternBuilder.append(")[ ]+([^ ]+)[ ]+(HTTP/[0-9]+\\.[0-9]+)");
        requestLinePattern = Pattern.compile(requestPatternBuilder.toString());
        headerPattern = Pattern.compile("([^ ]+)[ ]*:[ ]*(.+)");
    }
    
    static void getCRLF(InputStream reader) throws IOException, HttpFormatException {
        if (reader.read() != '\r') {
            throw new HttpFormatException("Expected CRLF");    
        }
        if (reader.read() != '\n') {
            throw new HttpFormatException("Expected CRLF");    
        }
    }

    static String readLine(InputStream input) throws HttpFormatException, IOException {
        StringBuilder builder = new StringBuilder();
        int currentByte = 0;
        boolean hasNewLine = false;
        while ((currentByte = input.read()) != -1) {
            if (currentByte == CR) {
                if (input.read() != LF) {
                    throw new HttpFormatException("Missing LF after CR");
                }
                hasNewLine = true;
                break;
            }
            builder.appendCodePoint(currentByte);
        }        
        if (!hasNewLine) {
            throw new HttpFormatException("Fail to read http request, missing CRLF maybe?");
        }
        return builder.toString();
    }

    static void parseRequestLine(InputStream input, HttpRequest req) throws IOException, HttpFormatException {
        String line = readLine(input);
        Matcher matcher = requestLinePattern.matcher(line);
        if (!matcher.matches()) {
            throw new HttpFormatException("Request line format error.");
        }
        req.method = Method.valueOf(matcher.group(1));
        req.rawQuery = matcher.group(2);
        req.version = matcher.group(3);
        Pair<String, Map<String, String>> parsed = parseQuery(req.rawQuery);
        req.getRequest = parsed.getSecond();
        req.uri = parsed.getFirst();
    }

    static Map<String, String> parseParams(String params) throws HttpFormatException {
        int paramPosition = 0; 
        int queryLength = params.length();
        HashMap<String, String> paramMap = new HashMap<>();
        while (paramPosition < queryLength) {
            int equalPosition = params.indexOf('=', paramPosition);
            int andPosition = params.indexOf('&', paramPosition);
            if (andPosition == -1) {
                andPosition = queryLength;
            }
            if (equalPosition == -1 && equalPosition > andPosition) {
                throw new HttpFormatException("Missing '=' in query string");
            }
            paramMap.put(params.substring(paramPosition, equalPosition),
                         params.substring(equalPosition + 1, andPosition));
            paramPosition = andPosition + 1;
        }
        return paramMap;
    }

    static Pair<String, Map<String, String>> parseQuery(String query) throws HttpFormatException {

        try {
            query = URLDecoder.decode(query, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new HttpFormatException("Cannot decode query string.");
        }
        int paramStart = query.indexOf('?');  
        int anchorPosition = query.indexOf('#');
        if (paramStart == -1 && anchorPosition == -1) {
            return new Pair<>(query, Collections.emptyMap());
        } else if (paramStart == -1) {
            return new Pair<>(query.substring(0, anchorPosition), Collections.emptyMap());
        }
        String uri = query.substring(0, paramStart);
        int paramPosition = paramStart + 1;
        int queryLength = query.length();
        if (anchorPosition != -1) {
            queryLength = anchorPosition;
        }
        return new Pair<>(uri, parseParams(query.substring(paramPosition, queryLength)));
    }

    static void parseHeaders(InputStream input, HttpRequest req) throws IOException, HttpFormatException {
        HashMap<String, String> headers = new HashMap<>();

        while (true) {
            String line = readLine(input);
            if (line.isEmpty()) {
                break;
            }
            Matcher matcher = headerPattern.matcher(line);
            if (!matcher.matches()) {
                throw new HttpFormatException("Illegal header format:" + line);
            }
            String name = matcher.group(1);
            String value = matcher.group(2);
            headers.put(name, value);
        }
        req.header = headers;
    }

    static void parseBody(InputStream input, HttpRequest req) throws IOException, HttpFormatException {

        int length = Integer.valueOf(req.header.getOrDefault("Content-Length", "0"));
        if (length <= 0) {
            // no request body
            return;
        }
        String contentEncoding = req.header.getOrDefault("Content-Encoding", null);
        // TODO deflate encoding (zlib)
        if (contentEncoding != null) {
            if (contentEncoding.equals("gzip")) {
                input = new GZIPInputStream(input);
            } else {
                throw new HttpFormatException("Unsupported content encoding: " + contentEncoding);
            }
        }
        // read "length" bytes
        byte[] buffer = new byte[length];
        int position = 0;
        int actualRead = 0;
        while ((actualRead = input.read(buffer, position, length - position)) != -1) {
            position += actualRead;
            if (position >= length) {
                break;
            }
        }
        req.rawBody = buffer;
        // optionally parse urlecoded data
        // TODO parse multipart-form data
        // TODO transfer encoding chunked
        if (req.header.getOrDefault("Content-Type", "").equals("application/x-www-form-urlencoded")) {
            String str = new String(req.rawBody, StandardCharsets.US_ASCII);
            req.postRequest = parseParams(str);
        }
    }

    public static HttpRequest parse(InputStream input) throws IOException, HttpFormatException {
        HttpRequest req = new HttpRequest();
        BufferedInputStream reader = new BufferedInputStream(input);
        parseRequestLine(reader, req);
        parseHeaders(reader, req);
        parseBody(reader, req);
        return req;
    }

	public Method getMethod() {
        return method;
    }

	public String getHeader(String name) {
        return header.getOrDefault(name, null);
    }

	public String getParameter(String name) {
        String result = getRequest.getOrDefault(name, null);
        if (result == null) {
            result = postRequest.getOrDefault(name, null);
        }
        return result;
    }

	public String getVersion() {
		return version;
    }
    
	public String getUri() {
		return uri;
    }

	public byte[] getRawBody() {
		return rawBody;
    }

	public String getRawQuery() {
		return rawQuery;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[[ HTTP Request ]]\n");
        builder.append("Method:" + this.method);
        builder.append("\nVersion:" + this.version);
        builder.append("\nURI:" + this.uri);
        builder.append("\nGetRequest:" + this.getRequest);
        builder.append("\nPostRequest:" + this.postRequest + "\n");
        for (Map.Entry<String, String> ent : header.entrySet()) {
            builder.append("[" + ent.getKey() + "] " + "[" + ent.getValue() + "]\n"); 
        }
        return builder.toString();
    }
    
}