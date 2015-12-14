/*
 * Copyright 2015 kec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.api.logic;

import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.logic.assertions.AllRole;
import gov.vha.isaac.ochre.api.logic.assertions.Assertion;
import gov.vha.isaac.ochre.api.logic.assertions.ConceptAssertion;
import gov.vha.isaac.ochre.api.logic.assertions.Feature;
import gov.vha.isaac.ochre.api.logic.assertions.LogicalSet;
import gov.vha.isaac.ochre.api.logic.assertions.NecessarySet;
import gov.vha.isaac.ochre.api.logic.assertions.SomeRole;
import gov.vha.isaac.ochre.api.logic.assertions.SufficientSet;
import gov.vha.isaac.ochre.api.logic.assertions.Template;
import gov.vha.isaac.ochre.api.logic.assertions.connectors.And;
import gov.vha.isaac.ochre.api.logic.assertions.connectors.Connector;
import gov.vha.isaac.ochre.api.logic.assertions.connectors.DisjointWith;
import gov.vha.isaac.ochre.api.logic.assertions.connectors.Or;
import gov.vha.isaac.ochre.api.logic.assertions.literal.BooleanLiteral;
import gov.vha.isaac.ochre.api.logic.assertions.literal.FloatLiteral;
import gov.vha.isaac.ochre.api.logic.assertions.literal.InstantLiteral;
import gov.vha.isaac.ochre.api.logic.assertions.literal.IntegerLiteral;
import gov.vha.isaac.ochre.api.logic.assertions.literal.LiteralAssertion;
import gov.vha.isaac.ochre.api.logic.assertions.literal.StringLiteral;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.BooleanSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.ConceptSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.FloatSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.InstantSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.IntegerSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.StringSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.SubstitutionFieldSpecification;
import java.time.Instant;

/**
 *
 * @author kec
 */
public interface LogicalExpressionBuilder {

    void addToRoot(LogicalSet logicalSet);
    
    
    static void AddToRoot(LogicalSet logicalSet) {
        logicalSet.getBuilder().addToRoot(logicalSet);
    }

    LogicalExpression build() throws IllegalStateException;
    
    /**
     * Use to add a subtree from an existing logical expression to 
     * an expression being built. 
     * @param subTreeRoot The root node, which is included in the new expression, 
     * along with its children. 
     * @return an Assertion corresponding to the node equivalent to the 
     * {@code subTreeRoot} in the new expression. 
     */
    Assertion cloneSubTree(Node subTreeRoot);

    NecessarySet necessarySet(Connector... connector);

    static NecessarySet NecessarySet(Connector... connectors) {
        return connectors[0].getBuilder().necessarySet(connectors);
    }

    SufficientSet sufficientSet(Connector... connector);

    static SufficientSet SufficientSet(Connector... connectors) {
        return connectors[0].getBuilder().sufficientSet(connectors);
    }

    DisjointWith disjointWith(ConceptChronology<?> conceptChronology);
    DisjointWith disjointWith(ConceptSpecification conceptSpecification);
    
    static DisjointWith DisjointWith(ConceptChronology<?> conceptChronology, LogicalExpressionBuilder builder) {
        return builder.disjointWith(conceptChronology);
    }


    static DisjointWith DisjointWith(ConceptSpecification conceptSpecification, LogicalExpressionBuilder builder) {
        return builder.disjointWith(conceptSpecification);
    }

    And and(Assertion... assertions);

    static And And(Assertion... assertions) {
        return assertions[0].getBuilder().and(assertions);
    }

    ConceptAssertion conceptAssertion(ConceptChronology<?> conceptChronology);
    ConceptAssertion conceptAssertion(ConceptSpecification conceptSpecification);
    ConceptAssertion conceptAssertion(Integer conceptId);

    static ConceptAssertion ConceptAssertion(ConceptChronology<?> conceptChronology, LogicalExpressionBuilder builder) {
        return builder.conceptAssertion(conceptChronology);
    }

    static ConceptAssertion ConceptAssertion(ConceptSpecification conceptSpecification, LogicalExpressionBuilder builder) {
        return builder.conceptAssertion(conceptSpecification);
    }

    AllRole allRole(ConceptChronology<?> roleTypeChronology, Assertion roleRestriction);
    
    static AllRole AllRole(ConceptChronology<?> roleTypeChronology, Assertion roleRestriction) {
        return roleRestriction.getBuilder().allRole(roleTypeChronology, roleRestriction);
    }

    AllRole allRole(ConceptSpecification roleTypeSpecification, Assertion roleRestriction);
    
    static AllRole AllRole(ConceptSpecification roleTypeSpecification, Assertion roleRestriction) {
        return roleRestriction.getBuilder().allRole(roleTypeSpecification, roleRestriction);
    }

    Feature feature(ConceptChronology<?> featureTypeChronology, LiteralAssertion literal);
    
