/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.api.util;

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
public class NaturalOrderTest {
   
   public NaturalOrderTest() {
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

   /**
    * Test of compare method, of class NaturalOrder.
    */
   @Test
   public void testCompare() {
      System.out.println("compare");
      String s1 = "i";
      String s2 = "I";
      int expResult = 0;
      int result = NaturalOrder.compareStrings(s1, s2);
      assertEquals(expResult, result);

      s1 = "ISAAC metadata (ISAAC)";
      s2 = "health concept";
      result = NaturalOrder.compareStrings(s1, s2);      
      assertTrue(result > 0);
      
      s1 = "1";
      s2 = "10";
      result = NaturalOrder.compareStrings(s1, s2);      
      assertTrue(result < 0);

      s1 = "2";
      s2 = "10";
      result = NaturalOrder.compareStrings(s1, s2);      
      assertTrue(result < 0);

      s1 = "Change feature type...";
      s2 = "Change feature type using";
      result = NaturalOrder.compareStrings(s1, s2);
      assertTrue(result < 0);
   }
   
}
