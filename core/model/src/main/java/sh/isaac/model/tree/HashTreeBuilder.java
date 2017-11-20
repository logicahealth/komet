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
 /*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.model.tree;

//~--- JDK imports ------------------------------------------------------------
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.list.IntArrayList;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.mahout.math.set.OpenIntHashSet;
import sh.isaac.api.Get;
import sh.isaac.api.alert.Alert;
import sh.isaac.api.alert.AlertCategory;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.alert.AlertType;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.ModelGet;
import sh.isaac.model.collections.SpinedIntIntArrayMap;
import sh.isaac.model.collections.SpinedIntObjectMap;
import static sh.isaac.model.tree.AbstractHashTree.MULTI_PARENT_SETS;

//~--- classes ----------------------------------------------------------------
/**
 * The Class HashTreeBuilder.
 *
 * @author kec
 */
public class HashTreeBuilder {
  private static final Logger LOG = LogManager.getLogger();
 
   /**
    * The Constant builderCount.
    */
   private static final AtomicInteger builderCount = new AtomicInteger();

   //~--- fields --------------------------------------------------------------
   /**
    * The child sequence parent sequence stream map.
    */
   final SpinedIntObjectMap<int[]> childNid_ParentNidSet_Map;

   /**
    * The parent sequence child sequence stream map.
    */
   final SpinedIntObjectMap<int[]> parentNid_ChildNidSet_Map;

   protected final ManifoldCoordinate manifoldCoordinate;

   /**
    * The concept sequences with parents.
    */
   final ConcurrentHashMap<Integer, Integer> conceptNidsWithParents = new ConcurrentHashMap();

   /**
    * The concept sequences with children.
    */
   final ConcurrentHashMap<Integer, Integer> conceptSequencesWithChildren = new ConcurrentHashMap();

   /**
    * The concept sequences.
    */
   final ConcurrentHashMap<Integer, Integer> conceptNids = new ConcurrentHashMap();

   /**
    * The builder id.
    */
   final int builderId;
   
   final int assemblageNid;

   String[] watchUuids = new String[]{};
   IntArrayList watchNids = new IntArrayList();

   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new hash tree builder.
    *
    * @param manifoldCoordinate
    * @param assemblageNid the assemblage nid which specifies the assemblage where the concepts in this tree
    * where created within.
    */
   public HashTreeBuilder(ManifoldCoordinate manifoldCoordinate, int assemblageNid) {
      int maxSequence = ModelGet.identifierService().getMaxSequenceForAssemblage(assemblageNid);
      childNid_ParentNidSet_Map = new SpinedIntObjectMap();
      parentNid_ChildNidSet_Map = new SpinedIntObjectMap();
      
      this.builderId = builderCount.getAndIncrement();
      this.manifoldCoordinate = manifoldCoordinate;
      this.assemblageNid = assemblageNid;
      for (String uuidStr : watchUuids) {
         watchNids.add(Get.identifierService().getNidForUuids(UUID.fromString(uuidStr)));
      }
   }

   //~--- methods -------------------------------------------------------------
   /**
    * Adds the.
    *
    * @param parent the parent
    * @param child the child
    */
   public void add(int parent, int child) {
       boolean testing = true;
      if (testing && parent == TermAux.SOLOR_ROOT.getNid()) {
         System.out.println("SOLOR root nid added to tree: " + TermAux.SOLOR_ROOT.getNid());
      }
     
      conceptNids.put(parent, parent);
      conceptNids.put(child, child);
      conceptNidsWithParents.put(child, child);
      conceptSequencesWithChildren.put(parent, parent);

      this.childNid_ParentNidSet_Map.accumulateAndGet(child, new int[] { parent }, this::merge);
      this.parentNid_ChildNidSet_Map.accumulateAndGet(parent, new int[] { child }, this::merge);
   }
   
