package com.helei.hspace.reflect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.lang.reflect.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.helei.hspace.reflect.Resource;
import com.helei.hspace.reflect.Autowire;
import com.helei.hspace.reflect.Route;
import java.lang.annotation.*;

/**
 * Scanning package and get annotated elements
 */
class ResourceFinder {

    /**
     * get classes annotated with @Resource in current class loader
     */
    public static List<Class<?>> getResource(String packageName) {
        String dirName = packageName.replace(".", "/");
        ArrayList<Class<?>> result = new ArrayList<>();
        try {
            Enumeration<URL> packageUrl = Thread.currentThread().getContextClassLoader().getResources(dirName);
            while (packageUrl.hasMoreElements()) {
                URI uri = packageUrl.nextElement().toURI();
                String scheme = uri.getScheme();
                final Path path;
                if (scheme.equals("jar")) {
                    FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
                    path = fs.getPath(dirName);
                } else {
                    path = Paths.get(uri);
                }
                String pathString = path.toString();
                List<Class<?>> current = Files.walk(path).map(p -> {
                    String currentString = p.toString();
                    int pos = currentString.indexOf(pathString) + pathString.length();
                    String normPath = (dirName + currentString.substring(pos));
                    return normPath;
                }).filter(p -> p.endsWith(".class")).map(p -> {
                        int length = p.length();
                        p = p.replace("/", ".").substring(0, length - 6);
                        try {
                            Class<?> cls = Class.forName(p);
                            return cls;
                        } catch (ClassNotFoundException e) {
                            return null;
                        }
                    }).filter(cls -> {
                        if (cls == null) {
                            return false;
                        }
                        return getAnnotation(cls, Resource.class) != null;
                    }).collect(Collectors.toList());
                result.addAll(current);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public static Annotation getAnnotation(AnnotatedElement element, 
        Class<? extends Annotation> annotation) {
        try {
            return element.getAnnotation(annotation);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getResourceId(Class<?> cls) {
        Annotation anno = getAnnotation(cls, Resource.class);
        if (anno != null) {
            String provideId = ((Resource)anno).id();
            if (provideId.isEmpty()) {
                return cls.getCanonicalName();
            } else {
                return provideId;
            }
        }
        return "";
    }

    public static List<Field> getAutowired(Class<?> cls) {
        Annotation classAnno = getAnnotation(cls, Resource.class);
        if (classAnno == null) {
            return Collections.emptyList();
        }
        Field[] fields = cls.getFields();
        ArrayList<Field> autowiredFields = new ArrayList<>();
        boolean wireAll = ((Resource)classAnno).autowireProperty();
        for (Field field : fields) {
            Annotation anno = getAnnotation(field, Autowire.class);
            boolean ignore = false;
            boolean hasAnno = false;
            if (anno != null) {
                Autowire wiring = (Autowire)anno; 
                ignore = wiring.ignore();
                hasAnno = true;
            } 
            if (!ignore && (wireAll || hasAnno)) {
                autowiredFields.add(field); 
            }
        }
        return autowiredFields;
    }

    public static List<Method> getRoute(Class<?> cls) {
        Annotation classAnno = getAnnotation(cls, Resource.class);
        if (classAnno == null) {
            return Collections.emptyList();
        }
        Method[] methods = cls.getMethods();
        ArrayList<Method> routes = new ArrayList<>();
        for (Method method : methods) {
            Annotation anno = getAnnotation(method, Route.class);
            if (anno != null) {
                Route route = (Route)anno; 
                if (!route.value().isEmpty()) {
                    routes.add(method);
                }
            } 
        }
        return routes;
    }

    public static void main(String[] args) {
        System.out.println(ResourceFinder.getResource("com.helei"));
    }

}