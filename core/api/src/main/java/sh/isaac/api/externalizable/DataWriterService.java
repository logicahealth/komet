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

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface DataWriterService.
 *
 * @author kec
 */
@Contract
public interface DataWriterService
        extends AutoCloseable {
   /**
    * Close.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Override
   public void close()
            throws IOException;;

   /**
    * Used when constructed via a no arg constructor (HK2 patterns) to configure the writer after the initial
    * construct.  Implements are free to not support reconfiguration after the initial call to configure
    * (or to not support this method at all, if the only configuration route is via the constructor)
    *
    * @param path the path
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws UnsupportedOperationException - when method not supported at all, or for reconfiguration
    */
   public void configure(Path path)
            throws IOException, UnsupportedOperationException;

   /**
    * flush any buffered data out to disk.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void flush()
            throws IOException;

   /**
    * flush any unwritten data, close the file writer, and block any {@link DataWriterService#put(OchreExternalizable)} calls until
    * resume is called.  This feature is useful when you want to ensure the file on disk doesn't change while another thread picks
    * up the file and pushes it to git, for example.
    *
    * Ensure that if pause() is called, that resume is called from the same thread.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void pause()
            throws IOException;

   /**
    * This does not throw an IOException, rather, they are possible, but mapped to runtime exceptions for stream convenience.
    * they still may occur, however, and should be handled.
    *
    * @param ochreObject the ochre object
    * @throws RuntimeException the runtime exception
    */
   public void put(OchreExternalizable ochreObject)
            throws RuntimeException;

   /**
    * open the file writer (closed by a {@link #pause()}) and unblock any blocked  {@link DataWriterService#put(OchreExternalizable)} calls.
    * Ensure that if pause() is called, that resume is called from the same thread.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void resume()
            throws IOException;

   //~--- get methods ---------------------------------------------------------

   /**
    * Return the path the writer is currently configured to.
    *
    * @return the current path
    */
   public Path getCurrentPath();
}

