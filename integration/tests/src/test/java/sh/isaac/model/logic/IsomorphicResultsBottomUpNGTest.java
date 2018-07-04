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
package sh.isaac.model.logic;


import java.io.File;
import java.util.prefs.BackingStoreException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.constants.SystemPropertyConstants;
import sh.isaac.api.logic.LogicalExpression;
import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.SomeRole;
import static sh.isaac.api.logic.LogicalExpressionBuilder.SufficientSet;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.preferences.PreferencesService;
import sh.isaac.api.util.RecursiveDelete;
import sh.isaac.komet.preferences.UserConfigurationPerOSProvider;
import sh.isaac.model.logic.definition.LogicalExpressionBuilderImpl;

/**
 *
 * @author kec
 */
@Test(suiteName = "isomorphic-suite")
public class IsomorphicResultsBottomUpNGTest {

	private static final Logger LOG = LogManager.getLogger();

	@BeforeClass
	public void configure() throws Exception {
		LOG.info("isomorphic-suite setup");
		File db = new File("target/isomorphic-suite");
		RecursiveDelete.delete(db);
		//Don't overwrite "real" config
		UserConfigurationPerOSProvider.nodeName = "userConfigForTest";
		db.mkdirs();
		System.setProperty(SystemPropertyConstants.DATA_STORE_ROOT_LOCATION_PROPERTY, db.getCanonicalPath());
		LookupService.startupPreferenceProvider();
		//Make sure remnants from any previous test are gone
		IsaacPreferences mainDataStore = Get.service(PreferencesService.class).getUserPreferences();
		mainDataStore.node(UserConfigurationPerOSProvider.nodeName).removeNode();
		Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);
		LookupService.startupIsaac();
	}

	@AfterClass
	public void shutdown() throws BackingStoreException {
		LOG.info("isomorphic-suite teardown");
		//cleanup
		IsaacPreferences mainDataStore = Get.service(PreferencesService.class).getUserPreferences();
		mainDataStore.node(UserConfigurationPerOSProvider.nodeName).removeNode();
		LookupService.shutdownSystem();
	}
    
    LogicalExpression referenceExpression = makeReferenceExpression();
    LogicalExpression comparisonExpression = makeComparisonExpression();

    public IsomorphicResultsBottomUpNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    public static LogicalExpression makeReferenceExpression() {
        LogicalExpressionBuilderImpl leb = new LogicalExpressionBuilderImpl();
//Root[152]➞[151]
//    Sufficient[151]➞[150]
SufficientSet(
//        And[150]➞[3, 7, 11, 15, 19, 23, 27, 33, 39, 43, 47, 55, 65, 69, 75, 81, 87, 95, 105, 115, 127, 143, 144, 145, 146, 147, 148, 149]
        And(
//            Some[3] Role group (SOLOR) <-2147483593>➞[2]
//                And[2]➞[1]
//                    Some[1] Direct morphology (attribute) <-2147353023>➞[0]
//                        Concept[0] Morphologically abnormal structure (morphologic abnormality) <-2146664933>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147353023,  
                          ConceptAssertion(-2146664933, leb)
                  ))), // and SomeRole
//            Some[7] Role group (SOLOR) <-2147483593>➞[6]
//                And[6]➞[5]
//                    Some[5] Approach (attribute) <-2147269307>➞[4]
//                        Concept[4] Transurethral approach (qualifier value) <-2146565172>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147269307,  
                          ConceptAssertion(-2146565172, leb)
                  ))), // and SomeRole
//            Some[11] Role group (SOLOR) <-2147483593>➞[10]
//                And[10]➞[9]
//                    Some[9] Procedure site (attribute) <-2147352955>➞[8]
//                        Concept[8] Urethral structure (body structure) <-2147262945>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147262945, leb)
                  ))), // and SomeRole
//            Some[15] Role group (SOLOR) <-2147483593>➞[14]
//                And[14]➞[13]
//                    Some[13] Instrumentation (attribute) <-2146371706>➞[12]
//                        Concept[12] Flexible cystoscope (physical object) <-2147325734>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2146371706,  
                          ConceptAssertion(-2147325734, leb)
                  ))), // and SomeRole
