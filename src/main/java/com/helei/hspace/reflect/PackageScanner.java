package com.helei.hspace.reflect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import java.lang.reflect.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.annotation.*;

/**
 * Scanning package and get annotated elements
 */
public class PackageScanner {

    /**
     * get classes annotated with @Resource in current class loader
     */
    public static Stream<Class<?>> scan(String packageName, Class<? extends Annotation> anno) {
        System.out.println("Scanning " + packageName);
        String dirName = packageName.replace(".", "/");
        Stream<Class<?>> result = Stream.empty();
        try {
            Enumeration<URL> packageUrl = Thread.currentThread().getContextClassLoader().getResources(dirName);
            while (packageUrl.hasMoreElements()) {
                URI uri = packageUrl.nextElement().toURI();
                System.out.println("URI: " + uri);
                String scheme = uri.getScheme();
                Path path;
                if (scheme.equals("jar")) {
                    try {
                        FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
                        path = fs.getPath(dirName);
                    } catch (FileSystemAlreadyExistsException e) {
                        FileSystem fs = FileSystems.getFileSystem(uri);
                        path = fs.getPath(dirName);
                    }
                } else {
                    path = Paths.get(uri);
                }
                String pathString = path.toString();
                result = Stream.concat(result, Files.walk(path).map(p -> {
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
                        return getAnnotation(cls, anno) != null;
                    }));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * get annotation from an element
     * @param element element
     * @param annotation expected annotation
     * @return annotation object (null if annotation not exists)
     */
    public static Annotation getAnnotation(AnnotatedElement element, 
        Class<? extends Annotation> annotation) {
        try {
            return element.getAnnotation(annotation);
        } catch (Exception e) {
            return null;
        }
    }

    public static Stream<Method> getAnnotatedMethods(Class<?> cls, 
        Class<? extends Annotation> annotation) {
        Builder<Method> builder = Stream.builder();
        for (Method m : cls.getMethods()) {
            if (getAnnotation(m, annotation) != null) {
                builder.accept(m);
            }
        }
        return builder.build();
    }

    public static Stream<Field> getAnnotatedFields(Class<?> cls, 
        Class<? extends Annotation> annotation) {
        Builder<Field> builder = Stream.builder();
        for (Field f : cls.getFields()) {
            if (getAnnotation(f, annotation) != null) {
                builder.accept(f);
            }
        }
        return builder.build();
    }

    // public static List<Field> getAutowired(Class<?> cls) {
    //     Annotation classAnno = getAnnotation(cls, Resource.class);
    //     if (classAnno == null) {
    //         return Collections.emptyList();
    //     }
    //     Field[] fields = cls.getFields();
    //     ArrayList<Field> autowiredFields = new ArrayList<>();
    //     boolean wireAll = ((Resource)classAnno).autowireProperty();
    //     for (Field field : fields) {
    //         Annotation anno = getAnnotation(field, Autowire.class);
    //         boolean ignore = false;
    //         boolean hasAnno = false;
    //         if (anno != null) {
    //             Autowire wiring = (Autowire)anno; 
    //             ignore = wiring.ignore();
    //             hasAnno = true;
    //         } 
    //         if (!ignore && (wireAll || hasAnno)) {
    //             autowiredFields.add(field); 
    //         }
    //     }
    //     return autowiredFields;
    // }

    // public static List<Method> getRoute(Class<?> cls) {
    //     Annotation classAnno = getAnnotation(cls, Resource.class);
    //     if (classAnno == null) {
    //         return Collections.emptyList();
    //     }
    //     Method[] methods = cls.getMethods();
    //     ArrayList<Method> routes = new ArrayList<>();
    //     for (Method method : methods) {
    //         Annotation anno = getAnnotation(method, Route.class);
    //         if (anno != null) {
    //             Route route = (Route)anno; 
    //             if (!route.value().isEmpty()) {
    //                 routes.add(method);
    //             }
    //         } 
    //     }
    //     return routes;
    // }

    public static void main(String[] args) {
        // System.out.println(ResourceFinder.getResource("com.helei"));
    }

}