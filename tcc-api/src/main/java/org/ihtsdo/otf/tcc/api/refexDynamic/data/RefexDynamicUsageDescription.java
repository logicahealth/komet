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
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
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
 * an assemblage concept that is {@link RefexDynamic#REFEX_DYNAMIC_DEFINITION} and the attached data is<br>
 * [{@link RefexDynamicIntegerBI}, {@link RefexDynamicUUIDBI}, {@link RefexDynamicStringBI}, {@link RefexDynamicPolymorphicBI}] 
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
 * </ul>
 * <br>
 * Note that while 0 rows of attached data is allowed, this would not allow the attachment of any data on the refex.
 * <br>
 * The assemblage concept must also contain a description of type {@link SnomedMetadataRf2#SYNONYM_RF2} which 
 * itself has a refex extension of type {@link RefexDynamic#REFEX_DYNAMIC_DEFINITION_DESCRIPTION} - the value of 
 * this description should explain the the overall purpose of this Refex.
 * <br>
 * <br>
 * This class provides an implementation for parsing the interesting bits out of an assemblage concept.
 * 
 * For an implementation on creating them, 
 * See {@link org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicUsageDescriptionBuilder#createNewRefexDynamicUsageDescriptionConcept} 
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamicUsageDescription
{
	int refexUsageDescriptorNid_;
	private transient int stampNid_;
	String refexUsageDescription_;
	RefexDynamicColumnInfo[] refexColumnInfo_;
	private static LRURefexDynamicDescriptorCache<Integer, RefexDynamicUsageDescription> cache_ = 
			new LRURefexDynamicDescriptorCache<Integer, RefexDynamicUsageDescription>(25);
	
	protected static final Logger logger = Logger.getLogger(RefexDynamicUsageDescription.class.getName());

	public static RefexDynamicUsageDescription read(int assemblageNid) throws IOException, ContradictionException
	{
		//TODO [REFEX] test and see if my cache mechanism is working right (especially stamp check)
		RefexDynamicUsageDescription temp = cache_.get(assemblageNid);
		if (temp == null || 
				Ts.get().getConceptVersion(StandardViewCoordinates.getSnomedInferredThenStatedLatest(), assemblageNid).getConceptAttributesActive().getStamp() != temp.stampNid_)
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
		@SuppressWarnings("deprecation")
		ConceptVersionBI assemblageConcept = Ts.get().getConceptVersion(StandardViewCoordinates.getSnomedInferredThenStatedLatest(), refexUsageDescriptorNid);
		stampNid_ = assemblageConcept.getConceptAttributesActive().getStamp();
		
		for (DescriptionVersionBI<?> d : assemblageConcept.getDescriptionsActive())
		{
			if (d.getTypeNid() == SnomedMetadataRf2.SYNONYM_RF2.getNid())
			{
				boolean hasCorrectAnnotation = false;
				for (RefexDynamicChronicleBI<?> descriptionAnnotation : d.getRefexesDynamic())
				{
					if (descriptionAnnotation.getAssemblageNid() == RefexDynamic.REFEX_DYNAMIC_DEFINITION_DESCRIPTION.getNid())
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
			if (refexUsageDescription_ != null)
			{
				break;
			}
		}
		if (StringUtils.isEmpty(refexUsageDescription_))
		{
			throw new IOException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for " 
					+ "a RefexDynamicData Refex Type.  It must contain a description of type Synonym with an annotation of type " + 
					"RefexDynamic.REFEX_DYNAMIC_DEFINITION_DESCRIPTION");
		}
		
		for (RefexDynamicVersionBI<?> rd : assemblageConcept.getRefexesDynamicActive(StandardViewCoordinates.getSnomedInferredThenStatedLatest()))
		{
			if (rd.getAssemblageNid() == RefexDynamic.REFEX_DYNAMIC_DEFINITION.getNid())
			{
				RefexDynamicDataBI[] refexDefinitionData = rd.getData();
				if (refexDefinitionData == null || refexDefinitionData.length < 3 || refexDefinitionData.length > 4)
				{
					throw new IOException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for " 
							+ "a RefexDynamicData Refex Type.  It must contain a 3 (or 4) column RefexDynamicDataBI attachment.");
				}
				
				//col 0 is the column number, 
				//col 1 is the concept with col name 
				//col 2 is the column data type, stored as a string.
				//col 3 (if present) is the default column data, stored as a subtype of RefexDynamicDataBI
				try
				{
					int column = (Integer)refexDefinitionData[0].getDataObject();
					UUID descriptionUUID = (UUID)refexDefinitionData[1].getDataObject();
					RefexDynamicDataType type = RefexDynamicDataType.valueOf((String)refexDefinitionData[2].getDataObject());
					Object defaultData = null;
					if (refexDefinitionData.length > 3)
					{
						defaultData = (refexDefinitionData[3] == null ? null : refexDefinitionData[3].getDataObject());
					}
					
					if (defaultData != null && type.getRefexMemberClass() != defaultData.getClass())
					{
						throw new IOException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for " 
							+ "a RefexDynamicData Refex Type.  The type of the column (column 3) must match the type of the defaultData (column 4)");
					}
					
					allowedColumnInfo.put(column, new RefexDynamicColumnInfo(column, descriptionUUID, type, defaultData));
				}
				catch (Exception e)
				{
					throw new IOException("The Assemblage concept: " + assemblageConcept + " is not correctly assembled for use as an Assemblage for " 
							+ "a RefexDynamicData Refex Type.  The first column must have a data type of integer, and the third column must be a string "
							+ "that is parseable as a RefexDynamicDataType");
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
}
