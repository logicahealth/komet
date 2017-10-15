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
package sh.isaac.provider.assemblage;

//~--- JDK imports ------------------------------------------------------------
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.AssemblageService;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.SemanticSequenceSet;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.index.AssemblageIndexService;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.waitfree.CasSequenceObjectMap;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticServiceTyped;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.SemanticVersion;

//~--- classes ----------------------------------------------------------------
/**
 * The Class AssemblageProvider.
 *
 * @author kec
 */
@Service
@RunLevel(value = 0)
@Rank(value = 10)
public class AssemblageProvider
        implements AssemblageService {

   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   /**
    * The in use assemblages.
    */
   private transient HashSet<Integer> inUseAssemblages = new HashSet<>();

   /**
    * The load required.
    */
   private final AtomicBoolean loadRequired = new AtomicBoolean();

   /**
    * The database validity.
    */
   private DatabaseValidity databaseValidity = DatabaseValidity.NOT_SET;

   /**
    * The sememe map.
    */
   final CasSequenceObjectMap<SemanticChronologyImpl> sememeMap;

   /**
    * The sememe path.
    */
   final Path sememePath;

   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new sememe provider.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   // For HK2
   private AssemblageProvider()
           throws IOException {
      try {
         this.sememePath = LookupService.getService(ConfigurationService.class)
                 .getChronicleFolderPath()
                 .resolve("sememe");

         if (!Files.exists(this.sememePath)) {
            this.databaseValidity = DatabaseValidity.MISSING_DIRECTORY;
         }

         this.loadRequired.set(!Files.exists(this.sememePath));
         Files.createDirectories(this.sememePath);
         LOG.info("Setting up sememe provider at " + this.sememePath.toAbsolutePath().toString());
         this.sememeMap = new CasSequenceObjectMap<>(
                 new AssemblageSerializer(),
                 this.sememePath,
                 "seg.",
                 ".sememe.map");
      } catch (final IOException e) {
         LookupService.getService(SystemStatusService.class)
                 .notifyServiceConfigurationFailure("Cradle Commit Manager", e);
         throw e;
      }
   }

   //~--- methods -------------------------------------------------------------
   /**
    * Clear database validity value.
    */
   @Override
   public void clearDatabaseValidityValue() {
      // Reset to enforce analysis
      this.databaseValidity = DatabaseValidity.NOT_SET;
   }

   /**
    * Of type.
    *
    * @param <V> the value type
    * @param versionType the version type
    * @return the sememe service typed
    */
   @Override
   public <V extends SemanticVersion> SemanticServiceTyped ofType(VersionType versionType) {
      return new AssemblageOfTypeProvider(versionType, this);
   }

   /**
    * Write sememe.
    *
    * @param sememeChronicle the sememe chronicle
    */
   @Override
   public void writeSemanticChronology(SemanticChronology sememeChronicle) {
      this.inUseAssemblages.add(sememeChronicle.getAssemblageSequence());
      this.sememeMap.put(sememeChronicle.getSemanticSequence(), (SemanticChronologyImpl) sememeChronicle);
   }

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      LOG.info("Loading sememeMap.");
      if (!this.loadRequired.get()) {
         LOG.info("Reading existing sememeMap.");
         
         final boolean isPopulated = this.sememeMap.initialize();
         
         if (isPopulated) {
            this.databaseValidity = DatabaseValidity.POPULATED_DIRECTORY;
         }
      }
      LOG.info("Finished SememeProvider load.");
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping SememeProvider pre-destroy. ");
      LOG.info("writing sememe-map.");
      this.sememeMap.write();
      LOG.info("Finished SememeProvider stop.");
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the assemblage types.
    *
    * @return the assemblage types
    */
   @Override
   public Stream<Integer> getAssemblageTypes() {
      return this.inUseAssemblages.stream();
   }

   /**
    * Gets the database folder.
    *
    * @return the database folder
    */
   @Override
   public Path getDatabaseFolder() {
      return this.sememePath;
   }

   /**
    * Gets the database validity status.
    *
    * @return the database validity status
    */
   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      return this.databaseValidity;
   }

   /**
    * Gets the descriptions for component.
    *
    * @param componentNid the component nid
    * @return the descriptions for component
    */
   @Override
   public Stream<SemanticChronology> getDescriptionsForComponent(int componentNid) {
      
      final SemanticSequenceSet sequences = getSemanticChronologySequencesForComponentFromAssemblage(componentNid, TermAux.ENGLISH_DESCRIPTION_ASSEMBLAGE.getConceptSequence());
      final IntFunction<SemanticChronology> mapper = (int sememeSequence) -> (SemanticChronology) getSemanticChronology(sememeSequence);

      return sequences.stream()
              .filter((int sememeSequence) -> {
                         final Optional<? extends SemanticChronology> sememe = getOptionalSemanticChronology(sememeSequence);

                         return sememe.isPresent() && (sememe.get().getVersionType() == VersionType.DESCRIPTION);
                      })
              .mapToObj(mapper);
   }

   /**
    * Gets the optional sememe.
    *
    * @param sememeSequence the sememe sequence
    * @return the optional sememe
    */
   @Override
   public Optional<? extends SemanticChronology> getOptionalSemanticChronology(int sememeSequence) {
      sememeSequence = Get.identifierService()
              .getSemanticSequence(sememeSequence);
      return this.sememeMap.get(sememeSequence);
   }

   /**
    * Gets the parallel sememe stream.
    *
    * @return the parallel sememe stream
    */
   @Override
   public Stream<SemanticChronology> getParallelSemanticChronologyStream() {
      return this.sememeMap.getParallelStream()
              .map((s) -> {
                         return (SemanticChronology) s;
                      });
   }

   /**
    * Gets the sememe.
    *
    * @param sememeId the sememe id
    * @return the sememe
    */
   @Override
   public SemanticChronology getSemanticChronology(int sememeId) {
      sememeId = Get.identifierService()
              .getSemanticSequence(sememeId);
      return this.sememeMap.getQuick(sememeId);
   }

   /**
    * Checks for sememe.
    *
    * @param sememeId the sememe id
    * @return true, if successful
    */
   @Override
   public boolean hasSemanticChronology(int sememeId) {
      if (sememeId < 0) {
         sememeId = Get.identifierService()
                 .getSemanticSequence(sememeId);
      }

      return this.sememeMap.containsKey(sememeId);
   }

   /**
    * Gets the sememe chronology stream.
    *
    * @return the sememe chronology stream
    */
   @Override
   public Stream<SemanticChronology> getSemanticChronologyStream() {
      return this.sememeMap.getStream()
              .map((s) -> {
                         return (SemanticChronology) s;
                      });
   }

   /**
    * Gets the sememe count.
    *
    * @return the sememe count
    */
   @Override
   public int getSemanticChronologyCount() {
      return this.sememeMap.getSize();
   }

   /**
    * Gets the sememe key parallel stream.
    *
    * @return the sememe key parallel stream
    */
   @Override
   public IntStream getSemanticChronologyKeyParallelStream() {
      return this.sememeMap.getKeyParallelStream();
   }

   /**
    * Gets the sememe key stream.
    *
    * @return the sememe key stream
    */
   @Override
   public IntStream getSemanticChronologyKeyStream() {
      return this.sememeMap.getKeyStream();
   }

   /**
    * Gets the sememe sequences for component.
    *
    * @param componentNid the component nid
    * @return the sememe sequences for component
    */
   @Override
   public SemanticSequenceSet getSemanticChronologySequencesForComponent(int componentNid) {
      
      AssemblageIndexService indexService = Get.service(AssemblageIndexService.class);
      return SemanticSequenceSet.of(indexService.getAttachmentNidsForComponent(componentNid));
   }

   /**
    * Gets the sememe sequences for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememe sequences for component from assemblage
    */
   @Override
   public SemanticSequenceSet getSemanticChronologySequencesForComponentFromAssemblage(int componentNid,
           int assemblageConceptSequence) {
      if (componentNid >= 0) {
         throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
      }

      assemblageConceptSequence = Get.identifierService()
              .getConceptSequence(assemblageConceptSequence);

      AssemblageIndexService indexService = Get.service(AssemblageIndexService.class);
      return SemanticSequenceSet.of(indexService.getAttachmentsForComponentInAssemblage(componentNid, assemblageConceptSequence));
   }

   /**
    * Gets the sememe sequences from assemblage.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememe sequences from assemblage
    */
   @Override
   public SemanticSequenceSet getSemanticChronologySequencesFromAssemblage(int assemblageConceptSequence) {
      assemblageConceptSequence = Get.identifierService()
              .getConceptSequence(assemblageConceptSequence);
      AssemblageIndexService indexService = Get.service(AssemblageIndexService.class);
      return SemanticSequenceSet.of(indexService.getAttachmentNidsInAssemblage(assemblageConceptSequence));
   }

   /**
    * Gets the sememes for component.
    *
    * @param componentNid the component nid
    * @return the sememes for component
    */
   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyForComponent(int componentNid) {
      return getSemanticChronologySequencesForComponent(componentNid).stream().mapToObj((int sememeSequence) -> (C) getSemanticChronology(sememeSequence));
   }

   /**
    * Gets the sememes for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememes for component from assemblage
    */
   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyForComponentFromAssemblage(int componentNid,
           int assemblageConceptSequence) {
      if (componentNid >= 0) {
         componentNid = Get.identifierService()
                 .getConceptNid(componentNid);
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


   /**
    * Gets the sememes from assemblage.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememes from assemblage
    */
   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyFromAssemblage(int assemblageConceptSequence) {
      final SemanticSequenceSet sememeSequences = getSemanticChronologySequencesFromAssemblage(assemblageConceptSequence);

      return sememeSequences.stream()
              .mapToObj((int sememeSequence) -> (C) getSemanticChronology(sememeSequence));
   }

   /**
    * Gets the snapshot.
    *
    * @param <V> the value type
    * @param versionType the version type
    * @param stampCoordinate the stamp coordinate
    * @return the snapshot
    */
   @Override
   public <V extends SemanticVersion> SemanticSnapshotService<V> getSnapshot(Class<V> versionType,
           StampCoordinate stampCoordinate) {
      return new AssemblageSnapshotProvider<>(versionType, stampCoordinate, this);
   }
}
