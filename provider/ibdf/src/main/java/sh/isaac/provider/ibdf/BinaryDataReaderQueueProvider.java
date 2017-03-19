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

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.externalizable.BinaryDataReaderQueueService;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.OchreExternalizable;
import sh.isaac.api.externalizable.OchreExternalizableObjectType;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

//~--- classes ----------------------------------------------------------------

/**
 * {@link BinaryDataReaderQueueProvider}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class BinaryDataReaderQueueProvider
        extends TimedTaskWithProgressTracker<Integer>
         implements BinaryDataReaderQueueService, Spliterator<OchreExternalizableUnparsed> {
   
   /** The objects. */
   int                  objects       = 0;
   
   /** The notstarted. */
   int                  NOTSTARTED    = 3;
   
   /** The running. */
   int                  RUNNING       = 2;
   
   /** The donereading. */
   int                  DONEREADING   = 1;
   
   /** The comlete. */
   int                  COMLETE       = 0;
   
   /** The complete. */
   final CountDownLatch complete      = new CountDownLatch(this.NOTSTARTED);
   
   /** The complete block. */
   Semaphore            completeBlock = new Semaphore(1);

   /** The read data. */
   // Only one thread doing the reading from disk, give it lots of buffer space
   private final BlockingQueue<OchreExternalizableUnparsed> readData = new ArrayBlockingQueue<>(5000);

   /** The parsed data. */
   // This buffers from between the time when we deserialize the object, and when we write it back to the DB.
   private final BlockingQueue<OchreExternalizable> parsedData = new ArrayBlockingQueue<>(50);
   
   /** The data path. */
   Path                                       dataPath;
   
   /** The input. */
   DataInputStream                            input;
   
   /** The stream bytes. */
   int                                        streamBytes;
   
   /** The es. */
   ExecutorService                            es_;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new binary data reader queue provider.
    *
    * @param dataPath the data path
    * @throws FileNotFoundException the file not found exception
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
         this.input.close();

         if (this.complete.getCount() == this.RUNNING) {
            this.complete.countDown();
         }

         this.es_.shutdown();

         while (!this.readData.isEmpty()) {
            Thread.sleep(10);
         }

         this.es_.shutdownNow();
         this.es_.awaitTermination(50, TimeUnit.MINUTES);

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
   public boolean tryAdvance(Consumer<? super OchreExternalizableUnparsed> action) {
      try {
         final int                           startBytes        = this.input.available();
         final OchreExternalizableObjectType type              = OchreExternalizableObjectType.fromDataStream(this.input);
         final byte                          dataFormatVersion = this.input.readByte();
         final int                           recordSize        = this.input.readInt();
         final byte[]                        objectData        = new byte[recordSize];

         this.input.readFully(objectData);

         final ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(objectData);

         buffer.setExternalData(true);
         buffer.setObjectDataFormatVersion(dataFormatVersion);
         action.accept(new OchreExternalizableUnparsed(type, buffer));
         this.objects++;
         completedUnitsOfWork(startBytes - this.input.available());
         return true;
      } catch (final EOFException ex) {
         shutdown();
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
   public Spliterator<OchreExternalizableUnparsed> trySplit() {
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
      return this.complete.getCount() == this.COMLETE;
   }

   /**
    * Gets the queue.
    *
    * @return the queue
    * @see sh.isaac.api.externalizable.BinaryDataReaderQueueService#getQueue()
    */
   @Override
   public BlockingQueue<OchreExternalizable> getQueue() {
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
               this.es_         = Executors.newFixedThreadPool(threadCount);

               for (int i = 0; i < threadCount; i++) {
                  this.es_.execute(() -> {
                                 while ((this.complete.getCount() > this.COMLETE) ||!this.readData.isEmpty()) {
                                    boolean accepted;

                                    try {
                                       accepted = this.parsedData.offer(this.readData.take()
                                             .parse(), 5, TimeUnit.MINUTES);
                                    } catch (final InterruptedException e) {
                                       break;
                                    }

                                    if (!accepted) {
                                       throw new RuntimeException("unexpeced queue issues");
                                    }
                                 }
                              });
               }

               Get.workExecutors().getExecutor().execute(() -> {
                              try {
                                 getStreamInternal().forEach((unparsed) -> {
                              try {
                                 this.readData.offer(unparsed, 5, TimeUnit.MINUTES);
                              } catch (final Exception e) {
                                 throw new RuntimeException(e);
                              }
                           });
                              } catch (final Exception e) {
                                 Get.workExecutors().getExecutor().execute(() -> {
                                                shutdown();
                                             });
                                 throw e;
                              }
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
   private Stream<OchreExternalizableUnparsed> getStreamInternal() {
      running();
      return StreamSupport.stream(this, false);
   }
}

