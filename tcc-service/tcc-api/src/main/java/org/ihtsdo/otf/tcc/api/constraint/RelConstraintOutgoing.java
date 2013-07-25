package org.ihtsdo.otf.tcc.api.constraint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

public class RelConstraintOutgoing extends RelConstraint {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }
	public RelConstraintOutgoing(ConceptSpec originSpec,
			ConceptSpec relTypeSpec, ConceptSpec destinationSpec) {
		super(originSpec, relTypeSpec, destinationSpec);
	}

}
