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

package sh.isaac.convert.mojo.mvx;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import sh.isaac.MetaData;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.convert.mojo.mvx.data.MVXCodes;
import sh.isaac.convert.mojo.mvx.data.MVXCodes.MVXInfo;
import sh.isaac.convert.mojo.mvx.data.MVXCodesHelper;
import sh.isaac.convert.mojo.mvx.propertyTypes.PT_Annotations;
import sh.isaac.convert.mojo.mvx.propertyTypes.PT_Descriptions;
import sh.isaac.convert.mojo.mvx.reader.MVXReader;
import sh.isaac.converters.sharedUtils.ComponentReference;
import sh.isaac.converters.sharedUtils.ConsoleUtil;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility.DescriptionType;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Refsets;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyType;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.model.semantic.types.DynamicStringImpl;

/**
 * 
 * {@link MVXImportMojo}
 *
 * Goal which converts MVX data into the workbench jbin format
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@Mojo(name = "convert-MVX-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class MVXImportMojo extends ConverterBaseMojo
{
	private IBDFCreationUtility importUtil_;

	private HashMap<UUID, String> loadedConcepts = new HashMap<>();

	private PropertyType attributes_;
	private BPT_Refsets refsets_;
	private PT_Descriptions descriptions_;

	private UUID allMvxConceptsRefset;

	private int conceptCount = 0;

	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			super.execute();

			// There is no global release date for mvx - but each item has its own date. This date will only be used for metadata.
			Date date = new Date();

			importUtil_ = new IBDFCreationUtility(Optional.of("MVX" + " " + converterSourceArtifactVersion), Optional.of(MetaData.MVX_MODULES____SOLOR),
					outputDirectory, converterOutputArtifactId, converterOutputArtifactVersion, converterOutputArtifactClassifier, false, date.getTime());

			attributes_ = new PT_Annotations();
			descriptions_ = new PT_Descriptions();

			refsets_ = new BPT_Refsets("MVX");
			refsets_.addProperty("All MVX Concepts");

			// Every time concept created add membership to "All MVX Concepts"
			allMvxConceptsRefset = refsets_.getProperty("All MVX Concepts").getUUID();

			final MVXReader importer = new MVXReader(inputFileLocation);
			final MVXCodes terminology = importer.process();

			ConsoleUtil.println("Loaded Terminology containing " + terminology.getMVXInfo().size() + " entries");

			/*
			 * Methods from MVXCodes.MVXInfo:
			 * MVX_CODE() // String (MetaData.MVX_CODE)
			 * getShortDescription() // Required String FSN description?
			 * getFullVaccinename() // Required String preferred term description?
			 * getNotes() // Optional String comment
			 * getOchreState() // Required State (ACTIVE or INACTIVE)
			 * getLastUpdatedDate(), // Required date ?
			 */
			// Parent mvxMetadata ComponentReference
			final ComponentReference mvxMetadata = ComponentReference.fromConcept(
					createType(MetaData.SOLOR_CONTENT_METADATA____SOLOR.getPrimordialUuid(), "MVX Metadata" + IBDFCreationUtility.METADATA_SEMANTIC_TAG));

			// loadTerminologyMetadataAttributes onto mvxMetadata
			importUtil_.loadTerminologyMetadataAttributes(converterSourceArtifactVersion, Optional.empty(), converterOutputArtifactVersion,
					Optional.ofNullable(converterOutputArtifactClassifier), converterVersion);

			// load metadata
			importUtil_.loadMetaDataItems(Arrays.asList(attributes_, refsets_, descriptions_), mvxMetadata.getPrimordialUuid());

			ConsoleUtil.println("Metadata summary:");
			for (String s : importUtil_.getLoadStats().getSummary())
			{
				ConsoleUtil.println("  " + s);
			}
			importUtil_.clearLoadStats();

			// Create MVX root concept under SOLOR_CONCEPT____SOLOR
			final ConceptChronology mvxRootConcept = importUtil_.createConcept("MVX", true, MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid());
			ConsoleUtil.println("Created MVX root concept " + mvxRootConcept.getPrimordialUuid() + " under SOLOR_CONCEPT____SOLOR");

			final UUID fsnSourceDescriptionTypeUUID = PT_Descriptions.Descriptions.ManufacturerName.getProperty().getUUID();
			final UUID mvxCodePropertyUuid = PT_Annotations.Attribute.MVX_CODE.getProperty().getUUID();

			UUID languageCode = MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid();
			UUID dialect = MetaData.US_ENGLISH_DIALECT____SOLOR.getPrimordialUuid();
			UUID caseSignificance = MetaData.DESCRIPTION_CASE_SENSITIVE____SOLOR.getPrimordialUuid();

			for (MVXInfo row : terminology.getMVXInfo())
			{
				try
				{
					String code = MVXCodesHelper.getMvxCode(row) + "";
					String fsn = MVXCodesHelper.getManufacturerName(row);
					Status state = MVXCodesHelper.getState(row);
					long lastUpdated = MVXCodesHelper.getLastUpdatedDate(row).getTime();

					// Create row concept
					UUID rowConceptUuid = ConverterUUID.createNamespaceUUIDFromString(code);
					final ConceptChronology rowConcept = importUtil_.createConcept(rowConceptUuid, lastUpdated, state, null);
					final ComponentReference rowComponentReference = ComponentReference.fromConcept(rowConcept);
					importUtil_.addParent(rowComponentReference, mvxRootConcept.getPrimordialUuid());

					importUtil_.addDescription(rowComponentReference, null, fsn, DescriptionType.FULLY_QUALIFIED_NAME, true, dialect, caseSignificance,
							languageCode, null, fsnSourceDescriptionTypeUUID, null, lastUpdated);

					// Add required MVXCode annotation
					importUtil_.addStaticStringAnnotation(rowComponentReference, code, mvxCodePropertyUuid, null);

					// Add optional Notes comment annotation
					if (StringUtils.isNotBlank(MVXCodesHelper.getNotes(row)))
					{
						importUtil_.addAnnotation(rowComponentReference, null, new DynamicStringImpl(MVXCodesHelper.getNotes(row)),
								DynamicConstants.get().DYNAMIC_COMMENT_ATTRIBUTE.getPrimordialUuid(), null, lastUpdated);
					}

					// Add to refset allMvxConceptsRefset
					importUtil_.addAssemblageMembership(rowComponentReference, allMvxConceptsRefset, null, lastUpdated);

					++conceptCount;
				}
				catch (Exception e)
				{
					final String msg = "Failed processing row with " + e.getClass().getSimpleName() + " " + e.getLocalizedMessage() + ": " + row;
					ConsoleUtil.printErrorln(msg);
					throw new RuntimeException(msg, e);
				}
			}
			ConsoleUtil.println("Processed " + conceptCount + " concepts");

			ConsoleUtil.println("Load Statistics");

			for (String line : importUtil_.getLoadStats().getSummary())
			{
				ConsoleUtil.println(line);
			}
			// this could be removed from final release. Just added to help debug editor problems.
			ConsoleUtil.println("Dumping UUID Debug File");
			ConverterUUID.dump(outputDirectory, "mvxUuid");

			importUtil_.shutdown();
			ConsoleUtil.writeOutputToFile(new File(outputDirectory, "ConsoleOutput.txt").toPath());
		}
		catch (Exception ex)
		{
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}
	}

	private ConceptChronology createType(UUID parentUuid, String typeName) throws Exception
	{
		ConceptChronology concept = importUtil_.createConcept(typeName, true);
		loadedConcepts.put(concept.getPrimordialUuid(), typeName);
		importUtil_.addParent(ComponentReference.fromConcept(concept), parentUuid);
		return concept;
	}

	public static void main(String[] args) throws MojoExecutionException
	{
		MVXImportMojo i = new MVXImportMojo();
		i.outputDirectory = new File("../mvx-ibdf/target");
		i.inputFileLocation = new File("../mvx-ibdf/target/generated-resources/src/");
		i.converterOutputArtifactVersion = "2016.01.07.foo";
		i.converterVersion = "SNAPSHOT";
		i.converterSourceArtifactVersion = "17.0";
		i.execute();
	}
}