package org.walkmod.checkstyle.treewalkers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.walkmod.checkstyle.visitors.AbstractCheckStyleRule;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ParseException;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.ImportDeclaration;
import org.walkmod.javalang.ast.PackageDeclaration;
import org.walkmod.javalang.ast.SymbolDataAware;
import org.walkmod.javalang.ast.SymbolReference;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;

@RequiresSemanticAnalysis
public class RedundantImport<A> extends AbstractCheckStyleRule<A> {

   private Map<String, ImportDeclaration> mapping;

   private String pakage = null;

   private Map<String, Set<String>> asteriskImports = new HashMap<String, Set<String>>();

   @Override
   public void visit(CompilationUnit cu, A ctx) {
      List<ImportDeclaration> imports = cu.getImports();
      if (imports != null) {
         PackageDeclaration pkg = cu.getPackage();
         if (pkg != null) {
            pakage = pkg.getName().toString();
         }
         mapping = new HashMap<String, ImportDeclaration>();

         Iterator<ImportDeclaration> it = imports.iterator();
         while (it.hasNext()) {
            ImportDeclaration id = it.next();
            if (id.isAsterisk()) {
               asteriskImports.put(id.getName().toString(), new HashSet<String>());
            }

         }

         it = imports.iterator();
         while (it.hasNext()) {
            ImportDeclaration id = it.next();
            String name = id.getName().toString();
            if (!id.isAsterisk()) {
               mapping.put(name, id);
               int index = name.lastIndexOf(".");
               if (index != -1) {
                  String pkgName = name.substring(0, index);
                  if (asteriskImports.containsKey(pkgName)) {
                     Set<String> relatedImports = asteriskImports.get(pkgName);
                     relatedImports.add(name);
                  }
               }
            }
         }
         ImportsCleaner ic = new ImportsCleaner();
         it = imports.iterator();
         while (it.hasNext()) {
            it.next().accept(ic, ctx);
         }
         cu.setImports(ic.getImports());

      }
   }

   private class ImportsCleaner extends VoidVisitorAdapter<A> {

      private List<ImportDeclaration> correctImports = new LinkedList<ImportDeclaration>();

      public List<ImportDeclaration> getImports() {
         return correctImports;
      }

      @Override
      public void visit(ImportDeclaration node, A ctx) {

         String importedName = node.getName().toString();

         ImportDeclaration aux = mapping.get(importedName);
         if (aux != null) {
            if (pakage == null || !importedName.startsWith(pakage)) {
               if (aux == node) {
                  if (!importedName.startsWith("java.lang")) {
                     correctImports.add(node);
                  }
               } else {
                  if (node.isStatic()) {
                     correctImports.add(node);
                  }
               }
            }
         } else {
            //isAsterisk
            if (pakage == null || !importedName.startsWith(pakage)) {
               List<SymbolReference> usages = node.getUsages();
               Set<String> relatedImports = asteriskImports.get(importedName);
               Set<String> importsToAdd = new HashSet<String>();
               if (usages != null) {
                  Iterator<SymbolReference> it = usages.iterator();
                  while (it.hasNext()) {
                     SymbolReference current = it.next();
                     if (current instanceof SymbolDataAware<?>) {
                        SymbolDataAware<?> sda = (SymbolDataAware<?>) current;
                        String typeName = sda.getSymbolData().getName();
                        if (!relatedImports.contains(typeName)) {
                           importsToAdd.add(typeName);
                        }
                     }
                  }
               }
               else if(node.isNewNode()){
                  correctImports.add(node); //we don't know if it is needed by other new nodes.
               }
               if (!importsToAdd.isEmpty()) {
                  Iterator<String> it = importsToAdd.iterator();
                  while (it.hasNext()) {
                     String next = it.next();
                     NameExpr name;
                     try {
                        name = (NameExpr) ASTManager.parse(NameExpr.class, next);
                        ImportDeclaration id = new ImportDeclaration(name, false, false);
                        correctImports.add(id);
                     } catch (ParseException e) {
                        throw new RuntimeException(e);
                     }

                  }
               }
            }
         }
      }
   }
}
