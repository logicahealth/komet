/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.lang;

import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author kec
 */
public class LANG_CODETest {
    
    public LANG_CODETest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of values method, of class LanguageCode.
     */
    @Test
    public void testValues() {
        System.out.println("values");
        LanguageCode[] expResult = LanguageCode.values();
        LanguageCode[] result = LanguageCode.values();
        assertEquals(expResult, result);
    }

    /**
     * Test of valueOf method, of class LanguageCode.
     */
    @Test
    public void testValueOf() {
        System.out.println("valueOf");
        String name = "EN_AU";
        LanguageCode expResult = LanguageCode.EN_AU;
        LanguageCode result = LanguageCode.valueOf(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of getFormatedLanguageCode method, of class LanguageCode.
     */
    @Test
    public void testGetFormatedLanguageCode() {
        System.out.println("getFormatedLanguageCode");
        LanguageCode instance = LanguageCode.EN_AU;
        String expResult = "en-AU";
        String result = instance.getFormatedLanguageCode();
        assertEquals(expResult, result);
        instance = LanguageCode.EN;
        expResult = "en";
        result = instance.getFormatedLanguageCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLangCode method, of class LanguageCode.
     */
    @Test
    public void testGetLangCode() {
        System.out.println("getLangCode");
        String name = "EN_AU";
        LanguageCode expResult = LanguageCode.EN_AU;
        LanguageCode result = LanguageCode.getLangCode(name);
        assertEquals(expResult, result);
        name = "en-AU";
        result = LanguageCode.getLangCode(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class LanguageCode.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        LanguageCode instance = LanguageCode.EN_AU;
        String expResult = "EN_AU";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
