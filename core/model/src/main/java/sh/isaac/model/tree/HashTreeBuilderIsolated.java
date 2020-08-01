package sh.isaac.model.tree;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.OpenIntHashSet;
import org.roaringbitmap.RoaringBitmap;
import sh.isaac.api.Get;
import sh.isaac.api.ProgressTracker;
import sh.isaac.api.alert.Alert;
import sh.isaac.api.alert.AlertType;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.constants.SystemPropertyConstants;
import sh.isaac.api.coordinate.PremiseSet;
import sh.isaac.api.coordinate.StampFilterImmutable;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.collections.MergeIntArray;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class HashTreeBuilderIsolated extends HashTreeWithIntArraySetsIsolated {

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
     * @param premiseTypes
     * @param assemblageNid the assemblage nid which specifies the assemblage
     * where the concepts in this tree where created within.
     */
    public HashTreeBuilderIsolated(StampFilterImmutable vertexFilter, String coordinateString, PremiseSet premiseTypes, int assemblageNid) {
        super(vertexFilter, coordinateString, premiseTypes, assemblageNid);
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
    public void combine(HashTreeBuilderIsolated another) {
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
    public HashTreeWithIntArraySetsIsolated getSimpleDirectedGraph() {
        return getSimpleDirectedGraph(null);
    }

    /**
     * Gets the simple directed graph graph.
     *
     * @param tracker
     * @return the simple directed graph graph
     */
    public HashTreeWithIntArraySetsIsolated getSimpleDirectedGraph(ProgressTracker tracker) {

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
                        .append(Get.defaultCoordinate().getPreferredDescriptionText(conceptSequence))
                        .append("\n");
            }

            Alert.publishAddition(
                    new TreeCycleError(cycle, visitData, this, premiseTypes + " Cycle found", cycleDescription.toString(), AlertType.ERROR));
        }

        LOG.debug("Nodes visited: " + visitData.getNodesVisited());

        for (int nid : watchNids.toList()) {
            RoaringBitmap multiParents = visitData.getUserNodeSet(MULTI_PARENT_SETS, nid);

            LOG.debug(Get.conceptDescriptionText(nid) + " multiParentSet: " + multiParents);
        }

        return this;
    }
}
