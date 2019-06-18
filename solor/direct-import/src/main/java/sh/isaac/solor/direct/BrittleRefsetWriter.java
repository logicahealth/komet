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

import org.apache.mahout.math.Arrays;
import sh.isaac.api.*;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UUIDUtil;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.LongVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;
import sh.isaac.model.semantic.version.brittle.*;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.Semaphore;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;

/**
 *
 * @author kec
 */
public class BrittleRefsetWriter extends TimedTaskWithProgressTracker<Void> {
   private static final int RF2_REFSET_MEMBER_UUID = 0;
   private static final int RF2_EFFECTIVE_TIME_INDEX = 1;
   private static final int RF2_ACTIVE_INDEX = 2;  // 0 == false, 1 == true
   private static final int RF2_MODULE_SCTID_INDEX = 3;
   private static final int RF2_ASSEMBLAGE_SCT_ID_INDEX = 4;
   private static final int RF2_REFERENCED_CONCEPT_SCT_ID_INDEX = 5;
   private static final int RF2_VARIABLE_FIELD_START = 5;

   private final List<String[]> refsetRecords;
   private final Semaphore writeSemaphore;
   private final List<IndexBuilderService> indexers;
   private final ImportSpecification importSpecification;
   private final ImportType importType;
   private final AssemblageService assemblageService = Get.assemblageService();
   private final IdentifierService identifierService = Get.identifierService();
   private final StampService stampService = Get.stampService();
   private final HashSet<String> refsetsToIgnore = new HashSet<>();
   private final int VARIABLE_FIELD_START;

   public BrittleRefsetWriter(List<String[]> refsetRecords, Semaphore writeSemaphore, String message,
           ImportSpecification importSpecification, ImportType importType) {
      this.refsetRecords = refsetRecords;
      this.writeSemaphore = writeSemaphore;
      this.importSpecification = importSpecification;
      this.importType = importType;
      this.writeSemaphore.acquireUninterruptibly();
      VARIABLE_FIELD_START = RF2_VARIABLE_FIELD_START;
      indexers = LookupService.get().getAllServices(IndexBuilderService.class);
      updateTitle("Importing semantic batch of size: " + refsetRecords.size());
      updateMessage(message);
      addToTotalWork(refsetRecords.size());
      Get.activeTasks().add(this);
      
      // TODO move these to an import preference... 
      refsetsToIgnore.add("6011000124106"); //6011000124106 | ICD-10-CM complex map reference set (foundation metadata concept)
      refsetsToIgnore.add("447563008"); //447563008 | ICD-9-CM equivalence complex map reference set (foundation metadata concept)
      refsetsToIgnore.add("447569007"); //447569007 | International Classification of Diseases, Ninth Revision, Clinical Modification reimbursement complex map reference set (foundation metadata concept)
      refsetsToIgnore.add("450993002"); //450993002 | International Classification of Primary Care, Second edition complex map reference set (foundation metadata concept) |
      refsetsToIgnore.add("447562003"); //447562003 | ICD-10 complex map reference set (foundation metadata concept)
      refsetsToIgnore.add("900000000000497000"); //900000000000497000 | CTV3 simple map reference set (foundation metadata concept) |
      refsetsToIgnore.add("467614008"); //467614008 | GMDN simple map reference set (foundation metadata concept)
      refsetsToIgnore.add("446608001"); //446608001 | ICD-O simple map reference set (foundation metadata concept)
      refsetsToIgnore.add("711112009"); //711112009 | ICNP diagnoses simple map reference set (foundation metadata concept)
      refsetsToIgnore.add("712505008"); //712505008 | ICNP interventions simple map reference set (foundation metadata concept) 
      refsetsToIgnore.add("900000000000498005"); //900000000000498005 | SNOMED RT identifier simple map (foundation metadata concept)
//      refsetsToIgnore.add("733900009"); //733900009 | UCUM simple map reference set (foundation metadata concept)
      
      refsetsToIgnore.add("900000000000490003");  // 900000000000490003 | Description inactivation indicator attribute value reference set (foundation metadata concept) |
      refsetsToIgnore.add("900000000000489007");  // 900000000000489007 | Concept inactivation indicator attribute value reference set (foundation metadata concept)
//      refsetsToIgnore.add("900000000000527005");  // 900000000000527005 | SAME AS association reference set (foundation metadata concept)

   }
   private void index(Chronology chronicle) {
      for (IndexBuilderService indexer: indexers) {
         indexer.indexNow(chronicle);
      }
   }
   
