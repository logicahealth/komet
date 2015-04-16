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
package org.ihtsdo.otf.tcc.api.metadata;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.media.MediaVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

/**
 * {@link ComponentType}
 *
 * Types of things you might get back which extend {@link ComponentVersionBI} when you look up a nid
 * 
 * May not be a complete list yet - extend this as necessary.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

//TODO get this tied to whatever concepts are linked via TypedComponentVersionBI - not sure if there are hard relationships there - at least get the UUIDs 
//in here as constants

public enum ComponentType
{
	CONCEPT("Concept"), 
	DESCRIPTION("Description"), 
	RELATIONSHIP("Relationship"), 
	SEMEME_DYNAMIC("Dynamic Sememe"), 
	SEMEME("Sememe"), 
	CONCEPT_ATTRIBUTES("Concept Attributes"),
	MEDIA("Media"), 
	UNKNOWN("Unknown");
	
	private String niceName_;
	
	private ComponentType(String niceName)
	{
		niceName_ = niceName;
	}

	/**
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString()
	{
		return niceName_;
	}
	
	/**
	 * A convenience method to find out what type of object you have
	 * @param component
	 * @return
	 */
	public static ComponentType getComponentVersionType(ComponentVersionBI component)
	{
		if (component instanceof ConceptVersionBI)
		{
			return CONCEPT;
		}
		else if (component instanceof ConceptAttributeVersionBI)
		{
			return CONCEPT_ATTRIBUTES;
		}
		else if (component instanceof DescriptionVersionBI)
		{
			return DESCRIPTION;
		}
		else if (component instanceof RelationshipVersionBI)
		{
			return RELATIONSHIP;
		}
		else if (component instanceof RefexVersionBI)
		{
			return SEMEME;
		}
		else if (component instanceof RefexDynamicVersionBI)
		{
			return SEMEME_DYNAMIC;
		}
		else if (component instanceof MediaVersionBI)
		{
			return MEDIA;
		}
		else
		{
			return UNKNOWN;
		}
	}
	
	public static ComponentType parse(String name)
	{
		for (ComponentType ct : values())
		{
			if (ct.name().equals(name) || ct.niceName_.equals(name))
			{
				return ct;
			}
		}
		return UNKNOWN;
	}
}
