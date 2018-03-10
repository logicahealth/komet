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

//~--- JDK imports ------------------------------------------------------------

import sh.isaac.model.datastream.IsaacExternalizableUnparsed;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.file.Path;

import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.externalizable.BinaryDataReaderQueueService;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.externalizable.IsaacExternalizable;

//~--- classes ----------------------------------------------------------------

/**
 * {@link BinaryDataReaderQueueProvider}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class BinaryDataReaderQueueProvider
        extends TimedTaskWithProgressTracker<Integer>
         implements BinaryDataReaderQueueService, Spliterator<IsaacExternalizableUnparsed> {
   
   private static final Logger LOG = LogManager.getLogger();
   /** The objects. */
   int objects = 0;

   /** The not started. */
   int NOTSTARTED = 3;

   /** The running. */
   int RUNNING = 2;

   /** The done reading. */
   int DONEREADING = 1;

   /** The complete. */
   int COMPLETE = 0;

   /** The complete. */
   final CountDownLatch complete = new CountDownLatch(this.NOTSTARTED);

   /** The complete block. */
   Semaphore completeBlock = new Semaphore(1);

   /** The read data. */

   // Only one thread doing the reading from disk, give it lots of buffer space
   private final BlockingQueue<IsaacExternalizableUnparsed> readData = new ArrayBlockingQueue<>(5000);

   /** The parsed data. */

   // This buffers from between the time when we deserialize the object, and when we write it back to the DB.
   private final BlockingQueue<IsaacExternalizable> parsedData = new ArrayBlockingQueue<>(50);

   /** The data path. */
   Path dataPath;

   /** The input. */
   DataInputStream input;

   /** The stream bytes. */
   int streamBytes;
   
   private boolean failed = false;

   /** The es. */
   ExecutorService es;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new binary data reader queue provider.
    *
    * @param dataPath the data path
    * @throws FileNotFoundException the file not found exception
    * this.input.available(); gives inconsistent results? 
    * @deprecated inconsistent results? Try BinaryDatastreamReader
    */
   public BinaryDataReaderQueueProvider(Path dataPath)
            throws FileNotFoundException {
      this.dataPath = dataPath;
      this.input    = new DataInputStream(new FileInputStream(dataPath.toFile()));

      try {
         this.streamBytes = this.input.available();
         addToTotalWork(this.streamBytes);
      } catch (final IOException ex) {
         throw new RuntimeException(ex);
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Characteristics.
    *
    * @return the int
    */
   @Override
   public int characteristics() {
      return IMMUTABLE | NONNULL;
   }

   /**
    * Estimate size.
    *
    * @return the long
    */
   @Override
   public long estimateSize() {
      return Long.MAX_VALUE;
   }

   /**
    * Shutdown.
    */
   @Override
   public void shutdown() {
      try {
         LOG.info("Shutdown called on BinaryDataReaderQueueProvider, read {} object byte arrays", this.objects);
         this.input.close();

         if (this.complete.getCount() == this.RUNNING) {
            this.complete.countDown();
         }

         this.es.shutdown();

         //Wait for the executor service to drain the queue
         while (!this.readData.isEmpty()) {
             Thread.sleep(50);
         }

         //Wake up the sleeping threads in the es
         this.es.shutdownNow();
         //wait for the last running threads to process their work
         this.es.awaitTermination(5, TimeUnit.MINUTES);
         LOG.info("All objects parsed");

         if (this.complete.getCount() == this.DONEREADING) {
            this.complete.countDown();
         }

         done();
      } catch (IOException | InterruptedException ex) {
         throw new RuntimeException(ex);
      }
   }

   /**
    * Try advance.
    *
    * @param action the action
    * @return true, if successful
    */
   @Override
   public boolean tryAdvance(Consumer<? super IsaacExternalizableUnparsed> action) {
      try {
         final int                           recordSizeInBytes        = this.input.readInt();
         final byte[]                        objectData        = new byte[recordSizeInBytes];

         this.input.readFully(objectData);

         final ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(objectData);

         buffer.setExternalData(true);
         action.accept(new IsaacExternalizableUnparsed(buffer));
         this.objects++;
         completedUnitsOfWork(objectData.length + 4);
         return true;
      } catch (final EOFException ex) {
         return false;
      } catch (final IOException ex) {
         throw new RuntimeException(ex);
      }
   }

   /**
    * Try split.
    *
    * @return the spliterator
    */
   @Override
   public Spliterator<IsaacExternalizableUnparsed> trySplit() {
      return null;
   }

   /**
    * Call.
    *
    * @return the number of objects read.
    */
   @Override
   protected Integer call() {
      try {
         this.complete.await();
      } catch (final InterruptedException ex) {
         throw new RuntimeException(ex);
      }

      return this.objects;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if finished.
    *
    * @return true, if finished
    */
   @Override
   public boolean isFinished() {
      if (failed)
      {
         throw new RuntimeException("Error in reading threads!");
      }
      return this.complete.getCount() == this.COMPLETE;
   }

   /**
    * Gets the queue.
    *
    * @return the queue
    * @see sh.isaac.api.externalizable.BinaryDataReaderQueueService#getQueue()
    */
   @Override
   public BlockingQueue<IsaacExternalizable> getQueue() {
      if (this.complete.getCount() == this.NOTSTARTED) {
         try {
            this.completeBlock.acquireUninterruptibly();

            if (this.complete.getCount() == this.NOTSTARTED) {
               this.complete.countDown();

               // These threads handle the parsing of the bytes back into ochre objects, which is kind of slow, as all of the UUIDs have
               // to be resolved back to nids and sequences.  Seems to work best to use about 2/3 of the processors here.
               int threadCount = Math.round(Runtime.getRuntime()
                                                   .availableProcessors() * (float) 0.667);

               threadCount = ((threadCount < 2) ? 2
                                                : threadCount);
               this.es    = Executors.newFixedThreadPool(threadCount);

               for (int i = 0; i < threadCount; i++) {
                  this.es.execute(() -> {
                     while (!((this.complete.getCount() <= this.DONEREADING) && this.readData.isEmpty())) {
                        Boolean accepted = null;

                        try {
                           IsaacExternalizable taken = this.readData.take().parse();
                           while (accepted == null)
                           {
                              try {
                                 accepted = this.parsedData.offer(taken, 5, TimeUnit.MINUTES);
                              } catch (InterruptedException e) {
                                 // we ignore interrupts when offering, we don't want to lose this.
                              }
                           }
                           if (!accepted) {
                              throw new RuntimeException("unexpeced queue issues");
                           }
                        } catch (final InterruptedException e) {
                           LOG.debug("es interrupted");
                        }
                        catch (Exception e) {
                           LOG.error("Parsing error", e);
                           failed = true;
                           throw e;  //this kills the thread....
                        }
                     }
                     LOG.debug("Thread ends");
                  });
               }

               Get.workExecutors().getExecutor().execute(() -> {
                  LOG.debug("Thread to read from disk begins");
                  try {
                     getStreamInternal().forEach((unparsed) -> {
                        try {
                           this.readData.offer(unparsed, 5, TimeUnit.MINUTES);
                        } catch (final Exception e) {
                           LOG.warn("exception in offer?", e);
                           throw new RuntimeException(e);
                        }
                     });
                  } catch (final Exception e) {
                     LOG.error("exception in read?", e);
                     failed = true;
                     Get.workExecutors().getExecutor().execute(() -> {
                        shutdown();
                     });
                     throw e;
                  }
                  LOG.debug("Thread to read from disk completes - doing shutdown");
                  shutdown();
               });
            }
         } finally {
            this.completeBlock.release();
         }
      }

      return this.parsedData;
   }

   /**
    * Gets the stream internal.
    *
    * @return the stream internal
    */
   private Stream<IsaacExternalizableUnparsed> getStreamInternal() {
      running();
      return StreamSupport.stream(this, false);
   }
}

