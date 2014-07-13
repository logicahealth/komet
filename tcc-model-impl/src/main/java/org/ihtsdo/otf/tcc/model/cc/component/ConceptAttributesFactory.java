package org.ihtsdo.otf.tcc.model.cc.component;

import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributesRevision;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

public class ConceptAttributesFactory extends ComponentFactory<ConceptAttributesRevision, ConceptAttributes> {

	@Override
	public ConceptAttributes create(ConceptChronicle enclosingConcept,
                                    DataInputStream input) throws IOException {
		return new ConceptAttributes(enclosingConcept, input);
	}

}
