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
package sh.isaac.model.tree;

//~--- JDK imports ------------------------------------------------------------
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.OpenIntHashSet;

import org.checkerframework.checker.units.qual.s;
import org.roaringbitmap.RoaringBitmap;
import sh.isaac.api.Get;
import sh.isaac.api.ProgressTracker;
import sh.isaac.api.alert.Alert;
import sh.isaac.api.alert.AlertType;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.constants.SystemPropertyConstants;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.collections.MergeIntArray;

//~--- classes ----------------------------------------------------------------
/**
 * The Class HashTreeBuilder.
 *
 * @author kec
 * @deprecated move to HashTreeBuilderIsolated
 */
public class HashTreeBuilder
        extends HashTreeWithIntArraySets {

    /**
     * The Constant BUILDER_COUNT.
     */
    private static final AtomicInteger BUILDER_COUNT = new AtomicInteger();

    private static final Logger LOG = LogManager.getLogger();

    //~--- fields --------------------------------------------------------------
    /**
     * The concept nids.
     */
    {
        System.setProperty(SystemPropertyConstants.ISAAC_DEBUG, "true");

    }

    String[] watchUuids = new String[]{ };
    IntArrayList watchNids = new IntArrayList();

    /**
     * The builder id.
     */
    final int builderId;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new hash tree builder.
     *
     * @param manifoldCoordinate
     * @param assemblageNid the assemblage nid which specifies the assemblage
     * where the concepts in this tree where created within.
     */
    public HashTreeBuilder(ManifoldCoordinate manifoldCoordinate, int assemblageNid) {
        super(manifoldCoordinate, assemblageNid);
        this.builderId = BUILDER_COUNT.getAndIncrement();

        for (String uuidStr : watchUuids) {
            if (Get.identifierService().hasUuid(UUID.fromString(uuidStr))) {
                watchNids.add(Get.identifierService()
                        .getNidForUuids(UUID.fromString(uuidStr)));
            }
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Combine.
     *
     * @param another the another
     */
    public void combine(HashTreeBuilder another) {
        addToOne(this.conceptNids, another.conceptNids);
        addToOne(this.conceptNidsWithChildren, another.conceptNidsWithChildren);
        addToOne(this.conceptNidsWithParents, another.conceptNidsWithParents);
        another.childNid_ParentNidSetArray_Map.forEach(
                (int childSequence,
                        int[] parentsFromAnother) -> {
                    if (this.childNid_ParentNidSetArray_Map.containsKey(childSequence)) {
                        int[] parentsFromThis = this.childNid_ParentNidSetArray_Map.get(childSequence);

                        this.childNid_ParentNidSetArray_Map.put(
                                childSequence,
                                MergeIntArray.merge(parentsFromThis, parentsFromAnother));
                    } else {
                        this.childNid_ParentNidSetArray_Map.put(childSequence, parentsFromAnother);
                    }
                });
        another.parentNid_ChildNidSetArray_Map.forEach(
                (int parentSequence,
                        int[] childrenFromAnother) -> {
                    if (this.parentNid_ChildNidSetArray_Map.containsKey(parentSequence)) {
                        int[] childrenFromThis = this.parentNid_ChildNidSetArray_Map.get(parentSequence);

                        this.childNid_ParentNidSetArray_Map.put(
                                parentSequence,
                                MergeIntArray.merge(childrenFromThis, childrenFromAnother));
                    } else {
                        this.parentNid_ChildNidSetArray_Map.put(parentSequence, childrenFromAnother);
                    }
                });
    }

    private void addToOne(OpenIntHashSet one, OpenIntHashSet another) {
        another.forEachKey(
                (sequence) -> {
                    one.add(sequence);
                    return true;
                });
    }

    //~--- get methods ---------------------------------------------------------
    public HashTreeWithIntArraySets getSimpleDirectedGraph() {
        return getSimpleDirectedGraph(null);
    }

    /**
     * Gets the simple directed graph graph.
     *
     * @param tracker
     * @return the simple directed graph graph
     */
    public HashTreeWithIntArraySets getSimpleDirectedGraph(ProgressTracker tracker) {

        if (Get.configurationService().isVerboseDebugEnabled()) {
            LOG.info("SOLOR root sequence: " + TermAux.SOLOR_ROOT.getNid());
            LOG.info("SOLOR root in concepts: " + conceptNids.contains(TermAux.SOLOR_ROOT.getNid()));
            LOG.info(
                    "SOLOR root in concepts with parents: " + conceptNidsWithParents.contains(TermAux.SOLOR_ROOT.getNid()));
        }

        computeRoots();

        int rootNid = TermAux.SOLOR_ROOT.getNid();

        TreeNodeVisitData visitData = depthFirstProcess(
                rootNid,
                (TreeNodeVisitData t,
                        int thisNid) -> {
                    if (watchNids.contains(thisNid)) {
                        printWatch(thisNid, "dfs: ");
                    }
                    if (tracker != null) {
                        tracker.completedUnitOfWork();
                    }
                },
                Get.taxonomyService()
                        .getTreeNodeVisitDataSupplier(getConceptAssemblageNid()));

        for (int[] cycle : visitData.getCycleSet()) {
            StringBuilder cycleDescription = new StringBuilder("Members: \n");

            for (int conceptSequence : cycle) {
                cycleDescription.append("   ")
                        .append(manifoldCoordinate.getPreferredDescriptionText(conceptSequence))
                        .append("\n");
            }

            Alert.publishAddition(
                    new TreeCycleError(cycle, visitData, this, manifoldCoordinate.getDigraph().getPremiseType() + " Cycle found", cycleDescription.toString(), AlertType.ERROR));
        }

        LOG.debug("Nodes visited: " + visitData.getNodesVisited());

        for (int nid : watchNids.toList()) {
            RoaringBitmap multiParents = visitData.getUserNodeSet(MULTI_PARENT_SETS, nid);

            LOG.debug(Get.conceptDescriptionText(nid) + " multiParentSet: " + multiParents);
        }

        return this;
    }
}
