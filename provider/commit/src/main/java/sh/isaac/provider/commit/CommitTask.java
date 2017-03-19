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
 *
 * @author kec
 */
public class CommitTask
        extends TimedTask<Optional<CommitRecord>> {
   private static final Logger log = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   final ConceptSequenceSet                           conceptsToCommit = new ConceptSequenceSet();
   final ConceptSequenceSet                           conceptsToCheck  = new ConceptSequenceSet();
   final SememeSequenceSet                            sememesToCommit  = new SememeSequenceSet();
   final SememeSequenceSet                            sememesToCheck   = new SememeSequenceSet();
   final String                                       commitComment;
   final long                                         lastCommit;
   private final ConcurrentSkipListSet<ChangeChecker> checkers;
   private final ConcurrentSkipListSet<Alert>         alertCollection;
   private final Map<UncommittedStamp, Integer>       pendingStampsForCommit;
   private final CommitProvider                       commitProvider;
   private final StampService                         stampProvider;

   //~--- constructors --------------------------------------------------------

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
      conceptsToCommit.or(uncommittedConceptsNoChecksSequenceSet);
      conceptsToCommit.or(uncommittedConceptsWithChecksSequenceSet);
      conceptsToCheck.or(uncommittedConceptsWithChecksSequenceSet);
      sememesToCommit.or(uncommittedSememesNoChecksSequenceSet);
      sememesToCommit.or(uncommittedSememesWithChecksSequenceSet);
      sememesToCheck.or(uncommittedSememesWithChecksSequenceSet);
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
         conceptsToCommit.stream().forEach((conceptSequence) -> {
                                     ConceptChronology c = Get.conceptService()
                                                              .getConcept(conceptSequence);

                                     if (conceptsToCheck.contains(conceptSequence)) {
                                        checkers.stream().forEach((check) -> {
                        check.check(c, alertCollection, CheckPhase.COMMIT);
                     });
                                     }
                                  });
         sememesToCommit.stream().forEach((sememeSequence) -> {
                                    SememeChronology sc = Get.sememeService()
                                                             .getSememe(sememeSequence);

                                    if (sememesToCheck.contains(sememeSequence)) {
                                       checkers.stream().forEach((check) -> {
                        check.check(sc, alertCollection, CheckPhase.COMMIT);
                     });
                                    }
                                 });

         if (alertCollection.stream()
                            .anyMatch((alert) -> (alert.getAlertType() == AlertType.ERROR))) {
            commitProvider.revertCommit(conceptsToCommit,
                                        conceptsToCheck,
                                        sememesToCommit,
                                        sememesToCheck,
                                        pendingStampsForCommit);
            return Optional.empty();
         }

         long             commitTime       = System.currentTimeMillis();
         StampSequenceSet stampSequenceSet = new StampSequenceSet();

         pendingStampsForCommit.entrySet().stream().forEach((entry) -> {
                                           int stampSequence = entry.getValue();

                                           stampSequenceSet.add(stampSequence);

                                           UncommittedStamp uncommittedStamp = entry.getKey();
                                           Stamp stamp = new Stamp(entry.getKey().status,
                                                                   commitTime,
                                                                   uncommittedStamp.authorSequence,
                                                                   uncommittedStamp.moduleSequence,
                                                                   uncommittedStamp.pathSequence);

                                           stampProvider.addStamp(stamp, stampSequence);
                                        });

         if (commitComment != null) {
            stampSequenceSet.stream()
                            .forEach((stamp) -> commitProvider.addComment(stamp, commitComment));
         }

         if (!stampSequenceSet.isEmpty()) {
            CommitRecord commitRecord = new CommitRecord(Instant.ofEpochMilli(commitTime),
                                                         stampSequenceSet,
                                                         new OpenIntIntHashMap(),
                                                         ConceptSequenceSet.of(conceptsToCheck).or(conceptsToCommit),
                                                         SememeSequenceSet.of(sememesToCheck).or(sememesToCommit),
                                                         commitComment);

            commitProvider.handleCommitNotification(commitRecord);
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
      } catch (Exception e1) {
         throw new RuntimeException("Commit Failure of commit with message " + commitComment, e1);
      } finally {
         Get.activeTasks()
            .remove(this);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    *
    * @param commitComment
    * @param uncommittedConceptsWithChecksSequenceSet
    * @param uncommittedConceptsNoChecksSequenceSet
    * @param uncommittedSememesWithChecksSequenceSet
    * @param uncommittedSememesNoChecksSequenceSet
    * @param lastCommit
    * @param checkers
    * @param alertCollection
    * @param pendingStampsForCommit
    * @param commitProvider
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
      CommitTask task = new CommitTask(commitComment,
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

