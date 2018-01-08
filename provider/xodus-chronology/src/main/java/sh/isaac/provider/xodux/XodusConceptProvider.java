/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.provider.xodux;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.bindings.IntegerBinding;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.StoreConfig;
import jetbrains.exodus.env.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import javafx.beans.value.ObservableObjectValue;
import sh.isaac.api.LookupService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.model.concept.ConceptChronologyImpl;

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = LookupService.SL_L1)

public abstract class XodusConceptProvider 
         implements ConceptService {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();
   private static final String CONCEPT_STORE = "concept-store";

   Environment environment;
   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      LOG.info("Starting Xodus ConceptProvider post-construct");
      environment = Environments.newInstance("myAppData");
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping Xodus ConceptProvider.");
      environment.close();
   }
   
   @Override
   public void writeConcept(final ConceptChronology concept) {
      environment.executeInTransaction((Transaction txn) -> {
         // opening a store without key duplicates and with prefixing
         Store store = environment.openStore(CONCEPT_STORE, StoreConfig.WITHOUT_DUPLICATES_WITH_PREFIXING, txn);
         
         ConceptChronologyImpl conceptImpl = (ConceptChronologyImpl) concept;
         ArrayByteIterable key = IntegerBinding.intToEntry(concept.getNid());
         ByteIterable oldValue = store.get(txn, key);
         if (oldValue != null) {
            int writeSequence = CasSequenceObjectMap.getWriteSequence(oldValue.getBytesUnsafe());
            // TODO: need a real compare and swap operation for Xodus...
            // maybe implement add, and have each version be a key duplicate? 
            
            // Cursors can navigate multiple values for same key
            // Cursor c = store.openCursor(txn);
            
            
            while (writeSequence != conceptImpl.getWriteSequence()) {
               
            } 
            ArrayByteIterable value = new ArrayByteIterable(conceptImpl.getDataToWrite());
            store.put(txn, key, value);
            
         } else {
            ArrayByteIterable value = new ArrayByteIterable(conceptImpl.getDataToWrite());
            store.put(txn, key, value);
         }
         txn.commit();
      });
      
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public ConceptChronology getConceptChronology(int conceptId) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public ConceptChronology getConceptChronology(UUID... conceptUuids) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public ConceptChronology getConceptChronology(ConceptSpecification conceptSpecification) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }


   @Override
   public boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Stream<ConceptChronology> getConceptChronologyStream() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public int getConceptCount() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public IntStream getConceptNidStream() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Optional<UUID> getDataStoreId() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Optional<? extends ConceptChronology> getOptionalConcept(int conceptId) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Optional<? extends ConceptChronology> getOptionalConcept(UUID... conceptUuids) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }



   @Override
   public ConceptSnapshotService getSnapshot(ManifoldCoordinate manifoldCoordinate) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }


   @Override
   public Path getDataStorePath() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public DataStoreStartState getDataStoreStartState() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }
   
}