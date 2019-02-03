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
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import sh.isaac.api.Get;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.NamedThreadFactory;
import sh.isaac.api.util.ThreadPoolExecutorFixed;

/**
 * Due to threading within this for the processing of input objects, it is unsafe to read back from the datastore while this import is happening.
 * 
 * This applies to the passed in BiConsumer as well - it is unsafe to perform reads on the datastore within the logic of the BiConsumer.
 * 
 * If you want to do readbacks during the load, you may need to special-handle obscure errors, or, you can use the constructor with 
 * processInOrder set to true, which will hurt performance, but make readback safe within the BiConsumer.  It will still not be safe
 * from other threads in the application...
 *
 * @author kec
 */
public class BinaryDatastreamReader
        extends TimedTaskWithProgressTracker<Integer> {

    private final BiConsumer<? super IsaacExternalizable, byte[]> action;
    private final Path path;
    private final InputStream inputStream;
    private final int permits;
    private final Semaphore processingSemaphore;
    private final long bytesToProcess;
    private final AtomicReference<Throwable> exception = new AtomicReference<>();
    private ThreadPoolExecutor parsingExecutor;
    
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
        this.inputStream = null;
        this.bytesToProcess = path.toFile().length();
        permits = Runtime.getRuntime().availableProcessors() * 2;
        processingSemaphore = new Semaphore(permits);
        parsingExecutor = processInOrder ? 
              new ThreadPoolExecutorFixed(1, 1, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new NamedThreadFactory("BinaryDataStreamOrderedRead", false)) 
              : Get.executor();
        addToTotalWork(this.bytesToProcess);
        updateTitle("Importing from " + path.toFile().getName());
        Get.activeTasks().add(this);
    }
    
    /**
     * Read an IBDF stream, parse each object, and call the provided consumer with each parsed object.
     * @param action where to send the parsed objects from the file
     * @param is - the input stream to read
     */
    public BinaryDatastreamReader(BiConsumer<? super IsaacExternalizable, byte[]> action, InputStream is) {
        this.action = action;
        this.path = null;
        this.inputStream = is;
        this.bytesToProcess = -1;
        permits = Runtime.getRuntime().availableProcessors() * 2;
        processingSemaphore = new Semaphore(permits);
        parsingExecutor = Get.executor();
        updateTitle("Importing from input stream" + is.toString());
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
    
    public BinaryDatastreamReader(Consumer<? super IsaacExternalizable> action, InputStream is) {
        this((externalizable, data) -> {action.accept(externalizable);}, is);
    }

    @Override
    protected Integer call() throws Exception {
        try (DataInputStream input = new DataInputStream(path == null ? inputStream : new FileInputStream(path.toFile()))) {
            long bytesProcessed = 0;
            int objectCount = 0;
            while (true) {
                throwIfException();
                try
                {
                    if (this.bytesToProcess > 0 && bytesProcessed >= this.bytesToProcess) {
                        //if we are processing a file, we know the byte count, so we can exit without an EOF exception.
                        break;
                    }
                    final int recordSizeInBytes = input.readInt();
                    objectCount++;
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
                    parsingExecutor.execute(processor);
                }
                catch (EOFException e) {
                    if (path == null) {
                        //Normal escape route, if we are processing a passed in stream
                        break;
                    }
                    else {
                        throw e;
                    }
                }
            }
            this.processingSemaphore.acquireUninterruptibly(permits);
            throwIfException();
            return objectCount;
        } finally {
            if (parsingExecutor != Get.executor()) {
                //they asked for in-order parsing, we need to shut down our own executor
                parsingExecutor.shutdown();
                parsingExecutor.awaitTermination(1, TimeUnit.MINUTES);
            }
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
