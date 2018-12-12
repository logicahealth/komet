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
package sh.isaac.api.query;

//~--- JDK imports ------------------------------------------------------------
import sh.isaac.api.query.properties.AssemblageForIterationClause;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;

//~--- non-JDK imports --------------------------------------------------------
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.query.clauses.*;
import sh.isaac.api.xml.ConceptSpecificationAdaptor;

//~--- classes ----------------------------------------------------------------
/**
 * Statements that are used to retrieve desired components in a
 * <code>Query</code>. Clauses in a Query are organized into a tree and computed
 using a depth-first search. The root node in a Query must be an instance of a
 <code>ParentClause</code>.
 *
 * @author kec
 */
@XmlRootElement(name = "Clause")
@XmlAccessorType(XmlAccessType.NONE)
public abstract class Clause implements ConceptSpecification, AssemblageForIterationClause {
    private static final Logger LOG = LogManager.getLogger();

    /**
     * The Constant PRE_AND_POST_ITERATION.
     */
    protected static final EnumSet<ClauseComputeType> PRE_AND_POST_ITERATION
            = EnumSet.of(ClauseComputeType.PRE_ITERATION,
                    ClauseComputeType.POST_ITERATION);

    /**
     * The Constant PRE_ITERATION.
     */
    protected static final EnumSet<ClauseComputeType> PRE_ITERATION = EnumSet.of(ClauseComputeType.PRE_ITERATION);

    /**
     * The Constant PRE_ITERATION_AND_ITERATION.
     */
    protected static final EnumSet<ClauseComputeType> PRE_ITERATION_AND_ITERATION
            = EnumSet.of(ClauseComputeType.PRE_ITERATION,
                    ClauseComputeType.ITERATION);

    /**
     * The Constant ITERATION.
     */
    protected static final EnumSet<ClauseComputeType> ITERATION = EnumSet.of(ClauseComputeType.ITERATION);

    /**
     * The Constant POST_ITERATION.
     */
    protected static final EnumSet<ClauseComputeType> POST_ITERATION = EnumSet.of(ClauseComputeType.POST_ITERATION);

    //~--- fields --------------------------------------------------------------
    /**
     * The parent clause.
     */
    Clause parent = null;

    ConceptSpecification assemblageForIteration = TermAux.UNINITIALIZED_COMPONENT_ID;

    /**
     * The instance of <code>Query</code> that contains the specifications.
     */
    protected Query enclosingQuery;

    //~--- constructors --------------------------------------------------------
    /**
     * Default no arg constructor for Jaxb.
     */
    protected Clause() {
        super();
    }

    /**
     * Instantiates a new clause.
     *
     * @param enclosingQuery the enclosing query
     */
    public Clause(Query enclosingQuery) {
        if (enclosingQuery == null) {
            throw new NullPointerException("Enclosing query cannot be null. ");
        }
        this.enclosingQuery = enclosingQuery;
        enclosingQuery.getComputePhases().addAll(getComputePhases());
    }
    
    public void reset() {
        resetResults();
        for (Clause child: getChildren()) {
            child.reset();
        }
    }
    
    public abstract void resetResults();

    @XmlElement
    @XmlJavaTypeAdapter(ConceptSpecificationAdaptor.class)    
    @Override
    public ConceptSpecification getAssemblageForIteration() {
        return assemblageForIteration;
    }

    @Override
    public void setAssemblageForIteration(ConceptSpecification assemblageForIteration) {
        if (assemblageForIteration == null) {
            LOG.error("assemblage for iteration attempt to set to null for class: " + 
                    this.getClass().getSimpleName());
            assemblageForIteration = TermAux.UNINITIALIZED_COMPONENT_ID;
        }
        this.assemblageForIteration = assemblageForIteration;
    }

    public abstract ClauseSemantic getClauseSemantic();

    public final void setEnclosingQuery(Query enclosingQuery) {
        this.enclosingQuery = enclosingQuery;
        enclosingQuery.getComputePhases().addAll(getComputePhases());
        for (Clause child : getChildren()) {
            child.setEnclosingQuery(enclosingQuery);
        }
    }

    public final ConceptSpecification getClauseConcept() {
        return getClauseSemantic().getClauseConcept();
    };

    @Override
    public String getFullyQualifiedName() {
        return getClauseConcept().getFullyQualifiedName();
    }

    @Override
    public Optional<String> getRegularName() {
        return getClauseConcept().getRegularName();
    }

    @Override
    public List<UUID> getUuidList() {
        return getClauseConcept().getUuidList();
    }

