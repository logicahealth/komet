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

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArrayBI;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * {@link DynamicSememeArray}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class DynamicSememeArray<T extends DynamicSememeDataBI> extends DynamicSememeData implements DynamicSememeArrayBI<T>
{
	private ReadOnlyObjectProperty<T[]> property_;
	
	protected DynamicSememeArray(byte[] data)
	{
		super(data);
	}
	
	protected DynamicSememeArray(byte[] data, int assemblageSequence, int columnNumber)
	{
		super(data, assemblageSequence, columnNumber);
	}
	
	public DynamicSememeArray(T[] dataArray) {
		super();
		
		if (dataArray == null)
		{
			throw new RuntimeException("The dataArray cannot be null", null);
		}
		byte[][] allData = new byte[dataArray.length][];
		
		long totalBytes = 0;
		
		for (int i = 0; i < dataArray.length; i++)
		{
			allData[i] = dataArray[i].getData();
			totalBytes += allData[i].length;
		}
		
		
		//data size + 4 bytes for the type token (per item) + 4 bytes for the length of each data item
		if ((totalBytes + (new Integer(dataArray.length).longValue() * 8l)) > Integer.MAX_VALUE)
		{
			throw new RuntimeException("To much data to store", null);
		}
		
		int expectedDataSize = (int)totalBytes + (dataArray.length * 8);
		data_ = new byte[expectedDataSize];
		ByteBuffer data = ByteBuffer.wrap(data_);
		
		//Then, for each data item, 4 bytes for the type, 4 bytes for the int size marker of the data, then the data.
		for (int i = 0; i < dataArray.length; i++)
		{
			//First 4 bytes will be the type token
			data.putInt(DynamicSememeDataType.classToType(dataArray[i].getClass()).getTypeToken());
			data.putInt(allData[i].length);
			data.put(allData[i]);
		}
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.DynamicSememe.data.DynamicSememeDataBI#getDataObject()
	 */
	@Override
	public Object getDataObject()
	{
		return getDataArray();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.DynamicSememe.data.DynamicSememeDataBI#getDataObjectProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<?> getDataObjectProperty()
	{
		return getDataArrayProperty();
	}
	
	/**
	 * @see org.ihtsdo.otf.tcc.api.DynamicSememe.data.dataTypes.DynamicSememeArrayBI#getDataArray()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T[] getDataArray()
	{
		ArrayList<T> result = new ArrayList<>();
		ByteBuffer bb = ByteBuffer.wrap(data_);
		
		HashSet<DynamicSememeDataType> foundTypes = new HashSet<>();
		
		while (bb.hasRemaining())
		{
			int type = bb.getInt();
			DynamicSememeDataType dt = DynamicSememeDataType.getFromToken(type);
			foundTypes.add(dt);
			int nextReadSize = bb.getInt();
			byte[] dataArray = new byte[nextReadSize];
			bb.get(dataArray);
			T data = (T)DynamicSememeTypeToClassUtility.typeToClass(dt, dataArray);
			result.add(data);
		}
		return (T[]) result.toArray((T[])Array.newInstance(foundTypes.size() > 1 ? DynamicSememeData.class 
				: DynamicSememeTypeToClassUtility.implClassForType(foundTypes.iterator().next()), result.size()));
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.DynamicSememe.data.dataTypes.DynamicSememeArrayBI#getDataArrayProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<T[]> getDataArrayProperty() 
	{
		if (property_ == null) {
			property_ = new SimpleObjectProperty<T[]>(null, getName(), getDataArray());
		}
		return property_;
	}
}
