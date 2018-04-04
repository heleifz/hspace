package com.helei.hspace.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.*;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.helei.hspace.reflect.*;

public class PackageScannerTest {

    @Test
    public void testScan() {
        Stream<Class<?>> result = PackageScanner.scan("com.helei", Resource.class);
        List<Class<?>> lst = result.collect(Collectors.toList());
        assertEquals(2, lst.size());
        assertEquals(ResourceA.class, lst.get(1));
        assertEquals(ResourceB.class, lst.get(0));
    }

    @Test
    public void testResourceId() {
        Stream<Class<?>> result = PackageScanner.scan("com.helei", Resource.class);
        List<Class<?>> lst = result.collect(Collectors.toList());
        assertEquals(2, lst.size());
        Class<?> clsA = lst.get(1);
        Class<?> clsB = lst.get(0);
        assertEquals("com.helei.hspace.reflect.ResourceA", PackageScanner.getResourceId(clsA));
        assertEquals("heyman", PackageScanner.getResourceId(clsB));
    }

}