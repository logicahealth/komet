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

/**
 * Fields to treat as refsets
 * 
 * @author Daniel Armbrust
 * 
 */
public class BPT_Refsets extends PropertyType
{
	private UUID refsetIdentityParent_;  //Typically "Term-name Refsets" under "Project Refsets"

	public BPT_Refsets(String terminologyName)
	{
		super("Refsets", terminologyName + " Refsets", true, null);
	}
	
	public void setRefsetIdentityParent(UUID refsetIdentityParent)
	{
		refsetIdentityParent_ = refsetIdentityParent; 
	}
	
	public UUID getRefsetIdentityParent()
	{
		return refsetIdentityParent_;
	}
}
