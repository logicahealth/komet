package sh.isaac.misc.exporters.rf2.files;

public enum StandardFiles
{
	Association("Content", new char[] {'c'}, new String[] {"targetComponentId"}), 
	AttributeValue("Content", new char[] {'c'}, new String[] {"valueId"}), 
	Simple("Content", new char[0], new String[0]), 
	ExtendedMap("Map", new char[] {'i', 'i', 's', 's', 's', 'c', 'c'}, 
			new String[] {"mapGroup", "mapPriority", "mapRule", "mapAdvice", "mapTarget", "correlationId", "mapCategoryId"}), 
	SimpleMap("Map", new char[] {'s'}, new String[] {"mapTarget"}), 
	RefsetDescriptor("Metadata", new char[] {'c', 'c', 'i'}, new String[] {"attributeDescription", "attributeType", "attributeOrder"}), 
	DescriptionType("Metadata", new char[] {'c', 'i'}, new String[] {"descriptionFormat, descriptionLength"}), 
	ModuleDependency("Metadata", new char[] {'s', 's'}, new String[] {"sourceEffectiveTime", "targetEffectiveTime"}), 
	
	Concept, Identifier, Relationship, OWLExpression, StatedRelationship;
	
	
	protected final String subfolder;
	protected final char[] additionalColTypes;
	protected final String[] additionalColNames;
	
	private StandardFiles()
	{
		this.subfolder = null;
		this.additionalColNames = null;
		this.additionalColTypes = null;
	}
	
	private StandardFiles(String subfolder, char[] additionalColTypes, String[] additionalColNames)
	{
		this.subfolder = subfolder;
		this.additionalColNames = additionalColNames;
		this.additionalColTypes = additionalColTypes;
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