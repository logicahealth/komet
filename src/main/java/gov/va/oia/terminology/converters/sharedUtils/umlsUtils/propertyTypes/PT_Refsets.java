package gov.va.oia.terminology.converters.sharedUtils.umlsUtils.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_MemberRefsets;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.Property;

/**
 * @author Daniel Armbrust
 */
public class PT_Refsets extends BPT_MemberRefsets
{
	public Property ALL;
	public Property CUI_CONCEPTS;
	public Property TERM_CONCEPTS;
	
	public PT_Refsets(String terminologyName)
	{
		super(terminologyName);
		//owner autofiled by addProperty call
		ALL = addProperty("All " + terminologyName + " Concepts");
		CUI_CONCEPTS = addProperty("All " + terminologyName + " CUI Concepts");
		TERM_CONCEPTS = addProperty("All " + terminologyName + " Code Concepts");
	}
}
