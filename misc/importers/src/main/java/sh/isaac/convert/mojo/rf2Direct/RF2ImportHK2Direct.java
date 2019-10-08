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
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.convert.directUtils.DirectConverter;
import sh.isaac.convert.directUtils.DirectConverterBaseMojo;
import sh.isaac.convert.directUtils.DirectWriteHelper;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.pombuilder.converter.ContentConverterCreator;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.isaac.solor.ContentProvider;
import sh.isaac.solor.direct.DirectImporter;
import sh.isaac.solor.direct.ImportType;
import sh.isaac.solor.direct.Rf2RelationshipTransformer;

/**
 * Stub class to keep the various dynamic class loaders happy...
 * {@link RF2ImportHK2Direct}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@PerLookup
public class RF2ImportHK2Direct extends DirectConverterBaseMojo implements DirectConverter
{
	private Logger log = LogManager.getLogger();
	
	/**
	 * For maven and HK2
	 */
	protected RF2ImportHK2Direct()
	{
		
	}
	
	@Override
	public ConverterOptionParam[] getConverterOptions()
	{
		return new Rf2DirectConfigOptions().getConfigOptions();
	}

	@Override
	public void setConverterOption(String internalName, String... values)
	{
		if (internalName.equals(ContentConverterCreator.CLASSIFIERS_OPTION))
		{
			if (values == null || values.length != 1)
			{
				throw new RuntimeException("One and only one option may be set for direct conversion");
			}
			this.converterOutputArtifactClassifier = values[0];
		}
		else
		{
			throw new RuntimeException("Unsupported converter option: " + internalName);
		}
	}
	
	@Override
	public void configure(File outputDirectory, Path inputFolder, String converterSourceArtifactVersion, StampCoordinate stampCoordinate)
	{
		this.outputDirectory = outputDirectory;
		this.inputFileLocationPath = inputFolder;
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
	public void convertContent(Consumer<String> messages, BiConsumer<Double, Double> progressUpdate) throws IOException
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
			
			log.info("Setting up import file structure");
			
			ArrayList<ContentProvider> items = new ArrayList<>();
			
			Files.walk(this.inputFileLocationPath, new FileVisitOption[] {}).forEach(path -> 
			{
				if (Files.isRegularFile(path, new LinkOption[] {}))
				{
					items.add(new ContentProvider(path));
				}
			});
			
			
			DirectImporter importer = new DirectImporter(it, items);
			
			log.info("Importing");
			Future<?> f = Get.executor().submit(importer);
			f.get();

			log.info("Transforming relationships");
			Rf2RelationshipTransformer transformer = new Rf2RelationshipTransformer(it);
			Future<?> transformTask = Get.executor().submit(transformer);
			transformTask.get();
			
			if (this.runningInMaven)
			{
				addModuleMetadata();
			}
			
		}
		catch (Exception e)
		{
			throw new IOException("Convert failed", e);
		}
	}

	@Override
	public void execute() throws MojoExecutionException
	{
		//Set up our function that will put these in the right place...
		//Needs to be a function, because we can't get nids prior to isaac startup
		toIgnore = () -> 
		{
			HashSet<Integer> toIgnore = new HashSet<>();
			toIgnore.add(MetaData.RF2_INFERRED_RELATIONSHIP_ASSEMBLAGE____SOLOR.getNid());
			toIgnore.add(MetaData.RF2_STATED_RELATIONSHIP_ASSEMBLAGE____SOLOR.getNid());
			return toIgnore;
		};
		super.execute();
	}
	
	/**
	 * Note, this routine is only valid if we are doing a maven conversion, as it makes assumptions about modules...
	 * TODO In general, its kinda dumb... and needs to be rewritten.
	 */
	private void addModuleMetadata()
	{
		HashSet<Integer> uniqueModules = new HashSet<>();
		
		StampService ss = Get.stampService();
		
		dwh = new DirectWriteHelper(TermAux.USER.getNid(), MetaData.SNOMED_CT_CORE_MODULES____SOLOR.getNid(), 
				TermAux.DEVELOPMENT_PATH.getNid(), converterUUID, "Snomed", false);

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
				dwh.makeTerminologyMetadataAnnotations(module, converterSourceArtifactVersion, converterSourceArtifactVersion, Optional.empty(), 
						Optional.of(converterOutputArtifactVersion), Optional.ofNullable(converterOutputArtifactClassifier), 
						Optional.of("http://snomed.info/sct"), importTime);
			}
		}
	}
	
	@Override
	protected Path[] getIBDFFilesToPreload() throws IOException
	{
		//There currently is no mechanism to automatically pre-load a required IBDF file if you are running the  converter live.
		//for now, if you are running live, you just have to manually load the reqs first

		// If we are converting an RF2 extension, we likely need snomed international first.  Look for it.... only expect one ibdf file in this folder. 
		final AtomicReference<Path> ibdfFile = new AtomicReference<>();
		Path ibdfPath = inputFileLocationPath.resolve("ibdf");
		if (ibdfPath.toFile().isDirectory())
		{
			Files.walk(ibdfPath, new FileVisitOption[] {}).forEach(path -> {
				if (path.toString().toLowerCase().endsWith(".ibdf"))
				{
					if (ibdfFile.get() != null)
					{
						throw new RuntimeException("Only expected to find one ibdf file in the folder " + inputFileLocationPath.resolve("ibdf").normalize());
					}
					ibdfFile.set(path);
				}
			});
		}

		if (ibdfFile.get() == null)
		{
			log.info("Didn't find any IBDF files to preload");
		}
		else
		{
			log.info("will preload {}", ibdfFile.get());
		}
		return ibdfFile.get() != null ? new Path[] { ibdfFile.get() } : new Path[0];
	}
}