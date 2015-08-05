package gov.vha.isaac.ochre.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class ConceptConstant {
	
	private String fsn_;
	private String preferredSynonym_;
	private List<String> synonyms_ = new ArrayList<>();
	private List<String> definitions_ = new ArrayList<>();
	private UUID uuid_;
	
	protected ConceptConstant(String fsn, String preferredSynonym, UUID uuid)
	{
		fsn_ = fsn;
		preferredSynonym_ = preferredSynonym;
		uuid_ = uuid;
	}
	
	protected ConceptConstant(String fsn, UUID uuid)
	{
		fsn_ = fsn;
		preferredSynonym_ = fsn_;
		uuid_ = uuid;
	}
	
	protected void addSynonym(String synonym)
	{
		synonyms_.add(synonym);
	}
	
	protected void addDefinition(String definition)
	{
		definitions_.add(definition);
	}
	
	
	/**
	 * @return  The FSN for this concept
	 */
	public String getFSN()
	{
		return fsn_;
	}
	
	/**
	 * @return  The preferred synonym for this concept
	 */
	public String getPreferredSynonym()
	{
		return preferredSynonym_;
	}
	
	/**
	 * @return  The alternate synonyms for this concept (if any) - does not include the preferred synonym.  Will not return null.
	 */
	public List<String> getSynonyms()
	{
		return synonyms_;
	}
	
	/**
	 * @return  The descriptions for this concept (if any).  Will not return null.
	 */
	public List<String> getDefinitions()
	{
		return definitions_;
	}
	
	/**
	 * @return  The UUID for the concept
	 */
	public UUID getUUID()
	{
		return uuid_;
	}
	
	/**
	 * @return  The nid for the concept.
	 */
	public int getNid()
	{
		return Get.conceptService().getConcept(getUUID()).getNid();
	}
	
	/**
	 * @return  The concept sequqnce for the concept.
	 */
	public int getSequence()
	{
		return Get.conceptService().getConcept(getUUID()).getConceptSequence();
	}
	
}
