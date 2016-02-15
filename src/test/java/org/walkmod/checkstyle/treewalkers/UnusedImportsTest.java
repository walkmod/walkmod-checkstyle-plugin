package org.walkmod.checkstyle.treewalkers;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.test.SemanticTest;

public class UnusedImportsTest extends SemanticTest{

   @Test
   public void test() throws Exception{
      CompilationUnit cu = compile("import java.util.*; public class Foo{}");
      UnusedImports<?> visitor = new UnusedImports<Object>();
      cu.accept(visitor, null);
      Assert.assertTrue(cu.getImports().isEmpty());
   }
}
