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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.RefexDynamic;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.store.Ts;


/**
 * {@link RefexDynamicColumnInfo}
 * 
 * A user friendly class for containing the information parsed out of the Assemblage concepts which defines the RefexDynamic.
 * See the class description for {@link RefexDynamicUsageDescription} for more details.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamicColumnInfo implements Comparable<RefexDynamicColumnInfo>
{
	private UUID columnDescriptionConceptUUID_;
	private transient String columnName_;
	private transient String columnDescription_;
	private int columnOrder_;
	private UUID assemblageConcept_;
	private RefexDynamicDataType columnDataType_;
	private Object defaultData_;

	/**
	 * calls {@link #RefexDynamicColumnInfo(UUID, int, UUID, RefexDynamicDataType, Object) with a null assemblage concept
	 */
	public RefexDynamicColumnInfo(int columnOrder, UUID columnDescriptionConcept, RefexDynamicDataType columnDataType, Object defaultData)
	{
		this(null, columnOrder, columnDescriptionConcept, columnDataType, defaultData);
	}
	
	/**
	 * Create this object by reading the columnName and columnDescription from the provided columnDescriptionConcept.
	 * 
	 * If a suitable concept to use for the column Name/Description does not yet exist, see 
	 * {@link RefexDynamicColumnInfo#createNewRefexDynamicColumnInfoConcept(String, String)}
	 * 
	 * and pass the result in here.
	 * 
	 * @param assemblageConcept - the assemblage concept that this was read from (or null, if not yet part of an assemblage)
	 * @param columnOrder - the column order as defined in the assemblage concept
	 * @param columnDescriptionConceptNid - The concept where columnName and columnDescription should be read from
	 * @param columnDataType - the data type as defined in the assemblage concept
	 * @param defaultData - The type of this Object must align with the data type specified in columnDataType.  For example, 
	 * if columnDataType is set to {@link RefexDynamicDataType#FLOAT} then this field must be a Float.
	 */
	public RefexDynamicColumnInfo(UUID assemblageConcept, int columnOrder, UUID columnDescriptionConcept, RefexDynamicDataType columnDataType, Object defaultData)
	{
		assemblageConcept_ = assemblageConcept;
		columnOrder_ = columnOrder;
		columnDescriptionConceptUUID_ = columnDescriptionConcept;
		columnDataType_ = columnDataType;
		defaultData_ = defaultData;
	}
	
	/**
	 * @return The user-friendly name of this column of data.  To be used by GUIs to label the data in this column.
	 */
	public String getColumnName()
	{
		if (columnName_ == null)
		{
			read();
		}
		return columnName_;
	}

	/**
	 * @return The user-friendly description of this column of data.  To be used by GUIs to provide a more detailed explanation of 
	 * the type of data found in this column. 
	 */
	public String getColumnDescription()
	{
		if (columnDescription_ == null)
		{
			read();
		}
		return columnDescription_;
	}
	
	/**
	 * @return the UUID of the assemblage concept that this column data was read from
	 * or null in the case where this column is not yet associated with an assemblage.
	 */
	public UUID getAssemblageConcept()
	{
		return assemblageConcept_;
	}

	/**
	 * @return Defined the order in which the data columns will be stored, so that the column name / description can be aligned 
	 * with the {@link RefexDynamicDataBI} columns in the {@link RefexDynamicVersionBI#getData(int)}.
	 * 
	 * Note, this value is 0 indexed (It doesn't start at 1)
	 */
	public int getColumnOrder()
	{
		return columnOrder_;
	}

	/**
	 * @return The defined data type for this column of the Refex.  Note that this value will be identical to the {@link RefexDynamicDataType} 
	 * returned by {@link RefexDynamicDataBI} EXCEPT for cases where this returns {@link RefexDynamicDataType#POLYMORPHIC}.  In those cases, the 
	 * data type can only be determined by examining the actual member data in {@link RefexDynamicDataBI}
	 */
	public RefexDynamicDataType getColumnDataType()
	{
		return columnDataType_;
	}
	
	/**
	 * @return the default value to use for this column, if no value is specified in a refex that is created using this column info
	 */
	public Object getDefaultColumnValue()
	{
		//Handle folks sending empty strings gracefully
		if (defaultData_ != null && defaultData_ instanceof String && ((String)defaultData_).length() == 0)
		{
			return null;
		}
		return defaultData_;
	}
	
	/**
	 * @return The UUID of the concept where the columnName and columnDescription were read from.
	 */
	public UUID getColumnDescriptionConcept()
	{
		return columnDescriptionConceptUUID_;
	}
	
	private void read()
	{
		//TODO [REFEX] figure out language details
		String fsn = null;
		String unknownSynonym = null;
		try
		{
			
			ConceptVersionBI cv = Ts.get().getConceptVersion(StandardViewCoordinates.getSnomedInferredThenStatedLatest(), columnDescriptionConceptUUID_);
			if (cv.getDescriptionsActive() != null)
			{
				for (DescriptionVersionBI<?> d : cv.getDescriptionsActive())
				{
					if (d.getTypeNid() == Snomed.FULLY_SPECIFIED_DESCRIPTION_TYPE.getNid())
					{
						fsn = d.getText();
					}
					else if (d.getTypeNid() == Snomed.SYNONYM_DESCRIPTION_TYPE.getNid())
					{
						Boolean isPreferred = null;
						for (RefexChronicleBI<?> refex : d.getRefexes())
						{
							if (refex instanceof RefexNidVersionBI)
							{
								if (((RefexNidVersionBI<?>)refex).getNid1() == SnomedMetadataRf2.PREFERRED_RF2.getNid())
								{
									isPreferred = true;
								}
								else if (((RefexNidVersionBI<?>)refex).getNid1() == SnomedMetadataRf2.ACCEPTABLE_RF2.getNid())
								{
									isPreferred = false;
								}
							}
						}
						if (isPreferred != null)
						{
							if (isPreferred)
							{
								columnName_ = d.getText();
							}
							else
							{
								columnDescription_ = d.getText();
							}
						}
						else
						{
							unknownSynonym = d.getText();
						}
					}
				}
			}
		}
		catch (IOException | ContradictionException e)
		{
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Failure reading RefexDynamicColumnInfo", e);
		}
		if (columnName_ == null)
		{
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "No preferred synonym found on '" + columnDescriptionConceptUUID_ + "' to use "
					+ "for the column name - using FSN");
			columnName_ = (fsn == null ? "ERROR - see log" : fsn);
		}
		
		if (columnDescription_ == null)
		{
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "No acceptable synonym found on '" + columnDescriptionConceptUUID_ + "' to use "
					+ "for the column description- using arbitrary synonym, if one was found");
			columnDescription_ = (unknownSynonym == null ? "There was an error reading the column info from the concept '" + columnDescriptionConceptUUID_ + "'."
					: unknownSynonym);
		}
	}
	
	/**
	 * Create a new concept using the provided columnName and columnDescription values which is suitable 
	 * for use as a column descriptor within {@link RefexDynamicUsageDescription}.
	 * 
	 * The new concept will be created under the concept {@link RefexDynamic#REFEX_DYNAMIC_COLUMNS}
	 * 
	 * A complete usage pattern (where both the refex assemblage concept and the column name concept needs
	 * to be created) would look roughly like this:
	 * 
	 * RefexDynamicUsageDescriptionBuilder.createNewRefexDynamicUsageDescriptionConcept(
	 *     "The name of the Refex", 
	 *     "The description of the Refex",
	 *     new RefexDynamicColumnInfo[]{new RefexDynamicColumnInfo(
	 *         0,
	 *         RefexDynamicColumnInfo.createNewRefexDynamicColumnInfoConcept(
	 *             "column name",
	 *             "column description"
	 *             )
	 *         RefexDynamicDataType.STRING,
	 *         new RefexString("default value")
	 *         )}
	 *     )
	 * 
	 * //TODO [REFEX] figure out language details (how we know what language to put on the name/description
	 * @throws ContradictionException 
	 * @throws InvalidCAB 
	 * @throws IOException 
	 */
	@SuppressWarnings("deprecation")
	public static ConceptChronicleBI createNewRefexDynamicColumnInfoConcept(String columnName, String columnDescription, EditCoordinate ec, ViewCoordinate vc) 
			throws IOException, InvalidCAB, ContradictionException
	{
		if (columnName == null || columnName.length() == 0 || columnDescription == null || columnDescription.length() == 0)
		{
			throw new InvalidCAB("Both the column name and column description are required");
		}
		//Yea, bad bad form.  This impl stuff doesn't not belong in API.  But, will save moving that to a bigger
		//task of getting all of the impl stuff in blueprint out of API.
		LanguageCode lc = LanguageCode.EN_US;
		UUID isA = Snomed.IS_A.getUuids()[0];
		IdDirective idDir = IdDirective.GENERATE_HASH;
		UUID module = Snomed.CORE_MODULE.getUuids()[0];
		UUID parents[] = new UUID[] { RefexDynamic.REFEX_DYNAMIC_COLUMNS.getUuids()[0] };

		ConceptCB cab = new ConceptCB(columnName, columnName, lc, isA, idDir, module, parents);
		
		DescriptionCAB dCab = new DescriptionCAB(cab.getComponentUuid(),  Snomed.SYNONYM_DESCRIPTION_TYPE.getUuids()[0], LanguageCode.EN, 
				columnDescription, false, IdDirective.GENERATE_HASH);
		dCab.getProperties().put(ComponentProperty.MODULE_ID, module);
		
		RefexCAB rCab = new RefexCAB(RefexType.CID, dCab.getComponentUuid(), 
				Snomed.US_LANGUAGE_REFEX.getUuids()[0], IdDirective.GENERATE_HASH, RefexDirective.EXCLUDE);
		rCab.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, SnomedMetadataRf2.ACCEPTABLE_RF2.getUuids()[0]);
		rCab.getProperties().put(ComponentProperty.MODULE_ID, module);
		
		dCab.addAnnotationBlueprint(rCab);
		
		cab.addDescriptionCAB(dCab);
		
		ConceptChronicleBI newCon = Ts.get().getTerminologyBuilder(ec, vc).construct(cab);
		Ts.get().addUncommitted(newCon);
		Ts.get().commit(newCon);
		
		return newCon;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RefexDynamicColumnInfo o)
	{
		return Integer.compare(this.getColumnOrder(), o.getColumnOrder());
	}
}
