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



package sh.isaac.convert.rxnorm.standard;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.StringSememe;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.convert.rxnorm.propertyTypes.PT_Annotations;
import sh.isaac.convert.rxnorm.propertyTypes.ValuePropertyPairWithSAB;
import sh.isaac.converters.sharedUtils.ComponentReference;
import sh.isaac.converters.sharedUtils.ConsoleUtil;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility.DescriptionType;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Annotations;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Associations;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Descriptions;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Relations;
import sh.isaac.converters.sharedUtils.propertyTypes.Property;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyAssociation;
import sh.isaac.converters.sharedUtils.propertyTypes.PropertyType;
import sh.isaac.converters.sharedUtils.propertyTypes.ValuePropertyPair;
import sh.isaac.converters.sharedUtils.sql.TableDefinition;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.converters.sharedUtils.umlsUtils.AbbreviationExpansion;
import sh.isaac.converters.sharedUtils.umlsUtils.RRFDatabaseHandle;
import sh.isaac.converters.sharedUtils.umlsUtils.Relationship;
import sh.isaac.converters.sharedUtils.umlsUtils.UMLSFileReader;
import sh.isaac.converters.sharedUtils.umlsUtils.ValuePropertyPairWithAttributes;
import sh.isaac.converters.sharedUtils.umlsUtils.propertyTypes.PT_Descriptions;
import sh.isaac.converters.sharedUtils.umlsUtils.propertyTypes.PT_Refsets;
import sh.isaac.converters.sharedUtils.umlsUtils.propertyTypes.PT_Relationship_Metadata;
import sh.isaac.converters.sharedUtils.umlsUtils.propertyTypes.PT_SAB_Metadata;
import sh.isaac.converters.sharedUtils.umlsUtils.rrf.REL;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.rxnorm.rrf.RXNCONSO;
import sh.isaac.rxnorm.rrf.RXNSAT;
import sh.isaac.api.component.sememe.version.DescriptionVersion;

//~--- classes ----------------------------------------------------------------

/**
 * Loader code to convert RxNorm into the workbench.
 */
