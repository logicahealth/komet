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
import java.io.IOException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Computes the exclusive disjunction between the result sets of each
 * <code>ChildClause</code>.
 *
 * @author dylangrald
 */
@XmlRootElement(name = "XOR")
@XmlAccessorType(value = XmlAccessType.PROPERTY)
public class Xor extends ParentClause {

    public Xor(Query enclosingQuery, Clause... clauses) {
        super(enclosingQuery, clauses);
    }

    /**
     * Default no arg constructor for Jaxb.
     */
    protected Xor() {
        super();
    }

    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.XOR);
        for (Clause clause : getChildren()) {
            whereClause.getChildren().add(clause.getWhereClause());
        }
        return whereClause;
    }

    @Override
    public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
        NidSet unionSet = new NidSet();
        getChildren().stream().forEach((c) -> {
            unionSet.or(c.computePossibleComponents(incomingPossibleComponents));
        });
        return unionSet;
    }

    @Override
    public NidSet computeComponents(NidSet incomingComponents) {
        NidSet xorSet = new NidSet();
        getChildren().stream().forEach((c) -> {
            xorSet.xor(c.computeComponents(incomingComponents));
        });
        return xorSet;
    }
}
