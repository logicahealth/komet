package sh.isaac.misc.exporters.rf2.files;

import org.apache.commons.lang3.ArrayUtils;

class ConvenienceArrays
{
	protected static final String[] coreColNames = new String[] {"id", "effectiveTime", "active", "moduleId"};
	protected static final String[] refsetColNames = ArrayUtils.addAll(coreColNames, new String[] {"refsetId", "referencedComponentId"});
}

/**
 * This class primarily exists to aid the {@link RF2FileFetcher} code.  
 * You probably want to use that class..
 *
 */
public enum StandardFiles
{
	//Note, this does not contain all the files you typically see, due to files like description and textdefinition having languageCodes as part of the 
	//file definition.  See RF2FileFetcher for a way to get most of these files...
	
	Association("Content", ConvenienceArrays.refsetColNames, new char[] {'c'}, new String[] {"targetComponentId"}), 
	AttributeValue("Content", ConvenienceArrays.refsetColNames, new char[] {'c'}, new String[] {"valueId"}), 
	Simple("Content", ConvenienceArrays.refsetColNames, new char[0], new String[0]), 
	ExtendedMap("Map", ConvenienceArrays.refsetColNames, new char[] {'i', 'i', 's', 's', 's', 'c', 'c'}, 
			new String[] {"mapGroup", "mapPriority", "mapRule", "mapAdvice", "mapTarget", "correlationId", "mapCategoryId"}), 
	SimpleMap("Map", ConvenienceArrays.refsetColNames, new char[] {'s'}, new String[] {"mapTarget"}), 
	RefsetDescriptor("Metadata", ConvenienceArrays.refsetColNames, new char[] {'c', 'c', 'i'}, new String[] {"attributeDescription", "attributeType", "attributeOrder"}), 
	DescriptionType("Metadata", ConvenienceArrays.refsetColNames, new char[] {'c', 'i'}, new String[] {"descriptionFormat, descriptionLength"}), 
	ModuleDependency("Metadata", ConvenienceArrays.refsetColNames, new char[] {'s', 's'}, new String[] {"sourceEffectiveTime", "targetEffectiveTime"}),
	
	Concept(ArrayUtils.addAll(ConvenienceArrays.coreColNames, new String[] {"definitionStatusId"})), 
	Identifier(new String[] {"identifierSchemeId", "alternateIdentifier", "effectiveTime", "active", "moduleId", "referencedComponentId"}), 
	OWLExpression(ArrayUtils.addAll(ConvenienceArrays.coreColNames, new String[] {"refsetId", "referencedComponentId", "owlExpression"})),
	StatedRelationship(ArrayUtils.addAll(ConvenienceArrays.coreColNames, new String[] {"sourceId", "destinationId", "relationshipGroup", "typeId", "characteristicTypeId", "modifierId"})),
	Relationship(ArrayUtils.addAll(ConvenienceArrays.coreColNames, new String[] {"sourceId", "destinationId", "relationshipGroup", "typeId", "characteristicTypeId", "modifierId"})),
	
	CustomRefset(ConvenienceArrays.refsetColNames),
	CustomTextFile(ArrayUtils.addAll(ConvenienceArrays.coreColNames, new String[] {"conceptId", "languageCode", "typeId", "term", "caseSignificanceId"}));
	
	protected final String subfolder;
	private final String[] baseColNames;
	protected final char[] additionalColTypes;
	private final String[] additionalColNames;
	
	private StandardFiles(String[] baseColNames)
	{
		this.subfolder = null;
		this.baseColNames = baseColNames;
		this.additionalColNames = null;
		this.additionalColTypes = null;
	}
	
	private StandardFiles(String subfolder, String[] baseColNames, char[] additionalColTypes, String[] additionalColNames)
	{
		this.subfolder = subfolder;
		this.baseColNames = baseColNames;
		this.additionalColNames = additionalColNames;
		this.additionalColTypes = additionalColTypes;
	}
	
	protected String[] getAllColNames()
	{
		int count = 0;
		if (baseColNames != null)
		{
			count += baseColNames.length; 
		}
		if (additionalColNames != null)
		{
			count += additionalColNames.length;
		}
		String[] result = new String[count];
		int i = 0;
		if (baseColNames != null)
		{
			for (String s : baseColNames)
			{
				result[i++] = s;
			}
		}
		if (additionalColNames != null)
		{
			for (String s : additionalColNames)
			{
				result[i++] = s;
			}
		}
		return result;
	}
	
	public static StandardFiles parse(String name)
	{
		for (StandardFiles sf : StandardFiles.values())
		{
			if (sf.name().toLowerCase().equals(name.toLowerCase()))
			{
				return sf;
			}
		}
		return null;
	}
}