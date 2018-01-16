package org.walkmod.checkstyle.treewalkers;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.test.SemanticTest;


public class RedundantImportTest extends SemanticTest{

   @Test
   public void test() throws Exception {
      CompilationUnit cu = compile(
            "import java.lang.String; import java.util.Collection; import java.util.*; import java.io.*; public class Foo{}");
      RedundantImport<?> visitor = new RedundantImport<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(1, cu.getImports().size());
      Assert.assertEquals("java.util.Collection", cu.getImports().get(0).getName().toString());
   }
   
   @Test
   public void testWithWildCardUsages() throws Exception{
      CompilationUnit cu = compile(
            "import java.lang.String; import java.util.Collection; import java.util.*; import java.io.*; public class Foo{ List bar; }");
      RedundantImport<?> visitor = new RedundantImport<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(2, cu.getImports().size());
      Assert.assertEquals("java.util.Collection", cu.getImports().get(0).getName().toString());
      Assert.assertEquals("java.util.List", cu.getImports().get(1).getName().toString());
   }

   @Test
   public void testItDoesNotRemovesReflectionPackages() throws Exception {
      CompilationUnit cu = compile(
              "import java.lang.reflect.Method; public class Foo{ Method bar; }");
      RedundantImport<?> visitor = new RedundantImport<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(1, cu.getImports().size());
   }
}
