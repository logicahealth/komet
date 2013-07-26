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
package org.ihtsdo.country;

import org.ihtsdo.otf.tcc.api.country.COUNTRY_CODE;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author kec
 */
public class COUNTRY_CODETest {
    
    public COUNTRY_CODETest() {
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
     * Test of values method, of class COUNTRY_CODE.
     */
    @Test
    public void testValues() {
        System.out.println("values");
        COUNTRY_CODE[] expResult = COUNTRY_CODE.values();
        COUNTRY_CODE[] result = COUNTRY_CODE.values();
        assertEquals(expResult, result);
    }

    /**
     * Test of valueOf method, of class COUNTRY_CODE.
     */
    @Test
    public void testValueOf() {
        System.out.println("valueOf");
        String name = "US";
        COUNTRY_CODE expResult = COUNTRY_CODE.US;
        COUNTRY_CODE result = COUNTRY_CODE.valueOf(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of getFormatedCountryCode method, of class COUNTRY_CODE.
     */
    @Test
    public void testGetFormatedCountryCode() {
        System.out.println("getFormatedCountryCode");
        COUNTRY_CODE instance = COUNTRY_CODE.US;
        String expResult = "US";
        String result = instance.getFormatedCountryCode();
        assertEquals(expResult, result);
    }
}
