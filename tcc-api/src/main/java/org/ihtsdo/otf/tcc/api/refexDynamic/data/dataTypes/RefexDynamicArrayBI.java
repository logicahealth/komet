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

package org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes;

import java.io.IOException;
import javafx.beans.property.ReadOnlyObjectProperty;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;

/**
 * 
 * {@link RefexDynamicArrayBI}
 * 
 * A storage class that allows the creation of a refex column which holds an array of a specific type of {@link RefexDynamicDataBI}
 * items, such as an array of {@link RefexDynamicStringBI}.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public interface RefexDynamicArrayBI<T extends RefexDynamicDataBI> extends RefexDynamicDataBI
{
	/**
	 * Returns the specific type of the items in the array.
	 */
	public RefexDynamicDataType getArrayDataType();
	
	public T[] getDataArray();
	
	public ReadOnlyObjectProperty<T[]> getDataArrayProperty() throws IOException, ContradictionException;
}
