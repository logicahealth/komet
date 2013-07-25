/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.ihtsdo.otf.tcc.api.conflict;

import java.util.Comparator;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;

class PartDateOrderSortComparator implements Comparator<ComponentVersionBI> {
    private boolean reverseOrder = false;

    @Override
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

    public PartDateOrderSortComparator(boolean reverseOrder) {
        super();
        this.reverseOrder = reverseOrder;
    }
}
