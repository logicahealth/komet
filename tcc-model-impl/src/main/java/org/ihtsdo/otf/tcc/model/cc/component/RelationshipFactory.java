package org.ihtsdo.otf.tcc.model.cc.component;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.relationship.Relationship;
import org.ihtsdo.otf.tcc.model.cc.relationship.RelationshipRevision;

public class RelationshipFactory extends ComponentFactory<RelationshipRevision, Relationship> {

	@Override
	public Relationship create(ConceptChronicle enclosingConcept, 
			DataInputStream input) throws IOException {
		return new Relationship(enclosingConcept, 
				input);
	}

}
