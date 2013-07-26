package org.ihtsdo.otf.tcc.api.conattr;

import java.beans.PropertyVetoException;

import org.ihtsdo.otf.tcc.api.AnalogBI;

public interface ConceptAttributeAnalogBI<A extends ConceptAttributeAnalogBI>
        extends AnalogBI, ConceptAttributeVersionBI<A> {
	
    public void setDefined(boolean defined) throws PropertyVetoException;

}
