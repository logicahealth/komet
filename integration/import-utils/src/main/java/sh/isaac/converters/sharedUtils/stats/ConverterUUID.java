/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government
 * employees, or under US Veterans Health Administration contracts.
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government
 * employees are USGovWork (17USC §105). Not subject to copyright.
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */

package sh.isaac.converters.sharedUtils.stats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.constants.MetadataConceptConstant;
import sh.isaac.api.constants.MetadataConceptConstantGroup;
import sh.isaac.api.util.UuidT5Generator;

/**
 * A utility class for generating UUIDs which keeps track of what was used to generate the UUIDs - which
 * can then be dumped to disk (or looked up by UUID)
 *
 * The in-memory map can be disabled by setting the static flag here - or - with loaders that extend {@link ConverterBaseMojo}
 * by setting the system property skipUUIDDebug to true - or in maven speak - '-DskipUUIDDebug' on the command line.
 * 
 * If you fetch this class via HK2, then you will be getting a singleton instance.
 * If you don't want to use the singleton instance, you may construct it directly via the constructor.
 *
 * @author darmbrust
 */
@Service
@Singleton  //If this is used via HK2, we treat it as a singleton.
public class ConverterUUID
{
	private static final Logger LOG = LogManager.getLogger();
	
	private boolean disableUUIDMap = false;  // Some loaders need to disable this due to memory constraints

	private final ConcurrentHashMap<UUID, String> masterUUIDMap = new ConcurrentHashMap<UUID, String>();

	private UUID namespace = null;

	private ConceptSpecification[] constants;
	
	private ConverterUUID()
	{
		//This constructor is for HK2
		List<ConceptSpecification> constantsList = new ArrayList<>();
		for (ConceptSpecification cs : MetaData.META_DATA_CONCEPTS)
		{
			constantsList.add(cs);
		}

		for (MetadataConceptConstant dc : DynamicConstants.get().getConstantsToCreate())
		{
			constantsList.add(dc);
			if (dc instanceof MetadataConceptConstantGroup)
			{
				handleGroup((MetadataConceptConstantGroup)dc, constantsList);
			}
		}
		constants = constantsList.toArray(new ConceptSpecification[constantsList.size()]);
	}

	public ConverterUUID(UUID namespace, boolean disableUUIDMap)
	{
		this();
		this.disableUUIDMap = disableUUIDMap;
		this.namespace = namespace;
	}

	private void initCheck()
	{
		if (namespace == null)
		{
			throw new RuntimeException("Namespace UUID has not yet been initialized");
		}
	}
	
	private void handleGroup(MetadataConceptConstantGroup group, List<ConceptSpecification> list)
	{
		for (MetadataConceptConstant c : group.getChildren())
		{
			list.add(c);
			if (c instanceof MetadataConceptConstantGroup)
			{
				handleGroup((MetadataConceptConstantGroup)c, list);
			}
		}
	}

	/**
	 * Allow this map to be updated with UUIDs that were not generated via this utility class.
	 *
	 * @param value the value
	 * @param uuid the uuid
	 */
	public void addMapping(String value, UUID uuid)
	{
		if (!disableUUIDMap)
		{
			final String putResult = masterUUIDMap.put(uuid, value);

			if (putResult != null)
			{
				throw new RuntimeException("Just made a duplicate UUID! '" + value + "' -> " + uuid);
			}
		}
	}

	/**
	 * Clear cache.
	 */
	public void clearCache()
	{
		masterUUIDMap.clear();
	}

	/**
	 * Configure (or reconfigure) the namespace.
	 *
	 * @param namespace the namespace
	 */
	public void configureNamespace(UUID namespace)
	{
		if (namespace != null)
		{
			LOG.info("Reconfiguring Namespace from {} to {}", 
					() -> {
						StringBuilder sb = new StringBuilder();
						if (!disableUUIDMap)
						{
							String s = getUUIDCreationString(this.namespace);
							if (s != null)
							{
								sb.append(s);
								sb.append(" - ");
							}
						}
						sb.append(this.namespace);
						return sb.toString();
					},
					() -> {
						StringBuilder sb = new StringBuilder();
						if (!disableUUIDMap)
						{
							String s = getUUIDCreationString(namespace);
							if (s != null)
							{
								sb.append(s);
								sb.append(" - ");
							}
						}
						sb.append(namespace.toString());
						return sb.toString();
					});
		}
		this.namespace = namespace;
	}

	/**
	 * Create a new Type5 UUID using the provided name as the seed in the configured namespace.
	 *
	 * Throws a runtime exception if the namespace has not been configured.
	 *
	 * @param name the name
	 * @return the uuid
	 */
	public UUID createNamespaceUUIDFromString(String name)
	{
		return createNamespaceUUIDFromString(name, false);
	}

