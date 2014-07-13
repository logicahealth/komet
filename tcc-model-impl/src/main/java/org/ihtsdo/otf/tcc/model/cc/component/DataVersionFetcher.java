package org.ihtsdo.otf.tcc.model.cc.component;

import org.ihtsdo.otf.tcc.model.cc.concept.OFFSETS;

import java.io.DataInputStream;
import java.io.IOException;

public class DataVersionFetcher  {

	public static int get(DataInputStream ti) throws IOException {
		ti.skip(OFFSETS.DATA_VERSION.getOffset());
		return ti.readInt();
	}

}

