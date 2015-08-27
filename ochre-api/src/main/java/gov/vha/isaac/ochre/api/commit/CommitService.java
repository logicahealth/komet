/*
 * Copyright 2015 kec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.api.commit;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.IntStream;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface CommitService {

	// should the change set get generated here?

	void addAlias(int stampSequence, int stampAlias, String aliasCommitComment);

	int[] getAliases(int stampSequence);

	void setComment(int stampSequence, String comment);

	Optional<String> getComment(int stampSequence);

	Task<Void> addUncommitted(ConceptChronology<?> cc);

	Task<Void> addUncommittedNoChecks(ConceptChronology<?> cc);

	Task<Void> addUncommitted(SememeChronology<?> sc);

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
	 *
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
	 * @param editCoordinate the edit coordinate to determine which changes to
	 * cancel.
	 * @return task representing the cancel.
	 */
	Task<Void> cancel(EditCoordinate editCoordinate);

	/**
	 * Cancels all pending changes using the provided EditCoordinate. The caller
	 * may chose to block on the returned task if synchronous operation is
	 * desired.
	 *
	 * @param chronicle the chronicle to cancel changes upon.
	 * @param editCoordinate the edit coordinate to determine which changes to
	 * cancel.
	 * @return task representing the cancel.
	 */
	Task<Void> cancel(ObjectChronology<?> chronicle, EditCoordinate editCoordinate);

	/**
	 * Commit all pending changes for the provided EditCoordinate. The caller may
	 * chose to block on the returned task if synchronous operation is desired.
	 *
	 * @param commitComment comment to associate with the commit.
	 * @param editCoordinate the edit coordinate to determine which changes to
	 * commit.
	 * @return task representing the cancel.
	 */
	Task<Optional<CommitRecord>> commit(EditCoordinate editCoordinate, String commitComment);

	/**
	 * Commit all pending changes for the provided EditCoordinate. The caller may
	 * chose to block on the returned task if synchronous operation is desired.
	 *
	 * @param chronicle
	 * @param commitComment comment to associate with the commit.
	 * @param editCoordinate the edit coordinate to determine which changes to
	 * commit.
	 * @return task representing the cancel.
	 */
	Task<Optional<CommitRecord>> commit(ObjectChronology<?> chronicle, EditCoordinate editCoordinate, String commitComment);

	/**
	 *
	 * @param commitComment
	 * @return
	 * @deprecated use corresponding method that specifies the edit coordinate.
	 */
	@Deprecated
	Task<Optional<CommitRecord>> commit(String commitComment);

	/**
	 *
	 * @param chronicledConcept
	 * @param commitComment
	 * @return
	 * @deprecated use corresponding method that specifies the edit coordinate.
	 */
	@Deprecated
	Task<Optional<CommitRecord>> commit(ConceptChronology<?> chronicledConcept, String commitComment);

	/**
	 *
	 * @param sememeChronicle
	 * @param commitComment
	 * @return
	 * @deprecated use corresponding method that specifies the edit coordinate.
	 */
	@Deprecated
	Task<Optional<CommitRecord>> commit(SememeChronology<?> sememeChronicle, String commitComment);

	ObservableList<Integer> getUncommittedConceptNids();

	ObservableList<Alert> getAlertList();

	void addChangeChecker(ChangeChecker checker);

	void removeChangeChecker(ChangeChecker checker);

	void addChangeListener(ChronologyChangeListener changeListener);

	void removeChangeListener(ChronologyChangeListener changeListener);

	long getCommitManagerSequence();

	long incrementAndGetSequence();

	int getAuthorSequenceForStamp(int stampSequence);

	int getModuleSequenceForStamp(int stampSequence);

	int getPathSequenceForStamp(int stampSequence);

	State getStatusForStamp(int stampSequence);

	long getTimeForStamp(int stampSequence);

	boolean isNotCanceled(int stampSequence);

	boolean isUncommitted(int stampSequence);

	/**
	 *
	 * @param stampSequence a stamp sequence to create an analog of
	 * @return a stampSequence with a State of {@link State#INACTIVE}, but the
	 * same time, author, module, and path as the provided stamp sequence.
	 */
	int getRetiredStampSequence(int stampSequence);

	/**
	 *
	 * @param stampSequence a stamp sequence to create an analog of
	 * @return a stampSequence with a State of {@link State#ACTIVE}, but the same
	 * time, author, module, and path as the provided stamp sequence.
	 */
	int getActivatedStampSequence(int stampSequence);

	int getStampSequence(State status, long time,
			  int authorSequence, int moduleSequence, int pathSequence);

	String describeStampSequence(int stampSequence);

	IntStream getStampSequences();

	default Instant getInstantForStamp(int stampSequence) {
		return Instant.ofEpochMilli(getTimeForStamp(stampSequence));
	}

	/**
	 * Use to compare if versions may be unnecessary duplicates. If their content
	 * is equal, see if their stampSequences indicate a semantic difference
	 * (change in status, module, or path).
	 *
	 * @param stampSequence1
	 * @param stampSequence2
	 * @return true if stampSequences are equal without considering the author
	 * and time.
	 */
	boolean stampSequencesEqualExceptAuthorAndTime(int stampSequence1, int stampSequence2);

}
