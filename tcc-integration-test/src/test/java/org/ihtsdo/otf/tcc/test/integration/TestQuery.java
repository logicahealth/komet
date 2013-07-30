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
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.query.Clause;
import org.ihtsdo.otf.tcc.api.query.Query;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.junit.BdbTestRunner;
import org.ihtsdo.otf.tcc.junit.BdbTestRunnerConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author kec
 */
@RunWith(BdbTestRunner.class)
@BdbTestRunnerConfig()
public class TestQuery {
    
    public TestQuery() {
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
     public void testQuery() throws IOException, Exception {
        System.out.println("Sequence: " + Ts.get().getSequence());
        try {
           Query q = new Query(StandardViewCoordinates.getSnomedInferredLatest()) {
                @Override
                protected NativeIdSetBI For() throws IOException {
                    return Ts.get().getAllConceptNids();
                }

                @Override
                protected void Let() throws IOException {
                    let("allergic-asthma", Snomed.ALLERGIC_ASTHMA);
                }

                @Override
                protected Clause Where() {
                    return And(ConceptIsKindOf("allergic-asthma"));
                }
            };

            NativeIdSetBI results = q.compute();
            System.out.println("Query result count: " + results.size());
            Assert.assertEquals(11, results.size());
            
            
        } catch (IOException ex) {
            Assert.fail(ex.toString());
        } catch (Exception ex) {
            Assert.fail(ex.toString());
        }
    
        System.out.println("Sequence: " + Ts.get().getSequence());
        OrTest orTest = new OrTest();
        Query q2 = orTest.getOrTest();
        NativeIdSetBI results2 = q2.compute();
        System.out.println("Query result count:" + results2.size());
        Assert.assertEquals(11 + 427, results2.size());
        
        
        //System.out.println("Sequence: " + Ts.get().getSequence());

        NotTest notTest = new NotTest();
        Query notQuery = notTest.getQuery();
        NativeIdSetBI notResults = notQuery.compute();
        System.out.println("Query result count: " + notResults.size());
        Assert.assertEquals(Ts.get().getAllConceptNids().size() - 438, notResults.size());
        
        /*
        IsChildOfTest isChildOfTest = new IsChildOfTest();
        Query q3 = isChildOfTest.getQuery();
        NativeIdSetBI results3 = q3.compute();
        System.out.println("Query result count " + results3.size());
        Assert.assertEquals(5, results3.size());
        */
        /*
        IsDescendentOfTest isDescendent = new IsDescendentOfTest();
        Query q4 = isDescendent.getQuery();
        NativeIdSetBI results4 = q4.compute();
        System.out.println("Query result count " + results4.size());
        Assert.assertEquals(6, results4.size());
        */
        
        /*
        ChangedFromPreviousVersionTest previous = new ChangedFromPreviousVersionTest();
        Query changesQuery = previous.getQuery();
        NativeIdSetBI changesResults = changesQuery.compute();
        System.out.println("Query result count " + changesResults.size());
        Assert.assertEquals(6, changesResults.size());
        */
        
        /*
        DescriptionRegexMatchTest regexMatch = new DescriptionRegexMatchTest();
        Query regexMatchQuery = regexMatch.getQuery();
        NativeIdSetBI regexResults = regexMatchQuery.compute();
        System.out.println("Query result count " + regexResults.size());
        Assert.assertEquals(20, regexResults.size());
        */
     }
}