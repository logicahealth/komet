/*
 * Copyright 2010 International Health Terminology Standards Development Organisation.
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

package org.ihtsdo.otf.tcc.model.cc.refex4.data.dataTypes;

import java.beans.PropertyVetoException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.ihtsdo.otf.tcc.api.refex4.data.RefexDataType;
import org.ihtsdo.otf.tcc.api.refex4.data.dataTypes.RefexByteArrayBI;
import org.ihtsdo.otf.tcc.model.cc.refex4.data.RefexData;

/**
 * 
 * {@link RefexByteArray}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexByteArray extends RefexData implements RefexByteArrayBI {

	private ObjectProperty<Byte[]> property_;

	public RefexByteArray(byte[] bytes, String name) throws PropertyVetoException {
		super(RefexDataType.BYTEARRAY, name);
		data_ = bytes;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex4.data.dataTypes.RefexByteArrayBI#getDataByteArray()
	 */
	@Override
	public byte[] getDataByteArray() {
		return data_;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex4.data.RefexDataBI#getDataObject()
	 */
	@Override
	public Object getDataObject() {
		return getDataByteArray();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex4.data.RefexDataBI#getDataObjectProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<?> getDataObjectProperty() {
		return getDataByteArrayProperty();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex4.data.dataTypes.RefexByteArrayBI#getDataByteArrayProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<Byte[]> getDataByteArrayProperty() {
		if (property_ == null) {
			property_ = new SimpleObjectProperty<Byte[]>(data_, getName());
		}
		return property_;
	}
}
