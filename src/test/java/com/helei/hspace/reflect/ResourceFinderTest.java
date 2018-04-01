package com.helei.hspace.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.*;
import java.util.*;
import com.helei.hspace.reflect.*;

public class ResourceFinderTest {

    @Test
    public void testFindResources() {
        List<Class<?>> result = ResourceFinder.getResource("com.helei");
        assertEquals(2, result.size());
        assertEquals(ResourceA.class, result.get(1));
        assertEquals(ResourceB.class, result.get(0));
    }

    @Test
    public void testResourceId() {
        List<Class<?>> result = ResourceFinder.getResource("com.helei.hspace");
        assertEquals(2, result.size());
        Class<?> clsA = result.get(1);
        Class<?> clsB = result.get(0);
        assertEquals("com.helei.hspace.reflect.ResourceA", ResourceFinder.getResourceId(clsA));
        assertEquals("heyman", ResourceFinder.getResourceId(clsB));
    }

}