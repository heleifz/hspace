package com.helei.hspace.ioc;
import static org.junit.Assert.assertEquals;

import org.junit.*;

public class LambdaResourceTest {

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
    public void testResolveGood() throws Exception {
        Resource<Integer> r = new LambdaResource<>(()->{
            return new Integer(3);
        });
        assertEquals(3, r.resolve().intValue());
    }
  
    @Test
    public void testResolveBad() throws Exception {
      // Test something else.
    }
}