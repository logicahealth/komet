/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.model;

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
public class DataBufferTest {
    
    public DataBufferTest() {
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
     * Test of getPosition method, of class DataBuffer.
     */
    @Test
    public void testGetPosition() {
        System.out.println("getPosition");
        DataBuffer instance = new DataBuffer(10);
        int expResult = 0;
        int result = instance.getPosition();
        assertEquals(expResult, result);
    }

    /**
     * Test of setPosition method, of class DataBuffer.
     */
    @Test
    public void testSetPosition() {
        System.out.println("setPosition");
        DataBuffer instance = new DataBuffer(10);
        int expResult = 5;
        instance.setPosition(expResult);
        int result = instance.getPosition();
        assertEquals(expResult, result);
    }

    /**
     * Test of getData method, of class DataBuffer.
     */
    @Test
    public void testGetData() {
        System.out.println("getData");
        DataBuffer instance = new DataBuffer(10);
        byte[] expResult = new byte[10];
        byte[] result = instance.getData();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of reset method, of class DataBuffer.
     */
    @Test
    public void testReset() {
        System.out.println("reset");
        DataBuffer instance = new DataBuffer(10);
        int expResult = 5;
        instance.setPosition(expResult);
        instance.reset();
        int result = instance.getPosition();
        assertEquals(0, result);
    }

    /**
     * Test of getByte method, of class DataBuffer.
     */
    @Test
    public void testGetByte() {
        System.out.println("getByte");
        DataBuffer instance = new DataBuffer(10);
        byte expResult = 5;
        instance.putByte(expResult);
        instance.reset();
        byte result = instance.getByte();
        assertEquals(expResult, result);
    }

    /**
     * Test of putByte method, of class DataBuffer.
     */
    @Test
    public void testPutByte() {
        System.out.println("putByte");
        DataBuffer instance = new DataBuffer(10);
        byte expResult = 23;
        instance.putByte(expResult);
        instance.reset();
        byte result = instance.getByte();
        assertEquals(expResult, result);
    }

    /**
     * Test of getShort method, of class DataBuffer.
     */
    @Test
    public void testGetShort() {
        System.out.println("getShort");
        DataBuffer instance = new DataBuffer(10);
        short expResult = 19;
        instance.putShort(expResult);
        instance.reset();
        short result = instance.getShort();
        assertEquals(expResult, result);
    }

    /**
     * Test of putShort method, of class DataBuffer.
     */
    @Test
    public void testPutShort() {
        System.out.println("putShort");
        DataBuffer instance = new DataBuffer(10);
        short expResult = 37;
        instance.putShort(expResult);
        instance.reset();
        short result = instance.getShort();
        assertEquals(expResult, result);
    }

    /**
     * Test of getChar method, of class DataBuffer.
     */
    @Test
    public void testGetChar() {
        System.out.println("getChar");
        DataBuffer instance = new DataBuffer(10);
        char expResult = ' ';
        instance.putChar(expResult);
        instance.reset();
        char result = instance.getChar();
        assertEquals(expResult, result);
    }

    /**
     * Test of putChar method, of class DataBuffer.
     */
    @Test
    public void testPutChar() {
        System.out.println("putChar");
        DataBuffer instance = new DataBuffer(10);
        char expResult = '&';
        instance.putChar(expResult);
        instance.reset();
        char result = instance.getChar();
        assertEquals(expResult, result);
    }

    /**
     * Test of getInt method, of class DataBuffer.
     */
    @Test
    public void testGetInt() {
        System.out.println("getInt");
        DataBuffer instance = new DataBuffer(10);
        int expResult = 9;
        instance.putInt(expResult);
        instance.reset();
        int result = instance.getInt();
        assertEquals(expResult, result);
    }

    /**
     * Test of putInt method, of class DataBuffer.
     */
    @Test
    public void testPutInt() {
        System.out.println("putInt");
        DataBuffer instance = new DataBuffer(10);
        int expResult = 19;
        instance.putInt(expResult);
        instance.reset();
        int result = instance.getInt();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFloat method, of class DataBuffer.
     */
    @Test
    public void testGetFloat() {
        System.out.println("getFloat");
        DataBuffer instance = new DataBuffer(10);
        float expResult = 7.3F;
        instance.putFloat(expResult);
        instance.reset();
        float result = instance.getFloat();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of putFloat method, of class DataBuffer.
     */
    @Test
    public void testPutFloat() {
        System.out.println("putFloat");
        DataBuffer instance = new DataBuffer(10);
        float expResult = 9.14F;
        instance.putFloat(expResult);
        instance.reset();
        float result = instance.getFloat();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getLong method, of class DataBuffer.
     */
    @Test
    public void testGetLong() {
        System.out.println("getLong");
        DataBuffer instance = new DataBuffer(10);
        long expResult = Long.MAX_VALUE - 5;
        instance.putLong(expResult);
        instance.reset();
        long result = instance.getLong();
        assertEquals(expResult, result);
    }

    /**
     * Test of putLong method, of class DataBuffer.
     */
    @Test
    public void testPutLong() {
        System.out.println("putLong");
        DataBuffer instance = new DataBuffer(10);
        long expResult = Long.MIN_VALUE + 7;
        instance.putLong(expResult);
        instance.reset();
        long result = instance.getLong();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDouble method, of class DataBuffer.
     */
    @Test
    public void testGetDouble() {
        System.out.println("getDouble");
        DataBuffer instance = new DataBuffer(10);
        double expResult = Double.MAX_VALUE;
        instance.putDouble(expResult);
        instance.reset();
        double result = instance.getDouble();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of putDouble method, of class DataBuffer.
     */
    @Test
    public void testPutDouble() {
        System.out.println("putDouble");
        DataBuffer instance = new DataBuffer(10);
        double expResult = Double.MIN_VALUE;
        instance.putDouble(expResult);
        instance.reset();
        double result = instance.getDouble();
        assertEquals(expResult, result, 0.0);
   }

    /**
     * Test of trimToSize method, of class DataBuffer.
     */
    @Test
    public void testCompress() {
        System.out.println("compress");
        DataBuffer instance = new DataBuffer(10);
        instance.putInt(5);
        instance.trimToSize();
        assertEquals(instance.getData().length, 4);
    }

    /**
     * Test of put method, of class DataBuffer.
     */
    @Test
    public void testPut() {
        System.out.println("put");
        byte[] src = new byte[]{3,2,9};
        int offset = 0;
        int length = 3;
        DataBuffer instance = new DataBuffer(10);
        instance.put(src);
        instance.reset();
        byte[] result = new byte[3];
        instance.get(result, offset, length);
        assertArrayEquals(src, result);

    }

    /**
     * Test of get method, of class DataBuffer.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        byte[] src = new byte[]{1,2,3};
        int offset = 0;
        int length = 3;
        DataBuffer instance = new DataBuffer(10);
        instance.put(src);
        instance.reset();
        byte[] result = new byte[3];
        instance.get(result, offset, length);
        assertArrayEquals(src, result);
    }

    /**
     * Test of putUTF method, of class DataBuffer.
     */
    @Test
    public void testPutUTF() {
        System.out.println("putUTF");
        String str = "Now is the time for all † ‡ °";
        DataBuffer instance = new DataBuffer(10);
        instance.putUTF(str);
        instance.reset();
        String result = instance.readUTF();
        assertEquals(str, result);
    }

    /**
     * Test of readUTF method, of class DataBuffer.
     */
    @Test
    public void testReadUTF() {
        System.out.println("readUTF");
        String str = "Fine for all ˜ Â the time for all † ‡ °";
        DataBuffer instance = new DataBuffer(10);
        instance.putUTF(str);
        instance.reset();
        String result = instance.readUTF();
        assertEquals(str, result);
    }
    @Test
    public void testEdgeUTF() {
        System.out.println("readUTF");
        String str = "is-a";
        DataBuffer instance = new DataBuffer(10);
        instance.putShort((short) 4);
        instance.putInt(13);
        instance.putInt(16);
        instance.putUTF(str);
        instance.putInt(11);
        instance.reset();
        assertEquals(4, instance.getShort());
        assertEquals(13, instance.getInt());
        assertEquals(16, instance.getInt());
        String result = instance.readUTF();
        assertEquals(str, result);
        assertEquals(11, instance.getInt());
    }
  
}
