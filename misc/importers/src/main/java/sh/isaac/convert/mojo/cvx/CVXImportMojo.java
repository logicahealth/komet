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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import javafx.application.Platform;
import sh.isaac.MetaData;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.convert.mojo.cvx.data.CVXCodes;
import sh.isaac.convert.mojo.cvx.data.CVXCodes.CVXInfo;
import sh.isaac.convert.mojo.cvx.data.CVXCodesHelper;
import sh.isaac.convert.mojo.cvx.propertyTypes.PT_Annotations;
import sh.isaac.convert.mojo.cvx.propertyTypes.PT_Descriptions;
import sh.isaac.convert.mojo.cvx.reader.CVXReader;
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
 * {@link CVXImportMojo}
 *
 * Goal which converts CVX data into the workbench jbin format
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@Mojo(name = "convert-CVX-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CVXImportMojo extends ConverterBaseMojo
{
	private HashMap<UUID, String> loadedConcepts = new HashMap<>();

	private PropertyType attributes_;
	private BPT_Refsets refsets_;
	private PT_Descriptions descriptions_;

	private UUID allCvxConceptsRefset;

	private int conceptCount = 0;

	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			super.execute();

			// There is no global release date for mvx - but each item has its own date. This date will only be used for metadata.
			Date date = new Date();

			importUtil = new IBDFCreationUtility(Optional.of(CVXConstants.TERMINOLOGY_NAME + " " + converterSourceArtifactVersion),
					Optional.of(MetaData.CVX_MODULES____SOLOR), outputDirectory, converterOutputArtifactId, converterOutputArtifactVersion,
					converterOutputArtifactClassifier, false, date.getTime());

			attributes_ = new PT_Annotations();
			descriptions_ = new PT_Descriptions();

			refsets_ = new BPT_Refsets(CVXConstants.TERMINOLOGY_NAME);
			refsets_.addProperty("All CVX Concepts");

			// Every time concept created add membership to "All CVX Concepts"
			allCvxConceptsRefset = refsets_.getProperty("All CVX Concepts").getUUID();

			final CVXReader importer = new CVXReader(inputFileLocation);
			final CVXCodes terminology = importer.process();

			ConsoleUtil.println("Loaded Terminology containing " + terminology.getCVXInfo().size() + " entries");

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
			final ComponentReference cvxMetadata = ComponentReference.fromConcept(
					createType(MetaData.SOLOR_CONTENT_METADATA____SOLOR.getPrimordialUuid(), "CVX Metadata" + IBDFCreationUtility.METADATA_SEMANTIC_TAG));

			// loadTerminologyMetadataAttributes onto cvxMetadata
			importUtil.loadTerminologyMetadataAttributes(converterSourceArtifactVersion, Optional.empty(), converterOutputArtifactVersion,
					Optional.ofNullable(converterOutputArtifactClassifier), converterVersion);

			// load metadata
			importUtil.loadMetaDataItems(Arrays.asList(attributes_, refsets_, descriptions_), cvxMetadata.getPrimordialUuid());

			ConsoleUtil.println("Metadata summary:");
			for (String s : importUtil.getLoadStats().getSummary())
			{
				ConsoleUtil.println("  " + s);
			}
			importUtil.clearLoadStats();

			// Create CVX root concept under SOLOR_CONCEPT____SOLOR
			final ConceptVersion cvxRootConcept = importUtil.createConcept(CVXConstants.TERMINOLOGY_NAME, true,
					MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid());
			ConsoleUtil.println("Created CVX root concept " + cvxRootConcept.getPrimordialUuid() + " under SOLOR_CONCEPT____SOLOR");

			final UUID fsnSourceDescriptionTypeUUID = PT_Descriptions.Descriptions.ShortDescription.getProperty().getUUID();
			final UUID preferredSynonymSourceDescriptionTypeUUID = PT_Descriptions.Descriptions.FullVaccinename.getProperty().getUUID();

			final UUID cvxCodePropertyUuid = attributes_.getProperty(PT_Annotations.Attribute.CVXCode.getKey()).getUUID();
			final UUID cvxStatusPropertyUuid = attributes_.getProperty(PT_Annotations.Attribute.VaccineStatus.getKey()).getUUID();

			UUID languageCode = MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid();
			UUID dialect = MetaData.US_ENGLISH_DIALECT____SOLOR.getPrimordialUuid();
			UUID caseSignificance = MetaData.DESCRIPTION_CASE_SENSITIVE____SOLOR.getPrimordialUuid();

			for (CVXInfo row : terminology.getCVXInfo())
			{
				try
				{
					final String code = CVXCodesHelper.getCVXCode(row) + "";
					final String fsn = CVXCodesHelper.getShortDescription(row);
					final String preferred = CVXCodesHelper.getFullVaccinename(row);
					final Status status = CVXCodesHelper.getOchreState(row);
					final String cvxStatus = CVXCodesHelper.getStatus(row);
					final long lastUpdateTime = CVXCodesHelper.getLastUpdatedDate(row).getTime();

					// Create row concept
					final UUID rowConceptUuid = ConverterUUID.createNamespaceUUIDFromString(code);
					final ConceptVersion rowConcept = importUtil.createConcept(rowConceptUuid, lastUpdateTime, status, null);
					final ComponentReference rowComponentReference = ComponentReference.fromConcept(rowConcept);
					importUtil.addParent(rowComponentReference, cvxRootConcept.getPrimordialUuid());

					importUtil.addDescription(rowComponentReference, null, fsn, DescriptionType.FULLY_QUALIFIED_NAME, true, dialect, caseSignificance,
							languageCode, null, fsnSourceDescriptionTypeUUID, null, lastUpdateTime);

					importUtil.addDescription(rowComponentReference, null, preferred, DescriptionType.REGULAR_NAME, true, dialect, caseSignificance,
							languageCode, null, preferredSynonymSourceDescriptionTypeUUID, null, lastUpdateTime);

					// Add required CVXCode annotation
					importUtil.addStaticStringAnnotation(rowComponentReference, code, cvxCodePropertyUuid, null);

					// Add required CVX extended Status annotation
					if (!(cvxStatus.toUpperCase().equals("ACTIVE") || cvxStatus.toUpperCase().equals("INACTIVE")))
					{
						importUtil.addAnnotation(rowComponentReference, null, new DynamicStringImpl(cvxStatus), cvxStatusPropertyUuid, null, lastUpdateTime);
					}

					// Add optional Notes comment annotation
					if (StringUtils.isNotBlank(CVXCodesHelper.getNotes(row)))
					{
						importUtil.addAnnotation(rowComponentReference, null, new DynamicStringImpl(CVXCodesHelper.getNotes(row)),
								DynamicConstants.get().DYNAMIC_COMMENT_ATTRIBUTE.getPrimordialUuid(), null, lastUpdateTime);
					}

					// Add to refset allCvxConceptsRefset
					importUtil.addAssemblageMembership(rowComponentReference, allCvxConceptsRefset, null, lastUpdateTime);

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

			for (String line : importUtil.getLoadStats().getSummary())
			{
				ConsoleUtil.println(line);
			}
			// this could be removed from final release. Just added to help debug editor problems.
			ConsoleUtil.println("Dumping UUID Debug File");
			ConverterUUID.dump(outputDirectory, "cvxUuid");

			importUtil.shutdown();
			ConsoleUtil.writeOutputToFile(new File(outputDirectory, "ConsoleOutput.txt").toPath());
		}
		catch (Exception ex)
		{
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}
	}

	private ConceptVersion createType(UUID parentUuid, String typeName) throws Exception
	{
		ConceptVersion concept = importUtil.createConcept(typeName, true);
		loadedConcepts.put(concept.getPrimordialUuid(), typeName);
		importUtil.addParent(ComponentReference.fromConcept(concept), parentUuid);
		return concept;
	}

	public static void main(String[] args) throws MojoExecutionException
	{
		CVXImportMojo i = new CVXImportMojo();
		i.outputDirectory = new File("../../integration/db-config-builder-ui/target/converter-executor/target/");
		i.inputFileLocation= new File("../../integration/db-config-builder-ui/target/converter-executor/target/generated-resources/src");
		i.converterOutputArtifactVersion = "2016.01.07.foo";
		i.converterVersion = "SNAPSHOT";
		i.converterSourceArtifactVersion = "17.0";
		i.execute();
		Platform.exit();
	}
}