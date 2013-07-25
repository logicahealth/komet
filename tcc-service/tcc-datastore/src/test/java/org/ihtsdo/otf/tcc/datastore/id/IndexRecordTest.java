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

import org.ihtsdo.otf.tcc.datastore.id.IndexCacheRecord;
import org.ihtsdo.otf.tcc.chronicle.cc.NidPairForRefex;
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
public class IndexRecordTest {
   private int[] test1 = new int[] { 3, 3, 10 };
   private int[] test2 = new int[] { 3, 3, 10, 11, 13 };    // 1 refset record
   private int[] test3 = new int[] { 3, 4, 10, 12 };        // 1 destination origin nid

   //~--- constructors --------------------------------------------------------

   public IndexRecordTest() {}

   //~--- methods -------------------------------------------------------------

   @After
   public void tearDown() {}

   @AfterClass
   public static void tearDownClass() {}

   /**
    * Test of getDestinationOriginNids method, of class IndexCacheRecord.
    */
   @Test
   public void testAddDestinationOriginNid() {
      System.out.println("getAddDestinationOriginNid");

      IndexCacheRecord instance  = new IndexCacheRecord(test1);
      int[]                expResult = new int[0];
      int[]                result    = instance.getDestinationOriginNids();

      assertArrayEquals(expResult, result);
      instance.addDestinationOriginNid(15);
      result    = instance.getDestinationOriginNids();
      expResult = new int[] { 15 };
      assertArrayEquals(expResult, result);
      instance.addDestinationOriginNid(13);
      result    = instance.getDestinationOriginNids();
      expResult = new int[] { 13, 15 };
      assertArrayEquals(expResult, result);
      instance  = new IndexCacheRecord(test2);
      expResult = new int[] {};
      result    = instance.getDestinationOriginNids();
      assertArrayEquals(expResult, result);
      instance.addDestinationOriginNid(15);
      result    = instance.getDestinationOriginNids();
      expResult = new int[] { 15 };
      assertArrayEquals(expResult, result);
      instance.addDestinationOriginNid(13);
      result    = instance.getDestinationOriginNids();
      expResult = new int[] { 13, 15 };
      assertArrayEquals(expResult, result);
      instance  = new IndexCacheRecord(test3);
      expResult = new int[] { 12 };
      result    = instance.getDestinationOriginNids();
      assertArrayEquals(expResult, result);
      instance.addDestinationOriginNid(13);
      result    = instance.getDestinationOriginNids();
      expResult = new int[] { 12, 13 };
      assertArrayEquals(expResult, result);
      instance.addDestinationOriginNid(9);
      result    = instance.getDestinationOriginNids();
      expResult = new int[] { 9, 12, 13 };
      assertArrayEquals(expResult, result);
      instance.addDestinationOriginNid(11);
      result    = instance.getDestinationOriginNids();
      expResult = new int[] { 9, 11, 12, 13 };
      assertArrayEquals(expResult, result);
      instance.addDestinationOriginNid(11);
      result    = instance.getDestinationOriginNids();
      expResult = new int[] { 9, 11, 12, 13 };
      assertArrayEquals(expResult, result);
   }

   /**
    *  Test of addNidPairForRefexes method, of class IndexCacheRecord.
    */
   @Test
   public void testAddNidPairForRefexes() {
      System.out.println("getNidPairsForRefexes");

      IndexCacheRecord instance  = new IndexCacheRecord(test1);
      NidPairForRefex[]    expResult = new NidPairForRefex[] {};
      NidPairForRefex[]    result    = instance.getNidPairsForRefsets();

      assertArrayEquals(expResult, result);
      instance.addNidPairForRefex(14, 15);
      result    = instance.getNidPairsForRefsets();
      expResult = new NidPairForRefex[] { NidPairForRefex.getRefexNidMemberNidPair(14, 15) };
      assertArrayEquals(expResult, result);
      instance  = new IndexCacheRecord(test2);
      expResult = new NidPairForRefex[] { NidPairForRefex.getRefexNidMemberNidPair(11, 13) };
      result    = instance.getNidPairsForRefsets();
      assertArrayEquals(expResult, result);
      instance.addNidPairForRefex(14, 15);
      result    = instance.getNidPairsForRefsets();
      expResult = new NidPairForRefex[] { NidPairForRefex.getRefexNidMemberNidPair(11, 13),
              NidPairForRefex.getRefexNidMemberNidPair(14, 15) };
      assertArrayEquals(expResult, result);
      instance  = new IndexCacheRecord(test3);
      expResult = new NidPairForRefex[] {};
      result    = instance.getNidPairsForRefsets();
      assertArrayEquals(expResult, result);
   }

