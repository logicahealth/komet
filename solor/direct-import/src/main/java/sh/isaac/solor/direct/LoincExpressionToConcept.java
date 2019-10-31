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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.Str1_Str2_Nid3_Nid4_Nid5_Version;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;

/**
 *
 * @author kec
 */
public class LoincExpressionToConcept extends TimedTaskWithProgressTracker<Void> {

    // https://confluence.ihtsdotools.org/display/DOCLOINC/4.2.2+LOINC+Term+to+Expression+Reference+Set
    ConceptProxy expressionRefset = new ConceptProxy(
            "LOINC Term to Expression reference set (foundation metadata concept)",
            UUID.fromString("0fb94c6f-7117-36ff-8789-c5cf9bf132fe"));
    ConceptProxy directSiteProxy = new ConceptProxy("Direct site (attribute)",
            UUID.fromString("a6eebe78-7ca6-309e-94d6-cf797a786cd9"));
    ConceptProxy hasSpecimenProxy = new ConceptProxy("Has specimen (attribute)",
            UUID.fromString("5ce3e93b-8594-3d38-b410-b06039e63e3c"));

    private final List<IndexBuilderService> indexers;
    private final TaxonomyService taxonomyService;
    private final long commitTime = System.currentTimeMillis();
    private final Transaction transaction;

    public LoincExpressionToConcept(Transaction transaction) {
        this.transaction = transaction;
        this.taxonomyService = Get.taxonomyService();
        this.indexers = LookupService.get().getAllServices(IndexBuilderService.class);
        Get.activeTasks().add(this);
        updateTitle("Converting LOINC expressions to concepts");
        if (Get.identifierService().hasUuid(expressionRefset.getPrimordialUuid())) {
           addToTotalWork(Get.assemblageService().getSemanticCount(expressionRefset.getNid()));
        }
    }

    @Override
    protected Void call() throws Exception {
        try {
            if (!Get.identifierService().hasUuid(expressionRefset.getPrimordialUuid())) {
                return null;
            }
            Get.assemblageService().getSemanticChronologyStream(expressionRefset.getNid()).parallel().forEach((semanticChronology) -> {
                for (Version version : semanticChronology.getVersionList()) {
                    Str1_Str2_Nid3_Nid4_Nid5_Version loincVersion = (Str1_Str2_Nid3_Nid4_Nid5_Version) version;
                    
                    String loincCode = loincVersion.getStr1(); // "48023-6"
                    String sctExpression = loincVersion.getStr2();

                    //  "363787002:246093002=720113009,370134009=123029007,246501002=702675006,704327008=122592007,370132008=117363000,704319004=50863008,704318007=705057003"
                    LogicalExpressionBuilder builder = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
                    
                    StringTokenizer tokenizer = new StringTokenizer(sctExpression, ":,={}()+", true);

                    // get necessary or sufficient from Nid2 e.g. "Sufficient concept definition (SOLOR)"
                    // getStandardAssertions just processes the Loinc/SNOMED refset
                    // getProcedureAssertions clones the standard assertions, and changes a few fields
                    // to get it to classify under the procedures taxonomy as well.
                    if (TermAux.SUFFICIENT_CONCEPT_DEFINITION.getNid() == loincVersion.getNid3()) {
                        builder.sufficientSet(builder.and(getStandardAssertions(tokenizer, builder)));
                        tokenizer = new StringTokenizer(sctExpression, ":,={}()+", true);
                        builder.sufficientSet(builder.and(getProcedureAssertions(tokenizer, builder)));
                    } else {
                        builder.necessarySet(builder.and(getStandardAssertions(tokenizer, builder)));
                        tokenizer = new StringTokenizer(sctExpression, ":,={}()+", true);
                        builder.necessarySet(builder.and(getProcedureAssertions(tokenizer, builder)));
                    }
                    LogicalExpression logicalExpression = builder.build();
                    logicalExpression.getNodeCount();
                    addLogicGraph(transaction, loincCode,
                            logicalExpression);
                }
                completedUnitOfWork();
            }
            );
            if (!missingIdentifiers.isEmpty()) {
               throw new NoSuchElementException(missingIdentifiers.toString());
            }
            return null;
        } finally {
            Get.activeTasks().remove(this);
        }
    }
    
