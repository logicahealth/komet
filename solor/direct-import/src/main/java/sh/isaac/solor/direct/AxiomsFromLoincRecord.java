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
package sh.isaac.solor.direct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import sh.isaac.MetaData;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.assertions.Assertion;

/**
 *
 * @author kec
 */
public class AxiomsFromLoincRecord {

    private final Set<String> methods = new HashSet<>();
    private final ConceptProxy methodProxy = new ConceptProxy("Method (attribute)",
            UUID.fromString("d0f9e3b1-29e4-399f-b129-36693ba4acbc"));
    private final ConceptProxy ultrasoundProxy = new ConceptProxy("Ultrasound imaging - action (qualifier value)",
            UUID.fromString("c02fd67b-db30-3371-be95-0b7a94509a10"));
    private final ConceptProxy ekgProxy = new ConceptProxy("Electrocardiographic procedure (procedure)",
            UUID.fromString("2dc7d2f4-1fc1-30d9-9d84-ac2202d98fb4"));
    private final ConceptProxy mrProxy = new ConceptProxy("Magnetic resonance imaging - action (qualifier value)",
            UUID.fromString("0145be15-1dc4-313b-9321-e6db22285cfe"));

    //ConceptProxy Set 1
    private final ConceptProxy coldProxy = new ConceptProxy("Cold incubation, 24 hours",UUID.fromString("1e4e0092-999f-32b6-8dd1-883d270ab536"));
    private final ConceptProxy aceProxy = new ConceptProxy("Adverse Childhood Experience questionnaire (assessment scale)",UUID.fromString("1b5e2fc4-5f55-36ec-aa46-67be3f087a48"));
    private final ConceptProxy actProxy = new ConceptProxy("Asthma control test (assessment scale)",UUID.fromString("f448ec92-0528-35fd-804c-a30706949325"));
    private final ConceptProxy auditProxy = new ConceptProxy("Alcohol use disorders identification test (assessment scale)",UUID.fromString("73f525db-1bb5-327f-95a0-548de59c4bd4"));
    private final ConceptProxy acidProxy = new ConceptProxy("Acid fast stain technique (qualifier value)",UUID.fromString("8cde1c5c-08d2-3cb6-a164-12054a9e0059"));
    private final ConceptProxy acidZiehlProxy = new ConceptProxy("Ziehl-Neelsen stain method (procedure)",UUID.fromString("d31ef55b-63d6-3e75-a2b9-8823b6baca29"));
    private final ConceptProxy acidPhosphateProxy = new ConceptProxy("Acid phosphatase stain method (procedure)",UUID.fromString("24a1dd80-20b7-3642-a6c8-12a375df3b15"));
    private final ConceptProxy acridineProxy = new ConceptProxy("Acridine orange stain method (procedure)",UUID.fromString("4817603d-75f6-31b8-a789-2816c305cc22"));
    private final ConceptProxy aerobicProxy = new ConceptProxy("Aerobic culture technique (qualifier value)",UUID.fromString("7eb6ad25-6f33-3d88-b25a-0bbd379d5c2a"));
    private final ConceptProxy aerospaceProxy = new ConceptProxy("Aerospace medicine (qualifier value)",UUID.fromString("404e1139-873e-3f54-b5d0-dcdae0077e29"));
    private final ConceptProxy airwayProxy = new ConceptProxy("Airway pressure monitoring (regime/therapy)",UUID.fromString("584c8d63-c5c2-39e6-83fe-c52b4eae6dea"));
    private final ConceptProxy albertsProxy = new ConceptProxy("Albert's stain method (procedure)",UUID.fromString("9a51dba0-13b7-38ef-8809-d0064251f7e0"));
    private final ConceptProxy alcianProxy = new ConceptProxy("Alcian blue stain method (procedure)",UUID.fromString("122fbccf-e7f0-34e1-9f66-a7393aa36bbe"));
    private final ConceptProxy alcianSulfatedProxy = new ConceptProxy("Sulphated alcian blue stain method",UUID.fromString("fd61f30b-9dec-3b1c-8068-5bd7766a673d"));
    private final ConceptProxy alcianAcidSchiffProxy = new ConceptProxy("Alcian blue with Periodic acid Schiff stain method (procedure)",UUID.fromString("5312c8f3-3f0c-3aef-8a57-5d3428d7eb2a"));
    private final ConceptProxy alizarinProxy = new ConceptProxy("Alizarin red S stain method (procedure)",UUID.fromString("18439499-bd0d-3fcd-8bf4-c6e7c480819e"));
    private final ConceptProxy allergyProxy = new ConceptProxy("Clinical immunology/allergy (qualifier value)",UUID.fromString("b83cea60-fced-3653-a7d8-0f7ede02b806"));
    private final ConceptProxy amniocentesisProxy = new ConceptProxy("Incision and drainage of amnion",UUID.fromString("09dee6b5-30d7-3a03-a7d9-bf5ac1b48865"));
    private final ConceptProxy anaerobicProxy = new ConceptProxy("Anaerobic culture technique (qualifier value)",UUID.fromString("5a352b7f-b86f-3129-b2e3-e2d5f77b1957"));
    private final ConceptProxy anesthesiologyProxy = new ConceptProxy("Anesthetics (qualifier value)",UUID.fromString("5c98fbba-4b1a-341a-9820-18795f4a8031"));
    private final ConceptProxy angiogramProxy = new ConceptProxy("Diagnostic angiography",UUID.fromString("e5eb5f33-9371-3881-88ea-14b343d01693"));
    private final ConceptProxy anoscopyProxy = new ConceptProxy("Endoscopy of anus",UUID.fromString("d3407043-8aae-3fdc-aadd-90b36d79f631"));
    private final ConceptProxy argentaffinProxy = new ConceptProxy("Argentaffin stain method (procedure)",UUID.fromString("6eadecb2-b415-39d2-8105-c1b146110dff"));
    private final ConceptProxy arthroscopyProxy = new ConceptProxy("Arthroscopy (procedure)",UUID.fromString("4bf05b37-076a-3a6a-ad53-b10bbf83cfc5"));
    private final ConceptProxy auscultationProxy = new ConceptProxy("Listening",UUID.fromString("a46ab576-56b5-3452-b5ae-b4629bebd867"));
    private final ConceptProxy automatedProxy = new ConceptProxy("Automatic",UUID.fromString("3bd927e3-db01-3af7-a9fb-ef82e8ed366d"));
    private final ConceptProxy automatedCountProxy = new ConceptProxy("Automated count technique (qualifier value)",UUID.fromString("abd11755-7337-3802-86ea-bcfa94df8523"));
    private final ConceptProxy azureEosinProxy = new ConceptProxy("Azure-eosin stain method (procedure)",UUID.fromString("12c85de6-ce8c-3937-adb2-239cf849d373"));
    private final ConceptProxy bsProxy = new ConceptProxy("Brigance Screens-II (assessment scale)",UUID.fromString("428c9add-15eb-3732-a2e9-cfa1dc225cf2"));
    private final ConceptProxy bacterialProxy = new ConceptProxy("Bacterial subtyping (procedure)",UUID.fromString("9a0de789-9cda-34f9-b11b-c713bb3841dc"));
    private final ConceptProxy bandingProxy = new ConceptProxy("Banding (procedure)",UUID.fromString("d859b722-6cd9-3e0f-b241-b0046bbae10d"));
    private final ConceptProxy basicProxy = new ConceptProxy("Fuchsin basic stain method (procedure)",UUID.fromString("6fc05803-283f-3bad-8284-e106d09b40f7"));
    private final ConceptProxy bennholdProxy = new ConceptProxy("Putchler's modification Congo red stain method",UUID.fromString("e6cc9507-e3db-39e1-b271-021452e0d870"));
    private final ConceptProxy bielschowskyProxy = new ConceptProxy("Bielschowsky's stain",UUID.fromString("a609519a-fde5-3a9c-8c7c-1660a75e86a1"));
    private final ConceptProxy bioassayProxy = new ConceptProxy("Bioassay (procedure)",UUID.fromString("355bddcb-8518-33d5-81e2-48f36dd982e1"));
    private final ConceptProxy bleachProxy = new ConceptProxy("Mallory's bleach stain",UUID.fromString("c51326d7-2f37-3aa7-87d8-bf465a97f1ee"));
    private final ConceptProxy bodianProxy = new ConceptProxy("Bodian stain method (procedure)",UUID.fromString("bea92c40-83fb-3c1e-97b4-46183a5c8126"));
    private final ConceptProxy brainProxy = new ConceptProxy("Acquired brain injury (disorder)",UUID.fromString("b587cb55-69e1-322b-8989-5736b87cfd93"));
    private final ConceptProxy brilliantProxy = new ConceptProxy("Cresyl blue BBS",UUID.fromString("9734f183-78f0-38e4-8027-86321f3547c6"));
    private final ConceptProxy bronchoscopyProxy = new ConceptProxy("Tracheobronchial endoscopy",UUID.fromString("a7e01789-b882-3563-8521-dfe073a53798"));
    private final ConceptProxy brownProxy = new ConceptProxy("Brown-Brenn stain method (procedure)",UUID.fromString("4e333536-5077-3a5f-889c-1230bbc48b12"));
    private final ConceptProxy butyrateProxy = new ConceptProxy("Butyrate esterase stain method (procedure)",UUID.fromString("80394986-57c2-3df6-970c-5e54e6721c03"));
    private final ConceptProxy c3bProxy = new ConceptProxy("Complement component 3b binding assay",UUID.fromString("992aedf2-201c-3f58-9e77-5f2f44f6b6b1"));
    private final ConceptProxy cdiProxy = new ConceptProxy("Child Development Inventory (assessment scale)",UUID.fromString("641f97c2-5d00-3e27-a431-b236c8d8c268"));
    private final ConceptProxy cdrProxy = new ConceptProxy("Dementia rating scale",UUID.fromString("c7ab9895-e80d-3a7c-b974-4a09b712ffcb"));
    private final ConceptProxy cieProxy = new ConceptProxy("Countercurrent electrophoresis measurement (procedure)",UUID.fromString("f1342b58-9c71-336e-8708-392ce418d7b7"));
    private final ConceptProxy ctProxy = new ConceptProxy("Connecticut (geographic location)",UUID.fromString("4a5e423c-e462-3eab-a3ac-22fdc5abd195"));
    private final ConceptProxy ctSpiralProxy = new ConceptProxy("Spiral computed tomography scan (procedure)",UUID.fromString("3b8cf2f3-85bf-360b-a956-cb5937532898"));
    private final ConceptProxy calculatedProxy = new ConceptProxy("Calculated (qualifier value)",UUID.fromString("3fd8182a-9976-3837-bf73-a93c90638746"));
    private final ConceptProxy calculatedOxygenProxy = new ConceptProxy("Calculation from oxygen partial pressure (qualifier value)",UUID.fromString("c70e7ec7-62f8-3696-b339-810e46ba370e"));
    private final ConceptProxy capillaryProxy = new ConceptProxy("Capillary electrophoresis (procedure)",UUID.fromString("bc3b7254-de8e-3dc1-bc45-2233f35020e3"));
    private final ConceptProxy carbolFuchsinProxy = new ConceptProxy("Product containing carbol-fuchsin (medicinal product)",UUID.fromString("94732756-cdb3-3cc7-8b31-05d12b986c6b"));
    private final ConceptProxy carbonProxy = new ConceptProxy("PCO>2<, blood",UUID.fromString("cfc0d49a-0fa2-3c68-ae50-468436073fed"));
    private final ConceptProxy cardiacCathProxy = new ConceptProxy("Insertion of catheter into heart chamber",UUID.fromString("21a5e5c5-7d51-3ce6-90d2-df08b6450d05"));
    private final ConceptProxy cardiacSurgeryProxy = new ConceptProxy("Operative procedure on heart",UUID.fromString("3300a761-a95d-3565-a0f6-19a73ee6962a"));
    private final ConceptProxy cardiovascularProxy = new ConceptProxy("Disorder of cardiovascular system (disorder)",UUID.fromString("98e3486c-c855-32ff-9052-dfe3790ac8d6"));
    private final ConceptProxy caseManagerProxy = new ConceptProxy("Case manager (occupation)",UUID.fromString("dd9f78b5-3239-36b9-95ca-37a3c34495e0"));
    private final ConceptProxy chemicalProxy = new ConceptProxy("Chemical pathology (qualifier value)",UUID.fromString("e3cad55e-054e-3223-acbf-200bde84c6e6"));
    private final ConceptProxy childProxy = new ConceptProxy("Child and adolescent psychiatry (qualifier value)",UUID.fromString("8a2a2cf2-0faa-34c6-aa97-0d8c217e5065"));
    private final ConceptProxy churukianSchenkProxy = new ConceptProxy("Churukian-Schenk stain method (procedure)",UUID.fromString("eaef5896-8658-3aa9-bd35-94564958aab2"));
    private final ConceptProxy clinicalGeneticsProxy = new ConceptProxy("Clinical genetics (qualifier value)",UUID.fromString("82b9f915-4477-3d1c-aab8-dbc6aae6189d"));
    private final ConceptProxy clinicalNurseProxy = new ConceptProxy("Clinical nurse specialist (occupation)",UUID.fromString("8669d1db-621a-3512-9028-1903d9a54f21"));
    private final ConceptProxy clinicalPharmacologyProxy = new ConceptProxy("Clinical pharmacology (qualifier value)",UUID.fromString("75e0fbd7-4800-3c0c-baf8-84a52dd0975a"));
    private final ConceptProxy coagProxy = new ConceptProxy("Coagulation system screening",UUID.fromString("597fd950-7548-30c1-862f-f78c002bdf17"));
    private final ConceptProxy cockcroftGaultProxy = new ConceptProxy("Cockcroft-Gault formula (qualifier value)",UUID.fromString("0aced273-18b9-30c2-a412-24ad1a585db0"));
    private final ConceptProxy colloidalProxy = new ConceptProxy("Hale's colloidal ferric oxide stain method",UUID.fromString("5e3ac6e4-efae-3280-85a6-636f6a424647"));
    private final ConceptProxy colonoscopyProxy = new ConceptProxy("Endoscopy of colon",UUID.fromString("76a6be8c-522a-3e24-b284-d7d26e3f0d7a"));
    private final ConceptProxy colposcopyProxy = new ConceptProxy("Colposcopy (procedure)",UUID.fromString("e2690ecc-e757-35ea-94a8-931b75318246"));
    private final ConceptProxy confirmProxy = new ConceptProxy("Confirmatory technique (qualifier value)",UUID.fromString("a855399b-2582-3f04-95b6-e57562a5577b"));
    private final ConceptProxy confrontationProxy = new ConceptProxy("Confrontation visual field test (procedure)",UUID.fromString("81ac574e-8651-3d7c-8e95-3cb7aff94d7d"));
    private final ConceptProxy conglutininProxy = new ConceptProxy("Conglutinin assay (procedure)",UUID.fromString("47f11283-1287-31d8-96b1-3df3f8050250"));
    private final ConceptProxy congoProxy = new ConceptProxy("Congo red stain method (procedure)",UUID.fromString("3d570fc9-ea42-3496-b767-86d233b260fb"));
    private final ConceptProxy continuousProxy = new ConceptProxy("Continuous (qualifier value)",UUID.fromString("e1d8431c-ceaf-3209-9383-71219f621b4e"));
    private final ConceptProxy coverProxy = new ConceptProxy("Eye screen test",UUID.fromString("e5cbcec0-b01c-3294-8e71-afcdc2abe33c"));
    private final ConceptProxy cresylProxy = new ConceptProxy("Cresyl echt violet stain method (procedure)",UUID.fromString("09551b1e-c828-3bab-9e1b-371d73fd783b"));
    private final ConceptProxy criticalProxy = new ConceptProxy("Critical care medicine (qualifier value)",UUID.fromString("4f7a31fd-4a63-37a3-b56e-7d3b09e096a9"));
    private final ConceptProxy crystalProxy = new ConceptProxy("Methyl violet 10B stain method",UUID.fromString("26087612-eb59-3e09-bbd0-f125caa66949"));
    private final ConceptProxy cysticProxy = new ConceptProxy("Cystic fibrosis respiratory culture (procedure)",UUID.fromString("c4b8334b-2a89-3ccf-bef7-0b59622f4368"));
    private final ConceptProxy cytologyProxy = new ConceptProxy("Cytology technique (qualifier value)",UUID.fromString("7a81540d-a7c7-389a-ae95-06d0a4019920"));
    private final ConceptProxy dastProxy = new ConceptProxy("Drug abuse screening test (assessment scale)",UUID.fromString("0568552c-8b3e-35f4-866a-0dc2ae37b7ac"));
    private final ConceptProxy deProxy = new ConceptProxy("De Galantha stain method (procedure)",UUID.fromString("dc32906e-8964-3d43-ae8c-e6789e99e1ec"));
    private final ConceptProxy dentistryProxy = new ConceptProxy("Dentistry (qualifier value)",UUID.fromString("1c973293-f529-3fda-bb71-5f5a30c0a366"));
    private final ConceptProxy dermatologyProxy = new ConceptProxy("Dermatology (qualifier value)",UUID.fromString("349dbabb-3156-3885-b9fd-1c3cf8db1bf4"));
    private final ConceptProxy diagnosticProxy = new ConceptProxy("Imaging (procedure)",UUID.fromString("5f9a1cfd-dd31-3bb2-aa55-98c453c2a311"));
    private final ConceptProxy dilutionProxy = new ConceptProxy("Dilution (qualifier value)",UUID.fromString("64c92399-c8fd-3906-83c3-d9be6e60d487"));
    private final ConceptProxy directProxy = new ConceptProxy("Direct assay technique (qualifier value)",UUID.fromString("b8f0b8b0-6f37-342a-9751-1100af90f8e3"));
    private final ConceptProxy dyeProxy = new ConceptProxy("Dye test (qualifier value)",UUID.fromString("f4e75e75-d37c-3467-9ae5-c292a46d38e9"));
    private final ConceptProxy dynamometerProxy = new ConceptProxy("Dynamometer (physical object)",UUID.fromString("5ac91e59-ed3f-364f-85da-8313ed3599a9"));
    private final ConceptProxy eegProxy = new ConceptProxy("Electroencephalography",UUID.fromString("e26b0d15-2577-37ef-b4c7-7a394c1f5f2d"));
    private final ConceptProxy egdProxy = new ConceptProxy("Upper gastrointestinal endoscopy",UUID.fromString("8c5b5a90-5b23-3b42-9fbc-5444e4d4b80b"));
    private final ConceptProxy epdsProxy = new ConceptProxy("Edinburgh postnatal depression scale (assessment scale)",UUID.fromString("a1d4af9d-b266-388a-bae7-235c4c96e418"));
    private final ConceptProxy ercpProxy = new ConceptProxy("Endoscopic retrograde choledochopancreatography",UUID.fromString("cad657a0-25dd-3450-b484-886bfbac8450"));
    private final ConceptProxy esiProxy = new ConceptProxy("Emergency Severity Index (assessment scale)",UUID.fromString("16e8e73f-a775-3b2c-aca6-e0acdcd3c466"));
    private final ConceptProxy electromyogramProxy = new ConceptProxy("Electromyography (procedure)",UUID.fromString("e840e11e-c570-3c23-9e39-da290b8fc8db"));
    private final ConceptProxy electronicProxy = new ConceptProxy("Electronic (qualifier value)",UUID.fromString("e47cbac2-1ecf-3bf4-bb1e-55c7ff574373"));
    private final ConceptProxy electrooculogramProxy = new ConceptProxy("Electro-oculography",UUID.fromString("97ef3265-16b0-3add-be6b-0e6cf377bc41"));
    private final ConceptProxy electrophoresisProxy = new ConceptProxy("Zone electrophoresis measurement",UUID.fromString("f700329d-1e87-39b4-aa87-a6998dacde80"));
    private final ConceptProxy electrophoresisAgaroseProxy = new ConceptProxy("Electrophoresis, agarose gel method (procedure)",UUID.fromString("8b86b81c-3577-3b02-8ee3-f1ed951e9786"));
    private final ConceptProxy electrophoresisCitrateProxy = new ConceptProxy("Electrophoresis, citrate agar method (procedure)",UUID.fromString("dd1b2cd2-7904-32c1-bbee-992bef14616d"));
    private final ConceptProxy electroretinogramProxy = new ConceptProxy("Electroretinography",UUID.fromString("1404fd40-a33c-3813-9673-7fcd73a7a994"));
    private final ConceptProxy endocrinologyProxy = new ConceptProxy("Endocrinology (qualifier value)",UUID.fromString("cf8bad57-2b13-39de-9022-346f7b7a3ec1"));
    private final ConceptProxy endoscopyProxy = new ConceptProxy("Inspection using endoscope",UUID.fromString("fb154f90-3ce7-3e92-8547-55947d431195"));
    private final ConceptProxy enteroscopyProxy = new ConceptProxy("Enteroscopy (procedure)",UUID.fromString("b917309b-9040-36e8-9cda-e6e5f16d6f61"));
    private final ConceptProxy environmentalProxy = new ConceptProxy("Environmental culture (procedure)",UUID.fromString("c640911f-5453-3933-9f59-336a85b295f8"));
    private final ConceptProxy epilepsyProxy = new ConceptProxy("Epileptic disorder",UUID.fromString("74ea5091-2131-343c-bb7c-c0238815f48f"));
    private final ConceptProxy esophagoscopyProxy = new ConceptProxy("Oesophagoscopy",UUID.fromString("e7fad093-503f-39cf-a9c5-5e84377cc66a"));
    private final ConceptProxy esteraseProxy = new ConceptProxy("Nonspecific esterase stain method (procedure)",UUID.fromString("06bafe98-cbbf-35d2-994d-9f8bd86d82cb"));
    private final ConceptProxy estimateProxy = new ConceptProxy("Estimated (qualifier value)",UUID.fromString("f0b32009-b800-33eb-95ef-5617d1bafbc9"));
    private final ConceptProxy estimatedFromHemogloobinProxy = new ConceptProxy("Estimated from glycated hemoglobin technique (qualifier value)",UUID.fromString("a41a55c8-d474-3f85-a9dc-1ec1c941b3f2"));
    private final ConceptProxy evokedProxy = new ConceptProxy("Evoked potential, function (observable entity)",UUID.fromString("31bff468-c19c-3a43-af94-61d053ca467b"));
    private final ConceptProxy exerciseProxy = new ConceptProxy("Exercise tolerance test (procedure)",UUID.fromString("7554ce6c-d510-3c50-b566-8717fa617a69"));
    private final ConceptProxy fishProxy = new ConceptProxy("Fish (organism)",UUID.fromString("0b353e99-572a-3f0e-8c6a-df99b35ba304"));
    private final ConceptProxy fabProxy = new ConceptProxy("Falls behavioural scale for older people",UUID.fromString("8c699773-1fcd-3238-a2b4-d764c116d30c"));
    private final ConceptProxy familyProxy = new ConceptProxy("Family practice (qualifier value)",UUID.fromString("61d9162c-44b3-3f03-9d7a-cf6b7179c4dd"));
    private final ConceptProxy fiteFaracoProxy = new ConceptProxy("Fite-Faraco stain method (procedure)",UUID.fromString("769750f8-94ad-3dea-8a4a-7fb7ad8da1f1"));
    private final ConceptProxy flexibleProxy = new ConceptProxy("Flexible sigmoidoscopy",UUID.fromString("9bd790a0-13db-3a82-8e0c-668c2ebadb6a"));
    private final ConceptProxy flowProxy = new ConceptProxy("Flow cytometry technique (qualifier value)",UUID.fromString("ac197c96-13f6-3330-a9cc-d15924b82ca3"));
    private final ConceptProxy fouchetProxy = new ConceptProxy("Fouchet stain method (procedure)",UUID.fromString("bbad8b72-7b08-3694-9a2a-c56533003712"));
    private final ConceptProxy fungalProxy = new ConceptProxy("Fungal subtyping (procedure)",UUID.fromString("a1a42443-e7ff-3bcd-ad86-ebc14a689f0e"));
    private final ConceptProxy fungusProxy = new ConceptProxy("Stain for fungus",UUID.fromString("4c72bd84-aeed-3799-9bfe-188aa1452c39"));
    private final ConceptProxy gcProxy = new ConceptProxy("Gas chromatography measurement (procedure)",UUID.fromString("95a0e1f8-87ba-30c7-bd6d-8723ce8b2603"));
    private final ConceptProxy gelProxy = new ConceptProxy("Gel (basic dose form)",UUID.fromString("879199f9-86f1-309f-81b1-611ac5cb0a0d"));
    private final ConceptProxy gastroenterologyProxy = new ConceptProxy("Gastroenterology (qualifier value)",UUID.fromString("586ffbe6-ef70-39c3-8083-9bb728f7ba6f"));
    private final ConceptProxy generalProxy = new ConceptProxy("General medicine (qualifier value)",UUID.fromString("70ab7c51-1962-3d04-bdb6-943fbc227980"));
    private final ConceptProxy genotypingProxy = new ConceptProxy("Genotype determination (procedure)",UUID.fromString("ed889903-29b2-3f65-a502-79b70de8ccdb"));
    private final ConceptProxy geriatricProxy = new ConceptProxy("Geriatric medicine (qualifier value)",UUID.fromString("7e7bca73-617b-35b0-ae97-e8825e06beaa"));
    private final ConceptProxy giemsaStainProxy = new ConceptProxy("Giemsa stain method (procedure)",UUID.fromString("5a26d32e-cfa2-3c4c-b4d7-fe8bd4df0cab"));
    private final ConceptProxy giemsaMicronProxy = new ConceptProxy("Three micron giemsa stain method (procedure)",UUID.fromString("bee0d093-7e2c-3dbe-9078-55349200b105"));


