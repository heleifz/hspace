package com.helei.hspace.server;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helei.hspace.router.PatternTree;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;;

public class RequestDispatcher implements RequestProcessor {

	PatternTree tree = new PatternTree();

    ConcurrentHashMap<String, Object> objectCache = new ConcurrentHashMap<>();

    private Object convertTypedParam(Parameter p, String capture) throws Exception {
        Class<?> pc = p.getType();
        System.out.println(pc.toString());
        if (pc == int.class || pc == Integer.class) {
            return Integer.valueOf(capture);
        } else if (pc == long.class || pc == Long.class) {
            return Long.valueOf(capture);
        } else if (pc == String.class) {
            return capture;
        } else {
            throw new Exception("Illegal param type.");
        }
    }

    // TODO: 
    // 1. passing request and response if needed
    // 2. component autowire
    private String invokeMethodWithTypedParams(String id, String className, String methodName, String[] captures, 
                                               HttpServletRequest req, HttpServletResponse resp) throws
        ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, Exception {
        Class<?> cls = Class.forName(className);
        // Method m = cls.getMethod(methodName, String[].class);
        Method[] methods = cls.getMethods();
        Method method = null;
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                method = m;
                break;
            }
        }
        if (method == null) {
            throw new NoSuchMethodException(methodName);
        }
        Object obj = null;
        if (objectCache.containsKey(id)) {
            obj = objectCache.get(id);
        } else {
            obj = cls.newInstance();
            objectCache.put(id, obj);
        }
        // get type of params, only support primitive type + String
        Parameter[] params = method.getParameters();
        if (params.length < 2 || params[0].getType() != HttpServletRequest.class || params[1].getType() != HttpServletResponse.class) {
            throw new Exception("Illegal param types"); 
        }
        params = Arrays.copyOfRange(params, 2, params.length);

        if (params.length > captures.length && !(params.length == captures.length + 1 && params[params.length - 1].isVarArgs())) {
            throw new Exception("Mismatch parameter.");
        }
        if (params.length < captures.length) { 
            if (params.length == 0) {
                throw new Exception("Mismatch parameter. Method param is zero.");
            }
            if (!params[params.length - 1].isVarArgs()) {
                throw new Exception("Last param is not varargs");
            }
            if (params[params.length - 1].getType() != String[].class) {
                throw new Exception("Last param is not string varargs");
            }
        }

        // only support integer, long, String 
        Object[] finalParams = new Object[params.length + 2];
        for (int i = 0; i < params.length - 1; ++i) {
            finalParams[i + 2] = convertTypedParam(params[i], captures[i]);
        }
        if (params[params.length - 1].isVarArgs()) {
            finalParams[params.length - 1 + 2] = Arrays.copyOfRange(captures, params.length - 1, captures.length);
        } else {
            finalParams[params.length - 1 + 2] = convertTypedParam(params[params.length - 1], captures[captures.length - 1]);
        }
        finalParams[0] = req;
        finalParams[1] = resp;
        return (String)method.invoke(obj, finalParams);
    }

	@Override
	public String handle(HttpServletRequest req, HttpServletResponse resp) {
        // String uri = req.getRequestURI();
        // String method = req.getMethod();
        // System.out.println(method);
        // System.out.println(matchers);
        // if (!matchers.containsKey(method)) {
        //     return notFound(resp, "Unsupported method:" + method);
        // }
        // PatternMatcher.Result match = matchers.get(method).match(uri);
        // if (!match.isSuccess()) {
        //     return notFound(resp, "Illegal URI:" + uri);
        // }
        // String[] captures = match.getCaptures();
        // String id = match.getPatternId();
        // String[] parts = id.split(" ");
        // // get method name
        // String className = parts[0];
        // String methodName = parts[1];
        // // find method
        // try {
        //     return invokeMethodWithTypedParams(id, className, methodName, captures, req, resp);
        // } catch (Exception exp) {
        //     exp.printStackTrace();
        //     return notFound(resp, exp.getMessage());
        // } 
        return "";
    }

    public String notFound(HttpServletResponse resp, String message) {
        resp.setStatus(404);
        return "404 NOT FOUND:" + message;
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