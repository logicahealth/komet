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
package org.ihtsdo.otf.tcc.test.integration.jaxb;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import junit.framework.Assert;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.query.JaxbForQuery;
import org.ihtsdo.otf.tcc.api.query.LetMap;
import org.ihtsdo.otf.tcc.junit.BdbTestRunner;
import org.ihtsdo.otf.tcc.junit.BdbTestRunnerConfig;
import org.junit.After;
import org.junit.AfterClass;
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
public class LetMapTest {
    
    public LetMapTest() {
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
    public void testForMap() {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("kind-of", Snomed.ALLERGIC_ASTHMA);
            map.put("old-view", StandardViewCoordinates.getSnomedInferredLatest());

            JAXBContext ctx = JaxbForQuery.get();
            StringWriter writer = new StringWriter();

            LetMap wrappedMap = new LetMap(map);
            ctx.createMarshaller().marshal(wrappedMap, writer);

            String forMapXml = writer.toString();
            System.out.println("Map: " + forMapXml);

            LetMap unmarshalledWrappedMap = (LetMap) ctx.createUnmarshaller()
                          .unmarshal(new StringReader(forMapXml));   
            boolean aTest = map.equals(unmarshalledWrappedMap.getMap());
            org.junit.Assert.assertEquals(map, unmarshalledWrappedMap.getMap());
            
            
        } catch (IOException | JAXBException ex) {
            Logger.getLogger(LetMapTest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail(ex.toString());
        }
    }
}