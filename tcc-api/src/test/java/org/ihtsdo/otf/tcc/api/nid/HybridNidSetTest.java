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
package org.ihtsdo.otf.tcc.api.nid;

import java.io.IOException;
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
public class HybridNidSetTest {

    public HybridNidSetTest() {
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

    public ConcurrentBitSet getBitSet() {
        ConcurrentBitSet nidSet = new ConcurrentBitSet();
        HybridNidSet example = new HybridNidSet();
        for (int i = 1; i < example.getThreshold() + 5; i++) {
            nidSet.add(Integer.MIN_VALUE + i);
        }
        return nidSet;
    }

    public HybridNidSet getLargeIntSet() {
        HybridNidSet example = new HybridNidSet();
        //IntSet nidSet = new IntSet();
        for (int i = 1; i < example.getThreshold() - 5; i++) {
            example.add(Integer.MIN_VALUE + i);
        }
        return example;
    }

    /**
     * Test of getIterator method, of class HybridNidSet.
     */
    @Test
    public void testGetIterator() throws IOException {
        System.out.println("getIterator");
        HybridNidSet first = new HybridNidSet();
        first.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        NativeIdSetItrBI iter = first.getSetBitIterator();
        assertTrue(iter.next());
        assertTrue(iter.next());
        assertEquals(Integer.MIN_VALUE + 2, iter.nid());
        assertTrue(iter.next());
        assertTrue(!iter.next());
    }

    /**
     * Test of and method, of class HybridNidSet.
     */
    @Test
    public void testAnd() {
        System.out.println("and");
        HybridNidSet first = new HybridNidSet(this.getBitSet());
        HybridNidSet second = new HybridNidSet();
        second.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 4});
        first.and(second);
        assertEquals(3, first.size());
        assertTrue(first.nidSet.getClass().isAssignableFrom(IntSet.class));

    }

    /**
     * Test of andNot method, of class HybridNidSet.
     */
    @Test
    public void testAndNot() {
        System.out.println("andNot");
        HybridNidSet first = new HybridNidSet(this.getBitSet());
        HybridNidSet second = this.getLargeIntSet();
        second.andNot(first);
        assertEquals(0, second.size());
    }

    /**
     * Test of contains method, of class HybridNidSet.
     */
    @Test
    public void testContains() {
        System.out.println("contains");
        HybridNidSet instance = new HybridNidSet();
        instance.add(Integer.MIN_VALUE + 1);
        assertTrue(instance.contains(Integer.MIN_VALUE + 1));
    }

    /**
     * Test of getSetValues method, of class HybridNidSet.
     */
    @Test
    public void testGetSetValues() {
        System.out.println("getSetValues");
        HybridNidSet instance = new HybridNidSet();
        instance.addAll(new int[]{Integer.MIN_VALUE + 5, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 4});
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 4, Integer.MIN_VALUE + 5}, instance.getSetValues());
    }

    /**
     * Test of add method, of class HybridNidSet.
     */
    @Test
    public void testAdd() {
        System.out.println("add");
        HybridNidSet instance = new HybridNidSet();
        instance.add(Integer.MIN_VALUE + 1);
        assertTrue(instance.contains(Integer.MIN_VALUE + 1));
    }

    /**
     * Test of remove method, of class HybridNidSet.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        HybridNidSet instance = new HybridNidSet();
        instance.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 4});
        assertTrue(instance.contains(Integer.MIN_VALUE + 2));
        instance.remove(Integer.MIN_VALUE + 2);
        assertTrue(!instance.contains(Integer.MIN_VALUE + 2));

    }

    /**
     * Test of addAll method, of class HybridNidSet.
     */
    @Test
    public void testAddAll() {
        System.out.println("addAll");
        HybridNidSet instance = new HybridNidSet();
        instance.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 4});
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 4}, instance.getSetValues());
    }

    /**
     * Test of removeAll method, of class HybridNidSet.
     */
    @Test
    public void testRemoveAll() {
        System.out.println("removeAll");
        HybridNidSet instance = new HybridNidSet();
        instance.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 4, Integer.MIN_VALUE + 6});
        instance.removeAll(new int[]{Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 4});
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 6}, instance.getSetValues());

    }

    /**
     * Test of clear method, of class HybridNidSet.
     */
    @Test
    public void testClear() {
        System.out.println("clear");
        HybridNidSet instance = new HybridNidSet();
        instance.add(Integer.MIN_VALUE + 1);
        assertTrue(!instance.isEmpty());
        instance.clear();
        assertTrue(instance.isEmpty());
    }

    /**
     * Test of size method, of class HybridNidSet.
     */
    @Test
    public void testSize() {
        System.out.println("size");
        HybridNidSet instance = new HybridNidSet();
        instance.addAll(new int[]{Integer.MIN_VALUE + 5, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 4});
        assertEquals(3, instance.size());
    }

    /**
     * Test of getMax method, of class HybridNidSet.
     */
    @Test
    public void testGetMax() {
        System.out.println("getMax");
        HybridNidSet instance = new HybridNidSet();
        instance.addAll(new int[]{Integer.MIN_VALUE + 5, Integer.MIN_VALUE + 11, Integer.MIN_VALUE + 4});
        assertEquals(Integer.MIN_VALUE + 11, instance.getMax());
    }

    /**
     * Test of getMin method, of class HybridNidSet.
     */
    @Test
    public void testGetMin() {
        System.out.println("getMin");
        HybridNidSet instance = new HybridNidSet();
        instance.addAll(new int[]{Integer.MIN_VALUE + 5, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 4});
        assertEquals(Integer.MIN_VALUE + 2, instance.getMin());
    }

    /**
     * Test of contiguous method, of class HybridNidSet.
     */
    @Test
    public void testContiguous() {
        System.out.println("contiguous");
        HybridNidSet nidSet = this.getLargeIntSet();
        assertTrue(nidSet.contiguous());
        nidSet.add(Integer.MIN_VALUE + nidSet.getThreshold() + 20);
        assertTrue(!nidSet.contiguous());
    }

    /**
     * Test of union method, of class HybridNidSet.
     */
    @Test
    public void testUnion() {
        System.out.println("union");
        HybridNidSet first = new HybridNidSet(this.getBitSet());
        HybridNidSet second = new HybridNidSet();
        second.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 4});
        first.union(second);
        assertEquals(first.getThreshold() + 4, first.size());
        assertTrue(first.nidSet.getClass().isAssignableFrom(ConcurrentBitSet.class));
    }

    /**
     * Test of xor method, of class HybridNidSet.
     * TODO fix method...
     */
    @Test
    @Ignore
    public void testXor() {
        System.out.println("xor");
        HybridNidSet first = new HybridNidSet(this.getBitSet());
        HybridNidSet second = this.getLargeIntSet();
        first.xor(second);
        assertEquals(10, first.size());
        assertTrue(first.nidSet.getClass().isAssignableFrom(IntSet.class));
    }

    /**
     * Test of isMember method, of class HybridNidSet.
     */
    @Test
    public void testIsMember() {
        System.out.println("isMember");
        HybridNidSet first = new HybridNidSet();
        first.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        assertTrue(first.isMember(Integer.MIN_VALUE + 2));
    }

    /**
     * Test of setMember method, of class HybridNidSet.
     */
    @Test
    public void testSetMember() {
        System.out.println("setMember");
        HybridNidSet instance = new HybridNidSet();
        instance.setMember(Integer.MIN_VALUE + 2);
        assertTrue(instance.contains(Integer.MIN_VALUE + 2));
    }

    /**
     * Test of setNotMember method, of class HybridNidSet.
     */
    @Test
    public void testSetNotMember() {
        System.out.println("setNotMember");
        HybridNidSet instance = new HybridNidSet();
        instance.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        instance.setNotMember(Integer.MIN_VALUE + 2);
        assertArrayEquals(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 5}, instance.getSetValues());
        
    }

    /**
     * Test of or method, of class HybridNidSet.
     */
    @Test
    public void testOr() {
        System.out.println("or");
        HybridNidSet first = new HybridNidSet();
        first.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        HybridNidSet second = new HybridNidSet();
        second.addAll(new int[]{Integer.MIN_VALUE + 5, Integer.MIN_VALUE + 3, Integer.MIN_VALUE + 2});
        first.or(second);
        assertEquals(4, first.size());
    }

    /**
     * Test of isEmpty method, of class HybridNidSet.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        HybridNidSet instance = new HybridNidSet();
        assertTrue(instance.isEmpty());
        instance.addAll(new int[]{Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MIN_VALUE + 5});
        assertTrue(!instance.isEmpty());
        instance.clear();
        assertTrue(instance.isEmpty());
    }
}