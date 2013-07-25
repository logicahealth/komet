package org.ihtsdo.otf.tcc.api.constraint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

public class DescriptionConstraint implements ConstraintBI {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(conceptSpec);
        out.writeObject(descTypeSpec);
        out.writeUTF(text);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        	conceptSpec = (ConceptSpec) in.readObject();
        	descTypeSpec = (ConceptSpec) in.readObject();
        	text = in.readUTF();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    private ConceptSpec conceptSpec;
	private ConceptSpec descTypeSpec;
    private String text;
    
	public DescriptionConstraint(ConceptSpec conceptSpec,
			ConceptSpec descTypeSpec, String text) {
		super();
		this.conceptSpec = conceptSpec;
		this.descTypeSpec = descTypeSpec;
		this.text = text;
	}

	public ConceptSpec getConceptSpec() {
		return conceptSpec;
	}

	public ConceptSpec getDescTypeSpec() {
		return descTypeSpec;
	}

	public String getText() {
		return text;
	}

	public ConceptVersionBI getConcept(ViewCoordinate c) throws IOException {
		return conceptSpec.getStrict(c);
	}

	public ConceptVersionBI getDescType(ViewCoordinate c) throws IOException {
		return descTypeSpec.getStrict(c);
	}


	public int getConceptNid() throws IOException {
		return Ts.get().getNidForUuids(conceptSpec.getUuids());
	}

	public int getDescTypeNid() throws IOException {
		return Ts.get().getNidForUuids(descTypeSpec.getUuids());
	}

    

}
