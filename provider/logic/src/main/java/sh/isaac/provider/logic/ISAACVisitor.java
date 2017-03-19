/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.provider.logic;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionBaseVisitor;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionLexer;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.AttributeContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.AttributeGroupContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.AttributeSetContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.ConceptReferenceContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.ExpressionContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.FocusConceptContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.NonGroupedAttributeSetContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.RefinementContext;
import se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.SubExpressionContext;

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.logic.assertions.SomeRole;
import sh.isaac.api.logic.assertions.connectors.And;
import sh.isaac.utility.Frills;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import static sh.isaac.api.logic.LogicalExpressionBuilder.SomeRole;
import static sh.isaac.api.logic.LogicalExpressionBuilder.SufficientSet;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link ISAACVisitor}
 *
 * @author Tony Weida
 */
public class ISAACVisitor
        extends SNOMEDCTExpressionBaseVisitor<Object> {
   private static final String PC_IRI        = "http://snomed.org/postcoord/";
   private static final String SCTID_IRI     = "http://snomed.info/id/";
   private static final String ROLEGROUP_IRI = "http://snomed.info/id/609096000";
   static Logger               logger        = LogManager.getLogger(ISAACVisitor.class);

   //~--- fields --------------------------------------------------------------

   private ConceptChronology<?>     definiendum_;
   private boolean                  defaultToPrimitive_;
   private LogicalExpressionBuilder defBuilder_;

   //~--- constructors --------------------------------------------------------

   public ISAACVisitor(LogicalExpressionBuilder defBuilder) {
      this(defBuilder, null);
   }

   public ISAACVisitor(LogicalExpressionBuilder defBuilder, ConceptChronology<?> c) {
      this(defBuilder, c, false);
   }

   public ISAACVisitor(LogicalExpressionBuilder defBuilder, ConceptChronology<?> c, boolean defaultToPrimitive) {
      super();
      this.definiendum_        = c;
      this.defaultToPrimitive_ = defaultToPrimitive;
      defBuilder_              = defBuilder;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public Object visitAttribute(se.liu.imt.mi.snomedct.expression.SNOMEDCTExpressionParser.AttributeContext ctx) {
      logger.debug("visitAttribute: " + ctx.getText());

      SomeRole role = null;

      if (ctx.attributeValue()
             .getChild(0)
             .getClass() == SNOMEDCTExpressionParser.ConceptReferenceContext.class) {
         ConceptChronology<?> property = (ConceptChronology<?>) visitConceptReference(ctx.conceptReference());
         ConceptChronology<?> value = (ConceptChronology<?>) visitConceptReference(ctx.attributeValue()
                                                                                      .conceptReference());

         role = SomeRole(property, ConceptAssertion(value, defBuilder_));
      } else if (ctx.attributeValue()
                    .getChild(0)
                    .getClass() == SNOMEDCTExpressionParser.NestedExpressionContext.class) {
         ConceptChronology<?> property = (ConceptChronology<?>) visitConceptReference(ctx.conceptReference());
         Assertion            result   = (Assertion) visit(ctx.attributeValue()
                                                              .nestedExpression()
                                                              .subExpression());

         role = SomeRole(property, result);
      } else {
         logger.warn("Shouldn't ever get here");
      }

      return role;
   }

   @Override
   public Object visitAttributeGroup(AttributeGroupContext ctx) {
      logger.debug("visitAttributeGroup: " + ctx.getText());
      throw new RuntimeException("LOINC EXPRESSION SERVICE> Cannot (yet) handle attribute group");
   }

   @Override
   public Object visitAttributeSet(AttributeSetContext ctx) {
      logger.debug("visitAttributeSet: " + ctx.getText());
      throw new RuntimeException("LOINC EXPRESSION SERVICE> Cannot (yet) handle attribute set");
   }

   @Override
   public Object visitConceptReference(ConceptReferenceContext ctx) {
      logger.debug("visitConceptReference: " + ctx.getText());

      Optional<Integer> nid = Frills.getNidForSCTID(Long.parseLong(ctx.getText()));

      if (!nid.isPresent()) {
         throw new RuntimeException(("LOINC EXPRESSION SERVICE> Missing nid for sctid: " + ctx.getText()));
      }

      return Get.conceptService()
                .getConcept(nid.get());
   }

   @Override
   public Object visitExpression(ExpressionContext ctx) {
      logger.debug("visitExpression: " + ctx.getText());

      Object subExpression = visit(ctx.subExpression());

      if (((ctx.definitionStatus() == null) && (defaultToPrimitive_ == true)) ||
            (((ctx.definitionStatus() != null) &&
              (ctx.definitionStatus().start.getType() == SNOMEDCTExpressionLexer.SC_OF)))) {
         return NecessarySet((And) subExpression);
      } else {
         return SufficientSet((And) subExpression);
      }
   }

   @Override
   public Object visitFocusConcept(FocusConceptContext ctx) {
      logger.debug("visitFocusConcept: " + ctx.getText());

      if (ctx.getChildCount() > 1) {
         throw new RuntimeException("LOINC EXPRESSION SERVICE> Cannot (yet) handle conjoined focus concept");
      }

      return visit(ctx.conceptReference(0));
   }

   // TODO: see more complex processing in OWLVisitor
   @Override
   public Object visitNonGroupedAttributeSet(NonGroupedAttributeSetContext ctx) {
      logger.debug("visitNonGroupedAttributeSet: " + ctx.getText());

      int         childCount = ctx.getChildCount();
      Assertion[] assertions = new Assertion[(childCount + 1) / 2];

      for (int i = 0; i < childCount; i = i + 2) {  // Use an iterator
         assertions[(i + 1) / 2] = (SomeRole) visitAttribute((AttributeContext) ctx.getChild(i));
      }

      return assertions;
   }

   @Override
   public Object visitRefinement(RefinementContext ctx) {
      logger.debug("visitRefinement: " + ctx.getText());
      return visit(ctx.nonGroupedAttributeSet());
   }

   @Override
   public Object visitSubExpression(SubExpressionContext ctx) {
      logger.debug("visitSubExpression: " + ctx.getText());

      Object result;

      if (ctx.getChildCount() > 1) {
         Assertion[] refinementAssertions = (Assertion[]) visit(ctx.refinement());
         Assertion[] assertions           = new Assertion[refinementAssertions.length + 1];

         assertions[0] = ConceptAssertion((ConceptChronology<?>) visit(ctx.focusConcept()), defBuilder_);
         System.arraycopy(refinementAssertions, 0, assertions, 1, refinementAssertions.length);
         result = And(assertions);
      } else {
         result = ConceptAssertion((ConceptChronology<?>) visit(ctx.focusConcept()), defBuilder_);
      }

      return result;
   }
}