    //ConceptProxy Set 2
    private final ConceptProxy giemsaMayGrunwaldProxy = new ConceptProxy("May-Grunwald giemsa stain method (procedure)",UUID.fromString("8fca006e-68b3-3c62-aa78-54a3ce638e70"));
    private final ConceptProxy gimenezGimenezProxy = new ConceptProxy("Gimenez stain method (procedure)",UUID.fromString("f610424d-bdc9-3fdb-9a4a-96df7c3ed622"));
    private final ConceptProxy glucometerProxy = new ConceptProxy("Blood glucose meters (physical object)",UUID.fromString("628e9c07-b203-324a-859a-66f59950ad58"));
    private final ConceptProxy gomoriProxy = new ConceptProxy("Gomori stain method (procedure)",UUID.fromString("9fbcf437-e2af-3d75-9f94-8620a359c023"));
    private final ConceptProxy gramProxy = new ConceptProxy("Gram stain (substance)",UUID.fromString("c3a46237-42c8-3261-80fa-9d7fea594862"));
    private final ConceptProxy gridleyProxy = new ConceptProxy("Gridley stain method (procedure)",UUID.fromString("c6dffa4c-13ed-3729-b57f-c398134eae1f"));
    private final ConceptProxy guthrieProxy = new ConceptProxy("Phenylalanine screening test, blood",UUID.fromString("cef204cc-04cb-3ddd-8a6c-a6adbc600410"));
    private final ConceptProxy gynecologicProxy = new ConceptProxy("Gynecological oncology (qualifier value)",UUID.fromString("1b6d73f5-4346-32e5-89b4-ae42851e8785"));
    private final ConceptProxy gynecologyProxy = new ConceptProxy("Gynecology (qualifier value)",UUID.fromString("db52ed68-116b-3261-b488-20deb122274f"));
    private final ConceptProxy haProxy = new ConceptProxy("Hemagglutination assay (procedure)",UUID.fromString("2896caae-5a11-3ddd-ac41-908abe32501e"));
    private final ConceptProxy haqProxy = new ConceptProxy("Stanford health assessment questionnaire",UUID.fromString("d7add6b5-c1de-3cb1-bd6f-0197c9e44fa1"));
    private final ConceptProxy hivProxy = new ConceptProxy("Human immunodeficiency virus (organism)",UUID.fromString("48ccc0c5-b120-3ed5-8f79-3eaeb644ddb8"));
    private final ConceptProxy hplcProxy = new ConceptProxy("High pressure liquid chromatography (procedure)",UUID.fromString("f640022e-71db-3030-8c6f-a620afc1141b"));
    private final ConceptProxy hallsProxy = new ConceptProxy("Hall's technique for bilirubin",UUID.fromString("dcf83756-e26f-3cb7-aee4-cab1ddacb9aa"));
    private final ConceptProxy hanselProxy = new ConceptProxy("Hansel's stain",UUID.fromString("8ae2cf73-94c3-383a-80e3-5f277c46c08f"));
    private final ConceptProxy hematoxylinProxy = new ConceptProxy("Hematoxylin and eosin stain method (procedure)",UUID.fromString("e8fdf03a-ca32-30d8-8a9e-ef5f688a1340"));
    private final ConceptProxy hematoxylinEosinHarrisProxy = new ConceptProxy("Harris regressive hematoxylin and eosin stain method (procedure)",UUID.fromString("69d99945-1807-3894-b5d6-9d2f218d8173"));
    private final ConceptProxy hematoxylinEosinMayersProxy = new ConceptProxy("Mayers progressive hematoxylin and eosin stain method (procedure)",UUID.fromString("57310ca9-4fa5-3915-8bb5-4990c3840de2"));
    private final ConceptProxy hepatologyProxy = new ConceptProxy("Hepatology (qualifier value)",UUID.fromString("43557dff-87f6-3603-a0e0-2432cbfc5635"));
    private final ConceptProxy highmanProxy = new ConceptProxy("Highman stain method (procedure)",UUID.fromString("d2d8d5de-702c-35e9-bb44-4c2b39150f39"));
    private final ConceptProxy holzerProxy = new ConceptProxy("Holzer stain method (procedure)",UUID.fromString("cfd2a0e1-d8fd-31fe-a6cc-052b1da42f0d"));
    private final ConceptProxy iaProxy = new ConceptProxy("Immunoassay method (procedure)",UUID.fromString("da1552e9-d57d-3bfd-bcb2-fa0f4d7aa902"));
    private final ConceptProxy ibProxy = new ConceptProxy("Immunoblot assay technique (qualifier value)",UUID.fromString("3f277a62-fe2b-3caf-808a-12676eafc622"));
    private final ConceptProxy icaProxy = new ConceptProxy("Islet cell cytoplasma antibody",UUID.fromString("50980d8a-be49-34ea-9f8e-92ac32665da6"));
    private final ConceptProxy ipssProxy = new ConceptProxy("International prostate symptom score (assessment scale)",UUID.fromString("29f88b06-5741-38b3-af20-a712df1ef023"));
    private final ConceptProxy iseProxy = new ConceptProxy("Ion selective electrode measurement technique (qualifier value)",UUID.fromString("ea4524fb-8a9b-3df2-b353-6c464fae15a6"));
    private final ConceptProxy imagingProxy = new ConceptProxy("Imaging technique (qualifier value)",UUID.fromString("8dd99fbe-40ac-3461-b22b-48cac7d6241a"));
    private final ConceptProxy immProxy = new ConceptProxy("Immunological",UUID.fromString("08ee9b9c-9387-33d1-99fd-8cce1b62b32a"));
    private final ConceptProxy immunoelectrophoresisProxy = new ConceptProxy("Immunoelectrophoresis technique (qualifier value)",UUID.fromString("8b8398ba-acc0-3b36-8ad4-4137fc0e13e4"));
    private final ConceptProxy immunofixationProxy = new ConceptProxy("Immunofixation (procedure)",UUID.fromString("ce41c420-0036-3faa-a316-66e61dff9baa"));
    private final ConceptProxy immunophenotypingProxy = new ConceptProxy("Immunophenotyping (procedure)",UUID.fromString("73b2ab0f-ff97-334f-b226-f63dea93a9b9"));
    private final ConceptProxy impedanceProxy = new ConceptProxy("Impedance (attribute)",UUID.fromString("d778d923-3f93-3b21-8841-377ab7385e18"));
    private final ConceptProxy indiaProxy = new ConceptProxy("India ink staining technique (qualifier value)",UUID.fromString("bef038d3-41d6-361c-a316-41de3fb62c51"));
    private final ConceptProxy indirectProxy = new ConceptProxy("Indirect antiglobulin test",UUID.fromString("fc2751e3-8f2a-39ca-9420-331840554d00"));
    private final ConceptProxy infectiousProxy = new ConceptProxy("Infective disorder",UUID.fromString("6a55322e-f3de-3d93-ae8d-be206b9339de"));
    private final ConceptProxy infraredProxy = new ConceptProxy("Infrared spectroscopy technique (qualifier value)",UUID.fromString("f65cf27c-f116-3ec7-848f-4dccc9363a9a"));
    private final ConceptProxy inspectionProxy = new ConceptProxy("Visual observation",UUID.fromString("325eab05-452f-3488-9e45-a42204f8f830"));
    private final ConceptProxy internalProxy = new ConceptProxy("Internal medicine (qualifier value)",UUID.fromString("c885f77a-6c25-3c77-8919-88bdb2aba3e9"));
    private final ConceptProxy interventionalProxy = new ConceptProxy("Interventional radiology (procedure)",UUID.fromString("ee4615a1-9f40-3df9-b724-cce682016279"));
    private final ConceptProxy invasiveProxy = new ConceptProxy("Invasive (qualifier value)",UUID.fromString("91666bef-12a5-3412-886f-ae03ea628fd8"));
    private final ConceptProxy ironProxy = new ConceptProxy("Iron hematoxylin stain method (procedure)",UUID.fromString("df998614-cb60-34b0-a88a-ccabffbfe454"));
    private final ConceptProxy isoelectricProxy = new ConceptProxy("Isoelectric focusing measurement (procedure)",UUID.fromString("1d9d745b-737f-311f-9de5-190edc72ac3a"));
    private final ConceptProxy kohProxy = new ConceptProxy("Potassium hydroxide preparation technique (qualifier value)",UUID.fromString("35a6bdda-0ac8-30d6-b109-9671931847b1"));
    private final ConceptProxy keratometryProxy = new ConceptProxy("Keratometry (procedure)",UUID.fromString("356da8d9-3833-3e39-ad40-6fea9be99365"));
    private final ConceptProxy laProxy = new ConceptProxy("Latex agglutination test (procedure)",UUID.fromString("701d99a3-9256-3889-8523-39d08fa3339e"));
    private final ConceptProxy lhrProxy = new ConceptProxy("Leukocyte histamine release test",UUID.fromString("9b8a9f40-e498-3acd-9801-a5e34afba5c5"));
    private final ConceptProxy lissProxy = new ConceptProxy("Low ionic strength saline technique (qualifier value)",UUID.fromString("66e88940-0069-3e72-b7b4-f603cdd8dea7"));
    private final ConceptProxy peritoneoscopyProxy = new ConceptProxy("Peritoneoscopy",UUID.fromString("2a9c218b-2883-3af0-8a08-f6580fab9bb1"));
    private final ConceptProxy lawsonvanProxy = new ConceptProxy("Lawson-Van Gieson stain method (procedure)",UUID.fromString("d48712e2-305c-3da5-8937-981188ddd75c"));
    private final ConceptProxy licensedProxy = new ConceptProxy("Licensed practical nurse (occupation)",UUID.fromString("a10839d5-6433-3d1c-a57b-fc4a439fa1b8"));
    private final ConceptProxy luxolProxy = new ConceptProxy("Luxol fast blue with cresyl violet stain method (procedure)",UUID.fromString("dd4280c5-a1f4-36f8-8bec-4f6ea4a3e413"));
    private final ConceptProxy luxolPeriodicProxy = new ConceptProxy("Luxol fast blue with Periodic acid-Schiff stain method (procedure)",UUID.fromString("0e45ddea-794f-31e8-b884-ed92d59b348e"));
    private final ConceptProxy mfadyeanProxy = new ConceptProxy("M'fadyean stain",UUID.fromString("680753b0-b7ef-3cdc-a6d5-4211539f5810"));
    private final ConceptProxy mastProxy = new ConceptProxy("Michigan alcoholism screening test (assessment scale)",UUID.fromString("07efdf71-2460-37b2-9cee-f4c416e0a5f0"));
    private final ConceptProxy mgProxy = new ConceptProxy("Radiographic examination of breast",UUID.fromString("6707d8bb-99ec-3e0d-ad7a-7a4745d40fe6"));
    private final ConceptProxy micProxy = new ConceptProxy("Minimum inhibitory concentration",UUID.fromString("b6159273-9b20-3067-8137-37249261c0e5"));
    private final ConceptProxy mmseProxy = new ConceptProxy("Mini-mental state examination (assessment scale)",UUID.fromString("446eb2ee-706a-3cbe-9a99-91237f27adea"));
    private final ConceptProxy mrSpectroscopyProxy = new ConceptProxy("Magnetic resonance spectroscopy (procedure)",UUID.fromString("20470e57-3f09-3a3e-bf34-8993da195c92"));
    private final ConceptProxy macchiavelloProxy = new ConceptProxy("Macchiavello stain method (procedure)",UUID.fromString("f08cfc51-2b81-3ec5-8582-ea6d8b6f7be8"));
    private final ConceptProxy macroscopyProxy = new ConceptProxy("Sample macroscopy (procedure)",UUID.fromString("d5fdb7b6-2cd2-31b8-ab83-58c9a15f253c"));
    private final ConceptProxy malachiteProxy = new ConceptProxy("Malachite green stain method (procedure)",UUID.fromString("6a29f510-0bfb-31ef-8a9d-a283b41a1929"));
    private final ConceptProxy malariaProxy = new ConceptProxy("Malaria smear (procedure)",UUID.fromString("03aa4165-ff95-3ea6-9705-5e330de20219"));
    private final ConceptProxy malariaThickProxy = new ConceptProxy("Malaria thick smear (procedure)",UUID.fromString("123266bb-207b-3ef4-be89-45ed2a583c91"));
    private final ConceptProxy malariaThinProxy = new ConceptProxy("Malaria thin smear (procedure)",UUID.fromString("7b2d171f-46e4-3ac4-9214-66fc10813a2b"));
    private final ConceptProxy malloryHeidenhainProxy = new ConceptProxy("Mallory Heidenhain stain method (procedure)",UUID.fromString("3c8005ae-7dd6-3fd1-ba05-a557aff1bf23"));
    private final ConceptProxy manualProxy = new ConceptProxy("Manual (qualifier value)",UUID.fromString("e48adcb0-3cc3-359f-b9b7-bc4943a8b6ec"));
    private final ConceptProxy manualCountProxy = new ConceptProxy("Manual count technique (qualifier value)",UUID.fromString("dd0da81b-83fc-3c75-a75c-0e22babe5224"));
    private final ConceptProxy measuredProxy = new ConceptProxy("Measured (qualifier value)",UUID.fromString("e215fcf2-7962-35e9-bfad-26f65842ca77"));
    private final ConceptProxy medicalProxy = new ConceptProxy("Medical student (occupation)",UUID.fromString("b629ecda-2dbf-3c06-8bf5-9a9f16945fda"));
    private final ConceptProxy mentalProxy = new ConceptProxy("Nurse psychotherapist (occupation)",UUID.fromString("b9440c1f-a925-326a-9148-dca6f49a2a6b"));
    private final ConceptProxy methenamineProxy = new ConceptProxy("Methenamine silver nitrate stain method (procedure)",UUID.fromString("6677a5e4-a366-34cf-b21d-305493b173ff"));
    private final ConceptProxy methenamineGrocottProxy = new ConceptProxy("Grocott methenamine silver stain method",UUID.fromString("1140a33d-23db-32ec-818d-9fa42b9f82ce"));
    private final ConceptProxy methenamineJonesProxy = new ConceptProxy("Jones stain method",UUID.fromString("4be79f21-d9e8-3eeb-b05c-c0fbf43307b0"));
    private final ConceptProxy methylProxy = new ConceptProxy("Methyl green stain method (procedure)",UUID.fromString("05cb3496-a14a-3bd2-aa67-a330b9d5742d"));
    private final ConceptProxy methyleneProxy = new ConceptProxy("Methylthioninium chloride stain method",UUID.fromString("51695334-620b-31bb-ab92-c9b42b1198b6"));
    private final ConceptProxy methyleneLoefflerProxy = new ConceptProxy("Loeffler methylene blue staining",UUID.fromString("9443e01b-06b8-3d67-a670-59b6bd642bd2"));
    private final ConceptProxy microscopyProxy = new ConceptProxy("Microscopy (procedure)",UUID.fromString("d5f3b7af-3308-3b86-a673-9909a744c448"));
    private final ConceptProxy microscopyElectronProxy = new ConceptProxy("Electron microscopic study (procedure)",UUID.fromString("6a03a7aa-ac46-3d1b-8f20-acb7592258bd"));
    private final ConceptProxy microscopyLightProxy = new ConceptProxy("Observation by light microscope",UUID.fromString("14075199-7f12-344e-8808-98f2b858463d"));
    private final ConceptProxy molgenProxy = new ConceptProxy("Molecular genetics procedure (procedure)",UUID.fromString("4d618061-0dff-3d52-aa0a-c7c54aeb7088"));
    private final ConceptProxy mucicarmineProxy = new ConceptProxy("Mucicarmine stain method (procedure)",UUID.fromString("462954ff-6d74-319c-a4b9-6f1dad1f1cc8"));
    private final ConceptProxy mucicarmineMayerProxy = new ConceptProxy("Mayer mucicarmine stain method (procedure)",UUID.fromString("287cc34c-0ff5-3638-a30e-ec2bd076748a"));
    private final ConceptProxy mycobacterialProxy = new ConceptProxy("Mycobacterial subtyping (procedure)",UUID.fromString("66dc8bca-2a5b-357e-a71f-c6fab27555bf"));
    private final ConceptProxy myeloperoxidaseProxy = new ConceptProxy("Myeloperoxidase stain method (procedure)",UUID.fromString("42266761-c213-3489-92a0-c2d306c5cbc9"));
    private final ConceptProxy nmProxy = new ConceptProxy("New Mexico (geographic location)",UUID.fromString("ad29aa91-8be2-3af0-acd8-10e8355c6445"));
    private final ConceptProxy ntdsProxy = new ConceptProxy("Neural tube defect (disorder)",UUID.fromString("39447cda-1fde-307a-b0cd-cdda390eb781"));
    private final ConceptProxy nyhaProxy = new ConceptProxy("New York Heart Association Classification (assessment scale)",UUID.fromString("9c9e86c9-efa6-3d4e-8f8f-e72086cd77b2"));
    private final ConceptProxy neisserProxy = new ConceptProxy("Neisser stain method (procedure)",UUID.fromString("24af0a08-8c3e-3c63-a543-8679381a2777"));
    private final ConceptProxy nephelometryProxy = new ConceptProxy("Turbidity test",UUID.fromString("a50bc0cd-54e1-3a61-b183-5035209bb1b4"));
    private final ConceptProxy nephrologyProxy = new ConceptProxy("Renal medicine",UUID.fromString("d20f017a-44fe-3d11-bc46-44fe91ad08a5"));
    private final ConceptProxy nerveProxy = new ConceptProxy("Nerve conduction (observable entity)",UUID.fromString("a7a36bac-4f13-3ddb-8940-37c27e2eb741"));
    private final ConceptProxy neurologicalProxy = new ConceptProxy("Operative procedure on nervous system",UUID.fromString("ff1df1be-e0a1-37c8-a4cc-f4c318a8a606"));
    private final ConceptProxy neurologyProxy = new ConceptProxy("Neurology (qualifier value)",UUID.fromString("1966e0c0-1c74-341f-8e63-495f01f75848"));
    private final ConceptProxy neurologyNurseProxy = new ConceptProxy("Neurology nurse (occupation)",UUID.fromString("19708ed6-5768-3c84-bffe-1b96eda36a73"));
    private final ConceptProxy neutralProxy = new ConceptProxy("Neutral red stain method (procedure)",UUID.fromString("913e7cbf-0a91-3156-b3d8-7390dc3d2f09"));
    private final ConceptProxy nightProxy = new ConceptProxy("Night blue stain method (procedure)",UUID.fromString("4b593ce9-6bda-38e1-9220-eb4954e1c828"));
    private final ConceptProxy nurseProxy = new ConceptProxy("Professional nurse (occupation)",UUID.fromString("8323b206-1927-38a8-8b16-f045cf3d6dc4"));
    private final ConceptProxy nursePracticionerProxy = new ConceptProxy("Nurse practitioner (occupation)",UUID.fromString("455c8cbc-911d-39eb-a564-518bfdbeb451"));
    private final ConceptProxy nursingProxy = new ConceptProxy("Nursing (qualifier value)",UUID.fromString("d734c42f-d25f-3a3d-b77a-cc339a3476df"));
    private final ConceptProxy octProxy = new ConceptProxy("Ornithine transcarbamylase",UUID.fromString("74173dcc-df72-3037-b69a-bab65ea32d9a"));
    private final ConceptProxy objectiveProxy = new ConceptProxy("Objective refraction (procedure)",UUID.fromString("e2e01c7d-d508-3fc5-a9a2-ceb59f46d9ba"));
    private final ConceptProxy obstetricsProxy = new ConceptProxy("Obstetrics and gynecology (qualifier value)",UUID.fromString("1c0be577-2044-3109-aefd-da49f83886c1"));
    private final ConceptProxy occupationalProxy = new ConceptProxy("Occupational medicine (qualifier value)",UUID.fromString("d047b6ea-73f7-34ef-af45-249d2f52ae0e"));
    private final ConceptProxy occupationalInerdisciplinaryProxy = new ConceptProxy("Occupational therapy (regime/therapy)",UUID.fromString("0db5d645-3a45-310a-9ee3-7eab0d9252a5"));
    private final ConceptProxy oilProxy = new ConceptProxy("Oil red O stain method (procedure)",UUID.fromString("61cfe82e-0ff4-3d91-a647-121ee9341564"));
    private final ConceptProxy ophthalmologyProxy = new ConceptProxy("Ophthalmology (qualifier value)",UUID.fromString("c1909ce3-d7e4-3649-ad38-a0383677cd68"));
    private final ConceptProxy ophthalmometerProxy = new ConceptProxy("Keratometer (physical object)",UUID.fromString("401018cd-c353-31a4-b8a9-aaf0977c3588"));
    private final ConceptProxy ophthalmoscopyProxy = new ConceptProxy("Ophthalmoscopy (procedure)",UUID.fromString("b65da9ee-5009-36fd-80ca-1fcc1ffca83a"));
    private final ConceptProxy oralProxy = new ConceptProxy("Oral and maxillofacial surgery (qualifier value)",UUID.fromString("052d9690-0ca6-34af-b222-736984908b31"));
    private final ConceptProxy orceinProxy = new ConceptProxy("Orcein stain method (procedure)",UUID.fromString("34f18ab3-24cf-355a-8739-75b0b91f5e4c"));
    private final ConceptProxy organismProxy = new ConceptProxy("Organism specific culture technique (qualifier value)",UUID.fromString("68f0b7e2-1e96-331f-8194-6e3912706399"));
    private final ConceptProxy otolaryngologyProxy = new ConceptProxy("Otolaryngology (qualifier value)",UUID.fromString("3118b2cf-b46f-310a-bfa4-5f3bd55f8c12"));
    private final ConceptProxy oximetryProxy = new ConceptProxy("Oxygen saturation measurement (procedure)",UUID.fromString("fa8c5f83-3f65-3e0f-a2b4-820a1254142d"));
    private final ConceptProxy oxygenProxy = new ConceptProxy("Oxygen analyzer, device (physical object)",UUID.fromString("2cf16995-71f9-304b-8378-086d93d34d54"));
    private final ConceptProxy pageProxy = new ConceptProxy("Polyacrylamide gel electrophoresis (procedure)",UUID.fromString("5d903f56-1cfe-388d-acf9-6b597f70802a"));
    private final ConceptProxy pfgeProxy = new ConceptProxy("Pulsed-field gel electrophoresis (procedure)",UUID.fromString("ab78b666-fc31-3d7a-8264-53d81879f28d"));
    private final ConceptProxy ptProxy = new ConceptProxy("Positron emission tomographic imaging - action (qualifier value)",UUID.fromString("0af8e5e8-a815-30ff-a157-4d287b372464"));
    private final ConceptProxy painProxy = new ConceptProxy("Analgesic (substance)",UUID.fromString("eb8f53dc-d4ec-370a-a1b1-f12ecdbc04bf"));
    private final ConceptProxy palliativeProxy = new ConceptProxy("Palliative care nurse (occupation)",UUID.fromString("ac64f3c0-b692-3397-b7a6-6f82b84afea9"));
    private final ConceptProxy palliativePhysicianProxy = new ConceptProxy("Palliative care physician (occupation)",UUID.fromString("115c5e74-b41a-34f3-8784-2ddf53de421f"));
    private final ConceptProxy palpationProxy = new ConceptProxy("Palpation - action (qualifier value)",UUID.fromString("3416ec21-d8fa-3656-848e-6d6837f3ce03"));
    private final ConceptProxy pastoralProxy = new ConceptProxy("Pastoral care (regime/therapy)",UUID.fromString("0bce05ec-0844-3af4-a928-cce9b1920216"));
    private final ConceptProxy patientProxy = new ConceptProxy("Patient (person)",UUID.fromString("86490a44-8539-31e2-8e0f-3a6253a72fca"));
    private final ConceptProxy peakProxy = new ConceptProxy("Peak flow meter (physical object)",UUID.fromString("cfe26ad0-6538-39a1-bab5-acd35cfda7dd"));
    private final ConceptProxy pediatricCardiologyProxy = new ConceptProxy("Pediatric cardiology (qualifier value)",UUID.fromString("579bff22-4102-3476-933e-d429b28984dd"));
    private final ConceptProxy pediatricEndocrinologyProxy = new ConceptProxy("Pediatric endocrinology (qualifier value)",UUID.fromString("65a6d728-8724-3701-8ed8-1d0141d7b87b"));
    private final ConceptProxy pediatricGastroProxy = new ConceptProxy("Pediatric gastroenterology (qualifier value)",UUID.fromString("53f92dd9-3fb9-346d-a27e-5a612ce500d1"));
    private final ConceptProxy pediatricNephrologyProxy = new ConceptProxy("Pediatric nephrology (qualifier value)",UUID.fromString("a1028c8d-0d1d-31ea-a065-fd43e79088b2"));
    private final ConceptProxy pediatricOtolaryngologyProxy = new ConceptProxy("Pediatric otolaryngology (qualifier value)",UUID.fromString("6df26b1d-997d-361e-bd14-601596cfbb2b"));
    private final ConceptProxy pediatricPulmonologyProxy = new ConceptProxy("Pediatric pulmonology (qualifier value)",UUID.fromString("821111ca-7b38-3beb-82a4-1ef9e9c2ad18"));
    private final ConceptProxy pediatricRheumatologyProxy = new ConceptProxy("Pediatric rheumatology (qualifier value)",UUID.fromString("cd13f274-be1f-3d3f-a4c0-abea94cc6f79"));
    private final ConceptProxy pediatricSurgeryProxy = new ConceptProxy("Pediatric surgery (qualifier value)",UUID.fromString("04adc570-5a1c-3a48-85c3-5a9bd6fc3ad7"));
    private final ConceptProxy pediatricsProxy = new ConceptProxy("Pediatrics",UUID.fromString("979558cc-99a3-3cb3-94c9-089fdffb8c67"));
    private final ConceptProxy pentachromeProxy = new ConceptProxy("Movat pentachrome stain method (procedure)",UUID.fromString("42bee416-4bf4-3760-a694-a2155a49a66a"));
    private final ConceptProxy perimetryProxy = new ConceptProxy("Perimetry (procedure)",UUID.fromString("51d68c7d-7c7f-393b-98bd-bdc7758a10a5"));
    private final ConceptProxy periodicProxy = new ConceptProxy("Periodic acid Schiff stain method (procedure)",UUID.fromString("8431464d-8f7e-3b70-9bba-26e44603bd89"));
    private final ConceptProxy peroxidaseProxy = new ConceptProxy("Peroxidase stain method, blood or bone marrow (procedure)",UUID.fromString("58b8b2ca-a46d-3165-9e65-8f9fb0915879"));
    private final ConceptProxy pharmacistProxy = new ConceptProxy("Pharmacist (occupation)",UUID.fromString("3bae419a-1707-357f-aa09-13caa7709bab"));
    private final ConceptProxy phenotypingProxy = new ConceptProxy("Phenotype determination (procedure)",UUID.fromString("3315e2df-a2bb-39d1-aabc-436da2ffc5f6"));
    private final ConceptProxy phoropterProxy = new ConceptProxy("Phoropter (physical object)",UUID.fromString("e6010daf-2a06-3166-ad57-95d8780e5f13"));