   public int[] merge(int[] existing, int[] update) {
         if (existing == null) {
            return update;
         }
         OpenIntHashSet mergedSet = new OpenIntHashSet();
         for (int key: existing) {
            mergedSet.add(key);
         }
         for (int key: update) {
            mergedSet.add(key);
         }
         return mergedSet.keys().elements(); 
   }

   /**
    * Combine.
    *
    * @param another the another
    */
   public void combine(HashTreeBuilder another) {
      addToOne(this.conceptNids, another.conceptNids);
      addToOne(this.conceptSequencesWithChildren, another.conceptSequencesWithChildren);
      addToOne(this.conceptNidsWithParents, another.conceptNidsWithParents);
      another.childNid_ParentNidSet_Map.forEach((int childSequence,
              int[] parentsFromAnother) -> {
         if (this.childNid_ParentNidSet_Map.containsKey(childSequence)) {
            int[] parentsFromThis = this.childNid_ParentNidSet_Map.get(childSequence);
            this.childNid_ParentNidSet_Map.put(childSequence, merge(parentsFromThis, parentsFromAnother));
         } else {
            this.childNid_ParentNidSet_Map.put(childSequence, parentsFromAnother);
         }
      });
      another.parentNid_ChildNidSet_Map.forEach((int parentSequence,
              int[] childrenFromAnother) -> {
         if (this.parentNid_ChildNidSet_Map.containsKey(parentSequence)) {
            int[] childrenFromThis = this.parentNid_ChildNidSet_Map.get(parentSequence);
            this.childNid_ParentNidSet_Map.put(parentSequence, merge(childrenFromThis, childrenFromAnother));
         } else {
            this.parentNid_ChildNidSet_Map.put(parentSequence, childrenFromAnother);
         }
      });
   }

   private void addToOne(OpenIntHashSet one, OpenIntHashSet another) {
      another.forEachKey((sequence) -> {
         one.add(sequence);
         return true;
      });
   }

   private void addToOne(ConcurrentHashMap<Integer, Integer> one, ConcurrentHashMap<Integer, Integer> another) {
      another.keySet().forEach((key) -> {
         one.put(key, key);
      });
   }

