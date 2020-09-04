package sh.isaac.model.taxonomy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.coordinate.VertexSort;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.model.tree.HashTreeBuilderIsolated;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.function.ObjIntConsumer;


/**
 * Stream-based, parallelizable,  collector to create a graph, which represents a
 * particular point in time, and a particular semantic state (stated or inferred)
 * of a taxonomy. The HashTreeBuilder does not require concurrent access, since there is one
 * HashTreeBuilder per thread, and then when the process completes, the HashTreeBuilders are
 * merged in a single thread.
 * @author kec
 */
public class GraphCollectorIsolated
        implements ObjIntConsumer<HashTreeBuilderIsolated>, BiConsumer<HashTreeBuilderIsolated, HashTreeBuilderIsolated> {

    private static final Logger LOG = LogManager.getLogger();
    /** The isa concept nid. */
    private final int ISA_CONCEPT_NID = TermAux.IS_A.getNid();

    /** The watch list. */
    private NidSet watchList = new NidSet();

    /** The taxonomy map. */
    private final IntFunction<int[]> taxonomyDataProvider;

    private final ManifoldCoordinateImmutable digraph;
    private final RelativePositionCalculator edgeComputer;
    private final RelativePositionCalculator vertexComputer;
    private final VertexSort sort;

    /** The taxonomy flags. */
    private final int[] taxonomyFlags;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new graph collector.
     *
     * @param taxonomyDataProvider the taxonomy map
     * @param manifoldCoordinate calculates current versions of components.
     */
    public GraphCollectorIsolated(IntFunction<int[]> taxonomyDataProvider,
                                  ManifoldCoordinate manifoldCoordinate) {
        this(taxonomyDataProvider, manifoldCoordinate.toManifoldCoordinateImmutable(), manifoldCoordinate.getVertexSort());
    }

    /**
     * Instantiates a new graph collector.
     *
     * @param taxonomyDataProvider the taxonomy map
     * @param manifoldCoordinateImmutable calculates current versions of components.
     */
    public GraphCollectorIsolated(IntFunction<int[]> taxonomyDataProvider,
                                  ManifoldCoordinateImmutable manifoldCoordinateImmutable, VertexSort sort) {
        if (taxonomyDataProvider == null) {
            throw new IllegalStateException("taxonomyDataProvider cannot be null");
        }
        this.taxonomyDataProvider = taxonomyDataProvider;
        this.digraph = manifoldCoordinateImmutable;
        this.edgeComputer = manifoldCoordinateImmutable.getViewStampFilter().getRelativePositionCalculator();
        this.vertexComputer = manifoldCoordinateImmutable.getVertexStampFilter().getRelativePositionCalculator();
        this.taxonomyFlags = manifoldCoordinateImmutable.getPremiseTypes().getFlags();
        this.sort = sort;
     }

    //~--- methods -------------------------------------------------------------

    /**
     * Accept.
     *
     * @param t the t
     * @param u the u
     */
    @Override
    public void accept(HashTreeBuilderIsolated t, HashTreeBuilderIsolated u) {
        t.combine(u);
    }

    /**
     * Accept.
     *
     * @param graphBuilder the graph builder
     * @param originNid the origin sequence
     */
    @Override
    public void accept(HashTreeBuilderIsolated graphBuilder, int originNid) {
        final int[] taxonomyData = this.taxonomyDataProvider.apply(originNid);

        if (taxonomyData == null) {
            LOG.error("No taxonomy data for: {} {} with NID: {}", Get.identifierService().getUuidPrimordialForNid(originNid), Get.conceptDescriptionText(originNid), originNid);

        } else {
            TaxonomyRecordPrimitive isaacPrimitiveTaxonomyRecord = new TaxonomyRecordPrimitive(taxonomyData);
            // For debugging.
            if (Get.configurationService().isVerboseDebugEnabled() && this.watchList.contains(originNid)) {
                LOG.debug("Found watch: " + isaacPrimitiveTaxonomyRecord);
            }

            final TaxonomyRecord taxonomyRecordUnpacked = isaacPrimitiveTaxonomyRecord.getTaxonomyRecordUnpacked();
            // TODO implement getConceptNidsForType on TaxonomyRecordPrimitive
            final int[] destinationConceptNids = taxonomyRecordUnpacked.getConceptNidsForType(this.ISA_CONCEPT_NID,
                    this.taxonomyDataProvider, this.taxonomyFlags, this.edgeComputer,
                    this.vertexComputer, this.sort, this.digraph);


         if (destinationConceptNids.length == 0  &&
                 originNid != TermAux.SOLOR_ROOT.getNid() &&
                 isaacPrimitiveTaxonomyRecord.containsStampOfTypeWithFlags(this.ISA_CONCEPT_NID, this.taxonomyFlags) &&
                 isaacPrimitiveTaxonomyRecord.isConceptActive(originNid, this.vertexComputer)) {
            // again for steping through with the debugger. Remove when issues resolved.
             //LOG.info("Found concept with no parents: " + Get.conceptDescriptionText(originNid) + " <" + originNid + ">");
//             final int[] destinationConceptNids2 = taxonomyRecordUnpacked.getConceptNidsForType(this.ISA_CONCEPT_NID,
//                    this.taxonomyDataProvider, this.taxonomyFlags, this.edgeComputer,
//                    this.vertexComputer, this.sort, this.digraph);
//             LOG.info("Second try equals: " + Arrays.equals(destinationConceptNids, destinationConceptNids2));
//             LOG.info("Second try: " + Arrays.toString(destinationConceptNids2));
         }
//         int parentCount = 0;
            for (int destinationNid: destinationConceptNids) {
//            parentCount++;
                graphBuilder.add(destinationNid, originNid);
            }
//         if (parentCount == 0) {
//            System.out.println("No parent for: " + Get.conceptDescriptionText(originNid));
//            System.out.println("TaxonomyRecord: " + taxonomyRecordUnpacked);
//            StringBuilder builder = new StringBuilder("[");
//            for (int element: taxonomyData) {
//               builder.append(element);
//               builder.append(", ");
//            }
//            builder.replace(builder.length()-1, builder.length()-1, "]");
//            System.out.println("Source data: " + builder.toString());
//         }
        }
    }

    /**
     * Adds the to watch list.
     *
     * @param uuid the uuid
     * @throws RuntimeException the runtime exception
     */
    public final void addToWatchList(String uuid)
            throws RuntimeException {
        this.watchList.add(Get.identifierService()
                .getNidForUuids(UUID.fromString(uuid)));
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        final StringBuilder buff = new StringBuilder();

        buff.append("GraphCollectorIsolated{");
        buff.append(digraph.toUserString());
        buff.append("}");
        return buff.toString();
    }
}
