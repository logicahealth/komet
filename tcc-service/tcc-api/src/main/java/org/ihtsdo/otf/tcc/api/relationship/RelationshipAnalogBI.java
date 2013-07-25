package org.ihtsdo.otf.tcc.api.relationship;

import java.beans.PropertyVetoException;

import org.ihtsdo.otf.tcc.api.chronicle.TypedComponentAnalogBI;

public interface RelationshipAnalogBI<A extends RelationshipAnalogBI>
        extends TypedComponentAnalogBI, RelationshipVersionBI<A> {


	public void setDestinationNid(int nid) throws PropertyVetoException;
	public void setRefinabilityNid(int nid) throws PropertyVetoException;
	public void setCharacteristicNid(int nid) throws PropertyVetoException;
	public void setGroup(int group) throws PropertyVetoException;

}
