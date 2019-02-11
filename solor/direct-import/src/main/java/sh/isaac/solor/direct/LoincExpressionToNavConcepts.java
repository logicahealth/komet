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
import java.util.StringTokenizer;
import java.util.UUID;
import sh.isaac.MetaData;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.TaxonomyService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.Str1_Str2_Nid3_Nid4_Nid5_Version;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.  logic.LogicalExpressionBuilder;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;

/**
 *
 * @author kec
 */
public class LoincExpressionToNavConcepts extends TimedTaskWithProgressTracker<Void> {

    // https://confluence.ihtsdotools.org/display/DOCLOINC/4.2.2+LOINC+Term+to+Expression+Reference+Set
    ConceptProxy expressionRefset = new ConceptProxy(
            "LOINC Term to Expression reference set (foundation metadata concept)",
            UUID.fromString("0fb94c6f-7117-36ff-8789-c5cf9bf132fe"));

    ConceptProxy componentProxy = new ConceptProxy("Component (attribute)",
            UUID.fromString("8f0696db-210d-37ab-8fe1-d4f949892ac4"));

    ConceptProxy inheresInProxy = new ConceptProxy("Inheres in (attribute)",
            UUID.fromString("c0403a4d-aa15-35ef-ba57-1c244ea7bda0"));
    
    ConceptProxy processOutputProxy = new ConceptProxy("Process output (attribute)",
            UUID.fromString("ef18b2b0-dc77-3ed9-a80f-0386da11c8e5"));

    ConceptProxy methodProxy = new ConceptProxy("Method (attribute)",
            UUID.fromString("d0f9e3b1-29e4-399f-b129-36693ba4acbc"));

 ;

    private final List<IndexBuilderService> indexers;
    private final TaxonomyService taxonomyService;
    private final long commitTime = System.currentTimeMillis();

    private final NidSet components = new NidSet();
    private final NidSet systems = new NidSet();
    private final ManifoldCoordinate manifold;

    public LoincExpressionToNavConcepts(ManifoldCoordinate manifold) {
        this.taxonomyService = Get.taxonomyService();
        this.indexers = LookupService.get().getAllServices(IndexBuilderService.class);
        this.manifold = manifold;
        Get.activeTasks().add(this);
        updateTitle("Converting LOINC expressions to concepts");
        if (Get.identifierService().hasUuid(expressionRefset.getPrimordialUuid())) {
            addToTotalWork(Get.assemblageService().getSemanticCount(expressionRefset.getNid()));
        }
    }

