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
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import org.apache.logging.log4j.LogManager;
import org.apache.mahout.math.Arrays;
import sh.isaac.MetaData;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;
import sh.isaac.model.semantic.version.brittle.Rf2RelationshipImpl;

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
   private static final int RF2_REL_SCT_ID_INDEX = 0;
   private static final int RF2_EFFECTIVE_TIME_INDEX = 1;
   private static final int RF2_ACTIVE_INDEX = 2; // 0 == false, 1 == true
   private static final int RF2_MODULE_SCTID_INDEX = 3;
   private static final int RF2_REFERENCED_CONCEPT_SCT_ID_INDEX = 4;
   private static final int RF2_DESTINATION_NID_INDEX = 5;
   private static final int RF2_REL_GROUP_INDEX = 6;
   private static final int RF2_REL_TYPE_NID_INDEX = 7;
   private static final int RF2_REL_CHARACTERISTIC_NID_INDEX = 8;
   private static final int RF2_REL_MODIFIER_NID_INDEX = 9;

   private final List<String[]> relationshipRecords;
   private final Semaphore writeSemaphore;
   private final ImportType importType;
   private final ImportSpecification importSpecification;

   public Rf2RelationshipWriter(List<String[]> relationshipRecords,
            Semaphore writeSemaphore, String message, 
            ImportSpecification importSpecification, ImportType importType) {
      this.relationshipRecords = relationshipRecords;
      this.writeSemaphore = writeSemaphore;
      this.writeSemaphore.acquireUninterruptibly();
      this.importSpecification = importSpecification;
      updateTitle("Importing relationship batch of size: " + relationshipRecords.size());
      updateMessage(message);
      addToTotalWork(relationshipRecords.size());
      Get.activeTasks().add(this);
      this.importType = importType;
   }
   protected static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger();

   @Override
   protected Void call() throws Exception {
      try {
         AssemblageService assemblageService = Get.assemblageService();
         IdentifierService identifierService = Get.identifierService();
         StampService stampService = Get.stampService();

         int authorNid = TermAux.USER.getNid();
         int pathNid = TermAux.DEVELOPMENT_PATH.getNid();
         int relAssemblageNid = TermAux.RF2_INFERRED_RELATIONSHIP_ASSEMBLAGE.getNid();

         if (importSpecification.streamType == ImportStreamType.STATED_RELATIONSHIP) {
             relAssemblageNid = TermAux.RF2_STATED_RELATIONSHIP_ASSEMBLAGE.getNid();
         }

         for (String[] relationshipRecord : relationshipRecords) {
             try {
                 final Status state = Status.fromZeroOneToken(relationshipRecord[RF2_ACTIVE_INDEX]);
                 //We cannot skip inactive records when importing snapshot_active_only, because we will end up with the wrong date
                 //on the logic graph, since it combines all of these records into a single graph.
                 UUID referencedConceptUuid = UuidT3Generator.fromSNOMED(relationshipRecord[RF2_REFERENCED_CONCEPT_SCT_ID_INDEX]);
                 UUID destinationUuid = UuidT3Generator.fromSNOMED(relationshipRecord[RF2_DESTINATION_NID_INDEX]);
                 UUID relTypeUuid = UuidT3Generator.fromSNOMED(relationshipRecord[RF2_REL_TYPE_NID_INDEX]);
                 if (importType == ImportType.SNAPSHOT_ACTIVE_ONLY) {
                     if (!identifierService.hasUuid(referencedConceptUuid)) {
                         // if concept was not imported because inactive then skip, we wouldn't process this row.
                         continue;
                     }
                     if (!identifierService.hasUuid(destinationUuid)) {
                         //This is trickier - if its inactive, will point at a placeholder, so we can load, and keep the timestamp.
                         //If its active, I think that is just an error, and will fail for now. 
                         if (state == Status.INACTIVE) {
                            destinationUuid = MetaData.UNINITIALIZED_COMPONENT____SOLOR.getPrimordialUuid();
                         }
                         else {
                            throw new RuntimeException("Unexpected case of snapshot active only with an active rel that references a missing destination:" 
                                  + Arrays.toString(relationshipRecord));
                         }
                     }
                     if (!identifierService.hasUuid(relTypeUuid)) {
                         //This is trickier - if its inactive, will point at a placeholder, so we can load, and keep the timestamp.
                         //If its active, I think that is just an error, and will fail for now. 
                         if (state == Status.INACTIVE) {
                             relTypeUuid = MetaData.UNINITIALIZED_COMPONENT____SOLOR.getPrimordialUuid();
                         }
                         else {
                            throw new RuntimeException("Unexpected case of snapshot active only with an active rel that references a missing reltype:" 
                                  + Arrays.toString(relationshipRecord));
                         }
                     }
                 }

                 UUID betterRelUuid, moduleUuid, relCharacteristicUuid, relModifierUuid;
                 TemporalAccessor accessor;

                 betterRelUuid = UuidT5Generator.get(
                         relationshipRecord[RF2_REL_SCT_ID_INDEX]
                                 + relationshipRecord[RF2_REFERENCED_CONCEPT_SCT_ID_INDEX]
                                 + relationshipRecord[RF2_REL_TYPE_NID_INDEX]
                                 + relationshipRecord[RF2_DESTINATION_NID_INDEX]
                                 + relationshipRecord[RF2_REL_CHARACTERISTIC_NID_INDEX]
                                 + relationshipRecord[RF2_REL_MODIFIER_NID_INDEX]
                                 + importSpecification.streamType
                 );
                 moduleUuid = UuidT3Generator.fromSNOMED(relationshipRecord[RF2_MODULE_SCTID_INDEX]);
                 relCharacteristicUuid = UuidT3Generator.fromSNOMED(relationshipRecord[RF2_REL_CHARACTERISTIC_NID_INDEX]);
                 relModifierUuid = UuidT3Generator.fromSNOMED(relationshipRecord[RF2_REL_MODIFIER_NID_INDEX]);
                 accessor = DateTimeFormatter.ISO_INSTANT.parse(DirectImporter.getIsoInstant(relationshipRecord[RF2_EFFECTIVE_TIME_INDEX]));

                 long time = accessor.getLong(INSTANT_SECONDS) * 1000;

                 // add to rel assemblage
                 int destinationNid = 0;
                 try {
                     destinationNid = identifierService.getNidForUuids(destinationUuid);
                 } catch (NoSuchElementException e) {
                     LOG.warn("No element for [2]: " + relationshipRecord[RF2_DESTINATION_NID_INDEX]);
                     destinationNid = identifierService.assignNid(destinationUuid);
                 }
                 int moduleNid = identifierService.getNidForUuids(moduleUuid);
                 int referencedConceptNid = identifierService.getNidForUuids(referencedConceptUuid);
                 int relTypeNid = identifierService.getNidForUuids(relTypeUuid);
                 int relCharacteristicNid = identifierService.getNidForUuids(relCharacteristicUuid);
                 int relModifierNid = identifierService.getNidForUuids(relModifierUuid);
                 
                 SemanticChronologyImpl relationshipToWrite
                         = new SemanticChronologyImpl(VersionType.RF2_RELATIONSHIP, betterRelUuid, //TODO Change to SRF Relationship?? AKS
                                 relAssemblageNid, referencedConceptNid);
                 // Add in original uuids for AMT content...
                 // 900062011000036108 = AU module
                 if (relationshipRecord[RF2_MODULE_SCTID_INDEX].equals("900062011000036108")) {
                     UUID relUuid = UuidT3Generator.fromSNOMED(relationshipRecord[RF2_REL_SCT_ID_INDEX]);
                     identifierService.addUuidForNid(relUuid, relationshipToWrite.getNid());
                     relationshipToWrite.addAdditionalUuids(relUuid);
                 }
                 
                 int relStamp = stampService.getStampSequence(state, time, authorNid, moduleNid, pathNid);
                 Rf2RelationshipImpl relVersion = relationshipToWrite.createMutableVersion(relStamp);
                 relVersion.setCharacteristicNid(relCharacteristicNid);
                 relVersion.setDestinationNid(destinationNid);
                 relVersion.setModifierNid(relModifierNid);
                 relVersion.setTypeNid(relTypeNid);
                 relVersion.setRelationshipGroup(Integer.parseInt(relationshipRecord[RF2_REL_GROUP_INDEX]));

                 assemblageService.writeSemanticChronology(relationshipToWrite);

                 // add to sct identifier assemblage
                 UUID identifierUuid;

                 identifierUuid = UuidT5Generator.get(TermAux.SNOMED_IDENTIFIER.getPrimordialUuid(), relationshipRecord[RF2_REL_SCT_ID_INDEX]);

                 SemanticChronologyImpl sctIdentifierToWrite = new SemanticChronologyImpl(VersionType.STRING,
                         identifierUuid,
                         TermAux.SNOMED_IDENTIFIER.getNid(),
                         relationshipToWrite.getNid());

                 StringVersionImpl idVersion = sctIdentifierToWrite.createMutableVersion(relStamp);
                 idVersion.setString(relationshipRecord[RF2_REL_SCT_ID_INDEX]);
                 assemblageService.writeSemanticChronology(sctIdentifierToWrite);

             } catch (NoSuchElementException noSuchElementException) {
                 StringBuilder builder = new StringBuilder();
                 builder.append("Error importing record: \n").append(Arrays.toString(relationshipRecord));
                 builder.append("\n");
                 LOG.error(builder.toString(), noSuchElementException);
             } finally {
                 completedUnitOfWork();
             }
            
         }

         return null;
      } finally {
         this.writeSemaphore.release();
         Get.activeTasks().remove(this);
      }
   }
}
