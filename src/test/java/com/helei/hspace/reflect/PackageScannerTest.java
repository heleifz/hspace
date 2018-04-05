package com.helei.hspace.reflect;

import static org.junit.Assert.assertEquals;

import org.junit.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PackageScannerTest {

    @Test
    public void testScan() {
        Stream<Class<?>> result = PackageScanner.scan("com.helei.hspace", Resource.class);
        List<Class<?>> lst = result.collect(Collectors.toList());
        assertEquals(2, lst.size());
        assertEquals(ResourceA.class, lst.get(1));
        assertEquals(ResourceB.class, lst.get(0));
    }

}