package org.walkmod.checkstyle.treewalkers;

import java.util.ArrayList;
import java.util.List;

import org.walkmod.javalang.ast.BlockComment;
import org.walkmod.javalang.ast.Comment;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.CatchClause;
import org.walkmod.javalang.ast.stmt.TryStmt;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;

public class EmptyCatchBlock<A> extends VoidVisitorAdapter<A> {

   private String commentFormat = " Intentionally blank ";

   private List<Comment> defaultCatchComments = new ArrayList<Comment>();

   public String getCommentFormat() {
      return commentFormat;
   }

   public void setCommentFormat(String commentFormat) {
      this.commentFormat = commentFormat;
   }

   @Override
   public void visit(TryStmt n, A ctx) {

      BlockStmt block = n.getTryBlock();
      if (block == null || block.getStmts() == null || block.getStmts().isEmpty()) {
         n.remove();
      } else {

         List<CatchClause> catchList = n.getCatchs();
         for (CatchClause catchClause : catchList) {
            if (catchClause.getCatchBlock().getStmts() == null) {
               addDefaultComment(catchClause.getCatchBlock());
            }
         }
         super.visit(n, ctx);
      }
   }

   @Override
   public void visit(CompilationUnit cu, A ctx) {
      super.visit(cu, ctx);
      List<Comment> cuComments = cu.getComments();
      if (!defaultCatchComments.isEmpty()) {
         if (cuComments != null && !cuComments.isEmpty()) {
            addOnlyNotOverlappedComments(cuComments);
         } else {
            cu.setComments(defaultCatchComments);
         }
      }
   }

   private void addOnlyNotOverlappedComments(List<Comment> cuComments) {
      List<Comment> commentsToAdd = new ArrayList<Comment>();
      for (Comment catchComment : defaultCatchComments) {
         for (Comment cuComment : cuComments) {
            if (!areOverlappedComments(cuComment, catchComment)) {
               commentsToAdd.add(catchComment);
            }
         }
      }
      cuComments.addAll(commentsToAdd);
   }

   private boolean areOverlappedComments(Comment cu1, Comment cu2) {
      return isCommentStartedInsideComment(cu1, cu2) || isCommentStartedInsideComment(cu2, cu1);
   }

   private boolean isCommentStartedInsideComment(Comment cu1, Comment cu2) {
      return (cu1.getBeginLine() > cu2.getBeginLine()
            || cu1.getBeginLine() == cu2.getBeginLine() && cu1.getBeginColumn() >= cu2.getBeginColumn())
            && (cu1.getBeginLine() < cu2.getEndLine()
                  || cu1.getBeginLine() == cu2.getEndLine() && cu1.getEndColumn() <= cu2.getEndColumn());
   }

   private void addDefaultComment(BlockStmt catchBlock) {
      BlockComment blockComment = new BlockComment();
      blockComment.setContent(commentFormat);
      blockComment.setBeginLine(catchBlock.getBeginLine());
      blockComment.setEndLine(catchBlock.getEndLine());
      blockComment.setBeginColumn(catchBlock.getBeginColumn());
      blockComment.setEndColumn(catchBlock.getEndColumn());
      defaultCatchComments.add(blockComment);
   }
}
