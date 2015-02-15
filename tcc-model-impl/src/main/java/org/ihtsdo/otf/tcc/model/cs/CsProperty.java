/**
 * 
 */
package org.ihtsdo.otf.tcc.model.cs;

public enum CsProperty {
	LAST_CHANGE_SET_WRITTEN, LAST_CHANGE_SET_READ;
	
	public String toString() {
		return this.getClass().getCanonicalName() + "." + name();
	};
}