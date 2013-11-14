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
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.datastore.Bdb;
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
public class GetAllComponentsTest {
    
    static final TerminologyStoreDI ts = Ts.get();
    
    public GetAllComponentsTest() {
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
    public void getAllComponents() throws IOException {
        System.out.println("All components test");
        NativeIdSetBI allConcepts = ts.getAllConceptNids();
        System.out.println("All concepts: " + allConcepts.size());
        NativeIdSetBI allConceptsFromCache = ts.getAllConceptNidsFromCache();
        System.out.println("All concepts from cache: " + allConceptsFromCache.size());
        NativeIdSetBI allComponents = ts.getComponentNidsForConceptNids(allConcepts);
        System.out.println("All components: " + allComponents.size());
        NativeIdSetBI orphanNids = ts.getOrphanNids(allConcepts);
        System.out.println("orphanNids: " + orphanNids.size());
        int maxNid = Bdb.getUuidsToNidMap().getCurrentMaxNid() + Integer.MIN_VALUE;
        System.out.println("maxNid: " + maxNid);
        Assert.assertTrue(allComponents.contains(maxNid) || orphanNids.contains(maxNid));
//        Assert.assertEquals(Bdb.getUuidsToNidMap().getCurrentMaxNid() + Integer.MIN_VALUE, 
//                allComponents.size() + orphanNids.size());
        allComponents.or(orphanNids);
        Assert.assertTrue(allComponents.contiguous());
        Assert.assertTrue(allComponents.isMember(Integer.MIN_VALUE));
        Assert.assertTrue(allComponents.isMember(maxNid));
        Assert.assertFalse(allComponents.isMember(maxNid + 1));
        // TODO revaluate if maxNid + 1 is the correct answer. 
        Assert.assertEquals(maxNid + 1, allComponents.size());
    }
}