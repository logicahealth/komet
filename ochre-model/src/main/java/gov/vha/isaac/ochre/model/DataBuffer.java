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

import java.io.UTFDataFormatException;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.concurrent.locks.StampedLock;

/**
 *
 * @author kec
 */
public class DataBuffer {

    private static final int MAX_DATA_SIZE = Integer.MAX_VALUE - 16;

    /**
     * The StampedLock is to ensure the backing array does not grow underneath a
     * concurrent operation. The locks do not prevent concurrent threads from
     * reading or writing to the same fields.
     */
    private final StampedLock sl = new StampedLock();

    private byte[] data;
    private int positionStart = 0;
    private int position = 0;
    private int used = 0;
    private boolean readOnly = false;

    public DataBuffer(byte[] data) {
        this.data = data;
        //TODO Keith - this null check likely shouldn't be here (I added it to get around a problem), I suspect related 
        //to other problems in the sememe service at the moment. 
        this.used = data == null ? 0 : data.length;
    }

    public DataBuffer(int size) {
        this.data = new byte[size];
    }

    public DataBuffer slice() {
        DataBuffer slice = new DataBuffer(data);
        slice.readOnly = true;
        slice.positionStart = this.position;
        slice.position = this.position;
        slice.used = this.used;
        return slice;
    }

    public DataBuffer newWrapper() {
        DataBuffer newWrapper = new DataBuffer(data);
        newWrapper.readOnly = true;
        newWrapper.positionStart = 0;
        newWrapper.position = 0;
        newWrapper.used = this.used;
        return newWrapper;
    }

    /**
     *
     * @return the position after the end of written data in the buffer.
     */
    public int getLimit() {
        this.used = Math.max(this.used, this.position);
        return this.used - positionStart;
    }

    public int getPosition() {
        return position - positionStart;
    }

    public void setPosition(int position) {
        this.used = Math.max(this.used, this.position);
        this.position = position + this.positionStart;
    }

    /**
     *
     * @return the byte[] that backs this buffer.
     */
    public byte[] getData() {
        return data;
    }

    public void reset() {
        this.used = Math.max(this.used, this.position);
        this.position = positionStart;
    }

    public byte getByte() {
        byte result = getByte(position);
        position += 1;
        return result;
    }

    public byte getByte(int position) {
        long lockStamp = sl.tryOptimisticRead();
        byte result = data[position];
        if (!sl.validate(lockStamp)) {
            lockStamp = sl.readLock();
            try {
                result = data[position];
            } finally {
                sl.unlockRead(lockStamp);
            }
        }
        return result;
    }
    
    public void putByteArrayField(byte[] array) {
        putInt(array.length);
        put(array, 0, array.length);
        
    }

    /**
     *
     * @return a byte[] written to the DataBuffer. Does not return the entire
     * data buffer as an array.
     * @see getData()
     */
    public byte[] getByteArrayField() {
        int length = getInt();
        long lockStamp = sl.tryOptimisticRead();
        byte[] results = new byte[length];
        System.arraycopy(data, position, results, 0, length);
        if (!sl.validate(lockStamp)) {
            lockStamp = sl.readLock();
            try {
                System.arraycopy(data, position, results, 0, length);
            } finally {
                sl.unlockRead(lockStamp);
            }
        }
        position += length;
        return results;
    }

    public void putByte(byte x) {
        ensureSpace(position + 1);
        long lockStamp = sl.tryOptimisticRead();
        data[position] = x;
        if (!sl.validate(lockStamp)) {
            lockStamp = sl.readLock();
            try {
                data[position] = x;
            } finally {
                sl.unlockRead(lockStamp);
            }
        }
        position += 1;
    }

    public short getShort() {
        short result = getShort(position);
        position += 2;
        return result;
    }

    public short getShort(int position) {
        long lockStamp = sl.tryOptimisticRead();
        short result = (short) (((data[position] & 0xff) << 8)
                | (data[position + 1] & 0xff));
        if (!sl.validate(lockStamp)) {
            lockStamp = sl.readLock();
            try {
                result = (short) (((data[position] & 0xff) << 8)
                        | (data[position + 1] & 0xff));
            } finally {
                sl.unlockRead(lockStamp);
            }
        }
        return result;
    }