	/**
	 * Create a new Type5 UUID using the provided namespace, and provided name as the seed.
	 *
	 * @param namespace the namespace
	 * @param name the name
	 * @return the uuid
	 */
	public UUID createNamespaceUUIDFromString(UUID namespace, String name)
	{
		return createNamespaceUUIDFromString(namespace, name, false);
	}

	/**
	 * Create a new Type5 UUID using the provided name as the seed in the configured namespace.
	 *
	 * Throws a runtime exception if the namespace has not been configured.
	 *
	 * @param name the name
	 * @param skipDupeCheck can be used to bypass the duplicate checking function - useful in cases where you know
	 *            you are creating the same UUID more than once. Normally, this method throws a runtime exception
	 *            if the same UUID is generated more than once.
	 * @return the uuid
	 */
	public UUID createNamespaceUUIDFromString(String name, boolean skipDupeCheck)
	{
		initCheck();
		return createNamespaceUUIDFromString(namespace, name, skipDupeCheck);
	}

	/**
	 * Create a new Type5 UUID using the provided namespace, and provided name as the seed.
	 *
	 * @param namespace the namespace
	 * @param name the name
	 * @param skipDupeCheck can be used to bypass the duplicate checking function - useful in cases where you know
	 *            you are creating the same UUID more than once. Normally, this method throws a runtime exception
	 *            if the same UUID is generated more than once.
	 * @return the uuid
	 */
	public UUID createNamespaceUUIDFromString(UUID namespace, String name, boolean skipDupeCheck)
	{
		UUID uuid;

		try
		{
			uuid = UuidT5Generator.get(namespace, name);
		}
		catch (final Exception e)
		{
			throw new RuntimeException("Unexpected error configuring UUID generator", e);
		}

		if (!disableUUIDMap)
		{
			final String putResult = masterUUIDMap.put(uuid, name);

			if (!skipDupeCheck && (putResult != null))
			{
				throw new RuntimeException("Just made a duplicate UUID! '" + name + "' -> " + uuid);
			}
		}

		return uuid;
	}

	/**
	 * Create a new Type5 UUID using the provided name as the seed in the configured namespace.
	 *
	 * Throws a runtime exception if the namespace has not been configured.
	 *
	 * @param values the values
	 * @return the uuid
	 */
	public UUID createNamespaceUUIDFromStrings(String... values)
	{
		final StringBuilder uuidKey = new StringBuilder();

		for (final String s : values)
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
	 * Write out a debug file with all of the UUID - String mappings.
	 *
	 * @param outputDirectory the output directory
	 * @param prefix the prefix
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void dump(File outputDirectory, String prefix) throws IOException
	{
		if (outputDirectory != null)
		{
			outputDirectory.mkdirs();
			try (BufferedWriter br = new BufferedWriter(new FileWriter(new File(outputDirectory, prefix + "DebugMap.txt"),
					Charset.forName(StandardCharsets.UTF_8.name())));)
			{
				if (disableUUIDMap)
				{
					LOG.info("UUID Debug map was disabled");
					br.write("Note - the UUID debug feature was disabled, this file is incomplete" + System.lineSeparator());
				}
	
				for (final Map.Entry<UUID, String> entry : masterUUIDMap.entrySet())
				{
					br.write(entry.getKey() + " - " + entry.getValue() + System.lineSeparator());
				}
			}
		}
		else
		{
			LOG.info("Can't write debug output, as no output directory is specified");
		}
	}

	/**
	 * In some scenarios, it isn't desirable to cache every creation string - allow the removal in these cases.
	 *
	 * @param uuid the uuid
	 */
	public void removeMapping(UUID uuid)
	{
		masterUUIDMap.remove(uuid);
	}

	/**
	 * Gets the namespace.
	 *
	 * @return the namespace
	 */
	public UUID getNamespace()
	{
		return namespace;
	}
	
	/**
	 * @return true, if the in-memory UUID map is enabled (which is the default state)
	 */
	public boolean isUUIDMapEnabled()
	{
		return !disableUUIDMap;
	}
	
	/**
	 * Some loaders need to disable this feature due to high memory usage
	 * @param enabled - set to false, to disable the in-memory map (which also disables duplicate detection)
	 */
	public void setUUIDMapState(boolean enabled)
	{
		this.disableUUIDMap = !enabled;
	}
	
	/**
	 * Return the string that was used to generate this UUID (if available - null if not).
	 *
	 * @param uuid the uuid
	 * @return the UUID creation string
	 */
	public String getUUIDCreationString(UUID uuid)
	{
		if (uuid == null)
		{
			return null;
		}

		final String found = masterUUIDMap.get(uuid);

		if (found == null)
		{
			for (final ConceptSpecification cs : constants)
			{
				if (uuid.equals(cs.getPrimordialUuid()))
				{
					return cs.getFullyQualifiedName();
				}
			}
		}

		return found;
	}
}