   /**
    * Test of getDestinationOriginNids method, of class IndexCacheRecord.
    */
   @Test
   public void testGetDestinationOriginNids() {
      System.out.println("getDestinationOriginNids");

      IndexCacheRecord instance  = new IndexCacheRecord(test1);
      int[]                expResult = new int[0];
      int[]                result    = instance.getDestinationOriginNids();

      assertArrayEquals(expResult, result);
      instance  = new IndexCacheRecord(test2);
      expResult = new int[] {};
      result    = instance.getDestinationOriginNids();
      assertArrayEquals(expResult, result);
      instance  = new IndexCacheRecord(test3);
      expResult = new int[] { 12 };
      result    = instance.getDestinationOriginNids();
      assertArrayEquals(expResult, result);
   }

   /**
    *  Test of getNidPairsForRefexes method, of class IndexCacheRecord.
    */
   @Test
   public void testGetNidPairsForRefexes() {
      System.out.println("getNidPairsForRefexes");

      IndexCacheRecord instance  = new IndexCacheRecord(test1);
      NidPairForRefex[]    expResult = new NidPairForRefex[] {};
      NidPairForRefex[]    result    = instance.getNidPairsForRefsets();

      assertArrayEquals(expResult, result);
      instance  = new IndexCacheRecord(test2);
      expResult = new NidPairForRefex[] { NidPairForRefex.getRefexNidMemberNidPair(11, 13) };
      result    = instance.getNidPairsForRefsets();
      assertArrayEquals(expResult, result);
      instance  = new IndexCacheRecord(test3);
      expResult = new NidPairForRefex[] {};
      result    = instance.getNidPairsForRefsets();
      assertArrayEquals(expResult, result);
   }

   /**
    * Test of getRefexIndexArray method, of class IndexCacheRecord.
    */
   @Test
   public void testGetRefexIndexArray() {
      System.out.println("getRefexIndexArray");

      IndexCacheRecord instance  = new IndexCacheRecord(test1);
      int[]                expResult = new int[0];
      int[]                result    = instance.getRefexIndexArray();

      assertArrayEquals(expResult, result);
      instance  = new IndexCacheRecord(test2);
      expResult = new int[] { 11, 13 };
      result    = instance.getRefexIndexArray();
      assertArrayEquals(expResult, result);
      instance  = new IndexCacheRecord(test3);
      expResult = new int[] {};
      result    = instance.getRefexIndexArray();
      assertArrayEquals(expResult, result);
   }

   /**
    * Test of getRelationshipOutgoingArray method, of class IndexCacheRecord.
    */
   @Test
   public void testGetRelationshipOutgoingArray() {
      System.out.println("getRelationshipOutgoingArray");

      IndexCacheRecord instance  = new IndexCacheRecord(test1);
      int[]                expResult = new int[] { 10 };
      int[]                result    = instance.getRelationshipOutgoingArray();

      assertArrayEquals(expResult, result);
      instance  = new IndexCacheRecord(test2);
      expResult = new int[] { 10 };
      result    = instance.getRelationshipOutgoingArray();
      assertArrayEquals(expResult, result);
      instance  = new IndexCacheRecord(test3);
      expResult = new int[] { 10 };
      result    = instance.getRelationshipOutgoingArray();
      assertArrayEquals(expResult, result);
   }

   /**
    *  Test of updateData method, of class IndexCacheRecord.
    */
   @Test
   public void testUpdateData() {
      System.out.println("getDestinationOriginNids");

      IndexCacheRecord instance                 = new IndexCacheRecord(test1);
      int[]                destinationOriginData    = instance.getDestinationOriginNids();
      int[]                relationshipOutgoingData = instance.getRelationshipOutgoingArray();
      int[]                refexData                = instance.getRefexIndexArray();
      int[]                result                   = instance.updateData(relationshipOutgoingData, destinationOriginData, refexData);

      assertArrayEquals(test1, result);
      instance                 = new IndexCacheRecord(test2);
      destinationOriginData    = instance.getDestinationOriginNids();
      relationshipOutgoingData = instance.getRelationshipOutgoingArray();
      refexData                = instance.getRefexIndexArray();
      result                   = instance.updateData(relationshipOutgoingData, destinationOriginData, refexData);
      assertArrayEquals(test2, result);
      instance                 = new IndexCacheRecord(test3);
      destinationOriginData    = instance.getDestinationOriginNids();
      relationshipOutgoingData = instance.getRelationshipOutgoingArray();
      refexData                = instance.getRefexIndexArray();
      result                   = instance.updateData(relationshipOutgoingData, destinationOriginData, refexData);
      assertArrayEquals(test3, result);
   }
   @Test
   public void testEmptyConstructor() {
      System.out.println("getDestinationOriginNids");
      IndexCacheRecord instance = new IndexCacheRecord();
      int[] empty = new int[0];
      assertArrayEquals(empty, instance.getDestinationOriginNids());
      assertArrayEquals(empty, instance.getRelationshipOutgoingArray());
      assertArrayEquals(empty, instance.getRefexIndexArray());

   }
   //~--- set methods ---------------------------------------------------------

   @Before
   public void setUp() {}

   @BeforeClass
   public static void setUpClass() {}
}
