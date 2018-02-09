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



package sh.isaac.provider.commit;

//~--- JDK imports ------------------------------------------------------------

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.map.OpenIntIntHashMap;

import sh.isaac.api.Get;
import sh.isaac.api.alert.AlertCategory;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.alert.AlertType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.commit.ChangeChecker;
import sh.isaac.api.commit.CheckPhase;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitTask;
import sh.isaac.api.commit.Stamp;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.commit.UncommittedStamp;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;

//~--- classes ----------------------------------------------------------------

/**
 * The Class CommitTaskGlobal.
 *
 * @author kec
 */
public class CommitTaskGlobal extends CommitTask{

   //~--- fields --------------------------------------------------------------

   /** The concepts to commit. */
   final NidSet conceptNidsToCommit = new NidSet();

   /** The concepts to check. */
   final NidSet conceptNidsToCheck = new NidSet();

   /** The semantics to commit. */
   final NidSet semanticNidsToCommit = new NidSet();

   /** The semantics to check. */
   final NidSet semanticNidsToCheck = new NidSet();

   /** The commit comment. */
   final String commitComment;

   /** The last commit. */
   final long lastCommit;

   /** The checkers. */
   private final ConcurrentSkipListSet<ChangeChecker> checkers;

   /** The pending stamps for commit. */
   private final Map<UncommittedStamp, Integer> pendingStampsForCommit;

   /** The commit provider. */
   private final CommitProvider commitProvider;

   /** The stamp provider. */
   private final StampService stampProvider;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new global commit task.
    *
    * @param commitComment the commit comment
    * @param uncommittedConceptsWithChecksNidSet the uncommitted concepts with checks sequence set
    * @param uncommittedConceptsNoChecksNidSet the uncommitted concepts no checks sequence set
    * @param uncommittedSememesWithChecksNidSet the uncommitted sememes with checks sequence set
    * @param uncommittedSememesNoChecksNidSet the uncommitted sememes no checks sequence set
    * @param lastCommit the last commit
    * @param checkers the checkers
    * @param alertCollection the alert collection
    * @param pendingStampsForCommit the pending stamps for commit
    * @param commitProvider the commit provider
    */
   private CommitTaskGlobal(String commitComment,
                      NidSet uncommittedConceptsWithChecksNidSet,
                      NidSet uncommittedConceptsNoChecksNidSet,
                      NidSet uncommittedSememesWithChecksNidSet,
                      NidSet uncommittedSememesNoChecksNidSet,
                      long lastCommit,
                      ConcurrentSkipListSet<ChangeChecker> checkers,
                      Map<UncommittedStamp, Integer> pendingStampsForCommit,
                      CommitProvider commitProvider) {
      this.commitComment = commitComment;
      this.conceptNidsToCommit.or(uncommittedConceptsNoChecksNidSet);
      this.conceptNidsToCommit.or(uncommittedConceptsWithChecksNidSet);
      this.conceptNidsToCheck.or(uncommittedConceptsWithChecksNidSet);
      this.semanticNidsToCommit.or(uncommittedSememesNoChecksNidSet);
      this.semanticNidsToCommit.or(uncommittedSememesWithChecksNidSet);
      this.semanticNidsToCheck.or(uncommittedSememesWithChecksNidSet);
      uncommittedConceptsNoChecksNidSet.clear();
      uncommittedConceptsWithChecksNidSet.clear();
      uncommittedSememesNoChecksNidSet.clear();
      uncommittedSememesWithChecksNidSet.clear();
      this.lastCommit             = lastCommit;
      this.checkers               = checkers;
      this.pendingStampsForCommit = pendingStampsForCommit;
      this.commitProvider         = commitProvider;
      this.stampProvider          = Get.stampService();
      addToTotalWork(this.conceptNidsToCommit.size());
      addToTotalWork(this.semanticNidsToCommit.size());
      updateTitle("Commit");
      updateMessage(commitComment);
      LOG.info("Spawning CommitTask " + taskSequenceId);
      Get.activeTasks().add(this);
   }

