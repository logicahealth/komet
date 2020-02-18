package sh.isaac.misc.exporters.rf2.files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RF2FileFetcher
{
	ConcurrentHashMap<String, RF2File> openFiles = new ConcurrentHashMap<>();
	private final File rootFolder;
	private final String namespace;
	private final String versionDate;
	
	public RF2FileFetcher(File rootFolder, String namespace, String versionDate)
	{
		this.rootFolder = rootFolder;
		this.namespace = namespace;
		this.versionDate = versionDate;
	}
	
	public void closeAll() throws IOException
	{
		for (RF2File file : openFiles.values())
		{
			file.close();
		}
	}
	
	/**
	 * Gets the cached file, or creates as necessary, one of these standard type files.
	 * @param releaseType
	 * @param fileKey
	 * @return the file, if the fileKey is known or creatable from {@link StandardFiles}
	 */
	public RF2File getFile(RF2ReleaseType releaseType, String fileKey)
	{
		return openFiles.computeIfAbsent((releaseType.name() + fileKey), keyAgain -> 
		{
			StandardFiles sf = StandardFiles.parse(fileKey);
			if (sf != null)
			{
				try
				{
					switch (sf)
					{
						case Association:
						case AttributeValue:
						case DescriptionType:
						case ExtendedMap:
						case ModuleDependency:
						case RefsetDescriptor:
						case Simple:
						case SimpleMap:
							return createRefsetFile(sf.subfolder, releaseType, sf.name(), Optional.empty(), sf.additionalColTypes, sf.additionalColNames );
						case Concept:
							return new SCTFile(rootFolder, sf.name(), releaseType, Optional.empty(), namespace, versionDate, 
									new String[] {"id", "effectiveTime", "active", "moduleId", "definitionStatusId"});
						case Identifier:
							return new SCTFile(rootFolder, sf.name(), releaseType, Optional.empty(), namespace, versionDate, 
									new String[] {"identifierSchemeId", "alternateIdentifier", "effectiveTime", "active", "moduleId", "referencedComponentId"});
						case OWLExpression:
							return new SCTFile(rootFolder, "sRefset", Optional.of(sf.name()), releaseType, Optional.empty(), namespace, versionDate, 
									new String[] {"id", "effectiveTime", "active", "moduleId", "refsetId", "referencedComponentId", "owlExpression"});
						case StatedRelationship:
						case Relationship:
							return new SCTFile(rootFolder, sf.name(), releaseType, Optional.empty(), namespace, versionDate, 
									new String[] {"id", "effectiveTime", "active", "moduleId", "sourceId", "destinationId", "relationshipGroup", "typeId", "characteristicTypeId", "modifierId"});
						default :
							throw new RuntimeException("oops");
					}
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
			return null;
		});
	}
	
	private DERFile createRefsetFile(String subfolder, RF2ReleaseType releaseType, String fileKey, Optional<String> languageCode, char[] additionalColTypes, String[] additionalColNames) throws IOException
	{
		if (additionalColNames.length != additionalColTypes.length)
		{
			throw new RuntimeException("oops");
		}
		ArrayList<String> colNames = new ArrayList<>(6 + additionalColTypes.length);
		colNames.add("id");
		colNames.add("effectiveTime");
		colNames.add("active");
		colNames.add("moduleId");
		colNames.add("refsetId");
		colNames.add("referencedComponentId");
		for (String s : additionalColNames)
		{
			colNames.add(s);
		}
		
		return new DERFile(rootFolder, subfolder, String.valueOf(additionalColTypes) + "Refset", Optional.of(fileKey), languageCode, releaseType, namespace, versionDate, 
				colNames.toArray(new String[colNames.size()]));
	}
	
	/**
	 * After the initial create, you get get it again by calling {@link #getFile(RF2ReleaseType, String)} with the refsetName as the fileKey
	 * @param releaseType
	 * @param refsetName
	 * @param additionalColTypes
	 * @param additionalColNames
	 * @return the custom refset file, newly created, if the refsetName / releaseType hasn't been previously requested.
	 */
	public RF2File getCustomRefsetFile(RF2ReleaseType releaseType, String refsetName, char[] additionalColTypes, String[] additionalColNames)
	{
		return openFiles.computeIfAbsent((releaseType.name() + refsetName), keyAgain -> 
		{
			try
			{
				return createRefsetFile("Content", releaseType, refsetName, Optional.empty(), additionalColTypes, additionalColNames);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		});
	}
	
	public RF2File getLanguageFile(RF2ReleaseType releaseType, String languageCode)
	{
		return openFiles.computeIfAbsent((releaseType.name() + "Language-" + languageCode), keyAgain -> 
		{
			try
			{
				return createRefsetFile("Language", releaseType, "Language", Optional.of(languageCode), new char[] {'c'}, new String[] {"acceptabilityId"});
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		});
	}
	
	public RF2File getDescriptionFile(RF2ReleaseType releaseType, String languageCode)
	{
		return getTextFile(releaseType, "Description", languageCode);
	}
	
	public RF2File getTextDefinitionFile(RF2ReleaseType releaseType, String languageCode)
	{
		return getTextFile(releaseType, "TextDefinition", languageCode);
	}
	
	private RF2File getTextFile(RF2ReleaseType releaseType, String fileType, String languageCode)
	{
		return openFiles.computeIfAbsent((releaseType.name() + fileType + "-" + languageCode), keyAgain -> 
		{
			try
			{
				return new SCTFile(rootFolder, fileType, releaseType, Optional.of(languageCode), namespace, versionDate, 
						new String[] {"id", "effectiveTime", "active", "moduleId", "conceptId", "languageCode", "typeId", "term", "caseSignificanceId"});
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		});
	}
}
