package org.ihtsdo.otf.tcc.model.cc.concept;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;

/**
 * Created by kec on 7/29/14.
 */
public interface ModificationTracker {
    void modified(ComponentChronicleBI component);

    void modified(ConceptComponent component, long sequence);
}
