package org.ihtsdo.otf.tcc.api;

import org.ihtsdo.otf.tcc.api.coordinate.Status;
import java.beans.PropertyVetoException;

@Deprecated
public interface AnalogBI {

    void setNid(int nid) throws PropertyVetoException;
    void setStatus(Status nid) throws PropertyVetoException;
    void setAuthorNid(int nid) throws PropertyVetoException;
    void setModuleNid(int nid) throws PropertyVetoException;
    void setPathNid(int nid) throws PropertyVetoException;
    void setTime(long time) throws PropertyVetoException;
}
