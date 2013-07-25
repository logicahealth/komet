/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.ihtsdo.otf.tcc.api.concept;

//~--- JDK imports ------------------------------------------------------------

import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.ContinuationTrackerBI;

/**
 *
 * @author kec
 */
public interface ProcessUnfetchedConceptDataBI extends ContinuationTrackerBI {

   /**
    * @return True if user is allowed to cancel this process.
    */
   boolean allowCancel();

   /**
    * Method description
    *
    *
    * @param cNid
    * @param fetcher
    *
    * @throws Exception
    */
   void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception;

   /**
    *
    * @return a <code>NativeIdSetBI</code> of concept nids to iterate over. A null value is allowed
    * and will cause all concepts to be iterated over.
    * @throws IOException
    */
   NativeIdSetBI getNidSet() throws IOException;

   /**
    *
    * @return Title for this processor, for display in global progress list.
    */
   String getTitle();
}
