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



package sh.isaac.provider.commit;

//~--- JDK imports ------------------------------------------------------------

import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.ChangeSetLoadService;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.metacontent.MetaContentService;
import sh.isaac.api.util.metainf.MetaInfReader;
import sh.isaac.model.configuration.EditCoordinates;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.api.component.sememe.version.StringVersion;

//~--- classes ----------------------------------------------------------------

/**
 * {@link ChangeSetLoadProvider}
 * This will load all .ibdf files in the database directory. It will rename the ChangeSet.ibdf
 * and ChangeSet.json files so they are not over written when ChangeSetWriterHandler starts.
 * Please make sure only files to be loaded are in this directory for loading at application startup.
 * The database directory the parent directory of the value returned from
 * LookupService.getService(ConfigurationService.class).getDataStoreFolderPath();
 * ChangeSetWritterHandler must have a RunLevel greater than the value of ChangeSetLoadProvider
 * otherwise the file ChangeSetWriterHandler will overwrite and lock the ChangeSet files.
 *
 * @author <a href="mailto:nmarques@westcoastinformatics.com">Nuno Marques</a>
 */
@Service
@RunLevel(value = 3)
public class ChangeSetLoadProvider
         implements ChangeSetLoadService {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   /** The Constant CHANGESETS. */
   private static final String CHANGESETS = "changesets";

   /** The Constant CHANGESETS_ID. */
   private static final String CHANGESETS_ID = "changesetId.txt";

   /** The Constant MAVEN_ARTIFACT_IDENTITY. */
   private static final String MAVEN_ARTIFACT_IDENTITY = "dbMavenArtifactIdentity.txt";

   /** The database path. */
   private static Optional<Path> databasePath;

   //~--- fields --------------------------------------------------------------

   /** The changeset path. */
   private Path changesetPath;

   /** The processed changesets. */
   private ConcurrentMap<String, Boolean> processedChangesets;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new change set load provider.
    */

   // For HK2
   private ChangeSetLoadProvider() {}

   //~--- methods -------------------------------------------------------------

   /**
    * Read changeset files.
    *
    * @return the int
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Override
   public int readChangesetFiles()
            throws IOException {
      final AtomicInteger loaded  = new AtomicInteger();
      final AtomicInteger skipped = new AtomicInteger();

      LOG.debug("Looking for .ibdf file in {}.", this.changesetPath.toAbsolutePath());

      final CommitService commitService = Get.commitService();

      Files.newDirectoryStream(this.changesetPath, path -> path.toFile().isFile() && path.toString().endsWith(".ibdf"))
           .forEach(path -> {
                       LOG.debug("File {}", path.toAbsolutePath());

                       try {
                          if ((this.processedChangesets !=
                               null) && this.processedChangesets.containsKey(path.getFileName().toString())) {
                             skipped.incrementAndGet();
                             LOG.debug("Skipping already processed changeset file");
                          } else {
                             loaded.incrementAndGet();
                             LOG.debug("Importing changeset file");
                             Get.binaryDataReader(path).getStream().forEach(o -> {
                                            commitService.importNoChecks(o);
                                         });
                             commitService.postProcessImportNoChecks();

                             if (this.processedChangesets != null) {
                                this.processedChangesets.put(path.getFileName()
                                      .toString(), true);
                             }
                          }
                       } catch (final FileNotFoundException e) {
                          LOG.error("Change Set Load Provider failed to load file {}", path.toAbsolutePath());
                          throw new RuntimeException(e);
                       }
                    });
      LOG.info("Finished Change Set Load Provider load.  Loaded {}, Skipped {} because they were previously processed",
               loaded.get(),
               skipped.get());
      return loaded.get();
   }

   /**
    * Read sememe db id.
    *
    * @return the uuid
    */
   private UUID readSememeDbId() {
      final Optional<SememeChronology> sdic = Get.assemblageService()
                                                                             .getSememesForComponentFromAssemblage(
                                                                                TermAux.SOLOR_ROOT.getNid(),
                                                                                      TermAux.DATABASE_UUID.getConceptSequence())
                                                                             .findFirst();

      if (sdic.isPresent()) {
         final LatestVersion<StringVersion> sdi =
            ((SememeChronology) sdic.get()).getLatestVersion(StampCoordinates.getDevelopmentLatest());

         if (sdi.isPresent()) {
            try {
               return UUID.fromString(sdi.get()
                                         .getString());
            } catch (final Exception e) {
               LOG.warn("The Database UUID annotation on Isaac Root does not contain a valid UUID!", e);
            }
         }
      }

      return null;
   }

   /**
    * Start me.
    */
   @PostConstruct
   private void startMe() {
      try {
         LOG.info("Loading change set files.");
         databasePath       = LookupService.getService(ConfigurationService.class)
                                           .getDataStoreFolderPath();
         this.changesetPath = databasePath.get()
                                          .resolve(CHANGESETS);
         Files.createDirectories(this.changesetPath);

         if (!this.changesetPath.toFile()
                                .isDirectory()) {
            throw new RuntimeException("Cannot initialize Changeset Store - was unable to create " +
                                       this.changesetPath.toAbsolutePath());
         }

         final UUID chronicleDbId = Get.conceptService()
                                       .getDataStoreId();

         if (chronicleDbId == null) {
            throw new RuntimeException("Chronicle store did not return a dbId!");
         }

         UUID       changesetsDbId   = null;
         final Path changesetsIdPath = this.changesetPath.resolve(CHANGESETS_ID);

         if (changesetsIdPath.toFile()
                             .exists()) {
            try {
               changesetsDbId = UUID.fromString(new String(Files.readAllBytes(changesetsIdPath)));
            } catch (final IOException e) {
               LOG.warn("The " + CHANGESETS_ID + " file does not contain a valid UUID!", e);
            }
         }

         try {
            final Path mavenMetadataIdentityPath = this.changesetPath.resolve(MAVEN_ARTIFACT_IDENTITY);

            if (!mavenMetadataIdentityPath.toFile()
                                          .exists()) {
               // write out this file as a debugging aid - when browsing git, can easily go from a changeset repo back to the maven artifact of the db
               Files.write(mavenMetadataIdentityPath, MetaInfReader.readDbMetadata()
                     .toString()
                     .getBytes());
            }
         } catch (final IOException e) {
            LOG.error("Error writing maven artifact identity file", e);
         }

         UUID sememeDbId = readSememeDbId();

         if (((sememeDbId != null) &&!sememeDbId.equals(chronicleDbId)) ||
               ((changesetsDbId != null) &&!changesetsDbId.equals(chronicleDbId))) {
            final StringBuilder msg = new StringBuilder();

            msg.append("Database identity mismatch!  ChronicleDbId: ")
               .append(chronicleDbId);
            msg.append(" SememeDbId: ")
               .append(sememeDbId);
            msg.append(" Changsets DbId: ")
               .append(changesetsDbId);
            throw new RuntimeException(msg.toString());
         }

         if (changesetsDbId == null) {
            changesetsDbId = chronicleDbId;
            Files.write(changesetsIdPath, changesetsDbId.toString()
                  .getBytes());
         }

         // if the sememeDbId is null, lets wait and see if it appears after processing the changesets.
         // We store the list of files that we have already read / processed in the metacontent store, so we don't have to process them again.
         // files that "appear" in this folder via the git integration, for example, we will need to process - but files that we create
         // during normal operation do not need to be reprocessed.  The BinaryDataWriterProvider also automatically updates this list with the
         // files as it writes them.
         final MetaContentService mcs = LookupService.get()
                                                     .getService(MetaContentService.class);

         this.processedChangesets = (mcs == null) ? null
               : mcs.<String, Boolean>openStore("processedChangesets");

         final int loaded = readChangesetFiles();

         if (sememeDbId == null) {
            sememeDbId = readSememeDbId();

            if (!Get.configurationService().inDBBuildMode() && (sememeDbId == null)) {
               if (loaded > 0) {
                  LOG.warn("No database identify was found stored in a sememe, after loading changesets.");
               }

               Get.sememeBuilderService()
                  .getStringSememeBuilder(chronicleDbId.toString(),
                                          TermAux.SOLOR_ROOT.getNid(),
                                          TermAux.DATABASE_UUID.getConceptSequence())
                  .build(EditCoordinates.getDefaultUserMetadata(), ChangeCheckerMode.ACTIVE)
                  .get();
               Get.commitService()
                  .commit("Storing database ID on root concept");
            }
         }
      } catch (final IOException | InterruptedException | RuntimeException | ExecutionException e) {
         LOG.error("Error ", e);
         LookupService.getService(SystemStatusService.class)
                      .notifyServiceConfigurationFailure("Change Set Load Provider", e);
         throw new RuntimeException(e);
      }
   }

   /**
    * Stop me.
    */
   @PreDestroy
   private void stopMe() {
      LOG.info("Finished ChangeSet Load Provider pre-destory.");
   }
}

