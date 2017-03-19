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



package sh.isaac.api.commit;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.nio.file.Path;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

//~--- interfaces -------------------------------------------------------------

/**
 * {@link ChangeSetWriterService}
 *
 * Any @Service annotated class which implements this interface will get the notifications below, when
 * index events happen.
 *
 * @author <a href="mailto:nmarques@westcoastinformatics.com">Nuno Marques</a>
 */
@Contract
public interface ChangeSetWriterService {
   /**
    * Disable {@link ChangeSetWriterService} from writing. Anything received
    * while service is disabled is skipped.
    *
    */
   public void disable();

   /**
    * Enable {@link ChangeSetWriterService} to write.
    *
    */
   public void enable();

   /**
    * flush any unwritten data, close the underlying file writer(s), and block further writes to disk until
    * resume is called. This feature is useful when you want to ensure the file on disk doesn't change while another thread picks
    * up the file and pushes it to git, for example.
    *
    * Ensure that if pause() is called, that resume is called from the same thread.
    *
    * @throws IOException
    */
   public void pause()
            throws IOException;

   /**
    * open the file writer (closed by a {@link #pause()}) and unblock any blocked write calls.
    * Ensure that if pause() is called, that resume is called from the same thread.
    *
    * @throws IOException
    */
   public void resume()
            throws IOException;

   //~--- get methods ---------------------------------------------------------

   /**
    * Return the path to the folder that contains the changesets.
    */
   public Path getWriteFolder();

   /**
    * Determine if the writer in the service is disabled or enabled for writing.
    *
    * @return {@code true} if enabled or {@code false} if disabled.
    */
   public boolean getWriteStatus();
}

