package org.walkmod.checkstyle.treewalkers;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.test.SemanticTest;


public class RedundantImportTest extends SemanticTest{

   @Test
   public void testRemovesAsteriskWhenThereAreRedundantImportsAndAreNotUsed() throws Exception {
      CompilationUnit cu = compile(
            "import java.util.Collection; import java.util.*; public class Foo{}");
      RedundantImport<?> visitor = new RedundantImport<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(1, cu.getImports().size());
      Assert.assertEquals("java.util.Collection", cu.getImports().get(0).getName().toString());
   }

   @Test
   public void testRemovesPrimitiveJavalangClasses() throws Exception{
      CompilationUnit cu = compile(
              "import java.lang.String; import java.util.List; public class Foo{ List bar; }");
      RedundantImport<?> visitor = new RedundantImport<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(1, cu.getImports().size());
      Assert.assertEquals("java.util.List", cu.getImports().get(0).getName().toString());
   }

   @Test
   public void testDoesNotRemovesAsteriskIfUsesExtraClasses() throws Exception{
      CompilationUnit cu = compile(
            "import java.util.Collection; import java.util.*; public class Foo{ List bar; }");
      RedundantImport<?> visitor = new RedundantImport<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(2, cu.getImports().size());
      Assert.assertEquals("java.util.Collection", cu.getImports().get(0).getName().toString());
      Assert.assertEquals("java.util", cu.getImports().get(1).getName().toString());
   }

   @Test
   public void testItDoesNotRemovesNestedJavalangPackages() throws Exception {
      CompilationUnit cu = compile(
              "import java.lang.reflect.Method; public class Foo{ Method bar; }");
      RedundantImport<?> visitor = new RedundantImport<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(1, cu.getImports().size());
   }

   @Test
   public void testItDoesNotRemovesUnusedAsteriskImports() throws Exception {
      CompilationUnit cu = compile(
              "import java.io.*; import java.util.*; public class Foo{ List bar; }");
      RedundantImport<?> visitor = new RedundantImport<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(2, cu.getImports().size());
      Assert.assertEquals("java.io", cu.getImports().get(0).getName().toString());
      Assert.assertEquals("java.util", cu.getImports().get(1).getName().toString());
   }

   @Test
   public void testNotRemovesImportsOfNestedPackages() throws Exception {
      CompilationUnit cu = ASTManager.parse(
              "package bar; import bar.foo.B;  public class Foo{ }");
      RedundantImport<?> visitor = new RedundantImport<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(1, cu.getImports().size());
   }
}
