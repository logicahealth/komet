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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Clause that computes the union of the results of the enclosed <code>Clauses</code>.
 *
 * @author dylangrald
 */
@XmlRootElement()
public class Or extends ParentClause {

    public Or(Query enclosingQuery, Clause... clauses) {
        super(enclosingQuery, clauses);
    }
    /**
     * Default no arg constructor for Jaxb.
     */
    protected Or() {
        super();
    }

    @Override
    public NidSet computePossibleComponents(NidSet searchSpace) {
        NidSet results = new NidSet();
        getChildren().stream().forEach((clause) -> {
            results.or(clause.computePossibleComponents(searchSpace));
        });
        return results;
    }

    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.OR);
        getChildren().stream().forEach((clause) -> {
            whereClause.getChildren().add(clause.getWhereClause());
        });
        return whereClause;
    }

    @Override
    public NidSet computeComponents(NidSet incomingComponents) {
        NidSet results = new NidSet();
        getChildren().stream().forEach((clause) -> {
            results.or(clause.computeComponents(incomingComponents));
        });
        return results;
    }
}
