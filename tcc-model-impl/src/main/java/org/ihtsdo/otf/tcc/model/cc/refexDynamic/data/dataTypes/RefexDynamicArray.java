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
package org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.dataTypes;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicArrayBI;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;

/**
 * {@link RefexDynamicArray}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class RefexDynamicArray<T extends RefexDynamicData> extends RefexDynamicData implements RefexDynamicArrayBI<T>
{
	private ReadOnlyObjectProperty<T[]> property_;
	
	protected RefexDynamicArray(byte[] data)
	{
		super(data);
	}
	
	protected RefexDynamicArray(byte[] data, int assemblageNid, int columnNumber)
	{
		super(data, assemblageNid, columnNumber);
	}
	
	public RefexDynamicArray(T[] dataArray) throws PropertyVetoException {
		super();
		
		if (dataArray == null)
		{
			throw new PropertyVetoException("The dataArray cannot be null", null);
		}
		byte[][] allData = new byte[dataArray.length][];
		
		long totalBytes = 0;
		int i = 0;
		for (T item : dataArray)
		{
			allData[i] = item.getData();
			totalBytes += allData[i++].length;
		}
		
		//data size + 4 bytes for the leading type token, + 4 bytes for the length of each data item
		if ((totalBytes + 4 + ((i + 1) * 4)) > Integer.MAX_VALUE)
		{
			throw new PropertyVetoException("To much data to store", null);
		}
		
		int expectedDataSize = (int)totalBytes + 4 + (i * 4);
		data_ = new byte[expectedDataSize];
		ByteBuffer data = ByteBuffer.wrap(data_);
		
		//First 4 bytes will be the type token
		data.putInt(RefexDynamicDataType.classToType(dataArray.getClass().getComponentType()).getTypeToken());
		
		//Then, for each data item, 4 bytes for the int size marker of the data, then the data.
		for (byte[] item : allData)
		{
			data.putInt(item.length);
			data.put(item);
		}
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObject()
	 */
	@Override
	public Object getDataObject()
	{
		return getDataArray();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObjectProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<?> getDataObjectProperty() throws IOException, ContradictionException
	{
		return getDataArrayProperty();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicArrayBI#getArrayDataType()
	 */
	@Override
	public RefexDynamicDataType getArrayDataType()
	{
		// read the first 4 bytes
		ByteBuffer bb = ByteBuffer.wrap(data_);
		int type = bb.getInt();
		return RefexDynamicDataType.getFromToken(type);
	}
	
	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicArrayBI#getDataArray()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T[] getDataArray()
	{
		ArrayList<T> result = new ArrayList<>();
		
		RefexDynamicDataType dt = getArrayDataType();
		ByteBuffer bb = ByteBuffer.wrap(data_);
		bb.getInt();  //skip the first 4
		
		while (bb.hasRemaining())
		{
			int nextReadSize = bb.getInt();
			byte[] dataArray = new byte[nextReadSize];
			bb.get(dataArray);
			T data = (T)RefexDynamicTypeToClassUtility.typeToClass(dt, dataArray);
			result.add(data);
		}

		return (T[]) result.toArray((T[])Array.newInstance(RefexDynamicTypeToClassUtility.implClassForType(dt), result.size()));
	}

	/**
	 * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicArrayBI#getDataArrayProperty()
	 */
	@Override
	public ReadOnlyObjectProperty<T[]> getDataArrayProperty() throws IOException, ContradictionException
	{
		if (property_ == null) {
			property_ = new SimpleObjectProperty<T[]>(null, getName(), getDataArray());
		}
		return property_;
	}
}
