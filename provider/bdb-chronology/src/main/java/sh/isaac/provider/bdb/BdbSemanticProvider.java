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
package sh.isaac.provider.bdb;

import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.SemanticSequenceSet;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticServiceTyped;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 1)
public class BdbSemanticProvider implements AssemblageService {
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
      LOG.info("Starting semantic provider.");
      bdb = Get.service(BdbProvider.class);
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping semantic provider.");
   }

   @Override
   public <V extends SemanticVersion> SemanticServiceTyped ofType(VersionType versionType) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void writeSemanticChronology(SemanticChronology semanticChronicle) {
      BdbProvider.writeChronologyData(bdb.getSemanticDatabase(), (ChronologyImpl) semanticChronicle);
   }

   @Override
   public Stream<Integer> getAssemblageTypes() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Stream<SemanticChronology> getDescriptionsForComponent(int componentNid) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Optional<? extends SemanticChronology> getOptionalSemanticChronology(int semanticId) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Stream<SemanticChronology> getParallelSemanticChronologyStream() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public SemanticChronology getSemanticChronology(int semanticId) {
      if (semanticId < 0) {
         semanticId = Get.identifierService()
                        .getConceptSequence(semanticId);
      }
      Optional<ByteArrayDataBuffer> optionalByteBuffer = BdbProvider.getChronologyData(bdb.getSemanticDatabase(), semanticId);
      if (optionalByteBuffer.isPresent()) {
         ByteArrayDataBuffer byteBuffer = optionalByteBuffer.get();
         IsaacObjectType.SEMANTIC.readAndValidateHeader(byteBuffer);
         return SemanticChronologyImpl.make(byteBuffer);
      }
      throw new NoSuchElementException("No element for: " + semanticId);
   }

   @Override
   public boolean hasSemantic(int semanticId) {
      return BdbProvider.hasKey(bdb.getSemanticDatabase(), semanticId);
   }

   @Override
   public Stream<SemanticChronology> getSemanticChronologyStream() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public int getSemanticChronologyCount() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public IntStream getSemanticChronologyKeyParallelStream() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public IntStream getSemanticChronologyKeyStream() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public SemanticSequenceSet getSemanticChronologySequencesForComponent(int componentNid) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public SemanticSequenceSet getSemanticChronologySequencesForComponentFromAssemblage(int componentNid, int assemblageConceptSequence) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public SemanticSequenceSet getSemanticChronologySequencesFromAssemblage(int assemblageConceptSequence) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponent(int componentNid) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponentFromAssemblage(int componentNid, int assemblageConceptSequence) {
      //TODO implement for real. 
      return Stream.empty();
   }

   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamFromAssemblage(int assemblageConceptSequence) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public <V extends SemanticVersion> SemanticSnapshotService<V> getSnapshot(Class<V> versionType, StampCoordinate stampCoordinate) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public UUID getDataStoreId() {
     return bdb.getDataStoreId();
   }
   
}
