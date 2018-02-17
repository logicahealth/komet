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
package sh.isaac.convert.mojo.icd10;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;
import javafx.application.Platform;
import sh.isaac.MetaData;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.convert.mojo.icd10.data.ICD10;
import sh.isaac.convert.mojo.icd10.propertyTypes.PT_Annotations;
import sh.isaac.convert.mojo.icd10.propertyTypes.PT_Descriptions;
import sh.isaac.convert.mojo.icd10.reader.ICD10Reader;
import sh.isaac.converters.sharedUtils.ComponentReference;
import sh.isaac.converters.sharedUtils.ConsoleUtil;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility.DescriptionType;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Refsets;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyType;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;

/**
 * 
 * Loader code to convert ICD 10 into isaac.
 * 
 * Paths are typically controlled by maven, however, the main() method has paths configured so that they
 * match what maven does for test purposes.
 */
@Mojo(name = "convert-ICD10-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class ICD10ImportMojo extends ConverterBaseMojo
{
	private IBDFCreationUtility importUtil_;

	private HashMap<UUID, String> loadedConcepts = new HashMap<>();

	private PropertyType attributes_;
	private BPT_Refsets refsets_;
	private PT_Descriptions descriptions_;

	private UUID allIcdConceptsRefset, HIPPA_Valid, icdRootConcept;

	private int conceptCount = 0;
	Map<String, UUID> codeToUuid = new HashMap<>();

	/**
	 * Converter source type - should be an ICD 10 variation such as 'PCS' or 'CM'
	 */
	@Parameter(required = true)
	protected String sourceType;

	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			ConsoleUtil.println("ICD10 Processing Begins " + new Date().toString());

			super.execute();

			Date date = null;
			try
			{
				date = new SimpleDateFormat("yyyy").parse(converterSourceArtifactVersion);
			}
			catch (Exception e)
			{
				throw new MojoExecutionException("Could not determine date from source Artifact Version - expected the version to be a 4 digit year.");
			}
			ConsoleUtil.println("Codeset date: [" + date + "]");

			if (StringUtils.isEmpty(sourceType))
			{
				throw new MojoExecutionException("sourceType must be set to a value such as 'CM' or 'PCS'");
			}

			importUtil_ = new IBDFCreationUtility(Optional.of("ICD-10-" + sourceType.toUpperCase() + " " + converterSourceArtifactVersion),
					Optional.of(MetaData.ICD10_MODULES____SOLOR), outputDirectory, converterOutputArtifactId, converterOutputArtifactVersion,
					converterOutputArtifactClassifier, false, date.getTime());

			// Normally, the importer configures this to the parent ICD10 modules UUID - but then we get duplicates generated between CM and PCS.
			// Need to use a different namespace for each.
			ConverterUUID.configureNamespace(UuidT5Generator.get(MetaData.ICD10_MODULES____SOLOR.getPrimordialUuid().toString() + sourceType.toUpperCase()));

			ConsoleUtil.println("Begin code import.");

			// Also reads in codes from file
			final ICD10Reader importer = new ICD10Reader(inputFileLocation, converterSourceArtifactVersion);

			ConsoleUtil.println("Loaded terminology containing " + importer.getAllCodesCount() + " entries");

			attributes_ = new PT_Annotations(sourceType);
			descriptions_ = new PT_Descriptions(sourceType);

			refsets_ = new BPT_Refsets("ICD-10-" + sourceType.toUpperCase());
			refsets_.addProperty("All ICD-10-" + sourceType.toUpperCase() + " Concepts");
			refsets_.addProperty("HIPAA Valid");

			allIcdConceptsRefset = refsets_.getProperty("All ICD-10-" + sourceType.toUpperCase() + " Concepts").getUUID();
			HIPPA_Valid = refsets_.getProperty("HIPAA Valid").getUUID();

			/*
			 * 1: Order number
			 * 2: Code (alpha-numeric, 3-7 characters)
			 * 3: Header (non-HIPAA value) => Subset
			 * 4: Short description (FSN?)
			 * 5: Long description (preferred term?)
			 */

			// Parent icdMetadata ComponentReference
			final ComponentReference icdMetadata = ComponentReference.fromConcept(createType(MetaData.SOLOR_CONTENT_METADATA____SOLOR.getPrimordialUuid(),
					"ICD-10-" + sourceType.toUpperCase() + " Metadata" + IBDFCreationUtility.METADATA_SEMANTIC_TAG));

			// loadTerminologyMetadataAttributes onto icdMetadata
			importUtil_.loadTerminologyMetadataAttributes(converterSourceArtifactVersion, Optional.empty(), converterOutputArtifactVersion,
					Optional.ofNullable(converterOutputArtifactClassifier), converterVersion);

			// load metadata
			importUtil_.loadMetaDataItems(Arrays.asList(attributes_, refsets_, descriptions_), icdMetadata.getPrimordialUuid());

			ConsoleUtil.println("Metadata summary:");
			for (String s : importUtil_.getLoadStats().getSummary())
			{
				ConsoleUtil.println("  " + s);
			}
			importUtil_.clearLoadStats();

			// Create ICD root concept under SOLOR_CONCEPT____SOLOR
			icdRootConcept = importUtil_.createConcept("ICD-10-" + sourceType.toUpperCase(), true, MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid())
					.getPrimordialUuid();
			ConsoleUtil.println("Created ICD-10-" + sourceType.toUpperCase() + " root concept " + icdRootConcept + " under SOLOR_CONCEPT____SOLOR");

			// Process headers first to get parent values for concepts
			importer.getIntermediateHeaderConcepts().forEach(icd10Item -> process(icd10Item));

			// Process leaf concepts
			importer.getLeafConcepts().forEach(icd10Item -> process(icd10Item));

			if (importer.getAllCodesCount() != conceptCount)
			{
				throw new MojoExecutionException("Size mismatch!");
			}

			ConsoleUtil.println("Processed " + conceptCount + " concepts");

			ConsoleUtil.println("Load stats");
			for (String line : importUtil_.getLoadStats().getSummary())
			{
				ConsoleUtil.println(line);
			}

			// this could be removed from final release. Just added to help debug editor problems.
			ConsoleUtil.println("Dumping UUID Debug File");
			ConverterUUID.dump(outputDirectory, "icd10Uuid");

			importUtil_.shutdown();
			ConsoleUtil.writeOutputToFile(new File(outputDirectory, "ConsoleOutput.txt").toPath());
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Exception", e);
		}
	}

	private void process(ICD10 row)
	{
		try
		{
			final String code = row.getCode();
			final String order = row.getOrderNumber();
			final String fsn = row.getShortDescription();
			final String preferred = row.getLongDescription();
			final Status state = Status.ACTIVE;

			// Create row concept
			final UUID rowConceptUuid = ConverterUUID.createNamespaceUUIDFromString(code);
			final ComponentReference conceptReference = ComponentReference.fromConcept(importUtil_.createConcept(rowConceptUuid));

			importUtil_.addDescription(conceptReference, null, fsn, DescriptionType.FULLY_QUALIFIED_NAME, false, null, // dialect
					null, // caseSignificance
					null, // languageCode
					null, descriptions_.getProperty(PT_Descriptions.Descriptions.ShortDescription.name()).getUUID(), state, null);

			importUtil_.addDescription(conceptReference, null, preferred, DescriptionType.REGULAR_NAME, true, null, // dialect
					null, // caseSignificance
					null, // languageCode
					null, descriptions_.getProperty(PT_Descriptions.Descriptions.LongDescription.name()).getUUID(), state, null);

			// Add required ICD-10 Code annotation
			importUtil_.addStaticStringAnnotation(conceptReference, code, attributes_.getProperty(PT_Annotations.Attribute.CODE.get()).getUUID(), state);

			// Add required ICD-10 Order annotation
			importUtil_.addStaticStringAnnotation(conceptReference, order, attributes_.getProperty(PT_Annotations.Attribute.ORDER.get()).getUUID(), state);

			if (!row.isHeader())
			{
				importUtil_.addAssemblageMembership(conceptReference, HIPPA_Valid, state, null);
			}

			// Figure out the correct parent code.
			if (code.length() <= 3)
			{
				// Hang it on root
				importUtil_.addParent(conceptReference, icdRootConcept);
			}
			else
			{
				String parentCode = code.substring(0, code.length() - 1);
				while (true)
				{
					if (parentCode.length() < 3)
					{
						throw new MojoExecutionException("Failed to find parent for " + code);
					}
					UUID temp = codeToUuid.get(parentCode);
					if (temp == null)
					{
						parentCode = code.substring(0, parentCode.length() - 1);
						continue;
					}
					else
					{
						importUtil_.addParent(conceptReference, temp);
						break;
					}
				}
			}

			// Add to refset allIcdConceptsRefset
			importUtil_.addAssemblageMembership(conceptReference, allIcdConceptsRefset, state, null);
			codeToUuid.put(code, conceptReference.getPrimordialUuid());

			conceptCount++;
			if (conceptCount % 500 == 0)
			{
				ConsoleUtil.showProgress();
			}
			if (conceptCount % 10000 == 0)
			{
				ConsoleUtil.println("Processed " + conceptCount + " concepts");
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Exception", e);
		}
	}

	private ConceptChronology createType(UUID parentUuid, String typeName) throws Exception
	{
		ConceptChronology concept = importUtil_.createConcept(typeName, true);
		loadedConcepts.put(concept.getPrimordialUuid(), typeName);
		importUtil_.addParent(ComponentReference.fromConcept(concept), parentUuid);
		return concept;
	}

	/**
	 * Used for debug. Sets up the same paths that maven would use.... allow the code to be run standalone.
	 * @param args 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception
	{
		ICD10ImportMojo icd10Converter = new ICD10ImportMojo();
		icd10Converter.outputDirectory = new File("../../integration/db-config-builder-ui/target/converter-executor/target/");
		icd10Converter.inputFileLocation= new File("../../integration/db-config-builder-ui/target/converter-executor/target/generated-resources/src");
		icd10Converter.sourceType = "CM";
		icd10Converter.converterVersion = "0.1";
		icd10Converter.converterOutputArtifactVersion = "0.2";
		icd10Converter.converterOutputArtifactClassifier = "foo";
		icd10Converter.converterSourceArtifactVersion = "2018";
		icd10Converter.execute();
		Platform.exit();
	}
}
