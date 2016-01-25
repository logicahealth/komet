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

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeInteger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * 
 * {@link DynamicSememeIntegerImpl}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeIntegerImpl extends DynamicSememeDataImpl implements DynamicSememeInteger {

	private ObjectProperty<Integer> property_;
	
	protected DynamicSememeIntegerImpl(byte[] data)
	{
		super(data);
	}

	protected DynamicSememeIntegerImpl(byte[] data, int assemblageSequence, int columnNumber)
	{
		super(data, assemblageSequence, columnNumber);
	}
	
	public DynamicSememeIntegerImpl(int integer) {
		super();
		data_ = intToByteArray(integer);
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicIntegerBI#getDataInteger()
	 */
	@Override
	public int getDataInteger() {
		return getIntFromByteArray(data_);
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObject()
	 */
	@Override
	public Object getDataObject() {
		return getDataInteger();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObjectProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<?> getDataObjectProperty() {
		return getDataIntegerProperty();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicIntegerBI#getDataIntegerProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<Integer> getDataIntegerProperty()  {
		if (property_ == null) {
			property_ = new SimpleObjectProperty<>(null, getName(), getDataInteger());
		}
		return property_;
	}

	/**
	 * Returns a 4 byte array
	 */
	protected static byte[] intToByteArray(int integer) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (integer >> 24);
		bytes[1] = (byte) (integer >> 16);
		bytes[2] = (byte) (integer >> 8);
		bytes[3] = (byte) (integer >> 0);
		return bytes;
	}

	protected static int getIntFromByteArray(byte[] bytes) {
		return ((bytes[0] << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | ((bytes[3] & 0xFF) << 0));
	}
}
