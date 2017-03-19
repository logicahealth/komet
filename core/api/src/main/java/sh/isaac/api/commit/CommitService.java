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

import sh.isaac.api.DatabaseServices;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.externalizable.OchreExternalizable;
import sh.isaac.api.externalizable.StampAlias;
import sh.isaac.api.externalizable.StampComment;

//~--- interfaces -------------------------------------------------------------

/**
 * @author kec
 */
@Contract
public interface CommitService
        extends DatabaseServices {
   // should the change set get generated here?
   void addAlias(int stampSequence, int stampAlias, String aliasCommitComment);

   void addChangeChecker(ChangeChecker checker);

   /**
    * Due to the use of Weak References in the implementation, you MUST maintain a reference to the change listener that is passed in here,
    * otherwise, it will be rapidly garbage collected, and you will randomly stop getting change notifications!
    * @param changeListener
    */
   void addChangeListener(ChronologyChangeListener changeListener);

   Task<Void> addUncommitted(ConceptChronology<?> cc);

   Task<Void> addUncommitted(SememeChronology<?> sc);

   Task<Void> addUncommittedNoChecks(ConceptChronology<?> cc);

   Task<Void> addUncommittedNoChecks(SememeChronology<?> sc);

   /**
    * Cancels all pending changes using the default EditCoordinate. The caller
    * may chose to block on the returned task if synchronous operation is
    * desired.
    *
    * @return task representing the cancel.
    * @deprecated use corresponding method that specifies the edit coordinate.
    */
   @Deprecated
   Task<Void> cancel();

   /**
    * Cancels all pending changes using the default EditCoordinate. The caller
    * may chose to block on the returned task if synchronous operation is
    * desired.
    *
    * @param chronicledConcept the concept to cancel changes upon.
    * @return task representing the cancel.
    * @deprecated use corresponding method that specifies the edit coordinate.
    */
   @Deprecated
   Task<Void> cancel(ConceptChronology<?> chronicledConcept);

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
    * Cancels all pending changes using the default EditCoordinate. The caller
    * may chose to block on the returned task if synchronous operation is
    * desired.
    *
    * @param sememeChronicle the sememe to cancel changes upon.
    * @return task representing the cancel.
    * @deprecated use corresponding method that specifies the edit coordinate.
    */
   @Deprecated
   Task<Void> cancel(SememeChronology<?> sememeChronicle);

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
   Task<Void> cancel(ObjectChronology<?> chronicle, EditCoordinate editCoordinate);

   /**
    * @param commitComment
    * @return
    * @deprecated use corresponding method that specifies the edit coordinate.
    */
   @Deprecated
   Task<Optional<CommitRecord>> commit(String commitComment);

   /**
    * @param chronicledConcept
    * @param commitComment
    * @return
    * @deprecated use corresponding method that specifies the edit coordinate.
    */
   @Deprecated
   Task<Optional<CommitRecord>> commit(ConceptChronology<?> chronicledConcept, String commitComment);

   /**
    * Commit all pending changes for the provided EditCoordinate. The caller may
    * chose to block on the returned task if synchronous operation is desired.
    *
    * @param commitComment  comment to associate with the commit.
    * @param editCoordinate the edit coordinate to determine which changes to
    *                       commit.
    * @return task representing the cancel.
    */
   Task<Optional<CommitRecord>> commit(EditCoordinate editCoordinate, String commitComment);

   /**
    * @param sememeChronicle
    * @param commitComment
    * @return
    * @deprecated use corresponding method that specifies the edit coordinate.
    */
   @Deprecated
   Task<Optional<CommitRecord>> commit(SememeChronology<?> sememeChronicle, String commitComment);

   /**
    * Commit all pending changes for the provided EditCoordinate. The caller may
    * chose to block on the returned task if synchronous operation is desired.
    *
    * @param chronicle
    * @param commitComment  comment to associate with the commit.
    * @param editCoordinate the edit coordinate to determine which changes to
    *                       commit.
    * @return task representing the cancel.
    */
   Task<Optional<CommitRecord>> commit(ObjectChronology<?> chronicle,
         EditCoordinate editCoordinate,
         String commitComment);

   /**
    * Import a object and immediately write to the proper service with no checks of any type performed.
    * Sememes and concepts will have their versions  merged with existing versions if they exist.
    *
    * one MUST call {@link CommitService#postProcessImportNoChecks()} when your import batch is complete
    * to ensure data integrity.
    *
    * @param ochreExternalizable the object to be imported.
    */
   void importNoChecks(OchreExternalizable ochreExternalizable);

   long incrementAndGetSequence();

   /**
    * Runs any update code (such as taxonomy updates) that may need to be done as a result of using
    * {@link #importNoChecks(OchreExternalizable)}.  There are certain operations that cannot be done
    * during the import, to avoid running into data consistency issues.  This must be called if any call to
    * importNoChecks returns a value greater than 0, implying there is at least one deferred operation.
    */
   void postProcessImportNoChecks();

   void removeChangeChecker(ChangeChecker checker);

   void removeChangeListener(ChronologyChangeListener changeListener);

   //~--- get methods ---------------------------------------------------------

   ObservableList<Alert> getAlertList();

   int[] getAliases(int stampSequence);

   Optional<String> getComment(int stampSequence);

   //~--- set methods ---------------------------------------------------------

   void setComment(int stampSequence, String comment);

   //~--- get methods ---------------------------------------------------------

   long getCommitManagerSequence();

   Stream<StampAlias> getStampAliasStream();

   Stream<StampComment> getStampCommentStream();

   /**
    * @return a summary of the uncommitted components being managed by the commit manager.
    */
   String getUncommittedComponentTextSummary();

   ObservableList<Integer> getUncommittedConceptNids();
}