    protected Assertion[] getProcedureAssertions(StringTokenizer tokenizer, LogicalExpressionBuilder builder) throws IllegalStateException {
        // nid 4: "Exact match map from SNOMED CT source code to target code (foundation metadata concept)"
        // nid 5: "Originally in LOINC (foundation metadata concept)"
        ArrayList<Assertion> assertions = new ArrayList<>();
        PARSE parseElement = PARSE.CONCEPT;
        while (parseElement == PARSE.CONCEPT) {
            String token = tokenizer.nextToken(); // SNOMED concept id
            try {
                int nid = Get.identifierService().getNidForUuids(UuidT3Generator.fromSNOMED(token));
            } catch (NoSuchElementException ex) {
                handleMissingIdentifier(token);
            }
            // substitute procedure for observation...
            
            assertions.add(builder.conceptAssertion(TermAux.PROCEDURE.getNid()));
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
            int nid = TermAux.UNINITIALIZED_COMPONENT_ID.getNid();
            try {
                nid = Get.identifierService().getNidForUuids(UuidT3Generator.fromSNOMED(token));
            } catch (NoSuchElementException ex) {
                handleMissingIdentifier(token);
            }
            if (nid == directSiteProxy.getNid()) {
                nid = hasSpecimenProxy.getNid();
            }
            String delimiter = tokenizer.nextToken();
            switch (delimiter) {
                case "=":
                    break;
                default:
                    throw new IllegalStateException("2. Unexpected delimiter: " + delimiter);
            }
            String token2 = tokenizer.nextToken(); // SNOMED concept id
            int nid2 = TermAux.UNINITIALIZED_COMPONENT_ID.getNid();
            try {
                nid2 = Get.identifierService().getNidForUuids(UuidT3Generator.fromSNOMED(token2));
            } catch (NoSuchElementException ex) {
                handleMissingIdentifier(token2);
            }
            
            assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(
                    builder.someRole(nid, builder.conceptAssertion(nid2)))));
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
        assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(
                    builder.someRole(new ConceptProxy("Method (attribute)", 
                            UUID.fromString("d0f9e3b1-29e4-399f-b129-36693ba4acbc")).getNid(), 
                            builder.conceptAssertion(new ConceptProxy("Measurement - action (qualifier value)", 
                            UUID.fromString("10424678-abfd-3d47-b92b-84015811c10c")).getNid())))));
        return assertions.toArray(new Assertion[assertions.size()]);
   }

    protected Assertion[] getStandardAssertions(StringTokenizer tokenizer, LogicalExpressionBuilder builder) throws IllegalStateException {
        // nid 4: "Exact match map from SNOMED CT source code to target code (foundation metadata concept)"
        // nid 5: "Originally in LOINC (foundation metadata concept)"
        ArrayList<Assertion> assertions = new ArrayList<>();
        PARSE parseElement = PARSE.CONCEPT;
        while (parseElement == PARSE.CONCEPT) {
            String token = tokenizer.nextToken(); // SNOMED concept id
            int nid = TermAux.UNINITIALIZED_COMPONENT_ID.getNid();
            try {
                nid = Get.identifierService().getNidForUuids(UuidT3Generator.fromSNOMED(token));
            } catch (NoSuchElementException ex) {
                handleMissingIdentifier(token);
            }
            if (nid == MetaData.OBSERVATION____SOLOR.getNid()) {
                nid = MetaData.PHENOMENON____SOLOR.getNid();
            }
            assertions.add(builder.conceptAssertion(nid));
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
            int nid = TermAux.UNINITIALIZED_COMPONENT_ID.getNid();
            try {
                nid = Get.identifierService().getNidForUuids(UuidT3Generator.fromSNOMED(token));
            } catch (NoSuchElementException ex) {
                handleMissingIdentifier(token);
            }
            String delimiter = tokenizer.nextToken();
            switch (delimiter) {
                case "=":
                    break;
                default:
                    throw new IllegalStateException("2. Unexpected delimiter: " + delimiter);
            }
            String token2 = tokenizer.nextToken(); // SNOMED concept id
            int nid2 = TermAux.UNINITIALIZED_COMPONENT_ID.getNid();
            try {
                nid2 = Get.identifierService().getNidForUuids(UuidT3Generator.fromSNOMED(token2));
            } catch (NoSuchElementException ex) {
                handleMissingIdentifier(token2);
            }
            
            assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(
                    builder.someRole(nid, builder.conceptAssertion(nid2)))));
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
        return assertions.toArray(new Assertion[assertions.size()]);
    }
    ConcurrentHashMap<String, AtomicInteger> missingIdentifiers = new ConcurrentHashMap<>();
    private void handleMissingIdentifier(String identifier) {
        AtomicInteger oldValue = missingIdentifiers.putIfAbsent(identifier, new AtomicInteger(1));
        if (oldValue != null) {
            oldValue.incrementAndGet();
        }
    }

    public enum PARSE {
        CONCEPT, ROLE, END
    }

    /**
     * Adds the relationship graph.
     *
     * @param loincCode the LOINC code
     * @param logicalExpression the logical expression
     * @return the semantic chronology
     */
    public SemanticChronology addLogicGraph(Transaction transaction, String loincCode,
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

        final SemanticChronology sci = (SemanticChronology) sb.build(transaction, stamp,
                builtObjects);
        // There should be no other build objects, so ignore the builtObjects list...

        if (builtObjects.size() != 1) {
            throw new IllegalStateException("More than one build object: " + builtObjects);
        }
        index(sci);
        Get.assemblageService().writeSemanticChronology(sci);

        return sci;

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