//            Some[19] Role group (SOLOR) <-2147483593>➞[18]
//                And[18]➞[17]
//                    Some[17] Access (attribute) <-2147271862>➞[16]
//                        Concept[16] Endoscopic approach - access (qualifier value) <-2146797373>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147271862,  
                          ConceptAssertion(-2146797373, leb)
                  ))), // and SomeRole
//            Some[23] Role group (SOLOR) <-2147483593>➞[22]
//                And[22]➞[21]
//                    Some[21] Has intent (attribute) <-2147352973>➞[20]
//                        Concept[20] Diagnostic intent (qualifier value) <-2147261952>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352973,  
                          ConceptAssertion(-2147261952, leb)
                  ))), // and SomeRole
//            Some[27] Role group (SOLOR) <-2147483593>➞[26]
//                And[26]➞[25]
//                    Some[25] Method (attribute) <-2147268827>➞[24]
//                        Concept[24] Biopsy - action (qualifier value) <-2146796171>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146796171, leb)
                  ))), // and SomeRole
//            Some[33] Role group (SOLOR) <-2147483593>➞[32]
//                And[32]➞[29, 31]
//                    Some[29] Procedure site (attribute) <-2147352955>➞[28]
//                        Concept[28] Urinary bladder structure (body structure) <-2147407049>
//                    Some[31] Method (attribute) <-2147268827>➞[30]
//                        Concept[30] Excision - action (qualifier value) <-2146796310>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147407049, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146796310, leb))
                  )), // and SomeRole
//            Some[39] Role group (SOLOR) <-2147483593>➞[38]
//                And[38]➞[35, 37]
//                    Some[35] Procedure site (attribute) <-2147352955>➞[34]
//                        Concept[34] Urinary bladder structure (body structure) <-2147407049>
//                    Some[37] Method (attribute) <-2147268827>➞[36]
//                        Concept[36] Destruction - action (qualifier value) <-2146795225>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147407049, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146795225, leb))
                  )), // and SomeRole
//            Some[43] Role group (SOLOR) <-2147483593>➞[42]
//                And[42]➞[41]
//                    Some[41] Using (attribute) <-2147252899>➞[40]
//                        Concept[40] Endoscope, device (physical object) <-2146855088>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147252899,  
                          ConceptAssertion(-2146855088, leb)
                  ))), // and SomeRole
//            Some[47] Role group (SOLOR) <-2147483593>➞[46]
//                And[46]➞[45]
//                    Some[45] Access instrument (attribute) <-2147225381>➞[44]
//                        Concept[44] Flexible cystoscope (physical object) <-2147325734>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147225381,  
                          ConceptAssertion(-2147325734, leb)
                  ))), // and SomeRole
//            Some[55] Role group (SOLOR) <-2147483593>➞[54]
//                And[54]➞[49, 51, 53]
//                    Some[49] Procedure site (attribute) <-2147352955>➞[48]
//                        Concept[48] Urinary bladder structure (body structure) <-2147407049>
//                    Some[51] Method (attribute) <-2147268827>➞[50]
//                        Concept[50] Excision - action (qualifier value) <-2146796310>
//                    Some[53] Method (attribute) <-2147268827>➞[52]
//                        Concept[52] Endoscopic inspection - action (qualifier value) <-2146795774>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147407049, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146796310, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146795774, leb))
                  )), // and SomeRole
//            Some[65] Role group (SOLOR) <-2147483593>➞[64]
//                And[64]➞[57, 59, 61, 63]
//                    Some[57] Procedure site (attribute) <-2147352955>➞[56]
//                        Concept[56] Urethral structure (body structure) <-2147262945>
//                    Some[59] Procedure site (attribute) <-2147352955>➞[58]
//                        Concept[58] Urinary bladder structure (body structure) <-2147407049>
//                    Some[61] Method (attribute) <-2147268827>➞[60]
//                        Concept[60] Destruction - action (qualifier value) <-2146795225>
//                    Some[63] Method (attribute) <-2147268827>➞[62]
//                        Concept[62] Endoscopic inspection - action (qualifier value) <-2146795774>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147262945, leb)),
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147407049, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146795225, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146795774, leb))
                  )), // and SomeRole
