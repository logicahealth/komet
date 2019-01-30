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
package sh.isaac.dbConfigBuilder.artifacts;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import javafx.concurrent.Task;
import sh.isaac.api.Get;
import sh.isaac.api.util.AlphanumComparator;
import sh.isaac.api.util.DownloadUnzipTask;
import sh.isaac.api.util.WorkExecutors;
import sh.isaac.api.util.metainf.VersionFinder;
import sh.isaac.dbConfigBuilder.artifacts.rest.query.NexusRead;
import sh.isaac.dbConfigBuilder.prefs.StoredPrefs;

/**
 * Convenience methods related to maven artifacts
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class MavenArtifactUtils
{
	private static Logger log = LogManager.getLogger();

	/**
	 * Reads source content files from the local folder, and a nexus repository in a background thread.
	 * 
	 * @param storedPrefs
	 * @param resultHandler results handed here, in a thread, when complete
	 * @return the already-executing task
	 */
	public static Task<Void> readAvailableSourceFiles(StoredPrefs storedPrefs, Consumer<List<SDOSourceContent>> resultHandler)
	{
		Task<Void> t = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				File temp = new File(storedPrefs.getLocalM2FolderPath());
				HashSet<SDOSourceContent> foundFiles = new HashSet<>();
				if (temp.isDirectory())
				{
					log.debug("Reading local m2 folder");
					updateMessage("Reading the local m2 folder");

					for (SDOSourceContent i : readLocalSDOArtifacts(temp, null))
					{
						foundFiles.add(i);
					}
				}

				try
				{
					if (StringUtils.isNotBlank(storedPrefs.getArtifactReadURL()))
					{
						log.debug("Reading available nexus versions");
						// TODO if/when we support more than just nexus, look at the URL, and use it to figure out which reader to construct
						foundFiles.addAll(new NexusRead(storedPrefs).readSDOFiles(null));
					}
				}
				catch (Exception e)
				{
					log.error("Error reading nexus repository: " + e );
				}

				ArrayList<SDOSourceContent> results = new ArrayList<>();
				results.addAll(foundFiles);
				Collections.sort(results);
				resultHandler.accept(results);
				return null;
			}
		};

		Get.workExecutors().getExecutor().execute(t);
		return t;
	}

	/**
	 * Reads converted ibdf files from the local folder, and a nexus repository in a background thread.
	 * 
	 * @param deltaArtifacts - true, if you want to read IBDF files that are deltas of other IBDF files.  False for standard IBDF files only (non-delta)
	 * @param storedPrefs
	 * @param resultHandler results handed here, in a thread, when complete
	 * @return the already-executing task
	 */
	public static Task<Void> readAvailableIBDFFiles(boolean deltaArtifacts, StoredPrefs storedPrefs, Consumer<List<IBDFFile>> resultHandler)
	{
		Task<Void> t = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				File temp = new File(storedPrefs.getLocalM2FolderPath());
				HashSet<IBDFFile> foundFiles = new HashSet<>();
				try
				{
					if (temp.isDirectory())
					{
						log.debug("Reading local m2 folder");
						updateMessage("Reading the local m2 folder");

						for (IBDFFile i : readLocalIBDFArtifacts(temp, deltaArtifacts))
						{
							if (i.getArtifactId().equals("metadata"))
							{
								continue;
							}
							foundFiles.add(i);
						}
					}
				}
				catch (Exception e1)
				{
					log.error("Error reading local M2 folder", e1);
				}

				try
				{
					if (StringUtils.isNotBlank(storedPrefs.getArtifactReadURL()))
					{
						log.debug("Reading available nexus versions");
						// TODO if/when we support more than just nexus, look at the URL, and use it to figure out which reader to construct
						foundFiles.addAll(new NexusRead(storedPrefs).readIBDFFiles(deltaArtifacts));
					}
				}
				catch (Exception e)
				{
					log.error("Error reading nexus repository", e);
				}

				ArrayList<IBDFFile> results = new ArrayList<>();
				results.addAll(foundFiles);
				Collections.sort(results);
				resultHandler.accept(results);
				return null;
			}
		};
		Get.workExecutors().getExecutor().execute(t);
		return t;
	}

	/**
	 * Reads available versions of the specified source artifact type from the local folder, and a nexus repository in a background thread.
	 * 
	 * @param storedPrefs
	 * @param artifactId
	 * @param resultHandler results handed here, in a thread, when complete
	 * @return the already-executing task
	 */
	public static Task<Void> readSourceUploadExistingVersions(StoredPrefs storedPrefs, String artifactId, Consumer<Stream<String>> resultHandler)
	{
		Task<Void> t = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				try
				{
					log.debug("Reading local source versions");
					File temp = new File(storedPrefs.getLocalM2FolderPath());
					TreeSet<String> foundVersions = new TreeSet<>(AlphanumComparator.getCachedInstance(true));
					if (temp.isDirectory())
					{
						for (SDOSourceContent i : readLocalSDOArtifacts(temp, artifactId))
						{
							foundVersions.add(i.getVersion());
						}
					}

					try
					{
						if (StringUtils.isNotBlank(storedPrefs.getArtifactReadURL()))
						{
							log.debug("Reading available nexus versions");
							// TODO if/when we support more than just nexus, look at the URL, and use it to figure out which reader to construct
							for (SDOSourceContent x : new NexusRead(storedPrefs).readSDOFiles(artifactId))
							{
								foundVersions.add(x.getVersion());
							}
						}
					}
					catch (Exception e)
					{
						log.error("Error reading nexus repository", e);
					}

					resultHandler.accept(foundVersions.stream());

				}
				catch (Exception e)
				{
					log.error("Unexpected error trying to calculate existing source versions", e);
				}
				return null;
			}
		};

		Get.workExecutors().getExecutor().execute(t);
		return t;
	}

	/**
	 * Reads available versions of the metadata ibdf from the local folder, and a nexus repository in a background thread.
	 * 
	 * @param storedPrefs
	 * @param resultHandler results handed here, in a thread, when complete
	 * @return the already-executing task
	 */
	public static Task<Void> readAvailableMetadataVersions(StoredPrefs storedPrefs, Consumer<Stream<String>> resultHandler)
	{
		Task<Void> t = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				final TreeSet<String> metadataVersions = new TreeSet<>(new AlphanumComparator(true));
				metadataVersions.add(VersionFinder.findProjectVersion(true));
				metadataVersions.add(VersionFinder.findProjectVersion(false));

				File temp = new File(storedPrefs.getLocalM2FolderPath());
				if (temp.isDirectory())
				{
					log.debug("Reading local m2 folder");
					for (IBDFFile i : readLocalMetadataArtifacts(temp))
					{
						metadataVersions.add(i.getVersion());
					}
				}
				try
				{
					if (StringUtils.isNotBlank(storedPrefs.getArtifactReadURL()))
					{
						log.debug("Reading available nexus versions");
						// TODO if/when we support more than just nexus, look at the URL, and use it to figure out which reader to construct
						metadataVersions.addAll(new NexusRead(storedPrefs).readMetadataVersions());
					}
				}
				catch (Exception e)
				{
					log.error("Error reading nexus repository", e);
				}

				resultHandler.accept(metadataVersions.stream());
				return null;
			}
		};
		Get.workExecutors().getExecutor().execute(t);
		return t;
	}

	/**
	 * Reads available versions of the metadata ibdf from the local folder, and a nexus repository in a background thread.
	 * 
	 * @param storedPrefs
	 * @param resultHandler results handed here, in a thread, when complete
	 * @return the already-executing task
	 */
	public static Task<Void> readAvailableConverterVersions(StoredPrefs storedPrefs, Consumer<Stream<String>> resultHandler)
	{
		Task<Void> t = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				final TreeSet<String> metadataVersions = new TreeSet<>(new AlphanumComparator(true));
				metadataVersions.add(VersionFinder.findProjectVersion(true));

				File temp = new File(storedPrefs.getLocalM2FolderPath());
				if (temp.isDirectory())
				{
					log.debug("Reading local m2 folder");
					for (Converter i : readLocalConverterVersions(temp))
					{
						metadataVersions.add(i.getVersion());
					}
				}
				try
				{
					if (StringUtils.isNotBlank(storedPrefs.getArtifactReadURL()))
					{
						log.debug("Reading available nexus versions");
						// TODO if/when we support more than just nexus, look at the URL, and use it to figure out which reader to construct
						new NexusRead(storedPrefs).readConverterVersions().forEach(version -> metadataVersions.add(version.getVersion()));
					}
				}
				catch (Exception e)
				{
					log.error("Error reading nexus repository", e);
				}

				resultHandler.accept(metadataVersions.stream());
				return null;
			}
		};
		Get.workExecutors().getExecutor().execute(t);
		return t;
	}

	/**
	 * A convenience method to read all available ibdf datasets from the local repository
	 * 
	 * @param mavenRepositoryFolder
	 * @param deltaArtifacts if true, returns artifacts that are deltas of other artifacts.  If false, returns standard (non-delta) artifacts.
	 * @return the IBDF Files found that are metadata
	 */
	public static IBDFFile[] readLocalIBDFArtifacts(File mavenRepositoryFolder, boolean deltaArtifacts)
	{
		ArrayList<IBDFFile> files = new ArrayList<>();
		if (mavenRepositoryFolder.isDirectory())
		{
			File browseFolder = new File(mavenRepositoryFolder, deltaArtifacts ? "sh/isaac/terminology/diffs" : "sh/isaac/terminology/converted");

			if (browseFolder.isDirectory())
			{
				for (File artifactId : browseFolder.listFiles())
				{
					if (artifactId.isDirectory())
					{
						for (File versionFolder : artifactId.listFiles())
						{
							if (versionFolder.isDirectory())
							{
								ArrayList<String> ibdfClassifiers = new ArrayList<>();
								for (File content : versionFolder.listFiles())
								{
									if (content.getName().toLowerCase().endsWith("ibdf.zip"))
									{
										// cpt-ibdf-2017-loader-4.48-SNAPSHOT.ibdf.zip
										String temp = content.getName().substring(artifactId.getName().length() + versionFolder.getName().length() + 1,
												content.getName().length());
										// should now have .ibdf.zip (or maybe with a classifier like -all.ibdf.zip)
										if (temp.startsWith("-"))
										{
											ibdfClassifiers.add(temp.substring(1, temp.indexOf(".")));
										}
										else
										{
											ibdfClassifiers.add("");  // no classifier
										}
									}
								}
								for (String classifier : ibdfClassifiers)
								{
									files.add(new IBDFFile(deltaArtifacts ? "sh.isaac.terminology.diffs" : "sh.isaac.terminology.converted", artifactId.getName(), versionFolder.getName(), classifier));
								}
							}
						}
					}
				}
			}
		}
		return files.toArray(new IBDFFile[files.size()]);
	}

	/**
	 * A convenience method to read all available metadata versions in the local m2 repository
	 * 
	 * @param mavenRepositoryFolder
	 * @return the IBDF Files found that are metadata
	 */
	public static Converter[] readLocalConverterVersions(File mavenRepositoryFolder)
	{
		ArrayList<Converter> files = new ArrayList<>();
		if (mavenRepositoryFolder.isDirectory())
		{
			File browseFolder = new File(mavenRepositoryFolder, "sh/isaac/misc/importers");
			for (File versionFolder : browseFolder.listFiles())
			{
				if (versionFolder.isDirectory())
				{
					for (File content : versionFolder.listFiles())
					{
						if (content.getName().toLowerCase().endsWith(".pom"))
						{
							files.add(new Converter("sh.isaac.misc", "importers", versionFolder.getName()));
						}
					}
				}
			}
		}
		return files.toArray(new Converter[files.size()]);
	}

	/**
	 * A convenience method to read all available metadata versions in the local m2 repository
	 * 
	 * @param mavenRepositoryFolder
	 * @return the IBDF Files found that are metadata
	 */
	public static IBDFFile[] readLocalMetadataArtifacts(File mavenRepositoryFolder)
	{
		ArrayList<IBDFFile> files = new ArrayList<>();
		if (mavenRepositoryFolder.isDirectory())
		{
			File browseFolder = new File(mavenRepositoryFolder, "sh/isaac/core/metadata");
			for (File versionFolder : browseFolder.listFiles())
			{
				if (versionFolder.isDirectory())
				{
					ArrayList<String> ibdfClassifiers = new ArrayList<>();
					for (File content : versionFolder.listFiles())
					{
						if (content.getName().toLowerCase().endsWith("ibdf.zip"))
						{
							// metadata-4.48-SNAPSHOT-all.ibdf.zip
							String temp = content.getName().substring("metadata".length() + versionFolder.getName().length() + 1, content.getName().length());
							// should now have -all.ibdf.zip
							if (temp.startsWith("-"))
							{
								ibdfClassifiers.add(temp.substring(1, temp.indexOf(".")));
							}
							else
							{
								ibdfClassifiers.add("");  // no classifier
							}
						}
					}
					for (String classifier : ibdfClassifiers)
					{
						files.add(new IBDFFile("sh.isaac.core", "metadata", versionFolder.getName(), classifier));
					}
				}
			}
		}
		return files.toArray(new IBDFFile[files.size()]);
	}

	/**
	 * A convenience method to read all available source versions from the local repository
	 * 
	 * @param mavenRepositoryFolder
	 * @param artifactIdFilter - optional - if provided, only return versions of the specified type
	 * @return the IBDF Files found that are metadata
	 */
	public static SDOSourceContent[] readLocalSDOArtifacts(File mavenRepositoryFolder, String artifactIdFilter)
	{
		ArrayList<SDOSourceContent> files = new ArrayList<>();
		if (mavenRepositoryFolder.isDirectory())
		{
			File browseFolder = new File(mavenRepositoryFolder, "sh/isaac/terminology/source");

			for (File artifactId : browseFolder.listFiles())
			{
				if (StringUtils.isNotBlank(artifactIdFilter) && !artifactId.getName().equals(artifactIdFilter))
				{
					continue;
				}
				if (artifactId.isDirectory())
				{
					for (File versionFolder : artifactId.listFiles())
					{
						if (versionFolder.isDirectory())
						{
							ArrayList<String> artifactClassifiers = new ArrayList<>();
							for (File content : versionFolder.listFiles())
							{
								if (content.getName().toLowerCase().endsWith(".zip"))
								{
									// rf2-src-data-sct-20170731T150000Z.zip
									String temp = content.getName().substring(artifactId.getName().length() + versionFolder.getName().length() + 1,
											content.getName().length());
									// should now have .zip (or maybe with a classifier like -all.zip)
									if (temp.startsWith("-"))
									{
										artifactClassifiers.add(temp.substring(1, temp.indexOf(".")));
									}
									else
									{
										artifactClassifiers.add("");  // no classifier
									}
								}
							}
							for (String classifier : artifactClassifiers)
							{
								files.add(new SDOSourceContent("sh.isaac.terminology.source", artifactId.getName(), versionFolder.getName(), classifier));
							}
						}
					}
				}
			}
		}
		return files.toArray(new SDOSourceContent[files.size()]);
	}

	/**
	 * Make full URL to download a resource from a maven respository
	 *
	 * @param baseMavenURL the base maven URL
	 * @param mavenUsername - optional - only used for a SNAPSHOT dependency
	 * @param mavenPassword - optional - only used for a SNAPSHOT dependency
	 * @param groupId the group id
	 * @param artifactId the artifact id
	 * @param version the version
	 * @param classifier - optional
	 * @param type the type
	 * @return the url
	 * @throws Exception the exception
	 */
	public static URL makeFullURL(String baseMavenURL, String mavenUsername, char[] mavenPassword, String groupId, String artifactId, String version,
			String classifier, String type) throws Exception
	{
		return new URL(baseMavenURL + (baseMavenURL.endsWith("/") ? "" : "/")
				+ makeMavenRelativePath(baseMavenURL, mavenUsername, mavenPassword, groupId, artifactId, version, classifier, type));
	}

	/**
	 * Make maven relative path.  This should only be used when requesting release versions.  SNAPSHOT versions require a trip to the server.
	 * see {@link #makeMavenRelativePath(String, String, char[], String, String, String, String, String)}
	 *
	 * @param groupId the group id
	 * @param artifactId the artifact id
	 * @param version the version
	 * @param classifier the classifier
	 * @param type the type
	 * @return the string
	 */
	public static String makeMavenRelativePath(String groupId, String artifactId, String version, String classifier, String type)
	{
		if (version.endsWith("-SNAPSHOT"))
		{
			throw new RuntimeException("Cannot create a valid path to a -SNAPSHOT url without downloading the corresponding maven-metadata.xml file.");
		}

		try
		{
			return makeMavenRelativePath(null, null, null, groupId, artifactId, version, classifier, type);
		}
		catch (final Exception e)
		{
			throw new RuntimeException("Unexpected", e);
		}
	}

	/**
	 * Make maven relative path.
	 *
	 * @param baseMavenURL - optional - but required if you are downloading a SNAPSHOT dependency, as this method will need to download the metadata
	 *            file
	 *            from the repository server in order to determine the proper version component for the SNAPSHOT.
	 * @param mavenUsername - optional - only used for a SNAPSHOT dependency
	 * @param mavenPassword - optional - only used for a SNAPSHOT dependency
	 * @param groupId the group id
	 * @param artifactId the artifact id
	 * @param version the version
	 * @param classifier - optional
	 * @param type the type
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String makeMavenRelativePath(String baseMavenURL, String mavenUsername, char[] mavenPassword, String groupId, String artifactId,
			String version, String classifier, String type) throws Exception
	{
		final String temp = groupId.replaceAll("\\.", "/");
		String snapshotVersion = "";
		String versionWithoutSnapshot = version;

		if (version.endsWith("-SNAPSHOT"))
		{
			versionWithoutSnapshot = version.substring(0, version.lastIndexOf("-SNAPSHOT"));

			final URL metadataUrl = new URL(
					baseMavenURL + (baseMavenURL.endsWith("/") ? "" : "/") + temp + "/" + artifactId + "/" + version + "/maven-metadata.xml");

			// Need to download the maven-metadata.xml file
			final Task<File> task = new DownloadUnzipTask(mavenUsername, mavenPassword, metadataUrl, false, false, null);

			WorkExecutors.get().getExecutor().execute(task);

			final File metadataFile = task.get();
			final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

			// added to avoid XXE injections
			domFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

			DocumentBuilder builder;
			Document dDoc = null;
			final XPath xPath = XPathFactory.newInstance().newXPath();

			builder = domFactory.newDocumentBuilder();
			dDoc = builder.parse(metadataFile);

			final String timestamp = ((Node) xPath.evaluate("/metadata/versioning/snapshot/timestamp", dDoc, XPathConstants.NODE)).getTextContent();
			final String buildNumber = ((Node) xPath.evaluate("/metadata/versioning/snapshot/buildNumber", dDoc, XPathConstants.NODE)).getTextContent();

			snapshotVersion = "-" + timestamp + "-" + buildNumber;
			metadataFile.delete();

			// The download task makes a subfolder in temp for this, delete that too
			metadataFile.getParentFile().delete();
		}

		return temp + "/" + artifactId + "/" + version + "/" + artifactId + "-" + versionWithoutSnapshot + snapshotVersion
				+ (StringUtils.isNotBlank(classifier) ? "-" + classifier : "") + "." + type;
	}
}