    static Feature Feature(ConceptChronology<?> featureTypeChronology, LiteralAssertion literal) {
        return literal.getBuilder().feature(featureTypeChronology, literal);
    }

    Feature feature(ConceptSpecification featureTypeSpecification, LiteralAssertion literal);
    
    static Feature Feature(ConceptSpecification featureTypeSpecification, LiteralAssertion literal) {
        return literal.getBuilder().feature(featureTypeSpecification, literal);
    }

    SomeRole someRole(ConceptChronology<?> roleTypeChronology, Assertion roleRestriction);
    
    static SomeRole SomeRole(ConceptChronology<?> roleTypeChronology, Assertion roleRestriction) {
        return roleRestriction.getBuilder().someRole(roleTypeChronology, roleRestriction);
    }

    SomeRole someRole(ConceptSpecification roleTypeSpecification, Assertion roleRestriction);
    
    static SomeRole SomeRole(ConceptSpecification roleTypeSpecification, Assertion roleRestriction) {
        return roleRestriction.getBuilder().someRole(roleTypeSpecification, roleRestriction);
    }

    Template template(ConceptChronology<?> templateChronology, ConceptChronology<?> assemblageToPopulateTemplateConcept);

    static Template Template(ConceptChronology<?> templateConcept, ConceptChronology<?> assemblageToPopulateTemplateConcept, LogicalExpressionBuilder builder) {
        return builder.template(templateConcept, assemblageToPopulateTemplateConcept);
    }

    Template template(ConceptSpecification templateSpecification, ConceptSpecification assemblageToPopulateTemplateSpecification);

    static Template Template(ConceptSpecification templateSpecification, ConceptSpecification assemblageToPopulateTemplateSpecification, LogicalExpressionBuilder builder) {
        return builder.template(templateSpecification, assemblageToPopulateTemplateSpecification);
    }

    Or or(Assertion... assertions);

    static Or Or(Assertion... assertions) {
        return assertions[0].getBuilder().or(assertions);
    }

    BooleanLiteral booleanLiteral(boolean booleanLiteral);

    static BooleanLiteral BooleanLiteral(boolean literal, 
            LogicalExpressionBuilder builder) {
        return builder.booleanLiteral(literal);
    }

    FloatLiteral floatLiteral(float floatLiteral);

    static FloatLiteral FloatLiteral(float literal, 
            LogicalExpressionBuilder builder) {
        return builder.floatLiteral(literal);
    }

    InstantLiteral instantLiteral(Instant instantLiteral);

    static InstantLiteral InstantLiteral(Instant literal, 
            LogicalExpressionBuilder builder) {
        return builder.instantLiteral(literal);
    }

    IntegerLiteral integerLiteral(int integerLiteral);

    static IntegerLiteral IntegerLiteral(int literal, 
            LogicalExpressionBuilder builder) {
        return builder.integerLiteral(literal);
    }

    StringLiteral stringLiteral(String stringLiteral);
    
    static StringLiteral StringLiteral(String literal, 
            LogicalExpressionBuilder builder) {
        return builder.stringLiteral(literal);
    }
    
    BooleanSubstitution booleanSubstitution(SubstitutionFieldSpecification fieldSpecification);
    
    static BooleanSubstitution BooleanSubstitution(SubstitutionFieldSpecification fieldSpecification, 
            LogicalExpressionBuilder builder) {
        return builder.booleanSubstitution(fieldSpecification);
    }

    ConceptSubstitution conceptSubstitution(SubstitutionFieldSpecification fieldSpecification);
    
    static ConceptSubstitution ConceptSubstitution(SubstitutionFieldSpecification fieldSpecification, 
            LogicalExpressionBuilder builder) {
        return builder.conceptSubstitution(fieldSpecification);
    }

    FloatSubstitution floatSubstitution(SubstitutionFieldSpecification fieldSpecification);
    
    static FloatSubstitution FloatSubstitution(SubstitutionFieldSpecification fieldSpecification, 
            LogicalExpressionBuilder builder) {
        return builder.floatSubstitution(fieldSpecification);
    }

    InstantSubstitution instantSubstitution(SubstitutionFieldSpecification fieldSpecification);
    
    static InstantSubstitution InstantSubstitution(SubstitutionFieldSpecification fieldSpecification, 
            LogicalExpressionBuilder builder) {
        return builder.instantSubstitution(fieldSpecification);
    }

    IntegerSubstitution integerSubstitution(SubstitutionFieldSpecification fieldSpecification);
    
    static IntegerSubstitution IntegerSubstitution(SubstitutionFieldSpecification fieldSpecification, 
            LogicalExpressionBuilder builder) {
        return builder.integerSubstitution(fieldSpecification);
    }

    StringSubstitution stringSubstitution(SubstitutionFieldSpecification fieldSpecification);
    
    static StringSubstitution StringSubstitution(SubstitutionFieldSpecification fieldSpecification, 
            LogicalExpressionBuilder builder) {
        return builder.stringSubstitution(fieldSpecification);
    }

    
}
