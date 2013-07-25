package org.ihtsdo.otf.tcc.api.contradiction;

public class ContradictionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ContradictionException() {
		super();
	}

	public ContradictionException(String message) {
		super(message);
	}

	public ContradictionException(Throwable cause) {
		super(cause);
	}

	public ContradictionException(String message, Throwable cause) {
		super(message, cause);
	}

}
