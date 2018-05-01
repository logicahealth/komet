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
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.convert.directUtils.DataWriteListenerImpl;
import sh.isaac.convert.directUtils.LoggingConfig;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.model.DataStore;
import sh.isaac.solor.direct.DirectImporter;
import sh.isaac.solor.direct.ImportType;
import sh.isaac.solor.direct.Rf2RelationshipTransformer;

/**
 * 
 * A mojo wrapper to execute the RF2 direct converter
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@Mojo(name = "convert-RF2-direct-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class Rf2DirectImportMojo extends ConverterBaseMojo
{
	private Logger log = LogManager.getLogger();
	
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

			HashSet<Integer> uniqueModules = new HashSet<>();
			
			StampService ss = Get.stampService();
			
			Get.service(ConverterUUID.class).configureNamespace(MetaData.SNOMED_CT_CORE_MODULES____SOLOR.getPrimordialUuid());
			importUtil = new IBDFCreationUtility(TermAux.USER.getPrimordialUuid(), MetaData.SNOMED_CT_CORE_MODULES____SOLOR.getPrimordialUuid(), 
					TermAux.DEVELOPMENT_PATH.getPrimordialUuid(), listener.getWriterHandle());
			

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
					Get.service(ConverterUUID.class).configureNamespace(module);
					importUtil.setModule(module, importTime);
					log.info("adding loader metadata to {}", Get.conceptDescriptionText(moduleNid));
					
					// loadTerminologyMetadataAttributes on each module that came out of the RF2 content
					importUtil.loadTerminologyMetadataAttributes(converterSourceArtifactVersion, Optional.empty(), converterOutputArtifactVersion,
							Optional.ofNullable(converterOutputArtifactClassifier), converterVersion);
				}
			}
			
			LookupService.shutdownSystem();
			
			listener.close();
			
			log.info("Conversion complete");
		}
		catch (Exception ex)
		{
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}
	}
}