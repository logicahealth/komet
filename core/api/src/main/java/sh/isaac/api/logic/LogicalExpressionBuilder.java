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

import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.logic.assertions.AllRole;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.logic.assertions.ConceptAssertion;
import sh.isaac.api.logic.assertions.Feature;
import sh.isaac.api.logic.assertions.LogicalSet;
import sh.isaac.api.logic.assertions.NecessarySet;
import sh.isaac.api.logic.assertions.SomeRole;
import sh.isaac.api.logic.assertions.SufficientSet;
import sh.isaac.api.logic.assertions.Template;
import sh.isaac.api.logic.assertions.connectors.And;
import sh.isaac.api.logic.assertions.connectors.Connector;
import sh.isaac.api.logic.assertions.connectors.DisjointWith;
import sh.isaac.api.logic.assertions.connectors.Or;
import sh.isaac.api.logic.assertions.literal.BooleanLiteral;
import sh.isaac.api.logic.assertions.literal.FloatLiteral;
import sh.isaac.api.logic.assertions.literal.InstantLiteral;
import sh.isaac.api.logic.assertions.literal.IntegerLiteral;
import sh.isaac.api.logic.assertions.literal.LiteralAssertion;
import sh.isaac.api.logic.assertions.literal.StringLiteral;
import sh.isaac.api.logic.assertions.substitution.BooleanSubstitution;
import sh.isaac.api.logic.assertions.substitution.ConceptSubstitution;
import sh.isaac.api.logic.assertions.substitution.FloatSubstitution;
import sh.isaac.api.logic.assertions.substitution.InstantSubstitution;
import sh.isaac.api.logic.assertions.substitution.IntegerSubstitution;
import sh.isaac.api.logic.assertions.substitution.StringSubstitution;
import sh.isaac.api.logic.assertions.substitution.SubstitutionFieldSpecification;

//~--- interfaces -------------------------------------------------------------

/**
 *
 * @author kec
 */
public interface LogicalExpressionBuilder {
   static void AddToRoot(LogicalSet logicalSet) {
      logicalSet.getBuilder()
                .addToRoot(logicalSet);
   }

   static AllRole AllRole(ConceptChronology<?> roleTypeChronology, Assertion roleRestriction) {
      return roleRestriction.getBuilder()
                            .allRole(roleTypeChronology, roleRestriction);
   }

   static AllRole AllRole(ConceptSpecification roleTypeSpecification, Assertion roleRestriction) {
      return roleRestriction.getBuilder()
                            .allRole(roleTypeSpecification, roleRestriction);
   }

   static And And(Assertion... assertions) {
      return assertions[0].getBuilder()
                          .and(assertions);
   }

   static BooleanLiteral BooleanLiteral(boolean literal, LogicalExpressionBuilder builder) {
      return builder.booleanLiteral(literal);
   }

   static BooleanSubstitution BooleanSubstitution(SubstitutionFieldSpecification fieldSpecification,
         LogicalExpressionBuilder builder) {
      return builder.booleanSubstitution(fieldSpecification);
   }

   static ConceptAssertion ConceptAssertion(ConceptChronology<?> conceptChronology, LogicalExpressionBuilder builder) {
      return builder.conceptAssertion(conceptChronology);
   }

   static ConceptAssertion ConceptAssertion(ConceptSpecification conceptSpecification,
         LogicalExpressionBuilder builder) {
      return builder.conceptAssertion(conceptSpecification);
   }

   static ConceptAssertion ConceptAssertion(Integer conceptId, LogicalExpressionBuilder builder) {
      return builder.conceptAssertion(conceptId);
   }

   static ConceptSubstitution ConceptSubstitution(SubstitutionFieldSpecification fieldSpecification,
         LogicalExpressionBuilder builder) {
      return builder.conceptSubstitution(fieldSpecification);
   }

   static DisjointWith DisjointWith(ConceptChronology<?> conceptChronology, LogicalExpressionBuilder builder) {
      return builder.disjointWith(conceptChronology);
   }

   static DisjointWith DisjointWith(ConceptSpecification conceptSpecification, LogicalExpressionBuilder builder) {
      return builder.disjointWith(conceptSpecification);
   }

   static Feature Feature(ConceptChronology<?> featureTypeChronology, LiteralAssertion literal) {
      return literal.getBuilder()
                    .feature(featureTypeChronology, literal);
   }

   static Feature Feature(ConceptSpecification featureTypeSpecification, LiteralAssertion literal) {
      return literal.getBuilder()
                    .feature(featureTypeSpecification, literal);
   }

   static FloatLiteral FloatLiteral(float literal, LogicalExpressionBuilder builder) {
      return builder.floatLiteral(literal);
   }

   static FloatSubstitution FloatSubstitution(SubstitutionFieldSpecification fieldSpecification,
         LogicalExpressionBuilder builder) {
      return builder.floatSubstitution(fieldSpecification);
   }

   static InstantLiteral InstantLiteral(Instant literal, LogicalExpressionBuilder builder) {
      return builder.instantLiteral(literal);
   }