    //ConceptProxy Set 3
    private final ConceptProxy physicalProxy = new ConceptProxy("Physiotherapy procedure",UUID.fromString("42d5f509-2418-3d87-ab8d-05263b822ff1"));
    private final ConceptProxy physicianProxy = new ConceptProxy("Physician (occupation)",UUID.fromString("021e3dc2-8afe-3705-8d23-288b3fe077a9"));
    private final ConceptProxy physicianAssistantProxy = new ConceptProxy("Physician assistant (occupation)",UUID.fromString("15a12eb9-945a-3066-a4a9-60a73e788d02"));
    private final ConceptProxy physicianAttendingProxy = new ConceptProxy("Attending physician (occupation)",UUID.fromString("62923c0c-34b6-3c7e-ae3b-a699e3b3e24a"));
    private final ConceptProxy physicianResidentProxy = new ConceptProxy("Resident physician (occupation)",UUID.fromString("827c4a44-eef1-3b75-a049-9dd20ef5fc8b"));
    private final ConceptProxy plasticProxy = new ConceptProxy("Plastic operation (qualifier value)",UUID.fromString("3daac316-c0cd-3e6c-af0e-8c5a7f0df6a3"));
    private final ConceptProxy plateletProxy = new ConceptProxy("Platelet aggregation test (procedure)",UUID.fromString("c063cf79-3099-3429-ba33-9ce2cf2290c0"));
    private final ConceptProxy podiatryProxy = new ConceptProxy("Podiatry (qualifier value)",UUID.fromString("78f84195-2cbf-3bf5-904e-d93de19fe8e9"));
    private final ConceptProxy polysomnographyProxy = new ConceptProxy("Polysomnography (procedure)",UUID.fromString("b06b7729-cbd5-3380-b0e1-0db1ddfc1651"));
    private final ConceptProxy potassiumProxy = new ConceptProxy("Potassium ferrocyanide stain method",UUID.fromString("518afa3b-56be-35ff-a329-5da562b6d860"));
    private final ConceptProxy preventiveProxy = new ConceptProxy("Preventive medicine (qualifier value)",UUID.fromString("803685fc-e0a9-378b-8670-d3be7f4e883a"));
    private final ConceptProxy primaryProxy = new ConceptProxy("Primary care physician (occupation)",UUID.fromString("485a0709-0876-307d-b376-85171a497f9c"));
    private final ConceptProxy probeProxy = new ConceptProxy("Probe, device (physical object)",UUID.fromString("134bfb4e-86a8-3bab-a463-b680dd280a99"));
    private final ConceptProxy probeAmpTarProxy = new ConceptProxy("Probe with target amplification technique (qualifier value)",UUID.fromString("400973df-c050-3885-b8ad-e438375ef0ca"));
    private final ConceptProxy proctosigmoidoscopyRigidProxy = new ConceptProxy("Rigid proctosigmoidoscopy (procedure)",UUID.fromString("b27cd8e6-6f05-3807-8e74-64184f26c6e6"));
    private final ConceptProxy psychiatryProxy = new ConceptProxy("Psychiatry (qualifier value)",UUID.fromString("7b1e9ae7-7a15-3937-8478-2094a992af7a"));
    private final ConceptProxy psychologyProxy = new ConceptProxy("Psychology (qualifier value)",UUID.fromString("6f22d321-1bf4-31b2-8c2b-87c6e3c9682a"));
    private final ConceptProxy pulmonaryProxy = new ConceptProxy("Pulmonary disease",UUID.fromString("3882c94c-86db-3086-8c9a-4d44e39f1a21"));
    private final ConceptProxy pulmonaryFunctionProxy = new ConceptProxy("Pulmonary function (observable entity)",UUID.fromString("ce1df6e1-8757-3d3c-aa7e-9bcb97615ee6"));
    private final ConceptProxy pulseProxy = new ConceptProxy("Pulse oximetry technique (qualifier value)",UUID.fromString("5e05e0ef-9775-374d-a001-9e61e419799e"));
    private final ConceptProxy quinacrineProxy = new ConceptProxy("Quinacrine fluorescent stain method (procedure)",UUID.fromString("193bae62-f5a4-3842-8b40-c5bf6883ee16"));
    private final ConceptProxy rastProxy = new ConceptProxy("Radioallergosorbent test (procedure)",UUID.fromString("7ed75b6f-c91b-3aa3-a8da-12a0cd12c533"));
    private final ConceptProxy rflpProxy = new ConceptProxy("Restriction fragment length polymorphism technique (qualifier value)",UUID.fromString("0377d06a-f06e-3e79-b985-d09105ffe135"));
    private final ConceptProxy riaProxy = new ConceptProxy("Radioimmunoassay technique (qualifier value)",UUID.fromString("371d3025-af0d-3673-bb8a-2597a29f483f"));
    private final ConceptProxy ripaProxy = new ConceptProxy("Radioimmunoprecipitation assay (procedure)",UUID.fromString("8c8b22dd-508c-317d-9f74-da7aab9bf4b4"));
    private final ConceptProxy rprProxy = new ConceptProxy("Rapid plasma reagin test (procedure)",UUID.fromString("2f4df3c5-8767-3077-ae34-db6161b6ef79"));
    private final ConceptProxy radiationProxy = new ConceptProxy("Therapeutic radiology",UUID.fromString("66166d24-5bc0-3654-a4b3-c0035af0a099"));
    private final ConceptProxy rajiProxy = new ConceptProxy("Raji cell assay",UUID.fromString("f2d75b4f-2498-3768-b035-cca0d4beff6e"));
    private final ConceptProxy rapidProxy = new ConceptProxy("Rapidly (qualifier value)",UUID.fromString("09210ec5-df19-30e0-b3e2-7234f5d2d704"));
    private final ConceptProxy recreationalProxy = new ConceptProxy("Recreational therapy (regime/therapy)",UUID.fromString("9a7dcf0c-53d3-3195-8882-9ad74ae16c2d"));
    private final ConceptProxy registeredProxy = new ConceptProxy("Registered nurse (occupation)",UUID.fromString("5e98e0d7-c7c5-35aa-ba7e-49c8d349c2d9"));
    private final ConceptProxy reportedProxy = new ConceptProxy("Reporting",UUID.fromString("416df33b-fb51-3db3-a7bc-08f40e70bf94"));
    private final ConceptProxy respiratoryProxy = new ConceptProxy("Respiratory therapy (procedure)",UUID.fromString("579ac6f4-d26f-3c5a-a438-d24b18221926"));
    private final ConceptProxy reticulinProxy = new ConceptProxy("Reticulin stain method (procedure)",UUID.fromString("ce9bb41f-e25a-39da-b19f-ab36b9dd963c"));
    private final ConceptProxy rheumatologyProxy = new ConceptProxy("Rheumatology (qualifier value)",UUID.fromString("69521a28-7ff9-38fb-bfc3-426f4daf3b51"));
    private final ConceptProxy rhodamineProxy = new ConceptProxy("Rhodamine stain method (procedure)",UUID.fromString("15641ebe-cb07-3c45-ac3d-90c8fb0894ad"));
    private final ConceptProxy rhodamineAuramineProxy = new ConceptProxy("Rhodamine-auramine fluorochrome stain method (procedure)",UUID.fromString("742ce6a3-97c2-3968-8497-89ca87f143e1"));
    private final ConceptProxy romanowskyProxy = new ConceptProxy("Romanowsky stain method (procedure)",UUID.fromString("38462f58-43b5-3ea9-b1a5-7d9ef6f4532a"));
    private final ConceptProxy rosenbergProxy = new ConceptProxy("Rosenberg self-esteem scale (assessment scale)",UUID.fromString("9e1fd687-ebb7-381c-90ed-3d5a7bb9a1ef"));
    private final ConceptProxy sbtProxy = new ConceptProxy("Serum cidal test",UUID.fromString("2ea9cdc6-b284-3013-9e92-f00ae0f883a0"));
    private final ConceptProxy safraninProxy = new ConceptProxy("Safranine O stain",UUID.fromString("19813816-66a5-3f01-bb46-066ca5b8b0de"));
    private final ConceptProxy salineProxy = new ConceptProxy("Sodium chloride solution (substance)",UUID.fromString("ad6c905a-8640-39bc-bc30-273d4cfe959b"));
    private final ConceptProxy schirmerProxy = new ConceptProxy("Schirmer's test (procedure)",UUID.fromString("6bfb7f91-1860-35d9-b41c-dc7413d2a159"));
    private final ConceptProxy schmorlProxy = new ConceptProxy("Schmorl stain method (procedure)",UUID.fromString("08896cc6-afed-3464-a406-5b45b80ced64"));
    private final ConceptProxy schoberProxy = new ConceptProxy("Schober's test",UUID.fromString("9c40c846-3661-3087-9970-68e124abef62"));
    private final ConceptProxy sequencingProxy = new ConceptProxy("Nucleic acid sequencing (procedure)",UUID.fromString("28031315-2764-36ae-896d-033a04c25ddd"));
    private final ConceptProxy sevierMungerProxy = new ConceptProxy("Sevier-Munger stain method (procedure)",UUID.fromString("a36ec319-f341-3af5-a71f-e5c320f28774"));
    private final ConceptProxy silverImpregnationProxy = new ConceptProxy("Dieterle's stain",UUID.fromString("636e4cd3-08ae-334d-b9aa-a8bf3e568ba9"));
    private final ConceptProxy silverProxy = new ConceptProxy("Silver stain method (procedure)",UUID.fromString("223baa52-cbb8-3247-beb5-1ec499715a90"));
    private final ConceptProxy silveFontanarProxy = new ConceptProxy("Fontana Masson silver stain method (procedure)",UUID.fromString("84ec165e-b9fa-3147-8dc6-d5e12d8ac0bf"));
    private final ConceptProxy silverGrimeliusProxy = new ConceptProxy("Grimelius stain",UUID.fromString("5c153870-37b2-37f3-ba9c-2d57df3ef1c0"));
    private final ConceptProxy slitProxy = new ConceptProxy("Slit lamp examination",UUID.fromString("97d0539c-6c9d-342c-a3f0-9caa98c9db98"));
    private final ConceptProxy snellenProxy = new ConceptProxy("Snellen eye chart",UUID.fromString("9d9ae0bb-fcb1-346b-9bcb-275f468ba52e"));
    private final ConceptProxy solubilityProxy = new ConceptProxy("Solubility test technique (qualifier value)",UUID.fromString("c9d2b0de-8c28-3020-a642-e9e3292640cb"));
    private final ConceptProxy spectrophotometryProxy = new ConceptProxy("Spectrophotometric measurement (procedure)",UUID.fromString("41b33120-fddf-33a5-a0b2-3cb8eb0fa4a0"));
    private final ConceptProxy speechProxy = new ConceptProxy("Speech therapy (regime/therapy)",UUID.fromString("7d6191fe-21de-3b28-8212-fe7b0ab97433"));
    private final ConceptProxy spirometryProxy = new ConceptProxy("Spirometry (procedure)",UUID.fromString("df6abd6d-f0de-3bd1-83b3-46a8742e65af"));
    private final ConceptProxy sterileProxy = new ConceptProxy("Sterile body fluid culture (procedure)",UUID.fromString("70feb116-945f-3dde-8d25-3744220f46ca"));
    private final ConceptProxy subjectiveProxy = new ConceptProxy("Subjective refraction (procedure)",UUID.fromString("bb3dfcb1-315d-3bef-b683-c187df577caf"));
    private final ConceptProxy sudan3Proxy = new ConceptProxy("Sudan III stain method (procedure)",UUID.fromString("4c1c6f8a-16fe-31b7-9bf7-d1b2c6723439"));
    private final ConceptProxy sudan4Proxy = new ConceptProxy("Sudan IV stain method (procedure)",UUID.fromString("8d7ac826-6d7f-3d1c-b46e-2c6026bb83e8"));
    private final ConceptProxy sudanBlackBProxy = new ConceptProxy("Sudan black B stain method (procedure)",UUID.fromString("a6e6c356-65c6-3aca-abad-4902a5db8c7a"));
    private final ConceptProxy sudanBlackProxy = new ConceptProxy("Sudan black stain method (procedure)",UUID.fromString("85c9150c-7368-3a6b-a194-fcd43d33aebf"));
    private final ConceptProxy supravitalProxy = new ConceptProxy("Supravital staining method",UUID.fromString("1f19df2e-32d6-370f-878c-b4ded531503b"));
    private final ConceptProxy surgeryProxy = new ConceptProxy("Surgical procedures",UUID.fromString("85923350-9ead-323f-b2d9-8ade0ac4264c"));
    private final ConceptProxy surgeryHandProxy = new ConceptProxy("Operative procedure on hand (procedure)",UUID.fromString("9b9ffb80-090a-32c4-b174-0304f7aa8207"));
    private final ConceptProxy surgicalProxy = new ConceptProxy("Surgical oncology (qualifier value)",UUID.fromString("001dc03e-e670-37ae-907b-90e97da7fdab"));
    private final ConceptProxy tapeProxy = new ConceptProxy("Measuring tape, device (physical object)",UUID.fromString("6eab5002-eac7-3be9-8875-87eae198c080"));
    private final ConceptProxy tartrateResistantProxy = new ConceptProxy("Tartrate resistant acid phosphatase stain method, blood or bone marrow (procedure)",UUID.fromString("db366cb2-a656-3bb8-a78d-30919da6b1d8"));
    private final ConceptProxy terminalProxy = new ConceptProxy("Terminal transferase stain",UUID.fromString("d0cf3b3f-b603-3039-a865-8fe4edf58f3b"));
    private final ConceptProxy testProxy = new ConceptProxy("Test strip technique (qualifier value)",UUID.fromString("0dabeb0a-4bce-3090-8deb-408b6fc889ad"));
    private final ConceptProxy thioflavineSProxy = new ConceptProxy("Thioflavine-S stain",UUID.fromString("16b01807-c316-385d-b2fb-beb241d0fa81"));
    private final ConceptProxy thoracicProxy = new ConceptProxy("Surgical procedure on thorax (procedure)",UUID.fromString("031bf635-945c-392a-8dc7-17694715fc88"));
    private final ConceptProxy thromboelastographyProxy = new ConceptProxy("Thromboelastography (procedure)",UUID.fromString("05fdb508-116f-3319-a013-9efad04455e3"));
    private final ConceptProxy toluidineProxy = new ConceptProxy("Toluidine blue stain method (procedure)",UUID.fromString("6f8881d2-6601-335f-b09d-9ff51d1f088b"));
    private final ConceptProxy tonometryProxy = new ConceptProxy("Tonometry (procedure)",UUID.fromString("4a9a23be-fb5d-3f87-9f61-8ffca8d068f5"));
    private final ConceptProxy tonometryNonProxy = new ConceptProxy("Noncontact tonometry",UUID.fromString("e8dd187a-f001-3e1a-b083-a0d227982300"));
    private final ConceptProxy transcutaneousCO2Proxy = new ConceptProxy("Transcutaneous carbon dioxide monitor method (qualifier value)",UUID.fromString("cecbfa3c-a7d0-33c6-b28c-f67d6b0fe133"));
    private final ConceptProxy transcutaneousO2Proxy = new ConceptProxy("Transcutaneous oxygen monitor method (qualifier value)",UUID.fromString("65a4d583-71a8-386b-bc06-8c7fb8c9d957"));
    private final ConceptProxy traumaProxy = new ConceptProxy("Trauma",UUID.fromString("5ab41bfb-b8ad-3279-9376-cc204e6de03b"));
    private final ConceptProxy trichromeProxy = new ConceptProxy("Ryan-blue stain",UUID.fromString("69c0d0aa-2d85-346a-9a30-ab5bb4221372"));
    private final ConceptProxy trichromeStainProxy = new ConceptProxy("Trichrome staining",UUID.fromString("47664e7c-8985-3593-bf93-6bb70bab3809"));
    private final ConceptProxy trichromeGomoroProxy = new ConceptProxy("Modified Gomori-Wheatley trichrome stain method (procedure)",UUID.fromString("d89c651d-6883-3ef1-b6eb-fc8c68960603"));
    private final ConceptProxy trichromeMassonProxy = new ConceptProxy("Masson trichrome stain method (procedure)",UUID.fromString("fd6b94dc-4de6-36a9-885b-0e17a352210e"));
    private final ConceptProxy trichromeMassonModProxy = new ConceptProxy("Modified Masson trichrome stain method (procedure)",UUID.fromString("0efa7b50-201e-32ab-b3a1-e3f6391de391"));
    private final ConceptProxy tzanckProxy = new ConceptProxy("Tzanck smear method (procedure)",UUID.fromString("8ea6b46e-ce9e-363d-829e-150c4e72bc1e"));


