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
package sh.isaac.convert.mojo.rf2;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import static sh.isaac.api.logic.LogicalExpressionBuilder.SomeRole;
import static sh.isaac.api.logic.LogicalExpressionBuilder.SufficientSet;
import java.io.File;
import java.io.FileFilter;
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
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import javafx.application.Platform;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LanguageCode;
import sh.isaac.api.MavenConceptProxy;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
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
import sh.isaac.utility.MetaDataFinder;

/**
 * Loader code to convert RF2 files into isaac.
 */
@Mojo(name = "convert-RF2-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class RF2Mojo extends ConverterBaseMojo
{
   private IBDFCreationUtility importUtil_;
   private H2DatabaseHandle db_;
   private boolean outputJson = false;  // Set to true to produce a json dump file

   String contentNameVersion_;
   String timeString_;

   @SuppressWarnings("unused")
   private String CONCEPT, IDENTIFIER, RELATIONSHIP, STATED_RELATIONSHIP;
   private ArrayList<String> DESCRIPTIONS = new ArrayList<>();
   private ArrayList<String> LANGUAGES = new ArrayList<>();

   // Some constants from SCT
   // "Part of (attribute)"
   public static UUID PART_OF = UUID.fromString("b4c3f6f9-6937-30fd-8412-d0c77f8a7f73");

   // "Laterality (attribute)"
   public static UUID LATERALITY = UUID.fromString("26ca4590-bbe5-327c-a40a-ba56dc86996b");

   // "Has active ingredient (attribute)"
   public static UUID HAS_ACTIVE_INGREDIENT = UUID.fromString("65bf3b7f-c854-36b5-81c3-4915461020a8");

   // "Has dose form (attribute)"
   public static UUID HAS_DOSE_FORM = UUID.fromString("072e7737-e22e-36b5-89d2-4815f0529c63");

   protected static SimpleDateFormat dateParse = new SimpleDateFormat("yyyyMMdd");

   private HashMap<String, TableDefinition> tables_ = new HashMap<>();

   /**
    * The concept definition status cache. The map is from a concept UUID to a treemap that has the time and the status value.
    * TODO [KEC] the time is not sufficient to define time points. You need time, path, and module. Could shrink the size of this if necessary by
    * using con sequence ids....
    * Dan doesn't understand the comment above, as to my knowledge, the rf2 importer as I built it doesn't handle multiple modules during the same
    * execution.
    * And path never changes...
    */
   private HashMap<UUID, TreeMap<Long, UUID>> conceptDefinitionStatusCache = new HashMap<>();

   // This cache is to work around a data problem where stated rels are missing from SCT.
   private HashSet<UUID> consWithNoStatedRel = new HashSet<>();

   private HashSet<UUID> neverRoleGroupSet = new HashSet<>();
   {
      neverRoleGroupSet.add(PART_OF);
      neverRoleGroupSet.add(LATERALITY);
      neverRoleGroupSet.add(HAS_ACTIVE_INGREDIENT);
      neverRoleGroupSet.add(HAS_DOSE_FORM);
   }

   private InputType inputType = null;

   /**
    * Default value from SNOMED_CT_CORE_MODULE
    */
   @Parameter(required = false)
   private ConceptSpecification moduleUUID = MetaData.SNOMED_CT_CORE_MODULES____SOLOR;

   public void setModuleUUID(MavenConceptProxy conceptProxy)
   {
      moduleUUID = conceptProxy;
   }

   @Override
   public void execute() throws MojoExecutionException
   {
      try
      {
         super.execute();

         ConsoleUtil.println(
               "input moduleUUID: " + (moduleUUID == null ? "NULL!?!" : moduleUUID.getPrimordialUuid() + " [" + moduleUUID.getRegularName().get() + "]"));

         inputType = InputType.parse(converterOutputArtifactClassifier);

         File zipFile = init();
         loadDatabase(zipFile);

         ComponentReference rf2Metadata = ComponentReference.fromConcept(importUtil_.createConcept("RF2 Metadata " + contentNameVersion_, true));
         importUtil_.addParent(rf2Metadata, MetaData.SOLOR_CONTENT_METADATA____SOLOR.getPrimordialUuid());

         importUtil_.loadTerminologyMetadataAttributes(converterSourceArtifactVersion, Optional.ofNullable(timeString_), converterOutputArtifactVersion,
               Optional.of(converterOutputArtifactClassifier), converterVersion);

         // process content

         transformConcepts();
         transformDescriptions();
         // stated first, so we can know what doesn't get stated graphs
         transformRelationships(true);
         ConsoleUtil.println("Noticed " + consWithNoStatedRel.size() + " concepts with no stated relationships");
         transformRelationships(false);
         // can clear this cache now
         ConsoleUtil.println("After copying inferred rels, still " + consWithNoStatedRel.size() + " concepts with no stated relationships");
         consWithNoStatedRel.clear();

         ConsoleUtil.println("Dumping UUID Debug File");
         ConverterUUID.dump(outputDirectory, converterOutputArtifactClassifier + "-RF2UUID");

         ConsoleUtil.println("Load Statistics");
         for (String s : importUtil_.getLoadStats().getSummary())
         {
            ConsoleUtil.println(s);
         }

         // shutdown
         importUtil_.shutdown();
         db_.shutdown();

         ConsoleUtil.println("Finished converting " + contentNameVersion_ + "-" + converterOutputArtifactClassifier);
         ConsoleUtil.writeOutputToFile(new File(outputDirectory, converterOutputArtifactClassifier + "-ConsoleOutput.txt").toPath());
      }
      catch (Exception e)
      {
         throw new MojoExecutionException("Failure during conversion", e);
      }
   }

   /**
    * This will return batches of relationships, each item the iterator returns will be all of the relationships
    * for a particular source concepts, while each RelBatch within the list will be all versions of a particular relationship.
    * 
    * @param td
    */
   private Iterator<ArrayList<RelBatch>> getRelationships(String table, TableDefinition td) throws SQLException
   {
      PreparedStatement ps = db_.getConnection().prepareStatement("Select * from " + table + " order by sourceid, id");
      ResultSet rs = ps.executeQuery();

      Iterator<ArrayList<RelBatch>> iter = new Iterator<ArrayList<RelBatch>>()
      {
         RelBatch relBatchWorking = null;
         ArrayList<RelBatch> conceptRelsWorking = new ArrayList<>();
         ArrayList<RelBatch> conceptRelsNextReady = null;

         @Override
         public boolean hasNext()
         {
            if (conceptRelsNextReady == null)
            {
               read();
            }
            if (conceptRelsNextReady == null)
            {
               try
               {
                  rs.close();
               }
               catch (SQLException e)
               {
                  // noop
               }
            }
            return conceptRelsNextReady != null;
         }

         @Override
         public ArrayList<RelBatch> next()
         {
            if (conceptRelsNextReady == null)
            {
               read();
            }
            ArrayList<RelBatch> temp = conceptRelsNextReady;
            conceptRelsNextReady = null;
            return temp;
         }

         private void read()
         {

            try
            {
               while ((conceptRelsNextReady == null) && rs.next())
               {
                  Rel r = new Rel(rs, td);
                  if (relBatchWorking == null)
                  {
                     relBatchWorking = new RelBatch(r);
                  }
                  else if (relBatchWorking.getBatchId().equals(r.id))
                  {
                     relBatchWorking.addRel(r);
                  }
                  else  // different batchId than previous - need a new RelBatch. Move last relBatch into conceptRels.
                  {
                     if (conceptRelsWorking.size() > 0 && !conceptRelsWorking.get(0).getSourceId().equals(relBatchWorking.getSourceId()))
                     {
                        conceptRelsNextReady = conceptRelsWorking;
                        conceptRelsWorking = new ArrayList<>();
                     }
                     conceptRelsWorking.add(relBatchWorking);

                     // Put this rel into a new batch.
                     relBatchWorking = new RelBatch(r);
                  }
               }
            }
            catch (SQLException | ParseException e)
            {
               throw new RuntimeException(e);
            }

            if (conceptRelsNextReady != null)
            {
               return;
            }

            if (conceptRelsWorking.size() > 0 && !conceptRelsWorking.get(0).getSourceId().equals(relBatchWorking.getSourceId()))
            {
               conceptRelsNextReady = conceptRelsWorking;
               conceptRelsWorking = new ArrayList<>();
               return;
            }

            // If we get here, the only thing left is the last relBatch.
            if (relBatchWorking != null)
            {
               conceptRelsWorking.add(relBatchWorking);
               conceptRelsNextReady = conceptRelsWorking;
               relBatchWorking = null;
               conceptRelsWorking = new ArrayList<>();
            }
         }
      };

      return iter;
   }

   private void transformRelationships(boolean stated) throws SQLException
   {
      ConsoleUtil.println("Converting " + (stated ? "stated" : "inferred") + " relationships into graphs");
      String table = (stated ? STATED_RELATIONSHIP : RELATIONSHIP);

      TableDefinition td = tables_.get(table);

      int graphCount = 0;

      Iterator<ArrayList<RelBatch>> rels = getRelationships(table, td);
      UUID lastId = null;

      while (rels.hasNext())
      {
         // each Rel here will be for the same sourceId.
         graphCount++;

         ArrayList<RelBatch> conRels = rels.next();
         long newestRelTime = 0;

         LogicalExpressionBuilder leb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
         ArrayList<Assertion> assertions = new ArrayList<>();
         HashMap<String, ArrayList<Assertion>> groupedAssertions = new HashMap<>();

         // Each member of a RelBatch contains the same rel ID (so different versions of the same rel)
         for (RelBatch rb : conRels)
         {
            if (!rb.isActiveNow())
            {
               // TODO handle historical relationships
            }
            else
            {
               // TODO handle history - only loading latest for now.
               Rel r = rb.getRels().last();
               
               if (inputType == InputType.DELTA) {
                  //make sure nids are assigned for the load process
                  Get.identifierService().assignNid(r.destinationId);
                  Get.identifierService().assignNid(r.sourceId);
                  Get.identifierService().assignNid(r.typeId);
               }

               if ((stated && r.characteristicTypeId.equals(MetaData.INFERRED_PREMISE_TYPE____SOLOR.getPrimordialUuid()))
                     || (!stated && r.characteristicTypeId.equals(MetaData.STATED_PREMISE_TYPE____SOLOR.getPrimordialUuid())))
               {
                  throw new RuntimeException("Unexpected - table type and characteristic type do not match!");
               }

               if (r.characteristicTypeId.equals(MetaData.INFERRED_PREMISE_TYPE____SOLOR.getPrimordialUuid())
                     || r.characteristicTypeId.equals(MetaData.STATED_PREMISE_TYPE____SOLOR.getPrimordialUuid()))
               {
                  if (r.effectiveTime > newestRelTime)
                  {
                     newestRelTime = r.effectiveTime;
                  }
                  if (r.relGroup.trim().equals("0"))
                  {
                     // Don't just check primordial, IS_A has multiple UUIDs
                     if (Arrays.stream(MetaData.IS_A____SOLOR.getUuids()).anyMatch(uuid -> uuid.equals(r.typeId)))
                     {
                        assertions.add(ConceptAssertion(Get.identifierService().getNidForUuids(r.destinationId), leb));
                     }
                     else
                     {
                        // TODO [graph] ask Keith about the never group stuff.
                        // TODO [graph] handle modifier?
                        // TODO [graph] handle sctid
                        // TODO [graph] handle id
                        // TODO [graph] maintain actual group numbers?
                        if (neverRoleGroupSet.contains(r.typeId))
                        {
                           assertions.add(SomeRole(Get.identifierService().getNidForUuids(r.typeId),
                                 ConceptAssertion(Get.identifierService().getNidForUuids(r.destinationId), leb)));
                        }
                        else
                        {
                           assertions
                                 .add(SomeRole(MetaData.ROLE_GROUP____SOLOR.getNid(), And(SomeRole(Get.identifierService().getNidForUuids(r.typeId),
                                       ConceptAssertion(Get.identifierService().getNidForUuids(r.destinationId), leb)))));
                        }
                     }
                  }
                  else
                  {
                     ArrayList<Assertion> groupAssertions = groupedAssertions.get(r.relGroup.trim());
                     if (groupAssertions == null)
                     {
                        groupAssertions = new ArrayList<>();
                        groupedAssertions.put(r.relGroup.trim(), groupAssertions);
                     }
                     groupAssertions.add(SomeRole(Get.identifierService().getNidForUuids(r.typeId),
                           ConceptAssertion(Get.identifierService().getNidForUuids(r.destinationId), leb)));
                  }
               }
               else
               {
                  // kick it over into an association bucket
                  // TODO should I toss these when processing inferred?
                  SemanticChronology assn = importUtil_.addAssociation(ComponentReference.fromConcept(r.sourceId), r.id, r.destinationId, r.typeId,
                        r.isActive ? Status.ACTIVE : Status.INACTIVE, r.effectiveTime, r.moduleId);
                  // TODO put on modifier, group

                  if (r.sctID != null && !r.id.equals(lastId))
                  {
                     importUtil_.addStaticStringAnnotation(ComponentReference.fromChronology(assn, () -> "Association"), r.sctID + "",
                           MetaData.SCTID____SOLOR.getPrimordialUuid(), Status.ACTIVE);
                  }

               }
               lastId = r.id;
            }
         }

         // handle relationship groups
         for (ArrayList<Assertion> groupAssertions : groupedAssertions.values())
         {
            assertions.add(SomeRole(MetaData.ROLE_GROUP____SOLOR.getNid(), And(groupAssertions.toArray(new Assertion[groupAssertions.size()]))));
         }

         if (assertions.size() > 0)
         {
            Boolean defined = null;
            TreeMap<Long, UUID> conDefStatus = conceptDefinitionStatusCache.get(conRels.get(0).getSourceId());
            if (conDefStatus == null)
            {
               // Try Frills - in the case of US Extension, we should have SCT loaded, pull from that.
               // Can definition status vary between stated and inferred? Just read stated for now.
               Optional<Boolean> sctDefined = Frills.isConceptFullyDefined(ComponentReference.fromConcept(conRels.get(0).getSourceId()).getNid(), true);
               if (sctDefined.isPresent())
               {
                  defined = sctDefined.get();
               }
               else
               {
                  ConsoleUtil.printErrorln("No definition status found!");
               }
            }
            else
            {
               if (TermAux.SUFFICIENT_CONCEPT_DEFINITION.isIdentifiedBy(conDefStatus.lastEntry().getValue()))
               {
                  defined = true;
               }
               else if (TermAux.NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION.isIdentifiedBy(conDefStatus.lastEntry().getValue()))
               {
                  defined = false;
               }
               else
               {
                  throw new RuntimeException("Unexpected concept definition status: " + conDefStatus.lastEntry());
               }
            }
            if (defined != null)
            {
               if (defined.booleanValue())
               {
                  SufficientSet(And(assertions.toArray(new Assertion[assertions.size()])));
               }
               else
               {
                  NecessarySet(And(assertions.toArray(new Assertion[assertions.size()])));
               }

               LogicalExpression le = leb.build();

               if (le.isMeaningful())
               {
                  if (newestRelTime == 0)
                  {
                     throw new RuntimeException("Time sort failure!");
                  }
                  // TODO [graph] what if the modules are different across the graph rels?
                  importUtil_.addRelationshipGraph(ComponentReference.fromConcept(conRels.get(0).getSourceId()), null, le, stated, newestRelTime,
                        conRels.get(0).getRels().first().moduleId);

                  if (!stated && consWithNoStatedRel.contains(conRels.get(0).getSourceId()))
                  {
                     // substitute inferred expression, as early SNOMED stated expressions where lost.
                     importUtil_.addRelationshipGraph(ComponentReference.fromConcept(conRels.get(0).getSourceId()), null, le, true, newestRelTime,
                           conRels.get(0).getRels().first().moduleId);
                  }
                  consWithNoStatedRel.remove(conRels.get(0).getSourceId());
               }
               else
               {
                  ConsoleUtil.printErrorln("expression not meaningful?");
               }
            }
         }
         else
         {
            // TODO [graph] ask Keith about these cases where no associations get generated.
         }

         if (graphCount % 1000 == 0)
         {
            ConsoleUtil.showProgress();
         }
         if (graphCount % 25000 == 0)
         {
            ConsoleUtil.println("Processed " + graphCount + " relationship graphs...");
         }

      }
      ConsoleUtil.println("Created " + graphCount + " graphs");

   }

   private void transformDescriptions() throws SQLException, ParseException, MojoExecutionException
   {
      ConsoleUtil.println("Converting descriptions");
      for (String DESCRIPTION : DESCRIPTIONS)
      {
         TableDefinition descriptionTable = tables_.get(DESCRIPTION);

         String lang = DESCRIPTION.split("_")[3];
         String LANGUAGE = null;
         for (String s : LANGUAGES)
         {
            if (s.split("_")[3].equals(lang))
            {
               LANGUAGE = s;
               break;
            }
         }
         if (LANGUAGE == null)
         {
            throw new MojoExecutionException("Failed to find the language table for the language: " + lang);
         }
         TableDefinition acceptabilityTable = tables_.get(LANGUAGE);

         ConsoleUtil.println("Processing " + descriptionTable.getTableName() + ", " + acceptabilityTable.getTableName());

         int descCount = 0;
         int accCount = 0;
         PreparedStatement ps = db_.getConnection().prepareStatement("Select * from " + DESCRIPTION + " order by conceptId, id");
         PreparedStatement ps2 = db_.getConnection().prepareStatement("Select * from " + LANGUAGE + " where referencedComponentId = ? ");
         UUID lastId = null;
         ResultSet descRS = ps.executeQuery();
         while (descRS.next())
         {
            descCount++;
            Long sctID = null;
            UUID id;
            if (descriptionTable.getColDataType("ID").isLong())
            {
               sctID = descRS.getLong("ID");
               id = UuidT3Generator.fromSNOMED(sctID);
            }
            else
            {
               id = UUID.fromString(descRS.getString("ID"));
            }
            long time = dateParse.parse(descRS.getString("EFFECTIVETIME")).getTime();
            boolean active = descRS.getBoolean("ACTIVE");
            UUID moduleId = (descriptionTable.getColDataType("MODULEID").isLong() ? UuidT3Generator.fromSNOMED(descRS.getLong("MODULEID"))
                  : UUID.fromString(descRS.getString("MODULEID")));
            UUID conceptId = (descriptionTable.getColDataType("CONCEPTID").isLong() ? UuidT3Generator.fromSNOMED(descRS.getLong("CONCEPTID"))
                  : UUID.fromString(descRS.getString("CONCEPTID")));
            String languageCode = descRS.getString("LANGUAGECODE");
            UUID typeId = (descriptionTable.getColDataType("TYPEID").isLong() ? UuidT3Generator.fromSNOMED(descRS.getLong("TYPEID"))
                  : UUID.fromString(descRS.getString("TYPEID")));
            String term = descRS.getString("TERM");
            UUID caseSigId = (descriptionTable.getColDataType("CASESIGNIFICANCEID").isLong()
                  ? UuidT3Generator.fromSNOMED(descRS.getLong("CASESIGNIFICANCEID"))
                  : UUID.fromString(descRS.getString("CASESIGNIFICANCEID")));

            if (inputType == InputType.DELTA) 
            {
               //Need to make sure the concept has a nid, because we probably didn't load it here.
               Get.identifierService().assignNid(conceptId);
            }
            
            SemanticChronology desc = importUtil_.addDescription(ComponentReference.fromConcept(conceptId), id, term, DescriptionType.parse(typeId), null,
                  null, caseSigId, LanguageMap.getConceptForLanguageCode(LanguageCode.getLangCode(languageCode)).getPrimordialUuid(), moduleId, null,
                  active ? Status.ACTIVE : Status.INACTIVE, time);

            // add SCTID if this is the first sighting
            if (sctID != null && !id.equals(lastId))
            {
               lastId = id;
               importUtil_.addStaticStringAnnotation(ComponentReference.fromChronology(desc), sctID + "", MetaData.SCTID____SOLOR.getPrimordialUuid(),
                     Status.ACTIVE);
            }

            ps2.clearParameters();
            if (acceptabilityTable.getColDataType("referencedComponentId").isLong())
            {
               if (sctID == null)
               {
                  throw new RuntimeException("type mismatch!");
               }
               ps2.setLong(1, sctID);
            }
            else
            {
               ps2.setString(1, id.toString());
            }
            ResultSet langRS = ps2.executeQuery();
            boolean foundAcceptability = false;
            while (langRS.next())
            {
               accCount++;
               foundAcceptability = true;

               UUID acceptID = UUID.fromString(langRS.getString("id"));
               long acceptTime = dateParse.parse(langRS.getString("EFFECTIVETIME")).getTime();
               boolean acceptActive = langRS.getBoolean("ACTIVE");
               UUID acceptModuleId = (acceptabilityTable.getColDataType("MODULEID").isLong() ? UuidT3Generator.fromSNOMED(langRS.getLong("MODULEID"))
                     : UUID.fromString(langRS.getString("MODULEID")));
               UUID refsetId = (acceptabilityTable.getColDataType("refsetID").isLong() ? UuidT3Generator.fromSNOMED(langRS.getLong("refsetID"))
                     : UUID.fromString(langRS.getString("refsetID")));
               UUID acceptabilityId = (acceptabilityTable.getColDataType("acceptabilityId").isLong()
                     ? UuidT3Generator.fromSNOMED(langRS.getLong("acceptabilityId"))
                     : UUID.fromString(langRS.getString("acceptabilityId")));

               boolean preferred;
               if (MetaData.ACCEPTABLE____SOLOR.getPrimordialUuid().equals(acceptabilityId))
               {
                  preferred = false;
               }
               else if (MetaData.PREFERRED____SOLOR.getPrimordialUuid().equals(acceptabilityId))
               {
                  preferred = true;
               }
               else
               {
                  throw new RuntimeException("Unexpected acceptibility: " + acceptabilityId);
               }

               importUtil_.addDescriptionAcceptability(ComponentReference.fromChronology(desc), acceptID, refsetId, preferred,
                     acceptActive ? Status.ACTIVE : Status.INACTIVE, acceptTime, acceptModuleId);

            }
            if (inputType != InputType.DELTA && !foundAcceptability)
            {
               ConsoleUtil.printErrorln("No acceptibility found for: " + id + " " + sctID);
            }

            if (descCount % 1000 == 0)
            {
               ConsoleUtil.showProgress();
            }
            if (descCount % 25000 == 0)
            {
               ConsoleUtil.println("Processed " + descCount + " descriptions with " + accCount + " acceptabilities...");
            }

         }
         ConsoleUtil.println("Converted " + descCount + " descriptions");
      }

   }

   private void transformConcepts() throws SQLException, ParseException
   {
      ConsoleUtil.println("Converting concepts");
      TableDefinition td = tables_.get(CONCEPT);

      int conCount = 0;
      PreparedStatement ps = db_.getConnection().prepareStatement("Select * from " + CONCEPT + " order by id");
      UUID lastId = null;
      ResultSet rs = ps.executeQuery();
      while (rs.next())
      {
         conCount++;
         Long sctID = null;
         UUID id, moduleId, definitionStatusId;
         if (td.getColDataType("ID").isLong())
         {
            sctID = rs.getLong("ID");
            id = UuidT3Generator.fromSNOMED(sctID);
         }
         else
         {
            id = UUID.fromString(rs.getString("ID"));
         }

         consWithNoStatedRel.add(id);

         long time = dateParse.parse(rs.getString("EFFECTIVETIME")).getTime();
         boolean active = rs.getBoolean("ACTIVE");
         moduleId = (td.getColDataType("MODULEID").isLong() ? UuidT3Generator.fromSNOMED(rs.getLong("MODULEID"))
               : UUID.fromString(rs.getString("MODULEID")));
         Get.identifierService().assignNid(moduleId);
         definitionStatusId = (td.getColDataType("DEFINITIONSTATUSID").isLong() ? UuidT3Generator.fromSNOMED(rs.getLong("DEFINITIONSTATUSID"))
               : UUID.fromString(rs.getString("DEFINITIONSTATUSID")));
         Get.identifierService().assignNid(definitionStatusId);

         TreeMap<Long, UUID> conDefStatus = conceptDefinitionStatusCache.get(id);
         if (conDefStatus == null)
         {
            conDefStatus = new TreeMap<>();
            conceptDefinitionStatusCache.put(id, conDefStatus);
         }
         UUID oldValue = conDefStatus.put(time, definitionStatusId);
         if (oldValue != null && !oldValue.equals(definitionStatusId))
         {
            throw new RuntimeException("Unexpeted - multiple definition status values at the same time: " + sctID + " " + id + " " + definitionStatusId);
         }

         ConceptVersion con = importUtil_.createConcept(id, time, active ? Status.ACTIVE : Status.INACTIVE, moduleId);
         if (sctID != null && !id.equals(lastId))
         {
            lastId = id;
            importUtil_.addStaticStringAnnotation(ComponentReference.fromConcept(con), sctID + "", MetaData.SCTID____SOLOR.getPrimordialUuid(),
                  Status.ACTIVE);
         }
         if (conCount % 1000 == 0)
         {
            ConsoleUtil.showProgress();
         }
         if (conCount % 25000 == 0)
         {
            ConsoleUtil.println("Processed " + conCount + " concepts...");
         }

      }
      ConsoleUtil.println("Converted " + conCount + " concepts");
   }

   private void loadDatabase(File zipFile) throws Exception
   {
      long time = System.currentTimeMillis();
      db_ = new H2DatabaseHandle();

      File dbFile = new File(outputDirectory, contentNameVersion_ + "-" + converterOutputArtifactClassifier + ".h2.db");
      boolean createdNew = db_.createOrOpenDatabase(new File(outputDirectory, contentNameVersion_ + "-" + converterOutputArtifactClassifier));

      if (!createdNew)
      {
         ConsoleUtil.println("Using existing database.  To load from scratch, delete the file '" + dbFile.getCanonicalPath() + ".*'");
      }
      ZipFile zf = new ZipFile(zipFile);

      Enumeration<? extends ZipEntry> zipEntries = zf.entries();
      int tableCount = 0;
      while (zipEntries.hasMoreElements())
      {
         ZipEntry ze = zipEntries.nextElement();
         String[] structure = ze.getName().split("\\/");
         if ((structure[0].toUpperCase().equals(inputType.name()) || (structure.length > 1 && structure[1].toUpperCase().equals(inputType.name()))
               || (structure.length > 2 && structure[2].toUpperCase().equals(inputType.name()))) && ze.getName().toLowerCase().endsWith(".txt"))
         {
            // One of the data files we want to load
            ConsoleUtil.println("Loading " + ze.getName());

            RF2FileReader fileReader = new RF2FileReader(zf.getInputStream(ze));

            String tableName = structure[structure.length - 1];
            tableName = tableName.substring(0, tableName.length() - 4);
            tableName = tableName.replaceAll("-", "_");  // hyphens cause sql issues

            if (tableName.toLowerCase().startsWith("sct2_concept_"))
            {
               CONCEPT = tableName;
            }
            else if (tableName.toLowerCase().startsWith("sct2_description_") || tableName.toLowerCase().startsWith("sct2_textdefinition_"))
            {
               DESCRIPTIONS.add(tableName);
            }
            else if (tableName.toLowerCase().startsWith("der2_crefset_") && tableName.toLowerCase().contains("language"))
            {
               LANGUAGES.add(tableName);
            }
            else if (tableName.toLowerCase().startsWith("sct2_identifier_"))
            {
               IDENTIFIER = tableName;
            }
            else if (tableName.toLowerCase().startsWith("sct2_relationship_"))
            {
               RELATIONSHIP = tableName;
            }
            else if (tableName.toLowerCase().startsWith("sct2_statedrelationship_"))
            {
               STATED_RELATIONSHIP = tableName;
            }

            TableDefinition td = createTableDefinition(tableName, fileReader.getHeader(), fileReader.peekNextRow());
            tables_.put(tableName, td);

            tableCount++;
            
            if (!createdNew)
            {
               // Only need to process this far to read the metadata about the DB
               continue;
            }

            db_.createTable(td);

            int rowCount = db_.loadDataIntoTable(td, fileReader);
            fileReader.close();

            // don't bother indexing small tables
            if (rowCount > 10000)
            {
               HashSet<String> colsToIndex = new HashSet<String>();
               colsToIndex.add("conceptId");
               colsToIndex.add("referencedComponentId");
               colsToIndex.add("sourceId");

               for (String s : fileReader.getHeader())
               {
                  if (colsToIndex.contains(s))
                  {
                     Statement statement = db_.getConnection().createStatement();
                     ConsoleUtil.println("Indexing " + tableName + " on " + s);
                     if (s.equals("referencedComponentId"))
                     {
                        statement.execute("CREATE INDEX " + tableName + "_" + s + "_index ON " + tableName + " (" + s + ", refsetId)");
                     }
                     else
                     {
                        if (td.getColDataType("id") != null)
                        {
                           statement.execute("CREATE INDEX " + tableName + "_" + s + "_index ON " + tableName + " (" + s + ", id)");
                        }
                        else
                        {
                           statement.execute("CREATE INDEX " + tableName + "_" + s + "_index ON " + tableName + " (" + s + ")");
                        }
                     }
                     statement.close();
                  }
               }
            }

         }
      }
      zf.close();

      ConsoleUtil.println("Processing DB loaded " + tableCount + " tables in " + ((System.currentTimeMillis() - time) / 1000) + " seconds");
      if (tableCount == 0)
      {
         throw new RuntimeException("Failed to find tables in zip file!");
      }
   }

   private TableDefinition createTableDefinition(String tableName, String[] header, String[] sampleDataRow)
   {
      TableDefinition td = new TableDefinition(tableName);

      for (int i = 0; i < header.length; i++)
      {
         DataType dataType;
         if (header[i].equals("id") || header[i].endsWith("Id"))
         {
            // See if this looks like a UUID or a long
            try
            {
               Long.parseLong(sampleDataRow[i]);
               dataType = new DataType(DataType.SUPPORTED_DATA_TYPE.LONG, null, false);
            }
            catch (Exception e)  // Might be a null pointer if there is no data, just treat it as a string (doesn't matter)
            {
               // UUID
               dataType = new DataType(DataType.SUPPORTED_DATA_TYPE.STRING, 36, false);
            }
         }
         else if (header[i].equals("active"))
         {
            dataType = new DataType(DataType.SUPPORTED_DATA_TYPE.BOOLEAN, null, false);
         }
         else if (header[i].equals("effectiveTime") || header[i].equals("sourceEffectiveTime") || header[i].equals("targetEffectiveTime"))
         {
            dataType = new DataType(DataType.SUPPORTED_DATA_TYPE.STRING, 8, false);
         }
         else
         {
            dataType = new DataType(DataType.SUPPORTED_DATA_TYPE.STRING, null, true);
            ConsoleUtil.println("Treating " + header[i] + " as a string");
         }

         ColumnDefinition cd = new ColumnDefinition(header[i], dataType);
         td.addColumn(cd);
      }

      return td;
   }

   private File init() throws Exception
   {
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

      contentNameVersion_ = zipFile.getName().substring(0, zipFile.getName().length() - 4);
      ConsoleUtil.println("Converting " + contentNameVersion_ + "-" + converterOutputArtifactClassifier);

      String[] temp = contentNameVersion_.split("_");

      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
      SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
      SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US);
      sdf3.setTimeZone(TimeZone.getTimeZone("UTC"));
      long defaultTime = 0;
      for (int i = temp.length - 1; i > 0; i--)
      {
         if (temp[i].length() == 8 && NumberUtils.isDigits(temp[i]))
         {
            defaultTime = sdf.parse(temp[i]).getTime();
            timeString_ = temp[i];
            break;
         }
         // in 2017, they changed the date format to 20170131T120000 (and then later added a Z....)
         else if (temp[i].length() == 15 && NumberUtils.isDigits(temp[i].substring(0, 8)))
         {
            defaultTime = sdf2.parse(temp[i]).getTime();
            timeString_ = temp[i];
            break;
         }
         else if (temp[i].length() == 16 && NumberUtils.isDigits(temp[i].substring(0, 8)))
         {
            defaultTime = sdf3.parse(temp[i]).getTime();
            timeString_ = temp[i];
            break;
         }
      }

      if (defaultTime == 0)
      {
         throw new MojoExecutionException("Couldn't parse date out of " + contentNameVersion_);
      }

      clearTargetFiles(contentNameVersion_);

      File[] ibdfFiles = new File[0];
      File ibdfFolder = new File(inputFileLocation, "ibdf");
      if (ibdfFolder.isDirectory())
      {
         ibdfFiles = ibdfFolder.listFiles(new FileFilter()
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
      }

      if (moduleUUID.getPrimordialUuid() == null)
      {
         throw new MojoExecutionException("The module UUID must be provided.");
      }

      String modulePrefix = moduleUUID.getRegularName().get();

      // There is a design issue with the way Prisme builds the config file - in that the description field doesn't get populated.
      // But if the UUID is for a constant we know about, we need to go get the proper designation for the logic below to work properly.
      ConceptSpecification cs = MetaDataFinder.findConstant(moduleUUID.getPrimordialUuid());
      if (cs == null && StringUtils.isBlank(modulePrefix))
      {
         throw new RuntimeException("Couldn't find the constant for '" + moduleUUID.getPrimordialUuid() + "' and the description wasn't provided");
      }
      if (StringUtils.isBlank(modulePrefix))
      {
         modulePrefix = cs.getRegularName().get();
      }
      else if (!modulePrefix.equals(cs.getRegularName().get()))
      {
         throw new RuntimeException("The specified module description of '" + modulePrefix + "' does not equal the constant of '" + cs.getRegularName().get()
               + "'.  This should be resolved.");
      }

      if (modulePrefix.endsWith(" modules"))
      {
         modulePrefix = modulePrefix.substring(0, modulePrefix.length() - " modules".length());
      }
      importUtil_ = new IBDFCreationUtility(Optional.of(modulePrefix + " " + converterSourceArtifactVersion), Optional.of(moduleUUID), outputDirectory,
            converterOutputArtifactId, converterOutputArtifactVersion, converterOutputArtifactClassifier, outputJson, defaultTime,
            Arrays.asList(new VersionType[] { VersionType.DESCRIPTION, VersionType.COMPONENT_NID, VersionType.DYNAMIC, VersionType.LONG }), true,
            ibdfFiles);

      return zipFile;
   }

   private void clearTargetFiles(String contentNameVersion)
   {
      new File(outputDirectory, converterOutputArtifactClassifier + "-RF2UUIDDebugMap.txt").delete();
      new File(outputDirectory, converterOutputArtifactClassifier + "-ConsoleOutput.txt").delete();
      new File(outputDirectory, "RF2-" + contentNameVersion + "-" + converterOutputArtifactClassifier + ".ibdf").delete();
      // For debug only, normally commented out
      // new File(outputDirectory, contentNameVersion + ".h2.db").delete();
   }

   public static void main(String[] args) throws MojoExecutionException
   {
      RF2Mojo mojo = new RF2Mojo();
      mojo.outputDirectory = new File("../../integration/db-config-builder-ui/target/converter-executor/target/");
      mojo.inputFileLocation= new File("../../integration/db-config-builder-ui/target/converter-executor/target/generated-resources/src");
      mojo.converterVersion = "foo";
      mojo.converterOutputArtifactVersion = "bar";
      mojo.converterOutputArtifactClassifier = "Delta";  //Use this to switch which one is loaded...
      mojo.converterSourceArtifactVersion = "bar";
      mojo.execute();
      Platform.exit();
   }
}
