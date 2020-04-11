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

import java.util.ArrayList;
import java.util.Collection;
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
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.model.configuration.LanguageCoordinates;
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

      final LatestVersion<ConceptVersion> latestVersion = manifoldCoordinate.getStampFilter()
              .getRelativePositionCalculator().getLatestVersion(conceptChronology);
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
      return this.conceptChronology.containsDescription(descriptionText, this.manifoldCoordinate.getLanguageStampFilter());
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
      return this.getLanguageCoordinate().getAnyName(this.getNid(), this.getLanguageStampFilter());
   }
   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the author nid.
    *
    * @return the author nid
    */
   @Override
   public int getAuthorNid() {
      return this.snapshotVersion.get()
                                 .getAuthorNid();
   }

    @Override
    public int getPathNid() {
        return this.snapshotVersion.get().getPathNid();
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
      return getLanguageCoordinate().getFullyQualifiedName(getNid(), getLanguageStampFilter()).orElse("No FQN description for: " + getNid());
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
    * Gets the fully specified description.
    *
    * @return the fully specified description
    */
   @Override
   public LatestVersion<DescriptionVersion> getFullyQualifiedDescription() {
      return getLanguageCoordinate().getFullyQualifiedDescription(getNid(),
            getLanguageStampFilter());
   }

   /**
    * Gets the module nid.
    *
    * @return the module nid
    */
   @Override
   public int getModuleNid() {
      return this.snapshotVersion.get().getModuleNid();
   }

   /**
    * Gets the nid.
    *
    * @return the nid
    */
   @Override
   public int getNid() {
      return this.snapshotVersion.get().getNid();
   }

   /**
    * Gets the preferred description.
    *
    * @return the preferred description
    */
   @Override
   public LatestVersion<DescriptionVersion> getPreferredDescription() {
      return getLanguageCoordinate().getPreferredDescription(getNid(), getLanguageStampFilter());
   }

   /**
    * Gets the primordial uuid.
    *
    * @return the primordial uuid
    */
   @Override
   public UUID getPrimordialUuid() {
      return this.snapshotVersion.get().getPrimordialUuid();
   }

   /**
    * Gets the stamp sequence.
    *
    * @return the stamp sequence
    */
   @Override
   public int getStampSequence() {
      return this.snapshotVersion.get().getStampSequence();
   }

   /**
    * Gets the state.
    *
    * @return the state
    */
   @Override
   public Status getStatus() {
      return this.snapshotVersion.get().getStatus();
   }

   /**
    * Gets the time.
    *
    * @return the time
    */
   @Override
   public long getTime() {
      return this.snapshotVersion.get().getTime();
   }

   /**
    * Gets the uuid list.
    *
    * @return the uuid list
    */
   @Override
   public List<UUID> getUuidList() {
      return this.snapshotVersion.get().getUuidList();
   }
   

   @Override
   public Optional<String> getRegularName() {
     return getLanguageCoordinate().getPreferredDescriptionText(getNid(), getLanguageStampFilter());
   }

   @Override
   public LanguageCoordinate getLanguageCoordinate() {
      return this.manifoldCoordinate.getLanguageCoordinate();
   }

   @Override
   public LogicCoordinate getLogicCoordinate() {
      return this.manifoldCoordinate.getLogicCoordinate();
   }

    @Override
    public LatestVersion<DescriptionVersion> getDefinition() {
        return this.manifoldCoordinate.getLanguageCoordinate().getDefinitionDescription(this.conceptChronology.getConceptDescriptionList(), getLanguageStampFilter());
    }

    @Override
    public List<DescriptionVersion> getAllDescriptions() {
        List<SemanticChronology> descriptionChronologies = this.conceptChronology.getConceptDescriptionList();
        List<DescriptionVersion> versions = new ArrayList<>();
        for (SemanticChronology descriptionChronology: descriptionChronologies) {
           LatestVersion<DescriptionVersion> latestVersion = descriptionChronology.getLatestVersion(getLanguageStampFilter());
           latestVersion.ifPresent((dv) -> {
               versions.add(dv);
           });
        }
        return versions;
    }    

    @Override
    public VertexSort getVertexSort() {
        return this.manifoldCoordinate.getVertexSort();
    }

    @Override
    public DigraphCoordinate getDigraph() {
        return this.manifoldCoordinate.getDigraph();
    }

    @Override
    public StampFilter getStampFilter() {
        return manifoldCoordinate.getStampFilter();
    }
}

