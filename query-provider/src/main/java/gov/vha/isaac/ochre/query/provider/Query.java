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

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.collections.NidSet;
import gov.vha.isaac.ochre.collections.SememeSequenceSet;
import gov.vha.isaac.ochre.query.provider.clauses.ChangedFromPreviousVersion;
import gov.vha.isaac.ochre.query.provider.clauses.ConceptForComponent;
import gov.vha.isaac.ochre.query.provider.clauses.ConceptIs;
import gov.vha.isaac.ochre.query.provider.clauses.ConceptIsChildOf;
import gov.vha.isaac.ochre.query.provider.clauses.ConceptIsDescendentOf;
import gov.vha.isaac.ochre.query.provider.clauses.ConceptIsKindOf;
import gov.vha.isaac.ochre.query.provider.clauses.DescriptionActiveLuceneMatch;
import gov.vha.isaac.ochre.query.provider.clauses.DescriptionActiveRegexMatch;
import gov.vha.isaac.ochre.query.provider.clauses.DescriptionLuceneMatch;
import gov.vha.isaac.ochre.query.provider.clauses.DescriptionRegexMatch;
import gov.vha.isaac.ochre.query.provider.clauses.FullySpecifiedNameForConcept;
import gov.vha.isaac.ochre.query.provider.clauses.PreferredNameForConcept;
import gov.vha.isaac.ochre.query.provider.clauses.RefsetContainsConcept;
import gov.vha.isaac.ochre.query.provider.clauses.RefsetContainsKindOfConcept;
import gov.vha.isaac.ochre.query.provider.clauses.RefsetContainsString;
import gov.vha.isaac.ochre.query.provider.clauses.RefsetLuceneMatch;
import gov.vha.isaac.ochre.query.provider.clauses.RelRestriction;
import javax.xml.bind.annotation.*;
import java.util.*;

/**
 * Executes queries within the terminology hierarchy and returns the nids of the
 * components that match the criterion of query.
 *
 * @author kec
 */
@XmlRootElement(name = "query")
@XmlAccessorType(value = XmlAccessType.NONE)
@XmlType(factoryClass = QueryFactory.class,
        factoryMethod = "createQuery")

public abstract class Query {

    @XmlElementWrapper(name = "for")
    @XmlElement(name = "component")
    protected List<ComponentCollectionTypes> forCollectionTypes = new ArrayList<>();

    @XmlElementWrapper(name = "custom-for")
    @XmlElement(name = "uuid")
    protected Set<UUID> customCollection = new HashSet<>();

    public static final String currentTaxonomyCoordinateKey = "Current taxonomy coordinate";
    @XmlElementWrapper(name = "let")
    private HashMap<String, Object> letDeclarations;

    @XmlElementWrapper(name = "where")
    @XmlElement(name = "clause")
    protected Clause[] rootClause = new Clause[1];

    @XmlElementWrapper(name = "return")
    @XmlElement(name = "type")
    private final EnumSet<ReturnTypes> returnTypes = EnumSet.of(ReturnTypes.NIDS);

    public void setup() {
        getLetDeclarations();
        rootClause[0] = Where();
        ForSetSpecification forSetSpec = ForSetSpecification();
        forCollectionTypes = forSetSpec.getForCollectionTypes();
        customCollection = forSetSpec.getCustomCollection();
    }

    protected abstract ForSetSpecification ForSetSpecification();

    public HashMap<String, Object> getLetDeclarations() {
        if (letDeclarations == null) {
            letDeclarations = new HashMap<>();
            if (!letDeclarations.containsKey(currentTaxonomyCoordinateKey)) {
                if (taxonomyCoordinate != null) {
                    letDeclarations.put(currentTaxonomyCoordinateKey, taxonomyCoordinate);
                } else {
                    letDeclarations.put(currentTaxonomyCoordinateKey, Get.configurationService().getDefaultTaxonomyCoordinate());
                }
            }

            Let();
        }
        return letDeclarations;
    }
    /**
     * Number of Components output in the returnResultSet method.
     */
    int resultSetLimit = 50;

    /**
     * The concepts, stored as nids in a <code>NidSet</code>, that are
     * considered in the query.
     */
    private NidSet forSet;
    /**
     * The steps required to compute the query clause.
     */
    private final EnumSet<ClauseComputeType> computeTypes
            = EnumSet.noneOf(ClauseComputeType.class);
    /**
     * The <code>TaxonomyCoordinate</code> used in the query.
     */
    private TaxonomyCoordinate taxonomyCoordinate;

