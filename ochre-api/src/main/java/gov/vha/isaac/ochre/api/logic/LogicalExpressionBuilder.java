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

import gov.vha.isaac.ochre.api.ConceptProxy;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.logic.assertions.AllRole;
import gov.vha.isaac.ochre.api.logic.assertions.Assertion;
import gov.vha.isaac.ochre.api.logic.assertions.ConceptAssertion;
import gov.vha.isaac.ochre.api.logic.assertions.Feature;
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

    LogicalExpression build() throws IllegalStateException;

    NecessarySet necessarySet(Connector... connector);

    static NecessarySet NecessarySet(Connector... connectors) {
        return connectors[0].getBuilder().necessarySet(connectors);
    }

    SufficientSet sufficientSet(Connector... connector);

    static SufficientSet SufficientSet(Connector... connectors) {
        return connectors[0].getBuilder().sufficientSet(connectors);
    }

    DisjointWith disjointWith(ConceptProxy conceptProxy);
    
    static DisjointWith DisjointWith(ConceptProxy conceptProxy, 
            LogicalExpressionBuilder builder) {
        return builder.disjointWith(conceptProxy);
    }

    And and(Assertion... assertions);

    static And And(Assertion... assertions) {
        return assertions[0].getBuilder().and(assertions);
    }

    ConceptAssertion conceptAssertion(ConceptProxy conceptProxy);

    static ConceptAssertion ConceptAssertion(ConceptProxy conceptProxy, 
            LogicalExpressionBuilder builder) {
        return builder.conceptAssertion(conceptProxy);
    }

    AllRole allRole(ConceptProxy roleTypeProxy, ConceptAssertion roleRestriction);
    
    static AllRole AllRole(ConceptProxy roleTypeProxy, ConceptAssertion roleRestriction) {
        return roleRestriction.getBuilder().allRole(roleTypeProxy, roleRestriction);
    }

    Feature feature(ConceptProxy featureTypeProxy, LiteralAssertion literal);
    
    static Feature Feature(ConceptProxy featureTypeProxy, LiteralAssertion literal) {
        return literal.getBuilder().feature(featureTypeProxy, literal);
    }

    SomeRole someRole(ConceptProxy roleTypeProxy, ConceptAssertion roleRestriction);
    
    static SomeRole SomeRole(ConceptProxy roleTypeProxy, ConceptAssertion roleRestriction) {
        return roleRestriction.getBuilder().someRole(roleTypeProxy, roleRestriction);
    }

    Template template(ConceptProxy templateConcept, 
            ConceptProxy assemblageToPopulateTemplateConcept);

    static Template Template(ConceptProxy templateConcept, 
            ConceptProxy assemblageToPopulateTemplateConcept, 
            LogicalExpressionBuilder builder) {
        return builder.template(templateConcept, assemblageToPopulateTemplateConcept);
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
