/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.commit.manager;

import gov.vha.isaac.ochre.api.collections.NativeIntIntHashMap;
import gov.vha.isaac.ochre.api.externalizable.StampAlias;
import gov.vha.isaac.ochre.api.externalizable.StampComment;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Spliterator;
import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.SIZED;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.mahout.math.list.IntArrayList;

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
    private class StampAliasSpliterator extends IndexedStampSequenceSpliterator<StampAlias> {

        public StampAliasSpliterator() {
            super(stampAliasMap.keys());
        }

        @Override
        public boolean tryAdvance(Consumer<? super StampAlias> action) {
            if (getIndex() < getKeys().size()) {
                StampAlias stampAlias = new StampAlias(stampAliasMap.get(getIndex()), getIndex());
                action.accept(stampAlias);
                setIndex(getIndex() + 1);
                return true;
            }
            return false;
        }

        
    }
   public Stream<StampAlias> getStampAliasStream() {
        return StreamSupport.stream(new StampAliasSpliterator(), false);
    }

}
