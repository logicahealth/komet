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



package sh.isaac.api.externalizable.json;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.file.Path;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

//~--- non-JDK imports --------------------------------------------------------

import org.glassfish.hk2.api.PerLookup;

import org.jvnet.hk2.annotations.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cedarsoftware.util.io.JsonWriter;

import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.OchreExternalizable;
import sh.isaac.api.util.TimeFlushBufferedOutputStream;

//~--- classes ----------------------------------------------------------------

/**
 * {@link JsonDataWriterService} - serialize to JSON
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service(name = "jsonWriter")
@PerLookup
public class JsonDataWriterService
         implements DataWriterService {
   private final Logger           logger     = LoggerFactory.getLogger(JsonDataWriterService.class);
   private final Semaphore        pauseBlock = new Semaphore(1);
   private JsonWriter       json_;
   private FileOutputStream fos_;
   private Path             dataPath;

   //~--- constructors --------------------------------------------------------

   private JsonDataWriterService()
            throws IOException {
      // For HK2
   }

   public JsonDataWriterService(File path)
            throws IOException {
      this();
      configure(path.toPath());
   }

   /**
    * To support non HK2 useage
    * @param path
    * @throws IOException
    */
   public JsonDataWriterService(Path path)
            throws IOException {
      this();
      configure(path);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void close()
            throws IOException {
      try {
         this.json_.close();
         this.fos_.close();
      } finally {
         this.json_ = null;
         this.fos_  = null;
      }
   }

   @Override
   public void configure(Path path)
            throws IOException {
      if (this.json_ != null) {
         throw new IOException("Reconfiguration not supported");
      }

      final Map<String, Object> args = new HashMap<>();

      args.put(JsonWriter.PRETTY_PRINT, true);
      this.dataPath = path;
      this.fos_     = new FileOutputStream(path.toFile(), true);
      this.json_    = new JsonWriter(new TimeFlushBufferedOutputStream(this.fos_), args);
      this.json_.addWriter(ConceptChronology.class, new Writers.ConceptChronologyJsonWriter());
      this.json_.addWriter(SememeChronology.class, new Writers.SememeChronologyJsonWriter());
      this.logger.info("json changeset writer has been configured to write to " + this.dataPath.toAbsolutePath().toString());
   }

   /**
    * @throws IOException
    * @see sh.isaac.api.externalizable.DataWriterService#flush()
    */
   @Override
   public void flush()
            throws IOException {
      if (this.json_ != null) {
         this.json_.flush();
      }
   }

   @Override
   public void pause()
            throws IOException {
      if (this.json_ == null) {
         this.logger.warn("already paused!");
         return;
      }

      this.pauseBlock.acquireUninterruptibly();
      close();
      this.logger.debug("json writer paused");
   }

   @Override
   public void put(OchreExternalizable ochreObject) {
      try {
         this.pauseBlock.acquireUninterruptibly();
         this.json_.write(ochreObject);
      } finally {
         this.pauseBlock.release();
      }
   }

   /**
    * Write out a string object to the json file - this will encode all illegal characters within the string.
    * Useful for writing debugging files
    */
   public void put(String string) {
      try {
         this.pauseBlock.acquireUninterruptibly();
         this.json_.write(string);
      } finally {
         this.pauseBlock.release();
      }
   }

   @Override
   public void resume()
            throws IOException {
      if (this.pauseBlock.availablePermits() == 1) {
         this.logger.warn("asked to resume, but not paused?");
         return;
      }

      if (this.json_ == null) {
         configure(this.dataPath);
      }

      this.pauseBlock.release();
      this.logger.debug("json writer resumed");
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Path getCurrentPath() {
      return this.dataPath;
   }
}

