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
package sh.isaac.provider.bdb.chronology;

import com.sleepycat.je.Database;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.SemanticSequenceSet;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.index.AssemblageIndexService;
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
      LOG.info("Starting semantic provider.");
      bdb = Get.service(BdbProvider.class);
      this.database = bdb.getSemanticDatabase();
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping semantic provider.");
   }

   @Override
   public void writeSemanticChronology(SemanticChronology semanticChronicle) {
      BdbProvider.writeChronologyData(this.database, (ChronologyImpl) semanticChronicle);
   }

   @Override
   public Stream<SemanticChronology> getDescriptionsForComponent(int componentNid) {
         final SemanticSequenceSet sequences = getSemanticChronologySequencesForComponentFromAssemblage(
                                                componentNid,
                                                      TermAux.ENGLISH_DESCRIPTION_ASSEMBLAGE.getConceptSequence());
      final IntFunction<SemanticChronology> mapper = (int sememeSequence) -> (SemanticChronology) getSemanticChronology(
                                                         sememeSequence);

      return sequences.stream()
                      .filter(
                          (int sememeSequence) -> {
                             final Optional<? extends SemanticChronology> sememe = getOptionalSemanticChronology(
                                                                                       sememeSequence);

                             return sememe.isPresent() && (sememe.get().getVersionType() == VersionType.DESCRIPTION);
                          })
                      .mapToObj(mapper);
   }

   @Override
   public Optional<? extends SemanticChronology> getOptionalSemanticChronology(int semanticId) {
      if (hasSemantic(semanticId)) {
         return Optional.of(getSemanticChronology(semanticId));
      }
      return Optional.empty();
   }

   @Override
   public Stream<SemanticChronology> getParallelSemanticChronologyStream() {
      return StreamSupport.stream(new CursorChronologySpliterator(database, 
              Get.identifierService().getMaxSemanticSequence()), true)
              .map((byteBuffer) -> { 
                 IsaacObjectType.SEMANTIC.readAndValidateHeader(byteBuffer);
                 return SemanticChronologyImpl.make(byteBuffer); 
              }
              );
   }

   @Override
   public SemanticChronology getSemanticChronology(int semanticId) {
      if (semanticId < 0) {
         semanticId = Get.identifierService()
                        .getSemanticSequence(semanticId);
      }
      Optional<ByteArrayDataBuffer> optionalByteBuffer = BdbProvider.getChronologyData(this.database, semanticId);
      if (optionalByteBuffer.isPresent()) {
         ByteArrayDataBuffer byteBuffer = optionalByteBuffer.get();
         IsaacObjectType.SEMANTIC.readAndValidateHeader(byteBuffer);
         return SemanticChronologyImpl.make(byteBuffer);
      }
      throw new NoSuchElementException("No element for: " + semanticId);
   }

   @Override
   public boolean hasSemantic(int semanticId) {
      return BdbProvider.hasKey(this.database, semanticId);
   }

   @Override
   public Stream<SemanticChronology> getSemanticChronologyStream() {
      return StreamSupport.stream(new CursorChronologySpliterator(database, 
              Get.identifierService().getMaxSemanticSequence()), false)
               .map((byteBuffer) -> { 
                 IsaacObjectType.SEMANTIC.readAndValidateHeader(byteBuffer);
                 return SemanticChronologyImpl.make(byteBuffer); 
              });
  }

   @Override
   public int getSemanticChronologyCount() {
      return (int) getSemanticChronologyKeyParallelStream().count(); 
   }

   @Override
   public IntStream getSemanticChronologyKeyParallelStream() {
       return StreamSupport.intStream(new CursorSequenceSpliterator(database, Get.identifierService().getMaxSemanticSequence()),
              true);
  }

   @Override
   public IntStream getSemanticChronologyKeyStream() {
      return StreamSupport.intStream(new CursorSequenceSpliterator(database, Get.identifierService().getMaxSemanticSequence()),
              false);
   }

   @Override
   public SemanticSequenceSet getSemanticChronologySequencesForComponent(int componentNid) {
      AssemblageIndexService indexService = Get.service(AssemblageIndexService.class);

      return SemanticSequenceSet.of(indexService.getAttachmentNidsForComponent(componentNid));
   }

   @Override
   public SemanticSequenceSet getSemanticChronologySequencesForComponentFromAssemblage(int componentNid, int assemblageConceptSequence) {
     if (componentNid >= 0) {
         throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
      }

      assemblageConceptSequence = Get.identifierService()
                                     .getConceptSequence(assemblageConceptSequence);

      AssemblageIndexService indexService = Get.service(AssemblageIndexService.class);

      return SemanticSequenceSet.of(
          indexService.getAttachmentsForComponentInAssemblage(componentNid, assemblageConceptSequence));
   }

   @Override
   public SemanticSequenceSet getSemanticChronologySequencesFromAssemblage(int assemblageConceptSequence) {
      assemblageConceptSequence = Get.identifierService()
                                     .getConceptSequence(assemblageConceptSequence);

      AssemblageIndexService indexService = Get.service(AssemblageIndexService.class);

      return SemanticSequenceSet.of(indexService.getAttachmentNidsInAssemblage(assemblageConceptSequence));
   }

   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponent(int componentNid) {
      return getSemanticChronologySequencesForComponent(componentNid).stream()
            .mapToObj((int sememeSequence) -> (C) getSemanticChronology(sememeSequence));
   }

   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponentFromAssemblage(int componentNid, int assemblageConceptSequence) {
      if (componentNid >= 0) {
         throw new UnsupportedOperationException("Can't substitute a sequence for a nid: " + componentNid);
      }

      if (assemblageConceptSequence < 0) {
         assemblageConceptSequence = Get.identifierService()
                                        .getConceptSequence(assemblageConceptSequence);
      }

      final SemanticSequenceSet sememeSequences = getSemanticChronologySequencesForComponentFromAssemblage(
                                                      componentNid,
                                                            assemblageConceptSequence);

      return sememeSequences.stream()
                            .mapToObj((int sememeSequence) -> (C) getSemanticChronology(sememeSequence));
      }

   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamFromAssemblage(int assemblageConceptSequence) {
      final SemanticSequenceSet sememeSequences = getSemanticChronologySequencesFromAssemblage(
                                                      assemblageConceptSequence);

      return sememeSequences.stream()
                            .mapToObj((int sememeSequence) -> (C) getSemanticChronology(sememeSequence));
   }

   @Override
   public <V extends SemanticVersion> SemanticSnapshotService<V> getSnapshot(Class<V> versionType, StampCoordinate stampCoordinate) {
      return new AssemblageSnapshotProvider<>(versionType, stampCoordinate, this);
  }

   @Override
   public UUID getDataStoreId() {
     return bdb.getDataStoreId();
   }
   
}
