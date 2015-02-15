package org.ihtsdo.otf.tcc.api.constraint;

public enum ConstraintCheckType {
	IGNORE, EQUALS, KIND_OF, REGEX;
	
	public static ConstraintCheckType get(String type) {
		if (type.equals("x")) {
			return IGNORE;
		}
		if (type.equals("e")) {
			return EQUALS;
		}
		if (type.equals("k")) {
			return KIND_OF;
		}
		if (type.equals("r")) {
			return REGEX;
		}
		throw new UnsupportedOperationException("Can't handle type: " + type);
	}
}
