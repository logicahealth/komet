package sh.isaac.api.coordinate;

import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.commit.CommitListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.MarshalUtil;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

import javax.annotation.PreDestroy;
import java.util.Objects;
import java.util.UUID;

@Service
@RunLevel(value = LookupService.SL_L2)
// Singleton from the perspective of HK2 managed instances, there will be more than one
// ManifoldCoordinateImmutable created in normal use.
public class ManifoldCoordinateImmutable implements ManifoldCoordinate, ImmutableCoordinate, CommitListener {

    private static final ConcurrentReferenceHashMap<ManifoldCoordinateImmutable, ManifoldCoordinateImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private static final int marshalVersion = 5;


    private final StampFilterImmutable edgeStampFilter;
    private final LanguageCoordinateImmutable languageCoordinate;
    private final StampFilterImmutable languageStampFilter;
    private final VertexSort vertexSort;
    private final StampFilterImmutable vertexStampFilter;
    private final NavigationCoordinateImmutable navigationCoordinateImmutable;
    private final LogicCoordinateImmutable logicCoordinateImmutable;
    private final Activity activity;
    private final EditCoordinateImmutable editCoordinate;
    private TaxonomySnapshot digraphSnapshot;

    private ManifoldCoordinateImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.navigationCoordinateImmutable = null;
        this.vertexSort = null;
        this.vertexStampFilter = null;
        this.edgeStampFilter = null;
        this.languageStampFilter = null;
        this.languageCoordinate = null;
        this.logicCoordinateImmutable = null;
        this.activity = null;
        this.editCoordinate = null;
    }
    /**
     * {@inheritDoc}
     */
    @PreDestroy
    public void reset() {
        SINGLETONS.clear();
    }

    private ManifoldCoordinateImmutable(StampFilterImmutable edgeStampFilter,
                                        LanguageCoordinateImmutable languageCoordinate,
                                        StampFilterImmutable languageStampFilter,
                                        VertexSort vertexSort,
                                        StampFilterImmutable vertexStampFilter,
                                        NavigationCoordinateImmutable navigationCoordinateImmutable,
                                        LogicCoordinateImmutable logicCoordinateImmutable,
                                        Activity activity,
                                        EditCoordinateImmutable editCoordinate) {

        this.edgeStampFilter = edgeStampFilter;
        this.languageCoordinate = languageCoordinate;
        this.languageStampFilter = languageStampFilter;
        this.vertexSort = vertexSort;
        this.vertexStampFilter = vertexStampFilter;
        this.navigationCoordinateImmutable = navigationCoordinateImmutable;
        this.logicCoordinateImmutable = logicCoordinateImmutable;
        this.activity = activity;
        this.editCoordinate = editCoordinate;
    }

    private ManifoldCoordinateImmutable(ByteArrayDataBuffer in, int objectMarshalVersion) {
        switch (objectMarshalVersion) {
            case marshalVersion:
                this.vertexSort = MarshalUtil.unmarshal(in);
                this.vertexStampFilter = MarshalUtil.unmarshal(in);
                this.edgeStampFilter = MarshalUtil.unmarshal(in);
                this.languageStampFilter  = MarshalUtil.unmarshal(in);
                this.languageCoordinate  = MarshalUtil.unmarshal(in);
                this.navigationCoordinateImmutable = MarshalUtil.unmarshal(in);
                this.logicCoordinateImmutable = MarshalUtil.unmarshal(in);
                this.activity = MarshalUtil.unmarshal(in);
                this.editCoordinate = MarshalUtil.unmarshal(in);
                break;
            case 4:
                this.vertexSort = MarshalUtil.unmarshal(in);
                this.vertexStampFilter = MarshalUtil.unmarshal(in);
                this.edgeStampFilter = MarshalUtil.unmarshal(in);
                this.languageStampFilter  = MarshalUtil.unmarshal(in);
                this.languageCoordinate  = MarshalUtil.unmarshal(in);
                this.navigationCoordinateImmutable = MarshalUtil.unmarshal(in);
                this.logicCoordinateImmutable = MarshalUtil.unmarshal(in);
                this.activity = Activity.VIEWING;
                this.editCoordinate = null;
                break;
            case 3:
                this.vertexSort = MarshalUtil.unmarshal(in);
                this.vertexStampFilter = MarshalUtil.unmarshal(in);
                this.edgeStampFilter = MarshalUtil.unmarshal(in);
                this.languageStampFilter  = MarshalUtil.unmarshal(in);
                this.languageCoordinate  = MarshalUtil.unmarshal(in);
                this.navigationCoordinateImmutable = MarshalUtil.unmarshal(in);
                this.logicCoordinateImmutable = Coordinates.Logic.ElPlusPlus();
                this.activity = Activity.VIEWING;
                this.editCoordinate = null;
                break;
            default:
                throw new IllegalStateException("Can't handle marshalVersion: " + objectMarshalVersion);
        }

        // this.digraphSnapshot = Get.taxonomyService().getSnapshot(toDefaultManifold(this));
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        MarshalUtil.marshal(this.vertexSort, out);
        MarshalUtil.marshal(this.vertexStampFilter, out);
        MarshalUtil.marshal(this.edgeStampFilter, out);
        MarshalUtil.marshal(this.languageStampFilter, out);
        MarshalUtil.marshal(this.languageCoordinate, out);
        MarshalUtil.marshal(this.navigationCoordinateImmutable, out);
        MarshalUtil.marshal(this.logicCoordinateImmutable, out);
        MarshalUtil.marshal(this.activity, out);
        MarshalUtil.marshal(this.editCoordinate, out);
    }

    @Override
    public NavigationCoordinateImmutable getNavigationCoordinate() {
        return this.navigationCoordinateImmutable;
    }

    @Override
    public LogicCoordinateImmutable getLogicCoordinate() {
        return this.logicCoordinateImmutable;
    }

    @Override
    public LanguageCoordinateImmutable getLanguageCoordinate() {
        return this.languageCoordinate;
    }

    @Override
    public EditCoordinate getEditCoordinate() {
        return this.editCoordinate;
    }

    @Unmarshaler
    public static ManifoldCoordinateImmutable make(ByteArrayDataBuffer in) {
        // Using a static method rather than a constructor eliminates the need for
        // a readResolve method, but allows the implementation to decide how
        // to handle special cases.
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case 1:
            case 3:
            case 4:
            case marshalVersion:
                return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(in, objectMarshalVersion),
                        manifoldCoordinateImmutable -> manifoldCoordinateImmutable);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }
    public static ManifoldCoordinateImmutable make(StampFilter edgeStampFilter,
                                                   LanguageCoordinate languageCoordinate,
                                                   StampFilter languageStampFilter,
                                                   VertexSort vertexSort,
                                                   StampFilter vertexStampFilter,
                                                   NavigationCoordinate navigationCoordinate,
                                                   LogicCoordinate logicCoordinate,
                                                   Activity activity,
                                                   EditCoordinate editCoordinate) {
         return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(edgeStampFilter.toStampFilterImmutable(),
                 languageCoordinate.toLanguageCoordinateImmutable(),
                 languageStampFilter.toStampFilterImmutable(),
                 vertexSort,
                 vertexStampFilter.toStampFilterImmutable(),
                 navigationCoordinate.toNavigationCoordinateImmutable(),
                 logicCoordinate.toLogicCoordinateImmutable(),
                         activity, editCoordinate.toEditCoordinateImmutable()),
                        manifoldCoordinateImmutable -> manifoldCoordinateImmutable);
    }

    public static ManifoldCoordinateImmutable makeStated(StampFilter stampFilter, LanguageCoordinate languageCoordinate,
                                                         Activity activity, EditCoordinate editCoordinate) {
        NavigationCoordinateImmutable dci = NavigationCoordinateImmutable.makeStated();
        return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(stampFilter.toStampFilterImmutable(),
                        languageCoordinate.toLanguageCoordinateImmutable(),
                        stampFilter.toStampFilterImmutable(),
                        VertexSortNaturalOrder.SINGLETON,
                        stampFilter.toStampFilterImmutable(), dci,
                        Coordinates.Logic.ElPlusPlus(),
                        activity, editCoordinate.toEditCoordinateImmutable()),
                manifoldCoordinateImmutable -> manifoldCoordinateImmutable);
    }

    public static ManifoldCoordinateImmutable makeStated(StampFilter stampFilter, LanguageCoordinate languageCoordinate,
                                                         LogicCoordinate logicCoordinate, Activity activity, EditCoordinate editCoordinate) {
        NavigationCoordinateImmutable dci = NavigationCoordinateImmutable.makeStated(logicCoordinate);
        return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(stampFilter.toStampFilterImmutable(),
                        languageCoordinate.toLanguageCoordinateImmutable(),
                        stampFilter.toStampFilterImmutable(),
                        VertexSortNaturalOrder.SINGLETON,
                        stampFilter.toStampFilterImmutable(), dci,
                        Coordinates.Logic.ElPlusPlus(),
                        activity, editCoordinate.toEditCoordinateImmutable()),
                manifoldCoordinateImmutable -> manifoldCoordinateImmutable);
    }

    public static ManifoldCoordinateImmutable makeInferred(StampFilter stampFilter,
                                                           LanguageCoordinate languageCoordinate,
                                                           LogicCoordinate logicCoordinate,
                                                           Activity activity, EditCoordinate editCoordinate) {
        NavigationCoordinateImmutable dci = NavigationCoordinateImmutable.makeInferred(logicCoordinate);
        return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(stampFilter.toStampFilterImmutable(),
                        languageCoordinate.toLanguageCoordinateImmutable(),
                        stampFilter.toStampFilterImmutable(),
                        VertexSortNaturalOrder.SINGLETON,
                        stampFilter.toStampFilterImmutable(), dci,
                        Coordinates.Logic.ElPlusPlus(),
                        activity, editCoordinate.toEditCoordinateImmutable()),
                manifoldCoordinateImmutable -> manifoldCoordinateImmutable);
    }

    @Override
    public ManifoldCoordinateImmutable toManifoldCoordinateImmutable() {
        return this;
    }

    @Override
    public Activity getCurrentActivity() {
        return activity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ManifoldCoordinateImmutable)) return false;
        ManifoldCoordinateImmutable that = (ManifoldCoordinateImmutable) o;
        return this.navigationCoordinateImmutable.equals(that.navigationCoordinateImmutable) &&
                this.vertexSort.equals(that.vertexSort) &&
                this.vertexStampFilter.equals(that.vertexStampFilter) &&
                this.edgeStampFilter.equals(that.edgeStampFilter) &&
                this.languageStampFilter.equals(that.languageStampFilter) &&
                this.languageCoordinate.equals(that.languageCoordinate) &&
                this.navigationCoordinateImmutable.equals(that.navigationCoordinateImmutable) &&
                this.activity == that.activity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getManifoldCoordinateUuid());
    }

    @Override
    public VertexSort getVertexSort() {
        return this.vertexSort;
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
    public String toString() {
        return "ManifoldCoordinateImmutable{" + this.activity.toUserString() + " " + this.getNavigationCoordinate() +
                ",\n  label and sort: " + this.vertexSort.getVertexSortName() +
                ",\n  edge filter: " + this.edgeStampFilter +
                ", \n vertex filter: " + this.vertexStampFilter
                + ", \n lang filter: " + this.languageStampFilter + ", \n" +
                this.languageCoordinate +
                ",\n edit=" + getEditCoordinate() +
                ",\n uuid=" + getManifoldCoordinateUuid() + '}';
    }

    @Override
    public TaxonomySnapshot getNavigationSnapshot() {
        if (this.digraphSnapshot == null) {
            this.digraphSnapshot = Get.taxonomyService().getSnapshot(this);
            Get.commitService().addCommitListener(this);
        }
        return this.digraphSnapshot;
    }

    @Override
    public UUID getListenerUuid() {
        return this.getManifoldCoordinateUuid();
    }

    @Override
    public void handleCommit(CommitRecord commitRecord) {
        this.digraphSnapshot = null;
        Get.commitService().removeCommitListener(this);
    }

    @Override
    public ManifoldCoordinateImmutable makeCoordinateAnalog(long classifyTimeInEpochMillis) {
        return new ManifoldCoordinateImmutable(edgeStampFilter.makeCoordinateAnalog(classifyTimeInEpochMillis),
                languageCoordinate,
                languageStampFilter.makeCoordinateAnalog(classifyTimeInEpochMillis),
                vertexSort,
                vertexStampFilter.makeCoordinateAnalog(classifyTimeInEpochMillis),
                navigationCoordinateImmutable,
                logicCoordinateImmutable,
                activity,
                editCoordinate);
    }
}
