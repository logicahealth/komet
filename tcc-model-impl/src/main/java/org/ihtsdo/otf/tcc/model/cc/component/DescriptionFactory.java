package org.ihtsdo.otf.tcc.model.cc.component;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.component.ComponentFactory;

import org.ihtsdo.otf.tcc.model.cc.description.Description;
import org.ihtsdo.otf.tcc.model.cc.description.DescriptionRevision;

public class DescriptionFactory extends ComponentFactory<DescriptionRevision, Description> {

	@Override
	public Description create(ConceptChronicle enclosingConcept, DataInputStream input) throws IOException {
		return new Description(enclosingConcept, input);
	}

}
