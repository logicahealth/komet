/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
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
package sh.isaac.api.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.util.function.Consumer;

/**
 * Utilities to Translate between DataInput / DataOutput and ByteArray buffers
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class DataToBytesUtils
{
	public static byte[] getBytes(ThrowingConsumer<DataOutput> getBytesFrom)
	{
		ByteArrayOutputStream backing = new ByteArrayOutputStream();
		DataOutput dataOutput = new DataOutputStream(backing);
		getBytesFrom.accept(dataOutput);
		return backing.toByteArray();
	}
	
	public static DataInput getDataInput(byte[] bytes)
	{
		ByteArrayInputStream backing = new ByteArrayInputStream(bytes);
		DataInput dataInput= new DataInputStream(backing);
		return dataInput;
	}
	
	@FunctionalInterface
	public interface ThrowingConsumer<T> extends Consumer<T>
	{
		@Override
		default void accept(final T elem)
		{
			try
			{
				acceptThrows(elem);
			}
			catch (final Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		void acceptThrows(T elem) throws Exception;
	}
}