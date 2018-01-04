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



package sh.isaac.convert.loinc.techPreview;

import java.io.BufferedWriter;
import java.io.File;
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

import se.liu.imt.mi.snomedct.expression.tools.ExpressionSyntaxError;
import se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTParserUtil;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.convert.loinc.LOINCReader;
import sh.isaac.convert.loinc.LoincCsvFileReader;
import sh.isaac.convert.loinc.techPreview.propertyTypes.PT_Annotations;
import sh.isaac.convert.loinc.techPreview.propertyTypes.PT_Descriptions;
import sh.isaac.convert.loinc.techPreview.propertyTypes.PT_Refsets;
import sh.isaac.converters.sharedUtils.ComponentReference;
import sh.isaac.converters.sharedUtils.ConsoleUtil;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyType;
import sh.isaac.converters.sharedUtils.propertyTypes.ValuePropertyPair;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.provider.logic.ISAACVisitor;

/**
 *
 * Loader code to convert Loinc into the ISAAC datastore.
 *
 * Paths are typically controlled by maven, however, the main() method has paths configured so that they match what
 * maven does for test purposes.
 */
@Mojo(
    name         = "convert-loinc-tech-preview-to-ibdf",
    defaultPhase = LifecyclePhase.PROCESS_SOURCES
)
public class LoincTPLoaderMojo extends ConverterBaseMojo {

    /**
     * The Constant NECESSARY_SCTID.
     */
    private static final String NECESSARY_SCTID = "900000000000074008";

    /**
     * The Constant SUFFICIENT_SCTID.
     */
    private static final String SUFFICIENT_SCTID = "900000000000073002";

    /**
     * we also read a native loinc input file - get that version too.
     */
    @Parameter(
        required     = true,
        defaultValue = "${loinc-src-data.version}"
    )
    protected String converterSourceLoincArtifactVersion;

    /**
     * Builds the UUID.
     *
     * @param uniqueIdentifier the unique identifier
     * @return the uuid
     */
    private UUID buildUUID(String uniqueIdentifier) {
        return ConverterUUID.createNamespaceUUIDFromString(uniqueIdentifier, true);
    }

