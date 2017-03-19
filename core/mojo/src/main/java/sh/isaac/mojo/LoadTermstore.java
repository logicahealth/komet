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

import sh.isaac.api.ConceptProxy;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.collections.SememeSequenceSet;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.LogicGraphSememe;
import sh.isaac.api.component.sememe.version.MutableLogicGraphSememe;
import sh.isaac.api.externalizable.BinaryDataReaderQueueService;
import sh.isaac.api.externalizable.OchreExternalizable;
import sh.isaac.api.externalizable.StampAlias;
import sh.isaac.api.externalizable.StampComment;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.logic.IsomorphicResults;
import sh.isaac.api.logic.LogicalExpression;

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
   @Parameter(required = false)
   private boolean                   activeOnly        = false;
   private final HashSet<SememeType> sememeTypesToSkip = new HashSet<>();
   private final HashSet<Integer>    skippedItems      = new HashSet<>();
   private boolean                   skippedAny        = false;

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
   private int    conceptCount, sememeCount, stampAliasCount, stampCommentCount, itemCount, itemFailure;

   //~--- methods -------------------------------------------------------------

   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Override
   public void execute()
            throws MojoExecutionException {
      Get.configurationService()
         .setDBBuildMode();

      final int statedSequence = Get.identifierService()
                                    .getConceptSequenceForUuids(
                                        TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getPrimordialUuid());
      final long loadTime   = System.currentTimeMillis();

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

      final Set<Integer> deferredActionNids = new HashSet<>();

      try {
         for (final File f: temp) {
            getLog().info("Loading termstore from " + f.getCanonicalPath() + (this.activeOnly ? " active items only"
                  : ""));

            final BinaryDataReaderQueueService       reader = Get.binaryDataQueueReader(f.toPath());
            final BlockingQueue<OchreExternalizable> queue  = reader.getQueue();

            while (!queue.isEmpty() ||!reader.isFinished()) {
               final OchreExternalizable object = queue.poll(500, TimeUnit.MILLISECONDS);

               if (object != null) {
                  this.itemCount++;

                  try {
                     if (null != object.getOchreObjectType()) {
                        switch (object.getOchreObjectType()) {
                        case CONCEPT:
                           if (!this.activeOnly || isActive((ObjectChronology) object)) {
                              Get.conceptService()
                                 .writeConcept(((ConceptChronology) object));
                              this.conceptCount++;
                           } else {
                              this.skippedItems.add(((ObjectChronology) object).getNid());
                           }

                           break;

                        case SEMEME:
                           SememeChronology sc = (SememeChronology) object;

                           if (sc.getAssemblageSequence() == statedSequence) {
                              final SememeSequenceSet sequences = Get.sememeService()
                                                               .getSememeSequencesForComponentFromAssemblage(
                                                                  sc.getReferencedComponentNid(),
                                                                        statedSequence);

                              if (!sequences.isEmpty()) {
                                 final List<LogicalExpression> listToMerge = new ArrayList<>();

                                 listToMerge.add(getLatestLogicalExpression(sc));
                                 getLog().info("\nDuplicate: " + sc);
                                 sequences.stream()
                                          .forEach(
                                              (sememeSequence) -> listToMerge.add(
                                                  getLatestLogicalExpression(Get.sememeService()
                                                        .getSememe(sememeSequence))));
                                 getLog().info("Duplicates: " + listToMerge);

                                 if (listToMerge.size() > 2) {
                                    throw new UnsupportedOperationException("Can't merge list of size: " +
                                          listToMerge.size() + "\n" + listToMerge);
                                 }

                                 final IsomorphicResults isomorphicResults = listToMerge.get(0)
                                                                                  .findIsomorphisms(listToMerge.get(1));

                                 getLog().info("Isomorphic results: " + isomorphicResults);

                                 final SememeChronology existingChronology = Get.sememeService()
                                                                          .getSememe(sequences.findFirst()
                                                                                .getAsInt());
                                 final ConceptProxy moduleProxy = new ConceptProxy("SOLOR overlay module",
                                                                             "9ecc154c-e490-5cf8-805d-d2865d62aef3");
                                 final ConceptProxy pathProxy = new ConceptProxy("development path",
                                                                           "1f200ca6-960e-11e5-8994-feff819cdc9f");
                                 final ConceptProxy userProxy = new ConceptProxy("user",
                                                                           "f7495b58-6630-3499-a44e-2052b5fcf06c");
                                 final int stampSequence = Get.stampService()
                                                        .getStampSequence(State.ACTIVE,
                                                              loadTime,
                                                              userProxy.getConceptSequence(),
                                                              moduleProxy.getConceptSequence(),
                                                              pathProxy.getConceptSequence());
                                 final MutableLogicGraphSememe newVersion =
                                    (MutableLogicGraphSememe) existingChronology.createMutableVersion(
                                        MutableLogicGraphSememe.class,
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

                           if (!this.sememeTypesToSkip.contains(sc.getSememeType()) &&
                                 (!this.activeOnly ||
                                  (isActive(sc) &&!this.skippedItems.contains(sc.getReferencedComponentNid())))) {
                              Get.sememeService()
                                 .writeSememe(sc);

                              if (sc.getSememeType() == SememeType.LOGIC_GRAPH) {
                                 deferredActionNids.add(sc.getNid());
                              }

                              this.sememeCount++;
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
                  } catch (final Exception e) {
                     this.itemFailure++;
                     getLog().error("Failure at " + this.conceptCount + " concepts, " + this.sememeCount + " sememes, " +
                                    this.stampAliasCount + " stampAlias, " + this.stampCommentCount + " stampComments",
                                    e);

                     final Map<String, Object> args = new HashMap<>();

                     args.put(JsonWriter.PRETTY_PRINT, true);

                     final ByteArrayOutputStream baos       = new ByteArrayOutputStream();
                     final JsonWriter            json       = new JsonWriter(baos, args);
                     UUID                  primordial = null;

                     if (object instanceof ObjectChronology) {
                        primordial = ((ObjectChronology) object).getPrimordialUuid();
                     }

                     json.write(object);
                     getLog().error("Failed on " + ((primordial == null) ? ": "
                           : "object with primoridial UUID " + primordial.toString() + ": ") + baos.toString());
                     json.close();
                  }

                  if (this.itemCount % 50000 == 0) {
                     getLog().info("Read " + this.itemCount + " entries, " + "Loaded " + this.conceptCount + " concepts, " +
                                   this.sememeCount + " sememes, " + this.stampAliasCount + " stampAlias, " + this.stampCommentCount +
                                   " stampComment");
                  }
               }
            }
            ;

            if (this.skippedItems.size() > 0) {
               this.skippedAny = true;
            }

            getLog().info("Loaded " + this.conceptCount + " concepts, " + this.sememeCount + " sememes, " + this.stampAliasCount +
                          " stampAlias, " + this.stampCommentCount + " stampComment" +
                          ((this.skippedItems.size() > 0) ? ", skipped for inactive " + this.skippedItems.size()
                  : "") + ((this.itemFailure > 0) ? " Failures " + this.itemFailure
                                             : "") + " from file " + f.getName());
            this.conceptCount      = 0;
            this.sememeCount       = 0;
            this.stampAliasCount   = 0;
            this.stampCommentCount = 0;
            this.skippedItems.clear();
         }

         getLog().info("Completing processing on " + deferredActionNids.size() + " defered items");

         for (final int nid: deferredActionNids) {
            if (ObjectChronologyType.SEMEME.equals(Get.identifierService()
                  .getChronologyTypeForNid(nid))) {
               final SememeChronology sc = Get.sememeService()
                                        .getSememe(nid);

               if (sc.getSememeType() == SememeType.LOGIC_GRAPH) {
                  Get.taxonomyService()
                     .updateTaxonomy(sc);
               } else {
                  throw new UnsupportedOperationException("Unexpected nid in deferred set: " + nid);
               }
            } else {
               throw new UnsupportedOperationException("Unexpected nid in deferred set: " + nid);
            }
         }

         if (this.skippedAny) {
            // Loading with activeOnly set to true causes a number of gaps in the concept / sememe providers
            Get.identifierService()
               .clearUnusedIds();
         }
      } catch (final Exception ex) {
         getLog().info("Loaded " + this.conceptCount + " concepts, " + this.sememeCount + " sememes, " + this.stampAliasCount +
                       " stampAlias, " + this.stampCommentCount + " stampComments" +
                       ((this.skippedItems.size() > 0) ? ", skipped for inactive " + this.skippedItems.size()
               : ""));
         throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
      }
   }

   public void setibdfFiles(File[] files) {
      this.ibdfFiles = files;
   }

   public void skipSememeTypes(Collection<SememeType> types) {
      this.sememeTypesToSkip.addAll(types);
   }

   //~--- get methods ---------------------------------------------------------

   private boolean isActive(ObjectChronology<?> object) {
      if (object.getVersionList()
                .size() != 1) {
         throw new RuntimeException("Didn't expect version list of size " + object.getVersionList());
      } else {
         return ((StampedVersion) object.getVersionList()
                                        .get(0)).getState() == State.ACTIVE;
      }
   }

   //~--- set methods ---------------------------------------------------------

   public void setActiveOnly(boolean activeOnly) {
      this.activeOnly = activeOnly;
   }

   //~--- get methods ---------------------------------------------------------

   private static LogicalExpression getLatestLogicalExpression(SememeChronology sc) {
      final SememeChronology<? extends LogicGraphSememe> lgsc          = sc;
      LogicGraphSememe                             latestVersion = null;

      for (final LogicGraphSememe version: lgsc.getVersionList()) {
         if (latestVersion == null) {
            latestVersion = version;
         } else if (latestVersion.getTime() < version.getTime()) {
            latestVersion = version;
         }
      }

      return (latestVersion != null) ? latestVersion.getLogicalExpression()
                                     : null;
   }
}

