package sh.komet.converter.turtle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.constants.MetadataConceptConstant;
import sh.isaac.api.constants.MetadataConceptConstantGroup;
import sh.isaac.api.util.UuidT5Generator;

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

public class TempConverterUUID {
    private static final Logger LOG = LogManager.getLogger();

    private boolean disableUUIDMap = false;  // Some loaders need to disable this due to memory constraints

    private final ConcurrentHashMap<UUID, String> masterUUIDMap = new ConcurrentHashMap<UUID, String>();

    private UUID namespace = null;

    private ConceptSpecification[] constants;

    private TempConverterUUID()
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

    public TempConverterUUID(UUID namespace, boolean disableUUIDMap)
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
