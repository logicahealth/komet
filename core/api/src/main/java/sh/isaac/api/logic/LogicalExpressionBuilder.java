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



package sh.isaac.api.logic;

//~--- JDK imports ------------------------------------------------------------

import java.time.Instant;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.logic.assertions.*;
import sh.isaac.api.logic.assertions.connectors.And;
import sh.isaac.api.logic.assertions.connectors.Connector;
import sh.isaac.api.logic.assertions.connectors.DisjointWith;
import sh.isaac.api.logic.assertions.connectors.Or;
import sh.isaac.api.logic.assertions.literal.BooleanLiteral;
import sh.isaac.api.logic.assertions.literal.InstantLiteral;
import sh.isaac.api.logic.assertions.literal.IntegerLiteral;
import sh.isaac.api.logic.assertions.literal.LiteralAssertion;
import sh.isaac.api.logic.assertions.literal.StringLiteral;
import sh.isaac.api.logic.assertions.substitution.BooleanSubstitution;
import sh.isaac.api.logic.assertions.substitution.ConceptSubstitution;
import sh.isaac.api.logic.assertions.substitution.InstantSubstitution;
import sh.isaac.api.logic.assertions.substitution.IntegerSubstitution;
import sh.isaac.api.logic.assertions.substitution.StringSubstitution;
import sh.isaac.api.logic.assertions.substitution.SubstitutionFieldSpecification;
import sh.isaac.api.logic.assertions.literal.DoubleLiteral;
import sh.isaac.api.logic.assertions.substitution.DoubleSubstitution;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface LogicalExpressionBuilder.
 *
 * @author kec
 */
public interface LogicalExpressionBuilder {
   /**
    * Adds the to root.
    *
    * @param logicalSet the logical set
    */
   static void AddToRoot(LogicalSet logicalSet) {
      logicalSet.getBuilder()
                .addToRoot(logicalSet);
   }

   /**
    * All role.
    *
    * @param roleTypeChronology the role type chronology
    * @param roleRestriction the role restriction
    * @return the all role
    */
   static AllRole AllRole(ConceptChronology roleTypeChronology, Assertion roleRestriction) {
      return roleRestriction.getBuilder()
                            .allRole(roleTypeChronology, roleRestriction);
   }

   /**
    * All role.
    *
    * @param roleTypeSpecification the role type specification
    * @param roleRestriction the role restriction
    * @return the all role
    */
   static AllRole AllRole(ConceptSpecification roleTypeSpecification, Assertion roleRestriction) {
      return roleRestriction.getBuilder()
                            .allRole(roleTypeSpecification, roleRestriction);
   }

   /**
    * And.
    *
    * @param assertions the assertions
    * @return the and
    */
   static And And(Assertion... assertions) {
      return assertions[0].getBuilder()
                          .and(assertions);
   }

   /**
    * Boolean literal.
    *
    * @param literal the literal
    * @param builder the builder
    * @return the boolean literal
    */
   static BooleanLiteral BooleanLiteral(boolean literal, LogicalExpressionBuilder builder) {
      return builder.booleanLiteral(literal);
   }

   /**
    * Boolean substitution.
    *
    * @param fieldSpecification the field specification
    * @param builder the builder
    * @return the boolean substitution
    */
   static BooleanSubstitution BooleanSubstitution(SubstitutionFieldSpecification fieldSpecification,
         LogicalExpressionBuilder builder) {
      return builder.booleanSubstitution(fieldSpecification);
   }

   /**
    * Concept assertion.
    *
    * @param conceptChronology the concept chronology
    * @param builder the builder
    * @return the concept assertion
    */
   static ConceptAssertion ConceptAssertion(ConceptChronology conceptChronology, LogicalExpressionBuilder builder) {
      return builder.conceptAssertion(conceptChronology);
   }

