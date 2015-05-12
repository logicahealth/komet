/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.component.concept;

import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.component.concept.description.ConceptDescriptionChronicle;
import java.util.List;

/**
 *
 * @author kec
 * @param <V>
  */
public interface ConceptChronology<V extends StampedVersion> extends ObjectChronology<V> {
    
    int getConceptSequence();
    
    List<? extends ConceptDescriptionChronicle> getConceptDescriptionList();
}
