package sh.isaac.api.coordinate;

import sh.isaac.api.Status;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An immutable bitset implementation of a status set.
 */
public class StatusSet implements ImmutableCoordinate {

    private static final ConcurrentReferenceHashMap<StatusSet, StatusSet> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    public static final StatusSet ACTIVE_ONLY = make(Status.ACTIVE);
    public static final StatusSet ACTIVE_AND_INACTIVE = make(Status.ACTIVE, Status.INACTIVE, Status.WITHDRAWN);
    public static final StatusSet INACTIVE = make(Status.INACTIVE, Status.WITHDRAWN);
    public static final StatusSet WITHDRAWN = make(Status.WITHDRAWN);
    public static final StatusSet INACTIVE_ONLY = make(Status.INACTIVE);

    private static final int marshalVersion = 1;

    private long bits = 0;

    private StatusSet(Status... statuses) {
        for (Status status: statuses) {
            bits |= (1L << status.ordinal());
        }
    }

    private StatusSet(Collection<? extends Status> statuses) {
        for (Status status: statuses) {
            bits |= (1L << status.ordinal());
        }
    }


    @Unmarshaler
    public static Object make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                int size = in.getInt();
                List<Status> values = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    values.add(Status.valueOf(in.getUTF()));
                }
                return SINGLETONS.computeIfAbsent(new StatusSet(values), statusSet -> statusSet);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        EnumSet<Status> statusSet = toEnumSet();
        out.putInt(statusSet.size());
        for (Status status: statusSet) {
            out.putUTF(status.name());
        }
    }


    public static StatusSet of(Status... statuses) {
        return make(statuses);
    }

    public static StatusSet make(Status... statuses) {
        return SINGLETONS.computeIfAbsent(new StatusSet(statuses), statusSet -> statusSet);
    }

    public static StatusSet make(Collection<? extends Status> statuses) {
        return SINGLETONS.computeIfAbsent(new StatusSet(statuses), statusSet -> statusSet);
    }

    public static StatusSet of(Collection<? extends Status> statuses) {
        return make(statuses);
    }

    public boolean contains(Status status) {
        return (bits & (1L << status.ordinal())) != 0;
    }

    public Status[] toArray() {
        EnumSet<Status> statusSet = toEnumSet();
        return statusSet.toArray(new Status[statusSet.size()]);
    }
    public EnumSet<Status> toEnumSet() {
        EnumSet<Status> result = EnumSet.noneOf(Status.class);
        for (Status status: Status.values()) {
            if (contains(status)) {
                result.add(status);
            }
        }
        return result;
    }

    public boolean containsAll(Collection<Status> c) {
        for (Status status: c) {
            if (!contains(status)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsAny(Collection<Status> c) {
        for (Status status: c) {
            if (contains(status)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatusSet that = (StatusSet) o;
        return bits == that.bits;
    }

    public boolean isActiveOnly() {
        return (this.bits ^ ACTIVE_ONLY.bits) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bits);
    }

    @Override
    public String toString() {
        return "StatusSet{" +
                toEnumSet() +
                '}';
    }
    public String toUserString() {
        StringBuilder sb = new StringBuilder("[");
        AtomicInteger count = new AtomicInteger();
        addIfPresent(sb, count, Status.ACTIVE);
        addIfPresent(sb, count, Status.CANCELED);
        addIfPresent(sb, count, Status.INACTIVE);
        addIfPresent(sb, count, Status.PRIMORDIAL);
        addIfPresent(sb, count, Status.WITHDRAWN);
        sb.append("]");
        return sb.toString();
    }

    private void addIfPresent(StringBuilder sb, AtomicInteger count, Status status) {
        if (this.contains(status)) {
            if (count.getAndIncrement() > 0) {
                sb.append(", ");
            }
            sb.append(status);
        }
    }
}
