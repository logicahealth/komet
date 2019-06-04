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
package sh.isaac.solor.direct;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import sh.isaac.MetaData;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.bootstrap.TestConcept;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;
import sh.isaac.model.semantic.version.brittle.LoincVersionImpl;

/**
 *
 * @author kec
 */
public class LoincWriter extends TimedTaskWithProgressTracker<Void> {

    /*
    0: "10014-9",
    1: "R' wave amplitude.lead II",
    2: "Elpot",
    3: "Pt",
    4: "Heart",
    5: "Qn",
    6: "EKG",
    7: "EKG.MEAS",
    8: "2.48",
    9: "MIN",
    10: ,
    11: "ACTIVE",
    12: ,
    13: 2,
    14: ,
    15: ,
    16: ,
    17: ,
    18: ,
    19: "Y",
    20: ,
    21: "2; Cardiac; ECG; EKG.MEASUREMENTS; Electrical potential; 
         Electrocardiogram; Electrocardiograph; Hrt; Painter's colic; PB; 
         Plumbism; Point in time; QNT; Quan; Quant; Quantitative; R prime; 
         R' wave Amp L-II; R wave Amp L-II; Random; Right; Voltage",
    22: "R' wave Amp L-II","Observation",
    23: ,
    24: ,
    25: ,
    26: "mV",
    27: "R' wave amplitude in lead II"
    28: ,
    29: ,
    30: ,
    31: "mV",
    32: ,
    33: ,
    34: ,
    35: ,
    36: 0,
    37: 0,
    38: 0,
    39: ,
    40: ,
    41: ,
    42:,
    43:,
    44: "1.0i",
    45:
     */
    public static final int LOINC_NUM = 0;
    public static final int COMPONENT = 1;
    public static final int PROPERTY = 2;
    public static final int TIME_ASPCT = 3;
    public static final int SYSTEM = 4;
    public static final int SCALE_TYP = 5;
    public static final int METHOD_TYP = 6;
    public static final int CLASS = 7;
    public static final int VERSION_LAST_CHANGED = 8;
    public static final int CHNG_TYPE = 9;
    public static final int DEFINITION_DESCRIPTION = 10;
    public static final int STATUS = 11;
    public static final int CONSUMER_NAME = 12;
    public static final int CLASSTYPE = 13;
    public static final int FORMULA = 14;
    public static final int SPECIES = 15;
    public static final int EXMPL_ANSWERS = 16;
    public static final int SURVEY_QUEST_TEXT = 17;
    public static final int SURVEY_QUEST_SRC = 18;
    public static final int UNITSREQUIRED = 19;
    public static final int SUBMITTED_UNITS = 20;
    public static final int RELATEDNAMES2 = 21;
    public static final int SHORTNAME = 22;
    public static final int ORDER_OBS = 23;
    public static final int CDISC_COMMON_TESTS = 24;
    public static final int HL7_FIELD_SUBFIELD_ID = 25;
    public static final int EXTERNAL_COPYRIGHT_NOTICE = 26;
    public static final int EXAMPLE_UNITS = 27;
    public static final int LONG_COMMON_NAME = 28;
    public static final int UNITS_AND_RANGE = 29;
    public static final int DOCUMENT_SECTION = 30;
    public static final int EXAMPLE_UCUM_UNITS = 31;
    public static final int EXAMPLE_SI_UCUM_UNITS = 32;
    public static final int STATUS_REASON = 33;
    public static final int STATUS_TEXT = 34;
    public static final int CHANGE_REASON_PUBLIC = 35;
    public static final int COMMON_TEST_RANK = 36;
    public static final int COMMON_ORDER_RANK = 37;
    public static final int COMMON_SI_TEST_RANK = 38;
    public static final int HL7_ATTACHMENT_STRUCTURE = 39;
    public static final int EXTERNAL_COPYRIGHT_LINK = 40;
    public static final int PANEL_TYPE = 41;
    public static final int ASK_AT_ORDER_ENTRY = 42;
    public static final int ASSOCIATED_OBSERVATIONS = 43;
    public static final int VERSION_FIRST_RELEASED = 44;
    public static final int VALID_HL7_ATTACHMENT_REQUEST = 45;

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
            AxiomsFromLoincRecord loincAxiomMaker = new AxiomsFromLoincRecord();

            List<String[]> noSuchElementList = new ArrayList<>();

