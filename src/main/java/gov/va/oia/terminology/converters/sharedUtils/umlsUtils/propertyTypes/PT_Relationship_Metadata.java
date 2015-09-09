package gov.va.oia.terminology.converters.sharedUtils.umlsUtils.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.PropertyType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;

/**
 * @author Daniel Armbrust
 */
public class PT_Relationship_Metadata extends PropertyType
{
	public PT_Relationship_Metadata()
	{
		super("Relationship Metadata", true, DynamicSememeDataType.UUID);
		indexByAltNames();
		addProperty("General Rel Type");
		addProperty("Inverse General Rel Type");
		addProperty("Snomed Code");
		addProperty("Inverse Snomed Code");
		//TODO replace this refex definition with one that stores two columns (source and target)
		addProperty("Source AUI and Target AUI", null, "sAUI & tAUI", "The Source AUI and Target AUI that defines the actual source of the relationship.  Source is first, Target is second - seperated by \" -> \".  Required since multiple AUI atoms are combined to create a single WB concept.");
	}
}
