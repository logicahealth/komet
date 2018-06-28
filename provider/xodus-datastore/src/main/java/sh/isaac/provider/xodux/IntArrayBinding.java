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
package sh.isaac.provider.xodux;

import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.ByteIterator;

/**
 * Utility code to convert byte[] objects for xodus storage
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class IntArrayBinding
{
	public static ArrayByteIterable intArrayToEntry(final int[] object)
	{
		byte[] bytes = new byte[object.length * 4];

		for (int objectPos = 0; objectPos < object.length; objectPos++)
		{
			int offset = objectPos * 4;
			bytes[0 + offset] = (byte) (object[objectPos] >> 24);
			bytes[1 + offset] = (byte) (object[objectPos] >> 16);
			bytes[2 + offset] = (byte) (object[objectPos] >> 8);
			bytes[3 + offset] = (byte) (object[objectPos] >> 0);
		}
		return new ArrayByteIterable(bytes);
	}

	public static int[] entryToIntArray(final ByteIterable entry)
	{
		byte[] bytes = new byte[entry.getLength()];
		int[] result = new int[bytes.length / 4];
		
		ByteIterator it = entry.iterator();
		int i = 0;
		while (it.hasNext())
		{
			bytes[i++] = it.next();
		}
		
		for (int objectPos = 0; objectPos < result.length; objectPos++) 
		{
			int offset = objectPos * 4;
			result[objectPos] = ((bytes[0 + offset] << 24) | ((bytes[1 + offset] & 0xFF) << 16) 
					| ((bytes[2 + offset] & 0xFF) << 8) | ((bytes[3 + offset] & 0xFF) << 0));
		}
		return result;
	}
}
