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
import java.util.HashMap;
import java.util.HashSet;
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
    
    public static final HashSet<String>  CONCEPT_STRING_WHITELIST = new HashSet<>();
    static {
        // Concepts needed for loinc expressions, but where retired by SNOMED without updating the LOINC refset. 
        CONCEPT_STRING_WHITELIST.add("704318007"); // Property type (attribute)
        CONCEPT_STRING_WHITELIST.add("712561002"); // Four hours specimen (specimen)
        CONCEPT_STRING_WHITELIST.add("82052005"); // Pseudohypha (organism)
        CONCEPT_STRING_WHITELIST.add("418882002"); // Xanthine or xanthine derivative (substance)
        
        CONCEPT_STRING_WHITELIST.add("372812003"); // Valproate (substance)
        CONCEPT_STRING_WHITELIST.add("353889009"); // Ethyl esters of iodized fatty acids (product)
        CONCEPT_STRING_WHITELIST.add("116878006"); // Streptococcus pneumoniae serotype 1 antibody (substance)
        CONCEPT_STRING_WHITELIST.add("120679007"); // Streptococcus pneumoniae serotype 14 antibody (substance)
        CONCEPT_STRING_WHITELIST.add("116870004"); // Streptococcus pneumoniae serotype 3 antibody (substance)
        CONCEPT_STRING_WHITELIST.add("116889004"); // Streptococcus pneumoniae serotype 4 antibody (substance)
        CONCEPT_STRING_WHITELIST.add("116931006"); // Streptococcus pneumoniae serotype 51 antibody (substance)
        CONCEPT_STRING_WHITELIST.add("116890008"); // Streptococcus pneumoniae serotype 8 antibody (substance)
        CONCEPT_STRING_WHITELIST.add("121030007"); // Measles virus antigen (substance)
        CONCEPT_STRING_WHITELIST.add("116876005"); // Streptococcus pneumoniae serotype 56 antibody (substance)
        CONCEPT_STRING_WHITELIST.add("116962005"); // Streptococcus pneumoniae 26 antibody (substance)
        CONCEPT_STRING_WHITELIST.add("709235006"); // Antigen of Plasmodium (substance)
        CONCEPT_STRING_WHITELIST.add("708786009"); // Immunoglobulin E antibody to Setomelanomma rostrata (substance)
        CONCEPT_STRING_WHITELIST.add("710524001"); // Immunoglobulin G antibody to Streptococcus pneumoniae 23 (substance)
        CONCEPT_STRING_WHITELIST.add("710533004"); // Immunoglobulin G antibody to Streptococcus pneumoniae 9 (substance)
        CONCEPT_STRING_WHITELIST.add("720303006"); // Immunoglobulin G antibody to Streptococcus pneumoniae 17 (substance)
        CONCEPT_STRING_WHITELIST.add("710521009"); // Immunoglobulin G antibody to Streptococcus pneumoniae 12 (substance)
        CONCEPT_STRING_WHITELIST.add("710525000"); // Immunoglobulin G antibody to Streptococcus pneumoniae 26 (substance)
        CONCEPT_STRING_WHITELIST.add("710530001"); // Immunoglobulin G antibody to Streptococcus pneumoniae 6 (substance)
        CONCEPT_STRING_WHITELIST.add("710528003"); // Immunoglobulin G antibody to Streptococcus pneumoniae 51 (substance)
        CONCEPT_STRING_WHITELIST.add("710531002"); // Immunoglobulin G antibody to Streptococcus pneumoniae 7 (substance)
        CONCEPT_STRING_WHITELIST.add("710523007"); // Immunoglobulin G antibody to Streptococcus pneumoniae 19 (substance)
        CONCEPT_STRING_WHITELIST.add("710529006"); // Immunoglobulin G antibody to Streptococcus pneumoniae 56 (substance)
        CONCEPT_STRING_WHITELIST.add("720308002"); // Immunoglobulin G antibody to Streptococcus pneumoniae 34 (substance)
        CONCEPT_STRING_WHITELIST.add("720304000"); // Immunoglobulin G antibody to Streptococcus pneumoniae 18 (substance)
        CONCEPT_STRING_WHITELIST.add("720307007"); // Immunoglobulin G antibody to Streptococcus pneumoniae 22 (substance)
        CONCEPT_STRING_WHITELIST.add("720306003"); // Immunoglobulin G antibody to Streptococcus pneumoniae 20 (substance)
        CONCEPT_STRING_WHITELIST.add("720309005"); // Immunoglobulin G antibody to Streptococcus pneumoniae 43 (substance)
        
    }
    public static final HashMap<String, String> CONCEPT_REPLACEMENT_MAP_20180731 = new HashMap<>();
    static {
        // 20180131 CONCEPT_REPLACEMENT_MAP_20180731.put("704318007", "370130000");// Property type (attribute)
        // 20180131 CONCEPT_REPLACEMENT_MAP_20180731.put("712561002", "739029001"); // Four hours specimen (specimen)

        CONCEPT_REPLACEMENT_MAP_20180731.put("82052005", "769144008"); // Pseudohypha (organism)
        CONCEPT_REPLACEMENT_MAP_20180731.put("418882002", "768845000"); // Xanthine or xanthine derivative (substance)
        
        // CONCEPT_REPLACEMENT_MAP_20180731.put("372812003", ""); // Valproate (substance)
        // CONCEPT_REPLACEMENT_MAP_20180731.put("353889009", ""); // Ethyl esters of iodized fatty acids (product)
        CONCEPT_REPLACEMENT_MAP_20180731.put("116878006", "767516006"); // Streptococcus pneumoniae serotype 1 antibody (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("120679007", "767519004"); // Streptococcus pneumoniae serotype 14 antibody (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("116870004", "767517002"); // Streptococcus pneumoniae serotype 3 antibody (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("116889004", "767518007"); // Streptococcus pneumoniae serotype 4 antibody (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("116931006", "120683007"); // Streptococcus pneumoniae serotype 51 antibody (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("116890008", "767520005"); // Streptococcus pneumoniae serotype 8 antibody (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("121030007", "120997001"); // Measles virus antigen (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("116876005", "116927000"); // Streptococcus pneumoniae serotype 56 antibody (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("116962005", "116933009"); // Streptococcus pneumoniae 26 antibody (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("709235006", "407004008"); // Antigen of Plasmodium (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("708786009", "388275001"); // Immunoglobulin E antibody to Setomelanomma rostrata (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("710524001", "763424002"); // Immunoglobulin G antibody to Streptococcus pneumoniae 23 (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("710533004", "763430002"); // Immunoglobulin G antibody to Streptococcus pneumoniae 9 (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("720303006", "763417003"); // Immunoglobulin G antibody to Streptococcus pneumoniae 17 (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("710521009", "763415006"); // Immunoglobulin G antibody to Streptococcus pneumoniae 12 (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("710525000", "763427009"); // Immunoglobulin G antibody to Streptococcus pneumoniae 26 (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("710530001", "763426000"); // Immunoglobulin G antibody to Streptococcus pneumoniae 6 (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("710528003", "763429007"); // Immunoglobulin G antibody to Streptococcus pneumoniae 51 (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("710531002", "763428004"); // Immunoglobulin G antibody to Streptococcus pneumoniae 7 (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("710523007", "763421005"); // Immunoglobulin G antibody to Streptococcus pneumoniae 19 (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("710529006", "763418008"); // Immunoglobulin G antibody to Streptococcus pneumoniae 56 (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("720308002", "767408004"); // Immunoglobulin G antibody to Streptococcus pneumoniae 34 (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("720304000", "763419000"); // Immunoglobulin G antibody to Streptococcus pneumoniae 18 (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("720307007", "763423008"); // Immunoglobulin G antibody to Streptococcus pneumoniae 22 (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("720306003", "763422003"); // Immunoglobulin G antibody to Streptococcus pneumoniae 20 (substance)
        CONCEPT_REPLACEMENT_MAP_20180731.put("720309005", "767402003"); // Immunoglobulin G antibody to Streptococcus pneumoniae 43 (substance)
    }

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
   private final boolean solorReleaseFormat;

   public ConceptWriter(List<String[]> conceptRecords, Semaphore writeSemaphore, 
           String message, ImportType importType, boolean solorReleaseFormat) {
      this.conceptRecords = conceptRecords;
      this.writeSemaphore = writeSemaphore;
      this.writeSemaphore.acquireUninterruptibly();
      this.solorReleaseFormat = solorReleaseFormat;
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

         if(this.solorReleaseFormat){
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
            final Status state = this.solorReleaseFormat
                    ? Status.fromZeroOneToken(conceptRecord[SRF_STATUS_INDEX])
                    : Status.fromZeroOneToken(conceptRecord[RF2_ACTIVE_INDEX]);
            if (state == Status.INACTIVE && importType == ImportType.ACTIVE_ONLY) {
                if (!CONCEPT_STRING_WHITELIST.contains(conceptRecord[RF2_CONCEPT_SCT_ID_INDEX])) {
                    continue;
                }
            }

            UUID conceptUuid, moduleUuid, legacyDefStatus;
            TemporalAccessor accessor;

            if(this.solorReleaseFormat){
               authorNid = identifierService.getNidForUuids(UUID.fromString(conceptRecord[SRF_AUTHOR_INDEX]));
               pathNid = identifierService.getNidForUuids(UUID.fromString(conceptRecord[SRF_PATH_INDEX]));
               conceptUuid = UUID.fromString(conceptRecord[SRF_ID_INDEX]);
               moduleUuid = UUID.fromString(conceptRecord[SRF_MODULE_INDEX]);
               legacyDefStatus = UUID.fromString(conceptRecord[SRF_DEF_STATUS_INDEX]);
               accessor = DateTimeFormatter.ISO_INSTANT.parse(
                       DirectImporter.getIsoInstant(conceptRecord[SRF_TIME_INDEX]));
            }else{
               conceptUuid = UuidT3Generator.fromSNOMED(conceptRecord[RF2_CONCEPT_SCT_ID_INDEX]);
                if (conceptRecord[RF2_CONCEPT_SCT_ID_INDEX].contains("782587007")) {
                    LOG.info("Found watch: |" + conceptRecord[RF2_CONCEPT_SCT_ID_INDEX] + "|" + conceptUuid.toString());
                }
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

            if(this.solorReleaseFormat){
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

            if(this.solorReleaseFormat){
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
            idVersion.setString(this.solorReleaseFormat ? conceptRecord[SRF_ID_INDEX]: conceptRecord[RF2_CONCEPT_SCT_ID_INDEX]);
            index(identifierToWrite);
            assemblageService.writeSemanticChronology(identifierToWrite);
            completedUnitOfWork();
         }

         return null;
      } catch (Throwable ex) {
          LOG.error(ex.getLocalizedMessage(), ex);
          throw ex;
      } finally {
         this.writeSemaphore.release();
         Get.activeTasks().remove(this);
      }
   }
}
