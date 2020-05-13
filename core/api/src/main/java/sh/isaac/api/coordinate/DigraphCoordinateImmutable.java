package sh.isaac.api.coordinate;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.*;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.MarshalUtil;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

import javax.annotation.PreDestroy;
import java.util.Objects;

@Service
@RunLevel(value = LookupService.SL_L2)
// Singleton from the perspective of HK2 managed instances, there will be more than one
// StampFilterImmutable created in normal use.
public final class DigraphCoordinateImmutable implements DigraphCoordinate, ImmutableCoordinate {

    private static final ConcurrentReferenceHashMap<DigraphCoordinateImmutable, DigraphCoordinateImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private static final int marshalVersion = 1;

    private final StampFilterImmutable vertexStampFilter;
    private final StampFilterImmutable edgeStampFilter;
    private final StampFilterImmutable languageStampFilter;
    private final PremiseType premiseType;
    private final LanguageCoordinateImmutable languageCoordinate;
    private final LogicCoordinateImmutable logicCoordinate;
    private final ImmutableIntSet digraphConceptNids;
    private final TaxonomySnapshot digraphSnapshot;

    private static ManifoldCoordinateImmutable toDefaultManifold(DigraphCoordinateImmutable digraphCoordinateImmutable) {
        return ManifoldCoordinateImmutable.make(VertexSortRegularName.SINGLETON, digraphCoordinateImmutable, digraphCoordinateImmutable.getEdgeStampFilter());
    }

