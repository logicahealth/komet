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

import java.io.UTFDataFormatException;

import java.nio.ReadOnlyBufferException;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.StampedLock;

import javax.xml.bind.DatatypeConverter;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.commit.StampService;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ByteArrayDataBuffer {
   private static final int    MAX_DATA_SIZE = Integer.MAX_VALUE - 16;
   private static final int    DEFAULT_SIZE  = 1024;
   protected static final byte FALSE         = 0;
   protected static final byte TRUE          = 1;

   //~--- fields --------------------------------------------------------------

   protected int     position                = 0;
   protected boolean readOnly                = false;
   protected byte    objectDataFormatVersion = 0;
   protected boolean externalData            = false;

   /**
    * The StampedLock is to ensure the backing array does not grow underneath a
    * concurrent operation. The locks do not prevent concurrent threads from
    * reading or writing to the same fields.
    */
   protected final StampedLock sl   = new StampedLock();
   protected int               used = 0;
   protected final int         positionStart;
   protected IdentifierService identifierService;
   protected StampService      stampService;
   private byte[]              data;

   //~--- constructors --------------------------------------------------------

   public ByteArrayDataBuffer() {
      this(DEFAULT_SIZE);
   }

   public ByteArrayDataBuffer(byte[] data) {
      this(data, 0);
   }

   public ByteArrayDataBuffer(int size) {
      this.data          = new byte[size];
      this.positionStart = 0;
   }

   public ByteArrayDataBuffer(byte[] data, int positionStart) {
      this.data          = data;
      this.used          = data.length;
      this.positionStart = positionStart;
   }

   //~--- methods -------------------------------------------------------------

   public void append(ByteArrayDataBuffer db, int position, int length) {
      ensureSpace(this.position + length);
      System.arraycopy(db.data, position, this.data, this.position, length);
      this.position += length;
   }

   /**
    *  Makes this buffer ready for a new sequence of put operations:
    *  It sets the limit to positionStart, and the position to positionStart.
    */
   public ByteArrayDataBuffer clear() {
      this.used     = 0;
      this.position = this.positionStart;
      return this;
   }

   /**
    * Makes this buffer ready for a new sequence of get operations:
    * It sets the limit to the current position and then sets the position to zero.
    */
   public ByteArrayDataBuffer flip() {
      getLimit();
      this.position = this.positionStart;
      return this;
   }

   public ByteArrayDataBuffer newWrapper() {
      final ByteArrayDataBuffer newWrapper = new ByteArrayDataBuffer(this.data);

      newWrapper.readOnly = true;
      newWrapper.position = 0;
      newWrapper.used     = this.used;
      return newWrapper;
   }

   public void put(byte[] src) {
      put(src, 0, src.length);
   }

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
   }

   public void putBoolean(boolean x) {
      if (x) {
         putByte(TRUE);
      } else {
         putByte(FALSE);
      }
   }

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
   }

   public void putByteArrayField(byte[] array) {
      putInt(array.length);
      put(array, 0, array.length);
   }

   public void putChar(char x) {
      putShort((short) x);
   }

   public void putConceptSequence(int conceptSequence) {
      if (this.externalData) {
         final UUID uuid = this.identifierService.getUuidPrimordialForNid(this.identifierService.getConceptNid(conceptSequence))
                                      .get();

         putLong(uuid.getMostSignificantBits());
         putLong(uuid.getLeastSignificantBits());
      } else {
         putInt(conceptSequence);
      }
   }

   public void putDouble(double d) {
      putLong(Double.doubleToLongBits(d));
   }

   public void putFloat(float f) {
      putInt(Float.floatToRawIntBits(f));
   }

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
   }

   public void putIntArray(int[] src) {
      putInt(src.length);
      ensureSpace(this.position + (src.length * 4));

      long lockStamp        = this.sl.tryOptimisticRead();
      final int  startingPosition = this.position;

      putIntArrayIntoData(src);

      if (!this.sl.validate(lockStamp)) {
         lockStamp = this.sl.readLock();
         this.position  = startingPosition;

         try {
            putIntArrayIntoData(src);
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }
   }

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
   }

   public void putNid(int nid) {
      if (this.externalData) {
         final Optional<UUID> optionalUuid = this.identifierService.getUuidPrimordialForNid(nid);

         if (optionalUuid.isPresent()) {
            final UUID uuid = optionalUuid.get();

            putLong(uuid.getMostSignificantBits());
            putLong(uuid.getLeastSignificantBits());
         } else {
            throw new RuntimeException("Can't find uuid for nid: " + nid);
         }
      } else {
         putInt(nid);
      }
   }

   public void putSememeSequence(int sememeSequence) {
      if (this.externalData) {
         final UUID uuid = this.identifierService.getUuidPrimordialForNid(this.identifierService.getSememeNid(sememeSequence))
                                      .get();

         putLong(uuid.getMostSignificantBits());
         putLong(uuid.getLeastSignificantBits());
      } else {
         putInt(sememeSequence);
      }
   }

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
   }

   public void putStampSequence(int stampSequence) {
      if (this.externalData) {
         StampUniversal.get(stampSequence)
                       .writeExternal(this);
      } else {
         putInt(stampSequence);
      }
   }

   public void putUTF(String str) {
      final int strlen = str.length();
      int utflen = 0;
      int c;
      int count = 0;

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
      int    i       = 0;

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

   public void putUuid(UUID uuid) {
      putLong(uuid.getMostSignificantBits());
      putLong(uuid.getLeastSignificantBits());
   }

   public final String readUTF() {
      final int[]  positionArray = new int[] { this.position };
      final String result        = readUTF(positionArray);

      this.position = positionArray[0];
      return result;
   }

   public final String readUTF(int[] position) {
      final int    utflen  = getInt(position[0]);
      final byte[] bytearr = new byte[utflen];
      final char[] chararr = new char[utflen];
      int    c, char2, char3;
      int    count         = 0;
      int    chararr_count = 0;

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
    */
   public ByteArrayDataBuffer rewind() {
      this.position = this.positionStart;
      return this;
   }

   public ByteArrayDataBuffer slice() {
      final ByteArrayDataBuffer slice = new ByteArrayDataBuffer(this.data, this.position);

      slice.readOnly = true;
      slice.position = this.position;
      slice.used     = this.used;
      return slice;
   }

   @Override
   public String toString() {
      return "ByteArrayDataBuffer{" + "position=" + this.position + ", positionStart=" + this.positionStart + ", readOnly=" +
             this.readOnly + ", objectDataFormatVersion=" + this.objectDataFormatVersion + ", externalData=" + this.externalData +
             ", used=" + this.used + ", data=" + DatatypeConverter.printHexBinary(this.data) + '}';
   }

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

   private void putIntArrayIntoData(int[] src) {
      for (final int anInt: src) {
         this.data[this.position]     = (byte) (anInt >> 24);
         this.data[this.position + 1] = (byte) (anInt >> 16);
         this.data[this.position + 2] = (byte) (anInt >> 8);
         this.data[this.position + 3] = (byte) (anInt);
         this.position           += 4;
      }
   }

   //~--- get methods ---------------------------------------------------------

   public boolean getBoolean() {
      return getByte() != FALSE;
   }

   public boolean getBoolean(int position) {
      return getByte(position) != FALSE;
   }

   public byte getByte() {
      final byte result = getByte(this.position);

      this.position += 1;
      return result;
   }

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
    *
    * @return a byte[] written to the ByteArrayDataBuffer. Does not return the entire
    * data buffer as an array.
    */
   public byte[] getByteArrayField() {
      final int    length    = getInt();
      long   lockStamp = this.sl.tryOptimisticRead();
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

   public char getChar() {
      final char result = getChar(this.position);

      this.position += 2;
      return result;
   }

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

   public int getConceptSequence() {
      if (this.externalData) {
         return this.identifierService.getConceptSequenceForUuids(new UUID(getLong(), getLong()));
      }

      return getInt();
   }

   /**
    *
    * @return the byte[] that backs this buffer.
    */
   public byte[] getData() {
      return this.data;
   }

   public double getDouble() {
      return Double.longBitsToDouble(getLong());
   }

   public double getDouble(int position) {
      return Double.longBitsToDouble(getLong(position));
   }

   public boolean isExternalData() {
      return this.externalData;
   }

   //~--- set methods ---------------------------------------------------------

   public void setExternalData(boolean externalData) {
      if (externalData) {
         this.identifierService = Get.identifierService();
         this.stampService      = Get.stampService();
      }

      this.externalData = externalData;
   }

   //~--- get methods ---------------------------------------------------------

   public float getFloat() {
      return Float.intBitsToFloat(getInt());
   }

   public float getFloat(int position) {
      return Float.intBitsToFloat(getInt(position));
   }

   public void get(byte[] src, int offset, int length) {
      get(this.position, src, offset, length);
      this.position += length;
   }

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

   public int getInt() {
      final int result = getInt(this.position);

      this.position += 4;
      return result;
   }

   public int getInt(int position) {
      long lockStamp = this.sl.tryOptimisticRead();
      int result = (((this.data[position]) << 24) | ((this.data[position + 1] & 0xff) << 16) | ((this.data[position + 2] & 0xff) << 8)
                    | ((this.data[position + 3] & 0xff)));

      if (!this.sl.validate(lockStamp)) {
         lockStamp = this.sl.readLock();

         try {
            result = (((this.data[position]) << 24) | ((this.data[position + 1] & 0xff) << 16)
                      | ((this.data[position + 2] & 0xff) << 8) | ((this.data[position + 3] & 0xff)));
         } finally {
            this.sl.unlockRead(lockStamp);
         }
      }

      return result;
   }

   public int[] getIntArray() {
      final int[] array            = new int[getInt()];
      final int   startingPosition = this.position;
      long  lockStamp        = this.sl.tryOptimisticRead();

      for (int i = 0; i < array.length; i++) {
         array[i] = (((this.data[this.position]) << 24) | ((this.data[this.position + 1] & 255) << 16) | ((this.data[this.position + 2] & 255) << 8)
                     | ((this.data[this.position + 3] & 255)));
         this.position += 4;
      }

      if (!this.sl.validate(lockStamp)) {
         lockStamp = this.sl.readLock();
         this.position  = startingPosition;

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

   /**
    * The limit is the index of the first element that should not be read or written, relative to the position start.
    * It represents the end of valid data, and is never negative and is never greater than its capacity.
    * @return the position after the end of written data in the buffer, relative to the position start.
    */
   public int getLimit() {
      this.used = Math.max(this.used, this.position);
      return this.used - this.positionStart;
   }

   public long getLong() {
      final long result = getLong(this.position);

      this.position += 8;
      return result;
   }

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

   private long getLongResult(int position) {
      long result;

      result = ((((long) this.data[position]) << 56) | (((long) this.data[position + 1] & 0xff) << 48)
                | (((long) this.data[position + 2] & 0xff) << 40) | (((long) this.data[position + 3] & 0xff) << 32)
                | (((long) this.data[position + 4] & 0xff) << 24) | (((long) this.data[position + 5] & 0xff) << 16)
                | (((long) this.data[position + 6] & 0xff) << 8) | (((long) this.data[position + 7] & 0xff)));
      return result;
   }

   public int getNid() {
      if (this.externalData) {
         return this.identifierService.getNidForUuids(new UUID(getLong(), getLong()));
      }

      return getInt();
   }

   public byte getObjectDataFormatVersion() {
      return this.objectDataFormatVersion;
   }

   //~--- set methods ---------------------------------------------------------

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

   public int getSememeSequence() {
      if (this.externalData) {
         return this.identifierService.getSememeSequenceForUuids(new UUID(getLong(), getLong()));
      }

      return getInt();
   }

   public short getShort() {
      final short result = getShort(this.position);

      this.position += 2;
      return result;
   }

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

   public int getStampSequence() {
      if (this.externalData) {
         return StampUniversal.get(this)
                              .getStampSequence();
      }

      return getInt();
   }

   public UUID getUuid() {
      return new UUID(getLong(), getLong());
   }
}

