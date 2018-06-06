/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.model.datastream;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import sh.isaac.api.Get;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

/**
 *
 * @author kec
 */
public class BinaryDatastreamReader
        extends TimedTaskWithProgressTracker<Integer> {

    private final BiConsumer<? super IsaacExternalizable, byte[]> action;
    private final Path path;
    private final int permits;
    private final Semaphore processingSemaphore;
    private final long bytesToProcess;
    private final AtomicReference<Throwable> exception = new AtomicReference<>();
    
    /**
     * Read an IBDF file, parse each object, and call the provided consumer with each parsed object.
     * @param action where to send the parsed objects from the file
     * @param path - the file to read
     * @param processInOrder - if true, this runs single threaded, so the ibdf file will be parsed in order.  If false, 
     * the parsing runs in parallel threads, and may happen out-of-order.
     */
    public BinaryDatastreamReader(BiConsumer<? super IsaacExternalizable, byte[]> action, Path path, boolean processInOrder) {
        this.action = action;
        this.path = path;
        this.bytesToProcess = path.toFile().length();
        permits = processInOrder ? 1 : Runtime.getRuntime().availableProcessors() * 2;
        processingSemaphore = new Semaphore(permits);
        addToTotalWork(this.bytesToProcess);
        updateTitle("Importing from " + path.toFile().getName());
        Get.activeTasks().add(this);
    }
    
    /**
     * Calls {@link #BinaryDatastreamReader(BiConsumer, Path, boolean)} with processInOrder set to false.
     * @param action where to send the parsed objects from the file
     * @param path - the file to read
     */
    public BinaryDatastreamReader(BiConsumer<? super IsaacExternalizable, byte[]> action, Path path) {
        this(action, path, false);
    }

    public BinaryDatastreamReader(Consumer<? super IsaacExternalizable> action, Path path) {
        this((externalizable, data) -> {action.accept(externalizable);}, path);
    }

    @Override
    protected Integer call() throws Exception {
        try (DataInputStream input = new DataInputStream(new FileInputStream(path.toFile()))) {
            long bytesProcessed = 0;
            int objectCount = 0;
            while (this.bytesToProcess > bytesProcessed) {
                throwIfException();
                objectCount++;
                final int recordSizeInBytes = input.readInt();
                bytesProcessed += 4;
                final byte[] objectData = new byte[recordSizeInBytes];
                input.readFully(objectData);
                bytesProcessed += recordSizeInBytes;
 
                ByteArrayDataBuffer byteArrayDataBuffer = new ByteArrayDataBuffer(objectData);
                byteArrayDataBuffer.setExternalData(true);
                IsaacExternalizableUnparsed unparsedObject
                        = new IsaacExternalizableUnparsed(byteArrayDataBuffer);
                completedUnitsOfWork(recordSizeInBytes);

                this.processingSemaphore.acquireUninterruptibly();
                
                Processor processor = new Processor(unparsedObject);
                Get.executor().execute(processor);
            }
            this.processingSemaphore.acquireUninterruptibly(permits);
            throwIfException();
            return objectCount;
        } finally {
            Get.activeTasks().remove(this);
        }
    }

    private void throwIfException() throws Exception {
        if (exception.get() != null) {
            throw new Exception("Exceptions during import", exception.get());
        }
    }

    private class Processor implements Runnable {

        final IsaacExternalizableUnparsed unparsedObject;

        public Processor(IsaacExternalizableUnparsed unparsedObject) {
            this.unparsedObject = unparsedObject;
        }

        @Override
        public void run() {
            try {
                IsaacExternalizable isaacObject = unparsedObject.parse();
                action.accept(isaacObject, unparsedObject.getBytes());
            } catch (Throwable t) {
                exception.set(t);
            } finally {
                BinaryDatastreamReader.this.processingSemaphore.release();
            }
        }

    }

}
