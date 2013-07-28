package org.ihtsdo.otf.tcc.model.cc.component;

import java.io.IOException;

import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;

import com.sleepycat.bind.tuple.TupleInput;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributesRevision;

public class ConceptAttributesFactory extends ComponentFactory<ConceptAttributesRevision, ConceptAttributes> {

	@Override
	public ConceptAttributes create(ConceptChronicle enclosingConcept, 
			TupleInput input) throws IOException {
		return new ConceptAttributes(enclosingConcept, input);
	}

}
