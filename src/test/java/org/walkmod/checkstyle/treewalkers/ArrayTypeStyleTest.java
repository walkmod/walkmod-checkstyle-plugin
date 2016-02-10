package org.walkmod.checkstyle.treewalkers;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.type.ReferenceType;

public class ArrayTypeStyleTest {

   @Test
   public void testArrayType() throws Exception {
      ArrayTypeStyle<?> visitor = new ArrayTypeStyle<Object>();
      CompilationUnit cu = ASTManager.parse("public class Foo{ int x[]; }");
      visitor.visit(cu, null);

      FieldDeclaration fd = (FieldDeclaration) cu.getTypes().get(0).getMembers().get(0);
      Assert.assertTrue(fd.getType() instanceof ReferenceType);
      ReferenceType rt = (ReferenceType) fd.getType();
      Assert.assertEquals(1, rt.getArrayCount());
   }
}
