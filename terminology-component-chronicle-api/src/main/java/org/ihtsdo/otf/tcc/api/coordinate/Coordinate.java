package org.ihtsdo.otf.tcc.api.coordinate;

import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerBI;


public class Coordinate {
	private Precedence precedence;
	private PositionSetBI positionSet;
	private NidSetBI   allowedStatusNids;
	private NidSetBI   isaTypeNids;
	private ContradictionManagerBI contradictionManager;
	private int languageNid;

	public Coordinate(Precedence precedence, PositionSetBI positionSet,
			NidSetBI allowedStatusNids, NidSetBI isaTypeNids, 
			ContradictionManagerBI contradictionManager, 
			int languageNid) {
		super();
		assert precedence != null;
		assert positionSet != null;
		assert allowedStatusNids != null;
		assert isaTypeNids != null;
		assert contradictionManager != null;
		this.precedence = precedence;
		this.positionSet = positionSet;
		this.allowedStatusNids = allowedStatusNids;
		this.isaTypeNids = isaTypeNids;
		this.contradictionManager = contradictionManager;
		this.languageNid = languageNid;
	}
	
	public PositionSetBI getPositionSet() {
		return positionSet;
	}

	public NidSetBI getAllowedStatusNids() {
		return allowedStatusNids;
	}

	public Precedence getPrecedence() {
		return precedence;
	}
	
	public NidSetBI getIsaTypeNids() {
		return isaTypeNids;
	}

	public ContradictionManagerBI getContradictionManager() {
		return contradictionManager;
	}
	
	public int getLanguageNid() {
		return languageNid;
	}

}
