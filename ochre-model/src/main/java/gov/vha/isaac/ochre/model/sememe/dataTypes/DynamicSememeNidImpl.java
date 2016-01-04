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

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeNid;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * 
 * {@link DynamicSememeNidImpl}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeNidImpl extends DynamicSememeDataImpl implements DynamicSememeNid {
	
	private ObjectProperty<Integer> property_;
	
	protected DynamicSememeNidImpl(byte[] data)
	{
		super(data);
	}
	protected DynamicSememeNidImpl(byte[] data, int assemblageSequence, int columnNumber)
	{
		super(data, assemblageSequence, columnNumber);
	}
	
	public DynamicSememeNidImpl(int nid) {
		super();
		data_ = DynamicSememeIntegerImpl.intToByteArray(nid);
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicNidBI#getDataNid()
	 */
	@Override
	public int getDataNid() {
		return DynamicSememeIntegerImpl.getIntFromByteArray(data_);
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObject()
	 */
	@Override
	public Object getDataObject() {
		return getDataNid();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObjectProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<?> getDataObjectProperty()  {
		return getDataNidProperty();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicNidBI#getDataNidProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<Integer> getDataNidProperty()  {
		if (property_ == null) {
			property_ = new SimpleObjectProperty<>(null, getName(), getDataNid());
		}
		return property_;
	}
}
