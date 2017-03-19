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



package sh.isaac.provider.query;

//~--- JDK imports ------------------------------------------------------------

import java.util.*;

import javax.xml.bind.annotation.*;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.SememeSequenceSet;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.TaxonomyCoordinate;
import sh.isaac.provider.query.clauses.ChangedFromPreviousVersion;
import sh.isaac.provider.query.clauses.ConceptForComponent;
import sh.isaac.provider.query.clauses.ConceptIs;
import sh.isaac.provider.query.clauses.ConceptIsChildOf;
import sh.isaac.provider.query.clauses.ConceptIsDescendentOf;
import sh.isaac.provider.query.clauses.ConceptIsKindOf;
import sh.isaac.provider.query.clauses.DescriptionActiveLuceneMatch;
import sh.isaac.provider.query.clauses.DescriptionActiveRegexMatch;
import sh.isaac.provider.query.clauses.DescriptionLuceneMatch;
import sh.isaac.provider.query.clauses.DescriptionRegexMatch;
import sh.isaac.provider.query.clauses.FullySpecifiedNameForConcept;
import sh.isaac.provider.query.clauses.PreferredNameForConcept;
import sh.isaac.provider.query.clauses.RefsetContainsConcept;
import sh.isaac.provider.query.clauses.RefsetContainsKindOfConcept;
import sh.isaac.provider.query.clauses.RefsetContainsString;
import sh.isaac.provider.query.clauses.RefsetLuceneMatch;
import sh.isaac.provider.query.clauses.RelRestriction;

//~--- classes ----------------------------------------------------------------

/**
 * Executes queries within the terminology hierarchy and returns the nids of the
 * components that match the criterion of query.
 *
 * @author kec
 */
@XmlRootElement(name = "query")
@XmlAccessorType(value = XmlAccessType.NONE)
@XmlType(
   factoryClass  = QueryFactory.class,
   factoryMethod = "createQuery"
)
public abstract class Query {
   public static final String currentTaxonomyCoordinateKey = "Current taxonomy coordinate";

   //~--- fields --------------------------------------------------------------

   @XmlElementWrapper(name = "for")
   @XmlElement(name = "component")
   protected List<ComponentCollectionTypes> forCollectionTypes = new ArrayList<>();
   @XmlElementWrapper(name = "custom-for")
   @XmlElement(name = "uuid")
   protected Set<UUID>                      customCollection   = new HashSet<>();
   @XmlElementWrapper(name = "where")
   @XmlElement(name = "clause")
   protected Clause[]                       rootClause         = new Clause[1];
   @XmlElementWrapper(name = "return")
   @XmlElement(name = "type")
   private final EnumSet<ReturnTypes>       returnTypes        = EnumSet.of(ReturnTypes.NIDS);

   /**
    * Number of Components output in the returnResultSet method.
    */
   int resultSetLimit = 50;

   /**
    * The steps required to compute the query clause.
    */
   private final EnumSet<ClauseComputeType> computeTypes = EnumSet.noneOf(ClauseComputeType.class);
   private PremiseType                      premiseType  = PremiseType.INFERRED;
   @XmlElementWrapper(name = "let")
   private HashMap<String, Object>          letDeclarations;

   /**
    * The concepts, stored as nids in a <code>NidSet</code>, that are
    * considered in the query.
    */
   private NidSet forSet;

   /**
    * The <code>TaxonomyCoordinate</code> used in the query.
    */
   private TaxonomyCoordinate taxonomyCoordinate;

   //~--- constructors --------------------------------------------------------

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

   //~--- methods -------------------------------------------------------------

   public abstract void Let();

   public Not Not(Clause clause) {
      return new Not(this, clause);
   }

   /**
    * Retrieves the root clause of the query.
    *
    * @return root <code>Clause</code> in the query
    */
   public abstract Clause Where();

