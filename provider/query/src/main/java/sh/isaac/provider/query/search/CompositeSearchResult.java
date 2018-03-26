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



package sh.isaac.provider.query.search;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.IsaacObjectType;

//~--- classes ----------------------------------------------------------------

/**
 * Encapsulates a data store search result.
 * <p>
 * Logic has been mostly copied from LEGO {@code SnomedSearchResult}.
 *
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class CompositeSearchResult {
   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   /** The containing concept. */
   private ConceptSnapshot containingConcept = null;

   /** The matching components. */
   private final Set<Chronology> matchingComponents = new HashSet<>();

   /** The matching component nid. */
   private int matchingComponentNid;

   /** The best score. */
   private float bestScore;  // best score, rather than score, as multiple matches may go into a SearchResult

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new composite search result.
    *
    * @param matchingComponentNid the matching component nid
    * @param score the score
    */
   public CompositeSearchResult(int matchingComponentNid, float score) {
      this.bestScore = score;

      // matchingComponent may be null, if the match is not on our view path...
      this.matchingComponentNid = matchingComponentNid;
      getContainingConcept(matchingComponentNid);
   }

   /**
    * Instantiates a new composite search result.
    *
    * @param matchingComponent the matching component
    * @param score the score
    */
   public CompositeSearchResult(Chronology matchingComponent, float score) {
      this.matchingComponents.add(matchingComponent);
      this.bestScore = score;

      // matchingComponent may be null, if the match is not on our view path...
      if (matchingComponent == null) {
         throw new RuntimeException("Please call the constructor that takes a nid, if matchingComponent is null...");
      } else {
         this.matchingComponentNid = matchingComponent.getNid();
      }
      getContainingConcept(matchingComponent.getNid());
      
   }

   private void getContainingConcept(int nid) {
      Optional<ConceptSnapshot> containingConceptOptional = locateContainingConcept(nid);
      if (containingConceptOptional.isPresent()) {
         this.containingConcept = containingConceptOptional.get();
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Removes the null results.
    *
    * @param results the results
    * @return the int
    */
   public static int removeNullResults(Collection<CompositeSearchResult> results) {
      final Collection<CompositeSearchResult> nullResults = new ArrayList<>();

      if (results != null) {
         results.stream().filter((result) -> (!result.getContainingConcept().isPresent())).forEachOrdered((result) -> {
            nullResults.add(result);
         });

         results.removeAll(nullResults);
      }

      return nullResults.size();
   }

   /**
    * To long string.
    *
    * @return the string
    */
   public String toLongString() {
      final StringBuilder builder = new StringBuilder();

      builder.append("CompositeSearchResult [containingConcept=");
      builder.append(this.containingConcept);
      builder.append(", matchingComponentNid_=");
      builder.append(this.matchingComponentNid);
      builder.append(", bestScore=");
      builder.append(this.bestScore);
      builder.append(", getMatchingComponents()=");

      final List<String> matchingComponentDescs = new ArrayList<>();

      getMatchingComponents().forEach((matchingComponent) -> {
         matchingComponentDescs.add((matchingComponent != null) ? matchingComponent.toUserString()
                 : null);
      });

      builder.append(matchingComponentDescs);
      builder.append("]");
      return builder.toString();
   }

   /**
    * To short string.
    *
    * @return the string
    */
   public String toShortString() {
      final StringBuilder builder = new StringBuilder();

      builder.append("CompositeSearchResult [containingConcept=");
      builder.append(this.containingConcept);

      if (this.matchingComponentNid != 0) {
         builder.append(", matchingComponentNid_=");
         builder.append(this.matchingComponentNid);
      }

      builder.append(", bestScore=");
      builder.append(this.bestScore);

      if ((this.matchingComponents != null) && (this.matchingComponents.size() > 0)) {
         builder.append(", matchingComponents=");

         final List<Integer> matchingComponentNids = new ArrayList<>();

         this.matchingComponents.forEach((matchingComponent) -> {
            matchingComponentNids.add((matchingComponent != null) ? matchingComponent.getNid()
                    : null);
         });

         builder.append(matchingComponentNids);
      }

      builder.append("]");
      return builder.toString();
   }

   /**
    * To string with descriptions.
    *
    * @return the string
    */
   public String toStringWithDescriptions() {
      final StringBuilder builder = new StringBuilder();

      builder.append("CompositeSearchResult [containingConcept=");

      String containingConceptDesc = null;

      if (this.containingConcept != null) {
         try {
            containingConceptDesc = this.containingConcept.getFullyQualifiedName();
         } catch (final Exception e) {
            containingConceptDesc = "{nid=" + this.containingConcept.getNid() + "}";
         }
      }

      builder.append(containingConceptDesc);
      builder.append(", matchingComponentNid_=");
      builder.append(this.matchingComponentNid);

      String matchingComponentDesc = null;

      if (this.matchingComponentNid != 0) {
         try {
            final Optional<? extends Chronology> cc = Get.identifiedObjectService()
                                                                  .getChronology(
                                                                     this.matchingComponentNid);

            if (cc.isPresent()) {
               matchingComponentDesc = cc.get()
                                         .toUserString();
            }
         } catch (final Exception e) {
            LOG.warn("Unexpected:", e);
         }
      }

      if (matchingComponentDesc != null) {
         builder.append(", matchingComponent=");
         builder.append(matchingComponentDesc);
      }

      builder.append(", bestScore=");
      builder.append(this.bestScore);
      builder.append(", numMatchingComponents=");

      final List<Integer> matchingComponentNids = new ArrayList<>();

      getMatchingComponents().forEach((matchingComponent) -> {
         matchingComponentNids.add((matchingComponent != null) ? matchingComponent.getNid()
                 : null);
      });

      builder.append(matchingComponentNids);
      builder.append("]");
      return builder.toString();
   }

   /**
    * Adjust score.
    *
    * @param newScore the new score
    */
   protected void adjustScore(float newScore) {
      this.bestScore = newScore;
   }

   /**
    * Merge.
    *
    * @param other the other
    */
   protected void merge(CompositeSearchResult other) {
      if (this.containingConcept.getNid() != other.containingConcept.getNid()) {
         throw new RuntimeException("Unmergeable!");
      }

      if (other.bestScore > this.bestScore) {
         this.bestScore = other.bestScore;
      }

      this.matchingComponents.addAll(other.getMatchingComponents());
   }

   /**
    * Locate containing concept.
    *
    * @param componentNid the component nid
    * @return the optional
    */
   private Optional<ConceptSnapshot> locateContainingConcept(int componentNid) {
      final IsaacObjectType type = Get.identifierService().getObjectTypeForComponent(componentNid);

      if (null == type) {
         throw new RuntimeException("oops");
      } else {
         switch (type) {
            case UNKNOWN:
               return Optional.empty();
            case CONCEPT:
               return Optional.ofNullable(Get.defaultConceptSnapshotService().getConceptSnapshot(componentNid));
            case SEMANTIC:
               return locateContainingConcept(Get.assemblageService()
                       .getSemanticChronology(componentNid)
                       .getReferencedComponentNid());
            default:
               throw new RuntimeException("oops");
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the best score.
    *
    * @return the best score
    */
   public float getBestScore() {
      return this.bestScore;
   }

   /**
    * This may return an empty, if the concept and/or matching component was not on the path.
    *
    * @return the containing concept
    */
   public Optional<ConceptSnapshot> getContainingConcept() {
      return Optional.ofNullable(this.containingConcept);
   }

   /**
    * Gets the matching components.
    *
    * @return the matching components
    */
   public Set<Chronology> getMatchingComponents() {
      return this.matchingComponents;
   }

   /**
    * Convenience method to return a filtered list of matchingComponents such that it only returns
    * Description type components.
    *
    * @return the matching description components
    */
   public Set<SemanticChronology> getMatchingDescriptionComponents() {
      final Set<SemanticChronology> setToReturn = new HashSet<>();

      this.matchingComponents.stream().filter((comp) -> ((comp instanceof SemanticChronology) &&
              ((SemanticChronology) comp).getVersionType() == VersionType.DESCRIPTION)).forEachOrdered((comp) -> {
                 setToReturn.add(((SemanticChronology) comp));
      });

      return Collections.unmodifiableSet(setToReturn);
   }

   /**
    * A convenience method to get string values from the matching Components.
    *
    * @param stampCoord the stamp coord
    * @return the matching strings
    */
   public List<String> getMatchingStrings(Optional<StampCoordinate> stampCoord) {
      final ArrayList<String> strings = new ArrayList<>();

      if (this.matchingComponents.isEmpty()) {
         if (this.containingConcept == null) {
            strings.add("Match to NID (not on path):" + this.matchingComponentNid);
         } else {
            throw new RuntimeException("Unexpected");
         }
      }

      this.matchingComponents.forEach((iol) -> {
         if (iol instanceof ConceptChronology) {
            // This means they matched on a UUID or other ID lookup.
            // Return UUID for now - matches on other ID types will be handled differently
            // in the near future - so ignore the SCTID case for now.
            strings.add(iol.getPrimordialUuid()
                    .toString());
         } else if ((iol instanceof SemanticChronology) &&
                 ((SemanticChronology) iol).getVersionType() == VersionType.DESCRIPTION) {
            final LatestVersion<DescriptionVersion> ds =
                    ((SemanticChronology) iol).getLatestVersion(stampCoord.orElse(Get.configurationService()
                            .getDefaultStampCoordinate()));
            
            if (ds.isPresent()) {
               strings.add(ds.get()
                       .getText());
            } else {
               strings.add("No description available on stamp coordinate!");
            }
         } else if ((iol instanceof SemanticChronology) &&
                 ((SemanticChronology) iol).getVersionType() == VersionType.STRING) {
            final LatestVersion<StringVersion> ds =
                    ((SemanticChronology) iol).getLatestVersion(stampCoord.orElse(Get.configurationService()
                            .getDefaultStampCoordinate()));
            
            if (ds.isPresent()) {
               strings.add(ds.get()
                       .getString());
            } else {
               strings.add("No semantic available on stamp coordinate!");
            }
         } else if ((iol instanceof SemanticChronology) &&
                 ((SemanticChronology) iol).getVersionType() == VersionType.DYNAMIC) {
            final LatestVersion<DynamicVersion> ds =
                    ((SemanticChronology) iol).getLatestVersion(stampCoord.orElse(Get.configurationService()
                            .getDefaultStampCoordinate()));
            
            if (ds.isPresent()) {
               strings.add(ds.get()
                       .dataToString());
            } else {
               strings.add("No semantic available on stamp coordinate!");
            }
         } else {
            strings.add("ERROR: No string extractor available for " + iol.getClass().getName());
         }
      });

      return strings;
   }
}

