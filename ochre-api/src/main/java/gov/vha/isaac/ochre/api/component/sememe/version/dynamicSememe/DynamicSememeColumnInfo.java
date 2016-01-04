/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe;

import java.util.UUID;

import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeFloat;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;


/**
 * {@link DynamicSememeColumnInfo}
 * 
 * A user friendly class for containing the information parsed out of the Assemblage concepts which defines the DynamicSememe.
 * See the class description for {@link DynamicSememeUsageDescriptionBI} for more details.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

public class DynamicSememeColumnInfo implements Comparable<DynamicSememeColumnInfo>
{
	private UUID columnDescriptionConceptUUID_;
	private transient String columnName_;
	private transient String columnDescription_;
	private int columnOrder_;
	private UUID assemblageConcept_;
	private DynamicSememeDataType columnDataType_;
	private DynamicSememeData defaultData_;
	private boolean columnRequired_;
	private DynamicSememeValidatorType[] validatorType_;
	private DynamicSememeData[] validatorData_;

	/**
	 * Useful for building up a new one step by step
	 */
	public DynamicSememeColumnInfo()
	{
	}
	
	/**
	 * calls {@link #DynamicSememeColumnInfo(UUID, int, UUID, DynamicSememeDataType, DynamicSememeDataBI, Boolean, DynamicSememeValidatorType[], DynamicSememeDataBI[])
	 * with a null assemblage concept, null validator info
	 */
	public DynamicSememeColumnInfo(int columnOrder, UUID columnDescriptionConcept, DynamicSememeDataType columnDataType, DynamicSememeData defaultData, Boolean columnRequired)
	{
		this(null, columnOrder, columnDescriptionConcept, columnDataType, defaultData, columnRequired, null, null); 
	}
	
	/**
	 * calls {@link #DynamicSememeColumnInfo(UUID, int, UUID, DynamicSememeDataType, DynamicSememeDataBI, Boolean, DynamicSememeValidatorType[], DynamicSememeDataBI[])
	 * with a null assemblage concept, and a single array item for the validator info
	 */
	public DynamicSememeColumnInfo(int columnOrder, UUID columnDescriptionConcept, DynamicSememeDataType columnDataType, DynamicSememeData defaultData, Boolean columnRequired,
			DynamicSememeValidatorType validatorType, DynamicSememeData validatorData)
	{
		this(null, columnOrder, columnDescriptionConcept, columnDataType, defaultData, columnRequired, 
				validatorType == null ? null : new DynamicSememeValidatorType[] {validatorType}, 
				validatorData == null ? null : new DynamicSememeData[] {validatorData});
	}
	
	/**
	 * calls {@link #DynamicSememeColumnInfo(UUID, int, UUID, DynamicSememeDataType, DynamicSememeDataBI, Boolean, DynamicSememeValidatorType, DynamicSememeDataBI)
	 * with a null assemblage concept
	 */
	public DynamicSememeColumnInfo(int columnOrder, UUID columnDescriptionConcept, DynamicSememeDataType columnDataType, DynamicSememeData defaultData, Boolean columnRequired,
			DynamicSememeValidatorType[] validatorType, DynamicSememeData[] validatorData)
	{
		this(null, columnOrder, columnDescriptionConcept, columnDataType, defaultData, columnRequired, validatorType, validatorData);
	}
	
	/**
	 * Create this object by reading the columnName and columnDescription from the provided columnDescriptionConcept.
	 * 
	 * If a suitable concept to use for the column Name/Description does not yet exist, see 
	 * {@link DynamicSememeColumnInfo#createNewDynamicSememeColumnInfoConcept(String, String)}
	 * 
	 * and pass the result in here.
	 * 
	 * @param assemblageConcept - the assemblage concept that this was read from (or null, if not yet part of an assemblage)
	 * @param columnOrder - the column order as defined in the assemblage concept
	 * @param columnDescriptionConcept - The concept where columnName and columnDescription should be read from
	 * @param columnDataType - the data type as defined in the assemblage concept
	 * @param defaultData - The type of this Object must align with the data type specified in columnDataType.  For example, 
	 * if columnDataType is set to {@link DynamicSememeDataType#FLOAT} then this field must be a {@link DynamicSememeFloat}.
	 * @param columnRequired - Is this column required when creating an instance of the refex?  True for yes, false or null for no.
	 * @param validatorType - The Validator to use when creating an instance of this Refex.  Null for no validator
	 * @param validatorData - The data required to execute the validatorType specified.  The format and type of this will depend on the 
	 * validatorType field.  See {@link DynamicSememeValidatorType} for details on the valid data for this field.  Should be null when validatorType is null. 
	 */
	public DynamicSememeColumnInfo(UUID assemblageConcept, int columnOrder, UUID columnDescriptionConcept, DynamicSememeDataType columnDataType, DynamicSememeData defaultData,
			Boolean columnRequired, DynamicSememeValidatorType[] validatorType, DynamicSememeData[] validatorData)
	{
		assemblageConcept_ = assemblageConcept;
		columnOrder_ = columnOrder;
		columnDescriptionConceptUUID_ = columnDescriptionConcept;
		columnDataType_ = columnDataType;
		defaultData_ = defaultData;
		columnRequired_ = (columnRequired == null ? false : columnRequired);
		validatorType_ = validatorType;
		validatorData_ = validatorData;
	}
	
	/**
	 * @param assemblageConcept - the assemblage concept that this was read from (or null, if not yet part of an assemblage)
	 */
	public void setAssemblageConcept(UUID assemblageConcept)
	{
		assemblageConcept_ = assemblageConcept;
	}
	
	/**
	 * @param columnOrder - the column order as defined in the assemblage concept
	 */
	public void setColumnOrder(int columnOrder)
	{
		columnOrder_ = columnOrder;
	}
	
	
	/**
	 @param columnDescriptionConcept - The concept where columnName and columnDescription should be read from
	 */
	public void setColumnDescriptionConcept(UUID columnDescriptionConcept)
	{
		columnDescriptionConceptUUID_ = columnDescriptionConcept;
		columnName_ = null;
		columnDescription_ = null;
	}
	
	/**
	 * @param columnDataType - the data type as defined in the assemblage concept
	 */
	public void setColumnDataType(DynamicSememeDataType columnDataType)
	{
		columnDataType_ = columnDataType;
	}
	
	/**
	 * @param defaultData - The type of this Object must align with the data type specified in columnDataType.  For example, 
	 * if columnDataType is set to {@link DynamicSememeDataType#FLOAT} then this field must be a {@link DynamicSememeFloat}.
	 */
	public void setColumnDefaultData(DynamicSememeData defaultData)
	{
		defaultData_ = defaultData;
	}
	
	/**
	 * @param columnRequired - Is this column required when creating an instance of the sememe?  True for yes, false or null for no.
	 */
	public void setColumnRequired(boolean columnRequired)
	{
		columnRequired_ = columnRequired;
	}
	
	/**
	 * @param validatorType - The Validator(s) to use when creating an instance of this sememe.  Null for no validator
	 */
	public void setValidatorType(DynamicSememeValidatorType[] validatorType)
	{
		validatorType_ = validatorType;
	}
	
	/**
	 * @param validatorType - The Validator(s) to use when creating an instance of this Refex.  Null for no validator
	 */
	public void setValidatorData(DynamicSememeData[] validatorData)
	{
		validatorData_ = validatorData;
	}
	
	/**
	 * @return The user-friendly name of this column of data.  To be used by GUIs to label the data in this column.
	 */
	public String getColumnName()
	{
		if (columnName_ == null)
		{
			read();
		}
		return columnName_;
	}

	/**
	 * @return The user-friendly description of this column of data.  To be used by GUIs to provide a more detailed explanation of 
	 * the type of data found in this column. 
	 */
	public String getColumnDescription()
	{
		if (columnDescription_ == null)
		{
			read();
		}
		return columnDescription_;
	}
	
	private void read()
	{
		DynamicSememeUtility util = LookupService.get().getService(DynamicSememeUtility.class);
		if (util == null)
		{
			columnName_ = "Unable to locate reader!";
			columnDescription_ = "Unable to locate reader!";
		}
		else
		{
			String[] temp = util.readDynamicSememeColumnNameDescription(columnDescriptionConceptUUID_);
			columnName_ = temp[0];
			columnDescription_ = temp[1];
		}
	}
	
	/**
	 * @return the UUID of the assemblage concept that this column data was read from
	 * or null in the case where this column is not yet associated with an assemblage.
	 */
	public UUID getAssemblageConcept()
	{
		return assemblageConcept_;
	}

	/**
	 * @return Defined the order in which the data columns will be stored, so that the column name / description can be aligned 
	 * with the {@link DynamicSememeData} columns in the {@link DynamicSememeVersionBI#getData(int)}.
	 * 
	 * Note, this value is 0 indexed (It doesn't start at 1)
	 */
	public int getColumnOrder()
	{
		return columnOrder_;
	}

	/**
	 * @return The defined data type for this column of the sememe.  Note that this value will be identical to the {@link DynamicSememeDataType} 
	 * returned by {@link DynamicSememeData} EXCEPT for cases where this returns {@link DynamicSememeDataType#POLYMORPHIC}.  In those cases, the 
	 * data type can only be determined by examining the actual member data in {@link DynamicSememeData}
	 */
	public DynamicSememeDataType getColumnDataType()
	{
		return columnDataType_;
	}
	
	/**
	 * @return the default value to use for this column, if no value is specified in a refex that is created using this column info
	 */
	public DynamicSememeData getDefaultColumnValue()
	{
		//Handle folks sending empty strings gracefully
		if (defaultData_ != null && defaultData_ instanceof DynamicSememeString && ((DynamicSememeString)defaultData_).getDataString().length() == 0)
		{
			return null;
		}
		return defaultData_;
	}

	/**
	 * @return When creating this refex, must this column be provided?
	 */
	public boolean isColumnRequired()
	{
		return columnRequired_;
	}
	
	/**
	 * @return The type of the validator(s) (if any) which must be used to validate user data before accepting the refex
	 */
	public DynamicSememeValidatorType[] getValidator()
	{
		return validatorType_;
	}
	
	/**
	 * @param validatorData - The data required to execute the validatorType specified.  The format and type of this will depend on the 
	 * validatorType field.  See {@link DynamicSememeValidatorType} for details on the valid data for this field.  Should be null when validatorType is null. 
	 */
	public DynamicSememeData[] getValidatorData()
	{
		return validatorData_;
	}
	
	/**
	 * @return The UUID of the concept where the columnName and columnDescription were read from.
	 */
	public UUID getColumnDescriptionConcept()
	{
		return columnDescriptionConceptUUID_;
	}
	
	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DynamicSememeColumnInfo o)
	{
		return Integer.compare(this.getColumnOrder(), o.getColumnOrder());
	}
}