   private void removeFromOne(OpenIntHashSet one, ConcurrentHashMap<Integer, Integer> another) {
      another.forEach((key, value) -> {
         one.remove(key);
      });
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the simple directed graph graph.
    *
    * @return the simple directed graph graph
    */
   public HashTreeWithBitSets getSimpleDirectedGraph() {
      boolean testing = true;
      if (testing) {
         System.out.println("SOLOR root sequence: " + TermAux.SOLOR_ROOT.getNid());
         System.out.println("SOLOR root in concepts: " + conceptNids.contains(TermAux.SOLOR_ROOT.getNid()));
         System.out.println("SOLOR root in concepts with parents: " + conceptNidsWithParents.contains(TermAux.SOLOR_ROOT.getNid()));
      }
      OpenIntHashSet roots = new OpenIntHashSet();
      conceptNids.forEach((key, value) -> {
         roots.add(key);
      });
      removeFromOne(roots, conceptNidsWithParents);
      
      
      
      if (roots.size() != 1) {
         StringBuilder builder1 = new StringBuilder();
         builder1.append("Root count != 1: ");
         builder1.append(roots.size());
         LOG.warn(builder1.toString());
         final StringBuilder builder = new StringBuilder("Roots: \n");
         int count = 0;
         for (int sequence : roots.keys().elements()) {
            count++;
            if (count > 4) {
               break;
            }
            builder.append(sequence).append(": ").append(Get.conceptDescriptionText(sequence)).append("\n");
            printWatch(sequence, "root: ");
         }
         String title = "To many taxonomy roots";
         if (roots.isEmpty()) {
            title = "No taxonomy roots";
         }
         
         AlertObject alert = new AlertObject(title, builder.toString(), AlertType.ERROR, AlertCategory.TAXONOMY);
         Alert.publishAddition(alert);

      }

      int rootSequence = TermAux.SOLOR_ROOT.getNid();
      if (!roots.keys().isEmpty()) {
         rootSequence = roots.keys().get(0);
      } 

      final HashTreeWithBitSets graph = new HashTreeWithBitSets(
              manifoldCoordinate, assemblageNid);

      this.childNid_ParentNidSet_Map.forEach((int childNid,
              int[] parentNidSet) -> {
         if (parentNidSet.length != 0) {
            Arrays.sort(parentNidSet);
            graph.addParents(childNid, parentNidSet);
         }
      });
      this.parentNid_ChildNidSet_Map.forEach((int parentNid,
              int[] childNidSet) -> {
         if (childNidSet.length != 0) {
              Arrays.sort(childNidSet);
          graph.addChildren(parentNid, childNidSet);
         }
      });

      TreeNodeVisitData visitData = graph.depthFirstProcess(rootSequence, (TreeNodeVisitData t, int thisNid) -> {
         if (watchNids.contains(thisNid)) {
            printWatch(thisNid, "dfs: ");
         }
      }, Get.taxonomyService().getTreeNodeVisitDataSupplier(graph.getAssemblageNid()));

      for (int[] cycle : visitData.getCycleSet()) {
         StringBuilder cycleDescription = new StringBuilder("Members: \n");
         for (int conceptSequence : cycle) {
            cycleDescription.append("   ").append(manifoldCoordinate.getPreferredDescriptionText(conceptSequence)).append("\n");
         }
         Alert.publishAddition(new TreeCycleError(cycle, visitData, graph, "Cycle found", cycleDescription.toString(), AlertType.ERROR));
      }

      System.out.println("Nodes visited: " + visitData.getNodesVisited());
      for (int nid : watchNids.toList()) {
         OpenIntHashSet multiParents = visitData.getUserNodeSet(MULTI_PARENT_SETS, nid);
         System.out.println(Get.conceptDescriptionText(nid) + " multiParentSet: " + multiParents);
      }

      return graph;

   }

   private int[] setToSortedArray(OpenIntHashSet set) {
      IntArrayList list = set.keys();
      list.sort();
      return list.elements();
   }

   private void printWatch(int conceptNid, String prefix) {
      int nid = conceptNid;
      if (nid >= 0) {
         nid = ModelGet.identifierService().getNidForElementSequence(conceptNid, assemblageNid);
      }
      SpinedIntIntArrayMap taxonomyMap = ModelGet.taxonomyDebugService().getTaxonomyRecordMap(ModelGet.identifierService().getAssemblageNid(nid).getAsInt());
      int[] record = taxonomyMap.get(nid);

      int finalNid = nid;
      System.out.println("\n" + prefix + " watch: " + Get.conceptDescriptionText(nid));
      System.out.println("\nTaxonomy record: " + Arrays.toString(record));
     System.out.println("\n" + ModelGet.taxonomyDebugService().describeTaxonomyRecord(nid));
      
      childNid_ParentNidSet_Map.forEach((int sequence, int[] parentArray) -> {
         if (Arrays.stream(parentArray).anyMatch((value) -> value == finalNid)) {
            int sequenceNid = sequence;
            if (sequenceNid >= 0) {
               sequenceNid = ModelGet.identifierService().getNidForElementSequence(sequence, assemblageNid);
            }
            System.out.println(prefix + Get.conceptDescriptionText(finalNid) + " found in parent set of: " + sequence + " " + Get.conceptDescriptionText(sequenceNid));
         }
      });
      parentNid_ChildNidSet_Map.forEach((int sequence, int[] childArray) -> {
         if (Arrays.stream(childArray).anyMatch((value) -> value == finalNid)) {
            int sequenceNid = sequence;
            if (sequenceNid >= 0) {
               sequenceNid = ModelGet.identifierService().getNidForElementSequence(sequence, assemblageNid);
            }
            System.out.println(prefix + Get.conceptDescriptionText(finalNid) + " found in child set of: " + sequence + " " + Get.conceptDescriptionText(sequenceNid));
         }
      });
      System.out.println(Get.concept(nid).toString());
      System.out.println();
   }
}
