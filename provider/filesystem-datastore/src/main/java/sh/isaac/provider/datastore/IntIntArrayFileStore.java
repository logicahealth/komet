package sh.isaac.provider.datastore;

import sh.isaac.model.collections.SpineFileUtil;
import sh.isaac.model.collections.store.IntIntArrayStore;

import java.io.*;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class IntIntArrayFileStore extends SpinedArrayFileStore implements IntIntArrayStore {

    public IntIntArrayFileStore(File directory) {
        super(directory);
    }

    public IntIntArrayFileStore(File directory, Semaphore diskSemaphore) {
        super(directory, diskSemaphore);
    }

    @Override
    public Optional<AtomicReferenceArray<int[]>> get(int spineIndex) {
        String spineKey = SpineFileUtil.SPINE_PREFIX + spineIndex;
        File spineFile = new File(directory, spineKey);
        if (!spineFile.exists()) {
            return Optional.empty();
        }
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(spineFile)))) {
            int arraySize = dis.readInt();
            AtomicReferenceArray<int[]> data = new AtomicReferenceArray<>(arraySize);
            for (int i = 0; i < arraySize; i++) {
                int valueSize = dis.readInt();
                if (valueSize != 0) {
                    int[] value = new int[valueSize];
                    for (int j = 0; j < valueSize; j++) {
                        value[j] = dis.readInt();
                    }
                    data.set(i, value);
                }
            }
            return Optional.of(data);
        } catch (IOException ex) {
            LOG.error(ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void put(int spineIndex, AtomicReferenceArray<int[]> spine) {
        directory.mkdirs();
        String spineKey = SpineFileUtil.SPINE_PREFIX + spineIndex;
        File spineFile = new File(directory, spineKey);
        diskSemaphore.acquireUninterruptibly();
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(spineFile)))) {
            dos.writeInt(spine.length());
            for (int i = 0; i < spine.length(); i++) {
                int[] value = spine.get(i);
                if (value == null) {
                    dos.writeInt(0);
                } else {
                    dos.writeInt(value.length);
                    for (int valueElement : value) {
                        dos.writeInt(valueElement);
                    }
                }
            }
        } catch (IOException ex) {
            LOG.error(ex);
            throw new RuntimeException(ex);
        } finally {
            diskSemaphore.release();
        }
    }
}
