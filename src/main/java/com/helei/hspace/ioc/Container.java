package com.helei.hspace.ioc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * IoC Container that managing creation ofobjects
 */
public class Container 
{
    public static Container get() {
        return threadContainer.get();
    }

    private static ThreadLocal<Container> threadContainer = new ThreadLocal<Container>() {
        @Override
        protected Container initialValue() {
            return new Container();
        }
     };

    private ConcurrentHashMap<String, Resource<?>> resources = new ConcurrentHashMap<>();

    public <T> Resource<T> getResource(String id) throws Exception {
        if (!resources.containsKey(id)) {
            throw new Exception("resource not exist.");
        }
        Resource<?> obj = resources.get(id);
        return (Resource<T>)obj;
    }

    /**
     * supplier is called every time object is need
     * return newly created resource
     */
    public <T> Resource<T> registerLambda(String id, Supplier<T> supplier) {
        Resource<T> resource = new LambdaResource<>(supplier);
        resources.put(id, resource);
        return resource;
    }

    /**
     * object is create once (for every thread)
     * return newly created resource
     */
    public <T> Resource<T> register(String id, String className) {
        Resource<T> resource = new ThreadResource<>(className, this);
        resources.put(id, resource);
        return resource;
    }

    /**
     * create object and resolve its dependecies
     */
    public <T> T create(String id) throws Exception {
        // System.out.println("threadid" + Thread.currentThread().getId());
        Resource<T> resource = getResource(id);
        return resource.resolve();
    }

    /**
     * create object and resolve its dependecies
     */
    public <T> T forceCreate(String id) throws Exception {
        Resource<T> resource = getResource(id);
        return resource.forceResolve();
    }

}