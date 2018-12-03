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

package sh.isaac.convert.directUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import sh.isaac.MetaData;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.ConfigurationService.BuildMode;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.datastore.DataStore;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.mojo.LoadTermstore;

/**
 *
 * {@link DirectConverterBaseMojo}
 *
 * Base mojo class with shared parameters for reuse by terminology specific converters.
 * 
 * This base mojo is intended for converters that have been rewritten as "direct" importers.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class DirectConverterBaseMojo extends AbstractMojo
{
	protected Logger log = LogManager.getLogger();

	protected ConverterUUID converterUUID;
	protected DirectWriteHelper dwh;

	//Variables for the progress printer
	protected boolean runningInMaven = false;
	private boolean disableFancyConsoleProgress = (System.console() == null);
	private int printsSinceReturn = 0;
	private int lastStatus;
	
	protected StampCoordinate readbackCoordinate;
	
	/**
	 * A optional function that can be specified by a concrete subclass, which when called, will provide a set of nids that will be passed
	 * into {@link DataWriteListenerImpl#DataWriteListenerImpl(Path, java.util.Set)} as the assemblage types to ignore.
	 * This function will be executed AFTER isaac is started.
	 */
	protected Supplier<HashSet<Integer>> toIgnore = null;

	/**
	 * Location to write the output file.
	 */
	@Parameter(required = true, defaultValue = "${project.build.directory}")
	protected File outputDirectory;

	/**
	 * Location of the input source file(s). May be a file or a directory, depending on the specific loader. Usually a
	 * directory.
	 */
	@Parameter(required = true)
	private File inputFileLocation;
	
	//The file version, above, gets populated by maven, but we need to work with the Path version, to make things happy with both
	//maven and the GUI impl
	protected Path inputFileLocationPath;

	/**
	 * Output artifactId.
	 */
	@Parameter(required = true, defaultValue = "${project.artifactId}")
	protected String converterOutputArtifactId;

	/**
	 * Loader version number.
	 */
	@Parameter(required = true, defaultValue = "${loader.version}")
	protected String converterVersion;

	/**
	 * Converter result version number.
	 */
	@Parameter(required = true, defaultValue = "${project.version}")
	protected String converterOutputArtifactVersion;

	/**
	 * Converter result classifier.
	 */
	@Parameter(required = false, defaultValue = "${resultArtifactClassifier}")
	protected String converterOutputArtifactClassifier;

	/**
	 * Converter source artifact version.
	 */
	@Parameter(required = true, defaultValue = "${sourceData.version}")
	protected String converterSourceArtifactVersion;

	/**
	 * Set '-Dsdp' (skipUUIDDebugPublish) on the command line, to prevent the publishing of the debug UUID map (it will
	 * still be created, and written to a file) At the moment, this param is never used in code - it is just used as a
	 * pom trigger (but documented here).
	 */
	@Parameter(required = false, defaultValue = "${sdp}")
	private String createDebugUUIDMapSkipPublish;

	/**
	 * Set '-DskipUUIDDebug' on the command line, to disable the in memory UUID Debug map entirely (this disables UUID
	 * duplicate detection, but significantly cuts the required RAM overhead to run a loader).
	 */
	@Parameter(required = false, defaultValue = "${skipUUIDDebug}")
	private String skipUUIDDebugMap;

	/**
	 * Execute.
	 *
	 * @throws MojoExecutionException the mojo execution exception
	 */
	@Override
	public void execute() throws MojoExecutionException
	{
		runningInMaven = true;
		if (inputFileLocation != null)
		{
			inputFileLocationPath = inputFileLocation.toPath();
		}
		try
		{
			// Set up the output
			if (!this.outputDirectory.exists())
			{
				this.outputDirectory.mkdirs();
			}

			LoggingConfig.configureLogging(outputDirectory, converterOutputArtifactClassifier);
			
			log.info(this.getClass().getName() + " converter begins in maven mode");
			
			converterUUID = Get.service(ConverterUUID.class);
			converterUUID.clearCache();
			converterUUID.configureNamespace(UuidT5Generator.PATH_ID_FROM_FS_DESC);

			converterUUID.setUUIDMapState(
					((this.skipUUIDDebugMap == null) || (this.skipUUIDDebugMap.length() == 0)) ? true : !Boolean.parseBoolean(this.skipUUIDDebugMap));

			if (!converterUUID.isUUIDMapEnabled())
			{
				log.info("The UUID Debug map is disabled - this also prevents duplicate ID detection");
			}

			String outputName = converterOutputArtifactId
					+ (StringUtils.isBlank(converterOutputArtifactClassifier) ? "" : "-" + converterOutputArtifactClassifier) + "-"
					+ converterOutputArtifactVersion;
			Path ibdfFileToWrite = new File(outputDirectory, outputName + ".ibdf").toPath();
			ibdfFileToWrite.toFile().delete();

			log.info("Writing IBDF to " + ibdfFileToWrite.toFile().getCanonicalPath());

			File file = new File(outputDirectory, "isaac-db");
			// make sure this is empty
			FileUtils.deleteDirectory(file);

			Get.configurationService().setDataStoreFolderPath(file.toPath());

			LookupService.startupPreferenceProvider();

			Get.configurationService().setDBBuildMode(BuildMode.IBDF);  //enabled the nid to uuid cache
			Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);

			LookupService.startupIsaac();
			
			readbackCoordinate = StampCoordinates.getDevelopmentLatest();

			// Don't need to build indexes
			for (IndexBuilderService ibs : LookupService.getServices(IndexBuilderService.class))
			{
				ibs.setEnabled(false);
			}
			
			File[] filesToPreload = getIBDFFilesToPreload();
			if (filesToPreload != null && filesToPreload.length > 0)
			{
				log.info("Preloading IBDF files");
				LoadTermstore lt = new LoadTermstore();
				lt.dontSetDBMode();
				lt.setLog(new SystemStreamLog());
				lt.setibdfFiles(filesToPreload);
				lt.setActiveOnly(IBDFPreloadActiveOnly());
				lt.skipVersionTypes(getIBDFSkipTypes());
				lt.execute();
			}
			
			DataWriteListenerImpl listener = new DataWriteListenerImpl(ibdfFileToWrite, toIgnore == null ? null : toIgnore.get());

			// we register this after the metadata has already been written.
			LookupService.get().getService(DataStore.class).registerDataWriteListener(listener);

			convertContent(statusUpdate -> {}, (workDone, workTotal) -> {});

			LookupService.shutdownSystem();

			listener.close();
			
			log.info("Conversion complete");
		}
		catch (Exception ex)
		{
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}
	}

	/**
	 * Where the logic should be implemented to actually do the conversion
	 * @param statusUpdates - the converter should post status updates here.
	 * @param progresUpdates - optional - if provided, the converter should post progress on workDone here, the first argument
	 * is work done, the second argument is work total.
	 * @throws IOException 
	 */
	protected abstract void convertContent(Consumer<String> statusUpdates, BiConsumer<Double, Double> progresUpdates) throws IOException;
	
	
	/**
	 * Implementors should override this method, if they have IBDF files that should be pre-loaded, prior to the callback to 
	 * {@link #convertContent(Consumer)}, and prior to the injection of the IBDF change set listener.
	 * 
	 * This is only used by loaders that cannot execute without having another terminology preloaded - such as snomed extensions
	 * that need to do snomed lookups, or loinc tech preview, which requires snomed, and loinc, for example.
	 * @return
	 */
	protected File[] getIBDFFilesToPreload()
	{
		return new File[0];
	}
	
	/**
	 * Subclasses may override this, if they want to change the behavior.  The default behavior is to preload only active concepts
	 * and semantics
	 * @return
	 */
	protected boolean IBDFPreloadActiveOnly()
	{
		return true;
	}
	
	/**
	 * Subclasses may override this, if they want to change the behavior.  If a subclass provides a return that includes
	 * {@link VersionType#COMPONENT_NID}, for example, then that type will be skipped when encountered during IBDF preload
	 * @return
	 */
	protected Collection<VersionType> getIBDFSkipTypes()
	{
		return new HashSet<>(0);
	}
	
	/**
	 * Create the version specific module concept, and add the loader metadata to it.
	 * @param name
	 * @param parentModule
	 * @param releaseTime
	 * @return
	 */
	protected UUID setupModule(String name, UUID parentModule, long releaseTime)
	{
		//Create a module for this version.
		String fullName = name + " " + converterSourceArtifactVersion + " module";
		UUID versionModule = converterUUID.createNamespaceUUIDFromString(fullName);
		int moduleNid = Get.identifierService().assignNid(versionModule);
		dwh.changeModule(moduleNid);  //change to the version specific module for all future work.
		
		//Actually create the module concept
		dwh.makeConcept(versionModule, Status.ACTIVE, releaseTime);
		dwh.makeDescriptionEnNoDialect(versionModule, fullName + " (" + ConceptProxy.METADATA_SEMANTIC_TAG + ")", 
				MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
				Status.ACTIVE, releaseTime);
		dwh.makeDescriptionEnNoDialect(versionModule, fullName, 
				MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
				Status.ACTIVE, releaseTime);
		dwh.makeParentGraph(versionModule, Arrays.asList(new UUID[] {parentModule}), Status.ACTIVE, releaseTime);
		
		dwh.makeTerminologyMetadataAnnotations(versionModule, converterSourceArtifactVersion, Optional.of(new Date(releaseTime).toString()), 
				Optional.ofNullable(converterOutputArtifactVersion), Optional.ofNullable(converterOutputArtifactClassifier), releaseTime);
		
		return versionModule;
	}

	/**
	 * Should be called after calling showProgress(), but prior to logging anything else, to advance the console prior to the log output.
	 * Does nothing if not running on a console in maven mode
	 */
	protected void advanceProgressLine()
	{
		if (runningInMaven && printsSinceReturn > 0)
		{
			System.out.println();
			printsSinceReturn = 0;
		}
	}

	/**
	 * Can be called by converters so show progress on a console in maven mode
	 * Does nothing if not running on a console in maven mode
	 */
	protected void showProgress()
	{
		if (runningInMaven)
		{
			lastStatus++;

			if (lastStatus > 3)
			{
				lastStatus = 0;
			}

			if (disableFancyConsoleProgress)
			{
				System.out.print(".");
				printsSinceReturn++;

				if (printsSinceReturn >= 75)
				{
					advanceProgressLine();
				}
			}
			else
			{
				char c;

				switch (lastStatus)
				{
					case 0:
						c = '/';
						break;

					case 1:
						c = '-';
						break;

					case 2:
						c = '\\';
						break;

					case 3:
						c = '|';
						break;

					default :  // shouldn't be used
						c = '-';
						break;
				}
				if (printsSinceReturn == 0)
				{
					System.out.print(c);
				}
				else
				{
					System.out.print("\r" + c);
				}
				printsSinceReturn++;
			}
		}
		else
		{
			//TODO maybe provide a progress bar?
		}
	}
}
