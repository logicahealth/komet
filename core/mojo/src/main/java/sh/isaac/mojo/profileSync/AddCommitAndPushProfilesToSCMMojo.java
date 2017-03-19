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



package sh.isaac.mojo.profileSync;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.MojoExecutionException;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.sync.MergeFailOption;
import sh.isaac.api.sync.SyncFiles;

//~--- classes ----------------------------------------------------------------

/**
 * Goal which pushes the local changes to the server for the profiles SCM.
 *
 * This executes {@link SyncFiles#addUntrackedFiles(File)} followed by
 *
 * {@link SyncFiles#updateCommitAndPush(File, String, String, String, sh.isaac.api.sync.MergeFailOption, String...)}
 *
 *
 * See the above references for specific details on the behavior of this commit process
 * Keep this in a phase later than GenerateUsersMojo
 */
@Service(name = "add-commit-and-push-profiles-scm")
public class AddCommitAndPushProfilesToSCMMojo
        extends ProfilesMojoBase {
   /**
    * @throws MojoExecutionException
    */
   public AddCommitAndPushProfilesToSCMMojo()
            throws MojoExecutionException {
      super();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void execute()
            throws MojoExecutionException {
      super.execute();

      if (skipRun()) {
         return;
      }

      try {
         getLog().info("Committing " + userProfileFolderLocation.getAbsolutePath() + " for SCM management");
         getProfileSyncImpl().addUntrackedFiles();
         getProfileSyncImpl().updateCommitAndPush("Adding profiles after executing GenerateUsersMojo",
               getUsername(),
               getPassword(),
               MergeFailOption.KEEP_REMOTE,
               (String[]) null);
         getLog().info("Done Committing SCM for profiles");
      } catch (Exception e) {
         throw new MojoExecutionException("Unexpected error committing SCM for the profiles", e);
      }
   }
}

