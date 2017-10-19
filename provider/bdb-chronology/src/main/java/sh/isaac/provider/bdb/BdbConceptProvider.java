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



package sh.isaac.provider.bdb;

//~--- JDK imports ------------------------------------------------------------


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
import java.util.NoSuchElementException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import sh.isaac.api.Get;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.concept.ConceptChronologyImpl;

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
      BdbProvider.writeChronologyData(bdb.getConceptDatabase(), (ChronologyImpl) concept);
   }

   @Override
   public ConceptChronology getConceptChronology(int conceptId) {
      if (conceptId < 0) {
         conceptId = Get.identifierService()
                        .getConceptSequence(conceptId);
      }
      Optional<ByteArrayDataBuffer> optionalByteBuffer = BdbProvider.getChronologyData(bdb.getConceptDatabase(), conceptId);
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
     return BdbProvider.hasKey(bdb.getConceptDatabase(), conceptId);
   }

   @Override
   public boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate) {
      throw new UnsupportedOperationException(
          "4. Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Stream<ConceptChronology> getConceptChronologyStream() {
      throw new UnsupportedOperationException(
          "5. Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Stream<ConceptChronology> getConceptChronologyStream(ConceptSequenceSet conceptSequences) {
      throw new UnsupportedOperationException(
          "6. Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public int getConceptCount() {
      throw new UnsupportedOperationException(
          "7. Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public IntStream getConceptKeyParallelStream() {
      throw new UnsupportedOperationException(
          "8. Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public IntStream getConceptKeyStream() {
      throw new UnsupportedOperationException(
          "9. Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public UUID getDataStoreId() {
     return bdb.getDataStoreId();
   }

   @Override
   public Optional<? extends ConceptChronology> getOptionalConcept(int conceptId) {
      throw new UnsupportedOperationException(
          "13. Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Optional<? extends ConceptChronology> getOptionalConcept(UUID... conceptUuids) {
      throw new UnsupportedOperationException(
          "14. Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Stream<ConceptChronology> getParallelConceptChronologyStream() {
      throw new UnsupportedOperationException(
          "15. Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Stream<ConceptChronology> getParallelConceptChronologyStream(ConceptSequenceSet conceptSequences) {
      throw new UnsupportedOperationException(
          "16. Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public ConceptSnapshotService getSnapshot(ManifoldCoordinate manifoldCoordinate) {
      throw new UnsupportedOperationException(
          "17. Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }
}

