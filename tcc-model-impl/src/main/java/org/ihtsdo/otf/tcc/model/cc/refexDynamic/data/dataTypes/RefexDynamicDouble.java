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
import java.nio.ByteBuffer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicDoubleBI;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;

/**
 * 
 * {@link RefexDynamicDouble}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamicDouble extends RefexDynamicData implements RefexDynamicDoubleBI {

	private ObjectProperty<Double> property_;
	
	protected RefexDynamicDouble(byte[] data)
	{
		super(data);
	}

	protected RefexDynamicDouble(byte[] data, int assemblageNid, int columnNumber)
	{
		super(data, assemblageNid, columnNumber);
	}
	
	public RefexDynamicDouble(double d) throws PropertyVetoException {
		super();
		data_ = ByteBuffer.allocate(8).putDouble(d).array();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicDoubleBI#getDataDouble()
	 */
	@Override
	public double getDataDouble() {
		return ByteBuffer.wrap(data_).getDouble();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObject()
	 */
	@Override
	public Object getDataObject() {
		return getDataDouble();
	}

	/**
	 * @throws ContradictionException 
	 * @throws IOException 
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObjectProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<?> getDataObjectProperty() throws IOException, ContradictionException {
		return getDataDoubleProperty();
	}

	/**
	 * @throws ContradictionException 
	 * @throws IOException 
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicDoubleBI#getDataDoubleProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<Double> getDataDoubleProperty() throws IOException, ContradictionException {
		if (property_ == null) {
			property_ = new SimpleObjectProperty<>(null, getName(), getDataDouble());
		}
		return property_;
	}
}