            for (String[] loincRecord : loincRecords) {
                try {

                    if (loincRecord[STATUS].equals("ACTIVE")) {

                        int recordStamp = stampService.getStampSequence(Status.ACTIVE, commitTime, authorNid, moduleNid, pathNid);
                        // See if the concept is created (from the SNOMED/LOINC expressions. 
                        UUID conceptUuid = UuidT5Generator.loincConceptUuid(loincRecord[LOINC_NUM]);
                        int conceptNid = Get.nidWithAssignment(conceptUuid);
                        Optional<? extends ConceptChronology> optionalConcept = Get.conceptService().getOptionalConcept(conceptUuid);
                        if (!optionalConcept.isPresent()) {

                            // Need to create new concept, and a stated definition...
                            LogicalExpressionBuilder builder = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
                            addAxioms(builder, loincRecord, loincAxiomMaker);
                            
                            
                            LogicalExpression logicalExpression = builder.build();
                            logicalExpression.getNodeCount();
                            addLogicGraph(loincRecord[LOINC_NUM],
                                    logicalExpression);

                        }
                        // make 2 LOINC descriptions
                        String longCommonName = loincRecord[LONG_COMMON_NAME];
                        if (longCommonName == null || longCommonName.isEmpty()) {
                            longCommonName = loincRecord[LOINC_NUM] + " with no lcn";
                        }

                        addDescription(loincRecord, longCommonName,
                                TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE, conceptUuid, recordStamp, true);

                        addDescription(loincRecord, longCommonName,
                                TermAux.REGULAR_NAME_DESCRIPTION_TYPE, conceptUuid, recordStamp, true);

                        String shortName = loincRecord[SHORTNAME];
                        if (shortName == null || shortName.isEmpty()) {
                            shortName = longCommonName + " with no sn";
                        }

                        addDescription(loincRecord, shortName, TermAux.REGULAR_NAME_DESCRIPTION_TYPE, conceptUuid, recordStamp, false);

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
                } catch (NoSuchElementException ex) {
                    noSuchElementList.add(loincRecord);
                }
                completedUnitOfWork();
            }
            if (!noSuchElementList.isEmpty()) {
                LOG.error("Continuing after import failed with no such element exception for record count: " + noSuchElementList.size());
            }
            loincAxiomMaker.listMethods();
            return null;
        } finally {
            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }
    }

    private void addAxioms(LogicalExpressionBuilder builder, String[] loincRecord, AxiomsFromLoincRecord loincAxiomMaker) {
        Assertion[] assertions = loincAxiomMaker.make(builder, loincRecord);
        if (assertions.length == 0) {
            builder.necessarySet(builder.and(builder.conceptAssertion(MetaData.UNCATEGORIZED_PHENOMENON____SOLOR)));
        } else {
            builder.necessarySet(builder.and(assertions));
        }
    }

    private void addDescription(String[] loincRecord, String description, ConceptSpecification descriptionType,
            UUID conceptUuid, int recordStamp, boolean preferredInDialect) {

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
                MetaData.DESCRIPTION_INITIAL_CHARACTER_CASE_SENSITIVE____SOLOR.getNid());
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
        if (preferredInDialect) {
            dialectVersion.setComponentNid(TermAux.PREFERRED.getNid());
        } else {
            dialectVersion.setComponentNid(TermAux.ACCEPTABLE.getNid());
        }

        index(dialectToWrite);
        assemblageService.writeSemanticChronology(dialectToWrite);
    }
    

    /**
     * Adds the logic graph.
     *
     * @param loincCode the LOINC code
     * @param logicalExpression the logical expression
     * @return the semantic chronology
     */
    public SemanticChronology addLogicGraph(String loincCode,
            LogicalExpression logicalExpression) {

        int stamp = Get.stampService().getStampSequence(Status.ACTIVE,
                commitTime, TermAux.USER.getNid(),
                TermAux.SOLOR_OVERLAY_MODULE.getNid(),
                TermAux.DEVELOPMENT_PATH.getNid());
        UUID conceptUuid = UuidT5Generator.loincConceptUuid(loincCode);
        Optional<? extends ConceptChronology> optionalConcept = Get.conceptService().getOptionalConcept(conceptUuid);
        if (!optionalConcept.isPresent()) {
            ConceptChronologyImpl conceptToWrite
                    = new ConceptChronologyImpl(conceptUuid, TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
            conceptToWrite.createMutableVersion(stamp);
            Get.conceptService().writeConcept(conceptToWrite);
            index(conceptToWrite);

            // add to loinc identifier assemblage
            UUID loincIdentifierUuid = UuidT5Generator.get(MetaData.LOINC_ID_ASSEMBLAGE____SOLOR.getPrimordialUuid(),
                    loincCode);
            SemanticChronologyImpl loincIdentifierToWrite = new SemanticChronologyImpl(VersionType.STRING,
                    loincIdentifierUuid,
                    MetaData.LOINC_ID_ASSEMBLAGE____SOLOR.getNid(),
                    conceptToWrite.getNid());

            StringVersionImpl loincIdVersion = loincIdentifierToWrite.createMutableVersion(stamp);
            loincIdVersion.setString(loincCode);
            index(loincIdentifierToWrite);
            Get.assemblageService().writeSemanticChronology(loincIdentifierToWrite);
        }

        int graphAssemblageNid = TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid();

        final SemanticBuilder sb = Get.semanticBuilderService().getLogicalExpressionBuilder(logicalExpression,
              Get.identifierService().getNidForUuids(conceptUuid),
                graphAssemblageNid);

        UUID nameSpace = TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getPrimordialUuid();

        // Create UUID from seed and assign SemanticBuilder the value
        final UUID generatedGraphPrimordialUuid
                = UuidT5Generator.get(nameSpace, conceptUuid.toString());

        sb.setPrimordialUuid(generatedGraphPrimordialUuid);

        final ArrayList<IsaacExternalizable> builtObjects = new ArrayList<>();

        final SemanticChronology sci = (SemanticChronology) sb.build(stamp,
                builtObjects);
        // There should be no other build objects, so ignore the builtObjects list...

        if (builtObjects.size() != 1) {
            throw new IllegalStateException("More than one build object: " + builtObjects);
        }
        index(sci);
        Get.assemblageService().writeSemanticChronology(sci);

        return sci;

    }
    
}
