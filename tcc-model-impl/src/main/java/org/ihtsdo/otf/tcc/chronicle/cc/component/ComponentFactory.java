package org.ihtsdo.otf.tcc.chronicle.cc.component;

import java.io.IOException;

import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptChronicle;

import com.sleepycat.bind.tuple.TupleInput;
import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.Revision;

public abstract class ComponentFactory<V extends Revision<V, C>, C extends ConceptComponent<V, C>> {
	
	public abstract C create(ConceptChronicle enclosingConcept, TupleInput input) throws IOException;

}
