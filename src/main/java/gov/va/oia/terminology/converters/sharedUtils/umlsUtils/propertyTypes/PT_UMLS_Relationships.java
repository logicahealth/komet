package gov.va.oia.terminology.converters.sharedUtils.umlsUtils.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Relations;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.Property;

/**
 * @author Daniel Armbrust
 */
public class PT_UMLS_Relationships extends BPT_Relations
{
	public Property UMLS_CUI;
	
	public PT_UMLS_Relationships()
	{
		super("UMLS");
		UMLS_CUI = addProperty("has_UMLS_CUI", null, "Relationship to link CUI and AUI identifiers from UMLS");
	}
}
