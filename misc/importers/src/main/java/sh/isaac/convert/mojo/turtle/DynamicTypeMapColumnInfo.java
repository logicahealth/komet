/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
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
package sh.isaac.convert.mojo.turtle;

import java.util.UUID;

/**
 * Code to help in mapping anonymous constructs in owl into dynamic semantics
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class DynamicTypeMapColumnInfo
{
	private DynamicTypeMap dtm;
	private boolean moreThanOneValue = false;
	private boolean columnRequired = true;
	private UUID columnLabelConcept;
	private String uri;
	
	public DynamicTypeMapColumnInfo(String uri, DynamicTypeMap dynamicTypeMap, boolean moreThanOneValue, boolean required, UUID columnLabelConcept)
	{
		this.uri = uri;
		this.dtm = dynamicTypeMap;
		this.moreThanOneValue = moreThanOneValue;
		this.columnLabelConcept = columnLabelConcept;
		this.columnRequired = required;
	}
	
	public void setMoreThanOneValue()
	{
		this.moreThanOneValue = true;
	}
	
	public void setColumnNotRequired()
	{
		this.columnRequired = false;
	}
	
	public DynamicTypeMap getDynamicTypeMap()
	{
		return this.dtm;
	}
	
	public boolean hasMoreThanOneValue()
	{
		return moreThanOneValue;
	}
	
	public UUID getColumnLabelConcept()
	{
		return columnLabelConcept;
	}
	
	public boolean columnRequired()
	{
		return columnRequired;
	}
	
	public String getURI()
	{
		return uri;
	}
}
