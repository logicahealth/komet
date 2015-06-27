package org.ihtsdo.otf.tcc.api.description;

import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;

public interface DescriptionChronicleBI extends ComponentChronicleBI<DescriptionVersionBI>, 
        SememeChronology<DescriptionVersionBI>{
    
    int getConceptNid();
    
}