//            Some[69] Role group (SOLOR) <-2147483593>➞[68]
//                And[68]➞[67]
//                    Some[67] Direct device (attribute) <-2147353038>➞[66]
//                        Concept[66] Endoscope, device (physical object) <-2146855088>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147353038,  
                          ConceptAssertion(-2146855088, leb)
                  ))), // and SomeRole
//            Some[75] Role group (SOLOR) <-2147483593>➞[74]
//                And[74]➞[71, 73]
//                    Some[71] Procedure site (attribute) <-2147352955>➞[70]
//                        Concept[70] Urinary bladder structure (body structure) <-2147407049>
//                    Some[73] Method (attribute) <-2147268827>➞[72]
//                        Concept[72] Inspection - action (qualifier value) <-2146794608>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147407049, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146794608, leb))
                  )), // and SomeRole
//            Some[81] Role group (SOLOR) <-2147483593>➞[80]
//                And[80]➞[77, 79]
//                    Some[77] Procedure site (attribute) <-2147352955>➞[76]
//                        Concept[76] Urethral structure (body structure) <-2147262945>
//                    Some[79] Method (attribute) <-2147268827>➞[78]
//                        Concept[78] Inspection - action (qualifier value) <-2146794608>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147262945, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146794608, leb))
                  )), // and SomeRole
//            Some[87] Role group (SOLOR) <-2147483593>➞[86]
//                And[86]➞[83, 85]
//                    Some[83] Procedure site (attribute) <-2147352955>➞[82]
//                        Concept[82] Urinary bladder structure (body structure) <-2147407049>
//                    Some[85] Method (attribute) <-2147268827>➞[84]
//                        Concept[84] Biopsy - action (qualifier value) <-2146796171>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147407049, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146796171, leb))
                  )), // and SomeRole
//            Some[95] Role group (SOLOR) <-2147483593>➞[94]
//                And[94]➞[89, 91, 93]
//                    Some[89] Access (attribute) <-2147271862>➞[88]
//                        Concept[88] Endoscopic approach - access (qualifier value) <-2146797373>
//                    Some[91] Approach (attribute) <-2147269307>➞[90]
//                        Concept[90] Transurethral approach (qualifier value) <-2146565172>
//                    Some[93] Access instrument (attribute) <-2147225381>➞[92]
//                        Concept[92] Cystoscope, device (physical object) <-2147149073>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147271862,  
                          ConceptAssertion(-2146797373, leb)),
                      SomeRole(-2147269307,  
                          ConceptAssertion(-2146565172, leb)),
                      SomeRole(-2147225381,  
                          ConceptAssertion(-2147149073, leb))
                  )), // and SomeRole
//            Some[105] Role group (SOLOR) <-2147483593>➞[104]
//                And[104]➞[97, 99, 101, 103]
//                    Some[97] Procedure site (attribute) <-2147352955>➞[96]
//                        Concept[96] Urethral structure (body structure) <-2147262945>
//                    Some[99] Access (attribute) <-2147271862>➞[98]
//                        Concept[98] Endoscopic approach - access (qualifier value) <-2146797373>
//                    Some[101] Method (attribute) <-2147268827>➞[100]
//                        Concept[100] Inspection - action (qualifier value) <-2146794608>
//                    Some[103] Access instrument (attribute) <-2147225381>➞[102]
//                        Concept[102] Endoscope, device (physical object) <-2146855088>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147262945, leb)),
                      SomeRole(-2147271862,  
                          ConceptAssertion(-2146797373, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146794608, leb)),
                      SomeRole(-2147225381,  
                          ConceptAssertion(-2146855088, leb))
                  )), // and SomeRole
//            Some[115] Role group (SOLOR) <-2147483593>➞[114]
//                And[114]➞[107, 109, 111, 113]
//                    Some[107] Procedure site (attribute) <-2147352955>➞[106]
//                        Concept[106] Urinary bladder structure (body structure) <-2147407049>
//                    Some[109] Access (attribute) <-2147271862>➞[108]
//                        Concept[108] Endoscopic approach - access (qualifier value) <-2146797373>
//                    Some[111] Method (attribute) <-2147268827>➞[110]
//                        Concept[110] Inspection - action (qualifier value) <-2146794608>
//                    Some[113] Access instrument (attribute) <-2147225381>➞[112]
//                        Concept[112] Cystoscope, device (physical object) <-2147149073>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147407049, leb)),
                      SomeRole(-2147271862,  
                          ConceptAssertion(-2146797373, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146794608, leb)),
                      SomeRole(-2147225381,  
                          ConceptAssertion(-2147149073, leb)),
                      SomeRole(-2146724957,  
                          ConceptAssertion(-2147407049, leb))
                  )), // and SomeRole
