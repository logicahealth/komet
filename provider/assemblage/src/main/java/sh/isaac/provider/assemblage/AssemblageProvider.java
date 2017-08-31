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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Arrays;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
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
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.SememeSequenceSet;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeConstraints;
import sh.isaac.api.component.sememe.SememeServiceTyped;
import sh.isaac.api.component.sememe.SememeSnapshotService;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.model.sememe.SememeChronologyImpl;
import sh.isaac.model.waitfree.CasSequenceObjectMap;

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
    * The assemblage sequence sememe sequence map.
    */
   ConcurrentSkipListSet<AssemblageSememeKey> assemblageSequenceSememeSequenceMap = new ConcurrentSkipListSet<>();

   /**
    * The referenced nid assemblage sequence sememe sequence map.
    */
   ConcurrentSkipListSet<ReferencedNidAssemblageSequenceSememeSequenceKey> referencedNidAssemblageSequenceSememeSequenceMap =
      new ConcurrentSkipListSet<>();

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
   final CasSequenceObjectMap<SememeChronologyImpl> sememeMap;

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
   public <V extends SememeVersion> SememeServiceTyped ofType(SememeType versionType) {
      return new AssemblageOfTypeProvider(versionType, this);
   }

   /**
    * Write sememe.
    *
    * @param sememeChronicle the sememe chronicle
    * @param constraints the constraints
    */
   @Override
   public void writeSememe(SememeChronology sememeChronicle, SememeConstraints... constraints) {
      Arrays.stream(constraints)
            .forEach(
                (constraint) -> {
                   switch (constraint) {
                   case ONE_SEMEME_PER_COMPONENT:
                      final ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeStart =
                         new ReferencedNidAssemblageSequenceSememeSequenceKey(
                             sememeChronicle.getReferencedComponentNid(),
                             sememeChronicle.getAssemblageSequence(),
                             Integer.MIN_VALUE);  // yes
                      final ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeEnd =
                         new ReferencedNidAssemblageSequenceSememeSequenceKey(
                             sememeChronicle.getReferencedComponentNid(),
                             sememeChronicle.getAssemblageSequence(),
                             Integer.MAX_VALUE);  // no
                      final NavigableSet<ReferencedNidAssemblageSequenceSememeSequenceKey> subset =
                         this.referencedNidAssemblageSequenceSememeSequenceMap.subSet(
                             rcRangeStart,
                             rcRangeEnd);

                      if (!subset.isEmpty()) {
                         if (!subset.stream()
                                    .allMatch((value) -> value.sememeSequence == sememeChronicle.getSememeSequence())) {
                            throw new IllegalStateException(
                                "Attempt to add a second sememe for component, where assemblage has a ONE_SEMEME_PER_COMPONENT constraint." +
                                "\n New sememe: " + sememeChronicle + "\n Existing in index: " + subset);
                         }
                      }

                      break;

                   default:
                      throw new UnsupportedOperationException("Can't handle " + constraint);
                   }
                });
      this.assemblageSequenceSememeSequenceMap.add(
          new AssemblageSememeKey(sememeChronicle.getAssemblageSequence(), sememeChronicle.getSememeSequence()));
      this.inUseAssemblages.add(sememeChronicle.getAssemblageSequence());
      this.referencedNidAssemblageSequenceSememeSequenceMap.add(
          new ReferencedNidAssemblageSequenceSememeSequenceKey(
              sememeChronicle.getReferencedComponentNid(),
              sememeChronicle.getAssemblageSequence(),
              sememeChronicle.getSememeSequence()));
      this.sememeMap.put(sememeChronicle.getSememeSequence(), (SememeChronologyImpl) sememeChronicle);
   }

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      try {
         LOG.info("Loading sememeMap.");

         if (!this.loadRequired.get()) {
            LOG.info("Reading existing sememeMap.");

            final boolean isPopulated     = this.sememeMap.initialize();
            File          mapAsObjectFile = new File(this.sememePath.toFile(), "assemblage-sememe.csls");

            if (mapAsObjectFile.exists()) {
               try (ObjectInputStream ois = new ObjectInputStream(
                                                new BufferedInputStream(
                                                    new FileInputStream(mapAsObjectFile)))) {
                  this.assemblageSequenceSememeSequenceMap =
                     (ConcurrentSkipListSet<AssemblageSememeKey>) ois.readObject();
                  this.assemblageSequenceSememeSequenceMap.forEach(
                      (element) -> {
                         this.inUseAssemblages.add(element.assemblageSequence);
                      });
               }
            } else {
               LOG.info("Reading existing SememeKeys.");

               try (DataInputStream in = new DataInputStream(
                                             new BufferedInputStream(
                                                 new FileInputStream(
                                                     new File(this.sememePath.toFile(), "assemblage-sememe.keys"))))) {
                  final int size = in.readInt();

                  for (int i = 0; i < size; i++) {
                     final int assemblageSequence = in.readInt();
                     final int sequence           = in.readInt();

                     this.assemblageSequenceSememeSequenceMap.add(
                         new AssemblageSememeKey(assemblageSequence, sequence));
                     this.inUseAssemblages.add(assemblageSequence);
                  }
               }
            }

            File componentMapFile = new File(this.sememePath.toFile(), "component-sememe.csls");

            if (componentMapFile.exists()) {
               try (ObjectInputStream ois = new ObjectInputStream(
                                                new BufferedInputStream(
                                                    new FileInputStream(componentMapFile)))) {
                  this.referencedNidAssemblageSequenceSememeSequenceMap =
                     (ConcurrentSkipListSet<ReferencedNidAssemblageSequenceSememeSequenceKey>) ois.readObject();
                }
           } else {
               try (DataInputStream in = new DataInputStream(
                                             new BufferedInputStream(
                                                 new FileInputStream(
                                                     new File(this.sememePath.toFile(), "component-sememe.keys"))))) {
                  final int size = in.readInt();

                  for (int i = 0; i < size; i++) {
                     final int referencedNid      = in.readInt();
                     final int assemblageSequence = in.readInt();
                     final int sequence           = in.readInt();

                     this.referencedNidAssemblageSequenceSememeSequenceMap.add(
                         new ReferencedNidAssemblageSequenceSememeSequenceKey(
                             referencedNid,
                             assemblageSequence,
                             sequence));
                  }
               }
            }

            if (isPopulated) {
               this.databaseValidity = DatabaseValidity.POPULATED_DIRECTORY;
            }
         }

         final SememeSequenceSet statedGraphSequences = getSememeSequencesFromAssemblage(
                                                            Get.identifierService()
                                                                  .getConceptSequence(
                                                                        Get.identifierService()
                                                                              .getNidForUuids(
                                                                                    TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getUuids())));

         LOG.info("Stated logic graphs: " + statedGraphSequences.size());

         final SememeSequenceSet inferedGraphSequences = getSememeSequencesFromAssemblage(
                                                             Get.identifierService()
                                                                   .getConceptSequence(
                                                                         Get.identifierService()
                                                                               .getNidForUuids(
                                                                                     TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getUuids())));

         LOG.info("Inferred logic graphs: " + inferedGraphSequences.size());
         LOG.info("Finished SememeProvider load.");
      } catch (final IOException | ClassNotFoundException e) {
         LookupService.getService(SystemStatusService.class)
                      .notifyServiceConfigurationFailure("Cradle Commit Manager", e);
         throw new RuntimeException(e);
      }
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping SememeProvider pre-destroy. ");

      try {
         // Dan commented out this LOG statement because it is really slow...
         // log.info("sememeMap size: {}", sememeMap.getSize());
         LOG.info("writing sememe-map.");
         this.sememeMap.write();
         LOG.info("writing assemblageSequenceSememeSequenceMap as object");

         try (ObjectOutputStream out = new ObjectOutputStream(
                                           new BufferedOutputStream(
                                               new FileOutputStream(
                                                     new File(this.sememePath.toFile(), "assemblage-sememe.csls"))))) {
            out.writeObject(this.assemblageSequenceSememeSequenceMap);
         }

         LOG.info("writing SememeKeys.");

         try (DataOutputStream out = new DataOutputStream(
                                         new BufferedOutputStream(
                                             new FileOutputStream(
                                                   new File(this.sememePath.toFile(), "assemblage-sememe.keys"))))) {
            out.writeInt(this.assemblageSequenceSememeSequenceMap.size());

            for (final AssemblageSememeKey key: this.assemblageSequenceSememeSequenceMap) {
               out.writeInt(key.assemblageSequence);
               out.writeInt(key.sememeSequence);
            }
         }

         LOG.info("writing referencedNidAssemblageSequenceSememeSequenceMap as object");

         try (ObjectOutputStream out = new ObjectOutputStream(
                                           new BufferedOutputStream(
                                               new FileOutputStream(
                                                     new File(this.sememePath.toFile(), "component-sememe.csls"))))) {
            out.writeObject(this.referencedNidAssemblageSequenceSememeSequenceMap);
         }

         try (DataOutputStream out = new DataOutputStream(
                                         new BufferedOutputStream(
                                             new FileOutputStream(
                                                   new File(this.sememePath.toFile(), "component-sememe.keys"))))) {
            out.writeInt(this.referencedNidAssemblageSequenceSememeSequenceMap.size());

            for (final ReferencedNidAssemblageSequenceSememeSequenceKey key:
                  this.referencedNidAssemblageSequenceSememeSequenceMap) {
               out.writeInt(key.referencedNid);
               out.writeInt(key.assemblageSequence);
               out.writeInt(key.sememeSequence);
            }
         }
      } catch (final IOException e) {
         LOG.error("Exception during shutdown...", e);
         throw new RuntimeException(e);
      }

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
   public Stream<SememeChronology> getDescriptionsForComponent(int componentNid) {
      final SememeSequenceSet             sequences = getSememeSequencesForComponent(componentNid);
      final IntFunction<SememeChronology> mapper = (int sememeSequence) -> (SememeChronology) getSememe(sememeSequence);

      return sequences.stream()
                      .filter(
                          (int sememeSequence) -> {
                             final Optional<? extends SememeChronology> sememe = getOptionalSememe(sememeSequence);

                             return sememe.isPresent() && (sememe.get().getSememeType() == SememeType.DESCRIPTION);
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
   public Optional<? extends SememeChronology> getOptionalSememe(int sememeSequence) {
      sememeSequence = Get.identifierService()
                          .getSememeSequence(sememeSequence);
      return this.sememeMap.get(sememeSequence);
   }

   /**
    * Gets the parallel sememe stream.
    *
    * @return the parallel sememe stream
    */
   @Override
   public Stream<SememeChronology> getParallelSememeStream() {
      return this.sememeMap.getParallelStream()
                           .map(
                               (s) -> {
                                  return (SememeChronology) s;
                               });
   }

   /**
    * Gets the sememe.
    *
    * @param sememeId the sememe id
    * @return the sememe
    */
   @Override
   public SememeChronology getSememe(int sememeId) {
      sememeId = Get.identifierService()
                    .getSememeSequence(sememeId);
      return this.sememeMap.getQuick(sememeId);
   }

   /**
    * Checks for sememe.
    *
    * @param sememeId the sememe id
    * @return true, if successful
    */
   @Override
   public boolean hasSememe(int sememeId) {
      if (sememeId < 0) {
         sememeId = Get.identifierService()
                       .getSememeSequence(sememeId);
      }

      return this.sememeMap.containsKey(sememeId);
   }

   /**
    * Gets the sememe chronology stream.
    *
    * @return the sememe chronology stream
    */
   @Override
   public Stream<SememeChronology> getSememeChronologyStream() {
      return this.sememeMap.getStream()
                           .map(
                               (s) -> {
                                  return (SememeChronology) s;
                               });
   }

   /**
    * Gets the sememe count.
    *
    * @return the sememe count
    */
   @Override
   public int getSememeCount() {
      return this.sememeMap.getSize();
   }

   /**
    * Gets the sememe key parallel stream.
    *
    * @return the sememe key parallel stream
    */
   @Override
   public IntStream getSememeKeyParallelStream() {
      return this.sememeMap.getKeyParallelStream();
   }

   /**
    * Gets the sememe key stream.
    *
    * @return the sememe key stream
    */
   @Override
   public IntStream getSememeKeyStream() {
      return this.sememeMap.getKeyStream();
   }

   /**
    * Gets the sememe sequences for component.
    *
    * @param componentNid the component nid
    * @return the sememe sequences for component
    */
   @Override
   public SememeSequenceSet getSememeSequencesForComponent(int componentNid) {
      return getSememeSequencesForComponentFromAssemblages(componentNid, null);
   }

   /**
    * Gets the sememe sequences for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememe sequences for component from assemblage
    */
   @Override
   public SememeSequenceSet getSememeSequencesForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence) {
      if (componentNid >= 0) {
         throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
      }

      assemblageConceptSequence = Get.identifierService()
                                     .getConceptSequence(assemblageConceptSequence);

      final ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeStart =
         new ReferencedNidAssemblageSequenceSememeSequenceKey(
             componentNid,
             assemblageConceptSequence,
             Integer.MIN_VALUE);  // yes
      final ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeEnd =
         new ReferencedNidAssemblageSequenceSememeSequenceKey(
             componentNid,
             assemblageConceptSequence,
             Integer.MAX_VALUE);  // no
      final NavigableSet<ReferencedNidAssemblageSequenceSememeSequenceKey> referencedComponentRefexKeys =
         this.referencedNidAssemblageSequenceSememeSequenceMap.subSet(
             rcRangeStart,
             true,
             rcRangeEnd,
             true);
      final SememeSequenceSet referencedComponentSet = SememeSequenceSet.of(
                                                           referencedComponentRefexKeys.stream()
                                                                 .mapToInt(
                                                                       (ReferencedNidAssemblageSequenceSememeSequenceKey key) -> key.sememeSequence));

      return referencedComponentSet;
   }

   /**
    * Gets the sememe sequences for component from assemblages.
    *
    * @param componentNid the component nid
    * @param allowedAssemblageSequences the allowed assemblage sequences
    * @return the sememe sequences for component from assemblages
    */
   @Override
   public SememeSequenceSet getSememeSequencesForComponentFromAssemblages(int componentNid,
         Set<Integer> allowedAssemblageSequences) {
      if (componentNid >= 0) {
         LOG.warn("Component identifiers must be negative. Found: " + componentNid);

         // throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
         return SememeSequenceSet.of();
      }

      final NavigableSet<ReferencedNidAssemblageSequenceSememeSequenceKey> assemblageSememeKeys =
         this.referencedNidAssemblageSequenceSememeSequenceMap.subSet(
             new ReferencedNidAssemblageSequenceSememeSequenceKey(
                 componentNid,
                 Integer.MIN_VALUE,
                 Integer.MIN_VALUE),
             true,
             new ReferencedNidAssemblageSequenceSememeSequenceKey(
                 componentNid,
                 Integer.MAX_VALUE,
                 Integer.MAX_VALUE),
             true);

      return SememeSequenceSet.of(
          assemblageSememeKeys.stream()
                              .filter(
                                  (ReferencedNidAssemblageSequenceSememeSequenceKey key) -> {
                                     if ((allowedAssemblageSequences == null) ||
                                         allowedAssemblageSequences.isEmpty() ||
                                         allowedAssemblageSequences.contains(key.assemblageSequence)) {
                                        return true;
                                     }

                                     return false;
                                  })
                              .mapToInt((ReferencedNidAssemblageSequenceSememeSequenceKey key) -> key.sememeSequence));
   }

   /**
    * Gets the sememe sequences for components from assemblage.
    *
    * @param componentNidSet the component nid set
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememe sequences for components from assemblage
    */
   @Override
   public SememeSequenceSet getSememeSequencesForComponentsFromAssemblage(NidSet componentNidSet,
         final int assemblageConceptSequence) {
      if (assemblageConceptSequence < 0) {
         throw new IndexOutOfBoundsException("assemblageSequence must be >= 0. Found: " + assemblageConceptSequence);
      }

      final SememeSequenceSet resultSet = new SememeSequenceSet();

      componentNidSet.stream()
                     .forEach(
                         (componentNid) -> {
                            final ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeStart =
                               new ReferencedNidAssemblageSequenceSememeSequenceKey(
                                   componentNid,
                                   assemblageConceptSequence,
                                   Integer.MIN_VALUE);  // yes
                            final ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeEnd =
                               new ReferencedNidAssemblageSequenceSememeSequenceKey(
                                   componentNid,
                                   assemblageConceptSequence,
                                   Integer.MAX_VALUE);  // no
                            final NavigableSet<ReferencedNidAssemblageSequenceSememeSequenceKey> referencedComponentRefexKeys =
                               this.referencedNidAssemblageSequenceSememeSequenceMap.subSet(
                                   rcRangeStart,
                                   true,
                                   rcRangeEnd,
                                   true);

                            referencedComponentRefexKeys.stream()
                                  .forEach(
                                      (key) -> {
                                         resultSet.add(key.sememeSequence);
                                      });
                         });
      return resultSet;
   }

   /**
    * Gets the sememe sequences for components from assemblage modified after position.
    *
    * @param componentNidSet the component nid set
    * @param assemblageConceptSequence the assemblage concept sequence
    * @param position the position
    * @return the sememe sequences for components from assemblage modified after position
    */
   @Override
   public SememeSequenceSet getSememeSequencesForComponentsFromAssemblageModifiedAfterPosition(NidSet componentNidSet,
         int assemblageConceptSequence,
         StampPosition position) {
      final SememeSequenceSet sequencesToTest = getSememeSequencesForComponentsFromAssemblage(
                                                    componentNidSet,
                                                          assemblageConceptSequence);
      final SememeSequenceSet sequencesThatPassedTest = new SememeSequenceSet();

      sequencesToTest.stream()
                     .forEach(
                         (sememeSequence) -> {
                            final SememeChronologyImpl chronicle = (SememeChronologyImpl) getSememe(sememeSequence);

                            if (chronicle.getVersionStampSequences()
                                         .anyMatch(
                                               (stampSequence) -> {
                  return ((Get.stampService().getTimeForStamp(stampSequence) > position.getTime()) &&
                          (position.getStampPathSequence() == Get.stampService().getPathSequenceForStamp(
                              stampSequence)));
               })) {
                               sequencesThatPassedTest.add(sememeSequence);
                            }
                         });
      return sequencesThatPassedTest;
   }

   /**
    * Gets the sememe sequences from assemblage.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememe sequences from assemblage
    */
   @Override
   public SememeSequenceSet getSememeSequencesFromAssemblage(int assemblageConceptSequence) {
      assemblageConceptSequence = Get.identifierService()
                                     .getConceptSequence(assemblageConceptSequence);

      final AssemblageSememeKey rangeStart = new AssemblageSememeKey(
                                                 assemblageConceptSequence,
                                                       Integer.MIN_VALUE);  // yes
      final AssemblageSememeKey rangeEnd = new AssemblageSememeKey(assemblageConceptSequence, Integer.MAX_VALUE);  // no
      final NavigableSet<AssemblageSememeKey> assemblageSememeKeys = this.assemblageSequenceSememeSequenceMap.subSet(
                                                                         rangeStart,
                                                                               true,
                                                                               rangeEnd,
                                                                               true);

      return SememeSequenceSet.of(
          assemblageSememeKeys.stream()
                              .mapToInt((AssemblageSememeKey key) -> key.sememeSequence));
   }

   /**
    * Gets the sememes for component.
    *
    * @param componentNid the component nid
    * @return the sememes for component
    */
   @Override
   public <C extends SememeChronology> Stream<C> getSememesForComponent(int componentNid) {
      return getSememesForComponentFromAssemblages(componentNid, null);
   }

   /**
    * Gets the sememes for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememes for component from assemblage
    */
   @Override
   public <C extends SememeChronology> Stream<C> getSememesForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence) {
      if (componentNid >= 0) {
         componentNid = Get.identifierService()
                           .getConceptNid(componentNid);
      }

      if (assemblageConceptSequence < 0) {
         assemblageConceptSequence = Get.identifierService()
                                        .getConceptSequence(assemblageConceptSequence);
      }

      final SememeSequenceSet sememeSequences = getSememeSequencesForComponentFromAssemblage(
                                                    componentNid,
                                                          assemblageConceptSequence);

      return sememeSequences.stream()
                            .mapToObj((int sememeSequence) -> (C) getSememe(sememeSequence));
   }

   /**
    * Gets the sememes for component from assemblages.
    *
    * @param componentNid the component nid
    * @param allowedAssemblageSequences the allowed assemblage sequences
    * @return the sememes for component from assemblages
    */
   @Override
   public <C extends SememeChronology> Stream<C> getSememesForComponentFromAssemblages(int componentNid,
         Set<Integer> allowedAssemblageSequences) {
      final SememeSequenceSet sememeSequences = getSememeSequencesForComponentFromAssemblages(
                                                    componentNid,
                                                          allowedAssemblageSequences);

      return sememeSequences.stream()
                            .mapToObj((int sememeSequence) -> (C) getSememe(sememeSequence));
   }

   /**
    * Gets the sememes from assemblage.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememes from assemblage
    */
   @Override
   public <C extends SememeChronology> Stream<C> getSememesFromAssemblage(int assemblageConceptSequence) {
      final SememeSequenceSet sememeSequences = getSememeSequencesFromAssemblage(assemblageConceptSequence);

      return sememeSequences.stream()
                            .mapToObj((int sememeSequence) -> (C) getSememe(sememeSequence));
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
   public <V extends SememeVersion> SememeSnapshotService<V> getSnapshot(Class<V> versionType,
         StampCoordinate stampCoordinate) {
      return new AssemblageSnapshotProvider<>(versionType, stampCoordinate, this);
   }
}

