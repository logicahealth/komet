package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;

import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;

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
		associationInverseName_ = associationInverseName;
		associationComponentTypeRestriction_ = associationComponentTypeRestriction;
		associationComponentTypeSubRestriction_ = associationComponentTypeSubRestriction;
	}
	
	public PropertyAssociation(PropertyType owner, String sourcePropertyNameFSN, String sourcePropertyPreferredName, 
			String associationInverseName, String associationDescription, boolean disabled)
	{
		this(owner, sourcePropertyNameFSN, sourcePropertyPreferredName, null, associationInverseName, associationDescription, disabled, null, null);
	}

	
	protected String getAssociationInverseName()
	{
		return associationInverseName_;
	}

	protected ObjectChronologyType getAssociationComponentTypeRestriction()
	{
		return associationComponentTypeRestriction_;
	}

	protected SememeType getAssociationComponentTypeSubRestriction()
	{
		return associationComponentTypeSubRestriction_;
	}
	
}