    public void putShort(short x) {
        ensureSpace(position + 2);
        long lockStamp = sl.tryOptimisticRead();
        data[position] = (byte) (x >> 8);
        data[position + 1] = (byte) (x);
        if (!sl.validate(lockStamp)) {
            lockStamp = sl.readLock();
            try {
                data[position] = (byte) (x >> 8);
                data[position + 1] = (byte) (x);
            } finally {
                sl.unlockRead(lockStamp);
            }
        }
        position += 2;
    }

    public char getChar() {
        char result = getChar(position);
        position += 2;
        return result;
    }

    public char getChar(int position) {
        long lockStamp = sl.tryOptimisticRead();
        char result = (char) ((data[position] << 8)
                | (data[position + 1] & 0xff));
        if (!sl.validate(lockStamp)) {
            lockStamp = sl.readLock();
            try {
                result = (char) ((data[position] << 8)
                        | (data[position + 1] & 0xff));
            } finally {
                sl.unlockRead(lockStamp);
            }
        }
        return result;
    }

    public void putChar(char x) {
        ensureSpace(position + 2);
        long lockStamp = sl.tryOptimisticRead();
        data[position] = (byte) (x >> 8);
        data[position + 1] = (byte) (x);
        if (!sl.validate(lockStamp)) {
            lockStamp = sl.readLock();
            try {
                data[position] = (byte) (x >> 8);
                data[position + 1] = (byte) (x);
            } finally {
                sl.unlockRead(lockStamp);
            }
        }
        position += 2;
    }

    public int getInt() {
        int result = getInt(position);
        position += 4;
        return result;

    }

    public int getInt(int position) {
        long lockStamp = sl.tryOptimisticRead();
        int result = (((data[position]) << 24)
                | ((data[position + 1] & 0xff) << 16)
                | ((data[position + 2] & 0xff) << 8)
                | ((data[position + 3] & 0xff)));
        if (!sl.validate(lockStamp)) {
            lockStamp = sl.readLock();
            try {
                result = (((data[position]) << 24)
                        | ((data[position + 1] & 0xff) << 16)
                        | ((data[position + 2] & 0xff) << 8)
                        | ((data[position + 3] & 0xff)));
            } finally {
                sl.unlockRead(lockStamp);
            }
        }
        return result;
    }

    public void putInt(int x) {
        ensureSpace(position + 4);
        long lockStamp = sl.tryOptimisticRead();
        data[position] = (byte) (x >> 24);
        data[position + 1] = (byte) (x >> 16);
        data[position + 2] = (byte) (x >> 8);
        data[position + 3] = (byte) (x);
        if (!sl.validate(lockStamp)) {
            lockStamp = sl.readLock();
            try {
                data[position] = (byte) (x >> 24);
                data[position + 1] = (byte) (x >> 16);
                data[position + 2] = (byte) (x >> 8);
                data[position + 3] = (byte) (x);
            } finally {
                sl.unlockRead(lockStamp);
            }
        }
        position += 4;
    }

    public float getFloat() {
        return Float.intBitsToFloat(getInt());
    }

    public float getFloat(int position) {
        return Float.intBitsToFloat(getInt(position));
    }

    public void putFloat(float f) {
        putInt(Float.floatToRawIntBits(f));
    }

    public long getLong() {
        long result = getLong(position);
        position += 8;
        return result;
    }

    public long getLong(int position) {
        long lockStamp = sl.tryOptimisticRead();
        long result = ((((long) data[position]) << 56)
                | (((long) data[position + 1] & 0xff) << 48)
                | (((long) data[position + 2] & 0xff) << 40)
                | (((long) data[position + 3] & 0xff) << 32)
                | (((long) data[position + 4] & 0xff) << 24)
                | (((long) data[position + 5] & 0xff) << 16)
                | (((long) data[position + 6] & 0xff) << 8)
                | (((long) data[position + 7] & 0xff)));
        if (!sl.validate(lockStamp)) {
            lockStamp = sl.readLock();
            try {
                result = ((((long) data[position]) << 56)
                        | (((long) data[position + 1] & 0xff) << 48)
                        | (((long) data[position + 2] & 0xff) << 40)
                        | (((long) data[position + 3] & 0xff) << 32)
                        | (((long) data[position + 4] & 0xff) << 24)
                        | (((long) data[position + 5] & 0xff) << 16)
                        | (((long) data[position + 6] & 0xff) << 8)
                        | (((long) data[position + 7] & 0xff)));
            } finally {
                sl.unlockRead(lockStamp);
            }
        }
        return result;
    }

