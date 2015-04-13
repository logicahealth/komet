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
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicBooleanBI;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;

/**
 * 
 * {@link RefexDynamicBoolean}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamicBoolean extends RefexDynamicData implements RefexDynamicBooleanBI {

	private ObjectProperty<Boolean> property_;

	protected RefexDynamicBoolean(byte[] data)
	{
		super(data);
	}
	
	protected RefexDynamicBoolean(byte[] data, int assemblageNid, int columnNumber)
	{
		super(data, assemblageNid, columnNumber);
	}
	
	public RefexDynamicBoolean(boolean b) throws PropertyVetoException {
		super();
		data_ = (b ? new byte[] { 1 } : new byte[] { 0 });
	}

	/**
	 * @throws ContradictionException 
	 * @throws IOException 
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicBooleanBI#getDataBooleanProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<Boolean> getDataBooleanProperty() throws IOException, ContradictionException {
		if (property_ == null) {
			property_ = new SimpleObjectProperty<Boolean>(null, getName(), getDataBoolean());
		}
		return property_;
	}

	/**
	 * @throws ContradictionException 
	 * @throws IOException 
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObject()
	 */
	@Override
	public ReadOnlyObjectProperty<?> getDataObjectProperty() throws IOException, ContradictionException {
		return getDataBooleanProperty();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicBooleanBI#getDataBoolean()
	 */
	@Override
	public boolean getDataBoolean() {
		return data_[0] == 1 ? true : false;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObject()
	 */
	@Override
	public Object getDataObject() {
		return getDataBoolean();
	}
}
