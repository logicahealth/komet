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
package org.ihtsdo.otf.tcc.test.integration.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.ihtsdo.otf.tcc.api.query.ForCollection;
import org.ihtsdo.otf.tcc.api.query.JaxbForQuery;
import org.ihtsdo.otf.tcc.api.query.LetMap;
import org.ihtsdo.otf.tcc.api.query.Where;
import org.ihtsdo.otf.tcc.junit.BdbTestRunner;
import org.ihtsdo.otf.tcc.junit.BdbTestRunnerConfig;
import org.ihtsdo.otf.tcc.rest.server.QueryResource;
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
public class QueryTest extends JerseyTest {
    
    public QueryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    @Override
    public void setUp() {
    }
    
    @After
    @Override
    public void tearDown() {
    }
    
    @Override
    protected Application configure() {
        return new ResourceConfig(QueryResource.class);
    }

//    @Override
//    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
//        return super.getTestContainerFactory(); 
//    }
    
    @Test
    public void testQuery() {
        try {
            ExampleQuery q = new ExampleQuery(null);
            
            JAXBContext ctx = JaxbForQuery.get();
            
            String viewpointXml = 
"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
"<view-coordinate>\n" +
"    <allowedStatusAsString>ACTIVE</allowedStatusAsString>\n" +
"    <classifierSpec>\n" +
"        <description>IHTSDO Classifier</description>\n" +
"        <uuidStrs>7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9</uuidStrs>\n" +
"    </classifierSpec>\n" +
"    <contradictionManagerPolicy>IDENTIFY_ALL_CONFLICTS</contradictionManagerPolicy>\n" +
"    <langPrefConceptSpecList>\n" +
"        <description>United States of America English language reference set (foundation metadata concept)</description>\n" +
"        <uuidStrs>bca0a686-3516-3daf-8fcf-fe396d13cfad</uuidStrs>\n" +
"    </langPrefConceptSpecList>\n" +
"    <languageSort>RF2_LANG_REFEX</languageSort>\n" +
"    <languageSpec>\n" +
"        <description>United States of America English language reference set (foundation metadata concept)</description>\n" +
"        <uuidStrs>bca0a686-3516-3daf-8fcf-fe396d13cfad</uuidStrs>\n" +
"    </languageSpec>\n" +
"    <name>SNOMED Infered-Latest</name>\n" +
"    <precedence>PATH</precedence>\n" +
"    <relationshipAssertionType>INFERRED</relationshipAssertionType>\n" +
"    <vcUuid>0c734870-836a-11e2-9e96-0800200c9a66</vcUuid>\n" +
"    <viewPosition>\n" +
"        <path>\n" +
"            <conceptSpec>\n" +
"                <description>SNOMED Core</description>\n" +
"                <uuidStrs>8c230474-9f11-30ce-9cad-185a96fd03a2</uuidStrs>\n" +
"            </conceptSpec>\n" +
"            <origins>\n" +
"                <path>\n" +
"                    <conceptSpec>\n" +
"                        <description>Workbench Auxiliary</description>\n" +
"                        <uuidStrs>2faa9260-8fb2-11db-b606-0800200c9a66</uuidStrs>\n" +
"                    </conceptSpec>\n" +
"                </path>\n" +
"                <time>9223372036854775807</time>\n" +
"            </origins>\n" +
"        </path>\n" +
"        <time>9223372036854775807</time>\n" +
"    </viewPosition>\n" +
"</view-coordinate>";
            
            
            String forXml = getXmlString(ctx, new ForCollection());
            
            q.Let();
            Map<String, Object> map = q.getLetDeclarations();
            LetMap wrappedMap = new LetMap(map);
            String letMapXml = getXmlString(ctx, wrappedMap);
            
 
            Where.WhereClause where = q.Where().getWhereClause();

            String whereXml = getXmlString(ctx, where);

            
            final String hello = target("query").
                    queryParam("VIEWPOINT", viewpointXml).
                    queryParam("FOR", forXml).
                    queryParam("LET", letMapXml).
                    queryParam("WHERE", whereXml).request(MediaType.TEXT_PLAIN).get(String.class);
        } catch (JAXBException | IOException ex) {
            Logger.getLogger(QueryTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String getXmlString(JAXBContext ctx, Object obj) throws JAXBException {
        StringWriter writer;
        writer = new StringWriter();
        ctx.createMarshaller().marshal(obj, writer);
        String letMapXml = writer.toString();
        return letMapXml;
    }
}