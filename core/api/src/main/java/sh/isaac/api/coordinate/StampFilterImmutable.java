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



package sh.isaac.api.coordinate;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import javax.annotation.PreDestroy;

//~--- JDK imports ------------------------------------------------------------

import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.LookupService;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.MarshalUtil;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;

//~--- non-JDK imports --------------------------------------------------------

//~--- interfaces -------------------------------------------------------------

/**
 * A filter that operates in coordinate with path coordinate and the version computer. After the version computer computes the
 * latest versions at a point on a path, the filter provides a non-interfering, stateless predicate to apply to each element
 * to determine if it should be included in the results set.
 * <p\>
 * Filters must be immutable.
 * <p\>
 * q: How does the stamp coordinate relate to the stamp sequence?
 * <p\>
 a: A stamp sequence is a sequentially assigned identifier for a unique combination of Status, Time, Author, Module, and Path...
 A stamp coordinate specifies a position on a  path, with a particular set of modules, and allowed state values. Author constraints
 are not handled by the stamp filter. If necessary, results can be post-processed.

 <p\>
 * Created by kec on 2/16/15.
 *
 */
@Service
@RunLevel(value = LookupService.SL_L2)
// Singleton from the perspective of HK2 managed instances, there will be more than one
// StampFilterImmutable created in normal use.
public class StampFilterImmutable
        implements StampFilter, ImmutableCoordinate {

    private static final ConcurrentReferenceHashMap<StampFilterImmutable, StampFilterImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                                             ConcurrentReferenceHashMap.ReferenceType.WEAK);
    private static final int marshalVersion = 1;

    private final StatusSet allowedStates;
    private final StampPosition stampPosition;
    private final ImmutableIntSet moduleNids;
    private final ImmutableIntSet excludedModuleNids;
    private final ImmutableIntList modulePreferenceOrder;

    private transient RelativePositionCalculator relativePositionCalculator;

    private StampFilterImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.allowedStates = null;
        this.stampPosition = null;
        this.moduleNids = null;
        this.excludedModuleNids = null;
        this.modulePreferenceOrder = null;
    }

    /**
     * {@inheritDoc}
     */
    @PreDestroy
    public void reset() {
        SINGLETONS.clear();
    }

    private StampFilterImmutable(StatusSet allowedStates,
                                StampPosition stampPosition,
                                 ImmutableIntSet moduleNids,
                                 ImmutableIntSet excludedModuleNids,
                                ImmutableIntList modulePreferenceOrder) {
        this.allowedStates = allowedStates;
        this.stampPosition = stampPosition;
        this.moduleNids = moduleNids;
        this.excludedModuleNids = excludedModuleNids;
        this.modulePreferenceOrder = modulePreferenceOrder;
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        MarshalUtil.marshal(this.allowedStates, out);
        MarshalUtil.marshal(this.stampPosition, out);
        out.putNidArray(moduleNids.toArray());
        out.putNidArray(excludedModuleNids.toArray());
        out.putNidArray(modulePreferenceOrder.toArray());
    }

    @Unmarshaler
    public static StampFilterImmutable make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return SINGLETONS.computeIfAbsent(new StampFilterImmutable(MarshalUtil.unmarshal(in),
                        MarshalUtil.unmarshal(in),
                        IntSets.immutable.of(in.getNidArray()),
                        IntSets.immutable.of(in.getNidArray()),
                        IntLists.immutable.of(in.getNidArray())),
                        stampFilterImmutable -> stampFilterImmutable);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }
    public static StampFilterImmutable make(StatusSet allowedStates,
                                            StampPosition stampPosition,
                                            ImmutableIntSet moduleNids,
                                            ImmutableIntSet excludedModuleNids,
                                            ImmutableIntList modulePreferenceOrder) {
        return SINGLETONS.computeIfAbsent(new StampFilterImmutable(allowedStates, stampPosition,
                moduleNids, excludedModuleNids, modulePreferenceOrder), stampFilterImmutable -> stampFilterImmutable);
    }

    public static StampFilterImmutable make(StatusSet allowedStates,
                                            StampPosition stampPosition,
                                            ImmutableIntSet moduleNids,
                                            ImmutableIntList modulePreferenceOrder) {
        return SINGLETONS.computeIfAbsent(new StampFilterImmutable(allowedStates, stampPosition,
                moduleNids, IntSets.immutable.empty(), modulePreferenceOrder), stampFilterImmutable -> stampFilterImmutable);
    }


    public static StampFilterImmutable make(StatusSet allowedStates,
                         StampPosition stampPosition,
                         ImmutableIntSet moduleNids) {
        return SINGLETONS.computeIfAbsent(new StampFilterImmutable(allowedStates, stampPosition,
                moduleNids, IntSets.immutable.empty(), IntLists.immutable.empty()), stampFilterImmutable -> stampFilterImmutable);
    }

    public static StampFilterImmutable make(StatusSet allowedStates, int path,
                                            Set<ConceptSpecification> modules) {
        ImmutableIntSet moduleNids = IntSets.immutable.of(modules.stream().mapToInt(value -> value.getNid()).toArray());
        StampPositionImmutable stampPosition = StampPositionImmutable.make(Long.MAX_VALUE, path);

        return SINGLETONS.computeIfAbsent(new StampFilterImmutable(allowedStates,
                stampPosition, moduleNids, IntSets.immutable.empty(), IntLists.immutable.empty()), stampFilterImmutable -> stampFilterImmutable);
    }

    public static StampFilterImmutable make(StatusSet allowedStates, int path) {
        StampPositionImmutable stampPosition = StampPositionImmutable.make(Long.MAX_VALUE, path);

        return SINGLETONS.computeIfAbsent(new StampFilterImmutable(allowedStates,
                stampPosition,
                IntSets.immutable.empty(),
                IntSets.immutable.empty(),
                IntLists.immutable.empty()), stampFilterImmutable -> stampFilterImmutable);
    }

    public static StampFilterImmutable make(StatusSet allowedStates, StampPosition stampPosition) {
        return SINGLETONS.computeIfAbsent(new StampFilterImmutable(allowedStates,
                stampPosition,
                IntSets.immutable.empty(),
                IntSets.immutable.empty(),
                IntLists.immutable.empty()), stampFilterImmutable -> stampFilterImmutable);
    }

    @Override
    public StatusSet getAllowedStates() {
        return this.allowedStates;
    }

   @Override
   public StampPosition getStampPosition() {
       return this.stampPosition;
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
        return "StampFilterImmutable{" + toUserString() + "}";
    }

    public RelativePositionCalculator getRelativePositionCalculator() {
        if (this.relativePositionCalculator == null) {
            this.relativePositionCalculator = RelativePositionCalculator.getCalculator(this);
        }
        return this.relativePositionCalculator;
    }

    @Override
    public StampFilterImmutable makeCoordinateAnalog(StatusSet statusSet) {
        return make(statusSet,
                this.stampPosition,
                this.moduleNids,
                this.modulePreferenceOrder);
    }

    @Override
    public StampFilterImmutable makeCoordinateAnalog(long stampPositionTime) {
        return make(this.allowedStates,
                StampPositionImmutable.make(stampPositionTime, this.stampPosition.getPathForPositionNid()),
                this.moduleNids, this.modulePreferenceOrder);
    }

    public StampFilterImmutable toStampFilterImmutable() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StampFilterImmutable)) return false;
        StampFilterImmutable that = (StampFilterImmutable) o;
        return getAllowedStates().equals(that.getAllowedStates()) &&
                getStampPosition().equals(that.getStampPosition()) &&
                getModuleNids().equals(that.getModuleNids()) &&
                getExcludedModuleNids().equals(that.getExcludedModuleNids()) &&
                getModulePriorityOrder().equals(that.getModulePriorityOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAllowedStates(),
                getStampPosition(),
                getModuleNids(),
                getExcludedModuleNids(),
                getModulePriorityOrder());
    }

    @Override
    public int getPathNidForFilter() {
        return this.stampPosition.getPathForPositionNid();
    }


    @Override
    public StampFilterImmutable makeModuleAnalog(Collection<ConceptSpecification> modules, boolean add) {
        MutableIntSet mis = IntSets.mutable.ofAll(modules.stream().mapToInt(conceptSpecification -> conceptSpecification.getNid()));
        if (add) {
            mis.addAll(this.moduleNids);
        }
        return make(this.allowedStates,
                this.stampPosition,
                mis.toImmutable(), this.excludedModuleNids, IntLists.immutable.empty());
    }

    @Override
    public StampFilterImmutable makePathAnalog(ConceptSpecification pathForPosition) {
        return make(this.allowedStates,
                StampPositionImmutable.make(this.stampPosition.getTime(), pathForPosition.getNid()),
                this.moduleNids, this.excludedModuleNids, this.modulePreferenceOrder);
    }

    @Override
    public StampFilterTemplateImmutable toStampFilterTemplateImmutable() {
        return StampFilterTemplateImmutable.make(this.allowedStates, this.moduleNids, this.excludedModuleNids, this.modulePreferenceOrder);
    }
}

