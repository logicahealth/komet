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

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.file.Path;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------

import org.glassfish.hk2.api.PerLookup;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.metacontent.MetaContentService;
import sh.isaac.api.util.TimeFlushBufferedOutputStream;
import sh.isaac.api.externalizable.IsaacExternalizable;

//~--- classes ----------------------------------------------------------------

/**
 * The Class BinaryDataWriterProvider.
 *
 * @author kec
 */
@Service(name = "ibdfWriter")
@PerLookup
public class BinaryDataWriterProvider
         implements DataWriterService {
   /** The Constant BUFFER_SIZE. */
   private static final int BUFFER_SIZE = 1024;

   //~--- fields --------------------------------------------------------------

   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

   /** The pause block. */
   private final Semaphore pauseBlock = new Semaphore(1);

   /** The buffer. */
   ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(BUFFER_SIZE);

   /** The data path. */
   Path dataPath;

   /** The output. */
   DataOutputStream output;

   //~--- constructors --------------------------------------------------------

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
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public BinaryDataWriterProvider(Path dataPath)
            throws IOException {
      this();
      configure(dataPath);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Close.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Override
   public void close()
            throws IOException {
      try {
         this.output.flush();
         this.output.close();
      } finally {
         this.output = null;
      }
   }

   /**
    * Configure.
    *
    * @param path the path
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Override
   public final void configure(Path path)
            throws IOException {
      if (this.output != null) {
         throw new RuntimeException("Reconfiguration is not supported");
      }

      this.dataPath = path;
      this.output = new DataOutputStream(new TimeFlushBufferedOutputStream(new FileOutputStream(this.dataPath.toFile(),
            true)));
      this.buffer.setExternalData(true);
      LOG.info("ibdf changeset writer has been configured to write to " +
                       this.dataPath.toAbsolutePath().toString());

      if (!Get.configurationService()
              .inDBBuildMode()) {
         // record this file as already being in the database if we are in 'normal' run mode.
         final MetaContentService mcs = LookupService.get()
                                                     .getService(MetaContentService.class);

         if (mcs != null) {
            final ConcurrentMap<String, Boolean> processedChangesets = mcs.<String,
                                                                          Boolean>openStore("processedChangesets");

            processedChangesets.put(path.getFileName()
                                        .toString(), true);
         }
      }
   }

   /**
    * Flush.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    * @see sh.isaac.api.externalizable.DataWriterService#flush()
    */
   @Override
   public void flush()
            throws IOException {
      if (this.output != null) {
         this.output.flush();
      }
   }

   /**
    * Pause.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Override
   public void pause()
            throws IOException {
      if (this.output == null) {
         LOG.warn("already paused!");
         return;
      }

      this.pauseBlock.acquireUninterruptibly();
      close();
      LOG.debug("ibdf writer paused");
   }

   /**
    * Put.
    *
    * @param ochreObject the ochre object
    * @throws RuntimeException the runtime exception
    */
   @Override
   public void put(IsaacExternalizable ochreObject)
            throws RuntimeException {
      try {
         this.pauseBlock.acquireUninterruptibly();
         this.buffer.clear();
         ochreObject.putExternal(this.buffer);
         this.output.writeByte(ochreObject.getExternalizableObjectType()
                                          .getToken());
         this.output.writeByte(ochreObject.getDataFormatVersion());
         this.output.writeInt(this.buffer.getLimit());
         this.output.write(this.buffer.getData(), 0, this.buffer.getLimit());
      } catch (final IOException e) {
         throw new RuntimeException(e);
      } finally {
         this.pauseBlock.release();
      }
   }

   /**
    * Resume.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Override
   public void resume()
            throws IOException {
      if (this.pauseBlock.availablePermits() == 1) {
         LOG.warn("asked to resume, but not paused?");
         return;
      }

      if (this.output == null) {
         configure(this.dataPath);
      }

      this.pauseBlock.release();
      LOG.debug("ibdf writer resumed");
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the current path.
    *
    * @return the current path
    */
   @Override
   public Path getCurrentPath() {
      return this.dataPath;
   }
}

