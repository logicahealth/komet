package org.ihtsdo.otf.tcc.api.cs;

import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationThreadingPolicy;

public enum ChangeSetWriterThreading {
    SINGLE_THREAD("single threaded"), MULTI_THREAD("multi-threaded");

    String displayString;

    private ChangeSetWriterThreading(String displayString) {
        this.displayString = displayString;
    }

    @Override
    public String toString() {
        return displayString;
    }

   public static ChangeSetWriterThreading
           get(ChangeSetGenerationThreadingPolicy csgtp) {
    	switch (csgtp) {
		case SINGLE_THREAD:
			return SINGLE_THREAD;
		case MULTI_THREAD:
			return MULTI_THREAD;
		default:
			throw new UnsupportedOperationException("Can't handle csgtp: " + csgtp);
		}
    }

    public ChangeSetGenerationThreadingPolicy convert() {
    	switch (this) {
		case SINGLE_THREAD:
			return ChangeSetGenerationThreadingPolicy.SINGLE_THREAD;
		case MULTI_THREAD:
			return ChangeSetGenerationThreadingPolicy.MULTI_THREAD;
		default:
			throw new UnsupportedOperationException("Can't handle csgtp: " + this);
		}
    }

}
