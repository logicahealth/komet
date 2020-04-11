package sh.isaac.api.coordinate;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.Edge;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.util.UUIDUtil;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

/**
 * In mathematics, and more specifically in graph theory, a directed graph (or digraph)
 * is a graph that is made up of a set of vertices connected by edges, where the edges
 * have a direction associated with them.
 *
 * TODO change Graph NODE to Vertex everywhere since node is overloaded with JavaFx Node (which means something else)...
 */
public interface DigraphCoordinate {

    static UUID getDigraphCoordinateUuid(DigraphCoordinate digraphCoordinate) {
        ArrayList<UUID> uuidList = new ArrayList<>();
        for (int nid: digraphCoordinate.getDigraphIdentifierConceptNids().toArray()) {
            UUIDUtil.addSortedUuids(uuidList, nid);
        }
        uuidList.add(digraphCoordinate.getVertexStampFilter().getStampFilterUuid());
        uuidList.add(digraphCoordinate.getEdgeStampFilter().getStampFilterUuid());
        uuidList.add(digraphCoordinate.getLanguageStampFilter().getStampFilterUuid());
        uuidList.add(digraphCoordinate.getLanguageCoordinate().getLanguageCoordinateUuid());
        uuidList.add(digraphCoordinate.getLogicCoordinate().getLogicCoordinateUuid());
        return UUID.nameUUIDFromBytes(uuidList.toString().getBytes());
    }


    static ImmutableIntSet defaultDigraphConceptIdentifierNids() {
        return IntSets.immutable.of(TermAux.EL_PLUS_PLUS_DIGRAPH.getNid());
    }

    default UUID getDigraphCoordinateUuid() {
        return getDigraphCoordinateUuid(this);
    }

    int[] getRootNids();
    default ImmutableCollection<ConceptSpecification> getRoots() {
        return IntLists.immutable.of(getRootNids()).collect(nid -> Get.conceptSpecification(nid));
    }

    int[] getChildNids(int parentNid);
    default int[] getChildNids(ConceptSpecification parent) {
        return getChildNids(parent.getNid());
    }

    boolean isChildOf(int childNid, int parentNid);
    default boolean isChildOf(ConceptSpecification child, ConceptSpecification parent) {
        return isChildOf(child.getNid(), parent.getNid());
    }

    boolean isLeaf(int nid);
    default boolean isLeaf(ConceptSpecification concept) {
        return isLeaf(concept.getNid());
    }

    boolean isKindOf(int childNid, int parentNid);
    default boolean isKindOf(ConceptSpecification child, ConceptSpecification parent) {
        return isKindOf(child.getNid(), parent.getNid());
    }

    ImmutableIntSet getKindOfNidSet(int kindNid);
    default  ImmutableIntSet getKindOfNidSet(ConceptSpecification kind) {
        return getKindOfNidSet(kind.getNid());
    }

    boolean isDescendentOf(int descendantNid, int ancestorNid);
    default boolean isDescendentOf(ConceptSpecification descendant, ConceptSpecification ancestor) {
        return isDescendentOf(descendant.getNid(), ancestor.getNid());
    }

    ImmutableCollection<Edge> getParentEdges(int parentNid);
    default ImmutableCollection<Edge> getParentEdges(ConceptSpecification parent) {
        return getParentEdges(parent.getNid());
    }

    ImmutableCollection<Edge> getChildEdges(int childNid);
    default ImmutableCollection<Edge> getChildEdges(ConceptSpecification child) {
        return getChildEdges(child.getNid());
    }
    //---------------------------
    /**
     * In most cases, this coordinate will be the same object that is returned by {@link #getEdgeStampFilter()},
     * But, it may be a different, depending on the construction - for example, a use case like returning inactive
     * vertexes (concepts) linked by active edges (relationships).
     *
     * This filter is used on the vertexes (source and destination concepts)
     * in digraph operations, while {@link #getEdgeStampFilter()} is used
     * on the edges (relationships) themselves.
     *
     * @return The vertex stamp filter,
     */
    StampFilter getVertexStampFilter();

    /**
     * In most cases, this coordinate will be the same object that is returned by {@link #getVertexStampFilter()},
     * But, it may be a different, depending on the construction - for example, a use case like returning inactive
     * vertexes (concepts) linked by active edges (relationships).
     *
     * This filter is used on the edges (relationships) in digraph operations, while {@link #getVertexStampFilter()}
     * is used on the vertexes (concepts) themselves.
     *
     * @return The edge stamp filter,
     */
    StampFilter getEdgeStampFilter();

