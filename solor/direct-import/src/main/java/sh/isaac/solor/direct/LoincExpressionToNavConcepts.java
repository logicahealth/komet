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
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;

/**
 *
 * @author kec
 */
public class LoincExpressionToNavConcepts extends TimedTaskWithProgressTracker<Void> {

    public static final UUID NAV_CONCEPT_UUID_NAMESPACE = UUID.fromString("c5e4b5e0-8d8a-4b66-a251-6a49da082bf8");

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
    private final Transaction transaction;

    public LoincExpressionToNavConcepts(Transaction transaction, ManifoldCoordinate manifold) {
        this.transaction = transaction;
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
            Get.assemblageService().getSemanticChronologyStream(expressionRefset.getNid()).parallel().forEach((semanticChronology) -> {
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
                addInheresInConcept(transaction, new ConceptProxy("Medication (SOLOR)", 
                        UUID.fromString("5032532f-6b58-31f9-84c1-4a365dde4449")).getNid(), 
                        builderService, stamp);
                addInheresInConcept(transaction, new ConceptProxy("Substance (SOLOR)", 
                        UUID.fromString("95f41098-8391-3f5e-9d61-4b019f1de99d")).getNid(), 
                        builderService, stamp);
                addInheresInConcept(transaction, new ConceptProxy("Body structure (SOLOR)", 
                        UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790")).getNid(), 
                        builderService, stamp);
                addInheresInConcept(transaction, new ConceptProxy("Organism (SOLOR)", 
                        UUID.fromString("0bab48ac-3030-3568-93d8-aee0f63bf072")).getNid(), 
                        builderService, stamp);
                addObservesComponent(transaction, new ConceptProxy("Organism (SOLOR)", 
                        UUID.fromString("0bab48ac-3030-3568-93d8-aee0f63bf072")).getNid(), 
                        builderService, stamp);
                addObservesComponent(transaction, new ConceptProxy("Body structure (SOLOR)", 
                        UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790")).getNid(), 
                        builderService, stamp);
                addObservesComponent(transaction, new ConceptProxy("Substance (SOLOR)", 
                        UUID.fromString("95f41098-8391-3f5e-9d61-4b019f1de99d")).getNid(), 
                        builderService, stamp);
                addObservesComponent(transaction, new ConceptProxy("Medication (SOLOR)", 
                        UUID.fromString("5032532f-6b58-31f9-84c1-4a365dde4449")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Ultrasound imaging - action (qualifier value)", 
                        UUID.fromString("c02fd67b-db30-3371-be95-0b7a94509a10")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Imaging - action (qualifier value)", 
                        UUID.fromString("627971bd-2f76-3b1f-a8a7-de93426cf3b7")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Cine imaging - action (qualifier value)", 
                        UUID.fromString("5f8b31cd-d8e7-3b7e-aeb9-7cacb06ec632")).getNid(), 
                        builderService, stamp);                
                addObservedByMethod(transaction, new ConceptProxy("Illumination - action (qualifier value)", 
                        UUID.fromString("28af16ae-e0d3-367a-a9ff-d4b003aafb07")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Magnetic resonance imaging - action (qualifier value)", 
                        UUID.fromString("0145be15-1dc4-313b-9321-e6db22285cfe")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Radiographic imaging - action (qualifier value)", 
                        UUID.fromString("99b82f89-ba3b-3b3f-a0ee-a3cdcc598168")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Radionuclide imaging - action (qualifier value)", 
                        UUID.fromString("1cb086ed-7128-3cec-8f1f-a81bfa521b18")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Thermography imaging - action (qualifier value)", 
                        UUID.fromString("6719f141-7b72-3461-9d5d-56f92dfa1600")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Video imaging - action (qualifier value)", 
                        UUID.fromString("bbb375cb-94cb-38e6-9d78-ef0cbb02f015")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Electrocardiographic procedure (procedure)", 
                        UUID.fromString("2dc7d2f4-1fc1-30d9-9d84-ac2202d98fb4")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Staining technique (qualifier value)", 
                        UUID.fromString("06be16b3-9c84-370e-b812-7c28a85bb406")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Staining method (procedure)", 
                        UUID.fromString("a9edae7c-f464-314c-8a82-a3df9f40e095")).getNid(), 
                        builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Medical specialty (qualifier value)", 
                        UUID.fromString("840406cf-a69e-3ad6-83cf-5d166a7a1c9e")).getNid(), 
                        builderService, stamp);

                //ObservedByMethod Set 1
                addObservedByMethod(transaction, new ConceptProxy("Cold incubation, 24 hours", UUID.fromString("1e4e0092-999f-32b6-8dd1-883d270ab536")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Adverse Childhood Experience questionnaire (assessment scale)", UUID.fromString("1b5e2fc4-5f55-36ec-aa46-67be3f087a48")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Asthma control test (assessment scale)", UUID.fromString("f448ec92-0528-35fd-804c-a30706949325")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Alcohol use disorders identification test (assessment scale)", UUID.fromString("73f525db-1bb5-327f-95a0-548de59c4bd4")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Acid fast stain technique (qualifier value)", UUID.fromString("8cde1c5c-08d2-3cb6-a164-12054a9e0059")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Ziehl-Neelsen stain method (procedure)", UUID.fromString("d31ef55b-63d6-3e75-a2b9-8823b6baca29")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Acid phosphatase stain method (procedure)", UUID.fromString("24a1dd80-20b7-3642-a6c8-12a375df3b15")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Acridine orange stain method (procedure)", UUID.fromString("4817603d-75f6-31b8-a789-2816c305cc22")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Aerobic culture technique (qualifier value)", UUID.fromString("7eb6ad25-6f33-3d88-b25a-0bbd379d5c2a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Aerospace medicine (qualifier value)", UUID.fromString("404e1139-873e-3f54-b5d0-dcdae0077e29")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Airway pressure monitoring (regime/therapy)", UUID.fromString("584c8d63-c5c2-39e6-83fe-c52b4eae6dea")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Albert's stain method (procedure)", UUID.fromString("9a51dba0-13b7-38ef-8809-d0064251f7e0")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Alcian blue stain method (procedure)", UUID.fromString("122fbccf-e7f0-34e1-9f66-a7393aa36bbe")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Sulphated alcian blue stain method", UUID.fromString("fd61f30b-9dec-3b1c-8068-5bd7766a673d")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Alcian blue with Periodic acid Schiff stain method (procedure)", UUID.fromString("5312c8f3-3f0c-3aef-8a57-5d3428d7eb2a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Alizarin red S stain method (procedure)", UUID.fromString("18439499-bd0d-3fcd-8bf4-c6e7c480819e")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Clinical immunology/allergy (qualifier value)", UUID.fromString("b83cea60-fced-3653-a7d8-0f7ede02b806")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Incision and drainage of amnion", UUID.fromString("09dee6b5-30d7-3a03-a7d9-bf5ac1b48865")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Anaerobic culture technique (qualifier value)", UUID.fromString("5a352b7f-b86f-3129-b2e3-e2d5f77b1957")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Anesthetics (qualifier value)", UUID.fromString("5c98fbba-4b1a-341a-9820-18795f4a8031")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Diagnostic angiography", UUID.fromString("e5eb5f33-9371-3881-88ea-14b343d01693")).getNid(), builderService, stamp);
//dup                addObservedByMethod(transaction, new ConceptProxy("Diagnostic angiography", UUID.fromString("e5eb5f33-9371-3881-88ea-14b343d01693")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Endoscopy of anus", UUID.fromString("d3407043-8aae-3fdc-aadd-90b36d79f631")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Argentaffin stain method (procedure)", UUID.fromString("6eadecb2-b415-39d2-8105-c1b146110dff")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Arthroscopy (procedure)", UUID.fromString("4bf05b37-076a-3a6a-ad53-b10bbf83cfc5")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Listening", UUID.fromString("a46ab576-56b5-3452-b5ae-b4629bebd867")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Automatic", UUID.fromString("3bd927e3-db01-3af7-a9fb-ef82e8ed366d")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Automated count technique (qualifier value)", UUID.fromString("abd11755-7337-3802-86ea-bcfa94df8523")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Azure-eosin stain method (procedure)", UUID.fromString("12c85de6-ce8c-3937-adb2-239cf849d373")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Brigance Screens-II (assessment scale)", UUID.fromString("03ba4778-b8b7-3e42-88ca-b0f0cc60777c")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Bacterial subtyping (procedure)", UUID.fromString("9a0de789-9cda-34f9-b11b-c713bb3841dc")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Banding (procedure)", UUID.fromString("d859b722-6cd9-3e0f-b241-b0046bbae10d")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Fuchsin basic stain method (procedure)", UUID.fromString("6fc05803-283f-3bad-8284-e106d09b40f7")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Putchler's modification Congo red stain method", UUID.fromString("e6cc9507-e3db-39e1-b271-021452e0d870")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Bielschowsky's stain", UUID.fromString("a609519a-fde5-3a9c-8c7c-1660a75e86a1")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Bioassay (procedure)", UUID.fromString("355bddcb-8518-33d5-81e2-48f36dd982e1")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Mallory's bleach stain", UUID.fromString("c51326d7-2f37-3aa7-87d8-bf465a97f1ee")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Bodian stain method (procedure)", UUID.fromString("bea92c40-83fb-3c1e-97b4-46183a5c8126")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Acquired brain injury (disorder)", UUID.fromString("b587cb55-69e1-322b-8989-5736b87cfd93")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Cresyl blue BBS", UUID.fromString("9734f183-78f0-38e4-8027-86321f3547c6")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Tracheobronchial endoscopy", UUID.fromString("a7e01789-b882-3563-8521-dfe073a53798")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Brown-Brenn stain method (procedure)", UUID.fromString("4e333536-5077-3a5f-889c-1230bbc48b12")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Butyrate esterase stain method (procedure)", UUID.fromString("80394986-57c2-3df6-970c-5e54e6721c03")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Complement component 3b binding assay", UUID.fromString("992aedf2-201c-3f58-9e77-5f2f44f6b6b1")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Child Development Inventory (assessment scale)", UUID.fromString("f12d0775-85a5-38aa-8233-6697c28ed3ed")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Dementia rating scale", UUID.fromString("c7ab9895-e80d-3a7c-b974-4a09b712ffcb")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Countercurrent electrophoresis measurement (procedure)", UUID.fromString("f1342b58-9c71-336e-8708-392ce418d7b7")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Connecticut (geographic location)", UUID.fromString("4a5e423c-e462-3eab-a3ac-22fdc5abd195")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Spiral computed tomography scan (procedure)", UUID.fromString("3b8cf2f3-85bf-360b-a956-cb5937532898")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Calculated (qualifier value)", UUID.fromString("3fd8182a-9976-3837-bf73-a93c90638746")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Calculation from oxygen partial pressure (qualifier value)", UUID.fromString("c70e7ec7-62f8-3696-b339-810e46ba370e")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Capillary electrophoresis (procedure)", UUID.fromString("bc3b7254-de8e-3dc1-bc45-2233f35020e3")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Product containing carbol-fuchsin (medicinal product)", UUID.fromString("94732756-cdb3-3cc7-8b31-05d12b986c6b")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("PCO>2<, blood", UUID.fromString("cfc0d49a-0fa2-3c68-ae50-468436073fed")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Insertion of catheter into heart chamber", UUID.fromString("21a5e5c5-7d51-3ce6-90d2-df08b6450d05")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Operative procedure on heart", UUID.fromString("3300a761-a95d-3565-a0f6-19a73ee6962a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Disorder of cardiovascular system (disorder)", UUID.fromString("98e3486c-c855-32ff-9052-dfe3790ac8d6")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Case manager (occupation)", UUID.fromString("dd9f78b5-3239-36b9-95ca-37a3c34495e0")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Chemical pathology (qualifier value)", UUID.fromString("e3cad55e-054e-3223-acbf-200bde84c6e6")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Child and adolescent psychiatry (qualifier value)", UUID.fromString("8a2a2cf2-0faa-34c6-aa97-0d8c217e5065")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Churukian-Schenk stain method (procedure)", UUID.fromString("eaef5896-8658-3aa9-bd35-94564958aab2")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Clinical genetics (qualifier value)", UUID.fromString("82b9f915-4477-3d1c-aab8-dbc6aae6189d")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Clinical nurse specialist (occupation)", UUID.fromString("8669d1db-621a-3512-9028-1903d9a54f21")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Clinical pharmacology (qualifier value)", UUID.fromString("75e0fbd7-4800-3c0c-baf8-84a52dd0975a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Coagulation system screening", UUID.fromString("597fd950-7548-30c1-862f-f78c002bdf17")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Cockcroft-Gault formula (qualifier value)", UUID.fromString("0aced273-18b9-30c2-a412-24ad1a585db0")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Hale's colloidal ferric oxide stain method", UUID.fromString("5e3ac6e4-efae-3280-85a6-636f6a424647")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Endoscopy of colon", UUID.fromString("76a6be8c-522a-3e24-b284-d7d26e3f0d7a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Colposcopy (procedure)", UUID.fromString("e2690ecc-e757-35ea-94a8-931b75318246")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Confirmatory technique (qualifier value)", UUID.fromString("a855399b-2582-3f04-95b6-e57562a5577b")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Confrontation visual field test (procedure)", UUID.fromString("81ac574e-8651-3d7c-8e95-3cb7aff94d7d")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Conglutinin assay (procedure)", UUID.fromString("47f11283-1287-31d8-96b1-3df3f8050250")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Congo red stain method (procedure)", UUID.fromString("3d570fc9-ea42-3496-b767-86d233b260fb")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Continuous (qualifier value)", UUID.fromString("e1d8431c-ceaf-3209-9383-71219f621b4e")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Eye screen test", UUID.fromString("e5cbcec0-b01c-3294-8e71-afcdc2abe33c")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Cresyl echt violet stain method (procedure)", UUID.fromString("09551b1e-c828-3bab-9e1b-371d73fd783b")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Critical care medicine (qualifier value)", UUID.fromString("4f7a31fd-4a63-37a3-b56e-7d3b09e096a9")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Methyl violet 10B stain method", UUID.fromString("26087612-eb59-3e09-bbd0-f125caa66949")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Cystic fibrosis respiratory culture (procedure)", UUID.fromString("c4b8334b-2a89-3ccf-bef7-0b59622f4368")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Cytology technique (qualifier value)", UUID.fromString("7a81540d-a7c7-389a-ae95-06d0a4019920")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Drug abuse screening test (assessment scale)", UUID.fromString("0568552c-8b3e-35f4-866a-0dc2ae37b7ac")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("De Galantha stain method (procedure)", UUID.fromString("dc32906e-8964-3d43-ae8c-e6789e99e1ec")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Dentistry (qualifier value)", UUID.fromString("1c973293-f529-3fda-bb71-5f5a30c0a366")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Dermatology (qualifier value)", UUID.fromString("349dbabb-3156-3885-b9fd-1c3cf8db1bf4")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Imaging (procedure)", UUID.fromString("5f9a1cfd-dd31-3bb2-aa55-98c453c2a311")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Dilution (qualifier value)", UUID.fromString("64c92399-c8fd-3906-83c3-d9be6e60d487")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Direct assay technique (qualifier value)", UUID.fromString("b8f0b8b0-6f37-342a-9751-1100af90f8e3")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Dye test (qualifier value)", UUID.fromString("f4e75e75-d37c-3467-9ae5-c292a46d38e9")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Dynamometer (physical object)", UUID.fromString("5ac91e59-ed3f-364f-85da-8313ed3599a9")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Electroencephalography", UUID.fromString("e26b0d15-2577-37ef-b4c7-7a394c1f5f2d")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Upper gastrointestinal endoscopy", UUID.fromString("8c5b5a90-5b23-3b42-9fbc-5444e4d4b80b")).getNid(), builderService, stamp);
// dup                addObservedByMethod(transaction, new ConceptProxy("Electrocardiographic procedure (procedure)", UUID.fromString("2dc7d2f4-1fc1-30d9-9d84-ac2202d98fb4")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Edinburgh postnatal depression scale (assessment scale)", UUID.fromString("a1d4af9d-b266-388a-bae7-235c4c96e418")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Endoscopic retrograde choledochopancreatography", UUID.fromString("cad657a0-25dd-3450-b484-886bfbac8450")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Emergency Severity Index (assessment scale)", UUID.fromString("16e8e73f-a775-3b2c-aca6-e0acdcd3c466")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Electromyography (procedure)", UUID.fromString("e840e11e-c570-3c23-9e39-da290b8fc8db")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Electronic (qualifier value)", UUID.fromString("e47cbac2-1ecf-3bf4-bb1e-55c7ff574373")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Electro-oculography", UUID.fromString("97ef3265-16b0-3add-be6b-0e6cf377bc41")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Zone electrophoresis measurement", UUID.fromString("f700329d-1e87-39b4-aa87-a6998dacde80")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Electrophoresis, agarose gel method (procedure)", UUID.fromString("8b86b81c-3577-3b02-8ee3-f1ed951e9786")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Electrophoresis, citrate agar method (procedure)", UUID.fromString("dd1b2cd2-7904-32c1-bbee-992bef14616d")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Electroretinography", UUID.fromString("1404fd40-a33c-3813-9673-7fcd73a7a994")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Endocrinology (qualifier value)", UUID.fromString("cf8bad57-2b13-39de-9022-346f7b7a3ec1")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Inspection using endoscope", UUID.fromString("fb154f90-3ce7-3e92-8547-55947d431195")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Enteroscopy (procedure)", UUID.fromString("b917309b-9040-36e8-9cda-e6e5f16d6f61")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Environmental culture (procedure)", UUID.fromString("c640911f-5453-3933-9f59-336a85b295f8")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Epileptic disorder", UUID.fromString("74ea5091-2131-343c-bb7c-c0238815f48f")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Oesophagoscopy", UUID.fromString("e7fad093-503f-39cf-a9c5-5e84377cc66a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Nonspecific esterase stain method (procedure)", UUID.fromString("06bafe98-cbbf-35d2-994d-9f8bd86d82cb")).getNid(), builderService, stamp);
//dup                addObservedByMethod(transaction, new ConceptProxy("Estimated (qualifier value)", UUID.fromString("f0b32009-b800-33eb-95ef-5617d1bafbc9")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Estimated (qualifier value)", UUID.fromString("f0b32009-b800-33eb-95ef-5617d1bafbc9")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Estimated from glycated hemoglobin technique (qualifier value)", UUID.fromString("a41a55c8-d474-3f85-a9dc-1ec1c941b3f2")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Evoked potential, function (observable entity)", UUID.fromString("31bff468-c19c-3a43-af94-61d053ca467b")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Exercise tolerance test (procedure)", UUID.fromString("7554ce6c-d510-3c50-b566-8717fa617a69")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Fish (organism)", UUID.fromString("0b353e99-572a-3f0e-8c6a-df99b35ba304")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Falls behavioural scale for older people", UUID.fromString("8c699773-1fcd-3238-a2b4-d764c116d30c")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Family practice (qualifier value)", UUID.fromString("61d9162c-44b3-3f03-9d7a-cf6b7179c4dd")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Fite-Faraco stain method (procedure)", UUID.fromString("769750f8-94ad-3dea-8a4a-7fb7ad8da1f1")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Flexible sigmoidoscopy", UUID.fromString("9bd790a0-13db-3a82-8e0c-668c2ebadb6a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Flow cytometry technique (qualifier value)", UUID.fromString("ac197c96-13f6-3330-a9cc-d15924b82ca3")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Fouchet stain method (procedure)", UUID.fromString("bbad8b72-7b08-3694-9a2a-c56533003712")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Fungal subtyping (procedure)", UUID.fromString("a1a42443-e7ff-3bcd-ad86-ebc14a689f0e")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Stain for fungus", UUID.fromString("4c72bd84-aeed-3799-9bfe-188aa1452c39")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Gas chromatography measurement (procedure)", UUID.fromString("95a0e1f8-87ba-30c7-bd6d-8723ce8b2603")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Gel (basic dose form)", UUID.fromString("879199f9-86f1-309f-81b1-611ac5cb0a0d")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Gastroenterology (qualifier value)", UUID.fromString("586ffbe6-ef70-39c3-8083-9bb728f7ba6f")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("General medicine (qualifier value)", UUID.fromString("70ab7c51-1962-3d04-bdb6-943fbc227980")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Genotype determination (procedure)", UUID.fromString("ed889903-29b2-3f65-a502-79b70de8ccdb")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Geriatric medicine (qualifier value)", UUID.fromString("7e7bca73-617b-35b0-ae97-e8825e06beaa")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Giemsa stain method (procedure)", UUID.fromString("5a26d32e-cfa2-3c4c-b4d7-fe8bd4df0cab")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Three micron giemsa stain method (procedure)", UUID.fromString("bee0d093-7e2c-3dbe-9078-55349200b105")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("May-Grunwald giemsa stain method (procedure)", UUID.fromString("8fca006e-68b3-3c62-aa78-54a3ce638e70")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Gimenez stain method (procedure)", UUID.fromString("f610424d-bdc9-3fdb-9a4a-96df7c3ed622")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Blood glucose meters (physical object)", UUID.fromString("628e9c07-b203-324a-859a-66f59950ad58")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Gomori stain method (procedure)", UUID.fromString("9fbcf437-e2af-3d75-9f94-8620a359c023")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Gram stain (substance)", UUID.fromString("c3a46237-42c8-3261-80fa-9d7fea594862")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Gridley stain method (procedure)", UUID.fromString("c6dffa4c-13ed-3729-b57f-c398134eae1f")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Phenylalanine screening test, blood", UUID.fromString("cef204cc-04cb-3ddd-8a6c-a6adbc600410")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Gynecological oncology (qualifier value)", UUID.fromString("1b6d73f5-4346-32e5-89b4-ae42851e8785")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Gynecology (qualifier value)", UUID.fromString("db52ed68-116b-3261-b488-20deb122274f")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Hemagglutination assay (procedure)", UUID.fromString("2896caae-5a11-3ddd-ac41-908abe32501e")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Stanford health assessment questionnaire", UUID.fromString("d7add6b5-c1de-3cb1-bd6f-0197c9e44fa1")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Human immunodeficiency virus (organism)", UUID.fromString("48ccc0c5-b120-3ed5-8f79-3eaeb644ddb8")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("High pressure liquid chromatography (procedure)", UUID.fromString("f640022e-71db-3030-8c6f-a620afc1141b")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Hall's technique for bilirubin", UUID.fromString("dcf83756-e26f-3cb7-aee4-cab1ddacb9aa")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Hansel's stain", UUID.fromString("8ae2cf73-94c3-383a-80e3-5f277c46c08f")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Hematoxylin and eosin stain method (procedure)", UUID.fromString("e8fdf03a-ca32-30d8-8a9e-ef5f688a1340")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Harris regressive hematoxylin and eosin stain method (procedure)", UUID.fromString("69d99945-1807-3894-b5d6-9d2f218d8173")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Mayers progressive hematoxylin and eosin stain method (procedure)", UUID.fromString("57310ca9-4fa5-3915-8bb5-4990c3840de2")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Hepatology (qualifier value)", UUID.fromString("43557dff-87f6-3603-a0e0-2432cbfc5635")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Highman stain method (procedure)", UUID.fromString("d2d8d5de-702c-35e9-bb44-4c2b39150f39")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Holzer stain method (procedure)", UUID.fromString("cfd2a0e1-d8fd-31fe-a6cc-052b1da42f0d")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Immunoassay method (procedure)", UUID.fromString("da1552e9-d57d-3bfd-bcb2-fa0f4d7aa902")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Immunoblot assay technique (qualifier value)", UUID.fromString("3f277a62-fe2b-3caf-808a-12676eafc622")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Islet cell cytoplasma antibody", UUID.fromString("50980d8a-be49-34ea-9f8e-92ac32665da6")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("International prostate symptom score (assessment scale)", UUID.fromString("29f88b06-5741-38b3-af20-a712df1ef023")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Ion selective electrode measurement technique (qualifier value)", UUID.fromString("ea4524fb-8a9b-3df2-b353-6c464fae15a6")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Imaging technique (qualifier value)", UUID.fromString("8dd99fbe-40ac-3461-b22b-48cac7d6241a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Immunological", UUID.fromString("08ee9b9c-9387-33d1-99fd-8cce1b62b32a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Immunoelectrophoresis technique (qualifier value)", UUID.fromString("8b8398ba-acc0-3b36-8ad4-4137fc0e13e4")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Immunofixation (procedure)", UUID.fromString("ce41c420-0036-3faa-a316-66e61dff9baa")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Immunophenotyping (procedure)", UUID.fromString("73b2ab0f-ff97-334f-b226-f63dea93a9b9")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Impedance (attribute)", UUID.fromString("d778d923-3f93-3b21-8841-377ab7385e18")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("India ink staining technique (qualifier value)", UUID.fromString("bef038d3-41d6-361c-a316-41de3fb62c51")).getNid(), builderService, stamp);


                //ObservedByMethod Set 2
                addObservedByMethod(transaction, new ConceptProxy("Indirect antiglobulin test", UUID.fromString("fc2751e3-8f2a-39ca-9420-331840554d00")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Infective disorder", UUID.fromString("6a55322e-f3de-3d93-ae8d-be206b9339de")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Infrared spectroscopy technique (qualifier value)", UUID.fromString("f65cf27c-f116-3ec7-848f-4dccc9363a9a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Visual observation", UUID.fromString("325eab05-452f-3488-9e45-a42204f8f830")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Internal medicine (qualifier value)", UUID.fromString("c885f77a-6c25-3c77-8919-88bdb2aba3e9")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Interventional radiology (procedure)", UUID.fromString("ee4615a1-9f40-3df9-b724-cce682016279")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Invasive (qualifier value)", UUID.fromString("91666bef-12a5-3412-886f-ae03ea628fd8")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Iron hematoxylin stain method (procedure)", UUID.fromString("df998614-cb60-34b0-a88a-ccabffbfe454")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Isoelectric focusing measurement (procedure)", UUID.fromString("1d9d745b-737f-311f-9de5-190edc72ac3a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Potassium hydroxide preparation technique (qualifier value)", UUID.fromString("35a6bdda-0ac8-30d6-b109-9671931847b1")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Keratometry (procedure)", UUID.fromString("356da8d9-3833-3e39-ad40-6fea9be99365")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Latex agglutination test (procedure)", UUID.fromString("701d99a3-9256-3889-8523-39d08fa3339e")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Leukocyte histamine release test", UUID.fromString("9b8a9f40-e498-3acd-9801-a5e34afba5c5")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Low ionic strength saline technique (qualifier value)", UUID.fromString("66e88940-0069-3e72-b7b4-f603cdd8dea7")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Peritoneoscopy", UUID.fromString("2a9c218b-2883-3af0-8a08-f6580fab9bb1")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Lawson-Van Gieson stain method (procedure)", UUID.fromString("d48712e2-305c-3da5-8937-981188ddd75c")).getNid(), builderService, stamp);
//                addObservedByMethod(transaction, new ConceptProxy("Licensed practical nurse (occupation)", UUID.fromString("a10839d5-6433-3d1c-a57b-fc4a439fa1b8")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Luxol fast blue with cresyl violet stain method (procedure)", UUID.fromString("dd4280c5-a1f4-36f8-8bec-4f6ea4a3e413")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Luxol fast blue with Periodic acid-Schiff stain method (procedure)", UUID.fromString("0e45ddea-794f-31e8-b884-ed92d59b348e")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("M'fadyean stain", UUID.fromString("680753b0-b7ef-3cdc-a6d5-4211539f5810")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Michigan alcoholism screening test (assessment scale)", UUID.fromString("07efdf71-2460-37b2-9cee-f4c416e0a5f0")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Radiographic examination of breast", UUID.fromString("6707d8bb-99ec-3e0d-ad7a-7a4745d40fe6")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Minimum inhibitory concentration", UUID.fromString("b6159273-9b20-3067-8137-37249261c0e5")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Mini-mental state examination (assessment scale)", UUID.fromString("446eb2ee-706a-3cbe-9a99-91237f27adea")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("NMR - Nuclear magnetic resonance", UUID.fromString("34ffe888-797f-365d-9e9c-61c632b83fe0")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Magnetic resonance spectroscopy (procedure)", UUID.fromString("20470e57-3f09-3a3e-bf34-8993da195c92")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Macchiavello stain method (procedure)", UUID.fromString("f08cfc51-2b81-3ec5-8582-ea6d8b6f7be8")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Sample macroscopy (procedure)", UUID.fromString("d5fdb7b6-2cd2-31b8-ab83-58c9a15f253c")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Malachite green stain method (procedure)", UUID.fromString("6a29f510-0bfb-31ef-8a9d-a283b41a1929")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Malaria smear (procedure)", UUID.fromString("03aa4165-ff95-3ea6-9705-5e330de20219")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Malaria thick smear (procedure)", UUID.fromString("123266bb-207b-3ef4-be89-45ed2a583c91")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Malaria thin smear (procedure)", UUID.fromString("7b2d171f-46e4-3ac4-9214-66fc10813a2b")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Mallory Heidenhain stain method (procedure)", UUID.fromString("3c8005ae-7dd6-3fd1-ba05-a557aff1bf23")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Manual (qualifier value)", UUID.fromString("e48adcb0-3cc3-359f-b9b7-bc4943a8b6ec")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Manual count technique (qualifier value)", UUID.fromString("dd0da81b-83fc-3c75-a75c-0e22babe5224")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Measured (qualifier value)", UUID.fromString("e215fcf2-7962-35e9-bfad-26f65842ca77")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Medical student (occupation)", UUID.fromString("b629ecda-2dbf-3c06-8bf5-9a9f16945fda")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Nurse psychotherapist (occupation)", UUID.fromString("b9440c1f-a925-326a-9148-dca6f49a2a6b")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Methenamine silver nitrate stain method (procedure)", UUID.fromString("6677a5e4-a366-34cf-b21d-305493b173ff")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Grocott methenamine silver stain method", UUID.fromString("1140a33d-23db-32ec-818d-9fa42b9f82ce")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Jones stain method", UUID.fromString("4be79f21-d9e8-3eeb-b05c-c0fbf43307b0")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Methyl green stain method (procedure)", UUID.fromString("05cb3496-a14a-3bd2-aa67-a330b9d5742d")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Methylthioninium chloride stain method", UUID.fromString("51695334-620b-31bb-ab92-c9b42b1198b6")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Loeffler methylene blue staining", UUID.fromString("9443e01b-06b8-3d67-a670-59b6bd642bd2")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Microscopy (procedure)", UUID.fromString("d5f3b7af-3308-3b86-a673-9909a744c448")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Electron microscopic study (procedure)", UUID.fromString("6a03a7aa-ac46-3d1b-8f20-acb7592258bd")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Observation by light microscope", UUID.fromString("14075199-7f12-344e-8808-98f2b858463d")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Molecular genetics procedure (procedure)", UUID.fromString("4d618061-0dff-3d52-aa0a-c7c54aeb7088")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Mucicarmine stain method (procedure)", UUID.fromString("462954ff-6d74-319c-a4b9-6f1dad1f1cc8")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Mayer mucicarmine stain method (procedure)", UUID.fromString("287cc34c-0ff5-3638-a30e-ec2bd076748a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Mycobacterial subtyping (procedure)", UUID.fromString("66dc8bca-2a5b-357e-a71f-c6fab27555bf")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Myeloperoxidase stain method (procedure)", UUID.fromString("42266761-c213-3489-92a0-c2d306c5cbc9")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("New Mexico (geographic location)", UUID.fromString("ad29aa91-8be2-3af0-acd8-10e8355c6445")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Neural tube defect (disorder)", UUID.fromString("39447cda-1fde-307a-b0cd-cdda390eb781")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("New York Heart Association Classification (assessment scale)", UUID.fromString("9c9e86c9-efa6-3d4e-8f8f-e72086cd77b2")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Neisser stain method (procedure)", UUID.fromString("24af0a08-8c3e-3c63-a543-8679381a2777")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Turbidity test", UUID.fromString("a50bc0cd-54e1-3a61-b183-5035209bb1b4")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Renal medicine", UUID.fromString("d20f017a-44fe-3d11-bc46-44fe91ad08a5")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Nerve conduction (observable entity)", UUID.fromString("a7a36bac-4f13-3ddb-8940-37c27e2eb741")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Operative procedure on nervous system", UUID.fromString("ff1df1be-e0a1-37c8-a4cc-f4c318a8a606")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Neurology (qualifier value)", UUID.fromString("1966e0c0-1c74-341f-8e63-495f01f75848")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Neurology nurse (occupation)", UUID.fromString("19708ed6-5768-3c84-bffe-1b96eda36a73")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Neutral red stain method (procedure)", UUID.fromString("913e7cbf-0a91-3156-b3d8-7390dc3d2f09")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Night blue stain method (procedure)", UUID.fromString("4b593ce9-6bda-38e1-9220-eb4954e1c828")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Professional nurse (occupation)", UUID.fromString("8323b206-1927-38a8-8b16-f045cf3d6dc4")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Nurse practitioner (occupation)", UUID.fromString("455c8cbc-911d-39eb-a564-518bfdbeb451")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Nursing (qualifier value)", UUID.fromString("d734c42f-d25f-3a3d-b77a-cc339a3476df")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Ornithine transcarbamylase", UUID.fromString("74173dcc-df72-3037-b69a-bab65ea32d9a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Objective refraction (procedure)", UUID.fromString("e2e01c7d-d508-3fc5-a9a2-ceb59f46d9ba")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Obstetrics and gynecology (qualifier value)", UUID.fromString("1c0be577-2044-3109-aefd-da49f83886c1")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Occupational medicine (qualifier value)", UUID.fromString("d047b6ea-73f7-34ef-af45-249d2f52ae0e")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Occupational therapy (regime/therapy)", UUID.fromString("0db5d645-3a45-310a-9ee3-7eab0d9252a5")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Oil red O stain method (procedure)", UUID.fromString("61cfe82e-0ff4-3d91-a647-121ee9341564")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Ophthalmology (qualifier value)", UUID.fromString("c1909ce3-d7e4-3649-ad38-a0383677cd68")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Keratometer (physical object)", UUID.fromString("401018cd-c353-31a4-b8a9-aaf0977c3588")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Ophthalmoscopy (procedure)", UUID.fromString("b65da9ee-5009-36fd-80ca-1fcc1ffca83a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Oral and maxillofacial surgery (qualifier value)", UUID.fromString("052d9690-0ca6-34af-b222-736984908b31")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Orcein stain method (procedure)", UUID.fromString("34f18ab3-24cf-355a-8739-75b0b91f5e4c")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Organism specific culture technique (qualifier value)", UUID.fromString("68f0b7e2-1e96-331f-8194-6e3912706399")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Otolaryngology (qualifier value)", UUID.fromString("3118b2cf-b46f-310a-bfa4-5f3bd55f8c12")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Oxygen saturation measurement (procedure)", UUID.fromString("fa8c5f83-3f65-3e0f-a2b4-820a1254142d")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Oxygen analyzer, device (physical object)", UUID.fromString("2cf16995-71f9-304b-8378-086d93d34d54")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Polyacrylamide gel electrophoresis (procedure)", UUID.fromString("5d903f56-1cfe-388d-acf9-6b597f70802a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Pulsed-field gel electrophoresis (procedure)", UUID.fromString("ab78b666-fc31-3d7a-8264-53d81879f28d")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Positron emission tomographic imaging - action (qualifier value)", UUID.fromString("0af8e5e8-a815-30ff-a157-4d287b372464")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Analgesic (substance)", UUID.fromString("eb8f53dc-d4ec-370a-a1b1-f12ecdbc04bf")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Palliative care nurse (occupation)", UUID.fromString("ac64f3c0-b692-3397-b7a6-6f82b84afea9")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Palliative care physician (occupation)", UUID.fromString("115c5e74-b41a-34f3-8784-2ddf53de421f")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Palpation - action (qualifier value)", UUID.fromString("3416ec21-d8fa-3656-848e-6d6837f3ce03")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Pastoral care (regime/therapy)", UUID.fromString("0bce05ec-0844-3af4-a928-cce9b1920216")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Patient (person)", UUID.fromString("86490a44-8539-31e2-8e0f-3a6253a72fca")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Peak flow meter (physical object)", UUID.fromString("cfe26ad0-6538-39a1-bab5-acd35cfda7dd")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Pediatric cardiology (qualifier value)", UUID.fromString("579bff22-4102-3476-933e-d429b28984dd")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Pediatric endocrinology (qualifier value)", UUID.fromString("65a6d728-8724-3701-8ed8-1d0141d7b87b")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Pediatric gastroenterology (qualifier value)", UUID.fromString("53f92dd9-3fb9-346d-a27e-5a612ce500d1")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Pediatric nephrology (qualifier value)", UUID.fromString("a1028c8d-0d1d-31ea-a065-fd43e79088b2")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Pediatric otolaryngology (qualifier value)", UUID.fromString("6df26b1d-997d-361e-bd14-601596cfbb2b")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Pediatric pulmonology (qualifier value)", UUID.fromString("821111ca-7b38-3beb-82a4-1ef9e9c2ad18")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Pediatric rheumatology (qualifier value)", UUID.fromString("cd13f274-be1f-3d3f-a4c0-abea94cc6f79")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Pediatric surgery (qualifier value)", UUID.fromString("04adc570-5a1c-3a48-85c3-5a9bd6fc3ad7")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Pediatrics", UUID.fromString("979558cc-99a3-3cb3-94c9-089fdffb8c67")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Movat pentachrome stain method (procedure)", UUID.fromString("42bee416-4bf4-3760-a694-a2155a49a66a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Perimetry (procedure)", UUID.fromString("51d68c7d-7c7f-393b-98bd-bdc7758a10a5")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Periodic acid Schiff stain method (procedure)", UUID.fromString("8431464d-8f7e-3b70-9bba-26e44603bd89")).getNid(), builderService, stamp);
// dup                addObservedByMethod(transaction, new ConceptProxy("Peritoneoscopy", UUID.fromString("2a9c218b-2883-3af0-8a08-f6580fab9bb1")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Peroxidase stain method, blood or bone marrow (procedure)", UUID.fromString("58b8b2ca-a46d-3165-9e65-8f9fb0915879")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Pharmacist (occupation)", UUID.fromString("3bae419a-1707-357f-aa09-13caa7709bab")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Phenotype determination (procedure)", UUID.fromString("3315e2df-a2bb-39d1-aabc-436da2ffc5f6")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Phoropter (physical object)", UUID.fromString("e6010daf-2a06-3166-ad57-95d8780e5f13")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Physiotherapy procedure", UUID.fromString("42d5f509-2418-3d87-ab8d-05263b822ff1")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Physician (occupation)", UUID.fromString("021e3dc2-8afe-3705-8d23-288b3fe077a9")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Physician assistant (occupation)", UUID.fromString("15a12eb9-945a-3066-a4a9-60a73e788d02")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Attending physician (occupation)", UUID.fromString("62923c0c-34b6-3c7e-ae3b-a699e3b3e24a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Resident physician (occupation)", UUID.fromString("827c4a44-eef1-3b75-a049-9dd20ef5fc8b")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Plastic operation (qualifier value)", UUID.fromString("3daac316-c0cd-3e6c-af0e-8c5a7f0df6a3")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Platelet aggregation test (procedure)", UUID.fromString("c063cf79-3099-3429-ba33-9ce2cf2290c0")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Podiatry (qualifier value)", UUID.fromString("78f84195-2cbf-3bf5-904e-d93de19fe8e9")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Polysomnography (procedure)", UUID.fromString("b06b7729-cbd5-3380-b0e1-0db1ddfc1651")).getNid(), builderService, stamp);
// dup with different name: Prussian blue stain method                addObservedByMethod(transaction, new ConceptProxy("Potassium ferrocyanide stain method", UUID.fromString("518afa3b-56be-35ff-a329-5da562b6d860")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Preventive medicine (qualifier value)", UUID.fromString("803685fc-e0a9-378b-8670-d3be7f4e883a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Primary care physician (occupation)", UUID.fromString("485a0709-0876-307d-b376-85171a497f9c")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Probe, device (physical object)", UUID.fromString("134bfb4e-86a8-3bab-a463-b680dd280a99")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Probe with target amplification technique (qualifier value)", UUID.fromString("400973df-c050-3885-b8ad-e438375ef0ca")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Rigid proctosigmoidoscopy (procedure)", UUID.fromString("b27cd8e6-6f05-3807-8e74-64184f26c6e6")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Prussian blue stain method (procedure)", UUID.fromString("518afa3b-56be-35ff-a329-5da562b6d860")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Psychiatry (qualifier value)", UUID.fromString("7b1e9ae7-7a15-3937-8478-2094a992af7a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Psychology (qualifier value)", UUID.fromString("6f22d321-1bf4-31b2-8c2b-87c6e3c9682a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Pulmonary disease", UUID.fromString("3882c94c-86db-3086-8c9a-4d44e39f1a21")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Pulmonary function (observable entity)", UUID.fromString("ce1df6e1-8757-3d3c-aa7e-9bcb97615ee6")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Pulse oximetry technique (qualifier value)", UUID.fromString("5e05e0ef-9775-374d-a001-9e61e419799e")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Quinacrine fluorescent stain method (procedure)", UUID.fromString("193bae62-f5a4-3842-8b40-c5bf6883ee16")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Radioallergosorbent test (procedure)", UUID.fromString("7ed75b6f-c91b-3aa3-a8da-12a0cd12c533")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Restriction fragment length polymorphism technique (qualifier value)", UUID.fromString("0377d06a-f06e-3e79-b985-d09105ffe135")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Radioimmunoassay technique (qualifier value)", UUID.fromString("371d3025-af0d-3673-bb8a-2597a29f483f")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Radioimmunoprecipitation assay (procedure)", UUID.fromString("8c8b22dd-508c-317d-9f74-da7aab9bf4b4")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Rapid plasma reagin test (procedure)", UUID.fromString("2f4df3c5-8767-3077-ae34-db6161b6ef79")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Therapeutic radiology", UUID.fromString("66166d24-5bc0-3654-a4b3-c0035af0a099")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Raji cell assay", UUID.fromString("f2d75b4f-2498-3768-b035-cca0d4beff6e")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Rapidly (qualifier value)", UUID.fromString("09210ec5-df19-30e0-b3e2-7234f5d2d704")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Recreational therapy (regime/therapy)", UUID.fromString("9a7dcf0c-53d3-3195-8882-9ad74ae16c2d")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Registered nurse (occupation)", UUID.fromString("5e98e0d7-c7c5-35aa-ba7e-49c8d349c2d9")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Reporting", UUID.fromString("416df33b-fb51-3db3-a7bc-08f40e70bf94")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Respiratory therapy (procedure)", UUID.fromString("579ac6f4-d26f-3c5a-a438-d24b18221926")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Reticulin stain method (procedure)", UUID.fromString("ce9bb41f-e25a-39da-b19f-ab36b9dd963c")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Rheumatology (qualifier value)", UUID.fromString("69521a28-7ff9-38fb-bfc3-426f4daf3b51")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Rhodamine stain method (procedure)", UUID.fromString("15641ebe-cb07-3c45-ac3d-90c8fb0894ad")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Rhodamine-auramine fluorochrome stain method (procedure)", UUID.fromString("742ce6a3-97c2-3968-8497-89ca87f143e1")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Romanowsky stain method (procedure)", UUID.fromString("38462f58-43b5-3ea9-b1a5-7d9ef6f4532a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Rosenberg self-esteem scale (assessment scale)", UUID.fromString("9e1fd687-ebb7-381c-90ed-3d5a7bb9a1ef")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Serum cidal test", UUID.fromString("2ea9cdc6-b284-3013-9e92-f00ae0f883a0")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Safranine O stain", UUID.fromString("19813816-66a5-3f01-bb46-066ca5b8b0de")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Sodium chloride solution (substance)", UUID.fromString("ad6c905a-8640-39bc-bc30-273d4cfe959b")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Schirmer's test (procedure)", UUID.fromString("6bfb7f91-1860-35d9-b41c-dc7413d2a159")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Schmorl stain method (procedure)", UUID.fromString("08896cc6-afed-3464-a406-5b45b80ced64")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Schober's test", UUID.fromString("9c40c846-3661-3087-9970-68e124abef62")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Nucleic acid sequencing (procedure)", UUID.fromString("28031315-2764-36ae-896d-033a04c25ddd")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Sevier-Munger stain method (procedure)", UUID.fromString("a36ec319-f341-3af5-a71f-e5c320f28774")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Dieterle's stain", UUID.fromString("636e4cd3-08ae-334d-b9aa-a8bf3e568ba9")).getNid(), builderService, stamp);


                //ObservedByMethod Set 3
                addObservedByMethod(transaction, new ConceptProxy("Silver stain method (procedure)", UUID.fromString("223baa52-cbb8-3247-beb5-1ec499715a90")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Fontana Masson silver stain method (procedure)", UUID.fromString("84ec165e-b9fa-3147-8dc6-d5e12d8ac0bf")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Grimelius stain", UUID.fromString("5c153870-37b2-37f3-ba9c-2d57df3ef1c0")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Slit lamp examination", UUID.fromString("97d0539c-6c9d-342c-a3f0-9caa98c9db98")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Snellen eye chart", UUID.fromString("9d9ae0bb-fcb1-346b-9bcb-275f468ba52e")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Solubility test technique (qualifier value)", UUID.fromString("c9d2b0de-8c28-3020-a642-e9e3292640cb")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Spectrophotometric measurement (procedure)", UUID.fromString("41b33120-fddf-33a5-a0b2-3cb8eb0fa4a0")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Speech therapy (regime/therapy)", UUID.fromString("7d6191fe-21de-3b28-8212-fe7b0ab97433")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Spirometry (procedure)", UUID.fromString("df6abd6d-f0de-3bd1-83b3-46a8742e65af")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Sterile body fluid culture (procedure)", UUID.fromString("70feb116-945f-3dde-8d25-3744220f46ca")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Subjective refraction (procedure)", UUID.fromString("bb3dfcb1-315d-3bef-b683-c187df577caf")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Sudan III stain method (procedure)", UUID.fromString("4c1c6f8a-16fe-31b7-9bf7-d1b2c6723439")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Sudan IV stain method (procedure)", UUID.fromString("8d7ac826-6d7f-3d1c-b46e-2c6026bb83e8")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Sudan black B stain method (procedure)", UUID.fromString("a6e6c356-65c6-3aca-abad-4902a5db8c7a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Sudan black stain method (procedure)", UUID.fromString("85c9150c-7368-3a6b-a194-fcd43d33aebf")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Supravital staining method", UUID.fromString("1f19df2e-32d6-370f-878c-b4ded531503b")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Surgical procedures", UUID.fromString("85923350-9ead-323f-b2d9-8ade0ac4264c")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Operative procedure on hand (procedure)", UUID.fromString("9b9ffb80-090a-32c4-b174-0304f7aa8207")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Surgical oncology (qualifier value)", UUID.fromString("001dc03e-e670-37ae-907b-90e97da7fdab")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Measuring tape, device (physical object)", UUID.fromString("6eab5002-eac7-3be9-8875-87eae198c080")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Tartrate resistant acid phosphatase stain method, blood or bone marrow (procedure)", UUID.fromString("db366cb2-a656-3bb8-a78d-30919da6b1d8")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Terminal transferase stain", UUID.fromString("d0cf3b3f-b603-3039-a865-8fe4edf58f3b")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Test strip technique (qualifier value)", UUID.fromString("0dabeb0a-4bce-3090-8deb-408b6fc889ad")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Thioflavine-S stain", UUID.fromString("16b01807-c316-385d-b2fb-beb241d0fa81")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Surgical procedure on thorax (procedure)", UUID.fromString("031bf635-945c-392a-8dc7-17694715fc88")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Thromboelastography (procedure)", UUID.fromString("05fdb508-116f-3319-a013-9efad04455e3")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Toluidine blue stain method (procedure)", UUID.fromString("6f8881d2-6601-335f-b09d-9ff51d1f088b")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Tonometry (procedure)", UUID.fromString("4a9a23be-fb5d-3f87-9f61-8ffca8d068f5")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Noncontact tonometry", UUID.fromString("e8dd187a-f001-3e1a-b083-a0d227982300")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Transcutaneous carbon dioxide monitor method (qualifier value)", UUID.fromString("cecbfa3c-a7d0-33c6-b28c-f67d6b0fe133")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Transcutaneous oxygen monitor method (qualifier value)", UUID.fromString("65a4d583-71a8-386b-bc06-8c7fb8c9d957")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Trauma", UUID.fromString("5ab41bfb-b8ad-3279-9376-cc204e6de03b")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Ryan-blue stain", UUID.fromString("69c0d0aa-2d85-346a-9a30-ab5bb4221372")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Trichrome staining", UUID.fromString("47664e7c-8985-3593-bf93-6bb70bab3809")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Modified Gomori-Wheatley trichrome stain method (procedure)", UUID.fromString("d89c651d-6883-3ef1-b6eb-fc8c68960603")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Masson trichrome stain method (procedure)", UUID.fromString("fd6b94dc-4de6-36a9-885b-0e17a352210e")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Modified Masson trichrome stain method (procedure)", UUID.fromString("0efa7b50-201e-32ab-b3a1-e3f6391de391")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Tzanck smear method (procedure)", UUID.fromString("8ea6b46e-ce9e-363d-829e-150c4e72bc1e")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Voiding flow rate test", UUID.fromString("c008b44b-c596-38f2-81e5-57214faf0b71")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Urology (qualifier value)", UUID.fromString("35816de1-9f0e-3d78-a260-78904e5d7e5a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Venereal Disease Research Laboratory test (procedure)", UUID.fromString("f6c1e5e8-d09d-3b30-8d6e-2c4107d91fec")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Visual evoked responses", UUID.fromString("1b3c427f-999e-3f9e-888e-9f8fc08a2341")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Van Gieson stain (substance)", UUID.fromString("80c309a7-51c6-37b3-bad2-8934ae2bea15")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Vascular surgery procedure (procedure)", UUID.fromString("1b3d5c2b-1514-38e6-8897-5bfd6de259e6")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Vassar-Culling stain method (procedure)", UUID.fromString("434be47a-5962-3727-9e63-10c13f586e7a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Verhoeff-Van Gieson stain method (procedure)", UUID.fromString("687a872c-7fef-3427-980f-d7051857529a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Viral subtyping (procedure)", UUID.fromString("a960a5b8-fe57-387f-a7a8-9ade53679147")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Visual (qualifier value)", UUID.fromString("d62b46c7-b560-3349-bd05-11059b9c657f")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Vocational rehabilitation (regime/therapy)", UUID.fromString("15343e12-f30a-3185-83be-2148fefbf027")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("von Kossa stain method (procedure)", UUID.fromString("fbdc4918-ae7c-30c5-945b-fa8814aa4e1a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Wade-Fite stain method (procedure)", UUID.fromString("bf014272-5030-3dd8-afca-b6212d5cda0c")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Warthin-Starry staining", UUID.fromString("e27c758a-6fc1-3f02-a685-7d13f3c7597f")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Wayson stain (substance)", UUID.fromString("07dc6c82-4751-36d6-88a3-9fd454d8ed51")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Wound care management (procedure)", UUID.fromString("7041315b-10a6-39eb-a298-03a8d8dfb74e")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Wright-Giemsa stain technique (qualifier value)", UUID.fromString("f6630e3f-115a-3c2c-80f3-a8fc8101756a")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("Wright stain method (procedure)", UUID.fromString("51673115-8f82-37db-b3c4-25c3095f3169")).getNid(), builderService, stamp);
                addObservedByMethod(transaction, new ConceptProxy("X-ray electromagnetic radiation (physical force)", UUID.fromString("d0899fdf-02f6-34cc-8d68-19718bd0507b")).getNid(), builderService, stamp);



            }
            
            
            for (int systemNid : systems.asArray()) {
                Optional<? extends Chronology> c = Get.identifiedObjectService().getChronology(systemNid);
                if (c.isPresent() && c.get().isLatestVersionActive()) {
                    addInheresInConcept(transaction, systemNid, builderService, stamp);
                }
                
            }
            for (int componentNid : components.asArray()) {
                Optional<? extends Chronology> c = Get.identifiedObjectService().getChronology(componentNid);
                if (c.isPresent() && c.get().isLatestVersionActive()) { 
                    addObservesComponent(transaction, componentNid, builderService, stamp);
                }
            }
            return null;
        } finally {
            Get.activeTasks().remove(this);
        }
    }

    private void addObservedByMethod(Transaction transaction, int methodNid, ConceptBuilderService builderService, int stamp) throws NoSuchElementException, IllegalStateException {
        LogicalExpressionBuilder eb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
        eb.sufficientSet(eb.and(eb.conceptAssertion(MetaData.PHENOMENON____SOLOR),
                eb.someRole(MetaData.ROLE_GROUP____SOLOR,
                        eb.and(eb.someRole(methodProxy.getNid(), eb.conceptAssertion(methodNid))))));
                
        StringBuilder conceptNameBuilder = new StringBuilder();
        conceptNameBuilder.append("Phenomenon observed by ");
        conceptNameBuilder.append(manifold.getPreferredDescriptionText(methodNid));
        buildConcept(transaction, builderService, conceptNameBuilder, eb, stamp);
    }

    private void addObservesComponent(Transaction transaction, int componentNid, ConceptBuilderService builderService, int stamp) throws NoSuchElementException, IllegalStateException {
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
        buildConcept(transaction, builderService, conceptNameBuilder, eb, stamp);
    }

    private void addInheresInConcept(Transaction transaction, int inheresInNid, ConceptBuilderService builderService, int stamp) throws IllegalStateException, NoSuchElementException {
        LogicalExpressionBuilder eb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
        eb.sufficientSet(eb.and(eb.conceptAssertion(MetaData.PHENOMENON____SOLOR),
                eb.someRole(MetaData.ROLE_GROUP____SOLOR,
                        eb.and(eb.someRole(inheresInProxy.getNid(), eb.conceptAssertion(inheresInNid))))));
        
        StringBuilder conceptNameBuilder = new StringBuilder();
        conceptNameBuilder.append("Inheres in ");
        conceptNameBuilder.append(manifold.getPreferredDescriptionText(inheresInNid));
        conceptNameBuilder.append(" phenomenon");
        buildConcept(transaction, builderService, conceptNameBuilder, eb, stamp);
    }

    private void buildConcept(Transaction transaction, ConceptBuilderService builderService, StringBuilder conceptNameBuilder, LogicalExpressionBuilder eb, int stamp) throws IllegalStateException, NoSuchElementException {
        String conceptName = conceptNameBuilder.toString();
        ConceptBuilder builder = builderService.getDefaultConceptBuilder(conceptName,
                "OP",
                eb.build(),
                TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
        builder.setPrimordialUuid(UuidT5Generator.get(UUID.fromString("d96cb408-b9ae-473d-a08d-ece06dbcedf9"), conceptName));
        List<Chronology> builtObjects = new ArrayList<>();
        builder.build(transaction, stamp, builtObjects);
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
