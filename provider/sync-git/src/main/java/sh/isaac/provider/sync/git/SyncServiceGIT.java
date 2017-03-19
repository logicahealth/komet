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
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import javax.naming.AuthenticationException;

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand.Stage;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.StashApplyFailureException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.notes.Note;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.StringUtils;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import org.glassfish.hk2.api.PerLookup;

import org.jvnet.hk2.annotations.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import sh.isaac.api.sync.MergeFailOption;
import sh.isaac.api.sync.MergeFailure;
import sh.isaac.api.sync.SyncFiles;

//~--- classes ----------------------------------------------------------------

/**
 * {@link SyncServiceGIT}
 *
 * A GIT implementation of {@link SyncFiles}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service(name = "GIT")
@PerLookup
public class SyncServiceGIT
         implements SyncFiles {
   private static Logger                  log            = LoggerFactory.getLogger(SyncServiceGIT.class);
   private static volatile CountDownLatch jschConfigured = new CountDownLatch(1);

   //~--- fields --------------------------------------------------------------

   private final String NOTE_FAILED_MERGE_HAPPENED_ON_REMOTE = "Conflicted merge happened during remote merge";
   private final String NOTE_FAILED_MERGE_HAPPENED_ON_STASH  = "Conflicted merge happened during stash merge";
   private final String STASH_MARKER                         = ":STASH-";
   private File         localFolder                          = null;
   private String       readMeFileContent_                   = DEFAULT_README_CONTENT;
   private String       gitIgnoreText_                       = "lastUser.txt\r\n";

   //~--- constructors --------------------------------------------------------

   /**
    * If you are in an HK2 environment, you would be better served getting this from HK2 (by asking for it by interface and name)
    * but in other enviornments, when HK2 may not be up, you may construct it directly.
    */
   public SyncServiceGIT() {
      synchronized (jschConfigured) {
         if (jschConfigured.getCount() > 0) {
            log.debug("Disabling strict host key checking");

            final SshSessionFactory factory = new JschConfigSessionFactory() {
               @Override
               protected void configure(Host hc, Session session) {
                  session.setConfig("StrictHostKeyChecking", "no");
               }
            };

            SshSessionFactory.setInstance(factory);
            JSch.setLogger(new com.jcraft.jsch.Logger() {
                              private final HashMap<Integer, Consumer<String>> logMap     = new HashMap<>();
                              private final HashMap<Integer, BooleanSupplier>  enabledMap = new HashMap<>();
                              {

                                 // Note- JSCH is _really_  verbose at the INFO level, so I'm mapping info to DEBUG.
                                 this.logMap.put(com.jcraft.jsch.Logger.DEBUG, log::debug);
                                 this.logMap.put(com.jcraft.jsch.Logger.ERROR, log::debug);  // error
                                 this.logMap.put(com.jcraft.jsch.Logger.FATAL, log::debug);  // error
                                 this.logMap.put(com.jcraft.jsch.Logger.INFO, log::debug);
                                 this.logMap.put(com.jcraft.jsch.Logger.WARN, log::debug);   // warn
                                 this.enabledMap.put(com.jcraft.jsch.Logger.DEBUG, log::isDebugEnabled);
                                 this.enabledMap.put(com.jcraft.jsch.Logger.ERROR, log::isErrorEnabled);
                                 this.enabledMap.put(com.jcraft.jsch.Logger.FATAL, log::isErrorEnabled);
                                 this.enabledMap.put(com.jcraft.jsch.Logger.INFO, log::isDebugEnabled);
                                 this.enabledMap.put(com.jcraft.jsch.Logger.WARN, log::isWarnEnabled);
                              }
                              @Override
                              public void log(int level, String message) {
                                 this.logMap.get(level)
                                       .accept(message);
                              }
                              @Override
                              public boolean isEnabled(int level) {
                                 return this.enabledMap.get(level)
                                                  .getAsBoolean();
                              }
                           });
            jschConfigured.countDown();
         }
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * @see sh.isaac.api.sync.SyncFiles#addFiles(java.io.File, java.util.Set)
    */
   @Override
   public void addFiles(String... files)
            throws IllegalArgumentException, IOException {
      log.info("Add Files called {}", Arrays.toString(files));

      try (Git git = getGit()) {
         if (files.length == 0) {
            log.debug("No files to add");
         } else {
            final AddCommand ac = git.add();

            for (final String file: files) {
               ac.addFilepattern(file);
            }

            ac.call();
         }

         log.info("addFiles Complete.  Current status: " + statusToString(git.status().call()));
      } catch (final GitAPIException e) {
         log.error("Unexpected", e);
         throw new IOException("Internal error", e);
      }
   }

   /**
    * @see sh.isaac.api.sync.SyncFiles#addUntrackedFiles(java.io.File)
    */
   @Override
   public void addUntrackedFiles()
            throws IllegalArgumentException, IOException {
      log.info("Add Untracked files called");

      try (Git git = getGit()) {
         final Status s = git.status()
                       .call();

         addFiles(s.getUntracked()
                   .toArray(new String[s.getUntracked().size()]));
      } catch (final GitAPIException e) {
         log.error("Unexpected", e);
         throw new IOException("Internal error", e);
      }
   }

   /**
    * Create a new branch, and switch to it locally.  The new branch will contain no files.
    * @param branchName
    * @throws IOException
    */
   public void branch(String branchName)
            throws IOException {
      try (Git git = getGit()) {
         git.checkout()
            .setCreateBranch(true)
            .setName(branchName)
            .setOrphan(true)
            .call();
      } catch (final GitAPIException e) {
         log.error("Unexpected", e);
         throw new IOException("Internal error", e);
      }
   }

   /**
    * Create a new tag at the current point
    * @param tagName
    * @throws IOException
    * @throws IllegalArgumentException
    */
   public void commitAndTag(String commitMessage, String tagName)
            throws IllegalArgumentException, IOException {
      try (Git git = getGit()) {
         git.commit()
            .setAll(true)
            .setMessage(commitMessage)
            .call();
         git.tag()
            .setName(tagName)
            .call();
      } catch (final GitAPIException e) {
         log.error("Unexpected", e);
         throw new IOException("Internal error", e);
      }
   }

   /**
    * @throws AuthenticationException
    * @see sh.isaac.api.sync.SyncFiles#linkAndFetchFromRemote(java.io.File, java.lang.String, java.lang.String, java.lang.String)
    */
   @Override
   public void linkAndFetchFromRemote(String remoteAddress,
                                      String username,
                                      char[] password)
            throws IllegalArgumentException,
                   IOException,
                   AuthenticationException {
      log.info("linkAndFetchFromRemote called - folder: {}, remoteAddress: {}, username: {}",
               this.localFolder,
               remoteAddress,
               username);

      Repository r   = null;
      Git        git = null;

      try {
         final File gitFolder = new File(this.localFolder, ".git");

         r = new FileRepository(gitFolder);

         if (!gitFolder.isDirectory()) {
            log.debug("Root folder does not contain a .git subfolder.  Creating new git repository.");
            r.create();
         }

         relinkRemote(remoteAddress, username, password);
         git = new Git(r);

         final CredentialsProvider cp = new UsernamePasswordCredentialsProvider(username, ((password == null) ? new char[] {}
               : password));

         log.debug("Fetching");

         final FetchResult fr = git.fetch()
                             .setCheckFetchedObjects(true)
                             .setCredentialsProvider(cp)
                             .call();

         log.debug("Fetch messages: {}", fr.getMessages());

         boolean         remoteHasMaster = false;
         final Collection<Ref> refs            = git.lsRemote()
                                              .setCredentialsProvider(cp)
                                              .call();

         for (final Ref ref: refs) {
            if ("refs/heads/master".equals(ref.getName())) {
               remoteHasMaster = true;
               log.debug("Remote already has 'heads/master'");
               break;
            }
         }

         if (remoteHasMaster) {
            // we need to fetch and (maybe) merge - get onto origin/master.
            log.debug("Fetching from remote");

            final String fetchResult = git.fetch()
                                    .setCredentialsProvider(cp)
                                    .call()
                                    .getMessages();

            log.debug("Fetch Result: {}", fetchResult);
            log.debug("Resetting to origin/master");
            git.reset()
               .setMode(ResetType.MIXED)
               .setRef("origin/master")
               .call();

            // Get the files from master that we didn't have in our working folder
            log.debug("Checking out missing files from origin/master");

            for (final String missing: git.status()
                                    .call()
                                    .getMissing()) {
               log.debug("Checkout {}", missing);
               git.checkout()
                  .addPath(missing)
                  .call();
            }

            for (final String newFile: makeInitialFilesAsNecessary(this.localFolder)) {
               log.debug("Adding and committing {}", newFile);
               git.add()
                  .addFilepattern(newFile)
                  .call();
               git.commit()
                  .setMessage("Adding " + newFile)
                  .setAuthor(username, "42")
                  .call();

               for (final PushResult pr: git.push()
                                      .setCredentialsProvider(cp)
                                      .call()) {
                  log.debug("Push Message: {}", pr.getMessages());
               }
            }
         } else {
            // just push
            // make sure we have something to push
            for (final String newFile: makeInitialFilesAsNecessary(this.localFolder)) {
               log.debug("Adding and committing {}", newFile);
               git.add()
                  .addFilepattern(newFile)
                  .call();
            }

            git.commit()
               .setMessage("Adding initial files")
               .setAuthor(username, "42")
               .call();
            log.debug("Pushing repository");

            for (final PushResult pr: git.push()
                                   .setCredentialsProvider(cp)
                                   .call()) {
               log.debug("Push Result: {}", pr.getMessages());
            }
         }

         log.info("linkAndFetchFromRemote Complete.  Current status: " + statusToString(git.status().call()));
      } catch (final TransportException te) {
         if (te.getMessage().contains("Auth fail") || te.getMessage().contains("not authorized")) {
            log.info("Auth fail", te);
            throw new AuthenticationException("Auth fail");
         } else {
            log.error("Unexpected", te);
            throw new IOException("Internal error", te);
         }
      } catch (final GitAPIException e) {
         log.error("Unexpected", e);
         throw new IOException("Internal error", e);
      } finally {
         if (git != null) {
            git.close();
         }

         if (r != null) {
            r.close();
         }
      }
   }

   public void pushTag(final String tagName,
                       String username,
                       char[] password)
            throws IllegalArgumentException,
                   IOException,
                   AuthenticationException {
      try (Git git = getGit()) {
         final CredentialsProvider cp = new UsernamePasswordCredentialsProvider(username, ((password == null) ? new char[] {}
               : password));
         final Iterable<PushResult> pr = git.push()
                                      .setRefSpecs(new RefSpec("refs/tags/" + tagName))
                                      .setCredentialsProvider(cp)
                                      .call();
         final StringBuilder failures = new StringBuilder();

         pr.forEach(t -> {
		      log.debug("Push Result Messages: " + t.getMessages());

		      if (t.getRemoteUpdate("refs/tags/" + tagName)
		           .getStatus() != org.eclipse.jgit.transport.RemoteRefUpdate.Status.OK) {
		         failures.append("Push Failed: " +
		                         t.getRemoteUpdate("refs/tags/" + tagName).getStatus().name() +
		                         " reason: " + t.getRemoteUpdate("refs/tags/" + tagName).getMessage());
		      }
		   });

         if (failures.length() > 0) {
            throw new IOException(failures.toString());
         }
      } catch (final GitAPIException e) {
         if (e.getMessage().contains("Auth fail") || e.getMessage().contains("not authorized")) {
            log.info("Auth fail", e);
            throw new AuthenticationException("Auth fail");
         } else {
            log.error("Unexpected", e);
            throw new IOException("Internal error", e);
         }
      }
   }

   public ArrayList<String> readTags(String username,
                                     char[] password)
            throws IllegalArgumentException,
                   IOException,
                   AuthenticationException {
      try (Git git = getGit()) {
         final ArrayList<String>   results = new ArrayList<>();
         final CredentialsProvider cp = new UsernamePasswordCredentialsProvider(username, ((password == null) ? new char[] {}
               : password));

         git.fetch()
            .setTagOpt(TagOpt.FETCH_TAGS)
            .setCredentialsProvider(cp)
            .call();

         for (final Ref x: git.tagList()
                        .call()) {
            results.add(x.getName());
         }

         git.close();
         return results;
      } catch (final GitAPIException e) {
         if (e.getMessage().contains("Auth fail") || e.getMessage().contains("not authorized")) {
            log.info("Auth fail", e);
            throw new AuthenticationException("Auth fail");
         } else {
            log.error("Unexpected", e);
            throw new IOException("Internal error", e);
         }
      }
   }

   /**
    * @see sh.isaac.api.sync.SyncFiles#relinkRemote(java.lang.String, java.lang.String, java.lang.String)
    */
   @Override
   public void relinkRemote(String remoteAddress,
                            String username,
                            char[] password)
            throws IllegalArgumentException,
                   IOException {
      try (Git git = getGit()) {
         log.debug("Configuring remote URL and fetch defaults to {}", remoteAddress);

         final StoredConfig sc = git.getRepository()
                              .getConfig();

         sc.setString("remote", "origin", "url", remoteAddress);
         sc.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
         sc.save();
      }
   }

   /**
    * @see sh.isaac.api.sync.SyncFiles#removeFiles(java.io.File, java.util.Set)
    */
   @Override
   public void removeFiles(String... files)
            throws IllegalArgumentException, IOException {
      log.info("Remove Files called {}", Arrays.toString(files));

      try (Git git = getGit()) {
         if (files.length == 0) {
            log.debug("No files to remove");
         } else {
            final RmCommand rm = git.rm();

            for (final String file: files) {
               rm.addFilepattern(file);
            }

            rm.call();
         }

         log.info("removeFiles Complete.  Current status: " + statusToString(git.status().call()));
      } catch (final GitAPIException e) {
         log.error("Unexpected", e);
         throw new IOException("Internal error", e);
      }
   }

   /**
    * @throws MergeFailure
    * @throws NoWorkTreeException
    * @see sh.isaac.api.sync.SyncFiles#resolveMergeFailures(java.io.File, java.util.Map)
    */
   @Override
   public Set<String> resolveMergeFailures(Map<String, MergeFailOption> resolutions)
            throws IllegalArgumentException,
                   IOException,
                   NoWorkTreeException,
                   MergeFailure {
      log.info("resolve merge failures called - resolutions: {}", resolutions);

      try (Git git = getGit()) {
         final List<Note>  notes       = git.notesList()
                                      .call();
         final Set<String> conflicting = git.status()
                                      .call()
                                      .getConflicting();

         if (conflicting.size() == 0) {
            throw new IllegalArgumentException("You do not appear to have any conflicting files");
         }

         if (conflicting.size() != resolutions.size()) {
            throw new IllegalArgumentException(
                "You must provide a resolution for each conflicting file.  Files in conflict: " + conflicting);
         }

         for (final String s: conflicting) {
            if (!resolutions.containsKey(s)) {
               throw new IllegalArgumentException("No conflit resolution specified for file " + s +
                                                  ".  Resolutions must be specified for all files");
            }
         }

         if ((notes == null) || (notes.size() == 0)) {
            throw new IllegalArgumentException(
                "The 'note' that is required for tracking state is missing.  This merge failure must be resolved on the command line");
         }

         final String        noteValue = new String(git.getRepository().open(notes.get(0).getData()).getBytes());
         MergeFailType mergeFailType;

         if (noteValue.startsWith(this.NOTE_FAILED_MERGE_HAPPENED_ON_REMOTE)) {
            mergeFailType = MergeFailType.REMOTE_TO_LOCAL;
         } else if (noteValue.startsWith(this.NOTE_FAILED_MERGE_HAPPENED_ON_STASH)) {
            mergeFailType = MergeFailType.STASH_TO_LOCAL;
         } else {
            throw new IllegalArgumentException(
                "The 'note' that is required for tracking state contains an unexpected value of '" + noteValue + "'");
         }

         String stashIdToApply = null;

         if (noteValue.contains(this.STASH_MARKER)) {
            stashIdToApply = noteValue.substring(noteValue.indexOf(this.STASH_MARKER) + this.STASH_MARKER.length());
         }

         return resolveMergeFailures(mergeFailType, stashIdToApply, resolutions);
      } catch (GitAPIException | LargeObjectException e) {
         log.error("Unexpected", e);
         throw new IOException("Internal error", e);
      }
   }

   /**
    * @see sh.isaac.api.sync.SyncFiles#substituteURL(java.lang.String, java.lang.String)
    *
    * Turns
    *  ssh://someuser@csfe.aceworkspace.net:29418/... into
    *  ssh://username.toString()@csfe.aceworkspace.net:29418/...
    *
    *  Otherwise, returns URL.
    */
   @Override
   public String substituteURL(String url, String username) {
      if (url.startsWith("ssh://") && url.contains("@")) {
         final int index = url.indexOf("@");

         url = "ssh://" + username + url.substring(index);
      }

      return url;
   }

   /**
    * @throws MergeFailure
    * @throws AuthenticationException
    * @see sh.isaac.api.sync.SyncFiles#updateCommitAndPush(java.io.File, java.lang.String, java.lang.String, java.lang.String,
    * java.lang.String[])
    */
   @Override
   public Set<String> updateCommitAndPush(String commitMessage,
         String username,
         char[] password,
         MergeFailOption mergeFailOption,
         String... files)
            throws IllegalArgumentException,
                   IOException,
                   MergeFailure,
                   AuthenticationException {
      log.info("Commit Files called {}", ((files == null) ? "-null-"
            : Arrays.toString(files)));

      try (Git git = getGit()) {
         if (git.status()
                .call()
                .getConflicting()
                .size() > 0) {
            log.info("Previous merge failure not yet resolved");
            throw new MergeFailure(git.status().call().getConflicting(), new HashSet<>());
         }

         if (files == null) {
            files = git.status()
                       .call()
                       .getUncommittedChanges()
                       .toArray(new String[0]);
            log.info("Will commit the uncommitted files {}", Arrays.toString(files));
         }

         if (StringUtils.isEmptyOrNull(commitMessage) && (files.length > 0)) {
            throw new IllegalArgumentException("The commit message is required when files are specified");
         }

         if (files.length > 0) {
            final CommitCommand commit = git.commit();

            for (final String file: files) {
               commit.setOnly(file);
            }

            commit.setAuthor(username, "42");
            commit.setMessage(commitMessage);

            final RevCommit rv = commit.call();

            log.debug("Local commit completed: " + rv.getFullMessage());
         }

         // need to merge origin/master into master now, prior to push
         final Set<String> result = updateFromRemote(username, password, mergeFailOption);

         log.debug("Pushing");

         final CredentialsProvider  cp = new UsernamePasswordCredentialsProvider(username, ((password == null) ? new char[] {}
               : password));
         final Iterable<PushResult> pr = git.push()
                                      .setCredentialsProvider(cp)
                                      .call();

         pr.forEach(t -> log.debug("Push Result Messages: " + t.getMessages()));
         log.info("commit and push complete.  Current status: " + statusToString(git.status().call()));
         return result;
      } catch (final TransportException te) {
         if (te.getMessage().contains("Auth fail") || te.getMessage().contains("not authorized")) {
            log.info("Auth fail", te);
            throw new AuthenticationException("Auth fail");
         } else {
            log.error("Unexpected", te);
            throw new IOException("Internal error", te);
         }
      } catch (final GitAPIException e) {
         log.error("Unexpected", e);
         throw new IOException("Internal error", e);
      }
   }

   /**
    * @throws MergeFailure
    * @throws AuthenticationException
    * @see sh.isaac.api.sync.SyncFiles#updateFromRemote(java.io.File, java.lang.String, java.lang.String,
    * sh.isaac.api.sync.MergeFailOption)
    */
   @Override
   public Set<String> updateFromRemote(String username,
         char[] password,
         MergeFailOption mergeFailOption)
            throws IllegalArgumentException,
                   IOException,
                   MergeFailure,
                   AuthenticationException {
      log.info("update from remote called ");

      Set<String> filesChangedDuringPull;

      try (Git git = getGit()) {
         log.debug("Fetching from remote");

         if (git.status()
                .call()
                .getConflicting()
                .size() > 0) {
            log.info("Previous merge failure not yet resolved");
            throw new MergeFailure(git.status().call().getConflicting(), new HashSet<>());
         }

         final CredentialsProvider cp = new UsernamePasswordCredentialsProvider(username, ((password == null) ? new char[] {}
               : password));

         log.debug("Fetch Message" + git.fetch().setCredentialsProvider(cp).call().getMessages());

         final ObjectId masterIdBeforeMerge = git.getRepository()
                                           .findRef("master")
                                           .getObjectId();

         if (git.getRepository()
                .exactRef("refs/remotes/origin/master")
                .getObjectId()
                .getName()
                .equals(masterIdBeforeMerge.getName())) {
            log.info("No changes to merge");
            return new HashSet<String>();
         }

         RevCommit stash = null;

         if (git.status()
                .call()
                .getUncommittedChanges()
                .size() > 0) {
            log.info("Stashing uncommitted changes");
            stash = git.stashCreate()
                       .call();
         }

         {
            log.debug("Merging from remotes/origin/master");

            final MergeResult mr = git.merge()
                                .include(git.getRepository()
                                            .exactRef("refs/remotes/origin/master"))
                                .call();
            final AnyObjectId headAfterMergeID = mr.getNewHead();

            if (!mr.getMergeStatus()
                   .isSuccessful()) {
               if ((mergeFailOption == null) || (MergeFailOption.FAIL == mergeFailOption)) {
                  addNote(this.NOTE_FAILED_MERGE_HAPPENED_ON_REMOTE + ((stash == null) ? ":NO_STASH"
                        : this.STASH_MARKER + stash.getName()), git);

                  // We can use the status here - because we already stashed the stuff that they had uncommitted above.
                  throw new MergeFailure(mr.getConflicts().keySet(), git.status().call().getUncommittedChanges());
               } else if ((MergeFailOption.KEEP_LOCAL == mergeFailOption) ||
                          (MergeFailOption.KEEP_REMOTE == mergeFailOption)) {
                  final HashMap<String, MergeFailOption> resolutions = new HashMap<>();

                  for (final String s: mr.getConflicts()
                                   .keySet()) {
                     resolutions.put(s, mergeFailOption);
                  }

                  log.debug("Resolving merge failures with option {}", mergeFailOption);
                  filesChangedDuringPull = resolveMergeFailures(MergeFailType.REMOTE_TO_LOCAL, ((stash == null) ? null
                        : stash.getName()), resolutions);
               } else {
                  throw new IllegalArgumentException("Unexpected option");
               }
            } else {
               // Conflict free merge - or perhaps, no merge at all.
               if (masterIdBeforeMerge.getName()
                                      .equals(headAfterMergeID.getName())) {
                  log.debug("Merge didn't result in a commit - no incoming changes");
                  filesChangedDuringPull = new HashSet<>();
               } else {
                  filesChangedDuringPull = listFilesChangedInCommit(git.getRepository(),
                        masterIdBeforeMerge,
                        headAfterMergeID);
               }
            }
         }

         if (stash != null) {
            log.info("Replaying stash");

            try {
               git.stashApply()
                  .setStashRef(stash.getName())
                  .call();
               log.debug("stash applied cleanly, dropping stash");
               git.stashDrop()
                  .call();
            } catch (final StashApplyFailureException e) {
               log.debug("Stash failed to merge");

               if ((mergeFailOption == null) || (MergeFailOption.FAIL == mergeFailOption)) {
                  addNote(this.NOTE_FAILED_MERGE_HAPPENED_ON_STASH, git);
                  throw new MergeFailure(git.status().call().getConflicting(), filesChangedDuringPull);
               } else if ((MergeFailOption.KEEP_LOCAL == mergeFailOption) ||
                          (MergeFailOption.KEEP_REMOTE == mergeFailOption)) {
                  final HashMap<String, MergeFailOption> resolutions = new HashMap<>();

                  for (final String s: git.status()
                                    .call()
                                    .getConflicting()) {
                     resolutions.put(s, mergeFailOption);
                  }

                  log.debug("Resolving stash apply merge failures with option {}", mergeFailOption);
                  resolveMergeFailures(MergeFailType.STASH_TO_LOCAL, null, resolutions);

                  // When we auto resolve to KEEP_LOCAL - these files won't have really changed, even though we recorded a change above.
                  for (final Entry<String, MergeFailOption> r: resolutions.entrySet()) {
                     if (MergeFailOption.KEEP_LOCAL == r.getValue()) {
                        filesChangedDuringPull.remove(r.getKey());
                     }
                  }
               } else {
                  throw new IllegalArgumentException("Unexpected option");
               }
            }
         }

         log.info("Files changed during updateFromRemote: {}", filesChangedDuringPull);
         return filesChangedDuringPull;
      } catch (final CheckoutConflictException e) {
         log.error("Unexpected", e);
         throw new IOException(
             "A local file exists (but is not yet added to source control) which conflicts with a file from the server." +
             "  Either delete the local file, or call addFile(...) on the offending file prior to attempting to update from remote.",
             e);
      } catch (final TransportException te) {
         if (te.getMessage().contains("Auth fail") || te.getMessage().contains("not authorized")) {
            log.info("Auth fail", te);
            throw new AuthenticationException("Auth fail");
         } else {
            log.error("Unexpected", te);
            throw new IOException("Internal error", te);
         }
      } catch (final GitAPIException e) {
         log.error("Unexpected", e);
         throw new IOException("Internal error", e);
      }
   }

   private void addNote(String message, Git git)
            throws IOException, GitAPIException {
      final RevWalk   walk   = new RevWalk(git.getRepository());
      final Ref       head   = git.getRepository()
                            .exactRef("refs/heads/master");
      final RevCommit commit = walk.parseCommit(head.getObjectId());

      git.notesAdd()
         .setObjectId(commit)
         .setMessage(message)
         .call();
      walk.close();
   }

   private HashSet<String> listFilesChangedInCommit(Repository repository,
         AnyObjectId beforeID,
         AnyObjectId afterID)
            throws MissingObjectException,
                   IncorrectObjectTypeException,
                   IOException {
      log.info("calculating files changed in commit");

      final HashSet<String> result       = new HashSet<>();
      final RevWalk         rw           = new RevWalk(repository);
      final RevCommit       commitBefore = rw.parseCommit(beforeID);
      final RevCommit       commitAfter  = rw.parseCommit(afterID);

      rw.close();

      final DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);

      df.setRepository(repository);
      df.setDiffComparator(RawTextComparator.DEFAULT);
      df.setDetectRenames(true);

      final List<DiffEntry> diffs = df.scan(commitBefore.getTree(), commitAfter.getTree());

      for (final DiffEntry diff: diffs) {
         result.add(diff.getNewPath());
      }

      df.close();
      log.debug("Files changed between commits commit: {} and {} - {}", beforeID.getName(), afterID, result);
      return result;
   }

   /**
    * returns a list of newly created files and files that were modified.
    */
   private List<String> makeInitialFilesAsNecessary(File containingFolder)
            throws IOException {
      final ArrayList<String> result = new ArrayList<>();
      final File              readme = new File(containingFolder, "README.md");

      if (!readme.isFile()) {
         log.debug("Creating {}", readme.getAbsolutePath());
         Files.write(readme.toPath(), new String(this.readMeFileContent_).getBytes(), StandardOpenOption.CREATE_NEW);
         result.add(readme.getName());
      } else {
         log.debug("README.md already exists");
      }

      final File ignore = new File(containingFolder, ".gitignore");

      if (!ignore.isFile()) {
         log.debug("Creating {}", ignore.getAbsolutePath());
         Files.write(ignore.toPath(), new String(this.gitIgnoreText_).getBytes(), StandardOpenOption.CREATE_NEW);
         result.add(ignore.getName());
      } else {
         log.debug(".gitignore already exists");

         if (!new String(Files.readAllBytes(ignore.toPath())).contains(this.gitIgnoreText_)) {
            log.debug("Appending onto existing .gitignore file");
            Files.write(ignore.toPath(), new String("\r\n" + this.gitIgnoreText_).getBytes(), StandardOpenOption.APPEND);
            result.add(ignore.getName());
         }
      }

      return result;
   }

   private Set<String> resolveMergeFailures(MergeFailType mergeFailType,
         String stashIDToApply,
         Map<String, MergeFailOption> resolutions)
            throws IllegalArgumentException,
                   IOException,
                   MergeFailure {
      log.debug("resolve merge failures called - mergeFailType: {} stashIDToApply: {} resolutions: {}",
                mergeFailType,
                stashIDToApply,
                resolutions);

      try (Git git = getGit();) {
         // We unfortunately, must know the mergeFailType option, because the resolution mechanism here uses OURS and THEIRS - but the
         // meaning of OURS and THEIRS reverse, depending on if you are recovering from a merge failure, or a stash apply failure.
         for (final Entry<String, MergeFailOption> r: resolutions.entrySet()) {
            if (MergeFailOption.FAIL == r.getValue()) {
               throw new IllegalArgumentException("MergeFailOption.FAIL is not a valid option");
            } else if (MergeFailOption.KEEP_LOCAL == r.getValue()) {
               log.debug("Keeping our local file for conflict {}", r.getKey());
               git.checkout()
                  .addPath(r.getKey())
                  .setStage((MergeFailType.REMOTE_TO_LOCAL == mergeFailType) ? Stage.OURS
                     : Stage.THEIRS)
                  .call();
            } else if (MergeFailOption.KEEP_REMOTE == r.getValue()) {
               log.debug("Keeping remote file for conflict {}", r.getKey());
               git.checkout()
                  .addPath(r.getKey())
                  .setStage((MergeFailType.REMOTE_TO_LOCAL == mergeFailType) ? Stage.THEIRS
                     : Stage.OURS)
                  .call();
            } else {
               throw new IllegalArgumentException("MergeFailOption is required");
            }

            log.debug("calling add to mark merge resolved");
            git.add()
               .addFilepattern(r.getKey())
               .call();
         }

         if (mergeFailType == MergeFailType.STASH_TO_LOCAL) {
            // clean up the stash
            log.debug("Dropping stash");
            git.stashDrop()
               .call();
         }

         final RevWalk   walk                    = new RevWalk(git.getRepository());
         final Ref       head                    = git.getRepository()
                                                .exactRef("refs/heads/master");
         final RevCommit commitWithPotentialNote = walk.parseCommit(head.getObjectId());

         walk.close();
         log.info("resolve merge failures Complete.  Current status: " + statusToString(git.status().call()));

         final RevCommit rc = git.commit()
                           .setMessage("Merging with user specified merge failure resolution for files " +
                                       resolutions.keySet())
                           .call();

         git.notesRemove()
            .setObjectId(commitWithPotentialNote)
            .call();

         final Set<String> filesChangedInCommit = listFilesChangedInCommit(git.getRepository(),
                                                                     commitWithPotentialNote.getId(),
                                                                     rc);

         // When we auto resolve to KEEP_REMOTE - these will have changed - make sure they are in the list.
         // seems like this shouldn't really be necessary - need to look into the listFilesChangedInCommit algorithm closer.
         // this might already be fixed by the rework on 11/12/14, but no time to validate at the moment. - doesn't do any harm.
         for (final Entry<String, MergeFailOption> r: resolutions.entrySet()) {
            if (MergeFailOption.KEEP_REMOTE == r.getValue()) {
               filesChangedInCommit.add(r.getKey());
            }

            if (MergeFailOption.KEEP_LOCAL == r.getValue()) {
               filesChangedInCommit.remove(r.getKey());
            }
         }

         if (!StringUtils.isEmptyOrNull(stashIDToApply)) {
            log.info("Replaying stash identified in note");

            try {
               git.stashApply()
                  .setStashRef(stashIDToApply)
                  .call();
               log.debug("stash applied cleanly, dropping stash");
               git.stashDrop()
                  .call();
            } catch (final StashApplyFailureException e) {
               log.debug("Stash failed to merge");
               addNote(this.NOTE_FAILED_MERGE_HAPPENED_ON_STASH, git);
               throw new MergeFailure(git.status().call().getConflicting(), filesChangedInCommit);
            }
         }

         return filesChangedInCommit;
      } catch (final GitAPIException e) {
         log.error("Unexpected", e);
         throw new IOException("Internal error", e);
      }
   }

   private String statusToString(Status status) {
      final StringBuilder sb = new StringBuilder();

      sb.append(" Is clean: ")
        .append(status.isClean())
        .append(String.format("%n"));
      sb.append(" Changed: ")
        .append(status.getChanged())
        .append(String.format("%n"));
      sb.append(" Added: ")
        .append(status.getAdded())
        .append(String.format("%n"));
      sb.append(" Conflicting: ")
        .append(status.getConflicting())
        .append(String.format("%n"));
      sb.append(" Ignored, unindexed: ")
        .append(status.getIgnoredNotInIndex())
        .append(String.format("%n"));
      sb.append(" Missing: ")
        .append(status.getMissing())
        .append(String.format("%n"));
      sb.append(" Modified: ")
        .append(status.getModified())
        .append(String.format("%n"));
      sb.append(" Removed: ")
        .append(status.getRemoved())
        .append(String.format("%n"));
      sb.append(" UncomittedChanges: ")
        .append(status.getUncommittedChanges())
        .append(String.format("%n"));
      sb.append(" Untracked: ")
        .append(status.getUntracked())
        .append(String.format("%n"));
      sb.append(" UntrackedFolders: ")
        .append(status.getUntrackedFolders())
        .append(String.format("%n"));
      return sb.toString();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @throws IOException
    * @see sh.isaac.api.sync.SyncFiles#getFilesInMergeConflict()
    */
   @Override
   public Set<String> getFilesInMergeConflict()
            throws IOException {
      try (Git git = getGit()) {
         return git.status()
                   .call()
                   .getConflicting();
      } catch (final Exception e) {
         log.error("Unexpected", e);
         throw new IOException("Internal error", e);
      }
   }

   private Git getGit()
            throws IOException, IllegalArgumentException {
      if (this.localFolder == null) {
         throw new IllegalArgumentException("localFolder has not yet been set - please call setRootLocation(...)");
      }

      if (!this.localFolder.isDirectory()) {
         log.error("The passed in local folder '{}' didn't exist", this.localFolder);
         throw new IllegalArgumentException("The localFolder must be a folder, and must exist");
      }

      final File gitFolder = new File(this.localFolder, ".git");

      if (!gitFolder.isDirectory()) {
         log.error("The passed in local folder '{}' does not appear to be a git repository", this.localFolder);
         throw new IllegalArgumentException("The localFolder does not appear to be a git repository");
      }

      return Git.open(gitFolder);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set the contents of the gitIgnore file
    * @param gitIgnoreContent
    */
   public void setGitIgnoreContent(String gitIgnoreContent) {
      this.gitIgnoreText_ = gitIgnoreContent;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @throws IOException
    * @see sh.isaac.api.sync.SyncFiles#getLocallyModifiedFileCount()
    */
   @Override
   public int getLocallyModifiedFileCount()
            throws IOException {
      try (Git git = getGit()) {
         return git.status()
                   .call()
                   .getUncommittedChanges()
                   .size();
      } catch (final Exception e) {
         log.error("Unexpected", e);
         throw new IOException("Internal error", e);
      }
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @see sh.isaac.api.sync.SyncFiles#setReadmeFileContent(java.lang.String)
    */
   @Override
   public void setReadmeFileContent(String readmeFileContent) {
      this.readMeFileContent_ = readmeFileContent;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @see sh.isaac.api.sync.SyncFiles#getRootLocation()
    */
   @Override
   public File getRootLocation() {
      return this.localFolder;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @see sh.isaac.api.sync.SyncFiles#setRootLocation(java.io.File)
    */
   @Override
   public void setRootLocation(File localFolder)
            throws IllegalArgumentException {
      if (localFolder == null) {
         throw new IllegalArgumentException("The localFolder is required");
      }

      if (!localFolder.isDirectory()) {
         log.error("The passed in local folder '{}' didn't exist", localFolder);
         throw new IllegalArgumentException("The localFolder must be a folder, and must exist");
      }

      this.localFolder = localFolder;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @see sh.isaac.api.sync.SyncFiles#isRootLocationConfiguredForSCM()
    */
   @Override
   public boolean isRootLocationConfiguredForSCM() {
      return new File(this.localFolder, ".git").isDirectory();
   }
}

