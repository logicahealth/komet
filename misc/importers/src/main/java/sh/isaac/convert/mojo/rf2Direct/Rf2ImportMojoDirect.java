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

package sh.isaac.convert.mojo.rf2Direct;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.FileUtils;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.datastore.DataStore;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.convert.directUtils.DataWriteListenerImpl;
import sh.isaac.convert.directUtils.DirectConverter;
import sh.isaac.convert.directUtils.DirectConverterBaseMojo;
import sh.isaac.convert.directUtils.DirectWriteHelper;
import sh.isaac.convert.directUtils.LoggingConfig;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.isaac.solor.direct.DirectImporter;
import sh.isaac.solor.direct.ImportType;
import sh.isaac.solor.direct.Rf2RelationshipTransformer;

/**
 * 
 * A mojo wrapper to execute the RF2 direct converter
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@Mojo(name = "convert-RF2-direct-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
@Service
@PerLookup
public class Rf2DirectImportMojo extends DirectConverterBaseMojo implements DirectConverter
{
	private Logger log = LogManager.getLogger();
	
	/**
	 * For maven and HK2
	 */
	public Rf2DirectImportMojo()
	{
		
	}
	
	@Override
	public void configure(File outputDirectory, File inputFolder, String converterSourceArtifactVersion, StampCoordinate stampCoordinate)
	{
		this.outputDirectory = outputDirectory;
		this.inputFileLocation = inputFolder;
		this.converterSourceArtifactVersion = converterSourceArtifactVersion;
		this.converterUUID = new ConverterUUID(UuidT5Generator.PATH_ID_FROM_FS_DESC, false);
		this.readbackCoordinate = stampCoordinate == null ? StampCoordinates.getDevelopmentLatest() : stampCoordinate;
	}

	@Override
	public SupportedConverterTypes[] getSupportedTypes()
	{
		return new SupportedConverterTypes[] {SupportedConverterTypes.SCT, SupportedConverterTypes.SCT_EXTENSION};
	}

	@Override
	public void convertContent() throws IOException
	{
		try
		{
			ImportType it = ImportType.parseFromString(converterOutputArtifactClassifier);
			
			if (it == ImportType.DELTA)
			{
				throw new IOException("import type delta not yet supported by direct import");
			}
			
			converterUUID = new ConverterUUID(UuidT5Generator.PATH_ID_FROM_FS_DESC, true);
			
			DirectImporter.importDynamic = true;
			DirectImporter importer = new DirectImporter(it, inputFileLocation.getCanonicalFile());
			
			log.info("Importing");
			Future<?> f = Get.executor().submit(importer);
			f.get();

			log.info("Transforming relationships");
			Rf2RelationshipTransformer transformer = new Rf2RelationshipTransformer(it);
			Future<?> transformTask = Get.executor().submit(transformer);
			transformTask.get();
		}
		catch (Exception e)
		{
			throw new IOException("Convert failed", e);
		}
	}

	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			LoggingConfig.configureLogging(outputDirectory, converterOutputArtifactClassifier);
			
			ImportType it = ImportType.parseFromString(converterOutputArtifactClassifier);
			
			if (it == ImportType.DELTA)
			{
				log.warn("import type delta not yet supported by direct import");
				return;
			}
			converterUUID = Get.service(ConverterUUID.class);
			converterUUID.clearCache();
			
			String outputName = converterOutputArtifactId 
					+ (StringUtils.isBlank(converterOutputArtifactClassifier) ? "" : "-" + converterOutputArtifactClassifier) + "-" + converterOutputArtifactVersion;
			Path ibdfFileToWrite = new File(outputDirectory, outputName + ".ibdf").toPath();
			ibdfFileToWrite.toFile().delete();
			
			log.info("Writing IBDF to " + ibdfFileToWrite.toFile().getCanonicalPath());
			
			File file = new File(outputDirectory, "isaac-db");
			// make sure this is empty
			FileUtils.deleteDirectory(file);

			Get.configurationService().setDataStoreFolderPath(file.toPath());

			LookupService.startupPreferenceProvider();
			
			Get.configurationService().setDatabaseInitializationMode(DatabaseInitialization.LOAD_METADATA);

			LookupService.startupIsaac();
			
			//Don't need to build indexes
			for (IndexBuilderService ibs : LookupService.getServices(IndexBuilderService.class))
			{
				ibs.setEnabled(false);
			}
			
			HashSet<Integer> toIgnore = new HashSet<>();
			toIgnore.add(MetaData.RF2_INFERRED_RELATIONSHIP_ASSEMBLAGE____SOLOR.getNid());
			toIgnore.add(MetaData.RF2_STATED_RELATIONSHIP_ASSEMBLAGE____SOLOR.getNid());
			
			DataWriteListenerImpl listener = new DataWriteListenerImpl(ibdfFileToWrite, toIgnore);
			
			//we register this after the metadata has already been written.
			LookupService.get().getService(DataStore.class).registerDataWriteListener(listener);

			log.info("Setting up import file structure");
			
			DirectImporter.importDynamic = true;
			DirectImporter importer = new DirectImporter(it, inputFileLocation.getCanonicalFile());
			
			log.info("Importing");
			Future<?> f = Get.executor().submit(importer);
			f.get();

			log.info("Transforming relationships");
			Rf2RelationshipTransformer transformer = new Rf2RelationshipTransformer(it);
			Future<?> transformTask = Get.executor().submit(transformer);
			transformTask.get();

			addModuleMetadata();
			
			LookupService.shutdownSystem();
			
			listener.close();
			
			log.info("Conversion complete");
		}
		catch (Exception ex)
		{
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}
	}
	
	/*
	 * Note, this routine is only valid if we are doing a maven conversion, as it makes assumptions about modules...
	 * In general, its kinda dumb... and needs to be rewritten.
	 */
	private void addModuleMetadata()
	{
		HashSet<Integer> uniqueModules = new HashSet<>();
		
		StampService ss = Get.stampService();
		
		converterUUID.configureNamespace(MetaData.SNOMED_CT_CORE_MODULES____SOLOR.getPrimordialUuid());
		
		dwh = new DirectWriteHelper(TermAux.USER.getNid(), MetaData.SNOMED_CT_CORE_MODULES____SOLOR.getNid(), 
				TermAux.DEVELOPMENT_PATH.getNid(), converterUUID);

		log.info("Reading modules");
		ss.getStampSequences().forEach(stampSequence -> 
		{
			uniqueModules.add(ss.getModuleNidForStamp(stampSequence));
		});
		long importTime = System.currentTimeMillis();
		
		for (int moduleNid : uniqueModules)
		{
			if (moduleNid != MetaData.SOLOR_OVERLAY_MODULE____SOLOR.getNid() && moduleNid != MetaData.SOLOR_MODULE____SOLOR.getNid())
			{
				UUID module = Get.identifierService().getUuidPrimordialForNid(moduleNid);
				converterUUID.configureNamespace(module);
				dwh.changeModule(moduleNid);
				log.info("adding loader metadata to {}", Get.conceptDescriptionText(moduleNid));
				
				// loadTerminologyMetadataAttributes on each module that came out of the RF2 content
				dwh.makeTerminologyMetadataAnnotations(module, converterSourceArtifactVersion, Optional.empty(), 
						Optional.of(converterOutputArtifactVersion), Optional.ofNullable(converterOutputArtifactClassifier), importTime);
			}
		}
	}
}