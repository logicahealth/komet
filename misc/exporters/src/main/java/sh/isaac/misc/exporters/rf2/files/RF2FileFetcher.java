package sh.isaac.misc.exporters.rf2.files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.ArrayUtils;

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
	
	/**
	 * Just write the provided text to a text file.
	 * @param fileName 
	 * @param content 
	 * @throws IOException 
	 */
	public void writeTextFile(String fileName, String content) throws IOException
	{
		Path temp = rootFolder.toPath().resolve(fileName);
		temp.getParent().toFile().mkdirs();
		Files.writeString(temp, content, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
	}
	
	public int getOpenFileCount()
	{
		return openFiles.size();
	}
	
	public void closeAll() throws IOException
	{
		for (RF2File file : openFiles.values())
		{
			file.close();
		}
	}
	
	/**
	 * Gets the cached file, or creates as necessary, one of these standard type files.  Cannot be used for {@link StandardFiles#CustomRefset}
	 * or {@link StandardFiles#CustomTextFile}
	 * @param releaseType
	 * @param fileKey
	 * @return the file, if the fileKey is known or creatable from {@link StandardFiles} (the key being the name of the enum value)
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
							return new DERFile(rootFolder, sf.subfolder, String.valueOf(sf.additionalColTypes) + "Refset", Optional.of(fileKey), Optional.empty(), 
									releaseType, namespace, versionDate, sf.getAllColNames());
						case Concept:
						case Identifier:
						case OWLExpression:
						case StatedRelationship:
						case Relationship:
							return new SCTFile(rootFolder, sf.name(), releaseType, Optional.empty(), namespace, versionDate, sf.getAllColNames());
						case CustomRefset:
							throw new RuntimeException("Call getCustomRefsetFile");
						case CustomTextFile:
							throw new RuntimeException("Call getDescriptionFile or getTextDefinitionFile");
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
		return new DERFile(rootFolder, subfolder, String.valueOf(additionalColTypes) + "Refset", Optional.of(fileKey), languageCode, releaseType, namespace, versionDate, 
				ArrayUtils.addAll(StandardFiles.CustomRefset.getAllColNames(), additionalColNames));
	}
	
	/**
	 * After the initial create, you get get it again by calling {@link #getFile(RF2ReleaseType, String)} with the refsetName as the fileKey
	 * This method supports {@link StandardFiles#CustomRefset}
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
	
	/**
	 * supports {@link StandardFiles#CustomTextFile}
	 * @param releaseType
	 * @param languageCode
	 * @return
	 */
	public RF2File getDescriptionFile(RF2ReleaseType releaseType, String languageCode)
	{
		return getTextFile(releaseType, "Description", languageCode);
	}
	
	/**
	 * supports {@link StandardFiles#CustomTextFile}
	 * @param releaseType
	 * @param languageCode
	 * @return
	 */
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
						StandardFiles.CustomTextFile.getAllColNames());
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public String toString()
	{
		return "FileFetcher namespace: " + namespace + ", rootFolder: " + rootFolder.getAbsolutePath() + ", versionDate: " + versionDate;
	}
}
