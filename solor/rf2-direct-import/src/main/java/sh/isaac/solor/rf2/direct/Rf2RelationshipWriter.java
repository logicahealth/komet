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
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.Rf2RelationshipImpl;

/**
 *
 * @author kec
 */
public class Rf2RelationshipWriter extends TimedTaskWithProgressTracker<Void> {

/*
   
id	effectiveTime	active	moduleId	sourceId	destinationId	relationshipGroup	typeId	characteristicTypeId	modifierId
3187444026	20140131	1	900000000000207008	425630003	400195000	0	42752001	900000000000010007	900000000000451002
3187444026	20160131	0	900000000000207008	425630003	400195000	0	42752001	900000000000010007	900000000000451002
   
*/   

   private static final int REL_SCT_ID_INDEX = 0;
   private static final int EFFECTIVE_TIME_INDEX = 1;
   private static final int ACTIVE_INDEX = 2; // 0 == false, 1 == true
   private static final int MODULE_SCTID_INDEX = 3;
   private static final int REFERENCED_CONCEPT_SCT_ID_INDEX = 4;
   private static final int DESTINATION_NID_INDEX = 5;
   private static final int REL_GROUP_INDEX = 6;
   private static final int REL_TYPE_NID_INDEX = 7;
   private static final int REL_CHARACTERISTIC_NID_INDEX = 8;
   private static final int REL_MODIFIER_NID_INDEX = 8;

   private final List<String[]> relationshipRecords;
   private final Semaphore writeSemaphore;
   
   private final ImportStreamType importStreamType;

   public Rf2RelationshipWriter(List<String[]> descriptionRecords, Semaphore writeSemaphore, String message, ImportStreamType importStreamType) {
      this.relationshipRecords = descriptionRecords;
      this.writeSemaphore = writeSemaphore;
      this.writeSemaphore.acquireUninterruptibly();
      this.importStreamType = importStreamType;
      updateTitle("Importing rf2 relationship batch of size: " + descriptionRecords.size());
      updateMessage(message);
      addToTotalWork(descriptionRecords.size());
      Get.activeTasks().add(this);
   }

   @Override
   protected Void call() throws Exception {
      try {
         AssemblageService assemblageService = Get.assemblageService();
         IdentifierService identifierService = Get.identifierService();
         StampService stampService = Get.stampService();
         
         
         int authorNid = TermAux.USER.getNid();
         int pathNid = TermAux.MASTER_PATH.getNid();
         int relAssemblageNid = TermAux.RF2_INFERRED_RELATIONSHIP_ASSEMBLAGE.getNid();
         if (importStreamType == ImportStreamType.STATED_RELATIONSHIP) {
            relAssemblageNid = TermAux.RF2_STATED_RELATIONSHIP_ASSEMBLAGE.getNid();
         }

         for (String[] relationshipRecord : relationshipRecords) {
            UUID relUuid = UuidT3Generator.fromSNOMED(relationshipRecord[REL_SCT_ID_INDEX]);
            UUID moduleUuid = UuidT3Generator.fromSNOMED(relationshipRecord[MODULE_SCTID_INDEX]);
            Status state = Status.fromZeroOneToken(relationshipRecord[ACTIVE_INDEX]);
            UUID referencedConceptUuid = UuidT3Generator.fromSNOMED(relationshipRecord[REFERENCED_CONCEPT_SCT_ID_INDEX]);
            
            UUID destinationUuid = UuidT3Generator.fromSNOMED(relationshipRecord[DESTINATION_NID_INDEX]);
            UUID relTypeUuid = UuidT3Generator.fromSNOMED(relationshipRecord[REL_TYPE_NID_INDEX]);
            UUID relCharacteristicUuid = UuidT3Generator.fromSNOMED(relationshipRecord[REL_CHARACTERISTIC_NID_INDEX]);
            UUID relModifierUuid = UuidT3Generator.fromSNOMED(relationshipRecord[REL_MODIFIER_NID_INDEX]);
   
            TemporalAccessor accessor = DateTimeFormatter.ISO_INSTANT.parse(Rf2DirectImporter.getIsoInstant(relationshipRecord[EFFECTIVE_TIME_INDEX]));
            long time = accessor.getLong(INSTANT_SECONDS) * 1000;
            
            // add to rel assemblage
            int relNid = identifierService.getNidForUuids(relUuid);
            int destinationNid = identifierService.getNidForUuids(destinationUuid);
            int moduleNid = identifierService.getNidForUuids(moduleUuid);
            int referencedConceptNid = identifierService.getNidForUuids(referencedConceptUuid);
            int relTypeNid = identifierService.getNidForUuids(relTypeUuid);
            int relCharacteristicNid = identifierService.getNidForUuids(relCharacteristicUuid);
            int relModifierNid = identifierService.getNidForUuids(relModifierUuid);
            
            SemanticChronologyImpl relationshipToWrite = 
                    new SemanticChronologyImpl(VersionType.RF2_RELATIONSHIP, relUuid, relNid, 
                            relAssemblageNid, referencedConceptNid);
            int relStamp = stampService.getStampSequence(state, time, authorNid, moduleNid, pathNid);
            Rf2RelationshipImpl relVersion = relationshipToWrite.createMutableVersion(relStamp);
            relVersion.setCharacteristicNid(relCharacteristicNid);
            relVersion.setDestinationNid(destinationNid);
            relVersion.setModifierNid(relModifierNid);
            relVersion.setTypeNid(relTypeNid);
            relVersion.setRelationshipGroup(Integer.parseInt(relationshipRecord[REL_GROUP_INDEX]));
            assemblageService.writeSemanticChronology(relationshipToWrite);
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
