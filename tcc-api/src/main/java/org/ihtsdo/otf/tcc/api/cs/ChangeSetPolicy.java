package org.ihtsdo.otf.tcc.api.cs;

import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationPolicy;

public enum ChangeSetPolicy {
    /**
     * Don't generate change sets. 
     */
    OFF("no changeset"), 
    /**
     * Only include changes that represent the sapNids from the current commit. 
     */
    INCREMENTAL("incremental changeset"),
    /**
     * Only include sapNids that are written to the mutable database. 
     */
    MUTABLE_ONLY("mutable-only changeset"),
    /**
     * Include all changes. 
     */
    COMPREHENSIVE("comprehensive changeset");
    ;

    String displayString;

    private ChangeSetPolicy(String displayString) {
        this.displayString = displayString;
    }

    @Override
    public String toString() {
        return displayString;
    }
    
    public static ChangeSetPolicy get(ChangeSetGenerationPolicy csgp) {
    	switch (csgp) {
		case COMPREHENSIVE:
			return COMPREHENSIVE;
		case INCREMENTAL:
			return INCREMENTAL;
		case MUTABLE_ONLY: 
			return MUTABLE_ONLY;
		case OFF:
			return OFF;
		default:
			throw new UnsupportedOperationException("Can't handle csgp: " + csgp);
		}
    }

    public ChangeSetGenerationPolicy convert() {
    	switch (this) {
		case COMPREHENSIVE:
			return ChangeSetGenerationPolicy.COMPREHENSIVE;
		case INCREMENTAL:
			return ChangeSetGenerationPolicy.INCREMENTAL;
		case MUTABLE_ONLY: 
			return ChangeSetGenerationPolicy.MUTABLE_ONLY;
		case OFF:
			return ChangeSetGenerationPolicy.OFF;
		default:
			throw new UnsupportedOperationException("Can't handle csgp: " + this);
		}
    }

}
