package org.walkmod.checkstyle.treewalkers;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.Comment;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.MethodDeclaration;

import junit.framework.TestCase;

public class EmptyCatchBlockTest extends TestCase {

   @Test
   public void test_shouldAddComment_whenEmptyCatch() throws Exception {
      CompilationUnit cu = ASTManager
            .parse("public class Foo { public void bar() { try{ bar(); } catch (Exception e ){}}}");
      EmptyCatchBlock<?> visitor = new EmptyCatchBlock<Object>();
      cu.accept(visitor, null);
      List<Comment> comments = cu.getComments();
      Assert.assertTrue(comments.size() == 1);
   }

   @Test
   public void test_shouldDoNothing_whenNotEmptyCatch() throws Exception {
      CompilationUnit cu = ASTManager
            .parse("public class Foo { public void bar() { try{} catch (Exception e ){return;}}}");
      EmptyCatchBlock<?> visitor = new EmptyCatchBlock<Object>();
      cu.accept(visitor, null);
      List<Comment> comments = cu.getComments();
      Assert.assertTrue(comments == null);
   }

   @Test
   public void test_shouldDoNothing_whenCatchWithOnlyComments() throws Exception {
      CompilationUnit cu = ASTManager
            .parse("public class Foo { public void bar() { try{} catch (Exception e ){/*Hola*/}}}");
      EmptyCatchBlock<?> visitor = new EmptyCatchBlock<Object>();
      cu.accept(visitor, null);
      List<Comment> comments = cu.getComments();
      Assert.assertTrue(comments.size() == 1);
   }

   @Test
   public void test_shouldAddComment_whenEmptyCatchAndMoreComments() throws Exception {
      CompilationUnit cu = ASTManager
            .parse("public class Foo { public void bar() { try{bar(); /*hello*/} catch (Exception e ){}}}");
      EmptyCatchBlock<?> visitor = new EmptyCatchBlock<Object>();
      cu.accept(visitor, null);
      List<Comment> comments = cu.getComments();
      Assert.assertTrue(comments.size() == 2);
   }

   @Test
   public void test_shouldRemove_emptyCatch() throws Exception {
      CompilationUnit cu = ASTManager.parse("public class Foo { public void bar() { try{} catch (Exception e ){}}}");
      EmptyCatchBlock<?> visitor = new EmptyCatchBlock<Object>();
      cu.accept(visitor, null);

      MethodDeclaration md = (MethodDeclaration) cu.getTypes().get(0).getMembers().get(0);
      Assert.assertTrue(md.getBody().getStmts().isEmpty());
   }
}