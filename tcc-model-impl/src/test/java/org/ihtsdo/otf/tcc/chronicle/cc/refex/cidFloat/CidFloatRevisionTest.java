package org.ihtsdo.otf.tcc.chronicle.cc.refex.cidFloat;

import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_float.NidFloatMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_float.NidFloatRevision;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CidFloatRevisionTest {

    private NidFloatRevision testObj1;
    private NidFloatRevision testObj2;
    private NidFloatRevision testObj3;
    
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testEqualsObject() {
        // The contract of the equals method in Object 
        // specifies that equals must implement an equivalence 
        // relation on non-null objects:
        
        // Make all the test objects be the same 
        testObj1 = makeTestObject1();
        testObj2 = makeTestObject1();
        testObj3 = makeTestObject1();

        // Test for equality (2 objects created the same) 
        assertTrue(testObj1.equals(testObj2)); 
        
        // It is reflexive: 
        // for any non-null value x, the expression x.equals(x) should return true.
        assertTrue(testObj1.equals(testObj1)); 
        
        // It is symmetric: 
        // for any non-null values x and y, the expression x.equals(y) should return true 
        // if and only if y.equals(x) returns true.
        assertTrue(testObj1.equals(testObj2) && testObj2.equals(testObj1));
        
        // It is transitive: 
        // for any non-null values x, y, and z, if x.equals(y) returns true and 
        // y.equals(z) returns true, then x.equals(z) should return true.
        assertTrue(testObj1.equals(testObj2) && testObj2.equals(testObj3)
            && testObj3.equals(testObj1));
        
        // It is consistent: 
        // for any non-null values x and y, multiple invocations of x.equals(y) 
        // should consistently return true or consistently return false, 
        // provided no information used in equals comparisons on the objects is modified.
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 
        assertTrue(testObj1.equals(testObj2)); 

        // For any non-null value x, x.equals(null) should return false.
        assertFalse(testObj1.equals(null)); 
       
    }

    @Test
    public void testEqualsInACollection() {
        // Make both the objects be the same 
        try {
            testObj1 = makeTestObject1();
            testObj2 = makeTestObject1();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Put testComponent1 in a collection 
        Set<NidFloatRevision> coll = new java.util.HashSet<>();
        coll.add(testObj1);

        // Test for the presence of testComponent1 by using the  
        // equivalent object testComponent2
        assertTrue(coll.contains(testObj2));         

    }

    @Test
    public void testDifferentObjectsNotEqual() {
        // Make two different objects 
        try {
            testObj1 = makeTestObject1();
            testObj2 = makeTestObject2();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Test that they are not equal
        assertFalse(testObj1.equals(testObj2));         
        assertFalse(testObj2.equals(testObj1));         

    }

    private NidFloatRevision makeTestObject1() {
        
        // Create an object to test... 
        NidFloatRevision obj = new NidFloatRevision();
        obj.setC1Nid(1);
        obj.setFloatValue((float) 1.1);
        obj.stamp = 1; 
        
        NidFloatMember member = new NidFloatMember();
        member.revisions = null;
         
        member.nid = 1;
        member.primordialStamp = 1; 
        member.setC1Nid(1);
        member.setSTAMP(1);
        member.setFloatValue((float) 1.1);

        obj.primordialComponent = member; 

        return obj; 
    }

    private NidFloatRevision makeTestObject2() {
        
        // Create an object to test... 
        NidFloatRevision obj = new NidFloatRevision();
        obj.setC1Nid(2);
        obj.setFloatValue((float) 2.2);
        obj.stamp = 2; 
        
        NidFloatMember member = new NidFloatMember();
        member.revisions = null;
         
        member.nid = 2;
        member.primordialStamp = 2; 
        member.setC1Nid(2);
        member.setFloatValue((float) 2.2);
        member.setSTAMP(2);

        obj.primordialComponent = member; 

        return obj; 
    }
}

