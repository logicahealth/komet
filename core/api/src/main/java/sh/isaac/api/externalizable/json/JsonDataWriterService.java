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

import com.cedarsoftware.util.io.JsonWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.util.TimeFlushBufferedOutputStream;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.component.semantic.SemanticChronology;

//~--- classes ----------------------------------------------------------------

/**
 * {@link JsonDataWriterService} - serialize to JSON.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
//@Service(name = "jsonWriter")
//@PerLookup
public class JsonDataWriterService
         implements DataWriterService {
   /** The logger. */
   private static final Logger LOG = LogManager.getLogger();

   /** The pause block. */
   private final Semaphore pauseBlock = new Semaphore(1);

   /** The json. */
   private JsonWriter json;

   /** The fos. */
   private FileOutputStream fos;

   /** The data path. */
   private Path dataPath;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new json data writer service.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private JsonDataWriterService()
            throws IOException {
      // For HK2
   }

   /**
    * Instantiates a new json data writer service.
    *
    * @param path the path
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public JsonDataWriterService(File path)
            throws IOException {
      this();
      configure(path.toPath());
   }

   /**
    * To support non HK2 useage.
    *
    * @param path the path
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public JsonDataWriterService(Path path)
            throws IOException {
      this();
      configure(path);
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
         this.json.close();
         this.fos.close();
      } finally {
         this.json = null;
         this.fos  = null;
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
      if (this.json != null) {
         throw new IOException("Reconfiguration not supported");
      }

      final Map<String, Object> args = new HashMap<>();

      args.put(JsonWriter.PRETTY_PRINT, true);
      this.dataPath = path;
      this.fos     = new FileOutputStream(path.toFile(), true);
      this.json    = new JsonWriter(new TimeFlushBufferedOutputStream(this.fos), args);
      this.json.addWriter(ConceptChronology.class, new Writers.ConceptChronologyJsonWriter());
      this.json.addWriter(SemanticChronology.class, new Writers.SemanticChronologyJsonWriter());
      LOG.info("json changeset writer has been configured to write to " +
                       this.dataPath.toAbsolutePath().toString());
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
      if (this.json != null) {
         this.json.flush();
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
      if (this.json == null) {
         LOG.warn("already paused!");
         return;
      }

      this.pauseBlock.acquireUninterruptibly();
      close();
      LOG.debug("json writer paused");
   }

   /**
    * Put.
    *
    * @param ochreObject the ochre object
    */
   @Override
   public void put(IsaacExternalizable ochreObject) {
      try {
         this.pauseBlock.acquireUninterruptibly();
         this.json.write(ochreObject);
      } finally {
         this.pauseBlock.release();
      }
   }

   /**
    * Write out a string object to the json file - this will encode all illegal characters within the string.
    * Useful for writing debugging files
    *
    * @param string the string
    */
   public void put(String string) {
      try {
         this.pauseBlock.acquireUninterruptibly();
         this.json.write(string);
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

      if (this.json == null) {
         configure(this.dataPath);
      }

      this.pauseBlock.release();
      LOG.debug("json writer resumed");
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

