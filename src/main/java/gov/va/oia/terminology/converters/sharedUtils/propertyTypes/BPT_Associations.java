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

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;

/**
 * @author Daniel Armbrust
 * 
 * Associations get loaded using the new add-on association API (internally represented as sememes)
 * 
 * These get ignored by the classifier, for example.
 * 
 */
public class BPT_Associations extends PropertyType
{
	public BPT_Associations(String terminologyName)
	{
		super("Associations Types", terminologyName + " Association Type", false, null);
	}
	
	public BPT_Associations(String propertyTypeDescription, String terminologyName)
	{
		super(propertyTypeDescription, terminologyName + " Association Type", false, null);
	}

	@Override
	public Property addProperty(Property property)
	{
		if (!(property instanceof PropertyAssociation))
		{
			throw new RuntimeException("Must add PropertyAssociation objects to BPT_Associations type");
		}
		return super.addProperty(property);
	}

	//Override all of these as unsupported, as, we require only PropertyAssociation object here.
	
	@Override
	public Property addProperty(String propertyNameFSN)
	{
		throw new UnsupportedOperationException();	}

	@Override
	public Property addProperty(String propertyNameFSN, int propertySubType)
	{
		throw new UnsupportedOperationException();	}

	@Override
	public Property addProperty(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyDefinition)
	{
		throw new UnsupportedOperationException();	}

	@Override
	public Property addProperty(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyAltName, String sourcePropertyDefinition)
	{
		throw new UnsupportedOperationException();	}

	@Override
	public Property addProperty(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyDefinition, boolean disabled)
	{
		throw new UnsupportedOperationException();	}

	@Override
	public Property addProperty(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyDefinition, boolean disabled, int propertySubType)
	{
		throw new UnsupportedOperationException();	}

	@Override
	public Property addProperty(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyAltName, String sourcePropertyDefinition,
			boolean disabled, int propertySubType, DynamicSememeColumnInfo[] dataColumnForDynamicRefex)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Property addProperty(String propertyNameFSN, int minVersion, int maxVersion)
	{
		return super.addProperty(propertyNameFSN, minVersion, maxVersion);
	}

	@Override
	public Property addProperty(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyDefinition, int minVersion, int maxVersion,
			boolean disabled)
	{
		return super.addProperty(sourcePropertyNameFSN, sourcePropertyPreferredName, sourcePropertyDefinition, minVersion, maxVersion, disabled);
	}

	@Override
	public Property addProperty(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyDefinition, int minVersion, int maxVersion,
			boolean disabled, int propertySubType)
	{
		return super.addProperty(sourcePropertyNameFSN, sourcePropertyPreferredName, sourcePropertyDefinition, minVersion, maxVersion, disabled, propertySubType);
	}
}
