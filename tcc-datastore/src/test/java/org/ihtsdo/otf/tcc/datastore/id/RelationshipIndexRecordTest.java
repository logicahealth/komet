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



package org.ihtsdo.otf.tcc.datastore.id;

import org.ihtsdo.otf.tcc.datastore.id.RelationshipIndexRecord;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author kec
 */
public class RelationshipIndexRecordTest {
    //j-
   int[] data = new int[] {
      0, 
      0, 
      6, 
      1, 
      2, 
      0, 
      RelationshipIndexRecord.GROUP_BITMASK + RelationshipIndexRecord.INFERRED_BITMASK, 
      7
   };
   //j+
   //~--- constructors --------------------------------------------------------

   public RelationshipIndexRecordTest() {}

   //~--- methods -------------------------------------------------------------

   @After
   public void tearDown() {}

   @AfterClass
   public static void tearDownClass() {}

   /**
    * Test of advance method, of class RelationshipIndexRecord.
    */
   @Test
   public void testAdvance() {
      System.out.println("advance");

      RelationshipIndexRecord instance = new RelationshipIndexRecord(data, 2, data.length);

      try {
         instance.advance();
         fail("should have thrown an UnsupportedOperationException");
      } catch (NoSuchElementException e) {}
   }

   /**
    * Test of getDestinationNid method, of class RelationshipIndexRecord.
    */
   @Test
   public void testGetDestinationNid() {
      System.out.println("getDestinationNid");

      RelationshipIndexRecord instance  = new RelationshipIndexRecord(data, 2, data.length);
      int                     expResult = 2;
      int                     result    = instance.getDestinationNid();

      assertEquals(expResult, result);
   }

   /**
    * Test of getTypeNid method, of class RelationshipIndexRecord.
    */
   @Test
   public void testGetTypeNid() {
      System.out.println("getTypeNid");

      RelationshipIndexRecord instance  = new RelationshipIndexRecord(data, 2, data.length);
      int                     expResult = 1;
      int                     result    = instance.getTypeNid();

      assertEquals(expResult, result);
   }

   /**
    * Test of getVersions method, of class RelationshipIndexRecord.
    */
   @Test
   public void testGetVersions() throws Exception {
      System.out.println("getVersions");

      RelationshipIndexRecord instance  = new RelationshipIndexRecord(data, 2, data.length);
      List                    result    = instance.getVersions();

      assertEquals(2, result.size());
   }

   /**
    * Test of hasNext method, of class RelationshipIndexRecord.
    */
   @Test
   public void testHasNext() {
      System.out.println("hasNext");

      RelationshipIndexRecord instance  = new RelationshipIndexRecord(data, 2, data.length);
      boolean                 expResult = false;
      boolean                 result    = instance.hasNext();

      assertEquals(expResult, result);
   }

   /**
    * Test of setGroupFlag method, of class RelationshipIndexRecord.
    */
   @Test
   public void testSetGroupFlag() {
      System.out.println("setGroupFlag");

      int     stamp       = 0;
      boolean groupGtZero = true;
      int     expResult   = RelationshipIndexRecord.GROUP_BITMASK;
      int     result      = RelationshipIndexRecord.setGroupFlag(stamp, groupGtZero);

      assertEquals(expResult, result);
   }

   /**
    * Test of setInferredFlag method, of class RelationshipIndexRecord.
    */
   @Test
   public void testSetInferredFlag() {
      System.out.println("setInferredFlag");

      int     stamp     = 0;
      boolean inferred  = true;
      int     expResult = RelationshipIndexRecord.INFERRED_BITMASK;
      int     result    = RelationshipIndexRecord.setInferredFlag(stamp, inferred);

      assertEquals(expResult, result);
   }

   //~--- set methods ---------------------------------------------------------

   @Before
   public void setUp() {}

   @BeforeClass
   public static void setUpClass() {}
}