//            Some[127] Role group (SOLOR) <-2147483593>➞[126]
//                And[126]➞[117, 119, 121, 123, 125]
//                    Some[117] Direct morphology (attribute) <-2147353023>➞[116]
//                        Concept[116] Morphologically abnormal structure (morphologic abnormality) <-2146664933>
//                    Some[119] Access (attribute) <-2147271862>➞[118]
//                        Concept[118] Endoscopic approach - access (qualifier value) <-2146797373>
//                    Some[121] Method (attribute) <-2147268827>➞[120]
//                        Concept[120] Excision - action (qualifier value) <-2146796310>
//                    Some[123] Access instrument (attribute) <-2147225381>➞[122]
//                        Concept[122] Flexible cystoscope (physical object) <-2147325734>
//                    Some[125] Procedure site - Indirect (attribute) <-2146724957>➞[124]
//                        Concept[124] Urinary bladder structure (body structure) <-2147407049>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147353023,  
                          ConceptAssertion(-2146664933, leb)),
                      SomeRole(-2147271862,  
                          ConceptAssertion(-2146797373, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146796310, leb)),
                      SomeRole(-2147225381,  
                          ConceptAssertion(-2147325734, leb)),
                      SomeRole(-2146724957,  
                          ConceptAssertion(-2147407049, leb))
                  )), // and SomeRole
//            Some[143] Role group (SOLOR) <-2147483593>➞[142]
//                And[142]➞[129, 131, 133, 135, 137, 139, 141]
//                    Some[129] Direct morphology (attribute) <-2147353023>➞[128]
//                        Concept[128] Morphologically abnormal structure (morphologic abnormality) <-2146664933>
//                    Some[131] Access (attribute) <-2147271862>➞[130]
//                        Concept[130] Endoscopic approach - access (qualifier value) <-2146797373>
//                    Some[133] Approach (attribute) <-2147269307>➞[132]
//                        Concept[132] Transurethral approach (qualifier value) <-2146565172>
//                    Some[135] Method (attribute) <-2147268827>➞[134]
//                        Concept[134] Inspection - action (qualifier value) <-2146794608>
//                    Some[137] Method (attribute) <-2147268827>➞[136]
//                        Concept[136] Excision - action (qualifier value) <-2146796310>
//                    Some[139] Access instrument (attribute) <-2147225381>➞[138]
//                        Concept[138] Cystoscope, device (physical object) <-2147149073>
//                    Some[141] Procedure site - Indirect (attribute) <-2146724957>➞[140]
//                        Concept[140] Urinary bladder structure (body structure) <-2147407049>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147353023,  
                          ConceptAssertion(-2146664933, leb)),
                      SomeRole(-2147271862,  
                          ConceptAssertion(-2146797373, leb)),
                      SomeRole(-2147269307,  
                          ConceptAssertion(-2146565172, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146794608, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146796310, leb)),
                      SomeRole(-2146724957,  
                          ConceptAssertion(-2147407049, leb))
                  )), // and SomeRole
//            Concept[144] Endoscopy with surgical procedure (procedure) <-2147473793>
                 ConceptAssertion(-2147473793, leb),
//            Concept[145] Cystourethroscopy with biopsy of bladder (procedure) <-2147056005>
                ConceptAssertion(-2147056005, leb),
//            Concept[146] Cystoscopy and transurethral resection of bladder lesion (procedure) <-2147036672>
                ConceptAssertion(-2147036672, leb),
//            Concept[147] Transurethral bladder excision (procedure) <-2146767318>
                ConceptAssertion(-2146767318, leb),
