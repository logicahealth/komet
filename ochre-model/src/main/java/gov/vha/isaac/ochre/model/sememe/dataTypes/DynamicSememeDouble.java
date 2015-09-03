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

import java.nio.ByteBuffer;

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeDoubleBI;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * 
 * {@link DynamicSememeDouble}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeDouble extends DynamicSememeData implements DynamicSememeDoubleBI {

	private ObjectProperty<Double> property_;
	
	protected DynamicSememeDouble(byte[] data)
	{
		super(data);
	}

	protected DynamicSememeDouble(byte[] data, int assemblageSequence, int columnNumber)
	{
		super(data, assemblageSequence, columnNumber);
	}
	
	public DynamicSememeDouble(double d) {
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
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObjectProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<?> getDataObjectProperty() {
		return getDataDoubleProperty();
	}

	/**  
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicDoubleBI#getDataDoubleProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<Double> getDataDoubleProperty() {
            if (property_ == null) {
                property_ = new SimpleObjectProperty<>(null, getName(), getDataDouble());
            }
            return property_;
	}
}
