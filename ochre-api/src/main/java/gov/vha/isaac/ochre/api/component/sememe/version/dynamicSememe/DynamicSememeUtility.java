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
	
	/**
	 * A convenience method to read the values that should be used as the name and description for a data 
	 * column in a dynamic sememe from an existing concept
	 * @return an array of two strings, first entry name, seconde entry description
	 */
	public String[] readDynamicSememeColumnNameDescription(UUID columnDescriptionConcept);
}
