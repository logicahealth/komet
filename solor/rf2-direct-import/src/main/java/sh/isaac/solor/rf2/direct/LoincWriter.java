/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import org.apache.mahout.math.Arrays;
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
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.model.semantic.version.brittle.LoincVersionImpl;

/**
 *
 * @author kec
 */
public class LoincWriter extends TimedTaskWithProgressTracker<Void> {

    /*
"LOINC_NUM","COMPONENT","PROPERTY","TIME_ASPCT","SYSTEM","SCALE_TYP","METHOD_TYP","CLASS","VersionLastChanged","CHNG_TYPE","DefinitionDescription","STATUS","CONSUMER_NAME","CLASSTYPE","FORMULA","SPECIES","EXMPL_ANSWERS","SURVEY_QUEST_TEXT","SURVEY_QUEST_SRC","UNITSREQUIRED","SUBMITTED_UNITS","RELATEDNAMES2","SHORTNAME","ORDER_OBS","CDISC_COMMON_TESTS","HL7_FIELD_SUBFIELD_ID","EXTERNAL_COPYRIGHT_NOTICE","EXAMPLE_UNITS","LONG_COMMON_NAME","UnitsAndRange","DOCUMENT_SECTION","EXAMPLE_UCUM_UNITS","EXAMPLE_SI_UCUM_UNITS","STATUS_REASON","STATUS_TEXT","CHANGE_REASON_PUBLIC","COMMON_TEST_RANK","COMMON_ORDER_RANK","COMMON_SI_TEST_RANK","HL7_ATTACHMENT_STRUCTURE","EXTERNAL_COPYRIGHT_LINK","PanelType","AskAtOrderEntry","AssociatedObservations","VersionFirstReleased","ValidHL7AttachmentRequest"
"10013-1","R' wave amplitude.lead I","Elpot","Pt","Heart","Qn","EKG","EKG.MEAS","2.48","MIN",,"ACTIVE",,2,,,,,,"Y",,"Cardiac; ECG; EKG.MEASUREMENTS; Electrical potential; Electrocardiogram; Electrocardiograph; Hrt; Painter's colic; PB; Plumbism; Point in time; QNT; Quan; Quant; Quantitative; R prime; R' wave Amp L-I; R wave Amp L-I; Random; Right; Voltage","R' wave Amp L-I","Observation",,,,"mV","R' wave amplitude in lead I",,,"mV",,,,,0,0,0,,,,,,"1.0i",
"10014-9","R' wave amplitude.lead II","Elpot","Pt","Heart","Qn","EKG","EKG.MEAS","2.48","MIN",,"ACTIVE",,2,,,,,,"Y",,"2; Cardiac; ECG; EKG.MEASUREMENTS; Electrical potential; Electrocardiogram; Electrocardiograph; Hrt; Painter's colic; PB; Plumbism; Point in time; QNT; Quan; Quant; Quantitative; R prime; R' wave Amp L-II; R wave Amp L-II; Random; Right; Voltage","R' wave Amp L-II","Observation",,,,"mV","R' wave amplitude in lead II",,,"mV",,,,,0,0,0,,,,,,"1.0i",
     */
    private static final int LOINC_NUM = 0;
    private static final int COMPONENT = 1;
    private static final int PROPERTY = 2;
    private static final int TIME_ASPCT = 3;
    private static final int SYSTEM = 4;
    private static final int SCALE_TYP = 5;
    private static final int METHOD_TYP = 6;
    private static final int CLASS = 7;
    private static final int VERSION_LAST_CHANGED = 8;
    private static final int CHNG_TYPE = 9;
    private static final int DEFINITION_DESCRIPTION = 10;
    private static final int STATUS = 11;
    private static final int CONSUMER_NAME = 12;
    private static final int CLASSTYPE = 13;
    private static final int FORMULA = 14;
    private static final int SPECIES = 15;
    private static final int EXMPL_ANSWERS = 16;
    private static final int SURVEY_QUEST_TEXT = 17;
    private static final int SURVEY_QUEST_SRC = 18;
    private static final int UNITSREQUIRED = 19;
    private static final int SUBMITTED_UNITS = 20;
    private static final int RELATEDNAMES2 = 21;
    private static final int SHORTNAME = 22;
    private static final int ORDER_OBS = 23;
    private static final int CDISC_COMMON_TESTS = 24;
    private static final int HL7_FIELD_SUBFIELD_ID = 25;
    private static final int EXTERNAL_COPYRIGHT_NOTICE = 26;
    private static final int EXAMPLE_UNITS = 27;
    private static final int LONG_COMMON_NAME = 28;
    private static final int UNITS_AND_RANGE = 29;
    private static final int DOCUMENT_SECTION = 30;
    private static final int EXAMPLE_UCUM_UNITS = 31;
    private static final int EXAMPLE_SI_UCUM_UNITS = 32;
    private static final int STATUS_REASON = 33;
    private static final int STATUS_TEXT = 34;
    private static final int CHANGE_REASON_PUBLIC = 35;
    private static final int COMMON_TEST_RANK = 36;
    private static final int COMMON_ORDER_RANK = 37;
    private static final int COMMON_SI_TEST_RANK = 38;
    private static final int HL7_ATTACHMENT_STRUCTURE = 39;
    private static final int EXTERNAL_COPYRIGHT_LINK = 40;
    private static final int PANEL_TYPE = 41;
    private static final int ASK_AT_ORDER_ENTRY = 42;
    private static final int ASSOCIATED_OBSERVATIONS = 43;
    private static final int VERSION_FIRST_RELEASED = 44;
    private static final int VALID_HL7_ATTACHMENT_REQUEST = 45;

