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



package sh.isaac.api.externalizable;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.nio.file.Path;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sh.isaac.api.LookupService;

//~--- classes ----------------------------------------------------------------

/**
 * Simple wrapper class to allow us to serialize to multiple formats at once.
 *
 * Also includes logic for incorporating the date and a UUID into the file name to ensure uniqueueness,
 * and logic for rotating the changeset files.
 *
 * {@link MultipleDataWriterService}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class MultipleDataWriterService
         implements DataWriterService {
   ArrayList<DataWriterService>   writers_         = new ArrayList<>();
   private Logger                 logger           = LoggerFactory.getLogger(MultipleDataWriterService.class);
   private final SimpleDateFormat sdf              = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
   private AtomicInteger          objectWriteCount = new AtomicInteger();
   private final int rotateAfter =
      10000;  // This will cause us to rotate files after ~ 1 MB of IBDF content, in rough testing.
   private String  prefix;
   private boolean enableRotate;

   //~--- constructors --------------------------------------------------------

   /**
    * This constructor creates a multiple data writer service which writes to the specified files, and does not do any rotation or autonaming.
    * @param jsonPath
    * @param ibdfPath
    */
   public MultipleDataWriterService(Optional<Path> jsonPath, Optional<Path> ibdfPath)
            throws IOException {
      enableRotate = false;

      if (jsonPath.isPresent()) {
         // Use HK2 here to make fortify stop false-flagging an open resource error
         DataWriterService writer = LookupService.get()
                                                 .getService(DataWriterService.class, "jsonWriter");

         if (writer != null) {
            writer.configure(jsonPath.get());
            writers_.add(writer);
         } else {
            LogManager.getLogger()
                      .warn("json writer was requested, but not found on classpath!");
         }
      }

      if (ibdfPath.isPresent()) {
         DataWriterService writer = LookupService.get()
                                                 .getService(DataWriterService.class, "ibdfWriter");

         if (writer != null) {
            writer.configure(ibdfPath.get());
            writers_.add(writer);
         } else {
            LogManager.getLogger()
                      .warn("ibdf writer was requested, but not found on classpath!");
         }
      }
   }

   /**
    * This constructor sets up the multipleDataWriter in such a way that is will create date stamped and UUID unique file names, rotating them after
    * a certain number of writes, to prevent them from growing too large.
    *
    * This constructor will also start a mode where we do NOT keep 0 length files - therefore, if we start, and stop, and the last file that was being written
    * to is size 0, the last file will be deleted.
    *
    * @param folderToWriteInto
    * @param prefix
    * @param jsonExtension
    * @param ibdfExtension
    * @throws IOException
    */
   public MultipleDataWriterService(Path folderToWriteInto,
                                    String prefix,
                                    Optional<String> jsonExtension,
                                    Optional<String> ibdfExtension)
            throws IOException {
      this.prefix  = prefix;
      enableRotate = true;

      String fileNamePrefix = prefix + sdf.format(new Date()) + "_" + UUID.randomUUID().toString() + ".";

      if (jsonExtension.isPresent()) {
         // Use HK2 here to make fortify stop false-flagging an open resource error
         DataWriterService writer = LookupService.get()
                                                 .getService(DataWriterService.class, "jsonWriter");

         if (writer != null) {
            writer.configure(folderToWriteInto.resolve(fileNamePrefix + jsonExtension.get()));
            writers_.add(writer);
         } else {
            LogManager.getLogger()
                      .warn("json writer was requested, but not found on classpath!");
         }
      }

      if (ibdfExtension.isPresent()) {
         DataWriterService writer = LookupService.get()
                                                 .getService(DataWriterService.class, "ibdfWriter");

         if (writer != null) {
            writer.configure(folderToWriteInto.resolve(fileNamePrefix + ibdfExtension.get()));
            writers_.add(writer);
         } else {
            LogManager.getLogger()
                      .warn("ibdf writer was requested, but not found on classpath!");
         }
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * @throws IOException
    * @see sh.isaac.api.externalizable.DataWriterService#close()
    */
   @Override
   public void close()
            throws IOException {
      handleMulti((writer) -> {
                     try {
                        writer.close();
                        return null;
                     } catch (IOException e) {
                        return e;
                     }
                  });
   }

   /**
    * @see sh.isaac.api.externalizable.DataWriterService#configure(java.nio.file.Path)
    */
   @Override
   public void configure(Path path)
            throws UnsupportedOperationException {
      throw new UnsupportedOperationException("Method not supported");
   }

   /**
    * @see sh.isaac.api.externalizable.DataWriterService#flush()
    */
   @Override
   public void flush()
            throws IOException {
      handleMulti((writer) -> {
                     try {
                        writer.flush();
                        return null;
                     } catch (IOException e) {
                        return e;
                     }
                  });
   }

   public void handleMulti(Function<DataWriterService, IOException> function)
            throws IOException {
      ArrayList<IOException> exceptions = new ArrayList<>();

      for (DataWriterService writer: writers_) {
         IOException e = function.apply(writer);

         if (e != null) {
            exceptions.add(e);
         }
      }

      if (exceptions.size() > 0) {
         if (exceptions.size() > 1) {
            for (int i = 1; i < exceptions.size(); i++) {
               logger.error("extra, unthrown exception: ", exceptions.get(i));
            }
         }

         throw exceptions.get(0);
      }
   }

   /**
    * @throws IOException
    * @see sh.isaac.api.externalizable.DataWriterService#pause()
    */
   @Override
   public void pause()
            throws IOException {
      handleMulti((writer) -> {
                     try {
                        writer.pause();
                        return null;
                     } catch (IOException e) {
                        return e;
                     }
                  });
   }

   /**
    * @throws IOException
    * @see sh.isaac.api.externalizable.DataWriterService#put(sh.isaac.api.externalizable.OchreExternalizable)
    */
   @Override
   public void put(OchreExternalizable ochreObject)
            throws RuntimeException {
      try {
         handleMulti((writer) -> {
                        try {
                           writer.put(ochreObject);
                           return null;
                        } catch (RuntimeException e) {
                           return new IOException(e);
                        }
                     });
      } catch (IOException e) {
         if ((e.getCause() != null) && (e.getCause() instanceof RuntimeException)) {
            throw(RuntimeException) e.getCause();
         } else {
            logger.warn("Unexpected", e);
            throw new RuntimeException(e);
         }
      }

      if (enableRotate && (objectWriteCount.incrementAndGet() >= rotateAfter)) {
         rotateFiles();
      }
   }

   /**
    * @throws IOException
    * @see sh.isaac.api.externalizable.DataWriterService#resume()
    */
   @Override
   public void resume()
            throws IOException {
      handleMulti((writer) -> {
                     try {
                        writer.resume();
                        return null;
                     } catch (IOException e) {
                        return e;
                     }
                  });
   }

   private void rotateFiles()
            throws RuntimeException {
      try {
         pause();

         String fileNamePrefix = prefix + sdf.format(new Date()) + "_" + UUID.randomUUID().toString();

         for (DataWriterService writer: writers_) {
            String extension = writer.getCurrentPath()
                                     .getFileName()
                                     .toString();

            extension = extension.substring(extension.lastIndexOf('.'));
            writer.configure(writer.getCurrentPath()
                                   .getParent()
                                   .resolve(fileNamePrefix + extension));
         }

         objectWriteCount.set(0);
         resume();
      } catch (IOException e) {
         logger.error("Unexpected error rotating changeset files!", e);
         throw new RuntimeException(e);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Path getCurrentPath() {
      throw new UnsupportedOperationException("Method not supported");
   }
}

