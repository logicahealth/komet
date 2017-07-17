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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
import sh.isaac.api.query.clauses.ChangedFromPreviousVersion;
import sh.isaac.api.query.clauses.ConceptForComponent;
import sh.isaac.api.query.clauses.ConceptIs;
import sh.isaac.api.query.clauses.ConceptIsChildOf;
import sh.isaac.api.query.clauses.ConceptIsDescendentOf;
import sh.isaac.api.query.clauses.ConceptIsKindOf;
import sh.isaac.api.query.clauses.DescriptionActiveLuceneMatch;
import sh.isaac.api.query.clauses.DescriptionActiveRegexMatch;
import sh.isaac.api.query.clauses.DescriptionLuceneMatch;
import sh.isaac.api.query.clauses.DescriptionRegexMatch;
import sh.isaac.api.query.clauses.FullySpecifiedNameForConcept;
import sh.isaac.api.query.clauses.PreferredNameForConcept;
import sh.isaac.api.query.clauses.AssemblageContainsConcept;
import sh.isaac.api.query.clauses.AssemblageContainsKindOfConcept;
import sh.isaac.api.query.clauses.AssemblageContainsString;
import sh.isaac.api.query.clauses.AssemblageLuceneMatch;
import sh.isaac.api.query.clauses.RelRestriction;
import sh.isaac.api.coordinate.ManifoldCoordinate;

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
   /** The Constant CURRENT_TAXONOMY_RESULT. */
   public static final String CURRENT_TAXONOMY_RESULT = "Current taxonomy coordinate";

   //~--- fields --------------------------------------------------------------

   /** The for collection types. */
   @XmlElementWrapper(name = "for")
   @XmlElement(name = "component")
   protected List<ComponentCollectionTypes> forCollectionTypes = new ArrayList<>();

   /** The custom collection. */
   @XmlElementWrapper(name = "custom-for")
   @XmlElement(name = "uuid")
   protected Set<UUID> customCollection = new HashSet<>();

   /** The root clause. */
   @XmlElementWrapper(name = "where")
   @XmlElement(name = "clause")
   protected Clause[] rootClause = new Clause[1];

   /** The return types. */
   @XmlElementWrapper(name = "return")
   @XmlElement(name = "type")
   private final EnumSet<ReturnTypes> returnTypes = EnumSet.of(ReturnTypes.NIDS);

   /**
    * Number of Components output in the returnResultSet method.
    */
   int resultSetLimit = 50;

   /**
    * The steps required to compute the query clause.
    */
   private final EnumSet<ClauseComputeType> computeTypes = EnumSet.noneOf(ClauseComputeType.class);

   /** The premise type. */
   private PremiseType premiseType = PremiseType.INFERRED;

   /** The let declarations. */
   @XmlElementWrapper(name = "let")
   private HashMap<String, Object> letDeclarations;

   /**
    * The concepts, stored as nids in a <code>NidSet</code>, that are
    * considered in the query.
    */
   private NidSet forSet;

   /**
    * The <code>ManifoldCoordinate</code> used in the query.
    */
   private ManifoldCoordinate manifoldCoordinate;

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
    * @param manifoldCoordinate the taxonomy coordinate
    */
   public Query(ManifoldCoordinate manifoldCoordinate) {
      this.manifoldCoordinate = manifoldCoordinate;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Let.
    */
   public abstract void Let();

   /**
    * Not.
    *
    * @param clause the clause
    * @return the not
    */
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
                        concept.createMutableVersion(concept.getNid());

                        final ConceptChronology cch = concept;
                        final Optional<LatestVersion<ConceptVersion<?>>> latest =
                           cch.getLatestVersion(ConceptVersion.class, this.manifoldCoordinate);

                        // Optional<LatestVersion<ConceptVersion<?>>> latest
                        // = ((ConceptChronology<ConceptVersion<?>>) concept).getLatestVersion(ConceptVersion.class, stampCoordinate);
                        if (latest.isPresent() && latest.get().value().isPresent()) {
                           this.rootClause[0].getChildren().stream().forEach((c) -> {
                        c.getQueryMatches(latest.get()
                                                .value().get());
                     });
                        }
                     });
      }

      return this.rootClause[0].computeComponents(possibleComponents);
   }

   /**
    * Let.
    *
    * @param key the key
    * @param object the object
    */
   public void let(String key, Object object) {
      this.letDeclarations.put(key, object);
   }

   /**
    * Setup.
    */
   public void setup() {
      getLetDeclarations();
      this.rootClause[0] = Where();

      final ForSetSpecification forSetSpec = ForSetSpecification();

      this.forCollectionTypes = forSetSpec.getForCollectionTypes();
      this.customCollection   = forSetSpec.getCustomCollection();
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
    * @param previousCoordinateKey the previous coordinate key
    * @return the changed from previous version
    */
   protected ChangedFromPreviousVersion ChangedFromPreviousVersion(String previousCoordinateKey) {
      return new ChangedFromPreviousVersion(this, previousCoordinateKey);
   }

   /**
    * Creates <code>ConceptForComponent</code> clause with input child clause.
    *
    * @param child the child
    * @return the concept for component
    */
   protected ConceptForComponent ConceptForComponent(Clause child) {
      return new ConceptForComponent(this, child);
   }

   /**
    * Concept is.
    *
    * @param conceptSpecKey the concept spec key
    * @return the concept is
    */
   protected ConceptIs ConceptIs(String conceptSpecKey) {
      return new ConceptIs(this, conceptSpecKey, CURRENT_TAXONOMY_RESULT);
   }

   /**
    * Creates <code>ConceptIs</code> clause with input
    * <code>ViewCoordinate</code>.
    *
    * @param conceptSpecKey the concept spec key
    * @param viewCoordinateKey the view coordinate key
    * @return the concept is
    */
   protected ConceptIs ConceptIs(String conceptSpecKey, String viewCoordinateKey) {
      return new ConceptIs(this, conceptSpecKey, viewCoordinateKey);
   }

   /**
    * Concept is child of.
    *
    * @param conceptSpecKey the concept spec key
    * @return the concept is child of
    */
   protected ConceptIsChildOf ConceptIsChildOf(String conceptSpecKey) {
      return new ConceptIsChildOf(this, conceptSpecKey, CURRENT_TAXONOMY_RESULT);
   }

   /**
    * Creates <code>ConceptIsChildOf</code> clause with input
    * <code>ViewCoordinate</code>.
    *
    * @param conceptSpecKey the concept spec key
    * @param viewCoordinateKey the view coordinate key
    * @return the concept is child of
    */
   protected ConceptIsChildOf ConceptIsChildOf(String conceptSpecKey, String viewCoordinateKey) {
      return new ConceptIsChildOf(this, conceptSpecKey, viewCoordinateKey);
   }

   /**
    * Concept is descendent of.
    *
    * @param conceptSpecKey the concept spec key
    * @return the concept is descendent of
    */
   protected ConceptIsDescendentOf ConceptIsDescendentOf(String conceptSpecKey) {
      return new ConceptIsDescendentOf(this, conceptSpecKey, CURRENT_TAXONOMY_RESULT);
   }

   /**
    * Creates <code>ConceptIsDescendentOf</code> clause with input
    * <code>ViewCoordinate</code>.
    *
    * @param conceptSpecKey the concept spec key
    * @param viewCoordinateKey the view coordinate key
    * @return the concept is descendent of
    */
   protected ConceptIsDescendentOf ConceptIsDescendentOf(String conceptSpecKey, String viewCoordinateKey) {
      return new ConceptIsDescendentOf(this, conceptSpecKey, viewCoordinateKey);
   }

   /**
    * Creates <code>ConceptIsKindOf</code> clause with default
    * <code>ViewCoordinate</code>.
    *
    * @param conceptSpecKey the concept spec key
    * @return the concept is kind of
    */
   protected ConceptIsKindOf ConceptIsKindOf(String conceptSpecKey) {
      return new ConceptIsKindOf(this, conceptSpecKey, CURRENT_TAXONOMY_RESULT);
   }

   /**
    * Creates <code>ConceptIsKindOf</code> clause with input
    * <code>ViewCoordinate</code>.
    *
    * @param conceptSpecKey the concept spec key
    * @param viewCoordinateKey the view coordinate key
    * @return the concept is kind of
    */
   protected ConceptIsKindOf ConceptIsKindOf(String conceptSpecKey, String viewCoordinateKey) {
      return new ConceptIsKindOf(this, conceptSpecKey, viewCoordinateKey);
   }

   /**
    * Description active lucene match.
    *
    * @param queryTextKey the query text key
    * @return the description active lucene match
    */
   protected DescriptionActiveLuceneMatch DescriptionActiveLuceneMatch(String queryTextKey) {
      return new DescriptionActiveLuceneMatch(this, queryTextKey, CURRENT_TAXONOMY_RESULT);
   }

   /**
    * Description active lucene match.
    *
    * @param queryTextKey the query text key
    * @param viewCoordinateKey the view coordinate key
    * @return the description active lucene match
    */
   protected DescriptionActiveLuceneMatch DescriptionActiveLuceneMatch(String queryTextKey, String viewCoordinateKey) {
      return new DescriptionActiveLuceneMatch(this, queryTextKey, viewCoordinateKey);
   }

   /**
    * Description active regex match.
    *
    * @param regexKey the regex key
    * @return the description active regex match
    */
   protected DescriptionActiveRegexMatch DescriptionActiveRegexMatch(String regexKey) {
      return new DescriptionActiveRegexMatch(this, regexKey, CURRENT_TAXONOMY_RESULT);
   }

   /**
    * Description active regex match.
    *
    * @param regexKey the regex key
    * @param viewCoordinateKey the view coordinate key
    * @return the description active regex match
    */
   protected DescriptionActiveRegexMatch DescriptionActiveRegexMatch(String regexKey, String viewCoordinateKey) {
      return new DescriptionActiveRegexMatch(this, regexKey, viewCoordinateKey);
   }

   /**
    * Description lucene match.
    *
    * @param queryTextKey the query text key
    * @return the description lucene match
    */
   protected DescriptionLuceneMatch DescriptionLuceneMatch(String queryTextKey) {
      return new DescriptionLuceneMatch(this, queryTextKey, CURRENT_TAXONOMY_RESULT);
   }

   /**
    * Description regex match.
    *
    * @param regexKey the regex key
    * @return the description regex match
    */
   protected DescriptionRegexMatch DescriptionRegexMatch(String regexKey) {
      return new DescriptionRegexMatch(this, regexKey, CURRENT_TAXONOMY_RESULT);
   }

   /**
    * Description regex match.
    *
    * @param regexKey the regex key
    * @param viewCoordinateKey the view coordinate key
    * @return the description regex match
    */
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

   /**
    * For set specification.
    *
    * @return the for set specification
    */
   protected abstract ForSetSpecification ForSetSpecification();

   /**
    * Fully specified name for concept.
    *
    * @param clause the clause
    * @return the fully specified name for concept
    */
   protected FullySpecifiedNameForConcept FullySpecifiedNameForConcept(Clause clause) {
      return new FullySpecifiedNameForConcept(this, clause);
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
    * Preferred name for concept.
    *
    * @param clause the clause
    * @return the preferred name for concept
    */
   protected PreferredNameForConcept PreferredNameForConcept(Clause clause) {
      return new PreferredNameForConcept(this, clause);
   }

   /**
    * Refset contains concept.
    *
    * @param refsetSpecKey the refset spec key
    * @param conceptSpecKey the concept spec key
    * @return the refset contains concept
    */
   protected AssemblageContainsConcept RefsetContainsConcept(String refsetSpecKey, String conceptSpecKey) {
      return new AssemblageContainsConcept(this, refsetSpecKey, conceptSpecKey, CURRENT_TAXONOMY_RESULT);
   }

   /**
    * Refset contains concept.
    *
    * @param refsetSpecKey the refset spec key
    * @param conceptSpecKey the concept spec key
    * @param viewCoordinateKey the view coordinate key
    * @return the refset contains concept
    */
   protected AssemblageContainsConcept RefsetContainsConcept(String refsetSpecKey,
         String conceptSpecKey,
         String viewCoordinateKey) {
      return new AssemblageContainsConcept(this, refsetSpecKey, conceptSpecKey, viewCoordinateKey);
   }

   /**
    * Refset contains kind of concept.
    *
    * @param refsetSpecKey the refset spec key
    * @param conceptSpecKey the concept spec key
    * @return the refset contains kind of concept
    */
   protected AssemblageContainsKindOfConcept RefsetContainsKindOfConcept(String refsetSpecKey, String conceptSpecKey) {
      return new AssemblageContainsKindOfConcept(this, refsetSpecKey, conceptSpecKey, CURRENT_TAXONOMY_RESULT);
   }

   /**
    * Refset contains kind of concept.
    *
    * @param refsetSpecKey the refset spec key
    * @param conceptSpecKey the concept spec key
    * @param viewCoordinateKey the view coordinate key
    * @return the refset contains kind of concept
    */
   protected AssemblageContainsKindOfConcept RefsetContainsKindOfConcept(String refsetSpecKey,
         String conceptSpecKey,
         String viewCoordinateKey) {
      return new AssemblageContainsKindOfConcept(this, refsetSpecKey, conceptSpecKey, viewCoordinateKey);
   }

   /**
    * Refset contains string.
    *
    * @param refsetSpecKey the refset spec key
    * @param stringMatchKey the string match key
    * @return the refset contains string
    */
   protected AssemblageContainsString RefsetContainsString(String refsetSpecKey, String stringMatchKey) {
      return new AssemblageContainsString(this, refsetSpecKey, stringMatchKey, CURRENT_TAXONOMY_RESULT);
   }

   /**
    * Refset contains string.
    *
    * @param refsetSpecKey the refset spec key
    * @param stringMatchKey the string match key
    * @param viewCoordinateKey the view coordinate key
    * @return the refset contains string
    */
   protected AssemblageContainsString RefsetContainsString(String refsetSpecKey,
         String stringMatchKey,
         String viewCoordinateKey) {
      return new AssemblageContainsString(this, refsetSpecKey, stringMatchKey, viewCoordinateKey);
   }

   /**
    * Refset lucene match.
    *
    * @param queryString the query string
    * @return the refset lucene match
    */
   protected AssemblageLuceneMatch RefsetLuceneMatch(String queryString) {
      return new AssemblageLuceneMatch(this, queryString, CURRENT_TAXONOMY_RESULT);
   }

   /**
    * Rel restriction.
    *
    * @param relTypeKey the rel type key
    * @param destinationSpecKey the destination spec key
    * @return the rel restriction
    */
   protected RelRestriction RelRestriction(String relTypeKey, String destinationSpecKey) {
      return new RelRestriction(this, relTypeKey, destinationSpecKey, CURRENT_TAXONOMY_RESULT, null, null);
   }

   /**
    * Rel restriction.
    *
    * @param relTypeKey the rel type key
    * @param destinationSpecKey the destination spec key
    * @param key the key
    * @return the rel restriction
    */
   protected RelRestriction RelRestriction(String relTypeKey, String destinationSpecKey, String key) {
      if (this.letDeclarations.get(key) instanceof Boolean) {
         return new RelRestriction(this, relTypeKey, destinationSpecKey, CURRENT_TAXONOMY_RESULT, key, null);
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
   protected RelRestriction RelRestriction(String relTypeKey,
         String destinatonSpecKey,
         String relTypeSubsumptionKey,
         String targetSubsumptionKey) {
      return new RelRestriction(this,
                                relTypeKey,
                                destinatonSpecKey,
                                CURRENT_TAXONOMY_RESULT,
                                relTypeSubsumptionKey,
                                targetSubsumptionKey);
   }

   /**
    * Rel restriction.
    *
    * @param relTypeKey the rel type key
    * @param destinationSpecKey the destination spec key
    * @param viewCoordinateKey the view coordinate key
    * @param relTypeSubsumptionKey the rel type subsumption key
    * @param targetSubsumptionKey the target subsumption key
    * @return the rel restriction
    */
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
    * Getter for the For set.
    *
    * @return the <code>NativeIdSetBI</code> of the concepts that will be
    * searched in the query
    */
   public NidSet getForSet() {
      return this.forSet;
   }

   /**
    * Gets the language coordinate.
    *
    * @return the language coordinate
    */
   public LanguageCoordinate getLanguageCoordinate() {
      return this.manifoldCoordinate;
   }

   /**
    * Gets the let declarations.
    *
    * @return the let declarations
    */
   public HashMap<String, Object> getLetDeclarations() {
      if (this.letDeclarations == null) {
         this.letDeclarations = new HashMap<>();

         if (!this.letDeclarations.containsKey(CURRENT_TAXONOMY_RESULT)) {
            if (this.manifoldCoordinate != null) {
               this.letDeclarations.put(CURRENT_TAXONOMY_RESULT, this.manifoldCoordinate);
            } else {
               this.letDeclarations.put(CURRENT_TAXONOMY_RESULT,
                                        Get.configurationService()
                                           .getDefaultManifoldCoordinate());
            }
         }

         Let();
      }

      return this.letDeclarations;
   }

   /**
    * Gets the logic coordinate.
    *
    * @return the logic coordinate
    */
   public LogicCoordinate getLogicCoordinate() {
      return this.manifoldCoordinate.getLogicCoordinate();
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
    * @param limit the new number of Components output in the returnResultSet method
    */
   public void setResultSetLimit(int limit) {
      this.resultSetLimit = limit;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the stamp coordinate.
    *
    * @return the <code>StampCoordinate</code> in the query
    */
   public StampCoordinate getStampCoordinate() {
      return this.manifoldCoordinate.getStampCoordinate();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set <code>ManifoldCoordinate</code> used in the query.
    *
    * @param manifoldCoordinate the new <code>ManifoldCoordinate</code> used in the query
    */
   public void setManifoldCoordinate(ManifoldCoordinate manifoldCoordinate) {
      this.manifoldCoordinate = manifoldCoordinate;
   }
}

