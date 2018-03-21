package com.helei.hspace.server;

import com.helei.hspace.util.PatternMatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class RequestDispatcher implements RequestProcessor {


	Map<String, PatternMatcher> matchers = new HashMap<>();

    ConcurrentHashMap<String, Object> objectCache = new ConcurrentHashMap<>();

	@Override
	public String handle(HttpServletRequest req, HttpServletResponse resp) {
        String uri = req.getRequestURI();
        String method = req.getMethod();
        System.out.println(method);
        System.out.println(matchers);
        if (!matchers.containsKey(method)) {
            return notFound(resp, "Unsupported method:" + method);
        }
        PatternMatcher.Result match = matchers.get(method).match(uri);
        if (!match.isSuccess()) {
            return notFound(resp, "Illegal URI:" + uri);
        }
        String[] captures = match.getCaptures();
        String id = match.getPatternId();
        String[] parts = id.split(" ");
        // get method name
        String className = parts[0];
        String methodName = parts[1];
        // find method
        try {
            Class<?> cls = Class.forName(className);
            Method m = cls.getMethod(methodName, String[].class);
            Object obj = null;
            if (objectCache.containsKey(id)) {
                obj = objectCache.get(id);
            } else {
                obj = cls.newInstance();
                objectCache.put(id, obj);
            }
            // NOTE: invoke with varargs, must put all params into array.
            return (String)m.invoke(obj, new Object[] {captures});
        } catch (Exception exp) {
            exp.printStackTrace();
            return notFound(resp, exp.getMessage());
        } 
    }

    public String notFound(HttpServletResponse resp, String message) {
        resp.setStatus(404);
        return message;
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
        if (!matchers.containsKey(method)) {
            matchers.put(method, new PatternMatcher());
        }
        matchers.get(method).register(urlPattern, className + " " + methodName);
        return true;
    }

}