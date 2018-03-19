package com.helei.hspace.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestDispatcher implements RequestProcessor {

	@Override
	public String handle(HttpServletRequest req, HttpServletResponse resp) {
		return null;
    }
    
    /**
     * Register a processor in request dispatcher
     * 
     * @param urlPattern request url pattern
     * @param method HTTP method
     * @param className processor class name
     * @param methodName processor method name
     */
    public boolean registerProcessor(String urlPattern, String method, String className, String methodName) {
        return false;
    }

}