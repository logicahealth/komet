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

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.api.component.semantic.version.DescriptionVersion;

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

   /** The manifold coordinate. */
   private final ManifoldCoordinate manifoldCoordinate;

   /** The snapshot version. */
   private final LatestVersion<ConceptVersion> snapshotVersion;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new concept snapshot impl.
    *
    * @param conceptChronology the concept chronology
    * @param manifoldCoordinate
    */
   public ConceptSnapshotImpl(ConceptChronologyImpl conceptChronology,
                              ManifoldCoordinate manifoldCoordinate) {
      this.conceptChronology  = conceptChronology;
      this.manifoldCoordinate    = manifoldCoordinate;

      final LatestVersion<ConceptVersion> latestVersion =
         RelativePositionCalculator.getCalculator(manifoldCoordinate)
                                   .getLatestVersion(conceptChronology);

      this.snapshotVersion = latestVersion;
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
      return this.conceptChronology.containsDescription(descriptionText, this.manifoldCoordinate);
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
   public int getAuthorNid() {
      return this.snapshotVersion.get()
                                 .getAuthorNid();
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
      return this.snapshotVersion.get()
                                 .getCommitState();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getFullyQualifiedName() {
      return getLanguageCoordinate().getFullySpecifiedDescriptionText(getNid(), getStampCoordinate());
   }

   /**
    * Gets the contradictions.
    *
    * @return the contradictions
    */
   @Override
   public Set<? extends StampedVersion> getContradictions() {
      return this.snapshotVersion.contradictions();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DescriptionVersion getDescription() {
      
      LatestVersion<DescriptionVersion> optionalDescription = this.manifoldCoordinate.getDescription(conceptChronology.getConceptDescriptionList());
      if (optionalDescription.isPresent()) {
         return optionalDescription.get();
      }
      
      final LatestVersion<DescriptionVersion> fsd = getFullySpecifiedDescription();

      if (fsd.isPresent()) {
         return fsd.get();
      }

      final LatestVersion<DescriptionVersion> pd = getPreferredDescription();

      if (pd.isPresent()) {
         return pd.get();
      }

      // Last resort if none of above return a proper version. 
      return (DescriptionVersion) Get.assemblageService()
                .getDescriptionsForComponent(getNid()).get(0).getVersionList().get(0);
   }

   /**
    * Gets the fully specified description.
    *
    * @return the fully specified description
    */
   @Override
   public LatestVersion<DescriptionVersion> getFullySpecifiedDescription() {
      return this.manifoldCoordinate.getFullySpecifiedDescription(Get.assemblageService()
            .getDescriptionsForComponent(getNid()),
            this.manifoldCoordinate);
   }

   /**
    * Gets the module sequence.
    *
    * @return the module sequence
    */
   @Override
   public int getModuleNid() {
      return this.snapshotVersion.get()
                                 .getModuleNid();
   }

   /**
    * Gets the nid.
    *
    * @return the nid
    */
   @Override
   public int getNid() {
      return this.snapshotVersion.get()
                                 .getNid();
   }

   /**
    * Gets the path sequence.
    *
    * @return the path sequence
    */
   @Override
   public int getPathNid() {
      return this.snapshotVersion.get()
                                 .getPathNid();
   }

   /**
    * Gets the preferred description.
    *
    * @return the preferred description
    */
   @Override
   public LatestVersion<DescriptionVersion> getPreferredDescription() {
      return this.manifoldCoordinate.getPreferredDescription(Get.assemblageService()
            .getDescriptionsForComponent(getNid()),
            this.manifoldCoordinate);
   }

   /**
    * Gets the primordial uuid.
    *
    * @return the primordial uuid
    */
   @Override
   public UUID getPrimordialUuid() {
      return this.snapshotVersion.get()
                                 .getPrimordialUuid();
   }

   /**
    * Gets the stamp sequence.
    *
    * @return the stamp sequence
    */
   @Override
   public int getStampSequence() {
      return this.snapshotVersion.get()
                                 .getStampSequence();
   }

   /**
    * Gets the state.
    *
    * @return the state
    */
   @Override
   public Status getStatus() {
      return this.snapshotVersion.get()
                                 .getStatus();
   }

   /**
    * Gets the time.
    *
    * @return the time
    */
   @Override
   public long getTime() {
      return this.snapshotVersion.get()
                                 .getTime();
   }

   /**
    * Gets the uuid list.
    *
    * @return the uuid list
    */
   @Override
   public List<UUID> getUuidList() {
      return this.snapshotVersion.get()
                                 .getUuidList();
   }
   

   @Override
   public Optional<String> getRegularName() {
     return getLanguageCoordinate().getRegularName(getNid(), getStampCoordinate());
   }

   @Override
   public ManifoldCoordinate makeCoordinateAnalog(PremiseType taxonomyType) {
      return this.manifoldCoordinate.makeCoordinateAnalog(taxonomyType);
   }

   @Override
   public int getIsaConceptNid() {
      return this.manifoldCoordinate.getIsaConceptNid();
   }

   @Override
   public PremiseType getTaxonomyType() {
      return this.manifoldCoordinate.getTaxonomyType();
   }

   @Override
   public UUID getCoordinateUuid() {
      return this.manifoldCoordinate.getCoordinateUuid();
   }

   @Override
   public StampCoordinate getStampCoordinate() {
      return this.manifoldCoordinate;
   }

   @Override
   public LanguageCoordinate getLanguageCoordinate() {
      return this.manifoldCoordinate;
   }

   @Override
   public LogicCoordinate getLogicCoordinate() {
      return this.manifoldCoordinate;
   }

   @Override
   public void setDescriptionTypePreferenceList(int[] descriptionTypePreferenceList) {
     getLanguageCoordinate().setDescriptionTypePreferenceList(descriptionTypePreferenceList);
   }

   @Override
   public ConceptSnapshotImpl deepClone() {
      throw new UnsupportedOperationException();
   }
   
}

