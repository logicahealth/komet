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
	}
}
