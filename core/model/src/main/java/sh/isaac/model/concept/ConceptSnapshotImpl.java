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
 *
 * @author kec
 */
public class ConceptSnapshotImpl
         implements ConceptSnapshot {
   private final ConceptChronologyImpl             conceptChronology;
   private final StampCoordinate                   stampCoordinate;
   private final LanguageCoordinate                languageCoordinate;
   private final LatestVersion<ConceptVersionImpl> snapshotVersion;

   //~--- constructors --------------------------------------------------------

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

   @Override
   public boolean containsActiveDescription(String descriptionText) {
      return this.conceptChronology.containsDescription(descriptionText, this.stampCoordinate);
   }

   @Override
   public String toUserString() {
      return this.snapshotVersion.toString();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getAuthorSequence() {
      return this.snapshotVersion.value()
                            .getAuthorSequence();
   }

   @Override
   public ConceptChronologyImpl getChronology() {
      return this.conceptChronology;
   }

   @Override
   public CommitStates getCommitState() {
      return this.snapshotVersion.value()
                            .getCommitState();
   }

   @Override
   public String getConceptDescriptionText() {
      return getDescription().getText();
   }

   @Override
   public int getConceptSequence() {
      return this.conceptChronology.getConceptSequence();
   }

   @Override
   public Optional<? extends Set<? extends StampedVersion>> getContradictions() {
      return this.snapshotVersion.contradictions();
   }

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

   @Override
   public Optional<LatestVersion<DescriptionSememe<?>>> getFullySpecifiedDescription() {
      return this.languageCoordinate.getFullySpecifiedDescription(Get.sememeService()
            .getDescriptionsForComponent(getNid())
            .collect(Collectors.toList()),
            this.stampCoordinate);
   }

   @Override
   public LanguageCoordinate getLanguageCoordinate() {
      return this.languageCoordinate;
   }

   @Override
   public int getModuleSequence() {
      return this.snapshotVersion.value()
                            .getModuleSequence();
   }

   @Override
   public int getNid() {
      return this.snapshotVersion.value()
                            .getNid();
   }

   @Override
   public int getPathSequence() {
      return this.snapshotVersion.value()
                            .getPathSequence();
   }

   @Override
   public Optional<LatestVersion<DescriptionSememe<?>>> getPreferredDescription() {
      return this.languageCoordinate.getPreferredDescription(Get.sememeService()
            .getDescriptionsForComponent(getNid())
            .collect(Collectors.toList()),
            this.stampCoordinate);
   }

   @Override
   public UUID getPrimordialUuid() {
      return this.snapshotVersion.value()
                            .getPrimordialUuid();
   }

   @Override
   public StampCoordinate getStampCoordinate() {
      return this.stampCoordinate;
   }

   @Override
   public int getStampSequence() {
      return this.snapshotVersion.value()
                            .getStampSequence();
   }

   @Override
   public State getState() {
      return this.snapshotVersion.value()
                            .getState();
   }

   @Override
   public long getTime() {
      return this.snapshotVersion.value()
                            .getTime();
   }

   @Override
   public List<UUID> getUuidList() {
      return this.snapshotVersion.value()
                            .getUuidList();
   }
}

