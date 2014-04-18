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
package org.ihtsdo.otf.tcc.api.refexDynamic.data;

import java.util.UUID;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;


/**
 * {@link RefexDynamicColumnInfo}
 * 
 * A user friendly class for containing the information parsed out of the Assemblage concepts which defines the RefexDynamic.
 * See the class description for {@link RefexDynamicUsageDescription} for more details.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamicColumnInfo
{
	private UUID columnDescriptionConceptUUID_;
	private transient String columnName_;
	private transient String columnDescription_;
	private int columnOrder_;
	private RefexDynamicDataType columnDataType_;
	private RefexDynamicDataBI defaultData_;

	/**
	 * Create this object by reading the columnName and columnDescription from the provided columnDescriptionConcept.
	 * 
	 * If a suitable concept to use for the column Name/Description does not yet exist, see 
	 * {@link RefexDynamicColumnInfo#createNewRefexDynamicColumnInfoConcept(String, String)}
	 * 
	 * and pass the result in here.
	 * 
	 * @param columnOrder
	 * @param columnDescriptionConceptNid
	 * @param columnDataType
	 * @param defaultData
	 */
	public RefexDynamicColumnInfo(int columnOrder, UUID columnDescriptionConcept, RefexDynamicDataType columnDataType, RefexDynamicDataBI defaultData)
	{
		columnOrder_ = columnOrder;
		columnDescriptionConceptUUID_ = columnDescriptionConcept;
		columnDataType_ = columnDataType;
		defaultData_ = defaultData;
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

	/**
	 * @return Defined the order in which the data columns will be stored, so that the column name / description can be aligned 
	 * with the {@link RefexDynamicDataBI} columns in the {@link RefexDynamicVersionBI#getData(int)}.
	 */
	public int getColumnOrder()
	{
		return columnOrder_;
	}

	/**
	 * @return The defined data type for this column of the Refex.  Note that this value will be identical to the {@link RefexDynamicDataType} 
	 * returned by {@link RefexDynamicDataBI} EXCEPT for cases where this returns {@link RefexDynamicDataType#POLYMORPHIC}.  In those cases, the 
	 * data type can only be determined by examining the actual member data in {@link RefexDynamicDataBI}
	 */
	public RefexDynamicDataType getColumnDataType()
	{
		return columnDataType_;
	}
	
	/**
	 * @return the default value to use for this column, if no value is specified in a refex that is created using this column info
	 */
	public RefexDynamicDataBI getDefaultColumnValue()
	{
		return defaultData_;
	}
	
	/**
	 * @return The UUID of the concept where the columnName and columnDescription were read from.
	 */
	public UUID getColumnDescriptionConcept()
	{
		return columnDescriptionConceptUUID_;
	}
	
	private void read()
	{
		//TODO [REFEX] implement
	}
	
	/**
	 * Create a new concept using the provided columnName and columnDescription values which is suitable 
	 * for use as a column descriptor within {@link RefexDynamicUsageDescription}.
	 * 
	 * The new concept will be created under the hierarchy //TODO determine concept
	 * 
	 * A complete usage pattern (where both the refex assemblage concept and the column name concept needs
	 * to be created) would look roughly like this:
	 * 
	 * RefexDynamicUsageDescription.createNewRefexDynamicUsageDescriptionConcept(
	 *     "The name of the Refex", 
	 *     "The description of the Refex",
	 *     new RefexDynamicColumnInfo[]{new RefexDynamicColumnInfo(
	 *         0,
	 *         RefexDynamicColumnInfo.createNewRefexDynamicColumnInfoConcept(
	 *             "column name",
	 *             "column description"
	 *             )
	 *         RefexDynamicDataType.STRING,
	 *         new RefexString("default value")
	 *         )}
	 *     )
	 * 
	 * //TODO figure out language details (how we know what language to put on the name/description
	 */
	public static UUID createNewRefexDynamicColumnInfoConcept(String columnName, String columnDescription)
	{
		//TODO [REFEX] implement
		return UUID.randomUUID();
	}

}
