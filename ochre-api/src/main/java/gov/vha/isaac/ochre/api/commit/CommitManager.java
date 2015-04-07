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
    void addAlias(int stamp, int stampAlias, String aliasCommitComment);
    
    int[] getAliases(int stamp);
    
    void setComment(int stamp, String comment);
    
    Optional<String> getComment(int stamp);
    
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
    
    int getAuthorSequenceForStamp(int stamp);
    
    int getModuleSequenceForStamp(int stamp);

    int getPathSequenceForStamp(int stamp);

    State getStatusForStamp(int stamp);

    long getTimeForStamp(int stamp);
    
    boolean isNotCanceled(int stamp);
    
    int getStamp(State status, long time, 
            int authorSequence, int moduleSequence, int pathSequence);

}
