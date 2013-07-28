package org.ihtsdo.otf.tcc.model.cc.component;

import java.io.IOException;

import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.component.ComponentFactory;

import com.sleepycat.bind.tuple.TupleInput;
import org.ihtsdo.otf.tcc.model.cc.description.Description;
import org.ihtsdo.otf.tcc.model.cc.description.DescriptionRevision;

public class DescriptionFactory extends ComponentFactory<DescriptionRevision, Description> {

	@Override
	public Description create(ConceptChronicle enclosingConcept, TupleInput input) throws IOException {
		return new Description(enclosingConcept, input);
	}

}
