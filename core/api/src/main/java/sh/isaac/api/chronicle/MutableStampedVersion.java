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



package sh.isaac.api.chronicle;

import sh.isaac.api.Status;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.transaction.Transaction;

/**
 * The Interface MutableStampedVersion.
 *
 * @author kec
 */
public interface MutableStampedVersion
        extends StampedVersion {
   /**
    * Sets the state.
    * 
    * @param state the new Status
    * @param t transaction involved in this edit 
    */
   void setStatus(Status state, Transaction t);
   /**
    * Sets the author nid.
    *
    * @param authorNid the new author nid
    * @param t transaction involved in this edit
    */
   void setAuthorNid(int authorNid, Transaction t);

   /**
    * Sets the module nid.
    *
    * @param moduleNid the new module nid
    * @param t transaction involved in this edit
    */
   void setModuleNid(int moduleNid, Transaction t);

   /**
    * Sets the path nid.
    *
    * @param pathNid the new path nid
    * @param t transaction involved in this edit
    */
   void setPathNid(int pathNid, Transaction t);

   /**
    * Sets the time.
    *
    * @param time the new time
    * @param t transaction involved in this edit
    */
   void setTime(long time, Transaction t);
}

