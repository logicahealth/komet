package org.ihtsdo.otf.tcc.chronicle.cc.concept;

import java.util.Comparator;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;

public class ComponentComparator implements Comparator<ComponentChronicleBI> {

    @Override
    public int compare(ComponentChronicleBI o1, ComponentChronicleBI o2) {
        return o1.getNid() - o2.getNid();
    }
}
