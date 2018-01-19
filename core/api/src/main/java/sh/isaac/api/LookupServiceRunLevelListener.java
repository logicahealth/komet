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

package sh.isaac.api;

import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.ChangeableRunLevelFuture;
import org.glassfish.hk2.runlevel.ErrorInformation;
import org.glassfish.hk2.runlevel.RunLevelFuture;
import org.glassfish.hk2.runlevel.RunLevelListener;
import org.jvnet.hk2.annotations.Service;

/**
 * Simple class to capture otherwise-lost exceptions that happen during service shutdowns.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class LookupServiceRunLevelListener implements RunLevelListener{

   private static final Logger LOG = LogManager.getLogger();
   
   @Override
   public void onProgress(ChangeableRunLevelFuture currentJob, int levelAchieved) {
      // noop
      
   }

   @Override
   public void onCancelled(RunLevelFuture currentJob, int levelAchieved) {
      LOG.info("RunLevel change was cancelled?  Current Job: {} levelAchieved: {}", currentJob, levelAchieved);
   }

   //TODO [KEC] it would appear that when moving down levels, HK2 doesn't throw exceptions.. it eats them.
   //We should decide if we should pick up the exception this way, and throw it as part of our level change, 
   //Or, just log and let it go, since we are shutting down.
   @Override
   public void onError(RunLevelFuture currentJob, ErrorInformation errorInformation) {
      LOG.error("Error during Runlevel change - currentJob: {} Failed Service: {}", currentJob, 
            errorInformation.getFailedDescriptor().getImplementation(), errorInformation.getError());
   }
}
