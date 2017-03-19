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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.map.OpenIntIntHashMap;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.SememeSequenceSet;
import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.commit.Alert;
import sh.isaac.api.commit.AlertType;
import sh.isaac.api.commit.ChangeChecker;
import sh.isaac.api.commit.CheckPhase;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.Stamp;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.commit.UncommittedStamp;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.progress.ActiveTasks;
import sh.isaac.api.task.TimedTask;

//~--- classes ----------------------------------------------------------------

/**
 * The Class CommitTask.
 *
 * @author kec
 */
public class CommitTask
        extends TimedTask<Optional<CommitRecord>> {
   /** The Constant log. */
   private static final Logger log = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   /** The concepts to commit. */
   final ConceptSequenceSet conceptsToCommit = new ConceptSequenceSet();

   /** The concepts to check. */
   final ConceptSequenceSet conceptsToCheck = new ConceptSequenceSet();

   /** The sememes to commit. */
   final SememeSequenceSet sememesToCommit = new SememeSequenceSet();

   /** The sememes to check. */
   final SememeSequenceSet sememesToCheck = new SememeSequenceSet();

   /** The commit comment. */
   final String commitComment;

   /** The last commit. */
   final long lastCommit;

   /** The checkers. */
   private final ConcurrentSkipListSet<ChangeChecker> checkers;

   /** The alert collection. */
   private final ConcurrentSkipListSet<Alert> alertCollection;

   /** The pending stamps for commit. */
   private final Map<UncommittedStamp, Integer> pendingStampsForCommit;

   /** The commit provider. */
   private final CommitProvider commitProvider;

   /** The stamp provider. */
   private final StampService stampProvider;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new commit task.
    *
    * @param commitComment the commit comment
    * @param uncommittedConceptsWithChecksSequenceSet the uncommitted concepts with checks sequence set
    * @param uncommittedConceptsNoChecksSequenceSet the uncommitted concepts no checks sequence set
    * @param uncommittedSememesWithChecksSequenceSet the uncommitted sememes with checks sequence set
    * @param uncommittedSememesNoChecksSequenceSet the uncommitted sememes no checks sequence set
    * @param lastCommit the last commit
    * @param checkers the checkers
    * @param alertCollection the alert collection
    * @param pendingStampsForCommit the pending stamps for commit
    * @param commitProvider the commit provider
    */
   private CommitTask(String commitComment,
                      ConceptSequenceSet uncommittedConceptsWithChecksSequenceSet,
                      ConceptSequenceSet uncommittedConceptsNoChecksSequenceSet,
                      SememeSequenceSet uncommittedSememesWithChecksSequenceSet,
                      SememeSequenceSet uncommittedSememesNoChecksSequenceSet,
                      long lastCommit,
                      ConcurrentSkipListSet<ChangeChecker> checkers,
                      ConcurrentSkipListSet<Alert> alertCollection,
                      Map<UncommittedStamp, Integer> pendingStampsForCommit,
                      CommitProvider commitProvider) {
      LookupService.getService(ActiveTasks.class)
                   .get()
                   .add(this);
      this.commitComment = commitComment;
      this.conceptsToCommit.or(uncommittedConceptsNoChecksSequenceSet);
      this.conceptsToCommit.or(uncommittedConceptsWithChecksSequenceSet);
      this.conceptsToCheck.or(uncommittedConceptsWithChecksSequenceSet);
      this.sememesToCommit.or(uncommittedSememesNoChecksSequenceSet);
      this.sememesToCommit.or(uncommittedSememesWithChecksSequenceSet);
      this.sememesToCheck.or(uncommittedSememesWithChecksSequenceSet);
      uncommittedConceptsNoChecksSequenceSet.clear();
      uncommittedConceptsWithChecksSequenceSet.clear();
      uncommittedSememesNoChecksSequenceSet.clear();
      uncommittedSememesWithChecksSequenceSet.clear();
      this.lastCommit             = lastCommit;
      this.checkers               = checkers;
      this.alertCollection        = alertCollection;
      this.pendingStampsForCommit = pendingStampsForCommit;
      this.commitProvider         = commitProvider;
      this.stampProvider          = Get.stampService();
      updateTitle("Commit");
      updateMessage(commitComment);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Call.
    *
    * @return the optional
    * @throws Exception the exception
    */
   @Override
   protected Optional<CommitRecord> call()
            throws Exception {
      try {
         // TODO handle notification...
         // try {
         // GlobalPropertyChange.fireVetoableChange(TerminologyStoreDI.CONCEPT_EVENT.PRE_COMMIT, null, conceptsToCommit);
         // } catch (PropertyVetoException ex) {
         // return;
         // }
         this.conceptsToCommit.stream().forEach((conceptSequence) -> {
                                          final ConceptChronology c = Get.conceptService()
                                                                         .getConcept(conceptSequence);

                                          if (this.conceptsToCheck.contains(conceptSequence)) {
                                             this.checkers.stream().forEach((check) -> {
                        check.check(c, this.alertCollection, CheckPhase.COMMIT);
                     });
                                          }
                                       });
         this.sememesToCommit.stream().forEach((sememeSequence) -> {
                                         final SememeChronology sc = Get.sememeService()
                                                                        .getSememe(sememeSequence);

                                         if (this.sememesToCheck.contains(sememeSequence)) {
                                            this.checkers.stream().forEach((check) -> {
                        check.check(sc, this.alertCollection, CheckPhase.COMMIT);
                     });
                                         }
                                      });

         if (this.alertCollection.stream()
                                 .anyMatch((alert) -> (alert.getAlertType() == AlertType.ERROR))) {
            this.commitProvider.revertCommit(this.conceptsToCommit,
                                             this.conceptsToCheck,
                                             this.sememesToCommit,
                                             this.sememesToCheck,
                                             this.pendingStampsForCommit);
            return Optional.empty();
         }

         final long             commitTime       = System.currentTimeMillis();
         final StampSequenceSet stampSequenceSet = new StampSequenceSet();

         this.pendingStampsForCommit.entrySet().stream().forEach((entry) -> {
                  final int stampSequence = entry.getValue();

                  stampSequenceSet.add(stampSequence);

                  final UncommittedStamp uncommittedStamp = entry.getKey();
                  final Stamp stamp = new Stamp(entry.getKey().status,
                                                commitTime,
                                                uncommittedStamp.authorSequence,
                                                uncommittedStamp.moduleSequence,
                                                uncommittedStamp.pathSequence);

                  this.stampProvider.addStamp(stamp, stampSequence);
               });

         if (this.commitComment != null) {
            stampSequenceSet.stream()
                            .forEach((stamp) -> this.commitProvider.addComment(stamp, this.commitComment));
         }

         if (!stampSequenceSet.isEmpty()) {
            final CommitRecord commitRecord = new CommitRecord(Instant.ofEpochMilli(commitTime),
                                                               stampSequenceSet,
                                                               new OpenIntIntHashMap(),
                                                               ConceptSequenceSet.of(this.conceptsToCheck).or(
                                                                  this.conceptsToCommit),
                                                               SememeSequenceSet.of(this.sememesToCheck).or(
                                                                  this.sememesToCommit),
                                                               this.commitComment);

            this.commitProvider.handleCommitNotification(commitRecord);
            return Optional.of(commitRecord);
         }

         // TODO Indexers need to be change listeners
         // notifyCommit();
         // if (indexers != null) {
         // for (IndexService i : indexers) {
         // i.commitWriter();
         // }
         // }
         // GlobalPropertyChange.firePropertyChange(TerminologyStoreDI.CONCEPT_EVENT.POST_COMMIT, null, conceptsToCommit);
         return Optional.empty();
      } catch (final Exception e1) {
         throw new RuntimeException("Commit Failure of commit with message " + this.commitComment, e1);
      } finally {
         Get.activeTasks()
            .remove(this);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the.
    *
    * @param commitComment the commit comment
    * @param uncommittedConceptsWithChecksSequenceSet the uncommitted concepts with checks sequence set
    * @param uncommittedConceptsNoChecksSequenceSet the uncommitted concepts no checks sequence set
    * @param uncommittedSememesWithChecksSequenceSet the uncommitted sememes with checks sequence set
    * @param uncommittedSememesNoChecksSequenceSet the uncommitted sememes no checks sequence set
    * @param lastCommit the last commit
    * @param checkers the checkers
    * @param alertCollection the alert collection
    * @param pendingStampsForCommit the pending stamps for commit
    * @param commitProvider the commit provider
    * @return a {@code CommitTask} after it has been given to an executor, and
    * added to the activeTasks service.
    */
   public static CommitTask get(String commitComment,
                                ConceptSequenceSet uncommittedConceptsWithChecksSequenceSet,
                                ConceptSequenceSet uncommittedConceptsNoChecksSequenceSet,
                                SememeSequenceSet uncommittedSememesWithChecksSequenceSet,
                                SememeSequenceSet uncommittedSememesNoChecksSequenceSet,
                                long lastCommit,
                                ConcurrentSkipListSet<ChangeChecker> checkers,
                                ConcurrentSkipListSet<Alert> alertCollection,
                                Map<UncommittedStamp, Integer> pendingStampsForCommit,
                                CommitProvider commitProvider) {
      final CommitTask task = new CommitTask(commitComment,
                                             uncommittedConceptsWithChecksSequenceSet,
                                             uncommittedConceptsNoChecksSequenceSet,
                                             uncommittedSememesWithChecksSequenceSet,
                                             uncommittedSememesNoChecksSequenceSet,
                                             lastCommit,
                                             checkers,
                                             alertCollection,
                                             pendingStampsForCommit,
                                             commitProvider);

      Get.activeTasks()
         .add(task);
      Get.workExecutors()
         .getExecutor()
         .execute(task);
      return task;
   }
}

