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
package gov.vha.isaac.ochre.model.logic.definition;

import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import gov.vha.isaac.ochre.api.logic.assertions.AllRole;
import gov.vha.isaac.ochre.api.logic.assertions.ConceptAssertion;
import gov.vha.isaac.ochre.api.logic.assertions.Feature;
import gov.vha.isaac.ochre.api.logic.assertions.NecessarySet;
import gov.vha.isaac.ochre.api.logic.assertions.SomeRole;
import gov.vha.isaac.ochre.api.logic.assertions.SufficientSet;
import gov.vha.isaac.ochre.api.logic.assertions.Template;
import gov.vha.isaac.ochre.api.logic.assertions.connectors.And;
import gov.vha.isaac.ochre.api.logic.assertions.connectors.DisjointWith;
import gov.vha.isaac.ochre.api.logic.assertions.connectors.Or;
import gov.vha.isaac.ochre.api.logic.assertions.literal.BooleanLiteral;
import gov.vha.isaac.ochre.api.logic.assertions.literal.FloatLiteral;
import gov.vha.isaac.ochre.api.logic.assertions.literal.InstantLiteral;
import gov.vha.isaac.ochre.api.logic.assertions.literal.IntegerLiteral;
import gov.vha.isaac.ochre.api.logic.assertions.literal.StringLiteral;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.BooleanSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.ConceptSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.FloatSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.InstantSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.IntegerSubstitution;
import gov.vha.isaac.ochre.api.logic.assertions.substitution.StringSubstitution;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;

/**
 *
 * @author kec
 */
public class GenericAxiom implements NecessarySet, SufficientSet, And, DisjointWith,
        ConceptAssertion, AllRole, Feature, SomeRole, Template, Or, BooleanLiteral,
        FloatLiteral, InstantLiteral, IntegerLiteral, StringLiteral, BooleanSubstitution,
        ConceptSubstitution, FloatSubstitution, InstantSubstitution, IntegerSubstitution,
        StringSubstitution {
    private final LogicalExpressionBuilder builder;
    private final short index;
    private final NodeSemantic semantic;

    public GenericAxiom(NodeSemantic semantic, LogicalExpressionBuilderOchreImpl builder) {
        this.builder = builder;
        this.index = builder.getNextAxiomIndex();
        this.semantic = semantic;
    }
    
    @Override
    public LogicalExpressionBuilder getBuilder() {
        return builder;
    }

    public short getIndex() {
        return index;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.index;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GenericAxiom other = (GenericAxiom) obj;
        if (this.index != other.index) {
            return false;
        }
        return this.semantic == other.semantic;
    }
    
    public NodeSemantic getSemantic() {
        return semantic;
    }

    @Override
    public String toString() {
        return "GenericAxiom{" + "index=" + index + ", semantic=" + semantic + '}';
    }

}
