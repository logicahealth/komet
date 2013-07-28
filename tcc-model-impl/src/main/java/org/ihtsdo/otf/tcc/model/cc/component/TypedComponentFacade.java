package org.ihtsdo.otf.tcc.chronicle.cc.component;

import java.beans.PropertyVetoException;
import org.ihtsdo.otf.tcc.api.AnalogBI;

public interface TypedComponentFacade extends AnalogBI {

	public void setTypeNid(int nid) throws PropertyVetoException;

}
