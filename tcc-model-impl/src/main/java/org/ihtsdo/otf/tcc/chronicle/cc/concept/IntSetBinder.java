package org.ihtsdo.otf.tcc.chronicle.cc.concept;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IntSetBinder extends TupleBinding<Set<Integer>> {

	@Override
	public ConcurrentSkipListSet<Integer> entryToObject(TupleInput input) {
		int size = input.readInt();
		List<Integer> setValues = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			setValues.add(input.readInt());
		}
		return new ConcurrentSkipListSet<>(setValues);
	}

	@Override
	public void objectToEntry(Set<Integer> set, TupleOutput output) {
		output.writeInt(set.size());
		for (int nid: set) {
			output.writeInt(nid);
		}
		
	}

}
