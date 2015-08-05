package gov.vha.isaac.ochre.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class ConceptConstantGroup extends ConceptConstant{
	
	private List<ConceptConstant> children_ = new ArrayList<>();
	
	protected ConceptConstantGroup(String fsn, String preferredSynonym, UUID uuid) 
	{
		super(fsn, preferredSynonym, uuid);
	}
	
	protected ConceptConstantGroup(String fsn, UUID uuid) 
	{
		super(fsn, fsn, uuid);
	}
	
	protected void addChild(ConceptConstant child)
	{
		children_.add(child);
	}
	
	
	/**
	 * @return The constants that should be created under this constant in the taxonomy (if any).  Will not return null.
	 */
	public List<ConceptConstant> getChildren()
	{
		return children_;
	}
}
