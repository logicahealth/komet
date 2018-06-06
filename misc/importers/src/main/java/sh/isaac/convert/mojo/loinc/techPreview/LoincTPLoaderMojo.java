/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
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
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTParserUtil;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.convert.mojo.loinc.LOINCReader;
import sh.isaac.convert.mojo.loinc.LoincCsvFileReader;
import sh.isaac.convert.mojo.loinc.techPreview.propertyTypes.PT_Annotations;
import sh.isaac.convert.mojo.loinc.techPreview.propertyTypes.PT_Descriptions;
import sh.isaac.convert.mojo.loinc.techPreview.propertyTypes.PT_Refsets;
import sh.isaac.converters.sharedUtils.ComponentReference;
import sh.isaac.converters.sharedUtils.ConsoleUtil;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyType;
import sh.isaac.converters.sharedUtils.propertyTypes.ValuePropertyPair;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.provider.logic.IsaacVisitor;

/**
 * 
 * Loader code to convert Loinc into the ISAAC datastore.
 * 
 * Paths are typically controlled by maven, however, the main() method has paths configured so that they
 * match what maven does for test purposes.
 */
@Mojo( name = "convert-loinc-tech-preview-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class LoincTPLoaderMojo extends ConverterBaseMojo
{
	private static final String necessarySctid = "900000000000074008";
	private static final String sufficientSctid = "900000000000073002";
	private static final String eol = System.lineSeparator();
	
	/**
	 * we also read a native loinc input file - get that version too
	 */
	@Parameter (required = true, defaultValue = "${loinc-src-data.version}")
	protected String converterSourceLoincArtifactVersion;
	
	@SuppressWarnings("resource")
	@Override
	public void execute() throws MojoExecutionException
	{
		ConsoleUtil.println("LOINC Tech Preview Processing Begins " + new Date().toString());
		super.execute();
	
		ConsoleUtil.println("Processing LOINC");
		LOINCReader loincData = null;
		File tpZipFile = null;
		int expLineNumber = 1;
		
		BufferedWriter loincExpressionDebug = null;
		
		try
		{
			if (!inputFileLocation.isDirectory())
			{
				throw new MojoExecutionException("LoincDataFiles must point to a directory containing the required loinc data files");
			}
			
			for (File f : inputFileLocation.listFiles())
			{
				if (f.getName().toLowerCase().equals("loinc.csv"))
				{
					loincData = new LoincCsvFileReader(f, false);
				}
				if (f.isFile() && f.getName().toLowerCase().endsWith(".zip"))
				{
					if (f.getName().toLowerCase().contains("technologypreview") || f.getName().toLowerCase().contains("snomedct_loinc"))
					{
						if (tpZipFile != null)
						{
							throw new RuntimeException("Found multiple zip files in " + inputFileLocation.getAbsolutePath());
						}
						tpZipFile = f;
					}
					else
					{
						ZipFile zf = new ZipFile(f);
						Enumeration<? extends ZipEntry> zipEntries = zf.entries();
						while(zipEntries.hasMoreElements())
						{
							ZipEntry ze = zipEntries.nextElement();
							//see {@link SupportedConverterTypes}
							if (f.getName().toLowerCase().contains("text"))
							{
								if (ze.getName().toLowerCase().endsWith("loinc.csv"))
								{
									ConsoleUtil.println("Using the data file " + f.getAbsolutePath() + " - " + ze.getName());
									loincData = new LoincCsvFileReader(zf.getInputStream(ze));
									((LoincCsvFileReader)loincData).readReleaseNotes(f.getParentFile(), true);
								}
							}
						}
					}
				}
			}
			if (loincData == null)
			{
				throw new MojoExecutionException("Could not find the loinc data file in " + inputFileLocation.getAbsolutePath());
			}
			if (tpZipFile == null)
			{
				throw new RuntimeException("Couldn't find the tech preview zip file in " + inputFileLocation.getAbsolutePath());
			}
			loincExpressionDebug = new BufferedWriter(new FileWriter(new File(outputDirectory, "ExpressionDebug.log")));
			
			SimpleDateFormat dateReader = new SimpleDateFormat("MMMMMMMMMMMMM yyyy"); //Parse things like "June 2014"
			Date releaseDate = dateReader.parse(loincData.getReleaseDate());
			
			File[] ibdfFiles = new File(inputFileLocation, "ibdf").listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File pathname)
				{
					if (pathname.isFile() && pathname.getName().toLowerCase().endsWith(".ibdf"))
					{
						return true;
					}
					return false;
				}
			});
			
			importUtil = new IBDFCreationUtility(Optional.of("LOINC Solor " + converterSourceArtifactVersion), Optional.of(MetaData.LOINC_MODULES____SOLOR), outputDirectory, 
					converterOutputArtifactId, converterOutputArtifactVersion, converterOutputArtifactClassifier, false, releaseDate.getTime(),  
				Arrays.asList(new VersionType[] {VersionType.DESCRIPTION, VersionType.COMPONENT_NID, VersionType.LOGIC_GRAPH}), false, ibdfFiles);

			ConsoleUtil.println("Loading Metadata");
			
			// Set up a meta-data root concept
			ComponentReference metadata = ComponentReference.fromConcept(importUtil.createConcept("LOINC Tech Preview Metadata" + IBDFCreationUtility.METADATA_SEMANTIC_TAG, true,
					MetaData.SOLOR_CONTENT_METADATA____SOLOR.getPrimordialUuid()));
			
			importUtil.loadTerminologyMetadataAttributes(converterSourceArtifactVersion, 
					Optional.of(loincData.getReleaseDate()), converterOutputArtifactVersion, Optional.ofNullable(converterOutputArtifactClassifier), converterVersion);
			importUtil.addStaticStringAnnotation(metadata, converterSourceLoincArtifactVersion,  MetaData.SOURCE_ARTIFACT_VERSION____SOLOR.getPrimordialUuid(), Status.ACTIVE);
			
			PT_Refsets refsets = new PT_Refsets();
			PT_Annotations annotations = new PT_Annotations(new ArrayList<>());
			PT_Descriptions descTypes = new PT_Descriptions();
			
			importUtil.loadMetaDataItems(Arrays.asList((new PropertyType[] {refsets, annotations, descTypes})), metadata.getPrimordialUuid());
			
			
			//TODO do I need any other attrs right now?

			ConsoleUtil.println("Reading data file into memory.");
			int conCounter = 0;

			HashMap<String, String[]> loincNumToData = new HashMap<>();
			{
				String[] line = loincData.readLine();
				while (line != null)
				{
					if (line.length > 0)
					{
						loincNumToData.put(line[loincData.getFieldMap().get("LOINC_NUM")], line);
					}
					line = loincData.readLine();
					if (loincNumToData.size() % 1000 == 0)
					{
						ConsoleUtil.showProgress();
					}
					if (loincNumToData.size() % 10000 == 0)
					{
						ConsoleUtil.println("Read " + loincNumToData.size() + " lines");
					}
				}
			}
			loincData.close();

			ConsoleUtil.println("Read " + loincNumToData.size()  + " data lines from file");
			
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

			
			ConsoleUtil.println("Processing Expressions / Creating Concepts");
			
			LoincExpressionReader ler = new LoincExpressionReader(tpZipFile);
			String[] expressionLine = ler.readLine();
			while (expressionLine != null)
			{
				try
				{
					if (expressionLine.length > 0)
					{
						String[] loincConceptData = loincNumToData.get(expressionLine[ler.getPositionForColumn("mapTarget")]);
						
						if (loincConceptData == null)
						{
							ConsoleUtil.printErrorln("Skipping line " + expLineNumber + " because I can't find loincNum " + expressionLine[ler.getPositionForColumn("mapTarget")]);
						}
						
						boolean active = expressionLine[ler.getPositionForColumn("active")].equals("1");
						if (!active)
						{
							ConsoleUtil.printErrorln("Skipping line " + expLineNumber + " because it is inactive");
						}
						
						if (active && loincConceptData != null)
						{
							ParseTree parseTree;
							String definitionSctid = expressionLine[ler.getPositionForColumn("definitionStatusId")];
							if (definitionSctid.equals(sufficientSctid))
							{
								parseTree = SNOMEDCTParserUtil.parseExpression(expressionLine[ler.getPositionForColumn("Expression")]);
							}
							else if (definitionSctid.equals(necessarySctid))
							{
								//See <<< black magic from http://ihtsdo.org/fileadmin/user_upload/doc/download/doc_CompositionalGrammarSpecificationAndGuide_Current-en-US_INT_20150708.pdf?ok
								parseTree = SNOMEDCTParserUtil.parseExpression("<<< " + expressionLine[ler.getPositionForColumn("Expression")]);
							}
							else
							{
								throw new RuntimeException("Unexpected definition status: " + definitionSctid + " on line " + expLineNumber);
							}

							LogicalExpressionBuilder defBuilder = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
							IsaacVisitor visitor = new IsaacVisitor(defBuilder);
							visitor.visit(parseTree);
							LogicalExpression expression = defBuilder.build();
							
							UUID expressionId = UUID.fromString(expressionLine[ler.getPositionForColumn("id")]);
							
							loincExpressionDebug.write(expLineNumber + "," + expressionId + "," + expression.toString() + eol);
							
							
							//Build up a concept with the attributes we want, and the expression from the tech preview
							
							String loincNum = loincConceptData[loincData.getPositionForColumn("LOINC_NUM")];
							ComponentReference concept = ComponentReference.fromConcept(importUtil.createConcept(buildUUID(loincNum)));
							conCounter++;
							importUtil.addRelationshipGraph(concept, expressionId, expression, true, null, null);
							importUtil.addAssemblageMembership(concept, PT_Refsets.Refsets.ALL.getProperty().getUUID(), Status.ACTIVE, null);

							//add descriptions
							ArrayList<ValuePropertyPair> descriptions = new ArrayList<>();
							
							for (String property : descTypes.getPropertyNames())
							{
								String data = loincConceptData[loincData.getPositionForColumn(property)];
								if (!StringUtils.isBlank(data))
								{
									descriptions.add(new ValuePropertyPair(data, descTypes.getProperty(property)));
								}
							}
							
							importUtil.addDescriptions(concept, descriptions);
							
							//add attributes
							for (String property : annotations.getPropertyNames())
							{
								String data = loincConceptData[loincData.getPositionForColumn(property)];
								if (!StringUtils.isBlank(data))
								{
									if (annotations.getProperty(property).isIdentifier()) 
									{
										importUtil.addStaticStringAnnotation(concept, data, annotations.getProperty(property).getUUID(), Status.ACTIVE);
									}
									else
									{
										 importUtil.addStringAnnotation(concept, data, annotations.getProperty(property).getUUID(), Status.ACTIVE);
									}
								}
							}
						}
					}
				}
				catch (Exception e)
				{
					getLog().error("Failed with expression line number at " + expLineNumber + " " + e + " skipping line");
				}
				
				expressionLine = ler.readLine();
				expLineNumber++;
			}
			
			loincExpressionDebug.close();

			ConsoleUtil.println("Created " + conCounter + " concepts total");
			

			ConsoleUtil.println("Data Load Summary:");
			for (String s : importUtil.getLoadStats().getSummary())
			{
				ConsoleUtil.println("  " + s);
			}
			
			ConsoleUtil.println("Finished");
		}

		catch (Exception ex)
		{
			throw new MojoExecutionException("Failed with expression line number at " + expLineNumber, ex);
		}
		finally
		{
			try
			{
				if (importUtil != null)
				{
					importUtil.shutdown();
				}
				if (loincData != null)
				{
					loincData.close();
				}
				if (loincExpressionDebug != null)
				{
					loincExpressionDebug.close();
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException("Failure", e);
			}
		}
	}
	
	private UUID buildUUID(String uniqueIdentifier)
	{
		return Get.service(ConverterUUID.class).createNamespaceUUIDFromString(uniqueIdentifier, true);
	}
}
