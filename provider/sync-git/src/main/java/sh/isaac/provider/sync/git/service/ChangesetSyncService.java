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



package sh.isaac.provider.sync.git.service;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.ChangeSetLoadService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.RemoteServiceInfo;
import sh.isaac.api.commit.ChangeSetWriterService;
import sh.isaac.api.sync.MergeFailOption;
import sh.isaac.api.util.StringUtils;
import sh.isaac.provider.sync.git.SyncServiceGIT;
import sh.isaac.provider.sync.git.gitblit.GitBlitUtils;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link ChangesetSyncService}
 * This service will periodically check and see if there have been changeset files written that have not yet been synced to git, and as necessary,
 * pause the changeset writers, commit and push any new files to git, and then resume the changeset writers.
 *
 *  Upon Sync, if any incoming changeset files are found, then the changeset load provider will be triggered to read in any incoming changes.
 *
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@RunLevel(value = 5)
public class ChangesetSyncService {
   private static final Logger LOG           = LogManager.getLogger();
   public static boolean       syncJSONFiles = true;  // TODO we can turn this off later

   //~--- fields --------------------------------------------------------------

   private ScheduledFuture<?> scheduledCheck;
   private SyncServiceGIT     ssg;

   //~--- constructors --------------------------------------------------------

   // For HK2
   private ChangesetSyncService() {}

   //~--- methods -------------------------------------------------------------

   @PostConstruct
   private void startMe() {
      Optional<RemoteServiceInfo> gitConfig = Get.configurationService()
                                                 .getGitConfiguration();

      if (!gitConfig.isPresent() || StringUtils.isBlank(gitConfig.get().getURL())) {
         LOG.info("No git configuration is available - Changeset sync service will not be started.");
         return;
      }

      LOG.info("Background threading initial repository sync");
      Get.workExecutors().getExecutor().execute(() -> {
                     try {
                        LOG.debug("Reading repositories from {} as user {}", gitConfig.get()
                              .getURL(), gitConfig.get()
                                    .getUsername());

                        Set<String> remoteRepos = GitBlitUtils.readRepositories(gitConfig.get()
                                                                                         .getURL(),
                                                                                gitConfig.get()
                                                                                      .getUsername(),
                                                                                gitConfig.get()
                                                                                      .getPassword());

                        LOG.debug("Read {} repositories", remoteRepos.size());

                        String changeSetRepo = "db-changesets-" + Get.conceptService().getDataStoreId().toString() +
                                               ".git";

                        if (!remoteRepos.contains(changeSetRepo)) {
                           LOG.debug("Creating remote repository {}", changeSetRepo);
                           GitBlitUtils.createRepository(gitConfig.get()
                                 .getURL(),
                                 changeSetRepo,
                                 "Storage for database changesets",
                                 gitConfig.get()
                                          .getUsername(),
                                 gitConfig.get()
                                          .getPassword(),
                                 false);
                        }

                        ssg = new SyncServiceGIT();
                        ssg.setReadmeFileContent("ISAAC Changeset Storage \r" + "=== \r" +
                        "This is a repository for storing ISAAC changesets.\r" +
                        "It is highly recommended that you do not make changes to this repository manually - ISAAC interfaces with this.");
                        ssg.setGitIgnoreContent(syncJSONFiles ? ""
                  : "*.json");

                        ChangeSetWriterService csw = LookupService.get()
                                                                  .getService(ChangeSetWriterService.class);

                        ssg.setRootLocation(csw.getWriteFolder()
                                               .toFile());
                        csw.pause();
                        LOG.debug("Attempting to link and fetch from remote GIT repository");

                        String targetUrl = GitBlitUtils.adjustBareUrlForGitBlit(gitConfig.get()
                                                                                         .getURL()) + "r/" +
                                                                                            changeSetRepo;

                        ssg.linkAndFetchFromRemote(targetUrl,
                              gitConfig.get()
                                       .getUsername(),
                              gitConfig.get()
                                       .getPassword());
                        LOG.debug("Reading any newly arrived changeset files");

                        int loaded = LookupService.get()
                                                  .getService(ChangeSetLoadService.class)
                                                  .readChangesetFiles();

                        LOG.debug("Read {} files", loaded);
                        LOG.debug("Adding untracked local files");
                        ssg.addUntrackedFiles();
                        LOG.debug("Committing and Pushing");

                        Set<String> changedFiles = ssg.updateCommitAndPush("Synchronizing changesets",
                                                                           gitConfig.get()
                                                                                 .getUsername(),
                                                                           gitConfig.get()
                                                                                 .getPassword(),
                                                                           MergeFailOption.FAIL,
                                                                           (String[]) null);

                        if (changedFiles.size() != 0) {
                           LOG.debug("Commit pulled {} more files - reading newly arrived files", changedFiles.size());
                           loaded = LookupService.get()
                                                 .getService(ChangeSetLoadService.class)
                                                 .readChangesetFiles();
                           LOG.debug("Read {} files", loaded);
                        }

                        LOG.info(
                            "Initial sync with remote repository successful.  Scheduling remote and local checks.");
                        scheduledCheck = Get.workExecutors()
                                            .getScheduledThreadPoolExecutor()
                                            .scheduleAtFixedRate(() -> syncCheck(), 5, 5, TimeUnit.MINUTES);
                     } catch (Exception e) {
                        LOG.error(
                        "Unexpected error initializing remote repository sync.  Automated repository sync will not execute.",
                        e);
                     } finally {
                        try {
                           LookupService.get()
                                        .getService(ChangeSetWriterService.class)
                                        .resume();
                        } catch (Exception e) {
                           LOG.warn("Unexpected", e);
                        }
                     }

                     LOG.info("Finished ChangesetSyncService Provider postConstruct.");
                  });
   }

   @PreDestroy
   private void stopMe() {
      if (scheduledCheck != null) {
         scheduledCheck.cancel(true);
      }

      ssg = null;
      LOG.info("Finished ChangesetSyncService Provider preDestroy.");
   }

   private void syncCheck() {
      LOG.info("Launching sync check in background thread");
      Get.workExecutors().getExecutor().execute(() -> {
                     Optional<RemoteServiceInfo> gitConfig = Get.configurationService()
                                                                .getGitConfiguration();

                     if (!gitConfig.isPresent()) {
                        LOG.info("No git configuration is available - Changeset sync service cannot execute.");
                        return;
                     }

                     try {
                        LookupService.get()
                                     .getService(ChangeSetWriterService.class)
                                     .pause();
                        LOG.debug("Adding untracked local files");
                        ssg.addUntrackedFiles();
                        LOG.debug("Committing and Syncing");

                        Set<String> changedFiles = ssg.updateCommitAndPush("Synchronizing changesets",
                                                                           gitConfig.get()
                                                                                 .getUsername(),
                                                                           gitConfig.get()
                                                                                 .getPassword(),
                                                                           MergeFailOption.FAIL,
                                                                           (String[]) null);

                        if (changedFiles.size() != 0) {
                           LOG.debug("Commit pulled {} more files - reading newly arrived files", changedFiles.size());

                           int loaded = LookupService.get()
                                                     .getService(ChangeSetLoadService.class)
                                                     .readChangesetFiles();

                           LOG.debug("Read {} files", loaded);
                        }

                        LOG.info("Sync with remote successful.");
                     } catch (Exception e) {
                        LOG.error("Unexpected error while doing remote sync.", e);
                     } finally {
                        try {
                           LookupService.get()
                                        .getService(ChangeSetWriterService.class)
                                        .resume();
                        } catch (Exception e) {
                           LOG.warn("Unexpected", e);
                        }
                     }
                  });
   }
}

