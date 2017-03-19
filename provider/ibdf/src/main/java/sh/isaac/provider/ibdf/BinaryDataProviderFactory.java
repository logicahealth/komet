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

import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.file.Path;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.externalizable.BinaryDataReaderQueueService;
import sh.isaac.api.externalizable.BinaryDataReaderService;
import sh.isaac.api.externalizable.BinaryDataServiceFactory;
import sh.isaac.api.externalizable.DataWriterService;

//~--- classes ----------------------------------------------------------------

/**
 * A factory for creating BinaryDataProvider objects.
 *
 * @author kec
 */
@Service
@Singleton
public class BinaryDataProviderFactory
         implements BinaryDataServiceFactory {
   
   /**
    * Gets the queue reader.
    *
    * @param dataPath the data path
    * @return the queue reader
    * @throws FileNotFoundException the file not found exception
    */
   @Override
   public BinaryDataReaderQueueService getQueueReader(Path dataPath)
            throws FileNotFoundException {
      return new BinaryDataReaderQueueProvider(dataPath);
   }

   /**
    * Gets the reader.
    *
    * @param dataPath the data path
    * @return the reader
    * @throws FileNotFoundException the file not found exception
    */
   @Override
   public BinaryDataReaderService getReader(Path dataPath)
            throws FileNotFoundException {
      return new BinaryDataReaderProvider(dataPath);
   }

   /**
    * Gets the writer.
    *
    * @param dataPath the data path
    * @return the writer
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Override
   public DataWriterService getWriter(Path dataPath)
            throws IOException {
      return new BinaryDataWriterProvider(dataPath);
   }
}

