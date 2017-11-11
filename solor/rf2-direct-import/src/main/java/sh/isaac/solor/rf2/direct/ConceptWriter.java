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
import java.util.concurrent.Semaphore;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptService;
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

   public ConceptWriter(List<String[]> conceptRecords, Semaphore writeSemaphore, String message) {
      this.conceptRecords = conceptRecords;
      this.writeSemaphore = writeSemaphore;
      this.writeSemaphore.acquireUninterruptibly();
      updateTitle("Importing concept batch of size: " + conceptRecords.size());
      updateMessage(message);
      addToTotalWork(conceptRecords.size());
      Get.activeTasks().add(this);
   }

   @Override
   protected Void call() throws Exception {
      try {
         ConceptService conceptService = Get.conceptService();
         AssemblageService assemblageService = Get.assemblageService();
         IdentifierService identifierService = Get.identifierService();
         StampService stampService = Get.stampService();
         int conceptAssemblageNid = identifierService.getNidForProxy(TermAux.SOLOR_CONCEPT_ASSEMBLAGE);
         int sctIdentifierAssemblageNid = TermAux.SCT_IDENTIFIER_ASSEMBLAGE.getNid();
         int authorNid = TermAux.USER.getNid();
         int pathNid = TermAux.MASTER_PATH.getNid();
         int defStatusAssemblageNid = TermAux.RF2_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE.getNid();

         for (String[] conceptRecord : conceptRecords) {
            UUID conceptUuid = UuidT3Generator.fromSNOMED(conceptRecord[CONCEPT_SCT_ID_INDEX]);
            UUID moduleUuid = UuidT3Generator.fromSNOMED(conceptRecord[MODULE_SCTID_INDEX]);
            Status state = Status.fromZeroOneToken(conceptRecord[ACTIVE_INDEX]);
            UUID legacyDefStatus = UuidT3Generator.fromSNOMED(conceptRecord[DEF_STATUS_INDEX]);
            // '2011-12-03T10:15:30Z'

            TemporalAccessor accessor = DateTimeFormatter.ISO_INSTANT.parse(
                    Rf2DirectImporter.getIsoInstant(conceptRecord[EFFECTIVE_TIME_INDEX]));
            long time = accessor.getLong(INSTANT_SECONDS) * 1000;
            
            // add to concept assemblage
            int conceptNid = identifierService.getNidForUuids(conceptUuid);
            int moduleNid = identifierService.getNidForUuids(moduleUuid);
            int legacyDefStatusNid = identifierService.getNidForUuids(legacyDefStatus);
            
            ConceptChronologyImpl conceptToWrite = new ConceptChronologyImpl(conceptUuid, conceptNid, conceptAssemblageNid);
            int conceptStamp = stampService.getStampSequence(state, time, authorNid, moduleNid, pathNid);
            conceptToWrite.createMutableVersion(conceptStamp);
            conceptService.writeConcept(conceptToWrite);
            
            // add to legacy def status assemblage
            UUID defStatusPrimordialUuid = UuidT5Generator.get(TermAux.RF2_LEGACY_RELATIONSHIP_IMPLICATION_ASSEMBLAGE.getPrimordialUuid(), 
                    conceptRecord[CONCEPT_SCT_ID_INDEX]);
            int defStatusNid = identifierService.getNidForUuids(defStatusPrimordialUuid);
            SemanticChronologyImpl defStatusToWrite = new SemanticChronologyImpl(VersionType.COMPONENT_NID,
                               defStatusPrimordialUuid,
                               defStatusNid,
                               defStatusAssemblageNid,
                               conceptNid);
                               
            ComponentNidVersionImpl defStatusVersion = defStatusToWrite.createMutableVersion(conceptStamp);
            defStatusVersion.setComponentNid(legacyDefStatusNid);
            assemblageService.writeSemanticChronology(defStatusToWrite);
            
            // add to sct identifier assemblage
            UUID sctIdentifierUuid = UuidT5Generator.get(TermAux.SCT_IDENTIFIER_ASSEMBLAGE.getPrimordialUuid(), 
                    conceptRecord[CONCEPT_SCT_ID_INDEX]);
            int sctIdentifierNid = identifierService.getNidForUuids(sctIdentifierUuid);
            SemanticChronologyImpl sctIdentifierToWrite = new SemanticChronologyImpl(VersionType.STRING,
                               sctIdentifierUuid,
                               sctIdentifierNid,
                               sctIdentifierAssemblageNid,
                               conceptNid);
            
            StringVersionImpl sctIdVersion = sctIdentifierToWrite.createMutableVersion(conceptStamp);
            sctIdVersion.setString(conceptRecord[CONCEPT_SCT_ID_INDEX]);
            assemblageService.writeSemanticChronology(sctIdentifierToWrite);
            completedUnitOfWork();
         }

         return null;
      } finally {
         this.writeSemaphore.release();
         this.done();
         Get.activeTasks().remove(this);
      }
   }
}
