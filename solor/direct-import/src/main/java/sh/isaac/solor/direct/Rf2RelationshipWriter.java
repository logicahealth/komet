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
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
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

    private static final int SRF_ID_INDEX = 0;
    private static final int SRF_STATUS_INDEX = 1;
    private static final int SRF_TIME_INDEX = 2;
    private static final int SRF_AUTHOR_INDEX = 3;
    private static final int SRF_MODULE_INDEX = 4;
    private static final int SRF_PATH_INDEX = 5;
    private static final int SRF_SOURCE_ID_INDEX = 6;
    private static final int SRF_DESTINATION_ID_INDEX = 7;
    private static final int SRF_RELATIONSHIP_GROUP_INDEX = 8;
    private static final int SRF_TYPE_ID_INDEX = 9;
    private static final int SRF_CHARACTERISTIC_TYPE_ID_INDEX = 10;
    private static final int SRF_MODIFIER_ID_INDEX = 11;

    private final List<String[]> relationshipRecords;
   private final Semaphore writeSemaphore;
   private final List<IndexBuilderService> indexers;
   private final ImportType importType;

   private final ImportSpecification importSpecification;

   public Rf2RelationshipWriter(List<String[]> descriptionRecords, 
            Semaphore writeSemaphore, String message, 
            ImportSpecification importSpecification, ImportType importType) {
      this.relationshipRecords = descriptionRecords;
      this.writeSemaphore = writeSemaphore;
      this.writeSemaphore.acquireUninterruptibly();
      indexers = LookupService.get().getAllServices(IndexBuilderService.class);
      this.importSpecification = importSpecification;
      updateTitle(DirectImporter.SRF_IMPORT
              ? "Importing srf relationship batch of size: " + descriptionRecords.size()
              : "Importing rf2 relationship batch of size: " + descriptionRecords.size());
      updateMessage(message);
      addToTotalWork(descriptionRecords.size());
      Get.activeTasks().add(this);
      this.importType = importType;
   }
   protected static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger();

   private void index(Chronology chronicle) {
//      for (IndexService indexer : indexers) {
//         try {
//            indexer.index(chronicle).get();
//         } catch (InterruptedException | ExecutionException ex) {
//            LOG.error(ex);
//         }
//      }
   }

   @Override
   protected Void call() throws Exception {
      try {
         AssemblageService assemblageService = Get.assemblageService();
         IdentifierService identifierService = Get.identifierService();
         StampService stampService = Get.stampService();
         int authorNid = 1;
         int pathNid = 1;
         int relAssemblageNid;

         if(DirectImporter.SRF_IMPORT){
             relAssemblageNid = TermAux.SRF_INFERRED_RELATIONSHIP_ASSEMBLAGE.getNid();
             if (importSpecification.streamType == ImportStreamType.STATED_RELATIONSHIP) {
                 relAssemblageNid = TermAux.SRF_STATED_RELATIONSHIP_ASSEMBLAGE.getNid();
             }
         }else{
             authorNid = TermAux.USER.getNid();
             pathNid = TermAux.DEVELOPMENT_PATH.getNid();
             relAssemblageNid = TermAux.RF2_INFERRED_RELATIONSHIP_ASSEMBLAGE.getNid();
             if (importSpecification.streamType == ImportStreamType.STATED_RELATIONSHIP) {
                 relAssemblageNid = TermAux.RF2_STATED_RELATIONSHIP_ASSEMBLAGE.getNid();
             }
         }


         for (String[] relationshipRecord : relationshipRecords) {
             try {
                 final Status state = Status.fromZeroOneToken(DirectImporter.SRF_IMPORT
                         ? relationshipRecord[SRF_STATUS_INDEX]
                         : relationshipRecord[RF2_ACTIVE_INDEX]);
                 if (state == Status.INACTIVE && importType == ImportType.ACTIVE_ONLY) {
                     continue;
                 }
                 UUID referencedConceptUuid = DirectImporter.SRF_IMPORT
                         ? UUID.fromString(relationshipRecord[SRF_SOURCE_ID_INDEX])
                         : UuidT3Generator.fromSNOMED(relationshipRecord[RF2_REFERENCED_CONCEPT_SCT_ID_INDEX]);
                 if (importType == ImportType.ACTIVE_ONLY) {
                     if (!identifierService.hasUuid(referencedConceptUuid)) {
                         // if concept was not imported because inactive then skip
                         continue;
                     }
                 }

                 UUID betterRelUuid, moduleUuid, destinationUuid, relTypeUuid, relCharacteristicUuid, relModifierUuid;
                 TemporalAccessor accessor;

                 if(DirectImporter.SRF_IMPORT){
                     authorNid = identifierService.getNidForUuids(UUID.fromString(relationshipRecord[SRF_AUTHOR_INDEX]));
                     pathNid = identifierService.getNidForUuids(UUID.fromString(relationshipRecord[SRF_PATH_INDEX]));
                     betterRelUuid = UuidT5Generator.get(
                             relationshipRecord[SRF_ID_INDEX]
                                     + relationshipRecord[SRF_SOURCE_ID_INDEX]
                                     + relationshipRecord[SRF_TYPE_ID_INDEX]
                                     + relationshipRecord[SRF_DESTINATION_ID_INDEX]
                                     + relationshipRecord[SRF_CHARACTERISTIC_TYPE_ID_INDEX]
                                     + relationshipRecord[SRF_MODIFIER_ID_INDEX]
                                     + importSpecification.streamType
                     );
                     moduleUuid = UUID.fromString(relationshipRecord[SRF_MODULE_INDEX]);
                     destinationUuid = UUID.fromString(relationshipRecord[SRF_DESTINATION_ID_INDEX]);
                     relTypeUuid = UUID.fromString(relationshipRecord[SRF_TYPE_ID_INDEX]);
                     relCharacteristicUuid = UUID.fromString(relationshipRecord[SRF_CHARACTERISTIC_TYPE_ID_INDEX]);
                     relModifierUuid = UUID.fromString(relationshipRecord[SRF_MODIFIER_ID_INDEX]);
                     accessor = DateTimeFormatter.ISO_INSTANT.parse(DirectImporter.getIsoInstant(relationshipRecord[SRF_TIME_INDEX]));
                 }else{
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
                     destinationUuid = UuidT3Generator.fromSNOMED(relationshipRecord[RF2_DESTINATION_NID_INDEX]);
                     relTypeUuid = UuidT3Generator.fromSNOMED(relationshipRecord[RF2_REL_TYPE_NID_INDEX]);
                     relCharacteristicUuid = UuidT3Generator.fromSNOMED(relationshipRecord[RF2_REL_CHARACTERISTIC_NID_INDEX]);
                     relModifierUuid = UuidT3Generator.fromSNOMED(relationshipRecord[RF2_REL_MODIFIER_NID_INDEX]);
                     accessor = DateTimeFormatter.ISO_INSTANT.parse(DirectImporter.getIsoInstant(relationshipRecord[RF2_EFFECTIVE_TIME_INDEX]));
                 }
                 

                 long time = accessor.getLong(INSTANT_SECONDS) * 1000;

                 // add to rel assemblage
                 int destinationNid = identifierService.getNidForUuids(destinationUuid);
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
                 if (relationshipRecord[RF2_MODULE_SCTID_INDEX].equals("900062011000036108")
                         || relationshipRecord[SRF_MODULE_INDEX].equals("cee6956f-7f49-388d-9c48-f0116b5af980")) {
                     UUID relUuid = DirectImporter.SRF_IMPORT
                             ? UUID.fromString(relationshipRecord[SRF_ID_INDEX])
                             : UuidT3Generator.fromSNOMED(relationshipRecord[RF2_REL_SCT_ID_INDEX]);
                     identifierService.addUuidForNid(relUuid, relationshipToWrite.getNid());
                     relationshipToWrite.addAdditionalUuids(relUuid);
                 }
                 
                 int relStamp = stampService.getStampSequence(state, time, authorNid, moduleNid, pathNid);
                 Rf2RelationshipImpl relVersion = relationshipToWrite.createMutableVersion(relStamp);
                 relVersion.setCharacteristicNid(relCharacteristicNid);
                 relVersion.setDestinationNid(destinationNid);
                 relVersion.setModifierNid(relModifierNid);
                 relVersion.setTypeNid(relTypeNid);
                 relVersion.setRelationshipGroup(DirectImporter.SRF_IMPORT
                         ? Integer.parseInt(relationshipRecord[SRF_RELATIONSHIP_GROUP_INDEX])
                         : Integer.parseInt(relationshipRecord[RF2_REL_GROUP_INDEX]));
                 index(relationshipToWrite);
                 assemblageService.writeSemanticChronology(relationshipToWrite);
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
