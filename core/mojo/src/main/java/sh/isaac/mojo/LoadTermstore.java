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

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.cedarsoftware.util.io.JsonWriter;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;

import sh.isaac.api.ConceptProxy;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.externalizable.BinaryDataReaderQueueService;
import sh.isaac.api.externalizable.StampAlias;
import sh.isaac.api.externalizable.StampComment;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.logic.IsomorphicResults;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.MutableLogicGraphVersion;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.component.semantic.SemanticChronology;

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
 * Goal which loads a database from eConcept files.
 */
@Mojo(
   name         = "load-termstore",
   defaultPhase = LifecyclePhase.PROCESS_RESOURCES
)
public class LoadTermstore
        extends AbstractMojo {
   /** The active only. */
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

   /**
    * The optional (old) way to specify ibdf files - requires each file to be listed one by one.
    */
   @Parameter(required = false)
   private File[] ibdfFiles;

   /** The item failure. */
   private int conceptCount, semanticCount, stampAliasCount, stampCommentCount, itemCount, itemFailure;

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

      final int statedNid = Get.identifierService()
                                    .getNidForUuids(
                                        TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getPrimordialUuid());
      final long loadTime = System.currentTimeMillis();

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
                            .toLowerCase()
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
                                 getLog().info("\nDuplicate: " + sc);
                                 sequences.stream()
                                          .forEach(
                                              (semanticSequence) -> listToMerge.add(
                                                  getLatestLogicalExpression(Get.assemblageService()
                                                        .getSemanticChronology(semanticSequence))));
                                 getLog().info("Duplicates: " + listToMerge);

                                 if (listToMerge.size() > 2) {
                                    throw new UnsupportedOperationException("Can't merge list of size: " +
                                          listToMerge.size() + "\n" + listToMerge);
                                 }

                                 final IsomorphicResults isomorphicResults = listToMerge.get(0)
                                                                                        .findIsomorphisms(
                                                                                           listToMerge.get(1));

                                 getLog().info("Isomorphic results: " + isomorphicResults);

                                 final SemanticChronology existingChronology = Get.assemblageService()
                                                                                .getSemanticChronology(sequences.findFirst()
                                                                                      .getAsInt());
                                 final ConceptProxy moduleProxy = new ConceptProxy("SOLOR overlay module",
                                                                                   "9ecc154c-e490-5cf8-805d-d2865d62aef3");
                                 final ConceptProxy pathProxy = new ConceptProxy("development path",
                                                                                 "1f200ca6-960e-11e5-8994-feff819cdc9f");
                                 final ConceptProxy userProxy = new ConceptProxy("user",
                                                                                 "f7495b58-6630-3499-a44e-2052b5fcf06c");
                                 final int stampSequence = Get.stampService()
                                                              .getStampSequence(Status.ACTIVE,
                                                                    loadTime,
                                                                    userProxy.getNid(),
                                                                    moduleProxy.getNid(),
                                                                    pathProxy.getNid());
                                 final MutableLogicGraphVersion newVersion =
                                    (MutableLogicGraphVersion) existingChronology.createMutableVersion(
                                        stampSequence);

                                 newVersion.setGraphData(isomorphicResults.getMergedExpression()
                                       .getData(DataTarget.INTERNAL));

//                               TODO mess - this isn't merging properly - how should we merge!?
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
                           throw new UnsupportedOperationException("Unknown ochre object type: " + object);
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

            getLog().info("Loaded " + this.conceptCount + " concepts, " + this.semanticCount + " semantics, " +
                          this.stampAliasCount + " stampAlias, " + this.stampCommentCount + " stampComment" +
                          ((this.skippedItems.size() > 0) ? ", skipped for inactive " + this.skippedItems.size()
                  : "") + ((duplicateCount > 0) ? " Duplicates " + duplicateCount
                  : "") + ((this.itemFailure > 0) ? " Failures " + this.itemFailure
                  : "") + " from file " + f.getName());
            this.conceptCount      = 0;
            this.semanticCount       = 0;
            this.stampAliasCount   = 0;
            this.stampCommentCount = 0;
            this.skippedItems.clear();
         }
         
         
         Get.startIndexTask().get();
         getLog().info("Completing processing on " + deferredActionNids.size() + " defered items");

         for (final int nid: deferredActionNids) {
            if (ObjectChronologyType.SEMANTIC.equals(Get.identifierService()
                  .getOldChronologyTypeForNid(nid))) {
               final SemanticChronology sc = Get.assemblageService()
                                              .getSemanticChronology(nid);

               if (sc.getVersionType() == VersionType.LOGIC_GRAPH) {
                  Get.taxonomyService()
                     .updateTaxonomy(sc);
               } else {
                  throw new UnsupportedOperationException("1 Unexpected nid in deferred set: " + nid + " " + sc);
               }
            } else {
               throw new UnsupportedOperationException("2 Unexpected nid in deferred set: " + nid);
            }
         }

         if (this.skippedAny) {
            // Loading with activeOnly set to true causes a number of gaps in the concept / semantic providers
//            Get.identifierService()
//               .clearUnusedIds();
         }
      } catch (final ExecutionException | IOException | InterruptedException | UnsupportedOperationException ex) {
         getLog().info("Loaded " + this.conceptCount + " concepts, " + this.semanticCount + " semantics, " +
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

   /**
    * Skip semantic types.
    *
    * @param types the types
    */
   public void skipSememeTypes(Collection<VersionType> types) {
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
                                        .get(0)).getState() == Status.ACTIVE;
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
}