   /**
    * Constructs the query and computes the set of concepts that match the
    * criterion specified in the clauses.
    *
    * @return the <code>NativeIdSetBI</code> of nids that meet the criterion of
    * the query
    */
   public NidSet compute() {
      setup();
      this.forSet = For();
      getLetDeclarations();
      this.rootClause[0] = Where();

      final NidSet possibleComponents = this.rootClause[0].computePossibleComponents(this.forSet);

      if (this.computeTypes.contains(ClauseComputeType.ITERATION)) {
         final NidSet conceptsToIterateOver = NidSet.of(Get.identifierService()
                                                     .getConceptSequencesForConceptNids(possibleComponents));
         final ConceptSequenceSet conceptSequences = Get.identifierService()
                                                  .getConceptSequencesForConceptNids(conceptsToIterateOver);

         Get.conceptService()
            .getParallelConceptChronologyStream(conceptSequences)
            .forEach((concept) -> {
                        concept.createMutableVersion(
                               concept.getNid());
                        final ConceptChronology cch = concept;
                        final Optional<LatestVersion<ConceptVersion<?>>> latest =
                           cch.getLatestVersion(ConceptVersion.class, this.taxonomyCoordinate.getStampCoordinate());

                        // Optional<LatestVersion<ConceptVersion<?>>> latest
                        // = ((ConceptChronology<ConceptVersion<?>>) concept).getLatestVersion(ConceptVersion.class, stampCoordinate);

                        if (latest.isPresent()) {
                           this.rootClause[0].getChildren().stream().forEach((c) -> {
                                                    c.getQueryMatches(latest.get()
                                                          .value());
                                                 });
                        }
                     });
      }

      return this.rootClause[0].computeComponents(possibleComponents);
   }

   public void let(String key, Object object) {
      this.letDeclarations.put(key, object);
   }

   public void setup() {
      getLetDeclarations();
      this.rootClause[0] = Where();

      final ForSetSpecification forSetSpec = ForSetSpecification();

      this.forCollectionTypes = forSetSpec.getForCollectionTypes();
      this.customCollection   = forSetSpec.getCustomCollection();
   }

   protected And And(Clause... clauses) {
      return new And(this, clauses);
   }

   protected AndNot AndNot(Clause... clauses) {
      return new AndNot(this, clauses);
   }

