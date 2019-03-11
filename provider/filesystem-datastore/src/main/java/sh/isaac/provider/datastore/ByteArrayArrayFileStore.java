package sh.isaac.provider.datastore;

import sh.isaac.model.collections.store.ByteArrayArrayStore;

import java.io.*;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static sh.isaac.model.collections.SpineFileUtil.SPINE_PREFIX;

public class ByteArrayArrayFileStore extends SpinedArrayFileStore implements ByteArrayArrayStore {

    public ByteArrayArrayFileStore(File directory) {
        super(directory);
    }

    public ByteArrayArrayFileStore(File directory, Semaphore diskSemaphore) {
        super(directory, diskSemaphore);
    }

    @Override
    public Optional<AtomicReferenceArray<byte[][]>> get(int spineIndex) {
        String spineKey = SPINE_PREFIX + spineIndex;
        File spineFile = new File(directory, spineKey);
        if (spineFile.exists()) {
            diskSemaphore.acquireUninterruptibly();
            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(spineFile)))) {
                int arraySize = dis.readInt();
                byte[][][] spineArray = new byte[arraySize][][];
                for (int i = 0; i < arraySize; i++) {
                    int valueSize = dis.readInt();
                    if (valueSize != 0) {
                        byte[][] value = new byte[valueSize][];
                        for (int j = 0; j < valueSize; j++) {
                            int valuePartSize = dis.readInt();
                            byte[] valuePart = new byte[valuePartSize];
                            dis.readFully(valuePart);
                            value[j] = valuePart;
                        }
                        spineArray[i] = value;
                    }
                }
                AtomicReferenceArray<byte[][]> spine = new AtomicReferenceArray<>(spineArray);
                return Optional.of(spine);

            } catch (IOException ex) {
                LOG.error(ex);
            } finally {
                diskSemaphore.release();
            }
        }
        return Optional.empty();
    }

    @Override
    public void put(int spineIndex, AtomicReferenceArray<byte[][]> spine) {
        String spineKey = SPINE_PREFIX + spineIndex;
        File spineFile = new File(directory, spineKey);
        diskSemaphore.acquireUninterruptibly();
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(spineFile)))) {
            dos.writeInt(spine.length());
            for (int i = 0; i < spine.length(); i++) {
                byte[][] value = spine.get(i);
                if (value == null) {
                    dos.writeInt(0);
                } else {
                    dos.writeInt(value.length);
                    for (byte[] valuePart : value) {
                        dos.writeInt(valuePart.length);
                        dos.write(valuePart);
                    }
                }
            }
        } catch (IOException ex) {
            LOG.error(ex);
        } finally {
            diskSemaphore.release();
        }
    }

}
