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



package sh.isaac.model.concept;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ConceptSnapshotImpl.
 *
 * @author kec
 */
public class ConceptSnapshotImpl
         implements ConceptSnapshot {
   /** The concept chronology. */
   private final ConceptChronologyImpl conceptChronology;

   /** The stamp coordinate. */
   private final StampCoordinate stampCoordinate;

   /** The language coordinate. */
   private final LanguageCoordinate languageCoordinate;

   /** The snapshot version. */
   private final LatestVersion<ConceptVersionImpl> snapshotVersion;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new concept snapshot impl.
    *
    * @param conceptChronology the concept chronology
    * @param stampCoordinate the stamp coordinate
    * @param languageCoordinate the language coordinate
    */
   public ConceptSnapshotImpl(ConceptChronologyImpl conceptChronology,
                              StampCoordinate stampCoordinate,
                              LanguageCoordinate languageCoordinate) {
      this.conceptChronology  = conceptChronology;
      this.stampCoordinate    = stampCoordinate;
      this.languageCoordinate = languageCoordinate;

      final Optional<LatestVersion<ConceptVersionImpl>> optionalVersion =
         RelativePositionCalculator.getCalculator(stampCoordinate)
                                   .getLatestVersion(conceptChronology);

      this.snapshotVersion = optionalVersion.get();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Contains active description.
    *
    * @param descriptionText the description text
    * @return true, if successful
    */
   @Override
   public boolean containsActiveDescription(String descriptionText) {
      return this.conceptChronology.containsDescription(descriptionText, this.stampCoordinate);
   }

   /**
    * To user string.
    *
    * @return the string
    */
   @Override
   public String toUserString() {
      return this.snapshotVersion.toString();
   }

   @Override
   public String toString() {
      return this.getDescription().getText();
   }
   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the author sequence.
    *
    * @return the author sequence
    */
   @Override
   public int getAuthorSequence() {
      return this.snapshotVersion.value()
                                 .getAuthorSequence();
   }

   /**
    * Gets the chronology.
    *
    * @return the chronology
    */
   @Override
   public ConceptChronologyImpl getChronology() {
      return this.conceptChronology;
   }

   /**
    * Gets the commit state.
    *
    * @return the commit state
    */
   @Override
   public CommitStates getCommitState() {
      return this.snapshotVersion.value()
                                 .getCommitState();
   }

   /**
    * Gets the concept description text.
    *
    * @return the concept description text
    */
   @Override
   public String getFullySpecifiedConceptDescriptionText() {
      return getDescription().getText();
   }

   /**
    * Gets the concept sequence.
    *
    * @return the concept sequence
    */
   @Override
   public int getConceptSequence() {
      return this.conceptChronology.getConceptSequence();
   }

   /**
    * Gets the contradictions.
    *
    * @return the contradictions
    */
   @Override
   public Optional<? extends Set<? extends StampedVersion>> getContradictions() {
      return this.snapshotVersion.contradictions();
   }

   /**
    * Gets the description.
    *
    * @return the description
    */
   @Override
   public DescriptionSememe<?> getDescription() {
      final Optional<LatestVersion<DescriptionSememe<?>>> fsd = getFullySpecifiedDescription();

      if (fsd.isPresent()) {
         return fsd.get()
                   .value();
      }

      final Optional<LatestVersion<DescriptionSememe<?>>> pd = getPreferredDescription();

      if (pd.isPresent()) {
         return pd.get()
                  .value();
      }

      return Get.sememeService()
                .getDescriptionsForComponent(getNid())
                .findAny()
                .get()
                .getVersionList()
                .get(0);
   }

   /**
    * Gets the fully specified description.
    *
    * @return the fully specified description
    */
   @Override
   public Optional<LatestVersion<DescriptionSememe<?>>> getFullySpecifiedDescription() {
      return this.languageCoordinate.getFullySpecifiedDescription(Get.sememeService()
            .getDescriptionsForComponent(getNid())
            .collect(Collectors.toList()),
            this.stampCoordinate);
   }

   /**
    * Gets the language coordinate.
    *
    * @return the language coordinate
    */
   @Override
   public LanguageCoordinate getLanguageCoordinate() {
      return this.languageCoordinate;
   }

   /**
    * Gets the module sequence.
    *
    * @return the module sequence
    */
   @Override
   public int getModuleSequence() {
      return this.snapshotVersion.value()
                                 .getModuleSequence();
   }

   /**
    * Gets the nid.
    *
    * @return the nid
    */
   @Override
   public int getNid() {
      return this.snapshotVersion.value()
                                 .getNid();
   }

   /**
    * Gets the path sequence.
    *
    * @return the path sequence
    */
   @Override
   public int getPathSequence() {
      return this.snapshotVersion.value()
                                 .getPathSequence();
   }

   /**
    * Gets the preferred description.
    *
    * @return the preferred description
    */
   @Override
   public Optional<LatestVersion<DescriptionSememe<?>>> getPreferredDescription() {
      return this.languageCoordinate.getPreferredDescription(Get.sememeService()
            .getDescriptionsForComponent(getNid())
            .collect(Collectors.toList()),
            this.stampCoordinate);
   }

   /**
    * Gets the primordial uuid.
    *
    * @return the primordial uuid
    */
   @Override
   public UUID getPrimordialUuid() {
      return this.snapshotVersion.value()
                                 .getPrimordialUuid();
   }

   /**
    * Gets the stamp coordinate.
    *
    * @return the stamp coordinate
    */
   @Override
   public StampCoordinate getStampCoordinate() {
      return this.stampCoordinate;
   }

   /**
    * Gets the stamp sequence.
    *
    * @return the stamp sequence
    */
   @Override
   public int getStampSequence() {
      return this.snapshotVersion.value()
                                 .getStampSequence();
   }

   /**
    * Gets the state.
    *
    * @return the state
    */
   @Override
   public State getState() {
      return this.snapshotVersion.value()
                                 .getState();
   }

   /**
    * Gets the time.
    *
    * @return the time
    */
   @Override
   public long getTime() {
      return this.snapshotVersion.value()
                                 .getTime();
   }

   /**
    * Gets the uuid list.
    *
    * @return the uuid list
    */
   @Override
   public List<UUID> getUuidList() {
      return this.snapshotVersion.value()
                                 .getUuidList();
   }
   

   @Override
   public Optional<String> getPreferedConceptDescriptionText() {
     return Optional.ofNullable(Get.defaultCoordinate().getPreferredDescriptionText(this.getConceptSequence()));
   }
   
}

