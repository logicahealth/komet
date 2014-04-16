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
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
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
	private UUID columnDescriptionConceptNid_;
	private transient String columnName_;
	private transient String columnDescription_;
	private int columnOrder_;
	private RefexDynamicDataType columnDataType_;

	public RefexDynamicColumnInfo(int columnOrder, UUID columnDescriptionConceptNid, RefexDynamicDataType columnDataType)
	{
		columnOrder_ = columnOrder;
		columnDescriptionConceptNid_ = columnDescriptionConceptNid;
		columnDataType_ = columnDataType;
	}

	/**
	 * The user-friendly name of this column of data.  To be used by GUIs to label the data in this column.
	 * @return the column name
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
	 * The user-friendly description of this column of data.  To be used by GUIs to provide a more detailed explanation of 
	 * the type of data found in this column. 
	 * @return the column description
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
	 * Defined the order in which the data columns will be stored, so that the column name / description can be aligned 
	 * with the {@link RefexDynamicDataBI} columns in the {@link RefexDynamicVersionBI#getData(int)}.
	 * @return
	 */
	public int getColumnOrder()
	{
		return columnOrder_;
	}

	/**
	 * The defined data type for this column of the Refex.  Note that this value will be identical to the {@link RefexDynamicDataType} 
	 * returned by {@link RefexDynamicDataBI} EXCEPT for cases where this returns {@link RefexDynamicDataType#POLYMORPHIC}.  In those cases, the 
	 * data type can only be determined by examining the actual member data in {@link RefexDynamicDataBI}
	 * @return
	 */
	public RefexDynamicDataType getColumnDataType()
	{
		return columnDataType_;
	}
	
	private void read()
	{
		//TODO implement
	}
}
