package org.walkmod.checkstyle.treewalkers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ParseException;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.ImportDeclaration;
import org.walkmod.javalang.ast.SymbolData;
import org.walkmod.javalang.ast.SymbolDataAware;
import org.walkmod.javalang.ast.SymbolReference;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;

@RequiresSemanticAnalysis
public class AvoidStarImport<A> extends VoidVisitorAdapter<A> {


   @Override
   public void visit(ImportDeclaration node, A ctx) {
      if (node.isAsterisk()) {
         List<SymbolReference> refs = node.getUsages();
         Map<String, List<SymbolReference>> classes = new HashMap<String, List<SymbolReference>>();
         if (refs != null) {
            for (SymbolReference sr : refs) {
               if (sr instanceof SymbolDataAware<?>) {
                  SymbolDataAware<?> aux = (SymbolDataAware<?>) sr;
                  SymbolData sd = aux.getSymbolData();
                  if (sd != null && ! sd.getClazz().isMemberClass()) {
                     List<SymbolReference> refsAux = null;

                     String symbolClassName = sd.getName();

                     if (classes.containsKey(symbolClassName)) {
                        refsAux = classes.get(symbolClassName);
                     } else {
                        refsAux = new LinkedList<SymbolReference>();
                        classes.put(symbolClassName, refsAux);
                     }
                     refsAux.add(sr);

                  }
               }
            }
         }
         CompilationUnit cu = (CompilationUnit) node.getParentNode();
         List<ImportDeclaration> imports = new LinkedList<ImportDeclaration>(cu.getImports());
         Iterator<ImportDeclaration> it = imports.iterator();
         int i = -1;
         boolean errored = false;
         boolean found = false;
         while (it.hasNext() && !found) {
            i++;
            ImportDeclaration current = it.next();
            found = (current == node);
         }
         if (found) {
            it.remove();
            if (!classes.isEmpty()) {
               for (String clazz : classes.keySet()) {
                  ImportDeclaration id;
                  try {
                     id = new ImportDeclaration((NameExpr) ASTManager.parse(NameExpr.class, clazz), node.isStatic(),
                           false);
                     List<SymbolReference> refsAux = classes.get(clazz);
                     for(SymbolReference sr: refsAux){
                        sr.setSymbolDefinition(id);
                     }
                     id.setUsages(refsAux);
                     imports.add(i, id);
                  } catch (ParseException e) {
                     errored = true;
                     //probably it is an static import
                  }
               }
            }
         }
         if (!errored) {
            cu.setImports(imports);
         }
      }
   }
}
