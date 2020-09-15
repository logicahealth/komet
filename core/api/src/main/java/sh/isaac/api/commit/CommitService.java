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

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;
import org.jvnet.hk2.annotations.Contract;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import sh.isaac.api.DatastoreServices;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.StampAlias;
import sh.isaac.api.externalizable.StampComment;
import sh.isaac.api.transaction.Transaction;

/**
 * The Interface CommitService.
 *
 * @author kec
 */
@Contract
public interface CommitService
        extends DatastoreServices {
   /**
    * Adds the alias.  Note, you should only add the alias after 
    * the first stamp has been committed, otherwise, your alias comment
    * will be overwritten when the transaction with the first stamp is committed.
    *
    * @param stampSequence the stamp sequence
    * @param stampAlias the stamp alias
    * @param aliasCommitComment the alias commit comment
    */
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

   void addCommitListener(CommitListener commitListener);
   void removeCommitListener(CommitListener commitListener);

   Task<Void> addUncommitted(Transaction transaction, Version version);

   /**
    * TODO remove and depend on transaction? Make it a private interface or service for transactions?
    *
    * @param transaction
    * @param chronology
    * @return
    */
   Task<Void> addUncommitted(Transaction transaction, Chronology chronology);

   /**
    * Commit all pending changes for the provided EditCoordinate. The caller may
    * chose to block on the returned task if synchronous operation is desired.
    *
    * @param transaction the transaction to determine which changes to
    *                       commit.
    * @param commitComment  comment to associate with the commit.
    * @return task representing the cancel.
    * TODO remove and depend on transaction?
    */
   CommitTask commit(Transaction transaction, String commitComment, ConcurrentSkipListSet<AlertObject> alertCollection);

   CommitTask commit(Transaction transaction, String commitComment,
                     ConcurrentSkipListSet<AlertObject> alertCollection, Instant commitTime);

   /**
    * Import a object and immediately write to the proper service with no checks of any type performed.
    * Semantics and concepts will have their versions  merged with existing versions if they exist.
    *
    * one MUST call {@link CommitService#postProcessImportNoChecks()} when your import batch is complete
    * to ensure data integrity.
    *
    * @param isaacExternalizable the object to be imported.
    */
   void importNoChecks(IsaacExternalizable isaacExternalizable);
   /**
    * Import a object if more than the time of a version has changed,
    * and immediately write to the proper service with no checks other than commit time of any type performed.
    * Semantics and concepts will have their versions  merged with existing versions if they exist.
    *
    * one MUST call {@link CommitService#postProcessImportNoChecks()} when your import batch is complete
    * to ensure data integrity.
    *
    * @param isaacExternalizable the object to be imported.
    */
   void importIfContentChanged(IsaacExternalizable isaacExternalizable);

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

   /**
    * This method should NOT be considered part of the public API, and should NOT be used.
    * It is only for internal use when loading from IBDF.
    * 
    * Specify comments and stamps during commit.
    *
    * @param stampSequence the stamp sequence
    * @param comment the comment
    */
   void setComment(int stampSequence, String comment);

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
   
   /**
   * Calls {@link #newTransaction(Optional, ChangeCheckerMode)} with {@link ChangeCheckerMode#ACTIVE}
   * @param transactionName name for the transaction
   * @return a new transaction that will perform tests depending on value of performTests.
   */
   default Transaction newTransaction(String transactionName) {
      return newTransaction(Optional.ofNullable(transactionName), ChangeCheckerMode.ACTIVE);
   }

   /**
    * Calls {@link #newTransaction(Optional, ChangeCheckerMode)} with {@link ChangeCheckerMode#ACTIVE} and indexOnCommit=true
    * @param transactionName optional name for the transaction
    * @param changeCheckerMode true if tests should be performed.
    * @return a new transaction that will perform tests depending on value of performTests.
    */
   default Transaction newTransaction(Optional<String> transactionName, ChangeCheckerMode changeCheckerMode) {
       return newTransaction(transactionName, changeCheckerMode, true);
   }
   
   /**
   * @param transactionName optional name for the transaction
   * @param changeCheckerMode true if tests should be performed.
   * @param indexOnCommit if true, let indexers run after commit, if false, do not index after commit (typical only 
   * for a batch operation that handles its own indexing)
   * @return a new transaction that will perform tests depending on value of performTests.
   */
  Transaction newTransaction(Optional<String> transactionName, ChangeCheckerMode changeCheckerMode, boolean indexOnCommit);

   /**
    *
    * @return get a list of pending transactions.
    */
   ObservableSet<Transaction> getPendingTransactionList();

   /**
    * @return a current Instant that can be used as a commit time for a long-lived process, such as
    * wanting to commit the results of a classification process at the time of the stamp position used
    * to determine current axioms.
    */
   Instant getTimeForCommit();
}

