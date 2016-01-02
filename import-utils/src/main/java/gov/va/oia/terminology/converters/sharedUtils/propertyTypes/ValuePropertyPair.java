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
package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;

import java.util.UUID;

public class ValuePropertyPair implements Comparable<ValuePropertyPair>
{
	private Property property_;
	private String value_;
	private Boolean valueDisabled_ = null;  //used for overriding the property default with instance data
	private UUID descriptionUUID_;
	
	
	public ValuePropertyPair(String value, UUID descriptionUUID, Property property)
	{
		value_ = value;
		property_ = property;
		descriptionUUID_ = descriptionUUID;
	}
	
	public ValuePropertyPair(String value, Property property)
	{
		value_ = value;
		property_ = property;
		descriptionUUID_ = null;
	}
	
	public Property getProperty()
	{
		return property_;
	}

	public String getValue()
	{
		return value_;
	}
	
	public UUID getUUID()
	{
		return descriptionUUID_;
	}
	
	public void setDisabled(boolean disabled)
	{
		valueDisabled_ = disabled;
	}
	
	/**
	 * Should this description instance be disabled, taking into account local override (if set) and falling back to property default.
	 * @return
	 */
	public boolean isDisabled()
	{
		if (valueDisabled_ != null)
		{
			return valueDisabled_;
		}
		else
		{
			return property_.isDisabled();
		}
	}

	@Override
	public int compareTo(ValuePropertyPair o)
	{
		int result = property_.getPropertyType().getClass().getName().compareTo(o.property_.getPropertyType().getClass().getName());
		if (result == 0)
		{
			result = property_.getPropertySubType() - o.property_.getPropertySubType();
			if (result == 0)
			{
				result = property_.getSourcePropertyNameFSN().compareTo(o.property_.getSourcePropertyNameFSN());
				if (result == 0)
				{
					result = value_.compareTo(o.value_);
				}
			}
		}
		return result;
	}
}
