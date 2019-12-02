/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.api.externalizable;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;

import java.nio.ReadOnlyBufferException;

import java.util.*;
import java.util.concurrent.locks.StampedLock;

import javax.xml.bind.DatatypeConverter;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.Status;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptSpecification;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ByteArrayDataBuffer.
 *
 * @author kec
 */
public class ByteArrayDataBuffer {
   /** The Constant MAX_DATA_SIZE. */
   private static final int MAX_DATA_SIZE = Integer.MAX_VALUE - 16;

   /** The Constant DEFAULT_SIZE. */
   private static final int DEFAULT_SIZE = 1024;

   /** The Constant FALSE. */
   protected static final byte FALSE = 0;

   /** The Constant TRUE. */
   protected static final byte TRUE = 1;

   //~--- fields --------------------------------------------------------------

   /** The position. */
   protected int position = 0;

   /** The read only. */
   protected boolean readOnly = false;

   /** The object data format version. */
   protected byte objectDataFormatVersion = 0;

   /** The external data. */
   protected boolean externalData = false;

   /**
    * The StampedLock is to ensure the backing array does not grow underneath a
    * concurrent operation. The locks do not prevent concurrent threads from
    * reading or writing to the same fields.
    */
   protected final StampedLock sl = new StampedLock();

   /** The used. */
   protected int used = 0;

   /** The position start. */
   protected final int positionStart;

   /** The identifier service. */
   protected IdentifierService identifierService;

   /** The stamp service. */
   protected StampService stampService;

   /** The data. */
   private byte[] data;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new byte array data buffer.
    */
   public ByteArrayDataBuffer() {
      this(DEFAULT_SIZE);
   }

   /**
    * Instantiates a new byte array data buffer.
    *
    * @param data the data
    */
   public ByteArrayDataBuffer(byte[] data) {
      this(data, 0);
   }

   /**
    * Instantiates a new byte array data buffer.
    *
    * @param size the size
    */
   public ByteArrayDataBuffer(int size) {
      this.data          = new byte[size];
      this.positionStart = 0;
   }

