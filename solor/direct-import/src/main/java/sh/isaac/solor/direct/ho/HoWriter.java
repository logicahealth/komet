/*
 * Copyright 2019 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.solor.direct.ho;

import java.util.ArrayList;
import java.util.HashMap;
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
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.assertions.ConceptAssertion;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.ModelGet;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import static sh.isaac.solor.direct.ho.HoDirectImporter.HUMAN_DX_MODULE;
import static sh.isaac.solor.direct.ho.HoDirectImporter.LEGACY_HUMAN_DX_ROOT_CONCEPT;

/**
 *
 * @author kec
 */
public class HoWriter extends TimedTaskWithProgressTracker<Void> {
    //Name	ref id	Parents	Abbreviations	Description	Is Diagnosis?	Is category?	Deprecated	icd_10_cm	icd_10_pcs	icd_9_cm	
    //icf	icpc	loinc	mdc	mesh	radlex	rx_cui	snomed_ct	ccs-single_category_icd_10	ccs-multi_level_1_icd_10	ccs-multi_level_2_icd_10
    //Inexact RxNormS	Sibling/Child	Inexact SNOMED	Sibling/Child

    public static final int NAME = 0;
    public static final int REFID = 1;
    public static final int PARENTS = 2;
    public static final int ABBREVIATIONS = 3;
    public static final int DESCRIPTION = 4;
    public static final int IS_DIAGNOSIS = 5;
    public static final int IS_CATEGORY = 6;
    public static final int DEPRECATED = 7;
    public static final int ICD10CM = 8;
    public static final int ICD10PCS = 9;
    public static final int ICD9CM = 10;
    public static final int ICF = 11;
    public static final int ICPC = 12;
    public static final int LOINC = 13;
    public static final int MDC = 14;
    public static final int MESH = 15;
    public static final int RADLEX = 16;
    public static final int RXCUI = 17;
    public static final int SNOMEDCT = 18;
    public static final int CCS_SINGLE_CAT_ICD = 19;
    public static final int CCS_MULTI_LEVEL_1_ICD = 20;
    public static final int CCS_MULTI_LEVEL_2_ICD = 21;
    public static final int INEXACT_RXNORM = 22;
    public static final int RXNORM_SIB_CHILD = 23;
    public static final int INEXACT_SNOMED = 24;
    public static final int SNOMED_SIB_CHILD = 25;


    private final List<String[]> hoRecords;
    private final Semaphore writeSemaphore;
    private final List<IndexBuilderService> indexers;
    private final long commitTime;
    private final IdentifierService identifierService = Get.identifierService();
    private final AssemblageService assemblageService = Get.assemblageService();

    public static UUID refidToUuid(String refid) {
        return UuidT5Generator.get(LEGACY_HUMAN_DX_ROOT_CONCEPT.getPrimordialUuid(), refid);
    }

