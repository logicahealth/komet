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



package sh.isaac.convert.rxnorm.solor;

//~--- JDK imports ------------------------------------------------------------

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

//~--- non-JDK imports --------------------------------------------------------

import javafx.util.Pair;

import org.apache.maven.plugin.MojoExecutionException;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Util;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.task.TimedTask;
import sh.isaac.converters.sharedUtils.ConsoleUtil;
import sh.isaac.converters.sharedUtils.ConverterBaseMojo;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.converters.sharedUtils.umlsUtils.rrf.REL;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.LiteralNodeFloat;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.FeatureNodeWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;
import sh.isaac.rxnorm.rrf.RXNCONSO;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.Feature;
import static sh.isaac.api.logic.LogicalExpressionBuilder.FloatLiteral;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;
import static sh.isaac.api.logic.LogicalExpressionBuilder.SomeRole;
import sh.isaac.api.index.IndexQueryService;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.MutableLogicGraphVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DynamicVersion;

//~--- classes ----------------------------------------------------------------

/**
 * The Class RxNormLogicGraphsMojo.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service(name = "convert-rxnorm-to-solor-ibdf")
public class RxNormLogicGraphsMojo
        extends ConverterBaseMojo {
// int availStrengthCount = 0;
// int newLogicGraphs = 0;
// int modifiedLogicGraphs = 0;
// int errors = 0;
// 
// private PreparedStatement ingredSubstanceMergeCheck, scdProductMergeCheck, scdgToSCTIngredient;
// 
// 
// private AtomicInteger ingredSubstanceMerge_ = new AtomicInteger();
// private AtomicInteger scdProductMerge_ = new AtomicInteger();
// private AtomicInteger ingredSubstanceMergeDupeFail_ = new AtomicInteger();
// private AtomicInteger scdProductMergeDupeFail_ = new AtomicInteger();
// private AtomicInteger convertedTradenameCount_ = new AtomicInteger();
// private AtomicInteger scdgToSCTIngredientCount_ = new AtomicInteger();
// private AtomicInteger doseFormMappedItemsCount_ = new AtomicInteger();
// 
// private HashMap<String, DoseForm> doseFormMappings_ = new HashMap<>();  //rxCUI to DoseForm
// 
// 
// private HashMap<String, Optional<UUID>> mergeCache_ = new HashMap<>();  //rxcui to existing SCT concept
// 
// 
// @Override
// public void execute() throws MojoExecutionException
// {
   // super.execute();
//         getLog().info("RxNorm Logic Graph Processing Begins " + new Date().toString());
//         
//         Optional<UUID> mergeOntoSCTConceptUUID = sctMergeCheck(rxCui);
//         
//         if (mergeOntoSCTConceptUUID.isPresent())
//         {
//                 //Add the UUID we would have generated  
//                 //TODO need to debug, this doesn't seem to be working.
//                 importUtil_.addUUID(cuiConcept, cuiBasedUUID, ptSABs_.getProperty("RXNORM").getUUID());
//         }
//         
//         ConsoleUtil.println("Reading dose form mapping file");
//         DoseFormMapping.readDoseFormMapping().forEach(df ->
//         {
//                 doseFormMappings_.put(df.rxcui, df);
//         });
//         ConsoleUtil.println("Read " + doseFormMappings_.size() + " dose form mappings");
//         
//         
//         //Make special units concepts
//         for (UNIT u : UNIT.values())
//         {
//                 if (!u.hasRealSCTConcept())
//                 {
//                         importUtil_.createMetaDataConcept(u.getConceptUUID(), u.getFullName(), u.getFullName(), null, null, annotations.getPropertyTypeUUID(), null, null, dos_);
//                 }
//         }
//         
//         ingredSubstanceMergeCheck = db_.getConnection().prepareStatement("SELECT DISTINCT r2.code FROM RXNCONSO r1, RXNCONSO r2"
//                 + " WHERE r1.RXCUI = ?"
//                 + " AND r1.TTY='IN' AND r2.rxcui = r1.rxcui AND r2.sab='" + sctSab_ + "'" 
//                 + " AND r2.STR like '% (substance)'");
// 
// scdProductMergeCheck = db_.getConnection().prepareStatement("SELECT DISTINCT r2.code FROM RXNCONSO r1, RXNCONSO r2"
//                 + " WHERE r1.RXCUI = ?"
//                 + " AND r1.TTY='SCD' AND r2.rxcui = r1.rxcui AND r2.sab='" + sctSab_ + "'" 
//                 + " AND r2.STR like '% (product)'");
// 
// //See doc in addCustomRelationships
// scdgToSCTIngredient_ = db_.getConnection().prepareStatement("SELECT conso_2.code from RXNREL, RXNCONSO as conso_1, RXNCONSO as conso_2" 
//                 + " where RXCUI2=? and RELA='has_ingredient'"
//                 + " and RXNREL.RXCUI1 = conso_1.RXCUI and conso_1.SAB = 'RXNORM' and conso_1.TTY='IN'"
//                 + " and conso_2.RXCUI = RXNREL.RXCUI1 and conso_2.SAB='SNOMEDCT_US' and conso_2.STR like '%(product)' and conso_2.TTY = 'FN'");
//         
//         
//         TimedTask<Void> task = new Worker();
//         LookupService.getService(WorkExecutors.class).getExecutor().submit(task);
//         try
//         {
//                 Util.addToTaskSetAndWaitTillDone(task);
//         }
//         catch (InterruptedException | ExecutionException e)
//         {
//                 throw new MojoExecutionException("Failure", e);
//         }
//
//         getLog().info("RxNorm Logic Graph Processing Ends " + new Date().toString());
//         
//         getLog().info("Processed " + availStrengthCount + " strength annotations");
//         getLog().info("Created " + newLogicGraphs + " new logic graphs");
//         getLog().info("Modified " + modifiedLogicGraphs + " existing logic graphs");
//         getLog().info("Had errors processing  " + errors + " annotations");
// }
// 
// private class Worker extends TimedTask<Void>
// {
//         @SuppressWarnings("deprecation")
//         @Override
//         protected Void call() throws Exception
//         {
//                 getLog().info("Processing RxNorm Concrete Domains");
//                 updateTitle("Processing RxNorm Concrete Domains");
//                 updateMessage("Building Logic Graphs");
//                 updateProgress(1, 3);
//                 
//                 EditCoordinate ec = Get.configurationService().getDefaultEditCoordinate();
//                 //TODO find constant
//                 ConceptChronology unitConcept = Get.conceptService().getConcept(UUID.fromString("17055d89-84e3-3e12-9fb1-1bc4c75a122d"));  //Units (attribute)
//                 
//                 LogicalExpressionBuilderService expressionBuilderService = LookupService.getService(LogicalExpressionBuilderService.class);
//                 
//                 //Need to gather per concept, as some concepts have multiple instances of this assemblage
//                 
//                 HashMap<Integer, ArrayList<String>> entries = new HashMap<Integer, ArrayList<String>>();  //con nid to values
//                 Get.sememeService().getSememesFromAssemblage(findAssemblageNid("RXN_AVAILABLE_STRENGTH")).forEach(sememe ->
//                 {
//                         availStrengthCount++;
//                         try
//                         {
//                                 @SuppressWarnings({ "rawtypes", "unchecked" })
//                                 Optional<LatestVersion<DynamicSememe>> ds = ((SemanticChronology)sememe).getLatestVersion(DynamicVersion.class, Get.configurationService().getDefaultStampCoordinate());
//                                 if (ds.isPresent())
//                                 {
//                                         @SuppressWarnings("rawtypes")
//                                         DynamicVersion dsv = ds.get().value();
//                                         int descriptionSememe = dsv.getReferencedComponentNid();
//                                         int conceptNid = Get.sememeService().getSememe(descriptionSememe).getReferencedComponentNid();
//                                         String value = dsv.getData()[0].getDataObject().toString();
//                                         String[] multipart = value.split(" / ");
//                                         
//                                         ArrayList<String> itemEntries = entries.get(conceptNid);
//                                         if (itemEntries == null)
//                                         {
//                                                 itemEntries = new ArrayList<>();
//                                                 entries.put(conceptNid, itemEntries);
//                                         }
//                                         for (String s : multipart)
//                                         {
//                                                 itemEntries.add(s);
//                                         }
//                                 }
//                         }       
//                         catch (Exception e)
//                         {
//                                 errors++;
//                                 getLog().error("Failed reading " + sememe, e);
//                         }
//                         
//                 });
//                                         
//                 for (Entry<Integer, ArrayList<String>> item : entries.entrySet())
//                 {
//                         try
//                         {
//                                 Optional<LatestVersion<? extends LogicalExpression>> existingLogicExpr = Get.logicService().getLogicalExpression(item.getKey(), 
//                                                 LogicCoordinates.getStandardElProfile().getStatedAssemblageSequence(), 
//                                                 Get.configurationService().getDefaultStampCoordinate());
//                                 
//                                 LogicalExpression existing = null;
//                                 if (existingLogicExpr.isPresent())
//                                 {
//                                         existing = existingLogicExpr.get().value();
//                                 }
//                                 
//                                 LogicalExpressionBuilder leb = expressionBuilderService.getLogicalExpressionBuilder();
//                                 ArrayList<Assertion> assertions = new ArrayList<>();
//                                 
//                                 for (String part : item.getValue())
//                                 {
//                                         if (part.length() > 0)
//                                         {
//                                                 Pair<Float, UNIT> parsed = parseSpecifics(part);
//                                                 
//                                                 if (existing == null)
//                                                 {
//                                                         assertions.add(SomeRole(IsaacMetadataAuxiliaryBinding.ROLE_GROUP, And(
//                                                                                 Feature(IsaacMetadataAuxiliaryBinding.HAS_STRENGTH, FloatLiteral(parsed.getKey(), leb)),
//                                                                                 SomeRole(unitConcept, 
//                                                                                                 ConceptAssertion(Get.conceptService().getConcept(parsed.getValue().getConceptUUID()), leb)))));
//                                                 }
//                                                 else
//                                                 {
//                                                         //We can't use the builder, because there is currently no way to combine the logic graph from this builder, with 
//                                                         //the existing logic graph.  So, instead, manually build these nodes into the preexisting logic graph, below.
//                                                         
//                                                         boolean found = false;
//                                                         for (Node n : existing.getRoot().getChildren())
//                                                         {
//                                                                 if (n.getNodeSemantic() == NodeSemantic.NECESSARY_SET)
//                                                                 {
//                                                                         if (n.getChildren().length == 1 && n.getChildren()[0].getNodeSemantic() == NodeSemantic.AND)
//                                                                         {
//                                                                                 FeatureNodeWithNids feature = new FeatureNodeWithNids(
//                                                                                                 (LogicalExpressionImpl)existing, 
//                                                                                                 IsaacMetadataAuxiliaryBinding.HAS_STRENGTH.getConceptSequence(), 
//                                                                                                 new LiteralNodeFloat((LogicalExpressionImpl)existing, parsed.getKey().floatValue()));
//                                                                                 
//                                                                                 RoleNodeSomeWithNids unitRole = new RoleNodeSomeWithNids((LogicalExpressionImpl)existing, 
//                                                                                                 unitConcept.getConceptSequence(), 
//                                                                                                 new ConceptNodeWithNids((LogicalExpressionImpl)existing, 
//                                                                                                                 Get.identifierService().getConceptSequenceForUuids(parsed.getValue().getConceptUUID())));
//                                                                                 
//                                                                                 AndNode andNode = new AndNode((LogicalExpressionImpl)existing, feature, unitRole);
//                                                                                 
//                                                                                 RoleNodeSomeWithNids groupingRole = new RoleNodeSomeWithNids((LogicalExpressionImpl)existing, 
//                                                                                                 IsaacMetadataAuxiliaryBinding.ROLE_GROUP.getConceptSequence(), andNode);
//                                                                                 
//                                                                                 n.getChildren()[0].addChildren(groupingRole);
//                                                                                 found = true;
//                                                                                 break;
//                                                                         }
//                                                                 }
//                                                         }
//                                                         
//                                                         if (!found)
//                                                         {
//                                                                 throw new RuntimeException("oops! - couldn't merge on necessary");
//                                                         }
//                                                 }
//                                         }
//                                 }
//                                 
//                                 if (existing != null)
//                                 {
//                                         //I should find one and only 1, as we read it above, from the logic expression service, and it validates.
//                                         SemanticChronology<?> sc = Get.sememeService().getSememesForComponentFromAssemblage(item.getKey(), 
//                                                         LogicCoordinates.getStandardElProfile().getStatedAssemblageSequence()).findFirst().get();
//                                         
//                                         @SuppressWarnings("unchecked")
//                                         MutableLogicGraphVersion mls = ((SemanticChronology<LogicGraphSememe>)sc).createMutableVersion(MutableLogicGraphVersion.class, 
//                                                         sh.isaac.api.State.ACTIVE, 
//                                                         ec); 
//                                         
//                                         mls.setGraphData(existing.getData(DataTarget.INTERNAL));
//                                         
//                                         Get.commitService().addUncommitted(sc);
//                                         modifiedLogicGraphs++;
//                                 }
//                                 else
//                                 {
//                                         NecessarySet(And(assertions.toArray(new Assertion[0])));
//                                         LogicalExpression le = leb.build();
//                                         Get.sememeBuilderService().getLogicalExpressionSememeBuilder(le, item.getKey(), 
//                                                         LogicCoordinates.getStandardElProfile().getStatedAssemblageSequence()).build(ec, ChangeCheckerMode.ACTIVE);
//                                         newLogicGraphs++;
//                                 }
//                                 
//                         }
//                         catch (Exception e)
//                         {
//                                 errors++;
//                                 getLog().error("Failed creating logic graph for concept id  " + item.getKey(), e);
//                         }
//                 }
//                 
//                 getLog().info("Committing");
//                 updateMessage("Committing");
//                 updateProgress(2, 3);
//                 
//                 Get.commitService().commit("Adding RxNorm Concrete Domains").get();
//                 
//                 getLog().info("Done");
//                 updateMessage("Done");
//                 updateProgress(3, 3);
//                 
//                 return null;
//                 
//                 ingredSubstanceMergeCheck.close();
//                 scdProductMergeCheck.close();
//                 scdgToSCTIngredient_.close();
//                 
//                 
//                 ConsoleUtil.println("Ingredient / Substance merge concepts: " + ingredSubstanceMerge_.get());
//                 ConsoleUtil.println("Ingredient / Substance merge fail due to duplicates: " + ingredSubstanceMergeDupeFail_.get());
//                 ConsoleUtil.println("SCD / Product merge concepts: " + scdProductMerge_.get());
//                 ConsoleUtil.println("SCD / Product merge fail due to duplicates: " + scdProductMergeDupeFail_.get());
//                 ConsoleUtil.println("Dose Form merge concepts: " + doseFormMappedItemsCount_.get());
//                 ConsoleUtil.println("Converted tradename of relationships: " + convertedTradenameCount_.get());
//                 ConsoleUtil.println("Added is a relationships for scdg to ingredient: " + scdgToSCTIngredientCount_.get());
//         }
//         
// }
// 
// private void addCustomRelationships(String rxCui, TtkConceptChronicle cuiConcept, ArrayList<RXNCONSO> conceptData) throws SQLException
// {
//         /**
//          * Rule 2 is about: 
//          * Create is_a relationship from RxNorm SCDG to SNOMED [ingredient] product concept, 
//          * WHERE no SNOMED equivalent exists per RxNCONSO file for the SCDG.
//          * 
//          * (a) from RxNCONSO identify the SCDG that do NOT have an SCT equivalent concept in the product hierarchy.
//          * (b) from RxNREL identify the IN targets of SCDG (found in (a))  where "has_ingredient" relationship exists.
//          * (c) in RxNCONSO find the SCT product equivalent of the RxCUI TTY=IN found in (a); OR in RxNCONSO find the SCT substance 
//          * equivalent of the RxCUI TTY=IN found in (a), then find the SCT product using the SCT "active ingredient_of" (which is the inverse of 
//          * "has_active_ingredient" relationship) between substance and product.
//          * (d) Create a SOLOR is_a relationship between the SCDG (with no SCT equivalent in RxNCONSO) and the SCT [ingredient type] product concept.
//          * 
//          */
//         
//         HashSet<String> uniqueTTYs = new HashSet<String>();
//         HashSet<String> uniqueSABS = new HashSet<String>();
//         for (RXNCONSO x : conceptData)
//         {
//                 uniqueTTYs.add(x.tty);
//                 uniqueSABS.add(x.sab);
//         }
//         
//         //covers (a)
//         if (uniqueTTYs.contains("SCDG") && !uniqueSABS.contains(sctSab_))
//         {
//                 scdgToSCTIngredient_.setString(1, rxCui);
//                 ResultSet rs = scdgToSCTIngredient_.executeQuery();
//                 while (rs.next())
//                 {
//                         Long sctid = Long.parseLong(rs.getString("code"));
//                         
//                         UUID target = sctIDToUUID_.get(sctid);
//                         
//                         if (target == null)
//                         {
//                                 throw new RuntimeException("Unexpected - missing target for sctid " + sctid + " on cui " + rxCui);
//                         }
//                         
//                         importUtil_.addRelationship(cuiConcept, target);
//                         scdgToSCTIngredientCount_.incrementAndGet();
//                 }
//         }
// }
// 
//
// /**
//  * from RxNCONSO find all RxCUI with TTY = IN and SAB = SNOMED CT_US with STR = "*(substance)" 
//  * - that is, all RxCUI with TTY = IN and there is an equivalent SNOMEDCT_US concept in the Substance hierarchy
//  * @return the UUID from the snomed concept that is our merge target (if any)
//  */
// private Optional<UUID> sctMergeCheck(String rxCui) throws SQLException
// {
//         if (sctIDToUUID_ == null)
//         {
//                 return Optional.empty();
//         }
//
//         if (mergeCache_.get(rxCui) != null)
//         {
//                 return mergeCache_.get(rxCui);
//         }
//         
//         UUID snoConUUID = null;
//         
//         if (doseFormMappings_ != null)
//         {
//                 DoseForm df = doseFormMappings_.get(rxCui);
//                 if (df != null)
//                 {
//                         Long id = Long.parseLong(df.sctid);
//                         snoConUUID = sctIDToUUID_.get(id);
//                         if (snoConUUID != null)
//                         {
//                                 doseFormMappedItemsCount_.incrementAndGet();
//                                 mergeCache_.put(rxCui, Optional.of(snoConUUID));
//                                 return Optional.of(snoConUUID);
//                         }
//                 }
//         }
//         
//         ingredSubstanceMergeCheck.setString(1, rxCui);
//         
//         ResultSet rs = ingredSubstanceMergeCheck.executeQuery();
//         while (rs.next())
//         {
//                 long code = Long.parseLong(rs.getString(1));
//                 UUID found = sctIDToUUID_.get(code);
//                 if (found != null)
//                 {
//                         if (snoConUUID == null || found.equals(snoConUUID))
//                         {
//                                 if (snoConUUID == null)
//                                 {
//                                         ingredSubstanceMerge_.incrementAndGet();
//                                 }
//                                 snoConUUID = found;
//                         }
//                         else 
//                         {
//                                 ingredSubstanceMergeDupeFail_.incrementAndGet();
//                                 ConsoleUtil.printErrorln("Can't merge ingredient / substance to multiple Snomed concepts: " + rxCui);
//                         }
//                 }
//                 else
//                 {
//                         ConsoleUtil.printErrorln("Can't find UUID for SCTID " + code);
//                 }
//         }
//         
//         rs.close();
//         
//         scdProductMergeCheck.setString(1, rxCui);
//         rs = scdProductMergeCheck.executeQuery();
//         boolean passOne = true;
//         while (rs.next())
//         {
//                 if (passOne && snoConUUID != null)
//                 {
//                         ConsoleUtil.printErrorln("Cant merge to substance and to product at the same time! RXCUI " + rxCui);
//                         break;
//                 }
//                 passOne = false;
//                 long code = Long.parseLong(rs.getString(1));
//                 UUID found = sctIDToUUID_.get(code);
//                 if (found != null)
//                 {
//                         if (snoConUUID == null || found.equals(snoConUUID))
//                         {
//                                 if (snoConUUID == null)
//                                 {
//                                         scdProductMerge_.incrementAndGet();
//                                 }
//                                 snoConUUID = found;
//                         }
//                         else 
//                         {
//                                 scdProductMergeDupeFail_.incrementAndGet();
//                                 ConsoleUtil.printErrorln("Can't merge SCD / product to multiple Snomed concepts: " + rxCui);
//                         }
//                 }
//                 else
//                 {
//                         ConsoleUtil.printErrorln("Can't find UUID for SCTID " + code);
//                 }
//         }
//         
//         mergeCache_.put(rxCui, Optional.ofNullable(snoConUUID));
//         return Optional.ofNullable(snoConUUID);
// }
// 
// 
// private boolean handleAsRel(REL relationship) throws SQLException
// {
//         /**
//          * Rule 1 is about:
//          * Convert RxNorm Tradename_of to is_a between RxNorm SBD and RxNorm SCD
//          * tradename_of is created as both an association and a relationship - in the rel form, it maps to is_a.
//          */
//         if (hasTTYType(relationship.getSourceCUI(), "SBD") && hasTTYType(relationship.getTargetCUI(), "SCD"))
//         {
//                 convertedTradenameCount_.incrementAndGet();
//                 return true;
//         }
//
//         return false;
// }
//
// 
// private Pair<Float, UNIT> parseSpecifics(String value)
// {
//         value = removeParenStuff(value).trim();
//         String[] parts = value.split(" ");
//         if (parts.length == 1)
//         {
//                 return new Pair<>(1.0f, UNIT.parse(parts[0]));
//         }
//         else if (parts.length == 2)
//         {
//                 return new Pair<>(Float.parseFloat(parts[0]), UNIT.parse(parts[1]));
//         }
//         throw new RuntimeException("Wrong number of parts in '" + value + "'");
// }
// 
// private String removeParenStuff(String input)
// {
//         if (input.contains("(") && input.contains(")"))
//         {
//                 int i = input.indexOf("(");
//                 int z = input.lastIndexOf(")");
//                 if (z < i)
//                 {
//                         throw new RuntimeException("oops");
//                 }
//                 return input.substring(0, i) + input.substring(z + 1, input.length());
//         }
//         return input;
// }
// 
// private boolean hasTTYType(String cui, String tty) throws SQLException
// {
//         hasTTYType_.setString(1, cui);
//         hasTTYType_.setString(2, tty);
//         ResultSet rs = hasTTYType_.executeQuery();
//         if (rs.next())
//         {
//                 return rs.getInt("count") > 0;
//         }
//         throw new RuntimeException("Unexpected");
// }
// 
// private int findAssemblageNid(String uniqueName)
// {
//         IndexService si = LookupService.get().getService(IndexService.class, "description indexer");
//         if (si != null)
//         {
//                 //force the prefix algorithm, and add a trailing space - quickest way to do an exact-match type of search
//                 List<SearchResult> result = si.query(uniqueName + " ", true, 
//                                 null, 5, Long.MIN_VALUE);
//                 if (result.size() > 0)
//                 {
//                         return Get.sememeService().getSememe(result.get(0).getNid()).getReferencedComponentNid();
//                 }
//         }
//         throw new RuntimeException("Can't find assemblage nid with the name " + uniqueName);
// }
//   @Override
//   protected ConverterUUID.NAMESPACE getNamespace() {
//      return ConverterUUID.NAMESPACE.RXNORM;
//   }
}

