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
import org.ihtsdo.otf.tcc.api.refex4.data.dataTypes.RefexNidBI;
import org.ihtsdo.otf.tcc.model.cc.refex4.data.RefexData;

/**
 * 
 * {@link RefexNid}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexNid extends RefexData implements RefexNidBI {
	
	private ObjectProperty<Integer> property_;

	public RefexNid(int nid, String name) throws PropertyVetoException {
		super(RefexDataType.NID, name);
		data_ = RefexInteger.intToByteArray(nid);
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex4.data.dataTypes.RefexNidBI#getDataNid()
	 */
	@Override
	public int getDataNid() {
		return RefexInteger.getIntFromByteArray(data_);
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex4.data.RefexDataBI#getDataObject()
	 */
	@Override
	public Object getDataObject() {
		return getDataNid();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex4.data.RefexDataBI#getDataObjectProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<?> getDataObjectProperty() {
		return getDataNidProperty();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refex4.data.dataTypes.RefexNidBI#getDataNidProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<Integer> getDataNidProperty() {
		if (property_ == null) {
			property_ = new SimpleObjectProperty<>(getDataNid(), getName());
		}
		return property_;
	}
}
