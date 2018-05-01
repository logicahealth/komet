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
package sh.isaac.solor.direct;

import java.time.format.DateTimeFormatter;
import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import org.apache.logging.log4j.LogManager;
import sh.isaac.MetaData;
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

   private static final int RF2_CONCEPT_SCT_ID_INDEX = 0;
   private static final int RF2_EFFECTIVE_TIME_INDEX = 1;
   private static final int RF2_ACTIVE_INDEX = 2; // 0 == false, 1 == true
   private static final int RF2_MODULE_SCTID_INDEX = 3;
   private static final int RF2_DEF_STATUS_INDEX = 4; // primitive or defined

   private static final int SRF_ID_INDEX = 0;
   private static final int SRF_STATUS_INDEX = 1;
   private static final int SRF_TIME_INDEX = 2;
   private static final int SRF_AUTHOR_INDEX = 3;
   private static final int SRF_MODULE_INDEX = 4;
   private static final int SRF_PATH_INDEX = 5;
   private static final int SRF_DEF_STATUS_INDEX = 6;

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
         indexer.indexNow(chronicle);
      }
   }

   @Override
   protected Void call() throws Exception {
      try {
         ConceptService conceptService = Get.conceptService();
         AssemblageService assemblageService = Get.assemblageService();
         IdentifierService identifierService = Get.identifierService();
         StampService stampService = Get.stampService();
         int conceptAssemblageNid, identifierAssemblageNid, defStatusAssemblageNid;
         int authorNid = 1;   //TODO need to initialize them, rework the Logic AKS
         int pathNid = 1;     //TODO need to initialize them, rework the Logic AKS

         if(DirectImporter.SRF_IMPORT){
            conceptAssemblageNid = TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid();
            identifierAssemblageNid = MetaData.UUID____SOLOR.getNid();
            defStatusAssemblageNid = TermAux.RF2_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE.getNid();
         }else{
            conceptAssemblageNid = TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid();
            identifierAssemblageNid = TermAux.SNOMED_IDENTIFIER.getNid();
            authorNid = TermAux.USER.getNid();
            pathNid = TermAux.DEVELOPMENT_PATH.getNid();
            defStatusAssemblageNid = TermAux.RF2_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE.getNid();
         }

         for (String[] conceptRecord : conceptRecords) {
            final Status state = DirectImporter.SRF_IMPORT
                    ? Status.fromZeroOneToken(conceptRecord[SRF_STATUS_INDEX])
                    : Status.fromZeroOneToken(conceptRecord[RF2_ACTIVE_INDEX]);
            if (state == Status.INACTIVE && importType == ImportType.ACTIVE_ONLY) {
                continue;
            }

            UUID conceptUuid, moduleUuid, legacyDefStatus;
            TemporalAccessor accessor;

            if(DirectImporter.SRF_IMPORT){
               authorNid = identifierService.getNidForUuids(UUID.fromString(conceptRecord[SRF_AUTHOR_INDEX]));
               pathNid = identifierService.getNidForUuids(UUID.fromString(conceptRecord[SRF_PATH_INDEX]));
               conceptUuid = UUID.fromString(conceptRecord[SRF_ID_INDEX]);
               moduleUuid = UUID.fromString(conceptRecord[SRF_MODULE_INDEX]);
               legacyDefStatus = UUID.fromString(conceptRecord[SRF_DEF_STATUS_INDEX]);
               accessor = DateTimeFormatter.ISO_INSTANT.parse(
                       DirectImporter.getIsoInstant(conceptRecord[SRF_TIME_INDEX]));
            }else{
               conceptUuid = UuidT3Generator.fromSNOMED(conceptRecord[RF2_CONCEPT_SCT_ID_INDEX]);
               moduleUuid = UuidT3Generator.fromSNOMED(conceptRecord[RF2_MODULE_SCTID_INDEX]);
               legacyDefStatus = UuidT3Generator.fromSNOMED(conceptRecord[RF2_DEF_STATUS_INDEX]);
               accessor = DateTimeFormatter.ISO_INSTANT.parse(
                       DirectImporter.getIsoInstant(conceptRecord[RF2_EFFECTIVE_TIME_INDEX]));
            }

            long time = accessor.getLong(INSTANT_SECONDS) * 1000;

            // add to concept assemblage
            int moduleNid = identifierService.assignNid(moduleUuid);
            int legacyDefStatusNid = identifierService.assignNid(legacyDefStatus);

            ConceptChronologyImpl conceptToWrite = new ConceptChronologyImpl(conceptUuid, conceptAssemblageNid);
            index(conceptToWrite);
            int conceptStamp = stampService.getStampSequence(state, time, authorNid, moduleNid, pathNid);
            conceptToWrite.createMutableVersion(conceptStamp);
            conceptService.writeConcept(conceptToWrite);

            // add to legacy def status assemblage
            UUID defStatusPrimordialUuid;

            if(DirectImporter.SRF_IMPORT){
               defStatusPrimordialUuid = UuidT5Generator.get(TermAux.SRF_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE.getPrimordialUuid(),
                       conceptRecord[SRF_ID_INDEX]);
            }else{
               defStatusPrimordialUuid = UuidT5Generator.get(TermAux.RF2_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE.getPrimordialUuid(),
                       conceptRecord[RF2_CONCEPT_SCT_ID_INDEX]);
            }

            SemanticChronologyImpl defStatusToWrite = new SemanticChronologyImpl(VersionType.COMPONENT_NID,
                               defStatusPrimordialUuid,
                               defStatusAssemblageNid,
                               conceptToWrite.getNid());

            ComponentNidVersionImpl defStatusVersion = defStatusToWrite.createMutableVersion(conceptStamp);
            defStatusVersion.setComponentNid(legacyDefStatusNid);
            index(defStatusToWrite);
            assemblageService.writeSemanticChronology(defStatusToWrite);

            // add to sct identifier assemblage
            UUID identifierUuid;

            if(DirectImporter.SRF_IMPORT){
               identifierUuid = UuidT5Generator.get(MetaData.UUID____SOLOR.getPrimordialUuid(),
                       conceptRecord[SRF_ID_INDEX]);
            }else{
               identifierUuid = UuidT5Generator.get(TermAux.SNOMED_IDENTIFIER.getPrimordialUuid(),
                       conceptRecord[RF2_CONCEPT_SCT_ID_INDEX]);
            }

            SemanticChronologyImpl identifierToWrite = new SemanticChronologyImpl(VersionType.STRING,
                               identifierUuid,
                               identifierAssemblageNid,
                               conceptToWrite.getNid());

            StringVersionImpl idVersion = identifierToWrite.createMutableVersion(conceptStamp);
            idVersion.setString(DirectImporter.SRF_IMPORT ? conceptRecord[SRF_ID_INDEX]: conceptRecord[RF2_CONCEPT_SCT_ID_INDEX]);
            index(identifierToWrite);
            assemblageService.writeSemanticChronology(identifierToWrite);
            completedUnitOfWork();
         }

         return null;
      } finally {
         this.writeSemaphore.release();
         Get.activeTasks().remove(this);
      }
   }
}
