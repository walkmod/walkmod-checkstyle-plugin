package org.walkmod.checkstyle.treewalkers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.walkmod.checkstyle.visitors.AbstractCheckStyleRule;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.ImportDeclaration;
import org.walkmod.javalang.ast.SymbolReference;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;

@RequiresSemanticAnalysis
public class UnusedImports<A> extends AbstractCheckStyleRule<A> {

   @Override
   public void visit(ImportDeclaration node, A ctx) {
      List<SymbolReference> usages = node.getUsages();
      if (usages == null || usages.isEmpty()) {
         CompilationUnit cu = (CompilationUnit) node.getParentNode();
         List<ImportDeclaration> imports = new LinkedList<ImportDeclaration>(cu.getImports());
         Iterator<ImportDeclaration> it = imports.iterator();

         boolean found = false;
         while (it.hasNext() && !found) {
            ImportDeclaration current = it.next();
            found = (current == node);
         }
         if (found) {
            it.remove();
            cu.setImports(imports);
         }
         
      }
   }
}
