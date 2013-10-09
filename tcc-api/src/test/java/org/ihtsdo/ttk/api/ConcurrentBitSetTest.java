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
package org.ihtsdo.ttk.api;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetItrBI;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.nid.HybridNidSet;
import org.ihtsdo.otf.tcc.api.nid.IntSet;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author dylangrald
 */
public class ConcurrentBitSetTest {

    public ConcurrentBitSetTest() {
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
     * Test of get method, of class ConcurrentBitSet.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        ConcurrentBitSet instance = new ConcurrentBitSet(5);
        instance.set(Integer.MIN_VALUE + 1);
        System.out.println(instance.toString());
        assertTrue(instance.get(Integer.MIN_VALUE + 1));
    }

    /**
     * Test of set method, of class ConcurrentBitSet.
     */
    @Test
    public void testSet_int_boolean() {
        System.out.println("set");
        ConcurrentBitSet instance = new ConcurrentBitSet(5);
        instance.add(Integer.MIN_VALUE + 3);
        instance.set(Integer.MIN_VALUE + 3, false);
        assertTrue(instance.isEmpty());
    }

    /**
     * Test of set method, of class ConcurrentBitSet.
     */
    @Test
    public void testSet_int() {
        System.out.println("set");
        ConcurrentBitSet instance = new ConcurrentBitSet(5);
        instance.set(Integer.MIN_VALUE + 3);
        assertTrue(instance.contains(Integer.MIN_VALUE + 3));
    }

