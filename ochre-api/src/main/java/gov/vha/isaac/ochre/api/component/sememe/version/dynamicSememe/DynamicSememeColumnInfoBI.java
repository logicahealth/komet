package gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe;

import java.util.UUID;

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeFloatBI;

public interface DynamicSememeColumnInfoBI extends Comparable<DynamicSememeColumnInfoBI> {

	/**
	 * @param assemblageConcept - the assemblage concept that this was read from (or null, if not yet part of an assemblage)
	 */
	public void setAssemblageConcept(UUID assemblageConcept);

	/**
	 * @param columnOrder - the column order as defined in the assemblage concept
	 */
	public void setColumnOrder(int columnOrder);

	/**
	 @param columnDescriptionConcept - The concept where columnName and columnDescription should be read from
	 */
	public void setColumnDescriptionConcept(UUID columnDescriptionConcept);

	/**
	 * @param columnDataType - the data type as defined in the assemblage concept
	 */
	public void setColumnDataType(DynamicSememeDataType columnDataType);

	/**
	 * @param defaultData - The type of this Object must align with the data type specified in columnDataType.  For example, 
	 * if columnDataType is set to {@link DynamicSememeDataType#FLOAT} then this field must be a {@link DynamicSememeFloatBI}.
	 */
	public void setColumnDefaultData(DynamicSememeDataBI defaultData);

	/**
	 * @param columnRequired - Is this column required when creating an instance of the refex?  True for yes, false or null for no.
	 */
	public void setColumnRequired(boolean columnRequired);

	/**
	 * @param validatorType - The Validator to use when creating an instance of this Refex.  Null for no validator
	 */
	public void setValidatorType(DynamicSememeValidatorType validatorType);

	/**
	 * @param validatorType - The Validator to use when creating an instance of this Refex.  Null for no validator
	 */
	public void setValidatorData(DynamicSememeDataBI validatorData);

	/**
	 * @return The user-friendly name of this column of data.  To be used by GUIs to label the data in this column.
	 */
	public String getColumnName();

	/**
	 * @return The user-friendly description of this column of data.  To be used by GUIs to provide a more detailed explanation of 
	 * the type of data found in this column. 
	 */
	public String getColumnDescription();

	/**
	 * @return the UUID of the assemblage concept that this column data was read from
	 * or null in the case where this column is not yet associated with an assemblage.
	 */
	public UUID getAssemblageConcept();

	/**
	 * @return Defined the order in which the data columns will be stored, so that the column name / description can be aligned 
	 * with the {@link DynamicSememeDataBI} columns in the {@link DynamicSememeVersionBI#getData(int)}.
	 * 
	 * Note, this value is 0 indexed (It doesn't start at 1)
	 */
	public int getColumnOrder();

	/**
	 * @return The defined data type for this column of the Refex.  Note that this value will be identical to the {@link DynamicSememeDataType} 
	 * returned by {@link DynamicSememeDataBI} EXCEPT for cases where this returns {@link DynamicSememeDataType#POLYMORPHIC}.  In those cases, the 
	 * data type can only be determined by examining the actual member data in {@link DynamicSememeDataBI}
	 */
	public DynamicSememeDataType getColumnDataType();

	/**
	 * @return the default value to use for this column, if no value is specified in a refex that is created using this column info
	 */
	public DynamicSememeDataBI getDefaultColumnValue();

	/**
	 * @return When creating this refex, must this column be provided?
	 */
	public boolean isColumnRequired();

	/**
	 * @return The type of the validator (if any) which must be used to validate user data before accepting the refex
	 */
	public DynamicSememeValidatorType getValidator();

	/**
	 * @param validatorData - The data required to execute the validatorType specified.  The format and type of this will depend on the 
	 * validatorType field.  See {@link DynamicSememeValidatorType} for details on the valid data for this field.  Should be null when validatorType is null. 
	 */
	public DynamicSememeDataBI getValidatorData();

	/**
	 * @return The UUID of the concept where the columnName and columnDescription were read from.
	 */
	public UUID getColumnDescriptionConcept();

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(DynamicSememeColumnInfoBI o);

}