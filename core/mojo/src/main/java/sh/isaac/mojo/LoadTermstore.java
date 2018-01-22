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



package sh.isaac.mojo;

import static sh.isaac.api.logic.LogicalExpressionBuilder.And;
import static sh.isaac.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static sh.isaac.api.logic.LogicalExpressionBuilder.NecessarySet;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.cedarsoftware.util.io.JsonWriter;

import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.VersionManagmentPathService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.MutableLogicGraphVersion;
import sh.isaac.api.externalizable.BinaryDataReaderQueueService;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.StampAlias;
import sh.isaac.api.externalizable.StampComment;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.logic.IsomorphicResults;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.NecessarySetNode;
import sh.isaac.model.logic.node.external.ConceptNodeWithUuids;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;

//~--- classes ----------------------------------------------------------------

/*
* Copyright 2001-2005 The Apache Software Foundation.
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

/**
 * Goal which loads a database from ibdf files.
* @deprecated try {@link LoadTermstoreSemaphore}
* We need to figure out if/how to integrate the merge logic into the LoadTermStoreSemaphor
* in the meantime, the bug with missing random entries near the end of the file has been fixed.
 */
@Mojo(
   name         = "load-termstore",
   defaultPhase = LifecyclePhase.PROCESS_RESOURCES
)
public class LoadTermstore
        extends AbstractMojo {
   
   @Parameter(required = false)
   private boolean activeOnly = false;

   @Parameter(required = false)
   private int duplicatesToPrint = 20;

   /** The semantic types to skip. */
   private final HashSet<VersionType> semanticTypesToSkip = new HashSet<>();

   /** The skipped items. */
   private final HashSet<Integer> skippedItems = new HashSet<>();

   /** The skipped any. */
   private boolean skippedAny = false;

   /**
    * The preferred mechanism for specifying ibdf files - provide a folder that contains IBDF files, all found IBDF files in this
    * folder will be processed.
    */
   @Parameter(required = false)
   private File ibdfFileFolder;
   public void setibdfFilesFolder(File folder)
   {
      ibdfFileFolder = folder;
   }

   /**
    * The optional (old) way to specify ibdf files - requires each file to be listed one by one.
    */
   @Parameter(required = false)
   private File[] ibdfFiles;

   private int conceptCount, semanticCount, stampAliasCount, stampCommentCount, itemCount, itemFailure, mergeCount;

   //~--- methods -------------------------------------------------------------

   /**
    * Execute.
    *
    * @throws MojoExecutionException the mojo execution exception
    */
   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Override
   public void execute()
            throws MojoExecutionException {
      Get.configurationService()
         .setDBBuildMode();

      final int statedNid = Get.identifierService().getNidForUuids(TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getPrimordialUuid());

      // Load IsaacMetadataAuxiliary first, otherwise, we have issues....
      final AtomicBoolean hasMetadata = new AtomicBoolean(false);
      Set<File>           mergedFiles;

      try {
         mergedFiles = new HashSet<>();

         if (this.ibdfFiles != null) {
            for (final File f: this.ibdfFiles) {
               mergedFiles.add(f.getCanonicalFile());
            }
         }

         if (this.ibdfFileFolder != null) {
            if (!this.ibdfFileFolder.isDirectory()) {
               throw new MojoExecutionException("If ibdfFileFolder is provided, it must point to a folder");
            }

            for (final File f: this.ibdfFileFolder.listFiles()) {
               if (!f.isFile()) {
                  getLog().info("The file " + f.getAbsolutePath() + " is not a file - ignoring.");
               } else if (!f.getName()
                            .toLowerCase(Locale.ENGLISH)
                            .endsWith(".ibdf")) {
                  getLog().info("The file " + f.getAbsolutePath() +
                                " does not match the expected type of ibdf - ignoring.");
               } else {
                  mergedFiles.add(f);
               }
            }
         }
      } catch (final IOException e1) {
         throw new MojoExecutionException("Problem reading ibdf files", e1);
      }

      final File[] temp = mergedFiles.toArray(new File[mergedFiles.size()]);

      Arrays.sort(temp,
                  (o1, o2) -> {
                     if (o1.getName()
                           .equals("IsaacMetadataAuxiliary.ibdf")) {
                        hasMetadata.set(true);
                        return -1;
                     } else if (o2.getName()
                                  .equals("IsaacMetadataAuxiliary.ibdf")) {
                        hasMetadata.set(true);
                        return 1;
                     } else {
                        return ((o1.length() - o2.length()) > 0 ? 1
                  : ((o1.length() - o2.length()) < 0 ? -1
                  : 0));
                     }
                  });

      if (temp.length == 1 && temp[0].getName().equals("IsaacMetadataAuxiliary.ibdf"))
      {
         hasMetadata.set(true);
      }
      
      if (!hasMetadata.get()) {
         getLog().warn("No Metadata IBDF file found!  This probably isn't good....");
      }

      if (temp.length == 0) {
         throw new MojoExecutionException("Failed to find any ibdf files to load");
      }

      getLog().info("Identified " + temp.length + " ibdf files");

      final Set<Integer> deferredActionNids = new ConcurrentSkipListSet<>();

      try {
         for (final File f: temp) {
            getLog().info("Loading termstore from " + f.getCanonicalPath() + (this.activeOnly ? " active items only"
                  : ""));
            
            int duplicateCount = 0;
            
            final BinaryDataReaderQueueService       reader = Get.binaryDataQueueReader(f.toPath());
            final BlockingQueue<IsaacExternalizable> queue  = reader.getQueue();

            while (!queue.isEmpty() ||!reader.isFinished()) {
               final IsaacExternalizable object = queue.poll(500, TimeUnit.MILLISECONDS);

               if (object != null) {
                  this.itemCount++;

                  try {
                     if (null != object.getIsaacObjectType()) {
                        switch (object.getIsaacObjectType()) {
                        case CONCEPT:
                           if (!this.activeOnly || isActive((Chronology) object)) {
                              Get.conceptService()
                                 .writeConcept(((ConceptChronology) object));
                              this.conceptCount++;
                           } else {
                              this.skippedItems.add(((Chronology) object).getNid());
                           }

                           break;

                        case SEMANTIC:
                           SemanticChronology sc = (SemanticChronology) object;
                           
                           if (sc.getPrimordialUuid().equals(TermAux.MASTER_PATH_SEMANTIC_UUID)) {
                              getLog().info("Loading master path semantic at count: " + this.itemCount);
                           } else if (sc.getPrimordialUuid().equals(TermAux.DEVELOPMENT_PATH_SEMANTIC_UUID)) {
                              getLog().info("Loading development path semantic at count: " + this.itemCount);
                           }

                           if (sc.getAssemblageNid() == statedNid) {
                              final NidSet sequences = Get.assemblageService()
                                                                     .getSemanticNidsForComponentFromAssemblage(sc.getReferencedComponentNid(),
                                                                              statedNid);

                              if (sequences.size() == 1 && sequences.contains(sc.getNid())) {
                                 // not a duplicate, just an index for itself. 
                              } else if (!sequences.isEmpty() && duplicateCount < duplicatesToPrint) {
                                 duplicateCount++;
                                 final List<LogicalExpression> listToMerge = new ArrayList<>();

                                 listToMerge.add(getLatestLogicalExpression(sc));
                                 getLog().debug("\nDuplicate: " + sc);
                                 sequences.stream()
                                          .forEach(
                                              (semanticSequence) -> listToMerge.add(
                                                  getLatestLogicalExpression(Get.assemblageService()
                                                        .getSemanticChronology(semanticSequence))));
                                 getLog().debug("Duplicates: " + listToMerge);

                                 if (listToMerge.size() > 2) {
                                    throw new UnsupportedOperationException("Can't merge list of size: " +
                                          listToMerge.size() + "\n" + listToMerge);
                                 }
                                 
                                 Set<Integer> mergedParents = new HashSet<>();
                                 for (LogicalExpression le : listToMerge) {
                                    mergedParents.addAll(getParentConceptSequencesFromLogicExpression(le));
                                 }

                                 byte[][] data;

                                 if (mergedParents.size() == 0) {
                                    // The logic graph is too complex for our stupid merger - Use the isomorphic one.
                                    IsomorphicResults isomorphicResults = listToMerge.get(0).findIsomorphisms(listToMerge.get(1));
                                    getLog().debug("Isomorphic results: " + isomorphicResults);
                                    data = isomorphicResults.getMergedExpression().getData(DataTarget.INTERNAL);
                                 } else {
                                    // Use our stupid merger to just merge parents, cause the above merge isn't really designed to handle ibdf
                                    // import merges - especially in metadata where we keep adding additional parents one ibdf file at a time.
                                    // Note, this hack won't work at all to merge more complex logic graphs.
                                    // Probably won't work for RF2 content.
                                    // But for IBDF files, which are just adding extra parents, this avoids a bunch of issues with the logic graphs.
                                    Assertion[] assertions = new Assertion[mergedParents.size()];
                                    LogicalExpressionBuilder leb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
                                    int i = 0;
                                    for (Integer parent : mergedParents) {
                                       assertions[i++] = ConceptAssertion(parent, leb);
                                    }

                                    NecessarySet(And(assertions));
                                    data = leb.build().getData(DataTarget.INTERNAL);
                                 }
                                 
                                 mergeCount++;

                                 final SemanticChronology existingChronology = Get.assemblageService()
                                                                                .getSemanticChronology(sequences.findFirst()
                                                                                      .getAsInt());
                              int stampSequence = Get.stampService().getStampSequence(Status.ACTIVE, System.currentTimeMillis(), TermAux.USER.getNid(),
                                    TermAux.SOLOR_MODULE.getNid(), TermAux.DEVELOPMENT_PATH.getNid());
                              MutableLogicGraphVersion newVersion = (MutableLogicGraphVersion) existingChronology.createMutableVersion(stampSequence);
                              newVersion.setGraphData(data);

//                               TODO [DAN 2] mess - this isn't merging properly - how should we merge!? - I think this issue referrs to UUIDs... ?
                              //Need to take a new look at what is and isn't working right when merging concepts, and get the issues fixed.
//                               for (UUID uuid : sc.getUuidList())
//                               {
//                                       Get.identifierService().addUuidForNid(uuid, newVersion.getNid());
//                               }
                                 sc = existingChronology;
                              }
                           }

                           if (!this.semanticTypesToSkip.contains(sc.getVersionType()) &&
                                 (!this.activeOnly ||
                                  (isActive(sc) &&!this.skippedItems.contains(sc.getReferencedComponentNid())))) {
                              Get.assemblageService()
                                 .writeSemanticChronology(sc);
                              if (sc.getVersionType() == VersionType.LOGIC_GRAPH) {
                                 deferredActionNids.add(sc.getNid());
                              }

                              this.semanticCount++;
                           } else {
                              this.skippedItems.add(sc.getNid());
                           }

                           break;

                        case STAMP_ALIAS:
                           Get.commitService()
                              .addAlias(((StampAlias) object).getStampSequence(),
                                        ((StampAlias) object).getStampAlias(),
                                        null);
                           this.stampAliasCount++;
                           break;

                        case STAMP_COMMENT:
                           Get.commitService()
                              .setComment(((StampComment) object).getStampSequence(),
                                          ((StampComment) object).getComment());
                           this.stampCommentCount++;
                           break;

                        default:
                           throw new UnsupportedOperationException("Unknown object type: " + object);
                        }
                     }
                  } catch (final UnsupportedOperationException e) {
                     this.itemFailure++;
                     getLog().error("Failure at " + this.conceptCount + " concepts, " + this.semanticCount +
                                    " semantics, " + this.stampAliasCount + " stampAlias, " + this.stampCommentCount +
                                    " stampComments",
                                    e);

                     final Map<String, Object> args = new HashMap<>();

                     args.put(JsonWriter.PRETTY_PRINT, true);

                     final ByteArrayOutputStream baos       = new ByteArrayOutputStream();
                     try (JsonWriter json = new JsonWriter(baos, args)) {
                        UUID                        primordial = null;
                        
                        if (object instanceof Chronology) {
                           primordial = ((Chronology) object).getPrimordialUuid();
                        }
                        
                        json.write(object);
                        getLog().error("Failed on " + ((primordial == null) ? ": "
                                : "object with primoridial UUID " + primordial.toString() + ": ") + baos.toString());
                     }
                  }

                  if (this.itemCount % 50000 == 0) {
                     getLog().info("Read " + this.itemCount + " entries, " + "Loaded " + this.conceptCount +
                                   " concepts, " + this.semanticCount + " semantics, " + this.stampAliasCount +
                                   " stampAlias, " + this.stampCommentCount + " stampComment");
                  }
               }
            }

            if (this.skippedItems.size() > 0) {
               this.skippedAny = true;
            }

            getLog().info("Loaded " + this.conceptCount + " concepts, " + this.semanticCount + " semantics, " + this.stampAliasCount + " stampAlias, " 
                  + stampCommentCount + " stampComments, " + mergeCount + " merged sememes" + (skippedItems.size() > 0 ? ", skipped for inactive " + skippedItems.size() : "")  
                          + ((duplicateCount > 0) ? " Duplicates " + duplicateCount : "") 
                          + ((this.itemFailure > 0) ? " Failures " + this.itemFailure : "") + " from file " + f.getName());
            getLog().info("running item count: "  + this.itemCount);
            this.conceptCount      = 0;
            this.semanticCount       = 0;
            this.stampAliasCount   = 0;
            this.stampCommentCount = 0;
            this.skippedItems.clear();
         }
         
         Get.service(VersionManagmentPathService.class).rebuildPathMap();
         
         getLog().info("Completing processing on " + deferredActionNids.size() + " defered items");

         for (final int nid: deferredActionNids) {
            if (ObjectChronologyType.SEMANTIC.equals(Get.identifierService()
                  .getOldChronologyTypeForNid(nid))) {
               final SemanticChronology sc = Get.assemblageService()
                                              .getSemanticChronology(nid);

               if (sc.getVersionType() == VersionType.LOGIC_GRAPH) {
               try
               {
                  Get.taxonomyService().updateTaxonomy(sc);
               }
               catch (Exception e)
               {
                  Map<String, Object> args = new HashMap<>();
                  args.put(JsonWriter.PRETTY_PRINT, true);
                  ByteArrayOutputStream baos = new ByteArrayOutputStream();
                  JsonWriter json = new JsonWriter(baos, args);
                  
                  UUID primordial = sc.getPrimordialUuid();
                  json.write(sc);
                  getLog().error("Failed on taxonomy update for object with primoridial UUID " + primordial.toString() + ": " +  baos.toString(), e);
                  json.close();
               }
               } else {
                  throw new UnsupportedOperationException("1 Unexpected nid in deferred set: " + nid + " " + sc);
               }
            } else {
               throw new UnsupportedOperationException("2 Unexpected nid in deferred set: " + nid);
            }
         }

         if (this.skippedAny) {
            // Loading with activeOnly set to true causes a number of gaps in the concept /
            getLog().warn("Skipped components during import.");
         }
         getLog().info("Final item count: "  + this.itemCount);
         LookupService.syncAll();
         Get.startIndexTask().get();

      } catch (final ExecutionException | IOException | InterruptedException | UnsupportedOperationException ex) {
         getLog().info("Loaded with exception " + this.conceptCount + " concepts, " + this.semanticCount + " semantics, " +
                       this.stampAliasCount + " stampAlias, " + this.stampCommentCount + " stampComments" +
                       ((this.skippedItems.size() > 0) ? ", skipped for inactive " + this.skippedItems.size()
               : ""));
         throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
      } 
   }

   /**
    * Sets the ibdf files.
    *
    * @param files the new ibdf files
    */
   public void setibdfFiles(File[] files) {
      this.ibdfFiles = files;
   }
   
//   @Parameter(required = false) 
//   private String dbId = "";
//   
//   public void setDbId(String dbId)
//   {
//      this.dbId = dbId;
//   }

   /**
    * Skip semantic types.
    *
    * @param types the types
    */
   public void skipVersionTypes(Collection<VersionType> types) {
      this.semanticTypesToSkip.addAll(types);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if active.
    *
    * @param object the object
    * @return true, if active
    */
   private boolean isActive(Chronology object) {
      if (object.getVersionList()
                .size() != 1) {
         throw new RuntimeException("Didn't expect version list of size " + object.getVersionList());
      } else {
         return ((StampedVersion) object.getVersionList()
                                        .get(0)).getStatus() == Status.ACTIVE;
      }
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the active only.
    *
    * @param activeOnly the new active only
    */
   public void setActiveOnly(boolean activeOnly) {
      this.activeOnly = activeOnly;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the latest logical expression.
    *
    * @param sc the sc
    * @return the latest logical expression
    */
   private static LogicalExpression getLatestLogicalExpression(SemanticChronology sc) {
      final SemanticChronology lgsc          = sc;
      LogicGraphVersion                                   latestVersion = null;

      for (final StampedVersion version: lgsc.getVersionList()) {
         if (latestVersion == null) {
            latestVersion = (LogicGraphVersion) version;
         } else if (latestVersion.getTime() < version.getTime()) {
            latestVersion = (LogicGraphVersion) version;
         }
      }

      return (latestVersion != null) ? latestVersion.getLogicalExpression()
                                     : null;
   }
   
   
   /**
    * Shamelessly copied from FRILLS, as I can't use it there, due to dependency chain issues.  But then modified a bit, 
    * so it fails if it encounters things it can't handle.
    */
   private Set<Integer> getParentConceptSequencesFromLogicExpression(LogicalExpression logicExpression) {
      Set<Integer> parentConceptSequences = new HashSet<>();
      Stream<LogicNode> isAs = logicExpression.getNodesOfType(NodeSemantic.NECESSARY_SET);
      int necessaryCount = 0;
      int allCount = 1;  //start at 1, for root.
      for (Iterator<LogicNode> necessarySetsIterator = isAs.distinct().iterator(); necessarySetsIterator.hasNext();) {
         necessaryCount++;
         allCount++;
         NecessarySetNode necessarySetNode = (NecessarySetNode)necessarySetsIterator.next();
         for (AbstractLogicNode childOfNecessarySetNode : necessarySetNode.getChildren()) {
            allCount++;
            if (childOfNecessarySetNode.getNodeSemantic() == NodeSemantic.AND) {
               AndNode andNode = (AndNode)childOfNecessarySetNode;
               for (AbstractLogicNode childOfAndNode : andNode.getChildren()) {
                  allCount++;
                  if (childOfAndNode.getNodeSemantic() == NodeSemantic.CONCEPT) {
                     if (childOfAndNode instanceof ConceptNodeWithNids) {
                        ConceptNodeWithNids conceptNode = (ConceptNodeWithNids)childOfAndNode;
                        parentConceptSequences.add(conceptNode.getConceptNid());
                     } else if (childOfAndNode instanceof ConceptNodeWithUuids) {
                        ConceptNodeWithUuids conceptNode = (ConceptNodeWithUuids)childOfAndNode;
                        parentConceptSequences.add(Get.identifierService().getNidForUuids(conceptNode.getConceptUuid()));
                     } else {
                        // Should never happen - return an empty set to our call above doesn't use this mechanism
                        return new HashSet<>();
                     }
                  }
               }
            } else if (childOfNecessarySetNode.getNodeSemantic() == NodeSemantic.CONCEPT) {
               if (childOfNecessarySetNode instanceof ConceptNodeWithNids) {
                  ConceptNodeWithNids conceptNode = (ConceptNodeWithNids)childOfNecessarySetNode;
                  parentConceptSequences.add(conceptNode.getConceptNid());
               } else if (childOfNecessarySetNode instanceof ConceptNodeWithUuids) {
                  ConceptNodeWithUuids conceptNode = (ConceptNodeWithUuids)childOfNecessarySetNode;
                  parentConceptSequences.add(Get.identifierService().getNidForUuids(conceptNode.getConceptUuid()));
               } else {
                  // Should never happen - return an empty set to our call above doesn't use this mechanism
                  return new HashSet<>();
               }
            } else {
               // we don't understand this log graph.  Return an empty set to our call above doesn't use this mechanism
               return new HashSet<>();
            }
         }
      }
      
      if (logicExpression.getRoot().getChildren().length != necessaryCount || allCount != logicExpression.getNodeCount()) {
         // we don't understand this log graph.  Return an empty set to our call above doesn't use this mechanism
         return new HashSet<>();
      }
      
      return parentConceptSequences;
   }
}