//            Concept[149] Cystoscopy and transurethral resection of bladder lesion (procedure) <-2146337238>
                ConceptAssertion(-2146337238, leb)
           )// AND
        ); // SUFFICIENT SET
        
        
	return  leb.build();
    }

    public static LogicalExpression makeComparisonExpression() {
        LogicalExpressionBuilderImpl leb = new LogicalExpressionBuilderImpl();
//Root[152]➞[151]
//    Sufficient[151]➞[150]
SufficientSet(
//        And[150]➞[3, 7, 11, 15, 19, 23, 27, 33, 39, 43, 47, 55, 65, 69, 75, 81, 87, 95, 105, 115, 127, 143, 144, 145, 146, 147, 148, 149]
        And(//
//            Some[3] Role group (SOLOR) <-2147483593>➞[2]
//                And[2]➞[1]
//                    Some[1] Direct morphology (attribute) <-2147353023>➞[0]
//                        Concept[0] Morphologically abnormal structure (morphologic abnormality) <-2146664933>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147353023,  
                          ConceptAssertion(-2146664933, leb))
                  )),
                   // and SomeRole
//            Some[7] Role group (SOLOR) <-2147483593>➞[6]
//                And[6]➞[5]
//                    Some[5] Approach (attribute) <-2147269307>➞[4]
//                        Concept[4] Transurethral approach (qualifier value) <-2146565172>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147269307,  
                          ConceptAssertion(-2146565172, leb))
                  )),
//            Some[11] Role group (SOLOR) <-2147483593>➞[10]
//                And[10]➞[9]
//                    Some[9] Procedure site (attribute) <-2147352955>➞[8]
//                        Concept[8] Urethral structure (body structure) <-2147262945>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147262945, leb))
                  )),
//            Some[15] Role group (SOLOR) <-2147483593>➞[14]
//                And[14]➞[13]
//                    Some[13] Instrumentation (attribute) <-2146371706>➞[12]
//                        Concept[12] Flexible cystoscope (physical object) <-2147325734>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2146371706,  
                          ConceptAssertion(-2147325734, leb))
                  )),
//            Some[19] Role group (SOLOR) <-2147483593>➞[18]
//                And[18]➞[17]
//                    Some[17] Access (attribute) <-2147271862>➞[16]
//                        Concept[16] Endoscopic approach - access (qualifier value) <-2146797373>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147271862,  
                          ConceptAssertion(-2146797373, leb))
                  )),
//            Some[23] Role group (SOLOR) <-2147483593>➞[22]
//                And[22]➞[21]
//                    Some[21] Has intent (attribute) <-2147352973>➞[20]
//                        Concept[20] Diagnostic intent (qualifier value) <-2147261952>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352973,  
                          ConceptAssertion(-2147261952, leb))
                  )),
//            Some[27] Role group (SOLOR) <-2147483593>➞[26]
//                And[26]➞[25]
//                    Some[25] Method (attribute) <-2147268827>➞[24]
//                        Concept[24] Biopsy - action (qualifier value) <-2146796171>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146796171, leb))
                  )),
//            Some[33] Role group (SOLOR) <-2147483593>➞[32]
//                And[32]➞[29, 31]
//                    Some[29] Procedure site (attribute) <-2147352955>➞[28]
//                        Concept[28] Urinary bladder structure (body structure) <-2147407049>
//                    Some[31] Method (attribute) <-2147268827>➞[30]
//                        Concept[30] Excision - action (qualifier value) <-2146796310>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147407049, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146796310, leb))
                  )),
//            Some[39] Role group (SOLOR) <-2147483593>➞[38]
//                And[38]➞[35, 37]
//                    Some[35] Procedure site (attribute) <-2147352955>➞[34]
//                        Concept[34] Urinary bladder structure (body structure) <-2147407049>
//                    Some[37] Method (attribute) <-2147268827>➞[36]
//                        Concept[36] Destruction - action (qualifier value) <-2146795225>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147407049, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146795225, leb))
                  )),
//            Some[43] Role group (SOLOR) <-2147483593>➞[42]
//                And[42]➞[41]
//                    Some[41] Using (attribute) <-2147252899>➞[40]
//                        Concept[40] Endoscope, device (physical object) <-2146855088>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147252899,  
                          ConceptAssertion(-2146855088, leb))
                  )),
//            Some[47] Role group (SOLOR) <-2147483593>➞[46]
//                And[46]➞[45]
//                    Some[45] Access instrument (attribute) <-2147225381>➞[44]
//                        Concept[44] Flexible cystoscope (physical object) <-2147325734>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147225381,  
                          ConceptAssertion(-2147325734, leb))
                  )),
