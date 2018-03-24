package com.helei.hspace.ioc;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.*;
import com.helei.hspace.util.*;

import org.eclipse.jetty.util.ConcurrentArrayQueue;

public class ThreadResource<T> implements Resource<T>
{
    private Container parentContainer;

    /**
     * threads share dependency configuration
     * so it is saved in concurrent map
     */
    private ConcurrentHashMap<String, String> dependencies =
        new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> valueDependencies =
        new ConcurrentHashMap<>();

    private String className;
    /**
     * thread local object cache
     */
    private ThreadLocal<Object> cached = new ThreadLocal<>();

    public ThreadResource(String className, Container parentContainer) {
        this.parentContainer = parentContainer;
        this.className = className;
    } 

    public Resource<T> depends(String property, String id){
        dependencies.put(property, id);
        return this;
    }

    public Resource<T> dependsValue(String property, Object value) {
        valueDependencies.put(property, value);
        return this;
    }

    private void assignProperty(Class<?> cls, Object obj, 
                                String property, Object val) throws Exception {
        Method[] methods = cls.getMethods();
        Method good = null;
        String camel = "set" + StringUtil.toCamel(property);
        // checking method name and parameter type
        for (Method m : methods) {
            if (m.getName().equals(camel)) {
                Parameter[] params = m.getParameters();
                if (params.length == 1 && params[0].getType().isAssignableFrom(val.getClass()))
                {
                    good = m;
                }
            }
        }
        if (good == null) {
            throw new Exception("Missing setter method for property: " + property);
        }
        good.invoke(obj, val);
    }

    public T forceResolve() throws Exception {
        Class<?> cls = Class.forName(className);
        Object obj = cls.newInstance();
        // resolve all dependencies     
        // and invoke setter method on the object
        for (HashMap.Entry<String, String> entry : dependencies.entrySet()) {
            Resource<T> resource = parentContainer.getResource(entry.getValue());
            T val = resource.forceResolve();
            assignProperty(cls, obj, entry.getKey(), val);
        }
        for (HashMap.Entry<String, Object> entry : valueDependencies.entrySet()) {
            assignProperty(cls, obj, entry.getKey(), entry.getValue());
        }
        return (T)obj;
    }

    private void cacheObject(T obj) {
        cached.set(obj);
    }

    private boolean isCached() {
        return cached.get() != null; 
    }

    private Object getCached() {
        return cached.get();
    }

    @Override
    public T resolve() throws Exception {
        if (isCached()) {
            return (T)getCached();
        } else {
            System.out.println("notcache");
            T obj = forceResolve();
            cacheObject(obj);
            return obj;
        }
    }
}