package com.helei.hspace.router;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.*;

public class PatternTreeTest {

    @BeforeClass
    public static void suiteSetup() {
    }
  
    @AfterClass
    public static void suiteTeardown() {
    }
  
    @Before
    public void setup() {
    }
  
    @After
    public void teardown() {
      // Tear down something for the test.
    }
  
    @Test
    public void testSimpleTree() throws Exception {
        PatternTree tree = new PatternTree();
        tree.addPattern("/hello/world", "t1");
        tree.addPattern("/hello/nihao", "t2");
        tree.addPattern("/fuck/you", "t3");
        assertEquals("t1", tree.match("/hello/world").getTag());
        assertEquals("t2", tree.match("/hello/nihao").getTag());
        assertNull(tree.match("/hello"));
    }

    @Test
    public void testWildcard() throws Exception {
        PatternTree tree = new PatternTree();
        tree.addPattern("/*/world", "t1");
        tree.addPattern("/nihao/*/88", "t2");
        assertEquals("t1", tree.match("/hello/world").getTag());
        assertEquals("t2", tree.match("/nihao/xxxx/88").getTag());
        assertNull(tree.match("/nihao/88"));
    }

    @Test
    public void testCapture() throws Exception {
        PatternTree tree = new PatternTree();
        tree.addPattern("/*/world/:name", "t1");
        tree.addPattern("/:pig/*/88/:hey", "t2");
        MatchResult r = tree.match("/hello/world/hehe");
        assertEquals("t1", r.getTag());
        assertEquals("hehe", r.getCaptures().get("name"));
        r = tree.match("/asdfsdf/xxxx/88/44");
        assertEquals("t2", r.getTag());
        assertEquals("asdfsdf", r.getCaptures().get("pig"));
        assertEquals("44", r.getCaptures().get("hey"));
    }

}