//            Some[55] Role group (SOLOR) <-2147483593>➞[54]
//                And[54]➞[49, 51, 53]
//                    Some[49] Procedure site (attribute) <-2147352955>➞[48]
//                        Concept[48] Urinary bladder structure (body structure) <-2147407049>
//                    Some[51] Method (attribute) <-2147268827>➞[50]
//                        Concept[50] Excision - action (qualifier value) <-2146796310>
//                    Some[53] Method (attribute) <-2147268827>➞[52]
//                        Concept[52] Endoscopic inspection - action (qualifier value) <-2146795774>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147407049, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146796310, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146795774, leb))
                  )),
//            Some[65] Role group (SOLOR) <-2147483593>➞[64]
//                And[64]➞[57, 59, 61, 63]
//                    Some[57] Procedure site (attribute) <-2147352955>➞[56]
//                        Concept[56] Urethral structure (body structure) <-2147262945>
//                    Some[59] Procedure site (attribute) <-2147352955>➞[58]
//                        Concept[58] Urinary bladder structure (body structure) <-2147407049>
//                    Some[61] Method (attribute) <-2147268827>➞[60]
//                        Concept[60] Destruction - action (qualifier value) <-2146795225>
//                    Some[63] Method (attribute) <-2147268827>➞[62]
//                        Concept[62] Endoscopic inspection - action (qualifier value) <-2146795774>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147262945, leb)),
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147407049, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146795225, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146795774, leb))
                  )),
//            Some[69] Role group (SOLOR) <-2147483593>➞[68]
//                And[68]➞[67]
//                    Some[67] Direct device (attribute) <-2147353038>➞[66]
//                        Concept[66] Endoscope, device (physical object) <-2146855088>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147353038,  
                          ConceptAssertion(-2146855088, leb))
                  )),
//            Some[75] Role group (SOLOR) <-2147483593>➞[74]
//                And[74]➞[71, 73]
//                    Some[71] Procedure site (attribute) <-2147352955>➞[70]
//                        Concept[70] Urinary bladder structure (body structure) <-2147407049>
//                    Some[73] Method (attribute) <-2147268827>➞[72]
//                        Concept[72] Inspection - action (qualifier value) <-2146794608>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147407049, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146794608, leb))
                  )),
//            Some[81] Role group (SOLOR) <-2147483593>➞[80]
//                And[80]➞[77, 79]
//                    Some[77] Procedure site (attribute) <-2147352955>➞[76]
//                        Concept[76] Urethral structure (body structure) <-2147262945>
//                    Some[79] Method (attribute) <-2147268827>➞[78]
//                        Concept[78] Inspection - action (qualifier value) <-2146794608>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147262945, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146794608, leb))
                  )),
//            Some[87] Role group (SOLOR) <-2147483593>➞[86]
//                And[86]➞[83, 85]
//                    Some[83] Procedure site (attribute) <-2147352955>➞[82]
//                        Concept[82] Urinary bladder structure (body structure) <-2147407049>
//                    Some[85] Method (attribute) <-2147268827>➞[84]
//                        Concept[84] Biopsy - action (qualifier value) <-2146796171>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147407049, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146796171, leb))
                  )),
//            Some[95] Role group (SOLOR) <-2147483593>➞[94]
//                And[94]➞[89, 91, 93]
//                    Some[89] Access (attribute) <-2147271862>➞[88]
//                        Concept[88] Endoscopic approach - access (qualifier value) <-2146797373>
//                    Some[91] Approach (attribute) <-2147269307>➞[90]
//                        Concept[90] Transurethral approach (qualifier value) <-2146565172>
//                    Some[93] Access instrument (attribute) <-2147225381>➞[92]
//                        Concept[92] Cystoscope, device (physical object) <-2147149073>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147271862,  
                          ConceptAssertion(-2146797373, leb)),
                      SomeRole(-2147269307,  
                          ConceptAssertion(-2146565172, leb)),
                      SomeRole(-2147225381,  
                          ConceptAssertion(-2147149073, leb))
                  )),
