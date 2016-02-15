package org.walkmod.checkstyle.treewalkers;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.stmt.IfStmt;
import org.walkmod.javalang.ast.stmt.Statement;

public class AvoidInlineConditionalsTest {

   @Test
   public void testInReturnStmt() throws Exception {
      CompilationUnit cu = ASTManager.parse(
            "public class Foo{ public String bar(String a) { return (a==null || a.length() <1) ? null : a.substring(1); } }");
      AvoidInlineConditionals<Object> visitor = new AvoidInlineConditionals<Object>();
      cu.accept(visitor, null);
      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(0);
      Statement stmt = md.getBody().getStmts().get(0);
      Assert.assertTrue(stmt instanceof IfStmt);

   }

   @Test
   public void testFieldDeclarator() throws Exception {
      CompilationUnit cu = ASTManager
            .parse("public class Foo{ String a; String b = (a==null || a.length() <1) ? null : a.substring(1); }");
      AvoidInlineConditionals<Object> visitor = new AvoidInlineConditionals<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(3, cu.getTypes().get(0).getMembers().size());
   }

   @Test
   public void testFieldDeclaratorWithExistingConstructors() throws Exception {
      CompilationUnit cu = ASTManager.parse(
            "public class Foo{ String a; String b = (a==null || a.length() <1) ? null : a.substring(1);  public Foo(){}}");
      AvoidInlineConditionals<Object> visitor = new AvoidInlineConditionals<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(3, cu.getTypes().get(0).getMembers().size());
   }

   @Test
   public void testFieldDeclaratorWithExistingConstructorsAndSuper() throws Exception {
      CompilationUnit cu = ASTManager.parse(
            "public class Foo{ String a; String b = (a==null || a.length() <1) ? null : a.substring(1);  public Foo(){super();}}");
      AvoidInlineConditionals<Object> visitor = new AvoidInlineConditionals<Object>();
      cu.accept(visitor, null);
      Assert.assertEquals(3, cu.getTypes().get(0).getMembers().size());
   }

   @Test
   public void testInMethodCall() throws Exception {
      CompilationUnit cu = ASTManager.parse(
            "public class Foo{ public void bar(int c){ } public void hello() { int i = 1; bar( i == 1 ? i+1 : i); } }");
      AvoidInlineConditionals<Object> visitor = new AvoidInlineConditionals<Object>();
      cu.accept(visitor, null);
      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(1);
      List<Statement> stmts = md.getBody().getStmts();
      Assert.assertTrue(stmts.get(1) instanceof IfStmt);
   }
}
