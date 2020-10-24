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
import java.util.Map;
import java.util.function.Function;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.datastore.ExtendedStoreDataSerializer;

@Service(name = "SimpleTypeStoreMap")
@PerLookup
public class SimpleTypeStoreMap extends ExtendedStoreWithSerializer<Object, Object> implements ExtendedStoreDataSerializer<Object, Object>
{
	private SimpleTypeStoreHandler keyType = SimpleTypeStoreHandler.UNKNOWN;
	private SimpleTypeStoreHandler valueType = SimpleTypeStoreHandler.UNKNOWN;;

	public SimpleTypeStoreMap()
	{
	}

	@Override
	public String getServiceName()
	{
		return "SimpleTypeStoreMap";
	}
	
	@Override
	public Map.Entry<Object, Object> readEntry(DataInputStream dis) throws IOException
	{
		Object key = keyType.read(dis);
		Object value = valueType.read(dis);
		return Map.entry(key, value);
	}

	@Override
	public void writeEntry(Map.Entry<Object, Object> entry, DataOutputStream dos) throws IOException
	{
		keyType.write(entry.getKey(), dos);
		valueType.write(entry.getValue(), dos);
	}

	@Override
	public void writeMeta(DataOutputStream dos, Map.Entry<Object, Object> sampleEntry) throws IOException
	{
		//On the first write, store the types, if we don't know them
		if (keyType == SimpleTypeStoreHandler.UNKNOWN && sampleEntry != null)
		{
			keyType = SimpleTypeStoreHandler.forType(sampleEntry.getKey().getClass());
			valueType = SimpleTypeStoreHandler.forType(sampleEntry.getValue().getClass());
		}
		dos.writeUTF(keyType.name());
		dos.writeUTF(valueType.name());
	}

	@Override
	public void init(DataInputStream dis, Function<Object, Object> valueSerializer, Function<Object, Object> valueDeserializer) throws IOException
	{
		if (valueDeserializer != null || valueDeserializer != null)
		{
			throw new RuntimeException("Simple type store doesn't support custom serializers");
		}
		keyType = SimpleTypeStoreHandler.fromStream(dis);
		valueType = SimpleTypeStoreHandler.fromStream(dis);
		if (keyType == null || valueType == null)
		{
			throw new RuntimeException("unsupported key or value type!");
		}
		super.init(dis, valueSerializer, valueDeserializer);
	}
}
