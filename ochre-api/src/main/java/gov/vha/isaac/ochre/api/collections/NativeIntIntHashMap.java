/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.collections;

import org.apache.mahout.math.map.HashFunctions;
import org.apache.mahout.math.map.OpenIntIntHashMap;

/**
 *
 * @author kec
 */
public class NativeIntIntHashMap extends OpenIntIntHashMap {

    public NativeIntIntHashMap() {
    }

    public NativeIntIntHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public NativeIntIntHashMap(int initialCapacity, double minLoadFactor, double maxLoadFactor) {
        super(initialCapacity, minLoadFactor, maxLoadFactor);
    }

    public int[] getTable() {
        return table;
    }

    public void setTable(int[] table) {
        this.table = table;
    }

    public int[] getValues() {
        return values;
    }

    public void setValues(int[] values) {
        this.values = values;
    }

    public byte[] getState() {
        return state;
    }

    public void setState(byte[] state) {
        this.state = state;
    }

    public int getFreeEntries() {
        return freeEntries;
    }

    public void setFreeEntries(int freeEntries) {
        this.freeEntries = freeEntries;
    }

    public int getDistinct() {
        return distinct;
    }

    public void setDistinct(int distinct) {
        this.distinct = distinct;
    }

    /**
     * Reimplement rehash so that tables are not exposed until after copying is
     * complete.
     *
     * @param newCapacity
     */
    @Override
    protected void rehash(int newCapacity) {
        int oldCapacity = table.length;
        //if (oldCapacity == newCapacity) return;

        int[] oldTable = table;
        int[] oldValues = values;
        byte[] oldState = state;

        int[] newTable = new int[newCapacity];
        int[] newValues = new int[newCapacity];
        byte[] newState = new byte[newCapacity];

        for (int i = oldCapacity; i-- > 0;) {
            if (oldState[i] == FULL) {
                int element = oldTable[i];
                int index = indexOfInsertion(element, newTable, newState);
                newTable[index] = element;
                newValues[index] = oldValues[i];
                newState[index] = FULL;
            }
        }
        updateData(newTable, newValues, newState, newCapacity);
    }

    protected void updateData(int[] newTable, int[] newValues, byte[] newState, int newCapacity) {
        this.values = newValues;
        this.state = newState;
        this.table = newTable;
        
        this.lowWaterMark = chooseLowWaterMark(newCapacity, this.minLoadFactor);
        this.highWaterMark = chooseHighWaterMark(newCapacity, this.maxLoadFactor);
        
        this.freeEntries = newCapacity - this.distinct; // delta
    }

    @Override
    protected int indexOfInsertion(int key) {
        return indexOfInsertion(key, table, state); 
    }
    
    

    protected int indexOfInsertion(int key, int[] newTable, byte[] newState) {
        final int length = newTable.length;

        final int hash = HashFunctions.hash(key) & 0x7FFFFFFF;
        int i = hash % length;
        int decrement = hash % (length - 2); // double hashing, see http://www.eece.unm.edu/faculty/heileman/hash/node4.html
        //int decrement = (hash / length) % length;
        if (decrement == 0) {
            decrement = 1;
        }

        // stop if we find a removed or free slot, or if we find the key itself
        // do NOT skip over removed slots (yes, open addressing is like that...)
        while (newState[i] == FULL && newTable[i] != key) {
            i -= decrement;
            //hashCollisions++;
            if (i < 0) {
                i += length;
            }
        }

        if (newState[i] == REMOVED) {
            // stop if we find a free slot, or if we find the key itself.
            // do skip over removed slots (yes, open addressing is like that...)
            // assertion: there is at least one FREE slot.
            final int j = i;
            while (newState[i] != FREE && (newState[i] == REMOVED || newTable[i] != key)) {
                i -= decrement;
                //hashCollisions++;
                if (i < 0) {
                    i += length;
                }
            }
            if (newState[i] == FREE) {
                i = j;
            }
        }

        if (newState[i] == FULL) {
            // key already contained at slot i.
            // return a negative number identifying the slot.
            return -i - 1;
        }
        // not already contained, should be inserted at slot i.
        // return a number >= 0 identifying the slot.
        return i;
    }

}
