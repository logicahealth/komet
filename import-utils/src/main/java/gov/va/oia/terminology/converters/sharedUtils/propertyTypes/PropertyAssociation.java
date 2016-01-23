package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;

import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.model.constants.IsaacMetadataConstants;

public class PropertyAssociation extends Property
{
	private String associationInverseName_;
	private ObjectChronologyType associationComponentTypeRestriction_;
	private SememeType associationComponentTypeSubRestriction_;
	
	public PropertyAssociation(PropertyType owner, String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyAltName,
			String associationInverseName, String associationDescription, boolean disabled, 
			ObjectChronologyType associationComponentTypeRestriction, SememeType associationComponentTypeSubRestriction)
	{
		super(owner, sourcePropertyNameFSN, sourcePropertyPreferredName, sourcePropertyAltName, associationDescription, disabled, Integer.MAX_VALUE, 
				null);
		if (associationDescription == null)
		{
			throw new RuntimeException("association description is required");
		}
		associationInverseName_ = associationInverseName;
		associationComponentTypeRestriction_ = associationComponentTypeRestriction;
		associationComponentTypeSubRestriction_ = associationComponentTypeSubRestriction;
	}
	
	public PropertyAssociation(PropertyType owner, String sourcePropertyNameFSN, String sourcePropertyPreferredName, 
			String associationInverseName, String associationDescription, boolean disabled)
	{
		this(owner, sourcePropertyNameFSN, sourcePropertyPreferredName, null, associationInverseName, associationDescription, disabled, null, null);
	}

	
	public String getAssociationInverseName()
	{
		return associationInverseName_;
	}

	public ObjectChronologyType getAssociationComponentTypeRestriction()
	{
		return associationComponentTypeRestriction_;
	}

	public SememeType getAssociationComponentTypeSubRestriction()
	{
		return associationComponentTypeSubRestriction_;
	}

	@Override
	public DynamicSememeColumnInfo[] getDataColumnsForDynamicRefex()
	{
		DynamicSememeColumnInfo[] columns = new DynamicSememeColumnInfo[] {
				new DynamicSememeColumnInfo(0, IsaacMetadataConstants.DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT.getUUID(), 
						DynamicSememeDataType.UUID, null, false)};
		return columns;
	}
}
