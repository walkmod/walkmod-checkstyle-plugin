package org.walkmod.checkstyle.treewalkers;

import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.EmptyStmt;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;

public class EmptyStatement<A> extends VoidVisitorAdapter<A> {

   @Override
   public void visit(EmptyStmt n, A ctx) {
      Node parent = n.getParentNode();
      if (parent instanceof BlockStmt) {
         n.remove();
      }
   }
}
