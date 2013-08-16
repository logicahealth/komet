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
package org.ihtsdo.otf.tcc.api.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The WHERE clause is used to extract only those components that fulfill
 * specified criterion.
 *
 * @author kec
 */
@XmlRootElement(name = "where")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Where {

    public enum ClauseSemantic {

        AND, OR, NOT, XOR,
        CHANGED_FROM_PREVIOUS_VERSION,
        CONCEPT_FOR_COMPONENT,
        CONCEPT_IS_CHILD_OF,
        CONCEPT_IS_DESCENDENT_OF,
        CONCEPT_IS_KIND_OF,
        COMPONENT_IS_MEMBER_OF_REFSET,
        DESCRIPTION_ACTIVE_LUCENE_MATCH,
        DESCRIPTION_ACTIVE_REGEX_MATCH,
        DESCRIPTION_LUCENE_MATCH,
        DESCRIPTION_REGEX_MATCH,
        FULLY_SPECIFIED_NAME_FOR_CONCEPT,
        PREFERRED_NAME_FOR_CONCEPT,
        REFSET_LUCENE_MATCH,
        REL_TYPE,
    }
    private WhereClause rootClause;

    public WhereClause getRootClause() {
        return rootClause;
    }

    public void setRootClause(WhereClause rootClause) {
        this.rootClause = rootClause;
    }

    public Clause getWhereClause(Query q) throws IOException {
        return rootClause.getWhereClause(q);

    }

    @XmlRootElement(name = "clause")
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class WhereClause {

        ClauseSemantic semantic;
        List<String> letKeys = new ArrayList<>();
        List<WhereClause> children = new ArrayList<>();

        public Clause getWhereClause(Query q) throws IOException {
            Clause[] childClauses = new Clause[children.size()];
            for (int i = 0; i < childClauses.length; i++) {
                childClauses[i] = children.get(i).getWhereClause(q);
            }
            switch (semantic) {
                case AND:
                    assert letKeys.isEmpty() : "Let keys should be empty: " + letKeys;
                    return q.And(childClauses);
                case NOT:
                    assert letKeys.isEmpty() : "Let keys should be empty: " + letKeys;
                    assert childClauses.length == 1;
                    return q.Not(childClauses[0]);
                case OR:
                    assert letKeys.isEmpty() : "Let keys should be empty: " + letKeys;
                    return q.Or(childClauses);
                case XOR:
                    assert letKeys.isEmpty() : "Let keys should be empty: " + letKeys;
                    return q.Xor(childClauses);
                case CONCEPT_FOR_COMPONENT:
                    assert letKeys.isEmpty() : "Let keys should be empty: " + letKeys;
                    assert childClauses.length == 1;
                    return q.ConceptForComponent(childClauses[0]);
                case CONCEPT_IS_CHILD_OF:
                    assert childClauses.length == 0 : childClauses;
                    assert letKeys.size() == 1 : "Let keys should have one and only one value: " + letKeys;
                    return q.ConceptIsChildOf(letKeys.get(0));
                case CONCEPT_IS_DESCENDENT_OF:
                    assert childClauses.length == 0 : childClauses;
                    assert letKeys.size() == 1 : "Let keys should have one and only one value: " + letKeys;
                    return q.ConceptIsDescendentOf(letKeys.get(0));
                case CONCEPT_IS_KIND_OF:
                    assert childClauses.length == 0 : childClauses;
                    assert letKeys.size() == 1 : "Let keys should have one and only one value: " + letKeys;
                    return q.ConceptIsKindOf(letKeys.get(0));
                case CHANGED_FROM_PREVIOUS_VERSION:
                    assert childClauses.length == 0 : childClauses;
                    assert letKeys.size() == 1 : "Let keys should have one and only one value: " + letKeys;
                    return q.ChangedFromPreviousVersion(letKeys.get(0));
                case COMPONENT_IS_MEMBER_OF_REFSET:
                    assert childClauses.length == 0 : childClauses;
                    assert letKeys.size() == 1 : "Let keys should have one and only one value: " + letKeys;
                    return q.ConceptIsMemberOfRefset(letKeys.get(0));
                case DESCRIPTION_LUCENE_MATCH:
                    assert childClauses.length == 0 : childClauses;
                    assert letKeys.size() == 1 : "Let keys should have one and only one value: " + letKeys;
                    return q.DescriptionLuceneMatch(letKeys.get(0));
                case DESCRIPTION_ACTIVE_LUCENE_MATCH:
                    assert childClauses.length == 0 : childClauses;
                    assert letKeys.size() == 1 : "Let keys should have one and only one value: " + letKeys;
                    return q.DescriptionActiveLuceneMatch(letKeys.get(0));
                case DESCRIPTION_ACTIVE_REGEX_MATCH:
                    assert childClauses.length == 0 : childClauses;
                    assert letKeys.size() == 1 : "Let keys should have one and only one value: " + letKeys;
                    return q.DescriptionActiveRegexMatch(letKeys.get(0));
                case DESCRIPTION_REGEX_MATCH:
                    assert childClauses.length == 0 : childClauses;
                    assert letKeys.size() == 1 : "Let keys should have one and only one value: " + letKeys;
                    return q.DescriptionRegexMatch(letKeys.get(0));
                case FULLY_SPECIFIED_NAME_FOR_CONCEPT:
                    assert letKeys.isEmpty() : "Let keys should be empty: " + letKeys;
                    assert childClauses.length == 1;
                    return q.PreferredNameForConcept(childClauses[0]);
                case PREFERRED_NAME_FOR_CONCEPT:
                    assert letKeys.isEmpty() : "Let keys should be empty: " + letKeys;
                    assert childClauses.length == 1;
                    return q.PreferredNameForConcept(childClauses[0]);
                case REFSET_LUCENE_MATCH:
                    assert childClauses.length == 0: childClauses;
                    assert letKeys.size() == 1: "Let keys should have one and only one value: " + letKeys;
                    return q.RefsetLuceneMatch(letKeys.get(0));
                case REL_TYPE:
                    assert childClauses.length == 0 : childClauses;
                    assert letKeys.size() == 1 : "Let keys should have one and only one value: " + letKeys;
                    return q.RelType(letKeys.get(0));
                default:
                    throw new UnsupportedOperationException("Can't handle: " + semantic);
            }
        }

        @XmlTransient
        public ClauseSemantic getSemantic() {
            return semantic;
        }

        public void setSemantic(ClauseSemantic semantic) {
            this.semantic = semantic;
        }

        public String getSemanticString() {
            return semantic.name();
        }

        public void setSemanticString(String semanticName) {
            this.semantic = ClauseSemantic.valueOf(semanticName);
        }

        public List<String> getLetKeys() {
            return letKeys;
        }

        public void setLetKeys(List<String> letKeys) {
            this.letKeys = letKeys;
        }

        public List<WhereClause> getChildren() {
            return children;
        }

        public void setChildren(List<WhereClause> children) {
            this.children = children;
        }
    }
}