    /**
     * In most cases, this coordinate will be the same object that is returned by {@link #getVertexStampFilter()}
     * and {@link #getEdgeStampFilter()}.
     * @return the language stamp filter.
     */
    StampFilter getLanguageStampFilter();
    //---------------------------

    /**
     * Gets the edge type.
     *
     * @return PremiseType.STATED if edge operations should be based on stated definitions, or
     * PremiseType.INFERRED if edge operations should be based on inferred definitions.
     */
    PremiseType getPremiseType();

    LanguageCoordinate getLanguageCoordinate();

    LogicCoordinate getLogicCoordinate();

    ImmutableIntSet getDigraphIdentifierConceptNids();

    default ImmutableSet<ConceptSpecification> getDigraphIdentifierConcepts() {
        return IntSets.immutable.of(getDigraphIdentifierConceptNids().toArray()).collect(nid -> Get.conceptSpecification(nid));
    }

    DigraphCoordinateImmutable toDigraphImmutable();

    default Optional<LogicalExpression> getStatedLogicalExpression(ConceptSpecification spec) {
        return getStatedLogicalExpression(spec.getNid());
    }
    default Optional<LogicalExpression> getInferredLogicalExpression(ConceptSpecification spec) {
        return getInferredLogicalExpression(spec.getNid());
    }
    default LatestVersion<LogicGraphVersion> getInferredLogicGraphVersion(ConceptSpecification conceptSpecification) {
        return getInferredLogicGraphVersion(conceptSpecification.getNid());
    }
    default LatestVersion<LogicGraphVersion> getStatedLogicGraphVersion(ConceptSpecification conceptSpecification) {
        return getStatedLogicGraphVersion(conceptSpecification.getNid());
    }


    default Optional<LogicalExpression> getStatedLogicalExpression(int conceptNid) {
        return getLogicCoordinate().getLogicalExpression(conceptNid, PremiseType.STATED, getVertexStampFilter());
    }

    default Optional<LogicalExpression> getInferredLogicalExpression(int conceptNid) {
        return getLogicCoordinate().getLogicalExpression(conceptNid, PremiseType.INFERRED, getVertexStampFilter());
    }

    default Optional<LogicalExpression> getLogicalExpression(int conceptNid, PremiseType premiseType) {
        ConceptChronology concept = Get.concept(conceptNid);
        LatestVersion<LogicGraphVersion> logicalDef = concept.getLogicalDefinition(getVertexStampFilter(), premiseType, getLogicCoordinate());
        if (logicalDef.isPresent()) {
            return Optional.of(logicalDef.get().getLogicalExpression());
        }
        return Optional.empty();
    }

    default LatestVersion<LogicGraphVersion> getStatedLogicGraphVersion(int conceptNid) {
        return getLogicCoordinate().getLogicGraphVersion(conceptNid, PremiseType.STATED, getVertexStampFilter());
    }

    default LatestVersion<LogicGraphVersion> getInferredLogicGraphVersion(int conceptNid) {
        return getLogicCoordinate().getLogicGraphVersion(conceptNid, PremiseType.INFERRED, getVertexStampFilter());
    }

    default LatestVersion<LogicGraphVersion> getLogicGraphVersion(int conceptNid, PremiseType premiseType) {
        ConceptChronology concept = Get.concept(conceptNid);
        return concept.getLogicalDefinition(getVertexStampFilter(), premiseType, getLogicCoordinate());
    }
    default String toUserString() {
        StringBuilder sb = new StringBuilder("Digraph coordinate: ");
        sb.append("\nPremise: ").append(getPremiseType());
        sb.append("\nIncluded digraphs: ");
        for (int nid: getDigraphIdentifierConceptNids().toArray()) {
            sb.append("\n     ").append(Get.conceptDescriptionText(nid));
        }
        sb.append("\n\nLanguage coordinate:\n").append(getLanguageCoordinate().toUserString());
        sb.append("\n\nLogic coordinate:\n").append(getLogicCoordinate().toUserString());
        sb.append("\n\nEdge filter:\n").append(getEdgeStampFilter().toUserString());
        sb.append("\n\nVertex filter:\n").append(getVertexStampFilter().toUserString());
        sb.append("\n\nLanguage filter:\n").append(getLanguageStampFilter().toUserString());
        return sb.toString();
    }

}