    /**
     * Test of clear method, of class ConcurrentBitSet.
     */
    @Test
    public void testClear_int() {
        System.out.println("clear");
        ConcurrentBitSet instance = new ConcurrentBitSet(5);
        instance.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5, Integer.MIN_VALUE + 4});
        instance.clear(5);
        assertTrue(!instance.contains(Integer.MIN_VALUE + 5));
    }

    /**
     * Test of clearAll method, of class ConcurrentBitSet.
     */
    @Test
    public void testClearAll() {
        System.out.println("clearAll");
        ConcurrentBitSet instance = new ConcurrentBitSet(5);
        instance.set(Integer.MIN_VALUE + 3);
        instance.set(Integer.MIN_VALUE + 1);
        assertTrue(instance.contains(Integer.MIN_VALUE + 3));
        instance.clearAll();
        assertEquals(0, instance.size());
    }

    /**
     * Test of nextSetBit method, of class ConcurrentBitSet.
     */
    @Test
    public void testNextSetBit() {
        System.out.println("nextSetBit");
        ConcurrentBitSet instance = new ConcurrentBitSet(5);
        instance.set(Integer.MIN_VALUE + 3);
        instance.set(Integer.MIN_VALUE + 1);
        assertTrue(instance.contains(Integer.MIN_VALUE + 3));
        assertEquals(Integer.MIN_VALUE + 3, instance.nextSetBit(Integer.MIN_VALUE + 2));
    }

    /**
     * Test of length method, of class ConcurrentBitSet.
     */
    @Test
    public void testLength() {
        System.out.println("length");
        ConcurrentBitSet instance = new ConcurrentBitSet();
        instance.set(Integer.MIN_VALUE + 5);
        instance.set(Integer.MIN_VALUE + 6);
        assertEquals(7, instance.length());
    }

    /**
     * Test of and method, of class ConcurrentBitSet.
     */
    @Test
    public void testAnd_ConcurrentBitSet() {
        System.out.println("and");
        ConcurrentBitSet first = new ConcurrentBitSet(5);
        ConcurrentBitSet second = new ConcurrentBitSet(5);
        first.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        second.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 5});
        first.and(second);
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 5}, first.getSetValues());

    }

    /**
     * Test of or method, of class ConcurrentBitSet.
     */
    @Test
    public void testOr_ConcurrentBitSet() {
        System.out.println("or");
        ConcurrentBitSet first = new ConcurrentBitSet(5);
        ConcurrentBitSet second = new ConcurrentBitSet(5);
        first.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        second.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 5});
        first.or(second);
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 5}, first.getSetValues());
    }

    @Test
    public void testOr() {
        System.out.println("or test");
        ConcurrentBitSet first = new ConcurrentBitSet();
        ConcurrentBitSet second = new ConcurrentBitSet();
        //first.addAll(new int[]{Integer.MIN_VALUE + 63882});

        first.add(-2146844766);
        second.add(-2139228960);

        //second.addAll(new int[]{Integer.MIN_VALUE + 8254687});
        first.or(second);
        for (int i : first.getSetValues()) {
            System.out.println(i);
        }
        assertEquals(2, first.size());
    }

    /**
     * Test of xor method, of class ConcurrentBitSet. TODO fix me
     */
    @Test
    public void testXor_ConcurrentBitSet() {
        System.out.println("xor");
        ConcurrentBitSet first = new ConcurrentBitSet(5);
        ConcurrentBitSet second = new ConcurrentBitSet(5);
        first.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        second.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 5});
        first.xor(second);
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 3}, first.getSetValues());
    }

    /**
     * Test of andNot method, of class ConcurrentBitSet.
     */
    @Test
    public void testAndNot_ConcurrentBitSet() {
        System.out.println("andNot");
        ConcurrentBitSet first = new ConcurrentBitSet(5);
        ConcurrentBitSet second = new ConcurrentBitSet(5);
        first.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        second.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 5});
        first.andNot(second);
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 2}, first.getSetValues());
    }

    /**
     * Test of toIntArray method, of class ConcurrentBitSet.
     */
    @Test
    public void testToIntArray_0args() {
        System.out.println("toIntArray");
        ConcurrentBitSet instance = new ConcurrentBitSet();
        instance.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5}, instance.toIntArray());
    }

    /**
     * Test of cardinality method, of class ConcurrentBitSet.
     */
    @Test
    public void testCardinality() {
        System.out.println("cardinality");
        ConcurrentBitSet first = new ConcurrentBitSet(5);
        ConcurrentBitSet second = new ConcurrentBitSet(5);
        first.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        assertEquals(3, first.cardinality());

    }

    /**
     * Test of getIterator method, of class ConcurrentBitSet.
     */
    @Test
    @Ignore
    public void testGetIterator() {
        System.out.println("getIterator");
        ConcurrentBitSet instance = new ConcurrentBitSet();
        NativeIdSetItrBI expResult = null;
        NativeIdSetItrBI result = instance.getSetBitIterator();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of size method, of class ConcurrentBitSet.
     */
    @Test
    public void testSize() {
        System.out.println("size");
        ConcurrentBitSet instance = new ConcurrentBitSet(5);
        instance.set(Integer.MIN_VALUE + 1);
        assertEquals(1, instance.size());
        instance.set(Integer.MIN_VALUE + 2);
        assertEquals(2, instance.size());

    }

    /**
     * Test of isMember method, of class ConcurrentBitSet.
     */
    @Test
    public void testIsMember() {
        System.out.println("isMember");
        ConcurrentBitSet instance = new ConcurrentBitSet(5);
        instance.set(Integer.MIN_VALUE + 3);
        assertTrue(instance.contains(Integer.MIN_VALUE + 3));
    }

    /**
     * Test of setMember method, of class ConcurrentBitSet.
     */
    @Test
    public void testSetMember() {
        System.out.println("setMember");
        ConcurrentBitSet instance = new ConcurrentBitSet(5);
        instance.setMember(Integer.MIN_VALUE + 3);
        assertTrue(instance.contains(Integer.MIN_VALUE + 3));
    }

    /**
     * Test of and method, of class ConcurrentBitSet.
     */
    @Test
    public void testAnd_NativeIdSetBI() {
        System.out.println("and");
        ConcurrentBitSet first = new ConcurrentBitSet(5);
        IntSet second = new IntSet();
        first.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        second.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 5});
        first.and(second);
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 5}, first.getSetValues());
    }

    /**
     * Test of or method, of class ConcurrentBitSet.
     */
    @Test
    public void testOr_NativeIdSetBI() {
        System.out.println("or");
        ConcurrentBitSet first = new ConcurrentBitSet(5);
        IntSet second = new IntSet();
        first.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        second.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 5});
        first.or(second);
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 5}, first.getSetValues());
    }

    /**
     * Test of xor method, of class ConcurrentBitSet. TODO fix to work properly.
     */
    @Test
    @Ignore
    public void testXor_NativeIdSetBI() {
        System.out.println("xor");
        ConcurrentBitSet first = new ConcurrentBitSet(5);
        IntSet second = new IntSet();
        first.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        second.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 5});
        first.xor(second);
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 3}, first.getSetValues());
    }

    /**
     * Test of contains method, of class ConcurrentBitSet.
     */
    @Test
    public void testContains() {
        System.out.println("contains");
        ConcurrentBitSet instance = new ConcurrentBitSet(5);
        instance.set(Integer.MIN_VALUE + 3);
        instance.set(Integer.MIN_VALUE + 1);
        assertTrue(instance.contains(Integer.MIN_VALUE + 3));
    }

    /**
     * Test of getSetValues method, of class ConcurrentBitSet.
     */
    @Test
    public void testGetSetValues() {
        System.out.println("getSetValues");
        ConcurrentBitSet instance = new ConcurrentBitSet(5);
        instance.set(Integer.MIN_VALUE + 3);
        instance.set(Integer.MIN_VALUE + 1);
        instance.set(Integer.MIN_VALUE + 4);
        //System.out.println(instance.getSetValues().toString());
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 4}, instance.toIntArray());
    }

    /**
     * Test of add method, of class ConcurrentBitSet.
     */
    @Test
    public void testAdd() {
        System.out.println("add");
        ConcurrentBitSet instance = new ConcurrentBitSet(5);
        instance.add(Integer.MIN_VALUE + 3);
        instance.add(Integer.MIN_VALUE + 1);
        assertTrue(instance.contains(Integer.MIN_VALUE + 3));
    }

    /**
     * Test of addAll method, of class ConcurrentBitSet.
     */
    @Test
    public void testAddAll() {
        System.out.println("addAll");
        ConcurrentBitSet first = new ConcurrentBitSet(5);
        first.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5}, first.getSetValues());
    }

    /**
     * Test of remove method, of class ConcurrentBitSet.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        ConcurrentBitSet first = new ConcurrentBitSet(5);
        first.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        System.out.println("1: " + first);
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5}, first.getSetValues());
        first.remove(Integer.MIN_VALUE + 2);
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 5}, first.getSetValues());
    }

    /**
     * Test of removeAll method, of class ConcurrentBitSet.
     */
    @Test
    public void testRemoveAll() {
        System.out.println("removeAll");
        ConcurrentBitSet first = new ConcurrentBitSet(5);
        first.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5, Integer.MIN_VALUE + 4});
        System.out.println("1: " + first);
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 4, Integer.MIN_VALUE + 5}, first.getSetValues());
        first.removeAll(new int[]{Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 4}, first.getSetValues());
    }

    /**
     * Test of clear method, of class ConcurrentBitSet.
     */
    @Test
    public void testClear_0args() {
        System.out.println("clear");
        ConcurrentBitSet instance = new ConcurrentBitSet();
        instance.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5, Integer.MIN_VALUE + 4});
        instance.clear();
        assertTrue(instance.isEmpty());

    }

    /**
     * Test of getMax method, of class ConcurrentBitSet.
     */
    @Test
    public void testGetMax() {
        System.out.println("getMax");
        ConcurrentBitSet instance = new ConcurrentBitSet();
        instance.addAll(new int[]{Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 5});
        assertEquals(Integer.MIN_VALUE + 5, instance.getMax());
    }

    /**
     * Test of getMin method, of class ConcurrentBitSet.
     */
    @Test
    public void testGetMin() {
        System.out.println("getMin");
        ConcurrentBitSet first = new ConcurrentBitSet(5);
        first.addAll(new int[]{Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 5});
        assertEquals(Integer.MIN_VALUE + 1, first.getMin());

    }

    /**
     * Test of contiguous method, of class ConcurrentBitSet.
     */
    @Test
    public void testContiguous() {
        System.out.println("contiguous");
        ConcurrentBitSet first = new ConcurrentBitSet(5);
        first.addAll(new int[]{Integer.MIN_VALUE + 4, Integer.MIN_VALUE + 5, Integer.MIN_VALUE + 6});
        assertTrue(first.contiguous());
        first.add(Integer.MIN_VALUE + 2);
        assertTrue(!first.contiguous());
    }

    /**
     * Test of union method, of class ConcurrentBitSet.
     */
    @Test
    public void testUnion() {
        System.out.println("union");
        ConcurrentBitSet first = new ConcurrentBitSet(5);
        IntSet second = new IntSet();
        first.addAll(new int[]{Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        second.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 5});
        first.or(second);
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 5}, first.getSetValues());
    }

    /**
     * Test of setNotMember method, of class ConcurrentBitSet.
     */
    @Test
    public void testSetNotMember() {
        System.out.println("setNotMember");
        ConcurrentBitSet instance = new ConcurrentBitSet();
        instance.add(Integer.MIN_VALUE + 1);
        instance.add(Integer.MIN_VALUE + 2);
        assertTrue(instance.contains(Integer.MIN_VALUE + 2));
        instance.setNotMember(Integer.MIN_VALUE + 2);
        assertTrue(!instance.contains(Integer.MIN_VALUE + 2));

    }

    /**
     * Test of andNot method, of class ConcurrentBitSet.
     */
    @Test
    public void testAndNot_NativeIdSetBI() {
        System.out.println("andNot");
        ConcurrentBitSet first = new ConcurrentBitSet(5);
        IntSet second = new IntSet();
        first.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        second.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 5});
        first.andNot(second);
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 2}, first.getSetValues());
    }

    /**
     * Test of isEmpty method, of class ConcurrentBitSet.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        ConcurrentBitSet instance = new ConcurrentBitSet();
        assertTrue(instance.isEmpty());
        instance.add(Integer.MIN_VALUE + 1);
        assertTrue(!instance.isEmpty());
        instance.remove(Integer.MIN_VALUE + 1);
        assertTrue(instance.isEmpty());
    }

    @Test
    public void testSetAll() {
        System.out.println("not");
        ConcurrentBitSet instance = new ConcurrentBitSet(10);
        instance.setAll(Integer.MIN_VALUE + 8);
        assertEquals(8, instance.size());
    }

    @Test
    public void testConstructor() {
        System.out.println("constructor");
        NativeIdSetBI other = new HybridNidSet();
        other.add(Integer.MIN_VALUE + 1);
        other.add(Integer.MIN_VALUE + 5);
        ConcurrentBitSet instance = new ConcurrentBitSet(other);
        assertEquals(2, instance.size());
    }

    @Test
    public void testIterator() {
        System.out.println("iterator");
        ConcurrentBitSet first = new ConcurrentBitSet();
        
        int end = 64*10240 + 500;
        
        for(int i = 1; i < end; i++){
            first.add(Integer.MIN_VALUE + i);
        }
        
        NativeIdSetItrBI iter = first.getSetBitIterator();
        int[] setValues = new int[first.size()];
        int i = 0;
        try {
            while (iter.next()) {
                setValues[i] = iter.nid();
                i++;
            }
        } catch (IOException ex) {
            Logger.getLogger(ConcurrentBitSetTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        assertArrayEquals(first.getSetValues(), setValues);
    }
}