    public HoWriter(List<String[]> hoRecords,
            Semaphore writeSemaphore, String message, long commitTime) {
        this.hoRecords = hoRecords;
        this.writeSemaphore = writeSemaphore;
        this.writeSemaphore.acquireUninterruptibly();
        this.commitTime = commitTime;
        indexers = LookupService.get().getAllServices(IndexBuilderService.class);
        updateTitle("Importing LOINC batch of size: " + hoRecords.size());
        updateMessage(message);
        addToTotalWork(hoRecords.size());
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
            int moduleNid = HUMAN_DX_MODULE.getNid();
            int conceptAssemblageNid = TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid();

            List<String[]> noSuchElementList = new ArrayList<>();
            HashMap<String, String> nameRefidMap = new HashMap<>();
            for (String[] hoRec : hoRecords) {
                nameRefidMap.put(hoRec[NAME], hoRec[REFID]);
                
            }

            // All deprecated records are filtered out already
            for (String[] hoRec : hoRecords) {
                try {

                    int recordStamp = stampService.getStampSequence(Status.ACTIVE, commitTime, authorNid, moduleNid, pathNid);
                    // See if the concept is created (from the SNOMED/LOINC expressions. 
                    UUID conceptUuid = refidToUuid(hoRec[REFID]);
                    int conceptNid = Get.nidWithAssignment(conceptUuid);
                    Optional<? extends ConceptChronology> optionalConcept = Get.conceptService().getOptionalConcept(conceptUuid);
                    if (!optionalConcept.isPresent()) {
                        
                        
                        int[] parentNids = new int[] { LEGACY_HUMAN_DX_ROOT_CONCEPT.getNid() };
                        if (hoRec[PARENTS] != null &! hoRec[PARENTS].isEmpty()) {
                            String[] parentRefIds = hoRec[PARENTS].split("; ");
                            parentNids = new int[parentRefIds.length];
                            for (int i = 0; i < parentRefIds.length; i++) {
                                String refId = nameRefidMap.get(parentRefIds[i]);
                                UUID parentUuid = refidToUuid(refId);
                                parentNids[i] = Get.nidWithAssignment(parentUuid);
                                ModelGet.identifierService().setupNid(parentNids[i], TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid(), IsaacObjectType.CONCEPT, VersionType.CONCEPT);
                            }
                        }
                        // Need to create new concept, and a stated definition...
                        buildConcept(conceptUuid, hoRec[NAME], recordStamp, parentNids);
                        
//                        LogicalExpressionBuilder builder = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
//                        
//                        ConceptAssertion[] conceptAssertions = new ConceptAssertion[parentNids.length];
//                        for (int i = 0; i < conceptAssertions.length; i++) {
//                            conceptAssertions[i] = builder.conceptAssertion(parentNids[i]);
//                        }
//                        builder.necessarySet(builder.and(conceptAssertions));
//                        LogicalExpression logicalExpression = builder.build();
//                        logicalExpression.getNodeCount();
//                        addLogicGraph(conceptUuid, hoRec[NAME],
//                                logicalExpression);

                    }
                    // make regular descriptions
 
//                    String shortName = hoRec[SHORTNAME];
//                    if (shortName == null || shortName.isEmpty()) {
//                        shortName = fullyQualifiedName + " with no sn";
//                    }
//
//                    addDescription(hoRec, shortName, TermAux.REGULAR_NAME_DESCRIPTION_TYPE, conceptUuid, recordStamp);



                } catch (NoSuchElementException ex) {
                    noSuchElementList.add(hoRec);
                }
                completedUnitOfWork();
            }
            if (!noSuchElementList.isEmpty()) {
                LOG.error("Continuing after import failed with no such element exception for record count: " + noSuchElementList.size());
            }
            return null;
        } finally {
            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }
    }

    protected void buildConcept(UUID conceptUuid, String conceptName, int stamp, int[] parentConceptNids) throws IllegalStateException, NoSuchElementException {
        LogicalExpressionBuilder eb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
        
        ConceptAssertion[] parents = new ConceptAssertion[parentConceptNids.length];
        for (int i = 0; i < parentConceptNids.length; i++) {
            parents[i] = eb.conceptAssertion(parentConceptNids[i]);
        }
        eb.necessarySet(eb.and(parents));
        ConceptBuilderService builderService = Get.conceptBuilderService();
        ConceptBuilder builder = builderService.getDefaultConceptBuilder(conceptName,
                "HO",
                eb.build(),
                TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
        builder.setPrimordialUuid(conceptUuid);
        List<Chronology> builtObjects = new ArrayList<>();
        builder.build(stamp, builtObjects);
        for (Chronology chronology : builtObjects) {
            Get.identifiedObjectService().putChronologyData(chronology);
            if (chronology.getVersionType() == VersionType.LOGIC_GRAPH) {
                try {
                    Get.taxonomyService().updateTaxonomy((SemanticChronology) chronology);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
           }
            index(chronology);
        }
    }
    
    
    
    private void addDescription(String[] hoRec, String description, ConceptSpecification descriptionType,
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
                MetaData.DESCRIPTION_INITIAL_CHARACTER_CASE_SENSITIVE____SOLOR.getNid());
        descriptionVersion.setDescriptionTypeConceptNid(descriptionTypeNid);
        descriptionVersion.setLanguageConceptNid(TermAux.ENGLISH_LANGUAGE.getNid());
        descriptionVersion.setText(description);

        index(descriptionToWrite);
        assemblageService.writeSemanticChronology(descriptionToWrite);

        UUID acceptabilityUuid = UuidT5Generator.get(TermAux.US_DIALECT_ASSEMBLAGE.getPrimordialUuid(),
                hoRec[REFID] + description);
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

    /**
     * Adds the logic graph.
     *
     * @param conceptUuid the uuid of the concept this logic graph defines.
     * @param conceptName
     * @param logicalExpression the logical expression
     */
    private void addLogicGraph(UUID conceptUuid, String conceptName,
            LogicalExpression logicalExpression) {

        ConceptBuilderService builderService = Get.conceptBuilderService();
        ConceptBuilder builder = builderService.getDefaultConceptBuilder(conceptName,
                "HO",
                logicalExpression,
                TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
        builder.setPrimordialUuid(conceptUuid);
        
        int stamp = Get.stampService().getStampSequence(Status.ACTIVE,
                commitTime, TermAux.USER.getNid(),
                HUMAN_DX_MODULE.getNid(),
                TermAux.DEVELOPMENT_PATH.getNid());
        Optional<? extends ConceptChronology> optionalConcept = Get.conceptService().getOptionalConcept(conceptUuid);
        if (!optionalConcept.isPresent()) {
            ConceptChronologyImpl conceptToWrite
                    = new ConceptChronologyImpl(conceptUuid, TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
            conceptToWrite.createMutableVersion(stamp);
            Get.conceptService().writeConcept(conceptToWrite);
            index(conceptToWrite);
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

        final ArrayList<Chronology> builtObjects = new ArrayList<>();

        builder.build(stamp, builtObjects);
        for (Chronology chronology : builtObjects) {
            Get.identifiedObjectService().putChronologyData(chronology);
            index(chronology);
        }

    }

}
