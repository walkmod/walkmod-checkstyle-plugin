/* 
  Copyright (C) 2016 Raquel Pau.
 
  Walkmod is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  Walkmod is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/
package org.walkmod.checkstyle.visitors;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.walkmod.checkstyle.xml.ConfigurationReader;
import org.walkmod.javalang.ast.CompilationUnit;

import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

@RequiresSemanticAnalysis
public class CheckStyleVisitor extends VoidVisitorAdapter<VisitorContext> {

   private ConfigurationReader reader;

   private Set<String> rules = new HashSet<String>();

   private LinkedList<VoidVisitorAdapter<?>> visitors;

   public void setConfigurationFile(String configurationFile) throws Exception {
      this.reader = new ConfigurationReader(configurationFile);
      this.rules = reader.getRules();
   }

   protected Set<String> getRules() {
      return rules;
   }


   @Override
   public void visit(CompilationUnit cu, VisitorContext ctx) {

      if (reader == null || reader.getConfigurationFile() == null) {
         try {
            setConfigurationFile("sun_checks.xml");
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }
      if (rules != null && !rules.isEmpty()) {
         if (visitors == null) {
            visitors = new LinkedList<VoidVisitorAdapter<?>>();
            for (String rule : rules) {
               String beanName = "org.walkmod:walkmod-checkstyle-plugin:" + rule;
               if (ctx.getArchitectureConfig().getConfiguration().containsBean(beanName)) {
                  Object o = ctx.getBean(beanName, null);
                  if (o instanceof VoidVisitorAdapter) {
                     VoidVisitorAdapter<?> aux = (VoidVisitorAdapter<?>) o;
                     visitors.add(aux);
                  }
               }
            }
         }
         for (VoidVisitorAdapter<?> visitor : visitors) {
            visitor.visit(cu, null);
         }
      }
   }

}
