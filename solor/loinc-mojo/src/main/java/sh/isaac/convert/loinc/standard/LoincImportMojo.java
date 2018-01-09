/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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



package sh.isaac.convert.loinc.standard;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.assertions.ConceptAssertion;
import sh.isaac.convert.loinc.LOINCReader;
import sh.isaac.convert.loinc.LoincCsvFileReader;
import sh.isaac.convert.loinc.NameMap;
import sh.isaac.convert.loinc.TxtFileReader;
import sh.isaac.convert.loinc.standard.propertyTypes.PT_Annotations;
import sh.isaac.convert.loinc.standard.propertyTypes.PT_Associations;
import sh.isaac.convert.loinc.standard.propertyTypes.PT_Descriptions;
import sh.isaac.convert.loinc.standard.propertyTypes.PT_Refsets;
import sh.isaac.convert.loinc.standard.propertyTypes.PT_Relations;
import sh.isaac.convert.loinc.standard.propertyTypes.PT_SkipAxis;
import sh.isaac.convert.loinc.standard.propertyTypes.PT_SkipClass;
import sh.isaac.convert.loinc.standard.propertyTypes.PT_SkipOther;
import sh.isaac.converters.sharedUtils.ComponentReference;
import sh.isaac.converters.sharedUtils.ConsoleUtil;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility.DescriptionType;
import sh.isaac.converters.sharedUtils.propertyTypes.Property;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyType;
import sh.isaac.converters.sharedUtils.propertyTypes.ValuePropertyPair;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;

//~--- classes ----------------------------------------------------------------

/**
 *
 * Loader code to convert Loinc into isaac.
 *
 * Paths are typically controlled by maven, however, the main() method has paths configured so that they match what maven does for test
 * purposes.
 */
