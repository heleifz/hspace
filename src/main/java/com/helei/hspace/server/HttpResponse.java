package com.helei.hspace.server;

import java.io.OutputStream;
import java.util.*;

public class HttpResponse {

    private HttpStatus status;
    private String version;
    private Map<String, String> header;
    private String body;

    private HttpResponse() {}

    /**
     * make a default http response object from request
     */
    public static HttpResponse defaultResponse(HttpRequest request) {
        return null;
    }

    /**
     * serialize result to outputstream
     */
    public void serialize(OutputStream output) {

    }

    
}