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
package gov.va.oia.terminology.converters.sharedUtils.stats;

import gov.va.oia.terminology.converters.sharedUtils.ConsoleUtil;
import gov.va.oia.terminology.converters.sharedUtils.ConverterBaseMojo;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import org.dwfa.util.id.Type5UuidFactory;

/**
 * A utility class for generating UUIDs which keeps track of what was used to generate the UUIDs - which
 * can then be dumped to disk (or looked up by UUID)
 * 
 * The in-memory map can be disabled by setting the static flag here - or - with loaders that extend {@link ConverterBaseMojo}
 * by setting the system property skipUUIDDebug to true - or in maven speak - '-DskipUUIDDebug' on the command line.
 * 
 * @author darmbrust
 */

public class ConverterUUID
{
	public static boolean disableUUIDMap_ = false;  //Some loaders need to disable this due to memory constraints
	private static Hashtable<UUID, String> masterUUIDMap_ = new Hashtable<UUID, String>();
	private static UUID namespace_ = null;

	/**
	 * Create a new Type5 UUID using the provided name as the seed in the configured namespace.
	 * 
	 * Throws a runtime exception if the namespace has not been configured.
	 */
	public static UUID createNamespaceUUIDFromStrings(String ... values)
	{
		StringBuilder uuidKey = new StringBuilder();
		for (String s : values)
		{
			if (s != null)
			{
				uuidKey.append(s);
				uuidKey.append("|");
			}
		}
		if (uuidKey.length() > 1)
		{
			uuidKey.setLength(uuidKey.length() - 1);
		}
		else
		{
			throw new RuntimeException("No string provided!");
		}
		return createNamespaceUUIDFromString(uuidKey.toString());
	}

	/**
	 * Create a new Type5 UUID using the provided name as the seed in the configured namespace.
	 * 
	 * Throws a runtime exception if the namespace has not been configured.
	 */
	public static UUID createNamespaceUUIDFromString(String name)
	{
		return createNamespaceUUIDFromString(name, false);
	}
	
	/**
	 * Create a new Type5 UUID using the provided name as the seed in the configured namespace.
	 * 
	 * Throws a runtime exception if the namespace has not been configured.
	 * @param skipDupeCheck can be used to bypass the duplicate checking function - useful in cases where you know
	 * you are creating the same UUID more than once.  Normally, this method throws a runtime exception
	 * if the same UUID is generated more than once.
	 */
	public static UUID createNamespaceUUIDFromString(String name, boolean skipDupeCheck)
	{
		initCheck();
		return createNamespaceUUIDFromString(namespace_, name, skipDupeCheck);
	}
	
	private static void initCheck()
	{
		if (namespace_ == null)
		{
			throw new RuntimeException("Namespace UUID has not yet been initialized");
		}
	}
	
	/**
	 * Create a new Type5 UUID using the provided namespace, and provided name as the seed.
	 */
	public static UUID createNamespaceUUIDFromString(UUID namespace, String name)
	{
		return createNamespaceUUIDFromString(namespace, name, false);
	}
	
	/**
	 * Create a new Type5 UUID using the provided namespace, and provided name as the seed.
	 * @param skipDupeCheck can be used to bypass the duplicate checking function - useful in cases where you know
	 * you are creating the same UUID more than once.  Normally, this method throws a runtime exception
	 * if the same UUID is generated more than once.
	 */
	public static UUID createNamespaceUUIDFromString(UUID namespace, String name, boolean skipDupeCheck)
	{
		UUID uuid;
		try
		{
			uuid = Type5UuidFactory.get(namespace, name);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Unexpected error configuring UUID generator");
		}
		
		if (!disableUUIDMap_)
		{
			String putResult = masterUUIDMap_.put(uuid, name);
			if (!skipDupeCheck && putResult != null)
			{
				throw new RuntimeException("Just made a duplicate UUID! '" + name + "' -> " + uuid);
			}
		}
		return uuid;
	}
	
	/**
	 * Return the string that was used to generate this UUID (if available - null if not)
	 */
	public static String getUUIDCreationString(UUID uuid)
	{
		if (uuid == null)
		{
			return null;
		}
		return masterUUIDMap_.get(uuid);
	}

	/**
	 * Write out a debug file with all of the UUID - String mappings
	 */
	public static void dump(File outputDirectory, String prefix) throws IOException
	{
		BufferedWriter br = new BufferedWriter(new FileWriter(new File(outputDirectory, prefix + "DebugMap.txt")));
		if (disableUUIDMap_)
		{
			ConsoleUtil.println("UUID Debug map was disabled");
			br.write("Note - the UUID debug feature was disabled, this file is incomplete" + System.getProperty("line.separator"));
		}
		for (Map.Entry<UUID, String> entry : masterUUIDMap_.entrySet())
		{
			br.write(entry.getKey() + " - " + entry.getValue() + System.getProperty("line.separator"));
		}
		br.close();
	}

	public static void clearCache()
	{
		masterUUIDMap_.clear();
	}
	
	/**
	 * Allow this map to be updated with UUIDs that were not generated via this utility class
	 */
	public static void addMapping(String value, UUID uuid)
	{
		masterUUIDMap_.put(uuid, value);
	}
	
	/**
	 * In some scenarios, it isn't desireable to cache every creation string - allow the removal in these cases.
	 */
	public static void removeMapping(UUID uuid)
	{
		masterUUIDMap_.remove(uuid);
	}
	
	public static UUID getNamespace()
	{
		return namespace_;
	}
	
	public static void configureNamespace(UUID namespace)
	{
		if (namespace_ != null)
		{
			ConsoleUtil.println("Reconfiguring Namespace!");
		}
		namespace_ = namespace;
	}
}
