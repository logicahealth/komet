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



package sh.isaac.provider.bdb.chronology;

//~--- JDK imports ------------------------------------------------------------


import com.sleepycat.je.Database;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;


//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.concept.ConceptSnapshotImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 1)
public class BdbConceptProvider
         implements ConceptService {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();
   private BdbProvider bdb;
   private Database database;

   @Override
   public void clearDatabaseValidityValue() {
      bdb.clearDatabaseValidityValue();
   }

   @Override
   public Path getDatabaseFolder() {
      return bdb.getDatabaseFolder();
   }

   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      return bdb.getDatabaseValidityStatus();
   }
   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      LOG.info("Starting concept provider.");
      bdb = Get.service(BdbProvider.class);
      database = bdb.getConceptDatabase();
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping concept provider.");
   }

   @Override
   public ConceptChronology getConceptChronology(ConceptSpecification conceptSpecification) {
      return getConceptChronology(conceptSpecification.getConceptSequence());
   }

   @Override
   public void writeConcept(ConceptChronology concept) {
      BdbProvider.writeChronologyData(database, (ChronologyImpl) concept);
   }

   @Override
   public ConceptChronologyImpl getConceptChronology(int conceptId) {
      if (conceptId < 0) {
         conceptId = Get.identifierService()
                        .getConceptSequence(conceptId);
      }
      Optional<ByteArrayDataBuffer> optionalByteBuffer = BdbProvider.getChronologyData(database, conceptId);
      if (optionalByteBuffer.isPresent()) {
         ByteArrayDataBuffer byteBuffer = optionalByteBuffer.get();
         IsaacObjectType.CONCEPT.readAndValidateHeader(byteBuffer);
         return ConceptChronologyImpl.make(byteBuffer);
      }
      throw new NoSuchElementException("No element for: " + conceptId);
   }

   @Override
   public ConceptChronology getConceptChronology(UUID... conceptUuids) {
      int conceptSequence = Get.identifierService().getConceptSequenceForUuids(conceptUuids);
      return BdbConceptProvider.this.getConceptChronology(conceptSequence);
   }

   @Override
   public boolean hasConcept(int conceptId) {
     return BdbProvider.hasKey(database, conceptId);
   }

   @Override
   public boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate) {
      return Get.conceptActiveService().isConceptActive(conceptSequence, stampCoordinate);
   }

   @Override
   public Stream<ConceptChronology> getConceptChronologyStream() {
      return StreamSupport.stream(new CursorChronologyStream(database, 
              Get.identifierService().getMaxConceptSequence()), false)
              .map((byteBuffer) -> { 
                 IsaacObjectType.CONCEPT.readAndValidateHeader(byteBuffer);
                 return ConceptChronologyImpl.make(byteBuffer); 
              });
    }

   @Override
   public Stream<ConceptChronology> getConceptChronologyStream(ConceptSequenceSet conceptSequences) {
      return conceptSequences.stream().mapToObj((sequence) -> getConceptChronology(sequence));
   }

   @Override
   public int getConceptCount() {
      return (int) getConceptKeyParallelStream().count(); 
   }

   @Override
   public IntStream getConceptKeyParallelStream() {
      return StreamSupport.intStream(new CursorSequenceStream(database, Get.identifierService().getMaxConceptSequence()),
              true);
   }

   @Override
   public IntStream getConceptKeyStream() {
      return StreamSupport.intStream(new CursorSequenceStream(database, Get.identifierService().getMaxConceptSequence()),
              false);
   }

   @Override
   public UUID getDataStoreId() {
     return bdb.getDataStoreId();
   }

   @Override
   public Optional<? extends ConceptChronology> getOptionalConcept(int conceptId) {
      if (hasConcept(conceptId)) {
         return Optional.of(getConceptChronology(conceptId));
      }
      return Optional.empty();
   }

   @Override
   public Optional<? extends ConceptChronology> getOptionalConcept(UUID... conceptUuids) {
      if (hasConcept(Get.identifierService().getConceptSequenceForUuids(conceptUuids))) {
         return Optional.of(getConceptChronology(conceptUuids));
      }
      return Optional.empty();
   }

   @Override
   public Stream<ConceptChronology> getParallelConceptChronologyStream() {
     return StreamSupport.stream(new CursorChronologyStream(database, 
              Get.identifierService().getMaxConceptSequence()), true)
              .map((byteBuffer) -> { 
                 IsaacObjectType.CONCEPT.readAndValidateHeader(byteBuffer);
                 return ConceptChronologyImpl.make(byteBuffer); 
              });
    }

   @Override
   public Stream<ConceptChronology> getParallelConceptChronologyStream(ConceptSequenceSet conceptSequences) {
      return conceptSequences.stream().mapToObj((sequence) -> getConceptChronology(sequence));
   }

   @Override
   public ConceptSnapshotService getSnapshot(ManifoldCoordinate manifoldCoordinate) {
      return new ConceptSnapshotProvider(manifoldCoordinate);
   }

   /**
    * The Class ConceptSnapshotProvider.
    */
   public class ConceptSnapshotProvider
            implements ConceptSnapshotService {
      /** The manifold coordinate. */
      ManifoldCoordinate manifoldCoordinate;

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new concept snapshot provider.
       *
       * @param manifoldCoordinate
       */
      public ConceptSnapshotProvider(ManifoldCoordinate manifoldCoordinate) {
         this.manifoldCoordinate    = manifoldCoordinate;
      }

      //~--- methods ----------------------------------------------------------

      /**
       * Concept description text.
       *
       * @param conceptId the concept id
       * @return the string
       */
      @Override
      public String conceptDescriptionText(int conceptId) {
         final LatestVersion<DescriptionVersion> descriptionOptional = getDescriptionOptional(conceptId);

         if (descriptionOptional.isPresent()) {
            return descriptionOptional.get()
                                      .getText();
         }

         return "No desc for: " + conceptId;
      }

      /**
       * To string.
       *
       * @return the string
       */
      @Override
      public String toString() {
         return "ConceptSnapshotProvider{" + "manifoldCoordinate=" + this.manifoldCoordinate + '}';
      }

      //~--- get methods ------------------------------------------------------

      /**
       * Checks if concept active.
       *
       * @param conceptSequence the concept sequence
       * @return true, if concept active
       */
      @Override
      public boolean isConceptActive(int conceptSequence) {
         return BdbConceptProvider.this.isConceptActive(conceptSequence, this.manifoldCoordinate);
      }

      /**
       * Gets the concept snapshot.
       *
       * @param conceptSequence the concept sequence
       * @return the concept snapshot
       */
      @Override
      public ConceptSnapshot getConceptSnapshot(int conceptSequence) {
         return new ConceptSnapshotImpl(getConceptChronology(conceptSequence), this.manifoldCoordinate);
      }

      /**
       * Gets the description list.
       *
       * @param conceptId the concept id
       * @return the description list
       */
      private List<SemanticChronology> getDescriptionList(int conceptId) {
         final int conceptNid = Get.identifierService()
                                   .getConceptNid(conceptId);

         return Get.assemblageService()
                   .getDescriptionsForComponent(conceptNid)
                   .collect(Collectors.toList());
      }

      /**
       * Gets the description optional.
       *
       * @param conceptId the concept id
       * @return the description optional
       */
      @Override
      public LatestVersion<DescriptionVersion> getDescriptionOptional(int conceptId) {
         return this.manifoldCoordinate.getDescription(getDescriptionList(conceptId));
      }

      /**
       * Gets the fully specified description.
       *
       * @param conceptId the concept id
       * @return the fully specified description
       */
      @Override
      public LatestVersion<DescriptionVersion> getFullySpecifiedDescription(int conceptId) {
         return this.manifoldCoordinate.getFullySpecifiedDescription(getDescriptionList(conceptId));
      }

      /**
       * Gets the preferred description.
       *
       * @param conceptId the concept id
       * @return the preferred description
       */
      @Override
      public LatestVersion<DescriptionVersion> getPreferredDescription(int conceptId) {
         return this.manifoldCoordinate.getPreferredDescription(getDescriptionList(conceptId));
      }

      /**
       * Gets the stamp coordinate.
       *
       * @return the stamp coordinate
       */
      @Override
      public ManifoldCoordinate getManifoldCoordinate() {
         return this.manifoldCoordinate;
      }

      @Override
      public ConceptSnapshot getConceptSnapshot(ConceptSpecification conceptSpecification) {
         return getConceptSnapshot(conceptSpecification.getConceptSequence());
      }
   }   
}

