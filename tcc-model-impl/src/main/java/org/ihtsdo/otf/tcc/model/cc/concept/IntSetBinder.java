package org.ihtsdo.otf.tcc.model.cc.concept;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class IntSetBinder {


	public ConcurrentSkipListSet<Integer> entryToObject(DataInputStream input) throws IOException {
		int size = input.readInt();
		List<Integer> setValues = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			setValues.add(input.readInt());
		}
		return new ConcurrentSkipListSet<>(setValues);
	}


	public void objectToEntry(Set<Integer> set, DataOutputStream output) throws IOException {
		output.writeInt(set.size());
		for (int nid: set) {
			output.writeInt(nid);
		}
		
	}

}