@Mojo(name = "convert-loinc-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class LoincImportMojo extends ConverterBaseMojo {
   /** The property types. */
   private final ArrayList<PropertyType> propertyTypes_ = new ArrayList<>();

   /** The map to data. */
   private final HashMap<String, HashMap<String, String>> mapToData = new HashMap<>();

   /** The property to property type. */

   // Various caches for performance reasons
   private final Hashtable<String, PropertyType> propertyToPropertyType_ = new Hashtable<>();

   /** The sdf. */
   private final SimpleDateFormat sdf_ = new SimpleDateFormat("yyyyMMdd");

   /** The concepts. */
   Hashtable<UUID, ComponentReference> concepts_ = new Hashtable<>();

   /** The skipped deleted items. */
   private int skippedDeletedItems = 0;

   /** The multiaxial paths to root. */
   private final HashMap<UUID, HashSet<UUID>> multiaxialPathsToRoot = new HashMap<>();

   /** The pt skip axis. */

   // Need a handle to these
   private PropertyType pt_SkipAxis;

   /** The pt skip class. */
   private PropertyType pt_SkipClass;

   /** The field map. */
   protected Hashtable<String, Integer> fieldMap;

   /** The field map inverse. */
   protected Hashtable<Integer, String> fieldMapInverse;

   /** The class mapping. */
   private NameMap classMapping;

   /** The version time map. */
   private TreeMap<String, Long> versionTimeMap;

   // ~--- methods -------------------------------------------------------------

   /**
    * Execute.
    *
    * @throws MojoExecutionException the mojo execution exception
    */
   @Override
   public void execute() throws MojoExecutionException {
      ConsoleUtil.println("LOINC Processing Begins " + new Date().toString());

      LOINCReader loincData = null;
      LOINCReader mapTo = null;
      LOINCReader sourceOrg = null;
      LOINCReader loincMultiData = null;

      try {
         super.execute();

         if (!this.inputFileLocation.isDirectory()) {
            throw new MojoExecutionException("LoincDataFiles must point to a directory containing the required loinc data files");
         }

         for (final File f : this.inputFileLocation.listFiles()) {
            if (f.getName().toLowerCase().equals("loincdb.txt")) {
               loincData = new TxtFileReader(f);
            } else if (f.getName().toLowerCase().equals("loinc.csv")) {
               loincData = new LoincCsvFileReader(f, true);
               this.versionTimeMap = ((LoincCsvFileReader) loincData).getTimeVersionMap();
            } else if (f.getName().toLowerCase().equals("map_to.csv")) {
               mapTo = new LoincCsvFileReader(f, false);
            } else if (f.getName().toLowerCase().equals("source_organization.csv")) {
               sourceOrg = new LoincCsvFileReader(f, false);
            } else if (f.getName().toLowerCase().endsWith("multi-axial_hierarchy.csv")) {
               loincMultiData = new LoincCsvFileReader(f, false);
            } else if (f.getName().toLowerCase().endsWith(".zip")) {
               // New zip file set
               @SuppressWarnings("resource")
               final ZipFile zf = new ZipFile(f);
               final Enumeration<? extends ZipEntry> zipEntries = zf.entries();

               while (zipEntries.hasMoreElements()) {
                  final ZipEntry ze = zipEntries.nextElement();

                  // see {@link SupportedConverterTypes}
                  if (f.getName().toLowerCase().contains("text")) {
                     if (ze.getName().toLowerCase().endsWith("loinc.csv")) {
                        ConsoleUtil.println("Using the data file " + f.getAbsolutePath() + " - " + ze.getName());
                        loincData = new LoincCsvFileReader(zf.getInputStream(ze));
                        ((LoincCsvFileReader) loincData).readReleaseNotes(f.getParentFile(), true);
                        this.versionTimeMap = ((LoincCsvFileReader) loincData).getTimeVersionMap();
                     } else if (ze.getName().toLowerCase().endsWith("map_to.csv")) {
                        ConsoleUtil.println("Using the data file " + f.getAbsolutePath() + " - " + ze.getName());
                        mapTo = new LoincCsvFileReader(zf.getInputStream(ze));
                     } else if (ze.getName().toLowerCase().endsWith("source_organization.csv")) {
                        ConsoleUtil.println("Using the data file " + f.getAbsolutePath() + " - " + ze.getName());
                        sourceOrg = new LoincCsvFileReader(zf.getInputStream(ze));
                     }
                  } else if (f.getName().toLowerCase().contains("multi-axial_hierarchy")) {
                     if (ze.getName().toLowerCase().contains("multi-axial") && ze.getName().toLowerCase().endsWith(".csv")) {
                        ConsoleUtil.println("Using the data file " + f.getAbsolutePath() + " - " + ze.getName());
                        loincMultiData = new LoincCsvFileReader(zf.getInputStream(ze));
                     }
                  }
               }
            }
         }

         if (loincData == null) {
            throw new MojoExecutionException("Could not find the loinc data file in " + this.inputFileLocation.getAbsolutePath());
         }

         if (loincMultiData == null) {
            throw new MojoExecutionException("Could not find the multi-axial file in " + this.inputFileLocation.getAbsolutePath());
         }

         final SimpleDateFormat dateReader = new SimpleDateFormat("MMMMMMMMMMMMM yyyy"); // Parse things like "June 2014"
         final Date releaseDate = dateReader.parse(loincData.getReleaseDate());

         this.importUtil = new IBDFCreationUtility(Optional.of("LOINC " + converterSourceArtifactVersion), Optional.of(MetaData.LOINC_MODULES____SOLOR), outputDirectory,
               converterOutputArtifactId, converterOutputArtifactVersion, converterOutputArtifactClassifier, false, releaseDate.getTime());
         this.pt_SkipAxis = new PT_SkipAxis();
         this.pt_SkipClass = new PT_SkipClass();

         final String version = loincData.getVersion();

         this.fieldMap = loincData.getFieldMap();
         this.fieldMapInverse = loincData.getFieldMapInverse();

         String mapFileName = null;

         if (version.contains("2.36")) {
            PropertyType.setSourceVersion(1);
            mapFileName = "classMappings-2.36.txt";
         } else if (version.contains("2.38")) {
            PropertyType.setSourceVersion(2);
            mapFileName = "classMappings-2.36.txt"; // Yes, wrong one, never made the file for 2.38
         } else if (version.contains("2.40")) {
            PropertyType.setSourceVersion(3);
            mapFileName = "classMappings-2.40.txt";
         } else if (version.contains("2.44")) {
            PropertyType.setSourceVersion(4);
            mapFileName = "classMappings-2.44.txt";
         } else if (version.contains("2.46")) {
            PropertyType.setSourceVersion(4);
            mapFileName = "classMappings-2.46.txt";
         } else if (version.contains("2.48")) {
            PropertyType.setSourceVersion(4);
            mapFileName = "classMappings-2.48.txt";
         } else if (version.contains("2.50")) {
            PropertyType.setSourceVersion(5);
            mapFileName = "classMappings-2.52.txt"; // never did a 2.50, skipped to 2.52
         } else if (version.contains("2.52")) {
            PropertyType.setSourceVersion(6);
            mapFileName = "classMappings-2.52.txt";
         } else if (version.contains("2.54")) {
            PropertyType.setSourceVersion(7);
            mapFileName = "classMappings-2.54.txt";
         } else if (version.contains("2.56")) {
            PropertyType.setSourceVersion(7);
            mapFileName = "classMappings-2.56.txt";
         } else if (version.contains("2.59")) {
            PropertyType.setSourceVersion(8);
            mapFileName = "classMappings-2.59.txt";
         } else {
            ConsoleUtil.printErrorln("ERROR: UNTESTED VERSION - NO TESTED PROPERTY MAPPING EXISTS!");
            PropertyType.setSourceVersion(8);
            mapFileName = "classMappings-2.59.txt";
         }

         this.classMapping = new NameMap(mapFileName);

         if (mapTo != null) {
            String[] line = mapTo.readLine();

            while (line != null) {
               if (line.length > 0) {
                  HashMap<String, String> nestedData = this.mapToData.get(line[0]);

                  if (nestedData == null) {
                     nestedData = new HashMap<>();
                     this.mapToData.put(line[0], nestedData);
                  }

                  if (nestedData.put(line[1], line[2]) != null) {
                     throw new Exception("Oops - " + line[0] + " " + line[1] + " " + line[2]);
                  }
               }

               line = mapTo.readLine();
            }
         }

         initProperties();
         ConsoleUtil.println("Loading Metadata");

         // Set up a meta-data root concept
         final ComponentReference metadata = ComponentReference.fromConcept(
               this.importUtil.createConcept("LOINC Metadata" + IBDFCreationUtility.METADATA_SEMANTIC_TAG, true, MetaData.SOLOR_CONTENT_METADATA____SOLOR.getPrimordialUuid()));

         this.importUtil.loadTerminologyMetadataAttributes(this.converterSourceArtifactVersion, Optional.of(loincData.getReleaseDate()),
               this.converterOutputArtifactVersion, Optional.ofNullable(this.converterOutputArtifactClassifier), this.converterVersion);
         this.importUtil.loadMetaDataItems(this.propertyTypes_, metadata.getPrimordialUuid());

         // Load up the propertyType map for speed, perform basic sanity check
         for (final PropertyType pt : this.propertyTypes_) {
            for (final String propertyName : pt.getPropertyNames()) {
               if (this.propertyToPropertyType_.containsKey(propertyName)) {
                  ConsoleUtil.printErrorln("ERROR: Two different property types each contain " + propertyName);
               }

               this.propertyToPropertyType_.put(propertyName, pt);
            }
         }

         if (sourceOrg != null) {
            final ComponentReference sourceOrgConcept = ComponentReference.fromConcept(this.importUtil.createConcept("Source Organization", true, metadata.getPrimordialUuid()));
            String[] line = sourceOrg.readLine();

            while (line != null) {
               // ﻿"COPYRIGHT_ID","NAME","COPYRIGHT","TERMS_OF_USE","URL"
               if (line.length > 0) {
                  final ComponentReference c = ComponentReference.fromConcept(this.importUtil.createConcept(line[0], false, sourceOrgConcept.getPrimordialUuid()));

                  this.importUtil.addDescription(c, line[1], DescriptionType.REGULAR_NAME, true, this.propertyToPropertyType_.get("NAME").getProperty("NAME").getUUID(),
                        Status.ACTIVE);
                  this.importUtil.addStringAnnotation(c, line[2], this.propertyToPropertyType_.get("COPYRIGHT").getProperty("COPYRIGHT").getUUID(), Status.ACTIVE);
                  this.importUtil.addStringAnnotation(c, line[3], this.propertyToPropertyType_.get("TERMS_OF_USE").getProperty("TERMS_OF_USE").getUUID(), Status.ACTIVE);
                  this.importUtil.addStringAnnotation(c, line[4], this.propertyToPropertyType_.get("URL").getProperty("URL").getUUID(), Status.ACTIVE);
               }

               line = sourceOrg.readLine();
            }
         }

         final UUID loincAllConceptsRefset = PT_Refsets.Refsets.ALL.getProperty().getUUID();

         // The next line of the file is the header.
         final String[] headerFields = loincData.getHeader();

         // validate that we are configured to map all properties properly
         checkForLeftoverPropertyTypes(headerFields);
         ConsoleUtil.println("Metadata summary:");

         for (final String s : this.importUtil.getLoadStats().getSummary()) {
            ConsoleUtil.println("  " + s);
         }

         this.importUtil.clearLoadStats();

         // Root
         final ComponentReference rootConcept = ComponentReference.fromConcept(this.importUtil.createConcept("LOINC", true, MetaData.SOLOR_CONCEPT____SOLOR.getPrimordialUuid()));

         this.importUtil.addDescription(rootConcept, "Logical Observation Identifiers Names and Codes", DescriptionType.REGULAR_NAME, false, null, Status.ACTIVE);
         ConsoleUtil.println("Root concept FQN is 'LOINC' and the UUID is " + rootConcept.getPrimordialUuid());
         this.concepts_.put(rootConcept.getPrimordialUuid(), rootConcept);

         // Build up the Class metadata
         final ComponentReference classConcept = ComponentReference.fromConcept(
               this.importUtil.createConcept(this.pt_SkipClass.getPropertyTypeUUID(), this.pt_SkipClass.getPropertyTypeDescription(), true, rootConcept.getPrimordialUuid()));

         this.concepts_.put(classConcept.getPrimordialUuid(), classConcept);

         for (final String property : this.pt_SkipClass.getPropertyNames()) {
            final ComponentReference temp = ComponentReference
                  .fromConcept(this.importUtil.createConcept(this.pt_SkipClass.getProperty(property).getUUID(), property, true, classConcept.getPrimordialUuid()));

            this.concepts_.put(temp.getPrimordialUuid(), temp);
            this.importUtil.configureConceptAsAssociation(temp.getPrimordialUuid(), null);
         }

         // And the axis metadata
         final ComponentReference axisConcept = ComponentReference.fromConcept(
               this.importUtil.createConcept(this.pt_SkipAxis.getPropertyTypeUUID(), this.pt_SkipAxis.getPropertyTypeDescription(), true, rootConcept.getPrimordialUuid()));

         this.concepts_.put(axisConcept.getPrimordialUuid(), axisConcept);

         for (final String property : this.pt_SkipAxis.getPropertyNames()) {
            final ComponentReference temp = ComponentReference
                  .fromConcept(this.importUtil.createConcept(this.pt_SkipAxis.getProperty(property).getUUID(), property, true, axisConcept.getPrimordialUuid()));

            this.concepts_.put(temp.getPrimordialUuid(), temp);
            this.importUtil.configureConceptAsAssociation(temp.getPrimordialUuid(), null);
         }

         // load the data
         ConsoleUtil.println("Processing file....");

         int dataRows = 0;

         {
            String[] line = loincData.readLine();

            dataRows++;

            while (line != null) {
               if (line.length > 0) {
                  processDataLine(line);
               }

               line = loincData.readLine();
               dataRows++;

               if (dataRows % 1000 == 0) {
                  ConsoleUtil.showProgress();
               }

               if (dataRows % 10000 == 0) {
                  ConsoleUtil.println("Processed " + dataRows + " lines");
               }
            }
         }

         loincData.close();
         ConsoleUtil.println("Read " + dataRows + " data lines from file");
         ConsoleUtil.println("Processing multi-axial file");

         {

            // header - PATH_TO_ROOT,SEQUENCE,IMMEDIATE_PARENT,CODE,CODE_TEXT
            int lineCount = 0;
            String[] line = loincMultiData.readLine();

            while (line != null) {
               lineCount++;

               if (line.length == 5) {
                  processMultiAxialData(rootConcept.getPrimordialUuid(), line);
               }
               
               else
               {
                  ConsoleUtil.printErrorln("Skipping multiaxial line because its the wrong length: " + Arrays.toString(line));
               }

               line = loincMultiData.readLine();

               if (lineCount % 1000 == 0) {
                  ConsoleUtil.showProgress();
               }
            }

            loincMultiData.close();
            ConsoleUtil.println("Read " + lineCount + " data lines from file.  Creating graphs and hierarcy concepts");

            for (final Entry<UUID, HashSet<UUID>> items : this.multiaxialPathsToRoot.entrySet()) {
               final UUID source = items.getKey();
               final HashSet<UUID> parents = items.getValue();
               final LogicalExpressionBuilder leb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
               final ConceptAssertion[] assertions = new ConceptAssertion[parents.size()];
               int i = 0;

               for (final UUID parent : parents) {
                  assertions[i++] = ConceptAssertion(Get.identifierService().getNidForUuids(parent), leb);
               }

               NecessarySet(And(assertions));
               this.importUtil.addRelationshipGraph(ComponentReference.fromConcept(source), null, leb.build(), true, null, null);
            }
         }

         ConsoleUtil.println("Creating all concepts refset");

         // Add all of the concepts to a refset
         for (final ComponentReference concept : this.concepts_.values()) {
            this.importUtil.addAssemblageMembership(concept, loincAllConceptsRefset, Status.ACTIVE, null);
         }

         ConsoleUtil.println("Processed " + this.concepts_.size() + " concepts total");
         ConsoleUtil.println("Data Load Summary:");

         for (final String s : this.importUtil.getLoadStats().getSummary()) {
            ConsoleUtil.println("  " + s);
         }

         ConsoleUtil.println("Skipped " + this.skippedDeletedItems + " Loinc codes because they were flagged as DELETED and they had no desriptions.");

         // this could be removed from final release. Just added to help debug editor problems.
         ConsoleUtil.println("Dumping UUID Debug File");
         ConverterUUID.dump(this.outputDirectory, "loincUuid");
         ConsoleUtil.println("LOINC Processing Completes " + new Date().toString());
         ConsoleUtil.writeOutputToFile(new File(this.outputDirectory, "ConsoleOutput.txt").toPath());
      } catch (final Exception ex) {
         try {
            // make sure this is dumped
            ConverterUUID.dump(this.outputDirectory, "loincUuid");
         } catch (final IOException e) {
            // noop
         }

         throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
      } finally {
         try {
            if (this.importUtil != null) {
               this.importUtil.shutdown();
            }

            if (loincData != null) {
               loincData.close();
            }

            if (loincMultiData != null) {
               loincMultiData.close();
            }

            if (mapTo != null) {
               mapTo.close();
            }

            if (sourceOrg != null) {
               sourceOrg.close();
            }
         } catch (final IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
         }
      }
   }

   /**
    * Used for debug. Sets up the same paths that maven would use.... allow the code to be run standalone.
    *
    * @param args the arguments
    * @throws Exception the exception
    */
   public static void main(String[] args) throws Exception {
      final LoincImportMojo loincConverter = new LoincImportMojo();

      loincConverter.outputDirectory = new File("../loinc-ibdf/target/");
      loincConverter.inputFileLocation = new File("../loinc-ibdf/target/generated-resources/src");
      loincConverter.converterVersion = "foo";
      loincConverter.converterOutputArtifactVersion = "foo";
      loincConverter.converterOutputArtifactClassifier = "foo";
      loincConverter.converterSourceArtifactVersion = "foo";
      loincConverter.execute();
   }

   /**
    * Supports annotation skip list.
    *
    * @return true, if successful
    */
   @Override
   protected boolean supportsAnnotationSkipList() {
      return true;
   }

   /**
    * Utility to help build UUIDs in a consistent manner.
    *
    * @param uniqueIdentifier the unique identifier
    * @return the uuid
    */
   private UUID buildUUID(String uniqueIdentifier) {
      return ConverterUUID.createNamespaceUUIDFromString(uniqueIdentifier, true);
   }

   /**
    * Check for leftover property types.
    *
    * @param fileColumnNames the file column names
    * @throws Exception the exception
    */
   private void checkForLeftoverPropertyTypes(String[] fileColumnNames) throws Exception {
      for (final String name : fileColumnNames) {
         final PropertyType pt = this.propertyToPropertyType_.get(name);

         if (pt == null) {
            ConsoleUtil.printErrorln("ERROR:  No mapping for property type: " + name);
         }
      }
   }

   /**
    * Check path.
    *
    * @param concept the concept
    * @param pathToRoot the path to root
    */
   private void checkPath(ComponentReference concept,
         String[] pathToRoot) {
      // The passed in concept should have a relation to the item at the end of the root list.
      for (int i = (pathToRoot.length - 1); i >= 0; i--) {
         final UUID target = buildUUID(pathToRoot[i]);
         HashSet<UUID> parents = this.multiaxialPathsToRoot.get(concept.getPrimordialUuid());

         if (parents == null) {
            parents = new HashSet<>();
            this.multiaxialPathsToRoot.put(concept.getPrimordialUuid(), parents);
         }

         parents.add(target);
         concept = this.concepts_.get(target);

         if (concept == null) {
            ConsoleUtil.printErrorln("Missing concept! " + pathToRoot[i]);
            break;
         }
      }
   }

   /**
    * Inits the properties.
    */
   private void initProperties() {
      this.propertyTypes_.add(new PT_Annotations(this.annotationSkipList));
      this.propertyTypes_.add(new PT_Descriptions());
      this.propertyTypes_.add(new PT_Associations());
      this.propertyTypes_.add(this.pt_SkipAxis);
      this.propertyTypes_.add(this.pt_SkipClass);
      this.propertyTypes_.add(new PT_SkipOther(this.annotationSkipList));
      this.propertyTypes_.add(new PT_Refsets());
      this.propertyTypes_.add(new PT_Relations());
   }

   /**
    * Map status.
    *
    * @param status the status
    * @return the state
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private Status mapStatus(String status) throws IOException {
      switch (status) {
         case "ACTIVE":
         case "TRIAL":
         case "DISCOURAGED":
            return Status.ACTIVE;

         case "DEPRECATED":
            return Status.INACTIVE;

         default:
            ConsoleUtil.printErrorln("No mapping for status: " + status);
            return Status.ACTIVE;
      }
   }

   /**
    * Process data line.
    *
    * @param fields the fields
    * @throws ParseException the parse exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private void processDataLine(String[] fields) throws ParseException, IOException {
      Integer index = this.fieldMap.get("VersionLastChanged"); // They changed this in 2.54 release
      Long time;

      if (index != null) {
         time = this.versionTimeMap.get(fields[index]);

         if (time == null) {
            throw new IOException("Couldn't find time for version " + fields[index]);
         }
      } else {
         index = this.fieldMap.get("DATE_LAST_CHANGED"); // They changed this in 2.38 release

         if (index == null) {
            index = this.fieldMap.get("DT_LAST_CH");
         }

         final String lastChanged = fields[index];

         time = (StringUtils.isBlank(lastChanged) ? null : this.sdf_.parse(lastChanged).getTime());
      }

      final Status status = mapStatus(fields[this.fieldMap.get("STATUS")]);
      final String code = fields[this.fieldMap.get("LOINC_NUM")];
      final ComponentReference concept = ComponentReference.fromConcept(this.importUtil.createConcept(buildUUID(code), time, status, null));
      final ArrayList<ValuePropertyPair> descriptions = new ArrayList<>();

      for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
         if ((fields[fieldIndex] != null) && (fields[fieldIndex].length() > 0)) {
            final PropertyType pt = this.propertyToPropertyType_.get(this.fieldMapInverse.get(fieldIndex));

            if (pt == null) {
               ConsoleUtil.printErrorln("ERROR: No property type mapping for the property " + this.fieldMapInverse.get(fieldIndex) + ":" + fields[fieldIndex]);
               continue;
            }

            final Property p = pt.getProperty(this.fieldMapInverse.get(fieldIndex));

            if (pt instanceof PT_Annotations) {
               if ((p.getSourcePropertyNameFQN().equals("COMMON_TEST_RANK") || p.getSourcePropertyNameFQN().equals("COMMON_ORDER_RANK")
                     || p.getSourcePropertyNameFQN().equals("COMMON_SI_TEST_RANK")) && fields[fieldIndex].equals("0")) {
                  continue; // Skip attributes of these types when the value is 0
               }

               if (p.isIdentifier()) {
                  this.importUtil.addStaticStringAnnotation(concept, fields[fieldIndex], p.getUUID(), (p.isDisabled() ? Status.INACTIVE : Status.ACTIVE));
               } else {
                  this.importUtil.addStringAnnotation(concept, fields[fieldIndex], p.getUUID(), (p.isDisabled() ? Status.INACTIVE : Status.ACTIVE));
               }
            } else if (pt instanceof PT_Descriptions) {
               // Gather for later - need to make our own UUIDs, because the default algorithm doesn't take extended types into account.
               descriptions.add(new ValuePropertyPair(fields[fieldIndex], 
                     ConverterUUID.createNamespaceUUIDFromStrings(concept.getPrimordialUuid().toString(), fields[fieldIndex],
                     p.getPropertyType().getPropertyTypeUUID().toString(), p.getUUID().toString()), p));

            } else if (pt instanceof PT_SkipAxis) {
               // See if this class object exists yet.
               final UUID potential = ConverterUUID
                     .createNamespaceUUIDFromString(this.pt_SkipAxis.getPropertyTypeDescription() + ":" + this.fieldMapInverse.get(fieldIndex) + ":" + fields[fieldIndex], true);
               ComponentReference axisConcept = this.concepts_.get(potential);

               if (axisConcept == null) {
                  axisConcept = ComponentReference.fromConcept(this.importUtil.createConcept(potential, fields[fieldIndex], true));
                  this.importUtil.addParent(axisConcept, this.pt_SkipAxis.getProperty(this.fieldMapInverse.get(fieldIndex)).getUUID());
                  this.concepts_.put(axisConcept.getPrimordialUuid(), axisConcept);
               }

               this.importUtil.addAssociation(concept, null, axisConcept.getPrimordialUuid(), this.pt_SkipAxis.getProperty(this.fieldMapInverse.get(fieldIndex)).getUUID(),
                     Status.ACTIVE, concept.getTime(), null);
            } else if (pt instanceof PT_SkipClass) {
               // See if this class object exists yet.
               final UUID potential = ConverterUUID
                     .createNamespaceUUIDFromString(this.pt_SkipClass.getPropertyTypeDescription() + ":" + this.fieldMapInverse.get(fieldIndex) + ":" + fields[fieldIndex], true);
               ComponentReference classConcept = this.concepts_.get(potential);

               if (classConcept == null) {
                  classConcept = ComponentReference.fromConcept(this.importUtil.createConcept(potential, this.classMapping.getMatchValue(fields[fieldIndex]), true));

                  if (this.classMapping.hasMatch(fields[fieldIndex])) {
                     this.importUtil.addStringAnnotation(classConcept, fields[fieldIndex], this.propertyToPropertyType_.get("ABBREVIATION").getProperty("ABBREVIATION").getUUID(),
                           Status.ACTIVE);
                  }

                  this.importUtil.addParent(classConcept, this.pt_SkipClass.getProperty(this.fieldMapInverse.get(fieldIndex)).getUUID());
                  this.concepts_.put(classConcept.getPrimordialUuid(), classConcept);
               }

               this.importUtil.addAssociation(concept, null, classConcept.getPrimordialUuid(), this.pt_SkipClass.getProperty(this.fieldMapInverse.get(fieldIndex)).getUUID(),
                     Status.ACTIVE, concept.getTime(), null);
            } else if (pt instanceof PT_Relations) {
               // This will only ever be is_a
               final UUID parent = buildUUID(fields[fieldIndex]);

               this.importUtil.addParent(concept, parent, pt.getProperty(this.fieldMapInverse.get(fieldIndex)), null);
            } else if (pt instanceof PT_Associations) {
               this.importUtil.addAssociation(concept, null, buildUUID(fields[fieldIndex]), pt.getProperty(this.fieldMapInverse.get(fieldIndex)).getUUID(), Status.ACTIVE,
                     concept.getTime(), null);
            } else if (pt instanceof PT_SkipOther) {
               this.importUtil.getLoadStats().addSkippedProperty();
            } else {
               ConsoleUtil.printErrorln("oops - unexpected property type: " + pt);
            }
         }
      }

      // MAP_TO moved to a different file in 2.42.
      final HashMap<String, String> mappings = this.mapToData.get(code);

      if (mappings != null) {
         mappings.entrySet().forEach((mapping) -> {
            final String target = mapping.getKey();
            final String comment = mapping.getValue();
            final ComponentReference cr = ComponentReference.fromChronology(this.importUtil.addAssociation(concept, null, buildUUID(target),
                  this.propertyToPropertyType_.get("MAP_TO").getProperty("MAP_TO").getUUID(), Status.ACTIVE, concept.getTime(), null), () -> "Association");

            if ((comment != null) && (comment.length() > 0)) {
               this.importUtil.addStringAnnotation(cr, comment, this.propertyToPropertyType_.get("COMMENT").getProperty("COMMENT").getUUID(), Status.ACTIVE);
            }
         });
      }

      // Now add all the descriptions
      if (descriptions.isEmpty()) {
         if ("DEL".equals(fields[this.fieldMap.get("CHNG_TYPE")])) {
            // They put a bunch of these in 2.44... leaving out most of the important info... just makes a mess. Don't load them.
            this.skippedDeletedItems++;
            return;
         } else {
            ConsoleUtil.printErrorln("ERROR: no name for " + code);
            this.importUtil.addFullySpecifiedName(concept, code);
         }
      } else {
         this.importUtil.addDescriptions(concept, descriptions);
      }

      final ComponentReference current = this.concepts_.put(concept.getPrimordialUuid(), concept);

      if (current != null) {
         ConsoleUtil.printErrorln("Duplicate LOINC code (LOINC_NUM):" + code);
      }
   }

   /**
    * Process multi axial data.
    *
    * @param rootConcept the root concept
    * @param line the line
    */
   private void processMultiAxialData(UUID rootConcept,
         String[] line) {
      // PATH_TO_ROOT,SEQUENCE,IMMEDIATE_PARENT,CODE,CODE_TEXT
      // This file format used to be a disaster... but it looks like since 2.40, they encode proper CSV, so I've thrown out the custom parsing.
      // If you need the old custom parser that reads the crap they used to produce as 'CSV', look at the SVN history for this method.
      final String pathString = line[0];
      final String[] pathToRoot = ((pathString.length() > 0) ? pathString.split("\\.") : new String[] {});
      final String sequence = line[1];
      final String immediateParentString = line[2];
      final UUID immediateParent = (((immediateParentString == null) || (immediateParentString.length() == 0)) ? rootConcept : buildUUID(immediateParentString));
      final String code = line[3];
      final String codeText = line[4];

      if ((code.length() == 0) || (codeText.length() == 0)) {
         ConsoleUtil.printErrorln("missing code or text!");
      }

      final UUID potential = buildUUID(code);
      ComponentReference concept = this.concepts_.get(potential);

      if (concept == null) {
         concept = ComponentReference.fromConcept(this.importUtil.createConcept(potential));

         if ((sequence != null) && (sequence.length() > 0)) {
            this.importUtil.addStringAnnotation(concept, sequence, this.propertyToPropertyType_.get("SEQUENCE").getProperty("SEQUENCE").getUUID(), Status.ACTIVE);
         }

         if ((immediateParentString != null) && (immediateParentString.length() > 0)) {
            this.importUtil.addStringAnnotation(concept, immediateParentString, this.propertyToPropertyType_.get("IMMEDIATE_PARENT").getProperty("IMMEDIATE_PARENT").getUUID(),
                  Status.ACTIVE);
         }

         final ValuePropertyPair vpp = new ValuePropertyPair(codeText, this.propertyToPropertyType_.get("CODE_TEXT").getProperty("CODE_TEXT"));

         this.importUtil.addDescriptions(concept, Arrays.asList(vpp)); // This will get added as FULLY_QUALIFIED_NAME

         HashSet<UUID> parents = this.multiaxialPathsToRoot.get(concept.getPrimordialUuid());

         if (parents == null) {
            parents = new HashSet<>();
            this.multiaxialPathsToRoot.put(concept.getPrimordialUuid(), parents);
         }

         parents.add(immediateParent);

         if (!pathString.isEmpty()) {
            this.importUtil.addStringAnnotation(concept, pathString, this.propertyToPropertyType_.get("PATH_TO_ROOT").getProperty("PATH_TO_ROOT").getUUID(), Status.ACTIVE);
         }

         this.importUtil.addStringAnnotation(concept, code, this.propertyToPropertyType_.get("LOINC_NUM").getProperty("LOINC_NUM").getUUID(), Status.ACTIVE);
         this.importUtil.addStaticStringAnnotation(concept, code, MetaData.CODE____SOLOR.getPrimordialUuid(), Status.ACTIVE);
         this.concepts_.put(concept.getPrimordialUuid(), concept);
      }

      // Make sure everything in pathToRoot is linked.
      checkPath(concept, pathToRoot);
   }

   // ~--- get methods ---------------------------------------------------------
}
