package org.ihtsdo.otf.tcc.datastore;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class GetNidData implements Callable<byte[]> {
	private int nid;
	private Database db;
	
	public GetNidData(int nid, Database db) {
		super();
		this.nid = nid;
		this.db = db;
	}

	@Override
	public byte[] call() throws IOException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		byte[] nidData;
		IntegerBinding.intToEntry(nid, key);
		try {
			if (db.get(null, key, data, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
				nidData = data.getData().clone();
			} else {
				nidData = new byte[0];
			}
		} catch (DatabaseException e) {
			throw new IOException(e);
		}
		return nidData;
	}
}