@Mojo(
   name         = "convert-rxnorm-to-ibdf",
   defaultPhase = LifecyclePhase.PROCESS_SOURCES
)
public class RxNormMojo
        extends ConverterBaseMojo {
   /**
    * The Constant rxNormName.
    */
   public static final String rxNormName = "RxNorm";

   /**
    * The Constant cpcRefsetConceptKey.
    */
   public static final String cpcRefsetConceptKey = "Current Prescribable Content";

   //~--- fields --------------------------------------------------------------

   /**
    * The table prefix.
    */
   private final String tablePrefix = "RXN";

   /**
    * The sct sab.
    */
   private final String sctSab = "SNOMEDCT_US";

   /**
    * The name to relationship map.
    */
   private HashMap<String, Relationship> nameToRel = new HashMap<>();

   /**
    * The semantic types.
    */
   private final HashMap<String, UUID> semanticTypes = new HashMap<>();

   /**
    * The loaded relationships.
    */
   private final HashSet<UUID> loadedRels = new HashSet<>();

   /**
    * The skipped relationships.
    */
   private final HashSet<UUID> skippedRels = new HashSet<>();

   /**
    * The map to isa.
    */
   private final HashMap<String, Boolean> mapToIsa =
      new HashMap<>();  // FSN, true or false - true for rel only, false for a rel and association representation

   /**
    * The sct id to UUID.
    */
   private final HashMap<Long, UUID> sctIdToUUID = new HashMap<>();  // A map of real (found) SCTIDs to their concept UUID

   /**
    * The cui to SCT ID.
    */
   private final HashMap<String, Long> cuiToSCTID =
      new HashMap<>();  // Map CUI to SCTID for the real sctIds to UUIDs found above

   /**
    * The skipped rel for not matching CUI filter.
    */
   private final AtomicInteger skippedRelForNotMatchingCUIFilter = new AtomicInteger();

   /**
    * The date parse.
    */

   // Format to parse 01/28/2010
   private final SimpleDateFormat dateParse = new SimpleDateFormat("MM/dd/yyyy");

   /**
    * The s types.
    */
   private HashMap<String, UUID> sTypes;

   /**
    * The suppress.
    */
   private HashMap<String, UUID> suppress;

   /**
    * The source restriction levels.
    */
   private HashMap<String, UUID> sourceRestrictionLevels;

   /**
    * The pt UMLS attributes.
    */
   private PropertyType ptUMLSAttributes;

   /**
    * The pt SABs.
    */
   private PropertyType ptSABs;

   /**
    * The pt relationship metadata.
    */
   private PropertyType ptRelationshipMetadata;

   /**
    * The pt descriptions.
    */
   private PropertyType ptDescriptions;  // TODO get SAB types for these, annotate

   /**
    * The pt associations.
    */
   private BPT_Associations ptAssociations;  // TODO get SAB types for these, annotate

   /**
    * The pt relationships.
    */
   private BPT_Relations ptRelationships;  // TODO get SAB types for these, annotate

   /**
    * The pt term attributes.
    */
   private PropertyType ptTermAttributes;  // TODO get SAB types for these, annotate

   /**
    * The pt refsets.
    */
   private PT_Refsets ptRefsets;

   /**
    * The import util.
    */
   private IBDFCreationUtility importUtil;

   /**
    * The db.
    */
   private RRFDatabaseHandle db;

   /**
    * The meta data root.
    */
   private ComponentReference metaDataRoot;

   /**
    * The abbreviation expansions.
    */
   private HashMap<String, AbbreviationExpansion> abbreviationExpansions;

   /**
    * The all CUI refset concept.
    */
   private ComponentReference allCUIRefsetConcept;

   /**
    * The cpc refset concept.
    */
   private ComponentReference cpcRefsetConcept;

   /**
    * The has TTY type.
    */
   private PreparedStatement semanticTypeStatement, descSat, cuiRelStatementForward, cuiRelStatementBackward,
                             satRelStatement, hasTTYType;

   /**
    * The allowed CU is for SA bs.
    */
   private HashSet<String> allowedCUIsForSABs;

   /**
    * An optional list of TTY types which should be included. If left blank, we create concepts from all CUI's that are
    * in the SAB RxNorm. If provided, we only create concepts where the RxCUI has an entry with a TTY that matches one
    * of the TTY's provided here
    */
   @Parameter(required = false)
   protected List<String> ttyRestriction;

   /**
    * An optional list of SABs which should be included. We always include the SAB RXNORM. Use this parameter to specify
    * others to include. If SNOMEDCT_US is included, then a snomed CT ibdf file must be present - snomed CT is not
    * loaded from RxNorm, but rather, linked to the provided SCT IBDF file.
    */
   @Parameter(required = false)
   protected List<String> sabsToInclude;

   /**
    * The link snomed CT.
    */
   private boolean linkSnomedCT;

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
         init();
         this.allCUIRefsetConcept = ComponentReference.fromConcept(
             this.ptRefsets.getProperty(this.ptRefsets.CUI_CONCEPTS.getSourcePropertyNameFSN())
                           .getUUID());
         this.cpcRefsetConcept = ComponentReference.fromConcept(
             this.ptRefsets.getProperty(cpcRefsetConceptKey)
                           .getUUID());
         this.semanticTypeStatement = this.db.getConnection()
               .prepareStatement("select TUI, ATUI, CVF from RXNSTY where RXCUI = ?");

         // we always grab the description type NDC if present, even if NDC doesn't come from a SAB we are including.
         this.descSat = this.db.getConnection()
                               .prepareStatement(
                                   "select * from RXNSAT where RXCUI = ? and RXAUI = ? and (" + createSabQueryPart(
                                       "",
                                       false) + " or ATN='NDC')");

         // UMLS and RXNORM do different things with rels - UMLS never has null CUI's, while RxNorm always has null CUI's (when AUI is specified)
         // Also need to join back to MRCONSO to make sure that the target concept is one that we will load with the SAB filter in place.
         this.cuiRelStatementForward = this.db.getConnection()
               .prepareStatement(
                   "SELECT distinct r.RXCUI1, r.RXAUI1, r.STYPE1, r.REL, r.RXCUI2, r.RXAUI2, r.STYPE2, " +
                   "r.RELA, r.RUI, r.SRUI, r.SAB, r.SL, r.DIR, r.RG, r.SUPPRESS, r.CVF from RXNREL as r, RXNCONSO " +
                   "WHERE RXCUI2 = ? and RXAUI2 is null and " + createSabQueryPart(
                       "r.",
                       this.linkSnomedCT) + " and r.RXCUI1 = RXNCONSO.RXCUI and " + createSabQueryPart(
                           "RXNCONSO.",
                           this.linkSnomedCT));
         this.cuiRelStatementBackward = this.db.getConnection()
               .prepareStatement(
                   "SELECT distinct r.RXCUI1, r.RXAUI1, r.STYPE1, r.REL, r.RXCUI2, r.RXAUI2, r.STYPE2, " +
                   "r.RELA, r.RUI, r.SRUI, r.SAB, r.SL, r.DIR, r.RG, r.SUPPRESS, r.CVF from RXNREL as r, RXNCONSO " +
                   "WHERE RXCUI1 = ? and RXAUI1 is null and " + createSabQueryPart(
                       "r.",
                       this.linkSnomedCT) + " and r.RXCUI2 = RXNCONSO.RXCUI and " + createSabQueryPart(
                           "RXNCONSO.",
                           this.linkSnomedCT));

         int                       cuiCounter = 0;
         final HashSet<String>     skippedCUIForNotMatchingCUIFilter;
         final ArrayList<RXNCONSO> conceptData;

         try (Statement statement = this.db.getConnection().createStatement()) {
            final StringBuilder ttyRestrictionQuery = new StringBuilder();

            if ((this.ttyRestriction != null) && (this.ttyRestriction.size() > 0)) {
               ttyRestrictionQuery.append(" and (");
               this.ttyRestriction.stream()
                                  .map(
                                      (s) -> {
                                         ttyRestrictionQuery.append("TTY = '");
                                         ttyRestrictionQuery.append(s);
                                         return s;
                                      })
                                  .forEachOrdered(
                                      (_item) -> {
                                         ttyRestrictionQuery.append("' or ");
                                      });
               ttyRestrictionQuery.setLength(ttyRestrictionQuery.length() - " or ".length());
               ttyRestrictionQuery.append(")");
            }

            this.allowedCUIsForSABs = new HashSet<>();

            try (ResultSet rs = statement.executeQuery(
                                    "select RXCUI from RXNCONSO where " + createSabQueryPart(
                                        "",
                                        this.linkSnomedCT) + " " + ttyRestrictionQuery);) {
               while (rs.next()) {
                  this.allowedCUIsForSABs.add(rs.getString("RXCUI"));
               }
            }

            try (ResultSet rs = statement.executeQuery(
                                    "select RXCUI, LAT, RXAUI, SAUI, SCUI, SAB, TTY, CODE, STR, SUPPRESS, CVF from RXNCONSO " +
                                    "where " + createSabQueryPart(
                                        "",
                                        this.linkSnomedCT) + " order by RXCUI")) {
               skippedCUIForNotMatchingCUIFilter = new HashSet<>();
               conceptData                       = new ArrayList<>();

               while (rs.next()) {
                  final RXNCONSO current = new RXNCONSO(rs);

                  if (!this.allowedCUIsForSABs.contains(current.rxcui)) {
                     skippedCUIForNotMatchingCUIFilter.add(current.rxcui);
                     continue;
                  }

                  if ((conceptData.size() > 0) &&!conceptData.get(0).rxcui.equals(current.rxcui)) {
                     processCUIRows(conceptData);

                     if (cuiCounter % 100 == 0) {
                        ConsoleUtil.showProgress();
                     }

                     cuiCounter++;

                     if (cuiCounter % 10000 == 0) {
                        ConsoleUtil.println(
                            "Processed " + cuiCounter + " CUIs creating " +
                            this.importUtil.getLoadStats().getConceptCount() + " concepts");
                     }

                     conceptData.clear();
                  }

                  conceptData.add(current);
               }
            }
         }

         // process last
         processCUIRows(conceptData);
         ConsoleUtil.println(
             "Processed " + cuiCounter + " CUIs creating " + this.importUtil.getLoadStats().getConceptCount() +
             " concepts");
         ConsoleUtil.println(
             "Skipped " + skippedCUIForNotMatchingCUIFilter.size() + " concepts for not containing the desired TTY");
         ConsoleUtil.println(
             "Skipped " + this.skippedRelForNotMatchingCUIFilter +
             " relationships for linking to a concept we didn't include");
         this.semanticTypeStatement.close();
         this.descSat.close();
         this.cuiRelStatementForward.close();
         this.cuiRelStatementBackward.close();
         finish();
      } catch (final Exception e) {
         throw new MojoExecutionException("Failure during conversion", e);
      } finally {
         if (this.db != null) {
            try {
               this.db.shutdown();
            } catch (final SQLException e) {
               throw new RuntimeException(e);
            }
         }
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
      final RxNormMojo mojo = new RxNormMojo();

      mojo.outputDirectory                = new File("../rxnorm-ibdf/rxnorm/target");
      mojo.inputFileLocation              = new File("../rxnorm-ibdf/rxnorm/target/generated-resources/src");
      mojo.converterVersion               = "foo";
      mojo.converterOutputArtifactVersion = "bar";
      mojo.converterSourceArtifactVersion = "foooo";
      mojo.converterOutputArtifactId      = "rxnorm-ibdf";
      mojo.execute();
   }

   /**
    * Adds the relationships.
    *
    * @param concept the concept
    * @param relationships the relationships
    * @return the array list
    * @throws SQLException the SQL exception
    * @throws PropertyVetoException the property veto exception
    */
   private ArrayList<UUID> addRelationships(ComponentReference concept,
         List<REL> relationships)
            throws SQLException,
                   PropertyVetoException {
      final ArrayList<UUID> parents = new ArrayList<>();

      for (final REL relationship: relationships) {
         relationship.setSourceUUID(concept.getPrimordialUuid());

         if (relationship.getSourceAUI() == null) {
            if (this.cuiToSCTID.get(relationship.getTargetCUI()) != null) {
               if (this.cuiToSCTID.get(relationship.getSourceCUI()) != null) {
                  // Both source and target are concepts we are linking from SCT.  Don't load the rell.
                  continue;
               }

               // map to existing target SCT concept
               relationship.setTargetUUID(this.sctIdToUUID.get(this.cuiToSCTID.get(relationship.getTargetCUI())));
            } else {
               // must be a concept we are creating
               relationship.setTargetUUID(createCUIConceptUUID(relationship.getTargetCUI()));
            }
         } else {
            throw new RuntimeException("don't yet handle AUI associations");

//          relationship.setTargetUUID(createCuiSabCodeConceptUUID(relationship.getRxNormTargetCUI(), 
//                          relationship.getTargetSAB(), relationship.getTargetCode()));
         }

         // We currently don't check the properties on the (duplicate) inverse rels to make sure they are all present - we assume that they
         // created the inverse relationships as an exact copy of the primary rel direction.  So, just checking the first rel from our dupe list is good enough
         if (isRelPrimary(relationship.getRel(), relationship.getRela())) {
            // This can happen when the reverse of the rel equals the rel... sib/sib
            if (relCheckIsRelLoaded(relationship)) {
               continue;
            }

            final Property relTypeAsRel = this.ptRelationships.getProperty(
                                              ((relationship.getRela() == null) ? relationship.getRel()
                  : relationship.getRela()));
            final PropertyAssociation relTypeAsAssn = (PropertyAssociation) this.ptAssociations.getProperty(
                                                          ((relationship.getRela() == null) ? relationship.getRel()
                  : relationship.getRela()));
            ComponentReference r;

            if (relTypeAsRel != null) {
               parents.add(relationship.getTargetUUID());
               continue;
            } else if (relTypeAsAssn != null) {
               r = ComponentReference.fromChronology(
                   this.importUtil.addAssociation(
                       concept,
                       ((relationship.getRui() != null) ? ConverterUUID.createNamespaceUUIDFromString(
                           "RUI:" + relationship.getRui())
                     : null),
                       relationship.getTargetUUID(),
                       relTypeAsAssn.getUUID(),
                       State.ACTIVE,
                       null,
                       null),
                   () -> "Association");
            } else {
               throw new RuntimeException("Unexpected rel handling");
            }

            // Add the annotations
            final HashSet<String> addedRUIs = new HashSet<>();

            if (StringUtils.isNotBlank(relationship.getRela()))  // we already used rela - annotate with rel.
            {
               Property genericType = (this.ptAssociations.getProperty(
                                          relationship.getRel()) == null) ? this.ptRelationships.getProperty(
                                              relationship.getRel())
                     : this.ptAssociations.getProperty(relationship.getRel());
               boolean reversed = false;

               if ((genericType == null) && relationship.getRela().equals("mapped_from")) {
                  // This is to handle non-sensical data in UMLS... they have no consistency in the generic rel they assign - sometimes RB, sometimes RN.
                  // reverse it - currently, only an issue on 'mapped_from' rels - as the code in Relationship.java has some exceptions for this type.
                  genericType = (this.ptAssociations.getProperty(
                      reverseRel(
                          relationship.getRel())) == null) ? this.ptRelationships.getProperty(
                              reverseRel(relationship.getRel()))
                        : this.ptAssociations.getProperty(reverseRel(relationship.getRel()));
                  reversed = true;
               }

               this.importUtil.addUUIDAnnotation(
                   r,
                   genericType.getUUID(),
                   this.ptUMLSAttributes.getProperty(reversed ? "Generic rel type (inverse)"
                     : "Generic rel type")
                                        .getUUID());
            }

            if (StringUtils.isNotBlank(relationship.getRui())) {
               if (!addedRUIs.contains(relationship.getRui())) {
                  this.importUtil.addStringAnnotation(
                      r,
                      relationship.getRui(),
                      this.ptUMLSAttributes.getProperty("RUI")
                                           .getUUID(),
                      State.ACTIVE);
                  addedRUIs.add(relationship.getRui());
                  this.satRelStatement.clearParameters();
                  this.satRelStatement.setString(1, relationship.getRui());

                  final ArrayList<RXNSAT> satData;

                  try (ResultSet nestedRels = this.satRelStatement.executeQuery()) {
                     satData = new ArrayList<>();

                     while (nestedRels.next()) {
                        satData.add(new RXNSAT(nestedRels));
                     }
                  }

                  processSAT(r, satData, null, relationship.getSab(), null);
               }
            }

            if (StringUtils.isNotBlank(relationship.getRg())) {
               this.importUtil.addStringAnnotation(
                   r,
                   relationship.getRg(),
                   this.ptUMLSAttributes.getProperty("RG")
                                        .getUUID(),
                   State.ACTIVE);
            }

            if (StringUtils.isNotBlank(relationship.getDir())) {
               this.importUtil.addStringAnnotation(
                   r,
                   relationship.getDir(),
                   this.ptUMLSAttributes.getProperty("DIR")
                                        .getUUID(),
                   State.ACTIVE);
            }

            if (StringUtils.isNotBlank(relationship.getSuppress())) {
               this.importUtil.addUUIDAnnotation(
                   r,
                   this.suppress.get(relationship.getSuppress()),
                   this.ptUMLSAttributes.getProperty("SUPPRESS")
                                        .getUUID());
            }

            if (StringUtils.isNotBlank(relationship.getCvf())) {
               if (relationship.getCvf()
                               .equals("4096")) {
                  this.importUtil.addRefsetMembership(r, this.cpcRefsetConcept.getPrimordialUuid(), State.ACTIVE, null);
               } else {
                  throw new RuntimeException("Unexpected value in RXNSAT cvf column '" + relationship.getCvf() + "'");
               }
            }

            relCheckLoadedRel(relationship);
         } else {
            if (this.cuiToSCTID.containsKey(relationship.getSourceCUI())) {
               // this is telling us there was a relationship from an SCT concept, to a RXNorm concept, but because we are
               // not processing sct concept CUIs, we will never process this one in the forward direction.
               // For now, don't put it in the skip list.
               // Perhaps, in the future, we create a stub SCT concept, and create this association to the RxNorm concept
               // but not now.
            } else {
               relCheckSkippedRel(relationship);
            }
         }
      }

      return parents;
   }

   /**
    * Check relationships.
    */
   private void checkRelationships() {
      // if the inverse relationships all worked properly, skipped should be empty when loaded is subtracted from it.
      this.loadedRels.forEach(
          (uuid) -> {
             this.skippedRels.remove(uuid);
          });

      if (this.skippedRels.size() > 0) {
         ConsoleUtil.printErrorln(
             "Relationship design error - " + this.skippedRels.size() + " were skipped that should have been loaded");
      } else {
         ConsoleUtil.println("Yea! - no missing relationships!");
      }
   }

   /**
    * Clear target files.
    */
   private void clearTargetFiles() {
      new File(this.outputDirectory, "RxNormUUIDDebugMap.txt").delete();
      new File(this.outputDirectory, "ConsoleOutput.txt").delete();
      new File(this.outputDirectory, "RRF.jbin").delete();
   }

   /**
    * Creates the CUI concept UUID.
    *
    * @param cui the cui
    * @return the uuid
    */
   private UUID createCUIConceptUUID(String cui) {
      return ConverterUUID.createNamespaceUUIDFromString("CUI:" + cui, true);
   }

   /**
    * Creates the sab query part.
    *
    * @param tablePrefix the table prefix
    * @param includeSCT the include SCT
    * @return the string
    */
   private String createSabQueryPart(String tablePrefix, boolean includeSCT) {
      final StringBuffer sb = new StringBuffer();

      sb.append("(");
      this.sabsToInclude.forEach(
          (s) -> {
             sb.append(tablePrefix)
               .append("SAB='")
               .append(s)
               .append("' OR ");
          });

      if (includeSCT) {
         sb.append(tablePrefix)
           .append("SAB='" + this.sctSab + "' OR ");
      }

      sb.setLength(sb.length() - 4);
      sb.append(")");
      return sb.toString();
   }

   /**
    * Finish.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws SQLException the SQL exception
    */
   private void finish()
            throws IOException, SQLException {
      checkRelationships();
      this.satRelStatement.close();
      this.hasTTYType.close();
      ConsoleUtil.println("Load Statistics");
      this.importUtil.getLoadStats()
                     .getSummary()
                     .forEach(
                         (s) -> {
                            ConsoleUtil.println(s);
                         });

      // this could be removed from final release. Just added to help debug editor problems.
      ConsoleUtil.println("Dumping UUID Debug File");
      ConverterUUID.dump(this.outputDirectory, "RxNormUUID");
      this.importUtil.shutdown();
      ConsoleUtil.writeOutputToFile(new File(this.outputDirectory, "ConsoleOutput.txt").toPath());
   }

   /**
    * If sabList is null or empty, no sab filtering is done.
    *
    * @throws Exception the exception
    */
   private void init()
            throws Exception {
      clearTargetFiles();

      final String           fileNameDatePortion = loadDatabase();
      final SimpleDateFormat sdf                 = new SimpleDateFormat("MMddyyyy");
      final long             defaultTime         = sdf.parse(fileNameDatePortion)
                                                      .getTime();

      this.abbreviationExpansions = AbbreviationExpansion.load(
          getClass().getResourceAsStream("/RxNormAbbreviationsExpansions.txt"));
      this.mapToIsa.put("isa", false);
      this.mapToIsa.put("inverse_isa", false);

      // not translating this one to isa for now
      // mapToIsa.add("CHD");
      this.mapToIsa.put("tradename_of", false);
      this.mapToIsa.put("has_tradename", false);

      // Cleanup the sabsToInclude list
      final HashSet<String> temp = new HashSet<>();

      if (this.sabsToInclude != null) {
         this.sabsToInclude.forEach(
             (s) -> {
                temp.add(s.toUpperCase());
             });
      }

      temp.add("RXNORM");

      if (temp.contains(this.sctSab)) {
         this.linkSnomedCT = true;
         temp.remove(this.sctSab);
      } else {
         this.linkSnomedCT = false;
      }

      this.sabsToInclude = new ArrayList<>();
      this.sabsToInclude.addAll(temp);
      new File(this.inputFileLocation, "ibdf").listFiles(
          (FileFilter) pathname -> {
                          return RxNormMojo.this.linkSnomedCT &&
                                 pathname.isFile() &&
                                 pathname.getName().toLowerCase().endsWith(".ibdf");
                       });
      this.importUtil = new IBDFCreationUtility(
          Optional.empty(),
          Optional.of(MetaData.RXNORM_MODULES____ISAAC),
          this.outputDirectory,
          this.converterOutputArtifactId,
          this.converterOutputArtifactVersion,
          this.converterOutputArtifactClassifier,
          false,
          defaultTime);
      this.metaDataRoot = ComponentReference.fromConcept(
          this.importUtil.createConcept(
              "RxNorm Metadata" + IBDFCreationUtility.METADATA_SEMANTIC_TAG,
              true,
              MetaData.SOLOR_CONTENT_METADATA____ISAAC.getPrimordialUuid()));
      loadMetaData();
      this.importUtil.loadTerminologyMetadataAttributes(
          this.metaDataRoot,
          this.converterSourceArtifactVersion,
          Optional.of(fileNameDatePortion),
          this.converterOutputArtifactVersion,
          Optional.ofNullable(this.converterOutputArtifactClassifier),
          this.converterVersion);
      ConsoleUtil.println("Metadata Statistics");
      this.importUtil.getLoadStats()
                     .getSummary()
                     .forEach(
                         (s) -> {
                            ConsoleUtil.println(s);
                         });
      this.importUtil.clearLoadStats();
      this.satRelStatement = this.db.getConnection()
                                    .prepareStatement(
                                        "select * from " + this.tablePrefix + "SAT where RXAUI" +
                                        "= ? and STYPE='RUI' and " + createSabQueryPart(
                                              "",
                                                    this.linkSnomedCT));
      this.hasTTYType = this.db.getConnection()
                               .prepareStatement(
                                   "select count (*) as count from RXNCONSO where rxcui=? and TTY=? and " +
                                   createSabQueryPart(
                                       "",
                                       this.linkSnomedCT));

      if (this.linkSnomedCT) {
         prepareSCTMaps();
      }
   }

   /**
    * Returns the date portion of the file name - so from 'RxNorm_full_09022014.zip' it returns 09022014
    *
    * @return the string
    * @throws Exception the exception
    */
   private String loadDatabase()
            throws Exception {
      // Set up the DB for loading the temp data
      String toReturn = null;

      // Read the RRF file directly from the source zip file - need to find the zip first, to get the date out of the file name.
      ZipFile zf = null;

      for (final File f: this.inputFileLocation.listFiles()) {
         if (f.getName().toLowerCase().startsWith("rxnorm_full_") && f.getName().toLowerCase().endsWith(".zip")) {
            zf       = new ZipFile(f);
            toReturn = f.getName()
                        .substring("rxnorm_full_".length());
            toReturn = toReturn.substring(0, toReturn.length() - 4);
            break;
         }
      }

      if (zf == null) {
         throw new MojoExecutionException("Can't find source zip file");
      }

      this.db = new RRFDatabaseHandle();

      final File    dbFile     = new File(this.outputDirectory, "rrfDB.h2.db");
      final boolean createdNew = this.db.createOrOpenDatabase(new File(this.outputDirectory, "rrfDB"));

      if (!createdNew) {
         ConsoleUtil.println(
             "Using existing database.  To load from scratch, delete the file '" + dbFile.getAbsolutePath() + ".*'");
      } else {
         // RxNorm doesn't give us the UMLS tables that define the table definitions, so I put them into an XML file.
         final List<TableDefinition> tables = this.db.loadTableDefinitionsFromXML(
                                                  RxNormMojo.class.getResourceAsStream("/RxNormTableDefinitions.xml"));

         for (final TableDefinition td: tables) {
            final ZipEntry ze = zf.getEntry("rrf/" + td.getTableName() + ".RRF");

            if (ze == null) {
               throw new MojoExecutionException(
                   "Can't find the file 'rrf/" + td.getTableName() + ".RRF' in the zip file");
            }

            try (UMLSFileReader umlsReader = new UMLSFileReader(
                                                 new BufferedReader(
                                                       new InputStreamReader(zf.getInputStream(ze), "UTF-8")))) {
               this.db.loadDataIntoTable(td, umlsReader, null);
            }
         }

         zf.close();

         try (                                                                   // Build some indexes to support the queries we will run
               Statement s = this.db.getConnection().createStatement()) {
            ConsoleUtil.println("Creating indexes");
            ConsoleUtil.showProgress();
            s.execute("CREATE INDEX conso_rxcui_index ON RXNCONSO (RXCUI)");
            ConsoleUtil.showProgress();
            s.execute("CREATE INDEX conso_rxaui_index ON RXNCONSO (RXAUI)");
            ConsoleUtil.showProgress();
            s.execute("CREATE INDEX sat_rxcui_aui_index ON RXNSAT (RXCUI, RXAUI)");
            ConsoleUtil.showProgress();
            s.execute("CREATE INDEX sat_aui_index ON RXNSAT (RXAUI)");
            ConsoleUtil.showProgress();
            s.execute("CREATE INDEX sty_rxcui_index ON RXNSTY (RXCUI)");
            ConsoleUtil.showProgress();
            s.execute("CREATE INDEX sty_tui_index ON RXNSTY (TUI)");
            ConsoleUtil.showProgress();
            s.execute("CREATE INDEX rel_rxcui2_index ON RXNREL (RXCUI2, RXAUI2)");
            ConsoleUtil.showProgress();
            s.execute("CREATE INDEX rel_rxaui2_index ON RXNREL (RXCUI1, RXAUI1)");
            ConsoleUtil.showProgress();
            s.execute("CREATE INDEX rel_rela_rel_index ON RXNREL (RELA, REL)");  // helps with rel metadata
            ConsoleUtil.showProgress();
            s.execute("CREATE INDEX rel_sab_index ON RXNREL (SAB)");             // helps with rel metadata
         }

         ConsoleUtil.println("DB Setup complete");
      }

      return toReturn;
   }

   /**
    * Load meta data.
    *
    * @throws Exception the exception
    */
   private void loadMetaData()
            throws Exception {
      this.ptRefsets = new PT_Refsets(rxNormName);
      this.ptRefsets.addProperty(cpcRefsetConceptKey);

      final PropertyType sourceMetadata = new PT_SAB_Metadata();

      this.ptRelationshipMetadata = new PT_Relationship_Metadata();
      this.ptUMLSAttributes       = new PT_Annotations();
      this.importUtil.loadMetaDataItems(
          Arrays.asList(this.ptRefsets, sourceMetadata, this.ptRelationshipMetadata, this.ptUMLSAttributes),
          this.metaDataRoot.getPrimordialUuid());

      // Attributes from MRDoc
      // dynamically add more attributes from *DOC
      {
         ConsoleUtil.println("Creating attribute types");
         this.ptTermAttributes = new BPT_Annotations(rxNormName) {}
         ;
         this.ptTermAttributes.indexByAltNames();

         // extra logic at the end to keep NDC's from any sab when processing RXNorm
         try (Statement s =
               this.db.getConnection().createStatement();  // extra logic at the end to keep NDC's from any sab when processing RXNorm
            ResultSet rs = s.executeQuery(
                               "SELECT VALUE, TYPE, EXPL from " + this.tablePrefix +
                               "DOC where DOCKEY = 'ATN' and VALUE in (select distinct ATN from " + this.tablePrefix +
                               "SAT" + " where " + createSabQueryPart(
                                   "",
                                   false) + " or ATN='NDC')")) {
            while (rs.next()) {
               final String abbreviation = rs.getString("VALUE");
               final String type         = rs.getString("TYPE");
               final String expansion    = rs.getString("EXPL");

               if (!type.equals("expanded_form")) {
                  throw new RuntimeException("Unexpected type in the attribute data within DOC: '" + type + "'");
               }

               String altName     = null;
               String description = null;

               if (expansion.length() > 30) {
                  description = expansion;
               } else {
                  altName = expansion;
               }

               final AbbreviationExpansion ae = this.abbreviationExpansions.get(abbreviation);

               if (ae == null) {
                  ConsoleUtil.printErrorln("No Abbreviation Expansion found for " + abbreviation);
                  this.ptTermAttributes.addProperty(abbreviation, altName, description);
               } else {
                  this.ptTermAttributes.addProperty(ae.getExpansion(), ae.getAbbreviation(), ae.getDescription());
               }
            }
         }

         if (this.ptTermAttributes.getProperties()
                                  .size() > 0) {
            this.importUtil.loadMetaDataItems(this.ptTermAttributes, this.metaDataRoot.getPrimordialUuid());
         }
      }

      // description types
      {
         ConsoleUtil.println("Creating description_ types");
         this.ptDescriptions = new PT_Descriptions(rxNormName);
         this.ptDescriptions.indexByAltNames();

         final PreparedStatement ps;

         try (Statement s = this.db.getConnection().createStatement()) {
            ResultSet usedDescTypes;

            usedDescTypes = s.executeQuery("select distinct TTY from RXNCONSO WHERE " + createSabQueryPart("", false));
            ps = this.db.getConnection()
                        .prepareStatement(
                            "select TYPE, EXPL from " + this.tablePrefix + "DOC where DOCKEY='TTY' and VALUE=?");

            while (usedDescTypes.next()) {
               final String tty = usedDescTypes.getString(1);

               ps.setString(1, tty);

               String                expandedForm;
               final HashSet<String> classes;

               try (ResultSet descInfo = ps.executeQuery()) {
                  expandedForm = null;
                  classes      = new HashSet<>();

                  while (descInfo.next()) {
                     final String type = descInfo.getString("TYPE");
                     final String expl = descInfo.getString("EXPL");

                     switch (type) {
                     case "expanded_form":
                        if (expandedForm != null) {
                           throw new RuntimeException("Expected name to be null!");
                        }

                        expandedForm = expl;
                        break;

                     case "tty_class":
                        classes.add(expl);
                        break;

                     default:
                        throw new RuntimeException("Unexpected type in DOC for '" + tty + "'");
                     }
                  }
               }

               ps.clearParameters();

               Property                    p  = null;
               final AbbreviationExpansion ae = this.abbreviationExpansions.get(tty);

               if (ae == null) {
                  ConsoleUtil.printErrorln("No Abbreviation Expansion found for " + tty);
                  p = makeDescriptionType(tty, expandedForm, null, classes);
               } else {
                  p = makeDescriptionType(ae.getExpansion(), ae.getAbbreviation(), ae.getDescription(), classes);
               }

               this.ptDescriptions.addProperty(p);

               for (final String tty_class: classes) {
                  this.importUtil.addStringAnnotation(
                      ComponentReference.fromConcept(p.getUUID()),
                      tty_class,
                      this.ptUMLSAttributes.getProperty("tty_class")
                                           .getUUID(),
                      State.ACTIVE);
               }
            }

            usedDescTypes.close();
         }

         ps.close();

         if (this.ptDescriptions.getProperties()
                                .size() > 0) {
            this.importUtil.loadMetaDataItems(this.ptDescriptions, this.metaDataRoot.getPrimordialUuid());
         }
      }
      loadRelationshipMetadata();

      // STYPE values
      this.sTypes = new HashMap<>();
      {
         ConsoleUtil.println("Creating STYPE types");

         try (Statement s = this.db.getConnection().createStatement();
            ResultSet rs = s.executeQuery(
                               "SELECT DISTINCT VALUE, TYPE, EXPL FROM " + this.tablePrefix +
                               "DOC where DOCKEY like 'STYPE%'")) {
            while (rs.next()) {
               final String sType = rs.getString("VALUE");
               final String type  = rs.getString("TYPE");
               final String name  = rs.getString("EXPL");

               if (!type.equals("expanded_form")) {
                  throw new RuntimeException("Unexpected type in the attribute data within DOC: '" + type + "'");
               }

               final ComponentReference c = ComponentReference.fromConcept(
                                                this.importUtil.createConcept(
                                                      ConverterUUID.createNamespaceUUIDFromString(
                                                            this.ptUMLSAttributes.getProperty("STYPE")
                                                                  .getUUID() + ":" + name),
                                                            name,
                                                            null,
                                                            null,
                                                            sType,
                                                            this.ptUMLSAttributes.getProperty("STYPE")
                                                                  .getUUID(),
                                                            null));

               this.sTypes.put(name, c.getPrimordialUuid());
               this.sTypes.put(sType, c.getPrimordialUuid());
            }
         }
      }
      this.suppress = xDocLoaderHelper(
          "SUPPRESS",
          "Suppress",
          false,
          this.ptUMLSAttributes.getProperty("SUPPRESS")
                               .getUUID());

      // Not yet loading co-occurrence data yet, so don't need these yet.
      // xDocLoaderHelper("COA", "Attributes of co-occurrence", false);
      // xDocLoaderHelper("COT", "Type of co-occurrence", true);
      final HashMap<String, UUID> contextTypes = xDocLoaderHelper(
                                                     "CXTY",
                                                           "Context Type",
                                                           false,
                                                           sourceMetadata.getProperty("CXTY")
                                                                 .getUUID());

      // not yet loading mappings - so don't need this yet
      // xDocLoaderHelper("FROMTYPE", "Mapping From Type", false);
      // xDocLoaderHelper("TOTYPE", "Mapping To Type", false);
      // MAPATN - not yet used in UMLS
      // Handle the languages
      // Not actually doing anythign with these at the moment, we just map to metadata languages.
      {
         try (Statement s = this.db.getConnection().createStatement();
            ResultSet rs = s.executeQuery(
                               "SELECT * from " + this.tablePrefix +
                               "DOC where DOCKEY = 'LAT' and VALUE in (select distinct LAT from " + this.tablePrefix +
                               "CONSO where " + createSabQueryPart(
                                   "",
                                   false) + ")")) {
            while (rs.next()) {
               final String abbreviation = rs.getString("VALUE");
               final String type         = rs.getString("TYPE");

               // String expansion = rs.getString("EXPL");
               if (!type.equals("expanded_form")) {
                  throw new RuntimeException("Unexpected type in the language data within DOC: '" + type + "'");
               }

               if (abbreviation.equals("ENG") || abbreviation.equals("SPA")) {
                  // use official ISAAC languages
                  if (abbreviation.equals("ENG") || abbreviation.equals("SPA")) {
                     // We can map these onto metadata types.
                  } else {
                     throw new RuntimeException("unsupported language");
                  }
               }
            }
         }
      }

      // And Source Restriction Levels
      {
         ConsoleUtil.println("Creating Source Restriction Level types");
         this.sourceRestrictionLevels = new HashMap<>();

         try (PreparedStatement ps = this.db.getConnection().prepareStatement(
                                         "SELECT VALUE, TYPE, EXPL from " + this.tablePrefix +
                                         "DOC where DOCKEY=? ORDER BY VALUE")) {
            ps.setString(1, "SRL");

            final ResultSet rs          = ps.executeQuery();
            String          value       = null;
            String          description = null;
            String          uri         = null;

            // Two entries per SRL, read two rows, create an entry.
            while (rs.next()) {
               String type = rs.getString("TYPE");
               String expl = rs.getString("EXPL");

               switch (type) {
               case "expanded_form":
                  description = expl;
                  break;

               case "uri":
                  uri = expl;
                  break;

               default:
                  throw new RuntimeException("oops");
               }

               if (value == null) {
                  value = rs.getString("VALUE");
               } else {
                  if (!value.equals(rs.getString("VALUE"))) {
                     throw new RuntimeException("oops");
                  }

                  if ((description == null) || (uri == null)) {
                     throw new RuntimeException("oops");
                  }

                  final ComponentReference c = ComponentReference.fromConcept(
                                                   this.importUtil.createConcept(
                                                         ConverterUUID.createNamespaceUUIDFromString(
                                                               sourceMetadata.getProperty("SRL")
                                                                     .getUUID() + ":" + value),
                                                               value,
                                                               null,
                                                               null,
                                                               description,
                                                               sourceMetadata.getProperty("SRL")
                                                                     .getUUID(),
                                                               null));

                  this.sourceRestrictionLevels.put(value, c.getPrimordialUuid());
                  this.importUtil.addStringAnnotation(
                      c,
                      uri,
                      this.ptUMLSAttributes.getProperty("URI")
                                           .getUUID(),
                      State.ACTIVE);
                  type  = null;
                  expl  = null;
                  value = null;
               }
            }

            rs.close();
         }
      }

      // And Source vocabularies
      final PreparedStatement getSABMetadata = this.db.getConnection()
                                                      .prepareStatement(
                                                            "Select * from " + this.tablePrefix +
                                                            "SAB where (VSAB = ? or (RSAB = ? and CURVER='Y' ))");

      {
         ConsoleUtil.println("Creating Source Vocabulary types");
         this.ptSABs = new PropertyType("Source Vocabularies", true, DynamicSememeDataType.STRING) {}
         ;
         this.ptSABs.indexByAltNames();

         final HashSet<String> sabList = new HashSet<>();

         sabList.addAll(this.sabsToInclude);

         Statement s  = this.db.getConnection()
                               .createStatement();
         ResultSet rs = s.executeQuery("select distinct SAB from RXNSAT where ATN='NDC'");

         while (rs.next()) {
            sabList.add(rs.getString("SAB"));
         }

         rs.close();
         s.close();

         for (final String currentSab: sabList) {
            s = this.db.getConnection()
                       .createStatement();
            rs = s.executeQuery(
                "SELECT SON from " + this.tablePrefix + "SAB WHERE (VSAB='" + currentSab + "' or (RSAB='" +
                currentSab + "' and CURVER='Y'))");

            if (rs.next()) {
               final String             son = rs.getString("SON");
               final Property           p   = this.ptSABs.addProperty(son, currentSab, null);
               final ComponentReference cr  = ComponentReference.fromConcept(p.getUUID());

               try {
                  // lookup the other columns for the row with this newly added RSAB terminology
                  getSABMetadata.setString(
                      1,
                      (p.getSourcePropertyAltName() == null) ? p.getSourcePropertyNameFSN()
                        : p.getSourcePropertyAltName());
                  getSABMetadata.setString(
                      2,
                      (p.getSourcePropertyAltName() == null) ? p.getSourcePropertyNameFSN()
                        : p.getSourcePropertyAltName());

                  try (ResultSet rs2 = getSABMetadata.executeQuery()) {
                     if (rs2.next()) {
                        for (final Property metadataProperty: sourceMetadata.getProperties()) {
                           final String columnName = (metadataProperty.getSourcePropertyAltName() == null)
                                                     ? metadataProperty.getSourcePropertyNameFSN()
                                                     : metadataProperty.getSourcePropertyAltName();
                           final String columnValue = rs2.getString(columnName);

                           if (columnValue == null) {
                              continue;
                           }

                           switch (columnName) {
                           case "SRL":
                              this.importUtil.addUUIDAnnotation(
                                  cr,
                                  this.sourceRestrictionLevels.get(columnValue),
                                  metadataProperty.getUUID());
                              break;

                           case "CXTY":
                              this.importUtil.addUUIDAnnotation(
                                  cr,
                                  contextTypes.get(columnValue),
                                  sourceMetadata.getProperty("CXTY")
                                                .getUUID());
                              break;

                           default:
                              this.importUtil.addStringAnnotation(
                                  cr,
                                  columnValue,
                                  metadataProperty.getUUID(),
                                  State.ACTIVE);
                              break;
                           }
                        }
                     }

                     if (rs2.next()) {
                        throw new RuntimeException("Too many sabs.  Perhaps you should be using versioned sabs!");
                     }
                  }
               } catch (final SQLException e) {
                  throw new RuntimeException("Error loading *SAB", e);
               }
            } else {
               throw new RuntimeException("Too few? SABs - perhaps you need to use versioned SABs.");
            }

            if (rs.next()) {
               throw new RuntimeException(
                   "Too many SABs for '" + currentSab + "' - perhaps you need to use versioned SABs.");
            }

            rs.close();
            s.close();
         }

         this.importUtil.loadMetaDataItems(this.ptSABs, this.metaDataRoot.getPrimordialUuid());
         getSABMetadata.close();
      }

      // And semantic types
      {
         ConsoleUtil.println("Creating semantic types");

         try (Statement s = this.db.getConnection().createStatement();
            ResultSet rs = s.executeQuery("SELECT distinct TUI, STN, STY from " + this.tablePrefix + "STY")) {
            while (rs.next()) {
               final String tui = rs.getString("TUI");
               final String stn = rs.getString("STN");
               final String sty = rs.getString("STY");
               final ComponentReference c = ComponentReference.fromConcept(
                                                this.importUtil.createConcept(
                                                      ConverterUUID.createNamespaceUUIDFromString(
                                                            this.ptUMLSAttributes.getProperty("STY")
                                                                  .getUUID() + ":" + sty),
                                                            sty,
                                                            null,
                                                            null,
                                                            null,
                                                            this.ptUMLSAttributes.getProperty("STY")
                                                                  .getUUID(),
                                                            null));

               this.semanticTypes.put(tui, c.getPrimordialUuid());
               this.importUtil.addStringAnnotation(
                   c,
                   tui,
                   this.ptUMLSAttributes.getProperty("TUI")
                                        .getUUID(),
                   State.ACTIVE);
               this.importUtil.addStringAnnotation(
                   c,
                   stn,
                   this.ptUMLSAttributes.getProperty("STN")
                                        .getUUID(),
                   State.ACTIVE);
            }
         }
      }
   }

   /**
    * Load relationship metadata.
    *
    * @throws Exception the exception
    */
   private void loadRelationshipMetadata()
            throws Exception {
      ConsoleUtil.println("Creating relationship types");

      // Both of these get added as extra attributes on the relationship definition
      final HashMap<String, ArrayList<String>> snomedCTRelaMappings =
         new HashMap<>();  // Maps something like 'has_specimen_source_morphology' to '118168003' (may be more than one target SCT code)
      final HashMap<String, String> snomedCTRelMappings = new HashMap<>();  // Maps something like '118168003' to 'RO'

      this.nameToRel = new HashMap<>();

      Statement s = this.db.getConnection()
                           .createStatement();

      // get the inverses of first, before the expanded forms
      ResultSet rs = s.executeQuery(
                         "SELECT DOCKEY, VALUE, TYPE, EXPL FROM " + this.tablePrefix +
                         "DOC where DOCKEY ='REL' or DOCKEY = 'RELA' order by TYPE DESC ");

      while (rs.next()) {
         final String dockey = rs.getString("DOCKEY");
         final String value  = rs.getString("VALUE");
         final String type   = rs.getString("TYPE");
         final String expl   = rs.getString("EXPL");

         if (value == null) {
            continue;  // don't need this one
         }

         switch (type) {
         case "snomedct_rela_mapping":
            ArrayList<String> targetSCTIDs = snomedCTRelaMappings.get(expl);

            if (targetSCTIDs == null) {
               targetSCTIDs = new ArrayList<>();
               snomedCTRelaMappings.put(expl, targetSCTIDs);
            }

            targetSCTIDs.add(value);
            break;

         case "snomedct_rel_mapping":
            snomedCTRelMappings.put(value, expl);
            break;

         default:
            Relationship rel = this.nameToRel.get(value);

            if (rel == null) {
               if (type.endsWith("_inverse")) {
                  rel = this.nameToRel.get(expl);

                  if (rel == null) {
                     rel = new Relationship(dockey.equals("RELA"));
                     this.nameToRel.put(value, rel);
                     this.nameToRel.put(expl, rel);
                  } else {
                     throw new RuntimeException("shouldn't happen due to query order");
                  }
               } else {
                  // only cases where there is no inverse
                  rel = new Relationship(dockey.equals("RELA"));
                  this.nameToRel.put(value, rel);
               }
            }

            switch (type) {
            case "expanded_form":
               rel.addDescription(value, expl);
               break;

            case "rela_inverse":
            case "rel_inverse":
               rel.addRelInverse(value, expl);
               break;

            default:
               throw new RuntimeException("Oops");
            }

            break;
         }
      }

      rs.close();
      s.close();

      final HashSet<String> actuallyUsedRelsOrRelas = new HashSet<>();

      for (final Entry<String, ArrayList<String>> x: snomedCTRelaMappings.entrySet()) {
         if (!this.nameToRel.containsKey(x.getKey())) {
            // metamorphosys doesn't seem to remove these when the sct rel types aren't included - just silently remove them
            // unless it seems that they should map.
            // may_be_a appears to be a bug in RxNorm 2013-12-02.  silently ignore...
            // TODO see if they fix it in the future, make this check version specific?
            // seems to be getting worse... now it fails to remove 'has_life_circumstance' too in 2014AA, and a few others.
            // Changing to a warning.
            ConsoleUtil.printErrorln(
                "Warning - The 'snomedct_rela_mapping' '" + x.getKey() +
                "' does not have a corresponding REL entry!  Skipping");

//          if (!x.getKey().equals("may_be_a") && !x.getKey().equals("has_life_circumstance"))
//          {
//                  throw new RuntimeException("ERROR - No rel for " + x.getKey() + ".");
//          }
            x.getValue()
             .forEach(
                 (sctId) -> {
                    snomedCTRelMappings.remove(sctId);
                 });
         } else {
            x.getValue()
             .stream()
             .map(
                 (sctid) -> {
                    this.nameToRel.get(x.getKey())
                                  .addSnomedCode(x.getKey(), sctid);
                    return sctid;
                 })
             .map((sctid) -> snomedCTRelMappings.remove(sctid))
             .filter((relType) -> (relType != null))
             .map(
                 (relType) -> {
                    this.nameToRel.get(x.getKey())
                                  .addRelType(x.getKey(), relType);
                    return relType;
                 })
             .forEachOrdered(
                 (relType) -> {
               // Shouldn't need this, but there are some cases where the metadata is inconsistent - with how it is actually used.
                    actuallyUsedRelsOrRelas.add(relType);
                 });
         }
      }

      if (snomedCTRelMappings.size() > 0) {
         snomedCTRelMappings.entrySet()
                            .forEach(
                                (x) -> {
                                   ConsoleUtil.printErrorln(x.getKey() + ":" + x.getValue());
                                });
         throw new RuntimeException("oops - still have (things listed above)");
      }

      this.ptRelationships = new BPT_Relations(rxNormName) {}
      ;
      this.ptRelationships.indexByAltNames();
      this.ptAssociations = new BPT_Associations() {}
      ;
      this.ptAssociations.indexByAltNames();
      s = this.db.getConnection()
                 .createStatement();
      rs = s.executeQuery(
          "select distinct REL, RELA from " + this.tablePrefix + "REL where " + createSabQueryPart(
              "",
              this.linkSnomedCT));

      while (rs.next()) {
         actuallyUsedRelsOrRelas.add(rs.getString("REL"));

         if (rs.getString("RELA") != null) {
            actuallyUsedRelsOrRelas.add(rs.getString("RELA"));
         }
      }

      rs.close();
      s.close();

      final HashSet<Relationship> uniqueRels = new HashSet<>(this.nameToRel.values());

      // Sort the generic relationships first, these are needed when processing primary
      final ArrayList<Relationship> sortedRels = new ArrayList<>(uniqueRels);

      Collections.sort(
          sortedRels,
              (o1, o2) -> {
                 if (o1.getIsRela() &&!o2.getIsRela()) {
                    return 1;
                 }

                 if (o2.getIsRela() &&!o1.getIsRela()) {
                    return -1;
                 }

                 return 0;
              });

      for (final Relationship r: sortedRels) {
         r.setSwap(this.db.getConnection(), this.tablePrefix);

         if (!actuallyUsedRelsOrRelas.contains(r.getFSNName()) &&
               !actuallyUsedRelsOrRelas.contains(r.getInverseFSNName())) {
            continue;
         }

         Property      p          = null;
         final Boolean relTypeMap = this.mapToIsa.get(r.getFSNName());

         if (relTypeMap != null)                                                                       // true or false, make it a rel
         {
            p = new Property(((r.getAltName() == null) ? r.getFSNName()
                  : r.getAltName()), ((r.getAltName() == null) ? null
                  : r.getFSNName()), r.getDescription(), MetaData.IS_A____ISAAC.getPrimordialUuid());  // map to isA
            this.ptRelationships.addProperty(
                p);  // conveniently, the only thing we will treat as relationships are things mapped to isa.
         }

         if ((relTypeMap == null) || (relTypeMap == false))  // don't make it an association if set to true
         {
            p = new PropertyAssociation(null, ((r.getAltName() == null) ? r.getFSNName()
                  : r.getAltName()), ((r.getAltName() == null) ? null
                  : r.getFSNName()), ((r.getInverseAltName() == null) ? r.getInverseFSNName()
                  : r.getInverseAltName()), r.getDescription(), false);
            this.ptAssociations.addProperty(p);
         }

         final ComponentReference cr = ComponentReference.fromConcept(p.getUUID());

         // associations already handle inverse names
         if (!(p instanceof PropertyAssociation) && (r.getInverseFSNName() != null)) {
            this.importUtil.addDescription(
                cr,
                ((r.getInverseAltName() == null) ? r.getInverseFSNName()
                  : r.getInverseAltName()),
                DescriptionType.FSN,
                false,
                this.ptDescriptions.getProperty("Inverse FSN")
                                   .getUUID(),
                State.ACTIVE);
         }

         if (r.getAltName() != null) {
            // Need to create this UUID to be different than forward name, in case forward and reverse are identical (like 'RO')
            final UUID descUUID = ConverterUUID.createNamespaceUUIDFromStrings(
                                      cr.getPrimordialUuid()
                                        .toString(),
                                      r.getInverseFSNName(),
                                      DescriptionType.SYNONYM.name(),
                                      "false",
                                      "inverse");

            // Yes, this looks funny, no its not a copy/paste error.  We swap the FSN and alt names for... it a long story.  42.
            this.importUtil.addDescription(
                cr,
                descUUID,
                r.getInverseFSNName(),
                DescriptionType.SYNONYM,
                false,
                null,
                null,
                null,
                null,
                this.ptDescriptions.getProperty("Inverse Synonym")
                                   .getUUID(),
                State.ACTIVE,
                null);
         }

         if (r.getInverseDescription() != null) {
            this.importUtil.addDescription(
                cr,
                r.getInverseDescription(),
                DescriptionType.DEFINITION,
                true,
                this.ptDescriptions.getProperty("Inverse Description")
                                   .getUUID(),
                State.ACTIVE);
         }

         if (r.getRelType() != null) {
            final Relationship generalRel = this.nameToRel.get(r.getRelType());

            this.importUtil.addUUIDAnnotation(
                cr,
                (this.mapToIsa.containsKey(
                    generalRel.getFSNName()) ? this.ptRelationships.getProperty(generalRel.getFSNName())
                                             : this.ptAssociations.getProperty(generalRel.getFSNName())).getUUID(),
                this.ptRelationshipMetadata.getProperty("General Rel Type")
                                           .getUUID());
         }

         if (r.getInverseRelType() != null) {
            final Relationship generalRel = this.nameToRel.get(r.getInverseRelType());

            this.importUtil.addUUIDAnnotation(
                cr,
                (this.mapToIsa.containsKey(
                    generalRel.getFSNName()) ? this.ptRelationships.getProperty(generalRel.getFSNName())
                                             : this.ptAssociations.getProperty(generalRel.getFSNName())).getUUID(),
                this.ptRelationshipMetadata.getProperty("Inverse General Rel Type")
                                           .getUUID());
         }

         r.getRelSnomedCode()
          .forEach(
              (sctCode) -> {
                 this.importUtil.addUUIDAnnotation(
                     cr,
                     UuidT3Generator.fromSNOMED(sctCode),
                     this.ptRelationshipMetadata.getProperty("Snomed Code")
                           .getUUID());
              });
         r.getInverseRelSnomedCode()
          .forEach(
              (sctCode) -> {
                 this.importUtil.addUUIDAnnotation(
                     cr,
                     UuidT3Generator.fromSNOMED(sctCode),
                     this.ptRelationshipMetadata.getProperty("Inverse Snomed Code")
                           .getUUID());
              });
      }

      if (this.ptRelationships.getProperties()
                              .size() > 0) {
         this.importUtil.loadMetaDataItems(this.ptRelationships, this.metaDataRoot.getPrimordialUuid());
      }

      if (this.ptAssociations.getProperties()
                             .size() > 0) {
         this.importUtil.loadMetaDataItems(this.ptAssociations, this.metaDataRoot.getPrimordialUuid());
      }
   }

   /**
    * Make description type.
    *
    * @param fsnName the fsn name
    * @param altName the alt name
    * @param description the description
    * @param tty_classes the tty classes
    * @return the property
    */
   private Property makeDescriptionType(String fsnName,
         String altName,
         String description,
         final Set<String> tty_classes) {
      // The current possible classes are:
      // preferred
      // obsolete
      // entry_term
      // hierarchical
      // synonym
      // attribute
      // abbreviation
      // expanded
      // other
      int descriptionTypeRanking;

      // Note - ValuePropertyPairWithSAB overrides the sorting based on these values to kick RXNORM sabs to the top, where
      // they will get used as FSN.
      if (fsnName.equals("FN") && tty_classes.contains("preferred")) {
         descriptionTypeRanking = BPT_Descriptions.FSN;
      } else if (fsnName.equals("FN")) {
         descriptionTypeRanking = BPT_Descriptions.FSN + 1;
      }  // preferred gets applied with others as well, in some cases. Don't want 'preferred' 'obsolete' at the top.

      // Just preferred, and we make it the top synonym.
      else if (tty_classes.contains("preferred") && (tty_classes.size() == 1)) {
         // these sub-rankings are somewhat arbitrary at the moment, and in general, unused.  There is an error check up above which
         // will fail the conversion if it tries to rely on these sub-rankings to find a preferred term
         int preferredSubRank;

         switch (altName) {
         case "IN":
            preferredSubRank = 1;
            break;

         case "MIN":
            preferredSubRank = 2;
            break;

         case "PIN":
            preferredSubRank = 3;
            break;

         case "SCD":
            preferredSubRank = 4;
            break;

         case "BN":
            preferredSubRank = 5;
            break;

         case "SBD":
            preferredSubRank = 6;
            break;

         case "DF":
            preferredSubRank = 7;
            break;

         case "BPCK":
            preferredSubRank = 8;
            break;

         case "GPCK":
            preferredSubRank = 10;
            break;

         case "DFG":
            preferredSubRank = 11;
            break;

         case "PSN":
            preferredSubRank = 12;
            break;

         case "SBDC":
            preferredSubRank = 13;
            break;

         case "SCDC":
            preferredSubRank = 14;
            break;

         case "SBDF":
            preferredSubRank = 15;
            break;

         case "SCDF":
            preferredSubRank = 16;
            break;

         case "SBDG":
            preferredSubRank = 17;
            break;

         case "SCDG":
            preferredSubRank = 18;
            break;

         default:
            preferredSubRank = 20;
            ConsoleUtil.printErrorln("Unranked preferred TTY type! " + fsnName + " " + altName);
            break;
         }

         descriptionTypeRanking = BPT_Descriptions.SYNONYM + preferredSubRank;
      } else if (tty_classes.contains("entry_term")) {
         descriptionTypeRanking = BPT_Descriptions.SYNONYM + 21;
      } else if (tty_classes.contains("synonym")) {
         descriptionTypeRanking = BPT_Descriptions.SYNONYM + 22;
      } else if (tty_classes.contains("expanded")) {
         descriptionTypeRanking = BPT_Descriptions.SYNONYM + 23;
      } else if (tty_classes.contains("Prescribable Name")) {
         descriptionTypeRanking = BPT_Descriptions.SYNONYM + 24;
      } else if (tty_classes.contains("abbreviation")) {
         descriptionTypeRanking = BPT_Descriptions.SYNONYM + 25;
      } else if (tty_classes.contains("attribute")) {
         descriptionTypeRanking = BPT_Descriptions.SYNONYM + 26;
      } else if (tty_classes.contains("hierarchical")) {
         descriptionTypeRanking = BPT_Descriptions.SYNONYM + 27;
      } else if (tty_classes.contains("other")) {
         descriptionTypeRanking = BPT_Descriptions.SYNONYM + 28;
      } else if (tty_classes.contains("obsolete")) {
         descriptionTypeRanking = BPT_Descriptions.SYNONYM + 29;
      } else {
         throw new RuntimeException("Unexpected class type " + Arrays.toString(tty_classes.toArray()));
      }

      return new Property(null, fsnName, altName, description, false, descriptionTypeRanking, null);
   }

   /**
    * Prepare SCT maps.
    *
    * @throws SQLException the SQL exception
    */
   private void prepareSCTMaps()
            throws SQLException {
      Get.sememeService()
         .getSememeSequencesFromAssemblage(MetaData.SCTID____ISAAC.getConceptSequence())
         .stream()
         .forEach(
             sememe -> {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                final LatestVersion<StringSememe> lv = ((SememeChronology) Get.sememeService()
                                                                              .getSememe(
                                                                                    sememe)).getLatestVersion(
                                                                                          StringSememe.class,
                                                                                                StampCoordinates.getDevelopmentLatest());
                final StringSememe ss    = lv.value()
                                             .get();
                final Long         sctId = Long.parseLong(ss.getString());
                final UUID conceptUUID = Get.identifierService()
                                            .getUuidPrimordialForNid(ss.getReferencedComponentNid())
                                            .get();

                this.sctIdToUUID.put(sctId, conceptUUID);
             });
      ConsoleUtil.println("Read SCTID -> UUID mappings for " + this.sctIdToUUID.size() + " items");

      try (ResultSet rs = this.db.getConnection().createStatement().executeQuery(
                              "SELECT DISTINCT RXCUI, CODE from RXNCONSO where SAB='" + this.sctSab + "'")) {
         while (rs.next()) {
            final String cui   = rs.getString("RXCUI");
            final long   sctid = Long.parseLong(rs.getString("CODE"));

            if (this.sctIdToUUID.containsKey(sctid)) {
               this.cuiToSCTID.put(cui, sctid);
            }
         }
      }

      ConsoleUtil.println("Read CUI -> SCTID mappings for " + this.cuiToSCTID.size() + " items");
   }

   /**
    * Process CUI rows.
    *
    * @param conceptData the concept data
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws SQLException the SQL exception
    * @throws PropertyVetoException the property veto exception
    */
   private void processCUIRows(ArrayList<RXNCONSO> conceptData)
            throws IOException,
                   SQLException,
                   PropertyVetoException {
      final String          rxCui      = conceptData.get(0).rxcui;
      final HashSet<String> uniqueTTYs = new HashSet<>();
      final HashSet<String> uniqueSABs = new HashSet<>();

      // ensure all the same CUI, gather the TTYs involved
      conceptData.stream()
                 .map(
                     (row) -> {
                        uniqueTTYs.add(row.tty);
                        return row;
                     })
                 .map(
                     (row) -> {
                        uniqueSABs.add(row.sab);
                        return row;
                     })
                 .filter((row) -> (!row.rxcui.equals(rxCui)))
                 .forEachOrdered(
                     (_item) -> {
                        throw new RuntimeException("Oops");
                     });

      ComponentReference cuiConcept;

      if ((uniqueSABs.size() == 1) && uniqueSABs.iterator().next().equals(this.sctSab)) {
         // This is a SCT only concept - we don't want to create it.  But we might need to put some relationships or associations here.
         final String sctId = conceptData.get(0).code;

         if (sctId == null) {
            throw new RuntimeException("Unexpected");
         }

         cuiConcept = ComponentReference.fromConcept(this.sctIdToUUID.get(sctId));

         // Add the RxCUI UUID
         this.importUtil.addUUID(cuiConcept.getPrimordialUuid(), createCUIConceptUUID(rxCui));

         // TODO need to look at what else I should be grabbing - the RXCUI for example should be attached.  What else?
      } else {
         // just creating the reference here, with the UUID - because we don't know if it should be active or inactive yet.
         // create the real concept later.
         cuiConcept = ComponentReference.fromConcept(createCUIConceptUUID(rxCui));

         long conceptTime = Integer.MAX_VALUE;

         // Activate the concept if any description is active
         State conceptState = State.INACTIVE;

         this.importUtil.addStringAnnotation(
             cuiConcept,
             rxCui,
             this.ptUMLSAttributes.getProperty("RXCUI")
                                  .getUUID(),
             State.ACTIVE);

         final ArrayList<ValuePropertyPairWithSAB> cuiDescriptions = new ArrayList<>();
         final HashSet<String>                     sabs            = new HashSet<>();

         for (final RXNCONSO atom: conceptData) {
            if (atom.sab.equals(this.sctSab)) {
               continue;
            }

            // Add attributes from SAT table
            this.descSat.clearParameters();
            this.descSat.setString(1, rxCui);
            this.descSat.setString(2, atom.rxaui);

            final ArrayList<RXNSAT> satData;
            boolean                 disableDescription;
            Long                    descriptionTime;

            try (ResultSet rs = this.descSat.executeQuery()) {
               satData            = new ArrayList<>();
               disableDescription = false;
               descriptionTime    = null;

               while (rs.next()) {
                  final RXNSAT current = new RXNSAT(rs);

                  satData.add(current);

                  if ("RXN_OBSOLETED".equals(current.atn)) {
                     disableDescription = true;
                  }

                  if ("RXN_ACTIVATED".equals(current.atn)) {
                     try {
                        final long time = this.dateParse.parse(current.atv)
                                                        .getTime();

                        descriptionTime = time;

                        if (time < conceptTime) {
                           conceptTime = time;
                        }
                     } catch (final ParseException e) {
                        throw new RuntimeException("Can't parse date?");
                     }
                  }
               }
            }

            final ValuePropertyPairWithSAB desc = new ValuePropertyPairWithSAB(
                                                      atom.str,
                                                            this.ptDescriptions.getProperty(atom.tty),
                                                            atom.sab,
                                                            satData);

            if (disableDescription) {
               desc.setDisabled(true);
            } else {
               // if any description is active, concept is still active
               conceptState = State.ACTIVE;
            }

            if (descriptionTime != null) {
               desc.setTime(descriptionTime);
            }

            desc.setUUID(
                ConverterUUID.createNamespaceUUIDFromStrings(cuiConcept.getPrimordialUuid()
                      .toString(), atom.rxaui));

            // used for sorting description to figure out what to use for FSN
            cuiDescriptions.add(desc);
            desc.addStringAttribute(this.ptUMLSAttributes.getProperty("RXAUI")
                  .getUUID(), atom.rxaui);
            desc.addUUIDAttribute(
                this.ptUMLSAttributes.getProperty("SAB")
                                     .getUUID(),
                this.ptSABs.getProperty(atom.sab)
                           .getUUID());

            if (StringUtils.isNotBlank(atom.code) &&!atom.code.equals("NOCODE")) {
               desc.addStringAttribute(this.ptUMLSAttributes.getProperty("CODE")
                     .getUUID(), atom.code);
            }

            if (StringUtils.isNotBlank(atom.saui)) {
               desc.addStringAttribute(this.ptUMLSAttributes.getProperty("SAUI")
                     .getUUID(), atom.saui);
            }

            if (StringUtils.isNotBlank(atom.scui)) {
               desc.addStringAttribute(this.ptUMLSAttributes.getProperty("SCUI")
                     .getUUID(), atom.scui);
            }

            if (StringUtils.isNotBlank(atom.suppress)) {
               desc.addUUIDAttribute(
                   this.ptUMLSAttributes.getProperty("SUPPRESS")
                                        .getUUID(),
                   this.suppress.get(atom.suppress));
            }

            if (StringUtils.isNotBlank(atom.cvf)) {
               if (atom.cvf.equals("4096")) {
                  desc.addRefsetMembership(this.cpcRefsetConcept.getPrimordialUuid());
               } else {
                  throw new RuntimeException("Unexpected value in RXNCONSO cvf column '" + atom.cvf + "'");
               }
            }

            if (!atom.lat.equals("ENG")) {
               ConsoleUtil.printErrorln("Non-english lang settings not handled yet!");
            }

            // TODO - at this point, sometime in the future, we make make attributes out of the relationships that occur between the AUIs
            // and store them on the descriptions, since OTF doesn't allow relationships between descriptions
            // TODO am I supposed to be using sabs?
            sabs.add(atom.sab);
         }

         // sanity check on descriptions - make sure we only have one that is of type synonym with the preferred flag
         final ArrayList<String> items = new ArrayList<>();

         cuiDescriptions.stream()
                        .filter(
                            (vpp) -> ((vpp.getProperty().getPropertySubType() >= BPT_Descriptions.SYNONYM) &&
                                      (vpp.getProperty().getPropertySubType() <= (BPT_Descriptions.SYNONYM + 20))))
                        .forEachOrdered(
                            (vpp) -> {
                               items.add(
                                   vpp.getProperty()
                                      .getSourcePropertyNameFSN() + " " + vpp.getProperty().getPropertySubType());
                            });  // Numbers come from the rankings down below in makeDescriptionType(...)

         final HashSet<String> ranksLookedAt = new HashSet<>();

         ranksLookedAt.add("204");
         ranksLookedAt.add("206");
         ranksLookedAt.add("210");
         ranksLookedAt.add("208");
         ranksLookedAt.add("212");

         boolean oneNotInList = false;

         if (items.size() > 1) {
            for (final String s: items) {
               if (!ranksLookedAt.contains(s.substring(s.length() - 3, s.length()))) {
                  oneNotInList = true;
                  break;
               }
            }
         }

         if (oneNotInList) {
            ConsoleUtil.printErrorln(
                "Need to rank multiple synonym types that are each marked preferred, determine if ranking is appropriate!");
            items.forEach(
                (s) -> {
                   ConsoleUtil.printErrorln(s);
                });
         }

         final List<SememeChronology<DescriptionVersion>> addedDescriptions = this.importUtil.addDescriptions(
                                                                                 cuiConcept,
                                                                                       cuiDescriptions);

         if (addedDescriptions.size() != cuiDescriptions.size()) {
            throw new RuntimeException("oops");
         }

         final HashSet<String> uniqueUMLSCUI = new HashSet<>();

         for (int i = 0; i < cuiDescriptions.size(); i++) {
            final SememeChronology<DescriptionVersion> desc      = addedDescriptions.get(i);
            final ValuePropertyPairWithSAB            descPP    = cuiDescriptions.get(i);
            final BiFunction<String, String, Boolean> functions = (atn, atv) -> {
               // Pull these up to the concept.
                     if ("UMLSCUI".equals(atn)) {
                        uniqueUMLSCUI.add(atv);
                        return true;
                     }

                     return false;
                  };

            // TODO should I be passing in item code here?
            processSAT(ComponentReference.fromChronology(desc), descPP.getSatData(), null, descPP.getSab(), functions);
         }

         // pulling up the UMLS CUIs.
         // uniqueUMLSCUI is populated during processSAT
         uniqueUMLSCUI.forEach(
             (umlsCui) -> {
                final UUID itemUUID = ConverterUUID.createNamespaceUUIDFromString("UMLSCUI" + umlsCui);

                this.importUtil.addStringAnnotation(
                    cuiConcept,
                    itemUUID,
                    umlsCui,
                    this.ptTermAttributes.getProperty("UMLSCUI")
                                         .getUUID(),
                    State.ACTIVE);
             });
         ValuePropertyPairWithAttributes.processAttributes(this.importUtil, cuiDescriptions, addedDescriptions);

         // there are no attributes in rxnorm without an AUI.
//       try
//       {
         this.importUtil.addRefsetMembership(
             cuiConcept,
             this.allCUIRefsetConcept.getPrimordialUuid(),
             State.ACTIVE,
             null);

//       }
//       catch (RuntimeException e)
//       {
//               if (e.toString().contains("duplicate UUID"))
//               {
//                       //ok - this can happen due to multiple merges onto an existing SCT concept
//               }
//               else
//               {
//                       throw e;
//               }
//       }
         // add semantic types
         this.semanticTypeStatement.clearParameters();
         this.semanticTypeStatement.setString(1, rxCui);

         final ResultSet rs = this.semanticTypeStatement.executeQuery();

         processSemanticTypes(cuiConcept, rs);

         if (conceptTime < 0) {
            throw new RuntimeException("oops");
         }

         this.importUtil.createConcept(cuiConcept.getPrimordialUuid(), conceptTime, conceptState, null);
      }

      final HashSet<UUID> parents = new HashSet<>();

      this.cuiRelStatementForward.clearParameters();
      this.cuiRelStatementForward.setString(1, rxCui);
      parents.addAll(
          addRelationships(
              cuiConcept,
              REL.read(
                  null,
                  this.cuiRelStatementForward.executeQuery(),
                  true,
                  this.allowedCUIsForSABs,
                  this.skippedRelForNotMatchingCUIFilter,
                  true,
                  (string -> reverseRel(string)))));
      this.cuiRelStatementBackward.clearParameters();
      this.cuiRelStatementBackward.setString(1, rxCui);
      parents.addAll(
          addRelationships(
              cuiConcept,
              REL.read(
                  null,
                  this.cuiRelStatementBackward.executeQuery(),
                  false,
                  this.allowedCUIsForSABs,
                  this.skippedRelForNotMatchingCUIFilter,
                  true,
                  (string -> reverseRel(string)))));

      // Have to add multiple parents at once, no place to keep all the other details.  Load those as associations for now.
      if (parents.size() > 0) {
         ComponentReference.fromChronology(
             this.importUtil.addParent(cuiConcept, null, parents.toArray(new UUID[parents.size()]), null, null));
      }
   }

   /**
    * Process SAT.
    *
    * @param itemToAnnotate the item to annotate
    * @param satRows the sat rows
    * @param itemCode the item code
    * @param itemSab the item sab
    * @param skipCheck the skip check
    * @throws SQLException the SQL exception
    * @throws PropertyVetoException the property veto exception
    */
   private void processSAT(ComponentReference itemToAnnotate,
                           List<RXNSAT> satRows,
                           String itemCode,
                           String itemSab,
                           BiFunction<String, String, Boolean> skipCheck)
            throws SQLException,
                   PropertyVetoException {
      for (final RXNSAT rxnsat: satRows) {
         if (skipCheck != null) {
            if (skipCheck.apply(rxnsat.atn, rxnsat.atv)) {
               continue;
            }
         }

         // for some reason, ATUI isn't always provided - don't know why.  must gen differently in those cases...
         UUID       stringAttrUUID;
         final UUID refsetUUID = this.ptTermAttributes.getProperty(rxnsat.atn)
                                                      .getUUID();

         if (rxnsat.atui != null) {
            stringAttrUUID = ConverterUUID.createNamespaceUUIDFromString("ATUI" + rxnsat.atui);
         } else {
            // need to put the aui in here, to keep it unique, as each AUI frequently specs the same CUI
            stringAttrUUID = ConverterUUID.createNamespaceUUIDFromStrings(
                itemToAnnotate.getPrimordialUuid()
                              .toString(),
                rxnsat.rxaui,
                rxnsat.atv,
                refsetUUID.toString());
         }

         // You would expect that ptTermAttributes_.get() would be looking up sab, rather than having RxNorm hardcoded... but this is an oddity of
         // a hack we are doing within the RxNorm load.
         final ComponentReference attribute = ComponentReference.fromChronology(
                                                  this.importUtil.addStringAnnotation(
                                                        itemToAnnotate,
                                                              stringAttrUUID,
                                                              rxnsat.atv,
                                                              refsetUUID,
                                                              State.ACTIVE),
                                                        () -> "Attribute");

         if (StringUtils.isNotBlank(rxnsat.atui)) {
            this.importUtil.addStringAnnotation(
                attribute,
                rxnsat.atui,
                this.ptUMLSAttributes.getProperty("ATUI")
                                     .getUUID(),
                null);
         }

         if (StringUtils.isNotBlank(rxnsat.stype)) {
            this.importUtil.addUUIDAnnotation(
                attribute,
                this.sTypes.get(rxnsat.stype),
                this.ptUMLSAttributes.getProperty("STYPE")
                                     .getUUID());
         }

         if (StringUtils.isNotBlank(rxnsat.code) && StringUtils.isNotBlank(itemCode) &&!rxnsat.code.equals(itemCode)) {
            throw new RuntimeException("oops");

//          if ()
//          {
//                  eConcepts_.addStringAnnotation(attribute, code, ptUMLSAttributes_.getProperty("CODE").getUUID(), State.ACTIVE);
//          }
         }

         if (StringUtils.isNotBlank(rxnsat.satui)) {
            this.importUtil.addStringAnnotation(
                attribute,
                rxnsat.satui,
                this.ptUMLSAttributes.getProperty("SATUI")
                                     .getUUID(),
                State.ACTIVE);
         }

         // only load the sab if it is different than the sab of the item we are putting this attribute on
         if (StringUtils.isNotBlank(rxnsat.sab) &&!rxnsat.sab.equals(itemSab)) {
            throw new RuntimeException("Oops");

            // eConcepts_.addUuidAnnotation(attribute, ptSABs_.getProperty(sab).getUUID(), ptUMLSAttributes_.getProperty("SAB").getUUID());
         }

         if (StringUtils.isNotBlank(rxnsat.suppress)) {
            this.importUtil.addUUIDAnnotation(
                attribute,
                this.suppress.get(rxnsat.suppress),
                this.ptUMLSAttributes.getProperty("SUPPRESS")
                                     .getUUID());
         }

         if (StringUtils.isNotBlank(rxnsat.cvf)) {
            if (rxnsat.cvf.equals("4096")) {
               this.importUtil.addRefsetMembership(
                   attribute,
                   this.cpcRefsetConcept.getPrimordialUuid(),
                   State.ACTIVE,
                   null);
            } else {
               throw new RuntimeException("Unexpected value in RXNSAT cvf column '" + rxnsat.cvf + "'");
            }
         }
      }
   }

   /**
    * Process semantic types.
    *
    * @param concept the concept
    * @param rs the rs
    * @throws SQLException the SQL exception
    */
   private void processSemanticTypes(ComponentReference concept, ResultSet rs)
            throws SQLException {
      while (rs.next()) {
//       try
//       {
         final ComponentReference annotation = ComponentReference.fromChronology(
                                                   this.importUtil.addUUIDAnnotation(
                                                         concept,
                                                               this.semanticTypes.get(rs.getString("TUI")),
                                                               this.ptUMLSAttributes.getProperty("STY")
                                                                     .getUUID()),
                                                         () -> "Sememe Member");

         if (rs.getString("ATUI") != null) {
            this.importUtil.addStringAnnotation(
                annotation,
                rs.getString("ATUI"),
                this.ptUMLSAttributes.getProperty("ATUI")
                                     .getUUID(),
                State.ACTIVE);
         }

         if (rs.getObject("CVF") != null)  // might be an int or a string
         {
            this.importUtil.addStringAnnotation(
                annotation,
                rs.getString("CVF"),
                this.ptUMLSAttributes.getProperty("CVF")
                                     .getUUID(),
                State.ACTIVE);
         }

//       }
//       catch (RuntimeException e)
//       {
//               //ok if dupe - this can happen due to multiple merges onto an existing SCT concept
//               if (!e.toString().contains("duplicate UUID"))
//               {
//                       throw e;
//               }
//       }
      }

      rs.close();
   }

   /**
    * Rel check is rel loaded.
    *
    * @param rel the rel
    * @return true, if successful
    */
   private boolean relCheckIsRelLoaded(REL rel) {
      return this.loadedRels.contains(rel.getRelHash());
   }

   /**
    * Rel check loaded rel.
    *
    * @param rel the rel
    */
   private void relCheckLoadedRel(REL rel) {
      this.loadedRels.add(rel.getRelHash());
      this.skippedRels.remove(rel.getRelHash());
   }

   /**
    * Call this when a rel wasn't added because the rel was listed with the inverse name, rather than the primary name.
    *
    * @param rel the rel
    */
   private void relCheckSkippedRel(REL rel) {
      this.skippedRels.add(rel.getInverseRelHash(string -> this.nameToRel.get(string)));
   }

   /**
    * Reverse rel.
    *
    * @param eitherRelType the either rel type
    * @return the string
    */
   private String reverseRel(String eitherRelType) {
      if (eitherRelType == null) {
         return null;
      }

      final Relationship r = this.nameToRel.get(eitherRelType);

      if (r.getFSNName()
           .equals(eitherRelType)) {
         return r.getInverseFSNName();
      } else if (r.getInverseFSNName()
                  .equals(eitherRelType)) {
         return r.getFSNName();
      } else {
         throw new RuntimeException("gak");
      }
   }

   /**
    * X doc loader helper.
    *
    * @param dockey the dockey
    * @param niceName the nice name
    * @param loadAsDefinition the load as definition
    * @param parent the parent
    * @return the hash map
    * @throws Exception the exception
    */

   /*
    * Note - may return null, if there were no instances of the requested data
    */
   private HashMap<String, UUID> xDocLoaderHelper(String dockey,
         String niceName,
         boolean loadAsDefinition,
         UUID parent)
            throws Exception {
      final HashMap<String, UUID> result = new HashMap<>();

      ConsoleUtil.println("Creating '" + niceName + "' types");
      {
         try (Statement s = this.db.getConnection().createStatement();
            ResultSet rs = s.executeQuery(
                               "SELECT VALUE, TYPE, EXPL FROM " + this.tablePrefix + "DOC where DOCKEY='" + dockey +
                               "'")) {
            while (rs.next()) {
               final String value = rs.getString("VALUE");
               final String type  = rs.getString("TYPE");
               final String name  = rs.getString("EXPL");

               if (value == null) {
                  // there is a null entry, don't care about it.
                  continue;
               }

               if (!type.equals("expanded_form")) {
                  throw new RuntimeException("Unexpected type in the attribute data within DOC: '" + type + "'");
               }

               final UUID created = this.importUtil.createConcept(
                                        ConverterUUID.createNamespaceUUIDFromString(
                                            parent + ":" + (loadAsDefinition ? value
                     : name)),
                                        (loadAsDefinition ? value
                     : name),
                                        null,
                                        (loadAsDefinition ? null
                     : value),
                                        (loadAsDefinition ? name
                     : null),
                                        parent,
                                        null)
                                                   .getPrimordialUuid();

               result.put((loadAsDefinition ? value
                                            : name), created);

               if (!loadAsDefinition) {
                  result.put(value, created);
               }
            }
         }
      }

      if (result.isEmpty()) {
         // This can happen, depending on what is included during the metamorphosys run
         ConsoleUtil.println("No entries found for '" + niceName + "' - skipping");
         return null;
      }

      return result;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   protected ConverterUUID.NAMESPACE getNamespace() {
      return ConverterUUID.NAMESPACE.RXNORM;
   }

   /**
    * Checks if rel primary.
    *
    * @param relName the rel name
    * @param relaName the rela name
    * @return true, if rel primary
    */
   private boolean isRelPrimary(String relName, String relaName) {
      if (relaName != null) {
         return this.nameToRel.get(relaName)
                              .getFSNName()
                              .equals(relaName);
      } else {
         return this.nameToRel.get(relName)
                              .getFSNName()
                              .equals(relName);
      }
   }
}

