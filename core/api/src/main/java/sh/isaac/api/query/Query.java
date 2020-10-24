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

import javafx.beans.property.ReadOnlyProperty;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.query.clauses.*;

import java.io.Reader;
import java.util.*;

//~--- classes ----------------------------------------------------------------
/**
 * Executes queries within the terminology hierarchy and returns the nids of the
 * components that match the criterion of query.
 *
 * @author kec
 */
public class Query {

    /**
     * The Constant DEFAULT_MANIFOLD_COORDINATE_KEY.
     */
    public static final LetItemKey DEFAULT_MANIFOLD_COORDINATE_KEY 
            = new LetItemKey("Default manifold coordinate key", 
                    UUID.fromString("cd405b9d-3d41-4310-9228-68bd97c5b9b7"));

    //~--- fields --------------------------------------------------------------
    /**
     * The root clause.
     */
    protected Clause rootClause;

    /**
     * Number of Components output in the returnResultSet method.
     */
    int resultSetLimit = 50;

    /**
     * The steps required to compute the query clause.
     */
    private final EnumSet<ClauseComputeType> computeTypes = EnumSet.noneOf(ClauseComputeType.class);

    /**
     * The premise type.
     */
    private PremiseType premiseType = PremiseType.INFERRED;

    /**
     * The let declarations.
     */
    private Map<LetItemKey, Object> letDeclarations = new HashMap<>();

    /**
     * The concepts, stored as nids in a <code>NidSet</code>, that are
     * considered in the query.
     */
    private ForSet forSetSpecification;
    
    List<AttributeSpecification> attributeReturnSpecifications = new ArrayList();

    List<SortSpecification> sortReturnSpecifications = new ArrayList<>();

    //~--- constructors --------------------------------------------------------

    /**
     * For jaxb. 
     */
    public Query() {
        this.forSetSpecification = new ForSet();
    }

    /**
     * Constructor for <code>Query</code>.
     *
     * @param assemblageToIterate
     */
    public Query(ConceptSpecification assemblageToIterate) {
        this(new ForSet(Arrays.asList(new ConceptSpecification[]{assemblageToIterate})));
    }

    public Query(ForSet forSetSpecification) {
        this.forSetSpecification = forSetSpecification;
    }

    //~--- methods -------------------------------------------------------------
    
    /**
     * Erase all intermediate results, caches, and results from the clauses in 
     * preparation for re-execution or other re-use of the query. 
     */
    public void reset() {
        if (this.rootClause != null) {
            this.rootClause.reset();
        }
    }
    /**
     * Override to set let clauses. 
     */
    public void Let() {
        
    }

    /**
     * Not.
     *
     * @param clause the clause
     * @param stampCoordinateKey
     * @return the not
     */
    public Not Not(Clause clause, LetItemKey stampCoordinateKey) {
        return new Not(this, clause, stampCoordinateKey);
    }

    /**
     * Retrieves the root clause of the query.
     *
     * @return root <code>Clause</code> in the query
     */
    public Clause Where() {
        return this.rootClause;
    }

    protected Clause getWhereForJaxb() {
        return this.rootClause;
    }

    protected void setWhereForJaxb(Clause clause) {
        this.rootClause = clause;
    }

    public Clause getRoot() {
        return this.rootClause;
    }

    public void setRoot(Clause root) {
        this.rootClause = root;
        root.setEnclosingQuery(this);
    }

    /**
     * Constructs the query and computes the set of components that match the
     * criterion specified in the clauses.
     *
     * @return the <code>NidSet</code> of nids that meet the criterion of the
     * query
     */
    public Map<ConceptSpecification, NidSet> compute() {
        setup();
        getLetDeclarations();
        validateLet();
        this.rootClause = Where();

        final Map<ConceptSpecification, NidSet> possibleComponentMap = this.rootClause.computePossibleComponents(this.forSetSpecification.getPossibleComponents());

        return this.rootClause.computeComponents(possibleComponentMap);
    }