    //ConceptProxy Set 4
    private final ConceptProxy uroflowmetryProxy = new ConceptProxy("Voiding flow rate test",UUID.fromString("c008b44b-c596-38f2-81e5-57214faf0b71"));
    private final ConceptProxy urologyProxy = new ConceptProxy("Urology (qualifier value)",UUID.fromString("35816de1-9f0e-3d78-a260-78904e5d7e5a"));
    private final ConceptProxy vdrlProxy = new ConceptProxy("Venereal Disease Research Laboratory test (procedure)",UUID.fromString("f6c1e5e8-d09d-3b30-8d6e-2c4107d91fec"));
    private final ConceptProxy vepProxy = new ConceptProxy("Visual evoked responses",UUID.fromString("1b3c427f-999e-3f9e-888e-9f8fc08a2341"));
    private final ConceptProxy vanProxy = new ConceptProxy("Van Gieson stain (substance)",UUID.fromString("80c309a7-51c6-37b3-bad2-8934ae2bea15"));
    private final ConceptProxy vascularProxy = new ConceptProxy("Vascular surgery procedure (procedure)",UUID.fromString("1b3d5c2b-1514-38e6-8897-5bfd6de259e6"));
    private final ConceptProxy vassarCullingProxy = new ConceptProxy("Vassar-Culling stain method (procedure)",UUID.fromString("434be47a-5962-3727-9e63-10c13f586e7a"));
    private final ConceptProxy verhoeffVanProxy = new ConceptProxy("Verhoeff-Van Gieson stain method (procedure)",UUID.fromString("687a872c-7fef-3427-980f-d7051857529a"));
    private final ConceptProxy viralProxy = new ConceptProxy("Viral subtyping (procedure)",UUID.fromString("a960a5b8-fe57-387f-a7a8-9ade53679147"));
    private final ConceptProxy visualProxy = new ConceptProxy("Visual (qualifier value)",UUID.fromString("d62b46c7-b560-3349-bd05-11059b9c657f"));
    private final ConceptProxy vocationalProxy = new ConceptProxy("Vocational rehabilitation (regime/therapy)",UUID.fromString("15343e12-f30a-3185-83be-2148fefbf027"));
    private final ConceptProxy vonProxy = new ConceptProxy("von Kossa stain method (procedure)",UUID.fromString("fbdc4918-ae7c-30c5-945b-fa8814aa4e1a"));
    private final ConceptProxy wadeProxy = new ConceptProxy("Wade-Fite stain method (procedure)",UUID.fromString("bf014272-5030-3dd8-afca-b6212d5cda0c"));
    private final ConceptProxy warthinStarryProxy = new ConceptProxy("Warthin-Starry staining",UUID.fromString("e27c758a-6fc1-3f02-a685-7d13f3c7597f"));
    private final ConceptProxy waysonProxy = new ConceptProxy("Wayson stain (substance)",UUID.fromString("07dc6c82-4751-36d6-88a3-9fd454d8ed51"));
    private final ConceptProxy woundProxy = new ConceptProxy("Wound care management (procedure)",UUID.fromString("7041315b-10a6-39eb-a298-03a8d8dfb74e"));
    private final ConceptProxy wrightGiemsaProxy = new ConceptProxy("Wright-Giemsa stain technique (qualifier value)",UUID.fromString("f6630e3f-115a-3c2c-80f3-a8fc8101756a"));
    private final ConceptProxy wrightProxy = new ConceptProxy("Wright stain method (procedure)",UUID.fromString("51673115-8f82-37db-b3c4-25c3095f3169"));
    private final ConceptProxy xrProxy = new ConceptProxy("X-ray electromagnetic radiation (physical force)",UUID.fromString("d0899fdf-02f6-34cc-8d68-19718bd0507b"));





    public Assertion[] make(LogicalExpressionBuilder builder, String[] loincRecord) {
        List<Assertion> assertions = new ArrayList<>();

        if (loincRecord[LoincWriter.METHOD_TYP] != null & !loincRecord[LoincWriter.METHOD_TYP].isEmpty()) {
            addLoincMethod(builder, loincRecord[LoincWriter.METHOD_TYP], assertions);
        }

        if (!assertions.isEmpty()) {
            assertions.add(builder.conceptAssertion(MetaData.PHENOMENON____SOLOR));
        }
        return assertions.toArray(new Assertion[assertions.size()]);
    }

