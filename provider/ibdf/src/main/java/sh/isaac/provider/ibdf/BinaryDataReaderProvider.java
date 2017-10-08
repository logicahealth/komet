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
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.externalizable.BinaryDataReaderService;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.externalizable.IsaacExternalizable;

//~--- classes ----------------------------------------------------------------

/**
 * The Class BinaryDataReaderProvider.
 *
 * @author kec
 */
public class BinaryDataReaderProvider
        extends TimedTaskWithProgressTracker<Integer>
         implements BinaryDataReaderService, Spliterator<IsaacExternalizable> {
   /** The objects. */
   int objects = 0;

   /** The complete. */
   CountDownLatch complete = new CountDownLatch(1);

   /** The data path. */
   Path dataPath;

   /** The input. */
   DataInputStream input;

   /** The stream bytes. */
   int streamBytes;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new binary data reader provider.
    *
    * @param dataPath the data path
    * @throws FileNotFoundException the file not found exception
    */
   public BinaryDataReaderProvider(Path dataPath)
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
    * Close.
    */
   @Override
   public void close() {
      try {
         this.input.close();
         done();
         this.complete.countDown();
      } catch (final IOException ex) {
         throw new RuntimeException(ex);
      }
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
    * Try advance.
    *
    * @param action the action
    * @return true, if successful
    */
   @Override
   public boolean tryAdvance(Consumer<? super IsaacExternalizable> action) {
      try {
         final int                           startBytesAvailable        = this.input.available();
         final int                           recordSizeInBytes        = this.input.readInt();
         final byte[]                        objectData        = new byte[recordSizeInBytes];
         this.input.readFully(objectData);
         
         ByteArrayDataBuffer byteArrayDataBuffer = new ByteArrayDataBuffer(objectData);
         byteArrayDataBuffer.setExternalData(true);
         IsaacExternalizableUnparsed unparsedObject = new IsaacExternalizableUnparsed(byteArrayDataBuffer);
         action.accept(unparsedObject.parse());

         this.objects++;
         completedUnitsOfWork(startBytesAvailable - this.input.available());
         return true;
      } catch (final EOFException ex) {
         close();
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
   public Spliterator<IsaacExternalizable> trySplit() {
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
    * Gets the stream.
    *
    * @return the stream
    */
   @Override
   public Stream<IsaacExternalizable> getStream() {
      running();
      return StreamSupport.stream(this, false);
   }
}