   /**
    * Instantiates a new byte array data buffer.
    *
    * @param data the data
    * @param positionStart the position start
    */
   public ByteArrayDataBuffer(byte[] data, int positionStart) {
      this.data          = data;
      this.used          = data.length;
      this.positionStart = positionStart;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Append.
    *
    * @param db the db
    * @param position the position
    * @param length the length
    */
   public void append(ByteArrayDataBuffer db, int position, int length) {
      ensureSpace(this.position + length);
      System.arraycopy(db.data, position, this.data, this.position, length);
      this.position += length;
      this.used = Math.max(used, this.position);
   }

   /**
    *  Makes this buffer ready for a new sequence of put operations:
    *  It sets the limit to positionStart, and the position to positionStart.
    *
    * @return the byte array data buffer
    */
   public ByteArrayDataBuffer clear() {
      this.used     = 0;
      this.position = this.positionStart;
      return this;
   }

   /**
    * Makes this buffer ready for a new sequence of get operations:
    * It sets the limit to the current position and then sets the position to zero.
    *
    * @return the byte array data buffer
    */
   public ByteArrayDataBuffer flip() {
      getLimit();
      this.position = this.positionStart;
      return this;
   }

   /**
    * New wrapper.
    *
    * @return the byte array data buffer
    */
   public ByteArrayDataBuffer newWrapper() {
      final ByteArrayDataBuffer newWrapper = new ByteArrayDataBuffer(this.data);

      newWrapper.readOnly = true;
      newWrapper.position = 0;
      newWrapper.used     = this.used;
      return newWrapper;
   }

   /**
    * Put.
    *
    * @param src the src
    */
   public void put(byte[] src) {
      put(src, 0, src.length);
   }

   /**
    * Put.
    *
    * @param src the src
    * @param offset the offset
    * @param length the length
    */
   public void put(byte[] src, int offset, int length) {
      ensureSpace(this.position + length);

      long lockStamp = this.sl.tryOptimisticRead();

      System.arraycopy(src, offset, this.data, this.position, length);

      if (!this.sl.validate(lockStamp)) {
         lockStamp = this.sl.readLock();

         try {
            System.arraycopy(src, offset, this.data, this.position, length);
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }

      this.position += length;
      this.used = Math.max(used, this.position);
   }

   /**
    * Put boolean.
    *
    * @param x the x
    */
   public void putBoolean(boolean x) {
      if (x) {
         putByte(TRUE);
      } else {
         putByte(FALSE);
      }
   }

   /**
    * Put byte.
    *
    * @param x the x
    */
   public void putByte(byte x) {
      ensureSpace(this.position + 1);

      long lockStamp = this.sl.tryOptimisticRead();

      this.data[this.position] = x;

      if (!this.sl.validate(lockStamp)) {
         lockStamp = this.sl.readLock();

         try {
            this.data[this.position] = x;
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }

      this.position += 1;
      this.used = Math.max(used, this.position);
   }

   /**
    * Put byte array field.
    *
    * @param array the array
    */
   public void putByteArrayField(byte[] array) {
      putInt(array.length);
      put(array, 0, array.length);
   }

   /**
    * Put char.
    *
    * @param x the x
    */
   public void putChar(char x) {
      putShort((short) x);
   }

   /**
    * Put double.
    *
    * @param d the d
    */
   public void putDouble(double d) {
      putLong(Double.doubleToLongBits(d));
   }

   /**
    * Put float.
    *
    * @param f the f
    */
   public void putFloat(float f) {
      putInt(Float.floatToRawIntBits(f));
   }

   /**
    * Put int.
    *
    * @param x the x
    */
   public void putInt(int x) {
      ensureSpace(this.position + 4);

      long lockStamp = this.sl.tryOptimisticRead();

      this.data[this.position]     = (byte) (x >> 24);
      this.data[this.position + 1] = (byte) (x >> 16);
      this.data[this.position + 2] = (byte) (x >> 8);
      this.data[this.position + 3] = (byte) (x);

      if (!this.sl.validate(lockStamp)) {
         lockStamp = this.sl.readLock();

         try {
            this.data[this.position]     = (byte) (x >> 24);
            this.data[this.position + 1] = (byte) (x >> 16);
            this.data[this.position + 2] = (byte) (x >> 8);
            this.data[this.position + 3] = (byte) (x);
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }

      this.position += 4;
      this.used = Math.max(used, this.position);
   }

   /**
    * Put int array.
    *
    * @param src the src
    */
   public void putIntArray(int[] src) {
      putInt(src.length);
      ensureSpace(this.position + (src.length * 4));

      long      lockStamp        = this.sl.tryOptimisticRead();
      final int startingPosition = this.position;

      putIntArrayIntoData(src);

      if (!this.sl.validate(lockStamp)) {
         lockStamp     = this.sl.readLock();
         this.position = startingPosition;

         try {
            putIntArrayIntoData(src);
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }
      this.used = Math.max(used, this.position);
   }

   /**
    * Put long.
    *
    * @param x the x
    */
   public void putLong(long x) {
      ensureSpace(this.position + 8);

      long lockStamp = this.sl.tryOptimisticRead();

      this.data[this.position]     = (byte) (x >> 56);
      this.data[this.position + 1] = (byte) (x >> 48);
      this.data[this.position + 2] = (byte) (x >> 40);
      this.data[this.position + 3] = (byte) (x >> 32);
      this.data[this.position + 4] = (byte) (x >> 24);
      this.data[this.position + 5] = (byte) (x >> 16);
      this.data[this.position + 6] = (byte) (x >> 8);
      this.data[this.position + 7] = (byte) (x);

      if (!this.sl.validate(lockStamp)) {
         lockStamp = this.sl.readLock();

         try {
            this.data[this.position]     = (byte) (x >> 56);
            this.data[this.position + 1] = (byte) (x >> 48);
            this.data[this.position + 2] = (byte) (x >> 40);
            this.data[this.position + 3] = (byte) (x >> 32);
            this.data[this.position + 4] = (byte) (x >> 24);
            this.data[this.position + 5] = (byte) (x >> 16);
            this.data[this.position + 6] = (byte) (x >> 8);
            this.data[this.position + 7] = (byte) (x);
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }

      this.position += 8;
      this.used = Math.max(used, this.position);
   }

   /**
    * Put nid.
    *
    * @param nid the nid
    */
   public void putNid(int nid) {
      if (this.externalData) {
         final UUID uuid = this.identifierService.getUuidPrimordialForNid(nid);
            putLong(uuid.getMostSignificantBits());
            putLong(uuid.getLeastSignificantBits());
      } else {
         putInt(nid);
      }
   }

   /**
    * Put short.
    *
    * @param x the x
    */
   public void putShort(short x) {
      ensureSpace(this.position + 2);

      long lockStamp = this.sl.tryOptimisticRead();

      this.data[this.position]     = (byte) (x >> 8);
      this.data[this.position + 1] = (byte) (x);

      if (!this.sl.validate(lockStamp)) {
         lockStamp = this.sl.readLock();

         try {
            this.data[this.position]     = (byte) (x >> 8);
            this.data[this.position + 1] = (byte) (x);
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }

      this.position += 2;
      this.used = Math.max(used, this.position);
   }

   /**
    * Put stamp sequence.
    *
    * @param stampSequence the stamp sequence
    */
   public void putStampSequence(int stampSequence) {
      if (this.externalData) {
         StampUniversal.get(stampSequence)
                       .writeExternal(this);
      } else {
         putInt(stampSequence);
      }
   }

   /**
    * Put UTF.
    *
    * @param str the str
    */
   public void putUTF(String str) {
      final int strlen = str.length();
      int       utflen = 0;
      int       c;
      int       count = 0;

      for (int i = 0; i < strlen; i++) {
         c = str.charAt(i);

         if ((c >= 1) && (c <= 127)) {
            utflen++;
         } else if (c > 2047) {
            utflen += 3;
         } else {
            utflen += 2;
         }
      }

      final byte[] bytearr = new byte[utflen];
      int          i       = 0;

      for (; i < strlen; i++) {
         c = str.charAt(i);

         if (!((c >= 1) && (c <= 127))) {
            break;
         }

         bytearr[count++] = (byte) c;
      }

      for (; i < strlen; i++) {
         c = str.charAt(i);

         if ((c >= 1) && (c <= 127)) {
            bytearr[count++] = (byte) c;
         } else if (c > 2047) {
            bytearr[count++] = (byte) (224 | ((c >> 12) & 15));
            bytearr[count++] = (byte) (128 | ((c >> 6) & 63));
            bytearr[count++] = (byte) (128 | (c & 63));
         } else {
            bytearr[count++] = (byte) (192 | ((c >> 6) & 31));
            bytearr[count++] = (byte) (128 | (c & 63));
         }
      }

      putInt(utflen);
      put(bytearr, 0, utflen);
   }

   /**
    * Put uuid.
    *
    * @param uuid the uuid
    */
   public void putUuid(UUID uuid) {
      putLong(uuid.getMostSignificantBits());
      putLong(uuid.getLeastSignificantBits());
   }

   public int getUsed() {
       return this.used;
   }
   
   /**
    * Read UTF.
    *
    * @return the string
    */
   public final String getUTF() {
      final int[]  positionArray = new int[] { this.position };
      final String result        = getUTF(positionArray);

      this.position = positionArray[0];
      return result;
   }

   /**
    * Read UTF.
    *
    * @param position the position
    * @return the string
    */
   public final String getUTF(int[] position) {
      final int    utflen  = getInt(position[0]);
      final byte[] bytearr = new byte[utflen];
      final char[] chararr = new char[utflen];
      int          c, char2, char3;
      int          count         = 0;
      int          chararr_count = 0;

      get(position[0] + 4, bytearr, 0, utflen);
      position[0] = position[0] + 4 + utflen;

      while (count < utflen) {
         c = bytearr[count] & 0xff;

         if (c > 127) {
            break;
         }

         count++;
         chararr[chararr_count++] = (char) c;
      }

      while (count < utflen) {
         try {
            c = bytearr[count] & 0xff;

            switch (c >> 4) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:

               /* 0xxxxxxx */
               count++;
               chararr[chararr_count++] = (char) c;
               break;

            case 12:
            case 13:

               /* 110x xxxx   10xx xxxx */
               count += 2;

               if (count > utflen) {
                  throw new UTFDataFormatException("malformed input: partial character at end");
               }

               char2 = bytearr[count - 1];

               if ((char2 & 0xC0) != 0x80) {
                  throw new UTFDataFormatException("malformed input around byte " + count);
               }

               chararr[chararr_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
               break;

            case 14:

               /* 1110 xxxx  10xx xxxx  10xx xxxx */
               count += 3;

               if (count > utflen) {
                  throw new UTFDataFormatException("malformed input: partial character at end");
               }

               char2 = bytearr[count - 2];
               char3 = bytearr[count - 1];

               if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                  throw new UTFDataFormatException("malformed input around byte " + (count - 1));
               }

               chararr[chararr_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | (char3 & 0x3F));
               break;

            default:

               /* 10xx xxxx,  1111 xxxx */
               throw new UTFDataFormatException("malformed input around byte " + count);
            }
         } catch (final UTFDataFormatException ex) {
            throw new RuntimeException(ex);
         }
      }

      // The number of chars produced may be less than utflen
      return new String(chararr, 0, chararr_count);
   }

   /**
    * Makes this buffer ready for re-reading the data that it already contains:
    * It leaves the limit unchanged and sets the position to the positionStart.
    *
    * @return the byte array data buffer
    */
   public ByteArrayDataBuffer rewind() {
      this.position = this.positionStart;
      return this;
   }

   /**
    * Slice.
    *
    * @return the byte array data buffer
    */
   public ByteArrayDataBuffer slice() {
      final ByteArrayDataBuffer slice = new ByteArrayDataBuffer(this.data, this.position);

      slice.readOnly = true;
      slice.position = this.position;
      slice.used     = this.used;
      return slice;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ByteArrayDataBuffer{" + "position=" + this.position + ", positionStart=" + this.positionStart +
             ", readOnly=" + this.readOnly + ", objectDataFormatVersion=" + this.objectDataFormatVersion +
             ", externalData=" + this.externalData + ", used=" + this.used + ", data=" +
             DatatypeConverter.printHexBinary(this.data) + '}';
   }

   /**
    * Trim to size.
    */
   public void trimToSize() {
      if (this.readOnly) {
         throw new ReadOnlyBufferException();
      }

      final long lockStamp = this.sl.writeLock();

      try {
         if ((this.position < this.data.length) && (this.used < this.data.length)) {
            final int    newSize = Math.max(this.position, this.used);
            final byte[] newData = new byte[newSize];

            System.arraycopy(this.data, 0, newData, 0, newSize);
            this.data = newData;
         }
      } finally {
         this.sl.unlockWrite(lockStamp);
      }
   }

   /**
    * Ensure space.
    *
    * @param minSpace the min space
    */
   private void ensureSpace(int minSpace) {
      if (this.readOnly) {
         throw new ReadOnlyBufferException();
      }

      if (minSpace > this.data.length) {
         final long lockStamp = this.sl.writeLock();

         try {
            while (minSpace > this.data.length) {
               int newCapacity = this.data.length << 1;

               if (newCapacity > MAX_DATA_SIZE) {
                  newCapacity = MAX_DATA_SIZE;
               }

               this.data = Arrays.copyOf(this.data, newCapacity);
            }
         } finally {
            this.sl.unlockWrite(lockStamp);
         }
      }
   }

   /**
    * Put int array into data.
    *
    * @param src the src
    */
   private void putIntArrayIntoData(int[] src) {
      for (final int anInt: src) {
         this.data[this.position]     = (byte) (anInt >> 24);
         this.data[this.position + 1] = (byte) (anInt >> 16);
         this.data[this.position + 2] = (byte) (anInt >> 8);
         this.data[this.position + 3] = (byte) (anInt);
         this.position                += 4;
      }
      this.used = Math.max(used, this.position);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the boolean.
    *
    * @return the boolean
    */
   public boolean getBoolean() {
      return getByte() != FALSE;
   }

   /**
    * Gets the boolean.
    *
    * @param position the position
    * @return the boolean
    */
   public boolean getBoolean(int position) {
      return getByte(position) != FALSE;
   }

   /**
    * Gets the byte.
    *
    * @return the byte
    */
   public byte getByte() {
      final byte result = getByte(this.position);

      this.position += 1;
      return result;
   }

   /**
    * Gets the byte.
    *
    * @param position the position
    * @return the byte
    */
   public byte getByte(int position) {
      long lockStamp = this.sl.tryOptimisticRead();
      byte result    = this.data[position];

      if (!this.sl.validate(lockStamp)) {
         lockStamp = this.sl.readLock();

         try {
            result = this.data[position];
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }

      return result;
   }

   /**
    * Gets the byte array field.
    *
    * @return a byte[] written to the ByteArrayDataBuffer. Does not return the entire
    * data buffer as an array.
    */
   public byte[] getByteArrayField() {
      final int    length    = getInt();
      long         lockStamp = this.sl.tryOptimisticRead();
      final byte[] results   = new byte[length];

      System.arraycopy(this.data, this.position, results, 0, length);

      if (!this.sl.validate(lockStamp)) {
         lockStamp = this.sl.readLock();

         try {
            System.arraycopy(this.data, this.position, results, 0, length);
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }

      this.position += length;
      return results;
   }

   /**
    * The current capacity of the buffer. The buffer will grow if necessary, so the current capacity may not
    * reflect the maximum size that the buffer may obtain.
    * @return The currently allocated size of the buffer.
    */
   public int getCapacity() {
      return this.data.length;
   }

   /**
    * Gets the char.
    *
    * @return the char
    */
   public char getChar() {
      final char result = getChar(this.position);

      this.position += 2;
      return result;
   }

   /**
    * Gets the char.
    *
    * @param position the position
    * @return the char
    */
   public char getChar(int position) {
      long lockStamp = this.sl.tryOptimisticRead();
      char result    = (char) ((this.data[position] << 8) | (this.data[position + 1] & 0xff));

      if (!this.sl.validate(lockStamp)) {
         lockStamp = this.sl.readLock();

         try {
            result = (char) ((this.data[position] << 8) | (this.data[position + 1] & 0xff));
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }

      return result;
   }

   /**
    * Gets the data.
    *
    * @return the byte[] that backs this buffer.
    */
   public byte[] getData() {
      return this.data;
   }

   /**
    * Gets the double.
    *
    * @return the double
    */
   public double getDouble() {
      return Double.longBitsToDouble(getLong());
   }

   /**
    * Gets the double.
    *
    * @param position the position
    * @return the double
    */
   public double getDouble(int position) {
      return Double.longBitsToDouble(getLong(position));
   }

   /**
    * Checks if external data.
    *
    * @return true, if external data
    */
   public boolean isExternalData() {
      return this.externalData;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the external data.
    *
    * @param externalData the new external data
    */
   public void setExternalData(boolean externalData) {
      if (externalData) {
         this.identifierService = Get.identifierService();
         this.stampService      = Get.stampService();
      }

      this.externalData = externalData;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the float.
    *
    * @return the float
    */
   public float getFloat() {
      return Float.intBitsToFloat(getInt());
   }

   /**
    * Gets the float.
    *
    * @param position the position
    * @return the float
    */
   public float getFloat(int position) {
      return Float.intBitsToFloat(getInt(position));
   }

   /**
    * Gets the.
    *
    * @param src the src
    * @param offset the offset
    * @param length the length
    */
   public void get(byte[] src, int offset, int length) {
      get(this.position, src, offset, length);
      this.position += length;
   }

   /**
    * Gets the.
    *
    * @param position the position
    * @param src the src
    * @param offset the offset
    * @param length the length
    */
   public void get(int position, byte[] src, int offset, int length) {
      long lockStamp = this.sl.tryOptimisticRead();

      System.arraycopy(this.data, position, src, offset, length);

      if (!this.sl.validate(lockStamp)) {
         lockStamp = this.sl.readLock();

         try {
            System.arraycopy(this.data, position, src, offset, length);
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }
   }

   /**
    * Gets the int.
    *
    * @return the int
    */
   public int getInt() {
      final int result = getInt(this.position);

      this.position += 4;
      return result;
   }

   /**
    * Gets the int.
    *
    * @param position the position
    * @return the int
    */
   public int getInt(int position) {
      long lockStamp = this.sl.tryOptimisticRead();
      int result = getInt(this.data, position);

      if (!this.sl.validate(lockStamp)) {
         lockStamp = this.sl.readLock();

         try {
            result = getInt(this.data, position);
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }

      return result;
   }

   public static int getInt(byte[] data, int position) {
      if (position < 0) {
         throw new IllegalStateException("Error position: " + position);
      }
       return (((data[position]) << 24) | ((data[position + 1] & 0xff) << 16)
                      | ((data[position + 2] & 0xff) << 8) | ((data[position + 3] & 0xff)));
   }
   /**
    * Gets the int array.
    *
    * @return the int array
    */
   public int[] getIntArray() {
      int length = getInt();
      if (length < 0) {
          throw new IllegalStateException("Negative int array size: " + length);
      }
      final int[] array            = new int[length];
      final int   startingPosition = this.position;
      long        lockStamp        = this.sl.tryOptimisticRead();

      for (int i = 0; i < array.length; i++) {
         array[i] = (((this.data[this.position]) << 24) | ((this.data[this.position + 1] & 255) << 16)
                     | ((this.data[this.position + 2] & 255) << 8) | ((this.data[this.position + 3] & 255)));
         this.position += 4;
      }

      if (!this.sl.validate(lockStamp)) {
         lockStamp     = this.sl.readLock();
         this.position = startingPosition;

         try {
            for (int i = 0; i < array.length; i++) {
               array[i] = (((this.data[this.position]) << 24) | ((this.data[this.position + 1] & 255) << 16)
                           | ((this.data[this.position + 2] & 255) << 8) | ((this.data[this.position + 3] & 255)));
               this.position += 4;
            }
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }

      return array;
   }

   public void putUuidArray(UUID[] array) {
      putInt(array.length);
      for (UUID uuid: array) {
         putUuid(uuid);
      }
   }

   public UUID[] getUuidArray() {
      int length = getInt();
      if (length < 0) {
         throw new IllegalStateException("Negative array size: " + length);
      }
      final UUID[] array            = new UUID[length];
      final int   startingPosition = this.position;
      long        lockStamp        = this.sl.tryOptimisticRead();

      for (int i = 0; i < array.length; i++) {
         array[i] = getUuid();
      }

      if (!this.sl.validate(lockStamp)) {
         lockStamp     = this.sl.readLock();
         this.position = startingPosition;

         try {
            for (int i = 0; i < array.length; i++) {
               array[i] = getUuid();
            }
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }

      return array;
   }

   /**
    * The limit is the index of the first element that should not be read or written, relative to the position start.
    * It represents the end of valid data, and is never negative and is never greater than its capacity.
    * @return the position after the end of written data in the buffer, relative to the position start.
    */
   public int getLimit() {
      this.used = Math.max(this.used, this.position);
      return this.used - this.positionStart;
   }

   /**
    * Gets the long.
    *
    * @return the long
    */
   public long getLong() {
      final long result = getLong(this.position);

      this.position += 8;
      return result;
   }

   /**
    * Gets the long.
    *
    * @param position the position
    * @return the long
    */
   public long getLong(int position) {
      long lockStamp = this.sl.tryOptimisticRead();
      long result    = getLongResult(position);

      if (!this.sl.validate(lockStamp)) {
         lockStamp = this.sl.readLock();

         try {
            result = getLongResult(position);
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }

      return result;
   }

   /**
    * Gets the long result.
    *
    * @param position the position
    * @return the long result
    */
   private long getLongResult(int position) {
      long result;
      if (this.data.length <= position + 7) {
         throw new ArrayIndexOutOfBoundsException();
      }

      result = ((((long) this.data[position]) << 56) | (((long) this.data[position + 1] & 0xff) << 48)
                | (((long) this.data[position + 2] & 0xff) << 40) | (((long) this.data[position + 3] & 0xff) << 32)
                | (((long) this.data[position + 4] & 0xff) << 24) | (((long) this.data[position + 5] & 0xff) << 16)
                | (((long) this.data[position + 6] & 0xff) << 8) | (((long) this.data[position + 7] & 0xff)));
      return result;
   }

   /**
    * Gets the nid.
    *
    * @return the nid
    */
   public int getNid() {
      if (this.externalData) {
         UUID uuid = new UUID(getLong(), getLong());
         if (this.identifierService.hasUuid(uuid)) {
            return this.identifierService.getNidForUuids(uuid);
         }
         else {
            return this.identifierService.assignNid(uuid);
         }
      }

      return getInt();
   }

   public int[] getNidArray() {
      if (this.externalData) {
         int length = this.getInt();
         int[] nids = new int[length];
         for (int i = 0; i < length; i++) {
            UUID uuid = new UUID(getLong(), getLong());
            if (this.identifierService.hasUuid(uuid)) {
               nids[i] = this.identifierService.getNidForUuids(uuid);
            } else {
               nids[i] = this.identifierService.assignNid(uuid);
            }
         }
         return nids;
      }
      return getIntArray();
   }

   public void putNidArray(int[] nids) {

      if (this.externalData) {
         putInt(nids.length);
         for (int i = 0; i < nids.length; i++) {
            putUuid(Get.identifierService().getUuidPrimordialForNid(nids[i]));
         }
      } else {
         putIntArray(nids);
      }
   }

   public void putNidArray(Collection<Integer> nidCollection) {

      int[] nids  = new int[nidCollection.size()];
      int index = 0;
      for (Integer nid: nidCollection) {
         nids[index++] = nid;
      }
      if (this.externalData) {
         putInt(nids.length);
         for (int i = 0; i < nids.length; i++) {
            putUuid(Get.identifierService().getUuidPrimordialForNid(nids[i]));
         }
      } else {
         putIntArray(nids);
      }
   }

   /**
    * Gets the object data format version.
    *
    * @return the object data format version
    */
   public byte getObjectDataFormatVersion() {
      return this.objectDataFormatVersion;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the object data format version.
    *
    * @param objectDataFormatVersion the new object data format version
    */
   public void setObjectDataFormatVersion(byte objectDataFormatVersion) {
      this.objectDataFormatVersion = objectDataFormatVersion;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * The index of the next element to be read or written, relative to the position start.
    * The position is never negative and is never greater than its limit.
    * @return the index of the next element to be read or written.
    */
   public int getPosition() {
      return this.position - this.positionStart;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set the index of the next element to be read or written, relative to the position start.
    * @param position the index of the next element to be read or written, relative to the position start.
    */
   public void setPosition(int position) {
      this.used     = Math.max(this.used, this.position);
      this.position = position + this.positionStart;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * The position start for this ByteArrayDataBuffer. Since many ByteArrayDataBuffers may
    * use the same underlying data, the position start must be honored as the origin
    * of data for this buffer, and a rewind or clear operation should only go back to position start.
    * @return the position start for this ByteArrayDataBuffer.
    */
   public int getPositionStart() {
      return this.positionStart;
   }

   /**
    * Gets the short.
    *
    * @return the short
    */
   public short getShort() {
      final short result = getShort(this.position);

      this.position += 2;
      return result;
   }

   /**
    * Gets the short.
    *
    * @param position the position
    * @return the short
    */
   public short getShort(int position) {
      long  lockStamp = this.sl.tryOptimisticRead();
      short result    = (short) (((this.data[position] & 0xff) << 8) | (this.data[position + 1] & 0xff));

      if (!this.sl.validate(lockStamp)) {
         lockStamp = this.sl.readLock();

         try {
            result = (short) (((this.data[position] & 0xff) << 8) | (this.data[position + 1] & 0xff));
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }

      return result;
   }

   public void putConceptSpecification(ConceptSpecification specification) {
      putNid(specification.getNid());
   }

   public ConceptSpecification getConceptSpecification() {
      return Get.conceptSpecification(getNid());
   }

   public void putConceptSpecificationSet(Set<ConceptSpecification> conceptSpecificationSet) {
      putInt(conceptSpecificationSet.size());
      for (ConceptSpecification conceptSpecification: conceptSpecificationSet) {
         putConceptSpecification(conceptSpecification);
      }
   }

   public Set<ConceptSpecification> getConceptSpecificationSet() {
      int setSize = getInt();
      Set<ConceptSpecification> conceptSpecificationSet = new HashSet<>(setSize);
      for (int i = 0; i < setSize; i++) {
         conceptSpecificationSet.add(getConceptSpecification());
      }
      return conceptSpecificationSet;
   }

    public void putConceptSpecificationList(List<ConceptSpecification> conceptSpecificationList) {
        putInt(conceptSpecificationList.size());
        for (ConceptSpecification conceptSpecification: conceptSpecificationList) {
            putConceptSpecification(conceptSpecification);
        }
    }

    public List<ConceptSpecification> getConceptSpecificationList() {
        int listSize = getInt();
        List<ConceptSpecification> conceptSpecificationList = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++) {
            conceptSpecificationList.add(getConceptSpecification());
        }
        return conceptSpecificationList;
    }

    public void putConceptSpecificationArray(ConceptSpecification[] conceptSpecificationArray) {
        putInt(conceptSpecificationArray.length);
        for (ConceptSpecification conceptSpecification: conceptSpecificationArray) {
            putConceptSpecification(conceptSpecification);
        }
    }

    public ConceptSpecification[] getConceptSpecificationArray() {
        int listSize = getInt();
        ConceptSpecification[] conceptSpecificationArray = new ConceptSpecification[listSize];
        for (int i = 0; i < listSize; i++) {
            conceptSpecificationArray[i] = getConceptSpecification();
        }
        return conceptSpecificationArray;
    }

    public void putStatusSet(EnumSet<Status> statusSet) {
      putInt(statusSet.size());
      for (Status status: statusSet) {
         putUTF(status.name());
      }
   }

   public EnumSet<Status> getStatusSet() {
      int setSize = getInt();
      List<Status> statusSet = new ArrayList(setSize);
      for (int i = 0; i < setSize; i++) {
         statusSet.add(Status.valueOf(getUTF()));
      }
      return EnumSet.copyOf(statusSet);

   }

   /**
    * Gets the stamp sequence.
    *
    * @return the stamp sequence
    */
   public int getStampSequence() {
      if (this.externalData) {
         return StampUniversal.get(this)
                              .getStampSequence();
      }

      return getInt();
   }

   /**
    01 - type token; 01 = concept
    01 - data format version
    01 - data source token; 01 = internal; 02 = external.
    8b - primordialUuidMsb
    fb
    a9
    44
    39
    65
    39
    46
    9b - primordialUuidLsb
    cb
    1e
    80
    a5
    da
    63
    a2
    00 - additional uuid parts
    00
    00
    02
    d6 - part 1
    fa
    d9
    81
    7d
    f6
    33
    88
    94 - part 2
    d8
    23
    8c
    c0
    46
    5a
    79
    80 - assemblage nid
    00
    00
    66
    09 - version token (9 = concept)
    80 - nid
    00
    00
    14
    * @param bytes
    * @return
    */
   public static int getVersionStartPosition(byte[] bytes) {
      int startPosition = 0;
      if (bytes[startPosition] == 0) {
         startPosition = startPosition + 4;
      }
      IsaacObjectType objectType = IsaacObjectType.fromToken(bytes[startPosition]);
      startPosition = startPosition + 19;
      int additionalParts = ByteArrayDataBuffer.getInt(bytes, startPosition);
      startPosition = startPosition + (additionalParts * 8);
      startPosition = startPosition + 4; // assemblageNid
      startPosition = startPosition + 1; // version type
      startPosition = startPosition + 4; // nid
      // additional fields:
      switch (objectType) {
         case CONCEPT:
            // nothing additional
            break;
         case SEMANTIC:
            startPosition = startPosition + 4; // this.referencedComponentNid
      }
      return startPosition;
   }

   /**
    * Gets the uuid.
    *
    * @return the uuid
    */
   public UUID getUuid() {
      return new UUID(getLong(), getLong());
   }

   public byte[][] toDataArray() {
      // TODO eliminate the differences between the byte[][] data formatting, and the ByteArrayDataBuffer
      // Thus simplify the serialization representation.
      List<byte[]> dataArray = new ArrayList<>();
      byte[] dataToSplit = this.data;
      boolean startsWithZero = getInt(dataToSplit, 0) == 0;
      IsaacObjectType objectType;
      if (startsWithZero) {
         objectType = IsaacObjectType.fromToken(dataToSplit[4]);
      } else {
         objectType = IsaacObjectType.fromToken(dataToSplit[0]);
      }
      int versionStartPosition = getVersionStartPosition(dataToSplit);
      if (versionStartPosition < 0 || versionStartPosition > dataToSplit.length) {
         throw new IllegalStateException("versionStartPosition: " + versionStartPosition);
      }

      byte[] chronicleBytes = new byte[versionStartPosition + 4]; // +4 for the zero integer to start.
      if (startsWithZero) {
         for (int i = 0; i < chronicleBytes.length; i++) {
            chronicleBytes[i] = dataToSplit[i];
         }

      } else {
         for (int i = 0; i < chronicleBytes.length; i++) {
            if (i < 4) {
               chronicleBytes[i] = 0;
            } else {
               chronicleBytes[i] = dataToSplit[i - 4];
            }
         }
      }
      dataArray.add(chronicleBytes);

      int versionStart = versionStartPosition;
      if (startsWithZero) {
         versionStart = versionStart + 4;
      }
      getInt(dataToSplit, versionStart);
      int versionSize = getInt(dataToSplit, versionStart);

      while (versionSize != 0) {
         int versionTo = versionStart + versionSize;
         int newLength = versionTo - versionStart;
         if (versionTo < 0) {
            throw new IllegalStateException("Error versionTo: " + versionTo);
         }
         if (newLength < 0) {
           throw new IllegalStateException("Error newLength: " + newLength);
         }
         dataArray.add(Arrays.copyOfRange(dataToSplit, versionStart, versionTo));
         versionStart = versionStart + versionSize;
         versionSize = getInt(dataToSplit, versionStart);
      }
      return dataArray.toArray(new byte[dataArray.size()][]);
   }

   public static ByteArrayDataBuffer dataArrayToBuffer(byte[][] data) {
      // TODO eliminate the differences between the byte[][] data formatting, and the ByteArrayDataBuffer
      // Thus simplify the serialization representation.
      if (data == null) {
         throw new NullPointerException();
      }
      int size = 0;

      for (byte[] dataEntry : data) {
         size = size + dataEntry.length;
      }

      ByteArrayDataBuffer byteBuffer = new ByteArrayDataBuffer(
              size + 4);  // room for 0 int value at end to indicate last version

      for (int i = 0; i < data.length; i++) {
         if (i == 0) {
            // discard the 0 integer at the beginning of the record.
            // 0 put in to enable the chronicle to sort before the versions.
            if (data[0][0] != 0 && data[0][1] != 0 && data[0][2] != 0 && data[0][3] != 0) {
               throw new IllegalStateException("Record does not start with zero...");
            }
            byteBuffer.put(data[0], 4, data[0].length - 4);
         } else {
            byteBuffer.put(data[i]);
         }

      }

      byteBuffer.putInt(0);
      byteBuffer.rewind();

      if (byteBuffer.getUsed() != size) {
         throw new IllegalStateException("Size = " + size + " used = " + byteBuffer.getUsed());
      }
      return byteBuffer;
   }

   public void write(DataOutputStream output) throws IOException {
      output.writeInt(position);
      output.write(data, 0, position);
   }

   public static ByteArrayDataBuffer make(DataInputStream input) throws IOException {
      int size = input.readInt();
      byte[] data = new byte[size];
      input.read(data, 0, size);
      return new ByteArrayDataBuffer(data);
   }
}

