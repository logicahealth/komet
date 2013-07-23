package org.ihtsdo.otf.tcc.chronicle.cc.concept;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ComponentType;

public abstract class AttributeComparer {
	protected boolean comparerInitialized = false;
	protected ComponentType componentType = null;;
	
	protected ComponentType getComponentType() {
		return componentType;
	}
	
	public boolean isInitialized() {
		return comparerInitialized;
	}
	
	public void clear() { 
		comparerInitialized = false; 
	}

	abstract public void initializeAttributes(ComponentVersionBI v);
	abstract boolean hasSameAttributes(ComponentVersionBI v);
}

