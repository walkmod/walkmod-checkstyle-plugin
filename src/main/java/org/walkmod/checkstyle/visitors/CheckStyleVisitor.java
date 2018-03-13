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
import org.walkmod.javalang.ast.BlockComment;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.ImportDeclaration;
import org.walkmod.javalang.ast.LineComment;
import org.walkmod.javalang.ast.PackageDeclaration;
import org.walkmod.javalang.ast.TypeParameter;
import org.walkmod.javalang.ast.body.AnnotationDeclaration;
import org.walkmod.javalang.ast.body.AnnotationMemberDeclaration;
import org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration;
import org.walkmod.javalang.ast.body.ConstructorDeclaration;
import org.walkmod.javalang.ast.body.EmptyMemberDeclaration;
import org.walkmod.javalang.ast.body.EmptyTypeDeclaration;
import org.walkmod.javalang.ast.body.EnumConstantDeclaration;
import org.walkmod.javalang.ast.body.EnumDeclaration;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.InitializerDeclaration;
import org.walkmod.javalang.ast.body.JavadocComment;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.body.MultiTypeParameter;
import org.walkmod.javalang.ast.body.Parameter;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.body.VariableDeclaratorId;
import org.walkmod.javalang.ast.expr.ArrayAccessExpr;
import org.walkmod.javalang.ast.expr.ArrayCreationExpr;
import org.walkmod.javalang.ast.expr.ArrayInitializerExpr;
import org.walkmod.javalang.ast.expr.AssignExpr;
import org.walkmod.javalang.ast.expr.BinaryExpr;
import org.walkmod.javalang.ast.expr.BooleanLiteralExpr;
import org.walkmod.javalang.ast.expr.CastExpr;
import org.walkmod.javalang.ast.expr.CharLiteralExpr;
import org.walkmod.javalang.ast.expr.ClassExpr;
import org.walkmod.javalang.ast.expr.ConditionalExpr;
import org.walkmod.javalang.ast.expr.DoubleLiteralExpr;
import org.walkmod.javalang.ast.expr.EnclosedExpr;
import org.walkmod.javalang.ast.expr.FieldAccessExpr;
import org.walkmod.javalang.ast.expr.InstanceOfExpr;
import org.walkmod.javalang.ast.expr.IntegerLiteralExpr;
import org.walkmod.javalang.ast.expr.IntegerLiteralMinValueExpr;
import org.walkmod.javalang.ast.expr.LambdaExpr;
import org.walkmod.javalang.ast.expr.LongLiteralExpr;
import org.walkmod.javalang.ast.expr.LongLiteralMinValueExpr;
import org.walkmod.javalang.ast.expr.MarkerAnnotationExpr;
import org.walkmod.javalang.ast.expr.MemberValuePair;
import org.walkmod.javalang.ast.expr.MethodCallExpr;
import org.walkmod.javalang.ast.expr.MethodReferenceExpr;
import org.walkmod.javalang.ast.expr.NameExpr;
import org.walkmod.javalang.ast.expr.NormalAnnotationExpr;
import org.walkmod.javalang.ast.expr.NullLiteralExpr;
import org.walkmod.javalang.ast.expr.ObjectCreationExpr;
import org.walkmod.javalang.ast.expr.QualifiedNameExpr;
import org.walkmod.javalang.ast.expr.SingleMemberAnnotationExpr;
import org.walkmod.javalang.ast.expr.StringLiteralExpr;
import org.walkmod.javalang.ast.expr.SuperExpr;
import org.walkmod.javalang.ast.expr.ThisExpr;
import org.walkmod.javalang.ast.expr.TypeExpr;
import org.walkmod.javalang.ast.expr.UnaryExpr;
import org.walkmod.javalang.ast.expr.VariableDeclarationExpr;
import org.walkmod.javalang.ast.stmt.AssertStmt;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.stmt.BreakStmt;
import org.walkmod.javalang.ast.stmt.CatchClause;
import org.walkmod.javalang.ast.stmt.ContinueStmt;
import org.walkmod.javalang.ast.stmt.DoStmt;
import org.walkmod.javalang.ast.stmt.EmptyStmt;
import org.walkmod.javalang.ast.stmt.ExplicitConstructorInvocationStmt;
import org.walkmod.javalang.ast.stmt.ExpressionStmt;
import org.walkmod.javalang.ast.stmt.ForStmt;
import org.walkmod.javalang.ast.stmt.ForeachStmt;
import org.walkmod.javalang.ast.stmt.IfStmt;
import org.walkmod.javalang.ast.stmt.LabeledStmt;
import org.walkmod.javalang.ast.stmt.ReturnStmt;
import org.walkmod.javalang.ast.stmt.SwitchEntryStmt;
import org.walkmod.javalang.ast.stmt.SwitchStmt;
import org.walkmod.javalang.ast.stmt.SynchronizedStmt;
import org.walkmod.javalang.ast.stmt.ThrowStmt;
import org.walkmod.javalang.ast.stmt.TryStmt;
import org.walkmod.javalang.ast.stmt.TypeDeclarationStmt;
import org.walkmod.javalang.ast.stmt.WhileStmt;
import org.walkmod.javalang.ast.type.ClassOrInterfaceType;
import org.walkmod.javalang.ast.type.IntersectionType;
import org.walkmod.javalang.ast.type.PrimitiveType;
import org.walkmod.javalang.ast.type.ReferenceType;
import org.walkmod.javalang.ast.type.VoidType;
import org.walkmod.javalang.ast.type.WildcardType;
import org.walkmod.javalang.compiler.symbols.RequiresSemanticAnalysis;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

