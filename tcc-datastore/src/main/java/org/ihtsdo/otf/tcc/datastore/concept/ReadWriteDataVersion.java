package org.ihtsdo.otf.tcc.datastore.concept;

import java.io.IOException;

import org.ihtsdo.otf.tcc.datastore.Bdb;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.OFFSETS;

public class ReadWriteDataVersion {
	public static long get(int nid) throws IOException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		data.setPartial(OFFSETS.DATA_VERSION.getOffset(), OFFSETS.DATA_VERSION.getBytes(), true);
		LongBinding.longToEntry(nid, key);
		try {
			if (Bdb.getConceptDb().getReadWrite().get(null, key, data, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
				return LongBinding.entryToLong(data);
			} else {
				return Long.MIN_VALUE;
			}
		} catch (DatabaseException e) {
			throw new IOException(e);
		}
	}
}