    private final List<String[]> loincRecords;
    private final Semaphore writeSemaphore;
    private final List<IndexBuilderService> indexers;
    private final long commitTime;
    private final IdentifierService identifierService = Get.identifierService();
    private final AssemblageService assemblageService = Get.assemblageService();

    public LoincWriter(List<String[]> loincRecordsRecords,
            Semaphore writeSemaphore, String message, long commitTime) {
        this.loincRecords = loincRecordsRecords;
        this.writeSemaphore = writeSemaphore;
        this.writeSemaphore.acquireUninterruptibly();
        this.commitTime = commitTime;
        indexers = LookupService.get().getAllServices(IndexBuilderService.class);
        updateTitle("Importing LOINC batch of size: " + loincRecordsRecords.size());
        updateMessage(message);
        addToTotalWork(loincRecordsRecords.size());
        Get.activeTasks().add(this);
    }

    private void index(Chronology chronicle) {
        for (IndexBuilderService indexer : indexers) {
           indexer.indexNow(chronicle);
        }
    }

    @Override
    protected Void call() throws Exception {
        try {
            ConceptService conceptService = Get.conceptService();
            StampService stampService = Get.stampService();
            int authorNid = TermAux.USER.getNid();
            int pathNid = TermAux.DEVELOPMENT_PATH.getNid();
            int moduleNid = MetaData.LOINC_MODULES____SOLOR.getNid();
            int conceptAssemblageNid = TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid();

         List<String[]> noSuchElementList = new ArrayList<>();

         for (String[] loincRecord : loincRecords) {
                try {
                    
                    if (loincRecord[STATUS].equals("ACTIVE")) {
                        
                        int recordStamp = stampService.getStampSequence(Status.ACTIVE, commitTime, authorNid, moduleNid, pathNid);
                        // See if the concept is created (from the SNOMED/LOINC expressions. 
                        UUID conceptUuid = UuidT5Generator.loincConceptUuid(loincRecord[LOINC_NUM]);
                        int conceptNid = identifierService.getNidForUuids(conceptUuid);
                        Optional<? extends ConceptChronology> optionalConcept = Get.conceptService().getOptionalConcept(conceptUuid);
                        if (optionalConcept.isPresent()) {
                            // only import the ones with expressions already imported...
                            // make 2 LOINC descriptions
                            String longCommonName = loincRecord[LONG_COMMON_NAME];
                            if (longCommonName == null || longCommonName.isEmpty()) {
                                longCommonName = loincRecord[LOINC_NUM] + " with no lcn";
                            }
                            
                            addDescription(loincRecord, longCommonName,
                                    TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE, conceptUuid, recordStamp);
                            
                            String shortName = loincRecord[SHORTNAME];
                            if (shortName == null || shortName.isEmpty()) {
                                shortName = longCommonName + " with no sn";
                            }
                            
                            addDescription(loincRecord, shortName, TermAux.REGULAR_NAME_DESCRIPTION_TYPE, conceptUuid, recordStamp);

                            // make a LOINC semantic
                            UUID loincRecordUuid = UuidT5Generator.get(TermAux.LOINC_RECORD_ASSEMBLAGE.getPrimordialUuid(),
                                    loincRecord[LOINC_NUM]);
                            
                            SemanticChronologyImpl recordToWrite
                                    = new SemanticChronologyImpl(VersionType.LOINC_RECORD, loincRecordUuid,
                                            TermAux.LOINC_RECORD_ASSEMBLAGE.getNid(), conceptNid);
                            LoincVersionImpl recordVersion = recordToWrite.createMutableVersion(recordStamp);
                            recordVersion.setComponent(loincRecord[COMPONENT]);
                            recordVersion.setLoincNum(loincRecord[LOINC_NUM]);
                            recordVersion.setLoincStatus(loincRecord[STATUS]);
                            recordVersion.setLongCommonName(loincRecord[LONG_COMMON_NAME]);
                            recordVersion.setMethodType(loincRecord[METHOD_TYP]);
                            recordVersion.setProperty(loincRecord[PROPERTY]);
                            recordVersion.setScaleType(loincRecord[SCALE_TYP]);
                            recordVersion.setShortName(loincRecord[SHORTNAME]);
                            recordVersion.setSystem(loincRecord[SYSTEM]);
                            recordVersion.setTimeAspect(loincRecord[TIME_ASPCT]);
                            assemblageService.writeSemanticChronology(recordToWrite);
                        }
                        
                    }
             } catch (NoSuchElementException ex) {
                 noSuchElementList.add(loincRecord);
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

    private void addDescription(String[] loincRecord, String description, ConceptSpecification descriptionType,
            UUID conceptUuid, int recordStamp) {

        UUID descriptionUuid
                = UuidT5Generator.get(MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid(),
                        descriptionType.toString() + conceptUuid.toString() + description);

        int descriptionTypeNid = descriptionType.getNid();
        int conceptNid = identifierService.getNidForUuids(conceptUuid);

        SemanticChronologyImpl descriptionToWrite
                = new SemanticChronologyImpl(VersionType.DESCRIPTION, descriptionUuid, 
                        MetaData.ENGLISH_LANGUAGE____SOLOR.getNid(), conceptNid);
        DescriptionVersionImpl descriptionVersion = descriptionToWrite.createMutableVersion(recordStamp);
        descriptionVersion.setCaseSignificanceConceptNid(
                MetaData.DESCRIPTION_INITIAL_CHARACTER_SENSITIVE____SOLOR.getNid());
        descriptionVersion.setDescriptionTypeConceptNid(descriptionTypeNid);
        descriptionVersion.setLanguageConceptNid(TermAux.ENGLISH_LANGUAGE.getNid());
        descriptionVersion.setText(description);

        index(descriptionToWrite);
        assemblageService.writeSemanticChronology(descriptionToWrite);

        UUID acceptabilityUuid = UuidT5Generator.get(TermAux.US_DIALECT_ASSEMBLAGE.getPrimordialUuid(),
                loincRecord[LOINC_NUM] + description);
        SemanticChronologyImpl dialectToWrite = new SemanticChronologyImpl(
                VersionType.COMPONENT_NID,
                acceptabilityUuid,
                TermAux.US_DIALECT_ASSEMBLAGE.getNid(),
                descriptionToWrite.getNid());
        ComponentNidVersionImpl dialectVersion = dialectToWrite.createMutableVersion(recordStamp);
        dialectVersion.setComponentNid(TermAux.ACCEPTABLE.getNid());
        index(dialectToWrite);
        assemblageService.writeSemanticChronology(dialectToWrite);
    }
}
