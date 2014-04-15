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
import java.nio.ByteBuffer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexLongBI;
import org.ihtsdo.otf.tcc.model.cc.refex4.data.RefexData;

/**
 * 
 * {@link RefexLong}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexLong extends RefexData implements RefexLongBI {

	private ObjectProperty<Long> property_;

	public RefexLong(long l, String name) throws PropertyVetoException {
		super(RefexDataType.LONG, name);
		data_ = ByteBuffer.allocate(8).putLong(l).array();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexLongBI#getDataLong()
	 */
	@Override
	public long getDataLong() {
		return ByteBuffer.wrap(data_).getLong();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDataBI#getDataObject()
	 */
	@Override
	public Object getDataObject() {
		return getDataLong();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDataBI#getDataObjectProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<?> getDataObjectProperty() {
		return getDataLongProperty();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexLongBI#getDataLongProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<Long> getDataLongProperty() {
		if (property_ == null) {
			property_ = new SimpleObjectProperty<>(getDataLong(), getName());
		}
		return property_;
	}

	public static void main(String[] args) throws PropertyVetoException {
		// TODO turn this into a JUNit test
		RefexLong l = new RefexLong(5, "foo");

		System.out.println(l.getDataLong());
		System.out.println(l.getDataObject());
		System.out.println(Long.MAX_VALUE);
		l = new RefexLong(Long.MAX_VALUE, "foo");
		System.out.println(l.getDataLong());
		System.out.println(l.getDataObject());
		l = new RefexLong(Long.MIN_VALUE, "foo");
		System.out.println(Long.MIN_VALUE);
		System.out.println(l.getDataLong());
		System.out.println(l.getDataObject());
	}
}
