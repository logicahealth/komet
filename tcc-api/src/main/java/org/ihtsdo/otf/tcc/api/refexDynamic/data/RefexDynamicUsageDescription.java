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

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.ComponentType;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicIntegerBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicPolymorphicBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicStringBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicUUIDBI;
import org.ihtsdo.otf.tcc.api.store.Ts;


/**
 * {@link RefexDynamicUsageDescription}
 * 
 * In the new RefexDynanamicAPI - there are strict requirements on the structure of the 
 * assemblage concept.
 * <br>
 * <br>
 * The assemblage concept must define the combination of data columns being used within this Refex. 
 * To do this, the assemblage concept must itself contain 0 or more {@link RefexDynamicVersionBI} annotation(s) with
 * an assemblage concept that is {@link RefexDynamic#DYNAMIC_SEMEME_EXTENSION_DEFINITION} and the attached data is<br>
 * [{@link RefexDynamicIntegerBI}, {@link RefexDynamicUUIDBI}, {@link RefexDynamicStringBI}, {@link RefexDynamicPolymorphicBI},
 * {@link RefexBooleanBI}, {@link RefexDynamicStringBI}, {@link RefexDynamicPolymorphicBI}] 
 * 
 * <ul>
 * <li>The int value is used to align the column order with the data array here.  The column number should be 0 indexed.
 * <li>The UUID is a concept reference where the concept should have a preferred semantic name / FSN that is
 *       suitable for the name of the DynamicRefex data column, and a description suitable for use as the description of the 
 *       Dynamic Refex data column.  Note, while any concept can be used here, and there are no specific requirements for this 
 *       concept - there is a convenience method for creating one of these concepts in 
 *       {@link RefexDynamicColumnInfo#createNewRefexDynamicColumnInfoConcept(String, String, 
 *           org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate, org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate)}
 * <li>A string column which can be parsed as a member of the {@link RefexDynamicDataType} class, which represents
 *       the type of the column.
 * <li>An (optional) polymorphic column (any supported data type, but MUST match the data type specified in column 2) which contains 
 *       the default value (if any) for this column.  
 * <li>An (optional) boolean column which specifies if this column is required (true) or optional (false or null) for this column.  
 * <li>An (optional) string column which can be parsed as a member of the {@link RefexDynamicValidatorType} class, which represents
 *       the validator type assigned to the the column (if any).
 * <li>An (optional) polymorphic column (any supported data type, but MUST match the requirements of the validator specified in column 6) 
 *       which contains validator data (if any) for this column.  
 * </ul>
 * <br>
 * Note that while 0 rows of attached data is allowed, this would not allow the attachment of any data on the refex.
 * <br>
 * The assemblage concept must also contain a description of type {@link Snomed#DEFINITION_DESCRIPTION_TYPE} which 
 * itself has a refex extension of type {@link RefexDynamic#DYNAMIC_SEMEME_DEFINITION_DESCRIPTION} - the value of 
 * this description should explain the the overall purpose of this Refex.
 * <br>
 * <br>
 * The assemblage concept may also contain a single {@link RefexDynamicVersionBI} annotation of type {@link RefexDynamic#DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION}
 * with a single string column which can be parsed as a {@link ComponentType} - which will restrict the type of nid that can be placed 
 * into the referenced component field when creating an instance of the assemblage.
 * <br>
 * <br>
 * This class provides an implementation for parsing the interesting bits out of an assemblage concept.
 * 
 * For an implementation on creating them, 
 * See {@link org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder#createNewRefexDynamicUsageDescriptionConcept} 
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@SuppressWarnings("deprecation")
public class RefexDynamicUsageDescription
{
	int refexUsageDescriptorNid_;
	String refexUsageDescription_;
	String name_;
	boolean annotationStyle_;
	RefexDynamicColumnInfo[] refexColumnInfo_;
	ComponentType referencedComponentTypeRestriction_;
	
	private static LRURefexDynamicDescriptorCache<Integer, RefexDynamicUsageDescription> cache_ = 
			new LRURefexDynamicDescriptorCache<Integer, RefexDynamicUsageDescription>(25);
	
	protected static final Logger logger = Logger.getLogger(RefexDynamicUsageDescription.class.getName());

	public static RefexDynamicUsageDescription read(int assemblageNid) throws IOException, ContradictionException
	{
		//TODO (artf231860) [REFEX] maybe? implement a mechanism to allow the cache to be updated... for now
		//cache is uneditable, and may be wrong, if the user changes the definition of a dynamic refex.  Perhaps
		//implement a callback to clear the cache when we know a change of  a certain type happened instead?
		RefexDynamicUsageDescription temp = cache_.get(assemblageNid);
		if (temp == null)
		{
			logger.log(Level.FINEST, "Cache miss on RefexDynamicUsageDescription Cache");
			temp = new RefexDynamicUsageDescription(assemblageNid);
			cache_.put(assemblageNid, temp);
		}
		return temp;
	}
	
	/**
	 * Read the RefexUsageDescription data from the database for a given nid.
	 * 
	 * Note that most users should call {@link #read(int)} instead, as that utilizes a cache.
	 * This always reads directly from the DB.
	 * 
	 * @param refexUsageDescriptorNid
	 * @throws IOException 
	 * @throws ContradictionException 
	 */
	public RefexDynamicUsageDescription(int refexUsageDescriptorNid) throws IOException, ContradictionException
	{
		refexUsageDescriptorNid_ = refexUsageDescriptorNid;
		TreeMap<Integer, RefexDynamicColumnInfo> allowedColumnInfo = new TreeMap<>();
		ConceptChronicleBI assemblageConcept = Ts.get().getConcept(refexUsageDescriptorNid);
		
		annotationStyle_ = assemblageConcept.isAnnotationStyleRefex();
		
		for (DescriptionChronicleBI dc : assemblageConcept.getDescriptions())
		{
			for (DescriptionVersionBI<?> d : getAllActive(dc))
			{
				if (d.getTypeNid() == Snomed.DEFINITION_DESCRIPTION_TYPE.getNid())
				{
					boolean hasCorrectAnnotation = false;
					for (RefexDynamicChronicleBI<?> descriptionAnnotation : d.getRefexesDynamic())
					{
						if (descriptionAnnotation.getAssemblageNid() == RefexDynamic.DYNAMIC_SEMEME_DEFINITION_DESCRIPTION.getNid())
						{
							hasCorrectAnnotation = true;
						}
						if (hasCorrectAnnotation)
						{
							refexUsageDescription_ = d.getText();
							break;
						}
					}
				}
				if (d.getTypeNid() == SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getNid())
				{
					name_ = d.getText();
				}
				if (refexUsageDescription_ != null && name_ != null)
				{
					break;
				}
			}
			if (refexUsageDescription_ != null && name_ != null)
			{
				break;
			}
		}
		if (StringUtils.isEmpty(refexUsageDescription_))
		{
			throw new IOException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for " 
					+ "a RefexDynamicData Refex Type.  It must contain a description of type Definition with an annotation of type " + 
					"RefexDynamic.REFEX_DYNAMIC_DEFINITION_DESCRIPTION");
		}
		
		for (RefexDynamicChronicleBI<?> rdc : assemblageConcept.getRefexesDynamic())
		{
			for (RefexDynamicVersionBI<?> rd : getAllActive(rdc))
			{
				if (rd.getAssemblageNid() == RefexDynamic.DYNAMIC_SEMEME_EXTENSION_DEFINITION.getNid())
				{
					RefexDynamicDataBI[] refexDefinitionData = rd.getData();
					if (refexDefinitionData == null || refexDefinitionData.length < 3 || refexDefinitionData.length > 7)
					{
						throw new IOException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for " 
								+ "a RefexDynamicData Refex Type.  It must contain at least 3 columns in the RefexDynamicDataBI attachment, and no more than 7.");
					}
					
					//col 0 is the column number, 
					//col 1 is the concept with col name 
					//col 2 is the column data type, stored as a string.
					//col 3 (if present) is the default column data, stored as a subtype of RefexDynamicDataBI
					//col 4 (if present) is a boolean field noting whether the column is required (true) or optional (false or null)
					//col 5 (if present) is the validator {@link RefexDynamicValidatorType}, stored as a string.
					//col 6 (if present) is the validatorData for the validator in column 5, stored as a subtype of RefexDynamicDataBI
					try
					{
						int column = (Integer)refexDefinitionData[0].getDataObject();
						UUID descriptionUUID = (UUID)refexDefinitionData[1].getDataObject();
						RefexDynamicDataType type = RefexDynamicDataType.valueOf((String)refexDefinitionData[2].getDataObject());
						RefexDynamicDataBI defaultData = null;
						if (refexDefinitionData.length > 3)
						{
							defaultData = (refexDefinitionData[3] == null ? null : refexDefinitionData[3]);
						}
						
						if (defaultData != null && type.getRefexMemberClass() != refexDefinitionData[3].getRefexDataType().getRefexMemberClass())
						{
							throw new IOException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for " 
								+ "a RefexDynamicData Refex Type.  The type of the column (column 3) must match the type of the defaultData (column 4)");
						}
						
						Boolean columnRequired = null;
						if (refexDefinitionData.length > 4)
						{
							columnRequired = (refexDefinitionData[4] == null ? null : (Boolean)refexDefinitionData[4].getDataObject());
						}
						
						RefexDynamicValidatorType validator = null;
						RefexDynamicDataBI validatorData = null;
						if (refexDefinitionData.length > 5)
						{
							validator = (refexDefinitionData[5] == null ? null : RefexDynamicValidatorType.valueOf((String)refexDefinitionData[5].getDataObject()));
							if (refexDefinitionData.length > 6)
							{
								validatorData = (refexDefinitionData[6] == null ? null : refexDefinitionData[6]);
							}
						}
						
						allowedColumnInfo.put(column, new RefexDynamicColumnInfo(assemblageConcept.getPrimordialUuid(), column, descriptionUUID, type, 
								defaultData, columnRequired, validator, validatorData));
					}
					catch (Exception e)
					{
						throw new IOException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for " 
								+ "a RefexDynamicData Refex Type.  The first column must have a data type of integer, and the third column must be a string "
								+ "that is parseable as a RefexDynamicDataType");
					}
				}
				else if (rd.getAssemblageNid() == RefexDynamic.DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION.getNid())
				{
					RefexDynamicDataBI[] refexDefinitionData = rd.getData();
					if (refexDefinitionData == null || refexDefinitionData.length < 1)
					{
						throw new IOException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for " 
								+ "a RefexDynamicData Refex Type.  If it contains a " + RefexDynamic.DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION.getFsn()
								+ " then it must contain a single column of data, of type string, parseable as a " + ComponentType.class.getName());
					}
					
					//col 0 is Referenced component restriction information - as a string. 
					try
					{
						ComponentType type = ComponentType.parse(refexDefinitionData[0].getDataObject().toString());
						if (type == ComponentType.UNKNOWN)
						{
							//just ignore - it shouldn't have been saved that way anyway.
						}
						else
						{
							referencedComponentTypeRestriction_ = type;
						}
					}
					catch (Exception e)
					{
						throw new IOException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for " 
								+ "a RefexDynamicData Refex Type.  The component type restriction annotation has an invalid value");
					}
				}
			}
		}
		
		refexColumnInfo_ = new RefexDynamicColumnInfo[allowedColumnInfo.size()];
		
		int i = 0;
		for (int key : allowedColumnInfo.keySet())
		{
			if (key !=  i)
			{
				throw new IOException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for " 
						+ "a RefexDynamicData Refex Type.  It must contain sequential column numbers, with no gaps, which start at 0.");
			}
			refexColumnInfo_[i++] = allowedColumnInfo.get(key);
		}
	}

	/**
	 * @return The nid of the concept that the rest of the attributes of this type were read from.
	 */
	public int getRefexUsageDescriptorNid()
	{
		return refexUsageDescriptorNid_;
	}

	/**
	 * @return A user-friendly description of the overall purpose of this Refex.
	 */
	public String getRefexUsageDescription()
	{
		return refexUsageDescription_;
	}
	
	/**
	 * (Convenience method)
	 * @return returns the FSN of the assemblage concept this was read from
	 */
	public String getRefexName()
	{
		return name_;
	}
	
	/**
	 * (convenience method)
	 * @return true if this is an annotation style refex, false if a memberlist style refex.
	 * Value comes from {@link ConceptChronicleBI#isAnnotationStyleRefex()} on the assemblage concept
	 */
	public boolean isAnnotationStyle()
	{
		return annotationStyle_;
	}

	/**
	 * The ordered column information which will correspond with the data returned by {@link RefexDynamicChronicleBI#getData()}
	 * These arrays will be the same size, and in the same order.  Will not return null.
	 * @return the column information
	 */
	public RefexDynamicColumnInfo[] getColumnInfo()
	{
		if (refexColumnInfo_ == null)
		{
			refexColumnInfo_ = new RefexDynamicColumnInfo[] {};
		}
		return refexColumnInfo_;
	}
	
	/**
	 * Return the {@link ComponentType} of the restriction on referenced components for this refex (if any - may return null)
	 * 
	 * If there is a restriction, the nid set for the component type of this refex must resolve to the matching type.
	 */
	public ComponentType getReferencedComponentTypeRestriction()
	{
		return referencedComponentTypeRestriction_;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + refexUsageDescriptorNid_;
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RefexDynamicUsageDescription other = (RefexDynamicUsageDescription) obj;
		if (refexUsageDescriptorNid_ != other.refexUsageDescriptorNid_)
			return false;
		return true;
	}
	
	protected static ArrayList<DescriptionVersionBI<?>> getAllActive(DescriptionChronicleBI descriptionChronicle) throws IOException
	{
		ArrayList<DescriptionVersionBI<?>> result = new ArrayList<>();
		if (descriptionChronicle.getVersions() != null)
		{
			for (DescriptionVersionBI<?> rdv : descriptionChronicle.getVersions())
			{
				if (rdv.isActive())
				{
					result.add(rdv);
				}
			}
		}
		return result;
	}
	
	protected static ArrayList<RefexDynamicVersionBI<?>> getAllActive(RefexDynamicChronicleBI<?> refexDynamicChronicle) throws IOException
	{
		ArrayList<RefexDynamicVersionBI<?>> result = new ArrayList<>();
		if (refexDynamicChronicle.getVersions() != null)
		{
			for (RefexDynamicVersionBI<?> rdv : refexDynamicChronicle.getVersions())
			{
				if (rdv.isActive())
				{
					result.add(rdv);
				}
			}
		}
		return result;
	}
}
