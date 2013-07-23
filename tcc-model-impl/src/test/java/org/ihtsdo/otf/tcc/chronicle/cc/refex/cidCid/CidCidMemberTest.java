package org.ihtsdo.otf.tcc.chronicle.cc.refex.cidCid;

import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_nid.NidNidRevision;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_nid.NidNidMember;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.ihtsdo.otf.tcc.chronicle.cc.component.RevisionSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CidCidMemberTest {

    private NidNidMember testObj1;
    private NidNidMember testObj2;
    private NidNidMember testObj3;
    
    
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
        testObj1 = makeTestObject1();
        testObj2 = makeTestObject1();
        
        // Put testComponent1 in a collection 
        Set<NidNidMember> coll = new java.util.HashSet<>();
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

    private NidNidMember makeTestObject1() {
        
        // Create an object to test... 
        NidNidMember obj = new NidNidMember();
        obj.setC1Nid(1);
        obj.setC2Nid(1);
        obj.revisions = new RevisionSet(1);
        NidNidRevision rev = new NidNidRevision(); 
        rev.primordialComponent = obj; 
        rev.stamp = 1;
        obj.revisions.add(rev); 

         
        obj.nid = 1;
        obj.primordialStamp = 1; 
        
        return obj; 
    }

    private NidNidMember makeTestObject2() {
        
        // Create an object to test... 
        NidNidMember obj = new NidNidMember();
        obj.setC1Nid(2);
        obj.setC2Nid(2);
        obj.revisions = new RevisionSet(1);
        NidNidRevision rev = new NidNidRevision(); 
        rev.primordialComponent = obj; 
        rev.stamp = 1;
        obj.revisions.add(rev); 

         
        obj.nid = 2;
        obj.primordialStamp = 2; 
        
        return obj; 
    }
}

