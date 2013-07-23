package org.ihtsdo.otf.tcc.api;

import org.ihtsdo.otf.tcc.api.coordinate.Status;

@Deprecated
public interface AnalogGeneratorBI <T extends AnalogBI> {

	T makeAnalog(Status status, long time, int authorNid, int moduleNid, int pathNid);
}
