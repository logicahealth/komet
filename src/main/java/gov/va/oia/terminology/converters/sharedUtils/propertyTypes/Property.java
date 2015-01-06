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

import java.util.ArrayList;
import java.util.UUID;
import org.ihtsdo.etypes.EConcept;

/**
 * 
 * {@link Property}
 *
 * The converters common code uses this property abstraction system to handle converting different property 
 * types in the WB, while maintaining consistency in how properties are represented.  Also handles advanced
 * cases where we do things like map a property to an existing WB property type, and then annotate the property 
 * instance with the terminology specific property info.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class Property
{
	private String sourcePropertyNameFSN_;
	private String sourcePropertyPreferredName_;
	private String sourcePropertyAltName_;
	private String sourcePropertyDefinition_;
	private boolean isDisabled_ = false;
	private int propertySubType_ = Integer.MAX_VALUE;  //Used for subtypes of descriptions, at the moment - FSN, synonym, etc.
	private PropertyType owner_;
	private UUID propertyUUID = null;
	private UUID useWBPropertyTypeInstead = null;  //see comments in setter
	
	private ArrayList<ConceptCreationNotificationListener> listeners_ = new ArrayList<>(1);

	public Property(PropertyType owner, String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyAltName, 
			String sourcePropertyDefinition, boolean disabled, int propertySubType)
	{
		this.owner_ = owner;
		this.sourcePropertyNameFSN_ = sourcePropertyNameFSN;
		if (sourcePropertyNameFSN.equals(sourcePropertyPreferredName))
		{
			this.sourcePropertyPreferredName_ = null;
		}
		else
		{
			this.sourcePropertyPreferredName_ = sourcePropertyPreferredName;
		}
		this.sourcePropertyAltName_ = sourcePropertyAltName;
		this.sourcePropertyDefinition_ = sourcePropertyDefinition;
		this.isDisabled_ = disabled;
		this.propertySubType_ = propertySubType;
	}

	public Property(PropertyType owner, String sourcePropertyNameFSN, String sourcePropertyPreferredName, int propertySubType)
	{
		this(owner, sourcePropertyNameFSN, sourcePropertyPreferredName, null, null, false, propertySubType);
	}
	
	public Property(PropertyType owner, String sourcePropertyNameFSN, String sourcePropertyPreferredName, boolean disabled)
	{
		this(owner, sourcePropertyNameFSN, sourcePropertyPreferredName, null, null, disabled, Integer.MAX_VALUE);
	}

	public Property(PropertyType owner, String sourcePropertyNameFSN, String sourcePropertyPreferredName)
	{
		this(owner, sourcePropertyNameFSN, sourcePropertyPreferredName, null, null, false, Integer.MAX_VALUE);
	}

	public Property(PropertyType owner, String sourcePropertyNameFSN)
	{
		this(owner, sourcePropertyNameFSN, sourcePropertyNameFSN, null, null, false, Integer.MAX_VALUE);
	}
	
	/**
	 * owner must be set via the set method after using this constructor!
	 */
	public Property(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyDefinition, UUID wbRelType)
	{
		this(null, sourcePropertyNameFSN, sourcePropertyPreferredName, sourcePropertyDefinition, null, false, Integer.MAX_VALUE);
		setWBPropertyType(wbRelType);
	}
	
	/**
	 * owner must be set via the set method after using this constructor!
	 */
	public Property(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyAltName, String sourcePropertyDefinition, UUID wbRelType)
	{
		this(null, sourcePropertyNameFSN, sourcePropertyPreferredName, sourcePropertyAltName, sourcePropertyDefinition, false, Integer.MAX_VALUE);
		setWBPropertyType(wbRelType);
	}

	public String getSourcePropertyNameFSN()
	{
		return sourcePropertyNameFSN_;
	}

	public String getSourcePropertyPreferredName()
	{
		return sourcePropertyPreferredName_;
	}
	
	public String getSourcePropertyAltName()
	{
		return sourcePropertyAltName_;
	}
	
	public String getSourcePropertyDefinition()
	{
		return sourcePropertyDefinition_;
	}

	/**
	 * Normally, we just create the relation names as specified.  However, some, we map to 
	 * other existing WB relationships, and put the source rel name on as an extension - for example
	 * To enable the map case, set this (and use the appropriate addRelationship method)
	 */
	public void setWBPropertyType(UUID wbRelType)
	{
		this.useWBPropertyTypeInstead = wbRelType;
	}
	
	public UUID getWBTypeUUID()
	{
		return useWBPropertyTypeInstead;
	}

	protected void setOwner(PropertyType owner)
	{
		this.owner_ = owner;
	}

	public UUID getUUID()
	{
		if (propertyUUID == null)
		{
			propertyUUID = owner_.getPropertyUUID(this.sourcePropertyNameFSN_);
		}
		return propertyUUID;
	}

	public boolean isDisabled()
	{
		return isDisabled_;
	}
	
	public void setPropertySubType(int value)
	{
		this.propertySubType_ = value;
	}
	
	public int getPropertySubType()
	{
		return propertySubType_;
	}
	
	public PropertyType getPropertyType()
	{
		return owner_;
	}
	
	/**
	 * Mechanism to allow registration for notification when the corresponding eConcept has been created for this property.
	 * Callback occurs before the eConcept is written.  Useful for adding additional attributes to the eConcept.
	 * @param listener
	 */
	public void registerConceptCreationListener(ConceptCreationNotificationListener listener)
	{
		listeners_.add(listener);
	}
	
	/**
	 * This is called just before a metadata concept is written when the typical loadMetaDataItems(...) sequence is used in the eConceptUtility.  
	 * 
	 * Any the created concept will be passed to any registered listeners before the concept is written.
	 * @param concept
	 */
	public void conceptCreated(EConcept concept)
	{
		for (ConceptCreationNotificationListener ccn : listeners_)
		{
			ccn.conceptCreated(this, concept);
		}
	}
}
