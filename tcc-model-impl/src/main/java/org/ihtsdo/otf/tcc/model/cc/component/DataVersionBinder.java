package org.ihtsdo.otf.tcc.chronicle.cc.component;

import org.ihtsdo.otf.tcc.chronicle.cc.concept.OFFSETS;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class DataVersionBinder extends TupleBinding<Integer> {

	private static DataVersionBinder binder = new DataVersionBinder();
	
    public static DataVersionBinder getBinder() {
        return binder;
    }

	@Override
	public Integer entryToObject(TupleInput ti) {
		ti.skipFast(OFFSETS.DATA_VERSION.getOffset());
		return ti.readInt();
	}

	@Override
	public void objectToEntry(Integer arg0, TupleOutput to) {
		throw new UnsupportedOperationException();
	}

}

