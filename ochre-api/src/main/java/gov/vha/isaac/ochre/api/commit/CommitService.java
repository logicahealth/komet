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
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;

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

    Task<Void> cancel();

    Task<Void> cancel(ConceptChronology<?> chronicledConcept);

    Task<Void> cancel(SememeChronology<?> sememeChronicle);

    Task<Optional<CommitRecord>> commit(String commitComment);

    Task<Optional<CommitRecord>> commit(ConceptChronology<?> chronicledConcept, String commitComment);
    
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
     * @return a stampSequence with a State of INACTIVE, but the same
     * time, author, module, and pat as the provided stamp sequence. 
     */
    int getRetiredStampSequence(int stampSequence);
    
    int getStampSequence(State status, long time, 
            int authorSequence, int moduleSequence, int pathSequence);
    
    String describeStampSequence(int stampSequence);
    
    IntStream getStampSequences();
    
    default Instant getInstantForStamp(int stampSequence) {
       return Instant.ofEpochMilli(getTimeForStamp(stampSequence));
    }

    /**
     * Use to compare if versions may be unnecessary duplicates. If their
     * content is equal, see if their stampSequences indicate a semantic difference
     * (change in status, module, or path). 
     * @param stampSequence1
     * @param stampSequence2
     * @return true if stampSequences are equal without considering the author and time. 
     */
    boolean stampSequencesEqualExceptAuthorAndTime(int stampSequence1, int stampSequence2);

    
}
