/*
 * Copyright 2020 Mind Computing Inc, Sagebits LLC
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
package sh.isaac.api.datastore.extendedStore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

public enum SimpleTypeStoreHandler
{
	STRING, INTEGER, UUID, BYTE_ARRAY, INSTANT, UNKNOWN;

	protected void write(Object data, DataOutputStream dos) throws IOException
	{
		switch (this)
		{
			case INTEGER:
				dos.writeInt((Integer) data);
				break;
			case STRING:
				dos.writeUTF((String) data);
				break;
			case UUID:
				dos.writeLong(((UUID) data).getMostSignificantBits());
				dos.writeLong(((UUID) data).getLeastSignificantBits());
				break;
			case BYTE_ARRAY:
				dos.writeInt(((byte[])data).length);
				dos.write((byte[])data);
				break;
			case INSTANT:
				dos.writeLong(((Instant)data).getEpochSecond());
				dos.writeInt(((Instant)data).getNano());
				break;
			default :
				throw new RuntimeException("Someone goofed");
		}
	}

	protected Object read(DataInputStream dis) throws IOException
	{
		switch (this)
		{
			case INTEGER:
				return dis.readInt();
			case STRING:
				return dis.readUTF();
			case UUID:
				return new UUID(dis.readLong(), dis.readLong());
			case BYTE_ARRAY:
				int size = dis.readInt();
				return dis.readNBytes(size);
			case INSTANT:
				return Instant.ofEpochSecond(dis.readLong(), dis.readInt());
			default :
				throw new RuntimeException("Someone goofed " + this.name());
		}
	}

	public static SimpleTypeStoreHandler forType(Class<? extends Object> classType)
	{
		String className = classType.getSimpleName();
		if (className.contentEquals("String"))
		{
			return STRING;
		}
		else if (className.equals("Integer"))
		{
			return INTEGER;
		}
		else if (className.equals("UUID"))
		{
			return UUID;
		}
		else if (className.equals("byte[]"))
		{
			return BYTE_ARRAY;
		}
		else if (className.equals("Instant"))
		{
			return INSTANT;
		}
		else
		{
			throw new RuntimeException("Unsupported type: " + className);
		}
	}

	public static SimpleTypeStoreHandler fromStream(DataInputStream dis) throws IOException
	{
		return SimpleTypeStoreHandler.valueOf(dis.readUTF());
	}
}
