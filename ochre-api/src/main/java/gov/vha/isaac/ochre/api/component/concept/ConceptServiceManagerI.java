package gov.vha.isaac.ochre.api.component.concept;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface ConceptServiceManagerI
{
	/**
	 * Return the currently configured ConceptService implementation - which may be Ochre model or OTF model
	 * @return The active ConceptService
	 */
	public ConceptService get();
}
