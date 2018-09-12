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



package sh.isaac.api.sync;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.Map;
import java.util.Set;

import javax.naming.AuthenticationException;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

//~--- interfaces -------------------------------------------------------------

/**
 * {@link SyncFiles}
 *
 * An interface to specify the various methods available by remote profile sync services.
 *
 * Implementations of this interface include GIT and SVN backed remote sync services.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
 @Contract
public interface SyncFiles {
   /** The Constant DEFAULT_README_CONTENT. */
   public static final String DEFAULT_README_CONTENT =
      "ISAAC Profiles Storage \r" + "=== \r" + "This is a repository for storing ISAAC profiles and changesets.\r" +
      "It is highly recommended that you do not make changes to this repository manually - ISAAC interfaces with this.";

   //~--- methods -------------------------------------------------------------

   /**
    * Mark the specified files as files that should be synchronized.  This is a local operation only - does not push to the server.
    *
    * @param files - The relative path of each file that should be added (relative to the localFolder)
    * @throws IllegalArgumentException - if the passed parameters are invalid
    * @throws IOException - Thrown if an error occurs accessing local or remote resources
    */
   public void addFiles(String... files)
            throws IllegalArgumentException, IOException;

   /**
    * Equivalent of calling {@link #addFiles(File, Set)} for each file in the localFolder which is currently unmanaged.
    *
    * @throws IllegalArgumentException - if the passed parameters are invalid
    * @throws IOException - Thrown if an error occurs accessing local or remote resources
    */
   public void addUntrackedFiles()
            throws IllegalArgumentException, IOException;

   /**
    * Connect to remote service - pull down any existing files in the remote service.
    * <br><br>
    * In general, this operation should not result in any add / commit operations (though implementations may add/commit a 'README' file
    * if necessary to complete the linkage in the case of empty repositories - in fact, are encouraged to ensure that a reasonable README
    * file exists on both the client and the server at the end of this operation)
    * <br><br>
    * This is NOT intended to be called from a running application - rather - it should only be called during the initial configuration
    * (typically done during the build sequence) as it doesn't provide any feedback as to what local files changed, during its operations.
    * <pre>
    * Implementers should handle the following cases:
    *
    *  * Remote repository exists, but is empty
    *    o Local folder exists - is not a repository - may or may not contain content.
    *      ==> The expected behavior for this case is that the the local folder will be turned into a repository managed folder,
    *          and the remote server should be linked to it.  Implementers may commit and push a single file, such as a 'README' file
    *          if necessary - as implementations like GIT and SVN don't allow syncing when there is nothing to sync.  Any other pre-existing
    *          local content should NOT be added, committed or synced as part of this operation.
    *    o Local folder exists, and is already a repository - may or may not contain content.
    *      ==> The expected behavior for this case is that the local repository should be linked to the remote repository.  In the case of GIT,
    *          it is acceptable to push the local repository directly to the remote server.  This will push any content that was previously committed to the
    *          repository - but any existing content in the local folder that has not been added or committed should not be pushed to the remote server.
    *          For other source control systems, this is likely an impossible state - and they may simply discard the local repository information - and link
    *          to the new location.  Any local content should be preserved, but not committed or added to the remote repository.
    *
    *  * Remote repository exists, and is populated
    *    o Local folder exists - is not a repository - may or may not contain content
    *      ==> The expected behavior for this case is that the server content should be checked out locally - and added to the local folder.  If local files
    *          already exist that would be overwritten by the checkout (because they also exist on the server) the local files should be PRESERVED - not overwritten.
    *          The end result of the operation would show these files in a MODIFIED state.
    *    o Local folder exists, and is already a repository - may or may not contain content.
    *      ==> The expected behavior for this case is that the server and local repositories should be linked, and the content from the server should be checked
    *          out and added to the local folder.  The assumption is made that the client and server were a pair at one point - this should be the equivalent of
    *          updating the client to point to a new URL for the server if the server was relocated, for example.
    *          If the client repository is found to be incompatible with the server repository - then the client state should be discarded - and a new checkout
    *          should be done from the server.  Any local files which have naming collisions with server files should be PRESERVED during the checkout - not
    *          overwritten - leaving them in a MODIFIED state, if they happen to differ from the files that were on the server.
    * </pre>
    *
    * @param remoteAddress - the URL to the remote server
    * @param username - The username to use for remote operations
    * @param password - The password to use for remote operations
    * @throws IllegalArgumentException - if the passed parameters are invalid
    * @throws IOException - Thrown if an error occurs accessing local or remote resources
    * @throws AuthenticationException the authentication exception
    */
   public void linkAndFetchFromRemote(String remoteAddress,
                                      String username,
                                      char[] password)
            throws IllegalArgumentException,
                   IOException,
                   AuthenticationException;

   /**
    * Fix the URL to the remote service.  This call should only be used when both the local and remote repositories exist, and are a proper pair -
    * but the URL for the remote service needs to be corrected, for whatever reason (for example, the domain name changed)
    *
    * Has no impact on any local files.
    *
    * @param remoteAddress - the URL to the remote server
    * @param username - remote credentials
    * @param password - remote credentials
    * @throws IllegalArgumentException - if the passed parameters are invalid
    * @throws IOException - Thrown if an error occurs accessing local or remote resources
    * @throws AuthenticationException - if auth fails during remote relink
    */
   public void relinkRemote(String remoteAddress,
                            String username,
                            char[] password)
            throws IllegalArgumentException,
                   IOException,
                   AuthenticationException;

   /**
    * Mark the specified files as files that should be removed from the server.  This is a local operation only - does not push to the server.
    * If the file exists locally, it will be removed by this operation.
    *
    * @param files - The relative path of each file that should be removed (deleted) (relative to the localFolder)
    * @throws IllegalArgumentException - if the passed parameters are invalid
    * @throws IOException - Thrown if an error occurs accessing local or remote resources
    */
   public void removeFiles(String... files)
            throws IllegalArgumentException, IOException;

   /**
    * If {@link #updateCommitAndPush(File, String, String, String, MergeFailOption, String...) or {@link #updateFromRemote(File, String, String, MergeFailOption)}
    * resulted in a MergeFailure exception, this method should be called to specify how to resolve each merge failure.
    *
    * After calling this, you may call {@link #updateCommitAndPush(File, String, String, String, MergeFailOption, String...)} again.
    *
    * Note - some implementations (specifically GIT) may throw another {@link MergeFailure} during this operation - this is a secondary merge failure
    * which will also have to be resolved by the user (by calling this method again) before you can commit and push.
    *
    * @param resolutions - A map where each key is a relative file name of a file that had a mergeFailure, and the corresponding value is
    * the action that should be taken to resolve the issue.  Note that {@link MergeFailOption#FAIL} it not a valid option.
    * @return At a minimum, the set of files modified by the resolution of the merge.  Implementation may, at their choosing , return the complete set of files
    * that changed during the pull from the server that led to the merge failure, in addition to the files that were changes to resolve the conflicts.
    * @throws IllegalArgumentException - Thrown if an error occurs accessing local or remote resources
    * @throws IOException - if the passed parameters are invalid
    * @throws MergeFailure - If the update cannot be applied cleanly.  The exception will contain the list of files that were changed (cleanly, or not) during the
    * update attempt.
    */
   public Set<String> resolveMergeFailures(Map<String, MergeFailOption> resolutions)
            throws IllegalArgumentException,
                   IOException,
                   MergeFailure;

   /**
    * An optional method that allows implementations to do an implementation specific substitution on the URL that will be used to connect.
    *
    * For example, in the GIT implementation:
    *  ssh://someuser@csfe.aceworkspace.net:29418/... needs to become:
    *  ssh://<getUsername()>@csfe.aceworkspace.net:29418/...
    *
    * The default implementation simply returns url directly back.
    *
    * @param url the url
    * @param username the username
    * @return The substituted URL for this implementation.  Return url directly, if no substitution is required.
    */
   public default String substituteURL(String url, String username) {
      return url;
   }

   /**
    * Update (all), commit and push the specified files to the remote server.  This can include files that have been modified, added or removed.
    * Assuming that the status call reflects that state.  The implementation will also perform an update from remote as part of this operation.
    *
    * @param commitMessage - the message to attach to this commit
    * @param username - the username to use to push the commit remotely
    * @param password - The password to use to push the commit remotely
    * @param mergeFailOption - (optional - defaults to {@link MergeFailOption#FAIL}) - the action to take if the required update results in a merge conflict.
    * @param files - The list of files to commit.  May be empty, to support cases where a commit was completed, but the push failed due to a merge issue that
    * required resolution.  May be null to request that all tracked files with changes be committed and pushed.
    * @return The set of files that changed during the pull from the server.
    * @throws IllegalArgumentException - Thrown if an error occurs accessing local or remote resources
    * @throws IOException - if the passed parameters are invalid
    * @throws MergeFailure - If the update cannot be applied cleanly.  The exception will contain the list of files that were changed (cleanly, or not) during the
    * update attempt.
    * @throws AuthenticationException the authentication exception
    */
   public Set<String> updateCommitAndPush(String commitMessage,
         String username,
         char[] password,
         MergeFailOption mergeFailOption,
         String... files)
            throws IllegalArgumentException,
                   IOException,
                   MergeFailure,
                   AuthenticationException;

   /**
    * Get the latest files from the server.
    *
    * @param username - the username to use to pull the updates
    * @param password - The password to use to pull the updates
    * @param mergeFailOption - (optional - defaults to {@link MergeFailOption#FAIL}) - the action to take if the required update results in a merge conflit.
    * @return The set of files that changed during the pull from the server.
    * @throws IllegalArgumentException - Thrown if an error occurs accessing local or remote resources
    * @throws IOException - if the passed parameters are invalid
    * @throws MergeFailure - If the update cannot be applied cleanly.  The exception will contain the list of files that were changed (cleanly, or not) during the
    * update attempt.
    * @throws AuthenticationException the authentication exception
    */
   public Set<String> updateFromRemote(String username,
         char[] password,
         MergeFailOption mergeFailOption)
            throws IllegalArgumentException,
                   IOException,
                   MergeFailure,
                   AuthenticationException;

   //~--- get methods ---------------------------------------------------------

   /**
    * Get the list of files currently in a merge-conflicted state.  This should normally return an empty set.  Would typically only be called
    * if a merge sequence was abnormally aborted.  This would allow you to get the files that still need to be merged, then call resolveMergeFailures...
    *
    * @return the files in merge conflict
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public Set<String> getFilesInMergeConflict()
            throws IOException;

   /**
    * Check the local SCM status, and get a count of files that have changes that will be pushed.
    *
    * @return the locally modified file count
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public int getLocallyModifiedFileCount()
            throws IOException;

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the readme file content.
    *
    * @param readmeFileContent - The content to place in the README file that the implementation will create (if it doesn't exist).
    * If this method is never called, the implementation will default to the text from {@link SyncFiles#DEFAULT_README_CONTENT}.
    * If left blank - the README file may still be created by the implementation, as some SCM systems don't allow a commit with an empty
    * repository.
    */
   public abstract void setReadmeFileContent(String readmeFileContent);

   //~--- get methods ---------------------------------------------------------

   /**
    * Return the currently configured root location.
    * @return full path the the folder that should be synchronizable (or null, if not configured)
    */
   public abstract File getRootLocation();

   //~--- set methods ---------------------------------------------------------

   /**
    * Set the base folder that should be used in this instance of Profile Sync.  Necessary for HK2 style
    * init where it can't be passed in via the constructor.
    *
    * @param localFolder - full path the the folder that should be synchronizable
    * @throws IllegalArgumentException - if the passed in file is not a folder, or does not exist.
    */
   public abstract void setRootLocation(File localFolder)
            throws IllegalArgumentException;

   //~--- get methods ---------------------------------------------------------

   /**
    * Returns true if the specified location appears to be a SCM store, false otherwise.
    *
    * @return true, if root location configured for SCM
    */
   public boolean isRootLocationConfiguredForSCM();
}

