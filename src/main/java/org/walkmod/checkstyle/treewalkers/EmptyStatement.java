package org.walkmod.checkstyle.treewalkers;

import org.walkmod.checkstyle.visitors.AbstractCheckStyleRule;
import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.EmptyStmt;

public class EmptyStatement<A> extends AbstractCheckStyleRule<A> {

   @Override
   public void visit(EmptyStmt n, A ctx) {
      Node parent = n.getParentNode();
      if (parent instanceof BlockStmt) {
         n.remove();
      }
   }
}
