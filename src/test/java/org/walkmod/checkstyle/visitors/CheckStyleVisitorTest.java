package org.walkmod.checkstyle.visitors;


import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class CheckStyleVisitorTest {

    @Test
    public void testConfigWithClassNames() throws Exception {
        CheckStyleVisitor visitor = new CheckStyleVisitor();
        visitor.setConfigurationFile("src/test/resources/checkstyle-with-classes.xml");
        Set<String> rules = visitor.getRules();

        Assert.assertTrue(rules.contains("EmptyStatement"));
        Assert.assertTrue(rules.contains("NeedBraces"));
        Assert.assertTrue(rules.contains("UnusedImports"));
    }

}
