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

package org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes;

import java.beans.PropertyVetoException;
import java.nio.ByteBuffer;
import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicUUIDBI;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexData;

/**
 * 
 * {@link RefexUUID}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexUUID extends RefexData implements RefexDynamicUUIDBI {

	private ObjectProperty<UUID> property_;

	public RefexUUID(UUID uuid, String name) throws PropertyVetoException {
		super(RefexDynamicDataType.UUID, name);
		ByteBuffer b = ByteBuffer.allocate(16);
		b.putLong(uuid.getMostSignificantBits());
		b.putLong(uuid.getLeastSignificantBits());
		data_ = b.array();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicUUIDBI#getDataUUID()
	 */
	@Override
	public UUID getDataUUID() {
		ByteBuffer b = ByteBuffer.wrap(data_);
		long most = b.getLong();
		long least = b.getLong();
		return new UUID(most, least);
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObject()
	 */
	@Override
	public Object getDataObject() {
		return getDataUUID();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObjectProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<?> getDataObjectProperty() {
		return getDataUUIDProperty();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicUUIDBI#getDataUUIDProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<UUID> getDataUUIDProperty() {
		if (property_ == null) {
			property_ = new SimpleObjectProperty<>(getDataUUID(), getName());
		}
		return property_;
	}

	public static void main(String[] args) throws PropertyVetoException {
		// TODO make this into a JUnit test
		UUID u = UUID.randomUUID();
		System.out.println(u);
		RefexUUID ru = new RefexUUID(u, "fo");
		System.out.println(ru.getDataUUID());
	}
}