    /**
     * Execute.
     *
     * @throws MojoExecutionException the mojo execution exception
     */
    @SuppressWarnings("resource")
    @Override
    public void execute() throws MojoExecutionException {
        ConsoleUtil.println("LOINC Tech Preview Processing Begins " + new Date().toString());
        super.execute();
        ConsoleUtil.println("Processing LOINC");

        LOINCReader    loincData            = null;
        File           tpZipFile            = null;
        int            expLineNumber        = 1;
        BufferedWriter loincExpressionDebug = null;

        try {
            if (!this.inputFileLocation.isDirectory()) {
                throw new MojoExecutionException(
                    "LoincDataFiles must point to a directory containing the required loinc data files");
            }

            for (final File f : this.inputFileLocation.listFiles()) {
                if (f.getName().toLowerCase().equals("loinc.csv")) {
                    loincData = new LoincCsvFileReader(f, false);
                }

                if (f.isFile() && f.getName().toLowerCase().endsWith(".zip")) {
                    if (f.getName().toLowerCase().contains("technologypreview")) {
                        if (tpZipFile != null) {
                            throw new RuntimeException("Found multiple zip files in "
                                                       + this.inputFileLocation.getAbsolutePath());
                        }

                        tpZipFile = f;
                    } else {
                        final ZipFile                         zf         = new ZipFile(f);
                        final Enumeration<? extends ZipEntry> zipEntries = zf.entries();

                        while (zipEntries.hasMoreElements()) {
                            final ZipEntry ze = zipEntries.nextElement();

                            // see {@link SupportedConverterTypes}
                            if (f.getName().toLowerCase().contains("text")) {
                                if (ze.getName().toLowerCase().endsWith("loinc.csv")) {
                                    ConsoleUtil.println("Using the data file " + f.getAbsolutePath() + " - "
                                                        + ze.getName());
                                    loincData = new LoincCsvFileReader(zf.getInputStream(ze));
                                    ((LoincCsvFileReader) loincData).readReleaseNotes(f.getParentFile(), true);
                                }
                            }
                        }
                    }
                }
            }

            if (loincData == null) {
                throw new MojoExecutionException("Could not find the loinc data file in "
                                                 + this.inputFileLocation.getAbsolutePath());
            }

            if (tpZipFile == null) {
                throw new RuntimeException("Couldn't find the tech preview zip file in "
                                           + this.inputFileLocation.getAbsolutePath());
            }

            loincExpressionDebug = new BufferedWriter(new FileWriter(new File(this.outputDirectory,
                                                                              "ExpressionDebug.log")));

            final SimpleDateFormat dateReader = new SimpleDateFormat("MMMMMMMMMMMMM yyyy");//Parse things like "June 2014"
            final Date releaseDate = dateReader.parse(loincData.getReleaseDate());

            this.importUtil = new IBDFCreationUtility(Optional.empty(),
                                                      Optional.of(MetaData.SOLOR_MODULE____SOLOR),
                                                      this.outputDirectory,
                                                      this.converterOutputArtifactId,
                                                      this.converterOutputArtifactClassifier,
                                                      this.converterOutputArtifactVersion,
                                                      false,
                                                      releaseDate.getTime());
            ConsoleUtil.println("Loading Metadata");

            // Set up a meta-data root concept
            final ComponentReference metadata = ComponentReference.fromConcept(
                                                    this.importUtil.createConcept(
                                                        "LOINC Tech Preview Metadata"
                                                        + IBDFCreationUtility.METADATA_SEMANTIC_TAG,
                                                        true,
                                                        MetaData.SOLOR_CONTENT_METADATA____SOLOR.getPrimordialUuid()));

            this.importUtil.loadTerminologyMetadataAttributes(metadata,
                                                              this.converterSourceArtifactVersion,
                                                              Optional.of(loincData.getReleaseDate()),
                                                              this.converterOutputArtifactVersion,
                                                              Optional.ofNullable(
                                                                  this.converterOutputArtifactClassifier),
                                                              this.converterVersion);
            this.importUtil.addStaticStringAnnotation(metadata,
                                                      this.converterSourceLoincArtifactVersion,
                                                      MetaData.SOURCE_ARTIFACT_VERSION____SOLOR.getPrimordialUuid(),
                                                      Status.ACTIVE);

            final PT_Refsets      refsets     = new PT_Refsets();
            final PT_Annotations  annotations = new PT_Annotations(new ArrayList<>());
            final PT_Descriptions descTypes   = new PT_Descriptions();

            this.importUtil.loadMetaDataItems(Arrays.asList((new PropertyType[] { refsets, annotations, descTypes })),
                                              metadata.getPrimordialUuid());

            // TODO do I need any other attrs right now?
            ConsoleUtil.println("Reading data file into memory.");

            int                             conCounter     = 0;
            final HashMap<String, String[]> loincNumToData = new HashMap<>();
            String[]                        line           = loincData.readLine();

            while (line != null) {
                if (line.length > 0) {
                    loincNumToData.put(line[loincData.getFieldMap().get("LOINC_NUM")], line);
                }

                line = loincData.readLine();

                if (loincNumToData.size() % 1000 == 0) {
                    ConsoleUtil.showProgress();
                }

                if (loincNumToData.size() % 10000 == 0) {
                    ConsoleUtil.println("Read " + loincNumToData.size() + " lines");
                }
            }

            loincData.close();
            ConsoleUtil.println("Read " + loincNumToData.size() + " data lines from file");

            /*
             *    Columns in this data file are:
             *    id - A UUID for this row
             *    effectiveTime
             *    active - 1 for active
             *    moduleId
             *    refsetId
             *    referencedComponentId
             *    mapTarget - LOINC_NUM
             *    Expression - the goods
             *    definitionStatusId
             *    correlationId
             *    contentOriginId
             */
            loincExpressionDebug.write("line number,expression id,converted expression\n");
            ConsoleUtil.println("Processing Expressions / Creating Concepts");

            final LoincExpressionReader ler            = new LoincExpressionReader(tpZipFile);
            String[]                    expressionLine = ler.readLine();

            while (expressionLine != null) {
                try {
                    if (expressionLine.length > 0) {
                        final String[] loincConceptData =
                            loincNumToData.get(expressionLine[ler.getPositionForColumn("mapTarget")]);

                        if (loincConceptData == null) {
                            ConsoleUtil.printErrorln("Skipping line " + expLineNumber
                                                     + " because I can't find loincNum "
                                                     + expressionLine[ler.getPositionForColumn("mapTarget")]);
                        }

                        final boolean active = expressionLine[ler.getPositionForColumn("active")].equals("1");

                        if (!active) {
                            ConsoleUtil.printErrorln("Skipping line " + expLineNumber + " because it is inactive");
                        }

                        if (active && (loincConceptData != null)) {
                            ParseTree    parseTree;
                            final String definitionSctid =
                                expressionLine[ler.getPositionForColumn("definitionStatusId")];

                            switch (definitionSctid) {
                            case SUFFICIENT_SCTID :
                                parseTree = SNOMEDCTParserUtil.parseExpression(
                                    expressionLine[ler.getPositionForColumn("Expression")]);

                                break;

                            case NECESSARY_SCTID :

                                // See <<< black magic from http://ihtsdo.org/fileadmin/user_upload/doc/download/doc_CompositionalGrammarSpecificationAndGuide_Current-en-US_INT_20150708.pdf?ok
                                parseTree = SNOMEDCTParserUtil.parseExpression(
                                    "<<< " + expressionLine[ler.getPositionForColumn("Expression")]);

                                break;

                            default :
                                throw new RuntimeException("Unexpected definition status: " + definitionSctid
                                                           + " on line " + expLineNumber);
                            }

                            final LogicalExpressionBuilder defBuilder = Get.logicalExpressionBuilderService()
                                                                           .getLogicalExpressionBuilder();
                            final ISAACVisitor visitor = new ISAACVisitor(defBuilder);

                            visitor.visit(parseTree);

                            final LogicalExpression expression   = defBuilder.build();
                            final UUID              expressionId =
                                UUID.fromString(expressionLine[ler.getPositionForColumn("id")]);

                            loincExpressionDebug.write(expLineNumber + "," + expressionId + "," + expression.toString()
                                                       + "\n");

                            // Build up a concept with the attributes we want, and the expression from the tech preview
                            final String             loincNum =
                                loincConceptData[loincData.getPositionForColumn("LOINC_NUM")];
                            final ComponentReference concept  =
                                ComponentReference.fromConcept(this.importUtil.createConcept(buildUUID(loincNum)));

                            conCounter++;
                            this.importUtil.addRelationshipGraph(concept, expressionId, expression, true, null, null);
                            this.importUtil.addAssemblageMembership(concept,
                                                                PT_Refsets.Refsets.ALL.getProperty().getUUID(),
                                                                Status.ACTIVE,
                                                                null);

                            // add descriptions
                            final ArrayList<ValuePropertyPair> descriptions = new ArrayList<>();

                            for (final String property : descTypes.getPropertyNames()) {
                                final String data = loincConceptData[loincData.getPositionForColumn(property)];

                                if (!StringUtils.isBlank(data)) {
                                    descriptions.add(new ValuePropertyPair(data, descTypes.getProperty(property)));
                                }
                            }

                            this.importUtil.addDescriptions(concept, descriptions);

                            // add attributes
                            for (final String property : annotations.getPropertyNames()) {
                                final String data = loincConceptData[loincData.getPositionForColumn(property)];

                                if (!StringUtils.isBlank(data)) {
                                    this.importUtil.addStringAnnotation(concept,
                                                                        data,
                                                                        annotations.getProperty(property).getUUID(),
                                                                        Status.ACTIVE);
                                }
                            }
                        }
                    }
                } catch (final IOException | RuntimeException | ExpressionSyntaxError e) {
                    getLog().error("Failed with expression line number at " + expLineNumber + " " + e
                                   + " skipping line");
                }

                expressionLine = ler.readLine();
                expLineNumber++;
            }

            loincExpressionDebug.close();
            ConsoleUtil.println("Created " + conCounter + " concepts total");
            ConsoleUtil.println("Data Load Summary:");

            for (final String s : this.importUtil.getLoadStats().getSummary()) {
                ConsoleUtil.println("  " + s);
            }

            ConsoleUtil.println("Finished");
        } catch (final Exception ex) {
            throw new MojoExecutionException("Failed with expression line number at " + expLineNumber, ex);
        } finally {
            try {
                if (this.importUtil != null) {
                    this.importUtil.shutdown();
                }

                if (loincData != null) {
                    loincData.close();
                }

                if (loincExpressionDebug != null) {
                    loincExpressionDebug.close();
                }
            } catch (final IOException e) {
                throw new RuntimeException("Failure", e);
            }
        }
    }
}