@RequiresSemanticAnalysis
public class CheckStyleVisitor extends VoidVisitorAdapter<VisitorContext> {

   private ConfigurationReader reader;

   private Set<String> rules = new HashSet<String>();

   private LinkedList<AbstractCheckStyleRule<?>> visitors;

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
            visitors = new LinkedList<AbstractCheckStyleRule<?>>();
            for (String rule : rules) {
               String beanName = "org.walkmod:walkmod-checkstyle-plugin:" + rule;
               if (ctx.getArchitectureConfig().getConfiguration().containsBean(beanName)) {
                  Object o = ctx.getBean(beanName, null);
                  if (o instanceof AbstractCheckStyleRule) {
                     AbstractCheckStyleRule<?> aux = (AbstractCheckStyleRule<?>) o;
                     aux.visitChildren(false);
                     visitors.add(aux);
                  }
               }
            }
         }
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(cu, null);
         }
         super.visit(cu, ctx);
      }
   }

   public void visit(PackageDeclaration n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ImportDeclaration n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(TypeParameter n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(LineComment n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(BlockComment n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ClassOrInterfaceDeclaration n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(EnumDeclaration n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(EmptyTypeDeclaration n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(EnumConstantDeclaration n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(AnnotationDeclaration n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(AnnotationMemberDeclaration n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(FieldDeclaration n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(VariableDeclarator n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(VariableDeclaratorId n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ConstructorDeclaration n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(MethodDeclaration n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(Parameter n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(EmptyMemberDeclaration n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(InitializerDeclaration n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(JavadocComment n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ClassOrInterfaceType n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(PrimitiveType n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ReferenceType n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(VoidType n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(WildcardType n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ArrayAccessExpr n, VisitorContext ctx) {
      for (AbstractCheckStyleRule<?> visitor : visitors) {
         visitor.visit(n, null);
      }
      super.visit(n, ctx);
   }

   public void visit(ArrayCreationExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ArrayInitializerExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(AssignExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(BinaryExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(CastExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ClassExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ConditionalExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(EnclosedExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(FieldAccessExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(InstanceOfExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(StringLiteralExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(IntegerLiteralExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(LongLiteralExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(IntegerLiteralMinValueExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(LongLiteralMinValueExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(CharLiteralExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(DoubleLiteralExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(BooleanLiteralExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(NullLiteralExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(MethodCallExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(NameExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ObjectCreationExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(QualifiedNameExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ThisExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(SuperExpr n, VisitorContext ctx) {
      for (AbstractCheckStyleRule<?> visitor : visitors) {
         visitor.visit(n, null);
      }
      super.visit(n, ctx);
   }

   public void visit(UnaryExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(VariableDeclarationExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(MarkerAnnotationExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(SingleMemberAnnotationExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(NormalAnnotationExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(MemberValuePair n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ExplicitConstructorInvocationStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(TypeDeclarationStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(AssertStmt n, VisitorContext ctx) {
      for (AbstractCheckStyleRule<?> visitor : visitors) {
         visitor.visit(n, null);
      }
      super.visit(n, ctx);
   }

   public void visit(BlockStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(LabeledStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(EmptyStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ExpressionStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(SwitchStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(SwitchEntryStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(BreakStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ReturnStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(IfStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(WhileStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ContinueStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(DoStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ForeachStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ForStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(ThrowStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(SynchronizedStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(TryStmt n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(CatchClause n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(MultiTypeParameter n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(LambdaExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(MethodReferenceExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(TypeExpr n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

   public void visit(IntersectionType n, VisitorContext ctx) {
      if (visitors != null) {
         for (AbstractCheckStyleRule<?> visitor : visitors) {
            visitor.visit(n, null);
         }
      }
      super.visit(n, ctx);
   }

}
