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
import gov.vha.isaac.ochre.api.chronicle.ChronicledConcept;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.IntStream;
import javafx.collections.ObservableList;
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
    
    void addUncommitted(ChronicledConcept cc);

    void addUncommittedNoChecks(ChronicledConcept cc);

    void cancel();

    void cancel(ChronicledConcept cc);

    void commit(String commitComment);

    void commit(ChronicledConcept cc, String commitComment);
    
    ObservableList<Integer> getUncommittedConceptNids();
    
    ObservableList<Alert> getAlertList();
    
    void addChangeChecker(ChangeChecker checker);

    void removeChangeChecker(ChangeChecker checker);
    
    long getCommitManagerSequence();

    long incrementAndGetSequence();
    
    int getAuthorSequenceForStamp(int stampSequence);
    
    int getModuleSequenceForStamp(int stampSequence);

    int getPathSequenceForStamp(int stampSequence);

    State getStatusForStamp(int stampSequence);

    long getTimeForStamp(int stampSequence);
    
    boolean isNotCanceled(int stampSequence);
    
    int getStamp(State status, long time, 
            int authorSequence, int moduleSequence, int pathSequence);
    
    String describeStampSequence(int stampSequence);
    
    IntStream getStampSequences();
    
    default Instant getInstantForStamp(int stampSequence) {
       return Instant.ofEpochMilli(getTimeForStamp(stampSequence));
    }

    
}
