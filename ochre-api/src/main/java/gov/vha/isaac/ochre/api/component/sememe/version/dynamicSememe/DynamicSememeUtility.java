package gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe;

import java.util.UUID;

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

import org.jvnet.hk2.annotations.Contract;

import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArray;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;

/**
 * {@link DynamicSememeUtility}
 * 
 * This class exists as an interface primarily to allow classes in ochre-api and ochre-impl to have access to these methods
 * that need to be implemented further down the dependency tree (with access to metadata, etc)
 * 
 *  Code in ochre-util and ochre-api will access the impl via HK2.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface DynamicSememeUtility {
	
	/**
	 * Convenience method to read all of the extended details of a DynamicSememeAssemblage
	 * @param assemblageNidOrSequence
	 */
	public DynamicSememeUsageDescription readDynamicSememeUsageDescription(int assemblageNidOrSequence);
	
	public DynamicSememeData[] configureDynamicSememeDefinitionDataForColumn(DynamicSememeColumnInfo ci);
	
	public DynamicSememeData[] configureDynamicSememeRestrictionData(ObjectChronologyType referencedComponentRestriction, 
			SememeType referencedComponentSubRestriction);
	
	/**
	 * This will return the column index configuration that will mark each supplied column that is indexable, for indexing.
	 * Returns null, if no columns need indexing.
	 */
	public DynamicSememeArray<DynamicSememeData> configureColumnIndexInfo(DynamicSememeColumnInfo[] columns);
	
	public DynamicSememeString createDynamicStringData(String value);

}
