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



package sh.isaac.provider.sync.git;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.HashMap;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.LookupService;
import sh.isaac.api.sync.MergeFailOption;
import sh.isaac.api.sync.SyncFiles;

//~--- classes ----------------------------------------------------------------

/**
 * {@link SyncTesting}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class SyncTesting {
   /**
    * The main method.
    *
    * @param args the arguments
    * @throws Exception the exception
    */
   public static void main(String[] args)
            throws Exception {
      // Configure Java logging into log4j2
      System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");

      final SyncFiles ssg         = LookupService.getService(SyncServiceGIT.class);
      final File      localFolder = new File("/mnt/SSD/scratch/gitTesting");

      ssg.setRootLocation(localFolder);

      final String username = "username";
      final char[] password = "password".toCharArray();

      ssg.linkAndFetchFromRemote("https://github.com/" + username + "/test.git", username, password);
      ssg.linkAndFetchFromRemote("ssh://" + username + "@csfe.aceworkspace.net:29418/testrepo", username, password);
      ssg.addUntrackedFiles();
      System.out.println("UpdateCommitAndPush result: " + ssg.updateCommitAndPush("mergetest2",
            username,
            password,
            MergeFailOption.FAIL,
            (String[]) null));
      ssg.removeFiles("b");
      System.out.println("Update from remote result: " + ssg.updateFromRemote(username,
            password,
            MergeFailOption.FAIL));

      final HashMap<String, MergeFailOption> resolutions = new HashMap<>();

      resolutions.put("b", MergeFailOption.KEEP_REMOTE);
      System.out.println("resolve merge failures result: " + ssg.resolveMergeFailures(resolutions));
      System.out.println("UpdateCommitAndPush result: " + ssg.updateCommitAndPush("mergetest2",
            username,
            password,
            MergeFailOption.FAIL,
            (String[]) null));
   }
}

