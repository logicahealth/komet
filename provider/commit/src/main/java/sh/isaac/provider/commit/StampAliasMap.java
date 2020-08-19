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



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sh.isaac.provider.commit;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.impl.factory.primitive.IntIntMaps;
import sh.isaac.api.datastore.ExtendedStore;
import sh.isaac.api.datastore.ExtendedStoreData;
import sh.isaac.api.externalizable.StampAlias;

//~--- classes ----------------------------------------------------------------

/**
 * The Class StampAliasMap.
 *
 * @author kec
 */
public class StampAliasMap {
    /**
     * The rwl.
     */
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    /**
     * The read.
     */
    private final Lock read = this.rwl.readLock();

    /**
     * The write.
     */
    private final Lock write = this.rwl.writeLock();

    /**
     * The stamp alias map.
     */
    private MutableIntIntMap stampAliasMap;

    /**
     * The alias stamp map.
     */
    private MutableIntIntMap aliasStampMap;

    private ExtendedStore dataStore;
    private ExtendedStoreData<Integer, Integer> stampToAlias;
    private ExtendedStoreData<Integer, Integer> aliasToStamp;

    //~--- methods -------------------------------------------------------------

    /**
     * Construct a default stamp alias map, which holds the alias's in memory, and must be read / written to the file system.
     */
    public StampAliasMap() {
        stampAliasMap = IntIntMaps.mutable.empty();
        aliasStampMap = IntIntMaps.mutable.empty();
    }

    /**
     * Construct a a StampAliasMap class, that is just a thin wrapper around a datastore.  Does not hold any data in memory.
     *
     * @param dataStore the datastore to read/write from
     */
    public StampAliasMap(ExtendedStore dataStore) {
        this.dataStore = dataStore;
        this.stampToAlias = dataStore.<Integer, Integer>getStore("stampMapStampToAlias");
        this.aliasToStamp = dataStore.<Integer, Integer>getStore("stampMapAliasToStamp");
    }

    /**
     * Adds the alias.
     *
     * @param stamp the stamp
     * @param alias the alias
     */
    public void addAlias(int stamp, int alias) {
        try {
            this.write.lock();

            if (dataStore == null) {
                if (!this.stampAliasMap.containsKey(stamp)) {
                    this.stampAliasMap.put(stamp, alias);
                    this.aliasStampMap.put(alias, stamp);
                } else if (this.stampAliasMap.get(stamp) == alias) {
                    // already added...
                } else {
                    // add an additional alias
                    this.aliasStampMap.put(alias, stamp);
                }
            } else {
                Integer currentAlias = stampToAlias.get(stamp);
                if (currentAlias != null) {
                    if (currentAlias.intValue() == alias) {
                        //Nothing to do
                    } else {
                        aliasToStamp.put(alias, stamp);
                    }
                } else {
                    stampToAlias.put(stamp, alias);
                    aliasToStamp.put(alias, stamp);
                }
            }
        } finally {
            if (this.write != null) {
                this.write.unlock();
            }
        }
    }

    /**
     * Read.
     *
     * @param mapFile the map file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void read(File mapFile)
            throws IOException {
        if (dataStore != null) {
            throw new RuntimeException("Shouldn't be reading from disk if constructed from a datastore");
        }
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(mapFile)))) {
            int size = input.readInt();
            stampAliasMap = IntIntMaps.mutable.withInitialCapacity(size);

            for (int i = 0; i < size; i++) {
                this.stampAliasMap.put(input.readInt(), input.readInt());
            }

            size = input.readInt();
            aliasStampMap = IntIntMaps.mutable.withInitialCapacity(size);

            for (int i = 0; i < size; i++) {
                this.aliasStampMap.put(input.readInt(), input.readInt());
            }
        }
    }

    /**
     * Write.
     *
     * @param mapFile the map file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void write(File mapFile)
            throws IOException {
        if (dataStore != null) {
            throw new RuntimeException("Shouldn't be writing to disk if constructed from a datastore");
        }
        try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mapFile)))) {
            output.writeInt(this.stampAliasMap.size());
            this.stampAliasMap.forEachKeyValue((int stampSequence,
                                                int aliasSequence) -> {
                try {
                    output.writeInt(stampSequence);
                    output.writeInt(aliasSequence);
                } catch (final IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
            output.writeInt(this.aliasStampMap.size());
            this.aliasStampMap.forEachKeyValue((int aliasSequence,
                                            int stampSequence) -> {
                try {
                    output.writeInt(aliasSequence);
                    output.writeInt(stampSequence);
                } catch (final IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    //~--- get methods ---------------------------------------------------------

    /**
     * Gets the aliases.
     *
     * @param stamp the stamp
     * @return array of unique aliases, which do not include the stamp itself.
     */
    public int[] getAliases(int stamp) {
        try {
            this.read.lock();

            final IntStream.Builder builder = IntStream.builder();

            getAliasesForward(stamp, builder);
            getAliasesReverse(stamp, builder);
            return builder.build()
                    .distinct()
                    .toArray();
        } finally {
            if (this.read != null) {
                this.read.unlock();
            }
        }
    }

    /**
     * Gets the aliases forward.
     *
     * @param stamp   the stamp
     * @param builder the builder
     */
    private void getAliasesForward(int stamp, IntStream.Builder builder) {
        if (dataStore == null) {
            if (this.stampAliasMap.containsKey(stamp)) {
                final int alias = this.stampAliasMap.get(stamp);

                builder.add(alias);
                getAliasesForward(alias, builder);
            }
        } else {
            Integer currentAlias = stampToAlias.get(stamp);
            if (currentAlias != null) {
                builder.add(currentAlias.intValue());
                getAliasesForward(currentAlias.intValue(), builder);
            }
        }

    }

    /**
     * Gets the aliases reverse.
     *
     * @param stamp   the stamp
     * @param builder the builder
     */
    private void getAliasesReverse(int stamp, IntStream.Builder builder) {
        if (dataStore == null) {
            if (this.aliasStampMap.containsKey(stamp)) {
                final int alias = this.aliasStampMap.get(stamp);

                builder.add(alias);
                getAliasesReverse(alias, builder);
            }
        } else {
            Integer currentAlias = aliasToStamp.get(stamp);
            if (currentAlias != null) {
                builder.add(currentAlias.intValue());
                getAliasesReverse(currentAlias.intValue(), builder);
            }
        }
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public int getSize() {
        assert this.stampAliasMap.size() == this.aliasStampMap.size() :
                "stampAliasMap.size() = " + this.stampAliasMap.size() + " aliasStampMap.size() = " +
                        this.aliasStampMap.size();
        return dataStore == null ? this.aliasStampMap.size() : aliasToStamp.size();
    }

    /**
     * Gets the stamp alias stream.
     *
     * @return the stamp alias stream
     */
    public Stream<StampAlias> getStampAliasStream() {
       return dataStore == null ?
               StreamSupport.stream(this.aliasStampMap.keyValuesView().collect(each -> new StampAlias(each.getTwo(), each.getOne())).spliterator(), false):
               aliasToStamp.getStream().map(entry -> new StampAlias(entry.getValue(), entry.getKey()));
    }

}

