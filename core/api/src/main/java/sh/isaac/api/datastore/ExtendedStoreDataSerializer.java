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
package sh.isaac.api.datastore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import org.jvnet.hk2.annotations.Contract;

@Contract
public interface ExtendedStoreDataSerializer<K, V>
{
	/**
	 * Must align with the name in your annotation: @Service (name="theNameHere")
	 * 
	 * @return the service name
	 */
	public String getServiceName();
	
	/**
	 * The provided serializers should map your external type into a supported {@link SimpleTypeStoreHandler} type, and back again.
	 * 
	 * Will be called once, if there is data to read, upon init.  
	 * @param dis - if there is a stream to read
	 * @param valueSerializer - optional custom serializer
	 * @param valueDeserializer  - optional custom deserializer
	 * @throws IOException 
	 */
	public void init(DataInputStream dis, Function<Object, Object> valueSerializer, Function<Object, Object> valueDeserializer) throws IOException;
	
	/**
	 * Will be called once, prior to a bulk data write.  Write any data that your implementation requires
	 * to bootstrap itself upon an {@link #init} call
	 * @param dos
	 * @throws IOException 
	 */
	public void writeMeta(DataOutputStream dos, Map.Entry<Object, Object> sampleEntry) throws IOException;

	/**
	 * Read a key/value pair from the data input stream
	 * 
	 * @param dis
	 * @return the key/value pair
	 * @throws IOException
	 */
	public Map.Entry<K, V> readEntry(DataInputStream dis) throws IOException;

	/**
	 * Write a key/value pair to the data input stream
	 * 
	 * @param entry
	 * @param dos
	 * @throws IOException
	 */
	public void writeEntry(Map.Entry<K, V> entry, DataOutputStream dos) throws IOException;
}
