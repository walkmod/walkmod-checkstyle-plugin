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

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.util.DomHelper;
import org.walkmod.walkers.VisitorContext;
import org.xml.sax.InputSource;

@RequiresSemanticAnalysis
public class CheckStyleVisitor extends VoidVisitorAdapter<VisitorContext> {

   private String configurationfile;

   private Set<String> rules = new HashSet<String>();

   public void setConfigurationFile(String configurationFile) throws Exception {
      this.configurationfile = configurationFile;
      parseCfg(configurationfile);
   }
   
   protected Set<String> getRules(){
      return rules;
   }

   private void parseCfg(String config) throws Exception {
      File cfgFile = new File(config);
      FileInputStream is = new FileInputStream(cfgFile);
      try {
         InputSource in = new InputSource(is);
         in.setSystemId(configurationfile);
         Document doc = DomHelper.parse(in);
         NodeList module = doc.getElementsByTagName("module");
         int max = module.getLength();
         for (int i = 0; i < max; i++) {
            Node rule = module.item(i);
            if (rule instanceof Element) {
               Element elem = (Element) rule;
               if (elem.hasAttribute("name")) {
                  String name = elem.getAttribute("name");
                  if (name.equals("TreeWalker")) {
                     NodeList children = elem.getChildNodes();
                     int limit = children.getLength();
                     for (int k = 0; k < limit; k++) {
                        Node child = children.item(k);
                        if (child.getNodeName().equals("module")) {
                           if (child instanceof Element) {
                              Element exclude = (Element) child;
                              String excludeName = exclude.getAttribute("name");
                              this.rules.add(excludeName);
                           }
                        }
                     }

                  }

               }
            }
         }

      } finally {
         is.close();
      }
   }
}
