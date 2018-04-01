package org.walkmod.checkstyle.treewalkers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.ImportDeclaration;
import org.walkmod.javalang.ast.PackageDeclaration;
import org.walkmod.javalang.ast.SymbolDataAware;
import org.walkmod.javalang.ast.SymbolReference;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;

/**
 * Checks for redundant import statements. An import statement is considered redundant if:
 *
 * - It is a duplicate of another import. This is, when a class is imported more than once.
 * - The class non-statically imported is from the java.lang package, e.g. importing java.lang.String.
 * - The class non-statically imported is from the same package as the current package.
 * @param <A> context
 */
@RequiresSemanticAnalysis
public class RedundantImport<A> extends VoidVisitorAdapter<A> {

   private Map<String, ImportDeclaration> nonAsteriskImports;

   private String package_ = null;

   private Map<String, Set<String>> asteriskImports = new HashMap<String, Set<String>>();

   private void setPackage(CompilationUnit cu) {
      PackageDeclaration pkg = cu.getPackage();
      if (pkg != null) {
         package_ = pkg.getName().toString();
      }
   }

   private void setAsteriskImports(CompilationUnit cu) {
      Iterator<ImportDeclaration> it = cu.getImports().iterator();
      while (it.hasNext()) {
         ImportDeclaration id = it.next();
         if (id.isAsterisk()) {
            asteriskImports.put(id.getName().toString(), new HashSet<String>());
         }
      }
   }

   private void setNonAsteriskImports(CompilationUnit cu) {
      nonAsteriskImports = new HashMap<String, ImportDeclaration>();
      Iterator<ImportDeclaration> it = cu.getImports().iterator();
      while (it.hasNext()) {
         ImportDeclaration id = it.next();
         String name = id.getName().toString();
         if (!id.isAsterisk()) {
            nonAsteriskImports.put(name, id);
         }
      }
   }

   private void setRedundantImportsForAsterisk(CompilationUnit cu) {
      Iterator<ImportDeclaration> it = cu.getImports().iterator();
      while (it.hasNext()) {
         ImportDeclaration id = it.next();
         String name = id.getName().toString();
         if (!id.isAsterisk()) {
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
   }

   @Override
   public void visit(CompilationUnit cu, A ctx) {
      List<ImportDeclaration> imports = cu.getImports();
      if (imports != null) {
         setPackage(cu);
         setAsteriskImports(cu);
         setNonAsteriskImports(cu);
         setRedundantImportsForAsterisk(cu);

         ImportsCleaner ic = new ImportsCleaner();
         Iterator<ImportDeclaration> it = imports.iterator();
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

      private boolean isInTheSamePackage(String importName) {

         if (package_ == null) {
            return false;
         }
         if (!importName.startsWith(package_)) {
            return false;
         }

         String[] packageTokens = package_.split("\\.");
         String[] tokens = importName.split("\\.");
         return (tokens.length == packageTokens.length);
      }

      private boolean isRedundantJavaLangImport(String importedName) {
         return importedName.startsWith("java.lang") && importedName.split("\\.").length == 3;
      }

      private boolean requiresAsteriskImport(ImportDeclaration importDeclaration) {

         String importedName = importDeclaration.getName().toString();

         if(asteriskImports.get(importedName).isEmpty()) {
            return true;
         }

         List<SymbolReference> usages = importDeclaration.getUsages();
         Set<String> usedImports = new HashSet<String>();

         if (usages != null) {
            Iterator<SymbolReference> it = usages.iterator();
            while (it.hasNext()) {
               SymbolReference current = it.next();
               if (current instanceof SymbolDataAware<?>) {
                  SymbolDataAware<?> sda = (SymbolDataAware<?>) current;
                  String typeName = sda.getSymbolData().getName();
                  usedImports.add(typeName);
               }
            }
         }

         return !nonAsteriskImports.keySet().containsAll(usedImports);
      }


      @Override
      public void visit(ImportDeclaration importDeclaration, A ctx) {
         String importedName = importDeclaration.getName().toString();
         if (nonAsteriskImports.containsKey(importedName)) {

            if (isRedundantJavaLangImport(importedName)) {
               return;
            }

            if (importDeclaration.isStatic() || !isInTheSamePackage(importedName)) {
               correctImports.add(importDeclaration);
            }
         } else {
            if (!isInTheSamePackage(importedName)) {
               if (importDeclaration.getUsages() == null && importDeclaration.isNewNode()) {
                  correctImports.add(importDeclaration);
               } else  if (requiresAsteriskImport(importDeclaration)){
                  correctImports.add(importDeclaration);
               }
            }
         }
      }
   }
}
