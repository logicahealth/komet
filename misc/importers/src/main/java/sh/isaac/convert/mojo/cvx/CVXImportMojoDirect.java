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

package sh.isaac.convert.mojo.cvx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.convert.directUtils.DirectConverter;
import sh.isaac.convert.directUtils.DirectConverterBaseMojo;
import sh.isaac.convert.directUtils.DirectWriteHelper;
import sh.isaac.convert.mojo.cvx.data.CVXCodes;
import sh.isaac.convert.mojo.cvx.data.CVXCodes.CVXInfo;
import sh.isaac.convert.mojo.cvx.data.CVXCodesHelper;
import sh.isaac.convert.mojo.cvx.reader.CVXReader;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;

/**
 * 
 * {@link CVXImportMojoDirect}
 *
 * Goal which converts CVX data into the workbench jbin format
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@Mojo(name = "convert-CVX-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CVXImportMojoDirect extends DirectConverterBaseMojo implements DirectConverter
{
	private int conceptCount = 0;
	
	/**
	 * This constructor is for maven and HK2 and should not be used at runtime.  You should 
	 * get your reference of this class from HK2, and then call the {@link #configure(File, Path, String, StampCoordinate)} method on it.
	 */
	public CVXImportMojoDirect()
	{
		
	}
	
	@Override
	public ConverterOptionParam[] getConverterOptions()
	{
		return new ConverterOptionParam[] {};
	}

	@Override
	public void setConverterOption(String internalName, String... values)
	{
		//noop, we don't require any.
	}
	
	/**
	 * If this was constructed via HK2, then you must call the configure method prior to calling {@link #convertContent()}
	 * If this was constructed via the constructor that takes parameters, you do not need to call this.
	 * 
	 * @see sh.isaac.convert.directUtils.DirectConverter#configure(java.io.File, java.io.File, java.lang.String, sh.isaac.api.coordinate.StampCoordinate)
	 */
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
		return new SupportedConverterTypes[] {SupportedConverterTypes.CVX};
	}

	/**
	 * @see sh.isaac.convert.directUtils.DirectConverterBaseMojo#convertContent(Consumer))
	 * @see DirectConverter#convertContent(Consumer))
	 */
	@Override
	public void convertContent(Consumer<String> statusUpdates) throws IOException 
	{
		// There is no global release date for mvx - but each item has its own date. This date will only be used for metadata.
		Date date = new Date();
		
		//Right now, we are configured for the CPT grouping modules nid
		dwh = new DirectWriteHelper(TermAux.USER.getNid(), MetaData.CVX_MODULES____SOLOR.getNid(), MetaData.DEVELOPMENT_PATH____SOLOR.getNid(), converterUUID, 
				"CVX", false);
		
		setupModule("CVX", MetaData.CPT_MODULES____SOLOR.getPrimordialUuid(), date.getTime());
		
		//Set up our metadata hierarchy
		dwh.makeMetadataHierarchy(true, true, true, false, true, false, date.getTime());
		
		dwh.makeDescriptionTypeConcept(null, "Short Description", null, null,
				MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, date.getTime());
		
		dwh.makeDescriptionTypeConcept(null, "Full Vaccine Name", null, null,
				MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, date.getTime());
		
		dwh.linkToExistingAttributeTypeConcept(MetaData.CODE____SOLOR, date.getTime(), readbackCoordinate);
		
		dwh.makeAttributeTypeConcept(null, CVXFieldsV1.VaccineStatus.name(), null, null, false, DynamicDataType.STRING, null, date.getTime());

		// Every time concept created add membership to "All CPT Concepts"
		dwh.makeRefsetTypeConcept(null, "All CVX Concepts", null, null, date.getTime());

		final CVXReader importer = new CVXReader(inputFileLocationPath);
		CVXCodes terminology;
		try
		{
			terminology = importer.process();
		}
		catch (JAXBException e1)
		{
			throw new IOException("Error reading file", e1);
		}

		log.info("Read " + terminology.getCVXInfo().size() + " entries");
		statusUpdates.accept("Read " + terminology.getCVXInfo().size() + " entries");

		/*
		 * Methods from CVXCodes.CVXInfo:
		 * getCVXCode() // float numeric id (CODE?)
		 * getShortDescription() // Required String FSN description?
		 * getFullVaccinename() // Required String preferred term description?
		 * getNotes() // Optional String comment
		 * getOchreState() // Required State (ACTIVE or INACTIVE)
		 * getLastUpdatedDate(), // Required date ?
		 */
		// Parent cvxMetadata ComponentReference

		log.info("Metadata load stats");
		for (String line : dwh.getLoadStats().getSummary())
		{
			log.info(line);
		}
		
		dwh.clearLoadStats();
		
		statusUpdates.accept("Loading content");

		// Create CVX root concept under SOLOR_CONCEPT____SOLOR
		final UUID cvxRootConcept = dwh.makeConceptEnNoDialect("CVX", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
				new UUID[] {MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid()}, Status.ACTIVE, date.getTime());

		for (CVXInfo row : terminology.getCVXInfo())
		{
			try
			{
				final String code = CVXCodesHelper.getCVXCode(row) + "";
				final String shortDesc = CVXCodesHelper.getShortDescription(row);
				final String vacName = CVXCodesHelper.getFullVaccinename(row);
				final Status status = CVXCodesHelper.getOchreState(row);
				final String cvxStatus = CVXCodesHelper.getStatus(row);
				final long lastUpdateTime = CVXCodesHelper.getLastUpdatedDate(row).getTime();

				// Create row concept
				final UUID rowConcept = dwh.makeConcept(converterUUID.createNamespaceUUIDFromString(code), status, lastUpdateTime);
				dwh.makeParentGraph(rowConcept, cvxRootConcept, Status.ACTIVE, lastUpdateTime);

				dwh.makeDescriptionEnNoDialect(rowConcept, shortDesc, dwh.getDescriptionType("Short Description"), status, lastUpdateTime);
				dwh.makeDescriptionEnNoDialect(rowConcept, vacName, dwh.getDescriptionType("Full Vaccine Name"), status, lastUpdateTime);

				// Add required CVXCode annotation
				dwh.makeBrittleStringAnnotation(MetaData.CODE____SOLOR.getPrimordialUuid(), rowConcept, code, lastUpdateTime);

				// Add required CVX extended Status annotation
				dwh.makeDynamicSemantic(dwh.getAttributeType(CVXFieldsV1.VaccineStatus.name()), rowConcept, 
						new DynamicData[] {new DynamicStringImpl(cvxStatus)}, lastUpdateTime);

				// Add optional Notes comment annotation
				if (StringUtils.isNotBlank(CVXCodesHelper.getNotes(row)))
				{
					dwh.makeComment(rowConcept, CVXCodesHelper.getNotes(row), null, lastUpdateTime);
				}

				// Add to refset allCvxConceptsRefset
				dwh.makeDynamicRefsetMember(dwh.getRefsetType("All CVX Concepts"), rowConcept, lastUpdateTime);

				++conceptCount;
			}
			catch (Exception e)
			{
				final String msg = "Failed processing row with " + e.getClass().getSimpleName() + " " + e.getLocalizedMessage() + ": " + row;
				throw new RuntimeException(msg, e);
			}
		}
		
		dwh.processTaxonomyUpdates();
		Get.taxonomyService().notifyTaxonomyListenersToRefresh();
		
		log.info("Processed " + conceptCount + " concepts");
		statusUpdates.accept("Processed " + conceptCount + " concepts");
		
		log.info("Load Statistics");

		for (String line : dwh.getLoadStats().getSummary())
		{
			log.info(line);
		}

		// this could be removed from final release. Just added to help debug editor problems.
		if (outputDirectory != null)
		{
			log.info("Dumping UUID Debug File");
			converterUUID.dump(outputDirectory, "cvxUuid");
		}
		converterUUID.clearCache();		
	}
}