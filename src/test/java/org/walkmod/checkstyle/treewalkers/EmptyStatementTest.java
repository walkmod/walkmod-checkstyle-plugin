package org.walkmod.checkstyle.treewalkers;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.stmt.Statement;

public class EmptyStatementTest {

   @Test
   public void test() throws Exception{
      CompilationUnit cu = ASTManager.parse("public class Foo{ public void bar() {;}}");
      EmptyStatement<?> visitor = new EmptyStatement<Object>();
      cu.accept(visitor, null);
      MethodDeclaration md = (MethodDeclaration)cu.getTypes().get(0).getMembers().get(0);
      List<Statement> stmts = md.getBody().getStmts();
      Assert.assertTrue(stmts.isEmpty());
   }
}
