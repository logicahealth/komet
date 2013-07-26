package org.ihtsdo.otf.tcc.api.conflict;

import java.util.Comparator;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;

public class VersionDateOrderSortComparator implements Comparator<ComponentVersionBI> {
    private boolean reverseOrder = false;

    public int compare(ComponentVersionBI o1, ComponentVersionBI o2) {
        if (reverseOrder) {
        	if (o2.getTime() - o1.getTime() > 0) {
        		return 1;
        	} else if (o2.getTime() - o1.getTime() < 0) {
        		return -1;
        	}
            return 0;
        } else {
        	if (o2.getTime() - o1.getTime() > 0) {
        		return -1;
        	} else if (o2.getTime() - o1.getTime() < 0) {
        		return 1;
        	}
            return 0;
        }
    }

    public VersionDateOrderSortComparator(boolean reverseOrder) {
        super();
        this.reverseOrder = reverseOrder;
    }
}