   static InstantSubstitution InstantSubstitution(SubstitutionFieldSpecification fieldSpecification,
         LogicalExpressionBuilder builder) {
      return builder.instantSubstitution(fieldSpecification);
   }

   static IntegerLiteral IntegerLiteral(int literal, LogicalExpressionBuilder builder) {
      return builder.integerLiteral(literal);
   }

   static IntegerSubstitution IntegerSubstitution(SubstitutionFieldSpecification fieldSpecification,
         LogicalExpressionBuilder builder) {
      return builder.integerSubstitution(fieldSpecification);
   }

   static NecessarySet NecessarySet(Connector... connectors) {
      return connectors[0].getBuilder()
                          .necessarySet(connectors);
   }

   static Or Or(Assertion... assertions) {
      return assertions[0].getBuilder()
                          .or(assertions);
   }

   static SomeRole SomeRole(ConceptChronology<?> roleTypeChronology, Assertion roleRestriction) {
      return roleRestriction.getBuilder()
                            .someRole(roleTypeChronology, roleRestriction);
   }

   static SomeRole SomeRole(ConceptSpecification roleTypeSpecification, Assertion roleRestriction) {
      return roleRestriction.getBuilder()
                            .someRole(roleTypeSpecification, roleRestriction);
   }

   static SomeRole SomeRole(Integer roleTypeConceptNid, Assertion roleRestriction) {
      return roleRestriction.getBuilder()
                            .someRole(roleTypeConceptNid, roleRestriction);
   }

   static StringLiteral StringLiteral(String literal, LogicalExpressionBuilder builder) {
      return builder.stringLiteral(literal);
   }

   static StringSubstitution StringSubstitution(SubstitutionFieldSpecification fieldSpecification,
         LogicalExpressionBuilder builder) {
      return builder.stringSubstitution(fieldSpecification);
   }

   static SufficientSet SufficientSet(Connector... connectors) {
      return connectors[0].getBuilder()
                          .sufficientSet(connectors);
   }

   static Template Template(ConceptChronology<?> templateConcept,
                            ConceptChronology<?> assemblageToPopulateTemplateConcept,
                            LogicalExpressionBuilder builder) {
      return builder.template(templateConcept, assemblageToPopulateTemplateConcept);
   }

   static Template Template(ConceptSpecification templateSpecification,
                            ConceptSpecification assemblageToPopulateTemplateSpecification,
                            LogicalExpressionBuilder builder) {
      return builder.template(templateSpecification, assemblageToPopulateTemplateSpecification);
   }

   void addToRoot(LogicalSet logicalSet);

   AllRole allRole(ConceptChronology<?> roleTypeChronology, Assertion roleRestriction);

   AllRole allRole(ConceptSpecification roleTypeSpecification, Assertion roleRestriction);

   And and(Assertion... assertions);

   BooleanLiteral booleanLiteral(boolean booleanLiteral);

   BooleanSubstitution booleanSubstitution(SubstitutionFieldSpecification fieldSpecification);

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

   ConceptAssertion conceptAssertion(ConceptChronology<?> conceptChronology);

   ConceptAssertion conceptAssertion(ConceptSpecification conceptSpecification);

   ConceptAssertion conceptAssertion(Integer conceptId);

   ConceptSubstitution conceptSubstitution(SubstitutionFieldSpecification fieldSpecification);

   DisjointWith disjointWith(ConceptChronology<?> conceptChronology);

   DisjointWith disjointWith(ConceptSpecification conceptSpecification);

   Feature feature(ConceptChronology<?> featureTypeChronology, LiteralAssertion literal);

   Feature feature(ConceptSpecification featureTypeSpecification, LiteralAssertion literal);

   FloatLiteral floatLiteral(float floatLiteral);

   FloatSubstitution floatSubstitution(SubstitutionFieldSpecification fieldSpecification);

   InstantLiteral instantLiteral(Instant instantLiteral);

   InstantSubstitution instantSubstitution(SubstitutionFieldSpecification fieldSpecification);

   IntegerLiteral integerLiteral(int integerLiteral);

   IntegerSubstitution integerSubstitution(SubstitutionFieldSpecification fieldSpecification);

   NecessarySet necessarySet(Connector... connector);

   Or or(Assertion... assertions);

   SomeRole someRole(ConceptChronology<?> roleTypeChronology, Assertion roleRestriction);

   SomeRole someRole(ConceptSpecification roleTypeSpecification, Assertion roleRestriction);

   SomeRole someRole(Integer roleTypeConceptNid, Assertion roleRestriction);

   StringLiteral stringLiteral(String stringLiteral);

   StringSubstitution stringSubstitution(SubstitutionFieldSpecification fieldSpecification);

   SufficientSet sufficientSet(Connector... connector);

   Template template(ConceptChronology<?> templateChronology, ConceptChronology<?> assemblageToPopulateTemplateConcept);

   Template template(ConceptSpecification templateSpecification,
                     ConceptSpecification assemblageToPopulateTemplateSpecification);
}

