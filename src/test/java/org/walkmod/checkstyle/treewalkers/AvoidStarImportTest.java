package org.walkmod.checkstyle.treewalkers;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.test.SemanticTest;

public class AvoidStarImportTest extends SemanticTest{

   @Test
   public void test() throws Exception{
      CompilationUnit cu = compile("import java.util.*; public class Foo{}");
      AvoidStarImport<?> visitor = new AvoidStarImport<Object>();
      cu.accept(visitor, null);
      Assert.assertTrue(cu.getImports().isEmpty());
   }
   
   @Test
   public void testWithUsages() throws Exception{
      CompilationUnit cu = compile("import java.util.*; public class Foo{ List l; }");
      AvoidStarImport<?> visitor = new AvoidStarImport<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(1, cu.getImports().size());
      Assert.assertEquals("java.util.List", cu.getImports().get(0).getName().toString());
   }

   @Test
   public void testInnerClasses() throws Exception {
      CompilationUnit cu = compile("import java.util.*; public class Foo{ Map.Entry e; }");
      AvoidStarImport<?> visitor = new AvoidStarImport<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(1, cu.getImports().size());
      Assert.assertEquals("java.util.Map", cu.getImports().get(0).getName().toString());
   }
}
