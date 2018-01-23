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
package sh.isaac.solor.rf2.direct;

import java.time.format.DateTimeFormatter;
import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import org.apache.logging.log4j.LogManager;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.index.IndexQueryService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;

/**
 * The concept data populates a concept as well as a legacy definition state assemblage, and sct identifier assemblage.
 *
 * @author kec
 */
public class ConceptWriter extends TimedTaskWithProgressTracker<Void> {

   /*
id	effectiveTime	active	moduleId	definitionStatusId
100005	20020131	0	900000000000207008	900000000000074008
101009	20020131	1	900000000000207008	900000000000074008
102002	20020131	1	900000000000207008	900000000000074008
    */

   private static final int CONCEPT_SCT_ID_INDEX = 0;
   private static final int EFFECTIVE_TIME_INDEX = 1;
   private static final int ACTIVE_INDEX = 2; // 0 == false, 1 == true
   private static final int MODULE_SCTID_INDEX = 3;
   private static final int DEF_STATUS_INDEX = 4; // primitive or defined

   private final List<String[]> conceptRecords;
   private final Semaphore writeSemaphore;
   private final List<IndexBuilderService> indexers;
   private final ImportType importType;

   public ConceptWriter(List<String[]> conceptRecords, Semaphore writeSemaphore, 
           String message, ImportType importType) {
      this.conceptRecords = conceptRecords;
      this.writeSemaphore = writeSemaphore;
      this.writeSemaphore.acquireUninterruptibly();
      indexers = LookupService.get().getAllServices(IndexBuilderService.class);
      updateTitle("Importing concept batch of size: " + conceptRecords.size());
      updateMessage(message);
      addToTotalWork(conceptRecords.size());
      this.importType = importType;
      Get.activeTasks().add(this);
   }
   protected static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger();
   private void index(Chronology chronicle) {
      for (IndexBuilderService indexer: indexers) {
         try {
            indexer.index(chronicle).get();
         } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex);
         }
      }
   }

   @Override
   protected Void call() throws Exception {
      try {
         ConceptService conceptService = Get.conceptService();
         AssemblageService assemblageService = Get.assemblageService();
         IdentifierService identifierService = Get.identifierService();
         StampService stampService = Get.stampService();
         int conceptAssemblageNid = identifierService.getNidForProxy(TermAux.SOLOR_CONCEPT_ASSEMBLAGE);
         int sctIdentifierAssemblageNid = TermAux.SNOMED_IDENTIFIER.getNid();
         int authorNid = TermAux.USER.getNid();
         int pathNid = TermAux.DEVELOPMENT_PATH.getNid();
         int defStatusAssemblageNid = TermAux.RF2_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE.getNid();

         for (String[] conceptRecord : conceptRecords) {
            final Status state = Status.fromZeroOneToken(conceptRecord[ACTIVE_INDEX]);
            if (state == Status.INACTIVE && importType == ImportType.ACTIVE_ONLY) {
                continue;
            }
            UUID conceptUuid = UuidT3Generator.fromSNOMED(conceptRecord[CONCEPT_SCT_ID_INDEX]);
            UUID moduleUuid = UuidT3Generator.fromSNOMED(conceptRecord[MODULE_SCTID_INDEX]);
            UUID legacyDefStatus = UuidT3Generator.fromSNOMED(conceptRecord[DEF_STATUS_INDEX]);
            // '2011-12-03T10:15:30Z'

            TemporalAccessor accessor = DateTimeFormatter.ISO_INSTANT.parse(
                    Rf2DirectImporter.getIsoInstant(conceptRecord[EFFECTIVE_TIME_INDEX]));
            long time = accessor.getLong(INSTANT_SECONDS) * 1000;
            
            // add to concept assemblage
            int moduleNid = identifierService.getNidForUuids(moduleUuid);
            int legacyDefStatusNid = identifierService.getNidForUuids(legacyDefStatus);
            
            ConceptChronologyImpl conceptToWrite = new ConceptChronologyImpl(conceptUuid, conceptAssemblageNid);
            index(conceptToWrite);
            int conceptStamp = stampService.getStampSequence(state, time, authorNid, moduleNid, pathNid);
            conceptToWrite.createMutableVersion(conceptStamp);
            conceptService.writeConcept(conceptToWrite);
            
            // add to legacy def status assemblage
            UUID defStatusPrimordialUuid = UuidT5Generator.get(TermAux.RF2_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE.getPrimordialUuid(), 
                    conceptRecord[CONCEPT_SCT_ID_INDEX]);
            SemanticChronologyImpl defStatusToWrite = new SemanticChronologyImpl(VersionType.COMPONENT_NID,
                               defStatusPrimordialUuid,
                               defStatusAssemblageNid,
                               conceptToWrite.getNid());
                               
            ComponentNidVersionImpl defStatusVersion = defStatusToWrite.createMutableVersion(conceptStamp);
            defStatusVersion.setComponentNid(legacyDefStatusNid);
            index(defStatusToWrite);
            assemblageService.writeSemanticChronology(defStatusToWrite);
            
            // add to sct identifier assemblage
            UUID sctIdentifierUuid = UuidT5Generator.get(TermAux.SNOMED_IDENTIFIER.getPrimordialUuid(), 
                    conceptRecord[CONCEPT_SCT_ID_INDEX]);
            SemanticChronologyImpl sctIdentifierToWrite = new SemanticChronologyImpl(VersionType.STRING,
                               sctIdentifierUuid,
                               sctIdentifierAssemblageNid,
                               conceptToWrite.getNid());
            
            StringVersionImpl sctIdVersion = sctIdentifierToWrite.createMutableVersion(conceptStamp);
            sctIdVersion.setString(conceptRecord[CONCEPT_SCT_ID_INDEX]);
            index(sctIdentifierToWrite);
            assemblageService.writeSemanticChronology(sctIdentifierToWrite);
            completedUnitOfWork();
         }

         return null;
      } finally {
         this.writeSemaphore.release();
         for (IndexBuilderService indexer : indexers) {
            indexer.sync().get();
         }
         Get.activeTasks().remove(this);
      }
   }
}
