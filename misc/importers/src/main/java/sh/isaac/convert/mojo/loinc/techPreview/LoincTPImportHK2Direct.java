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
package sh.isaac.convert.mojo.loinc.techPreview;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTParserUtil;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.convert.directUtils.DirectConverter;
import sh.isaac.convert.directUtils.DirectConverterBaseMojo;
import sh.isaac.convert.directUtils.DirectWriteHelper;
import sh.isaac.convert.mojo.loinc.LOINCReader;
import sh.isaac.convert.mojo.loinc.LoincCsvFileReader;
import sh.isaac.convert.mojo.loinc.TxtFileReader;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;

/**
 * {@link LoincTPImportHK2Direct}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@PerLookup
@Service
public class LoincTPImportHK2Direct extends DirectConverterBaseMojo implements DirectConverter
{
	private static final String necessarySctid = "900000000000074008";
	private static final String sufficientSctid = "900000000000073002";
	private static final String eol = System.lineSeparator();
	
	private HashMap<UUID, ConceptSpecification> extendedDescriptionTypes = new HashMap<>(); 
	private HashSet<UUID> preferredDescriptionTypes = new HashSet<>();
	
	/**
	 * we also read a native loinc input file - get that version too
	 */
	@Parameter (required = true, defaultValue = "${loinc-src-data.version}")
	protected String converterSourceLoincArtifactVersion;

	/**
	 * This constructor is for HK2 and should not be used at runtime.  You should 
	 * get your reference of this class from HK2, and then call the {@link DirectConverter#configure(File, Path, String, StampFilter, Transaction)} method on it.
	 */
	protected LoincTPImportHK2Direct() 
	{
		//For HK2 and maven
		super();
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

	@Override
	public SupportedConverterTypes[] getSupportedTypes()
	{
		return new SupportedConverterTypes[] { SupportedConverterTypes.LOINC_TECH_PREVIEW};
	}

	@Override
	protected Path[] getIBDFFilesToPreload() throws IOException
	{
		//There currently is no mechanism to automatically pre-load a required IBDF file if you are running the 
		//converter live.
		final AtomicReference<Path> ibdfFile = new AtomicReference<>();
		Files.walk(inputFileLocationPath.resolve("ibdf"), new FileVisitOption[] {}).forEach(path ->
		{
			if (path.toString().toLowerCase().endsWith(".ibdf"))
			{
				if (ibdfFile.get() != null)
				{
					throw new RuntimeException("Only expected to find one ibdf file in the folder " + inputFileLocationPath.resolve("ibdf").normalize());
				}
				ibdfFile.set(path);
			}
		});

		if (ibdfFile.get() == null)
		{
			throw new IOException("Failed to locate the ibdf file in " + inputFileLocationPath.resolve("ibdf"));
		}
		return new Path[] {ibdfFile.get()};
	}

	@Override
	protected Collection<VersionType> getIBDFSkipTypes()
	{
		//skip descriptions, acceptabilities
		return  Arrays.asList(new VersionType[] {VersionType.DESCRIPTION, VersionType.COMPONENT_NID});
	}

	/**
	 * @see sh.isaac.convert.directUtils.DirectConverterBaseMojo#convertContent(Consumer, BiConsumer)
	 * @see DirectConverter#convertContent(Consumer, BiConsumer)
	 */
	@Override
	public void convertContent(Consumer<String> statusUpdates, BiConsumer<Double, Double> progressUpdate) throws IOException
	{
		log.info("LOINC Tech Preview Processing Begins " + new Date().toString());
		AtomicReference<LOINCReader> loincData = new AtomicReference<>();
		AtomicReference<LoincExpressionReader> ler = new AtomicReference<LoincExpressionReader>();
		int expLineNumber = 1;
		
		BufferedWriter loincExpressionDebug = null;
		
		try
		{
			Files.walk(inputFileLocationPath, new FileVisitOption[] {}).forEach(path -> 
			{
				try
				{
					if (path.toString().toLowerCase().equals("loincdb.txt"))
					{
						loincData.set(new TxtFileReader(path));
					}
					else if (path.toString().toLowerCase().equals("loinc.csv"))
					{
						loincData.set(new LoincCsvFileReader(path, false));
					}
					else if (path.toString().toLowerCase().endsWith(".zip"))
					{
						// New zip file set
						try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(path, StandardOpenOption.READ)))
						{
							ZipEntry ze = zis.getNextEntry();
	
							while (ze != null)
							{
								if (path.toString().toLowerCase().contains("technologypreview") || path.toString().toLowerCase().contains("snomedct_loinc"))
								{
									if (ler.get() != null)
									{
										throw new RuntimeException("Found multiple zip files in " + inputFileLocationPath);
									}
									ler.set(new LoincExpressionReader(zis));
								}
								
								else if (path.toString().toLowerCase().contains("text"))
								{
									if (ze.getName().toLowerCase().endsWith("loinc.csv"))
									{
										log.info("Using the data file " + path + " - " + ze.getName());
										loincData.set(new LoincCsvFileReader(zis));
										((LoincCsvFileReader) loincData.get()).readReleaseNotes(path.getParent(), false);
									}
									
								}
							}
						}
					}
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			});

			if (loincData.get() == null)
			{
				throw new MojoExecutionException("Could not find the loinc data file in " + this.inputFileLocationPath);
			}
			
	
			loincExpressionDebug = new BufferedWriter(new FileWriter(new File(outputDirectory, "ExpressionDebug.log"), Charset.forName(StandardCharsets.UTF_8.name())));
			
			SimpleDateFormat dateReader = new SimpleDateFormat("MMMMMMMMMMMMM yyyy"); //Parse things like "June 2014"
			Date releaseDate = dateReader.parse(loincData.get().getReleaseDate());
			
			log.info("Setting up metadata");
			//Right now, we are configured for the LOINC grouping modules nid
			dwh = new DirectWriteHelper(transaction, TermAux.USER.getNid(), MetaData.LOINC_MODULES____SOLOR.getNid(), MetaData.DEVELOPMENT_PATH____SOLOR.getNid(),
					converterUUID, "LOINC Tech Preview", false);

			setupModule("LOINC Tech Preview", MetaData.LOINC_MODULES____SOLOR.getPrimordialUuid(), Optional.empty(), releaseDate.getTime());

			//Set up our metadata hierarchy
			dwh.makeMetadataHierarchy(true, true, false, false, true, false, releaseDate.getTime());


			dwh.makeBrittleStringAnnotation(MetaData.SOURCE_ARTIFACT_VERSION____SOLOR.getPrimordialUuid(), dwh.getMetadataRoot(), converterSourceLoincArtifactVersion, 
					releaseDate.getTime());
			
			// Every time concept created add membership to "All CPT Concepts"
			UUID allConceptsRefset = dwh.makeRefsetTypeConcept(null, "All LOINC Tech Preview Concepts", null, null, releaseDate.getTime());
			
			extendedDescriptionTypes.put(dwh.makeDescriptionTypeConcept(null, "CONSUMER_NAME", "Consumer Name", null, null, null, releaseDate.getTime()),
					MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR);
			extendedDescriptionTypes.put(dwh.makeDescriptionTypeConcept(null, "SHORTNAME", "Short Name", null, null, null, releaseDate.getTime()),
					MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR);
			extendedDescriptionTypes.put(dwh.makeDescriptionTypeConcept(null, "LONG_COMMON_NAME", "Long Common Name", null, null, null, releaseDate.getTime()),
					MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR);
			
			preferredDescriptionTypes.add(dwh.getDescriptionType("LONG_COMMON_NAME"));
			preferredDescriptionTypes.add(dwh.getDescriptionType("SHORT_NAME"));
			
			if (loincData.get().getFormatVersionNumber() >=6 ){
				extendedDescriptionTypes.put(dwh.makeDescriptionTypeConcept(null, "DefinitionDescription", "Definition Description", null, null, null,
						releaseDate.getTime()), MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR);
				preferredDescriptionTypes.add(dwh.getDescriptionType("DefinitionDescription"));
			}
			
			dwh.makeAttributeTypeConcept(null, "FORMULA", "Formula", null, false, DynamicDataType.STRING, null, releaseDate.getTime());
			dwh.makeAttributeTypeConcept(null, "EXMPL_ANSWERS", "Example Answers", null, false, DynamicDataType.STRING, null, releaseDate.getTime());
			dwh.makeAttributeTypeConcept(null, "RELATEDNAMES2", "Related Names 2", null, false, DynamicDataType.STRING, null, releaseDate.getTime());
			dwh.makeAttributeTypeConcept(null, "LOINC_NUM", "LOINC Number", null, "Carries the LOINC_NUM native identifier", false, DynamicDataType.STRING,
					null, releaseDate.getTime());
			
			log.info("Metadata load stats");
			for (String line : dwh.getLoadStats().getSummary())
			{
				log.info(line);
			}

			dwh.clearLoadStats();

			statusUpdates.accept("Loading content");

			//TODO do I need any other attrs right now?
			log.info("Reading data file into memory.");
			int conCounter = 0;

			HashMap<String, String[]> loincNumToData = new HashMap<>();
			{
				String[] line = loincData.get().readLine();
				while (line != null)
				{
					if (line.length > 0)
					{
						loincNumToData.put(line[loincData.get().getFieldMap().get("LOINC_NUM")], line);
					}
					line = loincData.get().readLine();
					if (loincNumToData.size() % 1000 == 0)
					{
						showProgress();
					}
					if (loincNumToData.size() % 10000 == 0)
					{
						advanceProgressLine();
						log.info("Read " + loincNumToData.size() + " lines");
					}
				}
			}

			advanceProgressLine();
			log.info("Read " + loincNumToData.size()  + " data lines from file");
			
			/*
			 * Columns in this data file are:
			 * id - A UUID for this row
			 * effectiveTime
			 * active - 1 for active
			 * moduleId
			 * refsetId
			 * referencedComponentId
			 * mapTarget - LOINC_NUM
			 * Expression - the goods
			 * definitionStatusId
			 * correlationId
			 * contentOriginId
			 */
			
			loincExpressionDebug.write("line number,expression id,converted expression" + eol);

			
			log.info("Processing Expressions / Creating Concepts");
			
			String[] expressionLine = ler.get().readLine();
			while (expressionLine != null)
			{
				try
				{
					if (expressionLine.length > 0)
					{
						String[] loincConceptData = loincNumToData.get(expressionLine[ler.get().getPositionForColumn("mapTarget")]);
						
						if (loincConceptData == null)
						{
							log.error("Skipping line " + expLineNumber + " because I can't find loincNum " + expressionLine[ler.get().getPositionForColumn("mapTarget")]);
						}
						
						boolean active = expressionLine[ler.get().getPositionForColumn("active")].equals("1");
						if (!active)
						{
							log.error("Skipping line " + expLineNumber + " because it is inactive");
						}
						
						if (active && loincConceptData != null)
						{
							ParseTree parseTree;
							String definitionSctid = expressionLine[ler.get().getPositionForColumn("definitionStatusId")];
							if (definitionSctid.equals(sufficientSctid))
							{
								parseTree = SNOMEDCTParserUtil.parseExpression(expressionLine[ler.get().getPositionForColumn("Expression")]);
							}
							else if (definitionSctid.equals(necessarySctid))
							{
								//See <<< black magic from http://ihtsdo.org/fileadmin/user_upload/doc/download/doc_CompositionalGrammarSpecificationAndGuide_Current-en-US_INT_20150708.pdf?ok
								parseTree = SNOMEDCTParserUtil.parseExpression("<<< " + expressionLine[ler.get().getPositionForColumn("Expression")]);
							}
							else
							{
								throw new RuntimeException("Unexpected definition status: " + definitionSctid + " on line " + expLineNumber);
							}

							LogicalExpressionBuilder defBuilder = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
							IsaacVisitor visitor = new IsaacVisitor(defBuilder);
							visitor.visit(parseTree);
							LogicalExpression expression = defBuilder.build();
							
							UUID expressionId = UUID.fromString(expressionLine[ler.get().getPositionForColumn("id")]);
							
							loincExpressionDebug.write(expLineNumber + "," + expressionId + "," + expression.toString() + eol);
							
							
							//Build up a concept with the attributes we want, and the expression from the tech preview
							
							String loincNum = loincConceptData[loincData.get().getPositionForColumn("LOINC_NUM")];
							UUID concept = dwh.makeConcept(buildUUID(loincNum), Status.ACTIVE, releaseDate.getTime());
							conCounter++;
							dwh.makeGraph(concept, expressionId, expression, Status.ACTIVE, releaseDate.getTime());
							dwh.makeDynamicRefsetMember(allConceptsRefset, concept, releaseDate.getTime());

							for (String dt : dwh.getDescriptionTypes())
							{
								String data = loincConceptData[loincData.get().getPositionForColumn(dt)];
								UUID extendedType = dwh.getDescriptionType(dt);
								UUID description = dwh.makeDescriptionEn(concept, data, extendedDescriptionTypes.get(extendedType).getPrimordialUuid(), 
										MetaData.DESCRIPTION_NOT_CASE_SENSITIVE____SOLOR.getPrimordialUuid(), Status.ACTIVE, releaseDate.getTime(), 
										preferredDescriptionTypes.contains(extendedType) ? MetaData.PREFERRED____SOLOR.getPrimordialUuid() : 
											MetaData.ACCEPTABLE____SOLOR.getPrimordialUuid());
								dwh.makeExtendedDescriptionTypeAnnotation(description, extendedType, releaseDate.getTime());
							}
							
							for (String at : dwh.getAttributeTypes())
							{
								String data = loincConceptData[loincData.get().getPositionForColumn(at)];
								UUID attributeType = dwh.getAttributeType(at);
								if (!StringUtils.isBlank(data))
								{
									dwh.makeStringAnnotation(attributeType, concept, data, releaseDate.getTime());
								}
							}
						}
					}
				}
				catch (Exception e)
				{
					getLog().error("Failed with expression line number at " + expLineNumber + " " + e + " skipping line");
				}
				
				expressionLine = ler.get().readLine();
				expLineNumber++;
			}
			
			loincExpressionDebug.close();

			dwh.processTaxonomyUpdates();
			dwh.clearIsaacCaches();
			log.info("Created " + conCounter + " concepts total");
			
			log.info("Data Load Summary:");

			log.info("Load Statistics");
			for (String line : dwh.getLoadStats().getSummary())
			{
				log.info(line);
			}

			// this could be removed from final release. Just added to help debug editor problems.
			if (outputDirectory != null)
			{
				log.info("Dumping UUID Debug File");
				converterUUID.dump(outputDirectory, "loincUuid");
			}
			converterUUID.clearCache();
			
			log.info("Finished");
		}

		catch (Exception ex)
		{
			throw new IOException("Failed with expression line number at " + expLineNumber, ex);
		}
		finally
		{
			if (loincExpressionDebug != null)
			{
				loincExpressionDebug.close();
			}
		}
	}
	
	private UUID buildUUID(String uniqueIdentifier)
	{
		return converterUUID.createNamespaceUUIDFromString(uniqueIdentifier, true);
	}
}