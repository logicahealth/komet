package sh.isaac.api.coordinate;

import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PremiseSet implements ImmutableCoordinate {

    private static final ConcurrentReferenceHashMap<PremiseSet, PremiseSet> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    public static final PremiseSet INFERRED_ONLY = make(PremiseType.INFERRED);
    public static final PremiseSet STATED_ONLY = make(PremiseType.STATED);
    public static final PremiseSet STATED_AND_INFERRED = make(PremiseType.INFERRED, PremiseType.STATED);

    private static final int marshalVersion = 1;

    private int[] flags;
    private long bits = 0;

    private PremiseSet(PremiseType... premises) {
        flags = new int[premises.length];
        for (int i = 0; i < premises.length; i++) {
            PremiseType premise = premises[i];
            bits |= (1L << premise.ordinal());
            flags[i] = TaxonomyFlag.getFlagsFromPremiseType(premise);
        }
    }

    private PremiseSet(Collection<? extends PremiseType> premises) {
        flags = new int[premises.size()];
        Iterator<? extends PremiseType> premiseIterator = premises.iterator();
        for (int i = 0; i < premises.size(); i++) {
            PremiseType premise = premiseIterator.next();
            bits |= (1L << premise.ordinal());
            flags[i] = TaxonomyFlag.getFlagsFromPremiseType(premise);
        }
    }


    @Unmarshaler
    public static Object make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                int size = in.getInt();
                List<PremiseType> values = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    values.add(PremiseType.valueOf(in.getUTF()));
                }
                return SINGLETONS.computeIfAbsent(new PremiseSet(values), statusSet -> statusSet);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        EnumSet<PremiseType> premiseSet = toEnumSet();
        out.putInt(premiseSet.size());
        for (PremiseType premise: premiseSet) {
            out.putUTF(premise.name());
        }
    }

    public int[] getFlags() {
        return flags;
    }

    public static PremiseSet of(PremiseType... premises) {
        return make(premises);
    }

    public static PremiseSet make(PremiseType... premises) {
        return SINGLETONS.computeIfAbsent(new PremiseSet(premises), premiseSet -> premiseSet);
    }

    public static PremiseSet make(Collection<? extends PremiseType> premises) {
        return SINGLETONS.computeIfAbsent(new PremiseSet(premises), premiseSet -> premiseSet);
    }

    public static PremiseSet of(Collection<? extends PremiseType> premises) {
        return make(premises);
    }

    public boolean contains(PremiseType status) {
        return (bits & (1L << status.ordinal())) != 0;
    }

    public PremiseType[] toArray() {
        EnumSet<PremiseType> statusSet = toEnumSet();
        return statusSet.toArray(new PremiseType[statusSet.size()]);
    }
    public EnumSet<PremiseType> toEnumSet() {
        EnumSet<PremiseType> result = EnumSet.noneOf(PremiseType.class);
        for (PremiseType premise: PremiseType.values()) {
            if (contains(premise)) {
                result.add(premise);
            }
        }
        return result;
    }

    public boolean containsAll(Collection<PremiseType> c) {
        for (PremiseType premise: c) {
            if (!contains(premise)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsAny(Collection<PremiseType> c) {
        for (PremiseType premise: c) {
            if (contains(premise)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PremiseSet that = (PremiseSet) o;
        return bits == that.bits;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bits);
    }

    @Override
    public String toString() {
        return "PremiseSet{" +
                toEnumSet() +
                '}';
    }
    public String toUserString() {
        StringBuilder sb = new StringBuilder();
        AtomicInteger count = new AtomicInteger();
        addIfPresent(sb, count, PremiseType.INFERRED);
        addIfPresent(sb, count, PremiseType.STATED);
        return sb.toString();
    }

    private void addIfPresent(StringBuilder sb, AtomicInteger count, PremiseType premise) {
        if (this.contains(premise)) {
            if (count.getAndIncrement() > 0) {
                sb.append(" and ");
            }
            sb.append(premise);
        }
    }
}