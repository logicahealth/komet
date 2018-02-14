package sh.isaac.convert.mojo.cvx.propertyTypes;

import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.convert.mojo.cvx.CVXConstants;
import sh.isaac.convert.mojo.cvx.CVXFieldsV1;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Annotations;
import sh.isaac.converters.sharedUtils.propertyTypes.Property;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyType;

public class PT_Annotations extends BPT_Annotations
{
	public enum Attribute
	{
		CVXCode(CVXFieldsV1.CVXCode, true), VaccineStatus(CVXFieldsV1.VaccineStatus, DynamicDataType.STRING);

		private final Property property_;
		private final String key_;

		private Attribute(CVXFieldsV1 annotationConceptFsnColumn, DynamicDataType type)
		{
			// Don't know the owner yet - will be autofilled when we add this to the parent, below.
			key_ = annotationConceptFsnColumn.toString();
			property_ = new Property((PropertyType) null, annotationConceptFsnColumn.toString(), (String) null, (String) null, false, Integer.MAX_VALUE,
					new DynamicColumnInfo[] { new DynamicColumnInfo(null, 0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), type, null, true,
							null, null, true) });
		}

		private Attribute(CVXFieldsV1 fieldName, boolean isIdentifier)
		{
			// Don't know the owner yet - will be autofilled when we add this to the parent, below.
			key_ = fieldName.toString();
			property_ = new Property(fieldName.toString(), null, null, isIdentifier);
		}

		public Property getProperty()
		{
			return property_;
		}

		public String getKey()
		{
			return key_;
		}
	}

	public PT_Annotations()
	{
		super(CVXConstants.TERMINOLOGY_NAME);
		for (Attribute attr : Attribute.values())
		{
			addProperty(attr.getProperty());
		}
	}
}
