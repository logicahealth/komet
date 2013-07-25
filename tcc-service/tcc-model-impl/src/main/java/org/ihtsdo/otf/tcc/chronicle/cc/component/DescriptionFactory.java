package org.ihtsdo.otf.tcc.chronicle.cc.component;

import java.io.IOException;

import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.chronicle.cc.component.ComponentFactory;

import com.sleepycat.bind.tuple.TupleInput;
import org.ihtsdo.otf.tcc.chronicle.cc.description.Description;
import org.ihtsdo.otf.tcc.chronicle.cc.description.DescriptionRevision;

public class DescriptionFactory extends ComponentFactory<DescriptionRevision, Description> {

	@Override
	public Description create(ConceptChronicle enclosingConcept, TupleInput input) throws IOException {
		return new Description(enclosingConcept, input);
	}

}
