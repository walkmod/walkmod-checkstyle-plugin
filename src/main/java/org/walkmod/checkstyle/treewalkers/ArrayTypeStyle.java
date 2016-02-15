package org.walkmod.checkstyle.treewalkers;

import java.util.List;

import org.walkmod.checkstyle.visitors.AbstractCheckStyleRule;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.body.VariableDeclaratorId;
import org.walkmod.javalang.ast.expr.VariableDeclarationExpr;
import org.walkmod.javalang.ast.type.ReferenceType;
import org.walkmod.javalang.ast.type.Type;

public class ArrayTypeStyle<A> extends AbstractCheckStyleRule<A> {

   
   @Override
   public void visit(FieldDeclaration n, A arg) {
      List<VariableDeclarator> list = n.getVariables();
      ReferenceType referenceType = getReferenceType(list, n.getType());
      if (referenceType != null) {
         n.setType(referenceType);
      }
      super.visit(n, arg);
   }

   
   @Override
   public void visit(VariableDeclarationExpr n, A arg) {
      List<VariableDeclarator> list = n.getVars();
      ReferenceType referenceType = getReferenceType(list, n.getType());
      if (referenceType != null) {
         n.setType(referenceType);
      }
      super.visit(n, arg);
   }

   /**
    * Gets the reference type.
    *
    * @param list
    *            the list
    * @param t
    *            the t
    * @return the reference type
    */
   private ReferenceType getReferenceType(List<VariableDeclarator> list, Type t) {
      ReferenceType referenceType = null;
      int arrayCount = -1;
      boolean replace = false;
      for (int i = 0; i < list.size(); i++) {
         VariableDeclarator vd = list.get(i);
         int count = vd.getId().getArrayCount();
         if (arrayCount == -1 || arrayCount == count) {
            arrayCount = count;
            if (arrayCount > 0) {
               replace = true;
            }
         } else {
            replace = false;
            break;
         }
      }
      if (replace) {
         referenceType = new ReferenceType();
         referenceType.setArrayCount(arrayCount);
         referenceType.setType(t);
         for (int i = 0; i < list.size(); i++) {
            VariableDeclarator vd = list.get(i);
            VariableDeclaratorId id = vd.getId();
            id.setArrayCount(0);
         }
      }
      return referenceType;
   }
}
