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



package sh.isaac.provider.sememe;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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

import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.SememeSequenceSet;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeConstraints;
import sh.isaac.api.component.sememe.SememeService;
import sh.isaac.api.component.sememe.SememeServiceTyped;
import sh.isaac.api.component.sememe.SememeSnapshotService;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.model.sememe.SememeChronologyImpl;
import sh.isaac.model.waitfree.CasSequenceObjectMap;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 0)
@Rank(value = 10)
public class SememeProvider
         implements SememeService {
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   final ConcurrentSkipListSet<AssemblageSememeKey> assemblageSequenceSememeSequenceMap = new ConcurrentSkipListSet<>();
   final ConcurrentSkipListSet<ReferencedNidAssemblageSequenceSememeSequenceKey> referencedNidAssemblageSequenceSememeSequenceMap =
      new ConcurrentSkipListSet<>();
   private transient HashSet<Integer>                                           inUseAssemblages = new HashSet<>();
   private AtomicBoolean                                                        loadRequired     = new AtomicBoolean();
   private DatabaseValidity databaseValidity = DatabaseValidity.NOT_SET;
   final CasSequenceObjectMap<SememeChronologyImpl<? extends SememeVersion<?>>> sememeMap;
   final Path                                                                   sememePath;

   //~--- constructors --------------------------------------------------------

   // For HK2
   private SememeProvider()
            throws IOException {
      try {
         sememePath = LookupService.getService(ConfigurationService.class)
                                   .getChronicleFolderPath()
                                   .resolve("sememe");

         if (!Files.exists(sememePath)) {
            databaseValidity = DatabaseValidity.MISSING_DIRECTORY;
         }

         loadRequired.set(!Files.exists(sememePath));
         Files.createDirectories(sememePath);
         LOG.info("Setting up sememe provider at " + sememePath.toAbsolutePath().toString());
         sememeMap = new CasSequenceObjectMap<>(new SememeSerializer(), sememePath, "seg.", ".sememe.map");
      } catch (Exception e) {
         LookupService.getService(SystemStatusService.class)
                      .notifyServiceConfigurationFailure("Cradle Commit Manager", e);
         throw e;
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void clearDatabaseValidityValue() {
      // Reset to enforce analysis
      databaseValidity = DatabaseValidity.NOT_SET;
   }

   @Override
   public <V extends SememeVersion> SememeServiceTyped<V> ofType(Class<V> versionType) {
      return new SememeTypeProvider<>(versionType, this);
   }

   @Override
   public void writeSememe(SememeChronology<?> sememeChronicle, SememeConstraints... constraints) {
      Arrays.stream(constraints).forEach((constraint) -> {
                        switch (constraint) {
                        case ONE_SEMEME_PER_COMPONENT:
                           ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeStart =
                              new ReferencedNidAssemblageSequenceSememeSequenceKey(
                                  sememeChronicle.getReferencedComponentNid(),
                                  sememeChronicle.getAssemblageSequence(),
                                  Integer.MIN_VALUE);  // yes
                           ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeEnd =
                              new ReferencedNidAssemblageSequenceSememeSequenceKey(
                                  sememeChronicle.getReferencedComponentNid(),
                                  sememeChronicle.getAssemblageSequence(),
                                  Integer.MAX_VALUE);  // no
                           NavigableSet<ReferencedNidAssemblageSequenceSememeSequenceKey> subset =
                              referencedNidAssemblageSequenceSememeSequenceMap.subSet(rcRangeStart,
                                                                                      rcRangeEnd);

                           if (!subset.isEmpty()) {
                              if (!subset.stream()
                                         .allMatch((value) -> value.sememeSequence ==
                                         sememeChronicle.getSememeSequence())) {
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
      assemblageSequenceSememeSequenceMap.add(new AssemblageSememeKey(sememeChronicle.getAssemblageSequence(),
            sememeChronicle.getSememeSequence()));
      inUseAssemblages.add(sememeChronicle.getAssemblageSequence());
      referencedNidAssemblageSequenceSememeSequenceMap.add(
          new ReferencedNidAssemblageSequenceSememeSequenceKey(sememeChronicle.getReferencedComponentNid(),
                sememeChronicle.getAssemblageSequence(),
                sememeChronicle.getSememeSequence()));
      sememeMap.put(sememeChronicle.getSememeSequence(), (SememeChronologyImpl<?>) sememeChronicle);
   }

   @PostConstruct
   private void startMe() {
      try {
         LOG.info("Loading sememeMap.");

         if (!loadRequired.get()) {
            LOG.info("Reading existing sememeMap.");

            boolean isPopulated = sememeMap.initialize();

            LOG.info("Reading existing SememeKeys.");

            try (DataInputStream in =
                  new DataInputStream(new BufferedInputStream(new FileInputStream(new File(sememePath.toFile(),
                                                                                           "assemblage-sememe.keys"))))) {
               int size = in.readInt();

               for (int i = 0; i < size; i++) {
                  int assemblageSequence = in.readInt();
                  int sequence           = in.readInt();

                  assemblageSequenceSememeSequenceMap.add(new AssemblageSememeKey(assemblageSequence, sequence));
                  inUseAssemblages.add(assemblageSequence);
               }
            }

            try (DataInputStream in =
                  new DataInputStream(new BufferedInputStream(new FileInputStream(new File(sememePath.toFile(),
                                                                                           "component-sememe.keys"))))) {
               int size = in.readInt();

               for (int i = 0; i < size; i++) {
                  int referencedNid      = in.readInt();
                  int assemblageSequence = in.readInt();
                  int sequence           = in.readInt();

                  referencedNidAssemblageSequenceSememeSequenceMap.add(
                      new ReferencedNidAssemblageSequenceSememeSequenceKey(referencedNid,
                            assemblageSequence,
                            sequence));
               }
            }

            if (isPopulated) {
               databaseValidity = DatabaseValidity.POPULATED_DIRECTORY;
            }
         }

         SememeSequenceSet statedGraphSequences = getSememeSequencesFromAssemblage(Get.identifierService()
                                                                                      .getConceptSequence(
                                                                                         Get.identifierService()
                                                                                               .getNidForUuids(
                                                                                                  TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getUuids())));

         LOG.info("Stated logic graphs: " + statedGraphSequences.size());

         SememeSequenceSet inferedGraphSequences = getSememeSequencesFromAssemblage(Get.identifierService()
                                                                                       .getConceptSequence(
                                                                                          Get.identifierService()
                                                                                                .getNidForUuids(
                                                                                                   TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getUuids())));

         LOG.info("Inferred logic graphs: " + inferedGraphSequences.size());
         LOG.info("Finished SememeProvider load.");
      } catch (Exception e) {
         LookupService.getService(SystemStatusService.class)
                      .notifyServiceConfigurationFailure("Cradle Commit Manager", e);
         throw new RuntimeException(e);
      }
   }

   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping SememeProvider pre-destroy. ");

      try {
         // Dan commented out this LOG statement because it is really slow...
         // log.info("sememeMap size: {}", sememeMap.getSize());
         LOG.info("writing sememe-map.");
         sememeMap.write();
         LOG.info("writing SememeKeys.");

         try (DataOutputStream out =
               new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(sememePath.toFile(),
                                                                                           "assemblage-sememe.keys"))))) {
            out.writeInt(assemblageSequenceSememeSequenceMap.size());

            for (AssemblageSememeKey key: assemblageSequenceSememeSequenceMap) {
               out.writeInt(key.assemblageSequence);
               out.writeInt(key.sememeSequence);
            }
         }

         try (DataOutputStream out =
               new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(sememePath.toFile(),
                                                                                           "component-sememe.keys"))))) {
            out.writeInt(referencedNidAssemblageSequenceSememeSequenceMap.size());

            for (ReferencedNidAssemblageSequenceSememeSequenceKey key:
                  referencedNidAssemblageSequenceSememeSequenceMap) {
               out.writeInt(key.referencedNid);
               out.writeInt(key.assemblageSequence);
               out.writeInt(key.sememeSequence);
            }
         }
      } catch (IOException e) {
         throw new RuntimeException(e);
      }

      LOG.info("Finished SememeProvider stop.");
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Stream<Integer> getAssemblageTypes() {
      return inUseAssemblages.stream();
   }

   @Override
   public Path getDatabaseFolder() {
      return sememePath;
   }

   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      return databaseValidity;
   }

   @Override
   public Stream<SememeChronology<? extends DescriptionSememe<?>>> getDescriptionsForComponent(int componentNid) {
      SememeSequenceSet sequences = getSememeSequencesForComponent(componentNid);
      IntFunction<SememeChronology<? extends DescriptionSememe<?>>> mapper =
         (int sememeSequence) -> (SememeChronology<? extends DescriptionSememe<?>>) getSememe(sememeSequence);

      return sequences.stream()
                      .filter((int sememeSequence) -> {
                                 Optional<? extends SememeChronology<?>> sememe = getOptionalSememe(sememeSequence);

                                 if (sememe.isPresent() && (sememe.get().getSememeType() == SememeType.DESCRIPTION)) {
                                    return true;
                                 }

                                 return false;
                              })
                      .mapToObj(mapper);
   }

   @Override
   public Optional<? extends SememeChronology<? extends SememeVersion<?>>> getOptionalSememe(int sememeSequence) {
      sememeSequence = Get.identifierService()
                          .getSememeSequence(sememeSequence);
      return sememeMap.get(sememeSequence);
   }

   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getParallelSememeStream() {
      return sememeMap.getParallelStream().map((s) -> {
                              return (SememeChronology<? extends SememeVersion<?>>) s;
                           });
   }

   @Override
   public SememeChronology<? extends SememeVersion<?>> getSememe(int sememeId) {
      sememeId = Get.identifierService()
                    .getSememeSequence(sememeId);
      return sememeMap.getQuick(sememeId);
   }

   @Override
   public boolean hasSememe(int sememeId) {
      if (sememeId < 0) {
         sememeId = Get.identifierService()
                       .getSememeSequence(sememeId);
      }

      return sememeMap.containsKey(sememeId);
   }

   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getSememeChronologyStream() {
      return sememeMap.getStream().map((s) -> {
                              return (SememeChronology<? extends SememeVersion<?>>) s;
                           });
   }

   @Override
   public int getSememeCount() {
      return sememeMap.getSize();
   }

   @Override
   public IntStream getSememeKeyParallelStream() {
      return sememeMap.getKeyParallelStream();
   }

   @Override
   public IntStream getSememeKeyStream() {
      return sememeMap.getKeyStream();
   }

   @Override
   public SememeSequenceSet getSememeSequencesForComponent(int componentNid) {
      return getSememeSequencesForComponentFromAssemblages(componentNid, null);
   }

   @Override
   public SememeSequenceSet getSememeSequencesForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence) {
      if (componentNid >= 0) {
         throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
      }

      assemblageConceptSequence = Get.identifierService()
                                     .getConceptSequence(assemblageConceptSequence);

      ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeStart =
         new ReferencedNidAssemblageSequenceSememeSequenceKey(componentNid,
                                                              assemblageConceptSequence,
                                                              Integer.MIN_VALUE);  // yes
      ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeEnd =
         new ReferencedNidAssemblageSequenceSememeSequenceKey(componentNid,
                                                              assemblageConceptSequence,
                                                              Integer.MAX_VALUE);  // no
      NavigableSet<ReferencedNidAssemblageSequenceSememeSequenceKey> referencedComponentRefexKeys =
         referencedNidAssemblageSequenceSememeSequenceMap.subSet(rcRangeStart,
                                                                 true,
                                                                 rcRangeEnd,
                                                                 true);
      SememeSequenceSet referencedComponentSet = SememeSequenceSet.of(referencedComponentRefexKeys.stream()
                                                                                                  .mapToInt(
                                                                                                     (ReferencedNidAssemblageSequenceSememeSequenceKey key) -> key.sememeSequence));

      return referencedComponentSet;
   }

   @Override
   public SememeSequenceSet getSememeSequencesForComponentFromAssemblages(int componentNid,
         Set<Integer> allowedAssemblageSequences) {
      if (componentNid >= 0) {
         throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
      }

      NavigableSet<ReferencedNidAssemblageSequenceSememeSequenceKey> assemblageSememeKeys =
         referencedNidAssemblageSequenceSememeSequenceMap.subSet(
             new ReferencedNidAssemblageSequenceSememeSequenceKey(componentNid,
                   Integer.MIN_VALUE,
                   Integer.MIN_VALUE),
             true,
             new ReferencedNidAssemblageSequenceSememeSequenceKey(componentNid,
                   Integer.MAX_VALUE,
                   Integer.MAX_VALUE),
             true);

      return SememeSequenceSet.of(assemblageSememeKeys.stream()
            .filter((ReferencedNidAssemblageSequenceSememeSequenceKey key) -> {
                       if ((allowedAssemblageSequences == null) ||
                           allowedAssemblageSequences.isEmpty() ||
                           allowedAssemblageSequences.contains(key.assemblageSequence)) {
                          return true;
                       }

                       return false;
                    })
            .mapToInt((ReferencedNidAssemblageSequenceSememeSequenceKey key) -> key.sememeSequence));
   }

   @Override
   public SememeSequenceSet getSememeSequencesForComponentsFromAssemblage(NidSet componentNidSet,
         final int assemblageConceptSequence) {
      if (assemblageConceptSequence < 0) {
         throw new IndexOutOfBoundsException("assemblageSequence must be >= 0. Found: " + assemblageConceptSequence);
      }

      SememeSequenceSet resultSet = new SememeSequenceSet();

      componentNidSet.stream().forEach((componentNid) -> {
                                 ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeStart =
                                    new ReferencedNidAssemblageSequenceSememeSequenceKey(componentNid,
                                                                                         assemblageConceptSequence,
                                                                                         Integer.MIN_VALUE);  // yes
                                 ReferencedNidAssemblageSequenceSememeSequenceKey rcRangeEnd =
                                    new ReferencedNidAssemblageSequenceSememeSequenceKey(componentNid,
                                                                                         assemblageConceptSequence,
                                                                                         Integer.MAX_VALUE);  // no
                                 NavigableSet<ReferencedNidAssemblageSequenceSememeSequenceKey> referencedComponentRefexKeys =
                                    referencedNidAssemblageSequenceSememeSequenceMap.subSet(rcRangeStart,
                                                                                            true,
                                                                                            rcRangeEnd,
                                                                                            true);

                                 referencedComponentRefexKeys.stream().forEach((key) -> {
                  resultSet.add(key.sememeSequence);
               });
                              });
      return resultSet;
   }

   @Override
   public SememeSequenceSet getSememeSequencesForComponentsFromAssemblageModifiedAfterPosition(NidSet componentNidSet,
         int assemblageConceptSequence,
         StampPosition position) {
      SememeSequenceSet sequencesToTest = getSememeSequencesForComponentsFromAssemblage(componentNidSet,
                                                                                        assemblageConceptSequence);
      SememeSequenceSet sequencesThatPassedTest = new SememeSequenceSet();

      sequencesToTest.stream().forEach((sememeSequence) -> {
                                 SememeChronologyImpl<?> chronicle =
                                    (SememeChronologyImpl<?>) getSememe(sememeSequence);

                                 if (chronicle.getVersionStampSequences().anyMatch((stampSequence) -> {
                  return ((Get.stampService().getTimeForStamp(stampSequence) > position.getTime()) &&
                          (position.getStampPathSequence() ==
                           Get.stampService().getPathSequenceForStamp(stampSequence)));
               })) {
                                    sequencesThatPassedTest.add(sememeSequence);
                                 }
                              });
      return sequencesThatPassedTest;
   }

   @Override
   public SememeSequenceSet getSememeSequencesFromAssemblage(int assemblageConceptSequence) {
      assemblageConceptSequence = Get.identifierService()
                                     .getConceptSequence(assemblageConceptSequence);

      AssemblageSememeKey rangeStart = new AssemblageSememeKey(assemblageConceptSequence, Integer.MIN_VALUE);  // yes
      AssemblageSememeKey rangeEnd = new AssemblageSememeKey(assemblageConceptSequence, Integer.MAX_VALUE);    // no
      NavigableSet<AssemblageSememeKey> assemblageSememeKeys = assemblageSequenceSememeSequenceMap.subSet(rangeStart,
                                                                                                          true,
                                                                                                          rangeEnd,
                                                                                                          true);

      return SememeSequenceSet.of(assemblageSememeKeys.stream()
            .mapToInt((AssemblageSememeKey key) -> key.sememeSequence));
   }

   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponent(int componentNid) {
      return getSememesForComponentFromAssemblages(componentNid, null);
   }

   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence) {
      if (componentNid >= 0) {
         componentNid = Get.identifierService()
                           .getConceptNid(componentNid);
      }

      if (assemblageConceptSequence < 0) {
         assemblageConceptSequence = Get.identifierService()
                                        .getConceptSequence(assemblageConceptSequence);
      }

      SememeSequenceSet sememeSequences = getSememeSequencesForComponentFromAssemblage(componentNid,
                                                                                       assemblageConceptSequence);

      return sememeSequences.stream()
                            .mapToObj((int sememeSequence) -> getSememe(sememeSequence));
   }

   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponentFromAssemblages(int componentNid,
         Set<Integer> allowedAssemblageSequences) {
      SememeSequenceSet sememeSequences = getSememeSequencesForComponentFromAssemblages(componentNid,
                                                                                        allowedAssemblageSequences);

      return sememeSequences.stream()
                            .mapToObj((int sememeSequence) -> getSememe(sememeSequence));
   }

   @Override
   public Stream<SememeChronology<? extends SememeVersion<?>>> getSememesFromAssemblage(int assemblageConceptSequence) {
      SememeSequenceSet sememeSequences = getSememeSequencesFromAssemblage(assemblageConceptSequence);

      return sememeSequences.stream()
                            .mapToObj((int sememeSequence) -> getSememe(sememeSequence));
   }

   @Override
   public <V extends SememeVersion> SememeSnapshotService<V> getSnapshot(Class<V> versionType,
         StampCoordinate stampCoordinate) {
      return new SememeSnapshotProvider<>(versionType, stampCoordinate, this);
   }
}