   int nidFromSctid(String sctid) {
      try {
         return identifierService.getNidForUuids(UuidT3Generator.fromSNOMED(sctid));
      } catch (NoSuchElementException e) {
         LOG.error("The SCTID {} was mapped to UUID {} but that UUID has not been loaded into the system", sctid, UuidT3Generator.fromSNOMED(sctid), e);
         throw e;
      }
   }
   
   @Override
   protected Void call() throws Exception {
      try {

         int authorNid = 1;
         int pathNid = 1;

         authorNid = TermAux.USER.getNid();
         pathNid = TermAux.DEVELOPMENT_PATH.getNid();

         List<String[]> noSuchElementList = new ArrayList<>();

         boolean skippedAny = false;
         for (String[] refsetRecord : refsetRecords) {
             try {
                 UUID referencedComponentUuid = UuidT3Generator.fromSNOMED(refsetRecord[RF2_REFERENCED_CONCEPT_SCT_ID_INDEX]);
                 final Status state = Status.fromZeroOneToken(refsetRecord[RF2_ACTIVE_INDEX]);
                 if (importType == ImportType.ACTIVE_ONLY) {
                    if (state == Status.INACTIVE) {
                        continue;
                    }
                    // if the referenced component not previously imported, may
                    // have been inactive, so don't import. 
                    if (!identifierService.hasUuid(referencedComponentUuid)) {
                        if (!skippedAny) {
                            skippedAny = true;
                            StringBuilder builder = new StringBuilder();
                            int assemblageNid = nidFromSctid(refsetRecord[RF2_ASSEMBLAGE_SCT_ID_INDEX]);
                            builder.append("Skipping at least one record in: ");
                            builder.append(Get.conceptDescriptionText(assemblageNid));
                            builder.append("\n");
                            builder.append(Arrays.toString(refsetRecord));
                            LOG.warn(builder.toString());
                        }
                        continue;
                    }
                 }
                 if (refsetsToIgnore.contains(refsetRecord[RF2_ASSEMBLAGE_SCT_ID_INDEX])) {
                     continue;
                 }

                 UUID elementUuid = UUID.fromString(refsetRecord[RF2_REFSET_MEMBER_UUID]);
                 int moduleNid;

                 if (UUIDUtil.isUUID(refsetRecord[RF2_MODULE_SCTID_INDEX])) {
                     moduleNid = identifierService.getNidForUuids(UUID.fromString(refsetRecord[RF2_MODULE_SCTID_INDEX]));
                 } else {
                     moduleNid = nidFromSctid(refsetRecord[RF2_MODULE_SCTID_INDEX]);
                 }

                 int assemblageNid = nidFromSctid(refsetRecord[RF2_ASSEMBLAGE_SCT_ID_INDEX]);
                 int referencedComponentNid = nidFromSctid(refsetRecord[RF2_REFERENCED_CONCEPT_SCT_ID_INDEX]);
                 TemporalAccessor accessor = DateTimeFormatter.ISO_INSTANT.parse(DirectImporter.getIsoInstant(refsetRecord[RF2_EFFECTIVE_TIME_INDEX]));
                 long time = accessor.getLong(INSTANT_SECONDS) * 1000;
                 int versionStamp = stampService.getStampSequence(state, time, authorNid, moduleNid, pathNid);
                 
                 SemanticChronologyImpl refsetMemberToWrite = new SemanticChronologyImpl(
                         this.importSpecification.streamType.getSemanticVersionType(),
                         elementUuid,
                         assemblageNid,
                         referencedComponentNid);

                 switch (importSpecification.streamType) {
                     case NID1_NID2_INT3_REFSET:
                         addVersionNID1_NID2_INT3_REFSET(refsetMemberToWrite, versionStamp, refsetRecord);
                         break;
                     
                     case NID1_INT2_REFSET:
                         addVersionNID1_INT2_REFSET(refsetMemberToWrite, versionStamp, refsetRecord);
                         break;
                     
                     case NID1_INT2_STR3_STR4_NID5_NID6_REFSET:
                         addVersionNID1_INT2_STR3_STR4_NID5_NID6_REFSET(refsetMemberToWrite, versionStamp, refsetRecord);
                         break;
                     
                     case NID1_REFSET:
                         addVersionNID1_REFSET(refsetMemberToWrite, versionStamp, refsetRecord);
                         break;
                     
                     case STR1_STR2_NID3_NID4_REFSET:
                         addVersionSTR1_STR2_NID3_NID4_REFSET(refsetMemberToWrite, versionStamp, refsetRecord);
                         break;
                     
                     case STR1_STR2_REFSET:
                         addVersionSTR1_STR2_REFSET(refsetMemberToWrite, versionStamp, refsetRecord);
                         break;
                     
                     case STR1_STR2_STR3_STR4_STR5_STR6_STR7_REFSET:
                         addVersionSTR1_STR2_STR3_STR4_STR5_STR6_STR7_REFSET(refsetMemberToWrite, versionStamp, refsetRecord);
                         break;
                     
                     case MEMBER_REFSET:
                         addVersionMEMBER_REFSET(refsetMemberToWrite, versionStamp, refsetRecord);
                         break;
                     
                     case INT1_INT2_STR3_STR4_STR5_NID6_NID7_REFSET:
                         addVersionINT1_INT2_STR3_STR4_STR5_NID6_NID7_REFSET(refsetMemberToWrite, versionStamp, refsetRecord);
                         break;
                     
                     case STR1_REFSET:
                         addVersionSTR1_REFSET(refsetMemberToWrite, versionStamp, refsetRecord);
                         break;
                     
                     case NID1_NID2_REFSET:
                         addVersionNID1_NID2_REFSET(refsetMemberToWrite, versionStamp, refsetRecord);
                         break;
                     
                     case NID1_NID2_STR3_REFSET:
                         addVersionNID1_NID2_STR3_REFSET(refsetMemberToWrite, versionStamp, refsetRecord);
                         break;
                     
                     case NID1_STR2_REFSET:
                         addVersionNID1_STR2_REFSET(refsetMemberToWrite, versionStamp, refsetRecord);
                         break;
                     
                     case INT1_REFSET:
                         addVersionINT1_REFSET(refsetMemberToWrite, versionStamp, refsetRecord);
                         break;
                     
                     case STR1_NID2_NID3_NID4_REFSET:
                         addVersionSTR1_NID2_NID3_NID4_REFSET(refsetMemberToWrite, versionStamp, refsetRecord);
                         break;
                     
                     case STR1_STR2_NID3_NID4_NID5_REFSET:
                         addVersionSTR1_STR2_NID3_NID4_NID5_REFSET(refsetMemberToWrite, versionStamp, refsetRecord);
                         break;
                     
                     default:
                         throw new UnsupportedOperationException("Can't handle: " + importSpecification.streamType);
                     
                 }
                 
                 index(refsetMemberToWrite);
                 assemblageService.writeSemanticChronology(refsetMemberToWrite);
             } catch (NoSuchElementException ex) {
                 noSuchElementList.add(refsetRecord);
             }
            completedUnitOfWork();
         }
         if (!noSuchElementList.isEmpty()) {
            LOG.error("Continuing after import failed with no such element exception for these records: \n" + noSuchElementList);
         }
         return null;
      } finally {
         this.writeSemaphore.release();
         Get.activeTasks().remove(this);
      }
   }

