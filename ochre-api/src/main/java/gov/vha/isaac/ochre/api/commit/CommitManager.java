/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.commit;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.ChronicledConcept;
import java.util.Optional;
import javafx.collections.ObservableList;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface CommitManager {
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

}
