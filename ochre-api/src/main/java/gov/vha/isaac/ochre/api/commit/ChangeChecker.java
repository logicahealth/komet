/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.commit;

import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import java.util.Collection;

/**
 *
 * @author kec
 */
public interface ChangeChecker extends Comparable<ChangeChecker> {
    
    void check(ConceptChronology cc, 
               Collection<Alert> alertCollection,
               CheckPhase checkPhase);
    
    void check(SememeChronology sc, 
               Collection<Alert> alertCollection,
               CheckPhase checkPhase);
    
}