//            Some[105] Role group (SOLOR) <-2147483593>➞[104]
//                And[104]➞[97, 99, 101, 103]
//                    Some[97] Procedure site (attribute) <-2147352955>➞[96]
//                        Concept[96] Urethral structure (body structure) <-2147262945>
//                    Some[99] Access (attribute) <-2147271862>➞[98]
//                        Concept[98] Endoscopic approach - access (qualifier value) <-2146797373>
//                    Some[101] Method (attribute) <-2147268827>➞[100]
//                        Concept[100] Inspection - action (qualifier value) <-2146794608>
//                    Some[103] Access instrument (attribute) <-2147225381>➞[102]
//                        Concept[102] Endoscope, device (physical object) <-2146855088>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147262945, leb)),
                      SomeRole(-2147271862,  
                          ConceptAssertion(-2146797373, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146794608, leb)),
                      SomeRole(-2147225381,  
                          ConceptAssertion(-2146855088, leb))
                  )),
//            Some[115] Role group (SOLOR) <-2147483593>➞[114]
//                And[114]➞[107, 109, 111, 113]
//                    Some[107] Procedure site (attribute) <-2147352955>➞[106]
//                        Concept[106] Urinary bladder structure (body structure) <-2147407049>
//                    Some[109] Access (attribute) <-2147271862>➞[108]
//                        Concept[108] Endoscopic approach - access (qualifier value) <-2146797373>
//                    Some[111] Method (attribute) <-2147268827>➞[110]
//                        Concept[110] Inspection - action (qualifier value) <-2146794608>
//                    Some[113] Access instrument (attribute) <-2147225381>➞[112]
//                        Concept[112] Cystoscope, device (physical object) <-2147149073>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147352955,  
                          ConceptAssertion(-2147407049, leb)),
                      SomeRole(-2147271862,  
                          ConceptAssertion(-2146797373, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146794608, leb)),
                      SomeRole(-2147225381,  
                          ConceptAssertion(-2147149073, leb))
                  )),
//            Some[127] Role group (SOLOR) <-2147483593>➞[126]
//                And[126]➞[117, 119, 121, 123, 125]
//                    Some[117] Direct morphology (attribute) <-2147353023>➞[116]
//                        Concept[116] Morphologically abnormal structure (morphologic abnormality) <-2146664933>
//                    Some[119] Access (attribute) <-2147271862>➞[118]
//                        Concept[118] Endoscopic approach - access (qualifier value) <-2146797373>
//                    Some[121] Method (attribute) <-2147268827>➞[120]
//                        Concept[120] Excision - action (qualifier value) <-2146796310>
//                    Some[123] Access instrument (attribute) <-2147225381>➞[122]
//                        Concept[122] Flexible cystoscope (physical object) <-2147325734>
//                    Some[125] Procedure site - Indirect (attribute) <-2146724957>➞[124]
//                        Concept[124] Urinary bladder structure (body structure) <-2147407049>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147353023,  
                          ConceptAssertion(-2146664933, leb)),
                      SomeRole(-2147271862,  
                          ConceptAssertion(-2146797373, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146796310, leb)),
                      SomeRole(-2147225381,  
                          ConceptAssertion(-2147325734, leb)),
                      SomeRole(-2146724957,  
                          ConceptAssertion(-2147407049, leb))
                  )),
//            Some[143] Role group (SOLOR) <-2147483593>➞[142]
//                And[142]➞[129, 131, 133, 135, 137, 139, 141]
//                    Some[129] Direct morphology (attribute) <-2147353023>➞[128]
//                        Concept[128] Morphologically abnormal structure (morphologic abnormality) <-2146664933>
//                    Some[131] Access (attribute) <-2147271862>➞[130]
//                        Concept[130] Endoscopic approach - access (qualifier value) <-2146797373>
//                    Some[133] Approach (attribute) <-2147269307>➞[132]
//                        Concept[132] Transurethral approach (qualifier value) <-2146565172>
//                    Some[135] Method (attribute) <-2147268827>➞[134]
//                        Concept[134] Inspection - action (qualifier value) <-2146794608>
//                    Some[137] Method (attribute) <-2147268827>➞[136]
//                        Concept[136] Excision - action (qualifier value) <-2146796310>
//                    Some[139] Access instrument (attribute) <-2147225381>➞[138]
//                        Concept[138] Cystoscope, device (physical object) <-2147149073>
//                    Some[141] Procedure site - Indirect (attribute) <-2146724957>➞[140]
//                        Concept[140] Urinary bladder structure (body structure) <-2147407049>
                SomeRole(-2147483593,
                  And(
                      SomeRole(-2147353023,  
                          ConceptAssertion(-2146664933, leb)),
                      SomeRole(-2147271862,  
                          ConceptAssertion(-2146797373, leb)),
                      SomeRole(-2147269307,  
                          ConceptAssertion(-2146565172, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146794608, leb)),
                      SomeRole(-2147268827,  
                          ConceptAssertion(-2146796310, leb)),
                      SomeRole(-2147225381,  
                          ConceptAssertion(-2147149073, leb)),
                      SomeRole(-2146724957,  
                          ConceptAssertion(-2147407049, leb))
                  )),
