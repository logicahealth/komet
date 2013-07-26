package org.ihtsdo.otf.tcc.chronicle.cc.media;

import java.beans.PropertyVetoException;

import org.ihtsdo.otf.tcc.chronicle.cc.component.TypedComponentFacade;
import org.ihtsdo.otf.tcc.api.media.MediaAnalogBI;

public interface MediaVersionFacade
        extends TypedComponentFacade, MediaAnalogBI<MediaRevision> {
	
    void setTextDescription(String desc) throws PropertyVetoException;
    
    @Override
    MediaRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid);
    
}