   /**
    * Concept assertion.
    *
    * @param conceptSpecification the concept specification
    * @param builder the builder
    * @return the concept assertion
    */
   static ConceptAssertion ConceptAssertion(ConceptSpecification conceptSpecification,
         LogicalExpressionBuilder builder) {
      return builder.conceptAssertion(conceptSpecification);
   }

   /**
    * Concept assertion.
    *
    * @param conceptId the concept id
    * @param builder the builder
    * @return the concept assertion
    */
   static ConceptAssertion ConceptAssertion(Integer conceptId, LogicalExpressionBuilder builder) {
      return builder.conceptAssertion(conceptId);
   }

   /**
    * Concept substitution.
    *
    * @param fieldSpecification the field specification
    * @param builder the builder
    * @return the concept substitution
    */
   static ConceptSubstitution ConceptSubstitution(SubstitutionFieldSpecification fieldSpecification,
         LogicalExpressionBuilder builder) {
      return builder.conceptSubstitution(fieldSpecification);
   }

   /**
    * Disjoint with.
    *
    * @param conceptChronology the concept chronology
    * @param builder the builder
    * @return the disjoint with
    */
   static DisjointWith DisjointWith(ConceptChronology conceptChronology, LogicalExpressionBuilder builder) {
      return builder.disjointWith(conceptChronology);
   }

   /**
    * Disjoint with.
    *
    * @param conceptSpecification the concept specification
    * @param builder the builder
    * @return the disjoint with
    */
   static DisjointWith DisjointWith(ConceptSpecification conceptSpecification, LogicalExpressionBuilder builder) {
      return builder.disjointWith(conceptSpecification);
   }

   /**
    * Feature.
    *
    * @param featureTypeChronology the feature type chronology
    * @param literal the literal
    * @return the feature
    */
   static Feature Feature(ConceptChronology featureTypeChronology, ConceptChronology measureSemanticChronology, ConcreteDomainOperators operator, LiteralAssertion literal) {
      return literal.getBuilder()
                    .feature(featureTypeChronology, measureSemanticChronology, operator, literal);
   }

   /**
    * Feature.
    *
    * @param featureTypeSpecification the feature type specification
    * @param literal the literal
    * @return the feature
    */
   static Feature Feature(ConceptSpecification featureTypeSpecification, ConceptSpecification measureSemanticSpecification, ConcreteDomainOperators operator, LiteralAssertion literal) {
      return literal.getBuilder()
                    .feature(featureTypeSpecification, measureSemanticSpecification, operator, literal);
   }

   /**
    * Float literal.
    *
    * @param literal the literal
    * @param builder the builder
    * @return the float literal
    */
   static DoubleLiteral DoubleLiteral(double literal, LogicalExpressionBuilder builder) {
      return builder.doubleLiteral(literal);
   }

   /**
    * Float substitution.
    *
    * @param fieldSpecification the field specification
    * @param builder the builder
    * @return the float substitution
    */
   static DoubleSubstitution FloatSubstitution(SubstitutionFieldSpecification fieldSpecification,
         LogicalExpressionBuilder builder) {
      return builder.doubleSubstitution(fieldSpecification);
   }

   /**
    * Instant literal.
    *
    * @param literal the literal
    * @param builder the builder
    * @return the instant literal
    */
   static InstantLiteral InstantLiteral(Instant literal, LogicalExpressionBuilder builder) {
      return builder.instantLiteral(literal);
   }

   /**
    * Instant substitution.
    *
    * @param fieldSpecification the field specification
    * @param builder the builder
    * @return the instant substitution
    */
   static InstantSubstitution InstantSubstitution(SubstitutionFieldSpecification fieldSpecification,
         LogicalExpressionBuilder builder) {
      return builder.instantSubstitution(fieldSpecification);
   }

   /**
    * Integer literal.
    *
    * @param literal the literal
    * @param builder the builder
    * @return the integer literal
    */
   static IntegerLiteral IntegerLiteral(int literal, LogicalExpressionBuilder builder) {
      return builder.integerLiteral(literal);
   }