    @Override
    protected Void call() throws Exception {
        try {
            int stamp = Get.stampService().getStampSequence(Status.ACTIVE,
                commitTime, TermAux.USER.getNid(),
                TermAux.SOLOR_OVERLAY_MODULE.getNid(),
                TermAux.DEVELOPMENT_PATH.getNid());
        if (!Get.identifierService().hasUuid(expressionRefset.getPrimordialUuid())) {
                return null;
            }
            Get.assemblageService().getSemanticChronologyStream(expressionRefset.getNid()).forEach((semanticChronology) -> {
                for (Version version : semanticChronology.getVersionList()) {
                    Str1_Str2_Nid3_Nid4_Nid5_Version loincVersion = (Str1_Str2_Nid3_Nid4_Nid5_Version) version;

                    String loincCode = loincVersion.getStr1(); // "48023-6"
                    String sctExpression = loincVersion.getStr2();

                    //  "363787002:246093002=720113009,370134009=123029007,246501002=702675006,704327008=122592007,370132008=117363000,704319004=50863008,704318007=705057003"
                    StringTokenizer tokenizer = new StringTokenizer(sctExpression, ":,={}()+", true);
                    processAssertions(tokenizer);
                    // get necessary or sufficient from Nid2 e.g. "Sufficient concept definition (SOLOR)"
//                    if (TermAux.SUFFICIENT_CONCEPT_DEFINITION.getNid() == loincVersion.getNid3()) {
//                        builder.sufficientSet(builder.and(processAssertions(tokenizer, builder)));
//                    } else {
//                        builder.necessarySet(builder.and(processAssertions(tokenizer, builder)));
//                    }
//                    LogicalExpression logicalExpression = builder.build();
//                    logicalExpression.getNodeCount();
//                    addLogicGraph(loincCode,
//                            logicalExpression);
                }
                completedUnitOfWork();
            }
            );
            ConceptBuilderService builderService = Get.conceptBuilderService();
            
            {
                addInheresInConcept(new ConceptProxy("Medication (SOLOR)", 
                        UUID.fromString("5032532f-6b58-31f9-84c1-4a365dde4449")).getNid(), 
                        builderService, stamp);
                addInheresInConcept(new ConceptProxy("Substance (SOLOR)", 
                        UUID.fromString("95f41098-8391-3f5e-9d61-4b019f1de99d")).getNid(), 
                        builderService, stamp);
                addInheresInConcept(new ConceptProxy("Body structure (SOLOR)", 
                        UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790")).getNid(), 
                        builderService, stamp);
                addInheresInConcept(new ConceptProxy("Organism (SOLOR)", 
                        UUID.fromString("0bab48ac-3030-3568-93d8-aee0f63bf072")).getNid(), 
                        builderService, stamp);
                addObservesComponent(new ConceptProxy("Organism (SOLOR)", 
                        UUID.fromString("0bab48ac-3030-3568-93d8-aee0f63bf072")).getNid(), 
                        builderService, stamp);
                addObservesComponent(new ConceptProxy("Body structure (SOLOR)", 
                        UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790")).getNid(), 
                        builderService, stamp);
                addObservesComponent(new ConceptProxy("Substance (SOLOR)", 
                        UUID.fromString("95f41098-8391-3f5e-9d61-4b019f1de99d")).getNid(), 
                        builderService, stamp);
                addObservesComponent(new ConceptProxy("Medication (SOLOR)", 
                        UUID.fromString("5032532f-6b58-31f9-84c1-4a365dde4449")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(new ConceptProxy("Ultrasound imaging - action (qualifier value)", 
                        UUID.fromString("c02fd67b-db30-3371-be95-0b7a94509a10")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(new ConceptProxy("Imaging - action (qualifier value)", 
                        UUID.fromString("627971bd-2f76-3b1f-a8a7-de93426cf3b7")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(new ConceptProxy("Cine imaging - action (qualifier value)", 
                        UUID.fromString("5f8b31cd-d8e7-3b7e-aeb9-7cacb06ec632")).getNid(), 
                        builderService, stamp);                
                addObservedByMethod(new ConceptProxy("Illumination - action (qualifier value)", 
                        UUID.fromString("28af16ae-e0d3-367a-a9ff-d4b003aafb07")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(new ConceptProxy("Magnetic resonance imaging - action (qualifier value)", 
                        UUID.fromString("0145be15-1dc4-313b-9321-e6db22285cfe")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(new ConceptProxy("Radiographic imaging - action (qualifier value)", 
                        UUID.fromString("99b82f89-ba3b-3b3f-a0ee-a3cdcc598168")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(new ConceptProxy("Radionuclide imaging - action (qualifier value)", 
                        UUID.fromString("1cb086ed-7128-3cec-8f1f-a81bfa521b18")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(new ConceptProxy("Thermography imaging - action (qualifier value)", 
                        UUID.fromString("6719f141-7b72-3461-9d5d-56f92dfa1600")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(new ConceptProxy("Video imaging - action (qualifier value)", 
                        UUID.fromString("bbb375cb-94cb-38e6-9d78-ef0cbb02f015")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(new ConceptProxy("Electrocardiographic procedure (procedure)", 
                        UUID.fromString("2dc7d2f4-1fc1-30d9-9d84-ac2202d98fb4")).getNid(), 
                        builderService, stamp);
            }
            
            
            for (int systemNid : systems.asArray()) {
                Optional<? extends Chronology> c = Get.identifiedObjectService().getChronology(systemNid);
                if (c.isPresent() && c.get().isLatestVersionActive()) {
                    addInheresInConcept(systemNid, builderService, stamp);
                }
                
            }
            for (int componentNid : components.asArray()) {
                Optional<? extends Chronology> c = Get.identifiedObjectService().getChronology(componentNid);
                if (c.isPresent() && c.get().isLatestVersionActive()) {
                    addObservesComponent(componentNid, builderService, stamp);
                }
            }
            return null;
        } finally {
            Get.activeTasks().remove(this);
        }
    }

    private void addObservedByMethod(int methodNid, ConceptBuilderService builderService, int stamp) throws NoSuchElementException, IllegalStateException {
        LogicalExpressionBuilder eb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
        eb.sufficientSet(eb.and(eb.conceptAssertion(MetaData.PHENOMENON____SOLOR),
                eb.someRole(MetaData.ROLE_GROUP____SOLOR,
                        eb.and(eb.someRole(methodProxy.getNid(), eb.conceptAssertion(methodNid))))));
                
        StringBuilder conceptNameBuilder = new StringBuilder();
        conceptNameBuilder.append("Phenomenon observed by ");
        conceptNameBuilder.append(manifold.getPreferredDescriptionText(methodNid));
        buildConcept(builderService, conceptNameBuilder, eb, stamp);
    }

    private void addObservesComponent(int componentNid, ConceptBuilderService builderService, int stamp) throws NoSuchElementException, IllegalStateException {
        LogicalExpressionBuilder eb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
        eb.sufficientSet(eb.and(eb.conceptAssertion(MetaData.PHENOMENON____SOLOR),
                eb.someRole(MetaData.ROLE_GROUP____SOLOR,
                        eb.and(eb.someRole(componentProxy.getNid(), eb.conceptAssertion(componentNid))))));
        
        eb.sufficientSet(eb.and(eb.conceptAssertion(MetaData.PHENOMENON____SOLOR),
                eb.someRole(MetaData.ROLE_GROUP____SOLOR,
                        eb.and(eb.someRole(processOutputProxy.getNid(), eb.conceptAssertion(componentNid))))));
        
        
        StringBuilder conceptNameBuilder = new StringBuilder();
        conceptNameBuilder.append(manifold.getPreferredDescriptionText(componentNid));
        conceptNameBuilder.append(" phenomenon");
        buildConcept(builderService, conceptNameBuilder, eb, stamp);
    }

    private void addInheresInConcept(int inheresInNid, ConceptBuilderService builderService, int stamp) throws IllegalStateException, NoSuchElementException {
        LogicalExpressionBuilder eb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
        eb.sufficientSet(eb.and(eb.conceptAssertion(MetaData.PHENOMENON____SOLOR),
                eb.someRole(MetaData.ROLE_GROUP____SOLOR,
                        eb.and(eb.someRole(inheresInProxy.getNid(), eb.conceptAssertion(inheresInNid))))));
        
        StringBuilder conceptNameBuilder = new StringBuilder();
        conceptNameBuilder.append("Inheres in ");
        conceptNameBuilder.append(manifold.getPreferredDescriptionText(inheresInNid));
        conceptNameBuilder.append(" phenomenon");
        buildConcept(builderService, conceptNameBuilder, eb, stamp);
    }

    private void buildConcept(ConceptBuilderService builderService, StringBuilder conceptNameBuilder, LogicalExpressionBuilder eb, int stamp) throws IllegalStateException, NoSuchElementException {
        String conceptName = conceptNameBuilder.toString();
        ConceptBuilder builder = builderService.getDefaultConceptBuilder(conceptName,
                "OP",
                eb.build(),
                TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
        builder.setPrimordialUuid(UuidT5Generator.get(UUID.fromString("d96cb408-b9ae-473d-a08d-ece06dbcedf9"), conceptName));
        List<Chronology> builtObjects = new ArrayList<>();
        builder.build(stamp, builtObjects);
        for (Chronology chronology : builtObjects) {
            Get.identifiedObjectService().putChronologyData(chronology);
            index(chronology);
        }
    }

    protected void processAssertions(StringTokenizer tokenizer) throws IllegalStateException {
        // nid 4: "Exact match map from SNOMED CT source code to target code (foundation metadata concept)"
        // nid 5: "Originally in LOINC (foundation metadata concept)"
        PARSE parseElement = PARSE.CONCEPT;
        while (parseElement == PARSE.CONCEPT) {
            String token = tokenizer.nextToken(); // SNOMED concept id
            int nid = Get.identifierService().getNidForUuids(UuidT3Generator.fromSNOMED(token));
            //assertions.add(builder.conceptAssertion(nid));
            if (tokenizer.hasMoreTokens()) {
                String delimiter = tokenizer.nextToken();
                switch (delimiter) {
                    case "+":
                        break;
                    case ":":
                        parseElement = PARSE.ROLE;
                        break;
                    default:
                        throw new IllegalStateException("1. Unexpected delimiter: " + delimiter);
                }
            } else {
                parseElement = PARSE.END;
            }
        }
        while (parseElement == PARSE.ROLE) {
            String token = tokenizer.nextToken(); // SNOMED concept id
            int nid = Get.identifierService().getNidForUuids(UuidT3Generator.fromSNOMED(token));
            String delimiter = tokenizer.nextToken();
            switch (delimiter) {
                case "=":
                    break;
                default:
                    throw new IllegalStateException("2. Unexpected delimiter: " + delimiter);
            }
            String token2 = tokenizer.nextToken(); // SNOMED concept id
            int nid2 = Get.identifierService().getNidForUuids(UuidT3Generator.fromSNOMED(token2));

            if (nid == componentProxy.getNid() || 
                    nid == processOutputProxy.getNid()) {
                components.add(nid2);
            } else if (nid == inheresInProxy.getNid()) {
                systems.add(nid2);
            }

            if (tokenizer.hasMoreTokens()) {
                delimiter = tokenizer.nextToken();
                switch (delimiter) {
                    case ",":
                        break;
                    default:
                        throw new IllegalStateException("3. Unexpected delimiter: " + delimiter);
                }
            } else {
                parseElement = PARSE.END;
            }
        }
    }

    private enum PARSE {
        CONCEPT, ROLE, END
    }

    private void index(Chronology chronicle) {
        if (chronicle instanceof SemanticChronology) {
            if (chronicle.getVersionType() == VersionType.LOGIC_GRAPH) {
                this.taxonomyService.updateTaxonomy((SemanticChronology) chronicle);
            }
        }
        for (IndexBuilderService indexer : indexers) {
            indexer.indexNow(chronicle);
        }

    }
 }
