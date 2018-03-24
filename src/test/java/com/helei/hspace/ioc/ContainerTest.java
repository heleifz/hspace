package com.helei.hspace.ioc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.*;

public class ContainerTest {

  @BeforeClass
  public static void suiteSetup() {
    // Set up something for the test cycle
  }

  @AfterClass
  public static void suiteTeardown() {
    // Tear down something for the test cycle
  }

  @Before
  public void setup() {
    // Set up something for the test.
  }

  @After
  public void teardown() {
    // Tear down something for the test.
  }

  @Test
  public void testResolveSimpleLambda() throws Exception {
    Container c = new Container();
    c.registerLambda("hello", ()->{
      return Double.valueOf(8.3);
    });
    c.registerLambda("world", ()->{
      return Integer.valueOf(3);
    });
    assertEquals(Integer.valueOf(3), c.create("world"));
    assertEquals(Double.valueOf(8.3), c.create("hello"));
    // error1: key not exist
  }

  @Test
  public void testResolveSimpleThread() throws Exception {
    Container c = new Container();
    c.register("ho", "com.helei.hspace.ioc.TestB");
    c.register("ha", "com.helei.hspace.ioc.TestA").depends("prop1", "ho");
    TestA aObj = c.create("ha");
    assertNotNull(aObj.prop1);
    assertEquals(3, aObj.prop1.val);
    TestA another = c.create("ha");
    assertEquals(aObj, another);
    // 跳过缓存
    TestA nu = c.forceCreate("ha");
    assertNotEquals(nu, another);
  }

  @Test
  public void testMultiThread() throws Exception {
    System.out.println("start multi thread");
    Container c = new Container();
    c.register("ho", "com.helei.hspace.ioc.TestB");
    c.register("ha", "com.helei.hspace.ioc.TestA").depends("prop1", "ho");
    TestA mainThreadObj = c.create("ha");
    ExecutorService ex = Executors.newCachedThreadPool();
    Future<TestA> f = ex.submit(()->{
      return (TestA)c.create("ha");
    });
    TestA threadA = f.get();
    assertNotEquals(threadA, mainThreadObj);

  }

}