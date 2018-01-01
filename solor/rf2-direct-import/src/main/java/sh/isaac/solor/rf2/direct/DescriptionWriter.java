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
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;

/**
 *
 * @author kec
 */
public class DescriptionWriter extends TimedTaskWithProgressTracker<Void> {

   /*
id	effectiveTime	active	moduleId	conceptId	languageCode	typeId	term	caseSignificanceId
101013	20020131	1	900000000000207008	126813005	en	900000000000013009	Neoplasm of anterior aspect of epiglottis	900000000000020002
101013	20170731	1	900000000000207008	126813005	en	900000000000013009	Neoplasm of anterior aspect of epiglottis	900000000000448009
102018	20020131	1	900000000000207008	126814004	en	900000000000013009	Neoplasm of junctional region of epiglottis	900000000000020002
102018	20170731	1	900000000000207008	126814004	en	900000000000013009	Neoplasm of junctional region of epiglottis	900000000000448009
    */

   private static final int DESCRIPITON_SCT_ID_INDEX = 0;
   private static final int EFFECTIVE_TIME_INDEX = 1;
   private static final int ACTIVE_INDEX = 2; // 0 == false, 1 == true
   private static final int MODULE_SCTID_INDEX = 3;
   private static final int REFERENCED_CONCEPT_SCT_ID_INDEX = 4;
   private static final int LANGUGE_CODE_INDEX = 5;
   private static final int DESCRIPTION_TYPE_SCT_ID_INDEX = 6;
   private static final int DESCRIPTION_TEXT_INDEX = 7;
   private static final int CASE_SIGNIFICANCE_INDEX = 8;

   private final List<String[]> descriptionRecords;
   private final Semaphore writeSemaphore;
   private final List<IndexBuilderService> indexers;

   public DescriptionWriter(List<String[]> descriptionRecords, Semaphore writeSemaphore, String message) {
      this.descriptionRecords = descriptionRecords;
      this.writeSemaphore = writeSemaphore;
      this.writeSemaphore.acquireUninterruptibly();
      indexers = LookupService.get().getAllServices(IndexBuilderService.class);
      updateTitle("Importing description batch of size: " + descriptionRecords.size());
      updateMessage(message);
      addToTotalWork(descriptionRecords.size());
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
         AssemblageService assemblageService = Get.assemblageService();
         IdentifierService identifierService = Get.identifierService();
         StampService stampService = Get.stampService();
         int sctIdentifierAssemblageNid = TermAux.SCT_IDENTIFIER_ASSEMBLAGE.getNid();
         int authorNid = TermAux.USER.getNid();
         int pathNid = TermAux.DEVELOPMENT_PATH.getNid();

         for (String[] descriptionRecord : descriptionRecords) {
            int descriptionAssemblageNid = LanguageCoordinates.iso639toDescriptionAssemblageNid(descriptionRecord[LANGUGE_CODE_INDEX]);
            int languageNid = LanguageCoordinates.iso639toConceptNid(descriptionRecord[LANGUGE_CODE_INDEX]);
            UUID descriptionUuid = UuidT3Generator.fromSNOMED(descriptionRecord[DESCRIPITON_SCT_ID_INDEX]);
            UUID moduleUuid = UuidT3Generator.fromSNOMED(descriptionRecord[MODULE_SCTID_INDEX]);
            Status state = Status.fromZeroOneToken(descriptionRecord[ACTIVE_INDEX]);
            UUID referencedConceptUuid = UuidT3Generator.fromSNOMED(descriptionRecord[REFERENCED_CONCEPT_SCT_ID_INDEX]);
            UUID caseSignificanceUuid = UuidT3Generator.fromSNOMED(descriptionRecord[CASE_SIGNIFICANCE_INDEX]);
            UUID descriptionTypeUuid = UuidT3Generator.fromSNOMED(descriptionRecord[DESCRIPTION_TYPE_SCT_ID_INDEX]);
            // '2011-12-03T10:15:30Z'

            TemporalAccessor accessor = DateTimeFormatter.ISO_INSTANT.parse(Rf2DirectImporter.getIsoInstant(descriptionRecord[EFFECTIVE_TIME_INDEX]));
            long time = accessor.getLong(INSTANT_SECONDS) * 1000;
            
            // add to description assemblage
            int descriptionNid = identifierService.getNidForUuids(descriptionUuid);
            int moduleNid = identifierService.getNidForUuids(moduleUuid);
            int referencedConceptNid = identifierService.getNidForUuids(referencedConceptUuid);
            int caseSignificanceNid = identifierService.getNidForUuids(caseSignificanceUuid);
            int descriptionTypeNid = identifierService.getNidForUuids(descriptionTypeUuid);
                        
            SemanticChronologyImpl descriptionToWrite = 
                    new SemanticChronologyImpl(VersionType.DESCRIPTION, descriptionUuid, descriptionNid, 
                            descriptionAssemblageNid, referencedConceptNid);
            int conceptStamp = stampService.getStampSequence(state, time, authorNid, moduleNid, pathNid);
            DescriptionVersionImpl descriptionVersion = descriptionToWrite.createMutableVersion(conceptStamp);
            descriptionVersion.setCaseSignificanceConceptNid(caseSignificanceNid);
            descriptionVersion.setDescriptionTypeConceptNid(descriptionTypeNid);
            descriptionVersion.setLanguageConceptNid(languageNid);
            descriptionVersion.setText(descriptionRecord[DESCRIPTION_TEXT_INDEX]);
            
            index(descriptionToWrite);
            assemblageService.writeSemanticChronology(descriptionToWrite);
            
            // add to sct identifier assemblage
            UUID sctIdentifierUuid = UuidT5Generator.get(TermAux.SCT_IDENTIFIER_ASSEMBLAGE.getPrimordialUuid(), 
                    descriptionRecord[DESCRIPITON_SCT_ID_INDEX]);
            int sctIdentifierNid = identifierService.getNidForUuids(sctIdentifierUuid);
            SemanticChronologyImpl sctIdentifierToWrite = new SemanticChronologyImpl(VersionType.STRING,
                               sctIdentifierUuid,
                               sctIdentifierNid,
                               sctIdentifierAssemblageNid,
                               descriptionNid);
            
            StringVersionImpl sctIdVersion = sctIdentifierToWrite.createMutableVersion(conceptStamp);
            sctIdVersion.setString(descriptionRecord[DESCRIPITON_SCT_ID_INDEX]);
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
         this.done();
         Get.activeTasks().remove(this);
      }
   }
}
