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
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexIntegerBI;
import org.ihtsdo.otf.tcc.model.cc.refex4.data.RefexData;

/**
 * 
 * {@link RefexInteger}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexInteger extends RefexData implements RefexIntegerBI {

	private ObjectProperty<Integer> property_;

	public RefexInteger(int integer, String name) throws PropertyVetoException {
		super(RefexDataType.INTEGER, name);
		data_ = intToByteArray(integer);
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexIntegerBI#getDataInteger()
	 */
	@Override
	public int getDataInteger() {
		return getIntFromByteArray(data_);
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDataBI#getDataObject()
	 */
	@Override
	public Object getDataObject() {
		return getDataInteger();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDataBI#getDataObjectProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<?> getDataObjectProperty() {
		return getDataIntegerProperty();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexIntegerBI#getDataIntegerProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<Integer> getDataIntegerProperty() {
		if (property_ == null) {
			property_ = new SimpleObjectProperty<>(getDataInteger(), getName());
		}
		return property_;
	}

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

	public static void main(String[] args) throws PropertyVetoException {
		// TODO turn this into a JUNit test
		RefexInteger i = new RefexInteger(5, "foo");

		System.out.println(i.getDataInteger());
		System.out.println(i.getDataObject());
		System.out.println(Integer.MAX_VALUE);
		i = new RefexInteger(Integer.MAX_VALUE, "foo");
		System.out.println(i.getDataInteger());
		System.out.println(i.getDataObject());
		i = new RefexInteger(Integer.MIN_VALUE, "foo");
		System.out.println(Integer.MIN_VALUE);
		System.out.println(i.getDataInteger());
		System.out.println(i.getDataObject());
	}
}
