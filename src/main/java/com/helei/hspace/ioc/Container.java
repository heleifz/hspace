package com.helei.hspace.ioc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.helei.hspace.reflect.PackageScanner;

/**
 * IoC Container that managing creation ofobjects
 */
public class Container 
{
    private static final Container singleton = new Container();

    public static Container get() {
        return singleton;
    }

    private ConcurrentHashMap<String, Resource<?>> resources = new ConcurrentHashMap<>();

    /**
     * scanning package and register @Resource(s) into container
     * auto wiring properties
     */
    public void autoRegister(String packageName) {
        Class<com.helei.hspace.reflect.Resource> resourceAnnotationClass = 
            com.helei.hspace.reflect.Resource.class;
        Class<com.helei.hspace.reflect.Autowire> autowireClass = 
            com.helei.hspace.reflect.Autowire.class;
        Stream<Class<?>> resources = PackageScanner.scan(packageName, resourceAnnotationClass);
        resources.forEach(resource -> {
            String resourceId = getResourceId(resource);
            // register class
            System.out.println("Found resource:" + resourceId);
            Resource<?> registered = register(resourceId, resource.getCanonicalName());
            com.helei.hspace.reflect.Resource anno = (com.helei.hspace.reflect.Resource)
                PackageScanner.getAnnotation(resource, resourceAnnotationClass);
            boolean wireAll = anno.autowireProperty();
            for (Field f : resource.getFields()) {
                com.helei.hspace.reflect.Autowire wire = (com.helei.hspace.reflect.Autowire)
                    PackageScanner.getAnnotation(resource, autowireClass);
                // ignore field
                if (wire != null && wire.ignore()) {
                    continue;
                }
                // autowire, resourceName == fieldName
                if (wireAll || (wire != null)) {
                    String name = f.getName();
                    registered.depends(name, name);
                }
            }
        }); 
    }

    public static String getResourceId(Class<?> cls) {
        Annotation anno = PackageScanner.getAnnotation(cls, 
            com.helei.hspace.reflect.Resource.class);
        if (anno != null) {
            String provideId = ((com.helei.hspace.reflect.Resource)anno).id();
            if (provideId.isEmpty()) {
                return cls.getCanonicalName();
            } else {
                return provideId;
            }
        }
        return null;
    }

    public <T> Resource<T> getResource(String id) throws Exception {
        if (!resources.containsKey(id)) {
            throw new Exception("resource not exist.");
        }
        System.out.println("Getting " + id);
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