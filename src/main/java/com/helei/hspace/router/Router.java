package com.helei.hspace.router;

import com.helei.hspace.router.*;
import com.helei.hspace.server.RequestProcessor;
import com.helei.hspace.util.Pair;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helei.hspace.ioc.*;
import com.helei.hspace.reflect.PackageScanner;
import com.helei.hspace.reflect.Route;

public class Router implements RequestProcessor
{
    private PatternTree patternTree = new PatternTree();
    private HashMap<String, Pair<String, Method>> routeMap = new HashMap<>();
    private static final Router singleton = new Router();

    public static Router get() {
        return singleton;
    }

    public void autoRegister(String packageName, Container container) {
        Class<com.helei.hspace.reflect.Resource> resourceAnnotationClass = 
            com.helei.hspace.reflect.Resource.class;
        Stream<Class<?>> resources = PackageScanner.scan(packageName, resourceAnnotationClass);
        resources.forEach(resource -> {
            String resourceId = Container.getResourceId(resource);
            for (Method m : resource.getMethods()) {
                Route r = (Route)PackageScanner.getAnnotation(m, Route.class);
                if (r != null) {
                    String value = r.value();
                    System.out.println("Register route:" + value + " " + resourceId);
                    registerRoute(value, new Pair<>(resourceId, m), container);
                }
            }
        }); 
    }

    public void registerRoute(String route, Pair<String, Method> method, 
        Container container) {
        String tag = route + "_" + method.getSecond().toString();
        patternTree.addPattern(route, tag);
        routeMap.put(tag, method);
    }

	@Override
	public String handle(HttpServletRequest req, HttpServletResponse resp, Container container) throws Exception {
        String uri = req.getRequestURI();
        String method = req.getMethod();
        String query = "[" + method + "]" + uri;
        // String query = uri;
        MatchResult match = patternTree.match(query);
        if (match == null) {
            throw new Exception("Page not exitst:" + uri);
        }
        Map<String, String> captures = match.getCaptures();
        Pair<String, Method> destination = routeMap.get(match.getTag());
        Object obj = container.getResource(destination.getFirst()).resolve();
        return invokeRouteMethod(obj, destination.getSecond(), captures, 
            req, resp, container);
    }

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
    
    private String invokeRouteMethod(Object obj, Method m, Map<String, String> args, 
        HttpServletRequest req, HttpServletResponse resp, Container container) throws Exception {
        Parameter[] params = m.getParameters();
        Object[] actualArgs = new Object[params.length];
        for (int i = 0; i < params.length; ++i) {
            Parameter p = params[i];
            // resolve every parameters
            // 0. httpresponse/request
            // 1. capture
            // 2. container
            Class<?> paramType = p.getType();
            String paramName = p.getName();
            if (paramType == HttpServletRequest.class) {
                actualArgs[i] = req; 
            } else if (paramType == HttpServletResponse.class) {
                actualArgs[i] = resp; 
            } else if (args.containsKey(paramName)) {
                Object converted = convertTypedParam(p, args.get(paramName));
                actualArgs[i] = converted;
            } else {
                Resource<?> r = container.getResource(paramName);
                if (r != null) {
                    actualArgs[i] = r.resolve();
                } else {
                    throw new Exception("Cannot resolve parameter " + paramName + " for route");
                }
            }
        }
        String result = m.invoke(obj, actualArgs).toString();
        System.out.println("result:" + result);
        return result;
    }

}