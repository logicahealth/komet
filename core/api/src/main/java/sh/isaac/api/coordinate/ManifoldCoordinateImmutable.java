package sh.isaac.api.coordinate;


import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.GlobalDatastoreConfiguration;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.commit.CommitListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.MarshalUtil;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

//This class is not treated as a service, however, it needs the annotation, so that the reset() gets fired at appropriate times.
@Service
public class ManifoldCoordinateImmutable implements ManifoldCoordinate, ImmutableCoordinate, CommitListener, StaticIsaacCache {

    private static final ConcurrentReferenceHashMap<ManifoldCoordinateImmutable, ManifoldCoordinateImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private static final int marshalVersion = 6;


    private final StampFilterImmutable viewStampFilter;
    private final LanguageCoordinateImmutable languageCoordinate;
    private final VertexSort vertexSort;
    private final StampFilterImmutable vertexStampFilter;
    private final NavigationCoordinateImmutable navigationCoordinateImmutable;
    private final LogicCoordinateImmutable logicCoordinateImmutable;
    private final Activity activity;
    private final EditCoordinateImmutable editCoordinate;
    private transient TaxonomySnapshot digraphSnapshot;
    private transient PremiseSet premiseTypes;

    private ManifoldCoordinateImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.navigationCoordinateImmutable = null;
        this.vertexSort = null;
        this.vertexStampFilter = null;
        this.viewStampFilter = null;
        this.languageCoordinate = null;
        this.logicCoordinateImmutable = null;
        this.activity = null;
        this.editCoordinate = null;
    }
    
    @Override
    public void reset() {
        SINGLETONS.clear();
    }

    /**
     * @see #make(StampFilter, LanguageCoordinate, VertexSort, StatusSet, NavigationCoordinate, LogicCoordinate, Activity, EditCoordinate)
     * for defaults on null values 
     */
    private ManifoldCoordinateImmutable(StampFilterImmutable viewStampFilter,
                                        LanguageCoordinateImmutable languageCoordinate,
                                        VertexSort vertexSort,
                                        StatusSet vertexStatusSet,
                                        NavigationCoordinateImmutable navigationCoordinateImmutable,
                                        LogicCoordinateImmutable logicCoordinateImmutable,
                                        Activity activity,
                                        EditCoordinateImmutable editCoordinate) {
        this.viewStampFilter = viewStampFilter == null ? 
            Get.configurationService().getGlobalDatastoreConfiguration().getDefaultManifoldCoordinate().getViewStampFilter().toStampFilterImmutable() : viewStampFilter;
        this.languageCoordinate = languageCoordinate == null ? 
            Get.configurationService().getGlobalDatastoreConfiguration().getDefaultLanguageCoordinate().toLanguageCoordinateImmutable() : languageCoordinate;
        this.vertexSort = vertexSort == null ? VertexSortNone.SINGLETON : vertexSort;
        this.vertexStampFilter = vertexStatusSet == null ? viewStampFilter : StampFilterImmutable.make(vertexStatusSet,
                viewStampFilter.getStampPosition(),
                viewStampFilter.getModuleNids(),
                viewStampFilter.getExcludedModuleNids(),
                viewStampFilter.getModulePriorityOrder());
        this.navigationCoordinateImmutable = navigationCoordinateImmutable == null ? 
            Get.configurationService().getGlobalDatastoreConfiguration().getDefaultManifoldCoordinate().toNavigationCoordinateImmutable() : navigationCoordinateImmutable;
        this.logicCoordinateImmutable = logicCoordinateImmutable == null ? 
            Get.configurationService().getGlobalDatastoreConfiguration().getDefaultLogicCoordinate().toLogicCoordinateImmutable() : logicCoordinateImmutable;
        this.activity = activity == null ? Activity.DEVELOPING : activity;
        this.editCoordinate = editCoordinate == null ? Get.configurationService().getGlobalDatastoreConfiguration().getDefaultWriteCoordinate().get().toEditCoordinate() :
            editCoordinate;
    }

    private ManifoldCoordinateImmutable(ByteArrayDataBuffer in, int objectMarshalVersion) {
        switch (objectMarshalVersion) {
            case marshalVersion:
                this.vertexSort = MarshalUtil.unmarshal(in);
                this.vertexStampFilter = MarshalUtil.unmarshal(in);
                this.viewStampFilter = MarshalUtil.unmarshal(in);
                this.languageCoordinate  = MarshalUtil.unmarshal(in);
                this.navigationCoordinateImmutable = MarshalUtil.unmarshal(in);
                this.logicCoordinateImmutable = MarshalUtil.unmarshal(in);
                this.activity = MarshalUtil.unmarshal(in);
                this.editCoordinate = MarshalUtil.unmarshal(in);
                break;
            case 5:
                this.vertexSort = MarshalUtil.unmarshal(in);
                this.vertexStampFilter = MarshalUtil.unmarshal(in);
                this.viewStampFilter = MarshalUtil.unmarshal(in);
                MarshalUtil.unmarshal(in); // Language stamp filter.
                this.languageCoordinate  = MarshalUtil.unmarshal(in);
                this.navigationCoordinateImmutable = MarshalUtil.unmarshal(in);
                this.logicCoordinateImmutable = MarshalUtil.unmarshal(in);
                this.activity = MarshalUtil.unmarshal(in);
                this.editCoordinate = MarshalUtil.unmarshal(in);
                break;
            case 4:
                this.vertexSort = MarshalUtil.unmarshal(in);
                this.vertexStampFilter = MarshalUtil.unmarshal(in);
                this.viewStampFilter = MarshalUtil.unmarshal(in);
                MarshalUtil.unmarshal(in); // Language stamp filter.
                this.languageCoordinate  = MarshalUtil.unmarshal(in);
                this.navigationCoordinateImmutable = MarshalUtil.unmarshal(in);
                this.logicCoordinateImmutable = MarshalUtil.unmarshal(in);
                this.activity = Activity.VIEWING;
                this.editCoordinate = null;
                break;
            case 3:
                this.vertexSort = MarshalUtil.unmarshal(in);
                this.vertexStampFilter = MarshalUtil.unmarshal(in);
                this.viewStampFilter = MarshalUtil.unmarshal(in);
                MarshalUtil.unmarshal(in); // Language stamp filter.
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
        MarshalUtil.marshal(this.viewStampFilter, out);
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
    
    /**
     * 
     * @param viewStampFilter - if null, uses {@link GlobalDatastoreConfiguration#getDefaultManifoldCoordinate()#getViewStampFilter()}
     * @param languageCoordinate - if null, uses {@link GlobalDatastoreConfiguration#getDefaultLanguageCoordinate()}
     * @param vertexSort - if null, does no sorting (using {@link VertexSortNone} )
     * @param vertexStatusSet - if null, use the statusSet from the viewStampFilter
     * @param navigationCoordinate - if null, uses {@link GlobalDatastoreConfiguration#getDefaultManifoldCoordinate()#toNavigationCoordinateImmutable()
     * @param logicCoordinate - if null, uses {@link GlobalDatastoreConfiguration#getDefaultLogicCoordinate()}
     * @param activity - if null, uses {@link Activity#DEVELOPING}
     * @param editCoordinate - if null, uses the {@link GlobalDatastoreConfiguration#getDefaultWriteCoordinate()} converted to an edit coordinate
     * @return
     */
    public static ManifoldCoordinateImmutable make(StampFilter viewStampFilter,
                                                   LanguageCoordinate languageCoordinate,
                                                   VertexSort vertexSort,
                                                   StatusSet vertexStatusSet,
                                                   NavigationCoordinate navigationCoordinate,
                                                   LogicCoordinate logicCoordinate,
                                                   Activity activity,
                                                   EditCoordinate editCoordinate) {
         return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(viewStampFilter.toStampFilterImmutable(),
                 languageCoordinate.toLanguageCoordinateImmutable(),
                 vertexSort,
                 vertexStatusSet,
                 navigationCoordinate.toNavigationCoordinateImmutable(),
                 logicCoordinate.toLogicCoordinateImmutable(),
                         activity, editCoordinate.toEditCoordinateImmutable()),
                        manifoldCoordinateImmutable -> manifoldCoordinateImmutable);
    }

    /**
     * @param stampFilter
     * @param languageCoordinate - optional - uses default if not provided
     * @return
     */
    public static ManifoldCoordinateImmutable makeStated(StampFilter stampFilter, LanguageCoordinate languageCoordinate) {
        return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(stampFilter.toStampFilterImmutable(),
                        languageCoordinate == null ? 
                                Get.configurationService().getGlobalDatastoreConfiguration().getDefaultLanguageCoordinate().toLanguageCoordinateImmutable() : 
                            languageCoordinate.toLanguageCoordinateImmutable(),
                        VertexSortNaturalOrder.SINGLETON,
                        stampFilter.getAllowedStates(), NavigationCoordinateImmutable.makeStated(),
                        Get.configurationService().getGlobalDatastoreConfiguration().getDefaultLogicCoordinate().toLogicCoordinateImmutable(),
                        Activity.DEVELOPING, Get.configurationService().getGlobalDatastoreConfiguration().getDefaultWriteCoordinate().get().toEditCoordinate()),
                manifoldCoordinateImmutable -> manifoldCoordinateImmutable);
    }

    public static ManifoldCoordinateImmutable makeStated(StampFilter stampFilter, LanguageCoordinate languageCoordinate,
                                                         LogicCoordinate logicCoordinate, Activity activity, EditCoordinate editCoordinate) {
        NavigationCoordinateImmutable dci = NavigationCoordinateImmutable.makeStated(logicCoordinate);
        return SINGLETONS.computeIfAbsent(new ManifoldCoordinateImmutable(stampFilter.toStampFilterImmutable(),
                        languageCoordinate.toLanguageCoordinateImmutable(),
                        VertexSortNaturalOrder.SINGLETON,
                        stampFilter.getAllowedStates(), dci,
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
                        VertexSortNaturalOrder.SINGLETON,
                        stampFilter.getAllowedStates(), dci,
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
                this.viewStampFilter.equals(that.viewStampFilter) &&
                this.languageCoordinate.equals(that.languageCoordinate) &&
                this.navigationCoordinateImmutable.equals(that.navigationCoordinateImmutable) &&
                this.activity == that.activity;
    }

    @Override
    public PremiseSet getPremiseTypes() {
        if (this.premiseTypes == null) {
            EnumSet<PremiseType> premiseTypeEnumSet = EnumSet.noneOf(PremiseType.class);
            if (getNavigationCoordinate().getNavigationConceptNids().contains(getLogicCoordinate().getInferredAssemblageNid())) {
                premiseTypeEnumSet.add(PremiseType.INFERRED);
            }
            if (getNavigationCoordinate().getNavigationConceptNids().contains(getLogicCoordinate().getStatedAssemblageNid())) {
                premiseTypeEnumSet.add(PremiseType.STATED);
            }
            this.premiseTypes = PremiseSet.of(premiseTypeEnumSet);
        }
        return this.premiseTypes;
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
    public StatusSet getVertexStatusSet() {
        return this.vertexStampFilter.getAllowedStates();
    }

    @Override
    public StampFilterImmutable getViewStampFilter() {
        return this.viewStampFilter;
    }

    @Override
    public StampFilter getVertexStampFilter() {
        return this.vertexStampFilter;
    }

    @Override
    public String toString() {
        return "ManifoldCoordinateImmutable{" + this.activity.toUserString() + ",\n  " +
                this.getNavigationCoordinate().toUserString() +
                ",\n  sort: " + this.vertexSort.getVertexSortName() +
                ",\n  view filter: " + this.viewStampFilter +
                ", \n vertex filter: " + this.vertexStampFilter +
                ", \n language:" + this.languageCoordinate +
                ", \n logic:" + this.logicCoordinateImmutable +
                ",\n current activity=" + getCurrentActivity() +
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
        return new ManifoldCoordinateImmutable(
                viewStampFilter.makeCoordinateAnalog(classifyTimeInEpochMillis),
                languageCoordinate,
                vertexSort,
                getVertexStatusSet(),
                navigationCoordinateImmutable,
                logicCoordinateImmutable,
                activity,
                editCoordinate);
    }
    
    @Override
    public ManifoldCoordinateImmutable makeCoordinateAnalog(PremiseType premiseType) {
        return new ManifoldCoordinateImmutable(
                viewStampFilter,
                languageCoordinate,
                vertexSort,
                getVertexStatusSet(),
                NavigationCoordinateImmutable.make(premiseType),
                logicCoordinateImmutable,
                activity,
                editCoordinate);
    }
}
