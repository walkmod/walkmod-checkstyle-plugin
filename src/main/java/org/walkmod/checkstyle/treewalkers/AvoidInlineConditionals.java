package org.walkmod.checkstyle.treewalkers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.walkmod.checkstyle.visitors.AbstractCheckStyleRule;
import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.body.BodyDeclaration;
import org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration;
import org.walkmod.javalang.ast.body.ConstructorDeclaration;
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.expr.AssignExpr;
import org.walkmod.javalang.ast.expr.ConditionalExpr;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.ExplicitConstructorInvocationStmt;
import org.walkmod.javalang.ast.stmt.ExpressionStmt;
import org.walkmod.javalang.ast.stmt.IfStmt;
import org.walkmod.javalang.ast.stmt.ReturnStmt;
import org.walkmod.javalang.ast.stmt.Statement;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;

public class AvoidInlineConditionals<A> extends VoidVisitorAdapter<A> {

   @Override
   public void visit(ConditionalExpr n, A ctx) {
      super.visit(n, ctx);
      Node parent = n.getParentNode();
      RewriteInlineConditional<A> visitor = new RewriteInlineConditional<A>(n);
      parent.accept(visitor, ctx);

   }

   private class RewriteInlineConditional<A> extends VoidVisitorAdapter<A> {

      private ConditionalExpr expr;

      public RewriteInlineConditional(ConditionalExpr expr) {
         this.expr = expr;

      }

      @Override
      public void visit(ReturnStmt n, A ctx) {
         List<Statement> thenStmts = new LinkedList<Statement>();
         thenStmts.add(new ReturnStmt(expr.getThenExpr()));
         BlockStmt thenBlock = new BlockStmt(thenStmts);

         List<Statement> elseStmts = new LinkedList<Statement>();
         elseStmts.add(new ReturnStmt(expr.getElseExpr()));
         BlockStmt elseBlock = new BlockStmt(elseStmts);

         IfStmt ifStmt = new IfStmt(expr.getCondition(), thenBlock, elseBlock);
         n.getParentNode().replaceChildNode(n, ifStmt);
      }

      @Override
      public void visit(MethodCallExpr n, A ctx) {

         Node parent = n.getParentNode();

         while (parent != null && parent instanceof Expression) {
            parent = parent.getParentNode();
         }

         if (parent instanceof ExpressionStmt) {

            Node grandParent = parent.getParentNode();
            BlockStmt block = null;

            if (grandParent instanceof BlockStmt) {

               block = (BlockStmt) grandParent;
            }

            MethodCallExpr thenMethodCall = null;
            MethodCallExpr elseMethodCall = null;

            List<Statement> thenStmts = new LinkedList<Statement>();

            MethodCallExpr newMethodCall = new MethodCallExpr(n.getScope(), n.getName(),
                  new LinkedList<Expression>(n.getArgs()));

            newMethodCall.replaceChildNode(expr, expr.getThenExpr());

            try {
               thenMethodCall = newMethodCall.clone();
            } catch (CloneNotSupportedException e) {
               throw new RuntimeException(e);
            }

            thenStmts.add(new ExpressionStmt(thenMethodCall));

            BlockStmt thenBlock = new BlockStmt(thenStmts);

            List<Statement> elseStmts = new LinkedList<Statement>();

            newMethodCall.replaceChildNode(expr.getThenExpr(), expr.getElseExpr());
            try {
               elseMethodCall = newMethodCall.clone();
            } catch (CloneNotSupportedException e) {
               throw new RuntimeException(e);
            }
            elseStmts.add(new ExpressionStmt(elseMethodCall));
            BlockStmt elseBlock = new BlockStmt(elseStmts);

            if (block == null) {
               List<Statement> stmts = new LinkedList<Statement>();
               stmts.add(new IfStmt(expr.getCondition(), thenBlock, elseBlock));
               block = new BlockStmt(stmts);
               grandParent.replaceChildNode(parent, block);
            } else {
               block.replaceChildNode(parent, new IfStmt(expr.getCondition(), thenBlock, elseBlock));
            }

         }
      }

