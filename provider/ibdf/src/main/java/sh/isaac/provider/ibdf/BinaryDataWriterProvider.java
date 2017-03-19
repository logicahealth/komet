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

//~--- non-JDK imports --------------------------------------------------------

import org.glassfish.hk2.api.PerLookup;

import org.jvnet.hk2.annotations.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.OchreExternalizable;
import sh.isaac.api.metacontent.MetaContentService;
import sh.isaac.api.util.TimeFlushBufferedOutputStream;

//~--- classes ----------------------------------------------------------------

/**
 * @author kec
 */
@Service(name = "ibdfWriter")
@PerLookup
public class BinaryDataWriterProvider
         implements DataWriterService {
   private static final int BUFFER_SIZE = 1024;

   //~--- fields --------------------------------------------------------------

   private Logger      logger     = LoggerFactory.getLogger(BinaryDataWriterProvider.class);
   private Semaphore   pauseBlock = new Semaphore(1);
   ByteArrayDataBuffer buffer     = new ByteArrayDataBuffer(BUFFER_SIZE);
   Path                dataPath;
   DataOutputStream    output;

   //~--- constructors --------------------------------------------------------

   private BinaryDataWriterProvider()
            throws IOException {
      // for HK2
   }

   /**
    * For non-HK2 use cases
    * @param dataPath
    * @throws IOException
    */
   public BinaryDataWriterProvider(Path dataPath)
            throws IOException {
      this();
      configure(dataPath);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void close()
            throws IOException {
      try {
         output.flush();
         output.close();
      } finally {
         output = null;
      }
   }

   @Override
   public void configure(Path path)
            throws IOException {
      if (this.output != null) {
         throw new RuntimeException("Reconfiguration is not supported");
      }

      dataPath = path;
      output   = new DataOutputStream(new TimeFlushBufferedOutputStream(new FileOutputStream(dataPath.toFile(), true)));
      buffer.setExternalData(true);
      logger.info("ibdf changeset writer has been configured to write to " + dataPath.toAbsolutePath().toString());

      if (!Get.configurationService()
              .inDBBuildMode()) {
         // record this file as already being in the database if we are in 'normal' run mode.
         MetaContentService mcs = LookupService.get()
                                               .getService(MetaContentService.class);

         if (mcs != null) {
            ConcurrentMap<String, Boolean> processedChangesets = mcs.<String, Boolean>openStore("processedChangesets");

            processedChangesets.put(path.getFileName()
                                        .toString(), true);
         }
      }
   }

   /**
    * @throws IOException
    * @see sh.isaac.api.externalizable.DataWriterService#flush()
    */
   @Override
   public void flush()
            throws IOException {
      if (output != null) {
         output.flush();
      }
   }

   @Override
   public void pause()
            throws IOException {
      if (output == null) {
         logger.warn("already paused!");
         return;
      }

      pauseBlock.acquireUninterruptibly();
      close();
      logger.debug("ibdf writer paused");
   }

   @Override
   public void put(OchreExternalizable ochreObject)
            throws RuntimeException {
      try {
         pauseBlock.acquireUninterruptibly();
         buffer.clear();
         ochreObject.putExternal(buffer);
         output.writeByte(ochreObject.getOchreObjectType()
                                     .getToken());
         output.writeByte(ochreObject.getDataFormatVersion());
         output.writeInt(buffer.getLimit());
         output.write(buffer.getData(), 0, buffer.getLimit());
      } catch (IOException e) {
         throw new RuntimeException(e);
      } finally {
         pauseBlock.release();
      }
   }

   @Override
   public void resume()
            throws IOException {
      if (pauseBlock.availablePermits() == 1) {
         logger.warn("asked to resume, but not paused?");
         return;
      }

      if (output == null) {
         configure(dataPath);
      }

      pauseBlock.release();
      logger.debug("ibdf writer resumed");
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Path getCurrentPath() {
      return dataPath;
   }
}

