package org.ihtsdo.otf.tcc.model.cc.component;

import java.io.IOException;

import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.component.ComponentFactory;

import com.sleepycat.bind.tuple.TupleInput;
import org.ihtsdo.otf.tcc.model.cc.relationship.Relationship;
import org.ihtsdo.otf.tcc.model.cc.relationship.RelationshipRevision;

public class RelationshipFactory extends ComponentFactory<RelationshipRevision, Relationship> {

	@Override
	public Relationship create(ConceptChronicle enclosingConcept, 
			TupleInput input) throws IOException {
		return new Relationship(enclosingConcept, 
				input);
	}

}