   /**
    * Execute the commit task
    *
    * @return the optional
    * @throws Exception the exception
    */
   @Override
   protected Optional<CommitRecord> call()
            throws Exception {
      try {

         LOG.debug("performing commit for '{}'", this.commitComment);
         this.conceptNidsToCommit.stream().forEach((conceptNid) -> {
            final ConceptChronology c = Get.conceptService().getConceptChronology(conceptNid);

            //TODO [KEC] in the merge, Keith switched all these streams back to for loops... see if necessary.
            if (this.conceptNidsToCheck.contains(conceptNid)) {
               this.checkers.stream().forEach((check) -> {
                  AlertObject ao = check.check(c, CheckPhase.COMMIT);
                  if (ao.getAlertType().preventsCheckerPass()) {
                     this.alertCollection.add(ao);
                     LOG.info("commit '{}' prevented by changechecker {} because {}", commitComment, check.getDescription(), ao);
                  }
               });
            }
            completedUnitOfWork();
         });
         this.semanticNidsToCommit.stream().forEach((semanticNid) -> {

            if (this.semanticNidsToCheck.contains(semanticNid)) {
            final SemanticChronology sc = Get.assemblageService().getSemanticChronology(semanticNid);  
               this.checkers.stream().forEach((check) -> {
                  AlertObject ao = check.check(sc, CheckPhase.COMMIT);
                  if (ao.getAlertType().preventsCheckerPass()) {
                     this.alertCollection.add(ao);
                     LOG.info("commit '{}' prevented by changechecker {} because {}", commitComment, check.getDescription(), ao);
                  }
               });
            }
            completedUnitOfWork();
         });

         if (this.alertCollection.size() > 0) {
            this.commitProvider.revertCommit(
                  this.conceptNidsToCommit, 
                  this.conceptNidsToCheck, 
                  this.semanticNidsToCommit, 
                  this.semanticNidsToCheck, 
                  this.pendingStampsForCommit);
            return Optional.empty();
         }

         final long commitTime = this.commitProvider.getTimeForCommit();
         final StampSequenceSet stampSequenceSet = new StampSequenceSet();

         this.pendingStampsForCommit.entrySet().stream().forEach((entry) -> {
            final int stampSequence = entry.getValue();

            stampSequenceSet.add(stampSequence);

            final UncommittedStamp uncommittedStamp = entry.getKey();
            final Stamp stamp = new Stamp(entry.getKey().status, commitTime, uncommittedStamp.authorNid, uncommittedStamp.moduleNid, uncommittedStamp.pathNid);

            this.stampProvider.addStamp(stamp, stampSequence);
         });

         if (this.commitComment != null) {
            stampSequenceSet.stream().forEach((stamp) -> this.commitProvider.addComment(stamp, this.commitComment));
         }

         if (!stampSequenceSet.isEmpty()) {
            final CommitRecord commitRecord = new CommitRecord(Instant.ofEpochMilli(commitTime), 
                  stampSequenceSet, 
                  new OpenIntIntHashMap(),
                  NidSet.of(this.conceptNidsToCheck).or(this.conceptNidsToCommit), 
                  NidSet.of(this.semanticNidsToCheck).or(this.semanticNidsToCommit), 
                  this.commitComment);

            this.commitProvider.handleCommitNotification(commitRecord);
            return Optional.of(commitRecord);
         }
         else{
            this.alertCollection.add(new AlertObject("nothing to commit", "Nothing was found to commit", AlertType.INFORMATION, AlertCategory.COMMIT));
            return Optional.empty();
         }
      } catch (final Exception e1) {
         LOG.error("Unexpected commit failure", e1);
         throw new RuntimeException("Commit Failure of commit with message " + this.commitComment, e1);
      } finally {
         Get.activeTasks().remove(this);
         this.commitProvider.getPendingCommitTasks().remove(this);
         LOG.info("Finished CommitTask " + taskSequenceId);
      }
   }

   /**
    * Construct a task to perform a global commit.  The task is already executed / running when this method returns.
    *
    * @param commitComment the commit comment
    * @param uncommittedConceptsWithChecksNidSet the uncommitted concepts with checks sequence set
    * @param uncommittedConceptsNoChecksNidSet the uncommitted concepts no checks sequence set
    * @param uncommittedSemanticsWithChecksNidSet the uncommitted sememes with checks sequence set
    * @param uncommittedSemanticsNoChecksNidSet the uncommitted sememes no checks sequence set
    * @param lastCommit the last commit
    * @param checkers the checkers
    * @param pendingStampsForCommit the pending stamps for commit
    * @param commitProvider the commit provider
    * @return a CommitTaskChronology, where calling get() on the task will return an optional - if populated, the commit was successfully handled.
    * If the get() returns an Optional.empty(), then the commit failed the change checkers.  Calling {@link CommitTaskChronology#getAlerts()} will
    * provide the details on the failed change checkers.
    */
   public static CommitTaskGlobal get(
                                String commitComment,
                                NidSet uncommittedConceptsWithChecksNidSet,
                                NidSet uncommittedConceptsNoChecksNidSet,
                                NidSet uncommittedSemanticsWithChecksNidSet,
                                NidSet uncommittedSemanticsNoChecksNidSet,
                                long lastCommit,
                                ConcurrentSkipListSet<ChangeChecker> checkers,
                                Map<UncommittedStamp, Integer> pendingStampsForCommit,
                                CommitProvider commitProvider) {
      final CommitTaskGlobal task = new CommitTaskGlobal(
                                  commitComment,
                                  uncommittedConceptsWithChecksNidSet,
                                  uncommittedConceptsNoChecksNidSet,
                                  uncommittedSemanticsWithChecksNidSet,
                                  uncommittedSemanticsNoChecksNidSet,
                                  lastCommit,
                                  checkers,
                                  pendingStampsForCommit,
                                  commitProvider);
      commitProvider.getPendingCommitTasks().add(task);
      Get.activeTasks().add(task);
      try {
         Get.workExecutors().getExecutor().execute(task);
         return task;
      } catch (Exception e) {
         Get.activeTasks().remove(task);
         throw e;
      }
   }
}

