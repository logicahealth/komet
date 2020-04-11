package sh.isaac.api.coordinate;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import sh.isaac.api.Edge;

public interface DigraphCoordinateProxy extends DigraphCoordinate {
    DigraphCoordinate getDigraph();

    @Override
    default int[] getRootNids() {
        return getDigraph().getRootNids();
    }

    @Override
    default int[] getChildNids(int parentNid) {
        return getDigraph().getChildNids(parentNid);
    }

    @Override
    default boolean isChildOf(int childNid, int parentNid) {
        return getDigraph().isChildOf(childNid, parentNid);
    }

    @Override
    default boolean isLeaf(int nid) {
        return getDigraph().isLeaf(nid);
    }

    @Override
    default boolean isKindOf(int childNid, int parentNid) {
        return getDigraph().isKindOf(childNid, parentNid);
    }

    @Override
    default ImmutableIntSet getKindOfNidSet(int kindNid) {
        return getDigraph().getKindOfNidSet(kindNid);
    }

    @Override
    default boolean isDescendentOf(int descendantNid, int ancestorNid) {
        return getDigraph().isDescendentOf(descendantNid, ancestorNid);
    }

    @Override
    default ImmutableCollection<Edge> getParentEdges(int parentNid) {
        return getDigraph().getParentEdges(parentNid);
    }

    @Override
    default ImmutableCollection<Edge> getChildEdges(int childNid) {
        return getDigraph().getChildEdges(childNid);
    }

    @Override
    default StampFilter getVertexStampFilter() {
        return getDigraph().getVertexStampFilter();
    }

    @Override
    default StampFilter getEdgeStampFilter() {
        return getDigraph().getEdgeStampFilter();
    }

    @Override
    default StampFilter getLanguageStampFilter() {
        return getDigraph().getLanguageStampFilter();
    }

    @Override
    default PremiseType getPremiseType() {
        return getDigraph().getPremiseType();
    }

    @Override
    default LanguageCoordinate getLanguageCoordinate() {
        return getDigraph().getLanguageCoordinate();
    }

    @Override
    default LogicCoordinate getLogicCoordinate() {
        return getDigraph().getLogicCoordinate();
    }

    @Override
    default ImmutableIntSet getDigraphIdentifierConceptNids() {
        return getDigraph().getDigraphIdentifierConceptNids();
    }

    @Override
    default DigraphCoordinateImmutable toDigraphImmutable() {
        return getDigraph().toDigraphImmutable();
    }

}