   /**
    * Integer substitution.
    *
    * @param fieldSpecification the field specification
    * @param builder the builder
    * @return the integer substitution
    */
   static IntegerSubstitution IntegerSubstitution(SubstitutionFieldSpecification fieldSpecification,
         LogicalExpressionBuilder builder) {
      return builder.integerSubstitution(fieldSpecification);
   }

   /**
    * Necessary set.
    *
    * @param connector the {@link And} or {@link Or} node
    * @return the necessary set
    */
   static NecessarySet NecessarySet(Connector connector) {
      return connector.getBuilder()
                          .necessarySet(connector);
   }
   static PropertySet PropertySet(Connector connector) {
      return connector.getBuilder()
              .propertySet(connector);
   }

   /**
    * Or.
    *
    * @param assertions the assertions
    * @return the or
    */
   static Or Or(Assertion... assertions) {
      return assertions[0].getBuilder()
                          .or(assertions);
   }

   /**
    * Some role.
    *
    * @param roleTypeChronology the role type chronology
    * @param roleRestriction the role restriction
    * @return the some role
    */
   static SomeRole SomeRole(ConceptChronology roleTypeChronology, Assertion roleRestriction) {
      return roleRestriction.getBuilder()
                            .someRole(roleTypeChronology, roleRestriction);
   }

   /**
    * Some role.
    *
    * @param roleTypeSpecification the role type specification
    * @param roleRestriction the role restriction
    * @return the some role
    */
   static SomeRole SomeRole(ConceptSpecification roleTypeSpecification, Assertion roleRestriction) {
      return roleRestriction.getBuilder()
                            .someRole(roleTypeSpecification, roleRestriction);
   }

   /**
    * Some role.
    *
    * @param roleTypeConceptNid the role type concept nid
    * @param roleRestriction the role restriction
    * @return the some role
    */
   static SomeRole SomeRole(Integer roleTypeConceptNid, Assertion roleRestriction) {
      return roleRestriction.getBuilder()
                            .someRole(roleTypeConceptNid, roleRestriction);
   }

   /**
    * String literal.
    *
    * @param literal the literal
    * @param builder the builder
    * @return the string literal
    */
   static StringLiteral StringLiteral(String literal, LogicalExpressionBuilder builder) {
      return builder.stringLiteral(literal);
   }

   /**
    * String substitution.
    *
    * @param fieldSpecification the field specification
    * @param builder the builder
    * @return the string substitution
    */
   static StringSubstitution StringSubstitution(SubstitutionFieldSpecification fieldSpecification,
         LogicalExpressionBuilder builder) {
      return builder.stringSubstitution(fieldSpecification);
   }

   /**
    * Sufficient set.
    *
    * @param connector the {@link And} or {@link Or} node
    * @return the sufficient set
    */
   static SufficientSet SufficientSet(Connector connector) {
      return connector.getBuilder()
                          .sufficientSet(connector);
   }

   /**
    * Template.
    *
    * @param templateConcept the template concept
    * @param assemblageToPopulateTemplateConcept the assemblage to populate template concept
    * @param builder the builder
    * @return the template
    */
   static Template Template(ConceptChronology templateConcept,
                            ConceptChronology assemblageToPopulateTemplateConcept,
                            LogicalExpressionBuilder builder) {
      return builder.template(templateConcept, assemblageToPopulateTemplateConcept);
   }

   /**
    * Template.
    *
    * @param templateSpecification the template specification
    * @param assemblageToPopulateTemplateSpecification the assemblage to populate template specification
    * @param builder the builder
    * @return the template
    */
   static Template Template(ConceptSpecification templateSpecification,
                            ConceptSpecification assemblageToPopulateTemplateSpecification,
                            LogicalExpressionBuilder builder) {
      return builder.template(templateSpecification, assemblageToPopulateTemplateSpecification);
   }

   /**
    * Adds the to root.
    *
    * @param logicalSet the logical set
    */
   void addToRoot(LogicalSet logicalSet);

