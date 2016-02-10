package org.walkmod.checkstyle.treewalkers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.walkmod.checkstyle.visitors.AbstractCheckStyleRule;
import org.walkmod.javalang.ast.expr.Expression;
import org.walkmod.javalang.ast.expr.LambdaExpr;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.DoStmt;
import org.walkmod.javalang.ast.stmt.ForStmt;
import org.walkmod.javalang.ast.stmt.IfStmt;
import org.walkmod.javalang.ast.stmt.Statement;
import org.walkmod.javalang.ast.stmt.SwitchEntryStmt;
import org.walkmod.javalang.ast.stmt.WhileStmt;

public class NeedBraces<A> extends AbstractCheckStyleRule<A> {

   private boolean allowSingleLineStatement = false;

   private boolean allowEmptyLoopBody = false;

   private Set<String> tokens = new HashSet<String>();

   public NeedBraces() {
      tokens.addAll(Arrays.asList("LITERAL_DO", "LITERAL_ELSE", "LITERAL_FOR", "LITERAL_IF", "LITERAL_WHILE"));
   }

   public boolean isAllowSingleLineStatement() {
      return allowSingleLineStatement;
   }

   public void setAllowSingleLineStatement(boolean allowSingleLineStatement) {
      this.allowSingleLineStatement = allowSingleLineStatement;
   }

   public boolean isAllowEmptyLoopBody() {
      return allowEmptyLoopBody;
   }

   public void setAllowEmptyLoopBody(boolean allowEmptyLoopBody) {
      this.allowEmptyLoopBody = allowEmptyLoopBody;
   }

   public void setTokens(Set<String> tokens) {
      this.tokens = tokens;
   }

   private BlockStmt convert(Statement stmt) {
      List<Statement> stmts = new LinkedList<Statement>();
      if (stmt != null) {
         stmts.add(stmt);
      }
      BlockStmt block = new BlockStmt(stmts);
      return block;
   }

   @Override
   public void visit(DoStmt n, A ctx) {
      if (tokens.contains("LITERAL_DO")) {
         Statement stmt = n.getBody();
         if (!(stmt instanceof BlockStmt)) {
            if ((allowEmptyLoopBody && stmt == null) || (stmt != null && !allowSingleLineStatement)) {
               n.setBody(convert(stmt));
            }
         }
      }
      super.visit(n, ctx);
   }

   @Override
   public void visit(ForStmt n, A ctx) {
      if (tokens.contains("LITERAL_FOR")) {
         Statement stmt = n.getBody();
         if (!(stmt instanceof BlockStmt)) {
            if ((allowEmptyLoopBody && stmt == null) || (stmt != null && !allowSingleLineStatement)) {
               n.setBody(convert(stmt));
            }
         }
      }
      super.visit(n, ctx);
   }

   @Override
   public void visit(IfStmt n, A ctx) {
      if (tokens.contains("LITERAL_IF")) {
         Statement stmt = n.getThenStmt();
         if (!(stmt instanceof BlockStmt)) {
            if (stmt == null || (stmt != null && !allowSingleLineStatement)) {
               n.setThenStmt(convert(stmt));
            }
         }
      }
      if (tokens.contains("LITERAL_ELSE")) {
         Statement stmt = n.getElseStmt();
         if (!(stmt instanceof BlockStmt)) {
            if (stmt == null || (stmt != null && !allowSingleLineStatement)) {
               n.setElseStmt(convert(stmt));
            }
         }
      }
      super.visit(n, ctx);
   }

   @Override
   public void visit(WhileStmt n, A ctx) {
      if (tokens.contains("LITERAL_WHILE")) {
         Statement stmt = n.getBody();
         if (!(stmt instanceof BlockStmt)) {
            if ((allowEmptyLoopBody && stmt == null) || (stmt != null && !allowSingleLineStatement)) {
               n.setBody(convert(stmt));
            }
         }
      }
      super.visit(n, ctx);
   }

   @Override
   public void visit(SwitchEntryStmt n, A ctx) {
      Expression label = n.getLabel();
      String labelName = label.toString().trim();
      if ((!labelName.equals("default") && tokens.contains("LITERAL_CASE"))
            || (labelName.equals("default") && tokens.contains("LITERAL_DEFAULT"))) {
         List<Statement> stmts = n.getStmts();
         if (stmts != null && stmts.size() == 1) {
            Statement stmt = stmts.get(0);
            if (!(stmt instanceof BlockStmt)) {
               if (stmt == null || (stmt != null && !allowSingleLineStatement)) {
                  stmts.clear();
                  stmts.add(convert(stmt));
                  n.setStmts(stmts);
               }
            }
         }
      }
      super.visit(n, ctx);
   }

   @Override
   public void visit(LambdaExpr n, A ctx) {
      if (tokens.contains("LAMBDA")) {
         Statement stmt = n.getBody();
         if (!(stmt instanceof BlockStmt)) {
            if (stmt == null || (stmt != null && !allowSingleLineStatement)) {
               n.setBody(convert(stmt));
            }
         }
      }
      super.visit(n, ctx);
   }

}
