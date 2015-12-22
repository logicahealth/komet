/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package gov.vha.isaac.ochre.query.provider;

import gov.vha.isaac.ochre.api.collections.NidSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <code>ParentClause</code> that computes the intersection of the set results
 * from the enclosed <code>Clauses</code>.
 *
 * @author kec
 */
@XmlRootElement(name = "and")
@XmlAccessorType(value = XmlAccessType.NONE)

public class And extends ParentClause {

    public And(Query enclosingQuery, Clause... clauses) {
        super(enclosingQuery, clauses);
    }
    /**
     * Default no arg constructor for Jaxb.
     */
    protected And() {
        super();
    }

    @Override
    public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
        NidSet results = NidSet.of(incomingPossibleComponents.stream());
        getChildren().stream().forEach((clause) -> {
            results.and(clause.computePossibleComponents(incomingPossibleComponents));
        });
        return results;
    }

    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.AND);
        for (Clause clause : getChildren()) {
            whereClause.getChildren().add(clause.getWhereClause());
        }
        return whereClause;
    }

    @Override
    public NidSet computeComponents(NidSet incomingComponents) {
        NidSet results =  NidSet.of(incomingComponents.stream());
        for (Clause clause : getChildren()) {
            results.and(clause.computeComponents(incomingComponents));
        }
        return results;
    }
}
