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



package sh.isaac.provider.concept;

//~--- JDK imports ------------------------------------------------------------

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.text.ParseException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.ConceptActiveService;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.concept.ConceptSnapshotImpl;
import sh.isaac.model.waitfree.CasSequenceObjectMap;
import sh.isaac.provider.concept.ConceptSerializer;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 1)
public class ConceptProvider
         implements ConceptService {
   private static final Logger LOG                          = LogManager.getLogger();
   public static final String  CRADLE_PROPERTIES_FILE_NAME  = "cradle.properties";
   public static final String  CRADLE_ID_FILE_NAME          = "dbid.txt";
   public static final String  CRADLE_DATA_VERSION          = "1.5";
   public static final String  CRADLE_DATA_VERSION_PROPERTY = "cradle.data.version";

   //~--- fields --------------------------------------------------------------

   private final AtomicBoolean                             loadRequired     = new AtomicBoolean(true);
   private DatabaseValidity                          databaseValidity = DatabaseValidity.NOT_SET;
   private UUID                                      dbId             = null;
   ConceptActiveService                              conceptActiveService;
   final CasSequenceObjectMap<ConceptChronologyImpl> conceptMap;
   private Path                                      ochreConceptPath;

   //~--- constructors --------------------------------------------------------

   public ConceptProvider()
            throws IOException, NumberFormatException, ParseException {
      try {
         final Path propertiesPath = LookupService.getService(ConfigurationService.class)
                                            .getChronicleFolderPath()
                                            .resolve(CRADLE_PROPERTIES_FILE_NAME);
         final Path dbIdPath = LookupService.getService(ConfigurationService.class)
                                      .getChronicleFolderPath()
                                      .resolve(CRADLE_ID_FILE_NAME);
         final Path folderPath = LookupService.getService(ConfigurationService.class)
                                        .getChronicleFolderPath()
                                        .resolve("ochre-concepts");

         Files.createDirectories(folderPath);
         LOG.info("Setting up OCHRE ConceptProvider at " + folderPath.toAbsolutePath());

         final Properties cradleProps = new Properties();

         if (propertiesPath.toFile()
                           .exists()) {
            this.loadRequired.set(true);

            try (FileInputStream in = new FileInputStream(propertiesPath.toFile())) {
               cradleProps.load(in);
            }

            if (!cradleProps.getProperty(CRADLE_DATA_VERSION_PROPERTY)
                            .equals(CRADLE_DATA_VERSION)) {
               throw new IllegalStateException("Unsupported data version: " + cradleProps);
            }

            if (dbIdPath.toFile()
                        .exists()) {
               try {
                  this.dbId = UUID.fromString(new String(Files.readAllBytes(dbIdPath)));
               } catch (final Exception e) {
                  throw new IllegalStateException("The " + CRADLE_ID_FILE_NAME + " file does not contain a valid UUID!",
                                                  e);
               }
            } else {
               LOG.warn("The " + CRADLE_ID_FILE_NAME + " file is missing from the database folder - creating a new ID");
               this.dbId = UUID.randomUUID();
               Files.write(dbIdPath, this.dbId.toString()
                                         .getBytes());
            }

            LOG.info("Reading an existing concept store at " + folderPath.toAbsolutePath() + " with the id of '" +
                     this.dbId.toString() + "'");
         } else {
            this.loadRequired.set(false);
            cradleProps.put(CRADLE_DATA_VERSION_PROPERTY, CRADLE_DATA_VERSION);

            try (FileOutputStream out = new FileOutputStream(propertiesPath.toFile())) {
               cradleProps.store(out, CRADLE_DATA_VERSION);
            }

            this.dbId = UUID.randomUUID();
            Files.write(dbIdPath, this.dbId.toString()
                                      .getBytes());
            LOG.info("Creating a new (empty) concept store at " + folderPath.toAbsolutePath() + " with the id of ]" +
                     this.dbId.toString() + "'");
         }

         this.ochreConceptPath = folderPath.resolve("ochre");

         if (!Files.exists(this.ochreConceptPath)) {
            this.databaseValidity = DatabaseValidity.MISSING_DIRECTORY;
         }

         this.conceptMap = new CasSequenceObjectMap<>(new ConceptSerializer(),
               this.ochreConceptPath,
               "seg.",
               ".ochre-concepts.map");
      } catch (IOException | IllegalStateException e) {
         LookupService.getService(SystemStatusService.class)
                      .notifyServiceConfigurationFailure("ChRonicled Assertion Database of Logical Expressions (OCHRE)",
                            e);
         throw e;
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void clearDatabaseValidityValue() {
      // Reset to enforce analysis
      this.databaseValidity = DatabaseValidity.NOT_SET;
   }

   @Override
   public void writeConcept(ConceptChronology<? extends ConceptVersion<?>> concept) {
      this.conceptMap.put(concept.getConceptSequence(), (ConceptChronologyImpl) concept);
   }

   @PostConstruct
   private void startMe() {
      LOG.info("Starting OCHRE ConceptProvider post-construct");
      this.conceptActiveService = LookupService.getService(ConceptActiveService.class);

      if (this.loadRequired.compareAndSet(true, false)) {
         LOG.info("Reading existing OCHRE concept-map.");

         if (this.conceptMap.initialize()) {
            this.databaseValidity = DatabaseValidity.POPULATED_DIRECTORY;
         }

         LOG.info("Finished OCHRE read.");
      }
   }

   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping OCHRE ConceptProvider.");
      LOG.info("Writing OCHRE concept-map.");
      this.conceptMap.write();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public ConceptChronologyImpl getConcept(int conceptId) {
      if (conceptId < 0) {
         conceptId = Get.identifierService()
                        .getConceptSequence(conceptId);
      }

      return this.conceptMap.getQuick(conceptId);
   }

   @Override
   public ConceptChronologyImpl getConcept(UUID... conceptUuids) {
      final int                             conceptNid      = Get.identifierService()
                                                           .getNidForUuids(conceptUuids);
      final int                             conceptSequence = Get.identifierService()
                                                           .getConceptSequence(conceptNid);
      final Optional<ConceptChronologyImpl> optionalConcept = this.conceptMap.get(conceptSequence);

      if (optionalConcept.isPresent()) {
         return optionalConcept.get();
      }

      final ConceptChronologyImpl concept = new ConceptChronologyImpl(conceptUuids[0], conceptNid, conceptSequence);

      if (conceptUuids.length > 1) {
         concept.setAdditionalUuids(Arrays.asList(Arrays.copyOfRange(conceptUuids, 1, conceptUuids.length)));
      }

      this.conceptMap.put(conceptSequence, concept);
      return this.conceptMap.getQuick(conceptSequence);
   }

   @Override
   public boolean hasConcept(int conceptId) {
      if (conceptId < 0) {
         conceptId = Get.identifierService()
                        .getConceptSequence(conceptId);
      }

      return this.conceptMap.containsKey(conceptId);
   }

   @Override
   public boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate) {
      return this.conceptActiveService.isConceptActive(conceptSequence, stampCoordinate);
   }

   @Override
   public Stream<ConceptChronology<? extends ConceptVersion<?>>> getConceptChronologyStream() {
      return this.conceptMap.getStream().map((cc) -> {
                               return (ConceptChronology<? extends ConceptVersion<?>>) cc;
                            });
   }

   @Override
   public Stream<ConceptChronology<? extends ConceptVersion<?>>> getConceptChronologyStream(
           ConceptSequenceSet conceptSequences) {
      return Get.identifierService().getConceptSequenceStream().filter((int sequence) -> conceptSequences.contains(sequence)).mapToObj((int sequence) -> {
                             final Optional<ConceptChronologyImpl> result = this.conceptMap.get(sequence);

                             if (result.isPresent()) {
                                return this.conceptMap.get(sequence)
                                      .get();
                             }

                             throw new IllegalStateException("No concept for sequence: " + sequence);
                          });
   }

   @Override
   public int getConceptCount() {
      return this.conceptMap.getSize();
   }

   public Optional<ConceptChronologyImpl> getConceptData(int i)
            throws IOException {
      if (i < 0) {
         i = Get.identifierService()
                .getConceptSequence(i);
      }

      return this.conceptMap.get(i);
   }

   @Override
   public IntStream getConceptKeyParallelStream() {
      return this.conceptMap.getKeyParallelStream();
   }

   @Override
   public IntStream getConceptKeyStream() {
      return this.conceptMap.getKeyStream();
   }

   @Override
   public UUID getDataStoreId() {
      return this.dbId;
   }

   @Override
   public Path getDatabaseFolder() {
      return this.ochreConceptPath;
   }

   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      return this.databaseValidity;
   }

   @Override
   public Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> getOptionalConcept(int conceptId) {
      if (conceptId < 0) {
         conceptId = Get.identifierService()
                        .getConceptSequence(conceptId);
      }

      return this.conceptMap.get(conceptId);
   }

   @Override
   public Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> getOptionalConcept(UUID... conceptUuids) {
      // check hasUuid first, because getOptionalConcept adds the UUID to the index if it doesn't exist...
      if (Get.identifierService()
             .hasUuid(conceptUuids)) {
         return getOptionalConcept(Get.identifierService()
                                      .getConceptSequenceForUuids(conceptUuids));
      } else {
         return Optional.empty();
      }
   }

   @Override
   public Stream<ConceptChronology<? extends ConceptVersion<?>>> getParallelConceptChronologyStream() {
      return this.conceptMap.getParallelStream().map((cc) -> {
                               return cc;
                            });
   }

   @Override
   public Stream<ConceptChronology<? extends ConceptVersion<?>>> getParallelConceptChronologyStream(
           ConceptSequenceSet conceptSequences) {
      return Get.identifierService().getParallelConceptSequenceStream().filter((int sequence) -> conceptSequences.contains(sequence)).mapToObj((int sequence) -> {
                             final Optional<ConceptChronologyImpl> result = this.conceptMap.get(sequence);

                             if (result.isPresent()) {
                                return this.conceptMap.get(sequence)
                                      .get();
                             }

                             throw new IllegalStateException("No concept for sequence: " + sequence);
                          });
   }

   @Override
   public ConceptSnapshotService getSnapshot(StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate) {
      return new ConceptSnapshotProvider(stampCoordinate, languageCoordinate);
   }

   //~--- inner classes -------------------------------------------------------

   public class ConceptSnapshotProvider
            implements ConceptSnapshotService {
      StampCoordinate    stampCoordinate;
      LanguageCoordinate languageCoordinate;

      //~--- constructors -----------------------------------------------------

      public ConceptSnapshotProvider(StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate) {
         this.stampCoordinate    = stampCoordinate;
         this.languageCoordinate = languageCoordinate;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public String conceptDescriptionText(int conceptId) {
         final Optional<LatestVersion<DescriptionSememe<?>>> descriptionOptional = getDescriptionOptional(conceptId);

         if (descriptionOptional.isPresent()) {
            return descriptionOptional.get()
                                      .value()
                                      .getText();
         }

         return "No desc for: " + conceptId;
      }

      @Override
      public String toString() {
         return "ConceptSnapshotProvider{" + "stampCoordinate=" + this.stampCoordinate + ", languageCoordinate=" +
                this.languageCoordinate + '}';
      }

      //~--- get methods ------------------------------------------------------

      @Override
      public boolean isConceptActive(int conceptSequence) {
         return ConceptProvider.this.isConceptActive(conceptSequence, this.stampCoordinate);
      }

      @Override
      public ConceptSnapshot getConceptSnapshot(int conceptSequence) {
         return new ConceptSnapshotImpl(getConcept(conceptSequence), this.stampCoordinate, this.languageCoordinate);
      }

      private List<SememeChronology<? extends DescriptionSememe<?>>> getDescriptionList(int conceptId) {
         final int conceptNid = Get.identifierService()
                             .getConceptNid(conceptId);

         return Get.sememeService()
                   .getDescriptionsForComponent(conceptNid)
                   .collect(Collectors.toList());
      }

      @Override
      public Optional<LatestVersion<DescriptionSememe<?>>> getDescriptionOptional(int conceptId) {
         return this.languageCoordinate.getDescription(getDescriptionList(conceptId), this.stampCoordinate);
      }

      @Override
      public Optional<LatestVersion<DescriptionSememe<?>>> getFullySpecifiedDescription(int conceptId) {
         return this.languageCoordinate.getFullySpecifiedDescription(getDescriptionList(conceptId), this.stampCoordinate);
      }

      @Override
      public LanguageCoordinate getLanguageCoordinate() {
         return this.languageCoordinate;
      }

      @Override
      public Optional<LatestVersion<DescriptionSememe<?>>> getPreferredDescription(int conceptId) {
         return this.languageCoordinate.getPreferredDescription(getDescriptionList(conceptId), this.stampCoordinate);
      }

      @Override
      public StampCoordinate getStampCoordinate() {
         return this.stampCoordinate;
      }
   }
}

