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

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.map.OpenIntIntHashMap;

import sh.isaac.api.Get;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.commit.ChangeChecker;
import sh.isaac.api.commit.CheckPhase;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitTask;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.model.observable.ObservableChronologyImpl;

/**
 * The Class CommitTaskChronology.
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class CommitTaskChronology extends CommitTask {


   private Chronology chronicle;
   private final EditCoordinate editCoordinate;
   private final String commitComment;
   private final NidSet uncommittedConceptNidsWithChecks;
   private final NidSet uncommittedConceptNidsNoChecks;
   private final NidSet uncommittedSemanticNidsWithChecks;
   private final NidSet uncommittedSemanticNidsNoChecksNidSet;
   private final ConcurrentSkipListSet<ChangeChecker> checkers;
   private final CommitProvider commitProvider;


   /**
    * @param chronicle
    * @param editCoordinate
    * @param commitComment
    * @param uncommittedConceptsWithChecksNidSet
    * @param uncommittedConceptsNoChecksNidSet
    * @param uncommittedSemanticsWithChecksNidSet
    * @param uncommittedSemanticsNoChecksNidSet
    * @param checkers
    * @param commitProvider
    */
   private CommitTaskChronology(Chronology chronicle, 
         EditCoordinate editCoordinate, 
         String commitComment, 
         NidSet uncommittedConceptsWithChecksNidSet,
         NidSet uncommittedConceptsNoChecksNidSet, 
         NidSet uncommittedSemanticsWithChecksNidSet, 
         NidSet uncommittedSemanticsNoChecksNidSet,
         ConcurrentSkipListSet<ChangeChecker> checkers, 
         CommitProvider commitProvider) {
      this.chronicle = chronicle;
      this.editCoordinate = editCoordinate;
      this.commitComment = commitComment;
      this.uncommittedConceptNidsWithChecks = uncommittedConceptsWithChecksNidSet;
      this.uncommittedConceptNidsNoChecks = uncommittedConceptsNoChecksNidSet;
      this.uncommittedSemanticNidsWithChecks = uncommittedSemanticsWithChecksNidSet;
      this.uncommittedSemanticNidsNoChecksNidSet = uncommittedSemanticsNoChecksNidSet;
      this.checkers = checkers;
      this.commitProvider = commitProvider;
      if (chronicle instanceof ConceptChronology) {
         addToTotalWork(1);
      }
      else if (chronicle instanceof SemanticChronology) {
         addToTotalWork(1);
      }
      updateTitle("Commit");
      updateMessage(commitComment);
      LOG.info("Spawning CommitTask " + taskSequenceId);
      Get.activeTasks().add(this);
   }

   /**
    * Execute this task
    *
    * @return the optional
    * @throws Exception the exception
    */
   @Override
   protected Optional<CommitRecord> call() throws Exception {
      try {
         LOG.debug("performing commit for '{}'", this.commitComment);
         if (this.chronicle instanceof ObservableChronologyImpl) {
            this.chronicle = ((ObservableChronologyImpl) this.chronicle).getWrappedChronology();
         }

         if (this.chronicle instanceof ConceptChronology) {
            final ConceptChronology conceptChronology = (ConceptChronology) this.chronicle;

           //TODO [KEC] in the merge, Keith switched all these streams back to for loops... see if necessary.
            if (this.uncommittedConceptNidsWithChecks.contains(conceptChronology.getNid())) {
               this.checkers.stream().forEach((check) -> {
                  AlertObject ao = check.check(conceptChronology, CheckPhase.COMMIT);
                  if (ao.getAlertType().preventsCheckerPass()) {
                     this.alertCollection.add(ao);
                     LOG.info("commit '{}' prevented by changechecker {} because {}", commitComment, check.getDescription(), ao);
                  }
               });
               completedUnitOfWork();
            }
         } else if (this.chronicle instanceof SemanticChronology) {
            final SemanticChronology semanticChronology = (SemanticChronology) this.chronicle;

            if (this.uncommittedSemanticNidsWithChecks.contains(semanticChronology.getNid())) {
               this.checkers.stream().forEach((check) -> {
                  AlertObject ao = check.check(semanticChronology, CheckPhase.COMMIT);
                  if (ao.getAlertType().preventsCheckerPass()) {
                     this.alertCollection.add(ao);
                     LOG.info("commit '{}' prevented by changechecker {} because {}", commitComment, check.getDescription(), ao);
                  }
               });
               completedUnitOfWork();
            }
         } else {
            throw new RuntimeException("Unsupported chronology type: " + this.chronicle);
         }

         if (this.alertCollection.size() > 0) {
            return Optional.empty();
         } else {
            long commitTime = this.commitProvider.getTimeForCommit();

            // TODO have it only commit the versions on the sememe consistent with the edit coordinate.
            // successful check, commit and remove uncommitted sequences...
            final StampSequenceSet stampsInCommit = new StampSequenceSet();
            final OpenIntIntHashMap stampAliases = new OpenIntIntHashMap();
            final NidSet conceptsInCommit = new NidSet();
            final NidSet sememesInCommit = new NidSet();

            this.chronicle.getVersionList().forEach((version) -> {
               if (version.isUncommitted() && version.getAuthorNid() == editCoordinate.getAuthorNid()) {
                  version.setTime(commitTime);
                  stampsInCommit.add(version.getStampSequence());
               }
            });

            if (this.chronicle instanceof ConceptChronology) {
               final ConceptChronology conceptChronology = (ConceptChronology) this.chronicle;

               conceptsInCommit.add(conceptChronology.getNid());
               this.uncommittedConceptNidsWithChecks.remove(conceptChronology.getNid());
               this.uncommittedConceptNidsNoChecks.remove(conceptChronology.getNid());
               Get.conceptService().writeConcept(conceptChronology);
            } else {
               final SemanticChronology semanticChronology = (SemanticChronology) this.chronicle;

               sememesInCommit.add(semanticChronology.getNid());
               this.uncommittedSemanticNidsWithChecks.remove(semanticChronology.getNid());
               this.uncommittedSemanticNidsNoChecksNidSet.remove(semanticChronology.getNid());
               Get.assemblageService().writeSemanticChronology(semanticChronology);
            }

            CommitRecord commitRecord = new CommitRecord(Instant.ofEpochMilli(commitTime), 
                  stampsInCommit, 
                  stampAliases, 
                  conceptsInCommit, 
                  sememesInCommit,
                  commitComment);
            this.commitProvider.handleCommitNotification(commitRecord);
            return Optional.of(commitRecord);
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
    * Construct a task to commit a single chronology object.  The task is already executed / running when this method returns.
    * 
    * 
    * @param chronicle
    * @param editCoordinate
    * @param commitComment
    * @param uncommittedConceptsWithChecksNidSet
    * @param uncommittedConceptsNoChecksNidSet
    * @param uncommittedSemanticsWithChecksNidSet
    * @param uncommittedSemanticsNoChecksNidSet
    * @param checkers
    * @param commitProvider
    * @return a CommitTaskChronology, where calling get() on the task will return an optional - if populated, the commit was successfully handled.
    * If the get() returns an Optional.empty(), then the commit failed the change checkers.  Calling {@link CommitTaskChronology#getAlerts()} will
    * provide the details on the failed change checkers.
    */
   public static CommitTaskChronology get(Chronology chronicle,
         EditCoordinate editCoordinate,
         String commitComment,
         NidSet uncommittedConceptsWithChecksNidSet,
         NidSet uncommittedConceptsNoChecksNidSet,
         NidSet uncommittedSemanticsWithChecksNidSet,
         NidSet uncommittedSemanticsNoChecksNidSet,
         ConcurrentSkipListSet<ChangeChecker> checkers,
         CommitProvider commitProvider) {
      final CommitTaskChronology task = new CommitTaskChronology(
            chronicle, 
            editCoordinate, 
            commitComment, 
            uncommittedConceptsWithChecksNidSet,
            uncommittedConceptsNoChecksNidSet, 
            uncommittedSemanticsWithChecksNidSet, 
            uncommittedSemanticsNoChecksNidSet, 
            checkers, 
            commitProvider);
      commitProvider.getPendingCommitTasks().add(task);
      Get.activeTasks().add(task);
      try {
         Get.workExecutors().getExecutor().execute(task);
      }
      catch (Exception e) {
         Get.activeTasks().remove(task);
         throw e;
      }
      return task;
   }
}
