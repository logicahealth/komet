package gov.vha.isaac.ochre.model.sememe;

import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.And;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.NecessarySet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilder;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilderService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.concept.description.DescriptionBuilder;
import gov.vha.isaac.ochre.api.component.concept.description.DescriptionBuilderService;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUtility;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArray;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeBoolean;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeByteArray;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeDouble;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeFloat;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeInteger;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeLong;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeNid;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeSequence;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUID;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilderService;
import gov.vha.isaac.ochre.model.configuration.EditCoordinates;
import gov.vha.isaac.ochre.model.configuration.LogicCoordinates;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeArrayImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeBooleanImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeIntegerImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeUUIDImpl;
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
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

/**
 * {@link DynamicSememeUtility}
 *
 * Convenience methods related to DynamicSememes.  Implemented as an interface and a singleton to provide 
 * lower level code with access to these methods at runtime via HK2.
  *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class DynamicSememeUtilityImpl implements DynamicSememeUtility
{
	
	/**
	 * Read the {@link DynamicSememeUsageDescription} for the specified assemblage concept
	 */
	@Override
	public DynamicSememeUsageDescription readDynamicSememeUsageDescription(int assemblageNidOrSequence)
	{
		return DynamicSememeUsageDescriptionImpl.read(assemblageNidOrSequence);
	}


	
	@Override
	public DynamicSememeData[] configureDynamicSememeRestrictionData(ObjectChronologyType referencedComponentRestriction,
			SememeType referencedComponentSubRestriction)
	{
		if (referencedComponentRestriction != null && ObjectChronologyType.UNKNOWN_NID != referencedComponentRestriction)
		{
			int size = 1;
			if (referencedComponentSubRestriction != null &&  SememeType.UNKNOWN != referencedComponentSubRestriction)
			{
				size = 2;
			}

			DynamicSememeData[] data = new DynamicSememeData[size];
			data[0] = new DynamicSememeStringImpl(referencedComponentRestriction.name());
			if (size == 2)
			{
				data[1] = new DynamicSememeStringImpl(referencedComponentSubRestriction.name());
			}
			return data;
		}
		return null;
	}

	@Override
	public DynamicSememeData[] configureDynamicSememeDefinitionDataForColumn(DynamicSememeColumnInfo ci)
	{
		DynamicSememeData[] data = new DynamicSememeData[7];
		
		data[0] = new DynamicSememeIntegerImpl(ci.getColumnOrder());
		data[1] = new DynamicSememeUUIDImpl(ci.getColumnDescriptionConcept());
		if (DynamicSememeDataType.UNKNOWN == ci.getColumnDataType())
		{
			throw new RuntimeException("Error in column - if default value is provided, the type cannot be polymorphic");
		}
		data[2] = new DynamicSememeStringImpl(ci.getColumnDataType().name());
		data[3] = convertPolymorphicDataColumn(ci.getDefaultColumnValue(), ci.getColumnDataType());
		data[4] = new DynamicSememeBooleanImpl(ci.isColumnRequired());
		
		if (ci.getValidator() != null)
		{
			DynamicSememeString[] validators = new DynamicSememeString[ci.getValidator().length];
			for (int i = 0; i < validators.length; i++)
			{
				validators[i] = new DynamicSememeStringImpl(ci.getValidator()[i].name());
			}
			data[5] = new DynamicSememeArrayImpl<DynamicSememeString>(validators);
		}
		else
		{
			data[5] = null;
		}
		
		if (ci.getValidatorData() != null)
		{
			DynamicSememeData[] validatorData = new DynamicSememeData[ci.getValidatorData().length];
			for (int i = 0; i < validatorData.length; i++)
			{
				validatorData[i] = convertPolymorphicDataColumn(ci.getValidatorData()[i], ci.getValidatorData()[i].getDynamicSememeDataType());
			}
			data[6] = new DynamicSememeArrayImpl<DynamicSememeData>(validatorData);
		}
		else
		{
			data[6] = null;
		}
		return data;
	}
	
	private static DynamicSememeData convertPolymorphicDataColumn(DynamicSememeData defaultValue, DynamicSememeDataType columnType) 
	{
		DynamicSememeData result;
		
		if (defaultValue != null)
		{
			try
			{
				if (DynamicSememeDataType.BOOLEAN == columnType)
				{
					result = (DynamicSememeBoolean)defaultValue;
				}
				else if (DynamicSememeDataType.BYTEARRAY == columnType)
				{
					result = (DynamicSememeByteArray)defaultValue;
				}
				else if (DynamicSememeDataType.DOUBLE == columnType)
				{
					result = (DynamicSememeDouble)defaultValue;
				}
				else if (DynamicSememeDataType.FLOAT == columnType)
				{
					result = (DynamicSememeFloat)defaultValue;
				}
				else if (DynamicSememeDataType.INTEGER == columnType)
				{
					result = (DynamicSememeInteger)defaultValue;
				}
				else if (DynamicSememeDataType.LONG == columnType)
				{
					result = (DynamicSememeLong)defaultValue;
				}
				else if (DynamicSememeDataType.NID == columnType)
				{
					result = (DynamicSememeNid)defaultValue;
				}
				else if (DynamicSememeDataType.STRING == columnType)
				{
					result = (DynamicSememeString)defaultValue;
				}
				else if (DynamicSememeDataType.UUID == columnType)
				{
					result = (DynamicSememeUUID)defaultValue;
				}
				else if (DynamicSememeDataType.ARRAY == columnType)
				{
					result = (DynamicSememeArray<?>)defaultValue;
				}
				else if (DynamicSememeDataType.SEQUENCE== columnType)
				{
					result = (DynamicSememeSequence)defaultValue;
				}
				else if (DynamicSememeDataType.POLYMORPHIC == columnType)
				{
					throw new RuntimeException("Error in column - if default value is provided, the type cannot be polymorphic");
				}
				else
				{
					throw new RuntimeException("Actually, the implementation is broken.  Ooops.");
				}
			}
			catch (ClassCastException e)
			{
				throw new RuntimeException("Error in column - if default value is provided, the type must be compatible with the the column descriptor type");
			}
		}
		else
		{
			result = null;
		}
		return result;
	}

	@Override
	public DynamicSememeString createDynamicStringData(String value) {
		return new DynamicSememeStringImpl(value);
	}

	@Override
	public DynamicSememeArray<DynamicSememeData> configureColumnIndexInfo(DynamicSememeColumnInfo[] columns) {
		ArrayList<DynamicSememeIntegerImpl> temp = new ArrayList<>();
		if (columns != null)
		{
			Arrays.sort(columns);
			for (DynamicSememeColumnInfo ci : columns)
			{
				//byte arrays are not currently indexable withing lucene
				if (ci.getColumnDataType() != DynamicSememeDataType.BYTEARRAY && ci.getIndexConfig())
				{
					temp.add(new DynamicSememeIntegerImpl(ci.getColumnOrder()));
				}
			}
			if (temp.size() > 0)
			{
				return new DynamicSememeArrayImpl<DynamicSememeData>(temp.toArray(new DynamicSememeData[temp.size()]));
			}
		}
		return null;
	}
}
