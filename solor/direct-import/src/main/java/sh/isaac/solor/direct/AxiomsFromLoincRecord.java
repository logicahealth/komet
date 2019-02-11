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
                break;
            case "BPI.short":
                break;
            case "Trichrome modified":
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
                break;
            case "US.measured.ellipse overlay":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Surgical critical care":
                break;
            case "Speech therapy":
                break;
            case "Electrophoresis.agarose gel":
                break;
            case "Aggl.rivanol":
                break;
            case "Saline":
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
                break;
            case "US.3D.segmentation":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Cresyl echt violet stain":
                break;
            case "Palliative care.team":
                break;
            case "Guthrie test":
                break;
            case "BFI":
                break;
            case "Nutrition and dietetics.interdisciplinary":
                break;
            case "NAACCR":
                break;
            case "Sudan III stain":
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
                break;
            case "Bennhold stain.Putchler modified":
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
                break;
            case "Diagnostic imaging":
                break;
            case "Carbol-fuchsin stain":
                break;
            case "Vascular surgery.physician attending":
                break;
            case "Alcian blue stain.sulfated":
                break;
            case "Ivy":
                break;
            case "US+Estimated from AC.Lessoway 1998":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Oral and maxillofacial surgery":
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
                break;
            case "HHS.ACA Section 4302":
                break;
            case "US+Estimated from AC&BPD.Hadlock 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "OCT":
                break;
            case "ACC-AHA":
                break;
            case "Primary care.physician resident":
                break;
            case "Gimenez stain":
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
                break;
            case "Clinical biochemical genetics":
                break;
            case "C3b binding assay":
                break;
            case "Pediatric transplant hepatology":
                break;
            case "Neurology.nurse":
                break;
            case "US.2D.PLAX":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Antihuman globulin":
                break;
            case "Pediatric otolaryngology":
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
                break;
            case "India ink preparation":
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
                break;
            case "Neisser stain":
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
                break;
            case "Gradient strip":
                break;
            case "US+Estimated from AC&FL&HC.Ott 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Coag":
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
                break;
            case "Romanowsky stain":
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
                break;
            case "Lee White":
                break;
            case "Oncology":
                break;
            case "estimated from serum level":
                break;
            case "RIPA":
                break;
            case "CRAFFT":
                break;
            case "Hepatology":
                break;
            case "Estimated":
                break;
            case "EPDS":
                break;
            case "US.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Potassium ferrocyanide stain":
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
                break;
            case "US+Estimated from BPD.Osaka 1989":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Silver stain.Grimelius":
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
                break;
            case "Pediatric gastroenterology":
                break;
            case "4 deg C incubation":
                break;
            case 
                "Mental health.case manager":
                break;
            case "Tzanck smear":
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
                break;
            case "Aerobic culture":
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
                break;
            case "Calcofluor white preparation":
                break;
            case "Oral and maxillofacial surgery.physician resident":
                break;
            case "Anesthesiology":
                break;
            case "Verhoeff-Van Gieson stain":
                break;
            case "US.derived.HWL":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Measured":
                break;
            case "COOP.WONCA":
                break;
            case "US+Estimated from BPD.Campbell 1975":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Otolaryngology":
                break;
            case "Palliative care.physician":
                break;
            case "Womens health.nurse":
                break;
            case "XXX stain":
                break;
            case "PLCO":
                break;
            case "Urology":
                break;
            case "US.derived from OFD&O-O TD":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Based on maternal age":
                break;
            case "Mallory-Heidenhain stain":
                break;
            case "Angiography.single plane":
                break;
            case "Supravital stain":
                break;
            case "Reported.Wong-Baker FACES pain rating scale":
                break;
            case "Estimated.pop birth wgt gestational age corr.ref":
                break;
            case "Pediatric nephrology":
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
                break;
            case "Psychiatry":
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
                break;
            case "US+Estimated from HC.Hansmann 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Hematoxylin-eosin-Mayers progressive stain":
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
                break;
            case "CDI":
                break;
            case "Reported.FPS-R":
                break;
            case "CDR":
                break;
            case "Chemical pathology":
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
                break;
            case "Plastic surgery":
                break;
            case "Methenamine silver stain.Jones":
                break;
            case "US.doppler+Calculated by continuity VTI":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Dilution":
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
                break;
            case "Podiatry":
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
                break;
            case "Perimetry":
                break;
            case "Thin film":
                break;
            case "1D cold incubation":
                break;
            case "US+Estimated from HC.Jeanty 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Probe.amp.tar detection limit = 2.6 log copies/mL":
                break;
            case "Test strip":
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
                break;
            case "Coag.derived":
                break;
            case "Gastroenterology.physician attending":
                break;
            case "Audiology":
                break;
            case "Physician":
                break;
            case "ICRB":
                break;
            case "Bacterial subtyping":
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
                break;
            case "Carbon dioxide measurement":
                break;
            case "Internal medicine.pharmacist":
                break;
            case "Nottingham":
                break;
            case "Pediatrics":
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
                break;
            case "Optometry.technician":
                break;
            case "General medicine":
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
                break;
            case "Framingham.D'Agostino 1994":
                break;
            case "Flow cytometry":
                break;
            case "MDSv3":
                break;
            case "Confirm":
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
                break;
            case "Screen>50 ng/mL":
                break;
            case "Bodian stain":
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
                break;
            case "Colonoscopy.thru stoma":
                break;
            case "4H cold incubation":
                break;
            case "Prussian blue stain":
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
                break;
            case "MR.spectroscopy":
                break;
            case "Family medicine":
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
                break;
            case "US.doppler+ECG":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Pastoral care":
                break;
            case "RF.video":
                break;
            case "Estimated from abdominal circumference":
                break;
            case "Gram stain":
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
                break;
            case "Medical toxicology":
                break;
            case "US.doppler.pressure half time":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Internal medicine":
                break;
            case "Malaria smear":
                break;
            case "Clinical cardiac electrophysiology":
                break;
            case "Palliative care.physician attending":
                break;
            case "Dynamometer":
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
                break;
            case "Fouchet stain":
                break;
            case "[9,10-3H] palmitate substrate":
                break;
            case "Fite-Faraco stain":
                break;
            case "LHR":
                break;
            case "Smear":
                break;
            case "Probe.amp.tar":
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
                break;
            case "DI-PAD CGP V 1.4":
                break;
            case "HAQ":
                break;
            case "US.2D+Calculated by dimension method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Nurse":
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
                break;
            case "Primary care.physician assistant":
                break;
            case "Physician assistant":
                break;
            case "CT.perfusion":
                break;
            case "RFLP":
                break;
            case "Nile blue prusside":
                break;
            case "Heterometer test":
                break;
            case "Inspection":
                break;
            case "Neurology":
                break;
            case "US+Estimated from HC.Lessoway 1998":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Evoked potential":
                break;
            case "US+Estimated from OFD.Hansmann 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Sudan black B stain":
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
                break;
            case "US+Estimated from BD.Jeanty 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Chromo":
                break;
            case "Patient":
                break;
            case "Immunophenotyping":
                break;
            case "Clinical nurse specialist":
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
                break;
            case "Hematology+Medical oncology.nurse":
                break;
            case "US+Estimated from BD.Jeanty 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Peak flow meter":
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
                break;
            case "Trichrome stain.Masson":
                break;
            case "Calculated":
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
                break;
            case "Conglutinin assay":
                break;
            case "Glucometer":
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
                break;
            case "Vascular surgery":
                break;
            case "Diepoxybutane":
                break;
            case "Butyrate esterase stain":
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
                break;
            case "Neurology.physician attending":
                break;
            case "Branemark scale":
                break;
            case "US.M-mode+Calculated by cube method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Angiography":
                break;
            case "Obstetrics and gynecology.nurse":
                break;
            case "US+Estimated from AC&BPD&FL.Hadlock 1985":
                break;
            case "Mental health.nurse":
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
                break;
            case "US+Estimated from BPD&FTA&FL.Osaka 1990":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Pediatric rheumatology":
                break;
            case "CTAS":
                break;
            case "Aggl.cord RBC":
                break;
            case "Pulmonary disease.nurse":
                break;
            case "BS II":
                break;
            case "Angiogram":
                break;
            case "Vassar-culling stain":
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
                break;
            case "Uroflowmetry":
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
                break;
            case "Creatinine-based formula (CKD-EPI)":
                break;
            case "PEG assay":
                break;
            case "Nuclear.blood pool":
                break;
            case "Schirmer test":
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
                break;
            case "Kinyoun iron hematoxylin stain":
                break;
            case "Airway pressure monitor":
                break;
            case "Lauren classification":
                break;
            case "Radiation oncology":
                break;
            case "4-MU-palmitate substrate":
                break;
            case "HA":
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
                break;
            case "IB":
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
                break;
            case "OPTIMAL":
                break;
            case "Pediatric cardiology":
                break;
            case "Coag inverse ratio":
                break;
            case "Prewarmed":
                break;
            case "Preventive medicine.nurse":
                break;
            case "Tartrate-resistant acid phosphatase stain":
                break;
            case "Orcein stain":
                break;
            case "Warthin-Starry stain":
                break;
            case "Angiography.biplane":
                break;
            case "Framingham.Wilson 1998":
                break;
            case "RF.angio":
                break;
            case "Thoracic and cardiac surgery":
                break;
            case "Albumin technique":
                break;
            case "Sequencing":
                break;
            case "Kleihauer-Betke":
                break;
            case "KM":
                break;
            case "NCFS":
                break;
            case "Giemsa stain.May-Grunwald":
                break;
            case "Electromyogram":
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
                break;
            case "HAQ-DI":
                break;
            case "Churukian-Schenk stain":
                break;
            case "Kinesiotherapy":
                break;
            case "Fluorescent polarization assay":
                break;
            case "LA":
                break;
            case "Geriatric medicine":
                break;
            case "Social service":
                break;
            case "Refractometry":
                break;
            case "Phenotyping":
                break;
            case "Periodic acid-Schiff stain":
                break;
            case "Wayson stain":
                break;
            case "Lap":
                break;
            case "Macchiavello stain":
                break;
            case "Cover test":
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
                break;
            case "CDC.PHIN":
                break;
            case "Molgen":
                break;
            case "US+Estimated from BPD.Hadlock 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "MG":
                break;
            case "US+Estimated from BPD.Hadlock 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Ophthalmology":
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
                break;
            case "Cardiac catheterization":
                break;
            case "Epilepsy":
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
                break;
            case "Giemsa stain.3 micron":
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
                break;
            case "US+Estimated from FL.Hansmann 1986":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "ERCP":
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
                break;
            case "AUDADIS-IV":
                break;
            case "Psychology":
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
                break;
            case "MIC":
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
                break;
            case "Carbapenemase Nordmann-Poirel":
                break;
            case "CPIC":
                break;
            case "Wright stain":
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
                break;
            case "Transplant surgery":
                break;
            case "CT.scanogram":
                break;
            case "Acridine orange and Giemsa stain":
                break;
            case "Child and adolescent psychiatry":
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
                break;
            case "Brain injury":
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
                break;
            case "Brilliant cresyl blue":
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
                break;
            case "Rheumatology":
                break;
            case "Pediatric critical care medicine":
                break;
            case "Per age":
                break;
            case "gender and height":
                break;
            case "PAGE":
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
                break;
            case "Endocrinology":
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
                break;
            case "US+Estimated from AC&BPD.Eik-Nes 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Anoscopy":
                break;
            case "US+Estimated from CRL.Jeanty 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Nurse practitioner":
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
                break;
            case "US.estimated from Hadlock 1984":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Sudan black stain":
                break;
            case "US.estimated from Hadlock 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Dermatology.nurse practitioner":
                break;
            case "Gomori stain":
                break;
            case "TIMP":
                break;
            case "VAP":
                break;
            case "Chest xray.calculated":
                break;
            case "Manual":
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
                break;
            case "US+Estimated from FL.Merz 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Invasive":
                break;
            case "US.2D.mod.single-plane ellipse.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "CT.angio":
                break;
            case "Alcian blue stain.with periodic acid-Schiff":
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
                break;
            case "Amniocentesis":
                break;
            case "Auscultation":
                break;
            case "Sudan IV stain":
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
                break;
            case "US+Estimated from AD&BPD.Rose 1987":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "VEP":
                break;
            case "Per age and gender":
                break;
            case "NHCS":
                break;
            case "Obstetrics and Gynecology":
                break;
            case "Estimated from selected delivery date":
                break;
            case "Flexible sigmoidoscopy":
                break;
            case "Screen>25 ng/mL":
                break;
            case "[9,10-3H] myristate substrate":
                break;
            case "Culture":
                break;
            case "Rhodamine stain":
                break;
            case "Jefferies":
                break;
            case "US standard certificate of death":
                break;
            case "EEG":
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
                break;
            case "Visual":
                break;
            case "Palliative care.physician resident":
                break;
            case "Gynecologic oncology":
                break;
            case "Chiropractic":
                break;
            case "EGD":
                break;
            case "Bioassay":
                break;
            case "Maddox double prism test":
                break;
            case "Mental health.team":
                break;
            case "KCCQ":
                break;
            case "Mucicarmine stain":
                break;
            case "Coag.two stage":
                break;
            case "Ophthalmoscopy":
                break;
            case "Pediatric urology":
                break;
            case "Manual count":
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
                break;
            case "High resolution":
                break;
            case "Cardiovascular disease":
                break;
            case "Acid fast stain.Kinyoun":
                break;
            case "Phoropter":
                break;
            case "Thoracic and cardiac surgery.medical student":
                break;
            case "Methenamine silver stain.Grocott":
                break;
            case "Viral subtyping":
                break;
            case "Pathologist comment":
                break;
            case "HL7.VMR-CDS":
                break;
            case "Pulse oximetry":
                break;
            case "28 deg C incubation":
                break;
            case "Critical care medicine.physician attending":
                break;
            case "Framingham.The Adult Treatment Panel III 2001":
                break;
            case "NYHA":
                break;
            case "Pediatric surgery":
                break;
            case "Emergency medicine":
                break;
            case "Estimated from FL.Merz 1988":
                break;
            case "Fungal subtyping":
                break;
            case "Confrontation":
                break;
            case "BIMS":
                break;
            case "Endocrinology.physician attending":
                break;
            case "Impedance.transthoracic":
                break;
            case "Impedance":
                break;
            case "NHIS":
                break;
            case "Infrared spectroscopy":
                break;
            case "Reported.PHQ-9.CMS":
                break;
            case "Thoracic and cardiac surgery.interdisciplinary":
                break;
            case "Calculated from oxygen partial pressure":
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
                break;
            case "CDAI":
                break;
            case "ISE":
                break;
            case "Physical therapy":
                break;
            case "Medical student":
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
                break;
            case "Aerospace medicine":
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
                break;
            case "Neutral red stain":
                break;
            case "Detection limit <= 0.05 mIU/L":
                break;
            case "ACT":
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
                break;
            case "US.doppler.velocity+Diameter.calculated":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "LIBCSP":
                break;
            case "Recreational therapy":
                break;
            case "Spinal cord injury medicine.nurse":
                break;
            case "Lawson-Van Gieson stain":
                break;
            case "Tape measure":
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
                break;
            case "UCLA Loneliness Scale v3":
                break;
            case "US.2D.mod.biplane":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Wade stain":
                break;
            case "Immobilization":
                break;
            case "Physician resident":
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
                break;
            case "Cardiac surgery":
                break;
            case "Hematoxylin-eosin-Harris regressive stain":
                break;
            case "FEAS":
                break;
            case "Surgery":
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
                break;
            case "ESI":
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
                break;
            case "Obstetrics and gynecology.physician attending":
                break;
            case "1-14C-pyruvate substrate":
                break;
            case "PFGE":
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
                break;
            case "US.estimated from Chitty 1994":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "HL7.v3":
                break;
            case "Occupational medicine":
                break;
            case "2D echo":
                break;
            case "Exercise stress test":
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
                break;
            case "Infectious disease":
                break;
            case "NHL":
                break;
            case "RPR":
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
                break;
            case "NSLAH":
                break;
            case "Methenamine silver nitrate stain":
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
                break;
            case "Geriatric medicine.team":
                break;
            case "Allergy and immunology":
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
                break;
            case "Birth defects":
                break;
            case "Cardiovascular disease.nurse practitioner":
                break;
            case "Dentistry":
                break;
            case "NeuroQol.Peds":
                break;
            case "Observed.CCC":
                break;
            case "Enteroscopy":
                break;
            case "Hematology+Medical oncology.physician attending":
                break;
            case "US+Estimated from FL.Chitty 1997":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Banding":
                break;
            case "Endocrinology.physician fellow":
                break;
            case "Observed.OMAHA":
                break;
            case "Screen>150 ng/mL":
                break;
            case "Tonometry.non contact":
                break;
            case "FISH":
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
                break;
            case "Rapid":
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
                break;
            case "Dye test":
                break;
            case "Aggl.serial ring test":
                break;
            case "Tonometry":
                break;
            case "C3d binding assay":
                break;
            case "Thromboelastography":
                break;
            case "Internal medicine.physician attending":
                break;
            case "Respiratory culture":
                break;
            case "Giemsa stain":
                break;
            case "Dermatology":
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
                break;
            case "Terminal deoxynucleotidyl transferase stain":
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
                break;
            case "Toluidine blue O stain":
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
                break;
            case "MAST":
                break;
            case "Probe.amp.tar detection limit = 400 copies/mL":
                break;
            case "Photometric":
                break;
            case "Isoelectric focusing":
                break;
            case "US.2D+Calculated by Devereux method":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Direct assay":
                break;
            case "Radiology.nurse":
                break;
            case "Estimated from gestational age":
                break;
            case "Microscopy":
                break;
            case "Clinical pharmacology":
                break;
            case "Environmental culture":
                break;
            case "US+Estimated from FL.Obrien 1982":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Anaerobic culture 25 deg C incubation":
                break;
            case "Silver stain":
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
                break;
            case "Luxol fast blue/Periodic acid-Schiff stain":
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
                break;
            case "Sedimentation":
                break;
            case "Iron hematoxylin stain":
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
                break;
            case "1st IRP":
                break;
            case "Acridine orange stain":
                break;
            case "Titmus":
                break;
            case "Peroxidase stain":
                break;
            case "US+Estimated from AC.Hansmann 1985":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Brown and Brenn stain":
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
                break;
            case "US+Estimated from AC&BPD&FL&HC":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Platelet aggr":
                break;
            case "Calculated.FibroSure":
                break;
            case "Urology.nurse":
                break;
            case "Schober test":
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
                break;
            case "Estimated from date fundal height reaches umb":
                break;
            case "2D echo.visual estimate":
                assertions.add(builder.someRole(MetaData.ROLE_GROUP____SOLOR,
                        builder.and(builder.someRole(methodProxy.getNid(), builder.conceptAssertion(ultrasoundProxy)))));
                break;
            case "Night blue stain":
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
