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

package gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes;

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataBI;
import javafx.beans.property.ReadOnlyObjectProperty;

/**
 * 
 * {@link DynamicSememeArrayBI}
 * 
 * A storage class that allows the creation of a refex column which holds an array of a specific type of {@link DynamicSememeDataBI}
 * items, such as an array of {@link DynamicSememeStringBI}.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public interface DynamicSememeArrayBI<T extends DynamicSememeDataBI> extends DynamicSememeDataBI
{
	public T[] getDataArray();
	
	public ReadOnlyObjectProperty<T[]> getDataArrayProperty();
}
