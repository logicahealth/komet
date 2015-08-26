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

import gov.va.oia.terminology.converters.sharedUtils.EConceptUtility;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.model.constants.IsaacMetadataConstants;
import java.beans.PropertyVetoException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.StringUtils;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.component.TtkUtils;

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
	private UUID propertyUUID = null;;
	private UUID useWBPropertyTypeInstead = null;  //see comments in setter
	private DynamicSememeColumnInfo[] dataColumnsForDynamicRefex_ = null;
	
	private ArrayList<ConceptCreationNotificationListener> listeners_ = new ArrayList<>(1);

	/**
	 * @param dataTypesForDynamicRefex - if null - will use the default information for the parent {@link PropertyType} - otherwise, 
	 * uses as provided here (even if empty)
	 */
	public Property(PropertyType owner, String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyAltName, 
			String sourcePropertyDefinition, boolean disabled, int propertySubType, DynamicSememeColumnInfo[] columnInforForDynamicRefex)
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
		
		//if owner is null, have to delay this until the setOwner call
		//leave the assemblageConceptUUID null for now - it should be set to "getUUID()" but that isn't always ready
		//at the time this code runs.  We make sure it is set down below, in the getter.
		if (columnInforForDynamicRefex == null && owner != null && owner_.getDefaultColumnInfo() != null)
		{
			//Create a single required column, with the column name just set to 'value'
			dataColumnsForDynamicRefex_ = new DynamicSememeColumnInfo[] { new DynamicSememeColumnInfo(null, 0, IsaacMetadataConstants.DYNAMIC_SEMEME_COLUMN_VALUE.getUUID(),
					owner_.getDefaultColumnInfo(), null, true, null, null)};
		}
		else
		{
			dataColumnsForDynamicRefex_ = columnInforForDynamicRefex;
		}
		
		if (dataColumnsForDynamicRefex_ != null && !owner_.createAsDynamicRefex()) 
		{
			throw new RuntimeException("Tried to attach dynamic refex data where it isn't allowed.");
		}
	}

	public Property(PropertyType owner, String sourcePropertyNameFSN, String sourcePropertyPreferredName, int propertySubType)
	{
		this(owner, sourcePropertyNameFSN, sourcePropertyPreferredName, null, null, false, propertySubType, null);
	}
	
	public Property(PropertyType owner, String sourcePropertyNameFSN, String sourcePropertyPreferredName, boolean disabled)
	{
		this(owner, sourcePropertyNameFSN, sourcePropertyPreferredName, null, null, disabled, Integer.MAX_VALUE, null);
	}

	public Property(PropertyType owner, String sourcePropertyNameFSN, String sourcePropertyPreferredName)
	{
		this(owner, sourcePropertyNameFSN, sourcePropertyPreferredName, null, null, false, Integer.MAX_VALUE, null);
	}

	public Property(PropertyType owner, String sourcePropertyNameFSN)
	{
		this(owner, sourcePropertyNameFSN, sourcePropertyNameFSN, null, null, false, Integer.MAX_VALUE, null);
	}
	
	/**
	 * owner must be set via the set method after using this constructor!
	 */
	public Property(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyDefinition, UUID wbRelType)
	{
		this(null, sourcePropertyNameFSN, sourcePropertyPreferredName, sourcePropertyDefinition, null, false, Integer.MAX_VALUE, null);
		setWBPropertyType(wbRelType);
	}
	
	/**
	 * owner must be set via the set method after using this constructor!
	 */
	public Property(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyAltName, String sourcePropertyDefinition, UUID wbRelType)
	{
		this(null, sourcePropertyNameFSN, sourcePropertyPreferredName, sourcePropertyAltName, sourcePropertyDefinition, false, Integer.MAX_VALUE, null);
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
		
		if (dataColumnsForDynamicRefex_ == null && owner_.getDefaultColumnInfo() != null)
		{
			//Create a single required column, with the column name concept tied back to the assemblage concept itself.
			//leave the assemblageConceptUUID null for now - it should be set to "getUUID()" but that isn't always ready
			//at the time this code runs.  We make sure it is set down below, in the getter.
			dataColumnsForDynamicRefex_ = new DynamicSememeColumnInfo[] { new DynamicSememeColumnInfo(null, 0, getUUID(),
					owner_.getDefaultColumnInfo(), null, true, null, null)};
		}
		if (dataColumnsForDynamicRefex_ != null && !owner_.createAsDynamicRefex()) 
		{
			throw new RuntimeException("Tried to attach dynamic refex data where it isn't allowed.");
		}
		
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
	
	public DynamicSememeColumnInfo[] getDataColumnsForDynamicRefex()
	{
		if (dataColumnsForDynamicRefex_ != null && dataColumnsForDynamicRefex_.length == 1 && dataColumnsForDynamicRefex_[0].getAssemblageConcept() == null)
		{
			dataColumnsForDynamicRefex_[0].setAssemblageConcept(getUUID());
		}
		return dataColumnsForDynamicRefex_;
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
	public BiConsumer<TtkConceptChronicle, EConceptUtility> getCallback()
	{
		return new BiConsumer<TtkConceptChronicle, EConceptUtility>()
		{
			@Override
			public void accept(TtkConceptChronicle concept, EConceptUtility utility)
			{
				if (Property.this.getPropertyType().createAsDynamicRefex())
				{
					try
					{
						TtkUtils.configureConceptAsRefex(concept, 
								(StringUtils.isNotEmpty(Property.this.getSourcePropertyDefinition()) ? Property.this.getSourcePropertyDefinition() : "Dynamic Sememe"),
								Property.this.getDataColumnsForDynamicRefex(), null, null, 
								(rev) -> (utility.setRevisionAttributes(rev, Status.ACTIVE, concept.getConceptAttributes().getTime())));
					}
					catch (NoSuchAlgorithmException | UnsupportedEncodingException | PropertyVetoException e)
					{
						throw new RuntimeException("Unexpected");
					}
				}
				
				for (ConceptCreationNotificationListener ccn : listeners_)
				{
					ccn.conceptCreated(Property.this, concept);
				}
			}
		};
	}
}
