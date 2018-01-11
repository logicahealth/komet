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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
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
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.ContainerSequenceService;
import sh.isaac.model.ModelGet;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.semantic.SemanticChronologyImpl;

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = LookupService.SL_L2_DATABASE_SERVICES_STARTED_RUNLEVEL)
public class BdbSemanticProvider implements AssemblageService {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();
   private BdbProvider bdb;

   @Override
   public Path getDataStorePath() {
      return bdb.getDataStorePath();
   }

   @Override
   public DataStoreStartState getDataStoreStartState() {
      return bdb.getDataStoreStartState();
   }
   
   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      try {
         LOG.info("Starting semantic provider.");
         bdb = Get.service(BdbProvider.class);
         
      } catch (Exception ex) {
         ex.printStackTrace();
         throw new RuntimeException(ex);
      }
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
      bdb.writeChronologyData((ChronologyImpl) semanticChronicle);
   }

   @Override
   public List<SemanticChronology> getDescriptionsForComponent(int componentNid) {
     if (componentNid >= 0) {
         throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
      }
      
      final NidSet sequences = getSemanticNidsForComponentFromAssemblage(
                                                componentNid,
                                                      TermAux.ENGLISH_DESCRIPTION_ASSEMBLAGE.getNid());
      List<SemanticChronology> results = new ArrayList<>(sequences.size());
      for (int semanticNid: sequences.asArray()) {
            SemanticChronology semanticChronology = getSemanticChronology(semanticNid);
            if (semanticChronology != null && semanticChronology.getVersionType() == VersionType.DESCRIPTION) {
               results.add(semanticChronology);
            }
      }
      return results;
   }

   @Override
   public Optional<? extends SemanticChronology> getOptionalSemanticChronology(int semanticNid) {
      if (Get.identifierService().getAssemblageNid(semanticNid).isPresent()) {
         return Optional.of(getSemanticChronology(semanticNid));
      }
      return Optional.empty();
   }

   @Override
   public SemanticChronology getSemanticChronology(int semanticId) {
       Optional<ByteArrayDataBuffer> optionalByteBuffer = 
              bdb.getChronologyData(semanticId);
      if (optionalByteBuffer.isPresent()) {
         ByteArrayDataBuffer byteBuffer = optionalByteBuffer.get();
         IsaacObjectType.SEMANTIC.readAndValidateHeader(byteBuffer);
         return SemanticChronologyImpl.make(byteBuffer);
      }
      throw new NoSuchElementException("No element for: " + semanticId);
   }

   @Override
   public Stream<SemanticChronology> getSemanticChronologyStream() {
      return getSemanticNidStream().mapToObj((value) -> {
         return getSemanticChronology(value); 
      });
  }

   @Override
   public IntStream getSemanticNidStream() {
      return ModelGet.identifierService().getNidStreamOfType(IsaacObjectType.SEMANTIC);
   }

   @Override
   public NidSet getSemanticNidsForComponent(int componentNid) {
      int[] semanticNids = bdb.getComponentToSemanticNidsMap().get(componentNid);
      return NidSet.of(semanticNids);
   }

   @Override
   public NidSet getSemanticNidsForComponentFromAssemblage(int componentNid, int assemblageNid) {
      return getSemanticNidsForComponentFromAssemblages(componentNid, Collections.singleton(assemblageNid));
   }
   
   @Override
   public NidSet getSemanticNidsForComponentFromAssemblages(int componentNid, Set<Integer> assemblageConceptNids) {
      if (componentNid >= 0) {
         throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
      }
      if (assemblageConceptNids == null)
      {
         throw new IndexOutOfBoundsException("Assemblage identifier(s) must be provided.");
      }
      
      for (int assemblageNid : assemblageConceptNids) {
         if (assemblageNid >= 0) {
            throw new IndexOutOfBoundsException("Assemblage identifiers must be negative. Found: " + componentNid);
         }
      }
      ContainerSequenceService identifierService = ModelGet.identifierService();
      NidSet semanticNids = new NidSet();
      for (int semanticNid: bdb.getComponentToSemanticNidsMap().get(componentNid)) {
         if (assemblageConceptNids.contains(identifierService.getAssemblageNidForNid(semanticNid))) {
            semanticNids.add(semanticNid);
         }
      }
      return semanticNids;
   }

   @Override
   public NidSet getSemanticNidsFromAssemblage(int assemblageNid) {
      return NidSet.of(ModelGet.identifierService().getNidsForAssemblage(assemblageNid));
   }

   @Override
   public IntStream getSemanticNidStream(int assemblageNid) {
      return ModelGet.identifierService().getNidsForAssemblage(assemblageNid);
   }

   @Override
   public int getSemanticCount() {
      return (int) ModelGet.identifierService().getNidStreamOfType(IsaacObjectType.SEMANTIC).count();
   }

   @Override
   public int getSemanticCount(int assemblageNid) {
      return (int) ModelGet.identifierService().getNidsForAssemblage(assemblageNid).count();
   }

   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponent(int componentNid) {
      return getSemanticNidsForComponent(componentNid).stream()
            .mapToObj((int sememeSequence) -> (C) getSemanticChronology(sememeSequence));
   }

   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponentFromAssemblage(int componentNid, int assemblageConceptNid) {
      return getSemanticChronologyStreamForComponentFromAssemblages(componentNid, Collections.singleton(assemblageConceptNid));
      }
   
   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponentFromAssemblages(int componentNid,
         Set<Integer> assemblageConceptNids) {
      final NidSet sememeSequences = getSemanticNidsForComponentFromAssemblages(componentNid, assemblageConceptNids);

      return sememeSequences.stream().mapToObj((int sememeSequence) -> (C) getSemanticChronology(sememeSequence));
   }

   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamFromAssemblage(int assemblageConceptNid) {
      final NidSet semanticSequences = getSemanticNidsFromAssemblage(assemblageConceptNid);

      return semanticSequences.stream()
                            .mapToObj((int semanticSequence) -> (C) getSemanticChronology(semanticSequence));
   }

   @Override
   public <V extends SemanticVersion> SemanticSnapshotService<V> getSnapshot(Class<V> versionType, StampCoordinate stampCoordinate) {
      return new AssemblageSnapshotProvider<>(versionType, stampCoordinate, this);
  }

   @Override
   public Optional<UUID> getDataStoreId() {
     UUID fromFile = bdb.getDataStoreId().orElse(null);
     
     //This is a sanity check, which gets run by the Lookup Service during the startup sequence.
     Optional<SemanticChronology> sdic = getSemanticChronologyStreamForComponentFromAssemblage(TermAux.SOLOR_ROOT.getNid(), TermAux.DATABASE_UUID.getNid())
           .findFirst();
     if (sdic.isPresent()) {
        LatestVersion<Version> sdi = sdic.get().getLatestVersion(StampCoordinates.getDevelopmentLatest());
        if (sdi.isPresent()) {
           try {
              UUID temp = UUID.fromString(((StringVersion) sdi.get()).getString());
              
              if (!temp.equals(fromFile)) {
                 LOG.error("Semantic Store has {} while bdb file store has {}", temp, fromFile);
                 throw new RuntimeException("UUID stored in the semantic store does not match the UUID on disk in the id file!");
              }
                 
           } catch (Exception e) {
              LOG.warn("The Database UUID annotation on Isaac Root does not contain a valid UUID!", e);
           }
        }
     }
     return Optional.ofNullable(fromFile);
   }

   @Override
   public boolean hasSemantic(int semanticId) {
      return bdb.getChronologyData(semanticId).isPresent();
   }

   @Override
   public Future<?> sync() {
      return this.bdb.sync();
   }
}