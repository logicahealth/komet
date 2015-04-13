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
import java.io.IOException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicFloatBI;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;

/**
 * 
 * {@link RefexDynamicFloat}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamicFloat extends RefexDynamicData implements RefexDynamicFloatBI {

	private ObjectProperty<Float> property_;

	protected RefexDynamicFloat(byte[] data)
	{
		super(data);
	}

	protected RefexDynamicFloat(byte[] data, int assemblageNid, int columnNumber)
	{
		super(data, assemblageNid, columnNumber);
	}
	
	public RefexDynamicFloat(float f) throws PropertyVetoException {
		super();
		data_ = RefexDynamicInteger.intToByteArray(Float.floatToIntBits(f));
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicFloatBI#getDataFloat()
	 */
	@Override
	public float getDataFloat() {
		return Float.intBitsToFloat(RefexDynamicInteger.getIntFromByteArray(data_));
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObject()
	 */
	@Override
	public Object getDataObject() {
		return getDataFloat();
	}

	/**
	 * @throws ContradictionException 
	 * @throws IOException 
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObjectProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<?> getDataObjectProperty() throws IOException, ContradictionException {
		return getDataFloatProperty();
	}

	/**
	 * @throws ContradictionException 
	 * @throws IOException 
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicFloatBI#getDataFloatProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<Float> getDataFloatProperty() throws IOException, ContradictionException {
		if (property_ == null) {
			property_ = new SimpleObjectProperty<>(null, getName(), getDataFloat());
		}
		return property_;
	}
}
