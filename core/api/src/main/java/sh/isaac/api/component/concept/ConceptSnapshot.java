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



package sh.isaac.api.component.concept;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import sh.isaac.api.Get;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.IdentifiedStampedVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;

//~--- interfaces -------------------------------------------------------------

/**
 * An object that identifies a concept, and has a specific {@code StampCoordinate}
 * and {@code LanguageCoordinate} which determine which versions of which components
 * will be returned in response to method calls such as {@code getFullySpecifiedDescription()}.
 * @author kec
 */
public interface ConceptSnapshot
        extends IdentifiedStampedVersion, ConceptSpecification, ManifoldCoordinate {
   /**
    * A test for validating that a concept contains an active description. Used
    * to validate concept proxies or concept specs at runtime.
    * @param descriptionText text to match against.
    * @return true if any active version of a description matches this text.
    */
   boolean containsActiveDescription(String descriptionText);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the chronology.
    *
    * @return the {@code ConceptChronology} that backs this snapshot.
    */
   ConceptChronology getChronology();

   /**
    * Gets the contradictions.
    *
    * @return any contradictions that may exist for the given {@code StampCoordinate}.
    */
   Set<? extends StampedVersion> getContradictions();

   /**
    * This method will try first to return the fully specified description,
    * next the preferred description, finally any description if there is no
    * preferred or fully specified description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    * @return a description for this concept.
    */
   DescriptionVersion getDescription();

   /**
    * Gets the fully specified description.
    *
    * @return The fully specified description for this concept. Optional in case
    * there is no description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    */
   LatestVersion<DescriptionVersion> getFullySpecifiedDescription();
   
   /**
    * Gets the fully specified description text.
    *
    * @return The fully specified description text for this concept. Optional in case
    * there is no description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    */
   default LatestVersion<String> getFullySpecifiedDescriptionText() {
       LatestVersion<DescriptionVersion> latest = getFullySpecifiedDescription();
       if (latest.isPresent()) {
           return LatestVersion.of(latest.get().getText());
       }
       return LatestVersion.empty();
   }

   /**
    * Gets the preferred description.
    *
    * @return The preferred description for this concept. Optional in case
    * there is no description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    */
   LatestVersion<DescriptionVersion> getPreferredDescription();
   
   /**
    * Gets the preferred description text.
    *
    * @return The preferred description text for this concept. Optional in case
    * there is no preferred description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    */
   default LatestVersion<String> getPreferredDescriptionText() {
       LatestVersion<DescriptionVersion> latest = getPreferredDescription();
       if (latest.isPresent()) {
           return LatestVersion.of(latest.get().getText());
       }
       return LatestVersion.empty();
   }

   /**
    * Gets the textual definition for this concept. 
    * @return The textual definition for this concept. Optional in case
    * there is no description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    */
   LatestVersion<DescriptionVersion> getDefinition();

   /**
    * Gets the text of the textual definition for this concept. 
    * @return The text of the textual definition for this concept. Optional in case
    * there is no description that satisfies the {@code StampCoordinate} and the
    * {@code LanguageCoordinate} of this snapshot.
    */
   default LatestVersion<String> getDefinitionText() {
       LatestVersion<DescriptionVersion> latest = getDefinition();
       if (latest.isPresent()) {
           return LatestVersion.of(latest.get().getText());
       }
       return LatestVersion.empty();
   }

   /**
    * Get all descriptions
    * @return a list of all descriptions that are present according to the snapshot specification.
    */
   List<DescriptionVersion> getAllDescriptions(); 
   
   
   /**
    * Get text of all descriptions
    * @return a list of the text of all descriptions that are present according to the snapshot specification.
    */
   default List<String> getTextOfAllDescriptions() {
       List<DescriptionVersion> descriptions = getAllDescriptions();
       ArrayList<String> textList = new ArrayList<>(descriptions.size());
       for (DescriptionVersion version: descriptions) {
           textList.add(version.getText());
       }
       return textList;
   }
   
   /**
    * 
    * @param <V> The type of versions to be returned
    * @param assemblageConceptNid The assemblage from which to retrieve semantic versions
    * @return A list of the latest semantic versions that reference this concept in the identified assemblage. 
    * TODO: consider creation and return of a SemanticSnapshot, rather than a version. 
    */
   default <V extends SemanticVersion> List<LatestVersion<V>> getLatestSemanticVersionsFromAssemblage(int assemblageConceptNid) {
       NidSet semanticNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(getNid(), assemblageConceptNid);
       List<LatestVersion<V>> semanticList = new ArrayList<>(semanticNids.size());
       for (int semanticNid: semanticNids.asArray()) {
           SemanticChronology chronology = Get.assemblageService().getSemanticChronology(semanticNid);
           LatestVersion<V> latestVersion = chronology.getLatestVersion(this);
           if (latestVersion.isPresent()) {
               semanticList.add(latestVersion);
           }
       }
       return semanticList;
   }
   
   /**
    * 
    * @param <V> The type of versions to be returned
    * @param assemblageConceptNid The assemblage from which to retrieve semantic versions
    * @return A (no guarantee of any particular ordering from which the first is chosen) 
    * latest semantic version that reference this concept in the identified assemblage. 
    * TODO: consider creation and return of a SemanticSnapshot, rather than a version. 
    */
   default <V extends SemanticVersion> LatestVersion<V> getFirstSemanticVersionFromAssemblage(int assemblageConceptNid) {
       NidSet semanticNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(getNid(), assemblageConceptNid);
       for (int semanticNid: semanticNids.asArray()) {
           SemanticChronology chronology = Get.assemblageService().getSemanticChronology(semanticNid);
           LatestVersion<V> latestVersion = chronology.getLatestVersion(this);
           if (latestVersion.isPresent()) {
               return latestVersion;
           }
       }
       return LatestVersion.empty();
   }
   
}

