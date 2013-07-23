package org.ihtsdo.otf.tcc.api.media;

import java.beans.PropertyVetoException;

import org.ihtsdo.otf.tcc.api.chronicle.TypedComponentAnalogBI;

public interface MediaAnalogBI<A extends MediaAnalogBI>
        extends TypedComponentAnalogBI, MediaVersionBI<A> {
	
    public void setTextDescription(String desc) throws PropertyVetoException;
    
}
