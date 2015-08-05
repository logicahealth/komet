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
package gov.vha.isaac.ochre.api.component.sememe.version;


import javax.naming.InvalidNameException;

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescriptionBI;

/**
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public interface DynamicSememe<T extends DynamicSememe<T>> extends SememeVersion<T> {
    /**
     * @return All of the data columns that are part of this DynamicSememe. See
     *         {@link #getData(int)}. May be empty, will not be null.
     */
    DynamicSememeDataBI[] getData();
    
    /**
     * The type and data (if any) in the specified column of the DynamicSememe.
     * 
     * @param columnNumber
     * @return The SememeMemberBI which contains the type and data (if any) for
     *         the specified column
     * @throws IndexOutOfBoundsException
     */
    DynamicSememeDataBI getData(int columnNumber) throws IndexOutOfBoundsException;
    
    /**
     * The type and data (if any) in the specified column of the DynamicSememe.
     * 
     * @param columnName
     * @return The DynamicSememeDataBI which contains the type and data (if any) for the specified column
     * @throws InvalidNameException
     */
    DynamicSememeDataBI getData(String columnName) throws InvalidNameException;

	
	/**
     * A convenience method that reads the concept referenced in {@link #getAssemblageNid()} and returns the actual column
     * information that is contained within that concept.
     */
    public DynamicSememeUsageDescriptionBI getDynamicSememeUsageDescription();
}
