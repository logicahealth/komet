package org.ihtsdo.otf.tcc.api.relationship.group;

import java.util.Collection;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;

public interface RelGroupChronicleBI extends ComponentChronicleBI<RelGroupVersionBI> {
	
	public Collection<? extends RelationshipChronicleBI> getRels();
	
	public int getRelGroup();
}