   /**
    * All role.
    *
    * @param roleTypeChronology the role type chronology
    * @param roleRestriction the role restriction
    * @return the all role
    */
   AllRole allRole(ConceptChronology roleTypeChronology, Assertion roleRestriction);

   /**
    * All role.
    *
    * @param roleTypeSpecification the role type specification
    * @param roleRestriction the role restriction
    * @return the all role
    */
   AllRole allRole(ConceptSpecification roleTypeSpecification, Assertion roleRestriction);

   /**
    * And.
    *
    * @param assertions the assertions
    * @return the and
    */
   And and(Assertion... assertions);

   /**
    * Boolean literal.
    *
    * @param booleanLiteral the boolean literal
    * @return the boolean literal
    */
   BooleanLiteral booleanLiteral(boolean booleanLiteral);

   /**
    * Boolean substitution.
    *
    * @param fieldSpecification the field specification
    * @return the boolean substitution
    */
   BooleanSubstitution booleanSubstitution(SubstitutionFieldSpecification fieldSpecification);

   /**
    * Builds the.
    *
    * @return the logical expression
    * @throws IllegalStateException the illegal state exception
    */
   LogicalExpression build()
            throws IllegalStateException;

   /**
    * Use to add a subtree from an existing logical expression to
    * an expression being built.
    * @param subTreeRoot The root node, which is included in the new expression,
    * along with its children.
    * @return an Assertion corresponding to the node equivalent to the
    * {@code subTreeRoot} in the new expression.
    */
   Assertion cloneSubTree(LogicNode subTreeRoot);

   /**
    * Concept assertion.
    *
    * @param conceptChronology the concept chronology
    * @return the concept assertion
    */
   ConceptAssertion conceptAssertion(ConceptChronology conceptChronology);

   /**
    * Concept assertion.
    *
    * @param conceptSpecification the concept specification
    * @return the concept assertion
    */
   ConceptAssertion conceptAssertion(ConceptSpecification conceptSpecification);

   /**
    * Concept assertion.
    *
    * @param conceptId the concept id
    * @return the concept assertion
    */
   ConceptAssertion conceptAssertion(Integer conceptId);

   /**
    * Concept substitution.
    *
    * @param fieldSpecification the field specification
    * @return the concept substitution
    */
   ConceptSubstitution conceptSubstitution(SubstitutionFieldSpecification fieldSpecification);

   /**
    * Disjoint with.
    *
    * @param conceptChronology the concept chronology
    * @return the disjoint with
    */
   DisjointWith disjointWith(ConceptChronology conceptChronology);

   /**
    * Disjoint with.
    *
    * @param conceptSpecification the concept specification
    * @return the disjoint with
    */
   DisjointWith disjointWith(ConceptSpecification conceptSpecification);

   /**
    * Feature.
    *
    * @param featureTypeChronology the feature type chronology
    * @param literal the literal
    * @return the feature
    */
   Feature feature(ConceptChronology featureTypeChronology, ConceptChronology measureSemanticChronology, ConcreteDomainOperators operator, LiteralAssertion literal);

   /**
    * Feature.
    *
    * @param featureTypeSpecification the feature type specification
    * @param literal the literal
    * @return the feature
    */
   Feature feature(ConceptSpecification featureTypeSpecification, ConceptSpecification measureSemanticSpecification, ConcreteDomainOperators operator, LiteralAssertion literal);

   /**
    * Float literal.
    *
    * @param doubleLiteral the float literal
    * @return the float literal
    */
   DoubleLiteral doubleLiteral(double doubleLiteral);

   /**
    * Float substitution.
    *
    * @param fieldSpecification the field specification
    * @return the float substitution
    */
   DoubleSubstitution doubleSubstitution(SubstitutionFieldSpecification fieldSpecification);

   /**
    * Instant literal.
    *
    * @param instantLiteral the instant literal
    * @return the instant literal
    */
   InstantLiteral instantLiteral(Instant instantLiteral);