   protected ChangedFromPreviousVersion ChangedFromPreviousVersion(String previousCoordinateKey) {
      return new ChangedFromPreviousVersion(this, previousCoordinateKey);
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

   protected DescriptionActiveLuceneMatch DescriptionActiveLuceneMatch(String queryTextKey) {
      return new DescriptionActiveLuceneMatch(this, queryTextKey, currentTaxonomyCoordinateKey);
   }

   protected DescriptionActiveLuceneMatch DescriptionActiveLuceneMatch(String queryTextKey, String viewCoordinateKey) {
      return new DescriptionActiveLuceneMatch(this, queryTextKey, viewCoordinateKey);
   }

   protected DescriptionActiveRegexMatch DescriptionActiveRegexMatch(String regexKey) {
      return new DescriptionActiveRegexMatch(this, regexKey, currentTaxonomyCoordinateKey);
   }

   protected DescriptionActiveRegexMatch DescriptionActiveRegexMatch(String regexKey, String viewCoordinateKey) {
      return new DescriptionActiveRegexMatch(this, regexKey, viewCoordinateKey);
   }

   protected DescriptionLuceneMatch DescriptionLuceneMatch(String queryTextKey) {
      return new DescriptionLuceneMatch(this, queryTextKey, currentTaxonomyCoordinateKey);
   }

   protected DescriptionRegexMatch DescriptionRegexMatch(String regexKey) {
      return new DescriptionRegexMatch(this, regexKey, currentTaxonomyCoordinateKey);
   }

   protected DescriptionRegexMatch DescriptionRegexMatch(String regexKey, String viewCoordinateKey) {
      return new DescriptionRegexMatch(this, regexKey, viewCoordinateKey);
   }

   /**
    * Determines the set that will be searched in the query.
    *
    * @return the <code>NativeIdSetBI</code> of the set that will be queried
    */
   protected final NidSet For() {
      this.forSet = new NidSet();

      for (final ComponentCollectionTypes collection: this.forCollectionTypes) {
         switch (collection) {
         case ALL_COMPONENTS:
            this.forSet.or(NidSet.ofAllComponentNids());
            break;

         case ALL_CONCEPTS:
            this.forSet.or(NidSet.of(ConceptSequenceSet.ofAllConceptSequences()));
            break;

         case ALL_SEMEMES:
            this.forSet.or(NidSet.of(SememeSequenceSet.ofAllSememeSequences()));
            break;

         case CUSTOM_SET:
            this.customCollection.stream().forEach((uuid) -> {
                                        this.forSet.add(Get.identifierService()
                                              .getNidForUuids(uuid));
                                     });
            break;

         default:
            throw new UnsupportedOperationException();
         }
      }

      return this.forSet;
   }

   protected abstract ForSetSpecification ForSetSpecification();

   protected FullySpecifiedNameForConcept FullySpecifiedNameForConcept(Clause clause) {
      return new FullySpecifiedNameForConcept(this, clause);
   }

   protected And Intersection(Clause... clauses) {
      return new And(this, clauses);
   }

   protected Or Or(Clause... clauses) {
      return new Or(this, clauses);
   }

   protected PreferredNameForConcept PreferredNameForConcept(Clause clause) {
      return new PreferredNameForConcept(this, clause);
   }

   protected RefsetContainsConcept RefsetContainsConcept(String refsetSpecKey, String conceptSpecKey) {
      return new RefsetContainsConcept(this, refsetSpecKey, conceptSpecKey, currentTaxonomyCoordinateKey);
   }

   protected RefsetContainsConcept RefsetContainsConcept(String refsetSpecKey,
         String conceptSpecKey,
         String viewCoordinateKey) {
      return new RefsetContainsConcept(this, refsetSpecKey, conceptSpecKey, viewCoordinateKey);
   }

   protected RefsetContainsKindOfConcept RefsetContainsKindOfConcept(String refsetSpecKey, String conceptSpecKey) {
      return new RefsetContainsKindOfConcept(this, refsetSpecKey, conceptSpecKey, currentTaxonomyCoordinateKey);
   }

   protected RefsetContainsKindOfConcept RefsetContainsKindOfConcept(String refsetSpecKey,
         String conceptSpecKey,
         String viewCoordinateKey) {
      return new RefsetContainsKindOfConcept(this, refsetSpecKey, conceptSpecKey, viewCoordinateKey);
   }

   protected RefsetContainsString RefsetContainsString(String refsetSpecKey, String stringMatchKey) {
      return new RefsetContainsString(this, refsetSpecKey, stringMatchKey, currentTaxonomyCoordinateKey);
   }

   protected RefsetContainsString RefsetContainsString(String refsetSpecKey,
         String stringMatchKey,
         String viewCoordinateKey) {
      return new RefsetContainsString(this, refsetSpecKey, stringMatchKey, viewCoordinateKey);
   }

   protected RefsetLuceneMatch RefsetLuceneMatch(String queryString) {
      return new RefsetLuceneMatch(this, queryString, currentTaxonomyCoordinateKey);
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

   protected RelRestriction RelRestriction(String relTypeKey,
         String destinatonSpecKey,
         String relTypeSubsumptionKey,
         String targetSubsumptionKey) {
      return new RelRestriction(this,
                                relTypeKey,
                                destinatonSpecKey,
                                currentTaxonomyCoordinateKey,
                                relTypeSubsumptionKey,
                                targetSubsumptionKey);
   }

   protected RelRestriction RelRestriction(String relTypeKey,
         String destinationSpecKey,
         String viewCoordinateKey,
         String relTypeSubsumptionKey,
         String targetSubsumptionKey) {
      return new RelRestriction(this,
                                relTypeKey,
                                destinationSpecKey,
                                viewCoordinateKey,
                                relTypeSubsumptionKey,
                                targetSubsumptionKey);
   }

   protected Or Union(Clause... clauses) {
      return new Or(this, clauses);
   }

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
    * Getter for the For set.
    *
    * @return the <code>NativeIdSetBI</code> of the concepts that will be
    * searched in the query
    */
   public NidSet getForSet() {
      return this.forSet;
   }

   public LanguageCoordinate getLanguageCoordinate() {
      return this.taxonomyCoordinate.getLanguageCoordinate();
   }

   public HashMap<String, Object> getLetDeclarations() {
      if (this.letDeclarations == null) {
         this.letDeclarations = new HashMap<>();

         if (!this.letDeclarations.containsKey(currentTaxonomyCoordinateKey)) {
            if (this.taxonomyCoordinate != null) {
               this.letDeclarations.put(currentTaxonomyCoordinateKey, this.taxonomyCoordinate);
            } else {
               this.letDeclarations.put(currentTaxonomyCoordinateKey,
                                   Get.configurationService()
                                      .getDefaultTaxonomyCoordinate());
            }
         }

         Let();
      }

      return this.letDeclarations;
   }

   public LogicCoordinate getLogicCoordinate() {
      return this.taxonomyCoordinate.getLogicCoordinate();
   }

   public PremiseType getPremiseType() {
      return this.premiseType;
   }

   //~--- set methods ---------------------------------------------------------

   public void setPremiseType(PremiseType premiseType) {
      this.premiseType = premiseType;
   }

   public void setResultSetLimit(int limit) {
      this.resultSetLimit = limit;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    *
    * @return the <code>StampCoordinate</code> in the query
    */
   public StampCoordinate getStampCoordinate() {
      return this.taxonomyCoordinate.getStampCoordinate();
   }

   //~--- set methods ---------------------------------------------------------

   public void setTaxonomyCoordinate(TaxonomyCoordinate taxonomyCoordinate) {
      this.taxonomyCoordinate = taxonomyCoordinate;
   }
}

