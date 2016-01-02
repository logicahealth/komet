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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.oia.terminology.converters.sharedUtils.umlsUtils.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Descriptions;

/**
 * {@link PT_Descriptions}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class PT_Descriptions extends BPT_Descriptions
{
	public PT_Descriptions(String termName)
	{
		super(termName);
		//from relationship metadata, but needs to be here because they are loaded as description_ types
		addProperty("Inverse FSN");
		addProperty("Inverse Synonym");
		addProperty("Inverse Description");
	}
}
