package org.walkmod.checkstyle.treewalkers;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.IfStmt;
import org.walkmod.javalang.ast.stmt.SwitchStmt;

public class NeedBracesTest {

   @Test
   public void testIfStmt() throws Exception {
      NeedBraces<?> visitor = new NeedBraces<Object>();
      CompilationUnit cu = ASTManager
            .parse("public class Foo{ void bar() { if (true) System.out.println(\"hello\"); }}");
      visitor.visit(cu, null);

      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(0);
      IfStmt ifStmt = (IfStmt) md.getBody().getStmts().get(0);
      Assert.assertTrue(ifStmt.getThenStmt() instanceof BlockStmt);
   }
   
   @Test
   public void testSwitch() throws Exception{
      
      NeedBraces<?> visitor = new NeedBraces<Object>();
      visitor.setTokens(new HashSet<String>(Arrays.asList("LITERAL_CASE")));
      CompilationUnit cu = ASTManager
            .parse("public class Foo{ void bar(int x) { switch (x){ case 1: System.out.println(\"hello\");} }}");
      visitor.visit(cu, null);

      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(0);
      SwitchStmt switchStmt = (SwitchStmt) md.getBody().getStmts().get(0);
      Assert.assertTrue(switchStmt.getEntries().get(0).getStmts().get(0) instanceof BlockStmt);
   }
}