      @Override
      public void visit(final VariableDeclarator n, A ctx) {
         Node parent = n.getParentNode();
         final String name = n.getId().getName();
         if (parent != null) {
            Node grandParent = parent.getParentNode();

            if (grandParent != null) {
               VoidVisitorAdapter<A> visitor = new VoidVisitorAdapter<A>() {

                  public void visit(ExpressionStmt node, A ctx) {
                     Node parent = node.getParentNode();
                     BlockStmt blockStmt = null;
                     if (parent instanceof BlockStmt) {
                        blockStmt = (BlockStmt) parent;
                     } else {

                        List<Statement> stmts = new LinkedList<Statement>();
                        stmts.add(node);
                        blockStmt = new BlockStmt(stmts);
                        parent.replaceChildNode(node, blockStmt);
                     }
                     List<Statement> stmts = blockStmt.getStmts();
                     List<Statement> thenStmts = new LinkedList<Statement>();
                     thenStmts.add(new ExpressionStmt(
                           new AssignExpr(new NameExpr(name), expr.getThenExpr(), AssignExpr.Operator.assign)));
                     BlockStmt thenBlock = new BlockStmt(thenStmts);

                     List<Statement> elseStmts = new LinkedList<Statement>();
                     elseStmts.add(new ExpressionStmt(
                           new AssignExpr(new NameExpr(name), expr.getElseExpr(), AssignExpr.Operator.assign)));
                     BlockStmt elseBlock = new BlockStmt(elseStmts);
                     stmts.add(new IfStmt(expr.getCondition(), thenBlock, elseBlock));
                     blockStmt.setStmts(stmts);
                  }

                  public void visit(ClassOrInterfaceDeclaration node, A ctx) {
                     n.setInit(null);
                     List<BodyDeclaration> members = node.getMembers();
                     Iterator<BodyDeclaration> it = members.iterator();
                     List<ConstructorDeclaration> constructors = new LinkedList<ConstructorDeclaration>();
                     while (it.hasNext()) {
                        BodyDeclaration member = it.next();
                        if (member instanceof ConstructorDeclaration) {
                           constructors.add((ConstructorDeclaration) member);
                        }
                     }
                     if (constructors.isEmpty()) {

                        BlockStmt block = new BlockStmt();
                        List<Statement> stmts = new LinkedList<Statement>();

                        List<Statement> thenStmts = new LinkedList<Statement>();
                        thenStmts.add(new ExpressionStmt(
                              new AssignExpr(new NameExpr(name), expr.getThenExpr(), AssignExpr.Operator.assign)));
                        BlockStmt thenBlock = new BlockStmt(thenStmts);

                        List<Statement> elseStmts = new LinkedList<Statement>();
                        elseStmts.add(new ExpressionStmt(
                              new AssignExpr(new NameExpr(name), expr.getElseExpr(), AssignExpr.Operator.assign)));
                        BlockStmt elseBlock = new BlockStmt(elseStmts);

                        stmts.add(new IfStmt(expr.getCondition(), thenBlock, elseBlock));
                        block.setStmts(stmts);
                        List<BodyDeclaration> newMembers = new LinkedList<BodyDeclaration>(members);
                        newMembers.add(new ConstructorDeclaration(null, ModifierSet.PUBLIC, null, null, node.getName(),
                              null, null, block));
                        node.setMembers(newMembers);

                     } else {
                        for (ConstructorDeclaration cd : constructors) {
                           BlockStmt block = cd.getBlock();
                           List<Statement> newStmts = null;
                           if (block.getStmts() != null) {
                              newStmts = new LinkedList<Statement>(block.getStmts());
                           } else {
                              newStmts = new LinkedList<Statement>();
                           }
                           int index = 0;

                           List<Statement> thenStmts = new LinkedList<Statement>();
                           thenStmts.add(new ExpressionStmt(
                                 new AssignExpr(new NameExpr(name), expr.getThenExpr(), AssignExpr.Operator.assign)));
                           BlockStmt thenBlock = new BlockStmt(thenStmts);

                           List<Statement> elseStmts = new LinkedList<Statement>();
                           elseStmts.add(new ExpressionStmt(
                                 new AssignExpr(new NameExpr(name), expr.getElseExpr(), AssignExpr.Operator.assign)));
                           BlockStmt elseBlock = new BlockStmt(elseStmts);

                           IfStmt ifStmt = new IfStmt(expr.getCondition(), thenBlock, elseBlock);

                           if (newStmts.isEmpty()) {
                              newStmts.add(ifStmt);
                           } else {
                              if (newStmts.get(0) instanceof ExplicitConstructorInvocationStmt) {
                                 index = 1;
                              }
                              newStmts.add(index, ifStmt);
                           }
                           block.setStmts(newStmts);
                        }
                     }
                  }

               };
               grandParent.accept(visitor, ctx);
            }
         }
      }
   }
}