    private void addLoincMethod(LogicalExpressionBuilder builder, String loincField, List<Assertion> assertions) {
        methods.add(loincField);
        switch (loincField) {
            case "US":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "EKG":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ekgProxy)))));
                break;
            case "MR":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(mrProxy)))));
                break;
            case "AUDIT":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(auditProxy)))));
                break;
            case "18 deg C incubation":
                break;
            case "US.estimated from Arbuckle 1993 female twins":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "XR.stereoscopic":
                break;
            case "Cardiovascular disease.physician assistant":
                break;
            case "Perm mount":
                break;
            case "Internal medicine.physician intern":
                break;
            case "US.2D+Calculated by area-length method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Calculated.HWL":
                break;
            case "High sensitivity":
                break;
            case "KCCQ-12":
                break;
            case "Based on general population risk":
                break;
            case "NM.SPECT":
                break;
            case "US.M-mode+ECG":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Detection limit <= 1.0 mg/L":
                break;
            case "Concentration.McMaster":
                break;
            case "Hansel stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(hanselProxy)))));
                break;
            case "BPI.short":
                break;
            case "Trichrome modified":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(trichromeProxy)))));
                break;
            case "BCG":
                break;
            case "US.2D.mod.biplane.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "*":
                break;
            case "US.2D.A2C+Calc by single plane method of disks":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Cardiovascular disease.physician fellow":
                break;
            case "BCP":
                break;
            case "ACE.BRFSS":
                break;
            case "Rodent substrate":
                break;
            case "Oil red O stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(oilProxy)))));
                break;
            case "US.measured.ellipse overlay":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Surgical critical care":
                break;
            case "Speech therapy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(speechProxy)))));
                break;
            case "Electrophoresis.agarose gel":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(electrophoresisAgaroseProxy)))));
                break;
            case "Aggl.rivanol":
                break;
            case "Saline":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(salineProxy)))));
                break;
            case "US.2D.mitral valve.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "HCL-32":
                break;
            case "Carolina Breast Cancer Study":
                break;
            case "US.M-mode":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Calculated.VectraDA":
                break;
            case "Categorization by comparison with standards":
                break;
            case "Radiology.technician":
                break;
            case "HPLC":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(hplcProxy)))));
                break;
            case "Probe.amp.tar detection limit = 1.7 log copies/mL":
                break;
            case "US.doppler.Vmax+LVOT.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Physical therapy.nurse":
                break;
            case "US.estimated from BPC":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Nephrology.physician resident":
                break;
            case "US+Estimated from TC.Nimrod 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Microscopy.electron":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(microscopyElectronProxy)))));
                break;
            case "US.3D.segmentation":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Cresyl echt violet stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(cresylProxy)))));
                break;
            case "Palliative care.team":
                break;
            case "Guthrie test":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(guthrieProxy)))));
                break;
            case "BFI":
                break;
            case "Nutrition and dietetics.interdisciplinary":
                break;
            case "NAACCR":
                break;
            case "Sudan III stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(sudan3Proxy)))));
                break;
            case "AmNART":
                break;
            case "NHANES III":
                break;
            case "Alkali denat":
                break;
            case "CT.densitometry":
                break;
            case "Physical medicine and rehab":
                break;
            case "US.doppler+Calculated by volumetric method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "3D RT incubation":
                break;
            case "Manometry":
                break;
            case "Internal medicine.physician resident":
                break;
            case "US.doppler.VTI+Aorta.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from AC.ASUM 2000":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "General medicine.physician attending":
                break;
            case "Estimated by palpation":
                break;
            case "Pastoral care.team":
                break;
            case "Pathology":
                break;
            case "Surgery.physician intern":
                break;
            case "Basic fuchsin stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(basicProxy)))));
                break;
            case "Bennhold stain.Putchler modified":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(bennholdProxy)))));
                break;
            case "US.derived.LWT":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.doppler+Calculated by continuity Vmax":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Dialysis":
                break;
            case "BSDS":
                break;
            case "Phage lysis":
                break;
            case "Automated count":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(automatedCountProxy)))));
                break;
            case "Diagnostic imaging":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(diagnosticProxy)))));
                break;
            case "Carbol-fuchsin stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(carbolFuchsinProxy)))));
                break;
            case "Vascular surgery.physician attending":
                break;
            case "Alcian blue stain.sulfated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(alcianSulfatedProxy)))));
                break;
            case "Ivy":
                break;
            case "US+Estimated from AC.Lessoway 1998":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Oral and maxillofacial surgery":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(oralProxy)))));
                break;
            case "BIA":
                break;
            case "Neuropsychology":
                break;
            case "SAMHSA confirm":
                break;
            case "US+Estimated from Campbell 1977":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Culture.standard plate count":
                break;
            case "PROMIS.PEDS":
                break;
            case "Capillary electrophoresis":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(capillaryProxy)))));
                break;
            case "Estimated from EDD":
                break;
            case "Mixed antiglobulin reaction":
                break;
            case "US.2D+Calculated by cube method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Surgery.physician attending":
                break;
            case "De Galantha stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(deProxy)))));
                break;
            case "HHS.ACA Section 4302":
                break;
            case "US+Estimated from AC&BPD.Hadlock 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "OCT":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(octProxy)))));
                break;
            case "ACC-AHA":
                break;
            case "Primary care.physician resident":
                break;
            case "Gimenez stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(gimenezGimenezProxy)))));
                break;
            case "Inner City Asthma Survey":
                break;
            case "US.doppler.VTI+LVOT.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Multi-specialty program":
                break;
            case "Radiology.physician attending":
                break;
            case "Otolaryngology.physician resident":
                break;
            case "Diabetology":
                break;
            case "{Imaging modality}":
                break;
            case "Microscopy.light":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(microscopyLightProxy)))));
                break;
            case "Electrical wave form":
                break;
            case "US.estimated from ulna length.Merz 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Community health care":
                break;
            case "Biopsy culture":
                break;
            case "US+Estimated from Campbell 1991":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Addiction medicine.physician attending":
                break;
            case "Peritoneoscopy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(peritoneoscopyProxy)))));
                break;
            case "Thoracic and cardiac surgery.physician fellow":
                break;
            case "Confirm>200 ng/mL":
                break;
            case "Probe.amp.tar detection limit = 5 IU/mL":
                break;
            case "Probe insertion.Pratt":
                break;
            case "VDRL":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(vdrlProxy)))));
                break;
            case "Clinical biochemical genetics":
                break;
            case "C3b binding assay":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(c3bProxy)))));
                break;
            case "Pediatric transplant hepatology":
                break;
            case "Neurology.nurse":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(neurologyNurseProxy)))));
                break;
            case "US.2D.PLAX":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Antihuman globulin":
                break;
            case "Pediatric otolaryngology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pediatricOtolaryngologyProxy)))));
                break;
            case "US.estimated from clavicle length.Yarkoni 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Diabetology.nurse":
                break;
            case "Mercury column":
                break;
            case "Anaerobic+Aerobic culture":
                break;
            case "Aggl.ring test":
                break;
            case "Clinical genetics":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(clinicalGeneticsProxy)))));
                break;
            case "India ink preparation":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(indiaProxy)))));
                break;
            case "US+Estimated from IOD.Mayden 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Perimetry.Tuebinger automatic":
                break;
            case "Microscopic count":
                break;
            case "Best estimate":
                break;
            case "Orthopaedic surgery.physician resident":
                break;
            case "Critical care medicine.medical student":
                break;
            case "US.estimated from Alexander 1996":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Physical therapy.interdisciplinary":
                break;
            case "Hematology+Medical oncology.physician resident":
                break;
            case "Highman stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(highmanProxy)))));
                break;
            case "Neisser stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(neisserProxy)))));
                break;
            case "Primary care":
                break;
            case "US.2D.LVOT area.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Vascular surgery.technician":
                break;
            case "SCARED-R":
                break;
            case "MLHFQ":
                break;
            case "Primary care.physician attending":
                break;
            case "PCAM":
                break;
            case "Medical genetics.nurse":
                break;
            case "Dermatology.physician resident":
                break;
            case "Electrooculogram":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(electrooculogramProxy)))));
                break;
            case "US.M-mode+doppler.A4C":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.estimated from Merz 1988":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Physical medicine and rehab.interdisciplinary":
                break;
            case "US.2D+Measured by planimetry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Silver stain.Fontana-Masson":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(silveFontanarProxy)))));
                break;
            case "Gradient strip":
                break;
            case "US+Estimated from AC&FL&HC.Ott 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Coag":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(coagProxy)))));
                break;
            case "Primary care.interdisciplinary":
                break;
            case "Colon and rectal surgery":
                break;
            case "Randot":
                break;
            case "With P-5'-P":
                break;
            case "Bleach stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(bleachProxy)))));
                break;
            case "Detection limit <= 20 mg/L test strip":
                break;
            case "MG.tomosynthesis":
                break;
            case "PASII":
                break;
            case "PROMIS":
                break;
            case "Reported.ACTG":
                break;
            case "Colon and rectal surgery.nurse practitioner":
                break;
            case "Worth test":
                break;
            case "No addition of P-5'-P":
                break;
            case "Pulmonary function":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pulmonaryFunctionProxy)))));
                break;
            case "Cytology.non-gyn":
                break;
            case "AQ Adolescent":
                break;
            case "Multidisk":
                break;
            case "Mini-Cog":
                break;
            case "Skin fold":
                break;
            case "Calculated.FibroMeter":
                break;
            case "JDS/JSCC":
                break;
            case "CNAP":
                break;
            case "Myeloperoxidase stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(myeloperoxidaseProxy)))));
                break;
            case "Romanowsky stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(romanowskyProxy)))));
                break;
            case "US.2D+Calculated by modified Simpson method":
                break;
            case "Auto":
                break;
            case "US.2D+Calculated by biplane method of disks":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Senior review":
                break;
            case "IB.test strip":
                break;
            case "Framingham":
                break;
            case "UPDRS":
                break;
            case "Maternal and fetal medicine":
                break;
            case "Cyto stain.thin prep":
                break;
            case "CT && CT.angio":
                break;
            case "SSA":
                break;
            case "Pharmacology":
                break;
            case "Preventive medicine":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(preventiveProxy)))));
                break;
            case "Screen>200 ng/mL":
                break;
            case "Microscopy.light.HPF":
                break;
            case 
                "Social work.case manager":
                break;
            case "RESt absorption":
                break;
            case "Creatine uptake":
                break;
            case "Orthopaedic surgery.nurse":
                break;
            case "Sleep medicine":
                break;
            case "Reported.living-HIV":
                break;
            case "Nephrology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(nephrologyProxy)))));
                break;
            case "Derived":
                break;
            case "US+Estimated from AC&HC":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "1W cold incubation":
                break;
            case "Reported.visual analog score":
                break;
            case "OASIS-C":
                break;
            case "NVSS":
                break;
            case "1W 37 deg C incubation":
                break;
            case "Physical medicine and rehab.nurse":
                break;
            case "Culture.FDA method":
                break;
            case "LTVH":
                break;
            case "SDAI":
                break;
            case "US.2D.tricuspid valve":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "1-14C-glutamate substrate":
                break;
            case "Hoechst stain":
                break;
            case "C1q binding assay":
                break;
            case "Social work.interdisciplinary":
                break;
            case "Direct spectrophotometry":
                break;
            case "US+Estimated from HC derived.Chitty 1997":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Warm absorption":
                break;
            case "Heat denaturation":
                break;
            case "Continuous":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(continuousProxy)))));
                break;
            case "Lee White":
                break;
            case "Oncology":
                break;
            case "estimated from serum level":
                break;
            case "RIPA":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ripaProxy)))));
                break;
            case "CRAFFT":
                break;
            case "Hepatology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(hepatologyProxy)))));
                break;
            case "Estimated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(estimateProxy)))));
                break;
            case "EPDS":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(epdsProxy)))));
                break;
            case "US.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Potassium ferrocyanide stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(potassiumProxy)))));
                break;
            case "2D cold incubation":
                break;
            case "Probe.amp detection limit = 75 copies/mL":
                break;
            case "Thoracic and cardiac surgery.nurse":
                break;
            case "US+Estimated from BPD.Tokyo 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Kessler 6 Distress":
                break;
            case "VectraDA":
                break;
            case "Alcian blue stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(alcianProxy)))));
                break;
            case "Coag.kaolin induced":
                break;
            case "Rapid stain":
                break;
            case "Screen>1000 ng/mL":
                break;
            case "Rapid response team":
                break;
            case "Pulmonary disease":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pulmonaryProxy)))));
                break;
            case "US+Estimated from BPD.Osaka 1989":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Silver stain.Grimelius":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(silverGrimeliusProxy)))));
                break;
            case "Cardiovascular disease.physician attending":
                break;
            case "Ophthalmology.physician resident":
                break;
            case "US+Estimated from BPD":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Endocrinology.nurse practitioner":
                break;
            case "PROCAM.HealthCheck.Cullen 1997":
                break;
            case "Immersion":
                break;
            case "Warm incubation":
                break;
            case "US+Estimated from BPD.Doubilet 1993":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Blood banking and transfusion medicine":
                break;
            case "Single breath.carbon monoxide+Helium":
                break;
            case "PROCAM.QuickCheck.Cullen 1997":
                break;
            case "Method for Slow-growing mycobacteria":
                break;
            case "US standard report of fetal death":
                break;
            case "Reported":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(reportedProxy)))));
                break;
            case "Pediatric gastroenterology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pediatricGastroProxy)))));
                break;
            case "4 deg C incubation":
                break;
            case 
                "Mental health.case manager":
                break;
            case "Tzanck smear":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(tzanckProxy)))));
                break;
            case "Otolaryngology.physician attending":
                break;
            case "US+Estimated from AD&BPD":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Infectious disease.physician fellow":
                break;
            case "CT spiral":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ctSpiralProxy)))));
                break;
            case "BOMC":
                break;
            case "Pulmonary disease.physician attending":
                break;
            case "APTA":
                break;
            case "US+Estimated from tibia length.Merz 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "FaB":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(fabProxy)))));
                break;
            case "4D cold incubation":
                break;
            case "US.doppler.velocity+Area.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Reynolds.Ridker 2007":
                break;
            case "Gas dilution.rebreathing":
                break;
            case 
                "Case manager":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(caseManagerProxy)))));
                break;
            case "Aerobic culture":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(aerobicProxy)))));
                break;
            case "OSQ":
                break;
            case "Immunoperoxidase stain":
                break;
            case "Spinal cord injury medicine.physician assistant":
                break;
            case "US.2D.mod.single-plane.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Hall's stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(hallsProxy)))));
                break;
            case "Womens health":
                break;
            case "US+Estimated from AC&FL":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Pediatric infectious diseases":
                break;
            case "MESA Air Pollution":
                break;
            case "Clinical.estimated from prior assessment":
                break;
            case "US.estimated from fibula length.Jeanty 1983":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Shell vial culture":
                break;
            case "US.estimated from fibula length.Jeanty 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Aggl.ring test.heat inact":
                break;
            case "Probe":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(probeProxy)))));
                break;
            case "Computed":
                break;
            case "CARE":
                break;
            case "Child and adolescent psychology":
                break;
            case "3D 37 deg C incubation":
                break;
            case "Hematology+Medical oncology":
                break;
            case "Perimetry.Peristat":
                break;
            case "US.continuity.VTI+Diameter":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Immunoelectrophoresis":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(immunoelectrophoresisProxy)))));
                break;
            case "Blood banking and transfusion medicine.nurse":
                break;
            case "CAST":
                break;
            case "Aggl.rapid":
                break;
            case "US.continuity.Vmax+Diameter":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Probe.amp.tar detection limit = 20 copies/mL":
                break;
            case "Surgery.nurse":
                break;
            case "PhenX":
                break;
            case "GEL":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(gelProxy)))));
                break;
            case "Calcofluor white preparation":
                break;
            case "Oral and maxillofacial surgery.physician resident":
                break;
            case "Anesthesiology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(anesthesiologyProxy)))));
                break;
            case "Verhoeff-Van Gieson stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(verhoeffVanProxy)))));
                break;
            case "US.derived.HWL":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Measured":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(measuredProxy)))));
                break;
            case "COOP.WONCA":
                break;
            case "US+Estimated from BPD.Campbell 1975":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Otolaryngology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(otolaryngologyProxy)))));
                break;
            case "Palliative care.physician":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(palliativePhysicianProxy)))));
                break;
            case "Womens health.nurse":
                break;
            case "XXX stain":
                break;
            case "PLCO":
                break;
            case "Urology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(urologyProxy)))));
                break;
            case "US.derived from OFD&O-O TD":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Based on maternal age":
                break;
            case "Mallory-Heidenhain stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(malloryHeidenhainProxy)))));
                break;
            case "Angiography.single plane":
                break;
            case "Supravital stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(supravitalProxy)))));
                break;
            case "Reported.Wong-Baker FACES pain rating scale":
                break;
            case "Estimated.pop birth wgt gestational age corr.ref":
                break;
            case "Pediatric nephrology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pediatricNephrologyProxy)))));
                break;
            case "Acoustic measurement":
                break;
            case "US+Estimated from FL.Shinozuka 1996":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Infectious disease.physician resident":
                break;
            case "Concentration":
                break;
            case "US.estimated from Hansmann 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Transcutaneous meter":
                break;
            case "US.traced":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Neonatal perinatal medicine":
                break;
            case "ACSCP Study II":
                break;
            case "Trichrome stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(trichromeStainProxy)))));
                break;
            case "Psychiatry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(psychiatryProxy)))));
                break;
            case "US.estimated from EFW1.Shinozuka 1996":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "SSAGA II":
                break;
            case "Allergy and immunology.nurse":
                break;
            case "Dentistry.nurse":
                break;
            case "Rheumatology.physician attending":
                break;
            case "P.E.D.S.-DM":
                break;
            case "Observed.CAM.MDSv3":
                break;
            case "FVIII dosing":
                break;
            case "Arthroscopy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(arthroscopyProxy)))));
                break;
            case "US+Estimated from HC.Hansmann 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Hematoxylin-eosin-Mayers progressive stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(hematoxylinEosinMayersProxy)))));
                break;
            case "US.doppler.VTI+Diameter.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from HC.Hansmann 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Neurology.team":
                break;
            case "Screen>500 ng/mL":
                break;
            case "AHIC":
                break;
            case "Dermatology.physician attending":
                break;
            case "US+Estimated from AD&BPD&FL":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Thioflavine-S stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(thioflavineSProxy)))));
                break;
            case "Lactophenol blue":
                break;
            case "LC/MS/MS":
                break;
            case "US.2D.A4C+Calc by single plane method of disks":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Mucicarmine stain.Mayer":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(mucicarmineMayerProxy)))));
                break;
            case "IPAQ":
                break;
            case "Surgery.interdisciplinary":
                break;
            case "Acetate esterase stain":
                break;
            case "3D cold incubation":
                break;
            case "Multi-specialty program.nurse":
                break;
            case "US.2D+doppler":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Cytology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(cytologyProxy)))));
                break;
            case "Electrophysiology":
                break;
            case "Pathology.nurse":
                break;
            case "US.estimated from radius length.Jeanty 1983":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Dermatology.nurse":
                break;
            case "US+Estimated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Autorefractor.man":
                break;
            case "Occupational therapy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(occupationalInerdisciplinaryProxy)))));
                break;
            case "Detection limit <= 3.47 pmol/L":
                break;
            case "Surgical pathology":
                break;
            case "GPCOG":
                break;
            case 
                "Physical medicine and rehab.case manager":
                break;
            case "Apgar":
                break;
            case "Estimated from patient reported EDC":
                break;
            case "Reese-Ellsworth system":
                break;
            case "Alizarin red S stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(alizarinProxy)))));
                break;
            case "US+Estimated from GSD.Tokyo 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Hepatology.nurse":
                break;
            case "US.doppler.Vmax+Aorta.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
               break;
            case "Dark field examination":
                break;
            case "Schmorl stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(schmorlProxy)))));
                break;
            case "Viability count":
                break;
            case "2,3-14C-succinate substrate":
                break;
            case "Ophthalmology.nurse":
                break;
            case "Probe.amp.tar detection limit = 50 copies/mL":
                break;
            case "Probe.amp detection limit = 1.9 log copies/mL":
                break;
            case "Spectrophotometry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(spectrophotometryProxy)))));
                break;
            case "Miller classification":
                break;
            case "Ophthalmology.physician fellow":
                break;
            case "Addiction medicine.nurse":
                break;
            case "Addiction psychiatry":
                break;
            case "CDC":
                break;
            case "Physician attending":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(physicianAttendingProxy)))));
                break;
            case "CDI":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(cdiProxy)))));
                break;
            case "Reported.FPS-R":
                break;
            case "CDR":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(cdrProxy)))));
                break;
            case "Chemical pathology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(chemicalProxy)))));
                break;
            case "Finger following":
                break;
            case "Chloracetate esterase stain":
                break;
            case "Pulmonary disease.medical student":
                break;
            case "Public health":
                break;
            case "US+Estimated from TCD.Hill 1990":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from GSD.Hellman 1969":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Calculated.SPINA":
                break;
            case "Pediatric endocrinology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pediatricEndocrinologyProxy)))));
                break;
            case "Plastic surgery":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(plasticProxy)))));
                break;
            case "Methenamine silver stain.Jones":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(methenamineJonesProxy)))));
                break;
            case "US.doppler+Calculated by continuity VTI":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Dilution":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(dilutionProxy)))));
                break;
            case "California Teachers Study":
                break;
            case "5M RT incubation":
                break;
            case "NeuroQol":
                break;
            case "Infectious disease.physician attending":
                break;
            case "MR.functional":
                break;
            case "Colon and rectal surgery.physician resident":
                break;
            case "M'Fadyean stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(mfadyeanProxy)))));
                break;
            case "Podiatry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(podiatryProxy)))));
                break;
            case "Comp fix":
                break;
            case "Spun Westergren tube":
                break;
            case "US.derived.planimetry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Malaria thin smear":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(malariaThinProxy)))));
                break;
            case "Low resolution":
                break;
            case "SCORE.Quick.Conroy 2003":
                break;
            case "NSRAS":
                break;
            case "Dialysis.nurse":
                break;
            case "Physical medicine and rehab.physician attending":
                break;
            case "US+Estimated from BPD.ASUM 1989":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "PAM":
                break;
            case "Rheumatology.physician fellow":
                break;
            case "PAS":
                break;
            case "Rhodamine-auramine fluorochrome stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(rhodamineAuramineProxy)))));
                break;
            case "Perimetry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(perimetryProxy)))));
                break;
            case "Thin film":
                break;
            case "1D cold incubation":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(coldProxy)))));
                break;
            case "US+Estimated from HC.Jeanty 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Probe.amp.tar detection limit = 2.6 log copies/mL":
                break;
            case "Test strip":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(testProxy)))));
                break;
            case "Thick film":
                break;
            case "Cardiovascular disease.medical student":
                break;
            case "US+Estimated from HC.Jeanty 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "CIE":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(cieProxy)))));
                break;
            case "Screen>100 ng/mL":
                break;
            case "Cardiovascular disease+Pulmonary disease":
                break;
            case "Cold absorption":
                break;
            case "NIH Toolbox":
                break;
            case "PCL-C":
                break;
            case "Vascular surgery.nurse":
                break;
            case "Bielschowsky stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(bielschowskyProxy)))));
                break;
            case "PEG Study":
                break;
            case "DEEDS":
                break;
            case "US.estimated from radius length":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "AHRQ":
                break;
            case "US.measured.real time two dimension":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Thoracic and cardiac surgery.physician attending":
                break;
            case "Addiction medicine":
                break;
            case "Methyl violet stain":
                break;
            case "MSCDM":
                break;
            case "Psychiatry.nurse":
                break;
            case "Detection limit <= 0.005 mIU/L":
                break;
            case "TLC":
                break;
            case "Esterase stain.combined":
                break;
            case "NCICTC":
                break;
            case "Undersea and hyperbaric medicine":
                break;
            case "Bazett formula":
                break;
            case "GSE":
                break;
            case "Hematology+Medical oncology.physician fellow":
                break;
            case "Endoscopy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(endoscopyProxy)))));
                break;
            case "Coag.derived":
                break;
            case "Gastroenterology.physician attending":
                break;
            case "Audiology":
                break;
            case "Physician":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(physicianProxy)))));
                break;
            case "ICRB":
                break;
            case "Bacterial subtyping":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(bacterialProxy)))));
                break;
            case "Nutrition and dietetics.nurse":
                break;
            case "US.2D.PSAX":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "PEG":
                break;
            case "US.2D+Calculated by truncated ellipsoid method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Estimated from ovulation date":
                break;
            case "US.2D+Calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "RAST":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(rastProxy)))));
                break;
            case "Carbon dioxide measurement":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(carbonProxy)))));
                break;
            case "Internal medicine.pharmacist":
                break;
            case "Nottingham":
                break;
            case "Pediatrics":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pediatricsProxy)))));
                break;
            case "US+Estimated from AC.Vintzileos 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "rPARQ":
                break;
            case "HITSP":
                break;
            case "Interdisciplinary":
                break;
            case "Psychiatry.nurse practitioner":
                break;
            case "Pain medicine.team":
                break;
            case "Alberts stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(albertsProxy)))));
                break;
            case "US+Estimated from OFD.ASUM 2000":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "NDNQI":
                break;
            case "Visual.Reinsch":
                break;
            case "Clinical pathology":
                break;
            case "Physician consulting":
                break;
            case "Calculated.Prosigna":
                break;
            case "Probe.amp.tar detection limit = 500 IU/mL":
                break;
            case "Guinea pig esophagus substrate":
                break;
            case "Monkey esophagus substrate":
                break;
            case "Pachymetry":
                break;
            case "Licensed practical nurse":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(licensedProxy)))));
                break;
            case "Optometry.technician":
                break;
            case "General medicine":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(generalProxy)))));
                break;
            case "Pinworm examination":
                break;
            case "Wintrobe":
                break;
            case "US.2D.tricuspid valve.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Methyl green-pyronine Y stain":
                break;
            case "Acid fast stain.Ziehl-Neelsen":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(acidZiehlProxy)))));
                break;
            case "Framingham.D'Agostino 1994":
                break;
            case "Flow cytometry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(flowProxy)))));
                break;
            case "MDSv3":
                break;
            case "Confirm":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(confirmProxy)))));
                break;
            case "Isotopic":
                break;
            case "Spinal cord injury medicine.physician fellow":
                break;
            case "EDDS":
                break;
            case "Ophthalmology.physician attending":
                break;
            case "Nephrology.nurse":
                break;
            case "Nystagmogram":
                break;
            case "Psychiatry.physician attending":
                break;
            case "Colonoscopy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(colonoscopyProxy)))));
                break;
            case "Screen>50 ng/mL":
                break;
            case "Bodian stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(bodianProxy)))));
                break;
            case "Aggl.micro":
                break;
            case "NIHSS":
                break;
            case "Mental health.physician attending":
                break;
            case "Perimetry.Perimat":
                break;
            case "US+Estimated from CRL.Osaka 1989":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from O-O BPD.Chitty 1997":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Social work.team":
                break;
            case "Helium single breath":
                break;
            case "Esterase stain.non-specific":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(esteraseProxy)))));
                break;
            case "Airway flow measurement":
                break;
            case "US.2D.mod.single-plane":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Calculated.ELF":
                break;
            case "Orthotics prosthetics":
                break;
            case "Plastic surgery.physician attending":
                break;
            case "22 deg C incubation":
                break;
            case "Observed.Norton scale":
                break;
            case "Coag.saline 1:1":
                break;
            case "Flotation":
                break;
            case "MIDUS II":
                break;
            case "Angiogram.visual estima":
                break;
            case "Observed.QAM":
                break;
            case "US.estimated from Goldstein 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "HAQII":
                break;
            case 
                "Geriatric medicine.case manager":
                break;
            case "Organism specific culture.NPIP method":
                break;
            case "US+Estimated from CRL.Tokyo 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Acid fast stain.Kinyoun modified":
                break;
            case "US+Estimated from BPD&TTD.Hansmann 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "{Role}":
                break;
            case "Electrophoresis":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(electrophoresisProxy)))));
                break;
            case "Colonoscopy.thru stoma":
                break;
            case "4H cold incubation":
                break;
            case "Prussian blue stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(potassiumProxy)))));
                break;
            case "CT.measured":
                break;
            case "Palliative care.pharmacist":
                break;
            case "Geriatric medicine.pharmacist":
                break;
            case "US+Estimated from GSD.Hansmann 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Obstetrics and gynecology.nurse practitioner":
                break;
            case "Silver nitrate stain":
                break;
            case "Framingham.D'Agostino":
                break;
            case "UPPS-P":
                break;
            case "Plastic surgery.physician resident":
                break;
            case "US+Estimated from Hadlock 1991":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Acupuncture":
                break;
            case "CAP cancer protocols":
                break;
            case "Acupuncture.nurse":
                break;
            case "US.2D.PSAX+Measured by planimetry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Nutrition and dietetics":
                break;
            case "US.estimated from APAD.Lessoway 1998":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from CRL.Hadlock 1992":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "HIV-ART":
                break;
            case "US+Estimated from AC&FL&HC.Weiner 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Aramany classification":
                break;
            case "Maddox wing":
                break;
            case "Morse Fall Scale":
                break;
            case "AUDIT-C":
                break;
            case 
                "Addiction medicine.case manager":
                break;
            case "Solubility test":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(solubilityProxy)))));
                break;
            case "PT+CT":
                break;
            case "Immune stain":
                break;
            case "Probe.amp.tar detection limit = 500 copies/mL":
                break;
            case "NLAAS":
                break;
            case "CES-DC":
                break;
            case "US.doppler":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Tonometry.Goldmann appl":
                break;
            case "US+Estimated from HL.Jeanty 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Screen>2000 ng/mL":
                break;
            case "IPSS":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ipssProxy)))));
                break;
            case "MR.spectroscopy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(mrSpectroscopyProxy)))));
                break;
            case "Family medicine":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(familyProxy)))));
                break;
            case "KOOSJR":
                break;
            case "NHEXAS":
                break;
            case "Viability count.FDA method":
                break;
            case "US.estimated from Shinozuka 1996":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Intravascular line culture":
                break;
            case "Microscopy.electron.thin section":
                break;
            case "Silver impregnation stain.Dieterle":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(silverImpregnationProxy)))));
                break;
            case "US.doppler+ECG":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Pastoral care":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pastoralProxy)))));
                break;
            case "RF.video":
                break;
            case "Estimated from abdominal circumference":
                break;
            case "Gram stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(gramProxy)))));
                break;
            case "US.continuity":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.estimated from fibula length":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Oral and maxillofacial surgery.physician attending":
                break;
            case "US.derived.PLN":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Nephrology.team":
                break;
            case "Non-airway pressure measurement":
                break;
            case "TUS-CPS":
                break;
            case "Imaging":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(imagingProxy)))));
                break;
            case "Holter monitor":
                break;
            case "Auramine fluorochrome stain":
                break;
            case "US+Estimated from FL":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.2D.mitral valve flow area.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Detection limit <= 3.0 mg/L":
                break;
            case "US.2D+Calculated by biplane ellipse method":
                 assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
               break;
            case "Malachite green stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(malachiteProxy)))));
                break;
            case "Medical toxicology":
                break;
            case "US.doppler.pressure half time":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Internal medicine":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(internalProxy)))));
                break;
            case "Malaria smear":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(malariaProxy)))));
                break;
            case "Clinical cardiac electrophysiology":
                break;
            case "Palliative care.physician attending":
                break;
            case "Dynamometer":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(dynamometerProxy)))));
                break;
            case "US+Estimated from FL.Tokyo 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Hematology+Medical oncology.team":
                break;
            case "Estimated from quickening date":
                break;
            case "US.estimated from Brenner 1976":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "IA.rapid":
                break;
            case "Probe.amp":
                break;
            case "Neurological surgery.physician resident":
                break;
            case "US+Estimated from AC.Campbell 1975":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from BPD.Hobbins 1983":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Aggl.slide":
                break;
            case "Estimate":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(estimateProxy)))));
                break;
            case "Fouchet stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(fouchetProxy)))));
                break;
            case "[9,10-3H] palmitate substrate":
                break;
            case "Fite-Faraco stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(fiteFaracoProxy)))));
                break;
            case "LHR":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(lhrProxy)))));
                break;
            case "Smear":
                break;
            case "Probe.amp.tar":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(probeAmpTarProxy)))));
                break;
            case "Team":
                break;
            case "Westergren.2H reading":
                break;
            case "US+Estimated from BPD.Merz 1988":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Oximetry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(oximetryProxy)))));
                break;
            case "BSL-23":
                break;
            case "Pediatric hematology-oncology":
                break;
            case "US+Estimated from AC.Jeanty 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.estimated from Williams 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "HAI":
                break;
            case "1-14C-leucine substrate":
                break;
            case "Reinsch":
                break;
            case "Observed.FLACC":
                break;
            case "US+Estimated from AC.Jeanty 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Neurological surgery":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(neurologicalProxy)))));
                break;
            case "DI-PAD CGP V 1.4":
                break;
            case "HAQ":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(haqProxy)))));
                break;
            case "US.2D+Calculated by dimension method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Nurse":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(nurseProxy)))));
                break;
            case "Rheumatology.physician resident":
                break;
            case "Hep2 substrate":
                break;
            case "US+Estimated from GSD.Daya 1991":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Fungus stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(fungusProxy)))));
                break;
            case "SEQ-C":
                break;
            case "Endocrinology.nurse":
                break;
            case "Digital model.measured":
                break;
            case "US+Estimated from AC&FL&HC":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Surgery of the hand":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(surgeryHandProxy)))));
                break;
            case "Primary care.physician assistant":
                break;
            case "Physician assistant":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(physicianAssistantProxy)))));
                break;
            case "CT.perfusion":
                break;
            case "RFLP":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(rflpProxy)))));
                break;
            case "Nile blue prusside":
                break;
            case "Heterometer test":
                break;
            case "Inspection":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(inspectionProxy)))));
                break;
            case "Neurology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(neurologyProxy)))));
                break;
            case "US+Estimated from HC.Lessoway 1998":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Evoked potential":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(evokedProxy)))));
                break;
            case "US+Estimated from OFD.Hansmann 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Sudan black B stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(sudanBlackBProxy)))));
                break;
            case "US+Estimated from OFD.Hansmann 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.mod.single-plane.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Interventional radiology.nurse":
                break;
            case "via Foley":
                break;
            case "US+Estimated from GSD.Rempen 1991":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Probe.amp.tar detection limit = 200 copies/mL":
                break;
            case "Dialysis.interdisciplinary":
                break;
            case "US+Estimated from HC":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "PSR scale":
                break;
            case "US+Estimated from HL":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Vascular surgery.physician assistant":
                break;
            case "Geriatric medicine.physician attending":
                break;
            case "Chiropractic medicine":
                break;
            case "Colposcopy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(colposcopyProxy)))));
                break;
            case "US+Estimated from BD.Jeanty 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Chromo":
                break;
            case "Patient":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(patientProxy)))));
                break;
            case "Immunophenotyping":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(immunophenotypingProxy)))));
                break;
            case "Clinical nurse specialist":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(clinicalNurseProxy)))));
                break;
            case "US+Estimated from IOD.Trout 1994":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Perimetry.octupus":
                break;
            case "Speech-language pathology+Audiology":
                break;
            case "Azure-eosin stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(azureEosinProxy)))));
                break;
            case "Hematology+Medical oncology.nurse":
                break;
            case "US+Estimated from BD.Jeanty 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Peak flow meter":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(peakProxy)))));
                break;
            case "USSG-FHT":
                break;
            case "Cystatin-based formula":
                break;
            case "Prosigna":
                break;
            case "US.derived":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from tibia length":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Crithidia luciliae IF":
                break;
            case "Recreational therapy.interdisciplinary":
                break;
            case "US.M-mode+Calculated by dimension method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Esophagoscopy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(esophagoscopyProxy)))));
                break;
            case "Sports medicine":
                break;
            case "NMMDS":
                break;
            case "US+Estimated from BPD.Jeanty 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Urology.physician assistant":
                break;
            case "US+Estimated from BPD.Jeanty 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case 
                "Diabetology.case manager":
                break;
            case "US+Estimated from FL.Hohler 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Kennedy classification":
                break;
            case "US+Estimated from HC.ASUM 2000":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "SAMHSA screen":
                break;
            case "Radiation oncology.physician attending":
                break;
            case "FTND":
                break;
            case "Safranin stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(safraninProxy)))));
                break;
            case "Trichrome stain.Masson":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(trichromeMassonProxy)))));
                break;
            case "Calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(calculatedProxy)))));
                break;
            case "US.estimated from fibula length.Merz 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Physical medicine and rehab.physician resident":
                break;
            case "Rebreathing":
                break;
            case "Multi-specialty program.case manager":
                break;
            case "Sticky tape for environmental fungus":
                break;
            case "US+Estimated from AD&BPD&FL.Rose 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Perimetry.Goldman":
                break;
            case "WHO classification":
                break;
            case "Isopropanol stability":
                break;
            case "NAACCR v.12":
                break;
            case "Dialysis.physician":
                break;
            case "NAACCR v.11":
                break;
            case "Palpation":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(palpationProxy)))));
                break;
            case "Conglutinin assay":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(conglutininProxy)))));
                break;
            case "Glucometer":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(glucometerProxy)))));
                break;
            case "Chromatography.column":
                break;
            case "US.Teicholz.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Post hydrolysis":
                break;
            case "Crystal violet stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(crystalProxy)))));
                break;
            case "US+Estimated from BPD.Shinozuka 1996":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "COOP":
                break;
            case "Urology.nurse practitioner":
                break;
            case "Obstetrics.midwife":
                break;
            case "Research.nurse":
                break;
            case "US+Estimated from TTD.Hansmann 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Oxygen analyzer":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(oxygenProxy)))));
                break;
            case "Physician fellow":
                break;
            case "Pulmonary disease.physician fellow":
                break;
            case "US+Estimated from AC&BPD&FL":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from FL.Osaka 1989":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "HIV":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(hivProxy)))));
                break;
            case "SLUMS":
                break;
            case "Pharmacology.nurse":
                break;
            case "Dosage of chromosome specific cf DNA":
                break;
            case "Occupational medicine.nurse":
                break;
            case "Electrophoresis pH 6.3":
                break;
            case "Electrophoresis pH 6.0":
                break;
            case "DAST":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(dastProxy)))));
                break;
            case "Probe.amp.tar detection limit = 0.5 log copies/mL":
                break;
            case "Perimetry.TEC":
                break;
            case "Community health care.nurse":
                break;
            case "US+Estimated from BD":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Plastic surgery.nurse":
                break;
            case "Zetafuge":
                break;
            case "Acid fast stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(acidProxy)))));
                break;
            case "Vascular surgery":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(vascularProxy)))));
                break;
            case "Diepoxybutane":
                break;
            case "Butyrate esterase stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(butyrateProxy)))));
                break;
            case "US.2D.mod.biplane ellipse.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "P.E.D.S.":
                break;
            case "Nutrition and dietetics.team":
                break;
            case "US+Estimated from AC&BPD.Shepard 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Tonometry.Schistz":
                break;
            case "Genotyping":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(genotypingProxy)))));
                break;
            case "Neurology.physician attending":
                break;
            case "Branemark scale":
                break;

            default:
                addLoincMethod2(builder, loincField, assertions);
        }
    }

    private void addLoincMethod2(LogicalExpressionBuilder builder, String loincField, List<Assertion> assertions) {
        switch (loincField) {
            case "US.M-mode+Calculated by cube method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Angiography":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(angiogramProxy)))));
                break;
            case "Obstetrics and gynecology.nurse":
                break;
            case "US+Estimated from AC&BPD&FL.Hadlock 1985":
                break;
            case "Mental health.nurse":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(mentalProxy)))));
                break;
            case "US+Estimated from AC&BPD&FL.Hadlock 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from AC&FL&HC.Vintzileos 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.estimated from radius length.Merz 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Cockcroft-Gault formula":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(cockcroftGaultProxy)))));
                break;
            case "BSA formula":
                break;
            case "Based on container volume":
                break;
            case "US+Estimated from AD&FL.Rose 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Electrophoresis.citrate agar":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(electrophoresisCitrateProxy)))));
                break;
            case "Noninvasive":
                break;
            case "Cell wall fatty acid analysis":
                break;
            case "Estimated.Bolton analysis":
                break;
            case "US.2D+Calculated by Gibson method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Fridericia formula":
                break;
            case "Probe insertion.Hagar":
                break;
            case "PROMIS.PARENTPROXY":
                break;
            case "US+Estimated from AC":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Cold incubation":
                break;
            case "MLPA":
                break;
            case "US.derived from Chitty 1994":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Snellen eye chart":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(snellenProxy)))));
                break;
            case "Pharmacology.technician":
                break;
            case "The Position Generator":
                break;
            case "PCORnet":
                break;
            case "Predicted":
                break;
            case "Primary care.physician":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(primaryProxy)))));
                break;
            case "US+Estimated from AC.Merz 1988":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "15M reading":
                break;
            case "XR.portable":
                break;
            case "US+Estimated from CRL.ASUM 2000":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.2D+Calculated by single-plane ellipse method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "RBCs diluted":
                break;
            case "Urology.physician resident":
                break;
            case "AQ":
                break;
            case "Psychiatry.team":
                break;
            case "Nursing":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(nursingProxy)))));
                break;
            case "Reported.dispatch":
                break;
            case "HL7.Attach":
                break;
            case "Carmine stain.Best":
                break;
            case "BH":
                break;
            case "PHQ.CMS":
                break;
            case "Nerve conduction":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(nerveProxy)))));
                break;
            case "AAAM":
                break;
            case "US.estimated from Arbuckle 1993 male singleton":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Pulse oximetry.plethysmograph":
                break;
            case "6D cold incubation":
                break;
            case "Aggl.plate.buffered acidified":
                break;
            case "Levamisole inhibition":
                break;
            case "Gastroenterology.nurse":
                break;
            case "General medicine.nurse":
                break;
            case "Interventional radiology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(interventionalProxy)))));
                break;
            case "Ring and weigh":
                break;
            case "Hand optometer":
                break;
            case "AM-PAC":
                break;
            case "Comp of est fetal wgt W std pop dist at same estga":
                break;
            case "CT":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ctProxy)))));
                break;
            case "US+Estimated from BPD&FTA&FL.Osaka 1990":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Pediatric rheumatology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pediatricRheumatologyProxy)))));
                break;
            case "CTAS":
                break;
            case "Aggl.cord RBC":
                break;
            case "Pulmonary disease.nurse":
                break;
            case "BS II":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(bsProxy)))));
                break;
            case "Angiogram":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(angiogramProxy)))));
                break;
            case "Vassar-culling stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(vassarCullingProxy)))));
                break;
            case "US+Estimated from CRL.Robinson 1975":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Transplant surgery.physician attending":
                break;
            case "WHO method":
                break;
            case "Orthopaedic surgery":
                break;
            case "Respiratory therapy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(respiratoryProxy)))));
                break;
            case "Uroflowmetry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(uroflowmetryProxy)))));
                break;
            case "US.2D.parasternal":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Perimetry.Kowa":
                break;
            case "SWAN ADHD":
                break;
            case "Adolescent medicine":
                break;
            case "Fick":
                break;
            case "Macroscopy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(macroscopyProxy)))));
                break;
            case "Perimetry.Humphrey":
                break;
            case "US+Estimated from CRL.Shinozuka 1996":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.transient elastography":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Cytogenetics":
                break;
            case "Anesthesiology.nurse":
                break;
            case "HRQOL":
                break;
            case "Geriatric medicine.interdisciplinary":
                break;
            case "LISS":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(lissProxy)))));
                break;
            case "US.cubed.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Apt-Downey":
                break;
            case "US+Estimated from AC&HC.Hadlock 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "HARK":
                break;
            case "Aggl.plate":
                break;
            case "US.estimated from Rempen 1991":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.composite.estimated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "GC":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(gcProxy)))));
                break;
            case "Creatinine-based formula (CKD-EPI)":
                break;
            case "PEG assay":
                break;
            case "Nuclear.blood pool":
                break;
            case "Schirmer test":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(schirmerProxy)))));
                break;
            case "Womens health.nurse practitioner":
                break;
            case "Obstetrics and gynecology.physician resident":
                break;
            case "Reported.GDS":
                break;
            case "Heparin protamine titration":
                break;
            case "HOOSJR":
                break;
            case "Methylene blue stain.Loeffler":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(methyleneLoefflerProxy)))));
                break;
            case "KFRE":
                break;
            case "US+Estimated from HC.Merz 1988":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "CMS Assessment":
                break;
            case "NTDS":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ntdsProxy)))));
                break;
            case "Kinyoun iron hematoxylin stain":
                break;
            case "Airway pressure monitor":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(airwayProxy)))));
                break;
            case "Lauren classification":
                break;
            case "Radiation oncology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(radiationProxy)))));
                break;
            case "4-MU-palmitate substrate":
                break;
            case "HA":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(haProxy)))));
                break;
            case "Mental health.interdisciplinary":
                break;
            case "Pelli-Robson eye chart":
                break;
            case "Cell saver":
                break;
            case "Community health care.case manager":
                break;
            case "HVS":
                break;
            case "Steiner stain":
                break;
            case "NM.SPECT+CT":
                break;
            case "Addiction medicine.physician":
                break;
            case "Exopthalmometer.Hertel":
                break;
            case "MG.stereotactic":
                break;
            case "IA":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(iaProxy)))));
                break;
            case "IB":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ibProxy)))));
                break;
            case "GOSE":
                break;
            case "IF":
                break;
            case "MEMS Cap":
                break;
            case "Plethysmograph.body box":
                break;
            case "Cystic fibrosis respiratory culture":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(cysticProxy)))));
                break;
            case "OPTIMAL":
                break;
            case "Pediatric cardiology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pediatricCardiologyProxy)))));
                break;
            case "Coag inverse ratio":
                break;
            case "Prewarmed":
                break;
            case "Preventive medicine.nurse":
                break;
            case "Tartrate-resistant acid phosphatase stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(tartrateResistantProxy)))));
                break;
            case "Orcein stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(orceinProxy)))));
                break;
            case "Warthin-Starry stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(warthinStarryProxy)))));
                break;
            case "Angiography.biplane":
                break;
            case "Framingham.Wilson 1998":
                break;
            case "RF.angio":
                break;
            case "Thoracic and cardiac surgery":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(thoracicProxy)))));
                break;
            case "Albumin technique":
                break;
            case "Sequencing":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(sequencingProxy)))));
                break;
            case "Kleihauer-Betke":
                break;
            case "KM":
                break;
            case "NCFS":
                break;
            case "Giemsa stain.May-Grunwald":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(giemsaMayGrunwaldProxy)))));
                break;
            case "Electromyogram":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(electromyogramProxy)))));
                break;
            case "US+Estimated from HL.Osaka 1989":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Quick method":
                break;
            case "Orthopaedic surgery.physician assistant":
                break;
            case "Polysomnography":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(polysomnographyProxy)))));
                break;
            case "HAQ-DI":
                break;
            case "Churukian-Schenk stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(churukianSchenkProxy)))));
                break;
            case "Kinesiotherapy":
                break;
            case "Fluorescent polarization assay":
                break;
            case "LA":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(laProxy)))));
                break;
            case "Geriatric medicine":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(geriatricProxy)))));
                break;
            case "Social service":
                break;
            case "Refractometry":
                break;
            case "Phenotyping":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(phenotypingProxy)))));
                break;
            case "Periodic acid-Schiff stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(periodicProxy)))));
                break;
            case "Wayson stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(waysonProxy)))));
                break;
            case "Lap":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(peritoneoscopyProxy)))));
                break;
            case "Macchiavello stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(macchiavelloProxy)))));
                break;
            case "Cover test":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(coverProxy)))));
                break;
            case "US+Estimated from BPD.Rempen 1991":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Radiation oncology.nurse":
                break;
            case "US.doppler.derived":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "MoCA":
                break;
            case "M3":
                break;
            case "Rosenberg":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(rosenbergProxy)))));
                break;
            case "CDC.PHIN":
                break;
            case "Molgen":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(molgenProxy)))));
                break;
            case "US+Estimated from BPD.Hadlock 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "MG":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(mgProxy)))));
                break;
            case "US+Estimated from BPD.Hadlock 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Ophthalmology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ophthalmologyProxy)))));
                break;
            case "MDQ":
                break;
            case "MDS":
                break;
            case "US.doppler.CW":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Interventional radiology.physician resident":
                break;
            case "Framingham.Murabito 1997":
                break;
            case "Pediatric dermatology":
                break;
            case "US.doppler.VTI+Area.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Test strip.automated":
                break;
            case "Radiology":
                break;
            case "Surgery.case manager":
                break;
            case "4-MU-oleate substrate":
                break;
            case "NM":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(nmProxy)))));
                break;
            case "Cardiac catheterization":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(cardiacCathProxy)))));
                break;
            case "Epilepsy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(epilepsyProxy)))));
                break;
            case "Calculated.AlloMap":
                break;
            case "Speech-language pathology.team":
                break;
            case "XR diffraction":
                break;
            case "Surgery.medical student":
                break;
            case "Developmental-behavioral pediatrics":
                break;
            case "Podiatry.nurse":
                break;
            case "US.tissue doppler":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "OASIS":
                break;
            case "US && MG":
                break;
            case "NAPIIA":
                break;
            case "US.2D+Calculated by Teichholz method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.doppler.Vmax+Area.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Estimated from glycated hemoglobin":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(estimatedFromHemogloobinProxy)))));
                break;
            case "Giemsa stain.3 micron":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(giemsaMicronProxy)))));
                break;
            case "Endocrinology.physician resident":
                break;
            case "Hypotonic dilution":
                break;
            case "Optometry":
                break;
            case "Cytotoxin tissue culture assay":
                break;
            case "US+Estimated from TCD.Chitty 1994":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Immediate spin":
                break;
            case "US.2D.aortic root.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Non-probe.amp.tar":
                break;
            case "Heat stability":
                break;
            case "Von Kossa stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(vonProxy)))));
                break;
            case "US+Estimated from FL.Hansmann 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.2D.mod":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Trichrome stain.Masson modified":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(trichromeMassonModProxy)))));
                break;
            case "US+Estimated from FL.Hansmann 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "ERCP":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ercpProxy)))));
                break;
            case "US.derived from OFD&O-I BPD":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Immunobead":
                break;
            case "Enteroscopy.thru stoma":
                break;
            case "US+Calculated by Teichholz method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Neurological surgery.nurse":
                break;
            case "PT":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ptProxy)))));
                break;
            case "AUDADIS-IV":
                break;
            case "Psychology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(psychologyProxy)))));
                break;
            case "US+Estimated from TTD.Lessoway 1998":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Orthopaedic surgery.nurse practitioner":
                break;
            case "US.doppler.PW":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "2-14C-pyruvate substrate":
                break;
            case "Mental health.physician":
                break;
            case "Radnuc":
                break;
            case "Lekholm & Zarb classification":
                break;
            case "Spinal cord injury medicine.physician resident":
                break;
            case "IF rat kidney substrate":
                break;
            case "AUASI":
                break;
            case "Pediatric pulmonology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pediatricPulmonologyProxy)))));
                break;
            case "Autorefractor.sciascopy":
                break;
            case "US.estimated from EFW3.Shinozuka 1996":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "IF Hep2 substrate":
                break;
            case "US+Estimated from AC&BPD&FL.Woo 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "SCORE.PC.Conroy 2003":
                break;
            case "EIA.3rd IS":
                break;
            case "Critical Care Medicine":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(criticalProxy)))));
                break;
            case "MIC":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(micProxy)))));
                break;
            case "CPHS":
                break;
            case "US+Estimated from AC.Hadlock 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Screen>300 ng/mL":
                break;
            case "OMB.1997":
                break;
            case "DXA":
                break;
            case "US+Estimated from AC.Hadlock 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "NPUAP":
                break;
            case "US+Estimated from AC&BPD":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "IQCODE":
                break;
            case "RF":
                break;
            case "US.estimated from ASUM 2000":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from AC&HC.Jordaan 1983":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Creatinine-based formula (MDRD)":
                break;
            case "vWF dosing":
                break;
            case "Holzer stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(holzerProxy)))));
                break;
            case "US.M-mode+Calculated by Gibson method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Anesthesiology.physician attending":
                break;
            case "Mouse bioassay.neutralization":
                break;
            case "US.M-mode+Calculated by Teichholz method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Bariatric surgery":
                break;
            case "Psychiatry.physician resident":
                break;
            case "Bagolini test":
                break;
            case "Organism specific culture":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(organismProxy)))));
                break;
            case "Carbapenemase Nordmann-Poirel":
                break;
            case "CPIC":
                break;
            case "Wright stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(wrightProxy)))));
                break;
            case "VAERS":
                break;
            case "US.doppler.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Nuclear.myocardial":
                break;
            case "DAST-10":
                break;
            case "ICA":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(icaProxy)))));
                break;
            case "Transplant surgery":
                break;
            case "CT.scanogram":
                break;
            case "Acridine orange and Giemsa stain":
                break;
            case "Child and adolescent psychiatry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(childProxy)))));
                break;
            case "KOOS":
                break;
            case "NI-ICG":
                break;
            case "Perimetry.standard":
                break;
            case "US+Estimated from TC.Chitkara 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "MR.angio":
                break;
            case "Detection limit <= 20 mg/L":
                break;
            case "Investigation":
                break;
            case "Coag.diatomaceous earth induced":
                break;
            case "Physical medicine and rehab.physician":
                break;
            case "Gastroenterology.physician resident":
                break;
            case "Raji cell assay":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(rajiProxy)))));
                break;
            case "Brain injury":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(brainProxy)))));
                break;
            case "NAMCS":
                break;
            case "US+Estimated from GSL.Hansmann 1979":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "MLC":
                break;
            case "Colloidal ferric oxide stain.Hale":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(colloidalProxy)))));
                break;
            case "Brilliant cresyl blue":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(brilliantProxy)))));
                break;
            case "Perimetry.data interchange":
                break;
            case "US+Estimated from CRL.Nelson 1981":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "IDI":
                break;
            case "Urology.physician attending":
                break;
            case "Social work":
                break;
            case "Vascular neurology":
                break;
            case "37 deg C incubation":
                break;
            case "Automated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(automatedProxy)))));
                break;
            case "Rheumatology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(rheumatologyProxy)))));
                break;
            case "Pediatric critical care medicine":
                break;
            case "Per age":
                break;
            case "gender and height":
                break;
            case "PAGE":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pageProxy)))));
                break;
            case "ALSPAC":
                break;
            case "VC":
                break;
            case "US.doppler+tissue doppler":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Pharmacology.team":
                break;
            case "US.estimated from FTA.Osaka 1989":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Internal medicine.interdisciplinary":
                break;
            case "Wound care management.nurse":
                break;
            case "Test strip manual":
                break;
            case "US.doppler.derived.full Bernoulli":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "VR":
                break;
            case "US+Estimated from CRL.Rempen 1991":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Subjective refraction":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(subjectiveProxy)))));
                break;
            case "Endocrinology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(endocrinologyProxy)))));
                break;
            case "Hemosiderin stain":
                break;
            case "Gastroenterology.physician fellow":
                break;
            case "TOAST":
                break;
            case "Mental health":
                break;
            case "RF.portable":
                break;
            case "Rearing/Culture":
                break;
            case "Ethics":
                break;
            case "Clark":
                break;
            case "Surgery.nurse practitioner":
                break;
            case "Screen>20 ng/mL":
                break;
            case "Pentachrome stain.Movat":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pentachromeProxy)))));
                break;
            case "US+Estimated from AC&BPD.Eik-Nes 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Anoscopy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(anoscopyProxy)))));
                break;
            case "US+Estimated from CRL.Jeanty 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Nurse practitioner":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(nursePracticionerProxy)))));
                break;
            case "US.estimated from Hadlock 1981":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Rosette test":
                break;
            case "Neurology w special qualifications in child neuro":
                break;
            case "US+Estimated from CRL.ASUM 1991":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Malaria thick smear":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(malariaThickProxy)))));
                break;
            case "US.2D+Calculated by biplane area-length method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Neurological surgery.nurse practitioner":
                break;
            case "Geriatric medicine.physician resident":
                break;
            case "Creatinine-based formula (Schwartz)":
                break;
            case "XR":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(xrProxy)))));
                break;
            case "US.estimated from Hadlock 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Sudan black stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(sudanBlackProxy)))));
                break;
            case "US.estimated from Hadlock 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Dermatology.nurse practitioner":
                break;
            case "Gomori stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(gomoriProxy)))));
                break;
            case "TIMP":
                break;
            case "VAP":
                break;
            case "Chest xray.calculated":
                break;
            case "Manual":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(manualProxy)))));
                break;
            case "Clinical.estimated from prior gest age assessment":
                break;
            case "US.2D.A2C+Measured by planimetry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.estimated from AXT.Shinozuka 1996":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Probe.amp.sig":
                break;
            case "Dentistry.hygienist":
                break;
            case  "Cardiovascular disease.case manager":
                break;
            case "Electroretinogram":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(electroretinogramProxy)))));
                break;
            case "Animal inoculation":
                break;
            case "XR.tomography":
                break;
            case "US+Estimated from CRL.Jeanty 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Periodic acid-Schiff stain with diatase digestion":
                break;
            case "Urology.interdisciplinary":
                break;
            case "US.doppler+Calculated by simplified Bernoulli":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Bronchoscopy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(bronchoscopyProxy)))));
                break;
            case "Speech-language pathology":
                break;
            case "Pharmacy records":
                break;
            case "US+Estimated from OFD.Lessoway 1998":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Forensic medicine":
                break;
            case "Radiation oncology.therapist":
                break;
            case "Addiction medicine.team":
                break;
            case "Kremer":
                break;
            case "Nephrology.physician fellow":
                break;
            case "Nephrology.physician attending":
                break;
            case "Hematoxylin and eosin stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(hematoxylinProxy)))));
                break;
            case "US+Estimated from FL.Merz 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Invasive":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(invasiveProxy)))));
                break;
            case "US.2D.mod.single-plane ellipse.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "CT.angio":
                break;
            case "Alcian blue stain.with periodic acid-Schiff":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(alcianAcidSchiffProxy)))));
                break;
            case "Research":
                break;
            case "Dry mount":
                break;
            case "Glock & McLean":
                break;
            case "Hematology+Medical oncology.nurse practitioner":
                break;
            case "US+Estimated from AC&BPD.Weinberger 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Pain medicine":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(painProxy)))));
                break;
            case "Amniocentesis":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(amniocentesisProxy)))));
                break;
            case "Auscultation":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(auscultationProxy)))));
                break;
            case "Sudan IV stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(sudan4Proxy)))));
                break;
            case "Primary care.case manager":
                break;
            case "US.estimated from Arbuckle 1993 male twins":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Gastric tonometry":
                break;
            case "Colon and rectal surgery.physician fellow":
                break;
            case "Wound care management":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(woundProxy)))));
                break;
            case "US+Estimated from AD&BPD.Rose 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "VEP":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(vepProxy)))));
                break;
            case "Per age and gender":
                break;
            case "NHCS":
                break;
            case "Obstetrics and Gynecology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(obstetricsProxy)))));
                break;
            case "Estimated from selected delivery date":
                break;
            case "Flexible sigmoidoscopy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(flexibleProxy)))));
                break;
            case "Screen>25 ng/mL":
                break;
            case "[9,10-3H] myristate substrate":
                break;
            case "Culture":
                break;
            case "Rhodamine stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(rhodamineProxy)))));
                break;
            case "Jefferies":
                break;
            case "US standard certificate of death":
                break;
            case "EEG":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(eegProxy)))));
                break;
            case "P-GBI":
                break;
            case "US+Estimated from AD&BPD.Vintzileos 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Aggr":
                break;
            case "Primary care.nurse practitioner":
                break;
            case "Physical therapy.team":
                break;
            case "2nd IRP":
                break;
            case "Acid phosphatase stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(acidPhosphateProxy)))));
                break;
            case "Aggl":
                break;
            case "Framingham.D'Agostino 2000":
                break;
            case "Mental health.physician assistant":
                break;
            case "Advanced heart failure and transplant cardiology":
                break;
            case "Neurological surgery.physician attending":
                break;
            case "Internal medicine.nurse practitioner":
                break;
            case "HL7.CCDAr1.1":
                break;
            case "US.estimated from EFW2.Shinozuka 1996":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.M-mode+Calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Reaction: lactate to pyruvate":
                break;
            case "Interventional cardiology":
                break;
            case "US+Estimated from OOD.Trout 1994":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "MMSE":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(mmseProxy)))));
                break;
            case "Visual":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(visualProxy)))));
                break;
            case "Palliative care.physician resident":
                break;
            case "Gynecologic oncology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(gynecologicProxy)))));
                break;
            case "Chiropractic":
                break;
            case "EGD":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(egdProxy)))));
                break;
            case "Bioassay":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(bioassayProxy)))));
                break;
            case "Maddox double prism test":
                break;
            case "Mental health.team":
                break;
            case "KCCQ":
                break;
            case "Mucicarmine stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(mucicarmineProxy)))));
                break;
            case "Coag.two stage":
                break;
            case "Ophthalmoscopy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ophthalmoscopyProxy)))));
                break;
            case "Pediatric urology":
                break;
            case "Manual count":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(manualCountProxy)))));
                break;
            case "Observed.BIMS":
                break;
            case "UPPS-R-C":
                break;
            case "Palliative care":
                break;
            case "US.doppler.CW+Calculated by simplified Bernoulli":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Gridley stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(gridleyProxy)))));
                break;
            case "High resolution":
                break;
            case "Cardiovascular disease":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(cardiovascularProxy)))));
                break;
            case "Acid fast stain.Kinyoun":
                break;
            case "Phoropter":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(phoropterProxy)))));
                break;
            case "Thoracic and cardiac surgery.medical student":
                break;
            case "Methenamine silver stain.Grocott":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(methenamineGrocottProxy)))));
                break;
            case "Viral subtyping":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(viralProxy)))));
                break;
            case "Pathologist comment":
                break;
            case "HL7.VMR-CDS":
                break;
            case "Pulse oximetry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pulseProxy)))));
                break;
            case "28 deg C incubation":
                break;
            case "Critical care medicine.physician attending":
                break;
            case "Framingham.The Adult Treatment Panel III 2001":
                break;
            case "NYHA":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(nyhaProxy)))));
                break;
            case "Pediatric surgery":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pediatricSurgeryProxy)))));
                break;
            case "Emergency medicine":
                break;
            case "Estimated from FL.Merz 1988":
                break;
            case "Fungal subtyping":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(fungalProxy)))));
                break;
            case "Confrontation":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(confrontationProxy)))));
                break;
            case "BIMS":
                break;
            case "Endocrinology.physician attending":
                break;
            case "Impedance.transthoracic":
                break;
            case "Impedance":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(impedanceProxy)))));
                break;
            case "NHIS":
                break;
            case "Infrared spectroscopy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(infraredProxy)))));
                break;
            case "Reported.PHQ-9.CMS":
                break;
            case "Thoracic and cardiac surgery.interdisciplinary":
                break;
            case "Calculated from oxygen partial pressure":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(calculatedOxygenProxy)))));
                break;
            case "RIOPA":
                break;
            case "US+Estimated from CRL.Daya 1993":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Fontana-Masson stain":
                break;
            case "MAST-G":
                break;
            case "US.2D.left ventricular outflow tract.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "BINS":
                break;
            case "Mycobacterial subtyping":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(mycobacterialProxy)))));
                break;
            case "CDAI":
                break;
            case "ISE":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(iseProxy)))));
                break;
            case "Physical therapy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(physicalProxy)))));
                break;
            case "Medical student":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(medicalProxy)))));
                break;
            case "Reported.howRU":
                break;
            case "US.2D+Calculated by bullet method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from HL.ASUM 2000":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "ACE":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(aceProxy)))));
                break;
            case "Aerospace medicine":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(aerospaceProxy)))));
                break;
            case "US+Estimated from BPD.Hansmann 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from BPD.Hansmann 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Proctosigmoidoscopy.rigid":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(proctosigmoidoscopyRigidProxy)))));
                break;
            case "Neutral red stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(neutralProxy)))));
                break;
            case "Detection limit <= 0.05 mIU/L":
                break;
            case "ACT":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(actProxy)))));
                break;
            case "Autorefractor.auto":
                break;
            case "AD8":
                break;
            case "Framingham.Wang 2003":
                break;
            case "Farr":
                break;
            case "QIDS":
                break;
            case "Vanderbilt ADHD":
                break;
            case "KOH preparation":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(kohProxy)))));
                break;
            case "Phosphotungstic acid hematoxyl":
                break;
            case "Observed.PHQ-9.CMS":
                break;
            case "US.estimated from cerebellar diameter":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Confirm>20 ng/mL":
                break;
            case "Keratometry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(keratometryProxy)))));
                break;
            case "US.doppler.velocity+Diameter.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "LIBCSP":
                break;
            case "Recreational therapy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(recreationalProxy)))));
                break;
            case "Spinal cord injury medicine.nurse":
                break;
            case "Lawson-Van Gieson stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(lawsonvanProxy)))));
                break;
            case "Tape measure":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(tapeProxy)))));
                break;
            case "Acute respiratory distress culture":
                break;
            case "GNWCH":
                break;
            case "Reported.HIV-SSC":
                break;
            case "Rees-Ecker":
                break;
            case "US+Estimated from TCD.Goldstein 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Trichrome stain.Gomori-Wheatley":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(trichromeGomoroProxy)))));
                break;
            case "US+Estimated from AC&BPD.Thurnau 1983":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Internal medicine.nurse":
                break;
            case "US.estimated from cerebellum.Goldstein 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.continuity.Vmax+Area":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Gastroenterology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(gastroenterologyProxy)))));
                break;
            case "US+Estimated from TAD.Hansmann 1979":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Machine estimate":
                break;
            case "Calculated.PLN":
                break;
            case "Rheumatology.nurse":
                break;
            case "US+Estimated from FL.Jeanty 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from FL.Jeanty 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Concentration.Baerman":
                break;
            case "RIA":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(riaProxy)))));
                break;
            case "Pedometer":
                break;
            case "Dentistry.technician":
                break;
            case "US+Estimated from CRL.Hansmann 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from CRL.Hansmann 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Macro/Micro ID":
                break;
            case "Luxol fast blue/Cresyl violet stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(luxolProxy)))));
                break;
            case "UCLA Loneliness Scale v3":
                break;
            case "US.2D.mod.biplane":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Wade stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(wadeProxy)))));
                break;
            case "Immobilization":
                break;
            case "Physician resident":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(physicianResidentProxy)))));
                break;
            case "Estimated from last menstrual period":
                break;
            case "US.M-mode+Measured":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "HOOS":
                break;
            case "Objective refraction":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(objectiveProxy)))));
                break;
            case "Cardiac surgery":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(cardiacSurgeryProxy)))));
                break;
            case "Hematoxylin-eosin-Harris regressive stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(hematoxylinEosinHarrisProxy)))));
                break;
            case "FEAS":
                break;
            case "Surgery":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(surgeryProxy)))));
                break;
            case "Microscopy.electron.negative stain":
                break;
            case "Probe.amp detection limit = 2.6 log copies/mL":
                break;
            case "Clinical.estimated":
                break;
            case "Calculated.HepaScore":
                break;
            case "SAMHSA":
                break;
            case "Probe.amp.tar.primer-probe set H5a":
                break;
            case "VSP":
                break;
            case "US+Estimated from tibia length.Jeanty 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from OOD.Mayden 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Probe.amp.tar.primer-probe set H5b":
                break;
            case "Measured.Bolton analysis":
                break;
            case "US.2D.A2C":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Automated image cytometry":
                break;
            case "Observed.MERSTH":
                break;
            case "Hematology+Medical oncology.pharmacist":
                break;
            case "US.measured from Chitty 1994":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "5D cold incubation":
                break;
            case "Pulmonary disease.physician resident":
                break;
            case "US+Estimated from O-I BPD.Chitty 1997":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Immunofixation":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(immunofixationProxy)))));
                break;
            case "Infectious disease.nurse":
                break;
            case "Microscopic observation":
                break;
            case "Probe.amp detection limit = 400 copies/mL":
                break;
            case "NDI":
                break;
            case "Palliative care.nurse":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(palliativeProxy)))));
                break;
            case "ESI":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(esiProxy)))));
                break;
            case "Tartrate inhibited":
                break;
            case "Primary care.nurse":
                break;
            case "Estimated from gestational age.Merz 1987":
                break;
            case "Cyto stain":
                break;
            case "Tetrachrome stain":
                break;
            case "Refractometry.automated":
                break;
            case "US.2D.A4C+Measured by planimetry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.estimated from ulna length":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "G-banded":
                break;
            case "Anaerobic culture":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(anaerobicProxy)))));
                break;
            case "Obstetrics and gynecology.physician attending":
                break;
            case "1-14C-pyruvate substrate":
                break;
            case "PFGE":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pfgeProxy)))));
                break;
            case "ETDRS eye chart":
                break;
            case "RHEA":
                break;
            case "Estimated from jugular venous distention":
                break;
            case "US.doppler.Vmax+Diameter.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.2D.A4C":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from Tokyo 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Estimated from conception date":
                break;
            case "NKDEP":
                break;
            case "US+Estimated from HC.Hoffbauer 1979":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Wet preparation":
                break;
            case "Psychiatry.interdisciplinary":
                break;
            case "US.Teicholz":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Intermitent measure":
                break;
            case "Minus glass":
                break;
            case "Microarray":
                break;
            case "Perceived Stress Scale-10":
                break;
            case "Next-of-kin":
                break;
            case "Perimetry.Dicon":
                break;
            case "US+Estimated from CRL.Yeh 1988":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Clinical neurophysiology":
                break;
            case "US+Estimated from HL.Merz 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Elution":
                break;
            case "Framingham.Schnabel 2009":
                break;
            case "Pain medicine.nurse":
                break;
            case "Trauma":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(traumaProxy)))));
                break;
            case "Geriatric medicine.nurse practitioner":
                break;
            case "Cardiovascular disease.physician resident":
                break;
            case "Psychology.team":
                break;
            case "RFC assessment":
                break;
            case "Transcutaneous CO2 monitor":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(transcutaneousCO2Proxy)))));
                break;
            case "US.estimated from Chitty 1994":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "HL7.v3":
                break;
            case "Occupational medicine":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(occupationalProxy)))));
                break;
            case "2D echo":
                break;
            case "Exercise stress test":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(exerciseProxy)))));
                break;
            case "US.measured":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Glickman classification":
                break;
            case "Confirm>25 ng/mL":
                break;
            case "Wright Giemsa stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(wrightGiemsaProxy)))));
                break;
            case "Infectious disease":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(infectiousProxy)))));
                break;
            case "NHL":
                break;
            case "RPR":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(rprProxy)))));
                break;
            case "LOINC Document Ontology":
                break;
            case "MS.MALDI-TOF":
                break;
            case "Spinal cord injury medicine.interdisciplinary":
                break;
            case "US.2D.pulmonic valve flow area.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Reproductive endocrinology and infertility":
                break;
            case "Duke":
                break;
            case "FACIT":
                break;
            case "ASRS":
                break;
            case "Occupational therapy.interdisciplinary":
                break;
            case "Reticulin stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(reticulinProxy)))));
                break;
            case "Spun":
                break;
            case "Thoracic and cardiac surgery.physician resident":
                break;
            case "US+Estimated from AC&FL.Woo 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Surgical oncology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(surgicalProxy)))));
                break;
            case "NSLAH":
                break;
            case "Methenamine silver nitrate stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(methenamineProxy)))));
                break;
            case "Computer assisted":
                break;
            case "Reaction: pyruvate to lactate":
                break;
            case "Immune diffusion":
                break;
            case "Denver Youth Survey":
                break;
            case "Radiation oncology.physician resident":
                break;
            case "US+Estimated from TAD.Tokyo 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Body hood":
                break;
            case "IFCC":
                break;
            case "Stated":
                break;
            case "Aggl.adult RBC":
                break;
            case "CARDIA":
                break;
            case "Gynecology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(gynecologyProxy)))));
                break;
            case "US.derived.ROT":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Helium rebreathing":
                break;
            case "Surgery.physician resident":
                break;
            case "US.estimated from length of vertebra.Tokyo 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Colon and rectal surgery.physician attending":
                break;
            case "Indirect antiglobulin test":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(indirectProxy)))));
                break;
            case "Geriatric medicine.team":
                break;
            case "Allergy and immunology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(allergyProxy)))));
                break;
            case "Chemical separation":
                break;
            case "US+Estimated from AC&BPD&FL&HC.Roberts 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from HC measured.Chitty 1997":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Agar diffusion":
                break;
            case "Congo red stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(congoProxy)))));
                break;
            case "US.estimated from Jeanty 1983":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Ophthalmology.technician":
                break;
            case "HKOI":
                break;
            case "US.estimated from Jeanty 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "MDS.basic":
                break;
            case "Vascular surgery.nurse practitioner":
                break;
            case "Aggl.standard tube":
                break;
            case "Observed":
                break;
            case "Medical microbiology - pathology":
                break;
            case "Nephelometry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(nephelometryProxy)))));
                break;
            case "Psychiatry.case manager":
                break;
            case "US+Estimated from AD&FL":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "ASQ":
                break;
            case "US.continuity.VTI+Area":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Vocational rehabilitation":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(vocationalProxy)))));
                break;
            case "Birth defects":
                break;
            case "Cardiovascular disease.nurse practitioner":
                break;
            case "Dentistry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(dentistryProxy)))));
                break;
            case "NeuroQol.Peds":
                break;
            case "Observed.CCC":
                break;
            case "Enteroscopy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(enteroscopyProxy)))));
                break;
            case "Hematology+Medical oncology.physician attending":
                break;
            case "US+Estimated from FL.Chitty 1997":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Banding":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(bandingProxy)))));
                break;
            case "Endocrinology.physician fellow":
                break;
            case "Observed.OMAHA":
                break;
            case "Screen>150 ng/mL":
                break;
            case "Tonometry.non contact":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(tonometryNonProxy)))));
                break;
            case "FISH":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(fishProxy)))));
                break;
            case "US+Estimated from TAD.Eriksen 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Mental health.physician resident":
                break;
            case "CareConnections":
                break;
            case "Detection limit <= 0.01 ng/mL":
                break;
            case "US+Estimated from CRL":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from BPD.Kurtz 1980":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Slit lamp biomicroscopy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(slitProxy)))));
                break;
            case "Rapid":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(rapidProxy)))));
                break;
            case "less than 30 minutes":
                break;
            case "Geriatric medicine.physician fellow":
                break;
            case "NEMSIS":
                break;
            case "Neut":
                break;
            case "Microscopy.light.LPF":
                break;
            case "Methyl green stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(methylProxy)))));
                break;
            case "Dye test":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(dyeProxy)))));
                break;
            case "Aggl.serial ring test":
                break;
            case "Tonometry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(tonometryProxy)))));
                break;
            case "C3d binding assay":
                break;
            case "Thromboelastography":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(thromboelastographyProxy)))));
                break;
            case "Internal medicine.physician attending":
                break;
            case "Respiratory culture":
                break;
            case "Giemsa stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(giemsaStainProxy)))));
                break;
            case "Dermatology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(dermatologyProxy)))));
                break;
            case "Reported.PHQ":
                break;
            case "US.2D.SAX at PM+Measured by planimetry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "EIA.RST":
                break;
            case "ALSFRS-R":
                break;
            case "Framingham.Kannel 1999":
                break;
            case "Westergren":
                break;
            case "Spinal cord injury medicine.physician attending":
                break;
            case "Physical medicine and rehab.team":
                break;
            case "Pediatric rehabilitation medicine":
                break;
            case "US+Estimated from FL.Mercer 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Primary care.team":
                break;
            case "Cardiovascular disease.nurse":
                break;
            case "US+Estimated from AC&BPD&FL&HC.Hadlock 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Quinacrine fluorescent stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(quinacrineProxy)))));
                break;
            case "Terminal deoxynucleotidyl transferase stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(terminalProxy)))));
                break;
            case "ACC-AHA Pooled Cohort.Goff 2013":
                break;
            case "Mental health.nurse practitioner":
                break;
            case "US.estimated from area corrected BPC":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US.derived.AUT":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Spirometry":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(spirometryProxy)))));
                break;
            case "Toluidine blue O stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(toluidineProxy)))));
                break;
            case "US.estimated from ulna length.Jeanty 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Indicator dilution":
                break;
            case "Geriatric medicine.nurse":
                break;
            case "Registered nurse":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(registeredProxy)))));
                break;
            case "MAST":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(mastProxy)))));
                break;
            case "Probe.amp.tar detection limit = 400 copies/mL":
                break;
            case "Photometric":
                break;
            case "Isoelectric focusing":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(isoelectricProxy)))));
                break;
            case "US.2D+Calculated by Devereux method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Direct assay":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(directProxy)))));
                break;
            case "Radiology.nurse":
                break;
            case "Estimated from gestational age":
                break;
            case "Microscopy":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(microscopyProxy)))));
                break;
            case "Clinical pharmacology":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(clinicalPharmacologyProxy)))));
                break;
            case "Environmental culture":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(environmentalProxy)))));
                break;
            case "US+Estimated from FL.Obrien 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Anaerobic culture 25 deg C incubation":
                break;
            case "Silver stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(silverProxy)))));
                break;
            case "XR && RF":
                break;
            case "US+Estimated from AC&FL&HC.Hadlock 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from AC&FL&HC.Hadlock 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
               break;
            case "Sterile body fluid culture":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(sterileProxy)))));
                break;
            case "Sheep cell aggl":
                break;
            case "Infrared absorption":
                break;
            case "Dark red glass test":
                break;
            case "XR.measured":
                break;
            case "Argentaffin stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(argentaffinProxy)))));
                break;
            case "Luxol fast blue/Periodic acid-Schiff stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(luxolPeriodicProxy)))));
                break;
            case "US.3D":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Social work.nurse":
                break;
            case "Observed.Braden scale":
                break;
            case "Vascular surgery.physician resident":
                break;
            case "General medicine.medical student":
                break;
            case "IF rat liver+stomach+kidney substrate":
                break;
            case "Methylene blue stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(methyleneProxy)))));
                break;
            case "NIOSH":
                break;
            case "Maddox Stabchen test":
                break;
            case "US+Estimated from GSL.Nyberg 1992":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Cardiovascular disease.clinical nurse specialist":
                break;
            case "Spinal cord injury medicine":
                break;
            case "Spinal cord injury medicine.physician":
                break;
            case "PT.perfusion":
                break;
            case "Ophthalmometer":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ophthalmometerProxy)))));
                break;
            case "US.derived.EMP":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Sukharev":
                break;
            case "Mouse bioassay":
                break;
            case "Modified Giemsa":
                break;
            case "US.modified Simpson.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "CIDI-SF":
                break;
            case "US+Estimated from FL.Lessoway 1998":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Nephrology.nurse practitioner":
                break;
            case "Tumor board":
                break;
            case "BSL-SUPP":
                break;
            case "US+Estimated from HC.Hadlock 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Hematology":
                break;
            case "Aerobic culture 25 deg C incubation":
                break;
            case "US+Estimated from HC.Hadlock 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Transplant surgery.physician resident":
                break;
            case "Electronic":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(electronicProxy)))));
                break;
            case "Neurology.physician resident":
                break;
            case "Plastic surgery.physician fellow":
                break;
            case "US.2D":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "NHANES":
                break;
            case "Pharmacist":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(pharmacistProxy)))));
                break;
            case "US+Estimated from AC.Warsof 1977":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "howRU":
                break;
            case "Probe.amp.tar detection limit = 50 IU/mL":
                break;
            case "Gastroenterology.clinical nurse specialist":
                break;
            case "Transplant surgery.case manager":
                break;
            case "Otolaryngology.nurse":
                break;
            case "WHI":
                break;
            case "Orthopaedic surgery.physician attending":
                break;
            case "MDS.full":
                break;
            case "Colon and rectal surgery.nurse":
                break;
            case "Van Gieson stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(vanProxy)))));
                break;
            case "US.estimated from Arbuckle 1993 female singleton":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from FL.Hadlock 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Spinal cord injury medicine.team":
                break;
            case "US+Estimated from GSD":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Imm":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(immProxy)))));
                break;
            case "Sedimentation":
                break;
            case "Iron hematoxylin stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ironProxy)))));
                break;
            case "SAB":
                break;
            case "US.estimated from spine length.Tokyo 1989":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "U.S. Food Security Survey":
                break;
            case "Sevier-Munger stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(sevierMungerProxy)))));
                break;
            case "1st IRP":
                break;
            case "Acridine orange stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(acridineProxy)))));
                break;
            case "Titmus":
                break;
            case "Peroxidase stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(peroxidaseProxy)))));
                break;
            case "US+Estimated from AC.Hansmann 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Brown and Brenn stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(brownProxy)))));
                break;
            case "Screen":
                break;
            case "Gradient strip ARD":
                break;
            case "US+Estimated from AC&BPD.Warsof 1977":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Histomorphometry stain":
                break;
            case "US.2D.pulmonic valve.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "IF rat liver substrate":
                break;
            case "Addiction medicine.therapist":
                break;
            case "US.tissue doppler.A4C":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from AC.Shinozuka 1996":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Detection limit <= 1.0 ng/dL":
                break;
            case "SBT":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(sbtProxy)))));
                break;
            case "US+Estimated from AC&BPD&FL&HC":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Platelet aggr":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(plateletProxy)))));
                break;
            case "Calculated.FibroSure":
                break;
            case "Urology.nurse":
                break;
            case "Schober test":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(schoberProxy)))));
                break;
            case "Aggl.card":
                break;
            case "CDC.CS":
                break;
            case "US+Estimated from BPD.Lessoway 1998":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Complement dependent cytotoxicity":
                break;
            case "US.doppler+Calculated by PISA method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "SCB":
                break;
            case "Electrophoresis pH 8.9":
                break;
            case "US+Estimated from AC&FL.Hadlock 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Transcutaneous O2 monitor":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(transcutaneousO2Proxy)))));
                break;
            case "Estimated from date fundal height reaches umb":
                break;
            case "2D echo.visual estimate":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Night blue stain":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR, builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(nightProxy)))));
                break;
            case "US+Estimated from BPD.Sabbagha 1978":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "US+Estimated from AC&FL.Hadlock 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;            
            default:
                System.out.println("Unknown loinc method: " + loincField);
        }
    }
    public void listMethods() {
        System.out.println("Methods: " + methods);
    }
}
