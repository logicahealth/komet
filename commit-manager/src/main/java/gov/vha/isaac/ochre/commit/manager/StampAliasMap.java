/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.commit.manager;

import gov.vha.isaac.ochre.api.collections.NativeIntIntHashMap;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

/**
 *
 * @author kec
 */
public class StampAliasMap {

    ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    NativeIntIntHashMap stampAliasMap = new NativeIntIntHashMap();
    NativeIntIntHashMap aliasStampMap = new NativeIntIntHashMap();

    public int getSize() {
        assert stampAliasMap.size() == aliasStampMap.size() : "stampAliasMap.size() = "
                + stampAliasMap.size() + " aliasStampMap.size() = " + aliasStampMap.size();
        return aliasStampMap.size();
    }

    public void addAlias(int stamp, int alias) {
        rwl.writeLock().lock();
        try {
            if (!stampAliasMap.containsKey(stamp)) {
                stampAliasMap.put(stamp, alias);
                aliasStampMap.put(alias, stamp);
            } else if (stampAliasMap.get(stamp) == alias) {
                // already added...
            } else {
                addAlias(stampAliasMap.get(stamp), alias);
            }
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * 
     * @param stamp
     * @return array of unique aliases, which do not include the stamp itself. 
     */
    public int[] getAliases(int stamp) {
        rwl.readLock().lock();
        try {
            IntStream.Builder builder = IntStream.builder();
            getAliasesForward(stamp, builder);
            getAliasesReverse(stamp, builder);
            return builder.build().distinct().toArray();
        } finally {
            rwl.readLock().unlock();
        }
    }

    private void getAliasesForward(int stamp, IntStream.Builder builder) {
        if (stampAliasMap.containsKey(stamp)) {
            int alias = stampAliasMap.get(stamp);
            builder.add(alias);
            getAliasesForward(alias, builder);
        }
    }
    private void getAliasesReverse(int stamp, IntStream.Builder builder) {
        if (aliasStampMap.containsKey(stamp)) {
            int alias = aliasStampMap.get(stamp);
            builder.add(alias);
            getAliasesReverse(alias, builder);
        }
    }

    public void write(File mapFile) throws IOException {
        try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mapFile)))) {
            output.writeInt(stampAliasMap.size());
            stampAliasMap.forEachPair((int nid, int sequence) -> {
                try {
                    output.writeInt(nid);
                    output.writeInt(sequence);
                    return true;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    public void read(File mapFile) throws IOException {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(mapFile)))) {
            int size = input.readInt();
            stampAliasMap.ensureCapacity(size);
            aliasStampMap.ensureCapacity(size);
            for (int i = 0; i < size; i++) {
                int nid = input.readInt();
                int sequence = input.readInt();
                stampAliasMap.put(nid, sequence);
                aliasStampMap.put(sequence, nid);
            }
        }
    }

}
