package sh.isaac.api.coordinate;

import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.MarshalUtil;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

import javax.annotation.PreDestroy;
import java.util.Objects;
import java.util.Set;

public class StampFilterTemplateImmutable  implements StampFilterTemplate, ImmutableCoordinate {

    private static final ConcurrentReferenceHashMap<StampFilterTemplateImmutable, StampFilterTemplateImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);
    private static final int marshalVersion = 1;

    private final StatusSet allowedStates;
    private final ImmutableIntSet moduleNids;
    private final ImmutableIntSet excludedModuleNids;
    private final ImmutableIntList modulePreferenceOrder;

    protected StampFilterTemplateImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.allowedStates = null;
        this.moduleNids = null;
        this.modulePreferenceOrder = null;
        this.excludedModuleNids = null;
    }

    /**
     * {@inheritDoc}
     */
    @PreDestroy
    public void reset() {
        SINGLETONS.clear();
    }

    private StampFilterTemplateImmutable(StatusSet allowedStates,
                                         ImmutableIntSet moduleNids,
                                         ImmutableIntSet excludedModuleNids,
                                 ImmutableIntList modulePreferenceOrder) {
        this.allowedStates = allowedStates;
        this.moduleNids = moduleNids;
        this.excludedModuleNids = excludedModuleNids;
        this.modulePreferenceOrder = modulePreferenceOrder;
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        MarshalUtil.marshal(this.allowedStates, out);
        out.putNidArray(moduleNids.toArray());
        out.putNidArray(excludedModuleNids.toArray());
        out.putNidArray(modulePreferenceOrder.toArray());
    }

    @Unmarshaler
    public static StampFilterTemplateImmutable make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return SINGLETONS.computeIfAbsent(new StampFilterTemplateImmutable(MarshalUtil.unmarshal(in),
                                IntSets.immutable.of(in.getNidArray()),
                                IntSets.immutable.of(in.getNidArray()),
                                IntLists.immutable.of(in.getNidArray())),
                        stampFilterImmutable -> stampFilterImmutable);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    public static StampFilterTemplateImmutable make(StatusSet allowedStates,
                                                    ImmutableIntSet moduleNids,
                                                    ImmutableIntSet excludedModuleNids,
                                                    ImmutableIntList modulePreferenceOrder) {
        return SINGLETONS.computeIfAbsent(new StampFilterTemplateImmutable(allowedStates,
                moduleNids, excludedModuleNids, modulePreferenceOrder), stampFilterImmutable -> stampFilterImmutable);
    }


    public static StampFilterTemplateImmutable make(StatusSet allowedStates,
                                            ImmutableIntSet moduleNids,
                                            ImmutableIntList modulePreferenceOrder) {
        return SINGLETONS.computeIfAbsent(new StampFilterTemplateImmutable(allowedStates,
                moduleNids, IntSets.immutable.empty(), modulePreferenceOrder), stampFilterImmutable -> stampFilterImmutable);
    }


    public static StampFilterTemplateImmutable make(StatusSet allowedStates,
                                            ImmutableIntSet moduleNids) {
        return SINGLETONS.computeIfAbsent(new StampFilterTemplateImmutable(allowedStates,
                moduleNids, IntSets.immutable.empty(), IntLists.immutable.empty()), stampFilterImmutable -> stampFilterImmutable);
    }

    public static StampFilterTemplateImmutable make(StatusSet allowedStates,
                                            Set<ConceptSpecification> modules) {
        ImmutableIntSet moduleNids = IntSets.immutable.of(modules.stream().mapToInt(value -> value.getNid()).toArray());

        return SINGLETONS.computeIfAbsent(new StampFilterTemplateImmutable(allowedStates,
                moduleNids, IntSets.immutable.empty(), IntLists.immutable.empty()), stampFilterImmutable -> stampFilterImmutable);
    }

    public static StampFilterTemplateImmutable make(StatusSet allowedStates) {

        return SINGLETONS.computeIfAbsent(new StampFilterTemplateImmutable(allowedStates,
                IntSets.immutable.empty(),
                IntSets.immutable.empty(),
                IntLists.immutable.empty()), stampFilterImmutable -> stampFilterImmutable);
    }

    @Override
    public StatusSet getAllowedStates() {
        return this.allowedStates;
    }

    @Override
    public ImmutableIntSet getModuleNids() {
        return this.moduleNids;
    }

    @Override
    public ImmutableIntSet getExcludedModuleNids() {
        return this.excludedModuleNids;
    }

    @Override
    public ImmutableIntList getModulePriorityOrder() {
        return this.modulePreferenceOrder;
    }

    @Override
    public String toString() {
        return "StampFilterTemplateImmutable{" + toUserString() + "}";
    }


    public StampFilterTemplateImmutable toStampFilterTemplateImmutable() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StampFilterTemplate)) return false;
        StampFilterTemplate that = (StampFilterTemplate) o;
        return getAllowedStates().equals(that.getAllowedStates()) &&
                getModuleNids().equals(that.getModuleNids()) &&
                getExcludedModuleNids().equals(that.getExcludedModuleNids()) &&
                getModulePriorityOrder().equals(that.getModulePriorityOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAllowedStates(),
                getModuleNids(),
                getExcludedModuleNids(),
                getModulePriorityOrder());
    }
}