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

import java.beans.PropertyVetoException;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.TtkRefexDynamicData;

/**
 * 
 * {@link TtkRefexDynamicInteger}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class TtkRefexDynamicInteger extends TtkRefexDynamicData
{
	public TtkRefexDynamicInteger(byte[] data)
	{
		super(data);
	}

	public TtkRefexDynamicInteger(int integer) throws PropertyVetoException
	{
		super();
		data_ = intToByteArray(integer);
	}

	public int getDataInteger()
	{
		return getIntFromByteArray(data_);
	}

	/**
	 * @see org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.TtkRefexDynamicData#getDataObject()
	 */
	@Override
	public Object getDataObject()
	{
		return getDataInteger();
	}

	protected static byte[] intToByteArray(int integer)
	{
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (integer >> 24);
		bytes[1] = (byte) (integer >> 16);
		bytes[2] = (byte) (integer >> 8);
		bytes[3] = (byte) (integer >> 0);
		return bytes;
	}

	protected static int getIntFromByteArray(byte[] bytes)
	{
		return ((bytes[0] << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | ((bytes[3] & 0xFF) << 0));
	}
}
