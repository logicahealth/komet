/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.commit;

import gov.vha.isaac.ochre.api.chronicle.ChronicledConcept;
import java.util.Collection;

/**
 *
 * @author kec
 */
public interface ChangeChecker {
    
    void check(ChronicledConcept cc, 
               Collection<Alert> alertCollection,
               CheckPhase checkPhase);
    
}
