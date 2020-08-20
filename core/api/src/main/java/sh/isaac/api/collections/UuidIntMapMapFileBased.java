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
package sh.isaac.api.collections;

//~--- JDK imports ------------------------------------------------------------
import java.io.*;
import java.nio.file.Files;
//~--- non-JDK imports --------------------------------------------------------
import sh.isaac.api.ConfigurationService.BuildMode;
import sh.isaac.api.Get;
import sh.isaac.api.collections.uuidnidmap.ConcurrentUuidToIntHashMap;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.memory.DiskSemaphore;
import sh.isaac.api.memory.HoldInMemoryCache;
import sh.isaac.api.memory.MemoryManagedReference;
import sh.isaac.api.memory.WriteToDiskCache;

//~--- classes ----------------------------------------------------------------
/**
 * Created by kec on 7/27/14.
 */
public class UuidIntMapMapFileBased
        extends UuidIntMapMap {

    //~--- fields --------------------------------------------------------------

    /**
     * The folder.
     */
    private final File folder;
    /**
     * The maps.
     */
    @SuppressWarnings("unchecked")
    protected final MemoryManagedReference<ConcurrentUuidToIntHashMap>[] maps = new MemoryManagedReference[NUMBER_OF_MAPS];

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new uuid int map map.
     *
     * @param folder the folder
     */
    private UuidIntMapMapFileBased(File folder) {
        folder.mkdirs();
        this.folder = folder;

        for (int i = 0; i < this.maps.length; i++) {
            this.maps[i] = new MemoryManagedReference<>(null, new File(folder, i + "-uuid-nid.map"));
            WriteToDiskCache.addToCache(this.maps[i]);
        }

        //Loader utility enables this when doing IBDF file creation to to get from nid back to UUID  - this prevents it from doing table scans.
        if (Get.configurationService().isInDBBuildMode(BuildMode.IBDF)) {
            enableInverseCache();
        }
        
        File params = new File(folder, "map.params");
        try {
         if (params.isFile()) {
              ByteArrayDataBuffer badb = new ByteArrayDataBuffer(Files.readAllBytes(params.toPath()));
              NEXT_NID_PROVIDER.set(badb.getInt());
           }
      } catch (IOException e) {
         throw new RuntimeException(e);
      }

        LOG.debug("Created UuidIntMapMap: " + this);
    }

    /**
     * Gets the map.
     *
     * @param index the index
     * @return the map
     * @throws RuntimeException the runtime exception
     */
    protected ConcurrentUuidToIntHashMap getMap(int index)
            throws RuntimeException {
        ConcurrentUuidToIntHashMap result = this.maps[index].get();

        while (result == null) {
            getNewMap(index);
            result = this.maps[index].get();
        }

        this.maps[index].elementRead();
        HoldInMemoryCache.addToCache(this.maps[index]);
        return result;
    }

    /**
     * Creates the.
     *
     * @param folder the folder
     * @return the uuid int map map
     */
    public static UuidIntMapMapFileBased create(File folder) {
        return new UuidIntMapMapFileBased(folder);
    }

    /**
     * Write.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void write()
            throws IOException {
        for (int i = 0; i < NUMBER_OF_MAPS; i++) {
            final ConcurrentUuidToIntHashMap map = this.maps[i].get();

            if ((map != null) && this.maps[i].hasUnwrittenUpdate()) {
                this.maps[i].write();
            }
        }
        
        ByteArrayDataBuffer badb = new ByteArrayDataBuffer();
        badb.putInt(getMaxNid());

        Files.write(new File(folder, "map.params").toPath(), badb.getData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDiskSpaceUsed() {
        int memoryInUse = 0;
        for (int mapIndex = 0; mapIndex < this.maps.length; mapIndex++) {
            memoryInUse += getMapDiskUsage(mapIndex);
        }
        return memoryInUse;
    }

    protected int getMapDiskUsage(int i) {
        final File mapFile = new File(this.folder, i + "-uuid-nid.map");

        if (mapFile.exists()) {
            return (int) mapFile.length();
        }
        return 0;
    }

    /**
     * Read map from disk.
     *
     * @param i the i
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void getNewMap(int i) {
        this.lock.lock();

        try {
            if (this.maps[i].get() == null) {
                final File mapFile = new File(this.folder, i + "-uuid-nid.map");

                if (mapFile.exists()) {
                    DiskSemaphore.acquire();

                    try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(mapFile)))) {
                        this.maps[i] = new MemoryManagedReference<>(ConcurrentUuidToIntHashMap.deserialize(in), mapFile);
                        WriteToDiskCache.addToCache(this.maps[i]);
                        LOG.trace("UuidIntMapMap restored: " + i + " from: " + this + " file: " + mapFile.getAbsolutePath());
                     } finally {
                        DiskSemaphore.release();
                    }
                } else {
                    this.maps[i] = new MemoryManagedReference<>(new ConcurrentUuidToIntHashMap(DEFAULT_MAP_SIZE,
                            MIN_LOAD_FACTOR,
                            MAX_LOAD_FACTOR),
                            new File(this.folder, i + "-uuid-nid.map"));
                    WriteToDiskCache.addToCache(this.maps[i]);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException();
        } finally {
            this.lock.unlock();
        }
    }

    protected void mapElementUpdated(int mapIndex) {
        this.maps[mapIndex].elementUpdated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMemoryInUse() {
        int memoryInUse = 0;
        for (MemoryManagedReference<ConcurrentUuidToIntHashMap> map : maps) {
            if (map.get() != null) {
                memoryInUse += map.get().getMemoryInUse();
            }
        }
        return memoryInUse;
    }

    //~--- get methods ---------------------------------------------------------

    //~--- get methods ---------------------------------------------------------

}
