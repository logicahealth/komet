package org.ihtsdo.otf.tcc.datastore;

import org.ihtsdo.otf.tcc.api.hash.Hashcode;

public class UncommittedStatusForPath {
	public int statusNid;
	public int pathNid;
	
	public UncommittedStatusForPath(int statusNid, int pathNid) {
		super();
		this.statusNid = statusNid;
		this.pathNid = pathNid;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UncommittedStatusForPath) {
			UncommittedStatusForPath other = (UncommittedStatusForPath) obj;
			if ((statusNid == other.statusNid) && (pathNid == other.pathNid)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Hashcode.compute(new int[] { statusNid, pathNid });
	}

}
