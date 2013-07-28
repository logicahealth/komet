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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.query.JaxbForQuery;
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
public class ViewCoordinateTest {
    
    public ViewCoordinateTest() {
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
    public void testJaxb() {
        try {
            
            ViewCoordinate originalViewCoordinate = StandardViewCoordinates.getSnomedInferredLatest();
            JAXBContext ctx = JaxbForQuery.get();
            StringWriter writer = new StringWriter();

            ctx.createMarshaller().marshal(originalViewCoordinate, writer);

            String viewCoordinateXml = writer.toString();
            System.out.println("ViewCoordinate: " + viewCoordinateXml);

            ViewCoordinate unmarshalledViewCoordinate = (ViewCoordinate) ctx.createUnmarshaller()
                          .unmarshal(new StringReader(viewCoordinateXml));   
            
            Assert.assertEquals(originalViewCoordinate, unmarshalledViewCoordinate);
        } catch (JAXBException | IOException ex) {
            Logger.getLogger(ViewCoordinateTest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail(ex.toString());
        }
    
    
    }
}