   private void addVersionNID1_NID2_INT3_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
      Nid1_Nid2_Int3_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
      brittleVersion.setNid1(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
      brittleVersion.setNid2(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 2].trim()));
      brittleVersion.setInt3(Integer.parseInt(refsetRecord[VARIABLE_FIELD_START + 3].trim()));
   }
   private void addVersionNID1_INT2_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
      Nid1_Int2_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
      brittleVersion.setNid1(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
      brittleVersion.setInt2(Integer.parseInt(refsetRecord[VARIABLE_FIELD_START + 2].trim()));
   }

   private void addVersionNID1_INT2_STR3_STR4_NID5_NID6_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
      Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
      brittleVersion.setNid1(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
      brittleVersion.setInt2(Integer.parseInt(refsetRecord[VARIABLE_FIELD_START + 2].trim()));
      brittleVersion.setStr3(refsetRecord[VARIABLE_FIELD_START + 3]);
      brittleVersion.setStr4(refsetRecord[VARIABLE_FIELD_START + 4]);
      brittleVersion.setNid5(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 5].trim()));
      brittleVersion.setNid6(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 6].trim()));
   }

   private void addVersionNID1_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
      ComponentNidVersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
      brittleVersion.setComponentNid(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
   }

   private void addVersionSTR1_STR2_NID3_NID4_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
      Str1_Str2_Nid3_Nid4_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
      brittleVersion.setStr1(refsetRecord[VARIABLE_FIELD_START + 1]);
      brittleVersion.setStr2(refsetRecord[VARIABLE_FIELD_START + 2]);
      brittleVersion.setNid3(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 3].trim()));
      brittleVersion.setNid4(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 4].trim()));
   }

   private void addVersionSTR1_STR2_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
      Str1_Str2_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
      brittleVersion.setStr1(refsetRecord[VARIABLE_FIELD_START + 1]);
      brittleVersion.setStr2(refsetRecord[VARIABLE_FIELD_START + 2]);
   }

   private void addVersionSTR1_STR2_STR3_STR4_STR5_STR6_STR7_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
      Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
      brittleVersion.setStr1(refsetRecord[VARIABLE_FIELD_START + 1]);
      brittleVersion.setStr2(refsetRecord[VARIABLE_FIELD_START + 2]);
      brittleVersion.setStr3(refsetRecord[VARIABLE_FIELD_START + 3]);
      brittleVersion.setStr4(refsetRecord[VARIABLE_FIELD_START + 4]);
      brittleVersion.setStr5(refsetRecord[VARIABLE_FIELD_START + 5]);
      brittleVersion.setStr6(refsetRecord[VARIABLE_FIELD_START + 6]);
      brittleVersion.setStr7(refsetRecord[VARIABLE_FIELD_START + 7]);
   }

   private void addVersionMEMBER_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
      refsetMemberToWrite.createMutableVersion(versionStamp);
   }

   private void addVersionINT1_INT2_STR3_STR4_STR5_NID6_NID7_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
      Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
      brittleVersion.setInt1(Integer.parseInt(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
      brittleVersion.setInt2(Integer.parseInt(refsetRecord[VARIABLE_FIELD_START + 2].trim()));
      brittleVersion.setStr3(refsetRecord[VARIABLE_FIELD_START + 3]);
      brittleVersion.setStr4(refsetRecord[VARIABLE_FIELD_START + 4]);
      brittleVersion.setStr5(refsetRecord[VARIABLE_FIELD_START + 5]);
      brittleVersion.setNid6(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 6].trim()));
      brittleVersion.setNid7(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 7].trim()));
   }

   private void addVersionSTR1_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
      StringVersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
      brittleVersion.setString(refsetRecord[VARIABLE_FIELD_START + 1]);      
   }

   private void addVersionNID1_NID2_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
      Nid1_Nid2_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
      brittleVersion.setNid1(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
      brittleVersion.setNid2(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 2].trim()));
   }

   private void addVersionNID1_NID2_STR3_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
      Nid1_Nid2_Str3_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
      brittleVersion.setNid1(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
      brittleVersion.setNid2(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 2].trim()));
      brittleVersion.setStr3(refsetRecord[VARIABLE_FIELD_START + 3]);
   }

   private void addVersionNID1_STR2_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
      Nid1_Str2_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
      brittleVersion.setNid1(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
      brittleVersion.setStr2(refsetRecord[VARIABLE_FIELD_START + 2]);
   }

   private void addVersionINT1_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
      LongVersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
      brittleVersion.setLongValue(Long.parseLong(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
   }

    private void addVersionSTR1_NID2_NID3_NID4_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
      Str1_Nid2_Nid3_Nid4_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
      brittleVersion.setStr1(refsetRecord[VARIABLE_FIELD_START + 1]);
      brittleVersion.setNid2(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 2].trim()));
      brittleVersion.setNid3(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 3].trim()));
      brittleVersion.setNid4(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 4].trim()));
    }

    private void addVersionSTR1_STR2_NID3_NID4_NID5_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
      Str1_Str2_Nid3_Nid4_Nid5_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
      brittleVersion.setStr1(refsetRecord[VARIABLE_FIELD_START + 1]);
      brittleVersion.setStr2(refsetRecord[VARIABLE_FIELD_START + 2]);
      brittleVersion.setNid3(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 3].trim()));
      brittleVersion.setNid4(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 4].trim()));
      brittleVersion.setNid5(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 5].trim()));
    }
}
