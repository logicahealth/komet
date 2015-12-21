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

import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;


import javax.xml.bind.annotation.*;

/**
 * Acts as root node for the construction of queries. Sorts results from one of
 * more child clauses, which can be instances of a ParentClause or
 * <code>LeafClause</code>. Every
 * <code>Query</code> must contain a ParentClause as the root node.
 *
 * @author kec
 */
@XmlRootElement(name = "parent")
@XmlAccessorType(value = XmlAccessType.NONE)
public abstract class ParentClause extends Clause {

    /**
     * Array of instances of
     * <code>Clause</code> that are children of the ParentClause in the tree
     * used to compute the constructed
     * <code>Query</code>.
     */
    @XmlElementWrapper(name="child-clauses")
    @XmlElement(name="clause")
    private List<Clause> children = new ArrayList();;

    public List<Clause> getChildren() {
        return children;
    }

    public void setChildren(List<Clause> children) {
        this.children = children;
        for (Clause child : children) {
            child.setParent(this);
        }
    }

    /**
     * Constructor from a Query and child clauses.
     *
     * @param enclosingQuery
     * @param children
     */
    public ParentClause(Query enclosingQuery, Clause... children) {
        super(enclosingQuery);
        setChildren(Arrays.asList(children));

    }
    public ParentClause(Query enclosingQuery, List<Clause> children) {
        super(enclosingQuery);
        setChildren(children);

    }
    /**
     * Default no arg constructor for Jaxb.
     */
    protected ParentClause() {
        super();
    }

    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return PRE_AND_POST_ITERATION;
    }

    @Override
    public final void getQueryMatches(ConceptVersion conceptVersion) {
        children.stream().forEach((c) -> {
            c.getQueryMatches(conceptVersion);
        });
    }
}
