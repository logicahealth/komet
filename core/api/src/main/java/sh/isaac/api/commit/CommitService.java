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



package sh.isaac.api.commit;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import javafx.collections.ObservableList;

import javafx.concurrent.Task;

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.DatastoreServices;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.externalizable.StampAlias;
import sh.isaac.api.externalizable.StampComment;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.ObservableVersion;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface CommitService.
 *
 * @author kec
 */
@Contract
public interface CommitService
        extends DatastoreServices {
   /**
    * Adds the alias.
    *
    * @param stampSequence the stamp sequence
    * @param stampAlias the stamp alias
    * @param aliasCommitComment the alias commit comment
    */

   // should the change set get generated here?
   void addAlias(int stampSequence, int stampAlias, String aliasCommitComment);

   /**
    * Adds the change checker.
    *
    * @param checker the checker
    */
   void addChangeChecker(ChangeChecker checker);

   /**
    * Due to the use of Weak References in the implementation, you MUST maintain a reference to the change listener that is passed in here,
    * otherwise, it will be rapidly garbage collected, and you will randomly stop getting change notifications!.
    *
    * @param changeListener the change listener
    */
   void addChangeListener(ChronologyChangeListener changeListener);

   /**
    * Adds the uncommitted.
    *
    * @param cc the cc
    * @return the task
    */
   Task<Void> addUncommitted(ConceptChronology cc);

   /**
    * Adds the uncommitted.
    *
    * @param sc the sc
    * @return the task
    */
   Task<Void> addUncommitted(SemanticChronology sc);

   /**
    * Adds the uncommitted no checks.
    *
    * @param cc the cc
    * @return the task
    */
   Task<Void> addUncommittedNoChecks(ConceptChronology cc);

   /**
    * Adds the uncommitted no checks.
    *
    * @param sc the sc
    * @return the task
    */
   Task<Void> addUncommittedNoChecks(SemanticChronology sc);

   /**
    * Cancels all pending changes using the provided EditCoordinate. The caller
    * may chose to block on the returned task if synchronous operation is
    * desired.
    *
    * @param editCoordinate the edit coordinate to determine which changes to
    *                       cancel.
    * @return task representing the cancel.
    */
   Task<Void> cancel(EditCoordinate editCoordinate);

   /**
    * Cancels all pending changes using the provided EditCoordinate. The caller
    * may chose to block on the returned task if synchronous operation is
    * desired.
    *
    * @param chronicle      the chronicle to cancel changes upon.
    * @param editCoordinate the edit coordinate to determine which changes to
    *                       cancel.
    * @return task representing the cancel.
    */
   Task<Void> cancel(Chronology chronicle, EditCoordinate editCoordinate);

   /**
    * Commit all pending changes for the provided EditCoordinate. The caller may
    * chose to block on the returned task if synchronous operation is desired.
    *
    * @param editCoordinate the edit coordinate to determine which changes to
    *                       commit.
    * @param commitComment  comment to associate with the commit.
    * @return task representing the cancel.
    */
   CommitTask commit(EditCoordinate editCoordinate, String commitComment);

   /**
    * Commit all pending changes for the provided EditCoordinate. The caller may
    * choose to block on the returned task if synchronous operation is desired.
    *
    * @param chronicle the chronicle
    * @param editCoordinate the edit coordinate to determine which changes to
    *                       commit.
    * @param commitComment  comment to associate with the commit.
    * @return task representing the cancel.
    */
   @Deprecated
   CommitTask commit(Chronology chronicle,
         EditCoordinate editCoordinate,
         String commitComment);

   
   CommitTask commit(ObservableVersion versionsToCommit,
         EditCoordinate editCoordinate,
         String commitComment);

   /**
    * Import a object and immediately write to the proper service with no checks of any type performed.
    * Semantics and concepts will have their versions  merged with existing versions if they exist.
    *
    * one MUST call {@link CommitService#postProcessImportNoChecks()} when your import batch is complete
    * to ensure data integrity.
    *
    * @param ochreExternalizable the object to be imported.
    */
   void importNoChecks(IsaacExternalizable ochreExternalizable);

   /**
    * Increment and get sequence.
    *
    * @return the long
    */
   long incrementAndGetSequence();

   /**
    * Runs any update code (such as taxonomy updates) that may need to be done as a result of using
    * {@link #importNoChecks(IsaacExternalizable)}.  There are certain operations that cannot be done
    * during the import, to avoid running into data consistency issues.  This must be called if any call to
    * importNoChecks returns a value greater than 0, implying there is at least one deferred operation.
    */
   void postProcessImportNoChecks();

   /**
    * Removes the change checker.
    *
    * @param checker the checker
    */
   void removeChangeChecker(ChangeChecker checker);

   /**
    * Removes the change listener.
    *
    * @param changeListener the change listener
    */
   void removeChangeListener(ChronologyChangeListener changeListener);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the aliases.
    *
    * @param stampSequence the stamp sequence
    * @return the aliases
    */
   int[] getAliases(int stampSequence);

   /**
    * Gets the comment.
    *
    * @param stampSequence the stamp sequence
    * @return the comment
    */
   Optional<String> getComment(int stampSequence);

   //~--- set methods ---------------------------------------------------------

   /**
    * Set comment.
    *
    * @param stampSequence the stamp sequence
    * @param comment the comment
    */
   void setComment(int stampSequence, String comment);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the commit manager sequence.
    *
    * @return the commit manager sequence
    */
   long getCommitManagerSequence();

   /**
    * Gets the stamp alias stream.
    *
    * @return the stamp alias stream
    */
   Stream<StampAlias> getStampAliasStream();

   /**
    * Gets the stamp comment stream.
    *
    * @return the stamp comment stream
    */
   Stream<StampComment> getStampCommentStream();

   /**
    * Gets the uncommitted component text summary.
    *
    * @return a summary of the uncommitted components being managed by the commit manager.
    */
   String getUncommittedComponentTextSummary();

   /**
    * Gets the uncommitted concept nids.
    *
    * @return the uncommitted concept nids
    */
   ObservableList<Integer> getUncommittedConceptNids();
}

