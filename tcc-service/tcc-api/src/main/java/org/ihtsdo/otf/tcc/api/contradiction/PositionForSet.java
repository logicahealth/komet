/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ihtsdo.otf.tcc.api.contradiction;


import org.ihtsdo.otf.tcc.api.hash.Hashcode;

public class PositionForSet implements Comparable<PositionForSet> {
    long time;
    int pathNid;

    public PositionForSet(long time, int pathNid) {
        super();
        this.time = time;
        this.pathNid = pathNid;
    }

    @Override
    public boolean equals(Object obj) {
        if (PositionForSet.class.isAssignableFrom(obj.getClass())) {
            PositionForSet another = (PositionForSet) obj;
            return another.time == time && another.pathNid == pathNid;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Hashcode.compute(new int[] { (int) time + pathNid } );
    }

    public long getTime() {
        return time;
    }

    public int getPathNid() {
        return pathNid;
    }

	@Override
	public int compareTo(PositionForSet o) {
		if (this.time == o.time && this.pathNid == o.pathNid) {
			return 0;
		} else if (this.time == o.time) {
			// If time same, use path Id
			if (this.pathNid < o.pathNid) {
				return -1;
			} else { 
				return 1;
			}
		} else {
			// If pathNid is same or different, use the time comparison for the method's result
			if (this.time < o.time) {
				return -1;
			} else { 
				return 1;
			}
		} 
	}
}
