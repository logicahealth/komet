package gov.vha.isaac.ochre.api.constants;

import java.util.UUID;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;

public class MetadataDynamicSememeConstant extends MetadataConceptConstant
{
	private String dynamicSememeDescription_;
	private DynamicSememeColumnInfo[] dynamicSememeColumns_;
	private ObjectChronologyType referencedComponentRestriction_;
	private SememeType referencedComponentSubRestriction_;

	/**
	 * @param fsn
	 * @param preferredSynonym
	 * @param uuid - optional - the UUID to assign to this sememe
	 * @param sememeDescription - describe the purpose of the use of this dynamic sememe
	 * @param columns - The definitions of the attached data columns that are allowed on this sememe (may be empty)
	 */
	protected MetadataDynamicSememeConstant(String fsn, String preferredSynonym, UUID uuid, String sememeDescription, DynamicSememeColumnInfo[] columns)
	{
		this(fsn, preferredSynonym, uuid, sememeDescription, columns, null, null, null, null);
	}
	
	/**
	 * @param fsn
	 * @param uuid - optional - the UUID to assign to this sememe
	 * @param sememeDescription - describe the purpose of the use of this dynamic sememe
	 * @param columns - The definitions of the attached data columns that are allowed on this sememe (may be empty)
	 */
	public MetadataDynamicSememeConstant(String fsn, UUID uuid, String sememeDescription, DynamicSememeColumnInfo[] columns)
	{
		this(fsn, null, uuid, sememeDescription, columns, null, null, null, null);
	}
	
	/**
	 * @param fsn
	 * @param uuid - optional - the UUID to assign to this sememe
	 * @param sememeDescription - describe the purpose of the use of this dynamic sememe
	 * @param columns - The definitions of the attached data columns that are allowed on this sememe (may be empty)
	 * @param synonyms - optional - extra synonyms
	 */
	public MetadataDynamicSememeConstant(String fsn, UUID uuid, String sememeDescription, DynamicSememeColumnInfo[] columns,
			String[] synonyms)
	{
		this(fsn, null, uuid, sememeDescription, columns, synonyms, null, null, null);
	}
	
	/**
	 * @param fsn
	 * @param uuid - optional - the UUID to assign to this sememe
	 * @param sememeDescription - describe the purpose of the use of this dynamic sememe
	 * @param columns - The definitions of the attached data columns that are allowed on this sememe (may be empty)
	 * @param synonyms - optional - extra synonyms
	 * @param definitions - optional - extra definitions
	 * @param requiresIndex - optional - used to specify that this particular DynamicSememe should always be indexed.  If null or empty - no indexing will 
	 * be performed.  The Integer array should be something like "new Integer[]{0, 2, 3}" - where the 0 indexed values correspond to the columns that 
	 * should also be indexed.
	 */
	public MetadataDynamicSememeConstant(String fsn, UUID uuid, String sememeDescription, DynamicSememeColumnInfo[] columns,
			String[] synonyms, String[] definitions, Integer[] requiresIndex)
	{
		this(fsn, null, uuid, sememeDescription, columns, synonyms, definitions, null, null);
	}
	
	/**
	 * @param fsn
	 * @param preferredSynonym
	 * @param uuid - optional - the UUID to assign to this sememe
	 * @param sememeDescription - describe the purpose of the use of this dynamic sememe
	 * @param columns - The definitions of the attached data columns that are allowed on this sememe (may be empty)
	 * @param synonyms - optional - extra synonyms
	 * @param definitions - optional - extra definitions
	 * @param referencedComponentRestriction - optional - used to limit the type of nid that can be used as the referenced component in an instance
	 * of this sememe.
	 * @param referencedComponentSubRestriction - optional - used to limit the type of sememe that can be used as the referenced component in an instance
	 * of this sememe.
	 */
	public MetadataDynamicSememeConstant(String fsn, String preferredSynonym, UUID uuid, String sememeDescription, DynamicSememeColumnInfo[] columns,
			String[] synonyms, String[] definitions, ObjectChronologyType referencedComponentRestriction, SememeType refererenceComponentSubRestriction)
	{
		super(fsn, preferredSynonym, uuid);
		if (definitions != null)
		{
			for (String s : definitions)
			{
				addDefinition(s);
			}
		}
		if (synonyms!= null)
		{
			for (String s : synonyms)
			{
				addSynonym(s);
			}
		}
		dynamicSememeDescription_ = sememeDescription;
		dynamicSememeColumns_ = columns;
		referencedComponentRestriction_ = referencedComponentRestriction;
		referencedComponentSubRestriction_ = refererenceComponentSubRestriction;
	}
	
	/**
	 * @return the dynamicSememeDescription_
	 */
	public String getSememeAssemblageDescription()
	{
		return dynamicSememeDescription_;
	}

	/**
	 * @return the sememeColumns
	 */
	public DynamicSememeColumnInfo[] getDynamicSememeColumns()
	{
		return dynamicSememeColumns_;
	}
	
	/**
	 * @return The limit (if any) on which {@link ObjectChronologyType} this DynamicSememe is restricted to.
	 */
	public ObjectChronologyType getReferencedComponentTypeRestriction()
	{
		return referencedComponentRestriction_;
	}
	
	/**
	 * @return The limit (if any) on which {@link SememeType} this dynamic sememe is restricted to.
	 */
	public SememeType getReferencedComponentSubTypeRestriction()
	{
		return referencedComponentSubRestriction_;
	}
}
