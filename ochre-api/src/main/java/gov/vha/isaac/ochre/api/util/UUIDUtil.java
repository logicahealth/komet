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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.api.util;

import java.util.Optional;
import java.util.UUID;

/**
 * Various UUID related utilities
 * 
 * @author darmbrust
 * @author kec
 */
public class UUIDUtil
{
	public static Optional<UUID> getUUID(String string)
	{
		if (string == null)
		{
			Optional.empty();
		}
		if (string.length() != 36)
		{
			Optional.empty();
		}
		try
		{
			return Optional.of(UUID.fromString(string));
		}
		catch (IllegalArgumentException e)
		{
			return Optional.empty();
		}
	}

	public static boolean isUUID(String string)
	{
		return (getUUID(string).isPresent());
	}
	
	public static long[] convert(UUID id)
	{
		long[] data = new long[2];
		data[0] = id.getMostSignificantBits();
		data[1] = id.getLeastSignificantBits();
		return data;
	}

	public static UUID convert(long[] data)
	{
		return new UUID(data[0], data[1]);
	}
}
