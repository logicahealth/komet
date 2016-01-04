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

package gov.vha.isaac.ochre.model.sememe.dataTypes;

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeByteArray;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * 
 * {@link DynamicSememeByteArrayImpl}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeByteArrayImpl extends DynamicSememeDataImpl implements DynamicSememeByteArray {

	private ObjectProperty<byte[]> property_;

	protected DynamicSememeByteArrayImpl(byte[] data, int assemblageSequence, int columnNumber)
	{
		super(data, assemblageSequence, columnNumber);
	}
	
	public DynamicSememeByteArrayImpl(byte[] bytes) {
		super(bytes);
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicByteArrayBI#getDataByteArray()
	 */
	@Override
	public byte[] getDataByteArray() {
		return data_;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObject()
	 */
	@Override
	public Object getDataObject() {
		return getDataByteArray();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObjectProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<?> getDataObjectProperty()  {
		return getDataByteArrayProperty();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicByteArrayBI#getDataByteArrayProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<byte[]> getDataByteArrayProperty()  {
		if (property_ == null) {
			property_ = new SimpleObjectProperty<byte[]>(null, getName(), data_);
		}
		return property_;
	}
}
