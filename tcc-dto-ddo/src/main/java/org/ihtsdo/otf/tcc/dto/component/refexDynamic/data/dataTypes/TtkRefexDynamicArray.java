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

package org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes;

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import java.beans.PropertyVetoException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.TtkRefexDynamicData;

/**
 * 
 * {@link TtkRefexDynamicArray}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class TtkRefexDynamicArray<T extends TtkRefexDynamicData> extends TtkRefexDynamicData {

	public TtkRefexDynamicArray(byte[] data)
	{
		super(data);
	}
	
	public TtkRefexDynamicArray(T[] dataArray) throws PropertyVetoException {
		super();
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
			data.putInt(TtkRefexDynamicData.classToType(dataArray[i].getClass()).getTypeToken());
			data.putInt(allData[i].length);
			data.put(allData[i]);
		}
	}

	@SuppressWarnings("unchecked")
	public T[] getDataArray() {
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
			T data = (T)TtkRefexDynamicData.typeToClass(dt, dataArray);
			result.add(data);
		}
		return (T[]) result.toArray((T[])Array.newInstance(foundTypes.size() > 1 ? TtkRefexDynamicData.class 
				: TtkRefexDynamicData.implClassForType(foundTypes.iterator().next()), result.size()));
	}
	
	

	@Override
	public byte[] getData()
	{
		return super.getData();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.TtkRefexDynamicData#getDataObject()
	 */
	@Override
	public Object getDataObject() {
		return getDataArray();
	}
}
