package org.ihtsdo.otf.tcc.api.db;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class EccsDependency extends DbDependency {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        	//
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }
    
	public EccsDependency(String name, String sizeInBytes) {
		super(name, sizeInBytes);
	}
	
	public String getName() {
		return getKey();
	}

	public String getSizeInBytes() {
		return getValue();
	}

	@Override
	public boolean satisfactoryValue(String value) {
		if (value == null) {
			return false;
		}
		Long dependencySize = Long.valueOf(getSizeInBytes());
		Long comparisonSize = Long.valueOf(value);
		return comparisonSize >= dependencySize;
	}

}
