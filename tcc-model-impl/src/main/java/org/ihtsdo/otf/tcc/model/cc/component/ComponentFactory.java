package org.ihtsdo.otf.tcc.model.cc.component;

import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

public abstract class ComponentFactory<V extends Revision<V, C>, C extends ConceptComponent<V, C>> {
	
	public abstract C create(ConceptChronicle enclosingConcept, DataInputStream input) throws IOException;

}
