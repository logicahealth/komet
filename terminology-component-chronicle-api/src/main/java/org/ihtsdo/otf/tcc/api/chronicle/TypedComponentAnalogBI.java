package org.ihtsdo.otf.tcc.api.chronicle;

import java.beans.PropertyVetoException;
import org.ihtsdo.otf.tcc.api.AnalogBI;

public interface TypedComponentAnalogBI extends AnalogBI {

	public void setTypeNid(int nid) throws PropertyVetoException;

}
