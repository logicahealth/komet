/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.commit;

import gov.vha.isaac.ochre.api.chronicle.ChronicledConcept;
import gov.vha.isaac.ochre.api.sememe.SememeChronicle;
import java.util.Collection;

/**
 *
 * @author kec
 */
public interface ChangeChecker extends Comparable<ChangeChecker> {
    
    void check(ChronicledConcept cc, 
               Collection<Alert> alertCollection,
               CheckPhase checkPhase);
    
    void check(SememeChronicle sc, 
               Collection<Alert> alertCollection,
               CheckPhase checkPhase);
    
}
