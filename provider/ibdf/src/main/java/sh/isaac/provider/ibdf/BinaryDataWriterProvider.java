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

package sh.isaac.provider.ibdf;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.metacontent.MetaContentService;
import sh.isaac.api.util.NamedThreadFactory;
import sh.isaac.api.util.TimeFlushBufferedOutputStream;

/**
 * The Class BinaryDataWriterProvider.
 *
 * @author kec
 */
@Service(name = "ibdfWriter")
@PerLookup
public class BinaryDataWriterProvider
         implements DataWriterService {

   private static final int BUFFER_SIZE = 1024;
   private static final Logger LOG = LogManager.getLogger();

   //Used to prevent corruption of the file being written, and to pause put operations, so other thread can read the file when necessary, 
   //for example, when doing a git sync.
   private final Semaphore ioBlock = new Semaphore(1);

   //Where the file is written.
   private Path dataPath;

   private DataOutputStream output;
   
   //In threaded mode, this is the intermediate buffer / queue to accept / hold input in a non-blocking way.
   private ArrayBlockingQueue<Runnable> queue = null;
   
   //in threaded mode, a single-threaded executor that processes the queue.
   private ThreadPoolExecutor tpe;

   /**
    * Instantiates a new binary data writer provider.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private BinaryDataWriterProvider()
            throws IOException {
      // for HK2
   }

   /**
    * For non-HK2 use cases.
    *
    * @param dataPath the data path
    * @param threadedWrites enable or disable caching all {@link #put(IsaacExternalizable)} calls into a blocking queue, 
    * and having a single thread do all writing from the queue.  This option is useful for a specific converter usecase.
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public BinaryDataWriterProvider(Path dataPath, boolean threadedWrites)
            throws IOException {
      this();
      if (threadedWrites) {
         queue = new ArrayBlockingQueue<>(1000);
         tpe = new ThreadPoolExecutor(1, 1, 5, TimeUnit.MINUTES, queue, new NamedThreadFactory("BinaryDataWriter thread", false));
         
         RejectedExecutionHandler block = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
               try {
                  executor.getQueue().put(r);
               }
               catch (InterruptedException e){
                  throw new RuntimeException(e);
               }
            }
         };
         tpe.setRejectedExecutionHandler(block);
      }
      configure(dataPath);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void close()
            throws IOException {
      try {
         if (tpe != null) {
            tpe.shutdown();
         }
      } finally {
          closeFileOnly();
      }
   }
   
   /*
    * For internal use by pause - doesn't tear down the executor.
    */
   private void closeFileOnly()
           throws IOException {
     try {
        this.output.flush();
        this.output.close();
     } finally {
        this.output = null;
     }
  }

   /**
    * {@inheritDoc}
    */
   @Override
   public final void configure(Path path)
            throws IOException {
      if (this.output != null) {
         throw new RuntimeException("Reconfiguration is not supported");
      }

      this.dataPath = path;
      this.dataPath.toFile().getParentFile().mkdirs();
      //this needs to be append mode, because the ibdf file writer is paused and resumed many times during the lifecycle - 
      //specifically, to allow syncing to git....
      this.output = new DataOutputStream(new TimeFlushBufferedOutputStream(new FileOutputStream(this.dataPath.toFile(), true)));
      LOG.info("ibdf changeset writer has been configured to write to " + this.dataPath.toAbsolutePath().toString());

      if (!Get.configurationService().isInDBBuildMode()) {
         // record this file as already being in the database if we are in 'normal' run mode.
         final MetaContentService mcs = LookupService.get().getService(MetaContentService.class);

         if (mcs != null) {
            final ConcurrentMap<String, Integer> processedChangesets = mcs.getChangesetStore();
            processedChangesets.put(path.getFileName().toString(), (int) path.toFile().length());
         } else {
            LOG.warn("No implementation of a MetaContentService is available, this will lead to reprocessing of all changeset files on each startup");
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void flush()
            throws IOException {
      if (queue != null)
      {
         while (!queue.isEmpty()) {
            // Ugly, but not sure of a better way to make sure the queue is drained...
            try {
               Thread.sleep(10);
            }
            catch (InterruptedException e) {
               LOG.debug("Interrupted while flushing?");
               // probably a reason, exit the loop.
            }
         }
      }
      DataOutputStream localOutput = this.output;
      if (localOutput != null) {
         localOutput.flush();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void pause()
            throws IOException {
      if (this.output == null) {
         LOG.warn("already paused!");
         return;
      }

      this.ioBlock.acquireUninterruptibly();
      closeFileOnly();
      LOG.debug("ibdf writer paused");
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void put(IsaacExternalizable ochreObject)
            throws RuntimeException {

      //Convert the content in the calling thread.
      ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(BUFFER_SIZE);
      buffer.setExternalData(true);
      ochreObject.putExternal(buffer);
      if (queue == null) {
         //no queue, go ahead and block / write on this thread. 
         realPut(buffer);
      }
      else {
         tpe.execute(() -> realPut(buffer));
      }
   }
   
   private void realPut(ByteArrayDataBuffer bufferToWrite)
           throws RuntimeException {
     try {
        this.ioBlock.acquireUninterruptibly();
        this.output.writeInt(bufferToWrite.getLimit());
        this.output.write(bufferToWrite.getData(), 0, bufferToWrite.getLimit());
     } catch (final IOException e) {
        throw new RuntimeException(e);
     } finally {
        this.ioBlock.release();
     }
  }

   /**
    * {@inheritDoc}
    */
   @Override
   public void resume()
            throws IOException {
      if (this.ioBlock.availablePermits() == 1) {
         LOG.warn("asked to resume, but not paused?");
         return;
      }

      if (this.output == null) {
         configure(this.dataPath);
      }

      this.ioBlock.release();
      LOG.debug("ibdf writer resumed");
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Path getCurrentPath() {
      return this.dataPath;
   }
}