//            Concept[144] Endoscopy with surgical procedure (procedure) <-2147473793>
                ConceptAssertion(-2147473793, leb),
//            Concept[145] Cystourethroscopy with biopsy of bladder (procedure) <-2147056005>
                ConceptAssertion(-2147056005, leb),
//            Concept[146] Cystoscopy and transurethral resection of bladder lesion (procedure) <-2147036672>
                ConceptAssertion(-2147036672, leb),
//            Concept[147] Transurethral bladder excision (procedure) <-2146767318>
                ConceptAssertion(-2146767318, leb),
//            Concept[148] Flexible cystoscopy (procedure) <-2146507200>
                ConceptAssertion(-2146507200, leb),
//            Concept[149] Cystoscopy and transurethral resection of bladder lesion (procedure) <-2146337238>
                ConceptAssertion(-2146337238, leb)
           )// AND
        ); // SUFFICIENT SET
        
        
	return  leb.build();
    }

    /**
     * Test of generatePossibleSolutions method, of class IsomorphicResultsBottomUp.
     */
    //@Test
    @Ignore
    public void testGeneratePossibleSolutions() {
        
        System.out.println("generatePossibleSolutions");
        long start = System.currentTimeMillis();
        IsomorphicResultsBottomUp results = new IsomorphicResultsBottomUp(referenceExpression, comparisonExpression);  
        System.out.println(results);
        
        IsomorphicResultsFromPathHash isomorphicResultsFromPathHash = new IsomorphicResultsFromPathHash(referenceExpression, comparisonExpression);
        IsomorphicSolution solution2 = isomorphicResultsFromPathHash.call();
        
        
        LOG.info("Duration: " + (System.currentTimeMillis() - start));
        //LOG.info(results);
        // assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    

    /**
     * Test of getAddedRelationshipRoots method, of class IsomorphicResultsBottomUp.
     */
    //@Test
    public void testGetAddedRelationshipRoots() {
    }

    /**
     * Test of getAdditionalNodeRoots method, of class IsomorphicResultsBottomUp.
     */
    //@Test
    public void testGetAdditionalNodeRoots() {
    }

    /**
     * Test of getComparisonExpression method, of class IsomorphicResultsBottomUp.
     */
    //@Test
    public void testGetComparisonExpression() {
    }

    /**
     * Test of getDeletedNodeRoots method, of class IsomorphicResultsBottomUp.
     */
    //@Test
    public void testGetDeletedNodeRoots() {
    }

    /**
     * Test of getDeletedRelationshipRoots method, of class IsomorphicResultsBottomUp.
     */
    //@Test
    public void testGetDeletedRelationshipRoots() {
    }

    /**
     * Test of getIsomorphicExpression method, of class IsomorphicResultsBottomUp.
     */
    //@Test
    public void testGetIsomorphicExpression() {
    }

    /**
     * Test of getMergedExpression method, of class IsomorphicResultsBottomUp.
     */
    //@Test
    public void testGetMergedExpression() {
    }

    /**
     * Test of getReferenceExpression method, of class IsomorphicResultsBottomUp.
     */
    //@Test
    public void testGetReferenceExpression() {
    }

    /**
     * Test of getSharedRelationshipRoots method, of class IsomorphicResultsBottomUp.
     */
    //@Test
    public void testGetSharedRelationshipRoots() {
    }

    /**
     * Test of equivalent method, of class IsomorphicResultsBottomUp.
     */
    //@Test
    public void testEquivalent() {
    }
    
}