    public void putLong(long x) {
        ensureSpace(position + 8);
        long lockStamp = sl.tryOptimisticRead();
        data[position] = (byte) (x >> 56);
        data[position + 1] = (byte) (x >> 48);
        data[position + 2] = (byte) (x >> 40);
        data[position + 3] = (byte) (x >> 32);
        data[position + 4] = (byte) (x >> 24);
        data[position + 5] = (byte) (x >> 16);
        data[position + 6] = (byte) (x >> 8);
        data[position + 7] = (byte) (x);
        if (!sl.validate(lockStamp)) {
            lockStamp = sl.readLock();
            try {
                data[position] = (byte) (x >> 56);
                data[position + 1] = (byte) (x >> 48);
                data[position + 2] = (byte) (x >> 40);
                data[position + 3] = (byte) (x >> 32);
                data[position + 4] = (byte) (x >> 24);
                data[position + 5] = (byte) (x >> 16);
                data[position + 6] = (byte) (x >> 8);
                data[position + 7] = (byte) (x);
            } finally {
                sl.unlockRead(lockStamp);
            }
        }
        position += 8;

    }

    public double getDouble() {
        return Double.longBitsToDouble(getLong());
    }

    public double getDouble(int position) {
        return Double.longBitsToDouble(getLong(position));
    }

    public void putDouble(double d) {
        putLong(Double.doubleToLongBits(d));
    }

    public void trimToSize() {
        if (readOnly) {
            throw new ReadOnlyBufferException();
        }
        long lockStamp = sl.writeLock();
        try {
            if (position < data.length && used < data.length) {
                int newSize = Math.max(position, used);
                byte[] newData = new byte[newSize];
                System.arraycopy(data, 0, newData, 0, newSize);
                data = newData;
            }
        } finally {
            sl.unlockWrite(lockStamp);
        }
    }

    private void ensureSpace(int minSpace) {
        if (readOnly) {
            throw new ReadOnlyBufferException();
        }
        if (minSpace > data.length) {
            long lockStamp = sl.writeLock();
            try {
                while (minSpace > data.length) {
                    int newCapacity = data.length << 1;
                    if (newCapacity > MAX_DATA_SIZE) {
                        newCapacity = MAX_DATA_SIZE;
                    }
                    data = Arrays.copyOf(data, newCapacity);
                }
            } finally {
                sl.unlockWrite(lockStamp);
            }
        }
    }

    public void putIntArray(int[] src) {
        putInt(src.length);
        ensureSpace(position + (src.length * 4));
        long lockStamp = sl.tryOptimisticRead();
        int startingPosition = position;
        for (int anInt : src) {
            data[position] = (byte) (anInt >> 24);
            data[position + 1] = (byte) (anInt >> 16);
            data[position + 2] = (byte) (anInt >> 8);
            data[position + 3] = (byte) (anInt);
            position += 4;
        }
        if (!sl.validate(lockStamp)) {
            lockStamp = sl.readLock();
            position = startingPosition;
            try {
                for (int anInt : src) {
                    data[position] = (byte) (anInt >> 24);
                    data[position + 1] = (byte) (anInt >> 16);
                    data[position + 2] = (byte) (anInt >> 8);
                    data[position + 3] = (byte) (anInt);
                    position += 4;
                }
            } finally {
                sl.unlockRead(lockStamp);
            }
        }

    }

    public int[] getIntArray() {
        int[] array = new int[getInt()];
        int startingPosition = position;
        long lockStamp = sl.tryOptimisticRead();
        for (int i = 0; i < array.length; i++) {
            array[i] = (((data[position]) << 24)
                    | ((data[position + 1] & 0xff) << 16)
                    | ((data[position + 2] & 0xff) << 8)
                    | ((data[position + 3] & 0xff)));
            position += 4;
        }
        if (!sl.validate(lockStamp)) {
            lockStamp = sl.readLock();
            position = startingPosition;
            try {
                for (int i = 0; i < array.length; i++) {
                    array[i] = (((data[position]) << 24)
                            | ((data[position + 1] & 0xff) << 16)
                            | ((data[position + 2] & 0xff) << 8)
                            | ((data[position + 3] & 0xff)));
                    position += 4;
                }
            } finally {
                sl.unlockRead(lockStamp);
            }
        }
        return array;
    }

