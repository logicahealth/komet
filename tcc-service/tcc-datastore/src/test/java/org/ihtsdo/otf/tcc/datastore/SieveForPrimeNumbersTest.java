/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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

package org.ihtsdo.otf.tcc.datastore;

import org.ihtsdo.otf.tcc.datastore.SieveForPrimeNumbers;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author kec
 */
public class SieveForPrimeNumbersTest {

    public SieveForPrimeNumbersTest() {
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
     * Test of sieve_of_eratosthenes method, of class SieveForPrimeNumbers.
     */
    @Test
    public void testSieve_of_eratosthenes() {
        System.out.println("sieve_of_eratosthenes");
        int max = 10;
        List<Integer> expResult = Arrays.asList(new Integer[] { 2, 3, 5, 7} );
        List<Integer> result = SieveForPrimeNumbers.sieve_of_eratosthenes(max);
        assertEquals(expResult, result);
    }


}