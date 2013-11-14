/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.test.integration;

import java.io.IOException;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.datastore.Bdb;
import org.ihtsdo.otf.tcc.junit.BdbTestRunner;
import org.ihtsdo.otf.tcc.junit.BdbTestRunnerConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author kec
 */
@RunWith(BdbTestRunner.class)
@BdbTestRunnerConfig()

public class DescendentOfTest {
    
    public DescendentOfTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    @Test
    public void hello() throws IOException, ContradictionException {
         ConceptChronicleBI motion = Ts.get().getConcept(UUID.fromString("45a8fde8-535d-3d2a-b76b-95ab67718b41"));
         
         
         ConceptVersionBI centrifugalForceVersion = Ts.get().getConceptVersion(
                  StandardViewCoordinates.getSnomedInferredLatest(), UUID.fromString("2b684fe1-8baf-34ef-9d2a-df03142c915a"));

          ConceptVersionBI motionVersion = Ts.get().getConceptVersion(
                  StandardViewCoordinates.getSnomedInferredLatest(), 
                  UUID.fromString("45a8fde8-535d-3d2a-b76b-95ab67718b41"));
          
          boolean kindOf = centrifugalForceVersion.isKindOf(motionVersion);
          assertTrue(kindOf);
          
          NativeIdSetBI kindOfNids = Bdb.getMemoryCache().getKindOfNids(motion.getNid(), 
                  StandardViewCoordinates.getSnomedInferredLatest());
          assertTrue(kindOfNids.contains(centrifugalForceVersion.getNid()));
    }
}