   /**
    * Instant substitution.
    *
    * @param fieldSpecification the field specification
    * @return the instant substitution
    */
   InstantSubstitution instantSubstitution(SubstitutionFieldSpecification fieldSpecification);

   /**
    * Integer literal.
    *
    * @param integerLiteral the integer literal
    * @return the integer literal
    */
   IntegerLiteral integerLiteral(int integerLiteral);

   /**
    * Integer substitution.
    *
    * @param fieldSpecification the field specification
    * @return the integer substitution
    */
   IntegerSubstitution integerSubstitution(SubstitutionFieldSpecification fieldSpecification);

   /**
    * Necessary set.
    *
    * @param connector the connector - expected to be an AND or a OR node
    * @return the necessary set
    */
   NecessarySet necessarySet(Connector connector);

   PropertySet propertySet(Connector connector);

   /**
    * Or.
    *
    * @param assertions the assertions
    * @return the or
    */
   Or or(Assertion... assertions);

   /**
    * Some role.
    *
    * @param roleTypeChronology the role type chronology
    * @param roleRestriction the role restriction
    * @return the some role
    */
   SomeRole someRole(ConceptChronology roleTypeChronology, Assertion roleRestriction);

   /**
    * Some role.
    *
    * @param roleTypeSpecification the role type specification
    * @param roleRestriction the role restriction
    * @return the some role
    */
   SomeRole someRole(ConceptSpecification roleTypeSpecification, Assertion roleRestriction);

   /**
    * Some role.
    *
    * @param roleTypeConceptNid the role type concept nid
    * @param roleRestriction the role restriction
    * @return the some role
    */
   SomeRole someRole(Integer roleTypeConceptNid, Assertion roleRestriction);

   /**
    * String literal.
    *
    * @param stringLiteral the string literal
    * @return the string literal
    */
   StringLiteral stringLiteral(String stringLiteral);

   /**
    * String substitution.
    *
    * @param fieldSpecification the field specification
    * @return the string substitution
    */
   StringSubstitution stringSubstitution(SubstitutionFieldSpecification fieldSpecification);

   /**
    * Sufficient set.
    *
    * @param connector the connector - expected to be an And or an OR node
    * @return the sufficient set
    */
   SufficientSet sufficientSet(Connector connector);

   /**
    * Template.
    *
    * @param templateChronology the template chronology
    * @param assemblageToPopulateTemplateConcept the assemblage to populate template concept
    * @return the template
    */
   Template template(ConceptChronology templateChronology, ConceptChronology assemblageToPopulateTemplateConcept);

   /**
    * Template.
    *
    * @param templateSpecification the template specification
    * @param assemblageToPopulateTemplateSpecification the assemblage to populate template specification
    * @return the template
    */
   Template template(ConceptSpecification templateSpecification,
                     ConceptSpecification assemblageToPopulateTemplateSpecification);

   PropertyPatternImplication propertyPatternImplication(int[] propertyPatternNids, int implicationNid);

   PropertyPatternImplication propertyPatternImplication(ConceptSpecification[] propertyPattern, ConceptSpecification implication, LogicalExpressionBuilder builder);

   static PropertyPatternImplication PropertyPatternImplication(int[] propertyPatternNids, int implicationNid, LogicalExpressionBuilder builder) {
      return builder.propertyPatternImplication(propertyPatternNids, implicationNid);
   }

   static PropertyPatternImplication PropertyPatternImplication(ConceptSpecification[] propertyPattern, ConceptSpecification implication, LogicalExpressionBuilder builder) {
      int[] propertyPatternNids = new int[propertyPattern.length];
      for (int i = 0; i < propertyPattern.length; i++) {
         propertyPatternNids[i] = Get.nidForUuids(propertyPattern[i].getUuids());
      }
      return builder.propertyPatternImplication(propertyPatternNids, Get.nidForUuids(implication.getUuids()));
   }
}

