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
package sh.isaac.convert.mojo.cpt;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import javafx.application.Platform;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.convert.mojo.cpt.TextReader.CPTFileType;
import sh.isaac.convert.mojo.cpt.propertyTypes.PT_Annotations;
import sh.isaac.converters.sharedUtils.ComponentReference;
import sh.isaac.converters.sharedUtils.ConsoleUtil;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility.DescriptionType;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Descriptions;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Refsets;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyType;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;

/**
 * 
 * {@link CPTImportMojo}
 * 
 * Goal which converts CPT data into the workbench jbin format
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Mojo(name = "convert-CPT-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CPTImportMojo extends ConverterBaseMojo
{
	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			super.execute();
			Date date = null;
			try
			{
				date = new SimpleDateFormat("yyyy").parse(converterSourceArtifactVersion);
			}
			catch (Exception e)
			{
				throw new MojoExecutionException("Failed to parse year from " + converterSourceArtifactVersion);
			}

			File zipFile = null;
			for (File f : inputFileLocation.listFiles())
			{
				if (f.getName().toLowerCase().endsWith(".zip"))
				{
					if (zipFile != null)
					{
						throw new MojoExecutionException("Only expected to find one zip file in the folder " + inputFileLocation.getCanonicalPath());
					}
					zipFile = f;
				}
			}

			if (zipFile == null)
			{
				throw new MojoExecutionException("Did not find a zip file in " + inputFileLocation.getCanonicalPath());
			}

			ZipFile zf = new ZipFile(zipFile);

			HashMap<String, CPTData> data = new HashMap<>();

			ConsoleUtil.println("Reading LONGULT.txt");
			int read1 = TextReader.read(zf.getInputStream(zf.getEntry("LONGULT.txt")), data, CPTFileType.LONGULT);
			ConsoleUtil.println("Reading MEDU.txt");
			int read2 = TextReader.read(zf.getInputStream(zf.getEntry("MEDU.txt")), data, CPTFileType.MEDU);
			ConsoleUtil.println("Reading SHORTU.txt");
			int read3 = TextReader.read(zf.getInputStream(zf.getEntry("SHORTU.txt")), data, CPTFileType.SHORTU);

			zf.close();

			if (read1 != read2 || read1 != read3)
			{
				throw new RuntimeException("Didn't find the same number of codes in all 3 files!");
			}
			importUtil = new IBDFCreationUtility(Optional.of("CPT" + " " + converterSourceArtifactVersion), Optional.of(MetaData.CPT_MODULES____SOLOR),
					outputDirectory, converterOutputArtifactId, converterOutputArtifactVersion, converterOutputArtifactClassifier, false, date.getTime());

			PropertyType attributes = new PT_Annotations();
			PropertyType descriptions = new BPT_Descriptions("CPT");
			descriptions.addProperty("LONGULT", "Long Description Upper/Lower Case", null);
			descriptions.addProperty("MEDU", "Medium Description Upper Case", null);
			descriptions.addProperty("SHORTU", "Short Description Upper Case", null);

			BPT_Refsets refsets_ = new BPT_Refsets("CPT");
			refsets_.addProperty("All CPT Concepts");

			// Every time concept created add membership to "All CPT Concepts"
			UUID allCPTConceptsRefset = refsets_.getProperty("All CPT Concepts").getUUID();

			// Parent nuccMetadata ComponentReference
			final ComponentReference cptMetadata = ComponentReference.fromConcept(
					createType(MetaData.SOLOR_CONTENT_METADATA____SOLOR.getPrimordialUuid(), "CPT Metadata" + IBDFCreationUtility.METADATA_SEMANTIC_TAG));

			// loadTerminologyMetadataAttributes onto nuccMetadata
			importUtil.loadTerminologyMetadataAttributes(converterSourceArtifactVersion, Optional.empty(), converterOutputArtifactVersion,
					Optional.ofNullable(converterOutputArtifactClassifier), converterVersion);

			// load metadata
			importUtil.loadMetaDataItems(Arrays.asList(attributes, refsets_, descriptions), cptMetadata.getPrimordialUuid());

			// Create CPT root concept under SOLOR_CONCEPT____SOLOR
			final ConceptVersion cptRootConcept = importUtil.createConcept("CPT", true, MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid());

			ConsoleUtil.println("Metadata load stats");
			for (String line : importUtil.getLoadStats().getSummary())
			{
				ConsoleUtil.println(line);
			}
			importUtil.clearLoadStats();

			String firstThree = "";
			ComponentReference parent = null;
			int cptConCount = 0;
			int groupingConCount = 0;

			List<CPTData> sorted = new ArrayList<>(data.values());
			Collections.sort(sorted, new Comparator<CPTData>()
			{
				@Override
				public int compare(CPTData o1, CPTData o2)
				{
					int left = Integer.parseInt(o1.code.substring(0, 3));
					int right = Integer.parseInt(o2.code.substring(0, 3));
					return Integer.compare(left, right);
				}
			});

			for (CPTData d : sorted)
			{
				String temp = d.code.substring(0, 3);
				if (!temp.equals(firstThree))
				{
					// Make a new grouping concept
					firstThree = d.code.substring(0, 3);
					parent = ComponentReference.fromConcept(importUtil.createConcept(firstThree, true, cptRootConcept.getPrimordialUuid()));
					groupingConCount++;
				}
				cptConCount++;
				ComponentReference concept = ComponentReference.fromConcept(importUtil.createConcept(Get.service(ConverterUUID.class).createNamespaceUUIDFromString(d.code)));

				importUtil.addParent(concept, parent.getPrimordialUuid());
				importUtil.addDescription(concept, d.code, DescriptionType.FULLY_QUALIFIED_NAME, true, null, Status.ACTIVE);
				importUtil.addStaticStringAnnotation(concept, d.code, attributes.getProperty(PT_Annotations.Attribute.Code.name()).getUUID(), Status.ACTIVE);

				importUtil.addAssemblageMembership(concept, allCPTConceptsRefset, Status.ACTIVE, null);

				if (StringUtils.isNotBlank(d.shortu))
				{
					addDescription(concept, d.shortu, DescriptionType.REGULAR_NAME, descriptions.getProperty("SHORTU").getUUID(), true);
				}
				if (StringUtils.isNotBlank(d.longult))
				{
					addDescription(concept, d.longult, DescriptionType.REGULAR_NAME, descriptions.getProperty("LONGULT").getUUID(), false);
				}
				if (StringUtils.isNotBlank(d.medu))
				{
					addDescription(concept, d.medu, DescriptionType.REGULAR_NAME, descriptions.getProperty("MEDU").getUUID(), false);
				}
			}

			ConsoleUtil.println("");
			ConsoleUtil.println("Load Statistics");
			for (String line : importUtil.getLoadStats().getSummary())
			{
				ConsoleUtil.println(line);
			}

			ConsoleUtil.println("Loaded " + cptConCount + " CPT Concepts");
			ConsoleUtil.println("Created " + groupingConCount + " Grouping Concepts");

			// this could be removed from final release. Just added to help debug editor problems.
			ConsoleUtil.println("Dumping UUID Debug File");
			Get.service(ConverterUUID.class).dump(outputDirectory, "cptUuid");

			importUtil.shutdown();
			ConsoleUtil.writeOutputToFile(new File(outputDirectory, "ConsoleOutput.txt").toPath());
		}
		catch (Exception ex)
		{
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}
	}

	private void addDescription(ComponentReference concept, String text, DescriptionType descriptionType, UUID extendedType, boolean preferred)
	{
		UUID descriptionPrimordialUUID = Get.service(ConverterUUID.class).createNamespaceUUIDFromStrings(concept.getPrimordialUuid().toString(), text, extendedType.toString(),
				descriptionType.name(), new Boolean(preferred).toString());
		importUtil.addDescription(concept, descriptionPrimordialUUID, text, descriptionType, preferred, extendedType, Status.ACTIVE);
	}

	private ConceptVersion createType(UUID parentUuid, String typeName) throws Exception
	{
		ConceptVersion concept = importUtil.createConcept(typeName, true);
		importUtil.addParent(ComponentReference.fromConcept(concept), parentUuid);
		return concept;
	}

	public static void main(String[] args) throws MojoExecutionException
	{
		CPTImportMojo i = new CPTImportMojo();
		i.outputDirectory = new File("../../integration/db-config-builder-ui/target/converter-executor/target/");
		i.inputFileLocation= new File("../../integration/db-config-builder-ui/target/converter-executor/target/generated-resources/src");
		i.converterOutputArtifactVersion = "2016.01.07.foo";
		i.converterVersion = "SNAPSHOT";
		i.converterSourceArtifactVersion = "2017";
		i.execute();
		Platform.exit();
	}
}