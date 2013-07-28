package org.ihtsdo.otf.tcc.model.cc.component;

import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponentBinder;
import org.ihtsdo.otf.tcc.model.cc.relationship.Relationship;
import org.ihtsdo.otf.tcc.model.cc.relationship.RelationshipRevision;

public class RelationshipBinder extends ConceptComponentBinder<RelationshipRevision, Relationship> {

	public static AtomicInteger encountered = new AtomicInteger();
	public static AtomicInteger written = new AtomicInteger();

	public RelationshipBinder() {
		super(new RelationshipFactory(), encountered, written);
	}

}
