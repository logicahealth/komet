/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package sh.isaac.api.externalizable;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;

/**
 * Simple wrapper class to allow us to serialize to multiple formats at once.
 *
 * Also includes logic for incorporating the date and a UUID into the file name to ensure uniqueueness,
 * and logic for rotating the changeset files.
 * 
 * Also provides logic to use a thread-per-writer, so that distinct format writes happen in parallel.
 *
 * {@link MultipleDataWriterService}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class MultipleDataWriterService implements DataWriterService
{
    private static final Logger LOG = LogManager.getLogger();
    
    private final ArrayList<DataWriterService> writers = new ArrayList<>();

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private final AtomicInteger objectWriteCount = new AtomicInteger();

    private final int rotateAfter = 10000;  // This will cause us to rotate files after ~ 1 MB of IBDF content, in rough testing.

    private final boolean enableRotate;
    
    private String prefix;
    
    private Semaphore threadedWrite;

    /**
     * This constructor creates a multiple data writer service which writes to the specified files, and does not do any rotation or autonaming.
     *
     * @param jsonPath the json path
     * @param ibdfPath the ibdf path
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public MultipleDataWriterService(Optional<Path> jsonPath, Optional<Path> ibdfPath) throws IOException {
        this.enableRotate = false;

        if (jsonPath.isPresent()) {
            // Use HK2 here to make fortify stop false-flagging an open resource error
            final DataWriterService writer = LookupService.get().getService(DataWriterService.class, "jsonWriter");

            if (writer != null) {
                writer.configure(jsonPath.get());
                this.writers.add(writer);
            }
            else {
                LogManager.getLogger().warn("json writer was requested, but not found on classpath!");
            }
        }

        if (ibdfPath.isPresent()) {
            final DataWriterService writer = LookupService.get().getService(DataWriterService.class, "ibdfWriter");

            if (writer != null) {
                writer.configure(ibdfPath.get());
                this.writers.add(writer);
            }
            else {
                LogManager.getLogger().warn("ibdf writer was requested, but not found on classpath!");
            }
        }
        threadedWrite = new Semaphore(writers.size());
    }

    /**
     * This constructor sets up the multipleDataWriter in such a way that is will create date stamped and UUID unique file names, rotating them after
     * a certain number of writes, to prevent them from growing too large.
     *
     * This constructor will also start a mode where we do NOT keep 0 length files - therefore, if we start, and stop, and the last file that was
     * being written to is size 0, the last file will be deleted.
     *
     * @param folderToWriteInto the folder to write into
     * @param prefix the prefix
     * @param jsonExtension the json extension
     * @param ibdfExtension the ibdf extension
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public MultipleDataWriterService(Path folderToWriteInto, String prefix, Optional<String> jsonExtension, Optional<String> ibdfExtension) throws IOException {
        this.prefix = prefix;
        this.enableRotate = true;

        final String fileNamePrefix = prefix + this.sdf.format(new Date()) + "_" + UUID.randomUUID().toString() + ".";

        if (jsonExtension.isPresent()) {
            // Use HK2 here to make fortify stop false-flagging an open resource error
            final DataWriterService writer = LookupService.get().getService(DataWriterService.class, "jsonWriter");

            if (writer != null) {
                writer.configure(folderToWriteInto.resolve(fileNamePrefix + jsonExtension.get()));
                this.writers.add(writer);
            }
            else {
                LogManager.getLogger().warn("json writer was requested, but not found on classpath!");
            }
        }

        if (ibdfExtension.isPresent())
        {
            final DataWriterService writer = LookupService.get().getService(DataWriterService.class, "ibdfWriter");

            if (writer != null) {
                writer.configure(folderToWriteInto.resolve(fileNamePrefix + ibdfExtension.get()));
                this.writers.add(writer);
            }
            else {
                LogManager.getLogger().warn("ibdf writer was requested, but not found on classpath!");
            }
        }
        threadedWrite = new Semaphore(writers.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        handleMulti((writer) -> {
            try {
                writer.close();
                return null;
            }
            catch (final IOException e) {
                return e;
            }
        }, false);
    }

    /**
     * Unsupported by this implementation
     */
    @Override
    public void configure(Path path) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Method not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        handleMulti((writer) -> {
            try {
                writer.flush();
                return null;
            }
            catch (final IOException e) {
                return e;
            }
        }, writers.size() > 1);
    }

    /**
     * Utility method to handle all of our writers
     *
     * @param function the function
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void handleMulti(Function<DataWriterService, IOException> function, boolean thread) throws IOException {
        final ArrayList<IOException> exceptions = new ArrayList<>();
        
        for (final DataWriterService writer : this.writers) {
            threadedWrite.acquireUninterruptibly();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        IOException e = function.apply(writer);
                        if (e != null) {
                            exceptions.add(e);
                        }
                    }
                    finally {
                        threadedWrite.release();
                    }
                }
            };
            
            if (thread) {
                Get.workExecutors().getPotentiallyBlockingExecutor().execute(r);
            }
            else {
                r.run();
            }
        }
        
        threadedWrite.acquireUninterruptibly(writers.size());
        threadedWrite.release(writers.size());
        
        if (exceptions.size() > 0) {
            if (exceptions.size() > 1) {
                for (int i = 1; i < exceptions.size(); i++) {
                    LOG.error("extra, unthrown exception: ", exceptions.get(i));
                }
            }
            throw exceptions.get(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pause() throws IOException {
        handleMulti((writer) -> {
            try {
                writer.pause();
                return null;
            }
            catch (final IOException e) {
                return e;
            }
        }, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(IsaacExternalizable isaacObject) throws RuntimeException
    {
        try {
            handleMulti((writer) -> {
                try {
                    writer.put(isaacObject);
                    return null;
                }
                catch (final RuntimeException e) {
                    return new IOException(e);
                }
            }, writers.size() > 1);
        }
        catch (final IOException e) {
            if ((e.getCause() != null) && (e.getCause() instanceof RuntimeException)) {
                throw (RuntimeException) e.getCause();
            }
            else {
                LOG.warn("Unexpected", e);
                throw new RuntimeException(e);
            }
        }

        if (this.enableRotate && (this.objectWriteCount.incrementAndGet() >= this.rotateAfter)) {
            rotateFiles();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resume() throws IOException {
        handleMulti((writer) -> {
            try {
                writer.resume();
                return null;
            }
            catch (final IOException e) {
                return e;
            }
        }, false);
    }

    /**
     * Rotate files.
     *
     * @throws RuntimeException the runtime exception
     */
    private void rotateFiles() throws RuntimeException
    {
        try {
            pause();

            final String fileNamePrefix = this.prefix + this.sdf.format(new Date()) + "_" + UUID.randomUUID().toString();

            handleMulti((writer) -> {
                String extension = writer.getCurrentPath().getFileName().toString();

                extension = extension.substring(extension.lastIndexOf('.'));
                try {
                    writer.configure(writer.getCurrentPath().getParent().resolve(fileNamePrefix + extension));
                }
                catch (IOException e) {
                    return e;
                }
                return null;
            }, writers.size() > 1);

            this.objectWriteCount.set(0);
            resume();
        }
        catch (final IOException e) {
            LOG.error("Unexpected error rotating changeset files!", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Unsupported by this implementation
     */
    @Override
    public Path getCurrentPath() {
        throw new UnsupportedOperationException("Method not supported");
    }
}
