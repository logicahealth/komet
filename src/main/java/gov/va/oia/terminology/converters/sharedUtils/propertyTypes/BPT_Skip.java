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

import java.util.List;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.PropertyType;


/**
 * Properties which have special handling during the conversion, and should not be loaded
 * the same way that other properties are handled.
 * @author Daniel Armbrust
 */
public abstract class BPT_Skip extends PropertyType
{
	public BPT_Skip(String description)
	{
		super(description);
	}
	
	protected void addSkipListEntries(List<String>[] skipLists)
	{
		if (skipLists != null)
		{
			for (List<String> skipList : skipLists)
			{
				if (skipList != null)
				{
					for (String s : skipList)
					{
						addProperty(s);
					}
				}
			}
		}
	}
}