    private DigraphCoordinateImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.vertexStampFilter = null;
        this.edgeStampFilter = null;
        this.languageStampFilter = null;
        this.premiseType = null;
        this.languageCoordinate = null;
        this.logicCoordinate = null;
        this.digraphConceptNids = null;
        this.digraphSnapshot = null;
    }

    /**
     * {@inheritDoc}
     */
    @PreDestroy
    public void reset() {
        SINGLETONS.clear();
    }

    /**
     *
     * @param vertexStampFilter
     * @param edgeStampFilter
     * @param languageStampFilter
     * @param premiseType
     * @param languageCoordinate
     * @param logicCoordinate
     * @param digraphConceptNids
     */
    private DigraphCoordinateImmutable(StampFilterImmutable vertexStampFilter,
                                      StampFilterImmutable edgeStampFilter,
                                      StampFilterImmutable languageStampFilter,
                                      PremiseType premiseType,
                                      LanguageCoordinateImmutable languageCoordinate,
                                      LogicCoordinateImmutable logicCoordinate,
                                      ImmutableIntSet digraphConceptNids) {
        this.vertexStampFilter = vertexStampFilter;
        this.edgeStampFilter = edgeStampFilter;
        this.languageStampFilter = languageStampFilter;
        this.premiseType = premiseType;
        this.languageCoordinate = languageCoordinate;
        this.logicCoordinate = logicCoordinate;
        this.digraphConceptNids = digraphConceptNids;
        this.digraphSnapshot = Get.taxonomyService().getSnapshot(toDefaultManifold(this));
    }
    public static DigraphCoordinateImmutable make(StampFilterImmutable vertexStampFilter,
                                                  StampFilterImmutable edgeStampFilter,
                                                  StampFilterImmutable languageStampFilter,
                                                  PremiseType premiseType,
                                                  LanguageCoordinateImmutable languageCoordinate,
                                                  LogicCoordinateImmutable logicCoordinate,
                                                  ImmutableIntSet digraphConceptNids) {
        return SINGLETONS.computeIfAbsent(new DigraphCoordinateImmutable(vertexStampFilter, edgeStampFilter, languageStampFilter,
                        premiseType, languageCoordinate, logicCoordinate, digraphConceptNids),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    /**
     * Uses the same stamp filter for vertex, edge, and language.
     * @param stampFilter
     * @param premiseType
     * @param languageCoordinate
     * @param logicCoordinate
     * @param digraphConceptNids
     */
    private DigraphCoordinateImmutable(StampFilterImmutable stampFilter,
                                      PremiseType premiseType,
                                      LanguageCoordinateImmutable languageCoordinate,
                                      LogicCoordinateImmutable logicCoordinate,
                                      ImmutableIntSet digraphConceptNids) {
        this(stampFilter, stampFilter, stampFilter, premiseType, languageCoordinate, logicCoordinate, digraphConceptNids);
    }
    public static DigraphCoordinateImmutable make(StampFilterImmutable stampFilter,
                                                  PremiseType premiseType,
                                                  LanguageCoordinateImmutable languageCoordinate,
                                                  LogicCoordinateImmutable logicCoordinate,
                                                  ImmutableIntSet digraphConceptNids) {
        return SINGLETONS.computeIfAbsent(new DigraphCoordinateImmutable(stampFilter,
                        premiseType, languageCoordinate, logicCoordinate, digraphConceptNids),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    public static DigraphCoordinateImmutable makeInferred(StampFilterImmutable stampFilter) {
        return SINGLETONS.computeIfAbsent(new DigraphCoordinateImmutable(stampFilter,
                        PremiseType.INFERRED, Coordinates.Language.UsEnglishFullyQualifiedName(),
                        Coordinates.Logic.ElPlusPlus(), IntSets.immutable.of(TermAux.EL_PLUS_PLUS_DIGRAPH.getNid())),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    public static DigraphCoordinateImmutable makeInferred(StampFilter stampFilter, LanguageCoordinate languageCoordinate) {
        return SINGLETONS.computeIfAbsent(new DigraphCoordinateImmutable(stampFilter.toStampFilterImmutable(),
                        PremiseType.INFERRED, languageCoordinate.toLanguageCoordinateImmutable(),
                        Coordinates.Logic.ElPlusPlus(), IntSets.immutable.of(TermAux.EL_PLUS_PLUS_DIGRAPH.getNid())),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    public static DigraphCoordinateImmutable makeInferred(StampFilter stampFilter, LanguageCoordinate languageCoordinate, LogicCoordinate logicCoordinate) {
        return SINGLETONS.computeIfAbsent(new DigraphCoordinateImmutable(stampFilter.toStampFilterImmutable(),
                        PremiseType.INFERRED, languageCoordinate.toLanguageCoordinateImmutable(),
                        logicCoordinate.toLogicCoordinateImmutable(), IntSets.immutable.of(TermAux.EL_PLUS_PLUS_DIGRAPH.getNid())),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    public static DigraphCoordinateImmutable makeStated(StampFilter stampFilter, LanguageCoordinate languageCoordinate, LogicCoordinate logicCoordinate) {
        return SINGLETONS.computeIfAbsent(new DigraphCoordinateImmutable(stampFilter.toStampFilterImmutable(),
                        PremiseType.STATED, languageCoordinate.toLanguageCoordinateImmutable(),
                        logicCoordinate.toLogicCoordinateImmutable(), IntSets.immutable.of(TermAux.EL_PLUS_PLUS_DIGRAPH.getNid())),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }


    public static DigraphCoordinateImmutable makeStated(StampFilter stampFilter, LanguageCoordinate languageCoordinate) {
        return SINGLETONS.computeIfAbsent(new DigraphCoordinateImmutable(stampFilter.toStampFilterImmutable(),
                        PremiseType.STATED, languageCoordinate.toLanguageCoordinateImmutable(),
                        Coordinates.Logic.ElPlusPlus(), IntSets.immutable.of(TermAux.EL_PLUS_PLUS_DIGRAPH.getNid())),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    public static DigraphCoordinateImmutable makeStated(StampFilterImmutable stampFilter) {
        return SINGLETONS.computeIfAbsent(new DigraphCoordinateImmutable(stampFilter,
                        PremiseType.STATED, Coordinates.Language.UsEnglishFullyQualifiedName(),
                        Coordinates.Logic.ElPlusPlus(), IntSets.immutable.of(TermAux.EL_PLUS_PLUS_DIGRAPH.getNid())),
                digraphCoordinateImmutable -> digraphCoordinateImmutable);
    }

    private DigraphCoordinateImmutable(ByteArrayDataBuffer in) {
        this.vertexStampFilter = MarshalUtil.unmarshal(in);
        this.edgeStampFilter = MarshalUtil.unmarshal(in);
        this.languageStampFilter  = MarshalUtil.unmarshal(in);
        this.premiseType = PremiseType.valueOf(in.getUTF());
        this.languageCoordinate  = MarshalUtil.unmarshal(in);
        this.logicCoordinate  = MarshalUtil.unmarshal(in);
        this.digraphConceptNids = IntSets.immutable.of(in.getNidArray());
        this.digraphSnapshot = Get.taxonomyService().getSnapshot(toDefaultManifold(this));
    }

    /**
     * Uses the same stamp filter for vertex, edge, and language.
     * Uses DigraphCoordinate.defaultDigraphConceptNids(); for the digraph concepts.
     * @param stampFilter
     * @param premiseType
     * @param languageCoordinate
     * @param logicCoordinate
     */
    private DigraphCoordinateImmutable(StampFilterImmutable stampFilter,
                                      PremiseType premiseType,
                                      LanguageCoordinateImmutable languageCoordinate,
                                      LogicCoordinateImmutable logicCoordinate) {
        this(stampFilter, premiseType, languageCoordinate, logicCoordinate, DigraphCoordinate.defaultDigraphConceptIdentifierNids());
    }

    public static DigraphCoordinateImmutable make(StampFilterImmutable stampFilter,
                                                  PremiseType premiseType,
                                                  LanguageCoordinateImmutable languageCoordinate,
                                                  LogicCoordinateImmutable logicCoordinate) {
         return SINGLETONS.computeIfAbsent(new DigraphCoordinateImmutable(stampFilter,
                         premiseType, languageCoordinate, logicCoordinate),
                        digraphCoordinateImmutable -> digraphCoordinateImmutable);
     }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        MarshalUtil.marshal(this.vertexStampFilter, out);
        MarshalUtil.marshal(this.edgeStampFilter, out);
        MarshalUtil.marshal(this.languageStampFilter, out);
        out.putUTF(this.premiseType.name());
        MarshalUtil.marshal(this.languageCoordinate, out);
        MarshalUtil.marshal(this.logicCoordinate, out);
        out.putIntArray(this.digraphConceptNids.toArray());
    }

    @Unmarshaler
    public static DigraphCoordinateImmutable make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return SINGLETONS.computeIfAbsent(new DigraphCoordinateImmutable(in),
                        digraphCoordinateImmutable -> digraphCoordinateImmutable);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    @Override
    public StampFilterImmutable getVertexStampFilter() {
        return this.vertexStampFilter;
    }

    @Override
    public StampFilterImmutable getEdgeStampFilter() {
        return this.edgeStampFilter;
    }

    @Override
    public StampFilterImmutable getLanguageStampFilter() {
        return this.languageStampFilter;
    }

    @Override
    public PremiseType getPremiseType() {
        return this.premiseType;
    }

    @Override
    public LanguageCoordinateImmutable getLanguageCoordinate() {
        return this.languageCoordinate;
    }

    @Override
    public LogicCoordinateImmutable getLogicCoordinate() {
        return this.logicCoordinate;
    }

    @Override
    public ImmutableIntSet getDigraphIdentifierConceptNids() {
        return this.digraphConceptNids;
    }

    @Override
    public int[] getRootNids() {
        return this.digraphSnapshot.getRootNids();
    }

    @Override
    public int[] getChildNids(int parentNid) {
        return this.digraphSnapshot.getTaxonomyChildConceptNids(parentNid);
    }

    @Override
    public boolean isChildOf(int childNid, int parentNid) {
        return this.digraphSnapshot.isChildOf(childNid, parentNid);
    }

    @Override
    public boolean isLeaf(int nid) {
        return this.digraphSnapshot.isLeaf(nid);
    }

    @Override
    public boolean isKindOf(int childNid, int parentNid) {
        return this.digraphSnapshot.isKindOf(childNid, parentNid);
    }

    @Override
    public ImmutableIntSet getKindOfNidSet(int kindNid) {
        return this.digraphSnapshot.getKindOfConcept(kindNid);
    }

    @Override
    public boolean isDescendentOf(int descendantNid, int ancestorNid) {
        return this.digraphSnapshot.isDescendentOf(descendantNid, ancestorNid);
    }

    @Override
    public ImmutableCollection<Edge> getParentEdges(int parentNid) {
        return this.digraphSnapshot.getTaxonomyParentLinks(parentNid);
    }

    @Override
    public ImmutableCollection<Edge> getChildEdges(int childNid) {
        return this.digraphSnapshot.getTaxonomyChildLinks(childNid);
    }

    @Override
    public DigraphCoordinateImmutable toDigraphImmutable() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DigraphCoordinateImmutable)) return false;
        DigraphCoordinateImmutable that = (DigraphCoordinateImmutable) o;
        return getVertexStampFilter().equals(that.getVertexStampFilter()) &&
                getEdgeStampFilter().equals(that.getEdgeStampFilter()) &&
                getLanguageStampFilter().equals(that.getLanguageStampFilter()) &&
                getPremiseType() == that.getPremiseType() &&
                getLanguageCoordinate().equals(that.getLanguageCoordinate()) &&
                getLogicCoordinate().equals(that.getLogicCoordinate()) &&
                getDigraphIdentifierConceptNids().equals(that.getDigraphIdentifierConceptNids());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVertexStampFilter(), getEdgeStampFilter(), getLanguageStampFilter(), getPremiseType(), getLanguageCoordinate(), getLogicCoordinate(), getDigraphIdentifierConceptNids());
    }

    @Override
    public String toString() {
        return "DigraphCoordinateImmutable{" + this.premiseType + ",\n  edge filter: " + this.edgeStampFilter + ", \n vertex filter: " + this.vertexStampFilter
                + ", \n lang filter: " + this.languageStampFilter + ", \n" +
                this.languageCoordinate + ", \n" + this.logicCoordinate +
                ", \n digraph id concepts: " + this.digraphConceptNids + ",\n uuid=" + getDigraphCoordinateUuid() + '}';
    }

}