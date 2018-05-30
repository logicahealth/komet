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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Path;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

//~--- interfaces -------------------------------------------------------------

/**
 * A factory for creating BinaryDataService objects.
 *
 * @author kec
 */
@Contract
public interface BinaryDataServiceFactory {
   /**
    * Gets the queue reader.
    *
    * @param dataPath data file location
    * @return the BinaryDataReaderService for the given dataPath
    * @throws FileNotFoundException the file not found exception
    */
   BinaryDataReaderQueueService getQueueReader(Path dataPath)
            throws FileNotFoundException;
   
   /**
    * Gets the queue reader.
    * @param inputStream the inputStream to read
    * @return the BinaryDataReaderService for the given dataPath
    */
   BinaryDataReaderQueueService getQueueReader(InputStream inputStream);

   /**
    * Gets the reader.
    *
    * @param dataPath data file location
    * @return the BinaryDataReaderService for the given dataPath
    * @throws FileNotFoundException the file not found exception
    */
   BinaryDataReaderService getReader(Path dataPath)
            throws FileNotFoundException;

   /**
    * Gets the reader.
    *
    * @param inputStream data stream to process
    * @return the BinaryDataReaderService for the given dataPath
    * @throws FileNotFoundException the file not found exception
    */
   BinaryDataReaderService getReader(InputStream inputStream)
            throws FileNotFoundException;

   /**
    * Gets the writer.
    *
    * @param dataPath data file location
    * @return the BinaryDataWriterService for the given dataPath
    * @throws IOException Signals that an I/O exception has occurred.
    */
   DataWriterService getWriter(Path dataPath)
            throws IOException;
}