    private void validateLet() {
        boolean error = false;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<LetItemKey, Object> entry: this.letDeclarations.entrySet()) {
            if (entry.getValue() == null) {
                error = true;
                sb.append(entry.getKey()).append(" has null value\n");
            } else if (entry.getValue() instanceof ConceptSpecification) {
                ConceptSpecification spec = (ConceptSpecification) entry.getValue();
                if (TermAux.UNINITIALIZED_COMPONENT_ID.equals(spec)) {
                    error = true;
                    sb.append(entry.getKey()).append(" has uninitialized value\n");
                }
            }
        }
        if (error) {
            throw new IllegalStateException(sb.toString());
        }
    }
    
    public ForSet getForSetSpecification() {
        return forSetSpecification;
    }
    
    public void setForSetSpecification(ForSet forSetSpecification) {
        this.forSetSpecification = forSetSpecification;
    }
    
    protected List<ConceptSpecification> getForSet() {
        return forSetSpecification.getForSet();
    }
        
    public List<AttributeSpecification> getReturnAttributeList() {
        return attributeReturnSpecifications;
    }
    
    public void setReturnAttributeList(List<AttributeSpecification> attributeReturnSpecifications) {
        this.attributeReturnSpecifications = attributeReturnSpecifications;
    }
    
     
     public List<SortSpecification> getSortAttributeList() {
        return sortReturnSpecifications;
    }
    
    public void setSortAttributeList(List<SortSpecification> sortReturnSpecifications) {
        this.sortReturnSpecifications = sortReturnSpecifications;
    }

    /**
     *
     * @return an array of component nids in an array...
     */
    public int[][] reify() {
            Map<ConceptSpecification, NidSet> assemlageMapResults = compute();
            assemlageMapResults.remove(TermAux.UNINITIALIZED_COMPONENT_ID); // TODO remove cause, not the symptom...
            if (assemlageMapResults.size() == 1) {
                for (Map.Entry<ConceptSpecification, NidSet> entry : assemlageMapResults.entrySet()) {
                    int[][] resultArray = new int[entry.getValue().size()][];
                    int row = 0;
                    for (int nid : entry.getValue().asArray()) {
                        resultArray[row++] = new int[]{nid};
                    }
                    return sort(resultArray);
                }
                throw new IllegalStateException("No entry found, though list is not empty. ");
            } else if (assemlageMapResults.size() == 2 && getRoot() instanceof Join) {                
                Join join = (Join) getRoot();
                return sort(join.getJoinResults());
            } else {
                throw new UnsupportedOperationException("Can't handle complex joins yet" + assemlageMapResults);
            }
    }

    private int[][] sort(int[][] resultArray) {
        if (sortReturnSpecifications.isEmpty()) {
            return resultArray;
        }
        Arrays.sort(resultArray, (int[] o1, int[] o2) -> {
            int comparison = 0;
            for (SortSpecification sortSpecification: sortReturnSpecifications) {
                comparison = sortSpecification.compare(o1, o2, this);
                if (comparison != 0) {
                    return comparison;
                }
            }
            return comparison;            
        });
        return resultArray;
    }

    /**
     * Let.
     *
     * @param key the key
     * @param object the object
     */
    public void let(LetItemKey key, Object object) {
        this.letDeclarations.put(key, object);
    }

    /**
     * Setup.
     */
    public void setup() {
        getLetDeclarations();
        this.rootClause = Where();
    }

    /**
     * And.
     *
     * @param clauses the clauses
     * @return the and
     */
    protected And And(Clause... clauses) {
        return new And(this, clauses);
    }

    /**
     * And not.
     *
     * @param clauses the clauses
     * @return the and not
     */
    protected AndNot AndNot(Clause... clauses) {
        return new AndNot(this, clauses);
    }

    /**
     * Changed from previous version.
     *
     * @param stampVersionOneKey the previous coordinate key
     * @param stampVersionTwoKey
     * @return the changed from previous version
     */
    protected ChangedBetweenVersions ChangedFromPreviousVersion(LetItemKey stampVersionOneKey, LetItemKey stampVersionTwoKey) {
        return new ChangedBetweenVersions(this, stampVersionOneKey, stampVersionTwoKey);
    }

    /**
     * Concept is.
     *
     * @param conceptSpecKey the concept spec key
     * @return the concept is
     */
    protected ConceptIs ConceptIs(LetItemKey conceptSpecKey) {
        return new ConceptIs(this, conceptSpecKey);
    }

    /**
     * Concept is child of.
     *
     * @param conceptSpecKey the concept spec key
     * @return the concept is child of
     */
    protected ConceptIsChildOf ConceptIsChildOf(LetItemKey conceptSpecKey) {
        return new ConceptIsChildOf(this, conceptSpecKey, DEFAULT_MANIFOLD_COORDINATE_KEY);
    }

    /**
     * Creates <code>ConceptIsChildOf</code> clause with input
     * <code>ViewCoordinate</code>.
     *
     * @param conceptSpecKey the concept spec key
     * @param manifoldCoordinateKey the manifold coordinate key
     * @return the concept is child of
     */
    protected ConceptIsChildOf ConceptIsChildOf(LetItemKey conceptSpecKey, LetItemKey manifoldCoordinateKey) {
        return new ConceptIsChildOf(this, conceptSpecKey, manifoldCoordinateKey);
    }

    /**
     * Concept is descendent of.
     *
     * @param conceptSpecKey the concept spec key
     * @return the concept is descendent of
     */
    protected ConceptIsDescendentOf ConceptIsDescendentOf(LetItemKey conceptSpecKey) {
        return new ConceptIsDescendentOf(this, conceptSpecKey, DEFAULT_MANIFOLD_COORDINATE_KEY);
    }

    /**
     * Creates <code>ConceptIsDescendentOf</code> clause with input
     * <code>ViewCoordinate</code>.
     *
     * @param conceptSpecKey the concept spec key
     * @param manifoldCoordinateKey the manifold coordinate key
     * @return the concept is descendent of
     */
    protected ConceptIsDescendentOf ConceptIsDescendentOf(LetItemKey conceptSpecKey, LetItemKey manifoldCoordinateKey) {
        return new ConceptIsDescendentOf(this, conceptSpecKey, manifoldCoordinateKey);
    }

    /**
     * Creates <code>ConceptIsKindOf</code> clause with default
     * <code>ViewCoordinate</code>.
     *
     * @param conceptSpecKey the concept spec key
     * @return the concept is kind of
     */
    protected ConceptIsKindOf ConceptIsKindOf(LetItemKey conceptSpecKey) {
        return new ConceptIsKindOf(this, conceptSpecKey, DEFAULT_MANIFOLD_COORDINATE_KEY);
    }

    /**
     * Creates <code>ConceptIsKindOf</code> clause with input
     * <code>ViewCoordinate</code>.
     *
     * @param conceptSpecKey the concept spec key
     * @param manifoldCoordinateKey the manifold coordinate key
     * @return the concept is kind of
     */
    protected ConceptIsKindOf ConceptIsKindOf(LetItemKey conceptSpecKey, LetItemKey manifoldCoordinateKey) {
        return new ConceptIsKindOf(this, conceptSpecKey, manifoldCoordinateKey);
    }

    /**
     * Description lucene match.
     *
     * @param queryTextKey the query text key
     * @return the description lucene match
     */
    protected DescriptionLuceneMatch DescriptionLuceneMatch(LetItemKey queryTextKey) {
        return new DescriptionLuceneMatch(this, queryTextKey);
    }

    /**
     * Description regex match.
     *
     * @param regexKey the regex key
     * @return the description regex match
     */
    protected DescriptionRegexMatch DescriptionRegexMatch(LetItemKey regexKey) {
        return new DescriptionRegexMatch(this, regexKey);
    }

    /**
     * Determines the set that will be searched in the query.
     *
     * @return the <code>NativeIdSetBI</code> of the set that will be queried
     */
    protected final Map<ConceptSpecification, NidSet> For() {
        return this.forSetSpecification.getCollectionMap();
    }

    /**
     * Intersection.
     *
     * @param clauses the clauses
     * @return the and
     */
    protected And Intersection(Clause... clauses) {
        return new And(this, clauses);
    }

    /**
     * Or.
     *
     * @param clauses the clauses
     * @return the or
     */
    protected Or Or(Clause... clauses) {
        return new Or(this, clauses);
    }


    /**
     * Rel restriction.
     *
     * @param relTypeKey the rel type key
     * @param destinationSpecKey the destination spec key
     * @return the rel restriction
     */
    protected RelRestriction RelRestriction(LetItemKey relTypeKey, LetItemKey destinationSpecKey) {
        return new RelRestriction(this, relTypeKey, destinationSpecKey, DEFAULT_MANIFOLD_COORDINATE_KEY, null, null);
    }

    /**
     * Rel restriction.
     *
     * @param relTypeKey the rel type key
     * @param destinationSpecKey the destination spec key
     * @param key the key
     * @return the rel restriction
     */
    protected RelRestriction RelRestriction(LetItemKey relTypeKey, LetItemKey destinationSpecKey, LetItemKey key) {
        if (this.letDeclarations.get(key) instanceof Boolean) {
            return new RelRestriction(this, relTypeKey, destinationSpecKey, DEFAULT_MANIFOLD_COORDINATE_KEY, key, null);
        } else {
            return new RelRestriction(this, relTypeKey, destinationSpecKey, key, null, null);
        }
    }

    /**
     * Rel restriction.
     *
     * @param relTypeKey the rel type key
     * @param destinatonSpecKey the destinaton spec key
     * @param relTypeSubsumptionKey the rel type subsumption key
     * @param targetSubsumptionKey the target subsumption key
     * @return the rel restriction
     */
    protected RelRestriction RelRestriction(LetItemKey relTypeKey,
            LetItemKey destinatonSpecKey,
            LetItemKey relTypeSubsumptionKey,
            LetItemKey targetSubsumptionKey) {
        return new RelRestriction(this,
                relTypeKey,
                destinatonSpecKey,
                DEFAULT_MANIFOLD_COORDINATE_KEY,
                relTypeSubsumptionKey,
                targetSubsumptionKey);
    }

    /**
     * Rel restriction.
     *
     * @param relTypeKey the rel type key
     * @param destinationSpecKey the destination spec key
     * @param manifoldCoordinateKey the manifold coordinate key
     * @param relTypeSubsumptionKey the rel type subsumption key
     * @param targetSubsumptionKey the target subsumption key
     * @return the rel restriction
     */
    protected RelRestriction RelRestriction(LetItemKey relTypeKey,
            LetItemKey destinationSpecKey,
            LetItemKey manifoldCoordinateKey,
            LetItemKey relTypeSubsumptionKey,
            LetItemKey targetSubsumptionKey) {
        return new RelRestriction(this,
                relTypeKey,
                destinationSpecKey,
                manifoldCoordinateKey,
                relTypeSubsumptionKey,
                targetSubsumptionKey);
    }

    /**
     * Union.
     *
     * @param clauses the clauses
     * @return the or
     */
    protected Or Union(Clause... clauses) {
        return new Or(this, clauses);
    }

    /**
     * Xor.
     *
     * @param clauses the clauses
     * @return the xor
     */
    protected Xor Xor(Clause... clauses) {
        return new Xor(this, clauses);
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Retrieves what type of iterations are required to compute the clause.
     *
     * @return an <code>EnumSet</code> of the compute types required
     */
    public EnumSet<ClauseComputeType> getComputePhases() {
        return this.computeTypes;
    }

    /**
     * Gets the let declarations.
     *
     * @return the let declarations
     */
    public Map<LetItemKey, Object> getLetDeclarations() {
        return this.letDeclarations;
    }
    
    public void setLetDeclarations(Map<LetItemKey, Object> letDeclarations) {
        this.letDeclarations = letDeclarations;
    }

    /**
     * Gets the premise type.
     *
     * @return the premise type
     */
    public PremiseType getPremiseType() {
        return this.premiseType;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the premise type.
     *
     * @param premiseType the new premise type
     */
    public void setPremiseType(PremiseType premiseType) {
        this.premiseType = premiseType;
    }

    /**
     * Set number of Components output in the returnResultSet method.
     *
     * @param limit the new number of Components output in the returnResultSet
     * method
     */
    public void setResultSetLimit(int limit) {
        this.resultSetLimit = limit;
    }

    public List<List<String>> executeQuery() throws NoSuchElementException {
        int[][] resultArray = reify();

        List<List<String>> results = new ArrayList<>();
        List<AttributeSpecification> resultColumns = getReturnAttributeList();
        int columnCount = resultColumns.size();
        
        OpenIntIntHashMap fastAssemblageNidToIndexMap = new OpenIntIntHashMap();
        for (Map.Entry<ConceptSpecification, Integer> entry : getForSetSpecification().getAssembalgeToIndexMap().entrySet()) {
            fastAssemblageNidToIndexMap.put(entry.getKey().getNid(), entry.getValue());
        }
        ObservableSnapshotService[] snapshotArray = new ObservableSnapshotService[columnCount];
        for (int column = 0; column < resultColumns.size(); column++) {
            AttributeSpecification columnSpecification = resultColumns.get(column);
            if (columnSpecification.getStampFilterKey() != null) {
                StampFilter stamp = (StampFilter) getLetDeclarations().get(columnSpecification.getStampFilterKey());
                snapshotArray[column] = Get.observableSnapshotService(stamp);
            }
        }
        
        for (int row = 0; row < resultArray.length; row++) {
            String[] resultRow = new String[columnCount];
            LatestVersion[] latestVersionArray = new LatestVersion[resultArray[row].length];
            List[] propertyListArray = new List[resultArray[row].length];
            for (int column = 0; column < latestVersionArray.length; column++) {
                latestVersionArray[column] = snapshotArray[column].getObservableVersion(resultArray[row][column]);
                if (latestVersionArray[column].isPresent()) {
                    propertyListArray[column] = ((ObservableVersion) latestVersionArray[column].get()).getProperties();
                } else {
                    propertyListArray[column] = null;
                }
            }
            for (int column = 0; column < columnCount; column++) {
                AttributeSpecification columnSpecification = resultColumns.get(column);
                int resultArrayNidIndex = fastAssemblageNidToIndexMap.get(columnSpecification.getAssemblageNid());
                if (latestVersionArray[resultArrayNidIndex].isPresent()) {
                    List<ReadOnlyProperty<?>> propertyList = propertyListArray[resultArrayNidIndex];
                    ReadOnlyProperty<?> property = propertyList.get(columnSpecification.getPropertyIndex());
                    if (columnSpecification.getAttributeFunction() != null) {
                        StampFilter stampFilter = (StampFilter) getLetDeclarations().get(columnSpecification.getStampFilterKey());
                        resultRow[column] = columnSpecification.getAttributeFunction().apply(property.getValue().toString(), stampFilter, this);
                    } else {
                        resultRow[column] = property.getValue().toString();
                    }
                }
            }
            results.add(Arrays.asList(resultRow));
        }
        return results;
    }
}
