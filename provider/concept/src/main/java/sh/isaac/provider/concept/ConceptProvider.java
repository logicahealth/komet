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
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.concept.ConceptSnapshotImpl;
import sh.isaac.model.waitfree.CasSequenceObjectMap;
import sh.isaac.api.component.sememe.version.DescriptionVersion;
import sh.isaac.model.observable.ObservableConceptChronologyImpl;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ConceptProvider.
 *
 * @author kec
 */
@Service
@RunLevel(value = 1)
public class ConceptProvider
         implements ConceptService {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   /** The Constant CRADLE_PROPERTIES_FILE_NAME. */
   public static final String CRADLE_PROPERTIES_FILE_NAME = "cradle.properties";

   /** The Constant CRADLE_ID_FILE_NAME. */
   public static final String CRADLE_ID_FILE_NAME = "dbid.txt";

   /** The Constant CRADLE_DATA_VERSION. We must use strings for integers  > 0 that Byte.parseByte() can parse for future data versions. */
   public static final String CRADLE_DATA_VERSION = "1.5";

   /** The Constant CRADLE_DATA_VERSION_PROPERTY. */
   public static final String CRADLE_DATA_VERSION_PROPERTY = "cradle.data.version";

   //~--- fields --------------------------------------------------------------

   /** The load required. */
   private final AtomicBoolean loadRequired = new AtomicBoolean(true);

   /** The database validity. */
   private DatabaseValidity databaseValidity = DatabaseValidity.NOT_SET;

   /** The db id. */
   private UUID dbId = null;

   /** The concept active service. */
   ConceptActiveService conceptActiveService;

   /** The concept map. */
   final CasSequenceObjectMap<ConceptChronologyImpl> conceptMap;

   /** The ochre concept path. */
   private Path ochreConceptPath;
   
   private final byte dataVersion;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new concept provider.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws NumberFormatException the number format exception
    * @throws ParseException the parse exception
    */
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
               } catch (final IOException e) {
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
            LOG.info("Creating a new (empty) concept store at " + folderPath.toAbsolutePath() + " with the id of '" +
                     this.dbId.toString() + "'");
         }

         this.ochreConceptPath = folderPath.resolve("ochre");

         if (!Files.exists(this.ochreConceptPath)) {
            this.databaseValidity = DatabaseValidity.MISSING_DIRECTORY;
         }
         
         if (cradleProps.getProperty(CRADLE_DATA_VERSION_PROPERTY).equals("1.5")) {
            dataVersion = 0;
         } else {
            dataVersion = Byte.parseByte(cradleProps.getProperty(CRADLE_DATA_VERSION_PROPERTY));
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

   /**
    * Clear database validity value.
    */
   @Override
   public void clearDatabaseValidityValue() {
      // Reset to enforce analysis
      this.databaseValidity = DatabaseValidity.NOT_SET;
   }

   /**
    * Write concept.
    *
    * @param concept the concept
    */
   @Override
   public void writeConcept(ConceptChronology concept) {
      if (concept instanceof ObservableConceptChronologyImpl) {
         concept = ((ObservableConceptChronologyImpl) concept).getConceptChronology();
      }
      this.conceptMap.put(concept.getConceptSequence(), (ConceptChronologyImpl) concept);
   }

   /**
    * Start me.
    */
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

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Stopping OCHRE ConceptProvider.");
      LOG.info("Writing OCHRE concept-map.");
      this.conceptMap.write();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept.
    *
    * @param conceptId the concept id
    * @return the concept
    */
   @Override
   public ConceptChronologyImpl getConcept(int conceptId) {
      if (conceptId < 0) {
         conceptId = Get.identifierService()
                        .getConceptSequence(conceptId);
      }

      return this.conceptMap.getQuick(conceptId);
   }

   /**
    * Gets the concept.
    *
    * @param conceptUuids the concept uuids
    * @return the concept
    */
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

   /**
    * Checks for concept.
    *
    * @param conceptId the concept id
    * @return true, if successful
    */
   @Override
   public boolean hasConcept(int conceptId) {
      if (conceptId < 0) {
         conceptId = Get.identifierService()
                        .getConceptSequence(conceptId);
      }

      return this.conceptMap.containsKey(conceptId);
   }

   /**
    * Checks if concept active.
    *
    * @param conceptSequence the concept sequence
    * @param stampCoordinate the stamp coordinate
    * @return true, if concept active
    */
   @Override
   public boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate) {
      return this.conceptActiveService.isConceptActive(conceptSequence, stampCoordinate);
   }

   /**
    * Gets the concept chronology stream.
    *
    * @return the concept chronology stream
    */
   @Override
   public Stream<ConceptChronology> getConceptChronologyStream() {
      return this.conceptMap.getStream().map((cc) -> {
                                    return (ConceptChronology) cc;
                                 });
   }

   /**
    * Gets the concept chronology stream.
    *
    * @param conceptSequences the concept sequences
    * @return the concept chronology stream
    */
   @Override
   public Stream<ConceptChronology> getConceptChronologyStream(
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

   /**
    * Gets the concept count.
    *
    * @return the concept count
    */
   @Override
   public int getConceptCount() {
      return this.conceptMap.getSize();
   }

   /**
    * Gets the concept data.
    *
    * @param i the i
    * @return the concept data
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public Optional<ConceptChronologyImpl> getConceptData(int i)
            throws IOException {
      if (i < 0) {
         i = Get.identifierService()
                .getConceptSequence(i);
      }

      return this.conceptMap.get(i);
   }

   /**
    * Gets the concept key parallel stream.
    *
    * @return the concept key parallel stream
    */
   @Override
   public IntStream getConceptKeyParallelStream() {
      return this.conceptMap.getKeyParallelStream();
   }

   /**
    * Gets the concept key stream.
    *
    * @return the concept key stream
    */
   @Override
   public IntStream getConceptKeyStream() {
      return this.conceptMap.getKeyStream();
   }

   /**
    * Gets the data store id.
    *
    * @return the data store id
    */
   @Override
   public UUID getDataStoreId() {
      return this.dbId;
   }

   /**
    * Gets the database folder.
    *
    * @return the database folder
    */
   @Override
   public Path getDatabaseFolder() {
      return this.ochreConceptPath;
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
    * Gets the optional concept.
    *
    * @param conceptId the concept id
    * @return the optional concept
    */
   @Override
   public Optional<? extends ConceptChronology> getOptionalConcept(int conceptId) {
      if (conceptId < 0) {
         conceptId = Get.identifierService()
                        .getConceptSequence(conceptId);
      }

      return this.conceptMap.get(conceptId);
   }

   /**
    * Gets the optional concept.
    *
    * @param conceptUuids the concept uuids
    * @return the optional concept
    */
   @Override
   public Optional<? extends ConceptChronology> getOptionalConcept(UUID... conceptUuids) {
      // check hasUuid first, because getOptionalConcept adds the UUID to the index if it doesn't exist...
      if (Get.identifierService()
             .hasUuid(conceptUuids)) {
         return getOptionalConcept(Get.identifierService()
                                      .getConceptSequenceForUuids(conceptUuids));
      } else {
         return Optional.empty();
      }
   }

   /**
    * Gets the parallel concept chronology stream.
    *
    * @return the parallel concept chronology stream
    */
   @Override
   public Stream<ConceptChronology> getParallelConceptChronologyStream() {
      return this.conceptMap.getParallelStream().map((cc) -> {
                                    return cc;
                                 });
   }

   /**
    * Gets the parallel concept chronology stream.
    *
    * @param conceptSequences the concept sequences
    * @return the parallel concept chronology stream
    */
   @Override
   public Stream<ConceptChronology> getParallelConceptChronologyStream(
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

   /**
    * Gets the snapshot.
    *
    * @param manifoldCoordinate the stamp coordinate
    * @return the sh.isaac.api.component.concept.ConceptSnapshotService
    */
   @Override
   public ConceptSnapshotService getSnapshot(sh.isaac.api.coordinate.ManifoldCoordinate manifoldCoordinate) {
      return new ConceptSnapshotProvider(manifoldCoordinate);
   }

   @Override
   public ConceptChronology getConcept(ConceptSpecification conceptSpecification) {
      return getConcept(conceptSpecification.getConceptSequence());
   }

   //~--- inner classes -------------------------------------------------------

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
         return ConceptProvider.this.isConceptActive(conceptSequence, this.manifoldCoordinate);
      }

      /**
       * Gets the concept snapshot.
       *
       * @param conceptSequence the concept sequence
       * @return the concept snapshot
       */
      @Override
      public ConceptSnapshot getConceptSnapshot(int conceptSequence) {
         return new ConceptSnapshotImpl(getConcept(conceptSequence), this.manifoldCoordinate);
      }

      /**
       * Gets the description list.
       *
       * @param conceptId the concept id
       * @return the description list
       */
      private List<SememeChronology> getDescriptionList(int conceptId) {
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

