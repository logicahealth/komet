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



package gov.vha.isaac.rf2.convert.mojo;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LanguageCode;
import sh.isaac.api.MavenConceptProxy;
import sh.isaac.api.State;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.converters.sharedUtils.ComponentReference;
import sh.isaac.converters.sharedUtils.ConsoleUtil;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility.DescriptionType;
import sh.isaac.converters.sharedUtils.sql.ColumnDefinition;
import sh.isaac.converters.sharedUtils.sql.DataType;
import sh.isaac.converters.sharedUtils.sql.H2DatabaseHandle;
import sh.isaac.converters.sharedUtils.sql.TableDefinition;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.utility.Frills;
import sh.isaac.utility.LanguageMap;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import static sh.isaac.api.logic.LogicalExpressionBuilder.SomeRole;
import static sh.isaac.api.logic.LogicalExpressionBuilder.SufficientSet;

//~--- classes ----------------------------------------------------------------

/**
 * Loader code to convert RxNorm into the workbench.
 */
@Mojo(
   name         = "convert-RF2-to-ibdf",
   defaultPhase = LifecyclePhase.PROCESS_SOURCES
)
public class RF2Mojo
        extends ConverterBaseMojo {
   // Some constants from SCT
   // "Part of (attribute)"
   // TODO get these contstants from metadata directly... ?

   /** The part of. */
   // TODO Add to or get from IsaacMetadataAuxiliary?
   public static UUID PART_OF = UUID.fromString("b4c3f6f9-6937-30fd-8412-d0c77f8a7f73");

   /** The laterality. */

   // "Laterality (attribute)"
   public static UUID LATERALITY = UUID.fromString("26ca4590-bbe5-327c-a40a-ba56dc86996b");

   /** The has active ingredient. */

   // "Has active ingredient (attribute)"
   public static UUID HAS_ACTIVE_INGREDIENT = UUID.fromString("65bf3b7f-c854-36b5-81c3-4915461020a8");

   /** The has dose form. */

   // "Has dose form (attribute)"
   public static UUID HAS_DOSE_FORM = UUID.fromString("072e7737-e22e-36b5-89d2-4815f0529c63");

   /** The sufficiently defined. */

   // 900000000000073002  "Sufficiently defined concept definition status (core metadata concept)"
   public static UUID SUFFICIENTLY_DEFINED = UUID.fromString("6d9cd46e-8a8f-310a-a298-3e55dcf7a986");

   /** The necessary but not sufficient. */

   // 900000000000074008  "Necessary but not sufficient concept definition status (core metadata concept)"
   public static UUID NECESSARY_BUT_NOT_SUFFICIENT = UUID.fromString("e1a12059-3b01-3296-9532-d10e49d0afc3");

   /** The date parse. */
   protected static SimpleDateFormat dateParse = new SimpleDateFormat("yyyyMMdd");

   //~--- fields --------------------------------------------------------------

   /** The output json. */
   private final boolean outputJson = false;  // Set to true to produce a json dump file

   /** The descriptions. */
   private final ArrayList<String> DESCRIPTIONS = new ArrayList<>();

   /** The languages. */
   private final ArrayList<String> LANGUAGES = new ArrayList<>();

   /** The tables. */
   private final HashMap<String, TableDefinition> tables_ = new HashMap<>();

   // map concept UUID to a treemap that has the time and the status value.

   /** The concept definition status cache. */
   // Could shrink the size of this if necessary by using con sequence ids....
   private final HashMap<UUID, TreeMap<Long, UUID>> conceptDefinitionStatusCache = new HashMap<>();

   /** The cons with no stated rel. */

   // This cache is to work around a data problem where stated rels are missing from SCT.
   private final HashSet<UUID> consWithNoStatedRel = new HashSet<>();

   /** The never role group set. */
   private final HashSet<UUID> neverRoleGroupSet = new HashSet<>();

   /** The input type. */
   private InputType inputType = null;

   /** Default value from SNOMED_CT_CORE_MODULE. */
   @Parameter(required = false)
   private ConceptSpecification moduleUUID = MetaData.SNOMED_CT_CORE_MODULES;

   /** The db. */
   private H2DatabaseHandle db;

   /** The content name version. */
   String contentNameVersion;

   /** The time string. */
   String timeString;

   /** The stated relationship. */
   private String CONCEPT, RELATIONSHIP, STATED_RELATIONSHIP;

   //~--- initializers --------------------------------------------------------

   {
      this.neverRoleGroupSet.add(PART_OF);
      this.neverRoleGroupSet.add(LATERALITY);
      this.neverRoleGroupSet.add(HAS_ACTIVE_INGREDIENT);
      this.neverRoleGroupSet.add(HAS_DOSE_FORM);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Execute.
    *
    * @throws MojoExecutionException the mojo execution exception
    */
   @Override
   public void execute()
            throws MojoExecutionException {
      try {
         super.execute();
         this.inputType = InputType.parse(this.converterOutputArtifactClassifier);

         final File zipFile = init();

         loadDatabase(zipFile);

         final ComponentReference rf2Metadata =
            ComponentReference.fromConcept(super.importUtil.createConcept("RF2 Metadata " + this.contentNameVersion,
                                                                          true));

         super.importUtil.addParent(rf2Metadata, MetaData.SOLOR_CONTENT_METADATA.getPrimordialUuid());
         super.importUtil.loadTerminologyMetadataAttributes(rf2Metadata,
               this.converterSourceArtifactVersion,
               Optional.ofNullable(this.timeString),
               this.converterOutputArtifactVersion,
               Optional.of(this.converterOutputArtifactClassifier),
               this.converterVersion);

         // process content
         transformConcepts();
         transformDescriptions();

         // stated first, so we can know what doesn't get stated graphs
         transformRelationships(true);
         ConsoleUtil.println("Noticed " + this.consWithNoStatedRel.size() + " concepts with no stated relationships");
         transformRelationships(false);

         // can clear this cache now
         ConsoleUtil.println("After copying inferred rels, still " + this.consWithNoStatedRel.size() +
                             " concepts with no stated relationships");
         this.consWithNoStatedRel.clear();
         ConsoleUtil.println("Dumping UUID Debug File");
         ConverterUUID.dump(this.outputDirectory, this.converterOutputArtifactClassifier + "-RF2UUID");
         ConsoleUtil.println("Load Statistics");

         for (final String s: super.importUtil.getLoadStats()
               .getSummary()) {
            ConsoleUtil.println(s);
         }

         // shutdown
         super.importUtil.shutdown();
         this.db.shutdown();
         ConsoleUtil.println("Finished converting " + this.contentNameVersion + "-" +
                             this.converterOutputArtifactClassifier);
         ConsoleUtil.writeOutputToFile(new File(this.outputDirectory,
               this.converterOutputArtifactClassifier + "-ConsoleOutput.txt").toPath());
      } catch (final Exception e) {
         throw new MojoExecutionException("Failure during conversion", e);
      }
   }

   /**
    * The main method.
    *
    * @param args the arguments
    * @throws MojoExecutionException the mojo execution exception
    */
   public static void main(String[] args)
            throws MojoExecutionException {
      final RF2Mojo mojo = new RF2Mojo();

      mojo.outputDirectory                   = new File("../rf2-ibdf/sct/target");
      mojo.inputFileLocation                 = new File("../rf2-ibdf/sct/target/generated-resources/src/");
      mojo.converterVersion                  = "foo";
      mojo.converterOutputArtifactVersion    = "bar";
      mojo.converterOutputArtifactClassifier = "Full";
      mojo.converterSourceArtifactVersion    = "bar";
      mojo.execute();
   }

   /**
    * Clear target files.
    *
    * @param contentNameVersion the content name version
    */
   private void clearTargetFiles(String contentNameVersion) {
      new File(this.outputDirectory, this.converterOutputArtifactClassifier + "-RF2UUIDDebugMap.txt").delete();
      new File(this.outputDirectory, this.converterOutputArtifactClassifier + "-ConsoleOutput.txt").delete();
      new File(this.outputDirectory,
               "RF2-" + contentNameVersion + "-" + this.converterOutputArtifactClassifier + ".ibdf").delete();

      // For debug only, normally commented out
      // new File(outputDirectory, contentNameVersion + ".h2.db").delete();
   }

   /**
    * Creates the table definition.
    *
    * @param tableName the table name
    * @param header the header
    * @param sampleDataRow the sample data row
    * @return the table definition
    */
   private TableDefinition createTableDefinition(String tableName, String[] header, String[] sampleDataRow) {
      final TableDefinition td = new TableDefinition(tableName);

      for (int i = 0; i < header.length; i++) {
         DataType dataType;

         if (header[i].equals("id") || header[i].endsWith("Id")) {
            // See if this looks like a UUID or a long
            try {
               Long.parseLong(sampleDataRow[i]);
               dataType = new DataType(DataType.SUPPORTED_DATA_TYPE.LONG, null, false);
            } catch (NumberFormatException
                  | NullPointerException e)  // Might be a null pointer if there is no data, just treat it as a string (doesn't matter)
            {
               // UUID
               dataType = new DataType(DataType.SUPPORTED_DATA_TYPE.STRING, 36, false);
            }
         } else if (header[i].equals("active")) {
            dataType = new DataType(DataType.SUPPORTED_DATA_TYPE.BOOLEAN, null, false);
         } else if (header[i].equals("effectiveTime") ||
                    header[i].equals("sourceEffectiveTime") ||
                    header[i].equals("targetEffectiveTime")) {
            dataType = new DataType(DataType.SUPPORTED_DATA_TYPE.STRING, 8, false);
         } else {
            dataType = new DataType(DataType.SUPPORTED_DATA_TYPE.STRING, null, true);
            ConsoleUtil.println("Treating " + header[i] + " as a string");
         }

         final ColumnDefinition cd = new ColumnDefinition(header[i], dataType);

         td.addColumn(cd);
      }

      return td;
   }

   /**
    * Inits the.
    *
    * @return the file
    * @throws Exception the exception
    */
   private File init()
            throws Exception {
      File zipFile = null;

      for (final File f: this.inputFileLocation.listFiles()) {
         if (f.getName()
              .toLowerCase()
              .endsWith(".zip")) {
            if (zipFile != null) {
               throw new MojoExecutionException("Only expected to find one zip file in the folder " +
                                                this.inputFileLocation.getCanonicalPath());
            }

            zipFile = f;
         }
      }

      if (zipFile == null) {
         throw new MojoExecutionException("Did not find a zip file in " + this.inputFileLocation.getCanonicalPath());
      }

      this.contentNameVersion = zipFile.getName()
                                        .substring(0, zipFile.getName()
                                              .length() - 4);
      ConsoleUtil.println("Converting " + this.contentNameVersion + "-" + this.converterOutputArtifactClassifier);

      final String[]         temp        = this.contentNameVersion.split("_");
      final SimpleDateFormat sdf         = new SimpleDateFormat("yyyyMMdd");
      long                   defaultTime = 0;

      for (int i = temp.length - 1; i > 0; i--) {
         if ((temp[i].length() == 8) && NumberUtils.isDigits(temp[i])) {
            defaultTime      = sdf.parse(temp[i])
                                  .getTime();
            this.timeString = temp[i];
            break;
         }
      }

      if (defaultTime == 0) {
         throw new MojoExecutionException("Couldn't parse date out of " + this.contentNameVersion);
      }

      clearTargetFiles(this.contentNameVersion);

      File[]     ibdfFiles  = new File[0];
      final File ibdfFolder = new File(this.inputFileLocation, "ibdf");

      if (ibdfFolder.isDirectory()) {
         ibdfFiles = ibdfFolder.listFiles((File pathname) -> {
                                             return pathname.isFile() &&
                                                   pathname.getName().toLowerCase().endsWith(".ibdf");
                                          });
      }

      super.importUtil = new IBDFCreationUtility(Optional.empty(),
            Optional.of(this.moduleUUID),
            this.outputDirectory,
            this.converterOutputArtifactId,
            this.converterOutputArtifactVersion,
            this.converterOutputArtifactClassifier,
            this.outputJson,
            defaultTime,
            Arrays.asList(new SememeType[] { SememeType.DESCRIPTION, SememeType.COMPONENT_NID, SememeType.DYNAMIC,
                  SememeType.LONG }),
            true,
            ibdfFiles);
      return zipFile;
   }

   /**
    * Load database.
    *
    * @param zipFile the zip file
    * @throws Exception the exception
    */
   private void loadDatabase(File zipFile)
            throws Exception {
      final long time = System.currentTimeMillis();

      this.db = new H2DatabaseHandle();

      final File dbFile = new File(this.outputDirectory,
                                   this.contentNameVersion + "-" + this.converterOutputArtifactClassifier + ".h2.db");
      final boolean createdNew = this.db.createOrOpenDatabase(new File(this.outputDirectory,
                                                                        this.contentNameVersion + "-" +
                                                                        this.converterOutputArtifactClassifier));

      if (!createdNew) {
         ConsoleUtil.println("Using existing database.  To load from scratch, delete the file '" +
                             dbFile.getCanonicalPath() + ".*'");
      }

      int tableCount;

      try (ZipFile zf = new ZipFile(zipFile)) {
         final Enumeration<? extends ZipEntry> zipEntries = zf.entries();

         tableCount = 0;

         while (zipEntries.hasMoreElements()) {
            final ZipEntry ze        = zipEntries.nextElement();
            final String[] structure = ze.getName()
                                         .split("\\/");

            if ((structure[0].toUpperCase().equals(this.inputType.name()) ||
                  ((structure.length > 1) && structure[1].toUpperCase().equals(this.inputType.name())) ||
                  ((structure.length > 2) && structure[2].toUpperCase().equals(this.inputType.name()))) &&
                  ze.getName().toLowerCase().endsWith(".txt")) {
               // One of the data files we want to load
               ConsoleUtil.println("Loading " + ze.getName());

               final RF2FileReader fileReader = new RF2FileReader(zf.getInputStream(ze));
               String              tableName  = structure[structure.length - 1];

               tableName = tableName.substring(0, tableName.length() - 4);
               tableName = tableName.replaceAll("-", "_");  // hyphens cause sql issues

               if (tableName.toLowerCase()
                            .startsWith("sct2_concept_")) {
                  this.CONCEPT = tableName;
               } else if (tableName.toLowerCase().startsWith("sct2_description_") ||
                          tableName.toLowerCase().startsWith("sct2_textdefinition_")) {
                  this.DESCRIPTIONS.add(tableName);
               } else if (tableName.toLowerCase().startsWith("der2_crefset_") &&
                          tableName.toLowerCase().contains("language")) {
                  this.LANGUAGES.add(tableName);
               } else if (tableName.toLowerCase()
                                   .startsWith("sct2_identifier_")) {}
               else if (tableName.toLowerCase()
                                 .startsWith("sct2_relationship_")) {
                  this.RELATIONSHIP = tableName;
               } else if (tableName.toLowerCase()
                                   .startsWith("sct2_statedrelationship_")) {
                  this.STATED_RELATIONSHIP = tableName;
               }

               final TableDefinition td = createTableDefinition(tableName,
                                                                fileReader.getHeader(),
                                                                fileReader.peekNextRow());

               this.tables_.put(tableName, td);

               if (!createdNew) {
                  // Only need to process this far to read the metadata about the DB
                  continue;
               }

               this.db.createTable(td);
               tableCount++;

               final int rowCount = this.db.loadDataIntoTable(td, fileReader);

               fileReader.close();

               // don't bother indexing small tables
               if (rowCount > 10000) {
                  final HashSet<String> colsToIndex = new HashSet<>();

                  colsToIndex.add("conceptId");
                  colsToIndex.add("referencedComponentId");
                  colsToIndex.add("sourceId");

                  for (final String s: fileReader.getHeader()) {
                     if (colsToIndex.contains(s)) {
                        try (Statement statement = this.db.getConnection().createStatement()) {
                           ConsoleUtil.println("Indexing " + tableName + " on " + s);

                           if (s.equals("referencedComponentId")) {
                              statement.execute("CREATE INDEX " + tableName + "_" + s + "_index ON " + tableName +
                                                " (" + s + ", refsetId)");
                           } else {
                              if (td.getColDataType("id") != null) {
                                 statement.execute("CREATE INDEX " + tableName + "_" + s + "_index ON " + tableName +
                                                   " (" + s + ", id)");
                              } else {
                                 statement.execute("CREATE INDEX " + tableName + "_" + s + "_index ON " + tableName +
                                                   " (" + s + ")");
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      ConsoleUtil.println("Processing DB loaded " + tableCount + " tables in " +
                          ((System.currentTimeMillis() - time) / 1000) + " seconds");

      if (tableCount == 0) {
         throw new RuntimeException("Failed to find tables in zip file!");
      }
   }

   /**
    * Transform concepts.
    *
    * @throws SQLException the SQL exception
    * @throws ParseException the parse exception
    */
   private void transformConcepts()
            throws SQLException, ParseException {
      ConsoleUtil.println("Converting concepts");

      final TableDefinition td       = this.tables_.get(this.CONCEPT);
      int                   conCount = 0;
      final PreparedStatement ps = this.db.getConnection()
                                           .prepareStatement("Select * from " + this.CONCEPT + " order by id");
      UUID            lastId = null;
      final ResultSet rs     = ps.executeQuery();

      while (rs.next()) {
         conCount++;

         Long sctID = null;
         UUID id, moduleId, definitionStatusId;

         if (td.getColDataType("ID")
               .isLong()) {
            sctID = rs.getLong("ID");
            id    = UuidT3Generator.fromSNOMED(sctID);
         } else {
            id = UUID.fromString(rs.getString("ID"));
         }

         this.consWithNoStatedRel.add(id);

         final long    time   = dateParse.parse(rs.getString("EFFECTIVETIME"))
                                         .getTime();
         final boolean active = rs.getBoolean("ACTIVE");

         moduleId = (td.getColDataType("MODULEID")
                       .isLong() ? UuidT3Generator.fromSNOMED(rs.getLong("MODULEID"))
                                 : UUID.fromString(rs.getString("MODULEID")));
         definitionStatusId = (td.getColDataType("DEFINITIONSTATUSID")
                                 .isLong() ? UuidT3Generator.fromSNOMED(rs.getLong("DEFINITIONSTATUSID"))
                                           : UUID.fromString(rs.getString("DEFINITIONSTATUSID")));

         TreeMap<Long, UUID> conDefStatus = this.conceptDefinitionStatusCache.get(id);

         if (conDefStatus == null) {
            conDefStatus = new TreeMap<>();
            this.conceptDefinitionStatusCache.put(id, conDefStatus);
         }

         final UUID oldValue = conDefStatus.put(time, definitionStatusId);

         if ((oldValue != null) &&!oldValue.equals(definitionStatusId)) {
            throw new RuntimeException("Unexpeted - multiple definition status values at the same time: " + sctID +
                                       " " + id + " " + definitionStatusId);
         }

         final ConceptChronology<? extends ConceptVersion<?>> con = super.importUtil.createConcept(id,
                                                                                                   time,
                                                                                                   active ? State.ACTIVE
               : State.INACTIVE,
                                                                                                   moduleId);

         if ((sctID != null) &&!id.equals(lastId)) {
            lastId = id;
            super.importUtil.addStaticStringAnnotation(ComponentReference.fromConcept(con),
                  sctID + "",
                  MetaData.SCTID.getPrimordialUuid(),
                  State.ACTIVE);
         }

         if (conCount % 1000 == 0) {
            ConsoleUtil.showProgress();
         }

         if (conCount % 75000 == 0) {
            ConsoleUtil.println("Processed " + conCount + " concepts...");
         }
      }

      ConsoleUtil.println("Converted " + conCount + " concepts");
   }

   /**
    * Transform descriptions.
    *
    * @throws SQLException the SQL exception
    * @throws ParseException the parse exception
    * @throws MojoExecutionException the mojo execution exception
    */
   private void transformDescriptions()
            throws SQLException, ParseException, MojoExecutionException {
      ConsoleUtil.println("Converting descriptions");

      for (final String DESCRIPTION: this.DESCRIPTIONS) {
         final TableDefinition descriptionTable = this.tables_.get(DESCRIPTION);
         final String          lang             = DESCRIPTION.split("_")[3];
         String                LANGUAGE         = null;

         for (final String s: this.LANGUAGES) {
            if (s.split("_")[3]
                 .equals(lang)) {
               LANGUAGE = s;
               break;
            }
         }

         if (LANGUAGE == null) {
            throw new MojoExecutionException("Failed to find the language table for the language: " + lang);
         }

         final TableDefinition acceptabilityTable = this.tables_.get(LANGUAGE);

         ConsoleUtil.println("Processing " + descriptionTable.getTableName() + ", " +
                             acceptabilityTable.getTableName());

         int descCount            = 0;
         int accCount             = 0;
         int noAcceptabilityCount = 0;
         final PreparedStatement ps = this.db.getConnection()
                                              .prepareStatement("Select * from " + DESCRIPTION +
                                                 " order by conceptId, id");
         final PreparedStatement ps2 = this.db.getConnection()
                                               .prepareStatement("Select * from " + LANGUAGE +
                                                  " where referencedComponentId = ? ");
         UUID            lastId = null;
         final ResultSet descRS = ps.executeQuery();

         while (descRS.next()) {
            descCount++;

            Long sctID = null;
            UUID id;

            if (descriptionTable.getColDataType("ID")
                                .isLong()) {
               sctID = descRS.getLong("ID");
               id    = UuidT3Generator.fromSNOMED(sctID);
            } else {
               id = UUID.fromString(descRS.getString("ID"));
            }

            final long    time   = dateParse.parse(descRS.getString("EFFECTIVETIME"))
                                            .getTime();
            final boolean active = descRS.getBoolean("ACTIVE");
            final UUID moduleId = (descriptionTable.getColDataType("MODULEID")
                                                   .isLong() ? UuidT3Generator.fromSNOMED(descRS.getLong("MODULEID"))
                  : UUID.fromString(descRS.getString("MODULEID")));
            final UUID conceptId = (descriptionTable.getColDataType("CONCEPTID")
                                                    .isLong() ? UuidT3Generator.fromSNOMED(descRS.getLong("CONCEPTID"))
                  : UUID.fromString(descRS.getString("CONCEPTID")));
            final String languageCode = descRS.getString("LANGUAGECODE");
            final UUID typeId = (descriptionTable.getColDataType("TYPEID")
                                                 .isLong() ? UuidT3Generator.fromSNOMED(descRS.getLong("TYPEID"))
                  : UUID.fromString(descRS.getString("TYPEID")));
            final String term = descRS.getString("TERM");
            final UUID caseSigId = (descriptionTable.getColDataType("CASESIGNIFICANCEID")
                                                    .isLong() ? UuidT3Generator.fromSNOMED(
                                                       descRS.getLong("CASESIGNIFICANCEID"))
                  : UUID.fromString(descRS.getString("CASESIGNIFICANCEID")));
            final SememeChronology<DescriptionSememe<?>> desc =
               super.importUtil.addDescription(ComponentReference.fromConcept(conceptId),
                                               id,
                                               term,
                                               DescriptionType.parse(typeId),
                                               null,
                                               null,
                                               caseSigId,
                                               LanguageMap.getConceptForLanguageCode(
                                                  LanguageCode.getLangCode(languageCode))
                                                     .getPrimordialUuid(),
                                               moduleId,
                                               null,
                                               active ? State.ACTIVE
                  : State.INACTIVE,
                                               time);

            // add SCTID if this is the first sighting
            if ((sctID != null) &&!id.equals(lastId)) {
               lastId = id;
               super.importUtil.addStaticStringAnnotation(ComponentReference.fromChronology(desc),
                     sctID + "",
                     MetaData.SCTID.getPrimordialUuid(),
                     State.ACTIVE);
            }

            ps2.clearParameters();

            if (acceptabilityTable.getColDataType("referencedComponentId")
                                  .isLong()) {
               if (sctID == null) {
                  throw new RuntimeException("type mismatch!");
               }

               ps2.setLong(1, sctID);
            } else {
               ps2.setString(1, id.toString());
            }

            final ResultSet langRS             = ps2.executeQuery();
            boolean         foundAcceptability = false;

            while (langRS.next()) {
               accCount++;
               foundAcceptability = true;

               final UUID    acceptID     = UUID.fromString(langRS.getString("id"));
               final long    acceptTime   = dateParse.parse(langRS.getString("EFFECTIVETIME"))
                                                     .getTime();
               final boolean acceptActive = langRS.getBoolean("ACTIVE");
               final UUID acceptModuleId = (acceptabilityTable.getColDataType("MODULEID")
                                                              .isLong() ? UuidT3Generator.fromSNOMED(
                                                                 langRS.getLong("MODULEID"))
                     : UUID.fromString(langRS.getString("MODULEID")));
               final UUID refsetId = (acceptabilityTable.getColDataType("refsetID")
                                                        .isLong() ? UuidT3Generator.fromSNOMED(
                                                           langRS.getLong("refsetID"))
                     : UUID.fromString(langRS.getString("refsetID")));
               final UUID acceptabilityId = (acceptabilityTable.getColDataType("acceptabilityId")
                                                               .isLong() ? UuidT3Generator.fromSNOMED(
                                                                  langRS.getLong("acceptabilityId"))
                     : UUID.fromString(langRS.getString("acceptabilityId")));
               boolean preferred;

               if (MetaData.ACCEPTABLE.getPrimordialUuid()
                                      .equals(acceptabilityId)) {
                  preferred = false;
               } else if (MetaData.PREFERRED.getPrimordialUuid()
                                            .equals(acceptabilityId)) {
                  preferred = true;
               } else {
                  throw new RuntimeException("Unexpected acceptibility: " + acceptabilityId);
               }

               super.importUtil.addDescriptionAcceptibility(ComponentReference.fromChronology(desc),
                     acceptID,
                     refsetId,
                     preferred,
                     acceptActive ? State.ACTIVE
                                  : State.INACTIVE,
                     acceptTime,
                     acceptModuleId);
            }

            if (!foundAcceptability) {
               noAcceptabilityCount++;

               // Might be better as "fine" or debug logging logging...
               // ConsoleUtil.printErrorln("No acceptibility found for: " + id + " " + sctID);
            }

            if (descCount % 1000 == 0) {
               ConsoleUtil.showProgress();
            }

            if (descCount % 75000 == 0) {
               ConsoleUtil.println("Processed " + descCount + " descriptions with " + accCount + " acceptabilities...");
            }
         }

         ConsoleUtil.println("Converted " + descCount + " descriptions. Descriptions with no acceptability: " +
                             noAcceptabilityCount);
      }
   }

   /**
    * Transform relationships.
    *
    * @param stated the stated
    * @throws SQLException the SQL exception
    */
   private void transformRelationships(boolean stated)
            throws SQLException {
      // TODO can ConsoleUtil use standard logging API instead?
      ConsoleUtil.println("Converting " + (stated ? "stated"
            : "inferred") + " relationships into graphs");

      final String                        table      = (stated ? this.STATED_RELATIONSHIP
            : this.RELATIONSHIP);
      final TableDefinition               td         = this.tables_.get(table);
      int                                 graphCount = 0;
      final Iterator<ArrayList<RelBatch>> rels       = getRelationships(table, td);
      UUID                                lastId     = null;

      while (rels.hasNext()) {
         // each Rel here will be for the same sourceId.
         graphCount++;

         final ArrayList<RelBatch>                   conRels           = rels.next();
         long                                        newestRelTime     = 0;
         final LogicalExpressionBuilder leb = Get.logicalExpressionBuilderService()
                                                 .getLogicalExpressionBuilder();
         final ArrayList<Assertion>                  assertions        = new ArrayList<>();
         final HashMap<String, ArrayList<Assertion>> groupedAssertions = new HashMap<>();

         // Each member of a RelBatch contains the same relBatch ID (so different versions of the same relBatch)
         for (final RelBatch rb: conRels) {
            if (!rb.isActiveNow()) {
               // TODO handle historical relationships
            } else {
               // TODO handle history - only loading latest for now.
               final Rel r = rb.getRels()
                               .last();

               if ((stated && r.characteristicTypeId.equals(MetaData.INFERRED.getPrimordialUuid())) ||
                     (!stated && r.characteristicTypeId.equals(MetaData.STATED.getPrimordialUuid()))) {
                  throw new RuntimeException("Unexpected - table type and characteristic type do not match!");
               }

               if (r.characteristicTypeId.equals(MetaData.INFERRED.getPrimordialUuid()) ||
                     r.characteristicTypeId.equals(MetaData.STATED.getPrimordialUuid())) {
                  if (r.effectiveTime > newestRelTime) {
                     newestRelTime = r.effectiveTime;
                  }

                  if (r.relGroup.trim()
                                .equals("0")) {
                     // Don't just check primordial, IS_A has multiple UUIDs
                     if (Arrays.stream(MetaData.IS_A.getUuids())
                               .anyMatch(uuid -> uuid.equals(r.typeId))) {
                        assertions.add(ConceptAssertion(Get.identifierService()
                                                           .getConceptSequenceForUuids(r.destinationId),
                                                        leb));
                     } else {
                        // TODO [graph] ask Keith about the never group stuff.
                        // TODO [graph] handle modifier?
                        // TODO [graph] handle sctid
                        // TODO [graph] handle id
                        // TODO [graph] maintain actual group numbers?
                        if (this.neverRoleGroupSet.contains(r.typeId)) {
                           assertions.add(SomeRole(Get.identifierService()
                                                      .getConceptSequenceForUuids(r.typeId),
                                                   ConceptAssertion(Get.identifierService()
                                                         .getConceptSequenceForUuids(r.destinationId),
                                                         leb)));
                        } else {
                           assertions.add(SomeRole(MetaData.ROLE_GROUP.getConceptSequence(),
                                                   And(SomeRole(Get.identifierService()
                                                         .getConceptSequenceForUuids(r.typeId),
                                                         ConceptAssertion(Get.identifierService()
                                                               .getConceptSequenceForUuids(r.destinationId),
                                                               leb)))));
                        }
                     }
                  } else {
                     ArrayList<Assertion> groupAssertions = groupedAssertions.get(r.relGroup.trim());

                     if (groupAssertions == null) {
                        groupAssertions = new ArrayList<>();
                        groupedAssertions.put(r.relGroup.trim(), groupAssertions);
                     }

                     groupAssertions.add(SomeRole(Get.identifierService()
                                                     .getConceptSequenceForUuids(r.typeId),
                                                  ConceptAssertion(Get.identifierService()
                                                        .getConceptSequenceForUuids(r.destinationId),
                                                        leb)));
                  }
               } else {
                  // kick it over into an association bucket
                  // TODO should I toss these when processing inferred?
                  final SememeChronology<DynamicSememe<?>> assn =
                     super.importUtil.addAssociation(ComponentReference.fromConcept(r.sourceId),
                                                     r.id,
                                                     r.destinationId,
                                                     r.typeId,
                                                     r.isActive ? State.ACTIVE
                        : State.INACTIVE,
                                                     r.effectiveTime,
                                                     r.moduleId);

                  // TODO put on modifier, group

                  if ((r.sctID != null) &&!r.id.equals(lastId)) {
                     super.importUtil.addStaticStringAnnotation(ComponentReference.fromChronology(assn,
                           () -> "Association"),
                           r.sctID + "",
                           MetaData.SCTID.getPrimordialUuid(),
                           State.ACTIVE);
                  }
               }

               lastId = r.id;
            }
         }

         // handle relationship groups
         for (final ArrayList<Assertion> groupAssertions: groupedAssertions.values()) {
            assertions.add(SomeRole(MetaData.ROLE_GROUP.getConceptSequence(),
                                    And(groupAssertions.toArray(new Assertion[groupAssertions.size()]))));
         }

         if (assertions.size() > 0) {
            Boolean                   defined      = null;
            final TreeMap<Long, UUID> conDefStatus = this.conceptDefinitionStatusCache.get(conRels.get(0)
                                                                                                  .getSourceId());

            if (conDefStatus == null) {
               // Try Frills - in the case of US Extension, we should have SCT loaded, pull from that.
               // Can definition status vary between stated and inferred?  Just read stated for now.
               final Optional<Boolean> sctDefined =
                  Frills.isConceptFullyDefined(ComponentReference.fromConcept(conRels.get(0)
                                                                                     .getSourceId())
                                                                 .getNid(),
                                               true);

               if (sctDefined.isPresent()) {
                  defined = sctDefined.get();
               } else {
                  final RelBatch      relBatch = conRels.get(0);
                  final Rel           rel      = relBatch.getRels()
                                                         .first();
                  final StringBuilder builder  = new StringBuilder();

                  builder.append(Get.conceptDescriptionText(Get.identifierService()
                        .getConceptSequenceForUuids(rel.sourceId)));
                  builder.append("|");
                  builder.append(Get.conceptDescriptionText(Get.identifierService()
                        .getConceptSequenceForUuids(rel.typeId)));
                  builder.append("|");
                  builder.append(Get.conceptDescriptionText(Get.identifierService()
                        .getConceptSequenceForUuids(rel.destinationId)));
                  ConsoleUtil.printErrorln("No definition status found for: " + conRels.get(0) + "\n" +
                                           builder.toString());
               }
            } else {
               if (conDefStatus.lastEntry()
                               .getValue()
                               .equals(SUFFICIENTLY_DEFINED)) {
                  defined = true;
               } else if (conDefStatus.lastEntry()
                                      .getValue()
                                      .equals(NECESSARY_BUT_NOT_SUFFICIENT)) {
                  defined = false;
               } else {
                  throw new RuntimeException("Unexpected concept definition status: " + conDefStatus.lastEntry());
               }
            }

            if (defined != null) {
               if (defined) {
                  SufficientSet(And(assertions.toArray(new Assertion[assertions.size()])));
               } else {
                  NecessarySet(And(assertions.toArray(new Assertion[assertions.size()])));
               }

               final LogicalExpression le = leb.build();

               if (le.isMeaningful()) {
                  if (newestRelTime == 0) {
                     throw new RuntimeException("Time sort failure!");
                  }

                  // TODO [graph] what if the modules are different across the graph rels?
                  super.importUtil.addRelationshipGraph(ComponentReference.fromConcept(conRels.get(0)
                        .getSourceId()),
                        le,
                        stated,
                        newestRelTime,
                        conRels.get(0)
                               .getRels()
                               .first().moduleId);

                  if (!stated && this.consWithNoStatedRel.contains(conRels.get(0).getSourceId())) {
                     // substitute inferred expression, as early SNOMED stated expressions where lost.
                     super.importUtil.addRelationshipGraph(ComponentReference.fromConcept(conRels.get(0)
                           .getSourceId()),
                           le,
                           true,
                           newestRelTime,
                           conRels.get(0)
                                  .getRels()
                                  .first().moduleId);
                  }

                  this.consWithNoStatedRel.remove(conRels.get(0)
                        .getSourceId());
               } else {
                  ConsoleUtil.printErrorln("expression not meaningful?");
               }
            }
         } else {
            // TODO [graph] ask Keith about these cases where no associations get generated.
         }

         if (graphCount % 1000 == 0) {
            ConsoleUtil.showProgress();
         }

         if (graphCount % 75000 == 0) {
            ConsoleUtil.println("Processed " + graphCount + " relationship graphs...");
         }
      }

      ConsoleUtil.println("Created " + graphCount + " graphs");
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set default value from SNOMED_CT_CORE_MODULE.
    *
    * @param conceptProxy the new default value from SNOMED_CT_CORE_MODULE
    */
   public void setModuleUUID(MavenConceptProxy conceptProxy) {
      this.moduleUUID = conceptProxy;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * This will return batches of relationships, each item the iterator returns will be all of the relationships
    * for a particular source concepts, while each RelBatch within the list will be all versions of a particular relationship.
    *
    * @param table the table
    * @param td the td
    * @return the relationships
    * @throws SQLException the SQL exception
    */
   private Iterator<ArrayList<RelBatch>> getRelationships(String table, TableDefinition td)
            throws SQLException {
      final PreparedStatement ps = this.db.getConnection()
                                           .prepareStatement("Select * from " + table + " order by sourceid, id");
      final ResultSet                     rs   = ps.executeQuery();
      final Iterator<ArrayList<RelBatch>> iter = new Iterator<ArrayList<RelBatch>>() {
         RelBatch            relBatchWorking      = null;
         ArrayList<RelBatch> conceptRelsWorking   = new ArrayList<>();
         ArrayList<RelBatch> conceptRelsNextReady = null;
         @Override
         public boolean hasNext() {
            if (this.conceptRelsNextReady == null) {
               read();
            }

            if (this.conceptRelsNextReady == null) {
               try {
                  rs.close();
               } catch (final SQLException e) {
                  // noop
               }
            }

            return this.conceptRelsNextReady != null;
         }
         @Override
         public ArrayList<RelBatch> next() {
            if (this.conceptRelsNextReady == null) {
               read();
            }

            final ArrayList<RelBatch> temp = this.conceptRelsNextReady;

            this.conceptRelsNextReady = null;
            return temp;
         }
         private void read() {
            try {
               while ((this.conceptRelsNextReady == null) && rs.next()) {
                  final Rel r = new Rel(rs, td);

                  if (this.relBatchWorking == null) {
                     this.relBatchWorking = new RelBatch(r);
                  } else if (this.relBatchWorking.getBatchId()
                                                 .equals(r.id)) {
                     this.relBatchWorking.addRel(r);
                  } else  // different batchId than previous - need a new RelBatch.  Move last relBatch into conceptRels.
                  {
                     if ((this.conceptRelsWorking.size() > 0) &&
                           !this.conceptRelsWorking.get(0).getSourceId().equals(this.relBatchWorking.getSourceId())) {
                        this.conceptRelsNextReady = this.conceptRelsWorking;
                        this.conceptRelsWorking   = new ArrayList<>();
                     }

                     this.conceptRelsWorking.add(this.relBatchWorking);

                     // Put this relBatch into a new batch.
                     this.relBatchWorking = new RelBatch(r);
                  }
               }
            } catch (SQLException | ParseException e) {
               throw new RuntimeException(e);
            }

            if (this.conceptRelsNextReady != null) {
               return;
            }

            if ((this.conceptRelsWorking.size() > 0) &&
                  !this.conceptRelsWorking.get(0).getSourceId().equals(this.relBatchWorking.getSourceId())) {
               this.conceptRelsNextReady = this.conceptRelsWorking;
               this.conceptRelsWorking   = new ArrayList<>();
               return;
            }

            // If we get here, the only thing left is the last relBatch.
            if (this.relBatchWorking != null) {
               this.conceptRelsWorking.add(this.relBatchWorking);
               this.conceptRelsNextReady = this.conceptRelsWorking;
               this.relBatchWorking      = null;
               this.conceptRelsWorking   = new ArrayList<>();
            }
         }
      };

      return iter;
   }
}

