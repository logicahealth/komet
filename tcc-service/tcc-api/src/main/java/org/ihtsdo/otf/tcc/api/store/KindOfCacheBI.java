package org.ihtsdo.otf.tcc.api.store;

import java.util.concurrent.CountDownLatch;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

public interface KindOfCacheBI {

	public abstract void setup(ViewCoordinate coordinate) throws Exception;

	public abstract boolean isKindOf(int childNid, int parentNid)
			throws Exception;
	
	public CountDownLatch getLatch();
        
        public void updateCache(ConceptChronicleBI c) throws Exception;
    

}