    public void put(byte[] src) {
        put(src, 0, src.length);
    }

    public void put(byte[] src, int offset, int length) {
        ensureSpace(position + length);
        long lockStamp = sl.tryOptimisticRead();
        System.arraycopy(src, offset, data, position, length);
        if (!sl.validate(lockStamp)) {
            lockStamp = sl.readLock();
            try {
                System.arraycopy(src, offset, data, position, length);
            } finally {
                sl.unlockRead(lockStamp);
            }
        }
        position += length;
    }

    public void get(byte[] src, int offset, int length) {
        get(position, src, offset, length);
        position += length;

    }

    public void get(int position, byte[] src, int offset, int length) {
        long lockStamp = sl.tryOptimisticRead();
        System.arraycopy(data, position, src, offset, length);
        if (!sl.validate(lockStamp)) {
            lockStamp = sl.readLock();
            try {
                System.arraycopy(data, position, src, offset, length);
            } finally {
                sl.unlockRead(lockStamp);
            }
        }
    }

    public void putUTF(String str) {
        int strlen = str.length();
        int utflen = 0;
        int c, count = 0;

        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        byte[] bytearr = new byte[utflen];

        int i = 0;
        for (; i < strlen; i++) {
            c = str.charAt(i);
            if (!((c >= 0x0001) && (c <= 0x007F))) {
                break;
            }
            bytearr[count++] = (byte) c;
        }

        for (; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;
            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | (c & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | (c & 0x3F));
            }
        }

        putInt(utflen);
        put(bytearr, 0, utflen);
    }

    public final String readUTF() {
        int[] positionArray = new int[]{position};
        String result = readUTF(positionArray);
        position = positionArray[0];
        return result;
    }

    public final String readUTF(int[] position) {
        int utflen = getInt(position[0]);
        byte[] bytearr = new byte[utflen];
        char[] chararr = new char[utflen];

        int c, char2, char3;
        int count = 0;
        int chararr_count = 0;

        get(position[0] + 4, bytearr, 0, utflen);

        position[0] = position[0] + 4 + utflen;
        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            if (c > 127) {
                break;
            }
            count++;
            chararr[chararr_count++] = (char) c;
        }

        while (count < utflen) {
            try {
                c = (int) bytearr[count] & 0xff;
                switch (c >> 4) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        /* 0xxxxxxx*/
                        count++;
                        chararr[chararr_count++] = (char) c;
                        break;
                    case 12:
                    case 13:
                        /* 110x xxxx   10xx xxxx*/
                        count += 2;
                        if (count > utflen) {
                            throw new UTFDataFormatException(
                                    "malformed input: partial character at end");
                        }
                        char2 = (int) bytearr[count - 1];
                        if ((char2 & 0xC0) != 0x80) {
                            throw new UTFDataFormatException(
                                    "malformed input around byte " + count);
                        }
                        chararr[chararr_count++] = (char) (((c & 0x1F) << 6)
                                | (char2 & 0x3F));
                        break;
                    case 14:
                        /* 1110 xxxx  10xx xxxx  10xx xxxx */
                        count += 3;
                        if (count > utflen) {
                            throw new UTFDataFormatException(
                                    "malformed input: partial character at end");
                        }
                        char2 = (int) bytearr[count - 2];
                        char3 = (int) bytearr[count - 1];
                        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                            throw new UTFDataFormatException(
                                    "malformed input around byte " + (count - 1));
                        }
                        chararr[chararr_count++] = (char) (((c & 0x0F) << 12)
                                | ((char2 & 0x3F) << 6)
                                | (char3 & 0x3F));
                        break;
                    default:
                        /* 10xx xxxx,  1111 xxxx */
                        throw new UTFDataFormatException(
                                "malformed input around byte " + count);
                }
            } catch (UTFDataFormatException ex) {
                throw new RuntimeException(ex);
            }
        }
        // The number of chars produced may be less than utflen
        return new String(chararr, 0, chararr_count);
    }

    public void append(DataBuffer db, int position, int length) {
        System.arraycopy(db.data, position, data, this.position, length);
    }
}