    private PremiseType premiseType = PremiseType.INFERRED;

    /**
     * Retrieves what type of iterations are required to compute the clause.
     *
     * @return an <code>EnumSet</code> of the compute types required
     */
    public EnumSet<ClauseComputeType> getComputePhases() {
        return computeTypes;
    }

    /**
     * No argument constructor, which creates a <code>Query</code> with the
     * Snomed inferred latest as the input <code>ViewCoordinate</code>.
     */
    public Query() {
        this(null);
    }

    /**
     * Constructor for <code>Query</code>. If a <code>ViewCoordinate</code> is
     * not specified, the default is the Snomed inferred latest.
     *
     * @param taxonomyCoordinate
     */
    public Query(TaxonomyCoordinate taxonomyCoordinate) {
        this.taxonomyCoordinate = taxonomyCoordinate;
    }

    /**
     * Determines the set that will be searched in the query.
     *
     * @return the <code>NativeIdSetBI</code> of the set that will be queried
     */
    protected final NidSet For() {
        forSet = new NidSet();
        for (ComponentCollectionTypes collection : forCollectionTypes) {
            switch (collection) {
                case ALL_COMPONENTS:
                    forSet.or(NidSet.ofAllComponentNids());
                    break;
                case ALL_CONCEPTS:
                    forSet.or(NidSet.of(ConceptSequenceSet.ofAllConceptSequences()));
                    break;
                case ALL_SEMEMES:
                    forSet.or(NidSet.of(SememeSequenceSet.ofAllSememeSequences()));
                    break;
                case CUSTOM_SET:
                    customCollection.stream().forEach((uuid) -> {
                        forSet.add(Get.identifierService().getNidForUuids(uuid));
                    });
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
        return forSet;
    }

    public abstract void Let();

    /**
     * Retrieves the root clause of the query.
     *
     * @return root <code>Clause</code> in the query
     */
    public abstract Clause Where();

    public void let(String key, Object object) {
        letDeclarations.put(key, object);
    }

    /**
     * Constructs the query and computes the set of concepts that match the
     * criterion specified in the clauses.
     *
     * @return the <code>NativeIdSetBI</code> of nids that meet the criterion of
     * the query
     */
    public NidSet compute() {
        setup();
        forSet = For();
        getLetDeclarations();
        rootClause[0] = Where();
        NidSet possibleComponents
                = rootClause[0].computePossibleComponents(forSet);
        if (computeTypes.contains(ClauseComputeType.ITERATION)) {
            NidSet conceptsToIterateOver = NidSet.of(Get.identifierService().getConceptSequencesForConceptNids(possibleComponents));

            ConceptSequenceSet conceptSequences = Get.identifierService().getConceptSequencesForConceptNids(conceptsToIterateOver);
            Get.conceptService().getParallelConceptChronologyStream(conceptSequences).forEach((concept) -> {
                
                ConceptVersion mutable = concept.createMutableVersion(concept.getNid()); //TODO needs to return a mutable version, not a ConceptVersion

                ConceptChronology cch = (ConceptChronology)concept;
                Optional<LatestVersion<ConceptVersion<?>>> latest = 
                        cch.getLatestVersion(ConceptVersion.class, taxonomyCoordinate.getStampCoordinate());
                //Optional<LatestVersion<ConceptVersion<?>>> latest
                //        = ((ConceptChronology<ConceptVersion<?>>) concept).getLatestVersion(ConceptVersion.class, stampCoordinate);

                if (latest.isPresent()) {
                    rootClause[0].getChildren().stream().forEach((c) -> {
                        c.getQueryMatches(latest.get().value());
                    });
                }

            });
        }
        return rootClause[0].computeComponents(possibleComponents);
    }

    public PremiseType getPremiseType() {
        return premiseType;
    }

    public void setPremiseType(PremiseType premiseType) {
        this.premiseType = premiseType;
    }

    public LogicCoordinate getLogicCoordinate() {
        return taxonomyCoordinate.getLogicCoordinate();
    }

    public void setTaxonomyCoordinate(TaxonomyCoordinate taxonomyCoordinate) {
        this.taxonomyCoordinate = taxonomyCoordinate;
    }

    /**
     *
     * @return the <code>StampCoordinate</code> in the query
     */
    public StampCoordinate getStampCoordinate() {
        return taxonomyCoordinate.getStampCoordinate();
    }

    public LanguageCoordinate getLanguageCoordinate() {
        return taxonomyCoordinate.getLanguageCoordinate();
    }

    public void setResultSetLimit(int limit) {
        this.resultSetLimit = limit;
    }

    /**
     * Creates <code>ConceptIsKindOf</code> clause with default
     * <code>ViewCoordinate</code>.
     *
     * @param conceptSpecKey
     * @return
     */
    protected ConceptIsKindOf ConceptIsKindOf(String conceptSpecKey) {
        return new ConceptIsKindOf(this, conceptSpecKey, currentTaxonomyCoordinateKey);
    }

    /**
     * Creates <code>ConceptIsKindOf</code> clause with input
     * <code>ViewCoordinate</code>.
     *
     * @param conceptSpecKey
     * @param viewCoordinateKey
     * @return
     */
    protected ConceptIsKindOf ConceptIsKindOf(String conceptSpecKey, String viewCoordinateKey) {
        return new ConceptIsKindOf(this, conceptSpecKey, viewCoordinateKey);
    }

    protected DescriptionRegexMatch DescriptionRegexMatch(String regexKey) {
        return new DescriptionRegexMatch(this, regexKey, currentTaxonomyCoordinateKey);
    }

    protected DescriptionRegexMatch DescriptionRegexMatch(String regexKey, String viewCoordinateKey) {
        return new DescriptionRegexMatch(this, regexKey, viewCoordinateKey);
    }

    protected DescriptionActiveRegexMatch DescriptionActiveRegexMatch(String regexKey) {
        return new DescriptionActiveRegexMatch(this, regexKey, currentTaxonomyCoordinateKey);
    }

    protected DescriptionActiveRegexMatch DescriptionActiveRegexMatch(String regexKey, String viewCoordinateKey) {
        return new DescriptionActiveRegexMatch(this, regexKey, viewCoordinateKey);
    }

    /**
     * Creates <code>ConceptForComponent</code> clause with input child clause.
     *
     * @param child
     * @return
     */
    protected ConceptForComponent ConceptForComponent(Clause child) {
        return new ConceptForComponent(this, child);
    }

    protected ConceptIs ConceptIs(String conceptSpecKey) {
        return new ConceptIs(this, conceptSpecKey, currentTaxonomyCoordinateKey);
    }

    /**
     * Creates <code>ConceptIs</code> clause with input
     * <code>ViewCoordinate</code>.
     *
     * @param conceptSpecKey
     * @param viewCoordinateKey
     * @return
     */
    protected ConceptIs ConceptIs(String conceptSpecKey, String viewCoordinateKey) {
        return new ConceptIs(this, conceptSpecKey, viewCoordinateKey);
    }

    protected ConceptIsDescendentOf ConceptIsDescendentOf(String conceptSpecKey) {
        return new ConceptIsDescendentOf(this, conceptSpecKey, currentTaxonomyCoordinateKey);
    }

    /**
     * Creates <code>ConceptIsDescendentOf</code> clause with input
     * <code>ViewCoordinate</code>.
     *
     * @param conceptSpecKey
     * @param viewCoordinateKey
     * @return
     */
    protected ConceptIsDescendentOf ConceptIsDescendentOf(String conceptSpecKey, String viewCoordinateKey) {
        return new ConceptIsDescendentOf(this, conceptSpecKey, viewCoordinateKey);
    }

    protected ConceptIsChildOf ConceptIsChildOf(String conceptSpecKey) {
        return new ConceptIsChildOf(this, conceptSpecKey, currentTaxonomyCoordinateKey);
    }

    /**
     * Creates <code>ConceptIsChildOf</code> clause with input
     * <code>ViewCoordinate</code>.
     *
     * @param conceptSpecKey
     * @param viewCoordinateKey
     * @return
     */
    protected ConceptIsChildOf ConceptIsChildOf(String conceptSpecKey, String viewCoordinateKey) {
        return new ConceptIsChildOf(this, conceptSpecKey, viewCoordinateKey);
    }

    protected DescriptionActiveLuceneMatch DescriptionActiveLuceneMatch(String queryTextKey) {
        return new DescriptionActiveLuceneMatch(this, queryTextKey, currentTaxonomyCoordinateKey);
    }

    protected DescriptionActiveLuceneMatch DescriptionActiveLuceneMatch(String queryTextKey, String viewCoordinateKey) {
        return new DescriptionActiveLuceneMatch(this, queryTextKey, viewCoordinateKey);
    }

    protected DescriptionLuceneMatch DescriptionLuceneMatch(String queryTextKey) {
        return new DescriptionLuceneMatch(this, queryTextKey, currentTaxonomyCoordinateKey);
    }

    protected And And(Clause... clauses) {
        return new And(this, clauses);
    }

    protected RelRestriction RelRestriction(String relTypeKey, String destinationSpecKey) {
        return new RelRestriction(this, relTypeKey, destinationSpecKey, currentTaxonomyCoordinateKey, null, null);
    }

    protected RelRestriction RelRestriction(String relTypeKey, String destinationSpecKey, String key) {
        if (this.letDeclarations.get(key) instanceof Boolean) {
            return new RelRestriction(this, relTypeKey, destinationSpecKey, currentTaxonomyCoordinateKey, key, null);
        } else {
            return new RelRestriction(this, relTypeKey, destinationSpecKey, key, null, null);
        }
    }

    protected RelRestriction RelRestriction(String relTypeKey, String destinatonSpecKey, String relTypeSubsumptionKey, String targetSubsumptionKey) {
        return new RelRestriction(this, relTypeKey, destinatonSpecKey, currentTaxonomyCoordinateKey, relTypeSubsumptionKey, targetSubsumptionKey);

    }

    protected RelRestriction RelRestriction(String relTypeKey, String destinationSpecKey, String viewCoordinateKey, String relTypeSubsumptionKey, String targetSubsumptionKey) {
        return new RelRestriction(this, relTypeKey, destinationSpecKey, viewCoordinateKey, relTypeSubsumptionKey, targetSubsumptionKey);
    }

    protected RefsetContainsConcept RefsetContainsConcept(String refsetSpecKey, String conceptSpecKey) {
        return new RefsetContainsConcept(this, refsetSpecKey, conceptSpecKey, currentTaxonomyCoordinateKey);
    }

    protected RefsetContainsConcept RefsetContainsConcept(String refsetSpecKey, String conceptSpecKey, String viewCoordinateKey) {
        return new RefsetContainsConcept(this, refsetSpecKey, conceptSpecKey, viewCoordinateKey);
    }

    protected RefsetContainsKindOfConcept RefsetContainsKindOfConcept(String refsetSpecKey, String conceptSpecKey) {
        return new RefsetContainsKindOfConcept(this, refsetSpecKey, conceptSpecKey, currentTaxonomyCoordinateKey);
    }

    protected RefsetContainsKindOfConcept RefsetContainsKindOfConcept(String refsetSpecKey, String conceptSpecKey, String viewCoordinateKey) {
        return new RefsetContainsKindOfConcept(this, refsetSpecKey, conceptSpecKey, viewCoordinateKey);
    }

    protected RefsetContainsString RefsetContainsString(String refsetSpecKey, String stringMatchKey) {
        return new RefsetContainsString(this, refsetSpecKey, stringMatchKey, currentTaxonomyCoordinateKey);
    }

    protected RefsetContainsString RefsetContainsString(String refsetSpecKey, String stringMatchKey, String viewCoordinateKey) {
        return new RefsetContainsString(this, refsetSpecKey, stringMatchKey, viewCoordinateKey);
    }

    protected RefsetLuceneMatch RefsetLuceneMatch(String queryString) {
        return new RefsetLuceneMatch(this, queryString, currentTaxonomyCoordinateKey);
    }

    protected PreferredNameForConcept PreferredNameForConcept(Clause clause) {
        return new PreferredNameForConcept(this, clause);
    }

    protected And Intersection(Clause... clauses) {
        return new And(this, clauses);
    }

    protected FullySpecifiedNameForConcept FullySpecifiedNameForConcept(Clause clause) {
        return new FullySpecifiedNameForConcept(this, clause);
    }

    public Not Not(Clause clause) {
        return new Not(this, clause);
    }

    /**
     * Getter for the For set.
     *
     * @return the <code>NativeIdSetBI</code> of the concepts that will be
     * searched in the query
     */
    public NidSet getForSet() {
        return forSet;
    }

    protected Or Or(Clause... clauses) {
        return new Or(this, clauses);
    }

    protected Or Union(Clause... clauses) {
        return new Or(this, clauses);
    }

    protected ChangedFromPreviousVersion ChangedFromPreviousVersion(String previousCoordinateKey) {
        return new ChangedFromPreviousVersion(this, previousCoordinateKey);
    }

    protected Xor Xor(Clause... clauses) {
        return new Xor(this, clauses);
    }

    protected AndNot AndNot(Clause... clauses) {
        return new AndNot(this, clauses);
    }
}