    @Override
    public int getNid() {
        return Get.identifierService().getNidForUuids(getPrimordialUuid());
    }
    
    public <T extends Object> T getLetItem(LetItemKey key) {
        return (T) enclosingQuery.getLetDeclarations().get(key);
    }

    public void let(LetItemKey key, Object value) {
        enclosingQuery.getLetDeclarations().put(key, value);
    }
    //~--- methods -------------------------------------------------------------
    /**
     * Compute final results based on possible components, and any cached query
     * matches. This third pass was necessary to support the NOT operator.
     *
     * @param incomingComponents the incoming components
     * @return the nid set
     */
    public abstract Map<ConceptSpecification, NidSet> computeComponents(Map<ConceptSpecification, NidSet> incomingComponents);

    /**
     * Compute components that meet the where clause criterion without using
     * iteration. If the set of possibilities cannot be computed without
     * iteration, the set of incomingPossibleComponents will be returned.
     *
     * @param incomingPossibleComponents the incoming possible components
     * @return the nid set
     */
    public abstract Map<ConceptSpecification, NidSet> computePossibleComponents(Map<ConceptSpecification, NidSet> incomingPossibleComponents);

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the children.
     *
     * @return the children
     */
    public abstract List<Clause> getChildren();

    /**
     * Getter for the iteration types required to compute the clause.
     *
     * @return an <code>EnumSet</code> of <code>ClauseComputeType</code>
     * elements for the clause.
     */
    public abstract EnumSet<ClauseComputeType> getComputePhases();

    /**
     * Getter for the <code>Query</code> that contains the specifications used
     * to retrieve components that satisfy criterion.
     *
     * @return the enclosing <code>Query</code>
     */
    public Query getEnclosingQuery() {
        return this.enclosingQuery;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the parent.
     *
     * @param parent the new parent
     */
    public void setParent(Clause parent) {
        this.parent = parent;
        this.enclosingQuery = parent.getEnclosingQuery();
        ParentClause parentClause = (ParentClause) parent;
        parentClause.addChild(this);
    }
   public void removeParent(Clause parent) {
        this.parent = null;
        this.enclosingQuery = null;
        ParentClause parentClause = (ParentClause) parent;
        parentClause.removeChild(this);
    }

    //~--- get methods ---------------------------------------------------------

    /**
     * Getter for the <code>Where</code> clause, which is the syntax of the
     * query.
     *
     * @return the where clause of the query
     */
    public abstract WhereClause getWhereClause();

    public static Clause getRootClause() {
        return new Or();
    }

    /**
     *
     * @return new instances of the all query clauses.
     */
    public static Clause[] getAllClauses() {
        return new Clause[]{new And(), new AndNot(), new Not(), new Or(), new Xor(),
            new Join(),
            new ComponentIsActive(),
            new ComponentIsInactive(),
            new ReferencedComponentIsActive(),
            new ReferencedComponentIsNotActive(),
            new ReferencedComponentIs(),
            new ReferencedComponentIsKindOf(),
            new ReferencedComponentIsNotKindOf(),
            new ReferencedComponentIsMemberOf(),
            new ReferencedComponentIsNotMemberOf()
            
//            new AssemblageContainsConcept(), new AssemblageContainsKindOfConcept(),
//            new AssemblageContainsString(), new AssemblageLuceneMatch(),
//            new ChangedBetweenVersions(), new ConceptForComponent(),
//            new ConceptIs(), new ConceptIsChildOf(), new ConceptIsDescendentOf(),
//            new ConceptIsKindOf(), new DescriptionActiveLuceneMatch(),
//            new DescriptionActiveRegexMatch(), new DescriptionLuceneMatch(),
//            new DescriptionRegexMatch(), new FullyQualifiedNameForConcept(),
//            new PreferredNameForConcept(), new RelRestriction(), new RelationshipIsCircular(), 
        };
    }

    /**
     *
     * @return new instances of allowed clauses that can substitute (in an
     * allowed nesting sense, not in an equivalent semantics sense) for this
     * clause in a query specification.
     */
    public abstract Clause[] getAllowedSubstutitionClauses();

    /**
     *
     * @return new instances of allowed children clauses for this clause.
     */
    public abstract Clause[] getAllowedChildClauses();

    /**
     *
     * @return new instances of allowed sibling clauses for this clause.
     */
    public abstract Clause[] getAllowedSiblingClauses();

    protected static Clause[] getParentClauses() {
        return new Clause[]{new And(), new AndNot(), new Not(), new Or(), new Xor(), new Join()};
    }

}
